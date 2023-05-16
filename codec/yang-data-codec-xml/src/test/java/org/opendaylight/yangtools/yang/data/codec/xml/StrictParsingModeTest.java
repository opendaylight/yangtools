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

import javax.xml.stream.XMLStreamException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class StrictParsingModeTest {
    private static EffectiveModelContext schemaContext;

    @BeforeClass
    public static void beforeClass() {
        schemaContext = YangParserTestUtils.parseYangResource("/strict-parsing-mode-test/foo.yang");
    }

    @AfterClass
    public static void afterClass() {
        schemaContext = null;
    }

    @Test
    // unknown child nodes in the top-level-container node will be skipped when the strictParsing is set to false
    public void testLenientParsing() throws Exception {
        final var resourceAsStream = StrictParsingModeTest.class.getResourceAsStream(
                "/strict-parsing-mode-test/foo.xml");

        final var reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final var xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schemaContext, QName.create("foo", "top-level-container")), false);
        xmlParser.parse(reader);

        final var transformedInput = result.getResult().data();
        assertNotNull(transformedInput);
    }

    @Test
    // should fail because strictParsing is switched on and the top-level-container node contains child nodes
    // which are not defined in the provided YANG model
    public void testStrictParsing() throws Exception {
        final var resourceAsStream = StrictParsingModeTest.class.getResourceAsStream(
                "/strict-parsing-mode-test/foo.xml");

        final var reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schemaContext, QName.create("foo", "top-level-container")), true);

        final var ex = assertThrows(XMLStreamException.class, () -> xmlParser.parse(reader));
        assertThat(ex.getMessage(), containsString("Schema for node with name unknown-container-a and namespace foo "
            + "does not exist in parent EmptyContainerEffectiveStatement{argument=(foo)top-level-container}"));
    }
}
