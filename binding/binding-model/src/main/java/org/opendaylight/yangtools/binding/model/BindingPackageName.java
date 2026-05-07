/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.yang.common.QNameModule;

/**
 * A {@link PackageName} known to be the result of {@link Naming#getRootPackageName(QNameModule)} or to be a sub-package
 * of such a package.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface BindingPackageName extends PackageName permits ModulePackageName, BPN {
    /**
     * {@return the root package name containing this package name}
     */
    ModulePackageName module();

    /**
     * {@return a {@link BindingPackageName} appending the specified part to this package name}
     * @param nextPart the part to append
     * @throws IllegalArgumentException if the part is not valid
     */
    BindingPackageName subPackage(String nextPart);

    @Override
    int compareTo(BindingPackageName other);

    @Override
    default int compareTo(final JavaPackageName other) {
        return other.compareTo(this);
    }
}
