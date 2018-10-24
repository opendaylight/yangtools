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
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

abstract class AbstractGroupingStatementSupport
        extends AbstractQNameStatementSupport<GroupingStatement, EffectiveStatement<QName, GroupingStatement>> {

    AbstractGroupingStatementSupport() {
        super(YangStmtMapping.GROUPING);
    }

    @Override
    public final QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    public final GroupingStatement createDeclared(final StmtContext<QName, GroupingStatement, ?> ctx) {
        // Shadowing check: make sure grandparent does not see a conflicting definition. This is required to ensure
        // that a grouping in child scope does not shadow a grouping in parent scope which occurs later in the text.
        final StmtContext<?, ?, ?> parent = ctx.getParentContext();
        if (parent != null) {
            final StmtContext<?, ?, ?> grandParent = parent.getParentContext();
            if (grandParent != null) {
                checkConflict(grandParent, ctx);
            }
        }

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

        if (stmt != null) {
            final Mutable<?, ?, ?> parent = stmt.getParentContext();
            if (parent != null) {
                // Shadowing check: make sure we do not trample on pre-existing definitions. This catches sibling
                // declarations and parent declarations which have already been declared.
                checkConflict(parent, stmt);
                parent.addContext(GroupingNamespace.class, stmt.coerceStatementArgument(), stmt);
            }
        }
    }

    private static void checkConflict(final StmtContext<?, ?, ?> parent, final StmtContext<QName, ?, ?> stmt) {
        final QName arg = stmt.coerceStatementArgument();
        final StmtContext<?, ?, ?> existing = parent.getFromNamespace(GroupingNamespace.class, arg);
        SourceException.throwIf(existing != null, stmt.getStatementSourceReference(), "Duplicate name for grouping %s",
                arg);
    }
}