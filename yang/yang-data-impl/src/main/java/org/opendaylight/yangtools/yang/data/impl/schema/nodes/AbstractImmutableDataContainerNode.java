/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import java.util.Map;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;

import com.google.common.base.Optional;

public abstract class AbstractImmutableDataContainerNode<K extends InstanceIdentifier.PathArgument>
        extends AbstractImmutableNormalizedNode<K, Iterable<DataContainerChild<? extends InstanceIdentifier.PathArgument, ?>>>
        implements DataContainerNode<K> {

    protected Map<InstanceIdentifier.PathArgument, DataContainerChild<? extends InstanceIdentifier.PathArgument, ?>> children;

    public AbstractImmutableDataContainerNode(Map<InstanceIdentifier.PathArgument, DataContainerChild<? extends InstanceIdentifier.PathArgument, ?>> children, K nodeIdentifier) {
        super(nodeIdentifier, children.values());
        this.children = children;
    }

    @Override
    public Optional<DataContainerChild<? extends InstanceIdentifier.PathArgument, ?>> getChild(InstanceIdentifier.PathArgument child) {
        return Optional.<DataContainerChild<? extends InstanceIdentifier.PathArgument, ?>>fromNullable(children.get(child));
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer(getClass().getSimpleName());
        sb.append("{nodeIdentifier=").append(nodeIdentifier);
        sb.append(", children=").append(children);
        sb.append('}');
        return sb.toString();
    }
}
