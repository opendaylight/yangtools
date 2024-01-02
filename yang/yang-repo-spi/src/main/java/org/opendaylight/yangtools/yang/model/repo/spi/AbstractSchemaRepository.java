/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.spi;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.util.concurrent.FluentFutures.immediateFailedFluentFuture;

import com.google.common.annotations.Beta;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for {@link SchemaRepository} implementations. It handles registration and lookup of schema
 * sources, subclasses need only to provide their own {@link #createEffectiveModelContextFactory()} implementation.
 */
@Beta
public abstract class AbstractSchemaRepository implements SchemaRepository, SchemaSourceRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSchemaRepository.class);

    /*
     * Source identifier -> representation -> provider map. We usually are looking for
     * a specific representation of a source.
     */
    @GuardedBy("this")
    private final Map<SourceIdentifier, ListMultimap<Class<? extends SourceRepresentation>,
            SchemaSourceRegistration>> sources = new HashMap<>();

    /*
     * Schema source listeners.
     */
    @GuardedBy("this")
    private final List<SchemaListenerRegistration> listeners = new ArrayList<>();

    private static <T extends SourceRepresentation> ListenableFuture<T> fetchSource(
            final SourceIdentifier sourceId, final Iterator<SchemaSourceRegistration> it) {
        final var reg = it.next();
        @SuppressWarnings("unchecked")
        final var provider = (SchemaSourceProvider<T>) reg.provider();

        return Futures.catchingAsync(provider.getSource(sourceId), Throwable.class, input -> {
            LOG.debug("Failed to acquire source from {}", reg, input);
            if (it.hasNext()) {
                return fetchSource(sourceId, it);
            }
            throw new MissingSchemaSourceException(sourceId, "All available providers exhausted", input);
        }, MoreExecutors.directExecutor());
    }

    @Override
    public <T extends SourceRepresentation> ListenableFuture<T> getSchemaSource(final SourceIdentifier id,
            final Class<T> representation) {
        final ArrayList<SchemaSourceRegistration> sortedSchemaSourceRegistrations;

        synchronized (this) {
            final var srcs = sources.get(id);
            if (srcs == null) {
                return immediateFailedFluentFuture(new MissingSchemaSourceException(id,
                    "No providers registered for source " + id));
            }

            sortedSchemaSourceRegistrations = new ArrayList<>(srcs.get(representation));
        }

        // TODO, remove and make sources keep sorted multimap (e.g. ArrayListMultimap with SortedLists)
        sortedSchemaSourceRegistrations.sort(SchemaProviderCostComparator.INSTANCE);

        final var regs = sortedSchemaSourceRegistrations.iterator();
        if (!regs.hasNext()) {
            return immediateFailedFluentFuture(new MissingSchemaSourceException(id,
                "No providers for source " + id + " representation " + representation + " available"));
        }

        final ListenableFuture<T> fetchSourceFuture = fetchSource(id, regs);
        // Add callback to notify cache listeners about encountered schema
        Futures.addCallback(fetchSourceFuture, new FutureCallback<T>() {
            @Override
            public void onSuccess(final T result) {
                for (var listener : listeners) {
                    listener.getInstance().schemaSourceEncountered(result);
                }
            }

            @Override
            @SuppressWarnings("checkstyle:parameterName")
            public void onFailure(final Throwable t) {
                LOG.trace("Skipping notification for encountered source {}, fetching source failed", id, t);
            }
        }, MoreExecutors.directExecutor());

        return fetchSourceFuture;
    }

    private synchronized void addSource(final SchemaSourceRegistration reg) {
        final var source = reg.getInstance();
        sources.computeIfAbsent(source.getSourceIdentifier(), ignored -> ArrayListMultimap.create())
            .put(source.getRepresentation(), reg);

        final var reps = Collections.<PotentialSchemaSource<?>>singleton(source);
        for (var l : listeners) {
            l.getInstance().schemaSourceRegistered(reps);
        }
    }

    private synchronized void removeSource(final SchemaSourceRegistration reg) {
        final var source = reg.getInstance();
        final var m = sources.get(source.getSourceIdentifier());
        if (m != null) {
            m.remove(source.getRepresentation(), reg);

            for (var l : listeners) {
                l.getInstance().schemaSourceUnregistered(source);
            }

            if (m.isEmpty()) {
                sources.remove(source.getSourceIdentifier());
            }
        }
    }

    @Override
    public <T extends SourceRepresentation> Registration registerSchemaSource(
            final SchemaSourceProvider<? super T> provider, final PotentialSchemaSource<T> source) {
        final var ret = new SchemaSourceRegistration(source.cachedReference(), provider);
        addSource(ret);
        return ret;
    }

    @Override
    public Registration registerSchemaSourceListener(final SchemaSourceListener listener) {
        final var ret = new SchemaListenerRegistration(listener);

        synchronized (this) {
            final var col = new ArrayList<PotentialSchemaSource<?>>();
            for (var m : sources.values()) {
                for (var r : m.values()) {
                    col.add(r.getInstance());
                }
            }

            // Notify first, so translator-type listeners, who react by registering a source
            // do not cause infinite loop.
            listener.schemaSourceRegistered(col);
            listeners.add(ret);
        }
        return ret;
    }

    private final class SchemaListenerRegistration extends AbstractObjectRegistration<SchemaSourceListener> {
        SchemaListenerRegistration(final SchemaSourceListener listener) {
            super(listener);
        }

        @Override
        protected void removeRegistration() {
            listeners.remove(this);
        }
    }

    private final class SchemaSourceRegistration extends AbstractObjectRegistration<PotentialSchemaSource<?>> {
        private final @NonNull SchemaSourceProvider<?> provider;

        SchemaSourceRegistration(final PotentialSchemaSource<?> source, final SchemaSourceProvider<?> provider) {
            super(source);
            this.provider = requireNonNull(provider);
        }

        @NonNull SchemaSourceProvider<?> provider() {
            return provider;
        }

        @Override
        protected void removeRegistration() {
            removeSource(this);
        }
    }

    private static final class SchemaProviderCostComparator
            implements Comparator<SchemaSourceRegistration>, Serializable {
        static final SchemaProviderCostComparator INSTANCE = new SchemaProviderCostComparator();

        @java.io.Serial
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(final SchemaSourceRegistration o1, final SchemaSourceRegistration o2) {
            return o1.getInstance().getCost() - o2.getInstance().getCost();
        }

        @java.io.Serial
        private Object readResolve() {
            return INSTANCE;
        }
    }
}
