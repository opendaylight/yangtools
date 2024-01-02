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
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.Submodule;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextSource;
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

        final EffectiveModelContext schemaContext = verifyNotNull(parser.buildEffectiveModel());
        parser = null;

        final Set<Module> modules = new HashSet<>();
        for (Module module : schemaContext.getModules()) {
            final SourceIdentifier modId = Util.moduleToIdentifier(module);
            LOG.debug("Looking for source {}", modId);
            if (modelsInProject.containsKey(modId)) {
                LOG.debug("Module {} belongs to current project", module);
                modules.add(module);

                for (Submodule sub : module.getSubmodules()) {
                    final SourceIdentifier subId = Util.moduleToIdentifier(sub);
                    if (!modelsInProject.containsKey(subId)) {
                        LOG.warn("Submodule {} not found in input files", sub);
                    }
                }
            }
        }

        return new ContextHolder(schemaContext, modules, modelsInProject.keySet());
    }

    Collection<YangTextSource> getModelsInProject() {
        return modelsInProject.values();
    }

    private static Collection<YangTextSource> toUniqueSources(final Collection<ScannedDependency> dependencies)
            throws IOException {
        final Map<String, YangTextSource> byContent = new HashMap<>();

        for (ScannedDependency dependency : dependencies) {
            for (YangTextSource s : dependency.sources()) {
                try (Reader reader = s.openStream()) {
                    final String contents = CharStreams.toString(reader);
                    byContent.putIfAbsent(contents, s);
                }
            }
        }
        return byContent.values();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("sources", modelsInProject.keySet()).toString();
    }
}
