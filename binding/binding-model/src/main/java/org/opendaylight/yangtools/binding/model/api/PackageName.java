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
import org.opendaylight.yangtools.concepts.Identifier;
import org.opendaylight.yangtools.yang.common.QNameModule;

/**
 * A string corresponding to {@link Package#getName()}.
 */
@NonNullByDefault
public sealed interface PackageName extends Identifier permits BindingPackageName, JPN {

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

    static BindingPackageName.Root of(final QNameModule namespace) {
        // FIXME: RPN
        throw new UnsupportedOperationException();
    }
}
