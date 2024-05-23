/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.loadTextFile;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Objects;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.stream.LoggingNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class StreamToNormalizedNodeTest extends AbstractComplexJsonTest {
    private static final Logger LOG = LoggerFactory.getLogger(StreamToNormalizedNodeTest.class);
    private static String streamAsString;

    @BeforeAll
    static void initialization() throws Exception {
        streamAsString = loadTextFile(new File(StreamToNormalizedNodeTest.class.getResource(
                "/complexjson/complex-json.json").toURI()));
    }

    @AfterAll
    static void cleanup() {
        streamAsString = null;
    }

    /**
     * Demonstrates how to log events produced by a {@link JsonReader}.
     */
    @Test
    void ownStreamWriterImplementationDemonstration() throws Exception {
        // GSON's JsonReader reading from the loaded string (our event source)
        final var reader = new JsonReader(new StringReader(streamAsString));

        // StreamWriter which outputs SLF4J events
        final var logWriter = new LoggingNormalizedNodeStreamWriter();

        // JSON -> StreamWriter parser
        try (var jsonHandler = JsonParserStream.create(logWriter, lhotkaCodecFactory)) {
            // Process multiple readers, flush()/close() as needed
            jsonHandler.parse(reader);
        }
    }

    /**
     * Demonstrates how to create an immutable NormalizedNode tree from a {@link JsonReader} and
     * then writes the data back into string representation.
     */
    @Test
    void immutableNormalizedNodeStreamWriterDemonstration() throws Exception {
        /*
         * This is the parsing part
         */
        // This is where we will output the nodes
        final var result = new NormalizationResultHolder();

        // StreamWriter which attaches NormalizedNode under parent
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        // JSON -> StreamWriter parser
        try (var handler = JsonParserStream.create(streamWriter, lhotkaCodecFactory)) {
            handler.parse(new JsonReader(new StringReader(streamAsString)));
        }

        // Finally build the node
        final var parsedData = result.getResult().data();

        /*
         * This is the serialization part.
         */
        // We want to write the first child out
        final var firstChild = (DataContainerChild) parsedData;

        // String holder
        final var writer = new StringWriter();

        // StreamWriter which outputs JSON strings
        // StreamWriter which outputs JSON strings
        final var jsonStream = JSONNormalizedNodeStreamWriter.createExclusiveWriter(
            lhotkaCodecFactory, JsonWriterFactory.createJsonWriter(writer, 2));

        // NormalizedNode -> StreamWriter
        final var nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream);

        // Write multiple NormalizedNodes fluently, flush()/close() as needed
        nodeWriter.write(firstChild).close();

        // Just to put it somewhere
        LOG.debug("Serialized JSON: {}", writer.toString());
    }

    @Test
    void immutableMapNodeStreamWriterDemonstration() throws Exception {
        var configId = new YangInstanceIdentifier.NodeIdentifier(QName.create(
            "http://example.com/basic-model", "config"));
        var interfacesQName = QName.create("http://example.com/basic-model", "interfaces");
        var interfaceQName = QName.create("http://example.com/basic-model", "interface");
        var nameQName = QName.create("http://example.com/basic-model", "name");

        var mtu = ImmutableNodes.leafNode(QName.create("http://example.com/basic-model", "mtu"), 1500);
        var name = ImmutableNodes.leafNode(QName.create("http://example.com/basic-model", "name"), "admin");
        var loopbackMode = ImmutableNodes.leafNode(QName.create(
            "http://example.com/basic-model", "loopback-mode"), false);
        var enabled = ImmutableNodes.leafNode(QName.create("http://example.com/basic-model", "enabled"), false);
        var type = ImmutableNodes.leafNode(QName.create("http://example.com/basic-model", "type"), "IF_ETHERNET");

        var interfaceIdWithPredicates = YangInstanceIdentifier.NodeIdentifierWithPredicates.of(
            interfaceQName, nameQName, "eth3");

        var path = YangInstanceIdentifier.builder()
            .node(interfacesQName)
            .node(interfaceQName)
            .nodeWithKey(interfaceQName, interfaceIdWithPredicates.asMap())
            .build();

        var configNodeBuilder = ImmutableNodes.newContainerBuilder().withNodeIdentifier(configId);
        configNodeBuilder.withChild(mtu).withChild(name).withChild(loopbackMode).withChild(enabled).withChild(type);

        var interfaceNode = ImmutableNodes.newMapEntryBuilder().withNodeIdentifier(interfaceIdWithPredicates)
            .withChild(configNodeBuilder.build())
            .withChild(ImmutableNodes.leafNode(QName.create("http://example.com/basic-model", "name"), "eth3"))
            .build();

        EffectiveModelContext context = YangParserTestUtils.parseYangResourceDirectory("/complexjson/yang");

        var inference = DataSchemaContextTree.from(context)
            .enterPath(Objects.requireNonNull(path))
            .orElseThrow()
            .stack()
            .toInference();

        var writer = new StringWriter();
        var jsonWriter = new JsonWriter(writer);
        var namespace = interfaceNode.name().getNodeType().getNamespace();
        var nodeWriter = JSONNormalizedNodeStreamWriter
            .createNestedWriter(lhotkaCodecFactory, inference, namespace, jsonWriter);
        var normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(nodeWriter);
        normalizedNodeWriter.write(interfaceNode);
    }

}
