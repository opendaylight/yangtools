/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

final class ContainerNodeCodecContext<D extends DataObject> extends DataObjectCodecContext<D,ContainerSchemaNode> {

    ContainerNodeCodecContext(final DataContainerCodecPrototype<ContainerSchemaNode> prototype) {
        super(prototype);
    }

    @Override
    public D deserialize(final NormalizedNode<?, ?> data) {
        Preconditions.checkState(data instanceof ContainerNode);
        return createBindingProxy((ContainerNode) data);
    }

    @Override
    protected Object deserializeObject(NormalizedNode<?, ?> normalizedNode) {
        return deserialize(normalizedNode);
    }

}