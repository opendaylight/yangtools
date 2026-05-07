/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A {@link PackageName} that is not {@link BindingPackageName}.
 */
@NonNullByDefault
record JPN(String str) implements PackageName {
    static final JPN EMPTY = new JPN("");

    JPN {
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
}
