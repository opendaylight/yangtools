/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.AttributesContainer;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;

import com.google.common.base.Objects.ToStringHelper;
import com.google.common.collect.ImmutableMap;

public abstract class AbstractImmutableNormalizedAttrNode<K extends InstanceIdentifier.PathArgument,V>
        extends AbstractImmutableNormalizedNode<K, V>
        implements AttributesContainer {

    private final Map<QName, String> attributes;

    protected AbstractImmutableNormalizedAttrNode(final K nodeIdentifier, final V value, final Map<QName, String> attributes) {
        super(nodeIdentifier, value);
        this.attributes = ImmutableMap.copyOf(attributes);
    }

    @Override
    public final Map<QName, String> getAttributes() {
        return attributes;
    }

    @Override
    public final Object getAttributeValue(final QName value) {
        return attributes.get(value);
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return super.addToStringAttributes(toStringHelper).add("attributes", attributes);
    }

    @Override
    protected int valueHashCode() {
        final int result = getValue().hashCode();
// FIXME: are attributes part of hashCode/equals?
//        for (final Entry<?, ?> a : attributes.entrySet()) {
//            result = 31 * result + a.hashCode();
//        }
        return result;
    }

    @Override
    protected boolean valueEquals(final AbstractImmutableNormalizedNode<?, ?> other) {
        if (!getValue().equals(other.getValue())) {
            return false;
        }

// FIXME: are attributes part of hashCode/equals?
//        final Set<Entry<QName, String>> tas = getAttributes().entrySet();
//        final Set<Entry<QName, String>> oas = container.getAttributes().entrySet();
//
//        return tas.containsAll(oas) && oas.containsAll(tas);
        return true;
    }

    protected abstract boolean interfaceEquals(AttributesContainer other);
}
