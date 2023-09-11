package org.jboss.weld.benchmark.core.invokableMethods;

public class InvokableMethodBenchmarkLookup extends InvokableMethodBenchmarkBase {

    @Override
    protected String performInvoke() {
        return lookupAllInvoker.invoke(bean, dummyMethodArgs);
    }
}
