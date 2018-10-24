/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.belongs_to;

import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.findFirstDeclaredSubstatement;

import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToModuleContext;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleCtx;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNamespaceForBelongsTo;

public final class BelongsToStatementSupport extends
        AbstractStatementSupport<String, BelongsToStatement, EffectiveStatement<String, BelongsToStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(YangStmtMapping.BELONGS_TO).addMandatory(YangStmtMapping.PREFIX).build();
    private static final BelongsToStatementSupport INSTANCE = new BelongsToStatementSupport();

    private BelongsToStatementSupport() {
        super(YangStmtMapping.BELONGS_TO);
    }

    public static BelongsToStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public BelongsToStatement createDeclared(
            final StmtContext<String, BelongsToStatement, ?> ctx) {
        return new BelongsToStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<String, BelongsToStatement> createEffective(
            final StmtContext<String, BelongsToStatement, EffectiveStatement<String, BelongsToStatement>> ctx) {
        return new BelongsToEffectiveStatementImpl(ctx);
    }

    @Override
    public void onPreLinkageDeclared(final StmtContext.Mutable<String, BelongsToStatement,
            EffectiveStatement<String, BelongsToStatement>> belongsToCtx) {
        belongsToCtx.addRequiredSource(getSourceIdentifier(belongsToCtx));
    }

    @Override
    public void onLinkageDeclared(final Mutable<String, BelongsToStatement,
            EffectiveStatement<String, BelongsToStatement>> belongsToCtx) {
        ModelActionBuilder belongsToAction = belongsToCtx.newInferenceAction(ModelProcessingPhase.SOURCE_LINKAGE);

        final SourceIdentifier belongsToSourceIdentifier = getSourceIdentifier(belongsToCtx);
        final Prerequisite<StmtContext<?, ?, ?>> belongsToPrereq = belongsToAction.requiresCtx(belongsToCtx,
            ModuleNamespaceForBelongsTo.class, belongsToCtx.coerceStatementArgument(),
            ModelProcessingPhase.SOURCE_LINKAGE);

        belongsToAction.apply(new InferenceAction() {
            @Override
            public void apply(final InferenceContext ctx) {
                StmtContext<?, ?, ?> belongsToModuleCtx = belongsToPrereq.resolve(ctx);

                belongsToCtx.addToNs(BelongsToModuleContext.class, belongsToSourceIdentifier, belongsToModuleCtx);
                belongsToCtx.addToNs(BelongsToPrefixToModuleCtx.class,
                    findFirstDeclaredSubstatement(belongsToCtx, PrefixStatement.class).coerceStatementArgument(),
                    belongsToModuleCtx);
            }

            @Override
            public void prerequisiteFailed(final Collection<? extends ModelActionBuilder.Prerequisite<?>> failed) {
                if (failed.contains(belongsToPrereq)) {
                    throw new InferenceException(belongsToCtx.getStatementSourceReference(),
                        "Module '%s' from belongs-to was not found", belongsToCtx.getStatementArgument());
                }
            }
        });
    }

    private static SourceIdentifier getSourceIdentifier(final StmtContext<String, BelongsToStatement,
            EffectiveStatement<String, BelongsToStatement>> belongsToCtx) {
        return RevisionSourceIdentifier.create(belongsToCtx.coerceStatementArgument());
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}