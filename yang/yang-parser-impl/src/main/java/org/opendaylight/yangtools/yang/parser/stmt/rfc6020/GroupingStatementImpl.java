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
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.parser.spi.GroupingNamespace;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.GroupingEffectiveStatementImpl;

public class GroupingStatementImpl extends AbstractDeclaredStatement<QName>
        implements GroupingStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .GROUPING)
            .addAny(Rfc6020Mapping.ANYXML)
            .addAny(Rfc6020Mapping.CHOICE)
            .addAny(Rfc6020Mapping.CONTAINER)
            .addOptional(Rfc6020Mapping.DESCRIPTION)
            .addAny(Rfc6020Mapping.GROUPING)
            .addAny(Rfc6020Mapping.LEAF)
            .addAny(Rfc6020Mapping.LEAF_LIST)
            .addAny(Rfc6020Mapping.LIST)
            .addOptional(Rfc6020Mapping.REFERENCE)
            .addOptional(Rfc6020Mapping.STATUS)
            .addAny(Rfc6020Mapping.TYPEDEF)
            .addAny(Rfc6020Mapping.USES)
            .build();

    protected GroupingStatementImpl(
            final StmtContext<QName, GroupingStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<QName, GroupingStatement, EffectiveStatement<QName, GroupingStatement>> {

        public Definition() {
            super(Rfc6020Mapping.GROUPING);
        }

        @Override
        public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return Utils.qNameFromArgument(ctx, value);
        }

        @Override
        public GroupingStatement createDeclared(
                final StmtContext<QName, GroupingStatement, ?> ctx) {
            return new GroupingStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName, GroupingStatement> createEffective(
                final StmtContext<QName, GroupingStatement, EffectiveStatement<QName, GroupingStatement>> ctx) {
            return new GroupingEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(final Mutable<QName, GroupingStatement,
                EffectiveStatement<QName, GroupingStatement>> stmt) {
            SUBSTATEMENT_VALIDATOR.validate(stmt);

            if (stmt != null && stmt.getParentContext() != null) {
                stmt.getParentContext().addContext(GroupingNamespace.class, stmt.getStatementArgument(), stmt);
            }
        }

    }

    @Nonnull
    @Override
    public QName getName() {
        return argument();
    }

    @Override
    public StatusStatement getStatus() {
        return firstDeclared(StatusStatement.class);
    }

    @Override
    public DescriptionStatement getDescription() {
        return firstDeclared(DescriptionStatement.class);
    }

    @Override
    public ReferenceStatement getReference() {
        return firstDeclared(ReferenceStatement.class);
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
