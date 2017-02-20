/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaResolutionException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.repo.YangTextSchemaContextResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An incremental state reactor. Allows resolution of a SchemaContext based on a set of sources.
 *
 * @author Robert Varga
 */
@NotThreadSafe
final class ProcessorModuleReactor {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessorModuleReactor.class);

    private final YangTextSchemaContextResolver resolver;
    private final Set<SourceIdentifier> sourcesInProject;

    ProcessorModuleReactor(final YangTextSchemaContextResolver resolver) {
        this.resolver = Preconditions.checkNotNull(resolver);
        sourcesInProject = ImmutableSet.copyOf(resolver.getAvailableSources());
    }

    void registerSource(final YangTextSchemaSource source) throws SchemaSourceException, IOException,
            YangSyntaxErrorException {
        resolver.registerSource(source);
    }

    ContextHolder toContext() throws SchemaResolutionException {
        final SchemaContext schemaContext = Verify.verifyNotNull(resolver.trySchemaContext());

        final Set<Module> modules = new HashSet<>();
        for (Module module : schemaContext.getModules()) {
            final SourceIdentifier modId = Util.moduleToIdentifier(module);
            LOG.debug("Looking for source {}", modId);
            if (sourcesInProject.contains(modId)) {
                LOG.debug("Module {} belongs to current project", module);
                modules.add(module);

                for (Module sub : module.getSubmodules()) {
                    final SourceIdentifier subId = Util.moduleToIdentifier(sub);
                    if (sourcesInProject.contains(subId)) {
                        LOG.warn("Submodule {} not found in input files", sub);
                    }
                }
            }
        }

        return new ContextHolder(schemaContext, modules, sourcesInProject);
    }

    Collection<YangTextSchemaSource> getModelsInProject() {
        return Collections2.transform(sourcesInProject, id -> resolver.getSourceTexts(id).iterator().next());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("sources", sourcesInProject).add("resolver", resolver).toString();
    }
}
