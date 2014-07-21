/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.model.repo.util;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactory;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceFilter;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistration;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceTransformationException;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceTransformer;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaTransformerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractSchemaRepository implements SchemaRepository, SchemaSourceRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSchemaRepository.class);
    private static final Comparator<SchemaTransformerRegistration> TRANSFORMER_COST_COMPARATOR = new Comparator<SchemaTransformerRegistration>() {
        @Override
        public int compare(final SchemaTransformerRegistration o1, final SchemaTransformerRegistration o2) {
            return o1.getInstance().getCost() - o2.getInstance().getCost();
        }
    };

    /*
     * Output-type -> transformer map. Our usage involves knowing the destination type,
     * so we have to work backwards and find a transformer chain which will get us
     * to that representation given our available sources.
     */
    private final Multimap<Class<? extends SchemaSourceRepresentation>, SchemaTransformerRegistration> transformers =
            HashMultimap.create();

    /*
     * Source identifier -> representation -> provider map. We usually are looking for
     * a specific representation a source.
     */
    private final Map<SourceIdentifier, Multimap<Class<?>, AbstractSchemaSourceRegistration>> sources = new HashMap<>();


    private static final <T extends SchemaSourceRepresentation> ListenableFuture<Optional<T>> fetchSource(final SourceIdentifier id, final Iterator<AbstractSchemaSourceRegistration> it) {
        if (!it.hasNext()) {
            return Futures.immediateFuture(Optional.<T>absent());
        }

        return Futures.transform(((SchemaSourceProvider<T>)it.next().getProvider()).getSource(id), new AsyncFunction<Optional<T>, Optional<T>>() {
            @Override
            public ListenableFuture<Optional<T>> apply(final Optional<T> input) throws Exception {
                if (input.isPresent()) {
                    return Futures.immediateFuture(input);
                } else {
                    return fetchSource(id, it);
                }
            }
        });
    }

    private <T extends SchemaSourceRepresentation> ListenableFuture<Optional<T>> transformSchemaSource(final SourceIdentifier id, final Class<T> representation) {
        final Multimap<Class<?>, AbstractSchemaSourceRegistration> srcs = sources.get(id);
        if (srcs.isEmpty()) {
            return Futures.immediateFailedFuture(new SchemaSourceTransformationException(
                    String.format("No providers producing a representation of %s registered", id)));
        }

        final Collection<SchemaTransformerRegistration> ts = transformers.get(representation);
        if (ts.isEmpty()) {
            return Futures.immediateFailedFuture(new SchemaSourceTransformationException(
                    String.format("No transformers producing representation %s registered", representation)));
        }

        // Build up the candidate list
        final List<SchemaTransformerRegistration> candidates = new ArrayList<>();
        for (SchemaTransformerRegistration tr : ts) {
            final SchemaSourceTransformer<?, ?> t = tr.getInstance();
            final Class<?> i = t.getInputRepresentation();
            if (srcs.containsKey(i)) {
                candidates.add(tr);
            } else {
                LOG.debug("Provider for {} in {} not found, skipping transfomer {}", id, i, t);
            }
        }

        if (candidates.isEmpty()) {
            return Futures.immediateFailedFuture(new SchemaSourceTransformationException(
                    String.format("No matching source/transformer pair for source %s representation %s found", id, representation)));
        }

        Collections.sort(candidates, TRANSFORMER_COST_COMPARATOR);
        // return transform(candidates.iterator(), id);
        return null;
    }

    /**
     * Obtain a SchemaSource is selected representation
     */
    protected <T extends SchemaSourceRepresentation> ListenableFuture<Optional<T>> getSchemaSource(final SourceIdentifier id, final Class<T> representation) {
        final Multimap<Class<?>, AbstractSchemaSourceRegistration> srcs = sources.get(id);
        if (srcs == null) {
            LOG.debug("No providers registered for source {}", id);
            return Futures.immediateFuture(Optional.<T>absent());
        }

        final Collection<AbstractSchemaSourceRegistration> candidates = srcs.get(representation);
        return Futures.transform(AbstractSchemaRepository.<T>fetchSource(id, candidates.iterator()), new AsyncFunction<Optional<T>, Optional<T>>() {
            @Override
            public ListenableFuture<Optional<T>> apply(final Optional<T> input) throws Exception {
                if (input.isPresent()) {
                    return Futures.immediateFuture(input);
                }

                return transformSchemaSource(id, representation);
            }
        });
    }

    @Override
    public SchemaContextFactory createSchemaContextFactory(final SchemaSourceFilter filter) {
        // TODO Auto-generated method stub
        return null;
    }

    private void addSource(final SourceIdentifier id, final Class<?> rep, final AbstractSchemaSourceRegistration reg) {
        Multimap<Class<?>, AbstractSchemaSourceRegistration> m = sources.get(id);
        if (m == null) {
            m = HashMultimap.create();
            sources.put(id, m);
        }

        m.put(rep, reg);
    }

    private void removeSource(final SourceIdentifier id, final Class<?> rep, final SchemaSourceRegistration reg) {
        final Multimap<Class<?>, AbstractSchemaSourceRegistration> m = sources.get(id);
        if (m != null) {
            m.remove(rep, reg);
            if (m.isEmpty()) {
                sources.remove(m);
            }
        }
    }

    @Override
    public <T extends SchemaSourceRepresentation> SchemaSourceRegistration registerSchemaSource(
            final SourceIdentifier identifier, final SchemaSourceProvider<? super T> provider, final Class<T> representation) {
        final AbstractSchemaSourceRegistration ret = new AbstractSchemaSourceRegistration(identifier, provider) {
            @Override
            protected void removeRegistration() {
                removeSource(identifier, representation, this);
            }
        };

        addSource(identifier, representation, ret);
        return ret;
    }

    @Override
    public SchemaTransformerRegistration registerSchemaSourceTransformer(final SchemaSourceTransformer<?, ?> transformer) {
        final SchemaTransformerRegistration ret = new AbstractSchemaTransformerRegistration(transformer) {
            @Override
            protected void removeRegistration() {
                transformers.remove(transformer.getOutputRepresentation(), this);
            }
        };

        transformers.put(transformer.getOutputRepresentation(), ret);
        return ret;
    }
}
