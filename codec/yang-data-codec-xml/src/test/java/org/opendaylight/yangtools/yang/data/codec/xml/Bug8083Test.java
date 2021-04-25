/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import javax.xml.stream.XMLStreamReader;
import org.junit.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug8083Test {
    private static final QNameModule FOOBAR = QNameModule.create(XMLNamespace.of("foobar-ns"));
    private static final QNameModule BAZ = QNameModule.create(XMLNamespace.of("baz-ns"));
    private static final QNameModule ZAB = QNameModule.create(XMLNamespace.of("zab-ns"));

    @Test
    public void testInstanceIdentifierPathWithEmptyListKey() throws Exception {
        final EffectiveModelContext schemaContext = YangParserTestUtils.parseYangResource("/bug8083/yang/baz.yang");

        final InputStream resourceAsStream = Bug8083Test.class.getResourceAsStream("/bug8083/xml/baz.xml");

        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        // deserialization
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schemaContext, QName.create(BAZ, "top-cont")));
        xmlParser.parse(reader);
        final NormalizedNode transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testInstanceIdentifierPathWithIdentityrefListKey() throws Exception {
        final EffectiveModelContext schemaContext = YangParserTestUtils.parseYangResource("/bug8083/yang/zab.yang");

        final InputStream resourceAsStream = Bug8083Test.class.getResourceAsStream("/bug8083/xml/zab.xml");

        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        // deserialization
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schemaContext, QName.create(ZAB, "top-cont")));
        xmlParser.parse(reader);
        final NormalizedNode transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testInstanceIdentifierPathWithInstanceIdentifierListKey() throws Exception {
        final EffectiveModelContext schemaContext = YangParserTestUtils.parseYangResource("/bug8083/yang/foobar.yang");

        final InputStream resourceAsStream = Bug8083Test.class.getResourceAsStream("/bug8083/xml/foobar.xml");

        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        // deserialization
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter,
            Inference.ofDataTreePath(schemaContext, QName.create(FOOBAR, "top-cont")));
        xmlParser.parse(reader);
        final NormalizedNode transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }
}
