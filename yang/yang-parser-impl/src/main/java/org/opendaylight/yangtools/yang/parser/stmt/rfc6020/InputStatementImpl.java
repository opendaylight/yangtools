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
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.InputEffectiveStatementImpl;

public class InputStatementImpl extends AbstractDeclaredStatement<QName>
        implements InputStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .INPUT)
            .addAny(Rfc6020Mapping.ANYXML)
            .addAny(Rfc6020Mapping.CHOICE)
            .addAny(Rfc6020Mapping.CONTAINER)
            .addAny(Rfc6020Mapping.GROUPING)
            .addAny(Rfc6020Mapping.LEAF)
            .addAny(Rfc6020Mapping.LEAF_LIST)
            .addAny(Rfc6020Mapping.LIST)
            .addAny(Rfc6020Mapping.TYPEDEF)
            .addAny(Rfc6020Mapping.USES)
            .build();

    protected InputStatementImpl(final StmtContext<QName, InputStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<QName, InputStatement, EffectiveStatement<QName, InputStatement>> {

        public Definition() {
            super(Rfc6020Mapping.INPUT);
        }

        @Override
        public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return Utils.qNameFromArgument(ctx, "input");
        }

        @Override
        public void onStatementAdded(final Mutable<QName, InputStatement, EffectiveStatement<QName, InputStatement>> stmt) {
            stmt.getParentContext().addToNs(ChildSchemaNodes.class, stmt.getStatementArgument(), stmt);
        }

        @Override
        public InputStatement createDeclared(
                final StmtContext<QName, InputStatement, ?> ctx) {
            return new InputStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName, InputStatement> createEffective(
                final StmtContext<QName, InputStatement, EffectiveStatement<QName, InputStatement>> ctx) {
            return new InputEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(final Mutable<QName, InputStatement,
                EffectiveStatement<QName, InputStatement>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            SUBSTATEMENT_VALIDATOR.validate(stmt);
        }
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

    @Nonnull
    @Override
    public Collection<? extends DataDefinitionStatement> getDataDefinitions() {
        return allDeclared(DataDefinitionStatement.class);
    }
}
