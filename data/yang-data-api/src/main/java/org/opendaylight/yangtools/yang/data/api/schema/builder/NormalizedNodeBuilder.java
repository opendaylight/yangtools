/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.builder;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedData;

@Beta
public interface NormalizedNodeBuilder<I extends PathArgument, V, R extends NormalizedData> extends Mutable {

    @NonNull NormalizedNodeBuilder<I, V, R> withValue(V value);

    @NonNull NormalizedNodeBuilder<I, V, R> withNodeIdentifier(I nodeIdentifier);

    /**
     * Return an implementation of the {@link R} NormalizedNode contract.
     *
     * @return A built NormalizedNode
     * @throws IllegalStateException if this builder does not have sufficient state
     */
    @NonNull R build();
}
