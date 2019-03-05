/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import java.util.Objects;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

public abstract class AbstractImmutableNormalizedSimpleValueNode<K extends PathArgument,V>
        extends AbstractImmutableNormalizedValueNode<K, V> {
    protected AbstractImmutableNormalizedSimpleValueNode(final K nodeIdentifier, final V value) {
        super(nodeIdentifier, value);
    }

    @Override
    protected int valueHashCode() {
        final V local = value();
        final int result = local != null ? local.hashCode() : 1;
        // FIXME: are attributes part of hashCode/equals?
        return result;
    }

    @Override
    protected boolean valueEquals(final AbstractImmutableNormalizedNode<?, ?> other) {
        // We can not call directly getValue.equals because of Empty Type
        // RequireInstanceStatementSupport leaves which always have NULL value

        // FIXME: are attributes part of hashCode/equals?
        return Objects.deepEquals(value(), other.getValue());
    }
}
