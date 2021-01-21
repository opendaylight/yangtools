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
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
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
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
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
        super(YangStmtMapping.ANYXML, instantiatedPolicy());
    }

    public static AnyxmlStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected AnyxmlStatement createDeclared(final StmtContext<QName, AnyxmlStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularAnyxmlStatement(ctx.getArgument(), substatements);
    }

    @Override
    protected AnyxmlStatement createEmptyDeclared(final StmtContext<QName, AnyxmlStatement, ?> ctx) {
        return new EmptyAnyxmlStatement(ctx.getArgument());
    }

    @Override
    protected AnyxmlEffectiveStatement createEffective(final Current<QName, AnyxmlStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final int flags = new FlagsBuilder()
            .setHistory(stmt.history())
            .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
            .setConfiguration(stmt.effectiveConfig().asNullable())
            .setMandatory(findFirstArgument(substatements, MandatoryEffectiveStatement.class, Boolean.FALSE))
            .toFlags();
        final SchemaPath path = stmt.wrapSchemaPath();

        return substatements.isEmpty()
            ? new EmptyAnyxmlEffectiveStatement(stmt.declared(), path, flags, findOriginal(stmt))
                : new RegularAnyxmlEffectiveStatement(stmt.declared(), path, flags, findOriginal(stmt),substatements);
    }

    private static @Nullable AnyxmlSchemaNode findOriginal(final Current<?, ?> stmt) {
        return (AnyxmlSchemaNode) stmt.original();
    }
}
