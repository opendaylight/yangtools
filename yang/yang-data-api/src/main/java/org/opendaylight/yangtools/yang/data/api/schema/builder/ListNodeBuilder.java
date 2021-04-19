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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;

@Beta
public interface ListNodeBuilder<T, V extends LeafSetNode<T>> extends CollectionNodeBuilder<LeafSetEntryNode<T>, V> {
    @Override
    ListNodeBuilder<T, V> withNodeIdentifier(NodeIdentifier nodeIdentifier);

    @Override
    ListNodeBuilder<T, V> withValue(Collection<LeafSetEntryNode<T>> value);

    @Override
    ListNodeBuilder<T, V> withChild(LeafSetEntryNode<T> child);

    @Override
    ListNodeBuilder<T, V> withoutChild(PathArgument key);

    ListNodeBuilder<T, V> withChildValue(T child);
}
