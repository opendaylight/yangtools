/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.NodeStep;
import org.opendaylight.yangtools.binding.runtime.api.ContainerLikeRuntimeType;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

sealed class ContainerLikeCodecContext<D extends DataObject>
        extends DataObjectCodecContext<D, ContainerLikeRuntimeType<?, ?>>
        permits StructuralContainerCodecContext {
    ContainerLikeCodecContext(final Class<D> cls, final ContainerLikeRuntimeType<?, ?> type,
            final CodecContextFactory factory) {
        this(new ContainerLikeCodecPrototype(new NodeStep<>(cls), type, factory));
    }

    ContainerLikeCodecContext(final ContainerLikeCodecPrototype prototype) {
        super(prototype);
    }

    @Override
    public final D deserialize(final NormalizedNode data) {
        return createBindingProxy(checkDataArgument(ContainerNode.class, data));
    }
}
