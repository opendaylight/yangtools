/*
 * Copyright (c) 2018 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.leafref;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT821Test {
    private static final QName ROOT = QName.create("urn:opendaylight:params:xml:ns:yang:foo", "2018-07-18", "root");
    private static final QName FOO = QName.create(ROOT, "foo");
    private static final QName BAR = QName.create(ROOT, "bar");
    private static final QName NAME = QName.create(ROOT, "name");
    private static final QName CONTAINER_IN_LIST = QName.create(ROOT, "container-in-list");
    private static final QName REF_FROM_AUG = QName.create(ROOT, "ref-from-aug");
    private static final QName CONTAINER_FROM_AUG = QName.create(ROOT, "container-from-aug");
    private static final QName REF_IN_CONTAINER = QName.create(ROOT, "ref-in-container");
    private static final YangInstanceIdentifier ROOT_ID = YangInstanceIdentifier.of(ROOT);

    private static EffectiveModelContext schemaContext;
    private static LeafRefContext leafRefContext;

    private DataTree dataTree;

    @BeforeClass
    public static void beforeClass() {
        schemaContext = YangParserTestUtils.parseYangResource("/yt821.yang");
        leafRefContext = LeafRefContext.create(schemaContext);
    }

    @AfterClass
    public static void afterClass() {
        schemaContext = null;
        leafRefContext = null;
    }

    @Before
    public void before() {
        dataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION, schemaContext);
    }

    @Test
    public void testValidRefFromAugmentation() throws Exception {
        final DataTreeModification writeModification = dataTree.takeSnapshot().newModification();
        writeModification.write(ROOT_ID, refFromAug("foo1"));
        writeModification.ready();
        final DataTreeCandidate writeContributorsCandidate = dataTree.prepare(writeModification);

        LeafRefValidation.validate(writeContributorsCandidate, leafRefContext);
        dataTree.commit(writeContributorsCandidate);
    }

    @Test(expected = LeafRefDataValidationFailedException.class)
    public void testInvalidRefFromAugmentation() throws Exception {
        final DataTreeModification writeModification = dataTree.takeSnapshot().newModification();
        writeModification.write(ROOT_ID, refFromAug("foo2"));
        writeModification.ready();
        final DataTreeCandidate writeContributorsCandidate = dataTree.prepare(writeModification);

        LeafRefValidation.validate(writeContributorsCandidate, leafRefContext);
    }

    @Test
    public void testValidRefInContainerFromAugmentation() throws Exception {
        final DataTreeModification writeModification = dataTree.takeSnapshot().newModification();
        writeModification.write(ROOT_ID, refInContainer("foo1"));
        writeModification.ready();
        final DataTreeCandidate writeContributorsCandidate = dataTree.prepare(writeModification);

        LeafRefValidation.validate(writeContributorsCandidate, leafRefContext);
        dataTree.commit(writeContributorsCandidate);
    }

    @Test(expected = LeafRefDataValidationFailedException.class)
    public void testInvalidRefInContainerFromAugmentation() throws Exception {
        final DataTreeModification writeModification = dataTree.takeSnapshot().newModification();
        writeModification.write(ROOT_ID, refInContainer("foo2"));
        writeModification.ready();
        final DataTreeCandidate writeContributorsCandidate = dataTree.prepare(writeModification);

        LeafRefValidation.validate(writeContributorsCandidate, leafRefContext);
    }

    private static ContainerNode refFromAug(final String refValue) {
        return Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(ROOT))
                .withChild(Builders.mapBuilder()
                    .withNodeIdentifier(new NodeIdentifier(FOO))
                    .withChild(Builders.mapEntryBuilder()
                        .withNodeIdentifier(NodeIdentifierWithPredicates.of(FOO, NAME, "foo1"))
                        .withChild(ImmutableNodes.leafNode(NAME, "foo1"))
                        .build())
                    .build())
                .withChild(Builders.mapBuilder()
                    .withNodeIdentifier(new NodeIdentifier(BAR))
                    .withChild(Builders.mapEntryBuilder()
                        .withNodeIdentifier(NodeIdentifierWithPredicates.of(BAR, NAME, "bar1"))
                        .withChild(ImmutableNodes.leafNode(NAME, "bar1"))
                        .withChild(Builders.containerBuilder()
                            .withNodeIdentifier(new NodeIdentifier(CONTAINER_IN_LIST))
                            .withChild(ImmutableNodes.leafNode(REF_FROM_AUG, refValue))
                            .build())
                        .build())
                    .build())
                .build();
    }

    private static ContainerNode refInContainer(final String refValue) {
        return Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(ROOT))
                .withChild(Builders.mapBuilder()
                    .withNodeIdentifier(new NodeIdentifier(FOO))
                    .withChild(Builders.mapEntryBuilder()
                        .withNodeIdentifier(NodeIdentifierWithPredicates.of(FOO, NAME, "foo1"))
                        .withChild(ImmutableNodes.leafNode(NAME, "foo1"))
                        .build())
                    .build())
                .withChild(Builders.mapBuilder()
                    .withNodeIdentifier(new NodeIdentifier(BAR))
                    .withChild(Builders.mapEntryBuilder()
                        .withNodeIdentifier(NodeIdentifierWithPredicates.of(BAR, NAME, "bar1"))
                        .withChild(ImmutableNodes.leafNode(NAME, "bar1"))
                        .withChild(Builders.containerBuilder()
                            .withNodeIdentifier(new NodeIdentifier(CONTAINER_FROM_AUG))
                            .withChild(ImmutableNodes.leafNode(REF_IN_CONTAINER, refValue))
                            .build())
                        .build())
                    .build())
                .build();
    }
}
