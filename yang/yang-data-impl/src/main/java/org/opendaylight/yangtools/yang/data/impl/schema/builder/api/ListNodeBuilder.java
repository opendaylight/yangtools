/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.api;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;

import java.util.List;
import java.util.Map;

public interface ListNodeBuilder<T, V>
        extends CollectionNodeBuilder<V, LeafSetNode<T>> {

    @Override
    ListNodeBuilder<T, V> withNodeIdentifier(InstanceIdentifier.NodeIdentifier nodeIdentifier);

    @Override
    ListNodeBuilder<T, V> withValue(List<V> value);

    @Override
    ListNodeBuilder<T, V> withChild(V child);

    ListNodeBuilder<T, V> withChildValue(T child);

    ListNodeBuilder<T, LeafSetEntryNode<T>> withChildValue(T value, Map<QName, String> attributes);
}
