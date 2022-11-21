/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opendaylight.yangtools.yang.data.impl.schema.Builders.containerBuilder;
import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.leafNode;

import javax.xml.transform.dom.DOMSource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NC747Test {
    private static EffectiveModelContext schemaContext;

    @BeforeClass
    public static void setup() {
        schemaContext = YangParserTestUtils.parseYangResourceDirectory("/netconf747");
    }

    @AfterClass
    public static void cleanup() {
        schemaContext = null;
    }

    @Test
    public void testXmlToNormalizedNodeWithAugmentation() throws Exception {
        final InputStream EDIT = XmlToNormalizedNodesTest.class.getResourceAsStream(
                "/netconf747/edit.xml");

        var cRoot = QName.create("test-ns", "container-root");
        var cLvl1 = QName.create("test-ns", "container-lvl1");
        var cAug = QName.create("test-ns-aug", "container-aug");
        var inf = SchemaInferenceStack.Inference.ofDataTreePath(schemaContext, cRoot, cLvl1,
                cAug);

        final NormalizedNode expectedData = containerBuilder().withNodeIdentifier(new YangInstanceIdentifier
                .NodeIdentifier(cAug))
                .withChild(leafNode(QName.create("test-ns-aug", "leaf-aug"), "data"))
                .build();

        // Reading data from edit patch in same way as netconf does
        final Document doc = UntrustedXML.newDocumentBuilder().parse(EDIT);
        final NodeList editNodes = doc.getElementsByTagName("edit");
        final Element firstEdit = (Element) editNodes.item(0);
        final Node firstValue = firstEdit.getElementsByTagName("value").item(0);
        final List<Element> result = new ArrayList<>();
        final NodeList childNodes = firstValue.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i) instanceof Element) {
                result.add((Element) childNodes.item(i));
            }
        }
        final Element firstValueElement = result.get(0);
        final NormalizedNode data = fromXML(firstValueElement, inf);
        assertNotNull(data);
        assertEquals(expectedData, data);
    }

    final NormalizedNode fromXML(final Element firstValueElement, final SchemaInferenceStack.Inference parentNode)
            throws Exception {
        final NormalizedNodeResult resultHolder = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter writer = ImmutableNormalizedNodeStreamWriter.from(resultHolder);
        final XmlParserStream xmlParser = XmlParserStream.create(writer, parentNode);
        xmlParser.traverse(new DOMSource(firstValueElement));
        return resultHolder.getResult();
    }
}
