/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.ImmutableLeafSetEntryNode;

public final class ImmutableLeafSetEntryNodeBuilder<T>
        extends AbstractImmutableNormalizedNodeBuilder<NodeWithValue, T, LeafSetEntryNode<T>> {

    public static <T> @NonNull ImmutableLeafSetEntryNodeBuilder<T> create() {
        return new ImmutableLeafSetEntryNodeBuilder<>();
    }

    @Override
    public LeafSetEntryNode<T> build() {
        return new ImmutableLeafSetEntryNode<>(getNodeIdentifier(), getValue());
    }
}
