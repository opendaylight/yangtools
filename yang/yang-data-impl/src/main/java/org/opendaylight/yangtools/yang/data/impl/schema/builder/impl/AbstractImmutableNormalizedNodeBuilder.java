/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeAttrBuilder;

import java.util.Collections;
import java.util.Map;

abstract class AbstractImmutableNormalizedNodeBuilder<I extends InstanceIdentifier.PathArgument, V, R extends NormalizedNode<I, ?>>
        implements NormalizedNodeAttrBuilder<I,V,R> {

    protected V value;
    protected I nodeIdentifier;
    protected Map<QName, String> attributes = Collections.emptyMap();


    @Override
    public NormalizedNodeAttrBuilder<I,V,R> withValue(V value) {
        this.value = value;
        return this;
    }

    @Override
    public NormalizedNodeAttrBuilder<I,V,R> withNodeIdentifier(I nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
        return this;
    }

    public NormalizedNodeAttrBuilder<I,V,R> withAttributes(Map<QName, String> attributes){
        this.attributes = attributes;
        return this;
    }
}
