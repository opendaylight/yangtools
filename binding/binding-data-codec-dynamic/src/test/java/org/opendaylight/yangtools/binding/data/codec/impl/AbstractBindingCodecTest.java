/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public abstract class AbstractBindingCodecTest extends AbstractBindingRuntimeTest {
    protected BindingCodecContext codecContext;

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
        codecContext = new BindingCodecContext(getRuntimeContext());
    }

    @SuppressWarnings("unchecked")
    protected <T extends DataObject> T thereAndBackAgain(final InstanceIdentifier<T> path, final T data) {
        final var there = codecContext.toNormalizedDataObject(path, data);
        final var backAgain = codecContext.fromNormalizedNode(there.path(), there.node());
        assertEquals(path, backAgain.getKey());
        return (T) backAgain.getValue();
    }
}
