/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.opendaylight.yangtools.concepts.Codec;

final class UnionValueOptionContext {

    final Method getter;
    final Class<?> bindingType;
    final Codec<Object,Object> codec;

    UnionValueOptionContext(final Class<?> valueType,final Method getter, final Codec<Object, Object> codec) {
        this.getter = getter;
        this.bindingType = valueType;
        this.codec = codec;
    }

    public Object serialize(final Object input) {
        Object baValue = getValueFrom(input);
        if(baValue != null) {
            return codec.serialize(baValue);
        }
        return null;
    }

    public Object getValueFrom(final Object input) {
        try {
            return getter.invoke(input);
        } catch (IllegalAccessException  | InvocationTargetException e) {
            throw new IllegalStateException(e);
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
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UnionValueOptionContext other = (UnionValueOptionContext) obj;
        return bindingType.equals(other.bindingType);
    }
}