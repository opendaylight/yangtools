/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.belongs_to;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class BelongsToStatementImpl extends AbstractDeclaredStatement<String>
        implements BelongsToStatement {
    BelongsToStatementImpl(final StmtContext<String, BelongsToStatement, ?> context) {
        super(context);
    }

    @Nonnull
    @Override
    public String getModule() {
        return argument();
    }

    @Nonnull
    @Override
    public PrefixStatement getPrefix() {
        return firstDeclared(PrefixStatement.class);
    }
}
