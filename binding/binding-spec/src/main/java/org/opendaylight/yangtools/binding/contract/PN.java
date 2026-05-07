/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.contract;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A {@link PackageName} that is not {@link BindingPackageName}.
 *
 * @since 16.0.0
 */
@NonNullByDefault
record PN(String str) implements PackageName {
    static final PN EMPTY = new PN("");

    PN {
        requireNonNull(str);
    }

    @Override
    public String toString() {
        return str;
    }

    @java.io.Serial
    private Object readResolve() {
        return str.isEmpty() ? EMPTY : this;
    }

    @Override
    public int compareTo(final PackageName other) {
        return switch (other) {
            case BindingPackageName binding -> str.compareTo(Naming.PACKAGE_PREFIX);
            case PN java -> str.compareTo(java.str);
        };
    }

    @Override
    public int compareTo(final BindingPackageName other) {
        requireNonNull(other);
        return str.compareTo(Naming.PACKAGE_PREFIX);
    }
}
