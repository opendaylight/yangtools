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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.export.SchemaContextEmitter.EffectiveSchemaContextEmitter;
import org.opendaylight.yangtools.yang.model.export.test.YinExportTestUtils;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class EffectiveSchemaContextEmitterTest {
    @Test
    public void test() throws Exception {
        final SchemaContext schema = YangParserTestUtils.parseYangSource("/bugs/bug2444/yang/notification.yang");
        assertNotNull(schema);

        final File outDir = new File("target/bug2444-export");
        outDir.mkdirs();

        for (final Module module : schema.getModules()) {
            exportModule(schema, module, outDir);
            final OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
            try {
                writeModuleToOutputStream(schema, module, bufferedOutputStream, false);
                final String output = byteArrayOutputStream.toString();
                assertNotNull(output);
                assertNotEquals(0, output.length());

                final Document doc = YinExportTestUtils
                        .loadDocument(String.format("/bugs/bug2444/yin-effective-emitter/%s@%s.yin", module.getName(),
                                SimpleDateFormatUtil.getRevisionFormat().format(module.getRevision())));
                assertXMLEquals(doc, output);
            } finally {
                byteArrayOutputStream.close();
                bufferedOutputStream.close();
            }
        }
    }

    private static void writeModuleToOutputStream(final SchemaContext ctx, final Module module, final OutputStream str,
            final boolean emitInstantiated) throws XMLStreamException {
        final XMLOutputFactory factory = XMLOutputFactory.newFactory();
        final XMLStreamWriter xmlStreamWriter = factory.createXMLStreamWriter(str);
        writeModuleToOutputStream(ctx, module, xmlStreamWriter, emitInstantiated);
        xmlStreamWriter.flush();
    }

    private static void writeModuleToOutputStream(final SchemaContext ctx, final Module module,
            final XMLStreamWriter xmlStreamWriter, final boolean emitInstantiated) {
        final URI moduleNs = module.getNamespace();
        final Map<String, URI> prefixToNs = prefixToNamespace(ctx, module);
        final StatementTextWriter statementWriter = SingleModuleYinStatementWriter.create(xmlStreamWriter, moduleNs,
                prefixToNs);
        final YangModuleWriter yangSchemaWriter = SchemaToStatementWriterAdaptor.from(statementWriter);
        final Map<QName, StatementDefinition> extensions = ExtensionStatement.mapFrom(ctx.getExtensions());
        new EffectiveSchemaContextEmitter(yangSchemaWriter, extensions,
                YangVersion.parse(module.getYangVersion()).orElse(null), emitInstantiated).emitModule(module);
    }

    private static Map<String, URI> prefixToNamespace(final SchemaContext ctx, final Module module) {
        final BiMap<String, URI> prefixMap = HashBiMap.create(module.getImports().size() + 1);
        prefixMap.put(module.getPrefix(), module.getNamespace());
        for (final ModuleImport imp : module.getImports()) {
            final String prefix = imp.getPrefix();
            final URI namespace = getModuleNamespace(ctx, imp.getModuleName());
            prefixMap.put(prefix, namespace);
        }
        return prefixMap;
    }

    private static URI getModuleNamespace(final SchemaContext ctx, final String moduleName) {
        for (final Module module : ctx.getModules()) {
            if (moduleName.equals(module.getName())) {
                return module.getNamespace();
            }
        }
        throw new IllegalArgumentException("Module " + moduleName + "does not exists in provided schema context");
    }

    private static File exportModule(final SchemaContext schemaContext, final Module module, final File outDir)
            throws Exception {
        final File outFile = new File(outDir, YinExportUtils.wellFormedYinName(module.getName(), module.getRevision()));
        try (OutputStream output = new FileOutputStream(outFile)) {
            writeModuleToOutputStream(schemaContext, module, output, false);
        }
        return outFile;
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
