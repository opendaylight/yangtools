/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.refine;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ArgumentUtils;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

abstract class AbstractRefineStatementSupport
        extends BaseStatementSupport<Descendant, RefineStatement, RefineEffectiveStatement> {

    AbstractRefineStatementSupport() {
        super(YangStmtMapping.REFINE, CopyPolicy.CONTEXT_INDEPENDENT);
    }

    @Override
    public final Descendant parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return ArgumentUtils.parseDescendantSchemaNodeIdentifier(ctx, value);
    }

    @Override
    protected final RefineStatement createDeclared(final StmtContext<Descendant, RefineStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RefineStatementImpl(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected final RefineStatement createEmptyDeclared(final StmtContext<Descendant, RefineStatement, ?> ctx) {
        // Empty refine is exceedingly unlikely: let's be lazy and reuse the implementation
        return createDeclared(ctx, ImmutableList.of());
    }

    @Override
    protected final RefineEffectiveStatement createEffective(final Current<Descendant, RefineStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        // Empty refine is exceedingly unlikely: let's be lazy and reuse the implementation
        return new RefineEffectiveStatementImpl(stmt.declared(), substatements, stmt.wrapSchemaPath(),
                (SchemaNode) stmt.caerbannog().getEffectOfStatement().iterator().next().buildEffective());
    }
}
