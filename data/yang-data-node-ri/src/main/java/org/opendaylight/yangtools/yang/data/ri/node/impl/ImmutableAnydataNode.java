/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.ri.node.impl;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;

public final class ImmutableAnydataNode<V>
        extends AbstractImmutableNormalizedSimpleValueNode<NodeIdentifier, AnydataNode<?>, V>
        implements AnydataNode<V> {
    private final Class<V> objectModel;

    public ImmutableAnydataNode(final NodeIdentifier nodeIdentifier, final V value, final Class<V> objectModel) {
        super(nodeIdentifier, value);
        this.objectModel = requireNonNull(objectModel);
    }

    @Override
    public Class<V> bodyObjectModel() {
        return objectModel;
    }

    @Override
    protected Class<AnydataNode<?>> implementedType() {
        return (Class) AnydataNode.class;
    }
}