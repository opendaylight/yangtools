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

import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug4969Test {

    @Test
    public void newParserLeafRefTest() throws IOException, URISyntaxException {
        EffectiveModelContext context = YangParserTestUtils.parseYangResourceDirectory("/bug-4969/yang");
        assertNotNull(context);

        verifyNormalizedNodeResult(context);
    }

    private static void verifyNormalizedNodeResult(final EffectiveModelContext context) throws IOException,
            URISyntaxException {
        final String inputJson = TestUtils.loadTextFile("/bug-4969/json/foo.json");
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(context));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode transformedInput = result.getResult();

        assertTrue(transformedInput instanceof ContainerNode);
        ContainerNode root = (ContainerNode) transformedInput;
        final DataContainerChild ref1 = root.childByArg(NodeIdentifier.create(
            QName.create("foo", "2016-01-22", "ref1")));
        final DataContainerChild ref2 = root.childByArg(NodeIdentifier.create(
            QName.create("foo", "2016-01-22", "ref2")));
        final DataContainerChild ref3 = root.childByArg(NodeIdentifier.create(
            QName.create("foo", "2016-01-22", "ref3")));
        final DataContainerChild ref4 = root.childByArg(NodeIdentifier.create(
            QName.create("foo", "2016-01-22", "ref4")));

        assertNotNull(ref1);
        assertNotNull(ref2);
        assertNotNull(ref3);
        assertNotNull(ref4);

        assertEquals("01", ref1.body()); // bit "a" (0)
        assertEquals("03", ref2.body()); // bits "a","b" (0,1)
        assertEquals("07", ref3.body()); // bits "a","b","c" (0-2)
        assertEquals("0F", ref4.body()); // bits "a","b","c","d" (0-3)
    }

    @Test
    public void newParserLeafRefTest2() throws URISyntaxException, IOException {
        EffectiveModelContext context = YangParserTestUtils.parseYangResourceDirectory("/leafref/yang");
        assertNotNull(context);

        parseJsonToNormalizedNodes(context);
    }

    private static void parseJsonToNormalizedNodes(final EffectiveModelContext context) throws IOException,
            URISyntaxException {
        final String inputJson = TestUtils.loadTextFile("/leafref/json/data.json");
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter,
            JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(context));
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));
        final NormalizedNode transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }
}
