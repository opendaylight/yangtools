/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.bit;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class BitStatementSupport extends AbstractStatementSupport<String, BitStatement, BitEffectiveStatement> {
    private static final @NonNull BitStatementSupport RFC6020_INSTANCE = new BitStatementSupport(
        SubstatementValidator.builder(YangStmtMapping.BIT)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addOptional(YangStmtMapping.POSITION)
            .build());
    private static final @NonNull BitStatementSupport RFC7950_INSTANCE = new BitStatementSupport(
        SubstatementValidator.builder(YangStmtMapping.BIT)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.IF_FEATURE)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addOptional(YangStmtMapping.POSITION)
            .build());

    private final SubstatementValidator validator;

    private BitStatementSupport(final SubstatementValidator validator) {
        super(YangStmtMapping.BIT, StatementPolicy.contextIndependent());
        this.validator = requireNonNull(validator);
    }

    public static @NonNull BitStatementSupport rfc6020Instance() {
        return RFC6020_INSTANCE;
    }

    public static @NonNull BitStatementSupport rfc7950Instance() {
        return RFC7950_INSTANCE;
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        // Performs de-duplication and interning in one go
        return StmtContextUtils.parseIdentifier(ctx, value).getLocalName();
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }

    @Override
    protected BitStatement createDeclared(final StmtContext<String, BitStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createBit(ctx.getArgument(), substatements);
    }

    @Override
    protected BitStatement createEmptyDeclared(final StmtContext<String, BitStatement, ?> ctx) {
        return DeclaredStatements.createBit(ctx.getArgument());
    }

    @Override
    protected BitEffectiveStatement createEffective(final Current<String, BitStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyBitEffectiveStatement(stmt.declared())
            : new RegularBitEffectiveStatement(stmt.declared(), substatements);
    }
}
