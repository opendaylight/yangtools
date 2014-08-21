package org.opendaylight.yangtools.yang.data.codec.gson;

/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.serialization.JSONNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.serialization.NormalizedNodeToStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangContextParser;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

public class StreamToNormalizedNodeTest {

    private static SchemaContext schemaContext;
    private static String streamAsString;

    @BeforeClass
    public static void initialization() throws IOException {
        schemaContext = loadModules("/complexjson/yang");
        streamAsString = loadTextFile(StreamToNormalizedNodeTest.class.getResource("/complexjson/complex-json.json")
                .getPath());
    }

    @Test
    public void ownStreamWriterImplementationDemonstration() throws IOException {
        JsonParserStream parser = new JsonParserStream(new NormalizedNodeStreamWriterImpl(), schemaContext);
        parser.parse(new JsonReader(new StringReader(streamAsString)), schemaContext);

    }

    @Test
    public void immutableNormalizedNodeStreamWriterDemonstration() throws IOException {
        NormalizedNodeContainerBuilder<YangInstanceIdentifier.NodeIdentifier, ?, ?, ? extends NormalizedNode<?, ?>> result = Builders
                .containerBuilder();
        result.withNodeIdentifier(new NodeIdentifier(QName.create("dummy", "2014-12-31", "dummy")));
        NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        JsonParserStream parser = new JsonParserStream(streamWriter, schemaContext);
        parser.parse(new JsonReader(new StringReader(streamAsString)), schemaContext);
        NormalizedNode<?, ?> parsedData = result.build();
        DataContainerChild<? extends PathArgument, ?> firstChild = ((ContainerNode) parsedData).getValue().iterator()
                .next();
        System.out.println(parsedData);

        OutputStream outputStream = new ByteArrayOutputStream();
        Writer w = new OutputStreamWriter(outputStream);
        NormalizedNodeStreamWriter nnswToJson = JSONNormalizedNodeStreamWriter.create(schemaContext, w, 2);

        new NormalizedNodeToStreamWriter().serialize(firstChild, nnswToJson);

        // FIXME: should be called in the writer
        w.close();

        System.out.print(outputStream.toString());
    }

    private static SchemaContext loadModules(final String resourceDirectory) throws IOException {
        YangContextParser parser = new YangParserImpl();
        String path = StreamToNormalizedNodeTest.class.getResource(resourceDirectory).getPath();
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

    private static String loadTextFile(final String filePath) throws IOException {
        FileReader fileReader = new FileReader(filePath);
        BufferedReader bufReader = new BufferedReader(fileReader);

        String line = null;
        StringBuilder result = new StringBuilder();
        while ((line = bufReader.readLine()) != null) {
            result.append(line);
        }
        bufReader.close();
        return result.toString();
    }

    private class NormalizedNodeStreamWriterImpl implements NormalizedNodeStreamWriter {

        int indent = 0;

        private String ind() {
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < indent; i++) {
                builder.append(" ");
            }
            return builder.toString();
        }

        @Override
        public void startUnkeyedListItem(final NodeIdentifier name, final int childSizeHint) throws IllegalStateException {
            System.out.println(ind() + name + "[](no key)");
            indent += 2;
        }

        @Override
        public void startUnkeyedList(final NodeIdentifier name, final int childSizeHint) throws IllegalArgumentException {
            System.out.println(ind() + name + "(no key)");
            indent += 2;
        }

        @Override
        public void startOrderedMapNode(final NodeIdentifier name, final int childSizeHint) throws IllegalArgumentException {

        }

        @Override
        public void startMapNode(final NodeIdentifier name, final int childSizeHint) throws IllegalArgumentException {
            System.out.println(ind() + name + "(key)");
            indent += 2;
        }

        @Override
        public void startMapEntryNode(final NodeIdentifierWithPredicates identifier, final int childSizeHint)
                throws IllegalArgumentException {
            System.out.println(ind() + identifier + "[](key)");
            indent += 2;
        }

        @Override
        public void startLeafSet(final NodeIdentifier name, final int childSizeHint) throws IllegalArgumentException {
            System.out.println(ind() + name + "(leaf-list)");
            indent += 2;
        }

        @Override
        public void startContainerNode(final NodeIdentifier name, final int childSizeHint) throws IllegalArgumentException {
            System.out.println(ind() + name + "(container)");
            indent += 2;
        }

        @Override
        public void startChoiceNode(final NodeIdentifier name, final int childSizeHint) throws IllegalArgumentException {
            System.out.println(ind() + name + "(choice)");
            indent += 2;
        }

        @Override
        public void startAugmentationNode(final AugmentationIdentifier identifier) throws IllegalArgumentException {
            System.out.println(ind() + identifier + "(augmentation)");
            indent += 2;
        }

        @Override
        public void leafSetEntryNode(final Object value) throws IllegalArgumentException {
            System.out.println(ind() + value + "(" + value.getClass().getSimpleName() + ") ");
        }

        @Override
        public void leafNode(final NodeIdentifier name, final Object value) throws IllegalArgumentException {
            System.out.println(ind() + name + "(leaf" + "(" + value.getClass().getSimpleName() + ")" + ")=" + value);
        }

        @Override
        public void endNode() throws IllegalStateException {
            indent -= 2;
            System.out.println(ind() + "(end)");
        }

        @Override
        public void anyxmlNode(final NodeIdentifier name, final Object value) throws IllegalArgumentException {
            System.out.println(ind() + name + "(anyxml)=" + value);
        }

        @Override
        public void flush() throws IOException {
            System.out.flush();
        }
    }
}
