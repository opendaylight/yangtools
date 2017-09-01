/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.StringWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.junit.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug8803Test {

    @Test
    public void test() throws Exception {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResourceDirectory("/bug8803");
        final SchemaPath topContPath = SchemaPath.create(true, QName.create("foo-ns", "1970-01-01", "top-cont"));
        final SchemaNode dataSchemaNode = SchemaContextUtil.findDataSchemaNode(schemaContext, topContPath);
        assertTrue(dataSchemaNode instanceof ContainerSchemaNode);
        final ContainerSchemaNode topContSchema = (ContainerSchemaNode) dataSchemaNode;

        final InputStream resourceAsStream = Bug8803Test.class.getResourceAsStream("/bug8803/foo.xml");

        // deserialization
        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, topContSchema);
        xmlParser.parse(reader);
        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);

        // serialization
        final StringWriter writer = new StringWriter();
        final XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
        // switching NS repairing to false does not help
        outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
        final XMLStreamWriter xmlStreamWriter = outputFactory.createXMLStreamWriter(writer);

        final NormalizedNodeStreamWriter xmlNormalizedNodeStreamWriter = XMLStreamNormalizedNodeStreamWriter.create(
                xmlStreamWriter, schemaContext);

        final NormalizedNodeWriter normalizedNodeWriter = NormalizedNodeWriter.forStreamWriter(
                xmlNormalizedNodeStreamWriter);
        normalizedNodeWriter.write(transformedInput);

        final String serializedXml = writer.toString();
        assertFalse(serializedXml.isEmpty());
    }
}
