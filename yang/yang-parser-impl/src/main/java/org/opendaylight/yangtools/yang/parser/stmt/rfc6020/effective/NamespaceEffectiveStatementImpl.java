/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import java.net.URI;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public final class NamespaceEffectiveStatementImpl extends DeclaredEffectiveStatementBase<URI, NamespaceStatement>
        implements NamespaceEffectiveStatement {
    public NamespaceEffectiveStatementImpl(final StmtContext<URI, NamespaceStatement, ?> ctx) {
        super(ctx);
    }
}
