/*
 * Copyright © 2020 Pantheon Technologies, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collection;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

@RunWith(Parameterized.class)
public class YT1108Test {
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return TestFactories.junitParameters();
    }

    private QNameModule fooModule;
    private QName fooLeafContainer;
    private QName fooIdentity;
    private QName fooUnionIdentityRefLeaf;
    private QName fooIdentityRefLeaf;

    private static EffectiveModelContext SCHEMA_CONTEXT;

    private final XMLOutputFactory factory;

    public YT1108Test(final String factoryMode, final XMLOutputFactory factory) {
        this.factory = factory;
    }

    @BeforeClass
    public static void beforeClass() {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYang("""
            module foo {
                namespace "foo-namespace";
                prefix "f";
                identity ident-base;
                identity ident-one {
                    base ident-base;
                }
                typedef union-type {
                    type union {
                        type uint8;
                        type identityref {
                            base ident-base;
                        }
                    }
                }
                container leaf-container {
                    leaf union-identityref-leaf {
                        type union-type;
                    }
                    leaf identityref-leaf {
                        type identityref {
                            base ident-base;
                        }
                    }
                }
            }""");
    }

    @AfterClass
    public static void afterClass() {
        SCHEMA_CONTEXT = null;
    }

    @Before
    public void setup() {
        fooModule = QNameModule.create(XMLNamespace.of("foo-namespace"));
        fooLeafContainer = QName.create(fooModule, "leaf-container");

        fooIdentity = QName.create(fooModule, "ident-one");
        fooUnionIdentityRefLeaf = QName.create(fooModule, "union-identityref-leaf");
        fooIdentityRefLeaf = QName.create(fooModule, "identityref-leaf");
    }

    @Test
    public void testLeafOfIdentityRefTypeNNToXmlSerialization()
            throws XMLStreamException, IOException, SAXException {
        final Document doc = loadDocument("/yt1108/xml/foo-leaf-of-identity-ref-type.xml");
        final DOMResult domResult = convertNormalizedNodeToXml(buildLeafContainerNodeWithIdentityRefLeaf());

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalize(true);

        final String expectedXml = toString(doc.getDocumentElement());
        final String serializedXml = toString(domResult.getNode());
        final Diff diff = new Diff(expectedXml, serializedXml);

        new XMLTestCase() {}.assertXMLEqual(diff, true);
    }

    @Test
    public void testLeafOfUnionWithIdentityRefNNToXmlSerialization()
            throws XMLStreamException, IOException, SAXException {
        final Document doc = loadDocument("/yt1108/xml/foo-leaf-of-union-with-identity-ref-type.xml");
        final DOMResult domResult = convertNormalizedNodeToXml(buildLeafContainerNodeWithUnionIdentityRefLeaf());

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalize(true);

        final String expectedXml = toString(doc.getDocumentElement());
        final String serializedXml = toString(domResult.getNode());
        final Diff diff = new Diff(expectedXml, serializedXml);

        new XMLTestCase() {}.assertXMLEqual(diff, true);
    }

    private DOMResult convertNormalizedNodeToXml(final NormalizedNode normalizedNode)
            throws XMLStreamException, IOException {
        final DOMResult domResult = new DOMResult(UntrustedXML.newDocumentBuilder().newDocument());

        final XMLStreamWriter xmlStreamWriter = factory.createXMLStreamWriter(domResult);

        final NormalizedNodeStreamWriter xmlNormalizedNodeStreamWriter = XMLStreamNormalizedNodeStreamWriter.create(
                xmlStreamWriter, SCHEMA_CONTEXT);

        final NormalizedNodeWriter normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(
                xmlNormalizedNodeStreamWriter);

        normalizedNodeWriter.write(normalizedNode);
        return domResult;
    }

    private NormalizedNode buildLeafContainerNodeWithIdentityRefLeaf() {
        return Builders.containerBuilder()
                .withNodeIdentifier(NodeIdentifier.create(fooLeafContainer))
                .withChild(Builders.leafBuilder()
                        .withNodeIdentifier(NodeIdentifier.create(fooIdentityRefLeaf))
                        .withValue(fooIdentity)
                        .build())
                .build();
    }

    private NormalizedNode buildLeafContainerNodeWithUnionIdentityRefLeaf() {
        return Builders.containerBuilder()
                .withNodeIdentifier(NodeIdentifier.create(fooLeafContainer))
                .withChild(Builders.leafBuilder()
                        .withNodeIdentifier(YangInstanceIdentifier.NodeIdentifier.create(fooUnionIdentityRefLeaf))
                        .withValue(fooIdentity)
                        .build())
                .build();
    }

    private static Document loadDocument(final String xmlPath) throws IOException, SAXException {
        final InputStream resourceAsStream = NormalizedNodesToXmlTest.class.getResourceAsStream(xmlPath);
        return requireNonNull(readXmlToDocument(resourceAsStream));
    }

    private static Document readXmlToDocument(final InputStream xmlContent) throws IOException, SAXException {
        final Document doc = UntrustedXML.newDocumentBuilder().parse(xmlContent);
        doc.getDocumentElement().normalize();
        return doc;
    }

    private static String toString(final Node xml) {
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
}
