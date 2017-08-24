/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.util;

import com.google.common.annotations.Beta;
import com.google.common.base.FinalizablePhantomReference;
import com.google.common.base.FinalizableReferenceQueue;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource.Costs;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistration;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;

@Beta
public class InMemorySchemaSourceCache<T extends SchemaSourceRepresentation> extends AbstractSchemaSourceCache<T>
        implements AutoCloseable {
    private final List<FinalizablePhantomReference<T>> regs = Collections.synchronizedList(new ArrayList<>());
    private final FinalizableReferenceQueue queue = new FinalizableReferenceQueue();
    private final Cache<SourceIdentifier, T> cache;

    protected InMemorySchemaSourceCache(final SchemaSourceRegistry consumer, final Class<T> representation,
            final CacheBuilder<Object, Object> builder) {
        super(consumer, representation, Costs.IMMEDIATE);
        cache = builder.build();
    }

    public static <R extends SchemaSourceRepresentation> InMemorySchemaSourceCache<R> createSoftCache(
            final SchemaSourceRegistry consumer, final Class<R> representation) {
        return new InMemorySchemaSourceCache<>(consumer, representation, CacheBuilder.newBuilder().softValues());
    }

    public static <R extends SchemaSourceRepresentation> InMemorySchemaSourceCache<R> createSoftCache(
            final SchemaSourceRegistry consumer, final Class<R> representation, final long lifetime,
            final TimeUnit units) {
        return new InMemorySchemaSourceCache<>(consumer, representation, CacheBuilder.newBuilder().softValues()
                .expireAfterAccess(lifetime, units));
    }

    @Override
    public ListenableFuture<? extends T> getSource(final SourceIdentifier sourceIdentifier) {
        final T present = cache.getIfPresent(sourceIdentifier);
        if (present != null) {
            return Futures.immediateFuture(present);
        }

        return Futures.immediateFailedFuture(new MissingSchemaSourceException("Source not found", sourceIdentifier));
    }

    @Override
    protected void offer(final T source) {
        final T present = cache.getIfPresent(source.getIdentifier());
        if (present == null) {
            cache.put(source.getIdentifier(), source);

            final SchemaSourceRegistration<T> reg = register(source.getIdentifier());
            final FinalizablePhantomReference<T> ref = new FinalizablePhantomReference<T>(source, queue) {
                @Override
                public void finalizeReferent() {
                    reg.close();
                    regs.remove(this);
                }
            };

            regs.add(ref);
        }
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
