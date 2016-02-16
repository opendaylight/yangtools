/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator.MAX;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByStatement;
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
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.LeafListEffectiveStatementImpl;

public class LeafListStatementImpl extends AbstractDeclaredStatement<QName>
        implements LeafListStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .LEAF_LIST)
            .add(Rfc6020Mapping.CONFIG, 0, 1)
            .add(Rfc6020Mapping.DESCRIPTION, 0, 1)
            .add(Rfc6020Mapping.IF_FEATURE, 0, MAX)
            .add(Rfc6020Mapping.MIN_ELEMENTS, 0, 1)
            .add(Rfc6020Mapping.MAX_ELEMENTS, 0, 1)
            .add(Rfc6020Mapping.MUST, 0, MAX)
            .add(Rfc6020Mapping.ORDERED_BY, 0, 1)
            .add(Rfc6020Mapping.REFERENCE, 0, 1)
            .add(Rfc6020Mapping.STATUS, 0, 1)
            .add(Rfc6020Mapping.TYPE, 1, 1)
            .add(Rfc6020Mapping.UNITS, 0, 1)
            .add(Rfc6020Mapping.WHEN, 0, 1)
            .build();

    protected LeafListStatementImpl(
            StmtContext<QName, LeafListStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<QName, LeafListStatement, EffectiveStatement<QName, LeafListStatement>> {

        public Definition() {
            super(Rfc6020Mapping.LEAF_LIST);
        }

        @Override
        public void onStatementAdded(
                Mutable<QName, LeafListStatement, EffectiveStatement<QName, LeafListStatement>> stmt) {
            stmt.getParentContext().addToNs(ChildSchemaNodes.class, stmt.getStatementArgument(), stmt);
        }

        @Override
        public QName parseArgumentValue(StmtContext<?, ?, ?> ctx, String value)
                {
            return Utils.qNameFromArgument(ctx, value);
        }

        @Override
        public LeafListStatement createDeclared(
                StmtContext<QName, LeafListStatement, ?> ctx) {
            return new LeafListStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName, LeafListStatement> createEffective(
                StmtContext<QName, LeafListStatement, EffectiveStatement<QName, LeafListStatement>> ctx) {
            return new LeafListEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(Mutable<QName, LeafListStatement,
                EffectiveStatement<QName, LeafListStatement>> stmt) {
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
    public ConfigStatement getConfig() {
        return firstDeclared(ConfigStatement.class);
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

    @Override
    public MinElementsStatement getMinElements() {
        return firstDeclared(MinElementsStatement.class);
    }

    @Override
    public MaxElementsStatement getMaxElements() {
        return firstDeclared(MaxElementsStatement.class);
    }

    @Override
    public OrderedByStatement getOrderedBy() {
        return firstDeclared(OrderedByStatement.class);
    }

    @Override
    public Collection<? extends MustStatement> getMusts() {
        return allDeclared(MustStatement.class);
    }

    @Override
    public TypeStatement getType() {
        return firstDeclared(TypeStatement.class);
    }

    @Override
    public UnitsStatement getUnits() {
        return firstDeclared(UnitsStatement.class);
    }
}
