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
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * An asynchronous factory for building {@link SchemaContext} instances based on a specification of what
 * {@link SourceIdentifier}s are required and dynamic recursive resolution.
 */
@Beta
public interface SchemaContextFactory {
    /**
     * Create a new schema context containing specified sources, pulling in any dependencies they may have.
     *
     * @param requiredSources a collection of sources which are required to be present
     * @return A checked future, which will produce a schema context, or fail with an explanation why the creation
     *         of the schema context failed.
     */
    @NonNull ListenableFuture<SchemaContext> createSchemaContext(@NonNull Collection<SourceIdentifier> requiredSources);
}
