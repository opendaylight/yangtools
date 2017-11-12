/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.submodule;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractRootStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class SubmoduleStatementImpl extends AbstractRootStatement<SubmoduleStatement> implements SubmoduleStatement {
    SubmoduleStatementImpl(final StmtContext<String, SubmoduleStatement, ?> context) {
        super(context);
    }

    @Nonnull
    @Override
    public String getName() {
        return rawArgument();
    }

    @Override
    public YangVersionStatement getYangVersion() {
        return firstDeclared(YangVersionStatement.class);
    }

    @Nonnull
    @Override
    public BelongsToStatement getBelongsTo() {
        return firstDeclared(BelongsToStatement.class);
    }
}
