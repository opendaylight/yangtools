/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.loadTextFile;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.containerBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.leafNode;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import com.google.gson.stream.JsonReader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class NC747Test {
    static EffectiveModelContext SCHEMA_CONTEXT;
    private static JSONCodecFactory CODEC_FACTORY;

    @BeforeClass
    public static void createFactory() {
        SCHEMA_CONTEXT = YangParserTestUtils.parseYangResourceDirectory("/netconf747/yang");
        CODEC_FACTORY = JSONCodecFactorySupplier.RFC7951.getShared(SCHEMA_CONTEXT);
    }

    @AfterClass
    public static void destroyFactory() {
        CODEC_FACTORY = null;
    }

    @Test
    public void testNetconf747() throws IOException, URISyntaxException {
        final String inputJson = loadTextFile("/netconf747/data/data.json");

        var cRoot = QName.create("test-ns", "container-root");
        var cLvl1 = QName.create("test-ns", "container-lvl1");
        var cAug = QName.create("test-ns-aug", "container-aug");

        final NormalizedNode expectedData = containerBuilder().withNodeIdentifier(new YangInstanceIdentifier
                .NodeIdentifier(cAug))
                .withChild(leafNode(QName.create("test-ns-aug", "leaf-aug"), "data"))
                .build();

        var inf = SchemaInferenceStack.Inference.ofDataTreePath(SCHEMA_CONTEXT, cRoot, cLvl1);
        final NormalizedNode data = fromJSON(inputJson, inf);

        // This code will solve issue with wrong data, but we should already have it from JsonParserStream without this
        // lines
//        if (data instanceof AugmentationNode) {
//            data = ((DataContainerNode) data).body().iterator().next();
//        }

        assertNotNull(data);
        assertEquals(expectedData, data);
    }

    JSONCodecFactory codecFactory() {
        return CODEC_FACTORY;
    }

    final NormalizedNode fromJSON(final String input, final SchemaInferenceStack.Inference parentNode) {
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, codecFactory(), parentNode);
        jsonParser.parse(new JsonReader(new StringReader(input)));
        return result.getResult();
    }
}
