/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;

/**
 * Namespace class similar to {@link org.opendaylight.yangtools.yang.parser.spi.ModuleNamespace} for storing modules
 * into Yang model storage but keyed by plain name.
 */
public final class ModuleNamespaceForBelongsTo
        extends StatementNamespace<Unqualified, ModuleStatement, ModuleEffectiveStatement> {
    public static final @NonNull NamespaceBehaviour<?, ?, ?> BEHAVIOUR =
        NamespaceBehaviour.global(ModuleNamespaceForBelongsTo.class);

    private ModuleNamespaceForBelongsTo() {
        // Hidden on purpose
    }
}
