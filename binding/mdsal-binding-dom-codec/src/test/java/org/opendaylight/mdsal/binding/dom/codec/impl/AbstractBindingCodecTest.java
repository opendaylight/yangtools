/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.junit.Assert.assertEquals;

import java.util.Map.Entry;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

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
        this.codecContext = new BindingCodecContext(getRuntimeContext());
    }

    @SuppressWarnings("unchecked")
    protected <T extends DataObject> T thereAndBackAgain(final InstanceIdentifier<T> path, final T data) {
        final Entry<YangInstanceIdentifier, NormalizedNode> there = codecContext.toNormalizedNode(path, data);
        final Entry<InstanceIdentifier<?>, DataObject> backAgain = codecContext.fromNormalizedNode(there.getKey(),
            there.getValue());
        assertEquals(path, backAgain.getKey());
        return (T) backAgain.getValue();
    }
}
