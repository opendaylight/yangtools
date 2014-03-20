/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;

abstract class AbstractImmutableDataContainerNodeAttrBuilder<I extends InstanceIdentifier.PathArgument, R extends DataContainerNode<I>>
        extends AbstractImmutableDataContainerNodeBuilder<I, R>
        implements DataContainerNodeAttrBuilder<I, R> {

    protected Map<QName, String> attributes = Collections.emptyMap();

    @Override
    public DataContainerNodeAttrBuilder<I, R> withAttributes(Map<QName, String> attributes){
        this.attributes = attributes;
        return this;
    }

    @Override
    public DataContainerNodeAttrBuilder<I, R> withValue(List<DataContainerChild<? extends InstanceIdentifier.PathArgument, ?>> value) {
        return (DataContainerNodeAttrBuilder<I, R>) super.withValue(value);
    }

    @Override
    public DataContainerNodeAttrBuilder<I, R> withChild(DataContainerChild<?, ?> child) {
        return (DataContainerNodeAttrBuilder<I, R>) super.withChild(child);
    }

    @Override
    public DataContainerNodeAttrBuilder<I, R> withNodeIdentifier(I nodeIdentifier) {
        return (DataContainerNodeAttrBuilder<I, R>) super.withNodeIdentifier(nodeIdentifier);
    }
}