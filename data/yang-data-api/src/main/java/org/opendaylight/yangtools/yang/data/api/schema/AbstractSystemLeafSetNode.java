/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Abstract base class for implementing {@link SystemLeafSetNode}s.
 */
public abstract non-sealed class AbstractSystemLeafSetNode<T>
        extends AbstractNormalizedValueNode<SystemLeafSetNode<T>, @NonNull Collection<@NonNull LeafSetEntryNode<T>>>
        implements SystemLeafSetNode<T> {
    @Override
    @SuppressWarnings("unchecked")
    protected final Class<SystemLeafSetNode<T>> implementedType() {
        return (@NonNull Class<SystemLeafSetNode<T>>) contract().asSubclass(SystemLeafSetNode.class);
    }
}
