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
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.LoggingNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamToNormalizedNodeTest {
    private static final Logger LOG = LoggerFactory.getLogger(StreamToNormalizedNodeTest.class);
    private static SchemaContext schemaContext;
    private static String streamAsString;

    @BeforeClass
    public static void initialization() throws IOException, URISyntaxException, ReactorException {
        schemaContext = YangParserTestUtils.parseYangSources("/complexjson/yang");
        streamAsString = loadTextFile(new File(StreamToNormalizedNodeTest.class.getResource(
                "/complexjson/complex-json.json").toURI()));
    }

    /**
     * Demonstrates how to log events produced by a {@link JsonReader}.
     */
    @Test
    public void ownStreamWriterImplementationDemonstration() throws IOException {
        // GSON's JsonReader reading from the loaded string (our event source)
        final JsonReader reader = new JsonReader(new StringReader(streamAsString));

        // StreamWriter which outputs SLF4J events
        final LoggingNormalizedNodeStreamWriter logWriter = new LoggingNormalizedNodeStreamWriter();

        // JSON -> StreamWriter parser
        try (JsonParserStream jsonHandler = JsonParserStream.create(logWriter, schemaContext)) {
            // Process multiple readers, flush()/close() as needed
            jsonHandler.parse(reader);
        }
    }

    /**
     * Demonstrates how to create an immutable NormalizedNode tree from a {@link JsonReader} and
     * then writes the data back into string representation.
     */
    @Ignore
    @Test
    public void immutableNormalizedNodeStreamWriterDemonstration() throws IOException {
        /*
         * This is the parsing part
         */
        // This is where we will output the nodes
        NormalizedNodeResult result = new NormalizedNodeResult();

        // StreamWriter which attaches NormalizedNode under parent
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        // JSON -> StreamWriter parser
        try (JsonParserStream handler = JsonParserStream.create(streamWriter, schemaContext)) {
            handler.parse(new JsonReader(new StringReader(streamAsString)));
        }

        // Finally build the node
        final NormalizedNode<?, ?> parsedData = result.getResult();
        LOG.debug("Parsed NormalizedNodes: {}", parsedData);

        /*
         * This is the serialization part.
         */
        // We want to write the first child out
        final DataContainerChild<? extends PathArgument, ?> firstChild =
                (DataContainerChild<? extends PathArgument, ?>) parsedData;
        LOG.debug("Serializing first child: {}", firstChild);

        // String holder
        final StringWriter writer = new StringWriter();

        // StreamWriter which outputs JSON strings
        // StreamWriter which outputs JSON strings
        final NormalizedNodeStreamWriter jsonStream = JSONNormalizedNodeStreamWriter.createExclusiveWriter(
            JSONCodecFactory.getShared(schemaContext), SchemaPath.ROOT, null,
            JsonWriterFactory.createJsonWriter(writer, 2));

        // NormalizedNode -> StreamWriter
        final NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream);

        // Write multiple NormalizedNodes fluently, flush()/close() as needed
        nodeWriter.write(firstChild).close();

        // Just to put it somewhere
        LOG.debug("Serialized JSON: {}", writer.toString());
    }
}
