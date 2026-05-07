/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.Naming;

/**
 * A {@link PackageName} known to be the result of
 * {@link Naming#getRootPackageName(org.opendaylight.yangtools.yang.common.QNameModule) or to be a sub-package of such
 * a package.
 */
@NonNullByDefault
public sealed interface BindingPackageName extends PackageName permits BindingPackageName.Root, BPN {
    /**
     * A root {@link BindingPackageName}.
     */
    sealed interface Root extends BindingPackageName permits RPN {
        @Override
        default Root root() {
            return this;
        }
    }

    /**
     * {@return the root package name containing this package name}
     */
    Root root();
}
