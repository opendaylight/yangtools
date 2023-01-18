/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

/**
 * Tests addressing notification support by json parser.
 */
class YT786Test {

    private static final XMLNamespace XML_NS = XMLNamespace.of("test:notification");
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

    private static final String JSON_LEAF = fromResource("/YT786/leaf-event.json");
    private static final String JSON_GROUP = fromResource("/YT786/group-event.json");
    private static final String JSON_CONTAINER = fromResource("/YT786/container-event.json");
    private static final String JSON_LIST = fromResource("/YT786/list-event.json");

    private static EffectiveModelContext schemaContext;
    private static JSONCodecFactory jsonCodecFactory;

    @BeforeAll
    static void beforeAll() {
        schemaContext = YangParserTestUtils.parseYangResource("/YT786/notification-test.yang");
        jsonCodecFactory = JSONCodecFactorySupplier.RFC7951.getShared(schemaContext);
    }

    @AfterAll
    static void afterAll() {
        schemaContext = null;
        jsonCodecFactory = null;
    }

    @ParameterizedTest(name = "Parsing notification: {0}")
    @MethodSource("testArgs")
    void parseNotification(final QName qname, final String json, final NormalizedNode expected) {

        // ensure notification with QName is known
        assertTrue(schemaContext.getNotifications().stream().anyMatch(n -> qname.equals(n.getQName())));

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, jsonCodecFactory, inferenceOf(qname));
        jsonParser.parse(new JsonReader(new StringReader(json)));

        assertEquals(expected, result.getResult());
    }

    @ParameterizedTest(name = "Write notification: {0}")
    @MethodSource("testArgs")
    void writeNotification(final QName qname, final String expected, final NormalizedNode normalized) throws Exception {

        final Writer writer = new StringWriter();
        final NormalizedNodeStreamWriter jsonStreamWriter = JSONNormalizedNodeStreamWriter.createExclusiveWriter(
                jsonCodecFactory, inferenceOf(qname), XML_NS, JsonWriterFactory.createJsonWriter(writer, 2));
        try (NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStreamWriter)) {
            nodeWriter.write(normalized);
        }
        assertEquals(expected, writer.toString());
    }

    private static Stream<Arguments> testArgs() {
        // notification definition qname, notification data as json, as normalized node
        return Stream.of(
                Arguments.of(QN_LEAF_EVENT, JSON_LEAF, NN_LEAF),
                Arguments.of(QN_GROUP_EVENT, JSON_GROUP, NN_GROUP),
                Arguments.of(QN_CONTAINER_EVENT, JSON_CONTAINER, NN_CONTAINER),
                Arguments.of(QN_LIST_EVENT, JSON_LIST, NN_LIST)
        );
    }

    private static QName qnameFor(final String localName) {
        return QName.create(XML_NS, localName);
    }

    private static ContainerNode containerNode(final QName qname, final DataContainerChild child) {
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
}