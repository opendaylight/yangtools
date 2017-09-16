/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.IdentityRefSpecification;
import org.opendaylight.yangtools.yang.parser.spi.IdentityNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.IdentityRefSpecificationEffectiveStatementImpl;

public class IdentityRefSpecificationImpl extends AbstractDeclaredStatement<String>
        implements IdentityRefSpecification {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .TYPE)
            .addMandatory(YangStmtMapping.BASE)
            .build();

    protected IdentityRefSpecificationImpl(final StmtContext<String, IdentityRefSpecification, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<String, IdentityRefSpecification,
            EffectiveStatement<String, IdentityRefSpecification>> {

        public Definition() {
            super(YangStmtMapping.TYPE);
        }

        @Override
        public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return value;
        }

        @Override
        public IdentityRefSpecification createDeclared(final StmtContext<String, IdentityRefSpecification, ?> ctx) {
            return new IdentityRefSpecificationImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, IdentityRefSpecification> createEffective(
                final StmtContext<String, IdentityRefSpecification,
                EffectiveStatement<String, IdentityRefSpecification>> ctx) {
            return new IdentityRefSpecificationEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(final Mutable<String, IdentityRefSpecification,
                EffectiveStatement<String, IdentityRefSpecification>> stmt) {
            super.onFullDefinitionDeclared(stmt);

            final Collection<StmtContext<QName, BaseStatement, ?>> baseStatements =
                    StmtContextUtils.<QName, BaseStatement>findAllDeclaredSubstatements(stmt, BaseStatement.class);
            for (StmtContext<QName, BaseStatement, ?> baseStmt : baseStatements) {
                final QName baseIdentity = baseStmt.getStatementArgument();
                final StmtContext<?, ?, ?> stmtCtx = stmt.getFromNamespace(IdentityNamespace.class, baseIdentity);
                InferenceException.throwIfNull(stmtCtx, stmt.getStatementSourceReference(),
                    "Referenced base identity '%s' doesn't exist in given scope (module, imported modules, submodules)",
                        baseIdentity.getLocalName());
            }
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return argument();
    }

    @Nonnull
    @Override
    public BaseStatement getBase() {
        return firstDeclared(BaseStatement.class);
    }

    @Nonnull
    @Override
    public Collection<? extends BaseStatement> getBases() {
        return allDeclared(BaseStatement.class);
    }
}
