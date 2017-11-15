/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * Source-specific mapping of prefixes to namespaces.
 */
public interface IncludedSubmoduleNameToModuleCtx extends IdentifierNamespace<String, StmtContext<?, ?, ?>> {
    NamespaceBehaviour<String, StmtContext<?, ?, ?>, @NonNull IncludedSubmoduleNameToModuleCtx> BEHAVIOUR =
            NamespaceBehaviour.sourceLocal(IncludedSubmoduleNameToModuleCtx.class);
}
