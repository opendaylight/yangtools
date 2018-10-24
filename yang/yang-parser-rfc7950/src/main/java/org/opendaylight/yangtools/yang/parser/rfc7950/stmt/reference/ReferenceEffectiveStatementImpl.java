/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.reference;

import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.DeclaredEffectiveStatementBase.WithArgument;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class ReferenceEffectiveStatementImpl extends WithArgument<String, ReferenceStatement>
        implements ReferenceEffectiveStatement {
    ReferenceEffectiveStatementImpl(final StmtContext<String, ReferenceStatement, ?> ctx) {
        super(ctx);
    }
}
