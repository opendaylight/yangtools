/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;

import com.google.common.base.Optional;

/**
 *
 * @param <I> Node Identifier type
 * @param <K> Child Node Identifier type
 * @param <V> Child Node type
 */
public interface NormalizedNodeContainer<I extends PathArgument, K extends PathArgument, V extends NormalizedNode<? extends K, ?>>
        extends NormalizedNode<I, Iterable<V>> {

    @Override
    public I getIdentifier();

    @Override
    public Iterable<V> getValue();

    /**
     *
     *
     * @param child
     * @return
     */
    Optional<V> getChild(K child);
}
