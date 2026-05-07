/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.contract;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A {@link PackageName} known to be the result of
 * {@link Naming#getRootPackageName(org.opendaylight.yangtools.yang.common.QNameModule)} or to be a sub-package of such
 * a package.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface BindingPackageName extends PackageName permits ModulePackageName, BPN {
    /**
     * {@return the root package name containing this package name}
     */
    ModulePackageName module();

    @Override
    default int compareTo(final PackageName other) {
        return switch (other) {
            case BindingPackageName binding -> compareTo(binding);
            case PN java -> Naming.PACKAGE_PREFIX.compareTo(java.str());
        };
    }

    @Override
    int compareTo(BindingPackageName other);
}
