/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;

import com.google.common.base.Optional;


/**
 *
 * Node representing Augmentation.
 *
 * Augmentation node MUST NOT be direct child of other augmentation node.
 *
 */
public interface AugmentationNode extends //
    MixinNode, //
    DataContainerNode<AugmentationIdentifier>,
    DataContainerChild<InstanceIdentifier.AugmentationIdentifier, Iterable<DataContainerChild<? extends PathArgument, ?>>> {

    @Override
    public Iterable<DataContainerChild<? extends PathArgument, ?>> getValue();


    @Override
    public Optional<DataContainerChild<? extends PathArgument, ?>> getChild(PathArgument child);

    @Override
    public AugmentationIdentifier getIdentifier();
}
