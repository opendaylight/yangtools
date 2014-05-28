/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;

import com.google.common.base.Preconditions;

public abstract class AbstractImmutableNormalizedValueNode<K extends InstanceIdentifier.PathArgument,V>
        extends AbstractImmutableNormalizedNode<K, V> {

    private final V value;

    protected AbstractImmutableNormalizedValueNode(final K nodeIdentifier, final V value) {
    	super(nodeIdentifier);
        this.value = Preconditions.checkNotNull(value, "value");
    }

    @Override
    public final V getValue() {
        return value;
    }
}
