package org.jboss.weld.benchmark.core.invokableMethods;

import static org.jboss.weld.benchmark.core.Configuration.BATCH_SIZE_NORMAL;
import static org.jboss.weld.benchmark.core.Configuration.FORKS;

import jakarta.enterprise.invoke.Invoker;
import org.jboss.weld.benchmark.core.Configuration;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

@Fork(FORKS)
@BenchmarkMode(Mode.Throughput)
@Warmup(batchSize = BATCH_SIZE_NORMAL, iterations = Configuration.ITERATIONS, time = 5)
@Measurement(batchSize = BATCH_SIZE_NORMAL, iterations = Configuration.ITERATIONS, time = 5)
@State(Scope.Benchmark)
public class InvokableMethodBenchmark {

    Weld weld;

    InvokableBean bean;

    Invoker<InvokableBean, String> invoker;
    Invoker<InvokableBean, String> lookupAllInvoker;

    @Setup
    public void setup() {
        weld = new Weld();
        weld.addExtensions(PortableExtension.class);
        WeldContainer container = weld.initialize();
        PortableExtension extension = container.select(PortableExtension.class).get();

        bean = container.select(InvokableBean.class).get();

        invoker = (Invoker<InvokableBean, String>) extension.getInvoker();
        lookupAllInvoker = (Invoker<InvokableBean, String>) extension.getLookupAllInvokerInvoker();
    }

    @TearDown
    public void tearDown() {
        weld.shutdown();
    }

    @Benchmark
    public String directInvocation() {
        return bean.ping("foo", Boolean.TRUE);
    }

    @Benchmark
    public String invocationThroughInvoker() {
        return invoker.invoke(bean, new Object[]{"foo", Boolean.TRUE});
    }

    @Benchmark
    public String invocationThroughInvokerWithLookups() {
        return lookupAllInvoker.invoke(bean, new Object[]{null, null});
    }
}
