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
import org.opendaylight.yangtools.yang.parser.spi.meta.ImportedNamespaceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * Namespace key class for storing belongs-to statements in YANG model storage.
 */
public interface BelongsToModuleContext extends ImportedNamespaceContext<SourceIdentifier> {
    NamespaceBehaviour<SourceIdentifier, StmtContext<?, ?, ?>, @NonNull BelongsToModuleContext> BEHAVIOUR =
            NamespaceBehaviour.sourceLocal(BelongsToModuleContext.class);
}
