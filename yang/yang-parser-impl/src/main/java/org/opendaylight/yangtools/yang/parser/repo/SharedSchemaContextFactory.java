/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.opendaylight.yangtools.util.concurrent.ExceptionMapper;
import org.opendaylight.yangtools.util.concurrent.ReflectiveExceptionMapper;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactory;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaResolutionException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceFilter;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.opendaylight.yangtools.yang.parser.impl.YangParserListenerImpl;
import org.opendaylight.yangtools.yang.parser.impl.util.YangModelDependencyInfo;
import org.opendaylight.yangtools.yang.parser.util.ASTSchemaSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SharedSchemaContextFactory implements SchemaContextFactory {
    private static final ExceptionMapper<SchemaResolutionException> MAPPER = ReflectiveExceptionMapper.create("resolve sources", SchemaResolutionException.class);
    private static final Logger LOG = LoggerFactory.getLogger(SharedSchemaContextFactory.class);

    private final Function<SourceIdentifier, ListenableFuture<ASTSchemaSource>> requestSources = new Function<SourceIdentifier, ListenableFuture<ASTSchemaSource>>() {
        @Override
        public ListenableFuture<ASTSchemaSource> apply(final SourceIdentifier input) {
            return repository.getSchemaSource(input, ASTSchemaSource.class);
        }
    };
    private final Cache<Collection<SourceIdentifier>, SchemaContext> cache = CacheBuilder.newBuilder().softValues().build();

    private final AsyncFunction<List<ASTSchemaSource>, SchemaContext> assembleSources = new AsyncFunction<List<ASTSchemaSource>, SchemaContext>() {
        @Override
        public ListenableFuture<SchemaContext> apply(final List<ASTSchemaSource> sources) throws SchemaResolutionException {
            final Map<SourceIdentifier, ASTSchemaSource> srcs =
                    Maps.uniqueIndex(sources, ASTSchemaSource.GET_IDENTIFIER);
            final Map<SourceIdentifier, YangModelDependencyInfo> deps =
                    Maps.transformValues(srcs, ASTSchemaSource.GET_DEPINFO);

            LOG.debug("Resolving dependency reactor {}", deps);

            final DependencyResolver res = DependencyResolver.create(deps);
            if (!res.getUnresolvedSources().isEmpty()) {
                LOG.debug("Omitting models {} due to unsatisfied imports {}", res.getUnresolvedSources(), res.getUnsatisfiedImports());

                // FIXME: push into DependencyResolver

                throw new SchemaResolutionException("Failed to resolve required models",
                        res.getResolvedSources(), res.getUnsatisfiedImports());
            }

            final Map<SourceIdentifier, ParserRuleContext> asts =
                    Maps.transformValues(srcs, ASTSchemaSource.GET_AST);
            final Map<String, TreeMap<Date, URI>> namespaceContext = BuilderUtils.createYangNamespaceContext(
                    asts.values(), Optional.<SchemaContext> absent());

            final ParseTreeWalker walker = new ParseTreeWalker();
            final Map<SourceIdentifier, ModuleBuilder> sourceToBuilder = new LinkedHashMap<>();

            for (Entry<SourceIdentifier, ParserRuleContext> entry : asts.entrySet()) {
                ModuleBuilder moduleBuilder = YangParserListenerImpl.create(namespaceContext, entry.getKey().getName(),
                        walker, entry.getValue()).getModuleBuilder();

                moduleBuilder.setSource(srcs.get(entry.getKey()).getYangText());
                sourceToBuilder.put(entry.getKey(), moduleBuilder);
            }
            LOG.debug("Modules ready for integration");

            final YangParserImpl parser = YangParserImpl.getInstance();
            final Collection<Module> modules = parser.buildModules(sourceToBuilder.values());
            LOG.debug("Integrated cross-references modules");
            return Futures.immediateCheckedFuture(parser.assembleContext(modules));
        }
    };

    private final SharedSchemaRepository repository;
    // FIXME: ignored right now
    private final SchemaSourceFilter filter;

    // FIXME SchemaRepository should be the type for repository parameter instead of SharedSchemaRepository (final implementation)
    public SharedSchemaContextFactory(final SharedSchemaRepository repository, final SchemaSourceFilter filter) {
        this.repository = Preconditions.checkNotNull(repository);
        this.filter = Preconditions.checkNotNull(filter);
    }

    @Override
    public CheckedFuture<SchemaContext, SchemaResolutionException> createSchemaContext(final Collection<SourceIdentifier> requiredSources) {
        // Make sources unique
        // TODO maybe a Set as parameter would be a better solution
        final Set<SourceIdentifier> uniqueSourceIdentifiers = deDuplicateSources(requiredSources);

        final SchemaContext existing = cache.getIfPresent(uniqueSourceIdentifiers);
        if (existing != null) {
            LOG.debug("Returning cached context {}", existing);
            return Futures.immediateCheckedFuture(existing);
        }

        // Request all sources be loaded
        final ListenableFuture<List<ASTSchemaSource>> sf = Futures.allAsList(Collections2.transform(uniqueSourceIdentifiers, requestSources));

        // Assemble sources into a schema context
        final ListenableFuture<SchemaContext> cf = Futures.transform(sf, assembleSources);

        // Populate cache when successful
        Futures.addCallback(cf, new FutureCallback<SchemaContext>() {
            @Override
            public void onSuccess(final SchemaContext result) {
                cache.put(uniqueSourceIdentifiers, result);
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.debug("Failed to assemble sources", t);
            }
        });

        return Futures.makeChecked(cf, MAPPER);
    }

    private Set<SourceIdentifier> deDuplicateSources(final Collection<SourceIdentifier> requiredSources) {
        final Set<SourceIdentifier> uniqueSourceIdentifiers = Collections.unmodifiableSet(Sets.newHashSet(requiredSources));
        if(uniqueSourceIdentifiers.size() != requiredSources.size()) {
            LOG.warn("Duplicate sources requested for schema context, removed duplicate sources: {}", Collections2.filter(uniqueSourceIdentifiers, new Predicate<SourceIdentifier>() {
                @Override
                public boolean apply(@Nullable final SourceIdentifier input) {
                    return Iterables.frequency(requiredSources, input) > 1;
                }
            }));
        }
        return uniqueSourceIdentifiers;
    }
}
