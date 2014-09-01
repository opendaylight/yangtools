/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.LoggingNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangContextParser;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamToNormalizedNodeTest {
    private static final Logger LOG = LoggerFactory.getLogger(StreamToNormalizedNodeTest.class);
    private static SchemaContext schemaContext;
    private static String streamAsString;

    @BeforeClass
    public static void initialization() throws IOException, URISyntaxException {
        schemaContext = loadModules("/complexjson/yang");
        streamAsString = loadTextFile(new File(StreamToNormalizedNodeTest.class.getResource(
                "/complexjson/complex-json.json").toURI()));
    }

    /**
     * Demonstrates how to log events produced by a {@link JsonReader}.
     *
     * @throws IOException
     */
    @Test
    public void ownStreamWriterImplementationDemonstration() throws IOException {
        // GSON's JsonReader reading from the loaded string (our event source)
        final JsonReader reader = new JsonReader(new StringReader(streamAsString));

        // StreamWriter which outputs SLF4J events
        final LoggingNormalizedNodeStreamWriter logWriter = new LoggingNormalizedNodeStreamWriter();

        // JSON -> StreamWriter parser
        try (final JsonParserStream jsonHandler = JsonParserStream.create(logWriter, schemaContext)) {
            // Process multiple readers, flush()/close() as needed
            jsonHandler.parse(reader);
        }
    }

    /**
     * Demonstrates how to create an immutable NormalizedNode tree from a {@link JsonReader} and
     * then writes the data back into string representation.
     *
     * @throws IOException
     */
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
        final DataContainerChild<? extends PathArgument, ?> firstChild = (DataContainerChild<? extends PathArgument, ?>) parsedData;
        LOG.debug("Serializing first child: {}", firstChild);

        // String holder
        final StringWriter writer = new StringWriter();

        // StreamWriter which outputs JSON strings
        final NormalizedNodeStreamWriter jsonStream = JSONNormalizedNodeStreamWriter.create(schemaContext, writer, 2);

        // NormalizedNode -> StreamWriter
        final NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream);

        // Write multiple NormalizedNodes fluently, flush()/close() as needed
        nodeWriter.write(firstChild).close();

        // Just to put it somewhere
        LOG.debug("Serialized JSON: {}", writer.toString());
    }

    private static SchemaContext loadModules(final String resourceDirectory) throws IOException, URISyntaxException {
        YangContextParser parser = new YangParserImpl();
        URI path = StreamToNormalizedNodeTest.class.getResource(resourceDirectory).toURI();
        final File testDir = new File(path);
        final String[] fileList = testDir.list();
        final List<File> testFiles = new ArrayList<File>();
        if (fileList == null) {
            throw new FileNotFoundException(resourceDirectory);
        }
        for (String fileName : fileList) {
            if (new File(testDir, fileName).isDirectory() == false) {
                testFiles.add(new File(testDir, fileName));
            }
        }
        return parser.parseFiles(testFiles);
    }

    private static String loadTextFile(final File file) throws IOException {
        FileReader fileReader = new FileReader(file);
        BufferedReader bufReader = new BufferedReader(fileReader);

        String line = null;
        StringBuilder result = new StringBuilder();
        while ((line = bufReader.readLine()) != null) {
            result.append(line);
        }
        bufReader.close();
        return result.toString();
    }
}
