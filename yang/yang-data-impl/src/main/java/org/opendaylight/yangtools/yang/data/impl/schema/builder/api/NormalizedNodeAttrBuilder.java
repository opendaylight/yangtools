/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.api;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public interface NormalizedNodeAttrBuilder<I extends PathArgument, V, R extends NormalizedNode<I, ?>>
        extends AttributesBuilder<NormalizedNodeAttrBuilder<I, V, R>>, NormalizedNodeBuilder<I, V, R> {

    @Override
    NormalizedNodeAttrBuilder<I, V, R> withValue(V value);

    @Override
    NormalizedNodeAttrBuilder<I, V, R> withNodeIdentifier(I nodeIdentifier);

}
