/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.contract.Naming.GETTER_PREFIX;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.Type;

/**
 * The code generation shape of a {@link MethodSignature}.
 */
@NonNullByDefault
final class GetterShape implements Comparable<GetterShape> {
    private static final int GETTER_PREFIX_LENGTH = GETTER_PREFIX.length();

    private final MethodSignature method;
    private final boolean hasOverride;
    private final String name;
    private final String propName;

    GetterShape(final String suffix, final MethodSignature method, final boolean hasOverride) {
        this.method = requireNonNull(method);
        this.hasOverride = hasOverride;
        name = GETTER_PREFIX + requireNonNull(suffix);
        propName = Character.toLowerCase(suffix.charAt(0)) + suffix.substring(1);

    }

    GetterShape(final MethodSignature method, final boolean hasOverride) {
        this(method.suffix(), method, hasOverride);
    }

    MethodSignature method() {
        return method;
    }

    boolean hasOverride() {
        return hasOverride;
    }

    String name() {
        return name;
    }

    Type type() {
        return method.returnType();
    }

    boolean isBinary() {
        // FIXME: compare to BaseYangTypes.BINARY_TYPE
        return type().isArray();
    }

    String suffix() {
        return name.substring(GETTER_PREFIX_LENGTH);
    }

    String fieldName() {
        return "_" + propName;
    }

    String propName() {
        return propName;
    }

    @Override
    public int compareTo(final GetterShape other) {
        return name.compareTo(other.name);
    }

    @Override
    public int hashCode() {
        return method.hashCode() + Boolean.hashCode(hasOverride);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj == this || obj instanceof GetterShape other
            && hasOverride == other.hasOverride && name.equals(other.name);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("method", method)
            .add("hasOverride", hasOverride)
            .toString();
    }
}
