/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Collections;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class InstanceIdToNodesTest {

    private static final String NS = "urn:opendaylight:params:xml:ns:yang:controller:md:sal:normalization:test";
    private static final String REVISION = "2014-03-13";
    private static final QName ID = QName.create(NS, REVISION, "id");
    private static SchemaContext ctx;

    private final NodeIdentifier rootContainer = new NodeIdentifier(QName.create(NS, REVISION, "test"));
    private final NodeIdentifier outerContainer = new NodeIdentifier(QName.create(NS, REVISION, "outer-container"));
    private final NodeIdentifier augmentedLeaf = new NodeIdentifier(QName.create(NS, REVISION, "augmented-leaf"));
    private final AugmentationIdentifier augmentation = new AugmentationIdentifier(
            Collections.singleton(augmentedLeaf.getNodeType()));

    private final NodeIdentifier outerList = new NodeIdentifier(
            QName.create(NS, REVISION, "outer-list"));
    private final NodeIdentifierWithPredicates outerListWithKey = new NodeIdentifierWithPredicates(
            QName.create(NS, REVISION, "outer-list"), ID, 1);
    private final NodeIdentifier choice = new NodeIdentifier(QName.create(NS, REVISION, "outer-choice"));
    private final NodeIdentifier leafFromCase = new NodeIdentifier(QName.create(NS, REVISION, "one"));

    private final NodeIdentifier leafList = new NodeIdentifier(QName.create(NS, REVISION, "ordered-leaf-list"));
    private final NodeWithValue<?> leafListWithValue = new NodeWithValue<>(
            leafList.getNodeType(), "abcd");

    static SchemaContext createTestContext() throws URISyntaxException, FileNotFoundException, ReactorException {
        final File resourceFile = new File(InstanceIdToNodesTest.class.getResource("/filter-test.yang").toURI());
        return YangParserTestUtils.parseYangSources(resourceFile);
    }

    @BeforeClass
    public static void setUp() throws Exception {
        ctx = createTestContext();

    }

    @Test
    public void testInAugment() throws Exception {
        final ContainerNode expectedFilter = Builders
                .containerBuilder()
                .withNodeIdentifier(rootContainer)
                .withChild(
                        Builders.containerBuilder()
                                .withNodeIdentifier(outerContainer)
                                .withChild(
                                        Builders.augmentationBuilder()
                                                .withNodeIdentifier(augmentation)
                                                .withChild(
                                                        Builders.leafBuilder().withNodeIdentifier(augmentedLeaf)
                                                                .build()).build()).build()).build();

        final NormalizedNode<?, ?> filter = ImmutableNodes.fromInstanceId(ctx,
                YangInstanceIdentifier.create(rootContainer, outerContainer, augmentation, augmentedLeaf));
        assertEquals(expectedFilter, filter);
    }

    @Test
    public void testInAugmentLeafOverride() throws Exception {
        final LeafNode<Object> lastLeaf = Builders.leafBuilder().withNodeIdentifier(augmentedLeaf)
                .withValue("randomValue").build();

        final ContainerNode expectedFilter = Builders
                .containerBuilder()
                .withNodeIdentifier(rootContainer)
                .withChild(
                        Builders.containerBuilder()
                                .withNodeIdentifier(outerContainer)
                                .withChild(
                                        Builders.augmentationBuilder().withNodeIdentifier(augmentation)
                                                .withChild(lastLeaf).build()).build()).build();

        final NormalizedNode<?, ?> filter = ImmutableNodes.fromInstanceId(ctx,
                YangInstanceIdentifier.create(rootContainer, outerContainer, augmentation, augmentedLeaf), lastLeaf);
        assertEquals(expectedFilter, filter);
    }

    @Test
    public void testListChoice() throws Exception {
        final ContainerNode expectedFilter = Builders
                .containerBuilder()
                .withNodeIdentifier(rootContainer)
                .withChild(
                        Builders.mapBuilder()
                                .withNodeIdentifier(outerList)
                                .withChild(
                                        Builders.mapEntryBuilder()
                                                .withNodeIdentifier(outerListWithKey)
                                                .withChild(
                                                        Builders.leafBuilder()
                                                                .withNodeIdentifier(
                                                                        new NodeIdentifier(ID))
                                                                .withValue(1).build())
                                                .withChild(
                                                        Builders.choiceBuilder()
                                                                .withNodeIdentifier(choice)
                                                                .withChild(
                                                                        Builders.leafBuilder()
                                                                                .withNodeIdentifier(leafFromCase)
                                                                                .build()).build()).build()).build())
                .build();

        final NormalizedNode<?, ?> filter = ImmutableNodes.fromInstanceId(ctx,
                YangInstanceIdentifier.create(rootContainer, outerList, outerListWithKey, choice, leafFromCase));
        assertEquals(expectedFilter, filter);
    }

    @Test
    public void testTopContainerLastChildOverride() throws Exception {
        final ContainerNode expectedStructure = Builders
                .containerBuilder()
                .withNodeIdentifier(rootContainer)
                .withChild(
                        Builders.mapBuilder()
                                .withNodeIdentifier(outerList)
                                .withChild(
                                        Builders.mapEntryBuilder()
                                                .withNodeIdentifier(outerListWithKey)
                                                .withChild(
                                                        Builders.leafBuilder()
                                                                .withNodeIdentifier(
                                                                        new NodeIdentifier(ID))
                                                                .withValue(1).build())
                                                .withChild(
                                                        Builders.choiceBuilder()
                                                                .withNodeIdentifier(choice)
                                                                .withChild(
                                                                        Builders.leafBuilder()
                                                                                .withNodeIdentifier(leafFromCase)
                                                                                .build()).build()).build()).build())
                .build();

        final NormalizedNode<?, ?> filter = ImmutableNodes.fromInstanceId(ctx,
                YangInstanceIdentifier.create(rootContainer), expectedStructure);
        assertEquals(expectedStructure, filter);
    }

    @Test
    public void testListLastChildOverride() throws Exception {
        final MapEntryNode outerListEntry = Builders
                .mapEntryBuilder()
                .withNodeIdentifier(outerListWithKey)
                .withChild(
                        Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(ID))
                                .withValue(1).build()).build();
        final MapNode lastChild = Builders.mapBuilder().withNodeIdentifier(this.outerList).withChild(outerListEntry)
                .build();
        final ContainerNode expectedStructure = Builders.containerBuilder().withNodeIdentifier(rootContainer)
                .withChild(lastChild).build();

        NormalizedNode<?, ?> filter = ImmutableNodes.fromInstanceId(ctx,
                YangInstanceIdentifier.create(rootContainer, outerList, outerListWithKey), outerListEntry);
        assertEquals(expectedStructure, filter);
        filter = ImmutableNodes.fromInstanceId(ctx,
                YangInstanceIdentifier.create(rootContainer, outerList, outerListWithKey));
        assertEquals(expectedStructure, filter);
    }

    @Test
    public void testLeafList() throws Exception {
        final ContainerNode expectedFilter = Builders
                .containerBuilder()
                .withNodeIdentifier(rootContainer)
                .withChild(
                        Builders.orderedLeafSetBuilder()
                                .withNodeIdentifier(leafList)
                                .withChild(
                                        Builders.leafSetEntryBuilder().withNodeIdentifier(leafListWithValue)
                                                .withValue(leafListWithValue.getValue()).build()).build()).build();

        final NormalizedNode<?, ?> filter = ImmutableNodes.fromInstanceId(ctx,
                YangInstanceIdentifier.create(rootContainer, leafList, leafListWithValue));
        assertEquals(expectedFilter, filter);
    }
}