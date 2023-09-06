package org.jboss.weld.benchmark.core.invokableMethods;

public class InvokableMethodBenchmarkNoLookup extends InvokableMethodBenchmarkBase {

    @Override
    protected void performInvoke() {
        invoker.invoke(bean, dummyMethodArgs);
    }
}
