/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export.test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.export.YinExportUtils;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Bug2444Test {
    @Test
    public void test() throws Exception {
        final SchemaContext schema = YangParserTestUtils.parseYangResources(Bug2444Test.class, "/bugs/bug2444/yang");
        assertNotNull(schema);

        final File outDir = new File("target/bug2444-export");
        outDir.mkdirs();

        for (final Module module : schema.getModules()) {
            exportModule(schema, module, outDir);

            final OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
            try {
                YinExportUtils.writeModuleToOutputStream(schema, module, bufferedOutputStream);
                final String output = byteArrayOutputStream.toString();
                assertNotNull(output);
                assertNotEquals(0, output.length());

                final Document doc = YinExportTestUtils.loadDocument(String.format("/bugs/bug2444/yin/%s@%s.yin",
                        module.getName(), SimpleDateFormatUtil.getRevisionFormat().format(module.getRevision())));
                assertXMLEquals(doc, output);
            } finally {
                byteArrayOutputStream.close();
                bufferedOutputStream.close();
            }
        }
    }

    private static void assertXMLEquals(final Document expectedXMLDoc, final String output)
            throws SAXException, IOException {
        final String expected = YinExportTestUtils.toString(expectedXMLDoc.getDocumentElement());

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalize(true);
        XMLUnit.setNormalizeWhitespace(true);

        final Diff diff = new Diff(expected, output);
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
        XMLAssert.assertXMLEqual(diff, true);
    }

    private static File exportModule(final SchemaContext schemaContext, final Module module, final File outDir)
            throws Exception {
        final File outFile = new File(outDir, YinExportUtils.wellFormedYinName(module.getName(), module.getRevision()));
        try (OutputStream output = new FileOutputStream(outFile)) {
            YinExportUtils.writeModuleToOutputStream(schemaContext, module, output);
        }
        return outFile;
    }
}
