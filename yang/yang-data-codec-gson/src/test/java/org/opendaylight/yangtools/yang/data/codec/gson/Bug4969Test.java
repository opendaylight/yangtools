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
import java.io.File;
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
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug4969Test {

    @Test
    public void newParserLeafRefTest() throws SourceException, ReactorException, URISyntaxException, IOException {
        File sourceDir = new File(Bug4969Test.class.getResource("/bug-4969/yang").toURI());
        SchemaContext context = YangParserTestUtils.parseYangSources(sourceDir.listFiles());
        assertNotNull(context);

        verifyNormalizedNodeResult(context);
    }

    private static void verifyNormalizedNodeResult(final SchemaContext context) throws IOException, URISyntaxException {
        final String inputJson = TestUtils.loadTextFile("/bug-4969/json/foo.json");
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(context));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();

        assertTrue(transformedInput instanceof ContainerNode);
        ContainerNode root = (ContainerNode) transformedInput;
        Optional<DataContainerChild<? extends PathArgument, ?>> ref1 = root.getChild(NodeIdentifier.create((QName
                .create("foo", "2016-01-22", "ref1"))));
        Optional<DataContainerChild<? extends PathArgument, ?>> ref2 = root.getChild(NodeIdentifier.create((QName
                .create("foo", "2016-01-22", "ref2"))));
        Optional<DataContainerChild<? extends PathArgument, ?>> ref3 = root.getChild(NodeIdentifier.create((QName
                .create("foo", "2016-01-22", "ref3"))));
        Optional<DataContainerChild<? extends PathArgument, ?>> ref4 = root.getChild(NodeIdentifier.create((QName
                .create("foo", "2016-01-22", "ref4"))));

        assertTrue(ref1.isPresent());
        assertTrue(ref2.isPresent());
        assertTrue(ref3.isPresent());
        assertTrue(ref4.isPresent());

        Object value1 = ref1.get().getValue();
        Object value2 = ref2.get().getValue();
        Object value3 = ref3.get().getValue();
        Object value4 = ref4.get().getValue();

        assertTrue(value1 instanceof Set);
        assertTrue(value2 instanceof Set);
        assertTrue(value3 instanceof Set);
        assertTrue(value4 instanceof Set);

        Set<?> set1 = (Set<?>) value1;
        Set<?> set2 = (Set<?>) value2;
        Set<?> set3 = (Set<?>) value3;
        Set<?> set4 = (Set<?>) value4;

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
    public void newParserLeafRefTest2() throws SourceException, ReactorException, URISyntaxException, IOException {
        File sourceDir = new File(Bug4969Test.class.getResource("/leafref/yang").toURI());
        SchemaContext context = YangParserTestUtils.parseYangSources(sourceDir.listFiles());
        assertNotNull(context);

        parseJsonToNormalizedNodes(context);
    }

    private static void parseJsonToNormalizedNodes(final SchemaContext context) throws IOException, URISyntaxException {
        final String inputJson = TestUtils.loadTextFile("/leafref/json/data.json");
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(context));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }
}
