/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Information about a property. It includes its getter method and, optionally, its nonnull method.
 */
abstract sealed class PropertyInfo {
    /**
     * This property has only a getFoo() method.
     */
    static final class Getter extends PropertyInfo {
        Getter(final Method getterMethod) {
            super(getterMethod);
        }
    }

    /**
     * This property has a getFoo() method and a non-default nonnullFoo() method.
     */
    static final class GetterAndNonnull extends PropertyInfo {
        private final @NonNull Method nonnullMethod;

        GetterAndNonnull(final Method getterMethod, final Method nonnullMethod) {
            super(getterMethod);
            this.nonnullMethod = requireNonNull(nonnullMethod);
        }

        @NonNull Method nonnullMethod() {
            return nonnullMethod;
        }
    }

    private final @NonNull Method getterMethod;

    private PropertyInfo(final Method getterMethod) {
        this.getterMethod = requireNonNull(getterMethod);
    }

    final @NonNull Method getterMethod() {
        return getterMethod;
    }
}
