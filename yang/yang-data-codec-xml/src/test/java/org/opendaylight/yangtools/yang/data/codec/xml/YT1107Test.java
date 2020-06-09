/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.junit.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.xml.sax.SAXException;

public class YT1107Test {
    private static final QName PARENT = QName.create("yt1107", "parent");
    private static final QName ADMIN = QName.create(PARENT, "admin");
    private static final QName NAME = QName.create(PARENT, "name");
    private static final QName USER = QName.create(PARENT, "user");

    @Test
    public void testInterleavingLists() throws XMLStreamException, URISyntaxException, IOException, SAXException {
        final EffectiveModelContext schemaContext = YangParserTestUtils.parseYangResource("/yt1107/yt1107.yang");
        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/yt1107/yt1107.xml");
        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext,
            SchemaContextUtil.findNodeInSchemaContext(schemaContext, Arrays.asList(PARENT)));
        xmlParser.parse(reader);

        assertEquals(Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(PARENT))
            .withChild(Builders.mapBuilder()
                .withNodeIdentifier(new NodeIdentifier(ADMIN))
                .withChild(Builders.mapEntryBuilder()
                    .withNodeIdentifier(NodeIdentifierWithPredicates.of(ADMIN, NAME, "John"))
                    .withChild(ImmutableNodes.leafNode(NAME, "John"))
                    .build())
                .build())
            .withChild(Builders.mapBuilder()
                .withNodeIdentifier(new NodeIdentifier(USER))
                .withChild(Builders.mapEntryBuilder()
                    .withNodeIdentifier(NodeIdentifierWithPredicates.of(USER, NAME, "Freud"))
                    .withChild(ImmutableNodes.leafNode(NAME, "Freud"))
                    .build())
                .withChild(Builders.mapEntryBuilder()
                    .withNodeIdentifier(NodeIdentifierWithPredicates.of(USER, NAME, "Bob"))
                    .withChild(ImmutableNodes.leafNode(NAME, "Bob"))
                    .build())
                .build())
            .build(), result.getResult());
    }
}
