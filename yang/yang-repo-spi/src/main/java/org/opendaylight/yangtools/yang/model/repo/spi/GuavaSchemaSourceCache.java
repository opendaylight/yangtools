/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.spi;

import com.google.common.annotations.Beta;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.FluentFuture;
import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

/**
 * A simple {@link AbstractSchemaSourceCache} based on {@link Cache Guava Cache}.
 *
 * @param <T> {@link SchemaSourceRepresentation} type stored in this cache
 */
@Beta
public final class GuavaSchemaSourceCache<T extends SchemaSourceRepresentation> extends AbstractSchemaSourceCache<T>
        implements AutoCloseable {
    private static final Cleaner CLEANER = Cleaner.create();

    private final Set<Cleanable> refs = ConcurrentHashMap.newKeySet();
    private final Cache<SourceIdentifier, T> cache;

    private GuavaSchemaSourceCache(final SchemaSourceRegistry consumer, final Class<T> representation,
            final CacheBuilder<Object, Object> cacheBuilder) {
        super(consumer, representation, PotentialSchemaSource.Costs.IMMEDIATE);
        cache = cacheBuilder.build();
    }

    public static <R extends SchemaSourceRepresentation> @NonNull GuavaSchemaSourceCache<R> createSoftCache(
            final SchemaSourceRegistry consumer, final Class<R> representation) {
        return new GuavaSchemaSourceCache<>(consumer, representation, CacheBuilder.newBuilder().softValues());
    }

    public static <R extends SchemaSourceRepresentation> @NonNull GuavaSchemaSourceCache<R> createSoftCache(
            final SchemaSourceRegistry consumer, final Class<R> representation, final long lifetime,
            final TimeUnit units) {
        return new GuavaSchemaSourceCache<>(consumer, representation, CacheBuilder.newBuilder().softValues()
            .expireAfterAccess(lifetime, units));
    }

    public static <R extends SchemaSourceRepresentation> @NonNull GuavaSchemaSourceCache<R> createSoftCache(
            final SchemaSourceRegistry consumer, final Class<R> representation, final Duration duration) {
        return new GuavaSchemaSourceCache<>(consumer, representation, CacheBuilder.newBuilder().softValues()
            .expireAfterAccess(duration));
    }

    @Override
    public FluentFuture<? extends T> getSource(final SourceIdentifier sourceIdentifier) {
        final T present = cache.getIfPresent(sourceIdentifier);
        return present != null ? FluentFutures.immediateFluentFuture(present)
                : FluentFutures.immediateFailedFluentFuture(new MissingSchemaSourceException("Source not found",
                    sourceIdentifier));
    }

    @Override
    protected void offer(final T source) {
        final T present = cache.getIfPresent(source.getIdentifier());
        if (present == null) {
            cache.put(source.getIdentifier(), source);

            final var reg = register(source.getIdentifier());
            refs.add(CLEANER.register(source, reg::close));
        }
    }

    @Override
    public void close() {
        while (!refs.isEmpty()) {
            final var it = refs.iterator();
            while (it.hasNext()) {
                final var ref = it.next();
                it.remove();
                ref.clean();
            }
        }

        cache.invalidateAll();
    }
}
