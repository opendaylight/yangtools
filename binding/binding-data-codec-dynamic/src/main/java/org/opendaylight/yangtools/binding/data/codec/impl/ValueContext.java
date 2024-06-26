/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.base.Throwables;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

final class ValueContext {
    private static final MethodType OBJECT_METHOD = MethodType.methodType(Object.class, Object.class);

    private final ValueCodec<Object, Object> codec;
    private final MethodHandle getter;
    private final Class<?> identifier;
    private final String getterName;

    ValueContext(final Class<?> identifier, final ValueNodeCodecContext leaf) {
        getterName = leaf.getGetterName();
        try {
            getter = MethodHandles.publicLookup().unreflect(identifier.getMethod(getterName)).asType(OBJECT_METHOD);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new IllegalStateException(String.format("Cannot find method %s in class %s", getterName, identifier),
                e);
        }
        this.identifier = identifier;
        codec = leaf.getValueCodec();
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    Object getAndSerialize(final Object obj) {
        final Object value;
        try {
            value = getter.invokeExact(obj);
        } catch (Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new IllegalStateException(e);
        }

        checkArgument(value != null, "All keys must be specified for %s. Missing key is %s. Supplied key is %s",
                identifier, getterName, obj);
        return codec.serialize(value);
    }

    Object deserialize(final Object obj) {
        checkArgument(obj != null, "Attempted to serialize null for %s component of %s", getterName, identifier);
        return verifyNotNull(codec.deserialize(obj), "Codec for %s of %s returned null for %s", getterName, identifier,
            obj);
    }
}
