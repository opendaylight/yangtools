/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.bit;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

abstract class AbstractBitStatementSupport extends BaseStatementSupport<String, BitStatement, BitEffectiveStatement> {
    AbstractBitStatementSupport() {
        super(YangStmtMapping.BIT);
    }

    @Override
    public final String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        // Performs de-duplication and interning in one go
        return StmtContextUtils.parseIdentifier(ctx, value).getLocalName();
    }

    @Override
    protected final BitStatement createDeclared(final StmtContext<String, BitStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularBitStatement(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected final BitStatement createEmptyDeclared(final StmtContext<String, BitStatement, ?> ctx) {
        return new EmptyBitStatement(ctx.coerceStatementArgument());
    }

    @Override
    protected final BitEffectiveStatement createEffective(
            final StmtContext<String, BitStatement, BitEffectiveStatement> ctx, final BitStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularBitEffectiveStatement(declared, substatements);
    }

    @Override
    protected final BitEffectiveStatement createEmptyEffective(
            final StmtContext<String, BitStatement, BitEffectiveStatement> ctx, final BitStatement declared) {
        return new EmptyBitEffectiveStatement(declared);
    }
}
