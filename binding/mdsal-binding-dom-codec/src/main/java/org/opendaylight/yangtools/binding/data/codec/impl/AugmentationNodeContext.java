/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;

final class AugmentationNodeContext<D extends DataObject & Augmentation<?>> extends DataObjectCodecContext<D,AugmentationSchema> {

    public AugmentationNodeContext(final DataContainerCodecPrototype<AugmentationSchema> prototype) {
        super(prototype);
    }

    @Override
    public D deserialize(final NormalizedNode<?, ?> normalizedNode) {
        Preconditions.checkArgument(normalizedNode instanceof AugmentationNode);
        return createBindingProxy((AugmentationNode)normalizedNode);
    }

    @Override
    protected Object deserializeObject(NormalizedNode<?, ?> normalizedNode) {
        return deserialize(normalizedNode);
    }

}