/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.Assert.assertNotNull;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.loadTextFile;

import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug8083Test {

    @Ignore("Instance-identifier codec has to be modified according to the RFC7951 to correctly parse this.")
    @Test
    public void testRFC7951InstanceIdentifierPath() throws IOException, URISyntaxException {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResourceDirectory("/bug8083/yang/");
        final String inputJson = loadTextFile("/bug8083/json/foo.json");

        // deserialization
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, schemaContext);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Ignore("JSONEmptyCodec needs to be fixed first.")
    @Test
    public void testInstanceIdentifierPathWithEmptyListKey() throws IOException, URISyntaxException {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResource("/bug8083/yang/baz.yang");
        final String inputJson = loadTextFile("/bug8083/json/baz.json");

        // deserialization
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, schemaContext);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testInstanceIdentifierPathWithIdentityrefListKey() throws IOException, URISyntaxException {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResource("/bug8083/yang/zab.yang");
        final String inputJson = loadTextFile("/bug8083/json/zab.json");

        // deserialization
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, schemaContext);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testInstanceIdentifierPathWithInstanceIdentifierListKey() throws IOException, URISyntaxException {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResource("/bug8083/yang/foobar.yang");
        final String inputJson = loadTextFile("/bug8083/json/foobar.json");

        // deserialization
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, schemaContext);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }
}
