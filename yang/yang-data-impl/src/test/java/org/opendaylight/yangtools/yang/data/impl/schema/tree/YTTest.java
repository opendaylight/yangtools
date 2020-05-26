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
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class YTTest {
    private static final QName MODULE = QName.create("yt", "yt");
    private static final NodeIdentifier SUBSCRIPTIONS = NodeIdentifier.create(QName.create(MODULE, "subscriptions"));
    private static final QName SUBSCRIPTION_QNAME = QName.create(MODULE, "subscription");
    private static final QName STREAM_QNAME = QName.create(MODULE, "stream");
    private static final ContainerNode DATA = Builders.containerBuilder()
                    .withNodeIdentifier(SUBSCRIPTIONS)
                    .withChild(Builders.mapBuilder()
                            .withNodeIdentifier(NodeIdentifier.create(SUBSCRIPTION_QNAME))
                            .withChild(Builders.mapEntryBuilder()
                                    .withNodeIdentifier(NodeIdentifierWithPredicates.of(
                                            SUBSCRIPTION_QNAME,
                                            QName.create(MODULE, "id"),
                                            Uint32.valueOf(0)
                                    ))
                                    .withChild(Builders.choiceBuilder()
                                            .withNodeIdentifier(NodeIdentifier.create(QName.create(MODULE, "target")))
                                            .withChild(Builders.augmentationBuilder()
                                                    .withNodeIdentifier(AugmentationIdentifier.create(
                                                            Sets.newHashSet(STREAM_QNAME)
                                                    ))
                                                    .withChild(Builders.leafBuilder()
                                                            .withNodeIdentifier(NodeIdentifier.create(STREAM_QNAME))
                                                            .withValue("stream-name")
                                                            .build())
                                                    .build())
                                            .build())
                                    .build())
                            .build())
                    .build();

    private static SchemaContext SCHEMA_CONTEXT;
    private DataTree dataTree;

    @BeforeClass
    public static void beforeClass() {
        SCHEMA_CONTEXT = TestModel.createTestContext("/yt/yt.yang");
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
    public void testWriteWriteContainer() throws Exception {
        commit(write(DATA));
    }

    @Test
    public void testMergeWriteContainer() throws Exception {
        commit(merge(DATA));
    }

    private DataTreeModification write(final ContainerNode data) {
        final DataTreeModification mod = dataTree.takeSnapshot().newModification();
        mod.write(YangInstanceIdentifier.create(SUBSCRIPTIONS), data);
        return mod;
    }

    private DataTreeModification merge(final ContainerNode data) {
        final DataTreeModification mod = dataTree.takeSnapshot().newModification();
        mod.write(YangInstanceIdentifier.create(SUBSCRIPTIONS), data);
        return mod;
    }

    private void commit(final DataTreeModification mod) throws DataValidationFailedException {
        mod.ready();
        dataTree.validate(mod);
        dataTree.commit(dataTree.prepare(mod));
    }
}
