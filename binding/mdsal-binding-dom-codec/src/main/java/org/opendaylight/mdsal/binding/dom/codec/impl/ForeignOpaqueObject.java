/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.binding.AbstractOpaqueObject;
import org.opendaylight.yangtools.yang.binding.OpaqueData;
import org.opendaylight.yangtools.yang.binding.OpaqueObject;

/**
 * Proxy support for an OpaqueObject.
 *
 * @param <T> OpaqueObject type
 */
// FIXME: MDSAL-442: this class has no run-time dependencies on other generated types, hence is a prime candidate for
//        becoming a prototype instantiated in a separate ClassLoader (see MDSAL-401) and not being a proxy at all.
final class ForeignOpaqueObject<T extends OpaqueObject<T>> extends AbstractOpaqueObject<T>
        implements InvocationHandler {
    private final @NonNull Class<T> implementedInterface;
    private final @NonNull ForeignOpaqueData<?> value;

    ForeignOpaqueObject(final Class<T> implementedInterface, final ForeignOpaqueData<?> value) {
        this.implementedInterface = requireNonNull(implementedInterface);
        this.value = requireNonNull(value);
    }

    @Override
    public Class<T> implementedInterface() {
        return implementedInterface;
    }

    @Override
    public OpaqueData<?> getValue() {
        return value;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) {
        switch (method.getName()) {
            case "equals":
                return equals(args[0]);
            case "hashCode":
                return hashCode();
            case "getValue":
                return getValue();
            case "toString":
                return toString();
            case BindingMapping.DATA_CONTAINER_IMPLEMENTED_INTERFACE_NAME:
                return implementedInterface;
            default:
                throw new NoSuchMethodError("Unknown method " + method);
        }
    }
}
