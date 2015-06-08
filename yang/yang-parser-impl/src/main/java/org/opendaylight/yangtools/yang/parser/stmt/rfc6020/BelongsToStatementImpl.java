/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * <p/>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.base.Optional;
import java.net.URI;
import java.util.Collection;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleIdentifierImpl;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToModuleContext;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNamespaceForBelongsTo;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.BelongsEffectiveToStatementImpl;

public class BelongsToStatementImpl extends AbstractDeclaredStatement<String>
        implements BelongsToStatement {

    protected BelongsToStatementImpl(
            StmtContext<String, BelongsToStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, BelongsToStatement, EffectiveStatement<String, BelongsToStatement>> {

        public Definition() {
            super(Rfc6020Mapping.BELONGS_TO);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return value;
        }

        @Override
        public BelongsToStatement createDeclared(
                StmtContext<String, BelongsToStatement, ?> ctx) {
            return new BelongsToStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, BelongsToStatement> createEffective(
                StmtContext<String, BelongsToStatement, EffectiveStatement<String, BelongsToStatement>> ctx) {
            return new BelongsEffectiveToStatementImpl(ctx);
        }

        @Override
        public void onLinkageDeclared(final StmtContext.Mutable<String, BelongsToStatement, EffectiveStatement<String, BelongsToStatement>> belongsToCtx) throws SourceException {
            ModelActionBuilder belongsToAction = belongsToCtx.newInferenceAction(ModelProcessingPhase.SOURCE_LINKAGE);

            final ModuleIdentifier belongsToModuleIdentifier = getModuleIdentifier(belongsToCtx);
            final ModelActionBuilder.Prerequisite<StmtContext<?, ?, ?>> belongsToPrereq = belongsToAction.requiresCtx(belongsToCtx, ModuleNamespaceForBelongsTo.class, belongsToModuleIdentifier.getName(), ModelProcessingPhase.SOURCE_LINKAGE);

            belongsToAction.apply(new InferenceAction() {

                @Override
                public void apply() throws InferenceException {
                    StmtContext<?, ?, ?> belongsToModuleCtx = belongsToPrereq.get();

                    belongsToCtx.addToNs(BelongsToModuleContext.class, belongsToModuleIdentifier,
                            belongsToModuleCtx);
                }

                @Override
                public void prerequisiteFailed(Collection<? extends ModelActionBuilder.Prerequisite<?>> failed) throws InferenceException {
                    if (failed.contains(belongsToPrereq)) {
                        throw new InferenceException("Module from belongs-to was not found: " + belongsToCtx.getStatementArgument(), belongsToCtx
                                .getStatementSourceReference());
                    }
                }
            });
        }

        private ModuleIdentifier getModuleIdentifier(StmtContext.Mutable<String, BelongsToStatement, EffectiveStatement<String, BelongsToStatement>> belongsToCtx) {
            String moduleName = belongsToCtx.getStatementArgument();
            return new ModuleIdentifierImpl(moduleName, Optional.<URI> absent(), Optional.of(SimpleDateFormatUtil.DEFAULT_BELONGS_TO_DATE));
        }
    }

    @Override
    public String getModule() {
        return argument();
    }

    @Override
    public PrefixStatement getPrefix() {
        return firstDeclared(PrefixStatement.class);
    }

}
