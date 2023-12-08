/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.spi;

import com.google.common.annotations.Beta;
import com.google.common.base.FinalizablePhantomReference;
import com.google.common.base.FinalizableReferenceQueue;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.FluentFuture;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
 * @deprecated This class has a rather complicated and ugly design. Use {@link SoftSchemaSourceCache} instead.
 */
@Beta
@Deprecated(since = "7.0.13", forRemoval = true)
public final class GuavaSchemaSourceCache<T extends SchemaSourceRepresentation> extends AbstractSchemaSourceCache<T>
        implements AutoCloseable {
    // FIXME: 7.0.0: use a java.util.Cleaner?
    private final List<FinalizablePhantomReference<T>> regs = Collections.synchronizedList(new ArrayList<>());
    private final FinalizableReferenceQueue queue = new FinalizableReferenceQueue();
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
        final var srcId = source.getIdentifier();
        if (cache.getIfPresent(srcId) != null) {
            // We already have this source, do not track it
            return;
        }

        // Make the source available
        cache.put(srcId, source);
        final var reg = register(srcId);

        final var ref = new FinalizablePhantomReference<>(source, queue) {
            @Override
            public void finalizeReferent() {
                reg.close();
                regs.remove(this);
            }
        };

        regs.add(ref);
    }

    @Override
    public void close() {
        while (!regs.isEmpty()) {
            final FinalizablePhantomReference<?> ref = regs.get(0);
            ref.finalizeReferent();
        }

        cache.invalidateAll();
        queue.close();
    }
}
