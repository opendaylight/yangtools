/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * The Type interface defines the base type for all types defined in java. Each
 * Type defined in java MUST contain name and package name, except of primitive
 * types like int, byte etc. In case of mapping of primitive type the package
 * name MUST be left as empty string.
 */
@NonNullByDefault
public interface Type extends Immutable {
    /**
     * {@return this type's {@link JavaTypeName}}
     */
    JavaTypeName name();

    /**
     * {@return this type's {@link JavaTypeName}}
     * @deprecated Use {@link #name()} instead.
     */
    @Deprecated(since = "15.0.0", forRemoval = true)
    default JavaTypeName getIdentifier() {
        return name();
    }

    /**
     * {@return name of the package that interface belongs to}
     */
    default String packageName() {
        return name().packageName();
    }

    /**
     * {@return the {@code simple name} of this type}
     */
    default String simpleName() {
        return name().simpleName();
    }

    /**
     * {@return the {@code canonical name} of this type}
     */
    default String canonicalName() {
        return name().canonicalName();
    }

    static Type of(final JavaTypeName identifier) {
        return new DefaultType(identifier);
    }

    static Type of(final Class<?> type) {
        return of(JavaTypeName.create(type));
    }
}
