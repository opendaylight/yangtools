/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.api;

import java.util.Collection;
import org.opendaylight.yangtools.concepts.ItemOrder.Ordered;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserLeafSetNode;

public interface OrderedListNodeBuilder<T, V> extends ListNodeBuilder<Ordered, T, UserLeafSetNode<T>> {
    @Override
    OrderedListNodeBuilder<T, V> withNodeIdentifier(NodeIdentifier nodeIdentifier);

    @Override
    OrderedListNodeBuilder<T, V> withValue(Collection<LeafSetEntryNode<T>> value);

    @Override
    OrderedListNodeBuilder<T, V> withChild(LeafSetEntryNode<T> child);

    @Override
    OrderedListNodeBuilder<T, V> withoutChild(PathArgument key);

    @Override
    OrderedListNodeBuilder<T, V> withChildValue(T child);
}
