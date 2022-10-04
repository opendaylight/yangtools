/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;

public final class ImmutableLeafSetEntryNode<T>
        extends AbstractImmutableNormalizedSimpleValueNode<NodeWithValue<T>, LeafSetEntryNode<?>, T>
        implements LeafSetEntryNode<T> {
    public ImmutableLeafSetEntryNode(final NodeWithValue<T> nodeIdentifier, final T value) {
        super(nodeIdentifier, value);
        checkArgument(Objects.deepEquals(nodeIdentifier.getValue(), value),
                "Node identifier contains different value: %s than value itself: %s", nodeIdentifier, value);
    }

    @Override
    protected Class<LeafSetEntryNode<?>> implementedType() {
        return (Class) LeafSetEntryNode.class;
    }
}