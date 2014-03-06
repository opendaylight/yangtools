/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import java.util.Map;

import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;

import com.google.common.base.Optional;

public abstract class AbstractDataContainerNode<K extends InstanceIdentifier.PathArgument> implements DataContainerNode<K> {
    protected Map<InstanceIdentifier.PathArgument, DataContainerChild<?, ?>> children;

    public AbstractDataContainerNode(Map<InstanceIdentifier.PathArgument, DataContainerChild<?, ?>> children) {
        this.children = children;
    }


    @Override
    public CompositeNode getParent() {
        throw new UnsupportedOperationException("Deprecated");
    }

    @Override
    public Iterable<DataContainerChild<?, ?>> getValue() {
        return children.values();
    }

    @Override
    public Iterable<DataContainerChild<? extends InstanceIdentifier.PathArgument, ?>> setValue(Iterable<DataContainerChild<? extends InstanceIdentifier.PathArgument, ?>> value) {
        throw new UnsupportedOperationException("Immutable");
    }

    @Override
    public Optional<DataContainerChild<?, ?>> getChild(InstanceIdentifier.PathArgument child) {
        return Optional.<DataContainerChild<?, ?>>fromNullable(children.get(child));
    }
}
