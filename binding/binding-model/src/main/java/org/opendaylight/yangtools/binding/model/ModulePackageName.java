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
 * A {@link BindingPackageName} corresponding to a {@code module}. This is a more efficient and type-safe equivalent of
 * {@link Naming#getRootPackageName(QNameModule)}.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface ModulePackageName extends BindingPackageName permits MPN {
    /**
     * {@return a {@link ModulePackageName} for the specified namespace}
     * @param namespace the namespace
     */
    static ModulePackageName of(final QNameModule namespace) {
        return new MPN(namespace);
    }

    @Override
    default ModulePackageName module() {
        return this;
    }
}