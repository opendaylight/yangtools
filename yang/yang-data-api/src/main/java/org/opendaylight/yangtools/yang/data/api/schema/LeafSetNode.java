/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;

/**
 * Node representing set of simple leaf nodes. Node containing instances of {@link LeafSetEntryNode}.
 *
 * <p>
 * Schema and semantics of this node are described by instance of
 * {@link org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode}.
 *
 * @param <T> Type of leaf node values.
 */
public interface LeafSetNode<T>
        extends DistinctNodeContainer<NodeIdentifier, NodeWithValue<?>, LeafSetEntryNode<T>>, DataContainerChild,
                MixinNode {
    /**
     * {@inheritDoc}
     *
     * <p>
     * Please note that implementations of this method may legally return a transiently-instantiated view over
     * {@link #value()}.
     */
    @Override
    Collection<@NonNull LeafSetEntryNode<T>> body();

    @Beta
    // FIXME: YANGTOOLS-1074 this is a LeafSetNodeBody method and is further specialized
    @NonNull Collection<@NonNull T> values();
}
