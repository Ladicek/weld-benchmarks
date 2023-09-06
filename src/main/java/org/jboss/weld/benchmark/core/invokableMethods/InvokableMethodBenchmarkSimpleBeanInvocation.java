package org.jboss.weld.benchmark.core.invokableMethods;

public class InvokableMethodBenchmarkSimpleBeanInvocation extends InvokableMethodBenchmarkBase {
    @Override
    protected void performInvoke() {
        bean.ping(dummyStringArg, Boolean.TRUE);
    }
}
