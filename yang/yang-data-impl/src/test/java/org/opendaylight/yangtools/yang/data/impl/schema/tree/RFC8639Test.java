/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.collect.Sets;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class RFC8639Test {
    private static final QName MODULE = QName.create(
            "urn:ietf:params:xml:ns:yang:ietf-subscribed-notifications",
            "sn"
    );
    private static final NodeIdentifier SUBSCRIPTIONS = new NodeIdentifier(
            QName.create(MODULE, "subscriptions")
    );
    private static final ContainerNode DATA = prepareData("stream-name", "subscriber");

    private static ContainerNode prepareData(final String streamName, final String subscriber) {
        final DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> subscriptionEntry = Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(
                        QName.create(MODULE, "subscription"),
                        QName.create(MODULE, "id"),
                        Uint32.valueOf(0)
                ))
                .withChild(Builders.leafBuilder()
                        .withNodeIdentifier(NodeIdentifier.create(QName.create(MODULE, "encoding")))
                        .withValue("encode-json")
                        .build()
                );

        subscriptionEntry.withChild(
                Builders.choiceBuilder()
                        .withNodeIdentifier(NodeIdentifier.create(QName.create(MODULE, "target")))
                        .withChild(
                                Builders.augmentationBuilder()
                                        .withNodeIdentifier(AugmentationIdentifier.create(
                                                Sets.newHashSet(QName.create(MODULE, "stream"))
                                        ))
                                        .withChild(Builders.leafBuilder()
                                                .withNodeIdentifier(NodeIdentifier.create(QName.create(MODULE, "stream")))
                                                .withValue(streamName)
                                                .build())
                                        .build()
                        )
                        .build()
        );

        final DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> receiverEntry = Builders
                .mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(
                        QName.create(MODULE, "receiver"),
                        QName.create(MODULE, "name"),
                        subscriber
                        )
                )
                .withChild(Builders.leafBuilder()
                        .withNodeIdentifier(NodeIdentifier.create(
                                QName.create(MODULE, "state"))
                        )
                        .withValue("active")
                        .build()
                );

        subscriptionEntry.withChild(Builders.containerBuilder()
                .withNodeIdentifier(NodeIdentifier.create(QName.create(MODULE, "receivers")))
                .withChild(Builders.mapBuilder()
                        .withNodeIdentifier(NodeIdentifier.create(QName.create(MODULE, "receiver")))
                        .withChild(receiverEntry.build())
                        .build())
                .build());

        return Builders.containerBuilder()
                .withNodeIdentifier(SUBSCRIPTIONS)
                .withChild(Builders.mapBuilder()
                        .withNodeIdentifier(NodeIdentifier.create(QName.create(MODULE, "subscription")))
                        .withChild(subscriptionEntry.build())
                        .build())
                .build();
    }

    private static SchemaContext SCHEMA_CONTEXT;

    private DataTree dataTree;

    @BeforeClass
    public static void beforeClass() {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYangResources(
                RFC8639Test.class,
                "/rfc8639/ietf-inet-types@2013-07-15.yang",
                "/rfc8639/ietf-interfaces@2018-02-20.yang",
                "/rfc8639/ietf-ip@2018-02-22.yang",
                "/rfc8639/ietf-netconf-acm@2018-02-14.yang",
                "/rfc8639/ietf-network-instance@2019-01-21.yang",
                "/rfc8639/ietf-restconf@2017-01-26.yang",
                "/rfc8639/ietf-subscribed-notifications@2019-09-09.yang",
                "/rfc8639/ietf-yang-schema-mount@2019-01-14.yang",
                "/rfc8639/ietf-yang-types@2013-07-15.yang"
        );
    }

    @AfterClass
    public static void afterClass() {
        SCHEMA_CONTEXT = null;
    }

    @Before
    public void init() {
        dataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION, SCHEMA_CONTEXT);
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
