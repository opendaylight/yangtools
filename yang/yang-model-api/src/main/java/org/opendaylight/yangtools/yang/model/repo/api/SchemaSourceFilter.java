/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collections;

/**
 * A filter of schema sources. This is used to restrict which sources representation
 * instances are allowed to participate in construction of a schema context. This
 * allows, for example, to create an non-shared island, or require the sources to
 * be certified before use.
 */
@Beta
public interface SchemaSourceFilter {
    /**
     * A {@link SchemaSourceFilter} which accepts any schema source it is presented with.
     */
    public static final SchemaSourceFilter ALWAYS_ACCEPT = new SchemaSourceFilter() {
        private final Iterable<Class<? extends SchemaSourceRepresentation>> REPRESENTATIONS =
                Collections.<Class<? extends SchemaSourceRepresentation>>singletonList(SchemaSourceRepresentation.class);

        @Override
        public Iterable<Class<? extends SchemaSourceRepresentation>> supportedRepresentations() {
            return REPRESENTATIONS;
        }

        @Override
        public ListenableFuture<Boolean> apply(final SchemaSourceRepresentation schemaSource) {
            return Futures.immediateFuture(Boolean.TRUE);
        }
    };

    /**
     * Get the representations this filter supports. A schema source is translated
     * into one of these representations before it is presented for filtering.
     *
     * @return Set of supported representations.
     */
    Iterable<Class<? extends SchemaSourceRepresentation>> supportedRepresentations();

    /**
     * Check if a particular schema source is acceptable to the filter. The process
     * of checking may be asynchronous, but at some point it needs to produce an
     * affirmative or negative answer before the schema context construction can
     * proceed.
     *
     * @param schemaSource Schema source to be filtered
     * @return Promise of a filtering decision. The result should be {@link Boolean#TRUE}
     *         if the source is acceptable.
     */
    ListenableFuture<Boolean> apply(SchemaSourceRepresentation schemaSource);
}
