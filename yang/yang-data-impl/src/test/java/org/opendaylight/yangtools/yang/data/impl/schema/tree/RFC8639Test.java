/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class RFC8639Test {
    private static final QName MODULE = QName.create("urn:ietf:params:xml:ns:yang:ietf-subscribed-notifications",
        "2019-09-09", "sn");
    private static final NodeIdentifier SUBSCRIPTIONS = new NodeIdentifier(QName.create(MODULE, "subscriptions"));
    private static final ContainerNode DATA = prepareData("stream-name", "subscriber");

    private static ContainerNode prepareData(final String streamName, final String subscriber) {
        return Builders.containerBuilder()
                .withNodeIdentifier(SUBSCRIPTIONS)
                .withChild(Builders.mapBuilder()
                    .withNodeIdentifier(NodeIdentifier.create(
                        QName.create(MODULE, "subscription")))
                    .withChild(Builders.mapEntryBuilder()
                        .withNodeIdentifier(NodeIdentifierWithPredicates.of(
                            QName.create(MODULE, "subscription"), QName.create(MODULE, "id"), Uint32.ZERO))
                        .withChild(ImmutableNodes.leafNode(QName.create(MODULE, "encoding"), "encode-json"))
                        .withChild(Builders.choiceBuilder()
                            .withNodeIdentifier(NodeIdentifier.create(QName.create(MODULE, "target")))
                            .withChild(ImmutableNodes.leafNode(QName.create(MODULE, "stream"), streamName))
                            .build())
                        .withChild(Builders.containerBuilder()
                            .withNodeIdentifier(NodeIdentifier.create(QName.create(MODULE, "receivers")))
                            .withChild(Builders.mapBuilder()
                                .withNodeIdentifier(NodeIdentifier.create(QName.create(MODULE, "receiver")))
                                .withChild(Builders.mapEntryBuilder()
                                    .withNodeIdentifier(NodeIdentifierWithPredicates.of(
                                        QName.create(MODULE, "receiver"), QName.create(MODULE, "name"), subscriber))
                                    .withChild(ImmutableNodes.leafNode(QName.create(MODULE, "name"), subscriber))
                                    // config=false
                                    .withChild(ImmutableNodes.leafNode(QName.create(MODULE, "state"), "active"))
                                    .build())
                                .build())
                            .build())
                        .build())
                    .build())
                .build();
    }

    private static SchemaContext SCHEMA_CONTEXT;

    private DataTree dataTree;

    @BeforeClass
    public static void beforeClass() {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYangResourceDirectory("/rfc8639");
    }

    @AfterClass
    public static void afterClass() {
        SCHEMA_CONTEXT = null;
    }

    @Before
    public void init() {
        dataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_OPERATIONAL, SCHEMA_CONTEXT);
    }

    @Test
    public void testWriteData() throws Exception {
        commit(write(DATA));
    }

    @Test
    public void testMergeData() throws Exception {
        commit(merge(DATA));
    }

    private DataTreeModification write(final ContainerNode data) {
        final DataTreeModification mod = dataTree.takeSnapshot().newModification();
        mod.write(YangInstanceIdentifier.create(SUBSCRIPTIONS), data);
        return mod;
    }

    private DataTreeModification merge(final ContainerNode data) {
        final DataTreeModification mod = dataTree.takeSnapshot().newModification();
        mod.merge(YangInstanceIdentifier.create(SUBSCRIPTIONS), data);
        return mod;
    }

    private void commit(final DataTreeModification mod) throws DataValidationFailedException {
        mod.ready();
        dataTree.validate(mod);
        dataTree.commit(dataTree.prepare(mod));
    }
}
