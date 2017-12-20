/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.benchmark.core.event.beanManager;

import java.lang.annotation.Annotation;

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.weld.benchmark.core.BeanUnderTest;
import org.jboss.weld.benchmark.core.DummyEvent;

public abstract class AbstractDispatcher implements BeanUnderTest {

    private static final DummyEvent EVENT = new DummyEvent(true);
    private final BeanManager manager;
    private final Annotation qualifier;

    AbstractDispatcher(BeanManager manager, Annotation qualifier) {
        this.manager = manager;
        this.qualifier = qualifier;
    }

    public boolean getResult() {
        manager.fireEvent(EVENT, qualifier);
        return true;
    }
}