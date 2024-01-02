/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.repo;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;
import org.opendaylight.yangtools.yang.model.spi.source.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.spi.source.SourceIdentifier;

class SettableSchemaProvider<T extends SchemaSourceRepresentation> implements SchemaSourceProvider<T> {

    private final SettableFuture<T> future = SettableFuture.create();
    private final T schemaSourceRepresentation;
    private final PotentialSchemaSource<T> potentialSchemaSource;

    SettableSchemaProvider(final T schemaSourceRepresentation, final SourceIdentifier sourceIdentifier,
            final Class<T> representation, final int cost) {
        this.schemaSourceRepresentation = schemaSourceRepresentation;
        potentialSchemaSource = PotentialSchemaSource.create(sourceIdentifier, representation, cost);
    }

    public static <T extends SchemaSourceRepresentation> SettableSchemaProvider<T> createRemote(
            final T schemaSourceRepresentation, final Class<T> representation) {
        return new SettableSchemaProvider<>(schemaSourceRepresentation, schemaSourceRepresentation.sourceId(),
                representation, PotentialSchemaSource.Costs.REMOTE_IO.getValue());
    }

    public static <T extends SchemaSourceRepresentation> SettableSchemaProvider<T> createImmediate(
            final T schemaSourceRepresentation, final Class<T> representation) {
        return new SettableSchemaProvider<>(schemaSourceRepresentation, schemaSourceRepresentation.sourceId(),
                representation, PotentialSchemaSource.Costs.IMMEDIATE.getValue());
    }

    @Override
    public ListenableFuture<T> getSource(final SourceIdentifier sourceIdentifier) {
        return future;
    }

    public T getSchemaSourceRepresentation() {
        return schemaSourceRepresentation;
    }

    public SourceIdentifier getId() {
        return schemaSourceRepresentation.sourceId();
    }

    public void setResult() {
        future.set(schemaSourceRepresentation);
    }

    public void setException(final Throwable ex) {
        future.setException(ex);
    }

    public void register(final SchemaSourceRegistry repo) {
        repo.registerSchemaSource(this, potentialSchemaSource);
    }
}
