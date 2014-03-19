/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import com.google.common.collect.ImmutableMap;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.AttributesContainer;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

import com.google.common.base.Preconditions;

import java.util.Map;

public abstract class AbstractImmutableNormalizedAttrNode<K extends InstanceIdentifier.PathArgument,V>
        extends AbstractImmutableNormalizedNode<K, V>
        implements AttributesContainer {

    private final Map<QName, String> attributes;

    protected AbstractImmutableNormalizedAttrNode(K nodeIdentifier, V value, Map<QName, String> attributes) {
        super(nodeIdentifier, value);
        this.attributes = ImmutableMap.copyOf(attributes);
    }

    @Override
    public Map<QName, String> getAttributes() {
        return attributes;
    }

    @Override
    public Object getAttributeValue(QName value) {
        return attributes.get(value);
    }

}
