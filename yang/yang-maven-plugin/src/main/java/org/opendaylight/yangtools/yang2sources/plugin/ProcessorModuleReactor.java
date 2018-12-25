/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangParser;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
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

    private final Map<SourceIdentifier, YangTextSchemaSource> modelsInProject;
    private final Collection<ScannedDependency> dependencies;
    private final YangParser parser;

    ProcessorModuleReactor(final YangParser parser, final Collection<YangTextSchemaSource> modelsInProject,
        final Collection<ScannedDependency> dependencies) {
        this.parser = requireNonNull(parser);
        this.modelsInProject = Maps.uniqueIndex(modelsInProject, YangTextSchemaSource::getIdentifier);
        this.dependencies = ImmutableList.copyOf(dependencies);
    }

    ContextHolder toContext() throws IOException, YangParserException {
        for (YangTextSchemaSource source : toUniqueSources(dependencies)) {
            // This source is coming from a dependency:
            // - its identifier should be accurate, as it should have been processed into a file with accurate name
            // - it is not required to be parsed, hence we add it just as a library source
            parser.addLibSource(source);
        }

        final SchemaContext schemaContext = Verify.verifyNotNull(parser.buildSchemaContext());

        final Set<Module> modules = new HashSet<>();
        for (Module module : schemaContext.getModules()) {
            final SourceIdentifier modId = Util.moduleToIdentifier(module);
            LOG.debug("Looking for source {}", modId);
            if (modelsInProject.containsKey(modId)) {
                LOG.debug("Module {} belongs to current project", module);
                modules.add(module);

                for (Module sub : module.getSubmodules()) {
                    final SourceIdentifier subId = Util.moduleToIdentifier(sub);
                    if (!modelsInProject.containsKey(subId)) {
                        LOG.warn("Submodule {} not found in input files", sub);
                    }
                }
            }
        }

        return new ContextHolder(schemaContext, modules, modelsInProject.keySet());
    }

    Collection<YangTextSchemaSource> getModelsInProject() {
        return modelsInProject.values();
    }

    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE",
        justification = "https://github.com/spotbugs/spotbugs/issues/600")
    private static Collection<YangTextSchemaSource> toUniqueSources(final Collection<ScannedDependency> dependencies)
            throws IOException {
        final Map<String, YangTextSchemaSource> byContent = new HashMap<>();

        for (ScannedDependency dependency : dependencies) {
            for (YangTextSchemaSource s : dependency.sources()) {
                try (Reader reader = s.asCharSource(StandardCharsets.UTF_8).openStream()) {
                    final String contents = CharStreams.toString(reader);
                    byContent.putIfAbsent(contents, s);
                }
            }
        }
        return byContent.values();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("sources", modelsInProject.keySet()).add("parser", parser)
                .toString();
    }
}
