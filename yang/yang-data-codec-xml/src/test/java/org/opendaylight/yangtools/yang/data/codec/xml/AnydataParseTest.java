/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.junit.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.xml.sax.SAXException;

public class AnydataParseTest extends AbstractAnydataTest {

    @Test
    public void testAnydata() throws XMLStreamException, IOException, URISyntaxException, SAXException {
        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(
            toInputStream("<foo xmlns=\"test-anydata\"><bar/></foo>"));

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, SCHEMA_CONTEXT,
            SCHEMA_CONTEXT.findDataChildByName(FOO_QNAME).get(), true);
        xmlParser.parse(reader);

        final NormalizedNode<?, ?> parsed = result.getResult();
        assertTrue(parsed instanceof AnydataNode);
    }
}
