/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.parser;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc8791.model.api.StructureEffectiveStatement;
import org.opendaylight.yangtools.rfc8791.model.api.StructureStatement;
import org.opendaylight.yangtools.rfc8791.model.api.YangDataStructureStatements;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class StructureStatementSupport
        extends AbstractQNameStatementSupport<StructureStatement, StructureEffectiveStatement> {

    public StructureStatementSupport(final YangParserConfiguration config, final SubstatementValidator validator) {
        super(YangDataStructureStatements.STRUCTURE, StatementPolicy.reject(), config, validator);
    }

    static StructureStatementSupport rfc6020(final YangParserConfiguration config) {
        return new StructureStatementSupport(config,
            SubstatementValidator.builder(YangDataStructureStatements.STRUCTURE)
                .addAny(YangStmtMapping.MUST)
                .addOptional(YangStmtMapping.STATUS)
                .addOptional(YangStmtMapping.DESCRIPTION)
                .addOptional(YangStmtMapping.REFERENCE)
                .addAny(YangStmtMapping.TYPEDEF)
                .addAny(YangStmtMapping.GROUPING)
                .addAny(YangStmtMapping.CONTAINER)
                .addAny(YangStmtMapping.LEAF)
                .addAny(YangStmtMapping.LEAF_LIST)
                .addAny(YangStmtMapping.LIST)
                .addAny(YangStmtMapping.CHOICE)
                .addAny(YangStmtMapping.ANYXML)
                .addAny(YangStmtMapping.USES)
                .build());
    }

    static StructureStatementSupport rfc7950(final YangParserConfiguration config) {
        return new StructureStatementSupport(config,
            SubstatementValidator.builder(YangDataStructureStatements.STRUCTURE)
                .addAny(YangStmtMapping.MUST)
                .addOptional(YangStmtMapping.STATUS)
                .addOptional(YangStmtMapping.DESCRIPTION)
                .addOptional(YangStmtMapping.REFERENCE)
                .addAny(YangStmtMapping.TYPEDEF)
                .addAny(YangStmtMapping.GROUPING)
                .addAny(YangStmtMapping.CONTAINER)
                .addAny(YangStmtMapping.LEAF)
                .addAny(YangStmtMapping.LEAF_LIST)
                .addAny(YangStmtMapping.LIST)
                .addAny(YangStmtMapping.CHOICE)
                .addAny(YangStmtMapping.ANYDATA)
                .addAny(YangStmtMapping.ANYXML)
                .addAny(YangStmtMapping.USES)
                .build());
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    protected @NonNull StructureStatement createDeclared(@NonNull BoundStmtCtx<QName> ctx,
            @NonNull ImmutableList<DeclaredStatement<?>> substatements) {
        return new StructureStatementImpl(ctx.getArgument(), substatements);
    }

    @Override
    protected @NonNull StructureStatement attachDeclarationReference(@NonNull StructureStatement stmt,
            @NonNull DeclarationReference reference) {
        return new RefStructureStatement(stmt, reference);
    }

    @Override
    public boolean isIgnoringConfig() {
        return true;
    }

    @Override
    public void onStatementAdded(StmtContext.@NonNull Mutable<QName, StructureStatement,
            StructureEffectiveStatement> stmt) {
        if (stmt.coerceParentContext().getParentContext() != null) {
            stmt.setUnsupported();
        }
    }

    @Override
    protected @NonNull StructureEffectiveStatement createEffective(EffectiveStmtCtx.@NonNull Current<QName,
            StructureStatement> stmt, @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        int flag = new EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder()
                .setHistory(stmt.history())
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .setConfiguration(stmt.effectiveConfig().asNullable())
                .toFlags();
        return new StructureEffectiveStatementImpl(stmt, substatements, flag);
    }
}
