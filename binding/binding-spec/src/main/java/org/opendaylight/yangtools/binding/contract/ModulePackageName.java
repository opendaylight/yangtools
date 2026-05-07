/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.contract;

import org.opendaylight.yangtools.yang.common.QNameModule;

/**
 * A {@link BindingPackageName} corresponding to a {@code module}.
 *
 * @since 16.0.0
 */
public sealed interface ModulePackageName extends BindingPackageName permits MPN {

    static ModulePackageName of(final QNameModule namespace) {
        // FIXME: RPN
        throw new UnsupportedOperationException();
    }

    @Override
    default ModulePackageName module() {
        return this;
    }
}