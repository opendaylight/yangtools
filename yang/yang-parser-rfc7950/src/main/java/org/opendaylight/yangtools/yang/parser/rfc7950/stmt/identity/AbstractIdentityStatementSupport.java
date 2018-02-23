/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.identity;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.parser.spi.IdentityNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

abstract class AbstractIdentityStatementSupport
        extends AbstractQNameStatementSupport<IdentityStatement, IdentityEffectiveStatement> {

    AbstractIdentityStatementSupport() {
        super(YangStmtMapping.IDENTITY);
    }

    @Override
    public final QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    public final IdentityStatement createDeclared(final StmtContext<QName, IdentityStatement, ?> ctx) {
        return new IdentityStatementImpl(ctx);
    }

    @Override
    public final IdentityEffectiveStatement createEffective(
            final StmtContext<QName, IdentityStatement, IdentityEffectiveStatement> ctx) {
        return new IdentityEffectiveStatementImpl(ctx);
    }

    @Override
    public final void onStatementDefinitionDeclared(
            final Mutable<QName, IdentityStatement, IdentityEffectiveStatement> stmt) {
        stmt.addToNs(IdentityNamespace.class, stmt.coerceStatementArgument(), stmt);
    }
}
