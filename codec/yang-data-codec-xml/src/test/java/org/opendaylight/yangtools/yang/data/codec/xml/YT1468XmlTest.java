/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.xml.transform.dom.DOMSource;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
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
import org.w3c.dom.Element;

public class YT1468XmlTest {
    private static final QName CONTAINER_ROOT = QName.create("test-ns", "container-root").intern();
    private static final QName CONTAINER_LVL_1 = QName.create("test-ns", "container-lvl1").intern();
    private static final QName CONTAINER_AUG = QName.create("test-ns-aug", "container-aug").intern();
    private static final QName DATA = QName.create("test-ns-aug", "leaf-aug").intern();

    private static EffectiveModelContext schemaContext;

    @BeforeClass
    public static void setup() {
        schemaContext = YangParserTestUtils.parseYangResourceDirectory("/yt1468");
    }

    /**
     * Test to read augmented container data from XML.
     * <p>
     * Assert that resulting data is a container and equals to expected data.
     */
    @Test
    public void testReadAugmentedContainer() throws Exception {
        final var expectedData = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(CONTAINER_AUG))
                .withChild(ImmutableNodes.leafNode(DATA, "data"))
                .build();

        final var stream = YT1468XmlTest.class.getResourceAsStream("/yt1468/data.xml");
        final var document = UntrustedXML.newDocumentBuilder().parse(stream);
        final var element = ((Element) document.getElementsByTagName("container-aug").item(0));
        final var inf = Inference.ofDataTreePath(schemaContext,
                CONTAINER_ROOT, CONTAINER_LVL_1, CONTAINER_AUG);
        final var data = fromXML(element, inf);

        assertTrue(data instanceof ContainerNode);
        assertEquals(CONTAINER_AUG, data.getIdentifier().getNodeType());
        assertEquals(expectedData, data);
    }

    private static NormalizedNode fromXML(final Element element, final Inference inference) throws Exception {
        final var resultHolder = new NormalizedNodeResult();
        final var writer = ImmutableNormalizedNodeStreamWriter.from(resultHolder);
        final var xmlParser = XmlParserStream.create(writer, inference);
        xmlParser.traverse(new DOMSource(element));
        return resultHolder.getResult();
    }
}
