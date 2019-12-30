/*
 * Copyright (c) 2018 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefContext;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefDataValidationFailedException;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefValidation;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Yangtools891Test {
    private static final QName FOO_TOP = QName.create("urn:opendaylight:params:xml:ns:yang:foo", "2018-07-27",
        "foo-top");
    private static final QName CONTAINER_IN_LIST = QName.create(FOO_TOP, "container-in-list");
    private static final QName LIST_IN_GROUPING = QName.create(FOO_TOP, "list-in-grouping");
    private static final QName NAME = QName.create(FOO_TOP, "name");
    private static final QName REF = QName.create(FOO_TOP, "ref");
    private static final YangInstanceIdentifier FOO_TOP_ID = YangInstanceIdentifier.of(FOO_TOP);
    private static final QName BAZ_TOP = QName.create("urn:opendaylight:params:xml:ns:yang:baz", "2018-07-27",
        "baz-top");
    private static final QName BAZ_NAME = QName.create(BAZ_TOP, "name");
    private static final QName LIST_IN_CONTAINER = QName.create(BAZ_TOP, "list-in-container");
    private static final YangInstanceIdentifier BAZ_TOP_ID = YangInstanceIdentifier.of(BAZ_TOP);

    private static SchemaContext schemaContext;
    private static LeafRefContext leafRefContext;

    private DataTree dataTree;

    @Before
    public void before() {
        dataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION, schemaContext);
    }

    @BeforeClass
    public static void beforeClass() {
        schemaContext = YangParserTestUtils.parseYangResourceDirectory("/yangtools891");
        leafRefContext = LeafRefContext.create(schemaContext);
    }

    @AfterClass
    public static void afterClass() {
        schemaContext = null;
        leafRefContext = null;
    }

    @Test
    public void testValid() throws Exception {
        final DataTreeModification writeModification = dataTree.takeSnapshot().newModification();
        writeModification.write(FOO_TOP_ID, Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(FOO_TOP))
            .withChild(Builders.mapBuilder()
                .withNodeIdentifier(new NodeIdentifier(LIST_IN_GROUPING))
                .withChild(Builders.mapEntryBuilder()
                    .withNodeIdentifier(NodeIdentifierWithPredicates.of(LIST_IN_GROUPING, NAME, "name1"))
                    .withChild(ImmutableNodes.leafNode(NAME, "name1"))
                    .withChild(Builders.containerBuilder()
                        .withNodeIdentifier(new NodeIdentifier(CONTAINER_IN_LIST))
                        .withChild(ImmutableNodes.leafNode(NAME, "name1"))
                        .build())
                    .build())
                .build())
            .build());
        writeModification.ready();
        final DataTreeCandidate writeContributorsCandidate = dataTree.prepare(writeModification);

        LeafRefValidation.validate(writeContributorsCandidate, leafRefContext);
        dataTree.commit(writeContributorsCandidate);
    }

    @Test(expected = LeafRefDataValidationFailedException.class)
    public void testInvalid() throws Exception {
        final DataTreeModification writeModification = dataTree.takeSnapshot().newModification();
        writeModification.write(FOO_TOP_ID, Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(FOO_TOP))
            .withChild(Builders.mapBuilder()
                .withNodeIdentifier(new NodeIdentifier(LIST_IN_GROUPING))
                .withChild(Builders.mapEntryBuilder()
                    .withNodeIdentifier(NodeIdentifierWithPredicates.of(LIST_IN_GROUPING, NAME, "name1"))
                    .withChild(ImmutableNodes.leafNode(NAME, "name1"))
                    .withChild(Builders.containerBuilder()
                        .withNodeIdentifier(new NodeIdentifier(CONTAINER_IN_LIST))
                        .withChild(ImmutableNodes.leafNode(NAME, "name2"))
                        .build())
                    .build())
                .build())
            .build());
        writeModification.ready();
        final DataTreeCandidate writeContributorsCandidate = dataTree.prepare(writeModification);

        LeafRefValidation.validate(writeContributorsCandidate, leafRefContext);
    }

    @Test
    public void testGroupingWithLeafrefValid() throws Exception {
        final DataTreeModification writeModification = dataTree.takeSnapshot().newModification();
        writeModification.write(BAZ_TOP_ID, bazTop());
        writeModification.write(FOO_TOP_ID, Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(FOO_TOP))
            .withChild(ImmutableNodes.leafNode(REF, "name1"))
            .build());
        writeModification.ready();
        final DataTreeCandidate writeContributorsCandidate = dataTree.prepare(writeModification);

        LeafRefValidation.validate(writeContributorsCandidate, leafRefContext);
    }

    @Test(expected = LeafRefDataValidationFailedException.class)
    public void testGroupingWithLeafrefInvalid() throws Exception {
        final DataTreeModification writeModification = dataTree.takeSnapshot().newModification();
        writeModification.write(BAZ_TOP_ID, bazTop());
        writeModification.write(FOO_TOP_ID, Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(FOO_TOP))
            .withChild(ImmutableNodes.leafNode(REF, "name3"))
            .build());
        writeModification.ready();
        final DataTreeCandidate writeContributorsCandidate = dataTree.prepare(writeModification);

        LeafRefValidation.validate(writeContributorsCandidate, leafRefContext);
    }

    private static ContainerNode bazTop() {
        return Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(BAZ_TOP))
                .withChild(Builders.mapBuilder()
                    .withNodeIdentifier(new NodeIdentifier(LIST_IN_CONTAINER))
                    .withChild(Builders.mapEntryBuilder()
                        .withNodeIdentifier(NodeIdentifierWithPredicates.of(LIST_IN_CONTAINER, BAZ_NAME, "name1"))
                        .withChild(ImmutableNodes.leafNode(BAZ_NAME, "name1"))
                        .build())
                    .build())
                .build();
    }
}
