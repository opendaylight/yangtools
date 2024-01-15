/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

class YT1562Test {
    @Test
    void regualarLeafToString() {
        assertEquals("ImmutableLeafNode{name=(test)test, body=42}",
            ImmutableNodes.leafNode(QName.create("test", "test"), 42).toString());
    }

    @Test
    void binaryLeafToString() {
        assertEquals("ImmutableLeafNode{name=(test)test, body=[42, 43]}",
            ImmutableNodes.leafNode(QName.create("test", "test"), new byte[] { 42, 43 }).toString());
    }

    @Test
    void regualarLeafSetEntryToString() {
        assertEquals("ImmutableLeafSetEntryNode{name=(test)test[42], body=42}", entryNode(42).toString());
    }

    @Test
    void binaryLeafSetEntryToString() {
        assertEquals("ImmutableLeafSetEntryNode{name=(test)test[[42, 43]], body=[42, 43]}",
            entryNode(new byte[] { 42, 43 }).toString());
    }

    private static LeafSetEntryNode<?> entryNode(final Object value) {
        return Builders.leafSetEntryBuilder()
            .withNodeIdentifier(new NodeWithValue<>(QName.create("test", "test"), value))
            .withValue(value)
            .build();
    }
}
