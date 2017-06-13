/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.ChildSchemaNodes;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.effective.ActionEffectiveStatementImpl;

public class ActionStatementImpl extends AbstractDeclaredStatement<QName> implements ActionStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
            YangStmtMapping.ACTION)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.GROUPING)
            .addAny(YangStmtMapping.IF_FEATURE)
            .addOptional(YangStmtMapping.INPUT)
            .addOptional(YangStmtMapping.OUTPUT)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addAny(YangStmtMapping.TYPEDEF)
            .build();

    protected ActionStatementImpl(final StmtContext<QName,ActionStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends AbstractStatementSupport<QName, ActionStatement, EffectiveStatement<QName, ActionStatement>> {

        private static final Set<StatementDefinition> ILLEGAL_PARENTS = ImmutableSet.of(YangStmtMapping.NOTIFICATION,
                YangStmtMapping.RPC, YangStmtMapping.ACTION);

        public Definition() {
            super(YangStmtMapping.ACTION);
        }

        @Override
        public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return StmtContextUtils.qnameFromArgument(ctx, value);
        }

        @Override
        public void onStatementAdded(
                final StmtContext.Mutable<QName, ActionStatement, EffectiveStatement<QName, ActionStatement>> stmt) {
            stmt.getParentContext().addToNs(ChildSchemaNodes.class, stmt.getStatementArgument(), stmt);
        }

        @Override
        public ActionStatement createDeclared(
                final StmtContext<QName, ActionStatement, ?> ctx) {
            return new ActionStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName, ActionStatement> createEffective(
                final StmtContext<QName, ActionStatement, EffectiveStatement<QName, ActionStatement>> ctx) {
            SourceException.throwIf(StmtContextUtils.hasAncestorOfType(ctx, ILLEGAL_PARENTS),
                    ctx.getStatementSourceReference(), "Action %s is defined within a notification, rpc or another action",
                    ctx.getStatementArgument());
            SourceException.throwIf(!StmtContextUtils.hasAncestorOfTypeWithChildOfType(ctx, YangStmtMapping.LIST,
                    YangStmtMapping.KEY), ctx.getStatementSourceReference(),
                    "Action %s is defined within a list that has no key statement", ctx.getStatementArgument());
            SourceException.throwIf(StmtContextUtils.hasParentOfType(ctx, YangStmtMapping.CASE),
                    ctx.getStatementSourceReference(), "Action %s is defined within a case statement",
                    ctx.getStatementArgument());
            SourceException.throwIf(StmtContextUtils.hasParentOfType(ctx, YangStmtMapping.MODULE),
                    ctx.getStatementSourceReference(), "Action %s is defined at the top level of a module",
                    ctx.getStatementArgument());
            return new ActionEffectiveStatementImpl(ctx);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
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
