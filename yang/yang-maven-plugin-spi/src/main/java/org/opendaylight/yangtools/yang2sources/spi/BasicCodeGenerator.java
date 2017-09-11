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

/**
 * Maven 3.1.x and newer uses SLF4J internally, which means we do not need to pass
 * a logger instance around.
 */
public interface BasicCodeGenerator {
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
    Collection<File> generateSources(final SchemaContext context, final File outputBaseDir,
            final Set<Module> currentModules,
            final Function<Module, Optional<String>> moduleResourcePathResolver) throws IOException;
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
}
