/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

class OrderingEqualityTest {
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAR = QName.create("foo", "bar");

    @Test
    void testUserMap() {
        final var firstMap = Builders.orderedMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(FOO))
            .withChild(Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(FOO, BAR, "two"))
                .withChild(ImmutableNodes.leafNode(BAR, "two"))
                .build())
            .withChild(Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(FOO, BAR, "one"))
                .withChild(ImmutableNodes.leafNode(BAR, "one"))
                .build())
            .build();

        final var secondMap = Builders.orderedMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(FOO))
            .withChild(Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(FOO, BAR, "one"))
                .withChild(ImmutableNodes.leafNode(BAR, "one"))
                .build())
            .withChild(Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(FOO, BAR, "two"))
                .withChild(ImmutableNodes.leafNode(BAR, "two"))
                .build())
            .build();

        assertEquals(firstMap.asMap(), secondMap.asMap());
        assertNotEquals(firstMap, secondMap);
        assertNotEquals(secondMap, firstMap);

        final var thirdMap = Builders.orderedMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(FOO))
            .withChild(Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(FOO, BAR, "one"))
                .withChild(ImmutableNodes.leafNode(BAR, "one"))
                .build())
            .withChild(Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(FOO, BAR, "two"))
                .withChild(ImmutableNodes.leafNode(BAR, "two"))
                .build())
            .build();

        assertEquals(secondMap.asMap(), thirdMap.asMap());
        assertNotEquals(firstMap, thirdMap);
        assertEquals(secondMap, thirdMap);
        assertArrayEquals(secondMap.body().toArray(), thirdMap.body().toArray());
        assertEquals(secondMap.hashCode(), thirdMap.hashCode());

        // Although this map looks as secondMap, it is not equal
        final var systemMap = Builders.mapBuilder()
            .withNodeIdentifier(new NodeIdentifier(FOO))
            .withChild(Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(FOO, BAR, "one"))
                .withChild(ImmutableNodes.leafNode(BAR, "one"))
                .build())
            .withChild(Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(FOO, BAR, "two"))
                .withChild(ImmutableNodes.leafNode(BAR, "two"))
                .build())
            .build();

        assertEquals(secondMap.asMap(), systemMap.asMap());
        assertNotEquals(firstMap, systemMap);
        assertNotEquals(secondMap, systemMap);
    }

    @Test
    void testSystemMap() {
        final var firstMap = Builders.mapBuilder()
            .withNodeIdentifier(new NodeIdentifier(FOO))
            .withChild(Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(FOO, BAR, "one"))
                .withChild(ImmutableNodes.leafNode(BAR, "one"))
                .build())
            .withChild(Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(FOO, BAR, "two"))
                .withChild(ImmutableNodes.leafNode(BAR, "two"))
                .build())
            .build();
        final var secondMap = Builders.mapBuilder()
            .withNodeIdentifier(new NodeIdentifier(FOO))
            .withChild(Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(FOO, BAR, "two"))
                .withChild(ImmutableNodes.leafNode(BAR, "two"))
                .build())
            .withChild(Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(FOO, BAR, "one"))
                .withChild(ImmutableNodes.leafNode(BAR, "one"))
                .build())
            .build();

        assertEquals(firstMap.asMap(), secondMap.asMap());
        // Order does not matter
        assertEquals(firstMap, secondMap);
        assertEquals(secondMap, firstMap);
        assertEquals(firstMap.hashCode(), secondMap.hashCode());
    }
}
