/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;

public class AugmentationNode extends DataObjectCodecContext<AugmentationSchema> {

    public AugmentationNode(final DataContainerCodecPrototype<AugmentationSchema> prototype) {
        super(prototype);
    }

    @Override
    Object dataFromNormalizedNode(final NormalizedNode<?, ?> normalizedNode) {
        Preconditions.checkArgument(normalizedNode instanceof org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode);
        return LazyDataObject.create(this, (org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode) normalizedNode);
    }
}