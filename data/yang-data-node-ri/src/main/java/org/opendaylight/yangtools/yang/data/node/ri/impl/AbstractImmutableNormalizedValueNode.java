/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.node.ri.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.spi.node.AbstractNormalizedNode;

public abstract class AbstractImmutableNormalizedValueNode<K extends PathArgument, N extends NormalizedNode, V>
        extends AbstractNormalizedNode<K, N> {
    private final @NonNull V value;

    protected AbstractImmutableNormalizedValueNode(final K nodeIdentifier, final @NonNull V value) {
        super(nodeIdentifier);
        this.value = requireNonNull(value);
    }

    @Override
    public final V body() {
        return wrapValue(value);
    }

    protected final @NonNull V value() {
        return value;
    }

    protected V wrapValue(final V valueToWrap) {
        return valueToWrap;
    }
}
