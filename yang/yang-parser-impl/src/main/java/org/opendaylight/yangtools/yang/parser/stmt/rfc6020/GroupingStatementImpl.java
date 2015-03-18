/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class GroupingStatementImpl extends AbstractDeclaredStatement<QName>
        implements GroupingStatement {

    protected GroupingStatementImpl(
            StmtContext<QName, GroupingStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<QName, GroupingStatement, EffectiveStatement<QName, GroupingStatement>> {

        public Definition() {
            super(Rfc6020Mapping.Grouping);
        }

        @Override
        public QName parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return Utils.qNameFromArgument(ctx, value);
        }

        @Override
        public GroupingStatement createDeclared(
                StmtContext<QName, GroupingStatement, ?> ctx) {
            return new GroupingStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName, GroupingStatement> createEffective(
                StmtContext<QName, GroupingStatement, EffectiveStatement<QName, GroupingStatement>> ctx) {
            throw new UnsupportedOperationException();
        }

    }

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

}
