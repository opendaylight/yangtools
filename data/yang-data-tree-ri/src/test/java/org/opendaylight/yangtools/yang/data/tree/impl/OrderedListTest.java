/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.ConflictingModificationAppliedException;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OrderedListTest {
    private static final Logger LOG = LoggerFactory.getLogger(OrderedListTest.class);

    private DataTree inMemoryDataTree;

    private QNameModule testModule;
    private QName parentContainer;
    private QName childContainer;
    private QName parentOrderedList;
    private QName childOrderedList;
    private QName parentKeyLeaf;
    private QName parentOrdinaryLeaf;
    private QName childKeyLeaf;
    private QName childOrdinaryLeaf;

    @BeforeEach
    void setup() {
        testModule = QNameModule.of("ordered-list-modification-test");
        parentContainer = QName.create(testModule, "parent-container");
        childContainer = QName.create(testModule, "child-container");
        parentOrderedList = QName.create(testModule, "parent-ordered-list");
        childOrderedList = QName.create(testModule, "child-ordered-list");
        parentKeyLeaf = QName.create(testModule, "parent-key-leaf");
        childKeyLeaf = QName.create(testModule, "child-key-leaf");
        parentOrdinaryLeaf = QName.create(testModule, "parent-ordinary-leaf");
        childOrdinaryLeaf = QName.create(testModule, "child-ordinary-leaf");
        inMemoryDataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_OPERATIONAL,
            YangParserTestUtils.parseYang("""
                module ordered-list-modification-test {
                  namespace "ordered-list-modification-test";
                  prefix "olmt";

                  container parent-container {
                    container child-container {
                      list parent-ordered-list {
                        ordered-by user;
                        key "parent-key-leaf";

                        leaf parent-key-leaf {
                          type string;
                        }
                        leaf parent-ordinary-leaf {
                          type string;
                        }

                        list child-ordered-list {
                          ordered-by user;
                          key "child-key-leaf";

                          leaf child-key-leaf {
                            type string;
                          }
                          leaf child-ordinary-leaf {
                            type string;
                          }
                        }
                      }
                    }
                  }
                }"""));
    }

    @Test
    void testsequentialModifications() throws DataValidationFailedException {
        modification1();
        modification2();
        delete1();
        delete2();
        modification3();
        modification4();
    }

    public void modification1() throws DataValidationFailedException {
        final var parentOrderedListNode = ImmutableNodes.newUserMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(parentOrderedList))
            .withChild(createParentOrderedListEntry("pkval1", "plfval1"))
            .withChild(createParentOrderedListEntry("pkval2", "plfval2"))
            .withChild(createParentOrderedListEntry("pkval3", "plfval3"))
            .build();

        final var parentContainerNode = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(parentContainer))
            .withChild(ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(childContainer))
                .withChild(parentOrderedListNode)
                .build())
            .build();

        final var path1 = YangInstanceIdentifier.of(parentContainer);

        final var treeModification = inMemoryDataTree.takeSnapshot().newModification();
        treeModification.write(path1, parentContainerNode);

        final var childOrderedListNode = ImmutableNodes.newUserMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(childOrderedList))
            .withChild(createChildOrderedListEntry("chkval1", "chlfval1"))
            .withChild(createChildOrderedListEntry("chkval2", "chlfval2"))
            .build();

        final var path2 = YangInstanceIdentifier.of(parentContainer, childContainer, parentOrderedList)
            .node(createParentOrderedListEntryPath("pkval2")).node(childOrderedList);

        treeModification.write(path2, childOrderedListNode);
        treeModification.ready();
        inMemoryDataTree.validate(treeModification);
        inMemoryDataTree.commit(inMemoryDataTree.prepare(treeModification));

        final var snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        var readNode = snapshotAfterCommits.readNode(path1);
        assertTrue(readNode.isPresent());

        readNode = snapshotAfterCommits.readNode(path2);
        assertTrue(readNode.isPresent());
    }

    public void modification2() throws DataValidationFailedException {
        final var parentOrderedListNode = ImmutableNodes.newUserMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(parentOrderedList))
            .withChild(createParentOrderedListEntry("pkval3", "plfval3updated"))
            .withChild(createParentOrderedListEntry("pkval4", "plfval4"))
            .withChild(createParentOrderedListEntry("pkval5", "plfval5"))
            .build();

        final var parentContainerNode = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(parentContainer))
            .withChild(ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(childContainer))
                .withChild(parentOrderedListNode)
                .build())
            .build();

        final var treeModification = inMemoryDataTree.takeSnapshot().newModification();

        final var path1 = YangInstanceIdentifier.of(parentContainer);
        treeModification.merge(path1, parentContainerNode);

        final var childOrderedListNode = ImmutableNodes.newUserMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(childOrderedList))
            .withChild(createChildOrderedListEntry("chkval1", "chlfval1updated"))
            .withChild(createChildOrderedListEntry("chkval2", "chlfval2updated"))
            .withChild(createChildOrderedListEntry("chkval3", "chlfval3"))
            .build();

        final var path2 = YangInstanceIdentifier.of(parentContainer).node(childContainer)
                .node(parentOrderedList).node(createParentOrderedListEntryPath("pkval2")).node(childOrderedList);
        treeModification.merge(path2, childOrderedListNode);

        treeModification.ready();
        inMemoryDataTree.validate(treeModification);
        inMemoryDataTree.commit(inMemoryDataTree.prepare(treeModification));

        final var snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        var readNode = snapshotAfterCommits.readNode(path1);
        assertTrue(readNode.isPresent());

        readNode = snapshotAfterCommits.readNode(path2);
        assertTrue(readNode.isPresent());
    }

    public void modification3() throws DataValidationFailedException {
        final var parentOrderedListNode = ImmutableNodes.newUserMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(parentOrderedList))
            .withChild(createParentOrderedListEntry("pkval1", "plfval1"))
            .build();

        final var parentContainerNode = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(parentContainer))
            .withChild(ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(childContainer))
                .withChild(parentOrderedListNode)
                .build())
            .build();

        final var path1 = YangInstanceIdentifier.of(parentContainer);

        final var treeModification = inMemoryDataTree.takeSnapshot().newModification();
        treeModification.write(path1, parentContainerNode);

        final var childOrderedListNode = ImmutableNodes.newUserMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(childOrderedList))
            .withChild(createChildOrderedListEntry("chkval1", "chlfval1new"))
            .build();

        final var path2 = YangInstanceIdentifier.of(parentContainer).node(childContainer)
                .node(parentOrderedList)
                .node(createParentOrderedListEntryPath("pkval4")).node(childOrderedList);

        treeModification.merge(path2, childOrderedListNode);

        final var ex = assertThrows(IllegalArgumentException.class, treeModification::ready);
        assertTrue(ex.getMessage().contains("Metadata not available for modification ModifiedNode"));

        final var snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        var readNode = snapshotAfterCommits.readNode(path1);
        assertTrue(readNode.isPresent());

        readNode = snapshotAfterCommits.readNode(path2);
        assertFalse(readNode.isPresent());
    }

    public void modification4() throws DataValidationFailedException {
        final var treeModification1 = inMemoryDataTree.takeSnapshot().newModification();
        final var treeModification2 = inMemoryDataTree.takeSnapshot().newModification();

        final var parentOrderedListNode = ImmutableNodes.newUserMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(parentOrderedList))
            .withChild(createParentOrderedListEntry("pkval1", "plfval1"))
            .build();

        final var parentOrderedListNode2 = ImmutableNodes.newUserMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(parentOrderedList))
            .withChild(createParentOrderedListEntry("pkval2", "plfval2"))
            .build();

        final var parentContainerNode = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(parentContainer))
            .withChild(ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(childContainer))
                .withChild(parentOrderedListNode)
                .build())
            .build();

        final var parentContainerNode2 = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(parentContainer))
            .withChild(ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(childContainer))
                .withChild(parentOrderedListNode2)
                .build())
            .build();

        final var path = YangInstanceIdentifier.of(parentContainer);

        treeModification1.write(path, parentContainerNode);
        treeModification2.write(path, parentContainerNode2);
        treeModification1.ready();
        treeModification2.ready();

        inMemoryDataTree.validate(treeModification1);
        inMemoryDataTree.commit(inMemoryDataTree.prepare(treeModification1));

        final var ex = assertThrows(ConflictingModificationAppliedException.class,
            () -> inMemoryDataTree.validate(treeModification2));
        LOG.debug("ConflictingModificationAppliedException was thrown as expected", ex);
        assertTrue(ex.getMessage().contains("Node was replaced by other transaction"));

        final var snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        final var readNode = snapshotAfterCommits.readNode(path);
        assertTrue(readNode.isPresent());
    }

    public void delete1() throws DataValidationFailedException {
        final var path = YangInstanceIdentifier.of(parentContainer).node(childContainer)
                .node(parentOrderedList).node(createParentOrderedListEntryPath("pkval2")).node(childOrderedList)
                .node(createChildOrderedListEntryPath("chkval1"));

        final var treeModification = inMemoryDataTree.takeSnapshot().newModification();
        treeModification.delete(path);
        treeModification.ready();
        inMemoryDataTree.validate(treeModification);
        inMemoryDataTree.commit(inMemoryDataTree.prepare(treeModification));

        final var snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        final var readNode = snapshotAfterCommits.readNode(path);
        assertFalse(readNode.isPresent());
    }

    public void delete2() throws DataValidationFailedException {
        final var path = YangInstanceIdentifier.of(parentContainer).node(childContainer)
                .node(parentOrderedList).node(createParentOrderedListEntryPath("pkval2"));

        final var treeModification = inMemoryDataTree.takeSnapshot().newModification();
        treeModification.delete(path);
        treeModification.ready();
        inMemoryDataTree.validate(treeModification);
        inMemoryDataTree.commit(inMemoryDataTree.prepare(treeModification));

        final var snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        final var readNode = snapshotAfterCommits.readNode(path);
        assertFalse(readNode.isPresent());
    }

    private MapEntryNode createParentOrderedListEntry(final String keyValue, final String leafValue) {
        return ImmutableNodes.newMapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(parentOrderedList, parentKeyLeaf, keyValue))
            .withChild(ImmutableNodes.leafNode(parentOrdinaryLeaf, leafValue))
            .build();
    }

    private MapEntryNode createChildOrderedListEntry(final String keyValue, final String leafValue) {
        return ImmutableNodes.newMapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(childOrderedList, childKeyLeaf, keyValue))
            .withChild(ImmutableNodes.leafNode(childOrdinaryLeaf, leafValue))
            .build();
    }

    private NodeIdentifierWithPredicates createParentOrderedListEntryPath(final String keyValue) {
        return NodeIdentifierWithPredicates.of(parentOrderedList, parentKeyLeaf, keyValue);
    }

    private NodeIdentifierWithPredicates createChildOrderedListEntryPath(final String keyValue) {
        return NodeIdentifierWithPredicates.of(childOrderedList, childKeyLeaf, keyValue);
    }
}
