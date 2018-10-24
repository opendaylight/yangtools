/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.IdentityRefSpecification;
import org.opendaylight.yangtools.yang.parser.spi.IdentityNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

abstract class AbstractIdentityRefSpecificationSupport
        extends AbstractStatementSupport<String, IdentityRefSpecification,
            EffectiveStatement<String, IdentityRefSpecification>> {
    AbstractIdentityRefSpecificationSupport() {
        super(YangStmtMapping.TYPE);
    }

    @Override
    public final String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public final IdentityRefSpecification createDeclared(final StmtContext<String, IdentityRefSpecification, ?> ctx) {
        return new IdentityRefSpecificationImpl(ctx);
    }

    @Override
    public final EffectiveStatement<String, IdentityRefSpecification> createEffective(
            final StmtContext<String, IdentityRefSpecification,
            EffectiveStatement<String, IdentityRefSpecification>> ctx) {
        return new IdentityRefSpecificationEffectiveStatement(ctx);
    }

    @Override
    public final void onFullDefinitionDeclared(final Mutable<String, IdentityRefSpecification,
            EffectiveStatement<String, IdentityRefSpecification>> stmt) {
        super.onFullDefinitionDeclared(stmt);

        final Collection<StmtContext<QName, BaseStatement, ?>> baseStatements =
                StmtContextUtils.findAllDeclaredSubstatements(stmt, BaseStatement.class);
        for (StmtContext<QName, BaseStatement, ?> baseStmt : baseStatements) {
            final QName baseIdentity = baseStmt.coerceStatementArgument();
            final StmtContext<?, ?, ?> stmtCtx = stmt.getFromNamespace(IdentityNamespace.class, baseIdentity);
            InferenceException.throwIfNull(stmtCtx, stmt.getStatementSourceReference(),
                "Referenced base identity '%s' doesn't exist in given scope (module, imported modules, submodules)",
                    baseIdentity.getLocalName());
        }
    }
}