/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Bug890Test {
    private static final QNameModule FOO_MODULE = QNameModule.of("foo", "2018-07-10");
    private static final QName OUTGOING_LABELS_QNAME = QName.create(FOO_MODULE, "outgoing-labels");
    private static final QName INDEX_QNAME = QName.create(FOO_MODULE, "index");

    @Test
    void testinputXml() throws Exception {
        final var schemaContext = YangParserTestUtils.parseYangResourceDirectory("/bug890");
        final var reader = UntrustedXML.createXMLStreamReader(new ByteArrayInputStream("""
            <root xmlns="foo">
                <outgoing-labels>
                    <outgoing-labels>
                        <index>0</index>
                        <config>
                            <index>0</index>
                            <label>103</label>
                        </config>
                    </outgoing-labels>
                    <outgoing-labels>
                        <index>1</index>
                        <config>
                            <index>1</index>
                            <label>104</label>
                        </config>
                    </outgoing-labels>
                </outgoing-labels>
            </root>""".getBytes(StandardCharsets.UTF_8)));
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schemaContext, QName.create(FOO_MODULE, "root")));
        xmlParser.parse(reader);

        assertNotNull(result.getResult());
        assertTrue(result.getResult().data() instanceof ContainerNode);
        final ContainerNode rootContainer = (ContainerNode) result.getResult().data();

        DataContainerChild myLeaf = rootContainer.childByArg(new NodeIdentifier(OUTGOING_LABELS_QNAME));
        assertTrue(myLeaf instanceof ContainerNode);

        ContainerNode outgoingLabelsContainer = (ContainerNode)myLeaf;
        DataContainerChild outgoingLabelsList =
                outgoingLabelsContainer.childByArg(new NodeIdentifier(OUTGOING_LABELS_QNAME));
        assertTrue(outgoingLabelsList instanceof MapNode);
        MapNode outgoingLabelsMap = (MapNode) outgoingLabelsList;

        assertEquals(2, outgoingLabelsMap.size());
        Collection<MapEntryNode> labels = outgoingLabelsMap.body();
        NodeIdentifierWithPredicates firstNodeId =
                NodeIdentifierWithPredicates.of(OUTGOING_LABELS_QNAME, INDEX_QNAME, 0);
        NodeIdentifierWithPredicates secondNodeId =
                NodeIdentifierWithPredicates.of(OUTGOING_LABELS_QNAME, INDEX_QNAME, 1);
        assertTrue(labels.stream().anyMatch(mapEntryNode -> mapEntryNode.name().compareTo(firstNodeId) == 0));
        assertTrue(labels.stream().anyMatch(mapEntryNode -> mapEntryNode.name().compareTo(secondNodeId) == 0));
    }
}
