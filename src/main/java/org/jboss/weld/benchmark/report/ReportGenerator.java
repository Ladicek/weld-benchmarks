/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.benchmark.report;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.trimou.Mustache;
import org.trimou.engine.MustacheEngine;
import org.trimou.engine.MustacheEngineBuilder;
import org.trimou.engine.config.EngineConfigurationKey;
import org.trimou.engine.interpolation.BracketDotKeySplitter;
import org.trimou.engine.locator.ClassPathTemplateLocator;
import org.trimou.handlebars.EvalHelper;
import org.trimou.handlebars.EvalHelper.BracketDotNotation;
import org.trimou.handlebars.HelpersBuilder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 *
 * @author Martin Kouba
 */
public class ReportGenerator {

    public static void main(String[] args) throws IOException {

        if (args.length < 1) {
            throw new IllegalStateException("First argument - list of versions, second argument - output path (optional)");
        }

        List<File> files = new ArrayList<File>();
        for (String resultFilePath : args[0].split(" ")) {
            if (resultFilePath.trim().equals("")) {
                continue;
            }
            File file = new File(resultFilePath);
            if (!file.canRead()) {
                throw new IllegalArgumentException("Unable to read the results file: " + file);
            }
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                Collections.addAll(files, file.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isFile() && !pathname.isHidden() && pathname.getName().endsWith(".json");
                    }
                }));
            }
        }

        if (files.isEmpty()) {
            throw new IllegalStateException("No results to process!");
        }
        if (files.size() == 1) {
            // benchmark was run for a singular version, no need to generate comparison
            System.out.println("Report generation skipped since only one version of Weld was specified.");
            return;
        }
        File outputPath = args.length < 2 ? new File(System.getProperty("user.dir")) : new File(args[1]);
        if (!outputPath.exists()) {
            outputPath.mkdirs();
        }

        // Parse results
        Set<String> allBenchmarks = new LinkedHashSet<String>();
        Map<String, Map<String, JsonObject>> versionToResults = new LinkedHashMap<String, Map<String, JsonObject>>();
        for (File file : files) {
            Map<String, JsonObject> benchmarkToSeries = new HashMap<String, JsonObject>();
            JsonArray series = readJsonElementFromFile(file).getAsJsonArray();
            for (JsonElement jsonElement : series) {
                JsonObject benchmark = jsonElement.getAsJsonObject();
                String benchmarkName = benchmark.get("benchmark").getAsString();
                benchmarkName = benchmarkName.substring(0, benchmarkName.lastIndexOf('.'));
                allBenchmarks.add(benchmarkName);
                benchmarkToSeries.put(benchmarkName, benchmark);
            }
            versionToResults.put(file.getName().replace("results-", "").replace(".json", ""), benchmarkToSeries);
        }

        // Group benchmarks by package
        Map<String, List<String>> packageToBenchmarkNames = allBenchmarks.stream().collect(Collectors.groupingBy(b -> b.substring(0, b.lastIndexOf('.'))));

        // Report data
        List<String> charts = new ArrayList<>();
        Map<String, Map<String, Score>> versionToBenchmarkToScore = new HashMap<>();

        for (Entry<String, List<String>> entry : packageToBenchmarkNames.entrySet()) {
            // Generate chart for each package
            CategoryChart chart = new CategoryChartBuilder().width(1280).height(1024).title(entry.getKey()).xAxisTitle("Benchmarks").yAxisTitle("Results")
                    .build();
            chart.getStyler().setXAxisTicksVisible(true);
            chart.getStyler().setXAxisLabelRotation(45);

            List<String> benchmarkNames = entry.getValue();
            benchmarkNames.sort(null);

            for (Entry<String, Map<String, JsonObject>> series : versionToResults.entrySet()) {

                List<String> benchmarks = new ArrayList<>();
                List<BigDecimal> scores = new ArrayList<>();
                List<BigDecimal> errors = new ArrayList<>();
                Map<String, Score> benchmarkToScore = versionToBenchmarkToScore.computeIfAbsent(series.getKey(), k -> new HashMap<>());
                versionToBenchmarkToScore.put(series.getKey(), benchmarkToScore);

                for (String benchmarkName : benchmarkNames) {
                    benchmarks.add(abbreviateBenchmarkName(benchmarkName));
                    JsonObject benchmark = series.getValue().get(benchmarkName);
                    if (benchmark != null) {
                        BigDecimal score = benchmark.get("primaryMetric").getAsJsonObject().get("score").getAsBigDecimal();
                        BigDecimal error = benchmark.get("primaryMetric").getAsJsonObject().get("scoreError").getAsBigDecimal();
                        benchmarkToScore.put(benchmarkName, new Score(score.setScale(3, RoundingMode.HALF_UP), error.setScale(3, RoundingMode.HALF_UP)));
                        scores.add(score);
                        errors.add(error);
                    } else {
                        scores.add(BigDecimal.ZERO);
                        errors.add(BigDecimal.ZERO);
                        benchmarkToScore.put(benchmarkName, new Score(BigDecimal.ZERO, BigDecimal.ZERO));
                    }
                }

                chart.addSeries(series.getKey(), benchmarks, scores, errors);
            }
            // Save as png
            File chartFile = new File(outputPath, entry.getKey() + ".png");
            try (FileOutputStream out = new FileOutputStream(chartFile)) {
                BitmapEncoder.saveBitmap(chart, out, BitmapFormat.PNG);
            }
            charts.add(chartFile.getName());
        }

        MustacheEngine engine = MustacheEngineBuilder.newBuilder().setKeySplitter(new BracketDotKeySplitter())
                .setProperty(EngineConfigurationKey.PRECOMPILE_ALL_TEMPLATES, false)
                .addTemplateLocator(ClassPathTemplateLocator.builder(1).setRootPath("templates").build())
                .registerHelper(HelpersBuilder.EVAL, new EvalHelper(new BracketDotNotation()))
                .registerHelpers(HelpersBuilder.empty().addInvoke().build()).build();
        Mustache reportTemplate = engine.getMustache("report.html");
        if (reportTemplate == null) {
            throw new IllegalStateException("Report template not found!");
        }

        // Prepare report data
        for (String benchmarkName : allBenchmarks) {
            List<Score> scores = versionToBenchmarkToScore.values().stream().filter(m -> m.containsKey(benchmarkName)).map(m -> m.get(benchmarkName))
                    .collect(Collectors.toList());
            scores.sort((s1, s2) -> s2.value.compareTo(s1.value));
            // Highest score has 40% luminosity and lowest 95%
            int lower = 40;
            int upper = 95;
            int step = (upper - lower) / (scores.size() - 1);
            int luminosity = lower;
            for (Score score : scores) {
                score.color = String.format("hsl(140, 100%%, %s%%)", luminosity);
                luminosity += step;
            }
        }
        List<String> versions = new ArrayList<>(versionToBenchmarkToScore.keySet());
        charts.sort(null);

        // Generate html report
        File reportFile = new File(outputPath, "report.html");
        try (Writer writer = Files.newBufferedWriter(reportFile.toPath(), Charset.forName("UTF-8"))) {
            Map<String, Object> data = new HashMap<>();
            data.put("timestamp", LocalDateTime.now());
            data.put("versionToBenchmarkToScore", versionToBenchmarkToScore);
            data.put("benchmarks", allBenchmarks);
            data.put("versions", versions);
            data.put("charts", charts);
            reportTemplate.render(writer, data);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create the report file: " + reportFile, e);
        }
    }

    static JsonElement readJsonElementFromFile(File inputFile) throws IOException {
        try (Reader reader = Files.newBufferedReader(inputFile.toPath(), Charset.forName("UTF-8"))) {
            JsonParser jsonParser = new JsonParser();
            return jsonParser.parse(reader);
        }
    }

    static String abbreviateBenchmarkName(String benchmark) {
        String[] parts = benchmark.split("\\.");
        return parts[parts.length - 1];
    }

    public static class Score {

        public BigDecimal value;

        public BigDecimal error;

        public String color;

        public Score(BigDecimal value, BigDecimal error) {
            this.value = value;
            this.error = error;
        }

    }

}
