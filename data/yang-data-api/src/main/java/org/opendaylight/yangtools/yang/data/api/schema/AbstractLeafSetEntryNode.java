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
 * Abstract base class for {@link LeafSetEntryNode} implementations.
 */
public abstract non-sealed class AbstractLeafSetEntryNode<T>
        extends AbstractNormalizedSimpleValueNode<LeafSetEntryNode<T>, T> implements LeafSetEntryNode<T> {
    @Override
    @SuppressWarnings("unchecked")
    protected final Class<LeafSetEntryNode<T>> implementedType() {
        return (@NonNull Class<LeafSetEntryNode<T>>) contract().asSubclass(LeafSetEntryNode.class);
    }
}
