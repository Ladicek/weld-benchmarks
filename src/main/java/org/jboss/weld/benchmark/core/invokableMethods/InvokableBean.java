package org.jboss.weld.benchmark.core.invokableMethods;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.invoke.Invokable;

@RequestScoped
public class InvokableBean {

    @Invokable
    public String ping(String s, Boolean b) {
        return InvokableBean.class.getSimpleName();
    }
}
