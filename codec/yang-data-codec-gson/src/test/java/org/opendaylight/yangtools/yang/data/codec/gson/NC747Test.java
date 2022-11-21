/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class NC747Test {
    private static final QName CONTAINER_ROOT = QName.create("test-ns", "container-root").intern();
    private static final QName CONTAINER_LVL_1 = QName.create("test-ns", "container-lvl1").intern();
    private static final QName CONTAINER_AUG = QName.create("test-ns-aug", "container-aug").intern();
    private static final QName DATA = QName.create("test-ns-aug", "leaf-aug").intern();

    private static EffectiveModelContext context;
    private static JSONCodecFactory factory;

    @BeforeClass
    public static void createFactory() {
        context = YangParserTestUtils.parseYangResourceDirectory("/netconf747/yang");
        factory = JSONCodecFactorySupplier.RFC7951.getShared(context);
    }

    /**
     * Test to read container data from JSON.
     * </p>
     * Assert that resulting data is a container and equals to expected data.
     */
    @Test
    public void testNetconf747() throws Exception {
        final var expectedData = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(CONTAINER_AUG))
                .withChild(ImmutableNodes.leafNode(DATA, "data"))
                .build();

        final var inputJson = TestUtils.loadTextFile("/netconf747/data/data.json");
        final var inf = Inference.ofDataTreePath(context,
                CONTAINER_ROOT, CONTAINER_LVL_1, CONTAINER_AUG);
        final var data = fromJSON(inputJson, inf);

        assertTrue(data instanceof ContainerNode);
        assertEquals(expectedData, data);
    }

    private static NormalizedNode fromJSON(final String input, final Inference inference) {
        final var result = new NormalizedNodeResult();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var jsonParser = JsonParserStream.create(streamWriter, factory, inference);
        jsonParser.parse(new JsonReader(new StringReader(input)));
        return result.getResult();
    }
}
