/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Preconditions;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.binding.BindingMapping;

final class ValueContext {
    private final Codec<Object, Object> codec;
    private final Method getter;

    ValueContext(final Class<?> identifier, final LeafNodeCodecContext leaf) {
        final String getterName = BindingCodecContext.GETTER_PREFIX
                + BindingMapping.getClassName(leaf.getDomPathArgument().getNodeType());
        try {
            getter = identifier.getMethod(getterName);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException(e);
        }
        codec = leaf.getValueCodec();
    }

    Object getAndSerialize(final Object obj) {
        try {
            final Object value = getter.invoke(obj);
            Preconditions.checkArgument(value != null,
                    "All keys must be specified for %s. Missing key is %s. Supplied key is %s",
                    getter.getDeclaringClass(), getter.getName(), obj);
            return codec.serialize(value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

    Object deserialize(final Object obj) {
        return codec.deserialize(obj);
    }

}