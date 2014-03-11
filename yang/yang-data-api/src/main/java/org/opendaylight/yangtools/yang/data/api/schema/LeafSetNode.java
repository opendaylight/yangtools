/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeWithValue;

import com.google.common.base.Optional;

public interface LeafSetNode<T> extends
    MixinNode, //
    DataContainerChild<NodeIdentifier, Iterable<LeafSetEntryNode<T>>>, //
    NormalizedNodeContainer<NodeIdentifier, NodeWithValue,LeafSetEntryNode<T>> {

    @Override
    public NodeIdentifier getIdentifier();

    @Override
    public Iterable<LeafSetEntryNode<T>> getValue();


    @Override
    public Optional<LeafSetEntryNode<T>> getChild(NodeWithValue child);

}
