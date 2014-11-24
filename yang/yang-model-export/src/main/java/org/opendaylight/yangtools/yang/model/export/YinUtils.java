/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

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

public class YinUtils {

    public static String wellFormedYinName(final String name, final Date revision) {
        return wellFormedYinName(name, SimpleDateFormatUtil.getRevisionFormat().format(revision));
    }

    public static String wellFormedYinName(final String name, final String revision) {
        return name + "@" + revision + ".yin";
    }

    public static void writeModuleToOutputStream(final SchemaContext ctx, final Module module, final OutputStream str) throws XMLStreamException {
        final XMLOutputFactory factory = XMLOutputFactory.newFactory();
        //factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
        final XMLStreamWriter xmlStreamWriter = factory.createXMLStreamWriter(str);
        writeModuleToOutputStream(ctx,module, xmlStreamWriter);
    }

    private static void writeModuleToOutputStream(final SchemaContext ctx, final Module module, final XMLStreamWriter xmlStreamWriter) {
        final URI moduleNs = module.getNamespace();
        final Map<String, URI> prefixToNs = prefixToNamespace(ctx,module);
        final StatementWriter yinWriter = SingleModuleYinStatementWriter.create(xmlStreamWriter, moduleNs, prefixToNs);
        SchemaExportUtils.writeToStatementWriter(module, ctx,yinWriter);
    }

    private static Map<String, URI> prefixToNamespace(final SchemaContext ctx, final Module module) {
        final BiMap<String, URI> prefixMap = HashBiMap.create(module.getImports().size() + 1);
        prefixMap.put(module.getPrefix(), module.getNamespace());
        for(final ModuleImport imp : module.getImports()) {
            final String prefix = imp.getPrefix();
            final URI namespace = getModuleNamespace(ctx,imp.getModuleName());
            prefixMap.put(prefix, namespace);
        }
        return prefixMap;
    }

    private static URI getModuleNamespace(final SchemaContext ctx, final String moduleName) {
        for(final Module module : ctx.getModules()) {
            if(moduleName.equals(module.getName())) {
                return module.getNamespace();
            }
        }
        throw new IllegalArgumentException("Module " + moduleName + "does not exists in provided schema context");
    }

}
