/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import com.google.common.collect.Maps;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;

import java.util.List;
import java.util.Map;

abstract class AbstractImmutableDataContainerNodeBuilder<I extends InstanceIdentifier.PathArgument, R extends DataContainerNode<I>>
        implements DataContainerNodeBuilder<I, R> {

    protected final Map<InstanceIdentifier.PathArgument, DataContainerChild<? extends InstanceIdentifier.PathArgument, ?>> value;
    protected I nodeIdentifier;

    protected AbstractImmutableDataContainerNodeBuilder() {
        this.value = Maps.newLinkedHashMap();
    }

    @Override
    public DataContainerNodeBuilder<I, R> withValue(final List<DataContainerChild<? extends InstanceIdentifier.PathArgument, ?>> value) {
        // TODO Replace or putAll ?
        for (DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> dataContainerChild : value) {
            withChild(dataContainerChild);
        }
        return this;
    }

    @Override
    public DataContainerNodeBuilder<I, R> withChild(final DataContainerChild<?, ?> child) {
        this.value.put(child.getIdentifier(), child);
        return this;
    }


    @Override
    public DataContainerNodeBuilder<I, R> withNodeIdentifier(final I nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
        return this;
    }

    @Override
    public DataContainerNodeBuilder<I, R> addChild(
            final DataContainerChild<? extends PathArgument, ?> child) {
        return withChild(child);
    }
}
