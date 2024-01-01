/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.builder.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Bug4295Test {

    private DataTree inMemoryDataTree;
    private QName root;
    private QName subRoot;
    private QName outerList;
    private QName innerList;
    private QName oid;
    private QName iid;
    private QName oleaf;
    private QName ileaf;
    private QNameModule foo;

    @Test
    void test() throws DataValidationFailedException {
        foo = QNameModule.create(XMLNamespace.of("foo"));
        root = QName.create(foo, "root");
        subRoot = QName.create(foo, "sub-root");
        outerList = QName.create(foo, "outer-list");
        innerList = QName.create(foo, "inner-list");
        oid = QName.create(foo, "o-id");
        iid = QName.create(foo, "i-id");
        oleaf = QName.create(foo, "o");
        ileaf = QName.create(foo, "i");
        inMemoryDataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_OPERATIONAL,
                YangParserTestUtils.parseYang("""
                module foo {
                  namespace "foo";
                  prefix foo;

                  container root {
                    container sub-root {
                      list outer-list {
                        key "o-id";
                        leaf o-id {
                          type string;
                        }
                        list inner-list {
                          key "i-id";
                          leaf i-id {
                            type string;
                          }
                          leaf i {
                            type string;
                          }
                        }
                        leaf o {
                          type string;
                        }
                      }
                    }
                  }
                }"""));

        firstModification();
        secondModification(1);
        secondModification(2);
        secondModification(3);
    }

    private void firstModification() throws DataValidationFailedException {
        /*  MERGE */
        YangInstanceIdentifier path = YangInstanceIdentifier.of(root);
        DataTreeModification modification = inMemoryDataTree.takeSnapshot().newModification();
        modification.merge(path, createRootContainerBuilder()
            .withChild(createSubRootContainerBuilder()
                .withChild(ImmutableNodes.newSystemMapBuilder()
                    .withNodeIdentifier(NodeIdentifier.create(outerList))
                    .withChild(createOuterListEntry("1", "o-1"))
                    .withChild(createOuterListEntry("2", "o-2"))
                    .withChild(createOuterListEntry("3", "o-3"))
                    .build())
                .build())
            .build());

        /*  WRITE INNER LIST WITH ENTRIES*/
        path = YangInstanceIdentifier.of(root).node(subRoot).node(outerList).node(createOuterListEntryPath("2"))
                .node(innerList);
        modification.write(path, createInnerListBuilder()
            .withChild(createInnerListEntry("a", "i-a"))
            .withChild(createInnerListEntry("b", "i-b"))
            .build());

        /*  COMMIT */
        modification.ready();
        inMemoryDataTree.validate(modification);
        inMemoryDataTree.commit(inMemoryDataTree.prepare(modification));
    }

    private void secondModification(final int testScenarioNumber) throws DataValidationFailedException {
        /*  MERGE */
        ContainerNode rootContainerNode = createRootContainerBuilder()
            .withChild(createSubRootContainerBuilder()
                .withChild(ImmutableNodes.newSystemMapBuilder()
                    .withNodeIdentifier(NodeIdentifier.create(outerList))
                    .withChild(createOuterListEntry("3", "o-3"))
                    .withChild(createOuterListEntry("4", "o-4"))
                    .withChild(createOuterListEntry("5", "o-5"))
                    .build())
                .build())
            .build();

        YangInstanceIdentifier path = YangInstanceIdentifier.of(root);
        DataTreeModification modification = inMemoryDataTree.takeSnapshot().newModification();
        modification.merge(path, rootContainerNode);

        switch (testScenarioNumber) {
            case 1:
                /* WRITE EMPTY INNER LIST */
                writeEmptyInnerList(modification, "2");
                break;
            case 2: {
                /* WRITE INNER LIST ENTRY */
                MapEntryNode innerListEntryA = createInnerListEntry("a", "i-a-2");
                path = YangInstanceIdentifier.of(root, subRoot, outerList).node(createOuterListEntryPath("2"))
                    .node(innerList).node(createInnerListEntryPath("a"));
                modification.write(path, innerListEntryA);
                break;
            }
            case 3: {
                /* WRITE INNER LIST WITH ENTRIES */
                path = YangInstanceIdentifier.of(root, subRoot, outerList).node(createOuterListEntryPath("2"))
                    .node(innerList);
                modification.write(path, createInnerListBuilder()
                    .withChild(createInnerListEntry("a", "i-a-3"))
                    .withChild(createInnerListEntry("c", "i-c"))
                    .build());
                break;
            }
            default:
                break;
        }

        /*  COMMIT */
        modification.ready();
        inMemoryDataTree.validate(modification);
        inMemoryDataTree.commit(inMemoryDataTree.prepare(modification));
    }

    private void writeEmptyInnerList(final DataTreeModification modification, final String outerListEntryKey) {
        YangInstanceIdentifier path = YangInstanceIdentifier.of(root, subRoot, outerList)
                .node(createOuterListEntryPath(outerListEntryKey)).node(innerList);
        modification.write(path, createInnerListBuilder().build());
    }

    private DataContainerNodeBuilder<NodeIdentifier, ContainerNode> createRootContainerBuilder() {
        return ImmutableNodes.newContainerBuilder().withNodeIdentifier(new NodeIdentifier(root));
    }

    private DataContainerNodeBuilder<NodeIdentifier, ContainerNode> createSubRootContainerBuilder() {
        return  ImmutableNodes.newContainerBuilder().withNodeIdentifier(new NodeIdentifier(subRoot));
    }

    private CollectionNodeBuilder<MapEntryNode, SystemMapNode> createInnerListBuilder() {
        return ImmutableNodes.newSystemMapBuilder().withNodeIdentifier(NodeIdentifier.create(innerList));
    }

    private NodeIdentifierWithPredicates createInnerListEntryPath(final String keyValue) {
        return NodeIdentifierWithPredicates.of(innerList, iid, keyValue);
    }

    private NodeIdentifierWithPredicates createOuterListEntryPath(final String keyValue) {
        return NodeIdentifierWithPredicates.of(outerList, oid, keyValue);
    }

    private MapEntryNode createOuterListEntry(final String keyValue, final String leafValue) {
        return ImmutableNodes.mapEntryBuilder(outerList, oid, keyValue)
                .withChild(ImmutableNodes.leafNode(oleaf, leafValue)).build();
    }

    private MapEntryNode createInnerListEntry(final String keyValue, final String leafValue) {
        return ImmutableNodes.mapEntryBuilder(innerList, iid, keyValue)
                .withChild(ImmutableNodes.leafNode(ileaf, leafValue)).build();
    }
}