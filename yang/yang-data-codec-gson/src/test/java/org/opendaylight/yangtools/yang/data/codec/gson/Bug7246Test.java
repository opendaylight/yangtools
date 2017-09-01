/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.Assert.assertEquals;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug7246Test {
    private static final String NS = "my-namespace";
    private static final String REV = "1970-01-01";

    @Test
    public void test() throws Exception {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResource("/bug7246/yang/rpc-test.yang");
        final JsonParser parser = new JsonParser();
        final JsonElement expextedJson = parser
                .parse(new FileReader(new File(getClass().getResource("/bug7246/json/expected-output.json").toURI())));

        final DataContainerChild<? extends PathArgument, ?> inputStructure = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifier(qN("my-name")))
                .withChild(ImmutableNodes.leafNode(new NodeIdentifier(qN("my-name")), "my-value")).build();
        final SchemaPath rootPath = SchemaPath.create(true, qN("my-name"), qN("input"));
        final Writer writer = new StringWriter();
        final String jsonOutput = normalizedNodeToJsonStreamTransformation(schemaContext, rootPath, writer,
                inputStructure);
        final JsonElement serializedJson = parser.parse(jsonOutput);

        assertEquals(expextedJson, serializedJson);
    }

    private static QName qN(final String localName) {
        return QName.create(NS, REV, localName);
    }

    private static String normalizedNodeToJsonStreamTransformation(final SchemaContext schemaContext,
            final SchemaPath path, final Writer writer, final NormalizedNode<?, ?> inputStructure)
            throws IOException, URISyntaxException {

        final NormalizedNodeStreamWriter jsonStream = JSONNormalizedNodeStreamWriter.createExclusiveWriter(
                JSONCodecFactory.getShared(schemaContext), path, new URI(NS),
                JsonWriterFactory.createJsonWriter(writer, 2));
        final NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream);
        nodeWriter.write(inputStructure);

        nodeWriter.close();
        return writer.toString();
    }
}
