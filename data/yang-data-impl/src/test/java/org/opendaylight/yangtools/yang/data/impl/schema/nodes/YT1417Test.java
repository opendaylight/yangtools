/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserLeafSetNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

@ExtendWith(MockitoExtension.class)
class YT1417Test {
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAR = QName.create("foo", "bar");

    @Test
    void testContainerNodeEquality() {
        final var mock = mock(ContainerNode.class);
        doReturn(new NodeIdentifier(FOO)).when(mock).name();
        doReturn(1).when(mock).size();
        doReturn(ImmutableNodes.leafNode(BAR, "abc")).when(mock).childByArg(new NodeIdentifier(BAR));

        assertEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(FOO))
            .withChild(ImmutableNodes.leafNode(BAR, "abc"))
            .build(), mock);

        // Mismatched identifier
        assertNotEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(BAR))
            .withChild(ImmutableNodes.leafNode(BAR, "abc"))
            .build(), mock);

        // Mismatched child size
        assertNotEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(FOO))
            .withChild(ImmutableNodes.leafNode(FOO, "abc"))
            .withChild(ImmutableNodes.leafNode(BAR, "abc"))
            .build(), mock);

        // Mismatched child
        assertNotEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(FOO))
            .withChild(ImmutableNodes.leafNode(FOO, "abc"))
            .build(), mock);
    }

    @Test
    void testSystemLeafSetNodeEquality() {
        final var mock = mock(SystemLeafSetNode.class);
        doReturn(new NodeIdentifier(FOO)).when(mock).name();
        doReturn(1).when(mock).size();
        doReturn(ImmutableNodes.leafSetEntry(FOO, "abc")).when(mock).childByArg(new NodeWithValue<>(FOO, "abc"));

        assertEquals(ImmutableNodes.newSystemLeafSetBuilder()
            .withNodeIdentifier(new NodeIdentifier(FOO))
            .withChild(ImmutableNodes.leafSetEntry(FOO, "abc"))
            .build(), mock);

        // Mismatched identifier
        assertNotEquals(ImmutableNodes.newSystemLeafSetBuilder()
            .withNodeIdentifier(new NodeIdentifier(BAR))
            .withChild(ImmutableNodes.leafSetEntry(BAR, "abc"))
            .build(), mock);

        // Mismatched child size
        assertNotEquals(ImmutableNodes.newSystemLeafSetBuilder()
            .withNodeIdentifier(new NodeIdentifier(FOO))
            .withChild(ImmutableNodes.leafSetEntry(FOO, "abc"))
            .withChild(ImmutableNodes.leafSetEntry(FOO, "def"))
            .build(), mock);

        // Mismatched child
        assertNotEquals(ImmutableNodes.newSystemLeafSetBuilder()
            .withNodeIdentifier(new NodeIdentifier(FOO))
            .withChild(ImmutableNodes.leafSetEntry(FOO, "def"))
            .build(), mock);
    }

    @Test
    void testUserLeafSetNodeEquality() {
        final var mock = mock(UserLeafSetNode.class);
        doReturn(new NodeIdentifier(FOO)).when(mock).name();
        doReturn(List.of(
            ImmutableNodes.leafSetEntry(FOO, "abc"),
            ImmutableNodes.leafSetEntry(FOO, "def"))).when(mock).body();

        assertEquals(ImmutableNodes.newUserLeafSetBuilder()
            .withNodeIdentifier(new NodeIdentifier(FOO))
            .withChild(ImmutableNodes.leafSetEntry(FOO, "abc"))
            .withChild(ImmutableNodes.leafSetEntry(FOO, "def"))
            .build(), mock);

        // Mismatched identifier
        assertNotEquals(ImmutableNodes.newUserLeafSetBuilder()
            .withNodeIdentifier(new NodeIdentifier(BAR))
            .withChild(ImmutableNodes.leafSetEntry(BAR, "abc"))
            .withChild(ImmutableNodes.leafSetEntry(BAR, "def"))
            .build(), mock);

        // Mismatched child order
        assertNotEquals(ImmutableNodes.newUserLeafSetBuilder()
            .withNodeIdentifier(new NodeIdentifier(FOO))
            .withChild(ImmutableNodes.leafSetEntry(FOO, "def"))
            .withChild(ImmutableNodes.leafSetEntry(FOO, "abc"))
            .build(), mock);
    }
}
