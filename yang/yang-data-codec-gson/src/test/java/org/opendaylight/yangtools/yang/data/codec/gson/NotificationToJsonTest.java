/*
 * Copyright (c) 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.gson.stream.JsonReader;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
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

import static org.junit.Assert.assertTrue;

/**
 * Created by geng.xingyuan@zte.com.cn on 2017/6/5.
 */
public class NotificationToJsonTest {
    private static SchemaContext schemaContext;

    @BeforeClass
    public static void initialization() throws IOException, URISyntaxException, ReactorException {
        schemaContext = YangParserTestUtils.parseYangSources("/notification/yang");
    }
    @Test
    public void notificationToJsonTest() throws IOException, URISyntaxException  {
        String inputJson = loadTextFile("/notification/json/notification-cont.json");
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        NotificationDefinition notification = schemaContext.getNotifications().stream()
                .filter(o->o.getQName().getLocalName().equals("notifi-cont")).findFirst().get();
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, schemaContext,notification);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();

        final StringWriter writer = new StringWriter();
        String transformInputJson = normalizedNodeToJsonStreamTransformation(writer,notification.getPath(),transformedInput);
        assertTrue(transformInputJson.contains("lf-test"));
    }
    static String loadTextFile(final String relativePath) throws IOException, URISyntaxException {
        return loadTextFile(new File(TestUtils.class.getResource(relativePath).toURI()));
    }
    static String loadTextFile(final File file) throws IOException {
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
