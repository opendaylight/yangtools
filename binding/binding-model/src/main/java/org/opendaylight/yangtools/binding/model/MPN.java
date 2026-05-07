/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import static com.google.common.base.Verify.verify;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.yang.common.QNameModule;

/**
 * The sole implementation of {@link ModulePackageName}.
 *
 * @since 16.0.0
 */
@NonNullByDefault
record MPN(String infix) implements ModulePackageName {
    private static final int STRIP_LENGTH = Naming.PACKAGE_PREFIX.length() + 1;

    MPN {
        verify(!infix.isEmpty());
    }

    MPN(final QNameModule namespace) {
        this(Naming.getRootPackageName(namespace).substring(STRIP_LENGTH));
    }

    @Override
    public BindingPackageName subPackage(final String nextPart) {
        return new BPN(this, BPN.checkPart(nextPart));
    }

    @Override
    public int compareTo(final PackageName other) {
        return switch (other) {
            case JPN(var str) -> Naming.PACKAGE_PREFIX.compareTo(str);
            case MPN(var otherInfix) -> infix.compareTo(otherInfix);
            case BPN binding -> {
                final int cmp = infix.compareTo(binding.module().infix);
                yield cmp != 0 ? cmp : -1;
            }
        };
    }

    @Override
    public String toString() {
        return Naming.PACKAGE_PREFIX + "." + infix;
    }
}
