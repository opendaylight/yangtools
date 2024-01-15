/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;

class YT1562Test {
    @Test
    void regualarLeafToString() {
        assertEquals("ImmutableLeafNode{name=(test)test, body=42}",
            ImmutableLeafNode.of(new NodeIdentifier(QName.create("test", "test")), 42).toString());
    }

    @Test
    void binaryLeafToString() {
        assertEquals("ImmutableLeafNode{name=(test)test, body=b64:Kis=}",
            ImmutableLeafNode.of(new NodeIdentifier(QName.create("test", "test")), new byte[] { 42, 43 }).toString());
    }

    @Test
    void regualarLeafSetEntryToString() {
        assertEquals("ImmutableLeafSetEntryNode{name=(test)test[42], body=42}",
            ImmutableLeafSetEntryNode.of(new NodeWithValue<>(QName.create("test", "test"), 42)).toString());
    }

    @Test
    void binaryLeafSetEntryToString() {
        assertEquals("ImmutableLeafSetEntryNode{name=(test)test[b64:Kis=], body=b64:Kis=}",
            ImmutableLeafSetEntryNode.of(new NodeWithValue<>(QName.create("test", "test"), new byte[] { 42, 43 }))
                .toString());
    }
}
