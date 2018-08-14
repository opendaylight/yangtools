/*
 * Copyright (c) 2018 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import java.io.InputStream;
import javax.xml.stream.XMLStreamReader;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefContext;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefValidation;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.InMemoryDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Yangtools892Test {
    private static final String TEST_BGP_NAME = "test-bgp";
    private static final String TEST_BGP_NS = "urn:opendaylight:params:xml:ns:yang:test:bgp";
    private static final String TEST_BGP_REV = "2018-08-14";
    private static final QName BGP = QName.create(TEST_BGP_NS, TEST_BGP_REV, "bgp");
    private static final YangInstanceIdentifier BGP_ID = YangInstanceIdentifier.of(BGP);

    private static final String NETWORK_INSTANCE_NAME = "test-network-instance";
    private static final String NETWORK_INSTANCE_NS = "urn:opendaylight:params:xml:ns:yang:test:network:instance";
    private static final String NETWORK_INSTANCE_REV = "2018-08-14";
    private static final QName NETWORK_INSTANCES =
        QName.create(NETWORK_INSTANCE_NS, NETWORK_INSTANCE_REV, "network-instances");
    private static final YangInstanceIdentifier NETWORK_INSTANCES_ID = YangInstanceIdentifier.of(NETWORK_INSTANCES);

    private SchemaContext schemaContext;
    private LeafRefContext leafRefContext;
    private DataTree dataTree;
    private ContainerSchemaNode bgpNode;
    private ContainerSchemaNode networkInstancesNode;

    @Before
    public void setup() {
        schemaContext = YangParserTestUtils.parseYangResourceDirectory("/yangtools892");
        leafRefContext = LeafRefContext.create(schemaContext);
        dataTree = new InMemoryDataTreeFactory().create(DataTreeConfiguration.DEFAULT_CONFIGURATION, schemaContext);
        final Module testBgpModule = schemaContext.findModule(TEST_BGP_NAME, Revision.of(TEST_BGP_REV)).get();
        bgpNode = (ContainerSchemaNode) testBgpModule.findDataChildByName(BGP).get();
        final Module networkInstanceModule =
            schemaContext.findModule(NETWORK_INSTANCE_NAME, Revision.of(NETWORK_INSTANCE_REV)).get();
        networkInstancesNode = (ContainerSchemaNode) networkInstanceModule.findDataChildByName(NETWORK_INSTANCES).get();
    }

    @Test
    public void testWriteBgpNeighbour() throws Exception {
        final DataTreeModification writeModification = dataTree.takeSnapshot().newModification();
        final NormalizedNode<?, ?> bgp = readNode("/yangtools892/peer-groups.xml", bgpNode);
        writeModification.write(BGP_ID, bgp);
        final NormalizedNode<?, ?> networkInstances = readNode("/yangtools892/neighbour.xml", networkInstancesNode);
        writeModification.write(NETWORK_INSTANCES_ID, networkInstances);

        writeModification.ready();
        final DataTreeCandidate writeContributorsCandidate = dataTree.prepare(writeModification);
        LeafRefValidation.validate(writeContributorsCandidate, leafRefContext);
        dataTree.commit(writeContributorsCandidate);
    }

    private NormalizedNode<?, ?> readNode(final String filename, final ContainerSchemaNode node) throws Exception {
        final InputStream resourceAsStream = Yangtools891Test.class.getResourceAsStream(filename);
        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, node);
        xmlParser.parse(reader);
        return result.getResult();
    }
}