/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.ListenableFuture;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Interface exposed by repository implementations. A schema repository is a logically
 * centralized place for model storage and creation of {@link SchemaContext} instances.
 */
@Beta
public interface SchemaRepository {
    /**
     * Instantiate a new {@link SchemaContextFactory}, which will filter available schema
     * sources using the provided filter.
     *
     * @param filter Filter which acts as the gating function before a schema source is
     *               considered by the factory for inclusion in the {@link SchemaContext}
     *               it produces.
     * @return A new schema context factory.
     */
    SchemaContextFactory createSchemaContextFactory(@Nonnull SchemaSourceFilter filter);

    <T extends SchemaSourceRepresentation> ListenableFuture<T> getSchemaSource(@Nonnull SourceIdentifier id,
            @Nonnull Class<T> represetation);
}
