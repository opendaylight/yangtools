/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1468Test {
    private static final QName CONTAINER_ROOT = QName.create("test-ns", "container-root").intern();
    private static final QName CONTAINER_LVL_1 = QName.create("test-ns", "container-lvl1").intern();
    private static final QName CONTAINER_AUG = QName.create("test-ns-aug", "container-aug").intern();
    private static final QName LEAF_AUG = QName.create("test-ns-aug", "leaf-aug").intern();

    private static final LeafNode<?> NODE_LEAF = ImmutableNodes.leafNode(LEAF_AUG, "data");
    private static final ContainerNode NODE_CONTAINER_AUG = Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(CONTAINER_AUG))
            .withChild(ImmutableNodes.leafNode(LEAF_AUG, "data"))
            .build();
    private static final ContainerNode NODE_CONTAINER_LVL1 = Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(CONTAINER_LVL_1))
            .withChild(Builders.augmentationBuilder()
                    .withNodeIdentifier(YangInstanceIdentifier.AugmentationIdentifier.create(Set.of(CONTAINER_AUG)))
                    .withChild(NODE_CONTAINER_AUG)
                    .build())
            .build();

    private static EffectiveModelContext context;
    private static JSONCodecFactory factory;

    @BeforeAll
    static void setup() {
        context = YangParserTestUtils.parseYangResourceDirectory("/yt1468");
        factory = JSONCodecFactorySupplier.RFC7951.getShared(context);
    }

    @Test
    void testReadLvl1Container() throws Exception {
        final var inf = Inference.ofDataTreePath(context, CONTAINER_ROOT);
        final var json = """
                {
                  "test-ns:container-lvl1": {
                    "test-ns-aug:container-aug": {
                      "leaf-aug": "data"
                    }
                  }
                }
                """;
        assertEquals(NODE_CONTAINER_LVL1,  parseJson(json, inf));
    }

    @Test
    void testReadAugmentation() throws Exception {
        final var inf = Inference.ofDataTreePath(context, CONTAINER_ROOT, CONTAINER_LVL_1);
        final var json = """
                {
                  "container-aug": {
                    "leaf-aug": "data"
                  }
                }
                """;
        assertEquals(NODE_CONTAINER_AUG, parseJson(json, inf));
    }

    @Test
    void testReadLeaf() throws Exception {
        final var inf = Inference.ofDataTreePath(context, CONTAINER_ROOT, CONTAINER_LVL_1, CONTAINER_AUG);
        final var json = """
                {
                  "leaf-aug": "data"
                }
                """;
        assertEquals(NODE_LEAF, parseJson(json, inf));
    }

    private static NormalizedNode parseJson(final String json, final Inference inference) throws Exception {
        final var result = new NormalizedNodeResult();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var jsonParser = JsonParserStream.create(streamWriter, factory, inference);
        jsonParser.parse(new JsonReader(new StringReader(json)));
        return result.getResult();
    }
}
