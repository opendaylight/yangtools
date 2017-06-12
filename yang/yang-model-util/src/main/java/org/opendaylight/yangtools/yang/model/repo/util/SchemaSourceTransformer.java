/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.util;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.util.concurrent.ExceptionMapper;
import org.opendaylight.yangtools.util.concurrent.ReflectiveExceptionMapper;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceListener;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistration;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;

public class SchemaSourceTransformer<S extends SchemaSourceRepresentation, D extends SchemaSourceRepresentation>
        implements SchemaSourceListener, SchemaSourceProvider<D> {
    private static final ExceptionMapper<SchemaSourceException> MAPPER = ReflectiveExceptionMapper.create(
            "Source transformation", SchemaSourceException.class);

    @FunctionalInterface
    public interface Transformation<S extends SchemaSourceRepresentation, D extends SchemaSourceRepresentation>
            extends AsyncFunction<S, D> {
        @Override
        CheckedFuture<D, SchemaSourceException> apply(@Nonnull final S input) throws Exception;
    }

    private final Map<PotentialSchemaSource<?>, RefcountedRegistration> sources = new HashMap<>();
    private final SchemaSourceRegistry consumer;
    private final SchemaRepository provider;
    private final AsyncFunction<S, D> function;
    private final Class<S> srcClass;
    private final Class<D> dstClass;

    public SchemaSourceTransformer(final SchemaRepository provider, final Class<S> srcClass,
            final SchemaSourceRegistry consumer, final Class<D> dstClass, final AsyncFunction<S, D> function) {
        this.provider = Preconditions.checkNotNull(provider);
        this.consumer = Preconditions.checkNotNull(consumer);
        this.function = Preconditions.checkNotNull(function);
        this.srcClass = Preconditions.checkNotNull(srcClass);
        this.dstClass = Preconditions.checkNotNull(dstClass);
    }

    @Override
    public CheckedFuture<D, SchemaSourceException> getSource(final SourceIdentifier sourceIdentifier) {
        final CheckedFuture<S, SchemaSourceException> f = provider.getSchemaSource(sourceIdentifier, srcClass);
        return Futures.makeChecked(Futures.transform(f, function), MAPPER);
    }

    @Override
    public final void schemaSourceEncountered(final SchemaSourceRepresentation source) {
        // Not interesting
    }

    @Override
    public final void schemaSourceRegistered(final Iterable<PotentialSchemaSource<?>> sources) {
        for (PotentialSchemaSource<?> src : sources) {
            final Class<?> rep = src.getRepresentation();
            if (srcClass.isAssignableFrom(rep) && dstClass != rep) {
                registerSource(src);
            }
        }
    }

    @Override
    public final void schemaSourceUnregistered(final PotentialSchemaSource<?> source) {
        final Class<?> rep = source.getRepresentation();
        if (srcClass.isAssignableFrom(rep) && dstClass != rep) {
            unregisterSource(source);
        }
    }

    private void registerSource(final PotentialSchemaSource<?> src) {
        RefcountedRegistration reg = sources.get(src);
        if (reg != null) {
            reg.incRef();
            return;
        }

        final PotentialSchemaSource<D> newSrc = PotentialSchemaSource.create(src.getSourceIdentifier(), dstClass,
                src.getCost() + PotentialSchemaSource.Costs.COMPUTATION.getValue());

        final SchemaSourceRegistration<D> r = consumer.registerSchemaSource(this, newSrc);
        sources.put(src, new RefcountedRegistration(r));
    }

    private void unregisterSource(final PotentialSchemaSource<?> src) {
        final RefcountedRegistration reg = sources.get(src);
        if (reg != null && reg.decRef()) {
            sources.remove(src);
        }
    }
}
