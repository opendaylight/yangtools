/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.Naming;

/**
 * A non-root {@link BindingPackageName}.
 */
@NonNullByDefault
record BPN(RPN root, String suffix) implements BindingPackageName {
    BPN {
        requireNonNull(root);
        verify(!suffix.isEmpty());
    }

    @Override
    public String toString() {
        return Naming.PACKAGE_PREFIX + "." + root.infix() + "." + suffix;
    }
}
