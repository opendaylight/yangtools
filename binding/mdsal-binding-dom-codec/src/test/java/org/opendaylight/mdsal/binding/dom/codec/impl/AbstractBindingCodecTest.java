/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class AbstractBindingCodecTest extends AbstractBindingRuntimeTest {
    protected BindingNormalizedNodeCodecRegistry registry;

    @BeforeClass
    public static void beforeClass() {
        AbstractBindingRuntimeTest.beforeClass();
    }

    @AfterClass
    public static void afterClass() {
        AbstractBindingRuntimeTest.afterClass();
    }

    @Before
    public void before() {
        this.registry = new BindingNormalizedNodeCodecRegistry(getRuntimeContext());
    }
}
