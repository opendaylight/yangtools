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
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public interface CollectionNodeBuilder<CK extends PathArgument, CV extends NormalizedNode<? extends CK, ?>, R extends NormalizedNode<InstanceIdentifier.NodeIdentifier, ?>>
        extends NormalizedNodeContainerBuilder<InstanceIdentifier.NodeIdentifier,CK, CV, R> {

    //TODO might be list to keep ordering and map internal
    @Override
    CollectionNodeBuilder<CK,CV, R> withValue(List<CV> value);

    @Override
    CollectionNodeBuilder<CK,CV, R> withNodeIdentifier(InstanceIdentifier.NodeIdentifier nodeIdentifier);

    @Override
    CollectionNodeBuilder<CK,CV, R> withChild(CV child);
}
