/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FluentFuture;
import java.io.IOException;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactoryConfiguration;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaResolutionException;
import org.opendaylight.yangtools.yang.model.repo.api.YangIRSchemaSource;
import org.opendaylight.yangtools.yang.model.spi.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangModelDependencyInfo;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class AssembleSources implements AsyncFunction<List<YangIRSchemaSource>, EffectiveModelContext> {
    private static final Logger LOG = LoggerFactory.getLogger(AssembleSources.class);

    private final @NonNull Function<YangIRSchemaSource, SourceIdentifier> getIdentifier;
    private final @NonNull SchemaContextFactoryConfiguration config;
    private final @NonNull YangParserFactory parserFactory;

    AssembleSources(final @NonNull YangParserFactory parserFactory,
            final @NonNull SchemaContextFactoryConfiguration config) {
        this.parserFactory = parserFactory;
        this.config = config;
        getIdentifier = switch (config.getStatementParserMode()) {
            case DEFAULT_MODE -> YangIRSchemaSource::sourceId;
        };
    }

    @Override
    public FluentFuture<EffectiveModelContext> apply(final List<YangIRSchemaSource> sources) {
        final var srcs = Maps.uniqueIndex(sources, getIdentifier);
        final var deps = Maps.transformValues(srcs, YangModelDependencyInfo::forIR);
        LOG.debug("Resolving dependency reactor {}", deps);

        final var res = switch (config.getStatementParserMode()) {
            case DEFAULT_MODE -> RevisionDependencyResolver.create(deps);
        };

        final var unresolved = res.unresolvedSources();
        if (!unresolved.isEmpty()) {
            LOG.debug("Omitting models {} due to unsatisfied imports {}", unresolved, res.unsatisfiedImports());
            return FluentFutures.immediateFailedFluentFuture(
                new SchemaResolutionException("Failed to resolve required models", unresolved.get(0),
                    res.resolvedSources(), res.unsatisfiedImports()));
        }

        final var parser = parserFactory.createParser(res.parserConfig());
        config.getSupportedFeatures().ifPresent(parser::setSupportedFeatures);
        config.getModulesDeviatedByModules().ifPresent(parser::setModulesWithSupportedDeviations);

        for (var entry : srcs.entrySet()) {
            try {
                parser.addSource(entry.getValue());
            } catch (YangSyntaxErrorException | IOException e) {
                final var sourceId = entry.getKey();
                return FluentFutures.immediateFailedFluentFuture(
                    new SchemaResolutionException("Failed to add source " + sourceId, sourceId, e));
            }
        }

        final EffectiveModelContext schemaContext;
        try {
            schemaContext = parser.buildEffectiveModel();
        } catch (final YangParserException e) {
            return FluentFutures.immediateFailedFluentFuture(e.getCause() instanceof ReactorException re
                ? new SchemaResolutionException("Failed to resolve required models", re.getSourceIdentifier(), re) : e);
        }

        return FluentFutures.immediateFluentFuture(schemaContext);
    }
}
