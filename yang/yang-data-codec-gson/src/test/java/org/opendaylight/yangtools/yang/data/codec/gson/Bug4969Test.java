/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.Set;
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
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug4969Test {

    @Test
    public void newParserLeafRefTest() throws IOException, URISyntaxException {
        SchemaContext context = YangParserTestUtils.parseYangResourceDirectory("/bug-4969/yang");
        assertNotNull(context);

        verifyNormalizedNodeResult(context);
    }

    private static void verifyNormalizedNodeResult(final SchemaContext context) throws IOException, URISyntaxException {
        final String inputJson = TestUtils.loadTextFile("/bug-4969/json/foo.json");
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, context);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();

        assertTrue(transformedInput instanceof ContainerNode);
        ContainerNode root = (ContainerNode) transformedInput;
        final Optional<DataContainerChild<? extends PathArgument, ?>> ref1 = root.getChild(NodeIdentifier.create(
            QName.create("foo", "2016-01-22", "ref1")));
        final Optional<DataContainerChild<? extends PathArgument, ?>> ref2 = root.getChild(NodeIdentifier.create(
            QName.create("foo", "2016-01-22", "ref2")));
        final Optional<DataContainerChild<? extends PathArgument, ?>> ref3 = root.getChild(NodeIdentifier.create(
            QName.create("foo", "2016-01-22", "ref3")));
        final Optional<DataContainerChild<? extends PathArgument, ?>> ref4 = root.getChild(NodeIdentifier.create(
            QName.create("foo", "2016-01-22", "ref4")));

        assertTrue(ref1.isPresent());
        assertTrue(ref2.isPresent());
        assertTrue(ref3.isPresent());
        assertTrue(ref4.isPresent());

        final Object value1 = ref1.get().getValue();
        final Object value2 = ref2.get().getValue();
        final Object value3 = ref3.get().getValue();
        final Object value4 = ref4.get().getValue();

        assertTrue(value1 instanceof Set);
        assertTrue(value2 instanceof Set);
        assertTrue(value3 instanceof Set);
        assertTrue(value4 instanceof Set);

        final Set<?> set1 = (Set<?>) value1;
        final Set<?> set2 = (Set<?>) value2;
        final Set<?> set3 = (Set<?>) value3;
        final Set<?> set4 = (Set<?>) value4;

        assertEquals(1, set1.size());
        assertEquals(2, set2.size());
        assertEquals(3, set3.size());
        assertEquals(4, set4.size());

        assertTrue(set1.contains("a"));
        assertTrue(set2.contains("a") && set2.contains("b"));
        assertTrue(set3.contains("a") && set3.contains("b") && set3.contains("c"));
        assertTrue(set4.contains("a") && set4.contains("b") && set4.contains("c") && set4.contains("d"));
    }

    @Test
    public void newParserLeafRefTest2() throws URISyntaxException, IOException {
        SchemaContext context = YangParserTestUtils.parseYangResourceDirectory("/leafref/yang");
        assertNotNull(context);

        parseJsonToNormalizedNodes(context);
    }

    private static void parseJsonToNormalizedNodes(final SchemaContext context) throws IOException, URISyntaxException {
        final String inputJson = TestUtils.loadTextFile("/leafref/json/data.json");
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, context);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }
}
