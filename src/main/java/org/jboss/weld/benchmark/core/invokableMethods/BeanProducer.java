package org.jboss.weld.benchmark.core.invokableMethods;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class BeanProducer {

    @Produces
    String createString() {
        return "foo";
    }

    @Produces
    Boolean createBoolean() {
        return Boolean.TRUE;
    }
}
