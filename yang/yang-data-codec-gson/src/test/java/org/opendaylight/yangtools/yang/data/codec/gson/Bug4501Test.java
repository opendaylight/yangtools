/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.loadTextFile;

import com.google.common.collect.ImmutableSet;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.Optional;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug4501Test {

    private static EffectiveModelContext schemaContext;

    @BeforeClass
    public static void initialization() {
        schemaContext = YangParserTestUtils.parseYangResourceDirectory("/bug-4501/yang");
    }

    @AfterClass
    public static void cleanup() {
        schemaContext = null;
    }

    @Test
    public void testCorrectInput() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/bug-4501/json/foo-correct.json");
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertTrue(transformedInput instanceof UnkeyedListNode);

        final UnkeyedListNode hop = (UnkeyedListNode) transformedInput;
        final Optional<DataContainerChild<? extends PathArgument, ?>> lrsBits = hop.getChild(0).getChild(
                NodeIdentifier.create(QName.create("foo", "lrs-bits")));

        assertEquals(ImmutableSet.of("lookup", "rloc-probe", "strict"), lrsBits.get().getValue());
    }

    @Test
    public void testIncorrectInput() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/bug-4501/json/foo-incorrect.json");
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext));

        final JsonReader reader = new JsonReader(new StringReader(inputJson));
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> jsonParser.parse(reader));
        assertEquals("Node '(foo)lrs-bits' has already set its value to '[lookup]'", ex.getMessage());
    }
}