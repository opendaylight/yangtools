/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o..  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.xml.stream.XMLStreamException;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.Submodule;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

abstract class AbstractYinExportTest {
    final void exportYinModules(final String yangDir, final String yinDir) throws IOException, SAXException,
            XMLStreamException {
        final var schemaContext = YangParserTestUtils.parseYangResourceDirectory(yangDir);
        final var modules = schemaContext.getModules();
        assertNotEquals(0, modules.size());

        for (var module : modules) {
            readAndValidateModule(schemaContext, module, yinDir);

            for (var submodule : module.getSubmodules()) {
                readAndValidateSubmodule(schemaContext, module, submodule, yinDir);
            }
        }
    }

    void validateOutput(final String yinDir, final String fileName, final String fileBody) throws IOException,
            SAXException {
        assertNotEquals(0, fileBody.length());
        if (yinDir != null) {
            final Document doc = YinExportTestUtils.loadDocument(yinDir + "/" + fileName);
            assertXMLEquals(fileName, doc, fileBody);
        }
    }

    private void readAndValidateModule(final EffectiveModelContext schemaContext, final Module module,
            final String yinDir) throws XMLStreamException, IOException, SAXException {
        final String fileName = YinExportUtils.wellFormedYinName(module.getName(), module.getRevision());
        validateOutput(yinDir, fileName, export(module));
    }

    private void readAndValidateSubmodule(final EffectiveModelContext schemaContext, final Module module,
            final Submodule submodule, final String yinDir) throws XMLStreamException, IOException, SAXException {
        final String fileName = YinExportUtils.wellFormedYinName(submodule.getName(), submodule.getRevision());
        validateOutput(yinDir, fileName, export(module, submodule));
    }

    private static String export(final Module module) throws XMLStreamException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        YinExportUtils.writeModuleAsYinText(module.asEffectiveStatement(), bos);
        return bos.toString(StandardCharsets.UTF_8);
    }

    private static String export(final Module module, final Submodule submodule) throws XMLStreamException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        YinExportUtils.writeSubmoduleAsYinText(module.asEffectiveStatement(), submodule.asEffectiveStatement(), bos);
        return bos.toString(StandardCharsets.UTF_8);
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
