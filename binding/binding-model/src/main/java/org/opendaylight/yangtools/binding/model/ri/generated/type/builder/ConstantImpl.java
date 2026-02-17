/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.Type;

@NonNullByDefault
record ConstantImpl(Type type, String name, Object value) implements Constant {
    ConstantImpl {
        requireNonNull(type);
        requireNonNull(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode() * 31 + type.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof ConstantImpl other && name.equals(other.name) && type.equals(other.type)
            && value.equals(other.value);
    }

    @Override
    public String toString() {
        return "Constant [type=" + type + ", name=" + name + ", value=" + value + "]";
    }
}
