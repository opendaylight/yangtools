/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeAttrBuilder;

abstract class AbstractImmutableNormalizedNodeBuilder<I extends PathArgument, V, R extends NormalizedNode<I, ?>>
        implements NormalizedNodeAttrBuilder<I,V,R> {

    private Map<QName, String> attributes = ImmutableMap.of();
    private I nodeIdentifier;
    private V value;

    protected final I getNodeIdentifier() {
        checkState(nodeIdentifier != null, "Identifier has not been set");
        return nodeIdentifier;
    }

    protected final V getValue() {
        checkState(value != null, "Value has not been set");
        return value;
    }

    protected final Map<QName, String> getAttributes() {
        return attributes;
    }

    @Override
    public NormalizedNodeAttrBuilder<I, V, R> withValue(final V withValue) {
        this.value = requireNonNull(withValue);
        return this;
    }

    @Override
    public NormalizedNodeAttrBuilder<I, V, R> withNodeIdentifier(final I withNodeIdentifier) {
        this.nodeIdentifier = requireNonNull(withNodeIdentifier);
        return this;
    }

    @Override
    public NormalizedNodeAttrBuilder<I, V, R> withAttributes(final Map<QName, String> withAttributes) {
        this.attributes = requireNonNull(withAttributes);
        return this;
    }
}
