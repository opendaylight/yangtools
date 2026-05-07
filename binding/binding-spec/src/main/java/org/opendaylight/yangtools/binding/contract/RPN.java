/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.contract;

import static com.google.common.base.Verify.verify;

/**
 * A root {@link BindingPackageName}.
 *
 * @since 16.0.0
 */
record RPN(String infix) implements RootPackageName {
    RPN {
        verify(!infix.isEmpty());
    }

    @Override
    public String toString() {
        return Naming.PACKAGE_PREFIX + "." + infix;
    }
}
