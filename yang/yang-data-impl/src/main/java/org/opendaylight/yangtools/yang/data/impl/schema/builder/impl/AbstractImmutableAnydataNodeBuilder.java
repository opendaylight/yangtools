/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedSimpleValueNode;

@Beta
public abstract class AbstractImmutableAnydataNodeBuilder<V, T extends AnydataNode<V>>
        extends AbstractImmutableNormalizedNodeBuilder<NodeIdentifier, V, T> {
    protected AbstractImmutableAnydataNodeBuilder() {

    }

    protected abstract static class AbstractImmutableAnydataNode<V>
            extends AbstractImmutableNormalizedSimpleValueNode<NodeIdentifier, V> implements AnydataNode<V> {
        protected AbstractImmutableAnydataNode(final NodeIdentifier nodeIdentifier, final V value) {
            super(nodeIdentifier, value);
        }
    }
}
