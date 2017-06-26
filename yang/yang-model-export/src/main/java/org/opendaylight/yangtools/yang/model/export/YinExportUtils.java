/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.io.OutputStream;
import java.net.URI;
import java.util.Date;
import java.util.Map;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public final class YinExportUtils {

    private YinExportUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     *
     * Returns well-formed file name of YIN file as defined in RFC6020.
     *
     * @param name
     *            Module or submodule name
     * @param revision
     *            Revision of module or submodule
     * @return well-formed file name of YIN file as defined in RFC6020.
     */
    public static String wellFormedYinName(final String name, final Date revision) {
        return wellFormedYinName(name, SimpleDateFormatUtil.getRevisionFormat().format(revision));
    }

    /**
     *
     * Returns well-formed file name of YIN file as defined in RFC6020.
     *
     * @param name
     *            name Module or submodule name
     * @param revision
     *            Revision of module or submodule
     * @return well-formed file name of YIN file as defined in RFC6020.
     */
    public static String wellFormedYinName(final String name, final String revision) {
        return String.format("%s@%s.yin", Preconditions.checkNotNull(name), Preconditions.checkNotNull(revision));
    }

    /**
     * Writes YIN representation of supplied module to specified output stream.
     *
     * @param ctx
     *            Schema Context which contains module and extension definitions
     *            to be used during export of model.
     * @param module
     *            Module to be exported.
     * @param str
     *            Output stream to which YIN representation of model will be
     *            written.
     * @throws XMLStreamException
     */
    public static void writeModuleToOutputStream(final SchemaContext ctx, final Module module, final OutputStream str)
            throws XMLStreamException {
        writeModuleToOutputStream(ctx, module, str, false);
    }

    /**
     * Writes YIN representation of supplied module to specified output stream.
     *
     * @param ctx
     *            Schema Context which contains module and extension definitions
     *            to be used during export of model.
     * @param module
     *            Module to be exported.
     * @param str
     *            Output stream to which YIN representation of model will be
     *            written.
     * @param emitInstantiated
     *            Option to emit also instantiated statements (e.g. statements
     *            added by uses or augment)
     * @throws XMLStreamException
     */
    public static void writeModuleToOutputStream(final SchemaContext ctx, final Module module, final OutputStream str,
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
        final StatementTextWriter yinWriter = SingleModuleYinStatementWriter.create(xmlStreamWriter, moduleNs,
                prefixToNs);
        SchemaContextEmitter.writeToStatementWriter(module, ctx, yinWriter, emitInstantiated);
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

}
