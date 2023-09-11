package org.jboss.weld.benchmark.core.invokableMethods;

public class InvokableMethodBenchmarkSimpleBeanInvocation extends InvokableMethodBenchmarkBase {
    @Override
    protected String performInvoke() {
        return bean.ping(dummyStringArg, Boolean.TRUE);
    }
}
