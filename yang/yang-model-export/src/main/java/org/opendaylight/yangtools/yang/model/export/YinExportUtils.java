/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;

public final class YinExportUtils {
    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    private YinExportUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Returns well-formed file name of YIN file as defined in RFC6020.
     *
     * @param name Module or submodule name
     * @param revision Revision of module or submodule
     * @return well-formed file name of YIN file as defined in RFC6020.
     */
    public static String wellFormedYinName(final String name, final Optional<Revision> revision) {
        return wellFormedYinName(name, revision.map(Revision::toString).orElse(null));
    }

    /**
     * Returns well-formed file name of YIN file as defined in RFC6020.
     *
     * @param name name Module or submodule name
     * @param revision Revision of module or submodule
     * @return well-formed file name of YIN file as defined in RFC6020.
     */
    public static String wellFormedYinName(final String name, final String revision) {
        if (revision == null) {
            return name + YangConstants.RFC6020_YIN_FILE_EXTENSION;
        }
        return requireNonNull(name) + '@' + revision +  YangConstants.RFC6020_YIN_FILE_EXTENSION;
    }

    /**
     * Write a module as a YIN text into specified {@link OutputStream}. Supplied module must have the
     * {@link ModuleEffectiveStatement} trait.
     *
     * @param module Module to be exported
     * @throws IllegalArgumentException if the module is not an ModuleEffectiveStatement or if it declared
     *                                  representation is not available.
     * @throws NullPointerException if any of of the parameters is null
     * @throws XMLStreamException if an input-output error occurs
     */
    @Beta
    public static void writeModuleAsYinText(final Module module, final OutputStream output) throws XMLStreamException {
        requireNonNull(module);
        checkArgument(module instanceof ModuleEffectiveStatement, "Module %s is not a ModuleEffectiveStatement",
            module);
        final ModuleEffectiveStatement effective = (ModuleEffectiveStatement) module;
        writeReaderToOutput(YinXMLEventReaderFactory.defaultInstance().createXMLEventReader(effective), output);
    }

    /**
     * Write a submodule as a YIN text into specified {@link OutputStream}. Supplied submodule must have the
     * {@link SubmoduleEffectiveStatement} trait.
     *
     * @param parentModule Parent module
     * @param submodule Submodule to be exported
     * @throws IllegalArgumentException if the parent module is not a ModuleEffectiveStatement, if the submodule is not
     *                                  a SubmoduleEffectiveStatement or if its declared representation is not available
     * @throws NullPointerException if any of of the parameters is null
     * @throws XMLStreamException if an input-output error occurs
     */
    @Beta
    public static void writeSubmoduleAsYinText(final Module parentModule, final Module submodule,
            final OutputStream output) throws XMLStreamException {
        requireNonNull(parentModule);
        checkArgument(parentModule instanceof ModuleEffectiveStatement, "Parent %s is not a ModuleEffectiveStatement",
            parentModule);
        requireNonNull(submodule);
        checkArgument(submodule instanceof SubmoduleEffectiveStatement,
            "Submodule %s is not a SubmoduleEffectiveStatement", submodule);
        writeReaderToOutput(YinXMLEventReaderFactory.defaultInstance().createXMLEventReader(
            (ModuleEffectiveStatement) parentModule, (SubmoduleEffectiveStatement)submodule), output);
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

    private static void writeReaderToOutput(final XMLEventReader reader, final OutputStream output)
            throws XMLStreamException {
        try {
            final Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.transform(new StAXSource(reader), new StreamResult(output));
        } catch (TransformerException e) {
            throw new XMLStreamException("Failed to stream XML events", e);
        }
    }
}
