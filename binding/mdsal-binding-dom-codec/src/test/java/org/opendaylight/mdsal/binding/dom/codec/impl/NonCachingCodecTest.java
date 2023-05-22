/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeCodec;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class NonCachingCodecTest {
    @Mock
    public BindingNormalizedNodeCodec<DataObject> codec;
    @Mock
    public NormalizedNode node;
    @Mock
    public DataObject object;

    @Before
    public void before() {
        doReturn(node).when(codec).serialize(object);
        doReturn(object).when(codec).deserialize(node);
    }

    @Test
    public void basicTest() {
        try (var nonCachingCodec = new NonCachingCodec<>(codec)) {
            nonCachingCodec.serialize(object);
            verify(codec).serialize(object);
            nonCachingCodec.deserialize(node);
            verify(codec).deserialize(node);
        }
    }
}
