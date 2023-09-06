package org.jboss.weld.benchmark.core.invokableMethods;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessManagedBean;
import jakarta.enterprise.invoke.Invoker;

import java.util.Collection;

public class PortableExtension implements Extension {

    private Invoker<InvokableBean, ?> invoker;
    private Invoker<InvokableBean, ?> lookupAllInvoker;

    public void observe(@Observes ProcessManagedBean<InvokableBean> pmb) {
        Collection<AnnotatedMethod<? super InvokableBean>> invokableMethods = pmb.getInvokableMethods();
        if (invokableMethods.size() != 1) {
            throw new IllegalStateException("InvokableBean should have only one method annotated @Invokable");
        }
        invoker = pmb.createInvoker(invokableMethods.iterator().next()).build();
        lookupAllInvoker = pmb.createInvoker(invokableMethods.iterator().next()).setInstanceLookup().setArgumentLookup(0).setArgumentLookup(1).build();
    }

    public Invoker<InvokableBean, ?> getInvoker() {
        return invoker;
    }

    public Invoker<InvokableBean, ?> getLookupAllInvokerInvoker() {
        return lookupAllInvoker;
    }
}
