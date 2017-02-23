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
import org.opendaylight.yangtools.yang.maven.spi.generator.ProjectContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Maven 3.1.x and newer uses SLF4J internally, which means we do not need to pass
 * a logger instance around.
 */
public interface BasicCodeGenerator {
    /**
     * Generate sources from provided {@link SchemaContext}
     *
     * @param context
     *            parsed from YANG files
     * @param outputBaseDir
     *            expected output directory for generated sources configured by
     *            user
     * @param currentModules
     *            YANG modules parsed from yangFilesRootDir
     * @return collection of files that were generated from schema context
     * @throws IOException
     *
     * @deprecated Implement {@link #generateSources(SchemaContext, File, Set, Function)} instead.
     */
    @Deprecated
    Collection<File> generateSources(SchemaContext context, File outputBaseDir, Set<Module> currentModules)
            throws IOException;

    /**
     * Generate sources from provided {@link ProjectContext}
     *
     * @param context
     *            parsed from YANG files
     * @param outputBaseDir
     *            expected output directory for generated sources configured by
     *            user
     * @return collection of files that were generated from schema context
     * @throws IOException
     */
    default Collection<File> generateSources(final SchemaContext context, final File outputBaseDir,
            final Set<Module> currentModules,
            final Function<Module, Optional<String>> moduleResourcePathResolver) throws IOException {
        return generateSources(context, outputBaseDir, currentModules);
    }

    /**
     * Provided map contains all configuration that was set in pom for code
     * generator in additionalConfiguration tag
     *
     * @param additionalConfiguration
     */
    void setAdditionalConfig(Map<String, String> additionalConfiguration);

    /**
     * Provided folder is marked as resources and its content will be packaged
     * in resulting jar. Feel free to add necessary resources
     *
     * @param resourceBaseDir
     */
    void setResourceBaseDir(File resourceBaseDir);
}
