/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import java.util.Date;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public final class RevisionDateEffectiveStatementImpl
        extends DeclaredEffectiveStatementBase<Date, RevisionDateStatement> {
    public RevisionDateEffectiveStatementImpl(final StmtContext<Date, RevisionDateStatement, ?> ctx) {
        super(ctx);
    }
}
