/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import javax.annotation.Nonnull;

import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Interface exposed by repository implementations. A schema repository is a logically
 * centralized place for model storage and creation of {@link SchemaContext} instances.
 */
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
}
