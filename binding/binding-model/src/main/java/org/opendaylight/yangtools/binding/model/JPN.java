/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.Naming;

@NonNullByDefault
record JPN(String str) implements JavaPackageName {
    static final JPN EMPTY = new JPN("");

    JPN {
        requireNonNull(str);
    }

    @Override
    public int compareTo(final PackageName pn) {
        return switch (pn) {
            case BindingPackageName bpn -> str.compareTo(Naming.PACKAGE_PREFIX);
            case JPN jpn -> str.compareTo(jpn.str);
        };
    }

    @Override
    public String toString() {
        return str;
    }
}
