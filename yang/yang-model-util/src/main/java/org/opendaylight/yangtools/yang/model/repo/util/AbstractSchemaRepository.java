/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.model.repo.util;

import com.google.common.annotations.Beta;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureFallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
    private static final ExceptionMapper<SchemaSourceException> FETCH_MAPPER = ReflectiveExceptionMapper.create("Schema source fetch", SchemaSourceException.class);

    /*
     * Source identifier -> representation -> provider map. We usually are looking for
     * a specific representation of a source.
     */
    @GuardedBy("this")
    private final Map<SourceIdentifier, Multimap<Class<? extends SchemaSourceRepresentation>, AbstractSchemaSourceRegistration<?>>> sources = new HashMap<>();

    /*
     * Schema source listeners.
     */
    @GuardedBy("this")
    private final Collection<SchemaListenerRegistration> listeners = new ArrayList<>();

    private static final <T extends SchemaSourceRepresentation> CheckedFuture<T, SchemaSourceException> fetchSource(final SourceIdentifier id, final Iterator<AbstractSchemaSourceRegistration<?>> it) {
        final AbstractSchemaSourceRegistration<?> reg = it.next();

        @SuppressWarnings("unchecked")
        final CheckedFuture<? extends T, SchemaSourceException> f = ((SchemaSourceProvider<T>)reg.getProvider()).getSource(id);
        return Futures.makeChecked(Futures.withFallback(f, new FutureFallback<T>() {
            @Override
            public ListenableFuture<T> create(final Throwable t) throws SchemaSourceException {
                LOG.debug("Failed to acquire source from {}", reg, t);

                if (it.hasNext()) {
                    return fetchSource(id, it);
                }

                throw new MissingSchemaSourceException("All available providers exhausted");
            }
        }), FETCH_MAPPER);
    }

    @Override
    public <T extends SchemaSourceRepresentation> CheckedFuture<T, SchemaSourceException> getSchemaSource(final SourceIdentifier id, final Class<T> representation) {
        final Multimap<Class<? extends SchemaSourceRepresentation>, AbstractSchemaSourceRegistration<?>> srcs = sources.get(id);
        if (srcs == null) {
            return Futures.<T, SchemaSourceException>immediateFailedCheckedFuture(new MissingSchemaSourceException("No providers registered for source" + id));
        }

        final Iterator<AbstractSchemaSourceRegistration<?>> regs = srcs.get(representation).iterator();
        if (!regs.hasNext()) {
            return Futures.<T, SchemaSourceException>immediateFailedCheckedFuture(
                    new MissingSchemaSourceException("No providers for source " + id + " representation " + representation + " available"));
        }

        return fetchSource(id, regs);
    }

    private synchronized <T extends SchemaSourceRepresentation> void addSource(final PotentialSchemaSource<T> source, final AbstractSchemaSourceRegistration<T> reg) {
        Multimap<Class<? extends SchemaSourceRepresentation>, AbstractSchemaSourceRegistration<?>> m = sources.get(source.getSourceIdentifier());
        if (m == null) {
            m = HashMultimap.create();
            sources.put(source.getSourceIdentifier(), m);
        }

        m.put(source.getRepresentation(), reg);

        final Collection<PotentialSchemaSource<?>> reps = Collections.<PotentialSchemaSource<?>>singleton(source);
        for (SchemaListenerRegistration l : listeners) {
            l.getInstance().schemaSourceRegistered(reps);
        }
    }

    private synchronized <T extends SchemaSourceRepresentation> void removeSource(final PotentialSchemaSource<?> source, final SchemaSourceRegistration<?> reg) {
        final Multimap<Class<? extends SchemaSourceRepresentation>, AbstractSchemaSourceRegistration<?>> m = sources.get(source.getSourceIdentifier());
        if (m != null) {
            m.remove(source.getRepresentation(), reg);

            for (SchemaListenerRegistration l : listeners) {
                l.getInstance().schemaSourceUnregistered(source);
            }

            if (m.isEmpty()) {
                sources.remove(m);
            }
        }
    }

    @Override
    public <T extends SchemaSourceRepresentation> SchemaSourceRegistration<T> registerSchemaSource(final SchemaSourceProvider<? super T> provider, final PotentialSchemaSource<T> source) {
        final AbstractSchemaSourceRegistration<T> ret = new AbstractSchemaSourceRegistration<T>(provider, source) {
            @Override
            protected void removeRegistration() {
                removeSource(source, this);
            }
        };

        addSource(source, ret);
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
            for (Multimap<Class<? extends SchemaSourceRepresentation>, AbstractSchemaSourceRegistration<?>> m : sources.values()) {
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
}
