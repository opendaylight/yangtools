/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.api;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;

public interface DataContainerNodeBuilder<I extends PathArgument, R extends DataContainerNode<I>>
        extends NormalizedNodeContainerBuilder<I, PathArgument, DataContainerChild, R> {
    @Override
    DataContainerNodeBuilder<I, R> withValue(Collection<DataContainerChild> value);

    @Override
    DataContainerNodeBuilder<I, R> withNodeIdentifier(I nodeIdentifier);

    @NonNull DataContainerNodeBuilder<I, R> withChild(DataContainerChild child);

    @NonNull DataContainerNodeBuilder<I, R> withoutChild(PathArgument key);
}