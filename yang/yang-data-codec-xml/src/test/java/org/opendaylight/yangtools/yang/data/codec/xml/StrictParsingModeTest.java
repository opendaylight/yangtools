/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.InputStream;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.junit.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class StrictParsingModeTest {

    @Test
    public void testLenientParsing() throws Exception {
        // unknown child nodes in the top-level-container node will be skipped when the strictParsing is set to false
        final EffectiveModelContext schemaContext = YangParserTestUtils.parseYangResource(
                "/strict-parsing-mode-test/foo.yang");
        final Module fooModule = schemaContext.getModules().iterator().next();
        final ContainerSchemaNode topLevelContainer = (ContainerSchemaNode) fooModule.findDataChildByName(
                QName.create(fooModule.getQNameModule(), "top-level-container")).get();

        final InputStream resourceAsStream = StrictParsingModeTest.class.getResourceAsStream(
                "/strict-parsing-mode-test/foo.xml");

        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, topLevelContainer, false);
        xmlParser.parse(reader);

        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testStrictParsing() throws Exception {
        // should fail because strictParsing is switched on and the top-level-container node contains child nodes
        // which are not defined in the provided YANG model
        final EffectiveModelContext schemaContext = YangParserTestUtils.parseYangResource(
                "/strict-parsing-mode-test/foo.yang");
        final Module fooModule = schemaContext.getModules().iterator().next();
        final ContainerSchemaNode topLevelContainer = (ContainerSchemaNode) fooModule.findDataChildByName(
                QName.create(fooModule.getQNameModule(), "top-level-container")).get();

        final InputStream resourceAsStream = StrictParsingModeTest.class.getResourceAsStream(
                "/strict-parsing-mode-test/foo.xml");

        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, topLevelContainer, true);
        try {
            xmlParser.parse(reader);
            fail("XMLStreamException should have been thrown because of an unknown child node.");
        } catch (XMLStreamException ex) {
            assertEquals("Schema for node with name unknown-container-a and namespace foo does not exist at "
                    + "AbsoluteSchemaPath{path=[(foo)top-level-container]}", ex.getMessage());
        }
    }
}
