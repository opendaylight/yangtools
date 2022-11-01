/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.plugin.generator.api;

import com.google.common.collect.Table;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Interface implemented by plugins which can generate files from a {@link EffectiveModelContext}.
 *
 * @author Robert Varga
 */
@NonNullByDefault
public interface FileGenerator {
    /**
     * Indicate import resolution mode this code generator requires. Default implementation indicates
     * {@link ImportResolutionMode#REVISION_EXACT_OR_LATEST}.
     *
     * @implNote
     *     Default implementation returns {@link ImportResolutionMode#REVISION_EXACT_OR_LATEST}.
     *
     * @return Required import resolution mode.
     */
    default ImportResolutionMode importResolutionMode() {
        return ImportResolutionMode.REVISION_EXACT_OR_LATEST;
    }

    /**
     * Generate files from a {@link SchemaContext}, being aware the that specific modules are local to the current
     * project being processed.
     *
     * <p>
     * Implementations of this interface must not interact with project directory directly, but rather supply the files
     * generated as a set of {@link GeneratedFile}s. The caller of this method will use these to integrate with build
     * management to ensure proper dependency tracking is performed.
     *
     * @param context SchemaContext to examine
     * @param localModules Modules local to the project
     * @param moduleResourcePathResolver Module-to-resource path resolver
     * @return The set of generated files.
     * @throws FileGeneratorException if anything bad happens
     */
    Table<GeneratedFileType, GeneratedFilePath, GeneratedFile> generateFiles(EffectiveModelContext context,
            Set<Module> localModules, ModuleResourceResolver moduleResourcePathResolver) throws FileGeneratorException;

    /**
     * {@link EffectiveModelContext} can be assembled in multiple ways, we hold known modes here.
     */
    enum ImportResolutionMode {
        /**
         * Standard, RFC6020 and RFC7950 compliant mode. Imports are satisfied by exact revision match (if specified),
         * or by latest available revision.
         */
        REVISION_EXACT_OR_LATEST;
    }
}
