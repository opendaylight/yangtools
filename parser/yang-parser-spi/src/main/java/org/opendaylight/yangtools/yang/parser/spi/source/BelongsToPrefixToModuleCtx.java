/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.parser.spi.meta.ImportedNamespaceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;

/**
 * Source-specific mapping of belongsTo prefixes to module identifiers. This mapping allows source-specific context
 * to correctly populate prefixes map for actual parsing phase and eventually, resolve QName for any valid declared
 * statement.
 */
public final class BelongsToPrefixToModuleCtx extends ImportedNamespaceContext<String> {
    public static final @NonNull BelongsToPrefixToModuleCtx INSTANCE = new BelongsToPrefixToModuleCtx();

    private BelongsToPrefixToModuleCtx() {
        super(NamespaceBehaviour.sourceLocal(BelongsToPrefixToModuleCtx.class));
    }
}
