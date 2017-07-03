/*
 * Copyright (c) 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.loadTextFile;

import com.google.gson.stream.JsonReader;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;


/**
 * Created by geng.xingyuan@zte.com.cn  on 2017/6/5.
 */
public class RpcInputOutputTest {
    private static SchemaContext schemaContext;

    @BeforeClass
    public static void initialization() throws IOException, URISyntaxException, ReactorException {
        schemaContext = YangParserTestUtils.parseYangSources("/rpc/yang");
    }

    @Test
    public void RpcInputTest() throws IOException, URISyntaxException {
        String inputJson = loadTextFile("/rpc/json/rpc-input.json");
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        RpcDefinition rpc = schemaContext.getOperations().stream().findFirst().get();
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, schemaContext,rpc);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();

        final StringWriter writer = new StringWriter();

        SchemaPath path = SchemaPath.create(true,rpc.getPath().getLastComponent(), QName.create("org:opendaylight:test","input"));
        String outputJson = normalizedNodeToJsonStreamTransformation(writer,path,transformedInput);
        assertTrue(outputJson.contains("lf-test"));
    }

    @Test
    public void RpcOutputTest() throws IOException, URISyntaxException {
        String inputJson = loadTextFile("/rpc/json/rpc-output.json");
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        RpcDefinition rpc = schemaContext.getOperations().stream().findFirst().get();
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, schemaContext,rpc);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();

        final StringWriter writer = new StringWriter();

        SchemaPath path = SchemaPath.create(true,rpc.getPath().getLastComponent(), QName.create("org:opendaylight:test","output"));
        String outputJson = normalizedNodeToJsonStreamTransformation(writer,path,transformedInput);
        assertTrue(outputJson.contains("lf-test"));
    }

    private static String normalizedNodeToJsonStreamTransformation(final Writer writer, SchemaPath path,
                                                                   final NormalizedNode<?, ?> inputStructure) throws IOException {

        final NormalizedNodeStreamWriter jsonStream = JSONNormalizedNodeStreamWriter.
                createExclusiveWriter(JSONCodecFactory.create(schemaContext), path, null,
                        JsonWriterFactory.createJsonWriter(writer, 2));
        final NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream);
        nodeWriter.write(inputStructure);

        nodeWriter.close();
        return writer.toString();
    }
}

