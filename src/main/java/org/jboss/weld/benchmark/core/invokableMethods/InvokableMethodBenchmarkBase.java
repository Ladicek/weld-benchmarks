package org.jboss.weld.benchmark.core.invokableMethods;

import static org.jboss.weld.benchmark.core.Configuration.BATCH_SIZE_NORMAL;
import static org.jboss.weld.benchmark.core.Configuration.FORKS;

import jakarta.enterprise.invoke.Invoker;
import jakarta.enterprise.util.AnnotationLiteral;
import org.jboss.weld.benchmark.core.Configuration;
import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.unbound.Unbound;
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
@State(Scope.Thread)
public abstract class   InvokableMethodBenchmarkBase {

    private RequestContext requestContext;

    protected InvokableBean bean;
    protected PortableExtension extension;
    protected Invoker<InvokableBean, String> invoker;
    protected Invoker<InvokableBean, String> lookupAllInvoker;

    protected String dummyStringArg = "foo";
    protected Object[] dummyMethodArgs = new Object[]{dummyStringArg, Boolean.TRUE};

    @State(Scope.Benchmark)
    public static class ContainerState {

        private Weld weld;

        private WeldContainer container;

        @Setup
        public void setup() {
            weld = new Weld();
            weld.addExtensions(PortableExtension.class);
            container = weld.initialize();
        }

        @TearDown
        public void tearDown() {
            weld.shutdown();
        }

        public WeldContainer getContainer() {
            return container;
        }

    }

    @Setup
    public void setup(ContainerState containerState) {
        bean = containerState.getContainer().select(InvokableBean.class).get();
        extension = containerState.getContainer().select(PortableExtension.class).get();
        invoker = (Invoker<InvokableBean, String>) extension.getInvoker();
        lookupAllInvoker = (Invoker<InvokableBean, String>) extension.getLookupAllInvokerInvoker();
        requestContext = containerState.getContainer().select(RequestContext.class, new AnnotationLiteral<Unbound>() {
        }).get();
        requestContext.activate();
    }

    @TearDown
    public void tearDown() {
        requestContext.invalidate();
        requestContext.deactivate();
    }

    @Benchmark
    public String run() {
        // return result of benchmark to avoid dead code elimination
        return performInvoke();
    }

    protected abstract String performInvoke();
}
