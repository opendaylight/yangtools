/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.spi;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.plugin.generator.api.FileGenerator;

/**
 * Maven 3.1.x and newer uses SLF4J internally, which means we do not need to pass a logger instance around.
 *
 * @deprecated Use {@link FileGenerator} instead.
 */
@Deprecated
public interface BasicCodeGenerator {
    enum ImportResolutionMode {
        /**
         * Standard, RFC6020 and RFC7950 compliant mode. Imports are satisfied by exact revision match (if specified),
         * or by latest available revision.
         */
        REVISION_EXACT_OR_LATEST,
        /**
         * Semantic version based mode. Imports which specify a semantic version (via the OpenConfig extension) will
         * be satisfied by module which exports the latest compatible revision. Imports which do not specify semantic
         * version will be resolved just as they would be via {@link #REVISION_EXACT_OR_LATEST}.
         */
        SEMVER_LATEST,
    }

    /**
     * Generate sources from provided {@link SchemaContext}.
     *
     * @param context
     *            parsed from YANG files
     * @param outputBaseDir
     *            expected output directory for generated sources configured by
     *            user
     * @param currentModules
     *            YANG modules parsed from yangFilesRootDir
     * @param moduleResourcePathResolver
     *            Function converting a local module to the packaged resource path
     * @return collection of files that were generated from schema context
     */
    Collection<File> generateSources(SchemaContext context, File outputBaseDir, Set<Module> currentModules,
            Function<Module, Optional<String>> moduleResourcePathResolver) throws IOException;

    /**
     * Provided map contains all configuration that was set in pom for code
     * generator in additionalConfiguration tag.
     */
    void setAdditionalConfig(Map<String, String> additionalConfiguration);

    /**
     * Provided folder is marked as resources and its content will be packaged
     * in resulting jar. Feel free to add necessary resources.
     */
    void setResourceBaseDir(File resourceBaseDir);

    /**
     * Indicate import resolution mode this code generator requires. Default implementation indicates
     * {@link ImportResolutionMode#REVISION_EXACT_OR_LATEST}.
     *
     * @return Required import resolution mode, null if the code generator does not care.
     */
    // FIXME: This is not really extensible, we should be returning a collection of acceptable modes, or have some sort
    //        of two-step negotiation protocol:
    //        - Optional<ImportResolutionMode> suggestImportResolutionMode();
    //        - boolean isImportResolutionModeAcceptable(ImportResolutionMode);
    //        Let's go with something hacky until we figure out exact requirements.
    default ImportResolutionMode getImportResolutionMode() {
        return ImportResolutionMode.REVISION_EXACT_OR_LATEST;
    }
}
