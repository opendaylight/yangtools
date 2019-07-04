/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf.LeafEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.xml.sax.SAXException;

@RunWith(Parameterized.class)
public class Bug968Test {

    private static SchemaContext SCHEMA_CONTEXT;
    private static final QName FLAVOR_QNAME = QName.create("test-leafref", "leaf-leafref-deref");

    private final XMLOutputFactory factory;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return TestFactories.junitParameters();
    }

    public Bug968Test(final String factoryMode, final XMLOutputFactory factory) {
        this.factory = factory;
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYangResource("/bug968/foo.yang");
    }

    @AfterClass
    public static void afterClass() {
        SCHEMA_CONTEXT = null;
    }

    @Test
    public void testParseLeafRef()
            throws IOException, SAXException, XMLStreamException, URISyntaxException, TransformerException {
        //Create Data Scheme from yang file
        SchemaPath leafrefPath = SchemaPath.create(true, FLAVOR_QNAME);
        final SchemaNode dataSchemaNode = SchemaContextUtil.findDataSchemaNode(SCHEMA_CONTEXT, leafrefPath);
        assertTrue(dataSchemaNode instanceof LeafEffectiveStatementImpl);
        final LeafEffectiveStatementImpl leafSchemaNode = (LeafEffectiveStatementImpl) dataSchemaNode;

        // deserialization
        final String invalidXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                                + "<leaf-leafref-deref xmlns=\"test-leafref\">INVALID</leaf-leafref-deref>";
        final String validXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                              + "<leaf-leafref-deref xmlns=\"test-leafref\">10</leaf-leafref-deref>";
        final XMLStreamReader readerIncorrect
                = UntrustedXML.createXMLStreamReader(toInputStream(invalidXml));
        final XMLStreamReader readCorrect
                = UntrustedXML.createXMLStreamReader(toInputStream(validXml));

        Assert.assertNull(getNormalizedNodeResult(leafSchemaNode, readerIncorrect));

        final NormalizedNodeResult result = getNormalizedNodeResult(leafSchemaNode, readCorrect);
        Assert.assertNotNull(result);

        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
        assertTrue(transformedInput instanceof LeafNode);
        LeafNode<?> leafNode = (LeafNode<?>) transformedInput;
        Assert.assertEquals(leafNode.getValue(), 10);
        // serialization
        final StringWriter writer = new StringWriter();
        final XMLStreamWriter xmlStreamWriter = factory.createXMLStreamWriter(writer);
        final NormalizedNodeStreamWriter xmlNormalizedNodeStreamWriter = XMLStreamNormalizedNodeStreamWriter.create(
                xmlStreamWriter, SCHEMA_CONTEXT);
        final NormalizedNodeWriter normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(
                xmlNormalizedNodeStreamWriter);
        normalizedNodeWriter.write(transformedInput);
        normalizedNodeWriter.flush();

        final String serializedXml = writer.toString();
        Assert.assertEquals(serializedXml, "<leaf-leafref-deref xmlns=\"test-leafref\">10</leaf-leafref-deref>");
    }

    /**
     * Transform String to InputStream.
     * @param str Transformed String
     * @return InputStream as a Result
     */
    private static InputStream toInputStream(final String str) {
        return new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Create {@link NormalizedNodeResult} from input parameters.
     * @param schemaNode {@link SchemaNode}
     * @param reader {@link XMLStreamReader}
     * @return {@link NormalizedNodeResult} or null if there is {@link NumberFormatException}
     * @throws URISyntaxException {@link XmlParserStream} exception
     * @throws IOException {@link XmlParserStream} exception
     * @throws SAXException {@link XmlParserStream} exception
     * @throws XMLStreamException {@link XmlParserStream} exception
     */
    private static NormalizedNodeResult getNormalizedNodeResult(final SchemaNode schemaNode,
                                                                final XMLStreamReader reader)
            throws URISyntaxException, IOException, SAXException, XMLStreamException {
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final XmlParserStream xmlParserIncorrect = XmlParserStream.create(streamWriter, SCHEMA_CONTEXT, schemaNode);
        try {
            xmlParserIncorrect.parse(reader);
            return result;
        } catch (NumberFormatException ex) {
            return null;
        }
    }


    /**
     * Transform DomSource object to formatted XML string.
     * @param node {@link DOMSource}
     * @return {@link String}
     * @throws TransformerException Internal {@link Transformer} exception
     */
    private static String getXmlFromDOMSource(final DOMSource node) throws TransformerException {
        StringWriter writer = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(node, new StreamResult(writer));
        return writer.toString();
    }

    /**
     * Load external resources as {@link InputStream}.
     * @param xmlPath {@link String} Path to file
     * @return {@link InputStream}
     */
    private static InputStream loadResourcesAsInputStream(final String xmlPath) {
        return SchemalessXMLStreamNormalizedNodeStreamWriterTest.class
                .getResourceAsStream(xmlPath);
    }
}
