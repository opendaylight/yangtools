/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT776Test {
    private static final QName MODULE = QName.create("yt776", "yt776");
    private static final NodeIdentifier BOX = new NodeIdentifier(QName.create(MODULE, "box"));
    private static final QName OBJECT = QName.create(MODULE, "object");
    private static final QName OBJECT_ID = QName.create(MODULE, "object-id");
    private static final NodeIdentifier OBJECT_LIST = new NodeIdentifier(OBJECT);
    private static final NodeIdentifierWithPredicates OBJECT_ITEM = NodeIdentifierWithPredicates.of(OBJECT,
        ImmutableMap.of(OBJECT_ID, "1"));
    private static final LeafNode<?> OBJECT_ID_LEAF = ImmutableNodes.leafNode(OBJECT_ID, "1");
    private static final NodeIdentifier ATTRIBUTES = new NodeIdentifier(QName.create(MODULE, "attributes"));

    private static final QName NESTED = QName.create(MODULE, "nested");
    private static final QName NESTED_ATTRIBUTE = QName.create(MODULE, "nested-attribute");
    private static final NodeIdentifier NESTED_LIST = new NodeIdentifier(NESTED);
    private static final NodeIdentifierWithPredicates NESTED_ITEM = NodeIdentifierWithPredicates.of(NESTED,
        ImmutableMap.of(NESTED_ATTRIBUTE, "foo"));

    private static final NodeIdentifier ANY_OF = new NodeIdentifier(QName.create(MODULE, "any-of"));
    private static final QName SOME_LEAF = QName.create(MODULE, "some-leaf");
    private static final NodeIdentifier SOME_LEAF_ID = new NodeIdentifier(SOME_LEAF);
    private static final QName SOME_LIST = QName.create(MODULE, "some-list");
    private static final NodeIdentifier SOME_LIST_ID = new NodeIdentifier(SOME_LIST);
    private static final NodeIdentifierWithPredicates SOME_LIST_ITEM = NodeIdentifierWithPredicates.of(SOME_LIST,
                ImmutableMap.of(SOME_LEAF, "foo"));
    private static EffectiveModelContext SCHEMA_CONTEXT;

    private DataTree dataTree;

    @BeforeAll
    static void beforeClass() {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYang("""
            module yt776 {
              namespace yt776;
              prefix yt776;

              container box {
                list object {
                  key object-id;

                  leaf object-id {
                    type string;
                  }

                  leaf-list attributes {
                    type string;
                    min-elements 1;
                    max-elements 2;
                  }

                  list nested {
                    key nested-attribute;
                    max-elements 1;
                    leaf nested-attribute {
                      type string;
                    }
                  }
                }

                choice any-of {
                  leaf some-leaf {
                    type string;
                  }
                  list some-list {
                    key some-leaf;
                    min-elements 1;

                    leaf some-leaf {
                      type string;
                    }
                  }
                }
              }
            }""");
    }

    @AfterAll
    static void afterClass() {
        SCHEMA_CONTEXT = null;
    }

    @BeforeEach
    void init() {
        dataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION, SCHEMA_CONTEXT);
    }

    @Test
    void testNoAttributes() {
        final var mod = dataTree.takeSnapshot().newModification();
        mod.write(YangInstanceIdentifier.of(BOX), ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(BOX)
            .withChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(OBJECT_LIST)
                .addChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(OBJECT_ITEM)
                    .withChild(OBJECT_ID_LEAF)
                    .build())
                .build())
            .build());

        final var ex = assertThrows(IllegalArgumentException.class, mod::ready);
        // FIXME: This is actually mandatory leaf enforcer kicking in: attributes have to be present. This is
        //        most probably not what we want.
        assertEquals("Node (yt776)object[{(yt776)object-id=1}] is missing mandatory descendant /(yt776)attributes",
                ex.getMessage());
    }

    @Test
    void testEmptyAttributes() throws DataValidationFailedException {
        final var mod = write(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(BOX)
            .withChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(OBJECT_LIST)
                .addChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(OBJECT_ITEM)
                    .withChild(OBJECT_ID_LEAF)
                    .withChild(ImmutableNodes.newSystemLeafSetBuilder().withNodeIdentifier(ATTRIBUTES).build())
                    .build())
                .build())
            .build());

        final var ex = assertThrows(MinMaxElementsValidationFailedException.class, mod::ready);
        assertEquals("(yt776)attributes does not have enough elements (0), needs at least 1", ex.getMessage());
        ListConstraintsValidation.assertTooFewElements(ex);
    }

    @Test
    void testOneAttribute() throws DataValidationFailedException {
        writeAndCommit(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(BOX)
            .withChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(OBJECT_LIST)
                .addChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(OBJECT_ITEM)
                    .withChild(OBJECT_ID_LEAF)
                    .withChild(ImmutableNodes.newSystemLeafSetBuilder()
                        .withNodeIdentifier(ATTRIBUTES)
                        .withChildValue("object1")
                        .build())
                    .build())
                .build())
            .build());
    }

    @Test
    void testTwoAttributes() throws DataValidationFailedException {
        writeAndCommit(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(BOX)
            .withChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(OBJECT_LIST)
                .addChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(OBJECT_ITEM)
                    .withChild(OBJECT_ID_LEAF)
                    .withChild(ImmutableNodes.newSystemLeafSetBuilder()
                        .withNodeIdentifier(ATTRIBUTES)
                        .withChildValue("object1")
                        .withChildValue("object2")
                        .build())
                    .build())
                .build())
            .build());
    }

    @Test
    void testThreeAttributes() throws DataValidationFailedException {
        final var mod = write(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(BOX)
            .withChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(OBJECT_LIST)
                .addChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(OBJECT_ITEM)
                    .withChild(OBJECT_ID_LEAF)
                    .withChild(ImmutableNodes.newSystemLeafSetBuilder()
                        .withNodeIdentifier(ATTRIBUTES)
                        .withChildValue("object1")
                        .withChildValue("object2")
                        .withChildValue("object3")
                        .build())
                    .build())
                .build())
            .build());

        final var ex = assertThrows(MinMaxElementsValidationFailedException.class, mod::ready);
        assertEquals("(yt776)attributes has too many elements (3), can have at most 2", ex.getMessage());
        ListConstraintsValidation.assertTooManyElements(ex);
    }

    @Test
    void testEmptyAndMergeOne() throws DataValidationFailedException {
        final var mod = dataTree.takeSnapshot().newModification();
        mod.write(YangInstanceIdentifier.of(BOX), ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(BOX)
            .withChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(OBJECT_LIST)
                .addChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(OBJECT_ITEM)
                    .withChild(OBJECT_ID_LEAF)
                    .build())
                .build())
            .build());
        mod.merge(YangInstanceIdentifier.of(BOX), ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(BOX)
            .withChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(OBJECT_LIST)
                .addChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(OBJECT_ITEM)
                    .withChild(OBJECT_ID_LEAF)
                    .withChild(ImmutableNodes.newSystemLeafSetBuilder()
                        .withNodeIdentifier(ATTRIBUTES)
                        .withChildValue("object1")
                        .build())
                    .build())
                .build())
            .build());

        commit(mod);
    }

    @Test
    void testEmptyAndMergeOneWithListTouched() throws DataValidationFailedException {
        final var mod = dataTree.takeSnapshot().newModification();
        mod.write(YangInstanceIdentifier.of(BOX), ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(BOX)
            .withChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(OBJECT_LIST)
                .addChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(OBJECT_ITEM)
                    .withChild(OBJECT_ID_LEAF)
                    .build())
                .build())
            .build());
        mod.merge(YangInstanceIdentifier.of(BOX), ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(BOX)
            .withChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(OBJECT_LIST)
                .addChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(OBJECT_ITEM)
                    .withChild(OBJECT_ID_LEAF)
                    .withChild(ImmutableNodes.newSystemLeafSetBuilder()
                        .withNodeIdentifier(ATTRIBUTES)
                        .withChildValue("object1")
                        .build())
                    .build())
                .build())
            .build());

        mod.delete(YangInstanceIdentifier.of(BOX, OBJECT_LIST, OBJECT_ITEM, NESTED_LIST, NESTED_ITEM));

        commit(mod);
    }

    @Test
    void testDisappearInChoice() throws DataValidationFailedException {
        var mod = dataTree.takeSnapshot().newModification();
        // Initialize choice with list
        mod.write(YangInstanceIdentifier.of(BOX), ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(BOX)
            .withChild(ImmutableNodes.newChoiceBuilder()
                .withNodeIdentifier(ANY_OF)
                .withChild(ImmutableNodes.newSystemMapBuilder()
                    .withNodeIdentifier(SOME_LIST_ID)
                    .withChild(ImmutableNodes.newMapEntryBuilder()
                        .withNodeIdentifier(SOME_LIST_ITEM)
                        .withChild(ImmutableNodes.leafNode(SOME_LEAF_ID, "foo"))
                        .build())
                    .build())
                .build())
            .build());
        commit(mod);

        // Now delete the single item, causing the list to fizzle, while creating the alterinative case
        mod = dataTree.takeSnapshot().newModification();
        mod.delete(YangInstanceIdentifier.of(BOX, ANY_OF, SOME_LIST_ID, SOME_LIST_ITEM));
        mod.write(YangInstanceIdentifier.of(BOX, ANY_OF, SOME_LEAF_ID), ImmutableNodes.leafNode(SOME_LEAF_ID, "foo"));

        commit(mod);
    }

    private DataTreeModification write(final ContainerNode data) throws DataValidationFailedException {
        final var mod = dataTree.takeSnapshot().newModification();
        mod.write(YangInstanceIdentifier.of(BOX), data);
        return mod;
    }

    private void writeAndCommit(final ContainerNode data) throws DataValidationFailedException {
        final var mod = dataTree.takeSnapshot().newModification();
        mod.write(YangInstanceIdentifier.of(BOX), data);
        commit(mod);
    }

    private void commit(final DataTreeModification mod) throws DataValidationFailedException {
        mod.ready();
        dataTree.validate(mod);
        dataTree.commit(dataTree.prepare(mod));
    }
}
