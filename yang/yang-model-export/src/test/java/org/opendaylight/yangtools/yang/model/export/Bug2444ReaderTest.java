/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.StringWriter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement.NameToEffectiveSubmoduleNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.export.YinXMLEventReaderFactory;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Bug2444ReaderTest {
    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    @Test
    public void test() throws Exception {
        final SchemaContext schema = YangParserTestUtils.parseYangResourceDirectory("/bugs/bug2444/yang");
        assertNotNull(schema);

        final Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

        for (final Module module : schema.getModules()) {
            final ModuleEffectiveStatement effective = (ModuleEffectiveStatement) module;

            assertEqualDocument(module, transformer, YinXMLEventReaderFactory.defaultInstance()
                .createXMLEventReader(effective));

            for (SubmoduleEffectiveStatement submodule : effective.getAll(
                NameToEffectiveSubmoduleNamespace.class).values()) {
                assertEqualDocument((Module) submodule, transformer, YinXMLEventReaderFactory.defaultInstance()
                    .createXMLEventReader(effective, submodule));
            }
        }
    }

    private static void assertEqualDocument(final Module module, final Transformer transformer,
            final XMLEventReader reader) throws IOException, SAXException, TransformerException, XMLStreamException {
        final StreamResult result = new StreamResult(new StringWriter());
        transformer.transform(new StAXSource(reader), result);

        final String output = result.getWriter().toString();
        assertNotNull(output);
        assertNotEquals(0, output.length());

        final Document doc = YinExportTestUtils.loadDocument("/bugs/bug2444/yin", module);
        assertXMLEquals(module.getName(), doc, output);
    }

    private static void assertXMLEquals(final String fileName, final Document expectedXMLDoc, final String output)
            throws SAXException, IOException {
        final String expected = YinExportTestUtils.toString(expectedXMLDoc.getDocumentElement());

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalize(true);
        XMLUnit.setNormalizeWhitespace(true);

        final Diff diff = new Diff(expected, output);
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
        XMLAssert.assertXMLEqual(fileName, diff, true);
    }
}
