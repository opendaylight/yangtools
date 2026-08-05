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
import static org.opendaylight.yangtools.binding.contract.Naming.toFirstLower;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.Type;

/**
 * The code generation shape of a {@link MethodSignature}.
 */
@NonNullByDefault
record GetterShape(MethodSignature method, boolean hasOverride) implements Comparable<GetterShape> {
    private static final int GETTER_PREFIX_LENGTH = GETTER_PREFIX.length();

    GetterShape {
        requireNonNull(method);
    }

    String name() {
        return method.name();
    }

    Type type() {
        return method.returnType();
    }

    boolean isBinary() {
        // FIXME: compare to BaseYangTypes.BINARY_TYPE
        return type().isArray();
    }

    String suffix() {
        return name().substring(GETTER_PREFIX_LENGTH);
    }

    String fieldName() {
        return "_" + propName();
    }

    String propName() {
        return toFirstLower(suffix());
    }

    @Override
    public int compareTo(final GetterShape other) {
        return method.name().compareTo(other.method.name());
    }
}