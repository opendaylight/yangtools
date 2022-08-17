/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * Global mapping of modules to source identifier.
 */
public final class ModuleCtxToSourceIdentifier extends ParserNamespace<StmtContext<?, ?, ?>, SourceIdentifier> {
    public static final @NonNull ModuleCtxToSourceIdentifier NS = new ModuleCtxToSourceIdentifier();
    public static final @NonNull NamespaceBehaviour<?, ?, ?> BEHAVIOUR =
        NamespaceBehaviour.global(ModuleCtxToSourceIdentifier.class);

    private ModuleCtxToSourceIdentifier() {
        // Hidden on purpose
    }
}
