/*
 * Copyright (c) 2016 Intel Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.loadTextFile;

import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.Optional;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug6112Test {
    private static SchemaContext schemaContext;

    @BeforeClass
    public static void initialization() throws IOException, URISyntaxException, ReactorException {
        schemaContext = YangParserTestUtils.parseYangSources("/bug-6112/yang");
    }

    private static NormalizedNode<?, ?> readJson(final String jsonPath) throws IOException, URISyntaxException {
        final String inputJson = loadTextFile(jsonPath);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, schemaContext);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        return result.getResult();
    }

    @Test
    public void testUnionIdentityrefInput() throws IOException, URISyntaxException {
        final NormalizedNode<?, ?> transformedInput = readJson("/bug-6112/json/data-identityref.json");
        assertTrue(transformedInput instanceof ContainerNode);
        ContainerNode root = (ContainerNode) transformedInput;
        Optional<DataContainerChild<? extends PathArgument, ?>> leafValue = root.getChild(NodeIdentifier.create(
            QName.create("union:identityref:test", "2016-07-12", "leaf-value")));

        assertTrue(leafValue.isPresent());
        Object value = leafValue.get().getValue();
        assertTrue(value instanceof QName);
        QName identityref = (QName) value;
        assertEquals(QName.create("union:identityref:test", "2016-07-12", "ident-one"), identityref);
    }

    @Test
    public void testUnionUint8Input() throws IOException, URISyntaxException {
        final NormalizedNode<?, ?> transformedInput = readJson("/bug-6112/json/data-uint8.json");
        assertTrue(transformedInput instanceof ContainerNode);
        ContainerNode root = (ContainerNode) transformedInput;
        Optional<DataContainerChild<? extends PathArgument, ?>> leafValue = root.getChild(NodeIdentifier.create(
            QName.create("union:identityref:test", "2016-07-12", "leaf-value")));

        assertTrue(leafValue.isPresent());
        Object value = leafValue.get().getValue();
        assertTrue(value instanceof Short);
        Short uint8Value = (Short) value;
        assertEquals(1, uint8Value.shortValue());
    }
}
