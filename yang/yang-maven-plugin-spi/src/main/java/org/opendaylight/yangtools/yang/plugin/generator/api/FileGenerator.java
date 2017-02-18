/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.plugin.generator.api;

import com.google.common.annotations.Beta;
import com.google.common.collect.Table;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Interface implemented by plugins which can generate files from a {@link SchemaContext}.
 *
 * @author Robert Varga
 */
@Beta
public interface FileGenerator {
    Optional<ImportResolutionMode> suggestedImportResolutionMode();

    boolean isAcceptableImportResolutionMode(ImportResolutionMode mode);

    /**
     * Generate files from a {@link SchemaContext}, being aware the that specific modules are local to the current
     * project being processed.
     *
     * <p>
     * Implementations of this interface must not interact directly with project directory directly, but rather supply
     * the files generated as a set of {@link GeneratedFile}s. The caller of this method will use these to integrate
     * with build management to ensure proper dependency tracking is performed.
     *
     * @param context SchemaContext to examine
     * @param localModules Modules local to the project
     * @param moduleResourcePathResolver Module -> resource path resolver
     * @return The set of generated files. Note that the {@code column String} must use resource path separator
     *         ({@code '/'}).
     */
    Table<GeneratedFileKind, String, GeneratedFile> generateFiles(SchemaContext context,
            Set<Module> localModules, Function<Module, Optional<String>> moduleResourcePathResolver);
}
