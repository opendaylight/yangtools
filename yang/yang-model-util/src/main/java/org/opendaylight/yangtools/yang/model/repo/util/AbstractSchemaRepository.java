/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.util;

import com.google.common.annotations.Beta;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.FutureFallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.yangtools.util.concurrent.ExceptionMapper;
import org.opendaylight.yangtools.util.concurrent.ReflectiveExceptionMapper;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceFilter;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaListenerRegistration;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceListener;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistration;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for {@link SchemaRepository} implementations. It handles registration
 * and lookup of schema sources, subclasses need only to provide their own
 * {@link #createSchemaContextFactory(SchemaSourceFilter)} implementation.
 */
@Beta
public abstract class AbstractSchemaRepository implements SchemaRepository, SchemaSourceRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSchemaRepository.class);
    private static final ExceptionMapper<SchemaSourceException> FETCH_MAPPER = ReflectiveExceptionMapper.create(
            "Schema source fetch", SchemaSourceException.class);

    /*
     * Source identifier -> representation -> provider map. We usually are looking for
     * a specific representation of a source.
     */
    @GuardedBy("this")
    private final Map<SourceIdentifier, ListMultimap<Class<? extends SchemaSourceRepresentation>,
            AbstractSchemaSourceRegistration<?>>> sources = new HashMap<>();

    /*
     * Schema source listeners.
     */
    @GuardedBy("this")
    private final Collection<SchemaListenerRegistration> listeners = new ArrayList<>();

    private static <T extends SchemaSourceRepresentation> CheckedFuture<T, SchemaSourceException> fetchSource(
            final SourceIdentifier id, final Iterator<AbstractSchemaSourceRegistration<?>> it) {
        final AbstractSchemaSourceRegistration<?> reg = it.next();

        @SuppressWarnings("unchecked")
        final CheckedFuture<? extends T, SchemaSourceException> f =
            ((SchemaSourceProvider<T>)reg.getProvider()).getSource(id);

        return Futures.makeChecked(Futures.withFallback(f, new FutureFallback<T>() {
            @Override
            public ListenableFuture<T> create(@Nonnull final Throwable cause) throws SchemaSourceException {
                LOG.debug("Failed to acquire source from {}", reg, cause);

                if (it.hasNext()) {
                    return fetchSource(id, it);
                }

                throw new MissingSchemaSourceException("All available providers exhausted", id, cause);
            }
        }), FETCH_MAPPER);
    }

    @Override
    public <T extends SchemaSourceRepresentation> CheckedFuture<T, SchemaSourceException> getSchemaSource(
            @Nonnull final SourceIdentifier id, @Nonnull final Class<T> representation) {
        final ArrayList<AbstractSchemaSourceRegistration<?>> sortedSchemaSourceRegistrations;

        synchronized (this) {
            final ListMultimap<Class<? extends SchemaSourceRepresentation>, AbstractSchemaSourceRegistration<?>> srcs =
                sources.get(id);
            if (srcs == null) {
                return Futures.immediateFailedCheckedFuture(new MissingSchemaSourceException(
                            "No providers registered for source" + id, id));
            }

            sortedSchemaSourceRegistrations = Lists.newArrayList(srcs.get(representation));
        }

        // TODO, remove and make sources keep sorted multimap (e.g. ArrayListMultimap with SortedLists)
        Collections.sort(sortedSchemaSourceRegistrations, SchemaProviderCostComparator.INSTANCE);

        final Iterator<AbstractSchemaSourceRegistration<?>> regs = sortedSchemaSourceRegistrations.iterator();
        if (!regs.hasNext()) {
            return Futures.immediateFailedCheckedFuture(new MissingSchemaSourceException(
                        "No providers for source " + id + " representation " + representation + " available", id));
        }

        CheckedFuture<T, SchemaSourceException> fetchSourceFuture = fetchSource(id, regs);
        // Add callback to notify cache listeners about encountered schema
        Futures.addCallback(fetchSourceFuture, new FutureCallback<T>() {
            @Override
            public void onSuccess(final T result) {
                for (final SchemaListenerRegistration listener : listeners) {
                    listener.getInstance().schemaSourceEncountered(result);
                }
            }

            @Override
            @SuppressWarnings("checkstyle:parameterName")
            public void onFailure(@Nonnull final Throwable t) {
                LOG.trace("Skipping notification for encountered source {}, fetching source failed", id, t);
            }
        });

        return fetchSourceFuture;
    }

    private synchronized <T extends SchemaSourceRepresentation> void addSource(final PotentialSchemaSource<T> source,
            final AbstractSchemaSourceRegistration<T> reg) {
        ListMultimap<Class<? extends SchemaSourceRepresentation>, AbstractSchemaSourceRegistration<?>> map =
            sources.get(source.getSourceIdentifier());
        if (map == null) {
            map = ArrayListMultimap.create();
            sources.put(source.getSourceIdentifier(), map);
        }

        map.put(source.getRepresentation(), reg);

        final Collection<PotentialSchemaSource<?>> reps = Collections.singleton(source);
        for (SchemaListenerRegistration l : listeners) {
            l.getInstance().schemaSourceRegistered(reps);
        }
    }

    private synchronized <T extends SchemaSourceRepresentation> void removeSource(final PotentialSchemaSource<?> source,
            final SchemaSourceRegistration<?> reg) {
        final Multimap<Class<? extends SchemaSourceRepresentation>, AbstractSchemaSourceRegistration<?>> m =
            sources.get(source.getSourceIdentifier());
        if (m != null) {
            m.remove(source.getRepresentation(), reg);

            for (SchemaListenerRegistration l : listeners) {
                l.getInstance().schemaSourceUnregistered(source);
            }

            if (m.isEmpty()) {
                sources.remove(source.getSourceIdentifier());
            }
        }
    }

    @Override
    public <T extends SchemaSourceRepresentation> SchemaSourceRegistration<T> registerSchemaSource(
            final SchemaSourceProvider<? super T> provider, final PotentialSchemaSource<T> source) {
        final PotentialSchemaSource<T> src = source.cachedReference();

        final AbstractSchemaSourceRegistration<T> ret = new AbstractSchemaSourceRegistration<T>(provider, src) {
            @Override
            protected void removeRegistration() {
                removeSource(src, this);
            }
        };

        addSource(src, ret);
        return ret;
    }

    @Override
    public SchemaListenerRegistration registerSchemaSourceListener(final SchemaSourceListener listener) {
        final SchemaListenerRegistration ret = new AbstractSchemaListenerRegistration(listener) {
            @Override
            protected void removeRegistration() {
                listeners.remove(this);
            }
        };

        synchronized (this) {
            final Collection<PotentialSchemaSource<?>> col = new ArrayList<>();
            for (Multimap<Class<? extends SchemaSourceRepresentation>, AbstractSchemaSourceRegistration<?>> m
                    : sources.values()) {
                for (AbstractSchemaSourceRegistration<?> r : m.values()) {
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

    private static class SchemaProviderCostComparator implements Comparator<AbstractSchemaSourceRegistration<?>> {
        public static final SchemaProviderCostComparator INSTANCE = new SchemaProviderCostComparator();

        @Override
        public int compare(final AbstractSchemaSourceRegistration<?> o1, final AbstractSchemaSourceRegistration<?> o2) {
            return o1.getInstance().getCost() - o2.getInstance().getCost();
        }
    }
}
