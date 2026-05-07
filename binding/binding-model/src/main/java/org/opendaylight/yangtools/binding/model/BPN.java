/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.Naming;

/**
 * A {@link BindingPackageName} that is not a {@link ModulePackageName}.
 *
 * @since 16.0.0
 */
@NonNullByDefault
record BPN(MPN module, String suffix) implements BindingPackageName {
    BPN {
        requireNonNull(module);
        verify(!suffix.isEmpty());
    }

    @Override
    public BindingPackageName subPackage(final String nextPart) {
        return new BPN(module, suffix + "." + checkPart(nextPart));
    }

    @Override
    public int compareTo(final PackageName pn) {
        return switch (pn) {
            case JPN(var str) -> Naming.PACKAGE_PREFIX.compareTo(str);
            case MPN(var infix) -> {
                final int cmp = module.infix().compareTo(infix);
                yield cmp != 0 ? cmp : 1;
            }
            case BPN(var otherModule, var otherSuffix) -> {
                final int cmp = module.compareTo(otherModule);
                yield cmp != 0 ? cmp : suffix.compareTo(otherSuffix);
            }
        };
    }

    @Override
    public String toString() {
        return Naming.PACKAGE_PREFIX + "." + module.infix() + "." + suffix;
    }

    static String checkPart(final String part) {
        if (part.isEmpty()) {
            throw new IllegalArgumentException("empty part");
        }
        if (part.charAt(0) == '.') {
            throw new IllegalArgumentException("leading dot");
        }
        if (part.charAt(part.length() - 1) == '.') {
            throw new IllegalArgumentException("trailing dot");
        }
        return part;
    }
}
