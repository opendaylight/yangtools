/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import org.opendaylight.yangtools.binding.contract.Naming;

/**
 * A {@link PackageName} that is not a {@link BindingPackageName}.
 *
 * @since 16.0.0
 */
public sealed interface JavaPackageName extends PackageName permits JPN {


    static PackageName of(final String name) {
        if (name.isEmpty()) {
            return JPN.EMPTY;
        }
        if (!name.startsWith(Naming.PACKAGE_PREFIX)) {
            return new JPN(name);
        }
        // FIXME: BPN/RPN
        throw new UnsupportedOperationException();
    }

}
