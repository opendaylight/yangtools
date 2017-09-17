/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.repo;

import com.google.common.base.Function;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;

class SettableSchemaProvider<T extends SchemaSourceRepresentation> implements SchemaSourceProvider<T> {

    private final SettableFuture<T> future = SettableFuture.create();
    private final T schemaSourceRepresentation;
    private final PotentialSchemaSource<T> potentialSchemaSource;

    SettableSchemaProvider(final T schemaSourceRepresentation, final SourceIdentifier sourceIdentifier,
            final Class<T> representation, final int cost) {
        this.schemaSourceRepresentation = schemaSourceRepresentation;
        this.potentialSchemaSource = PotentialSchemaSource.create(sourceIdentifier, representation, cost);
    }

    public static <T extends SchemaSourceRepresentation> SettableSchemaProvider<T> createRemote(
            final T schemaSourceRepresentation, final Class<T> representation) {
        return new SettableSchemaProvider<>(schemaSourceRepresentation, schemaSourceRepresentation.getIdentifier(),
                representation, PotentialSchemaSource.Costs.REMOTE_IO.getValue());
    }

    public static <T extends SchemaSourceRepresentation> SettableSchemaProvider<T> createImmediate(
            final T schemaSourceRepresentation, final Class<T> representation) {
        return new SettableSchemaProvider<>(schemaSourceRepresentation, schemaSourceRepresentation.getIdentifier(),
                representation, PotentialSchemaSource.Costs.IMMEDIATE.getValue());
    }

    @Override
    public CheckedFuture<T, SchemaSourceException> getSource(final SourceIdentifier sourceIdentifier) {
        return Futures.makeChecked(future, new Function<Exception, SchemaSourceException>() {
            @Nullable
            @Override
            public SchemaSourceException apply(@Nullable final Exception input) {
                return new SchemaSourceException("Failed", input);
            }
        });
    }

    public T getSchemaSourceRepresentation() {
        return schemaSourceRepresentation;
    }

    public SourceIdentifier getId() {
        return schemaSourceRepresentation.getIdentifier();
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
