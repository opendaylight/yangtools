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
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ListEffectiveStatementImpl;

public class ListStatementImpl extends AbstractDeclaredStatement<QName>
        implements ListStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .LIST)
            .add(Rfc6020Mapping.ANYXML, 0, MAX)
            .add(Rfc6020Mapping.CHOICE, 0, MAX)
            .add(Rfc6020Mapping.CONFIG, 0, 1)
            .add(Rfc6020Mapping.CONTAINER, 0, MAX)
            .add(Rfc6020Mapping.DESCRIPTION, 0, 1)
            .add(Rfc6020Mapping.GROUPING, 0, MAX)
            .add(Rfc6020Mapping.IF_FEATURE, 0, MAX)
            .add(Rfc6020Mapping.KEY, 0, 1)
            .add(Rfc6020Mapping.LEAF, 0, MAX)
            .add(Rfc6020Mapping.LEAF_LIST, 0, MAX)
            .add(Rfc6020Mapping.LIST, 0, MAX)
            .add(Rfc6020Mapping.MAX_ELEMENTS, 0, 1)
            .add(Rfc6020Mapping.MIN_ELEMENTS, 0, 1)
            .add(Rfc6020Mapping.MUST, 0, MAX)
            .add(Rfc6020Mapping.ORDERED_BY, 0, 1)
            .add(Rfc6020Mapping.REFERENCE, 0, 1)
            .add(Rfc6020Mapping.STATUS, 0, 1)
            .add(Rfc6020Mapping.TYPEDEF, 0, MAX)
            .add(Rfc6020Mapping.UNIQUE, 0, MAX)
            .add(Rfc6020Mapping.USES, 0, MAX)
            .add(Rfc6020Mapping.WHEN, 0, 1)
            .build();

    protected ListStatementImpl(StmtContext<QName, ListStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<QName, ListStatement, EffectiveStatement<QName, ListStatement>> {

        public Definition() {
            super(Rfc6020Mapping.LIST);
        }

        @Override
        public QName parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return Utils.qNameFromArgument(ctx, value);
        }

        @Override
        public void onStatementAdded(Mutable<QName, ListStatement, EffectiveStatement<QName, ListStatement>> stmt) {
            stmt.getParentContext().addToNs(ChildSchemaNodes.class, stmt.getStatementArgument(), stmt);
        }

        @Override
        public ListStatement createDeclared(
                StmtContext<QName, ListStatement, ?> ctx) {
            return new ListStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName, ListStatement> createEffective(
                StmtContext<QName, ListStatement, EffectiveStatement<QName, ListStatement>> ctx) {
            return new ListEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(Mutable<QName, ListStatement,
                EffectiveStatement<QName, ListStatement>> stmt) {
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
    public Collection<? extends TypedefStatement> getTypedefs() {
        return allDeclared(TypedefStatement.class);
    }

    @Override
    public Collection<? extends GroupingStatement> getGroupings() {
        return allDeclared(GroupingStatement.class);
    }

    @Override
    public Collection<? extends DataDefinitionStatement> getDataDefinitions() {
        return allDeclared(DataDefinitionStatement.class);
    }

    @Override
    public Collection<? extends MustStatement> getMusts() {
        return allDeclared(MustStatement.class);
    }

    @Override
    public KeyStatement getKey() {
        return firstDeclared(KeyStatement.class);
    }

    @Override
    public Collection<? extends UniqueStatement> getUnique() {
        return allDeclared(UniqueStatement.class);
    }
}
