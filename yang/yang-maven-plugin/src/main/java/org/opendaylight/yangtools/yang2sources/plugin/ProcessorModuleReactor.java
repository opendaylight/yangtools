/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
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

    private SchemaContext schemaContext;
    private Map<Module, SourceIdentifier> modulesInProject;

    ProcessorModuleReactor(final YangTextSchemaContextResolver resolver) {
        this.resolver = Preconditions.checkNotNull(resolver);
        sourcesInProject = ImmutableSet.copyOf(resolver.getAvailableSources());
    }

    SchemaContext getSchemaContext() throws SchemaResolutionException {
        SchemaContext ret;
        return (ret = schemaContext) != null ? ret : (schemaContext = resolver.trySchemaContext());
    }

    void registerSource(final YangTextSchemaSource source) throws SchemaSourceException, IOException,
            YangSyntaxErrorException {
        resolver.registerSource(source);
    }

    Map<Module, SourceIdentifier> getModulesInProject() {
        Map<Module, SourceIdentifier> ret;
        return (ret = modulesInProject) != null ? ret : (modulesInProject = computeModulesInProject());
    }

    private Map<Module, SourceIdentifier> computeModulesInProject() {
        final SchemaContext context = schemaContext;
        Preconditions.checkState(context != null, "SchemaContext not initialized");

        final Map<Module, SourceIdentifier> modules = new HashMap<>();
        for (Module module : schemaContext.getModules()) {
            final QNameModule mod = module.getQNameModule();
            final Date rev = mod.getRevision();
            final Optional<String> optRev;
            if (!SimpleDateFormatUtil.DEFAULT_DATE_REV.equals(rev)) {
                optRev = Optional.of(mod.getFormattedRevision());
            } else {
                optRev = Optional.absent();
            }

            final SourceIdentifier modId = RevisionSourceIdentifier.create(module.getName(), optRev);
            LOG.debug("Looking for source {}", modId);
            if (sourcesInProject.contains(modId)) {
                LOG.debug("Module {} belongs to current project", module);
                modules.put(module, modId);
            }
        }

        return ImmutableMap.copyOf(modules);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("sources", sourcesInProject).add("resolver", resolver).toString();
    }

    Collection<YangTextSchemaSource> getModelsInProject() {
        return Collections2.transform(sourcesInProject, id -> resolver.getSourceTexts(id).iterator().next());
    }
}
