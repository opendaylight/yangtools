/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * A {@link SchemaSourceFilter} which accepts any schema source it is presented with.
 */
public final class AcceptingSchemaSourceFilter implements SchemaSourceFilter {
    private static final AcceptingSchemaSourceFilter INSTANCE = new AcceptingSchemaSourceFilter();

    private final Iterable<Class<? extends SchemaSourceRepresentation>> representations;

    private AcceptingSchemaSourceFilter() {
        final Builder<Class<? extends SchemaSourceRepresentation>> b = ImmutableList.builder();
        b.add(SchemaSourceRepresentation.class);
        representations = b.build();
    }

    /**
     * Return the singleton instance of this filter.
     *
     * @return Singleton shared instance.
     */
    public static final AcceptingSchemaSourceFilter getSingletonInstance() {
        return INSTANCE;
    }

    @Override
    public Iterable<Class<? extends SchemaSourceRepresentation>> supportedRepresentations() {
        return representations;
    }

    @Override
    public ListenableFuture<Boolean> apply(final SchemaSourceRepresentation schemaSource) {
        return Futures.immediateFuture(Boolean.TRUE);
    }
}
