/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.export.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import javax.xml.stream.XMLStreamException;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.export.YinExportUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class SchemaContextEmitterTest {

    @Test
    public void testSchemaContextEmitter() throws ReactorException, IOException, URISyntaxException,
            XMLStreamException, SAXException {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResourceDirectory(
            "/schema-context-emitter-test");
        assertNotNull(schemaContext);
        assertEquals(1, schemaContext.getModules().size());

        final OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);

        for (final Module module : schemaContext.getModules()) {
            YinExportUtils.writeModuleToOutputStream(schemaContext, module, bufferedOutputStream);
        }

        final String output = byteArrayOutputStream.toString();
        assertNotNull(output);
        assertNotEquals(0, output.length());

        final Document doc = YinExportTestUtils.loadDocument("/schema-context-emitter-test/foo.yin");
        final String expected = YinExportTestUtils.toString(doc.getDocumentElement());

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalize(true);

        final Diff diff = new Diff(expected, output);
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
        XMLAssert.assertXMLEqual(diff, true);
    }
}
