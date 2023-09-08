package org.jboss.weld.benchmark.core.invokableMethods;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.invoke.Invokable;
import org.openjdk.jmh.infra.Blackhole;

@Dependent
public class InvokableBean {

    @Invokable
    public String ping(String s, Boolean b) {
        Blackhole.consumeCPU(20);
        return "InvokableBean";
    }
}
