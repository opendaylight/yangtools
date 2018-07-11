/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Optional;
import javax.xml.stream.XMLStreamReader;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug890Test {
    private static final QNameModule FOO_MODULE = QNameModule.create(URI.create("foo"), Revision.of("2018-07-10"));
    private static final QName OUTGOING_LABELS_QNAME = QName.create(FOO_MODULE, "outgoing-labels");
    private static final QName INDEX_QNAME = QName.create(FOO_MODULE, "index");

    private SchemaContext schemaContext;

    @Before
    public void setUp() throws Exception {
        schemaContext = YangParserTestUtils.parseYangResource("/bug890/yang/foo.yang");
    }

    @Test
    public void testinputXml() throws Exception {
        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/bug890/xml/foo.xml");
        final Module fooModule = schemaContext.getModules().iterator().next();
        final Optional<DataSchemaNode> rootCont = fooModule.findDataChildByName(
                QName.create(fooModule.getQNameModule(), "root"));
        assertTrue(rootCont.isPresent());
        ContainerSchemaNode containerSchemaNode = (ContainerSchemaNode) rootCont.get();
        assertNotNull(containerSchemaNode);

        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();

        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, containerSchemaNode);
        xmlParser.parse(reader);

        assertNotNull(result.getResult());
        assertTrue(result.getResult() instanceof ContainerNode);
        final ContainerNode rootContainer = (ContainerNode) result.getResult();

        Optional<DataContainerChild<? extends PathArgument, ?>> myLeaf =
                rootContainer.getChild(new NodeIdentifier(OUTGOING_LABELS_QNAME));
        assertTrue(myLeaf.orElse(null) instanceof ContainerNode);

        ContainerNode outgoingLabelsContainer = (ContainerNode)myLeaf.get();
        Optional<DataContainerChild<? extends PathArgument, ?>> outgoingLabelsList =
                outgoingLabelsContainer.getChild(new NodeIdentifier(OUTGOING_LABELS_QNAME));
        assertTrue(outgoingLabelsList.orElse(null) instanceof MapNode);
        MapNode outgoingLabelsMap = (MapNode) outgoingLabelsList.get();

        assertEquals(2, outgoingLabelsMap.getValue().size());
        Collection<MapEntryNode> labels = outgoingLabelsMap.getValue();
        YangInstanceIdentifier.NodeIdentifierWithPredicates firstNodeId =
                new YangInstanceIdentifier.NodeIdentifierWithPredicates(OUTGOING_LABELS_QNAME, INDEX_QNAME, 0);
        YangInstanceIdentifier.NodeIdentifierWithPredicates secondNodeId =
                new YangInstanceIdentifier.NodeIdentifierWithPredicates(OUTGOING_LABELS_QNAME, INDEX_QNAME, 1);
        assertTrue(labels.stream().anyMatch(mapEntryNode -> mapEntryNode.getIdentifier().compareTo(firstNodeId) == 0));
        assertTrue(labels.stream().anyMatch(mapEntryNode -> mapEntryNode.getIdentifier().compareTo(secondNodeId) == 0));
    }
}
