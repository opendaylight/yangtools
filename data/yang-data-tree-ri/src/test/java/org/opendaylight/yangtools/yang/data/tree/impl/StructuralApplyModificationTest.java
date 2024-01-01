/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;

final class StructuralApplyModificationTest extends AbstractTestModelTest {
    private DataTree inMemoryDataTree;

    @BeforeEach
    void setUp() {
        inMemoryDataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION);
        inMemoryDataTree.setEffectiveModelContext(SCHEMA_CONTEXT);
    }

    @Test
    void testMapNodeParentAutoCreateDelete() throws DataValidationFailedException {
        final var addListEntryModification = inMemoryDataTree.takeSnapshot().newModification();

        // Prepare root
        final var rootContainerId = getNId(TestModel.TEST_QNAME);
        addListEntryModification.write(YangInstanceIdentifier.of(rootContainerId),
            ImmutableNodes.newContainerBuilder().withNodeIdentifier(rootContainerId).build());

        final var outerListEntryId = NodeIdentifierWithPredicates.of(
            TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 1);

        // Write list entry (MapEntryNode) without creating list parent (MapNode)
        final var outerListEntry = ImmutableNodes.newMapEntryBuilder().withNodeIdentifier(outerListEntryId).build();
        final var outerListParentPath = YangInstanceIdentifier.of(getNId(TestModel.TEST_QNAME),
            getNId(TestModel.OUTER_LIST_QNAME));
        final var outerListEntryPath = outerListParentPath.node(outerListEntryId);
        addListEntryModification.write(outerListEntryPath, outerListEntry);

        addListEntryModification.ready();
        inMemoryDataTree.validate(addListEntryModification);
        inMemoryDataTree.commit(inMemoryDataTree.prepare(addListEntryModification));

        // Check list parent auto created
        assertNodeExistence(outerListParentPath, true);

        // Now delete
        final var deleteListEntryModification = inMemoryDataTree.takeSnapshot().newModification();
        deleteListEntryModification.delete(outerListEntryPath);
        deleteListEntryModification.ready();
        inMemoryDataTree.validate(deleteListEntryModification);
        inMemoryDataTree.commit(inMemoryDataTree.prepare(deleteListEntryModification));

        // Check list parent auto deleted
        assertNodeExistence(outerListParentPath, false);
    }

    @Test
    void testMapNodeDirectEmptyWrite() {
        final var addListEntryModification = inMemoryDataTree.takeSnapshot().newModification();

        // Prepare root container
        final var rootContainerId = getNId(TestModel.TEST_QNAME);
        addListEntryModification.write(YangInstanceIdentifier.of(rootContainerId),
            ImmutableNodes.newContainerBuilder().withNodeIdentifier(rootContainerId).build());

        final var outerListParentPath = YangInstanceIdentifier.of(getNId(TestModel.TEST_QNAME),
            getNId(TestModel.OUTER_LIST_QNAME));
        addListEntryModification.merge(outerListParentPath, ImmutableNodes.newSystemMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
            .build());

        // Check empty map node auto deleted
        assertNodeExistence(outerListParentPath, false);
    }

    @Test
    void testNonPresenceContainerDirectEmptyWrite() throws DataValidationFailedException {
        final var addListEntryModification = inMemoryDataTree.takeSnapshot().newModification();

        final var rootContainerId = getNId(TestModel.NON_PRESENCE_QNAME);
        final var path = YangInstanceIdentifier.of(rootContainerId);
        addListEntryModification.write(path, ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(rootContainerId)
            .build());

        addListEntryModification.ready();
        inMemoryDataTree.validate(addListEntryModification);
        inMemoryDataTree.commit(inMemoryDataTree.prepare(addListEntryModification));

        // Check empty container auto deleted
        assertNodeExistence(path, false);
    }

    @Test
    void testNestedStrucutralNodes() throws DataValidationFailedException {
        final var addListEntryModification = inMemoryDataTree.takeSnapshot().newModification();

        final var path = TestModel.DEEP_CHOICE_PATH.node(TestModel.A_LIST_QNAME)
            .node(getNId(TestModel.A_LIST_QNAME, TestModel.A_NAME_QNAME, "1"));

        addListEntryModification.write(path,
            ImmutableNodes.newMapEntryBuilder()
                .withNodeIdentifier(getNId(TestModel.A_LIST_QNAME, TestModel.A_NAME_QNAME, "1"))
                .build());

        addListEntryModification.ready();
        inMemoryDataTree.validate(addListEntryModification);
        inMemoryDataTree.commit(inMemoryDataTree.prepare(addListEntryModification));

        // Check parent structure auto created
        assertNodeExistence(path, true);
        assertNodeExistence(TestModel.NON_PRESENCE_PATH, true);
        assertNodeExistence(TestModel.DEEP_CHOICE_PATH, true);
    }

    private void assertNodeExistence(final YangInstanceIdentifier outerListParentPath, final boolean shouldBePresent) {
        final var snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        final var readNode = snapshotAfterCommits.readNode(outerListParentPath);
        assertEquals(readNode.isPresent(), shouldBePresent);
    }

    private static NodeIdentifier getNId(final QName qname) {
        return YangInstanceIdentifier.NodeIdentifier.create(qname);
    }

    private static NodeIdentifierWithPredicates getNId(final QName qname, final QName key, final String val) {
        return NodeIdentifierWithPredicates.of(qname, key, val);
    }
}
