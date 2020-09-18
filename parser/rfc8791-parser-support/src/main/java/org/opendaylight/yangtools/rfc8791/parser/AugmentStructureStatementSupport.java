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
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureEffectiveStatement;
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureStatement;
import org.opendaylight.yangtools.rfc8791.model.api.YangDataStructureStatements;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ArgumentUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class AugmentStructureStatementSupport
        extends AbstractStatementSupport<Absolute, AugmentStructureStatement, AugmentStructureEffectiveStatement> {

    private AugmentStructureStatementSupport(final YangParserConfiguration config,
            final SubstatementValidator validator) {
        super(YangDataStructureStatements.AUGMENT_STRUCTURE, StatementPolicy.reject(), config, validator);
    }

    static AugmentStructureStatementSupport rfc6020(final YangParserConfiguration config) {
        return new AugmentStructureStatementSupport(config,
            SubstatementValidator.builder(YangDataStructureStatements.AUGMENT_STRUCTURE)
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
                .addAny(YangStmtMapping.CASE)
                .build());
    }

    static AugmentStructureStatementSupport rfc7950(final YangParserConfiguration config) {
        return new AugmentStructureStatementSupport(config,
            SubstatementValidator.builder(YangDataStructureStatements.AUGMENT_STRUCTURE)
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
                .addAny(YangStmtMapping.CASE)
                .build());
    }

    @Override
    public Absolute parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        final SchemaNodeIdentifier arg = ArgumentUtils.nodeIdentifierFromPath(ctx, value);
        SourceException.throwIf(!(arg instanceof Absolute), ctx.sourceReference(),
            "Argument '%s' is not an absolute schema node identifier", value);
        return (Absolute) arg;
    }

    @Override
    protected @NonNull AugmentStructureStatement createDeclared(@NonNull BoundStmtCtx<Absolute> ctx,
            @NonNull ImmutableList<DeclaredStatement<?>> substatements) {
        return new AugmentStructureStatementImpl(ctx, substatements);
    }

    @Override
    protected @NonNull AugmentStructureStatement attachDeclarationReference(@NonNull AugmentStructureStatement stmt,
            @NonNull DeclarationReference reference) {
        return new RafAugmentStructureStatement(stmt, reference);
    }

    @Override
    public boolean isIgnoringConfig() {
        return true;
    }

    @Override
    public void onStatementAdded(StmtContext.@NonNull Mutable<Absolute, AugmentStructureStatement,
            AugmentStructureEffectiveStatement> stmt) {
        if (stmt.coerceParentContext().getParentContext() != null) {
            stmt.setUnsupported();
        }
    }

    @Override
    protected @NonNull AugmentStructureEffectiveStatement createEffective(EffectiveStmtCtx.@NonNull Current<Absolute,
            AugmentStructureStatement> stmt, @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        int flag = new EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder()
                .setHistory(stmt.history())
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .setConfiguration(stmt.effectiveConfig().asNullable())
                .toFlags();
        return new AugmentEffectiveStructureStatementImpl(stmt, substatements, flag);
    }
}
