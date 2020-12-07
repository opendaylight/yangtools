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
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
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
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
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
        super(YangStmtMapping.ANYDATA, CopyPolicy.DECLARED_COPY);
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
        return new RegularAnydataStatement(ctx.getArgument(), substatements);
    }

    @Override
    protected AnydataStatement createEmptyDeclared(final StmtContext<QName, AnydataStatement, ?> ctx) {
        return new EmptyAnydataStatement(ctx.getArgument());
    }

    @Override
    protected AnydataEffectiveStatement createEffective(final Current<QName, AnydataStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final int flags = new FlagsBuilder()
            .setHistory(stmt.history())
            .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
            .setConfiguration(stmt.effectiveConfig().asNullable())
            .setMandatory(findFirstArgument(substatements, MandatoryEffectiveStatement.class, Boolean.FALSE))
            .toFlags();
        final SchemaPath path = stmt.wrapSchemaPath();

        return substatements.isEmpty()
            ? new EmptyAnydataEffectiveStatement(stmt.declared(), path, flags, findOriginal(stmt))
                : new RegularAnydataEffectiveStatement(stmt.declared(), path, flags, findOriginal(stmt), substatements);
    }

    private static @Nullable AnydataSchemaNode findOriginal(final Current<?, ?> stmt) {
        return (AnydataSchemaNode) stmt.original();
    }
}