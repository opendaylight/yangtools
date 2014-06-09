/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;

import com.google.common.base.Optional;

/**
 * Node which is not leaf, but has child {@link NormalizedNode}s as its valzue.
 * 
 * 
 * NormalizedNodeContainer does not have a value, but it has a child
 * nodes. Definition of possible and valid child nodes is introduced
 * in subclasses of this interface.
 * 
 * This interface should not be used directly, but rather use of of derived subinterfaces
 * such as {@link DataContainerNode}, {@link MapNode}, {@link LeafSetNode}.
 * 
 * @param <I>
 *            Node Identifier type
 * @param <K>
 *            Child Node Identifier type
 * @param <V>
 *            Child Node type
 */
public interface NormalizedNodeContainer<I extends PathArgument, K extends PathArgument, V extends NormalizedNode<? extends K, ?>>
        extends NormalizedNode<I, Iterable<V>> {

    @Override
    I getIdentifier();

    /**
     * Returns immutable iteration of child nodes of this node.
     * 
     */
    @Override
    Iterable<V> getValue();

    /**
     * Returns child node identified by provided key.
     * 
     * @param child
     *            Path argument identifying child node
     * @return Optional with child node if child exists.
     *         {@link Optional#absent()} if child does not exists.
     */
    Optional<V> getChild(K child);
}
