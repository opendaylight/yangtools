/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.util.ImmutableOffsetMap;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class InstanceIdToNodesTest {

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

    @BeforeClass
    public static void setUp() {
        ctx = YangParserTestUtils.parseYangResources(InstanceIdToNodesTest.class, "/filter-test.yang");
    }

    @AfterClass
    public static void teardown() {
        ctx = null;
    }

    @Test
    public void testListLastChildOverride() {
        assertEquals(Builders.containerBuilder()
            .withNodeIdentifier(rootContainer)
            .withChild(Builders.mapBuilder()
                .withNodeIdentifier(outerList)
                .withChild(Builders.mapEntryBuilder()
                    .withNodeIdentifier(outerListWithKey)
                    .withChild(Builders.leafBuilder()
                        .withNodeIdentifier(new NodeIdentifier(ID))
                        .withValue(1)
                        .build())
                    .build())
                .build())
            .build(),
            ImmutableNodes.fromInstanceId(ctx,
                YangInstanceIdentifier.create(rootContainer, outerList, outerListWithKey)));
    }

    @Test
    public void testLeafList() {
        assertEquals(Builders.containerBuilder()
            .withNodeIdentifier(rootContainer)
            .withChild(Builders.<String>orderedLeafSetBuilder()
                .withNodeIdentifier(leafList)
                .withChild(Builders.<String>leafSetEntryBuilder()
                    .withNodeIdentifier(leafListWithValue)
                    .withValue(leafListWithValue.getValue())
                    .build())
                .build())
            .build(),
            ImmutableNodes.fromInstanceId(ctx,
                YangInstanceIdentifier.create(rootContainer, leafList, leafListWithValue)));
    }

    @Test
    public void testEmptyInstanceIdentifier() {
        assertEquals(ImmutableNodes.containerNode(SchemaContext.NAME),
            ImmutableNodes.fromInstanceId(ctx, YangInstanceIdentifier.empty()));
    }

    @Test
    public void testKeyOrdering() {
        final Map<QName, Object> misordered = ImmutableOffsetMap.orderedCopyOf(ImmutableMap.of(BAR, "bar", FOO, "foo"));
        final NodeIdentifierWithPredicates id = NodeIdentifierWithPredicates.of(TWO_KEY_LIST.getNodeType(), misordered);
        assertArrayEquals(new Object[] { BAR, FOO }, id.keySet().toArray());

        final NormalizedNode filter = ImmutableNodes.fromInstanceId(ctx,
            YangInstanceIdentifier.create(TWO_KEY_LIST, id));
        assertThat(filter, isA(MapNode.class));
        final Collection<MapEntryNode> value = ((MapNode) filter).body();
        assertEquals(1, value.size());
        final MapEntryNode entry = value.iterator().next();

        // The entry must have a the proper order
        assertArrayEquals(new Object[] { FOO, BAR }, entry.getIdentifier().keySet().toArray());
    }
}
