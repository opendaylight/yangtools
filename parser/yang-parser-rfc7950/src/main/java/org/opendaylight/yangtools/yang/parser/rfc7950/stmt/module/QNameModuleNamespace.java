/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.module;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;

/**
 * Module-local namespace holding the module's QName.
 */
@Beta
public final class QNameModuleNamespace {
    public static final @NonNull ParserNamespace<Empty, QNameModule> INSTANCE = new ParserNamespace<>("QNameModule");
    public static final @NonNull NamespaceBehaviour<?, ?> BEHAVIOUR = NamespaceBehaviour.statementLocal(INSTANCE);

    private QNameModuleNamespace() {
        // Hidden on purpose
    }
}
