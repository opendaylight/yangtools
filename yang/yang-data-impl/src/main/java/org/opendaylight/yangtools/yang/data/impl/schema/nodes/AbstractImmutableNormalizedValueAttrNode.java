/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

public abstract class AbstractImmutableNormalizedValueAttrNode<K extends PathArgument,V>
        extends AbstractImmutableNormalizedValueNode<K, V> {

    private final ImmutableMap<QName, String> attributes;

    protected AbstractImmutableNormalizedValueAttrNode(final K nodeIdentifier, final V value,
            final Map<QName, String> attributes) {
        super(nodeIdentifier, value);
        this.attributes = ImmutableMap.copyOf(attributes);
    }

    public final Map<QName, String> getAttributes() {
        return attributes;
    }

    public final Object getAttributeValue(final QName value) {
        return attributes.get(value);
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        super.addToStringAttributes(toStringHelper);
        if (!attributes.isEmpty()) {
            toStringHelper.add("attributes", attributes);
        }
        return toStringHelper;
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
