/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.eclipse.jdt.annotation.NonNull;

public abstract sealed class AbstractNormalizedValueNode<N extends NormalizedNode, V>
        extends AbstractNormalizedNode<N>
        permits AbstractNormalizedSimpleValueNode, AbstractSystemLeafSetNode, AbstractUnkeyedListNode {
    @Override
    public final V body() {
        return wrappedValue();
    }

    protected abstract @NonNull V value();

    protected abstract @NonNull V wrappedValue();
}
