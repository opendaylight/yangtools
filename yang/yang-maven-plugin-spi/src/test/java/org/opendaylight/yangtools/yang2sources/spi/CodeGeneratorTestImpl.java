/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.apache.maven.project.MavenProject;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodeGeneratorTestImpl implements BasicCodeGenerator, MavenProjectAware {

    private static final Logger LOG = LoggerFactory.getLogger(CodeGeneratorTestImpl.class);

    @Override
    public Collection<File> generateSources(SchemaContext context, File outputBaseDir, Set<Module> currentModules,
            Function<Module, Optional<String>> moduleResourcePathResolver) throws IOException {
        LOG.debug("{} generateSources:context: {}", getClass().getCanonicalName(), context);
        LOG.debug("{} generateSources:outputBaseDir: {}", getClass().getCanonicalName(), outputBaseDir);
        LOG.debug("{} generateSources:currentModules: {}", getClass().getCanonicalName(), currentModules);
        return null;
    }

    @Override
    public void setAdditionalConfig(final Map<String, String> additionalConfiguration) {
        LOG.debug("{} additionalConfig: {}", getClass().getCanonicalName(), additionalConfiguration);
    }

    @Override
    public void setResourceBaseDir(final File resourceBaseDir) {
        LOG.debug("{} resourceBaseDir: {}", getClass().getCanonicalName(), resourceBaseDir);
    }

    @Override
    public void setMavenProject(final MavenProject project) {
        LOG.debug("{} maven project: {}", getClass().getCanonicalName(), project);
    }
}
