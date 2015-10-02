/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.parser.spi.source.StmtOrderingNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.AugmentEffectiveStatementImpl;

public class AugmentStatementImpl extends
        AbstractDeclaredStatement<SchemaNodeIdentifier> implements
        AugmentStatement {

    private static final Logger LOG = LoggerFactory
            .getLogger(AugmentStatementImpl.class);

    protected AugmentStatementImpl(
            StmtContext<SchemaNodeIdentifier, AugmentStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<SchemaNodeIdentifier, AugmentStatement, EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>> {

        public Definition() {
            super(Rfc6020Mapping.AUGMENT);
        }

        @Override
        public SchemaNodeIdentifier parseArgumentValue(
                StmtContext<?, ?, ?> ctx, String value) throws SourceException {
            return SchemaNodeIdentifier.create(
                    AugmentUtils.parseAugmentPath(ctx, value),
                    Utils.isXPathAbsolute(ctx, value));
        }

        @Override
        public AugmentStatement createDeclared(
                StmtContext<SchemaNodeIdentifier, AugmentStatement, ?> ctx) {
            return new AugmentStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<SchemaNodeIdentifier, AugmentStatement> createEffective(
                StmtContext<SchemaNodeIdentifier, AugmentStatement, EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>> ctx) {
            return new AugmentEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(
                final StmtContext.Mutable<SchemaNodeIdentifier, AugmentStatement, EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>> augmentNode)
                throws SourceException {

            if (StmtContextUtils.isInExtensionBody(augmentNode)) {
                return;
            }

            final ModelActionBuilder augmentAction = augmentNode
                    .newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
            final ModelActionBuilder.Prerequisite<StmtContext<SchemaNodeIdentifier, AugmentStatement, EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>>> sourceCtxPrereq = augmentAction
                    .requiresCtx(augmentNode, ModelProcessingPhase.EFFECTIVE_MODEL);
            final Prerequisite<Mutable<?, ?, EffectiveStatement<?, ?>>> target = augmentAction.mutatesEffectiveCtx(getSearchRoot(augmentNode), SchemaNodeIdentifierBuildNamespace.class, augmentNode.getStatementArgument());
            augmentAction.apply(new ModelActionBuilder.InferenceAction() {

                @Override
                public void apply() throws InferenceException {
                    final StatementContextBase<?, ?, ?> augmentTargetCtx = (StatementContextBase<?, ?, ?>) target.get();

                    if (!AugmentUtils.isSupportedAugmentTarget(augmentTargetCtx) || StmtContextUtils.isInExtensionBody(augmentTargetCtx)) {
                        augmentNode.setIsSupportedToBuildEffective(false);
                        return;
                    }
                    final StatementContextBase<?, ?, ?> augmentSourceCtx = (StatementContextBase<?, ?, ?>) augmentNode;
                    try {
                        AugmentUtils.copyFromSourceToTarget(augmentSourceCtx,
                                augmentTargetCtx);
                        augmentTargetCtx
                                .addEffectiveSubstatement(augmentSourceCtx);
                        updateAugmentOrder(augmentSourceCtx);
                    } catch (SourceException e) {
                        LOG.warn(e.getMessage(), e);
                    }

                }

                private void updateAugmentOrder(
                        final StatementContextBase<?, ?, ?> augmentSourceCtx) {
                    Integer currentOrder = augmentSourceCtx
                            .getFromNamespace(StmtOrderingNamespace.class,
                                    Rfc6020Mapping.AUGMENT);
                    if (currentOrder == null) {
                        currentOrder = 1;
                    } else {
                        currentOrder++;
                    }
                    augmentSourceCtx.setOrder(currentOrder);
                    augmentSourceCtx.addToNs(StmtOrderingNamespace.class,
                            Rfc6020Mapping.AUGMENT, currentOrder);
                }

                @Override
                public void prerequisiteFailed(
                        final Collection<? extends ModelActionBuilder.Prerequisite<?>> failed)
                        throws InferenceException {
                    throw new InferenceException("Augment target not found: "
                            + augmentNode.getStatementArgument(), augmentNode
                            .getStatementSourceReference());
                }
            });
        }

        private Mutable<?, ?, ?> getSearchRoot(Mutable<?, ?, ?> augmentContext) {
            Mutable<?, ?, ?> parent = augmentContext.getParentContext();
            // Augment is in uses - we need to augment instantiated nodes in parent.
            if(Rfc6020Mapping.USES.equals(parent.getPublicDefinition())) {
                return parent.getParentContext();
            }
            return parent;
        }
    }

    @Nonnull
    @Override
    public SchemaNodeIdentifier getTargetNode() {
        return argument();
    }

    @Override
    public Collection<? extends DataDefinitionStatement> getDataDefinitions() {
        return allDeclared(DataDefinitionStatement.class);
    }
}
