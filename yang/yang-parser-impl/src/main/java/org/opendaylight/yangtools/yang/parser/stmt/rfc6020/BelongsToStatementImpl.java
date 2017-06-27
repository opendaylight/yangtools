/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.util.ModuleIdentifierImpl;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToModuleContext;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNamespaceForBelongsTo;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.BelongsToEffectiveStatementImpl;

public class BelongsToStatementImpl extends AbstractDeclaredStatement<String>
        implements BelongsToStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(YangStmtMapping.BELONGS_TO).addMandatory(YangStmtMapping.PREFIX).build();

    protected BelongsToStatementImpl(final StmtContext<String, BelongsToStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, BelongsToStatement, EffectiveStatement<String, BelongsToStatement>> {

        public Definition() {
            super(YangStmtMapping.BELONGS_TO);
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
        public void onLinkageDeclared(
                final StmtContext.Mutable<String, BelongsToStatement, EffectiveStatement<String, BelongsToStatement>> belongsToCtx) {
            ModelActionBuilder belongsToAction = belongsToCtx.newInferenceAction(ModelProcessingPhase.SOURCE_LINKAGE);

            final ModuleIdentifier belongsToModuleIdentifier = getModuleIdentifier(belongsToCtx);
            final ModelActionBuilder.Prerequisite<StmtContext<?, ?, ?>> belongsToPrereq = belongsToAction.requiresCtx(
                belongsToCtx, ModuleNamespaceForBelongsTo.class, belongsToModuleIdentifier.getName(),
                ModelProcessingPhase.SOURCE_LINKAGE);

            belongsToAction.apply(new InferenceAction() {
                @Override
                public void apply(final InferenceContext ctx) {
                    StmtContext<?, ?, ?> belongsToModuleCtx = belongsToPrereq.resolve(ctx);

                    belongsToCtx.addToNs(BelongsToModuleContext.class, belongsToModuleIdentifier, belongsToModuleCtx);
                    belongsToCtx.addToNs(BelongsToPrefixToModuleIdentifier.class,
                        StmtContextUtils.findFirstDeclaredSubstatement(belongsToCtx, PrefixStatement.class)
                        .getStatementArgument(), belongsToModuleIdentifier);
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

        private static ModuleIdentifier getModuleIdentifier(
                final StmtContext.Mutable<String, BelongsToStatement, EffectiveStatement<String, BelongsToStatement>> belongsToCtx) {
            String moduleName = belongsToCtx.getStatementArgument();
            return ModuleIdentifierImpl.create(moduleName, Optional.empty(),
                Optional.of(SimpleDateFormatUtil.DEFAULT_BELONGS_TO_DATE));
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Nonnull
    @Override
    public String getModule() {
        return argument();
    }

    @Nonnull
    @Override
    public PrefixStatement getPrefix() {
        return firstDeclared(PrefixStatement.class);
    }

}
