/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.EnumTypeValue;

/**
 * Base class for {@link EnumTypeValue} implementations.
 */
public abstract sealed class AbstractEnumTypeValue implements EnumTypeValue
        permits CodegenEnumTypeValue, RuntimeEnumTypeValue {
    private final @NonNull String constantName;
    private final @NonNull String name;
    private final int value;

    @NonNullByDefault
    AbstractEnumTypeValue(final int value, final String name, final String constantName) {
        this.value = value;
        this.name = requireNonNull(name);
        this.constantName = requireNonNull(constantName);
    }

    @Override
    public final int value() {
        return value;
    }

    @Override
    public final String name() {
        return name;
    }

    @Override
    public final String constantName() {
        return constantName;
    }

    @Override
    public final int hashCode() {
        return name.hashCode() * 31 + value;
    }

    @Override
    public final boolean equals(final Object obj) {
        return obj == this || obj instanceof EnumTypeValue other && name.equals(other.name()) && value == other.value();
    }

    @Override
    public final String toString() {
        return new StringBuilder().append("EnumTypeValue [name=").append(name)
            .append(", mappedName=").append(constantName)
            .append(", value=").append(value)
            .append("]").toString();
    }
}