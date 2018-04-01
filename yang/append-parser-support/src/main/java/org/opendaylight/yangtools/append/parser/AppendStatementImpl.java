/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.append.parser;

import org.opendaylight.yangtools.append.model.api.AppendStatement;
import org.opendaylight.yangtools.append.model.api.StatementPath;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class AppendStatementImpl extends AbstractDeclaredStatement<StatementPath>
        implements AppendStatement {
    AppendStatementImpl(final StmtContext<StatementPath, AppendStatement, ?> context) {
        super(context);
    }

    @Override
    public StatementPath getArgument() {
        return argument();
    }
}
