/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.Assert.assertEquals;

import com.google.common.io.BaseEncoding;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug5446Test {

    private static QNameModule fooModuleQName;
    private static QName rootQName;
    private static QName ipAddressQName;
    private static SchemaContext schemaContext;

    @BeforeClass
    public static void init() throws Exception {
        fooModuleQName = QNameModule.create(new URI("foo"), SimpleDateFormatUtil.getRevisionFormat()
                .parse("2015-11-05"));
        rootQName = QName.create(fooModuleQName, "root");
        ipAddressQName = QName.create(fooModuleQName, "ip-address");

        schemaContext = YangParserTestUtils.parseYangResources(Bug5446Test.class, "/bug5446/yang/foo.yang");
    }

    @Test
    public void test() throws IOException, JsonIOException, JsonSyntaxException, URISyntaxException {
        final DataContainerChild<? extends PathArgument, ?> rootNode = createRootNode();

        final Writer writer = new StringWriter();
        final String jsonOutput = normalizedNodeToJsonStreamTransformation(writer, rootNode);

        final JsonParser parser = new JsonParser();
        final JsonElement serializedJson = parser.parse(jsonOutput);
        final JsonElement expextedJson = parser.parse(new FileReader(new File(getClass().getResource(
                "/bug5446/json/foo.json").toURI())));

        assertEquals(expextedJson, serializedJson);
    }

    private static String normalizedNodeToJsonStreamTransformation(final Writer writer,
            final NormalizedNode<?, ?> inputStructure) throws IOException {

        final NormalizedNodeStreamWriter jsonStream = JSONNormalizedNodeStreamWriter.createExclusiveWriter(
                JSONCodecFactory.getShared(schemaContext), SchemaPath.ROOT, null,
                JsonWriterFactory.createJsonWriter(writer, 2));
        final NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream);
        nodeWriter.write(inputStructure);

        nodeWriter.close();
        return writer.toString();
    }

    private static ContainerNode createRootNode() {
        LeafNode<byte[]> ipAddress = ImmutableNodes.leafNode(ipAddressQName, BaseEncoding.base64().decode("fwAAAQ=="));
        return ImmutableContainerNodeBuilder.create().withNodeIdentifier(new NodeIdentifier(rootQName))
                .withChild(ipAddress).build();
    }
}
