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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.stream.LoggingNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamToNormalizedNodeTest extends AbstractComplexJsonTest {
    private static final Logger LOG = LoggerFactory.getLogger(StreamToNormalizedNodeTest.class);
    private static String streamAsString;

    @BeforeClass
    public static void initialization() throws IOException, URISyntaxException {
        streamAsString = loadTextFile(new File(StreamToNormalizedNodeTest.class.getResource(
                "/complexjson/complex-json.json").toURI()));
    }

    @AfterClass
    public static void cleanup() {
        streamAsString = null;
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
        try (JsonParserStream jsonHandler = JsonParserStream.create(logWriter, lhotkaCodecFactory)) {
            // Process multiple readers, flush()/close() as needed
            jsonHandler.parse(reader);
        }
    }

    /**
     * Demonstrates how to create an immutable NormalizedNode tree from a {@link JsonReader} and
     * then writes the data back into string representation.
     */
    @Test
    public void immutableNormalizedNodeStreamWriterDemonstration() throws IOException {
        /*
         * This is the parsing part
         */
        // This is where we will output the nodes
        var result = new NormalizationResultHolder();

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
        final DataContainerChild firstChild = (DataContainerChild) parsedData;

        // String holder
        final StringWriter writer = new StringWriter();

        // StreamWriter which outputs JSON strings
        // StreamWriter which outputs JSON strings
        final NormalizedNodeStreamWriter jsonStream = JSONNormalizedNodeStreamWriter.createExclusiveWriter(
            lhotkaCodecFactory, JsonWriterFactory.createJsonWriter(writer, 2));

        // NormalizedNode -> StreamWriter
        final NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream);

        // Write multiple NormalizedNodes fluently, flush()/close() as needed
        nodeWriter.write(firstChild).close();

        // Just to put it somewhere
        LOG.debug("Serialized JSON: {}", writer.toString());
    }
}
