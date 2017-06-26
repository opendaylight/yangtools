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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
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
        final SchemaContext schema = YangParserTestUtils.parseYangSources("/bugs/bug2444/yang");
        assertNotNull(schema);

        final ImmutableSet<Module> modulesAndSubmodules = getAllModulesAndSubmodules(schema);
        for (final Module module : modulesAndSubmodules) {
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

    private ImmutableSet<Module> getAllModulesAndSubmodules(final SchemaContext schema) {
        final Builder<Module> builder = ImmutableSet.builder();
        builder.addAll(schema.getModules());
        for (final Module module : schema.getModules()) {
            builder.addAll(module.getSubmodules());
        }
        return builder.build();
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
}
