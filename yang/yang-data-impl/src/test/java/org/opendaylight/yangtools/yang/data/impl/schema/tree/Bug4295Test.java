/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.io.File;
import java.net.URI;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TipProducingDataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.impl.RetestUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class Bug4295Test {

    private TipProducingDataTree inMemoryDataTree;
    private SchemaContext context;
    private QName root;
    private QName subRoot;
    private QName outerList;
    private QName innerList;
    private QName oId;
    private QName iId;
    private QName oLeaf;
    private QName iLeaf;
    private QNameModule foo;

    @Before
    public void init() throws Exception {
        final File resourceFile = new File(Bug4295Test.class.getResource("/bug-4295/foo.yang")
                .toURI());
        context = RetestUtils.parseYangSources(resourceFile);
        foo = QNameModule.create(new URI("foo"), SimpleDateFormatUtil.getRevisionFormat().parse("1970-01-01"));
        root = QName.create(foo, "root");
        subRoot = QName.create(foo, "sub-root");
        outerList = QName.create(foo, "outer-list");
        innerList = QName.create(foo, "inner-list");
        oId = QName.create(foo, "o-id");
        iId = QName.create(foo, "i-id");
        oLeaf = QName.create(foo, "o");
        iLeaf = QName.create(foo, "i");
        inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create(TreeType.OPERATIONAL);
        inMemoryDataTree.setSchemaContext(context);
    }

    @Test
    public void test() throws DataValidationFailedException {
        firstModification();
        secondModification();
    }


    private void firstModification() throws DataValidationFailedException {
        /*  MERGE */
        MapNode outerListNode = ImmutableNodes.mapNodeBuilder().withNodeIdentifier(NodeIdentifier.create(outerList))
                .withChild(createOuterListEntry("1", "o-1"))
                .withChild(createOuterListEntry("2", "o-2"))
                .withChild(createOuterListEntry("3", "o-3"))
                .build();
        ContainerNode rootContainerNode = createRootContainerBuilder()
        .withChild(
                createSubRootContainerBuilder()
                .withChild(outerListNode)
                .build())
        .build();
        YangInstanceIdentifier path = YangInstanceIdentifier.of(root);
        DataTreeModification modification = inMemoryDataTree.takeSnapshot().newModification();
        modification.merge(path, rootContainerNode);

        /*  WRITE INNER LIST WITH ENTRIES*/
        MapNode innerListNode = createInnerListBuilder()
            .withChild(createInnerListEntry("a", "i-a"))
            .withChild(createInnerListEntry("b", "i-b"))
            .build();
        path = YangInstanceIdentifier.of(root).node(subRoot).node(outerList).node(createOuterListEntryPath("2")).node(innerList);
        modification.write(path, innerListNode);

        /*  COMMIT */
        modification.ready();
        inMemoryDataTree.validate(modification);
        inMemoryDataTree.commit(inMemoryDataTree.prepare(modification));
        System.out.println("1. modification: " + inMemoryDataTree);
    }

    private void secondModification() throws DataValidationFailedException {
        /*  MERGE */
        MapNode outerListNode = ImmutableNodes.mapNodeBuilder().withNodeIdentifier(NodeIdentifier.create(outerList))
                .withChild(createOuterListEntry("3", "o-3"))
                .withChild(createOuterListEntry("4", "o-4"))
                .withChild(createOuterListEntry("5", "o-5"))
                .build();

        ContainerNode rootContainerNode = createRootContainerBuilder()
        .withChild(
                createSubRootContainerBuilder()
                .withChild(outerListNode)
                .build())
        .build();

        YangInstanceIdentifier path = YangInstanceIdentifier.of(root);
        DataTreeModification modification = inMemoryDataTree.takeSnapshot().newModification();
        modification.merge(path, rootContainerNode);

        /* WRITE EMPTY INNER LIST*/
        writeEmptyInnerList(modification, "2");

        /*  WRITE INNER LIST ENTRY */
//        MapEntryNode innerListEntryA = createInnerListEntry("a", "i-a-2");
//        //MapEntryNode innerListEntryB = createInnerListEntry("b", "i-b");
//        path = YangInstanceIdentifier.of(root).node(subRoot).node(outerList).node(createOuterListEntryPath("2")).node(innerList).node(createInnerListEntryPath("a"));
//        modification.write(path, innerListEntryA);

        /*  WRITE INNER LIST WITH ENTRIES*/
//        MapNode innerListNode = createInnerListBuilder()
//            .withChild(createInnerListEntry("a", "i-a-3"))
//            .withChild(createInnerListEntry("c", "i-c"))
//            .build();
//        path = YangInstanceIdentifier.of(root).node(subRoot).node(outerList).node(createOuterListEntryPath("2")).node(innerList);
//        modification.write(path, innerListNode);

        /*  COMMIT */
        modification.ready();
        inMemoryDataTree.validate(modification);
        inMemoryDataTree.commit(inMemoryDataTree.prepare(modification));
        System.out.println("2. modification: " + inMemoryDataTree);
    }

    private void writeEmptyInnerList(DataTreeModification modification, String outerListEntryKey) {
        YangInstanceIdentifier path = YangInstanceIdentifier.of(root).node(subRoot).node(outerList).node(createOuterListEntryPath(outerListEntryKey)).node(innerList);
        modification.write(path, createInnerListBuilder().build());
    }

    private DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> createRootContainerBuilder() {
        return ImmutableContainerNodeBuilder
                .create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(root));
    }

    private DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> createSubRootContainerBuilder() {
        return ImmutableContainerNodeBuilder
                .create()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(subRoot));
    }

    private NodeIdentifierWithPredicates createInnerListEntryPath(String keyValue) {
        Builder<QName, Object> builder = ImmutableMap.builder();
        ImmutableMap<QName, Object> keys = builder.put(iId, keyValue).build();
        return new YangInstanceIdentifier.NodeIdentifierWithPredicates(innerList, keys);
    }

    private CollectionNodeBuilder<MapEntryNode, MapNode> createInnerListBuilder() {
     return ImmutableNodes.mapNodeBuilder().withNodeIdentifier(NodeIdentifier.create(innerList));
    }

    private CollectionNodeBuilder<MapEntryNode, MapNode> createOuterListBuilder() {
        return ImmutableNodes.mapNodeBuilder().withNodeIdentifier(NodeIdentifier.create(outerList));
       }

    private NodeIdentifierWithPredicates createOuterListEntryPath(String keyValue) {
        Builder<QName, Object> builder = ImmutableMap.builder();
        ImmutableMap<QName, Object> keys = builder.put(oId, keyValue).build();
        return new YangInstanceIdentifier.NodeIdentifierWithPredicates(outerList, keys);
    }

    private MapEntryNode createOuterListEntry(String keyValue, String leafValue) {
        return ImmutableNodes.mapEntryBuilder(outerList, oId, keyValue).withChild(ImmutableNodes.leafNode(oLeaf, leafValue))
                .build();
    }

    private MapEntryNode createOuterListEntryWithEmptyInnerList(String keyValue, String leafValue) {
        return ImmutableNodes.mapEntryBuilder(outerList, oId, keyValue)
                .withChild(ImmutableNodes.mapNodeBuilder().withNodeIdentifier(NodeIdentifier.create(innerList)).build())
                .withChild(ImmutableNodes.leafNode(oLeaf, leafValue))
                .build();
    }

    private MapEntryNode createOuterListEntryWithInnerListEmptyEntries(String keyValue, String leafValue, String... iIds) {
        DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> outerListEntryBuilder = ImmutableNodes.mapEntryBuilder(outerList, oId, keyValue)
                .withChild(ImmutableNodes.leafNode(oLeaf, leafValue));

        outerListEntryBuilder.withChild(createInnerListWithEmptyEntries(iIds));

        return outerListEntryBuilder.build();
    }

    private MapNode createInnerListWithEmptyEntries(String... iIds) {
        CollectionNodeBuilder<MapEntryNode, MapNode> innerListBuilder = ImmutableNodes.mapNodeBuilder().withNodeIdentifier(NodeIdentifier.create(innerList));
        for (String id : iIds) {
            innerListBuilder.withChild(createInnerListEmptyEntry(id));
        }
        return innerListBuilder.build();
    }

    private MapEntryNode createInnerListEmptyEntry(String keyValue) {
        return ImmutableNodes.mapEntryBuilder(innerList, iId, keyValue).build();
    }

    private MapEntryNode createInnerListEntry(String keyValue, String leafValue) {
        return ImmutableNodes.mapEntryBuilder(innerList, iId, keyValue).withChild(ImmutableNodes.leafNode(iLeaf, leafValue))
                .build();
    }
}