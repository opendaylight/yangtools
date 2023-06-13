/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.containerBuilder;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.stream.Stream;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.spi.DefaultSchemaTreeInference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Node;

/**
 * Tests addressing notification support by xml parser.
 */
class NotificationTest {
    private static final XMLNamespace NS = XMLNamespace.of("test:notification");

    private static EffectiveModelContext schemaContext;

    @BeforeAll
    public static void beforeAll() {
        schemaContext = YangParserTestUtils.parseYangResource("/notifications.yang");
    }

    @AfterAll
    static void afterAll() {
        schemaContext = null;
    }

    @ParameterizedTest(name = "Notification found in context: {0}")
    @MethodSource("testArgs")
    void notificationFoundInContext(final String testDesc, final String xml, final NormalizedNode normalized)
            throws Exception {
        assertTrue(schemaContext.findNotification(normalized.name().getNodeType()).isPresent());
    }


    @ParameterizedTest(name = "Parse notification: {0}")
    @MethodSource("testArgs")
    void parseNotification(final String testDesc, final String xml, final NormalizedNode expected) throws Exception {

        final var reader = UntrustedXML.createXMLStreamReader(new StringReader(xml));
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter,
            DefaultSchemaTreeInference.of(schemaContext, Absolute.of(expected.name().getNodeType())));
        xmlParser.parse(reader);

        assertEquals(expected, result.getResult().data());
    }

    @ParameterizedTest(name = "Write notification: {0}")
    @MethodSource("testArgs")
    void writeNotification(final String testDesc, final String expected, final NormalizedNode normalized)
            throws Exception {

        final var document = UntrustedXML.newDocumentBuilder().newDocument();
        final var domResult = new DOMResult(document);
        final var xmlStreamWriter = TestFactories.DEFAULT_OUTPUT_FACTORY.createXMLStreamWriter(domResult);
        final var nodeStreamWriter = XMLStreamNormalizedNodeStreamWriter.create(xmlStreamWriter,
                DefaultSchemaTreeInference.of(schemaContext, Absolute.of(normalized.name().getNodeType())));
        try (NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(nodeStreamWriter)) {
            nodeWriter.write(normalized);
        }
        assertEquals(expected, toXmlString(domResult.getNode()));
    }

    private static Stream<Arguments> testArgs() {
        return Stream.of(
            Arguments.of(
                "root level", """
                    <root-notification xmlns="test:notification">
                        <root-message>test 0</root-message>
                    </root-notification>
                    """,
                containerNode(QName.create(NS, "root-notification"),
                    leafNode(QName.create(NS, "root-message"), "test 0"))
            ),
            Arguments.of(
                "inline", """
                    <inline-notification xmlns="test:notification">
                        <inline-message>test 1</inline-message>
                    </inline-notification>
                    """,
                containerNode(QName.create(NS, "inline-notification"),
                    leafNode(QName.create(NS, "inline-message"), "test 1"))
            ),
            Arguments.of(
                "from grouping", """
                    <group-notification xmlns="test:notification">
                        <group-message>test 2</group-message>
                    </group-notification>
                    """,
                containerNode(QName.create(NS, "group-notification"),
                    leafNode(QName.create(NS, "inline-message"), "test 2"))
            )
        );
    }

    private static ContainerNode containerNode(final QName qname, final DataContainerChild child) {
        return containerBuilder().withNodeIdentifier(new NodeIdentifier(qname)).addChild(child).build();
    }

    private static LeafNode<?> leafNode(final QName qname, final Object value) {
        return Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(qname)).withValue(value).build();
    }

    private static String toXmlString(final Node xml) {
        try {
            final var transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            final var result = new StreamResult(new StringWriter());
            final var source = new DOMSource(xml);
            transformer.transform(source, result);

            return result.getWriter().toString();
        } catch (IllegalArgumentException | TransformerFactoryConfigurationError | TransformerException e) {
            throw new RuntimeException("Unable to serialize xml element " + xml, e);
        }
    }
}