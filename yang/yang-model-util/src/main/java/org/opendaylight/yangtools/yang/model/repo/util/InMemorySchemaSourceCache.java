/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;

import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource.Costs;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistration;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;

@Beta
public class InMemorySchemaSourceCache<T extends SchemaSourceRepresentation> extends AbstractSchemaSourceCache<T> {
    private static final class CacheEntry<T extends SchemaSourceRepresentation> {
        private final SchemaSourceRegistration<T> reg;
        private final T source;

        public CacheEntry(final T source, final SchemaSourceRegistration<T> reg) {
            this.source = Preconditions.checkNotNull(source);
            this.reg = Preconditions.checkNotNull(reg);
        }
    }

    private static final RemovalListener<SourceIdentifier, CacheEntry<?>> LISTENER = new RemovalListener<SourceIdentifier, CacheEntry<?>>() {
        @Override
        public void onRemoval(final RemovalNotification<SourceIdentifier, CacheEntry<?>> notification) {
            notification.getValue().reg.close();
        }
    };

    private final Cache<SourceIdentifier, CacheEntry<T>> cache;

    protected InMemorySchemaSourceCache(final SchemaSourceRegistry consumer, final Class<T> representation, final CacheBuilder<Object, Object> builder) {
        super(consumer, representation, Costs.IMMEDIATE);
        cache = builder.removalListener(LISTENER).build();
    }

    public static <R extends SchemaSourceRepresentation> InMemorySchemaSourceCache<R> createSoftCache(final SchemaSourceRegistry consumer, final Class<R> representation) {
        return new InMemorySchemaSourceCache<>(consumer, representation, CacheBuilder.newBuilder().softValues());
    }

    @Override
    public CheckedFuture<? extends T, SchemaSourceException> getSource(final SourceIdentifier sourceIdentifier) {
        final CacheEntry<T> present = cache.getIfPresent(sourceIdentifier);
        if (present != null) {
            return Futures.immediateCheckedFuture(present.source);
        }

        return Futures.<T, SchemaSourceException>immediateFailedCheckedFuture(new MissingSchemaSourceException("Source not found", sourceIdentifier));
    }

    @Override
    protected void offer(final T source) {
        final CacheEntry<T> present = cache.getIfPresent(source.getIdentifier());
        if (present == null) {
            final SchemaSourceRegistration<T> reg = register(source.getIdentifier());
            cache.put(source.getIdentifier(), new CacheEntry<T>(source, reg));
        }
    }
}
