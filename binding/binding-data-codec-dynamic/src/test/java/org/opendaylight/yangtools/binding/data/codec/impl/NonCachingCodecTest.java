/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeCodec;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

@ExtendWith(MockitoExtension.class)
class NonCachingCodecTest {
    @Mock
    private BindingNormalizedNodeCodec<DataObject> codec;
    @Mock
    private ContainerNode node;
    @Mock
    private DataObject object;

    @BeforeEach
    void beforeEach() {
        doReturn(node).when(codec).serialize(object);
        doReturn(object).when(codec).deserialize(node);
    }

    @Test
    void basicTest() {
        try (var nonCachingCodec = new NonCachingCodec<>(codec)) {
            nonCachingCodec.serialize(object);
            verify(codec).serialize(object);
            nonCachingCodec.deserialize(node);
            verify(codec).deserialize(node);
        }
    }
}
