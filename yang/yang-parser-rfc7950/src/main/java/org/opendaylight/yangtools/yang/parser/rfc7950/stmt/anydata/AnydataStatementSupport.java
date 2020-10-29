/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.anydata;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseSchemaTreeStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class AnydataStatementSupport
        extends BaseSchemaTreeStatementSupport<AnydataStatement, AnydataEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.ANYDATA)
        .addOptional(YangStmtMapping.CONFIG)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.IF_FEATURE)
        .addOptional(YangStmtMapping.MANDATORY)
        .addAny(YangStmtMapping.MUST)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .addOptional(YangStmtMapping.WHEN)
        .build();
    private static final AnydataStatementSupport INSTANCE = new AnydataStatementSupport();

    private AnydataStatementSupport() {
        super(YangStmtMapping.ANYDATA);
    }

    public static AnydataStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected AnydataStatement createDeclared(final StmtContext<QName, AnydataStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularAnydataStatement(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected AnydataStatement createEmptyDeclared(final StmtContext<QName, AnydataStatement, ?> ctx) {
        return new EmptyAnydataStatement(ctx.coerceStatementArgument());
    }

    @Override
    protected AnydataEffectiveStatement createEffective(
            final StmtContext<QName, AnydataStatement, AnydataEffectiveStatement> ctx,
            final AnydataStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularAnydataEffectiveStatement(declared, ctx.getSchemaPath().get(),
            computeFlags(ctx, substatements), findOriginal(ctx), substatements);
    }

    @Override
    protected AnydataEffectiveStatement createEmptyEffective(
            final StmtContext<QName, AnydataStatement, AnydataEffectiveStatement> ctx,
            final AnydataStatement declared) {
        return new EmptyAnydataEffectiveStatement(declared, ctx.getSchemaPath().get(),
            computeFlags(ctx, ImmutableList.of()), findOriginal(ctx));
    }

    private static int computeFlags(final StmtContext<?, ?, ?> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new FlagsBuilder()
                .setHistory(ctx.getCopyHistory())
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .setConfiguration(ctx.isConfiguration())
                .setMandatory(findFirstArgument(substatements, MandatoryEffectiveStatement.class, Boolean.FALSE))
                .toFlags();
    }

    private static @Nullable AnydataSchemaNode findOriginal(final StmtContext<?, ?, ?> ctx) {
        return (AnydataSchemaNode) ctx.getOriginalCtx().map(StmtContext::buildEffective).orElse(null);
    }
}