/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import org.opendaylight.yangtools.concepts.Codec;

final class UnionValueOptionContext {
    private static final MethodType OBJECT_TYPE = MethodType.methodType(Object.class, Object.class);
    private final Class<?> bindingType;
    private final MethodHandle getter;
    private final Codec<Object,Object> codec;

    UnionValueOptionContext(final Class<?> valueType, final Method getter, final Codec<Object, Object> codec) {
        this.bindingType = Preconditions.checkNotNull(valueType);
        this.codec = Preconditions.checkNotNull(codec);

        try {
            this.getter = MethodHandles.publicLookup().unreflect(getter).asType(OBJECT_TYPE);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to access method " + getter, e);
        }
    }

    Object serialize(final Object input) {
        final Object baValue = getValueFrom(input);
        if (baValue == null) {
            return null;
        }

        return codec.serialize(baValue);
    }

    Object getValueFrom(final Object input) {
        try {
            return getter.invokeExact(input);
        } catch (Throwable e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public int hashCode() {
        return bindingType.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UnionValueOptionContext)) {
            return false;
        }

        final UnionValueOptionContext other = (UnionValueOptionContext) obj;
        return bindingType.equals(other.bindingType);
    }
}