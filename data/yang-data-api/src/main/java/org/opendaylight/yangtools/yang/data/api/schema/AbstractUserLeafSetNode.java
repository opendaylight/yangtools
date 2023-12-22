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
 * Abstract base class for implementing {@link UserLeafSetNode}s.
 */
public abstract non-sealed class AbstractUserLeafSetNode<T>
        extends AbstractNormalizedValueNode<UserLeafSetNode<T>, @NonNull Collection<@NonNull LeafSetEntryNode<T>>>
        implements UserLeafSetNode<T> {
    @Override
    @SuppressWarnings("unchecked")
    protected final Class<UserLeafSetNode<T>> implementedType() {
        return (@NonNull Class<UserLeafSetNode<T>>) contract().asSubclass(UserLeafSetNode.class);
    }
}
