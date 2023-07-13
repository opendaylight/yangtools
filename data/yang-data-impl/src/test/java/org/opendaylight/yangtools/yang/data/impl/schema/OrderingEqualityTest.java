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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserMapNode;

public class OrderingEqualityTest {
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAR = QName.create("foo", "bar");

    @Test
    public void testUserMap() {
        final UserMapNode firstMap = Builders.orderedMapBuilder()
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

        final UserMapNode secondMap = Builders.orderedMapBuilder()
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
        assertFalse(firstMap.equals(secondMap));
        assertFalse(secondMap.equals(firstMap));

        final UserMapNode thirdMap = Builders.orderedMapBuilder()
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
        assertFalse(firstMap.equals(thirdMap));
        assertTrue(secondMap.equals(thirdMap));
        assertArrayEquals(secondMap.body().toArray(), thirdMap.body().toArray());
        assertEquals(secondMap.hashCode(), thirdMap.hashCode());

        // Although this map looks as secondMap, it is not equal
        final SystemMapNode systemMap = Builders.mapBuilder()
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
        assertFalse(firstMap.equals(systemMap));
        assertFalse(secondMap.equals(systemMap));
    }

    @Test
    public void testSystemMap() {
        final SystemMapNode firstMap = Builders.mapBuilder()
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
        final SystemMapNode secondMap = Builders.mapBuilder()
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
        assertTrue(firstMap.equals(secondMap));
        assertTrue(secondMap.equals(firstMap));
        assertEquals(firstMap.hashCode(), secondMap.hashCode());
    }
}
