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
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
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
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ChoiceEffectiveStatementImpl;

public class ChoiceStatementImpl extends AbstractDeclaredStatement<QName>
        implements ChoiceStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .CHOICE)
            .addAny(Rfc6020Mapping.ANYXML)
            .addAny(Rfc6020Mapping.CASE)
            .addOptional(Rfc6020Mapping.CONFIG)
            .addAny(Rfc6020Mapping.CONTAINER)
            .addOptional(Rfc6020Mapping.DEFAULT)
            .addOptional(Rfc6020Mapping.DESCRIPTION)
            .addAny(Rfc6020Mapping.IF_FEATURE)
            .addAny(Rfc6020Mapping.LEAF)
            .addAny(Rfc6020Mapping.LEAF_LIST)
            .addAny(Rfc6020Mapping.LIST)
            .addOptional(Rfc6020Mapping.MANDATORY)
            .addOptional(Rfc6020Mapping.REFERENCE)
            .addOptional(Rfc6020Mapping.STATUS)
            .addOptional(Rfc6020Mapping.WHEN)
            .build();

    protected ChoiceStatementImpl(final StmtContext<QName, ChoiceStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<QName, ChoiceStatement, EffectiveStatement<QName, ChoiceStatement>> {

        public Definition() {
            super(Rfc6020Mapping.CHOICE);
        }

        @Override
        public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return Utils.qNameFromArgument(ctx, value);
        }

        @Override
        public void onStatementAdded(final Mutable<QName, ChoiceStatement, EffectiveStatement<QName, ChoiceStatement>> stmt) {
            stmt.getParentContext().addToNs(ChildSchemaNodes.class, stmt.getStatementArgument(), stmt);
        }

        @Override
        public ChoiceStatement createDeclared(
                final StmtContext<QName, ChoiceStatement, ?> ctx) {
            return new ChoiceStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName, ChoiceStatement> createEffective(
                final StmtContext<QName, ChoiceStatement, EffectiveStatement<QName, ChoiceStatement>> ctx) {
            return new ChoiceEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(final Mutable<QName, ChoiceStatement,
                EffectiveStatement<QName, ChoiceStatement>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            SUBSTATEMENT_VALIDATOR.validate(stmt);
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
