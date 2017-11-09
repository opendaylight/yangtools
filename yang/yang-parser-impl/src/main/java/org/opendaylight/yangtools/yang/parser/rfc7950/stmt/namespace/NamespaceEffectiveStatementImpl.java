/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.namespace;

import java.net.URI;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class NamespaceEffectiveStatementImpl extends DeclaredEffectiveStatementBase<URI, NamespaceStatement>
        implements NamespaceEffectiveStatement {
    NamespaceEffectiveStatementImpl(final StmtContext<URI, NamespaceStatement, ?> ctx) {
        super(ctx);
    }
}
