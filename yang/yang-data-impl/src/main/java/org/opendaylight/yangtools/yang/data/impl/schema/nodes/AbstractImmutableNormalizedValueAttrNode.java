/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.AttributesContainer;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public abstract class AbstractImmutableNormalizedValueAttrNode<K extends YangInstanceIdentifier.PathArgument,V>
        extends AbstractImmutableNormalizedValueNode<K, V>
        implements AttributesContainer {

    private final Map<QName, String> attributes;

    protected AbstractImmutableNormalizedValueAttrNode(final K nodeIdentifier, final V value, final Map<QName, String> attributes) {
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
        final int result = getValue() != null ? getValue().hashCode() : 1;
// FIXME: are attributes part of hashCode/equals?
//        for (final Entry<?, ?> a : attributes.entrySet()) {
//            result = 31 * result + a.hashCode();
//        }
        return result;
    }

    @Override
    protected boolean valueEquals(final AbstractImmutableNormalizedNode<?, ?> other) {
        // We can not call directly getValue.equals because of Empty Type
        // Definition leaves
        // which allways have NULL value

        Object thisValue = getValue();
        Object otherValue = other.getValue();

        if (thisValue != null && otherValue != null && thisValue.getClass().isArray()
                && otherValue.getClass().isArray()) {
            return arrayEquals(thisValue, otherValue);
        }

        if (!Objects.equal(thisValue, otherValue)) {
            return false;
        }

        // FIXME: are attributes part of hashCode/equals?
        // final Set<Entry<QName, String>> tas = getAttributes().entrySet();
        // final Set<Entry<QName, String>> oas =
        // container.getAttributes().entrySet();
        //
        // return tas.containsAll(oas) && oas.containsAll(tas);
        return true;
    }

    private boolean arrayEquals(Object thisValue, Object otherValue) {

        if (thisValue instanceof Object[] && otherValue instanceof Object[])
            return Arrays.deepEquals((Object[]) thisValue, (Object[]) otherValue);

        else if (thisValue instanceof byte[] && otherValue instanceof byte[])
            return Arrays.equals((byte[]) thisValue, (byte[]) otherValue);

        else if (thisValue instanceof boolean[] && otherValue instanceof boolean[])
            return Arrays.equals((boolean[]) thisValue, (boolean[]) otherValue);

        else if (thisValue instanceof char[] && otherValue instanceof char[])
            return Arrays.equals((char[]) thisValue, (char[]) otherValue);

        else if (thisValue instanceof int[] && otherValue instanceof int[])
            return Arrays.equals((int[]) thisValue, (int[]) otherValue);

        else if (thisValue instanceof float[] && otherValue instanceof float[])
            return Arrays.equals((float[]) thisValue, (float[]) otherValue);

        else if (thisValue instanceof double[] && otherValue instanceof double[])
            return Arrays.equals((double[]) thisValue, (double[]) otherValue);

        else if (thisValue instanceof short[] && otherValue instanceof short[])
            return Arrays.equals((short[]) thisValue, (short[]) otherValue);

        else if (thisValue instanceof long[] && otherValue instanceof long[])
            return Arrays.equals((long[]) thisValue, (long[]) otherValue);

        return false;
    }

}
