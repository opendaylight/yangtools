/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserMapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.tree.api.ConflictingModificationAppliedException;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderedListTest {
    private static final Logger LOG = LoggerFactory.getLogger(OrderedListTest.class);
    private static final String TEST_YANG = """
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
            }""";

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

    @Before
    public void setup() {
        testModule = QNameModule.create(XMLNamespace.of("ordered-list-modification-test"));
        parentContainer = QName.create(testModule, "parent-container");
        childContainer = QName.create(testModule, "child-container");
        parentOrderedList = QName.create(testModule, "parent-ordered-list");
        childOrderedList = QName.create(testModule, "child-ordered-list");
        parentKeyLeaf = QName.create(testModule, "parent-key-leaf");
        childKeyLeaf = QName.create(testModule, "child-key-leaf");
        parentOrdinaryLeaf = QName.create(testModule, "parent-ordinary-leaf");
        childOrdinaryLeaf = QName.create(testModule, "child-ordinary-leaf");
        inMemoryDataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_OPERATIONAL,
            YangParserTestUtils.parseYang(TEST_YANG));
    }

    @Test
    public void testsequentialModifications() throws DataValidationFailedException {
        modification1();
        modification2();
        delete1();
        delete2();
        modification3();
        modification4();
    }

    public void modification1() throws DataValidationFailedException {
        UserMapNode parentOrderedListNode = Builders.orderedMapBuilder().withNodeIdentifier(
                new NodeIdentifier(parentOrderedList))
                .withChild(createParentOrderedListEntry("pkval1", "plfval1"))
                .withChild(createParentOrderedListEntry("pkval2", "plfval2"))
                .withChild(createParentOrderedListEntry("pkval3", "plfval3")).build();

        ContainerNode parentContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(parentContainer)).withChild(Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(childContainer)).withChild(parentOrderedListNode).build())
                .build();

        YangInstanceIdentifier path1 = YangInstanceIdentifier.of(parentContainer);

        DataTreeModification treeModification = inMemoryDataTree.takeSnapshot().newModification();
        treeModification.write(path1, parentContainerNode);

        UserMapNode childOrderedListNode = Builders.orderedMapBuilder().withNodeIdentifier(
                new NodeIdentifier(childOrderedList))
                .withChild(createChildOrderedListEntry("chkval1", "chlfval1"))
                .withChild(createChildOrderedListEntry("chkval2", "chlfval2")).build();

        YangInstanceIdentifier path2 = YangInstanceIdentifier.of(parentContainer).node(childContainer)
                .node(parentOrderedList).node(createParentOrderedListEntryPath("pkval2")).node(childOrderedList);

        treeModification.write(path2, childOrderedListNode);
        treeModification.ready();
        inMemoryDataTree.validate(treeModification);
        inMemoryDataTree.commit(inMemoryDataTree.prepare(treeModification));

        DataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        Optional<NormalizedNode> readNode = snapshotAfterCommits.readNode(path1);
        assertTrue(readNode.isPresent());

        readNode = snapshotAfterCommits.readNode(path2);
        assertTrue(readNode.isPresent());
    }

    public void modification2() throws DataValidationFailedException {
        UserMapNode parentOrderedListNode = Builders.orderedMapBuilder().withNodeIdentifier(
                new NodeIdentifier(parentOrderedList))
                .withChild(createParentOrderedListEntry("pkval3", "plfval3updated"))
                .withChild(createParentOrderedListEntry("pkval4", "plfval4"))
                .withChild(createParentOrderedListEntry("pkval5", "plfval5")).build();

        ContainerNode parentContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(parentContainer)).withChild(Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(childContainer)).withChild(parentOrderedListNode).build())
                .build();

        DataTreeModification treeModification = inMemoryDataTree.takeSnapshot().newModification();

        YangInstanceIdentifier path1 = YangInstanceIdentifier.of(parentContainer);
        treeModification.merge(path1, parentContainerNode);

        UserMapNode childOrderedListNode = Builders.orderedMapBuilder().withNodeIdentifier(
                new NodeIdentifier(childOrderedList))
                .withChild(createChildOrderedListEntry("chkval1", "chlfval1updated"))
                .withChild(createChildOrderedListEntry("chkval2", "chlfval2updated"))
                .withChild(createChildOrderedListEntry("chkval3", "chlfval3")).build();

        YangInstanceIdentifier path2 = YangInstanceIdentifier.of(parentContainer).node(childContainer)
                .node(parentOrderedList).node(createParentOrderedListEntryPath("pkval2")).node(childOrderedList);
        treeModification.merge(path2, childOrderedListNode);

        treeModification.ready();
        inMemoryDataTree.validate(treeModification);
        inMemoryDataTree.commit(inMemoryDataTree.prepare(treeModification));

        DataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        Optional<NormalizedNode> readNode = snapshotAfterCommits.readNode(path1);
        assertTrue(readNode.isPresent());

        readNode = snapshotAfterCommits.readNode(path2);
        assertTrue(readNode.isPresent());
    }

    public void modification3() throws DataValidationFailedException {
        UserMapNode parentOrderedListNode = Builders.orderedMapBuilder().withNodeIdentifier(
                new NodeIdentifier(parentOrderedList))
                .withChild(createParentOrderedListEntry("pkval1", "plfval1")).build();

        ContainerNode parentContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(parentContainer)).withChild(Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(childContainer)).withChild(parentOrderedListNode).build())
                .build();

        YangInstanceIdentifier path1 = YangInstanceIdentifier.of(parentContainer);

        DataTreeModification treeModification = inMemoryDataTree.takeSnapshot().newModification();
        treeModification.write(path1, parentContainerNode);

        UserMapNode childOrderedListNode = Builders.orderedMapBuilder().withNodeIdentifier(
                new NodeIdentifier(childOrderedList))
                .withChild(createChildOrderedListEntry("chkval1", "chlfval1new")).build();

        YangInstanceIdentifier path2 = YangInstanceIdentifier.of(parentContainer).node(childContainer)
                .node(parentOrderedList)
                .node(createParentOrderedListEntryPath("pkval4")).node(childOrderedList);

        treeModification.merge(path2, childOrderedListNode);

        try {
            treeModification.ready();
            fail("Exception should have been thrown.");
            inMemoryDataTree.validate(treeModification);
            inMemoryDataTree.commit(inMemoryDataTree.prepare(treeModification));
        } catch (final IllegalArgumentException ex) {
            LOG.debug("IllegalArgumentException was thrown as expected", ex);
            assertTrue(ex.getMessage().contains("Metadata not available for modification ModifiedNode"));
        }

        DataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        Optional<NormalizedNode> readNode = snapshotAfterCommits.readNode(path1);
        assertTrue(readNode.isPresent());

        readNode = snapshotAfterCommits.readNode(path2);
        assertFalse(readNode.isPresent());
    }

    public void modification4() throws DataValidationFailedException {
        DataTreeModification treeModification1 = inMemoryDataTree.takeSnapshot().newModification();
        DataTreeModification treeModification2 = inMemoryDataTree.takeSnapshot().newModification();

        UserMapNode parentOrderedListNode = Builders.orderedMapBuilder().withNodeIdentifier(
            new NodeIdentifier(parentOrderedList)).withChild(createParentOrderedListEntry("pkval1", "plfval1"))
                .build();

        UserMapNode parentOrderedListNode2 = Builders.orderedMapBuilder().withNodeIdentifier(
            new NodeIdentifier(parentOrderedList)).withChild(createParentOrderedListEntry("pkval2", "plfval2"))
                .build();

        ContainerNode parentContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(parentContainer)).withChild(Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(childContainer)).withChild(parentOrderedListNode).build())
                .build();

        ContainerNode parentContainerNode2 = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(parentContainer)).withChild(Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(childContainer)).withChild(parentOrderedListNode2).build())
                .build();

        YangInstanceIdentifier path = YangInstanceIdentifier.of(parentContainer);

        treeModification1.write(path, parentContainerNode);
        treeModification2.write(path, parentContainerNode2);
        treeModification1.ready();
        treeModification2.ready();

        inMemoryDataTree.validate(treeModification1);
        inMemoryDataTree.commit(inMemoryDataTree.prepare(treeModification1));

        try {
            inMemoryDataTree.validate(treeModification2);
            fail("Exception should have been thrown.");
            inMemoryDataTree.commit(inMemoryDataTree.prepare(treeModification2));
        } catch (ConflictingModificationAppliedException ex) {
            LOG.debug("ConflictingModificationAppliedException was thrown as expected", ex);
            assertTrue(ex.getMessage().contains("Node was replaced by other transaction"));
        }

        DataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        Optional<NormalizedNode> readNode = snapshotAfterCommits.readNode(path);
        assertTrue(readNode.isPresent());
    }

    public void delete1() throws DataValidationFailedException {
        YangInstanceIdentifier path = YangInstanceIdentifier.of(parentContainer).node(childContainer)
                .node(parentOrderedList).node(createParentOrderedListEntryPath("pkval2")).node(childOrderedList)
                .node(createChildOrderedListEntryPath("chkval1"));

        DataTreeModification treeModification = inMemoryDataTree.takeSnapshot().newModification();
        treeModification.delete(path);
        treeModification.ready();
        inMemoryDataTree.validate(treeModification);
        inMemoryDataTree.commit(inMemoryDataTree.prepare(treeModification));

        DataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        Optional<NormalizedNode> readNode = snapshotAfterCommits.readNode(path);
        assertFalse(readNode.isPresent());
    }

    public void delete2() throws DataValidationFailedException {
        YangInstanceIdentifier path = YangInstanceIdentifier.of(parentContainer).node(childContainer)
                .node(parentOrderedList).node(createParentOrderedListEntryPath("pkval2"));

        DataTreeModification treeModification = inMemoryDataTree.takeSnapshot().newModification();
        treeModification.delete(path);
        treeModification.ready();
        inMemoryDataTree.validate(treeModification);
        inMemoryDataTree.commit(inMemoryDataTree.prepare(treeModification));

        DataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        Optional<NormalizedNode> readNode = snapshotAfterCommits.readNode(path);
        assertFalse(readNode.isPresent());
    }

    private MapEntryNode createParentOrderedListEntry(final String keyValue, final String leafValue) {
        return Builders.mapEntryBuilder().withNodeIdentifier(NodeIdentifierWithPredicates.of(parentOrderedList,
                parentKeyLeaf, keyValue))
                .withChild(Builders.leafBuilder().withNodeIdentifier(NodeIdentifier.create(parentOrdinaryLeaf))
                    .withValue(leafValue).build()).build();
    }

    private MapEntryNode createChildOrderedListEntry(final String keyValue, final String leafValue) {
        return Builders.mapEntryBuilder().withNodeIdentifier(NodeIdentifierWithPredicates.of(childOrderedList,
                childKeyLeaf, keyValue))
                .withChild(Builders.leafBuilder().withNodeIdentifier(NodeIdentifier.create(childOrdinaryLeaf))
                    .withValue(leafValue).build()).build();
    }

    private NodeIdentifierWithPredicates createParentOrderedListEntryPath(final String keyValue) {
        ImmutableMap.Builder<QName, Object> builder = ImmutableMap.builder();
        ImmutableMap<QName, Object> keys = builder.put(parentKeyLeaf, keyValue).build();
        return NodeIdentifierWithPredicates.of(parentOrderedList, keys);
    }

    private NodeIdentifierWithPredicates createChildOrderedListEntryPath(final String keyValue) {
        ImmutableMap.Builder<QName, Object> builder = ImmutableMap.builder();
        ImmutableMap<QName, Object> keys = builder.put(childKeyLeaf, keyValue).build();
        return NodeIdentifierWithPredicates.of(childOrderedList, keys);
    }
}
