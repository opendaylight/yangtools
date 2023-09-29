/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o..  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.Submodule;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;

abstract class AbstractYinExportTest {
    final void exportYinModules(final String yangDir, final String yinDir) throws XMLStreamException {
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

    void validateOutput(final String yinDir, final String fileName, final String fileBody) {
        assertNotEquals(0, fileBody.length());
        if (yinDir != null) {
            assertXMLEquals(fileName,
                AbstractYinExportTest.class.getResourceAsStream(yinDir + "/" + fileName),
                fileBody);
        }
    }

    private void readAndValidateModule(final EffectiveModelContext schemaContext, final Module module,
            final String yinDir) throws XMLStreamException {
        final String fileName = YinExportUtils.wellFormedYinName(module.getName(), module.getRevision());
        validateOutput(yinDir, fileName, export(module));
    }

    private void readAndValidateSubmodule(final EffectiveModelContext schemaContext, final Module module,
            final Submodule submodule, final String yinDir) throws XMLStreamException {
        final var fileName = YinExportUtils.wellFormedYinName(submodule.getName(), submodule.getRevision());
        validateOutput(yinDir, fileName, export(module, submodule));
    }

    private static String export(final Module module) throws XMLStreamException {
        final var bos = new ByteArrayOutputStream();
        YinExportUtils.writeModuleAsYinText(module.asEffectiveStatement(), bos);
        return bos.toString(StandardCharsets.UTF_8);
    }

    private static String export(final Module module, final Submodule submodule) throws XMLStreamException {
        final var bos = new ByteArrayOutputStream();
        YinExportUtils.writeSubmoduleAsYinText(module.asEffectiveStatement(), submodule.asEffectiveStatement(), bos);
        return bos.toString(StandardCharsets.UTF_8);
    }

    private static void assertXMLEquals(final String fileName, final InputStream expectedDoc, final String output) {
        final var diff = DiffBuilder.compare(output)
            .withTest(new StreamSource(expectedDoc))
            .normalizeWhitespace()
            .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndText))
            .checkForIdentical()
            .build();
        assertFalse(diff.hasDifferences(), diff.toString());
    }
}
