/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.api;

import java.util.List;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public interface CollectionNodeBuilder<V extends NormalizedNode<?, ?>, R extends NormalizedNode<YangInstanceIdentifier.NodeIdentifier, ?>>
        extends NormalizedNodeContainerBuilder<NodeIdentifier,PathArgument, V, R> {

    @Override
    CollectionNodeBuilder<V, R> withValue(List<V> value);

    @Override
    CollectionNodeBuilder<V, R> withNodeIdentifier(YangInstanceIdentifier.NodeIdentifier nodeIdentifier);

    CollectionNodeBuilder<V, R> withChild(V child);
    CollectionNodeBuilder<V, R> withoutChild(YangInstanceIdentifier.PathArgument key);
}
