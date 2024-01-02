/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.spi;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource.Costs;

/**
 * Abstract base class for cache-type SchemaSourceListeners. It needs to be registered with a
 * {@link SchemaSourceRegistry}, where it gets notifications from. It performs filtering and
 * {@link #offer(SourceRepresentation)}s conforming sources to the subclass.
 *
 * @param <T> Cached schema source type.
 */
public abstract class AbstractSchemaSourceCache<T extends SourceRepresentation>
        implements SchemaSourceListener, SchemaSourceProvider<T> {
    private final SchemaSourceRegistry consumer;
    private final Class<T> representation;
    private final Costs cost;

    protected AbstractSchemaSourceCache(final SchemaSourceRegistry consumer, final Class<T> representation,
            final Costs cost) {
        this.consumer = requireNonNull(consumer);
        this.representation = requireNonNull(representation);
        this.cost = requireNonNull(cost);
    }

    /**
     * Offer a schema source in requested representation for caching. Subclasses
     * need to implement this method to store the schema source. Once they have
     * determined to cache the source, they should call {@link #register(SourceIdentifier)}.
     *
     * @param source schema source
     */
    protected abstract void offer(T source);

    /**
     * Register the presence of a cached schema source with the consumer. Subclasses need to call this method once they
     * have cached a schema source representation, or when they have determined they have a schema source is available
     * -- like when a persistent cache reads its cache index.
     *
     * @param sourceId Source identifier
     * @return schema source registration, which the subclass needs to {@link Registration#close()} once it expunges the
     *         source from the cache.
     */
    protected final Registration register(final SourceIdentifier sourceId) {
        return consumer.registerSchemaSource(this, PotentialSchemaSource.create(sourceId, representation,
            cost.getValue()));
    }

    @Override
    public void schemaSourceEncountered(final SourceRepresentation source) {
        if (representation.isAssignableFrom(source.getType())) {
            offer(representation.cast(source));
        }
    }

    @Override
    public final void schemaSourceRegistered(final Iterable<PotentialSchemaSource<?>> sources) {
        // Not interesting
    }

    @Override
    public final void schemaSourceUnregistered(final PotentialSchemaSource<?> source) {
        // Not interesting
    }
}
