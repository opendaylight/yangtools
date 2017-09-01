/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.serializer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.text.ParseException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.serializer.NodeSerializerDispatcher;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DomSerializerTestUtils {
    static final Document DOC = XmlDocumentUtils.getDocument();
    static final Element DATA = DOC.createElement("data");
    static final NodeSerializerDispatcher<Element> MOCK_DISPATCHER = Mockito.mock(NodeSerializerDispatcher.class);
    static final XmlCodecProvider CODEC_PROVIDER = TypeDefinitionAwareCodec::from;

    private DomSerializerTestUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static SchemaContext getSchemaContext() throws ReactorException, IOException, YangSyntaxErrorException,
            URISyntaxException {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResource("/dom-serializer-test/serializer-test.yang");

        assertNotNull("Schema context must not be null.", schemaContext);
        return schemaContext;
    }

    public static YangInstanceIdentifier.NodeIdentifier getNodeIdentifier(final String localName, final String
            namespace, final String revision) throws ParseException {
        return new YangInstanceIdentifier.NodeIdentifier(QName.create(namespace, revision, localName));
    }

    public static String toString(final Node xml) {
        try {
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            final StreamResult result = new StreamResult(new StringWriter());
            final DOMSource source = new DOMSource(xml);
            transformer.transform(source, result);

            return result.getWriter().toString();
        } catch (IllegalArgumentException | TransformerFactoryConfigurationError | TransformerException e) {
            throw new RuntimeException("Unable to serialize xml element " + xml, e);
        }
    }

    public static QName generateQname(final String name) {
        return QName.create("dom-serializer-test", "2016-01-01", name);
    }

    public static void testResults(final String node, final Element element) {
        DATA.appendChild(element);
        final String tempXml = toString(DATA);

        assertNotNull(tempXml);
        assertTrue(tempXml.contains(node));
    }
}