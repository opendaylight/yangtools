/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.ImplicitSubstatement;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementDefinitionContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ChoiceEffectiveStatementImpl;

public class ChoiceStatementImpl extends AbstractDeclaredStatement<QName>
        implements ChoiceStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .CHOICE)
            .addAny(YangStmtMapping.ANYXML)
            .addAny(YangStmtMapping.CASE)
            .addOptional(YangStmtMapping.CONFIG)
            .addAny(YangStmtMapping.CONTAINER)
            .addOptional(YangStmtMapping.DEFAULT)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.IF_FEATURE)
            .addAny(YangStmtMapping.LEAF)
            .addAny(YangStmtMapping.LEAF_LIST)
            .addAny(YangStmtMapping.LIST)
            .addOptional(YangStmtMapping.MANDATORY)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addOptional(YangStmtMapping.WHEN)
            .build();

    protected ChoiceStatementImpl(final StmtContext<QName, ChoiceStatement, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractQNameStatementSupport<ChoiceStatement, EffectiveStatement<QName, ChoiceStatement>> {
        private static final StatementSupport<?, ?, ?> IMPLICIT_CASE = new CaseStatementImpl.Definition();

        public Definition() {
            super(YangStmtMapping.CHOICE);
        }

        @Override
        public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return StmtContextUtils.qnameFromArgument(ctx, value);
        }

        @Override
        public Optional<StatementContextBase<?, ?, ?>> beforeSubStatementCreated(
                final StmtContext.Mutable<?, ?, ?> stmt, final int offset,
                final StatementDefinitionContext<?, ?, ?> def, final StatementSourceReference ref, final String argument) {

            if (YangValidationBundles.SUPPORTED_CASE_SHORTHANDS.contains(def.getPublicView())) {
                return Optional.of(createImplicitCase(stmt, offset, ref, argument));
            }
            return Optional.empty();
        }

        @Override
        public void onStatementAdded(
                final Mutable<QName, ChoiceStatement, EffectiveStatement<QName, ChoiceStatement>> stmt) {
            stmt.getParentContext().addToNs(ChildSchemaNodes.class, stmt.getStatementArgument(), stmt);
        }

        @Override
        public ChoiceStatement createDeclared(final StmtContext<QName, ChoiceStatement, ?> ctx) {
            return new ChoiceStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName, ChoiceStatement> createEffective(
                final StmtContext<QName, ChoiceStatement, EffectiveStatement<QName, ChoiceStatement>> ctx) {
            return new ChoiceEffectiveStatementImpl(ctx);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }

        protected StatementSupport<?, ?, ?> implictCase() {
            return IMPLICIT_CASE;
        }

        private StatementContextBase<?, ?, ?> createImplicitCase(final StmtContext.Mutable<?, ?, ?> stmt,
                final int offset, final StatementSourceReference ref, final String argument) {
            final StatementDefinitionContext<?, ?, ?> def = new StatementDefinitionContext<>(implictCase());
            return ((StatementContextBase<?, ?, ?>) stmt).createSubstatement(offset, def, ImplicitSubstatement.of(ref),
                    argument);
        }
    }

    @Nonnull
    @Override
    public QName getName() {
        return argument();
    }

    @Nullable
    @Override
    public DefaultStatement getDefault() {
        return firstDeclared(DefaultStatement.class);
    }

    @Nullable
    @Override
    public ConfigStatement getConfig() {
        return firstDeclared(ConfigStatement.class);
    }

    @Nullable
    @Override
    public MandatoryStatement getMandatory() {
        return firstDeclared(MandatoryStatement.class);
    }

    @Nonnull
    @Override
    public Collection<? extends CaseStatement> getCases() {
        return allDeclared(CaseStatement.class);
    }

    @Override
    public WhenStatement getWhenStatement() {
        return firstDeclared(WhenStatement.class);
    }

    @Nonnull
    @Override
    public Collection<? extends IfFeatureStatement> getIfFeatures() {
        return allDeclared(IfFeatureStatement.class);
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
}
