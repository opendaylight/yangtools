/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.net.URI;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ConflictingModificationAppliedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TipProducingDataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderedListTest {
    private Logger LOG = LoggerFactory.getLogger(OrderedListTest.class);

    private TipProducingDataTree inMemoryDataTree;
    private SchemaContext context;

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
    public void setup() throws Exception {
        final File resourceFile = new File(Bug4295Test.class.getResource("/ordered-list-modification-test.yang")
                .toURI());
        context = YangParserTestUtils.parseYangSources(resourceFile);
        testModule = QNameModule.create(new URI("ordered-list-modification-test"),
                SimpleDateFormatUtil.getRevisionFormat().parse("1970-01-01"));
        parentContainer = QName.create(testModule, "parent-container");
        childContainer = QName.create(testModule, "child-container");
        parentOrderedList = QName.create(testModule, "parent-ordered-list");
        childOrderedList = QName.create(testModule, "child-ordered-list");
        parentKeyLeaf = QName.create(testModule, "parent-key-leaf");
        childKeyLeaf = QName.create(testModule, "child-key-leaf");
        parentOrdinaryLeaf = QName.create(testModule, "parent-ordinary-leaf");
        childOrdinaryLeaf = QName.create(testModule, "child-ordinary-leaf");
        inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create(TreeType.OPERATIONAL);
        inMemoryDataTree.setSchemaContext(context);
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
        OrderedMapNode parentOrderedListNode = Builders.orderedMapBuilder().withNodeIdentifier(
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

        OrderedMapNode childOrderedListNode = Builders.orderedMapBuilder().withNodeIdentifier(
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
        Optional<NormalizedNode<?, ?>> readNode = snapshotAfterCommits.readNode(path1);
        assertTrue(readNode.isPresent());

        readNode = snapshotAfterCommits.readNode(path2);
        assertTrue(readNode.isPresent());
    }

    public void modification2() throws DataValidationFailedException {
        OrderedMapNode parentOrderedListNode = Builders.orderedMapBuilder().withNodeIdentifier(
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

        OrderedMapNode childOrderedListNode = Builders.orderedMapBuilder().withNodeIdentifier(
                new NodeIdentifier(childOrderedList))
                .withChild(createChildOrderedListEntry("chkval1", "chlfval1updated"))
                .withChild(createChildOrderedListEntry("chkval2", "chlfval2updated"))
                .withChild(createChildOrderedListEntry("chkval3", "chlfval3")).build();

        YangInstanceIdentifier path2 = YangInstanceIdentifier.of(parentContainer).node(childContainer).node
                (parentOrderedList).node(createParentOrderedListEntryPath("pkval2")).node(childOrderedList);
        treeModification.merge(path2, childOrderedListNode);

        treeModification.ready();
        inMemoryDataTree.validate(treeModification);
        inMemoryDataTree.commit(inMemoryDataTree.prepare(treeModification));

        DataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        Optional<NormalizedNode<?, ?>> readNode = snapshotAfterCommits.readNode(path1);
        assertTrue(readNode.isPresent());

        readNode = snapshotAfterCommits.readNode(path2);
        assertTrue(readNode.isPresent());
    }

    public void modification3() throws DataValidationFailedException {
        OrderedMapNode parentOrderedListNode = Builders.orderedMapBuilder().withNodeIdentifier(
                new NodeIdentifier(parentOrderedList))
                .withChild(createParentOrderedListEntry("pkval1", "plfval1")).build();

        ContainerNode parentContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(parentContainer)).withChild(Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(childContainer)).withChild(parentOrderedListNode).build())
                .build();

        YangInstanceIdentifier path1 = YangInstanceIdentifier.of(parentContainer);

        DataTreeModification treeModification = inMemoryDataTree.takeSnapshot().newModification();
        treeModification.write(path1, parentContainerNode);

        OrderedMapNode childOrderedListNode = Builders.orderedMapBuilder().withNodeIdentifier(
                new NodeIdentifier(childOrderedList))
                .withChild(createChildOrderedListEntry("chkval1", "chlfval1new")).build();

        YangInstanceIdentifier path2 = YangInstanceIdentifier.of(parentContainer).node(childContainer).node
                (parentOrderedList)
                .node(createParentOrderedListEntryPath("pkval4")).node(childOrderedList);

        treeModification.merge(path2, childOrderedListNode);

        try {
            treeModification.ready();
            fail("Exception should have been thrown.");
            inMemoryDataTree.validate(treeModification);
            inMemoryDataTree.commit(inMemoryDataTree.prepare(treeModification));
        } catch (final IllegalArgumentException ex) {
            LOG.debug("IllegalArgumentException was thrown as expected: {}", ex);
            assertTrue(ex.getMessage().contains("Metadata not available for modification NodeModification"));
        }

        DataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        Optional<NormalizedNode<?, ?>> readNode = snapshotAfterCommits.readNode(path1);
        assertTrue(readNode.isPresent());

        readNode = snapshotAfterCommits.readNode(path2);
        assertFalse(readNode.isPresent());
    }

    public void modification4() throws DataValidationFailedException {
        DataTreeModification treeModification1 = inMemoryDataTree.takeSnapshot().newModification();
        DataTreeModification treeModification2 = inMemoryDataTree.takeSnapshot().newModification();

        OrderedMapNode parentOrderedListNode = Builders.orderedMapBuilder().withNodeIdentifier(new
                NodeIdentifier(parentOrderedList)).withChild(createParentOrderedListEntry("pkval1",
                "plfval1")).build();

        OrderedMapNode parentOrderedListNode2 = Builders.orderedMapBuilder().withNodeIdentifier(new
                NodeIdentifier(parentOrderedList)).withChild(createParentOrderedListEntry("pkval2",
                "plfval2")).build();

        ContainerNode parentContainerNode = Builders.containerBuilder().withNodeIdentifier(
                new NodeIdentifier(parentContainer)).withChild(Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(childContainer)).withChild(parentOrderedListNode).build()).build();

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
            LOG.debug("ConflictingModificationAppliedException was thrown as expected: {}", ex);
            assertTrue(ex.getMessage().equals("Node /(ordered-list-modification-test?revision=1970-01-01)parent-container"
                    + " was replaced by other transaction."));
        }

        DataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        Optional<NormalizedNode<?, ?>> readNode = snapshotAfterCommits.readNode(path);
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
        Optional<NormalizedNode<?, ?>> readNode = snapshotAfterCommits.readNode(path);
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
        Optional<NormalizedNode<?, ?>> readNode = snapshotAfterCommits.readNode(path);
        assertFalse(readNode.isPresent());
    }

    private MapEntryNode createParentOrderedListEntry(String keyValue, String leafValue) {
        return Builders.mapEntryBuilder().withNodeIdentifier(new NodeIdentifierWithPredicates(parentOrderedList,
                parentKeyLeaf, keyValue))
                .withChild(Builders.leafBuilder().withNodeIdentifier(NodeIdentifier.create(parentOrdinaryLeaf)).withValue
                        (leafValue).build()).build();
    }

    private MapEntryNode createChildOrderedListEntry(String keyValue, String leafValue) {
        return Builders.mapEntryBuilder().withNodeIdentifier(new NodeIdentifierWithPredicates(childOrderedList,
                childKeyLeaf, keyValue))
                .withChild(Builders.leafBuilder().withNodeIdentifier(NodeIdentifier.create(childOrdinaryLeaf)).withValue
                        (leafValue).build()).build();
    }

    private NodeIdentifierWithPredicates createParentOrderedListEntryPath(String keyValue) {
        ImmutableMap.Builder<QName, Object> builder = ImmutableMap.builder();
        ImmutableMap<QName, Object> keys = builder.put(parentKeyLeaf, keyValue).build();
        return new NodeIdentifierWithPredicates(parentOrderedList, keys);
    }

    private NodeIdentifierWithPredicates createChildOrderedListEntryPath(String keyValue) {
        ImmutableMap.Builder<QName, Object> builder = ImmutableMap.builder();
        ImmutableMap<QName, Object> keys = builder.put(childKeyLeaf, keyValue).build();
        return new NodeIdentifierWithPredicates(childOrderedList, keys);
    }
}
