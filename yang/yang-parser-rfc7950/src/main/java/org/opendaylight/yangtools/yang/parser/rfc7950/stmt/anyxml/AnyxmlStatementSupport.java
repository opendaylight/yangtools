/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.anyxml;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseSchemaTreeStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class AnyxmlStatementSupport
        extends BaseSchemaTreeStatementSupport<AnyxmlStatement, AnyxmlEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .ANYXML)
        .addOptional(YangStmtMapping.CONFIG)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.IF_FEATURE)
        .addOptional(YangStmtMapping.MANDATORY)
        .addAny(YangStmtMapping.MUST)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .addOptional(YangStmtMapping.WHEN)
        .build();
    private static final AnyxmlStatementSupport INSTANCE = new AnyxmlStatementSupport();

    private AnyxmlStatementSupport() {
        super(YangStmtMapping.ANYXML);
    }

    public static AnyxmlStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected AnyxmlStatement createDeclared(final StmtContext<QName, AnyxmlStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularAnyxmlStatement(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected AnyxmlStatement createEmptyDeclared(final StmtContext<QName, AnyxmlStatement, ?> ctx) {
        return new EmptyAnyxmlStatement(ctx.coerceStatementArgument());
    }

    @Override
    protected AnyxmlEffectiveStatement createEffective(
            final StmtContext<QName, AnyxmlStatement, AnyxmlEffectiveStatement> ctx,
            final AnyxmlStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularAnyxmlEffectiveStatement(declared, ctx.getSchemaPath().get(),
            computeFlags(ctx, substatements), findOriginal(ctx),substatements);
    }

    @Override
    protected AnyxmlEffectiveStatement createEmptyEffective(
            final StmtContext<QName, AnyxmlStatement, AnyxmlEffectiveStatement> ctx, final AnyxmlStatement declared) {
        return new EmptyAnyxmlEffectiveStatement(declared, ctx.getSchemaPath().get(),
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

    private static @Nullable AnyxmlSchemaNode findOriginal(final StmtContext<?, ?, ?> ctx) {
        return (AnyxmlSchemaNode) ctx.getOriginalCtx().map(StmtContext::buildEffective).orElse(null);
    }
}
