/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.refine;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

@Beta
public final class RefineTargetNamespace extends ParserNamespace<Empty, StmtContext<?, ?, ?>> {
    public static final @NonNull RefineTargetNamespace NS = new RefineTargetNamespace();
    public static final @NonNull NamespaceBehaviour<?, ?, ?> BEHAVIOUR =
        NamespaceBehaviour.statementLocal(RefineTargetNamespace.class);

    private RefineTargetNamespace() {
        // Hidden on purpose
    }
}
