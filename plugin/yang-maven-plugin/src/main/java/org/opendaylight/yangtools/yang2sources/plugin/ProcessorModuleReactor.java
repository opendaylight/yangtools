/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParser;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An incremental state reactor. Allows resolution of a SchemaContext based on a set of sources.
 */
// FIXME: reneame to ExecutionModuleReactor or similar
final class ProcessorModuleReactor {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessorModuleReactor.class);

    private final Map<SourceIdentifier, YangTextSource> modelsInProject;
    private final Collection<ScannedDependency> dependencies;

    private YangParser parser;

    ProcessorModuleReactor(final YangParser parser, final Collection<YangTextSource> modelsInProject,
            final Collection<ScannedDependency> dependencies) {
        this.parser = requireNonNull(parser);
        this.modelsInProject = Maps.uniqueIndex(modelsInProject, YangTextSource::sourceId);
        this.dependencies = ImmutableList.copyOf(dependencies);
    }

    ContextHolder toContext() throws IOException, YangParserException {
        checkState(parser != null, "Context has already been assembled");

        for (var source : toUniqueSources(dependencies)) {
            // This source is coming from a dependency:
            // - its identifier should be accurate, as it should have been processed into a file with accurate name
            // - it is not required to be parsed, hence we add it just as a library source
            parser.addLibSource(source);
        }

        final var modelContext = verifyNotNull(parser.buildEffectiveModel());
        parser = null;

        final var modules = new HashSet<Module>();
        for (var module : modelContext.getModules()) {
            final var modId = ContextHolder.moduleToIdentifier(module);
            LOG.debug("Looking for source {}", modId);
            if (modelsInProject.containsKey(modId)) {
                LOG.debug("Module {} belongs to current project", module);
                modules.add(module);

                for (var sub : module.getSubmodules()) {
                    final var subId = ContextHolder.moduleToIdentifier(sub);
                    if (!modelsInProject.containsKey(subId)) {
                        LOG.warn("Submodule {} not found in input files", sub);
                    }
                }
            }
        }

        return new ContextHolder(modelContext, modules, modelsInProject.keySet());
    }

    Collection<YangTextSource> getModelsInProject() {
        return modelsInProject.values();
    }

    private static Collection<YangTextSource> toUniqueSources(final Collection<ScannedDependency> dependencies)
            throws IOException {
        final var byContent = new HashMap<String, YangTextSource>();

        for (var dependency : dependencies) {
            for (var source : dependency.sources()) {
                byContent.putIfAbsent(source.read(), source);
            }
        }
        return byContent.values();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("sources", modelsInProject.keySet()).toString();
    }
}
