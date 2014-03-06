/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;

public class ImmutableLeafSetEntryNodeBuilder<T> {
    private T value;
    private InstanceIdentifier.NodeWithValue nodeIdentifier;

    protected ImmutableLeafSetEntryNodeBuilder() {
    }

    public static <T> ImmutableLeafSetEntryNodeBuilder<T> get() {
        return new ImmutableLeafSetEntryNodeBuilder<>();
    }

    public ImmutableLeafSetEntryNodeBuilder<T> withValue(T value) {
        this.value = value;
        return this;
    }

    public ImmutableLeafSetEntryNodeBuilder<T> withNodeIdentifier(InstanceIdentifier.NodeWithValue nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
        return this;
    }

    public ImmutableLeafSetEntryNode<T> build() {
        return new ImmutableLeafSetEntryNode<>(nodeIdentifier, value);
    }
}
