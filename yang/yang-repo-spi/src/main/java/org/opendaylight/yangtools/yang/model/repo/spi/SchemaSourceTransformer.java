/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository;
import org.opendaylight.yangtools.yang.model.spi.source.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.spi.source.SourceIdentifier;

public class SchemaSourceTransformer<S extends SchemaSourceRepresentation, D extends SchemaSourceRepresentation>
        implements SchemaSourceListener, SchemaSourceProvider<D> {
    @FunctionalInterface
    public interface Transformation<S extends SchemaSourceRepresentation, D extends SchemaSourceRepresentation>
            extends AsyncFunction<S, D> {
        @Override
        ListenableFuture<D> apply(S input) throws Exception;
    }

    private final Map<PotentialSchemaSource<?>, RefcountedRegistration> availableSources = new HashMap<>();
    private final SchemaSourceRegistry consumer;
    private final SchemaRepository provider;
    private final AsyncFunction<S, D> function;
    private final Class<S> srcClass;
    private final Class<D> dstClass;

    public SchemaSourceTransformer(final SchemaRepository provider, final Class<S> srcClass,
            final SchemaSourceRegistry consumer, final Class<D> dstClass, final AsyncFunction<S, D> function) {
        this.provider = requireNonNull(provider);
        this.consumer = requireNonNull(consumer);
        this.function = requireNonNull(function);
        this.srcClass = requireNonNull(srcClass);
        this.dstClass = requireNonNull(dstClass);
    }

    @Override
    public final ListenableFuture<D> getSource(final SourceIdentifier sourceIdentifier) {
        return Futures.transformAsync(provider.getSchemaSource(sourceIdentifier, srcClass), function,
            MoreExecutors.directExecutor());
    }

    @Override
    public final void schemaSourceEncountered(final SchemaSourceRepresentation source) {
        // Not interesting
    }

    @Override
    public final void schemaSourceRegistered(final Iterable<PotentialSchemaSource<?>> sources) {
        for (var src : sources) {
            final var rep = src.getRepresentation();
            if (srcClass.isAssignableFrom(rep) && dstClass != rep) {
                registerSource(src);
            }
        }
    }

    @Override
    public final void schemaSourceUnregistered(final PotentialSchemaSource<?> source) {
        final var rep = source.getRepresentation();
        if (srcClass.isAssignableFrom(rep) && dstClass != rep) {
            unregisterSource(source);
        }
    }

    private void registerSource(final PotentialSchemaSource<?> src) {
        final var reg = availableSources.get(src);
        if (reg != null) {
            reg.incRef();
            return;
        }

        final var newSrc = PotentialSchemaSource.create(src.getSourceIdentifier(), dstClass,
            src.getCost() + PotentialSchemaSource.Costs.COMPUTATION.getValue());
        availableSources.put(src, new RefcountedRegistration(consumer.registerSchemaSource(this, newSrc)));
    }

    private void unregisterSource(final PotentialSchemaSource<?> src) {
        final RefcountedRegistration reg = availableSources.get(src);
        if (reg != null && reg.decRef()) {
            availableSources.remove(src);
        }
    }
}
