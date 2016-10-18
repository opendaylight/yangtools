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
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.LeafEffectiveStatementImpl;

public class LeafStatementImpl extends AbstractDeclaredStatement<QName> implements LeafStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .LEAF)
            .addOptional(Rfc6020Mapping.CONFIG)
            .addOptional(Rfc6020Mapping.DEFAULT)
            .addOptional(Rfc6020Mapping.DESCRIPTION)
            .addAny(Rfc6020Mapping.IF_FEATURE)
            .addOptional(Rfc6020Mapping.MANDATORY)
            .addAny(Rfc6020Mapping.MUST)
            .addOptional(Rfc6020Mapping.REFERENCE)
            .addOptional(Rfc6020Mapping.STATUS)
            .addMandatory(Rfc6020Mapping.TYPE)
            .addOptional(Rfc6020Mapping.UNITS)
            .addOptional(Rfc6020Mapping.WHEN)
            .build();

    protected LeafStatementImpl(final StmtContext<QName, LeafStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<QName,LeafStatement,EffectiveStatement<QName,LeafStatement>> {

        public Definition() {
            super(Rfc6020Mapping.LEAF);
        }

        @Override public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return Utils.qNameFromArgument(ctx,value);
        }

        @Override
        public void onStatementAdded(final Mutable<QName, LeafStatement, EffectiveStatement<QName, LeafStatement>> stmt) {
            stmt.getParentContext().addToNs(ChildSchemaNodes.class, stmt.getStatementArgument(), stmt);
        }

        @Override public LeafStatement createDeclared(
                final StmtContext<QName, LeafStatement, ?> ctx) {
            return new LeafStatementImpl(ctx);
        }

        @Override public EffectiveStatement<QName, LeafStatement> createEffective(
                final StmtContext<QName, LeafStatement, EffectiveStatement<QName, LeafStatement>> ctx) {
            return new LeafEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(final Mutable<QName, LeafStatement,
                EffectiveStatement<QName, LeafStatement>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            SUBSTATEMENT_VALIDATOR.validate(stmt);
        }
    }

    @Nullable @Override
    public Collection<? extends MustStatement> getMusts() {
        return allDeclared(MustStatement.class);
    }

    @Nullable @Override
    public DefaultStatement getDefault() {
        return firstDeclared(DefaultStatement.class);
    }

    @Nullable @Override
    public ConfigStatement getConfig() {
        return firstDeclared(ConfigStatement.class);
    }

    @Nullable @Override
    public MandatoryStatement getMandatory() {
        return firstDeclared(MandatoryStatement.class);
    }

    @Nonnull @Override
    public QName getName() {
        return argument();
    }

    @Override
    public WhenStatement getWhenStatement() {
        return firstDeclared(WhenStatement.class);
    }

    @Nonnull @Override
    public Collection<? extends IfFeatureStatement> getIfFeatures() {
        return allDeclared(IfFeatureStatement.class);
    }

    @Nullable @Override
    public DescriptionStatement getDescription() {
        return firstDeclared(DescriptionStatement.class);
    }

    @Nullable @Override
    public ReferenceStatement getReference() {
        return firstDeclared(ReferenceStatement.class);
    }

    @Nonnull @Override
    public TypeStatement getType() {
        return firstDeclared(TypeStatement.class);
    }

    @Nullable @Override
    public UnitsStatement getUnits() {
        return firstDeclared(UnitsStatement.class);
    }

    @Nullable @Override
    public StatusStatement getStatus() {
        return firstDeclared(StatusStatement.class);
    }
}
