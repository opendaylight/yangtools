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
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.IdentityrefTypeBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.IdentityNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

abstract class AbstractIdentityRefSpecificationSupport
        extends BaseStatementSupport<String, IdentityRefSpecification,
            EffectiveStatement<String, IdentityRefSpecification>> {
    AbstractIdentityRefSpecificationSupport() {
        super(YangStmtMapping.TYPE);
    }

    @Override
    public final String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
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

    @Override
    protected final IdentityRefSpecification createDeclared(final StmtContext<String, IdentityRefSpecification, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new IdentityRefSpecificationImpl(ctx, substatements);
    }

    @Override
    protected final IdentityRefSpecification createEmptyDeclared(
            final StmtContext<String, IdentityRefSpecification, ?> ctx) {
        throw noBase(ctx);
    }

    @Override
    protected final EffectiveStatement<String, IdentityRefSpecification> createEffective(
            final StmtContext<String, IdentityRefSpecification,
                EffectiveStatement<String, IdentityRefSpecification>> ctx,
            final IdentityRefSpecification declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final IdentityrefTypeBuilder builder = BaseTypes.identityrefTypeBuilder(ctx.getSchemaPath().get());
        for (final EffectiveStatement<?, ?> stmt : substatements) {
            if (stmt instanceof BaseEffectiveStatement) {
                final QName identityQName = ((BaseEffectiveStatement) stmt).argument();
                final StmtContext<?, IdentityStatement, IdentityEffectiveStatement> identityCtx =
                        ctx.getFromNamespace(IdentityNamespace.class, identityQName);
                builder.addIdentity((IdentitySchemaNode) identityCtx.buildEffective());
            }
        }

        return new TypeEffectiveStatementImpl<>(declared, substatements, builder);
    }

    @Override
    protected final EffectiveStatement<String, IdentityRefSpecification> createEmptyEffective(
            final StmtContext<String, IdentityRefSpecification,
                EffectiveStatement<String, IdentityRefSpecification>> ctx,
            final IdentityRefSpecification declared) {
        throw noBase(ctx);
    }

    private static SourceException noBase(final StmtContext<?, ?, ?> ctx) {
        /*
         *  https://tools.ietf.org/html/rfc7950#section-9.10.2
         *
         *     The "base" statement, which is a substatement to the "type"
         *     statement, MUST be present at least once if the type is
         *     "identityref".
         */
        return new SourceException("At least one base statement has to be present",
            ctx.getStatementSourceReference());
    }
}