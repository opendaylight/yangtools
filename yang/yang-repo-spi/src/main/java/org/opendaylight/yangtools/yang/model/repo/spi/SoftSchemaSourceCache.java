/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.spi;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource.Costs;
import org.opendaylight.yangtools.yang.model.spi.source.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.spi.source.SourceIdentifier;

/**
 * A simple {@link AbstractSchemaSourceCache} maintaining soft references.
 *
 * @param <T> {@link SchemaSourceRepresentation} type stored in this cache
 */
@Beta
public final class SoftSchemaSourceCache<T extends SchemaSourceRepresentation> extends AbstractSchemaSourceCache<T>
        implements AutoCloseable {
    private static final Cleaner CLEANER = Cleaner.create();

    private final ConcurrentMap<SourceIdentifier, SoftReference<T>> references = new ConcurrentHashMap<>();
    private final ConcurrentMap<Registration, Cleanable> cleanables = new ConcurrentHashMap<>();

    private boolean closed;

    public SoftSchemaSourceCache(final SchemaSourceRegistry consumer, final Class<T> representation) {
        super(consumer, representation, Costs.IMMEDIATE);
    }

    @Override
    public ListenableFuture<? extends T> getSource(final SourceIdentifier sourceIdentifier) {
        final var ref = references.get(sourceIdentifier);
        if (ref != null) {
            final var src = ref.get();
            if (src != null) {
                // We have a hit
                return Futures.immediateFuture(src);
            }

            // Expired entry: remove it
            references.remove(sourceIdentifier, ref);
        }

        return Futures.immediateFailedFuture(new MissingSchemaSourceException(sourceIdentifier, "Source not found"));
    }

    @Override
    public synchronized void close() {
        if (!closed) {
            closed = true;
            while (!cleanables.isEmpty()) {
                cleanables.values().forEach(Cleanable::clean);
            }
        }
    }

    @Override
    protected synchronized void offer(final T source) {
        if (closed) {
            return;
        }

        final var sourceId = source.sourceId();
        final var ref = new SoftReference<>(source);

        while (true) {
            final var prev = references.putIfAbsent(sourceId, ref);
            if (prev == null) {
                // We have performed a fresh insert and need to add a cleanup
                break;
            }

            if (prev.get() != null) {
                // We still have a source for this identifier, no further action is needed
                return;
            }

            // Existing reference is dead, remove it and retry
            references.remove(sourceId, prev);
        }

        // We have populated a cache entry, register the source and a cleanup action
        final var reg = register(sourceId);
        cleanables.put(reg, CLEANER.register(source, () -> {
            cleanables.remove(reg);
            reg.close();
            references.remove(sourceId, ref);
        }));

        // Ensure 'source' is still reachable here. This is needed to ensure the cleanable action does not fire before
        // we have had a chance to insert it into the map.
        Reference.reachabilityFence(source);
    }
}
