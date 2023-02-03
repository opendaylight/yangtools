/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.util.Set;
import javax.xml.stream.XMLStreamReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
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
                    .withNodeIdentifier(AugmentationIdentifier.create(Set.of(CONTAINER_AUG)))
                    .withChild(NODE_CONTAINER_AUG)
                    .build())
            .build();

    private static EffectiveModelContext schemaContext;

    @BeforeAll
    static void setup() {
        schemaContext = YangParserTestUtils.parseYangResourceDirectory("/yt1468");
    }

    @Test
    public void testReadLvl1Container() throws Exception {
        final var inf = Inference.ofDataTreePath(schemaContext, CONTAINER_ROOT, CONTAINER_LVL_1);
        final var xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <tns:container-lvl1 xmlns:tns="test-ns">
                    <tnsa:container-aug xmlns:tnsa="test-ns-aug">
                        <tnsa:leaf-aug>data</tnsa:leaf-aug>
                    </tnsa:container-aug>
                </tns:container-lvl1>
                """;
        assertEquals(NODE_CONTAINER_LVL1, parseXml(xml, inf));
    }

    @Test
    public void testReadAugmentedContainer() throws Exception {
        final var inf = Inference.ofDataTreePath(schemaContext, CONTAINER_ROOT, CONTAINER_LVL_1, CONTAINER_AUG);
        final var xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <container-aug xmlns="test-ns-aug">
                    <leaf-aug>data</leaf-aug>
                </container-aug>
                """;
        assertEquals(NODE_CONTAINER_AUG, parseXml(xml, inf));
    }

    @Test
    void testReadLeaf() throws Exception {
        final var inf =
                Inference.ofDataTreePath(schemaContext, CONTAINER_ROOT, CONTAINER_LVL_1, CONTAINER_AUG, LEAF_AUG);
        final var xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <leaf-aug xmlns="test-ns-aug">data</leaf-aug>
                """;
        assertEquals(NODE_LEAF, parseXml(xml, inf));
    }

    private static NormalizedNode parseXml(final String xml, final Inference inference) throws Exception {
        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(new StringReader(xml));
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, inference);
        xmlParser.parse(reader);
        return result.getResult();
    }
}
