/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.api.TreeType;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Bug8291Test {
    private static final String NS = "bug8291";
    private static final QName ROOT = QName.create(NS, "root");
    private static final QName OUTER_LIST = QName.create(NS, "outer-list");
    private static final QName OUTER_LIST_ID = QName.create(NS, "id");
    private static final QName INNER_LIST = QName.create(NS, "inner-list");

    private EffectiveModelContext schemaContext;

    private static DataTree initDataTree(final EffectiveModelContext schemaContext) {
        final var config = new DataTreeConfiguration.Builder(TreeType.CONFIGURATION).setRootPath(
                YangInstanceIdentifier.of(ROOT).node(OUTER_LIST)).build();
        return new InMemoryDataTreeFactory().create(config, schemaContext);
    }

    @Test
    void test() throws DataValidationFailedException {
        schemaContext = YangParserTestUtils.parseYang("""
            module bug8291 {
              yang-version 1;
              namespace bug8291;
              prefix bug8291;

              container root {
                list outer-list {
                  key "id";
                  leaf id {
                    type uint16;
                  }

                  list inner-list {
                    key name;
                    leaf name {
                      type string;
                    }
                    leaf value {
                      type string;
                    }
                  }
                }
              }
            }""");
        assertNotNull(schemaContext, "Schema context must not be null.");

        final var inMemoryDataTree = initDataTree(schemaContext);
        writeOuterListMapEntry(inMemoryDataTree);
        writeInnerList(inMemoryDataTree);
    }

    private static void writeInnerList(final DataTree inMemoryDataTree) throws DataValidationFailedException {
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();
        modificationTree.write(
            YangInstanceIdentifier.of(NodeIdentifierWithPredicates.of(OUTER_LIST, OUTER_LIST_ID, 1)).node(INNER_LIST),
            ImmutableNodes.newSystemMapBuilder().withNodeIdentifier(new NodeIdentifier(INNER_LIST)).build());

        modificationTree.ready();
        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }

    private static void writeOuterListMapEntry(final DataTree inMemoryDataTree)
            throws DataValidationFailedException {
        final var modificationTree = inMemoryDataTree.takeSnapshot().newModification();

        final var outerListMapEntry = ImmutableNodes.newMapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(OUTER_LIST, OUTER_LIST_ID, 1))
            .withChild(ImmutableNodes.leafNode(OUTER_LIST_ID, 1))
            .build();

        modificationTree.write(YangInstanceIdentifier.of(NodeIdentifierWithPredicates.of(OUTER_LIST, OUTER_LIST_ID, 1)),
            outerListMapEntry);
        modificationTree.ready();

        inMemoryDataTree.validate(modificationTree);
        final var prepare = inMemoryDataTree.prepare(modificationTree);
        inMemoryDataTree.commit(prepare);
    }
}
