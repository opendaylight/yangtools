/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.eclipse.jdt.annotation.NonNull;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.EffectiveModelContextFactory;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactoryConfiguration;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository;
import org.opendaylight.yangtools.yang.model.repo.spi.AbstractSchemaRepository;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.impl.DefaultYangParserFactory;

/**
 * A {@link SchemaRepository} which allows sharing of {@link SchemaContext} as long as their specification is the same.
 *
 * <p>
 * Note: for current implementation, "same" means the same filter and the same set of {@link SourceIdentifier}s.
 */
@Beta
@MetaInfServices(value = SchemaRepository.class)
public final class SharedSchemaRepository extends AbstractSchemaRepository implements Identifiable<String> {
    private final LoadingCache<SchemaContextFactoryConfiguration, EffectiveModelContextFactory> cacheByConfig =
            CacheBuilder.newBuilder().softValues()
            .build(new CacheLoader<SchemaContextFactoryConfiguration, EffectiveModelContextFactory>() {
                @Override
                public EffectiveModelContextFactory load(final SchemaContextFactoryConfiguration key) {
                    return new SharedEffectiveModelContextFactory(SharedSchemaRepository.this, key);
                }
            });

    private final @NonNull String id;
    private final @NonNull YangParserFactory factory;

    public SharedSchemaRepository() {
        this("unnamed");
    }

    public SharedSchemaRepository(final String id) {
        this(id, new DefaultYangParserFactory());
    }

    public SharedSchemaRepository(final String id, final YangParserFactory factory) {
        this.id = requireNonNull(id);
        this.factory = requireNonNull(factory);
    }

    @Override
    public @NonNull String getIdentifier() {
        return id;
    }

    @Override
    public @NonNull EffectiveModelContextFactory createEffectiveModelContextFactory(
            final @NonNull SchemaContextFactoryConfiguration config) {
        return cacheByConfig.getUnchecked(config);
    }

    @NonNull YangParserFactory factory() {
        return factory;
    }

    @Override
    public String toString() {
        return "SchemaRepository: " + id;
    }
}
