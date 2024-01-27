/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

@ExtendWith(MockitoExtension.class)
class NormalizedNodeDataOutputTest {
    @Mock
    private NormalizedNodeDataOutput output;

    @BeforeEach
    void before() throws Exception {
        doCallRealMethod().when(output).writeOptionalNormalizedNode(any());
    }

    @Test
    void testWriteOptionalNormalizedNodeAbsent() throws Exception {
        doNothing().when(output).writeBoolean(false);
        output.writeOptionalNormalizedNode(null);
        verify(output).writeBoolean(false);
    }

    @Test
    void testWriteOptionalNormalizedNodePresent() throws Exception {
        final var node = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(QName.create("test", "test")))
            .build();
        doNothing().when(output).writeBoolean(true);
        doNothing().when(output).writeNormalizedNode(node);
        output.writeOptionalNormalizedNode(node);
        verify(output).writeBoolean(true);
        verify(output).writeNormalizedNode(node);
    }
}
