/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.grouping;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.parser.spi.GroupingNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

abstract class AbstractGroupingStatementSupport
        extends AbstractQNameStatementSupport<GroupingStatement, EffectiveStatement<QName, GroupingStatement>> {

    AbstractGroupingStatementSupport() {
        super(YangStmtMapping.GROUPING);
    }

    @Override
    public final QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.qnameFromArgument(ctx, value);
    }

    @Override
    public final GroupingStatement createDeclared(final StmtContext<QName, GroupingStatement, ?> ctx) {
        return new GroupingStatementImpl(ctx);
    }

    @Override
    public final EffectiveStatement<QName, GroupingStatement> createEffective(
            final StmtContext<QName, GroupingStatement, EffectiveStatement<QName, GroupingStatement>> ctx) {
        return new GroupingEffectiveStatementImpl(ctx);
    }

    @Override
    public final void onFullDefinitionDeclared(final Mutable<QName, GroupingStatement,
            EffectiveStatement<QName, GroupingStatement>> stmt) {
        super.onFullDefinitionDeclared(stmt);

        if (stmt != null && stmt.getParentContext() != null) {
            stmt.getParentContext().addContext(GroupingNamespace.class, stmt.getStatementArgument(), stmt);
        }
    }
}