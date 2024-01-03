/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.util.ImmutableOffsetMap;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class InstanceIdToNodesTest {

    private static final String NS = "urn:opendaylight:params:xml:ns:yang:controller:md:sal:normalization:test";
    private static final String REVISION = "2014-03-13";
    private static final QName ID = QName.create(NS, REVISION, "id");
    private static final QName FOO = QName.create(ID, "foo");
    private static final QName BAR = QName.create(ID, "bar");
    private static final NodeIdentifier TWO_KEY_LIST = NodeIdentifier.create(QName.create(ID, "two-key-list"));

    private static EffectiveModelContext ctx;

    private final NodeIdentifier rootContainer = new NodeIdentifier(QName.create(NS, REVISION, "test"));
    private final NodeIdentifier outerList = new NodeIdentifier(QName.create(NS, REVISION, "outer-list"));
    private final NodeIdentifierWithPredicates outerListWithKey = NodeIdentifierWithPredicates.of(
            QName.create(NS, REVISION, "outer-list"), ID, 1);

    private final NodeIdentifier leafList = new NodeIdentifier(QName.create(NS, REVISION, "ordered-leaf-list"));
    private final NodeWithValue<String> leafListWithValue = new NodeWithValue<>(leafList.getNodeType(), "abcd");

    @BeforeAll
    static void setUp() {
        ctx = YangParserTestUtils.parseYangResources(InstanceIdToNodesTest.class, "/filter-test.yang");
    }

    @AfterAll
    static void teardown() {
        ctx = null;
    }

    @Test
    void testListLastChildOverride() {
        assertEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(rootContainer)
            .withChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(outerList)
                .withChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(outerListWithKey)
                    .withChild(ImmutableNodes.leafNode(new NodeIdentifier(ID), 1))
                    .build())
                .build())
            .build(),
            ImmutableNodes.fromInstanceId(ctx, YangInstanceIdentifier.of(rootContainer, outerList, outerListWithKey)));
    }

    @Test
    void testLeafList() {
        assertEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(rootContainer)
            .withChild(ImmutableNodes.<String>newUserLeafSetBuilder()
                .withNodeIdentifier(leafList)
                .withChild(ImmutableNodes.leafSetEntry(leafListWithValue))
                .build())
            .build(),
            ImmutableNodes.fromInstanceId(ctx, YangInstanceIdentifier.of(rootContainer, leafList, leafListWithValue)));
    }

    @Test
    void testEmptyInstanceIdentifier() {
        assertEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(SchemaContext.NAME))
            .build(), ImmutableNodes.fromInstanceId(ctx, YangInstanceIdentifier.of()));
    }

    @Test
    void testKeyOrdering() {
        final Map<QName, Object> misordered = ImmutableOffsetMap.orderedCopyOf(ImmutableMap.of(BAR, "bar", FOO, "foo"));
        final var id = NodeIdentifierWithPredicates.of(TWO_KEY_LIST.getNodeType(), misordered);
        assertArrayEquals(new Object[] { BAR, FOO }, id.keySet().toArray());

        final var filter = ImmutableNodes.fromInstanceId(ctx, YangInstanceIdentifier.of(TWO_KEY_LIST, id));
        final var value = assertInstanceOf(MapNode.class, filter).body();
        assertEquals(1, value.size());
        final var entry = value.iterator().next();

        // The entry must have a the proper order
        assertArrayEquals(new Object[] { FOO, BAR }, entry.name().keySet().toArray());
    }
}
