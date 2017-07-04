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
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.ImplicitSubstatement;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementDefinitionContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.RpcEffectiveStatementImpl;

public class RpcStatementImpl extends AbstractDeclaredStatement<QName>
        implements RpcStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .RPC)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.GROUPING)
            .addAny(YangStmtMapping.IF_FEATURE)
            .addOptional(YangStmtMapping.INPUT)
            .addOptional(YangStmtMapping.OUTPUT)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addAny(YangStmtMapping.TYPEDEF)
            .build();

    protected RpcStatementImpl(final StmtContext<QName, RpcStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends AbstractStatementSupport<QName, RpcStatement, EffectiveStatement<QName, RpcStatement>> {
        // TODO: share instances
        private static final StatementSupport<?, ?, ?> IMPLICIT_INPUT = new InputStatementImpl.Definition();
        private static final StatementSupport<?, ?, ?> IMPLICIT_OUTPUT = new OutputStatementImpl.Definition();

        public Definition() {
            super(YangStmtMapping.RPC);
        }

        @Override
        public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return StmtContextUtils.qnameFromArgument(ctx, value);
        }

        @Override
        public void onStatementAdded(final Mutable<QName, RpcStatement, EffectiveStatement<QName, RpcStatement>> stmt) {
            stmt.getParentContext().addToNs(ChildSchemaNodes.class, stmt.getStatementArgument(), stmt);
        }

        @Override
        public RpcStatement createDeclared(
                final StmtContext<QName, RpcStatement, ?> ctx) {
            return new RpcStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName, RpcStatement> createEffective(
                final StmtContext<QName, RpcStatement, EffectiveStatement<QName, RpcStatement>> ctx) {
            return new RpcEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(final Mutable<QName, RpcStatement, EffectiveStatement<QName, RpcStatement>> stmt) {
            super.onFullDefinitionDeclared(stmt);

            if (StmtContextUtils.findFirstDeclaredSubstatement(stmt, InputStatement.class) == null) {
                addImplicitStatement((StatementContextBase<?, ?, ?>) stmt, implictInput());
            }

            if (StmtContextUtils.findFirstDeclaredSubstatement(stmt, OutputStatement.class) == null) {
                addImplicitStatement((StatementContextBase<?, ?, ?>) stmt, implictOutput());
            }
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }

        protected StatementSupport<?, ?, ?> implictInput() {
            return IMPLICIT_INPUT;
        }

        protected StatementSupport<?, ?, ?> implictOutput() {
            return IMPLICIT_OUTPUT;
        }

        private static void addImplicitStatement(final StatementContextBase<?, ?, ?> parent,
                final StatementSupport<?, ?, ?> statementToAdd) {
            final StatementDefinitionContext<?, ?, ?> stmtDefCtx = new StatementDefinitionContext<>(statementToAdd);

            parent.createSubstatement(parent.declaredSubstatements().size(), stmtDefCtx,
                ImplicitSubstatement.of(parent.getStatementSourceReference()), null);
        }
    }

    @Override
    public QName getName() {
        return argument();
    }

    @Nonnull
    @Override
    public Collection<? extends TypedefStatement> getTypedefs() {
        return allDeclared(TypedefStatement.class);
    }

    @Nonnull
    @Override
    public Collection<? extends GroupingStatement> getGroupings() {
        return allDeclared(GroupingStatement.class);
    }

    @Nullable
    @Override
    public StatusStatement getStatus() {
        return firstDeclared(StatusStatement.class);
    }

    @Nullable
    @Override
    public DescriptionStatement getDescription() {
        return firstDeclared(DescriptionStatement.class);
    }

    @Nullable
    @Override
    public ReferenceStatement getReference() {
        return firstDeclared(ReferenceStatement.class);
    }

    @Override
    public InputStatement getInput() {
        return firstDeclared(InputStatement.class);
    }

    @Override
    public OutputStatement getOutput() {
        return firstDeclared(OutputStatement.class);
    }

    @Nonnull
    @Override
    public Collection<? extends IfFeatureStatement> getIfFeatures() {
        return allDeclared(IfFeatureStatement.class);
    }
}
