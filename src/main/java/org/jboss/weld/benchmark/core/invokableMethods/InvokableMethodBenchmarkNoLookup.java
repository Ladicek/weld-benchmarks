package org.jboss.weld.benchmark.core.invokableMethods;

public class InvokableMethodBenchmarkNoLookup extends InvokableMethodBenchmarkBase {

    @Override
    protected String performInvoke() {
        return invoker.invoke(bean, dummyMethodArgs);
    }
}
