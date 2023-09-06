package org.jboss.weld.benchmark.core.invokableMethods;

public class InvokableMethodBenchmarkLookup extends InvokableMethodBenchmarkBase {

    @Override
    protected void performInvoke() {
        lookupAllInvoker.invoke(bean, dummyMethodArgs);
    }
}
