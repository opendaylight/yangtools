/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Interface Contact is designed to hold and model java constant. In Java there are no constant keywords instead of the
 * constant is defined as static final field with assigned value. For this purpose the Constant interface contains
 * methods {@link #getType()} to provide wrapped return Type of Constant, {@link #getName()} the Name of constant and
 * the {@link #getValue()} for providing of value assigned to Constant. To determine of which type the constant value is
 * it is recommended firstly to retrieve Type from constant. The Type interface holds base information like java package
 * name and java type name (e.g. fully qualified name). From this string user should be able to determine to which type
 * can be {@link #getValue()} type typecasted to unbox and provide value assigned to constant.
 *
 * @param type the {@link Type}
 * @param name the name
 * @param value boxed value that is to be assigned
 */
@NonNullByDefault
public record Constant(Type type, String name, Object value) {
    public Constant {
        requireNonNull(type);
        requireNonNull(name);
        requireNonNull(value);
    }

    @Override
    public int hashCode() {
        return 31 * 31 + 31 * name.hashCode() + type.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof Constant other && name.equals(other.name) && type.equals(other.type)
            && value.equals(other.value);
    }
}
