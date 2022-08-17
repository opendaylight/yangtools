/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi;

import java.io.Serial;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;

/**
 * A derived namespace allowing lookup of modules based on their {@link QNameModule}.
 */
public final class NamespaceToModule
        extends StatementNamespace<QNameModule, ModuleStatement, ModuleEffectiveStatement> {
    public static final @NonNull NamespaceToModule NS = new NamespaceToModule();
    public static final @NonNull NamespaceBehaviour<?, ?, ?> BEHAVIOUR = NamespaceBehaviour.global(NS);
    @Serial
    private static final long serialVersionUID = 1L;

    private NamespaceToModule() {
        // Hidden on purpose
    }
}
