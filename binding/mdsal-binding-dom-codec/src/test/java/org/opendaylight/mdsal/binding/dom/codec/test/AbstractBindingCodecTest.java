/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.test;

import javassist.ClassPool;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.opendaylight.mdsal.binding.dom.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.generator.util.JavassistUtils;

public abstract class AbstractBindingCodecTest extends AbstractBindingRuntimeTest {
    private static JavassistUtils UTILS;

    protected BindingNormalizedNodeCodecRegistry registry;

    @BeforeClass
    public static void beforeClass() {
        AbstractBindingRuntimeTest.beforeClass();
        UTILS = JavassistUtils.forClassPool(ClassPool.getDefault());
    }

    @AfterClass
    public static void afterClass() {
        UTILS = null;
        AbstractBindingRuntimeTest.afterClass();
    }

    @Before
    public void before() {
        this.registry = new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(UTILS));
        this.registry.onBindingRuntimeContextUpdated(getRuntimeContext());
    }
}
