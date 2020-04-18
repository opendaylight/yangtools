/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Arrays;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * An asynchronous factory for building {@link SchemaContext} instances based on a specification of what
 * {@link SourceIdentifier}s are required and dynamic recursive resolution.
 */
@Beta
// FIXME: 6.0.0: evaluate if we still need to extend SchemaContext here
public interface EffectiveModelContextFactory extends SchemaContextFactory {
    /**
     * Create a new schema context containing specified sources, pulling in any dependencies they may have.
     *
     * @param requiredSources a collection of sources which are required to be present
     * @return A checked future, which will produce a schema context, or fail with an explanation why the creation
     *         of the schema context failed.
     */
    @NonNull ListenableFuture<EffectiveModelContext> createEffectiveModelContext(
            @NonNull Collection<SourceIdentifier> requiredSources);

    default @NonNull ListenableFuture<EffectiveModelContext> createEffectiveModelContext(
            final SourceIdentifier... requiredSources) {
        return createEffectiveModelContext(Arrays.asList(requiredSources));
    }

    @Override
    @Deprecated
    default ListenableFuture<SchemaContext> createSchemaContext(
            final Collection<SourceIdentifier> requiredSources) {
        return Futures.transform(createEffectiveModelContext(requiredSources), ctx -> ctx,
            MoreExecutors.directExecutor());
    }
}
