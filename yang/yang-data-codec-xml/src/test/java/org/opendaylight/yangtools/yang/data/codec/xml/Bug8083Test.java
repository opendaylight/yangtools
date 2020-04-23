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
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug8083Test {

    @Test
    public void testInstanceIdentifierPathWithEmptyListKey() throws Exception {
        final EffectiveModelContext schemaContext = YangParserTestUtils.parseYangResource("/bug8083/yang/baz.yang");
        final Module bazModule = schemaContext.getModules().iterator().next();
        final ContainerSchemaNode topCont = (ContainerSchemaNode) bazModule.findDataChildByName(
                QName.create(bazModule.getQNameModule(), "top-cont")).get();

        final InputStream resourceAsStream = Bug8083Test.class.getResourceAsStream("/bug8083/xml/baz.xml");

        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        // deserialization
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, topCont);
        xmlParser.parse(reader);
        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testInstanceIdentifierPathWithIdentityrefListKey() throws Exception {
        final EffectiveModelContext schemaContext = YangParserTestUtils.parseYangResource("/bug8083/yang/zab.yang");
        final Module zabModule = schemaContext.getModules().iterator().next();
        final ContainerSchemaNode topCont = (ContainerSchemaNode) zabModule.findDataChildByName(
                QName.create(zabModule.getQNameModule(), "top-cont")).get();

        final InputStream resourceAsStream = Bug8083Test.class.getResourceAsStream("/bug8083/xml/zab.xml");

        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        // deserialization
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, topCont);
        xmlParser.parse(reader);
        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    @Test
    public void testInstanceIdentifierPathWithInstanceIdentifierListKey() throws Exception {
        final EffectiveModelContext schemaContext = YangParserTestUtils.parseYangResource("/bug8083/yang/foobar.yang");
        final Module foobarModule = schemaContext.getModules().iterator().next();
        final ContainerSchemaNode topCont = (ContainerSchemaNode) foobarModule.findDataChildByName(
                QName.create(foobarModule.getQNameModule(), "top-cont")).get();

        final InputStream resourceAsStream = Bug8083Test.class.getResourceAsStream("/bug8083/xml/foobar.xml");

        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        // deserialization
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, topCont);
        xmlParser.parse(reader);
        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }
}
