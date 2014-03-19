/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.api;

import java.util.List;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;

public interface DataContainerNodeAttrBuilder<I extends InstanceIdentifier.PathArgument, R extends DataContainerNode<I>>
        extends DataContainerNodeBuilder<I, R>,
        AttributesBuilder<DataContainerNodeAttrBuilder<I, R>> {

    @Override
    DataContainerNodeAttrBuilder<I, R> withValue(List<DataContainerChild<? extends InstanceIdentifier.PathArgument, ?>> value);

    @Override
    DataContainerNodeAttrBuilder<I, R> withNodeIdentifier(I nodeIdentifier);

    DataContainerNodeAttrBuilder<I, R> withChild(DataContainerChild<?, ?> child);
}
