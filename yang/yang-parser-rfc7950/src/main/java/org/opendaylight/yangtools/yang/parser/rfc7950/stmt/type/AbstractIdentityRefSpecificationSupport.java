/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.IdentityRefSpecification;
import org.opendaylight.yangtools.yang.model.spi.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.spi.type.IdentityrefTypeBuilder;
import org.opendaylight.yangtools.yang.parser.spi.IdentityNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

abstract class AbstractIdentityRefSpecificationSupport
        extends AbstractStringStatementSupport<IdentityRefSpecification,
            EffectiveStatement<String, IdentityRefSpecification>> {
    AbstractIdentityRefSpecificationSupport() {
        super(YangStmtMapping.TYPE, StatementPolicy.exactReplica());
    }

    @Override
    public final void onFullDefinitionDeclared(final Mutable<String, IdentityRefSpecification,
            EffectiveStatement<String, IdentityRefSpecification>> stmt) {
        super.onFullDefinitionDeclared(stmt);

        final Collection<StmtContext<QName, BaseStatement, ?>> baseStatements =
                StmtContextUtils.findAllDeclaredSubstatements(stmt, BaseStatement.class);
        for (StmtContext<QName, BaseStatement, ?> baseStmt : baseStatements) {
            final QName baseIdentity = baseStmt.getArgument();
            final StmtContext<?, ?, ?> stmtCtx = stmt.getFromNamespace(IdentityNamespace.class, baseIdentity);
            InferenceException.throwIfNull(stmtCtx, stmt,
                "Referenced base identity '%s' doesn't exist in given scope (module, imported modules, submodules)",
                baseIdentity.getLocalName());
        }
    }

    @Override
    protected final IdentityRefSpecification createDeclared(final StmtContext<String, IdentityRefSpecification, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new IdentityRefSpecificationImpl(ctx.getRawArgument(), substatements);
    }

    @Override
    protected final IdentityRefSpecification createEmptyDeclared(
            final StmtContext<String, IdentityRefSpecification, ?> ctx) {
        throw noBase(ctx);
    }

    @Override
    protected final EffectiveStatement<String, IdentityRefSpecification> createEffective(
            final Current<String, IdentityRefSpecification> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        if (substatements.isEmpty()) {
            throw noBase(stmt);
        }

        final IdentityrefTypeBuilder builder = BaseTypes.identityrefTypeBuilder(stmt.argumentAsTypeQName());
        for (final EffectiveStatement<?, ?> subStmt : substatements) {
            if (subStmt instanceof BaseEffectiveStatement) {
                final QName identityQName = ((BaseEffectiveStatement) subStmt).argument();
                final StmtContext<?, IdentityStatement, IdentityEffectiveStatement> identityCtx =
                        stmt.getFromNamespace(IdentityNamespace.class, identityQName);
                builder.addIdentity((IdentitySchemaNode) identityCtx.buildEffective());
            }
        }

        return new TypeEffectiveStatementImpl<>(stmt.declared(), substatements, builder);
    }

    private static SourceException noBase(final CommonStmtCtx stmt) {
        /*
         *  https://tools.ietf.org/html/rfc7950#section-9.10.2
         *
         *     The "base" statement, which is a substatement to the "type"
         *     statement, MUST be present at least once if the type is
         *     "identityref".
         */
        return new SourceException("At least one base statement has to be present", stmt);
    }
}