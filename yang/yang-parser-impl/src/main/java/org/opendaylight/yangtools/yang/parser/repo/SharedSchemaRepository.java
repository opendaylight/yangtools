/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.repo;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactory;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceFilter;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.util.AbstractSchemaRepository;

/**
 * A {@link SchemaRepository} which allows sharing of {@link SchemaContext} as long as their specification is the same.
 *
 * <p>
 * Note: for current implementation, "same" means the same filter and the same set of {@link SourceIdentifier}s.
 */
@Beta
public final class SharedSchemaRepository extends AbstractSchemaRepository implements Identifiable<String> {
    private final LoadingCache<SchemaSourceFilter, SchemaContextFactory> cache =
            CacheBuilder.newBuilder().softValues().build(new CacheLoader<SchemaSourceFilter, SchemaContextFactory>() {
                @Override
                public SchemaContextFactory load(@Nonnull final SchemaSourceFilter key) {
                    return new SharedSchemaContextFactory(SharedSchemaRepository.this, key);
                }
            });
    private final String id;

    public SharedSchemaRepository(final String id) {
        this.id = Preconditions.checkNotNull(id);
    }

    @Override
    public String getIdentifier() {
        return id;
    }

    @Override
    public SchemaContextFactory createSchemaContextFactory(@Nonnull final SchemaSourceFilter filter) {
        return cache.getUnchecked(filter);
    }

    @Override
    public String toString() {
        return "SchemaRepository: " + id;
    }
}
