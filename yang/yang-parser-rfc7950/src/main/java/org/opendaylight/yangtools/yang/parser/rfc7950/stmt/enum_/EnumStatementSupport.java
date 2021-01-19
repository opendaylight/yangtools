/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.enum_;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class EnumStatementSupport extends AbstractStatementSupport<String, EnumStatement, EnumEffectiveStatement> {
    private static final @NonNull EnumStatementSupport RFC6020_INSTANCE = new EnumStatementSupport(
        SubstatementValidator.builder(YangStmtMapping.ENUM)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addOptional(YangStmtMapping.VALUE)
            .build());
    private static final @NonNull EnumStatementSupport RFC7950_INSTANCE = new EnumStatementSupport(
        SubstatementValidator.builder(YangStmtMapping.ENUM)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.IF_FEATURE)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addOptional(YangStmtMapping.VALUE)
            .build());

    private final SubstatementValidator validator;

    private EnumStatementSupport(final SubstatementValidator validator) {
        super(YangStmtMapping.ENUM, CopyPolicy.CONTEXT_INDEPENDENT);
        this.validator = requireNonNull(validator);
    }

    public static @NonNull EnumStatementSupport rfc6020Instance() {
        return RFC6020_INSTANCE;
    }

    public static @NonNull EnumStatementSupport rfc7950Instance() {
        return RFC7950_INSTANCE;
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        // FIXME: Checks for real value
        return value;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }

    @Override
    protected EnumStatement createDeclared(final StmtContext<String, EnumStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularEnumStatement(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected EnumStatement createEmptyDeclared(final StmtContext<String, EnumStatement, ?> ctx) {
        return new EmptyEnumStatement(ctx.getRawArgument(), ctx.getArgument());
    }

    @Override
    protected EnumEffectiveStatement createEffective(final Current<String, EnumStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyEnumEffectiveStatement(stmt.declared())
            : new RegularEnumEffectiveStatement(stmt.declared(), substatements);
    }
}