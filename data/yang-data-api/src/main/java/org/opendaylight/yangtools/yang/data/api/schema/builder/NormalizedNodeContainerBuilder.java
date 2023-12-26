/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.builder;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

@Beta
// FIXME: YANGTOOLS-1259: eliminate this interface
public interface NormalizedNodeContainerBuilder<I extends PathArgument, K extends PathArgument,
        V extends NormalizedNode, R extends NormalizedNode> extends NormalizedNode.Builder {

    @NonNull NormalizedNodeContainerBuilder<I, K, V, R> withNodeIdentifier(I nodeIdentifier);

    @NonNull NormalizedNodeContainerBuilder<I, K, V, R> withValue(Collection<V> value);

    @NonNull NormalizedNodeContainerBuilder<I, K, V, R> addChild(V child);

    @NonNull NormalizedNodeContainerBuilder<I, K, V, R> removeChild(K key);

    @Override
    R build();
}
