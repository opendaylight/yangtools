/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.amend.parser;

import org.opendaylight.yangtools.amend.model.api.AmendStatement;
import org.opendaylight.yangtools.amend.model.api.StatementPath;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class AmendStatementImpl extends AbstractDeclaredStatement<StatementPath>
        implements AmendStatement {
    AmendStatementImpl(final StmtContext<StatementPath, AmendStatement, ?> context) {
        super(context);
    }

    @Override
    public StatementPath getArgument() {
        return argument();
    }
}
