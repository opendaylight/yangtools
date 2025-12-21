/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.stmt;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName;

/**
 * Interface for binding {@code prefix}-based {code identifier} ABNF constructs -- for example as used in
 * {@code identifier-ref}, {@code unknown-statement} and {@code node-identifier} ABNF productions.
 *
 * @since 14.0.22
 */
@NonNullByDefault
public interface NamespaceBinding {
    /**
     * {@return the current module, i.e. the {@link QNameModule} a plain {@code identifier}s are bound to}
     */
    QNameModule currentModule();

    /**
     * {@return the module for specified prefix, or {@code null} if no such module exists}
     * @param prefix the prefix
     */
    @Nullable QNameModule lookupModule(UnresolvedQName.Unqualified prefix);
}