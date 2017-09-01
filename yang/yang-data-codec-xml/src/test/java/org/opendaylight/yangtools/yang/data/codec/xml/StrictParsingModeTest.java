/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class StrictParsingModeTest {

    @Test
    public void testLenientParsing() throws Exception {
        // unknown child nodes in the top-level-container node will be skipped when the strictParsing is set to false
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResource(
                "/strict-parsing-mode-test/foo.yang");
        final Module fooModule = schemaContext.getModules().iterator().next();
        final ContainerSchemaNode topLevelContainer = (ContainerSchemaNode) fooModule.getDataChildByName(
                QName.create(fooModule.getQNameModule(),
                "top-level-container"));

        final InputStream resourceAsStream = StrictParsingModeTest.class.getResourceAsStream(
                "/strict-parsing-mode-test/foo.xml");

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);

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
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResource(
                "/strict-parsing-mode-test/foo.yang");
        final Module fooModule = schemaContext.getModules().iterator().next();
        final ContainerSchemaNode topLevelContainer = (ContainerSchemaNode) fooModule.getDataChildByName(
                QName.create(fooModule.getQNameModule(), "top-level-container"));

        final InputStream resourceAsStream = StrictParsingModeTest.class.getResourceAsStream(
                "/strict-parsing-mode-test/foo.xml");

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, topLevelContainer, true);
        try {
            xmlParser.parse(reader);
            fail("IllegalStateException should have been thrown because of an unknown child node.");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("Schema for node with name unknown-container-a and namespace "
                    + "foo doesn't exist."));
        }
    }
}
