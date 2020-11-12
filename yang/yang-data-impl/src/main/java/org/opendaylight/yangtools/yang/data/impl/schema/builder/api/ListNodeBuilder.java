/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.api;

import java.util.Collection;
import org.opendaylight.yangtools.concepts.ItemOrder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;

public interface ListNodeBuilder<O extends ItemOrder<O>, T, V extends LeafSetNode<O, T>>
        extends CollectionNodeBuilder<LeafSetEntryNode<T>, V> {
    @Override
    ListNodeBuilder<O, T, V> withNodeIdentifier(NodeIdentifier nodeIdentifier);

    @Override
    ListNodeBuilder<O, T, V> withValue(Collection<LeafSetEntryNode<T>> value);

    @Override
    ListNodeBuilder<O, T, V> withChild(LeafSetEntryNode<T> child);

    @Override
    ListNodeBuilder<O, T, V> withoutChild(PathArgument key);

    ListNodeBuilder<O, T, V> withChildValue(T child);
}
