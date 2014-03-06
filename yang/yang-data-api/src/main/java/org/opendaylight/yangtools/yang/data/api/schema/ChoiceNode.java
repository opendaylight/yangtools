/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;

import com.google.common.base.Optional;

/**
 *
 * Node representing choice.
 *
 * @author Tony Tkacik
 *
 */
public interface ChoiceNode extends //
        MixinNode, //
        DataContainerNode<NodeIdentifier>,
        DataContainerChild<NodeIdentifier, Iterable<DataContainerChild<? extends PathArgument, ?>>> {

    @Override
    public NodeIdentifier getIdentifier();

    @Override
    public Optional<DataContainerChild<?, ?>> getChild(PathArgument child);

}
