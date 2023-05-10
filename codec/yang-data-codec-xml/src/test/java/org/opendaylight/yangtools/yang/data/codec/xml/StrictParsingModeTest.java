/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import java.io.InputStream;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class StrictParsingModeTest {
    private static EffectiveModelContext schemaContext;

    @BeforeClass
    public static void beforeClass() {
        schemaContext = YangParserTestUtils.parseYang("""
            module foo {
                namespace foo;
                prefix foo;
                container top-level-container {
                    container inner-container {}
                }
            }""");
    }

    @AfterClass
    public static void afterClass() {
        schemaContext = null;
    }

    @Test
    // unknown child nodes in the top-level-container node will be skipped when the strictParsing is set to false
    public void testLenientParsing() throws Exception {
        final InputStream resourceAsStream = StrictParsingModeTest.class.getResourceAsStream(
                "/strict-parsing-mode-test/foo.xml");

        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schemaContext, QName.create("foo", "top-level-container")), false);
        xmlParser.parse(reader);

        final NormalizedNode transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    // should fail because strictParsing is switched on and the top-level-container node contains child nodes
    // which are not defined in the provided YANG model
    public void testStrictParsing() throws Exception {
        final InputStream resourceAsStream = StrictParsingModeTest.class.getResourceAsStream(
                "/strict-parsing-mode-test/foo.xml");

        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schemaContext, QName.create("foo", "top-level-container")), true);

        final XMLStreamException ex = assertThrows(XMLStreamException.class, () -> xmlParser.parse(reader));
        assertThat(ex.getMessage(), containsString("Schema for node with name unknown-container-a and namespace foo "
            + "does not exist in parent EmptyContainerEffectiveStatement{argument=(foo)top-level-container}"));
    }
}
