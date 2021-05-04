/*
 * Copyright (c) 2021 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class ChoiceAugmentationTest {

    private static final String TEST_A_TO_Z_ID = "test-aToZ-id";
    private static final String TEST_SERVICE_NAME = "test-service-name";

    private static final QName TOP_SERVICE_QNAME = QName
        .create("dummy:org-openroadm-service", "2021-04-26", "top-service");
    private static final QName ETHERNET_CSMACD_QNAME = QName
        .create("dummy:org-openroadm-interfaces", "2021-04-26", "ethernetCsmacd");

    private static final QName TOPOLOGY_QNAME = QName.create(TOP_SERVICE_QNAME, "topology");
    private static final QName RESOURCE_QNAME = QName.create(TOP_SERVICE_QNAME, "resource");
    private static final QName ETHERNET_CONT_QNAME = QName.create(TOP_SERVICE_QNAME, "ethernet");
    private static final QName ETHERNET_SPEED_QNAME = QName.create(TOP_SERVICE_QNAME, "speed");
    private static final QName TYPE_QNAME = QName.create(TOP_SERVICE_QNAME, "type");
    private static final QName CHOICE_RESOURCE_QNAME = QName.create(TOP_SERVICE_QNAME, "resource");
    private static final NodeIdentifier CHOICE_RESOURCE_ID = new NodeIdentifier(CHOICE_RESOURCE_QNAME);

    private static final QName A_TO_Z_LIST_QNAME = QName.create(TOP_SERVICE_QNAME, "aToZ");
    private static final QName A_TO_Z_LIST_KEY_QNAME = QName.create(TOP_SERVICE_QNAME, "id");
    private static final Map<QName, Object> A_TO_Z_PREDICATES = ImmutableMap.of(A_TO_Z_LIST_KEY_QNAME, TEST_A_TO_Z_ID);

    private static final QName SERVICES_LIST_QNAME = QName.create(TOP_SERVICE_QNAME, "services");
    private static final QName SERVICES_LIST_KEY_LEAF_QNAME = QName.create(TOP_SERVICE_QNAME, "service-name");
    private static final Map<QName, Object> SERVICES_PREDICATES = ImmutableMap.of(SERVICES_LIST_KEY_LEAF_QNAME,
        TEST_SERVICE_NAME);

    private static final YangInstanceIdentifier TOP_SERVICE_PATH = YangInstanceIdentifier.of(TOP_SERVICE_QNAME);

    private SchemaContext schemaContext;
    private DataTree inMemoryDataTree;

    @Before
    public void init() {
        this.schemaContext = YangParserTestUtils.parseYangResourceDirectory("/choiceAugmentation");
        assertNotNull(schemaContext);
        inMemoryDataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION,
            schemaContext);
    }

    //TODO: if the augmented nodes (leaf "type" and container "ethernet") can't be written as direct children, it needs
    // to be fixed - and in that case this test is expected to fail
    @Test
    public void testWriteDataWithMandatoryLeafAsDirectChild() throws DataValidationFailedException {
        ContainerNode testResource = ImmutableContainerNodeBuilder.create()
            .withNodeIdentifier(NodeIdentifier.create(RESOURCE_QNAME))
            .withChild(Builders.choiceBuilder()
                .withNodeIdentifier(CHOICE_RESOURCE_ID)
                .withChild(Builders.leafBuilder().withNodeIdentifier(NodeIdentifier.create(TYPE_QNAME))
                    .withValue(ETHERNET_CSMACD_QNAME)
                    .build())
                .withChild(Builders
                    .containerBuilder()
                    .withNodeIdentifier(NodeIdentifier.create(ETHERNET_CONT_QNAME))
                    .withChild(Builders
                        .leafBuilder()
                        .withNodeIdentifier(NodeIdentifier.create(ETHERNET_SPEED_QNAME))
                        .withValue(123)
                        .build())
                    .build())
                .build())
            .build();

        ContainerNode topServiceData = generateTopServiceDataWithResource(testResource);

        writeData(TOP_SERVICE_PATH, topServiceData);

        // read the resource and verify the mandatory leaf
        NormalizedNode<?, ?> resourceCaseNode = readResourceCase();
        Optional<NormalizedNode<?, ?>> mandatoryTypeNode = NormalizedNodes.findNode(resourceCaseNode,
            YangInstanceIdentifier.of(TYPE_QNAME));
        Assert.assertTrue(mandatoryTypeNode.isPresent());
        Assert.assertEquals(mandatoryTypeNode.get().getValue(), ETHERNET_CSMACD_QNAME);
    }

    @Test
    public void testWriteDataWithMandatoryLeafInsideAugmentation() throws DataValidationFailedException {
        ContainerNode testResource = ImmutableContainerNodeBuilder.create()
            .withNodeIdentifier(NodeIdentifier.create(RESOURCE_QNAME))
            .withChild(Builders.choiceBuilder()
                .withNodeIdentifier(CHOICE_RESOURCE_ID)
                .withChild(Builders.augmentationBuilder()
                    .withNodeIdentifier(YangInstanceIdentifier.AugmentationIdentifier.create(Sets.newSet(TYPE_QNAME,
                        ETHERNET_CONT_QNAME)))
                    .withChild(Builders.leafBuilder()
                        .withNodeIdentifier(NodeIdentifier.create(TYPE_QNAME))
                        .withValue(ETHERNET_CSMACD_QNAME)
                        .build())
                    .withChild(Builders
                        .containerBuilder()
                        .withNodeIdentifier(NodeIdentifier.create(ETHERNET_CONT_QNAME))
                        .withChild(Builders
                            .leafBuilder()
                            .withNodeIdentifier(NodeIdentifier.create(ETHERNET_SPEED_QNAME))
                            .withValue(123)
                            .build())
                        .build())
                    .build())
                .build())
            .build();

        ContainerNode topServiceData = generateTopServiceDataWithResource(testResource);

        writeData(TOP_SERVICE_PATH, topServiceData);

        // read the resource and verify the mandatory leaf
        NormalizedNode<?, ?> resourceCaseNode = readResourceCase();
        Optional<NormalizedNode<?, ?>> mandatoryTypeNode = NormalizedNodes.findNode(resourceCaseNode,
            YangInstanceIdentifier.of(TYPE_QNAME));
        Assert.assertTrue(mandatoryTypeNode.isPresent());
        Assert.assertEquals(mandatoryTypeNode.get().getValue(), ETHERNET_CSMACD_QNAME);
    }

    private void writeData(final YangInstanceIdentifier path, final NormalizedNode<?, ?> data)
        throws DataValidationFailedException {
        DataTreeModification dataTreeModification = inMemoryDataTree.takeSnapshot().newModification();
        dataTreeModification.write(path, data);
        dataTreeModification.ready();
        final DataTreeCandidate writeCandidate = inMemoryDataTree.prepare(dataTreeModification);
        inMemoryDataTree.commit(writeCandidate);
    }

    private Optional<NormalizedNode<?, ?>> readData(final YangInstanceIdentifier path) {
        return inMemoryDataTree.takeSnapshot().readNode(path);
    }

    private NormalizedNode<?, ?> readResourceCase() {

        Optional<NormalizedNode<?, ?>> readResourceOpt = readData(YangInstanceIdentifier.builder()
            .node(TOP_SERVICE_QNAME)
            .node(SERVICES_LIST_QNAME)
            .nodeWithKey(SERVICES_LIST_QNAME, SERVICES_PREDICATES)
            .node(TOPOLOGY_QNAME)
            .node(A_TO_Z_LIST_QNAME)
            .nodeWithKey(A_TO_Z_LIST_QNAME, A_TO_Z_PREDICATES)
            .node(RESOURCE_QNAME)
            .node(RESOURCE_QNAME)
            .build());
        if (readResourceOpt.isPresent()) {
            return readResourceOpt.get();
        }
        throw new IllegalStateException("Failed to read test resource case");
    }

    private ContainerNode generateTopServiceDataWithResource(ContainerNode resourceNode) {
        MapNode mapNodeAtoZListWithNodes = ImmutableNodes.mapNodeBuilder()
            .withNodeIdentifier(new NodeIdentifier(A_TO_Z_LIST_QNAME))
            .withChild(ImmutableMapEntryNodeBuilder.create()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(A_TO_Z_LIST_QNAME, A_TO_Z_LIST_KEY_QNAME,
                    TEST_A_TO_Z_ID))
                .withChild(resourceNode)
                .build())
            .build();

        MapNode services = Builders.mapBuilder().withNodeIdentifier(NodeIdentifier.create(SERVICES_LIST_QNAME))
            .withChild(Builders.mapEntryBuilder()
                .withNodeIdentifier(NodeIdentifierWithPredicates.of(SERVICES_LIST_QNAME, SERVICES_LIST_KEY_LEAF_QNAME,
                    TEST_SERVICE_NAME))
                .withChild(Builders.containerBuilder()
                    .withNodeIdentifier(NodeIdentifier.create(TOPOLOGY_QNAME))
                    .withChild(mapNodeAtoZListWithNodes).build()).build()).build();

        ContainerNode topServiceContainer = Builders.containerBuilder()
            .withNodeIdentifier(NodeIdentifier.create(TOP_SERVICE_QNAME))
            .withChild(services)
            .build();

        return topServiceContainer;
    }
}
