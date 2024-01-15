/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import java.util.Objects;

abstract sealed class AbstractNormalizedSimpleValueNode<N extends NormalizedNode, V>
        extends AbstractNormalizedValueNode<N, V>
        permits AbstractAnydataNode, AbstractAnyxmlNode, AbstractValueNode {
    @Override
    protected final int valueHashCode() {
        return value().hashCode();
    }

    @Override
    protected final boolean valueEquals(final N other) {
        // We can not call directly body().equals because of Empty Type
        // RequireInstanceStatementSupport leaves which always have NULL value
        return Objects.deepEquals(value(), other.body());
    }
}
