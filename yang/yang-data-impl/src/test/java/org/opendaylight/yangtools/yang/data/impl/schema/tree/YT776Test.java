/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.containerBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.leafBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.leafSetBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.mapBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.mapEntryBuilder;

import com.google.common.collect.ImmutableMap;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class YT776Test {
    private static final QName MODULE = QName.create("yt776", "yt776");
    private static final NodeIdentifier BOX = new NodeIdentifier(QName.create(MODULE, "box"));
    private static final QName OBJECT = QName.create(MODULE, "object");
    private static final QName OBJECT_ID = QName.create(MODULE, "object-id");
    private static final NodeIdentifier OBJECT_LIST = new NodeIdentifier(QName.create(MODULE, "object"));
    private static final NodeIdentifierWithPredicates OBJECT_ITEM = new NodeIdentifierWithPredicates(OBJECT,
        ImmutableMap.of(OBJECT_ID, "1"));
    private static final LeafNode<?> OBJECT_ID_LEAF = leafBuilder().withNodeIdentifier(new NodeIdentifier(OBJECT_ID))
            .withValue("1").build();
    private static final NodeIdentifier ATTRIBUTES = new NodeIdentifier(QName.create(MODULE, "attributes"));

    private static SchemaContext SCHEMA_CONTEXT;

    private DataTree dataTree;

    @BeforeClass
    public static void beforeClass() {
        SCHEMA_CONTEXT = TestModel.createTestContext("/yt776/yt776.yang");
    }

    @AfterClass
    public static void afterClass() {
        SCHEMA_CONTEXT = null;
    }

    @Before
    public void init() {
        dataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION, SCHEMA_CONTEXT);
    }

    @Test
    public void testNoAttributes() {
        final DataTreeModification mod = dataTree.takeSnapshot().newModification();
        mod.write(YangInstanceIdentifier.create(BOX), containerBuilder().withNodeIdentifier(BOX)
            .withChild(mapBuilder().withNodeIdentifier(OBJECT_LIST)
                .addChild(mapEntryBuilder().withNodeIdentifier(OBJECT_ITEM)
                    .withChild(OBJECT_ID_LEAF)
                    .build())
                .build())
            .build());

        try {
            mod.ready();
            fail("Should fail with IAE");
        } catch (IllegalArgumentException e) {
            // This is actually mandatory leaf enforcer kicking in: attributes have to be present.
            assertEquals("Node (yt776)object[{(yt776)object-id=1}] is missing mandatory descendant /(yt776)attributes",
                e.getMessage());
        }
    }

    @Test
    public void testEmptyAttributes() {
        final ContainerNode data = containerBuilder().withNodeIdentifier(BOX)
                .withChild(mapBuilder().withNodeIdentifier(OBJECT_LIST)
                    .addChild(mapEntryBuilder().withNodeIdentifier(OBJECT_ITEM)
                        .withChild(OBJECT_ID_LEAF)
                        .withChild(leafSetBuilder().withNodeIdentifier(ATTRIBUTES).build())
                        .build())
                    .build())
                .build();

        final DataTreeModification mod = dataTree.takeSnapshot().newModification();
        mod.write(YangInstanceIdentifier.create(BOX), data);

        try {
            mod.ready();
            fail("Should fail with IAE");
        } catch (IllegalArgumentException e) {
            assertEquals("Node (yt776)attributes does not have enough elements (0), needs at least 1", e.getMessage());
        }
    }

    @Test
    public void testOneAttribute() {
        writeAndCommit(containerBuilder().withNodeIdentifier(BOX)
            .withChild(mapBuilder().withNodeIdentifier(OBJECT_LIST)
                .addChild(mapEntryBuilder().withNodeIdentifier(OBJECT_ITEM)
                    .withChild(OBJECT_ID_LEAF)
                    .withChild(leafSetBuilder().withNodeIdentifier(ATTRIBUTES)
                        .withChildValue("object1")
                        .build())
                    .build())
                .build())
            .build());
    }

    @Test
    public void testTwoAttributes() {
        writeAndCommit(containerBuilder().withNodeIdentifier(BOX)
            .withChild(mapBuilder().withNodeIdentifier(OBJECT_LIST)
                .addChild(mapEntryBuilder().withNodeIdentifier(OBJECT_ITEM)
                    .withChild(OBJECT_ID_LEAF)
                    .withChild(leafSetBuilder().withNodeIdentifier(ATTRIBUTES)
                        .withChildValue("object1")
                        .withChildValue("object2")
                        .build())
                    .build())
                .build())
            .build());
    }

    @Test
    public void testThreeAttributes() {
        final DataTreeModification mod = dataTree.takeSnapshot().newModification();
        mod.write(YangInstanceIdentifier.create(BOX), containerBuilder().withNodeIdentifier(BOX)
            .withChild(mapBuilder().withNodeIdentifier(OBJECT_LIST)
                .addChild(mapEntryBuilder().withNodeIdentifier(OBJECT_ITEM)
                    .withChild(OBJECT_ID_LEAF)
                    .withChild(leafSetBuilder().withNodeIdentifier(ATTRIBUTES)
                        .withChildValue("object1")
                        .withChildValue("object2")
                        .withChildValue("object3")
                        .build())
                    .build())
                .build())
            .build());

        try {
            mod.ready();
            fail("Should fail with IAE");
        } catch (IllegalArgumentException e) {
            assertEquals("Node (yt776)attributes has too many elements (3), can have at most 2", e.getMessage());
        }
    }

    @Test
    public void testEmptyAndMergeOne() {
        final DataTreeModification mod = dataTree.takeSnapshot().newModification();
        mod.write(YangInstanceIdentifier.create(BOX), containerBuilder().withNodeIdentifier(BOX)
            .withChild(mapBuilder().withNodeIdentifier(OBJECT_LIST)
                .addChild(mapEntryBuilder().withNodeIdentifier(OBJECT_ITEM)
                    .withChild(OBJECT_ID_LEAF)
                    .build())
                .build())
            .build());
        mod.merge(YangInstanceIdentifier.create(BOX), containerBuilder().withNodeIdentifier(BOX)
            .withChild(mapBuilder().withNodeIdentifier(OBJECT_LIST)
                .addChild(mapEntryBuilder().withNodeIdentifier(OBJECT_ITEM)
                    .withChild(OBJECT_ID_LEAF)
                    .withChild(leafSetBuilder().withNodeIdentifier(ATTRIBUTES)
                        .withChildValue("object1")
                        .build())
                    .build())
                .build())
            .build());

        commit(mod);
    }

    private void writeAndCommit(final ContainerNode data) {
        final DataTreeModification mod = dataTree.takeSnapshot().newModification();
        mod.write(YangInstanceIdentifier.create(BOX), data);
        commit(mod);
    }

    private void commit(final DataTreeModification mod) {
        mod.ready();
        dataTree.commit(dataTree.prepare(mod));
    }
}
