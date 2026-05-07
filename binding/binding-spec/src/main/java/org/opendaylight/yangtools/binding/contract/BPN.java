/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.contract;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A {@link BindingPackageName} that is not {@link ModulePackageName}.
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
    public int compareTo(final BindingPackageName other) {
        return switch (other) {
            case ModulePackageName module -> module.compareTo(module);
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
}
