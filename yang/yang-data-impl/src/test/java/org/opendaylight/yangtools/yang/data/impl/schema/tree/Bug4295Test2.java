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
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TipProducingDataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.impl.RetestUtils;
import org.opendaylight.yangtools.yang.data.impl.leafref.context.test.retest.DataTreeCandidateValidatorTest;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class Bug4295Test2 {

    private TipProducingDataTree inMemoryDataTree;
    private SchemaContext context;
    private QName list1;
    private QName id;
    private QName leaf1;
    private QNameModule foo;

    @Before
    public void ini() throws Exception {
        final File resourceFile = new File(DataTreeCandidateValidatorTest.class.getResource("/bug4295/foo.yang")
                .toURI());
        context = RetestUtils.parseYangSources(resourceFile);
        foo = QNameModule.create(new URI("foo"), SimpleDateFormatUtil.getRevisionFormat().parse("1970-01-01"));
        list1 = QName.create(foo, "list1");
        id = QName.create(foo, "id");
        leaf1 = QName.create(foo, "leaf1");
        inMemoryDataTree = InMemoryDataTreeFactory.getInstance().create(TreeType.OPERATIONAL);
        inMemoryDataTree.setSchemaContext(context);
    }

    @Test
    public void mergeOperationalTest() throws DataValidationFailedException {
        doWriteAndMerge();
        doWriteAndMerge();
        mergeWholeList();
        // writeWholeList();
        // mergeWholeList();
    }

    private void doWriteAndMerge() throws DataValidationFailedException {
        DataTreeModification initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        MapNode list1Node = ImmutableNodes.mapNodeBuilder().withNodeIdentifier(NodeIdentifier.create(list1))
                //.withChild(createEntry("1", "value 1")).withChild(createEntry("2", "value 2"))
                .build();
        YangInstanceIdentifier path = YangInstanceIdentifier.of(list1);
        initialDataTreeModification.merge(path, list1Node);

        Builder<QName, Object> builder = ImmutableMap.builder();
        ImmutableMap<QName, Object> keys = builder.put(id, "3").build();
        NodeIdentifierWithPredicates entryPath = new YangInstanceIdentifier.NodeIdentifierWithPredicates(list1, keys);
        path = YangInstanceIdentifier.of(list1).node(entryPath);
        initialDataTreeModification.write(path, createEntry("3", "value 3"));

        initialDataTreeModification.ready();

        inMemoryDataTree.validate(initialDataTreeModification);
        DataTreeCandidate writeContributorsCandidate = inMemoryDataTree.prepare(initialDataTreeModification);
        inMemoryDataTree.commit(writeContributorsCandidate);
        System.out.println("init: " + inMemoryDataTree);

        basicWrite();
        System.out.println("basicWrite: " + inMemoryDataTree);

        basicMerge();
        System.out.println("basicMerge: " + inMemoryDataTree);

        basicWrite();
        System.out.println("basicWrite -b : " + inMemoryDataTree);

        multipleWrite();
        System.out.println("multipleWrite: " + inMemoryDataTree);

        //multipleMerge();
        System.out.println("multipleMerge: " + inMemoryDataTree);

        multipleMergeAndWrite();
        System.out.println("multipleMergeAndWrite: " + inMemoryDataTree);

        basicWrite();
        System.out.println("basicWrite2: " + inMemoryDataTree);

        basicMerge();
        System.out.println("basicMerge2: " + inMemoryDataTree);

        basicWrite();
        System.out.println("basicWrite2 -b : " + inMemoryDataTree);

        multipleWrite();
        System.out.println("multipleWrite2: " + inMemoryDataTree);

        //multipleMerge();
        System.out.println("multipleMerge2: " + inMemoryDataTree);

        multipleMergeAndWrite();
        System.out.println("multipleMergeAndWrite2: " + inMemoryDataTree);
    }

    private void basicWrite() throws DataValidationFailedException {
        DataTreeModification initialDataTreeModification;
        YangInstanceIdentifier path;
        DataTreeCandidate writeContributorsCandidate;
        initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        Builder<QName, Object> builder = ImmutableMap.builder();
        ImmutableMap<QName, Object> keys = builder.put(id, "3").build();
        NodeIdentifierWithPredicates entryPath = new YangInstanceIdentifier.NodeIdentifierWithPredicates(list1, keys);
        path = YangInstanceIdentifier.of(list1).node(entryPath);
        initialDataTreeModification.write(path, createEntry("3", "value 3"));
        initialDataTreeModification.ready();
        inMemoryDataTree.validate(initialDataTreeModification);
        writeContributorsCandidate = inMemoryDataTree.prepare(initialDataTreeModification);
        inMemoryDataTree.commit(writeContributorsCandidate);
    }

    private void basicMerge() throws DataValidationFailedException {
        DataTreeModification initialDataTreeModification;
        YangInstanceIdentifier path;
        DataTreeCandidate writeContributorsCandidate;
        initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        Builder<QName, Object> builder = ImmutableMap.builder();
        ImmutableMap<QName, Object> keys = builder.put(id, "4").build();
        NodeIdentifierWithPredicates entryPath = new YangInstanceIdentifier.NodeIdentifierWithPredicates(list1, keys);
        path = YangInstanceIdentifier.of(list1).node(entryPath);
        initialDataTreeModification.merge(path, createEntry("4", "value 4"));
        initialDataTreeModification.ready();
        inMemoryDataTree.validate(initialDataTreeModification);
        writeContributorsCandidate = inMemoryDataTree.prepare(initialDataTreeModification);
        inMemoryDataTree.commit(writeContributorsCandidate);
    }

    private void multipleWrite() throws DataValidationFailedException {
        DataTreeModification initialDataTreeModification;
        YangInstanceIdentifier path;
        DataTreeCandidate writeContributorsCandidate;
        initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        Builder<QName, Object> builder = ImmutableMap.builder();
        ImmutableMap<QName, Object> keys = builder.put(id, "5").build();
        NodeIdentifierWithPredicates entryPath = new YangInstanceIdentifier.NodeIdentifierWithPredicates(list1, keys);
        path = YangInstanceIdentifier.of(list1).node(entryPath);
        initialDataTreeModification.write(path, createEntry("5", "value 5"));
        initialDataTreeModification.write(path, createEntry("5", "value 6"));
        initialDataTreeModification.write(path, createEntry("5", "value 7"));
        initialDataTreeModification.write(path, createEntry("5", "value 6"));
        initialDataTreeModification.write(path, createEntry("5", "value 5"));
        initialDataTreeModification.write(path, createEntry("5", "value 6"));
        initialDataTreeModification.write(path, createEntry("5", "value 7"));

        builder = ImmutableMap.builder();
        keys = builder.put(id, "3").build();
        entryPath = new YangInstanceIdentifier.NodeIdentifierWithPredicates(list1, keys);
        path = YangInstanceIdentifier.of(list1).node(entryPath);
        initialDataTreeModification.write(path, createEntry("3", "value 6"));
        initialDataTreeModification.write(path, createEntry("3", "value 5"));
        initialDataTreeModification.write(path, createEntry("3", "value 6"));
        initialDataTreeModification.write(path, createEntry("3", "value 7"));
        initialDataTreeModification.write(path, createEntry("3", "value 6"));
        initialDataTreeModification.write(path, createEntry("3", "value 5"));
        initialDataTreeModification.write(path, createEntry("3", "value 6"));
        initialDataTreeModification.write(path, createEntry("3", "value 7"));
        initialDataTreeModification.write(path, createEntry("3", "value 6"));

        builder = ImmutableMap.builder();
        keys = builder.put(id, "20").build();
        entryPath = new YangInstanceIdentifier.NodeIdentifierWithPredicates(list1, keys);
        path = YangInstanceIdentifier.of(list1).node(entryPath);
        initialDataTreeModification.write(path, createEntry("20", "value 6"));
        initialDataTreeModification.write(path, createEntry("20", "value 5"));
        initialDataTreeModification.write(path, createEntry("20", "value 6"));
        initialDataTreeModification.write(path, createEntry("20", "value 7"));
        initialDataTreeModification.write(path, createEntry("20", "value 6"));
        initialDataTreeModification.write(path, createEntry("20", "value 5"));
        initialDataTreeModification.write(path, createEntry("20", "value 6"));
        initialDataTreeModification.write(path, createEntry("20", "value 7"));
        initialDataTreeModification.write(path, createEntry("20", "value 6"));

        builder = ImmutableMap.builder();
        keys = builder.put(id, "3").build();
        entryPath = new YangInstanceIdentifier.NodeIdentifierWithPredicates(list1, keys);
        path = YangInstanceIdentifier.of(list1).node(entryPath);
        initialDataTreeModification.write(path, createEntry("3", "value 6"));
        initialDataTreeModification.write(path, createEntry("3", "value 5"));
        initialDataTreeModification.write(path, createEntry("3", "value 6"));
        initialDataTreeModification.write(path, createEntry("3", "value 7"));
        initialDataTreeModification.write(path, createEntry("3", "value 6"));
        initialDataTreeModification.write(path, createEntry("3", "value 5"));
        initialDataTreeModification.write(path, createEntry("3", "value 6"));
        initialDataTreeModification.write(path, createEntry("3", "value 7"));
        initialDataTreeModification.write(path, createEntry("3", "value 6"));

        initialDataTreeModification.ready();
        inMemoryDataTree.validate(initialDataTreeModification);
        writeContributorsCandidate = inMemoryDataTree.prepare(initialDataTreeModification);
        inMemoryDataTree.commit(writeContributorsCandidate);
    }

    private void multipleMerge() throws DataValidationFailedException {
        DataTreeModification initialDataTreeModification;
        YangInstanceIdentifier path;
        DataTreeCandidate writeContributorsCandidate;
        initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        Builder<QName, Object> builder = ImmutableMap.builder();
        ImmutableMap<QName, Object> keys = builder.put(id, "6").build();
        NodeIdentifierWithPredicates entryPath = new YangInstanceIdentifier.NodeIdentifierWithPredicates(list1, keys);
        path = YangInstanceIdentifier.of(list1).node(entryPath);
        initialDataTreeModification.merge(path, createEntry("6", "value 6"));
        initialDataTreeModification.merge(path, createEntry("6", "value 7"));
        initialDataTreeModification.merge(path, createEntry("6", "value 8"));
        initialDataTreeModification.merge(path, createEntry("6", "value 7"));
        initialDataTreeModification.ready();
        inMemoryDataTree.validate(initialDataTreeModification);
        writeContributorsCandidate = inMemoryDataTree.prepare(initialDataTreeModification);
        inMemoryDataTree.commit(writeContributorsCandidate);
    }

    private void multipleMergeAndWrite() throws DataValidationFailedException {
        DataTreeModification initialDataTreeModification;
        YangInstanceIdentifier path;
        DataTreeCandidate writeContributorsCandidate;
        initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        Builder<QName, Object> builder = ImmutableMap.builder();
        ImmutableMap<QName, Object> keys = builder.put(id, "7").build();
        NodeIdentifierWithPredicates entryPath = new YangInstanceIdentifier.NodeIdentifierWithPredicates(list1, keys);
        path = YangInstanceIdentifier.of(list1).node(entryPath);
        initialDataTreeModification.write(path, createEntry("7", "value 7"));
        initialDataTreeModification.write(path, createEntry("7", "value 8"));
        initialDataTreeModification.write(path, createEntry("7", "value 9"));
        initialDataTreeModification.write(path, createEntry("7", "value 8"));
        initialDataTreeModification.merge(path, createEntry("7", "value 10"));
        initialDataTreeModification.merge(path, createEntry("7", "value 11"));
        initialDataTreeModification.merge(path, createEntry("7", "value 12"));
        initialDataTreeModification.merge(path, createEntry("7", "value 11"));
        initialDataTreeModification.ready();
        inMemoryDataTree.validate(initialDataTreeModification);
        writeContributorsCandidate = inMemoryDataTree.prepare(initialDataTreeModification);
        inMemoryDataTree.commit(writeContributorsCandidate);
    }

    private MapEntryNode createEntry(String keyValue, String leafValue) {
        return ImmutableNodes.mapEntryBuilder(list1, id, keyValue).withChild(ImmutableNodes.leafNode(leaf1, leafValue))
                .build();
    }

    private void writeWholeList() {

    }

    private void mergeWholeList() throws DataValidationFailedException {
        DataTreeModification initialDataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        MapNode list1Node = ImmutableNodes.mapNodeBuilder().withNodeIdentifier(NodeIdentifier.create(list1))
                .withChild(createEntry("1", "value 1")).withChild(createEntry("2", "value 2"))
                .withChild(createEntry("3", "value 3")).withChild(createEntry("4", "value 4"))
                .withChild(createEntry("5", "value 5")).withChild(createEntry("8", "value 8")).build();
        YangInstanceIdentifier path = YangInstanceIdentifier.of(list1);
        initialDataTreeModification.merge(path, list1Node);
        initialDataTreeModification.ready();

        inMemoryDataTree.validate(initialDataTreeModification);
        DataTreeCandidate writeContributorsCandidate = inMemoryDataTree.prepare(initialDataTreeModification);
        inMemoryDataTree.commit(writeContributorsCandidate);

        System.out.println("merge list: " + inMemoryDataTree);
    }
}
