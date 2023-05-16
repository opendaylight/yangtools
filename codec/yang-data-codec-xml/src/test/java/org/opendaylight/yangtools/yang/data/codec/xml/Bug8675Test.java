/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug8675Test {
    private static final QNameModule FOO = QNameModule.create(XMLNamespace.of("foo"), Revision.of("2017-06-13"));

    private static EffectiveModelContext schemaContext;

    @BeforeClass
    public static void setup() {
        schemaContext = YangParserTestUtils.parseYangResource("/bug8675/foo.yang");
    }

    @AfterClass
    public static void cleanup() {
        schemaContext = null;
    }

    @Test
    public void testParsingEmptyElements() throws Exception {
        final var resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream(
                "/bug8675/foo.xml");
        final var reader = UntrustedXML.createXMLStreamReader(resourceAsStream);
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schemaContext, QName.create(FOO, "top-level-container")));
        xmlParser.parse(reader);

        final var transformedInput = result.getResult().data();
        assertNotNull(transformedInput);
    }

    @Test
    public void testParsingEmptyRootElement() throws Exception {
        final var resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/bug8675/foo-2.xml");
        final var reader = UntrustedXML.createXMLStreamReader(resourceAsStream);
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schemaContext, QName.create(FOO, "top-level-container")));
        xmlParser.parse(reader);

        final var transformedInput = result.getResult().data();
        assertNotNull(transformedInput);
    }

    @Test
    public void testListAsRootElement() throws Exception {
        final var resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/bug8675/foo-3.xml");
        final var reader = UntrustedXML.createXMLStreamReader(resourceAsStream);
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schemaContext, QName.create(FOO, "top-level-list")));
        xmlParser.parse(reader);

        final var transformedInput = result.getResult().data();
        assertNotNull(transformedInput);
    }

    @Test
    public void testAnyXmlAsRootElement() throws Exception {
        final var resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/bug8675/foo-4.xml");
        final var reader = UntrustedXML.createXMLStreamReader(resourceAsStream);
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schemaContext, QName.create(FOO, "top-level-anyxml")));
        xmlParser.parse(reader);

        final var transformedInput = result.getResult().data();
        assertNotNull(transformedInput);
    }

    @Test
    public void testLeafAsRootElement() throws Exception {
        final var resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/bug8675/foo-5.xml");
        final var reader = UntrustedXML.createXMLStreamReader(resourceAsStream);
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schemaContext, QName.create(FOO, "top-level-leaf")));
        xmlParser.parse(reader);

        final var transformedInput = result.getResult().data();
        assertNotNull(transformedInput);
    }

    @Test
    public void testLeafListAsRootElement() throws Exception {
        final var resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream("/bug8675/foo-6.xml");
        final var reader = UntrustedXML.createXMLStreamReader(resourceAsStream);
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schemaContext, QName.create(FOO, "top-level-leaf-list")));
        xmlParser.parse(reader);

        final var transformedInput = result.getResult().data();
        assertNotNull(transformedInput);
    }
}
