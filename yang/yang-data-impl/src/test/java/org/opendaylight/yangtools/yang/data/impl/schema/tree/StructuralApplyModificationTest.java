/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertEquals;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

public final class StructuralApplyModificationTest extends AbstractTestModelTest {
    private DataTree inMemoryDataTree;

    @Before
    public void setUp() {
        inMemoryDataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION);
        inMemoryDataTree.setSchemaContext(SCHEMA_CONTEXT);
    }

    @Test
    public void testMapNodeParentAutoCreateDelete() throws DataValidationFailedException {
        final DataTreeModification addListEntryModification = inMemoryDataTree.takeSnapshot().newModification();

        // Prepare root
        final YangInstanceIdentifier.NodeIdentifier rootContainerId = getNId(TestModel.TEST_QNAME);
        addListEntryModification.write(YangInstanceIdentifier.create(rootContainerId),
            Builders.containerBuilder().withNodeIdentifier(rootContainerId).build());

        final YangInstanceIdentifier.NodeIdentifierWithPredicates outerListEntryId =
            new YangInstanceIdentifier.NodeIdentifierWithPredicates(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 1);

        // Write list entry (MapEntryNode) without creating list parent (MapNode)
        final MapEntryNode outerListEntry = Builders.mapEntryBuilder().withNodeIdentifier(outerListEntryId).build();
        final YangInstanceIdentifier outerListParentPath = YangInstanceIdentifier.create(getNId(TestModel.TEST_QNAME),
            getNId(TestModel.OUTER_LIST_QNAME));
        final YangInstanceIdentifier outerListEntryPath = outerListParentPath.node(outerListEntryId);
        addListEntryModification.write(outerListEntryPath, outerListEntry);

        addListEntryModification.ready();
        inMemoryDataTree.validate(addListEntryModification);
        inMemoryDataTree.commit(inMemoryDataTree.prepare(addListEntryModification));

        // Check list parent auto created
        assertNodeExistence(outerListParentPath, true);

        // Now delete
        final DataTreeModification deleteListEntryModification = inMemoryDataTree.takeSnapshot().newModification();
        deleteListEntryModification.delete(outerListEntryPath);
        deleteListEntryModification.ready();
        inMemoryDataTree.validate(deleteListEntryModification);
        inMemoryDataTree.commit(inMemoryDataTree.prepare(deleteListEntryModification));

        // Check list parent auto deleted
        assertNodeExistence(outerListParentPath, false);
    }

    @Test
    public void testMapNodeDirectEmptyWrite() {
        final DataTreeModification addListEntryModification = inMemoryDataTree.takeSnapshot().newModification();

        // Prepare root container
        final YangInstanceIdentifier.NodeIdentifier rootContainerId = getNId(TestModel.TEST_QNAME);
        addListEntryModification.write(YangInstanceIdentifier.create(rootContainerId),
            Builders.containerBuilder().withNodeIdentifier(rootContainerId).build());

        final YangInstanceIdentifier outerListParentPath = YangInstanceIdentifier.create(getNId(TestModel.TEST_QNAME),
            getNId(TestModel.OUTER_LIST_QNAME));
        addListEntryModification.merge(outerListParentPath, ImmutableNodes.mapNode(TestModel.OUTER_LIST_QNAME));

        // Check empty map node auto deleted
        assertNodeExistence(outerListParentPath, false);
    }

    @Test
    public void testNonPresenceContainerDirectEmptyWrite() throws DataValidationFailedException {
        final DataTreeModification addListEntryModification = inMemoryDataTree.takeSnapshot().newModification();

        final YangInstanceIdentifier.NodeIdentifier rootContainerId = getNId(TestModel.NON_PRESENCE_QNAME);
        final YangInstanceIdentifier path = YangInstanceIdentifier.create(rootContainerId);
        addListEntryModification.write(path, Builders.containerBuilder().withNodeIdentifier(rootContainerId).build());

        addListEntryModification.ready();
        inMemoryDataTree.validate(addListEntryModification);
        inMemoryDataTree.commit(inMemoryDataTree.prepare(addListEntryModification));

        // Check empty container auto deleted
        assertNodeExistence(path, false);
    }

    @Test
    public void testNestedStrucutralNodes() throws DataValidationFailedException {
        final DataTreeModification addListEntryModification = inMemoryDataTree.takeSnapshot().newModification();

        final YangInstanceIdentifier path = YangInstanceIdentifier.create(
            getNId(TestModel.NON_PRESENCE_QNAME),
            getNId(TestModel.DEEP_CHOICE_QNAME),
            getNId(TestModel.A_LIST_QNAME),
            getNId(TestModel.A_LIST_QNAME, TestModel.A_NAME_QNAME, "1")
        );

        addListEntryModification.write(path,
            Builders.mapEntryBuilder()
                .withNodeIdentifier(getNId(TestModel.A_LIST_QNAME, TestModel.A_NAME_QNAME, "1"))
                .build());

        addListEntryModification.ready();
        inMemoryDataTree.validate(addListEntryModification);
        inMemoryDataTree.commit(inMemoryDataTree.prepare(addListEntryModification));

        // Check parent structure auto created
        assertNodeExistence(path, true);
        assertNodeExistence(YangInstanceIdentifier.create(getNId(TestModel.NON_PRESENCE_QNAME)), true);
        assertNodeExistence(YangInstanceIdentifier.create(
            getNId(TestModel.NON_PRESENCE_QNAME), getNId(TestModel.DEEP_CHOICE_QNAME)), true);
    }

    private void assertNodeExistence(final YangInstanceIdentifier outerListParentPath, final boolean shouldBePresent) {
        final DataTreeSnapshot snapshotAfterCommits = inMemoryDataTree.takeSnapshot();
        final Optional<NormalizedNode<?, ?>> readNode = snapshotAfterCommits.readNode(outerListParentPath);
        assertEquals(readNode.isPresent(), shouldBePresent);
    }

    private static NodeIdentifier getNId(final QName qname) {
        return YangInstanceIdentifier.NodeIdentifier.create(qname);
    }

    private static NodeIdentifierWithPredicates getNId(final QName qname, final QName key, final String val) {
        return new NodeIdentifierWithPredicates(qname, key, val);
    }
}
