/*
 * Copyright (c) 2018 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.leafref;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT892Test {
    private static final QName BGP = QName.create("urn:opendaylight:params:xml:ns:yang:test:bgp", "2018-08-14", "bgp");
    private static final QName PEER_GROUPS = QName.create(BGP, "peer-groups");
    private static final QName PEER_GROUP = QName.create(BGP, "peer-group");
    private static final QName PEER_GROUP_NAME = QName.create(BGP, "peer-group-name");
    private static final YangInstanceIdentifier BGP_ID = YangInstanceIdentifier.of(BGP);

    private static final QName NETWORK_INSTANCES =
            QName.create("urn:opendaylight:params:xml:ns:yang:test:network:instance", "2018-08-14",
                "network-instances");
    private static final QName NETWORK_INSTANCE = QName.create(NETWORK_INSTANCES, "network-instance");
    private static final QName NAME = QName.create(NETWORK_INSTANCES, "name");
    private static final QName CONFIG = QName.create(NETWORK_INSTANCES, "config");
    private static final QName PROTOCOLS = QName.create(NETWORK_INSTANCES, "protocols");
    private static final QName PROTOCOL = QName.create(NETWORK_INSTANCES, "protocol");
    private static final QName IDENTIFIER = QName.create(NETWORK_INSTANCES, "identifier");
    private static final QName BGP_POLICY = QName.create("urn:opendaylight:params:xml:ns:yang:test:policy:types",
        "2018-08-14", "BGP");
    private static final QName TEST_BGP = QName.create("urn:opendaylight:params:xml:ns:yang:bgp:test:extensions",
        "2018-08-14", "bgp");
    private static final QName NEIGHBORS = QName.create(TEST_BGP, "neighbors");
    private static final QName NEIGHBOR = QName.create(TEST_BGP, "neighbor");
    private static final QName NEIGHBOR_ADDRESS = QName.create(TEST_BGP, "neighbor-address");
    private static final QName TEST_CONFIG = QName.create(TEST_BGP, "config");
    private static final QName TEST_PEER_GROUP = QName.create(TEST_BGP, "peer-group");
    private static final QName AFI_SAFIS = QName.create(TEST_BGP, "afi-safis");
    private static final QName AFI_SAFI = QName.create(TEST_BGP, "afi-safi");
    private static final QName AFI_SAFI_NAME = QName.create(TEST_BGP, "afi-safi-name");
    private static final QName IPV4_UNICAST = QName.create("urn:opendaylight:params:xml:ns:yang:test:bgp:types",
        "2018-08-14", "IPV4-UNICAST");
    private static final QName RECEIVE = QName.create(TEST_BGP, "receive");
    private static final QName SEND_MAX = QName.create(TEST_BGP, "send-max");

    private static final YangInstanceIdentifier NETWORK_INSTANCES_ID = YangInstanceIdentifier.of(NETWORK_INSTANCES);

    private LeafRefContext leafRefContext;
    private DataTree dataTree;

    @BeforeEach
    void setup() {
        final var schemaContext = YangParserTestUtils.parseYangResourceDirectory("/yt892");
        leafRefContext = LeafRefContext.create(schemaContext);
        dataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION, schemaContext);
    }

    @Test
    void testWriteBgpNeighbour() throws Exception {
        final var writeModification = dataTree.takeSnapshot().newModification();
        writeModification.write(BGP_ID, ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(BGP))
            .withChild(ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(PEER_GROUPS))
                .withChild(ImmutableNodes.newSystemMapBuilder()
                    .withNodeIdentifier(new NodeIdentifier(PEER_GROUP))
                    .withChild(ImmutableNodes.newMapEntryBuilder()
                        .withNodeIdentifier(NodeIdentifierWithPredicates.of(PEER_GROUP,
                            PEER_GROUP_NAME, "application-peers"))
                        .withChild(ImmutableNodes.leafNode(PEER_GROUP_NAME, "application-peers"))
                        .build())
                    .build())
                .build())
            .build());

        writeModification.write(NETWORK_INSTANCES_ID, ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(NETWORK_INSTANCES))
            .withChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(NETWORK_INSTANCE))
                .withChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(NodeIdentifierWithPredicates.of(NETWORK_INSTANCE, NAME, "global-bgp"))
                    .withChild(ImmutableNodes.leafNode(NAME, "global-bgp"))
                    .withChild(ImmutableNodes.newContainerBuilder()
                        .withNodeIdentifier(new NodeIdentifier(CONFIG))
                        .withChild(ImmutableNodes.leafNode(NAME, "global-bgp"))
                        .build())
                    .withChild(ImmutableNodes.newContainerBuilder()
                        .withNodeIdentifier(new NodeIdentifier(PROTOCOLS))
                        .withChild(ImmutableNodes.newSystemMapBuilder()
                            .withNodeIdentifier(new NodeIdentifier(PROTOCOL))
                            .withChild(ImmutableNodes.newMapEntryBuilder()
                                .withNodeIdentifier(NodeIdentifierWithPredicates.of(PROTOCOL, ImmutableMap.of(
                                    IDENTIFIER, BGP_POLICY,
                                    NAME, "test-bgp-instance")))
                                .withChild(ImmutableNodes.leafNode(IDENTIFIER, BGP_POLICY))
                                .withChild(ImmutableNodes.leafNode(NAME, "test-bgp-instance"))
                                .withChild(ImmutableNodes.newContainerBuilder()
                                    .withNodeIdentifier(new NodeIdentifier(CONFIG))
                                    .withChild(ImmutableNodes.leafNode(IDENTIFIER, BGP_POLICY))
                                    .withChild(ImmutableNodes.leafNode(NAME, "test-bgp-instance"))
                                    .build())
                                .withChild(ImmutableNodes.newContainerBuilder()
                                    .withNodeIdentifier(new NodeIdentifier(TEST_BGP))
                                    .withChild(ImmutableNodes.newContainerBuilder()
                                        .withNodeIdentifier(new NodeIdentifier(NEIGHBORS))
                                        .withChild(ImmutableNodes.newSystemMapBuilder()
                                            .withNodeIdentifier(new NodeIdentifier(NEIGHBOR))
                                            .withChild(ImmutableNodes.newMapEntryBuilder()
                                                .withNodeIdentifier(NodeIdentifierWithPredicates.of(NEIGHBOR,
                                                    NEIGHBOR_ADDRESS, "10.25.1.9"))
                                                .withChild(ImmutableNodes.leafNode(NEIGHBOR_ADDRESS, "10.25.1.9"))
                                                .withChild(ImmutableNodes.newContainerBuilder()
                                                    .withNodeIdentifier(new NodeIdentifier(TEST_CONFIG))
                                                    .withChild(ImmutableNodes.leafNode(TEST_PEER_GROUP,
                                                        "application-peers"))
                                                    .build())
                                                .withChild(ImmutableNodes.newContainerBuilder()
                                                    .withNodeIdentifier(new NodeIdentifier(AFI_SAFIS))
                                                    .withChild(ImmutableNodes.newSystemMapBuilder()
                                                        .withNodeIdentifier(new NodeIdentifier(AFI_SAFI))
                                                        .withChild(ImmutableNodes.newMapEntryBuilder()
                                                            .withNodeIdentifier(NodeIdentifierWithPredicates.of(
                                                                AFI_SAFI,
                                                                ImmutableMap.of(AFI_SAFI_NAME, IPV4_UNICAST)))
                                                            .withChild(ImmutableNodes.leafNode(AFI_SAFI_NAME,
                                                                IPV4_UNICAST))
                                                            .withChild(ImmutableNodes.newContainerBuilder()
                                                                .withNodeIdentifier(
                                                                    new NodeIdentifier(TEST_CONFIG))
                                                                .withChild(ImmutableNodes.leafNode(
                                                                    AFI_SAFI_NAME, IPV4_UNICAST))
                                                                .build())
                                                            .withChild(ImmutableNodes.leafNode(RECEIVE,
                                                                Boolean.TRUE))
                                                            .withChild(ImmutableNodes.leafNode(SEND_MAX,
                                                                Uint8.ZERO))
                                                            .build())
                                                        .build())
                                                    .build())
                                                .build())
                                            .build())
                                        .build())
                                    .build())
                                .build())
                            .build())
                        .build())
                    .build())
                .build())
            .build());

        writeModification.ready();
        final var writeContributorsCandidate = dataTree.prepare(writeModification);
        LeafRefValidation.validate(writeContributorsCandidate, leafRefContext);
        dataTree.commit(writeContributorsCandidate);
    }
}
