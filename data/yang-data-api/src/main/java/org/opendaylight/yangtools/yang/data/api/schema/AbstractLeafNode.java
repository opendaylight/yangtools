/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Abstract base class for {@link LeafNode} implementations.
 */
public abstract non-sealed class AbstractLeafNode<T> extends AbstractNormalizedSimpleValueNode<LeafNode<T>, T>
        implements LeafNode<T> {
    @Override
    @SuppressWarnings("unchecked")
    protected final Class<LeafNode<T>> implementedType() {
        return (@NonNull Class<LeafNode<T>>) contract().asSubclass(LeafNode.class);
    }
}
