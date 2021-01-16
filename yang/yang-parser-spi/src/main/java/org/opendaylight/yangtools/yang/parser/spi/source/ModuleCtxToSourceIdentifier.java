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
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * Global mapping of modules to source identifier.
 */
public final class ModuleCtxToSourceIdentifier extends AbstractParserNamespace<StmtContext<?, ?, ?>, SourceIdentifier> {
    public static final @NonNull ModuleCtxToSourceIdentifier INSTANCE = new ModuleCtxToSourceIdentifier();

    private ModuleCtxToSourceIdentifier() {
        super(ModelProcessingPhase.SOURCE_LINKAGE, NamespaceBehaviour.global(ModuleCtxToSourceIdentifier.class));
    }
}
