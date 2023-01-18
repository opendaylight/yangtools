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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.SchemaTreeInference;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.spi.DefaultSchemaTreeInference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Tests addressing notification support by xml parser.
 */
public class YT786Test {

    private static final XMLNamespace NS = XMLNamespace.of("test:notification");
    private static final QName QN_LEAF_EVENT = qnameFor("leaf-event");
    private static final QName QN_GROUP_EVENT = qnameFor("group-event");
    private static final QName QN_CONTAINER_EVENT = qnameFor("container-event");
    private static final QName QN_LIST_EVENT = qnameFor("list-event");
    private static final QName QN_LF = qnameFor("lf");
    private static final QName QN_LST = qnameFor("lst");
    private static final String TEST = "test";

    private static final NormalizedNode NN_LEAF = containerNode(QN_LEAF_EVENT, leafNode(QN_LF, TEST));
    private static final NormalizedNode NN_GROUP = containerNode(QN_GROUP_EVENT, leafNode(QN_LF, TEST));
    private static final NormalizedNode NN_CONTAINER = containerNode(QN_CONTAINER_EVENT,
            containerNode(qnameFor("cont"), leafNode(QN_LF, TEST)));
    private static final NormalizedNode NN_LIST = containerNode(QN_LIST_EVENT,
            mapNodeWithLeafValue(QN_LST, QN_LF, TEST));

    private static final String XML_LEAF = fromResource("/YT786/leaf-event.xml");
    private static final String XML_GROUP = fromResource("/YT786/group-event.xml");
    private static final String XML_CONTAINER = fromResource("/YT786/container-event.xml");
    private static final String XML_LIST = fromResource("/YT786/list-event.xml");

    private static EffectiveModelContext schemaContext;

    @BeforeAll
    public static void beforeAll() throws Exception {
        schemaContext = YangParserTestUtils.parseYangResource("/YT786/notification-test.yang");
    }

    @AfterAll
    static void afterAll() {
        schemaContext = null;
    }

    @ParameterizedTest(name = "Parsing notification: {0}")
    @MethodSource("testArgs")
    public void parseNotification(final QName qname, final String xml, final NormalizedNode expected)
            throws Exception {

        // ensure notification with QName is known
        assertTrue(schemaContext.getNotifications().stream().anyMatch(n -> qname.equals(n.getQName())));

        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(new StringReader(xml));
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, inferenceOf(qname));
        xmlParser.parse(reader);

        assertEquals(expected, result.getResult());
    }

    @ParameterizedTest(name = "Write notification: {0}")
    @MethodSource("testArgs")
    public void writeNotification(final QName qname, final String expected, final NormalizedNode normalized)
            throws Exception {

        final Document document = UntrustedXML.newDocumentBuilder().newDocument();
        final DOMResult domResult = new DOMResult(document);
        final XMLStreamWriter xmlStreamWriter = TestFactories.DEFAULT_OUTPUT_FACTORY.createXMLStreamWriter(domResult);
        final NormalizedNodeStreamWriter nodeStreamWriter = XMLStreamNormalizedNodeStreamWriter
                .create(xmlStreamWriter, inferenceOf(qname));
        try (NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(nodeStreamWriter)) {
            nodeWriter.write(normalized);
        }
        assertEquals(expected, toString(domResult.getNode()));
    }

    private static Stream<Arguments> testArgs() throws Exception {
        // notification definition qname, notification data as xml, as normalized node
        return Stream.of(
                Arguments.of(QN_LEAF_EVENT, XML_LEAF, NN_LEAF),
                Arguments.of(QN_GROUP_EVENT, XML_GROUP, NN_GROUP),
                Arguments.of(QN_CONTAINER_EVENT, XML_CONTAINER, NN_CONTAINER),
                Arguments.of(QN_LIST_EVENT, XML_LIST, NN_LIST)
        );
    }

    private static QName qnameFor(final String localName) {
        return QName.create(NS, localName);
    }

    private static ContainerNode containerNode(final QName qname, DataContainerChild child) {
        return Builders.containerBuilder().withNodeIdentifier(new NodeIdentifier(qname)).addChild(child).build();
    }

    private static LeafNode<?> leafNode(final QName qname, final Object value) {
        return Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(qname)).withValue(value).build();
    }

    private static MapNode mapNodeWithLeafValue(final QName listQName, final QName leafQName, final Object leafValue) {
        return Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(listQName))
                .withChild(Builders.mapEntryBuilder()
                        .withNodeIdentifier(NodeIdentifierWithPredicates.of(listQName, leafQName, leafValue))
                        .addChild(leafNode(QN_LF, TEST)).build()).build();
    }

    private static SchemaTreeInference inferenceOf(final QName eventQName) {
        return DefaultSchemaTreeInference.of(schemaContext, Absolute.of(eventQName));
    }

    private static String fromResource(final String resource) {
        try {
            return Files.readString(Paths.get(YT786Test.class.getResource(resource).toURI()));
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException(e);
        }
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