/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.parser;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureArgument;
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureEffectiveStatement;
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureStatement;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class AugmentStructureStatementSupport
        extends AbstractStatementSupport<AugmentStructureArgument, AugmentStructureStatement,
                                         AugmentStructureEffectiveStatement> {

    private AugmentStructureStatementSupport(final YangParserConfiguration config,
            final SubstatementValidator validator) {
        super(AugmentStructureStatement.DEFINITION, StatementPolicy.reject(), config, validator);
    }

    static AugmentStructureStatementSupport rfc6020(final YangParserConfiguration config) {
        return new AugmentStructureStatementSupport(config,
            SubstatementValidator.builder(AugmentStructureStatement.DEFINITION)
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
            SubstatementValidator.builder(AugmentStructureStatement.DEFINITION)
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
    public AugmentStructureArgument parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        final var steps = ctx.identifierBinding().parseAbsoluteSchemaNodeid(ctx, value).getNodeIdentifiers();
        final var size = steps.size();
        final var descendant = size == 1 ? null : SchemaNodeIdentifier.Descendant.of(steps.subList(1, size));
        return new AugmentStructureArgument(steps.getFirst(), descendant);
    }

    @Override
    protected AugmentStructureStatement createDeclared(final BoundStmtCtx<AugmentStructureArgument> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return new AugmentStructureStatementImpl(ctx, substatements);
    }

    @Override
    protected AugmentStructureStatement attachDeclarationReference(final AugmentStructureStatement stmt,
            final DeclarationReference reference) {
        return new RefAugmentStructureStatement(stmt, reference);
    }

    @Override
    public boolean isIgnoringConfig() {
        return true;
    }

    @Override
    public void onStatementAdded(final Mutable<AugmentStructureArgument, AugmentStructureStatement,
            AugmentStructureEffectiveStatement> stmt) {
        if (stmt.coerceParentContext().getParentContext() != null) {
            throw new SourceException(stmt, "Structure augmentation may only be used as top-level statement");
        }

        final var argument = stmt.getArgument();
        final var descendant = argument.descendant();
        if (descendant == null) {
            // target is the structure itself, hook directly to modify it
            final var augmentAction = stmt.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
            augmentAction.requiresCtx(stmt, ModelProcessingPhase.EFFECTIVE_MODEL);
            augmentAction.apply(new AugmentStructureInference(stmt,
                augmentAction.mutatesEffectiveCtx(stmt, StructureStatementSupport.NAMESPACE, argument.structure())));
            return;
        }

        // Resolve the structure reference first
        final var lookupAction = stmt.newInferenceAction(ModelProcessingPhase.FULL_DECLARATION);
        final var structureReq = lookupAction.requiresCtx(stmt, StructureStatementSupport.NAMESPACE,
            argument.structure(), ModelProcessingPhase.FULL_DECLARATION);
        lookupAction.apply(new InferenceAction() {
            @Override
            public void apply(final InferenceContext ctx) {
                final var structure = structureReq.resolve(ctx);

                // target is the structure itself, hook directly to modify it
                final var augmentAction = stmt.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
                augmentAction.requiresCtx(stmt, ModelProcessingPhase.EFFECTIVE_MODEL);
                augmentAction.apply(new AugmentStructureInference(stmt,
                    augmentAction.mutatesEffectiveCtxPath(structure, ParserNamespaces.schemaTree(),
                        descendant.getNodeIdentifiers())));
            }

            @Override
            public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                throw new InferenceException(stmt, "Augment structure '%s' not found", stmt.getArgument().structure());
            }
        });
    }

    @Override
    protected AugmentStructureEffectiveStatement createEffective(
            final EffectiveStmtCtx.Current<AugmentStructureArgument, AugmentStructureStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        int flag = new EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder()
                .setHistory(stmt.history())
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .setConfiguration(stmt.effectiveConfig().asNullable())
                .toFlags();
        return new AugmentEffectiveStructureStatementImpl(stmt, substatements, flag);
    }
}
