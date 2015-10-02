/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase.FULL_DECLARATION;

import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.parser.spi.GroupingNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UsesEffectiveStatementImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UsesStatementImpl extends AbstractDeclaredStatement<QName> implements UsesStatement {

    private static final Logger LOG = LoggerFactory.getLogger(UsesStatementImpl.class);

    protected UsesStatementImpl(StmtContext<QName, UsesStatement, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractStatementSupport<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> {

        public Definition() {
            super(Rfc6020Mapping.USES);
        }

        @Override
        public QName parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return Utils.qNameFromArgument(ctx, value);
        }

        @Override
        public void onFullDefinitionDeclared(
                final StmtContext.Mutable<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> usesNode)
                throws SourceException {

            if(StmtContextUtils.isInExtensionBody(usesNode)) {
                return;
            }

            ModelActionBuilder usesAction = usesNode.newInferenceAction(FULL_DECLARATION);
            final QName groupingName = usesNode.getStatementArgument();

            final Prerequisite<StmtContext<?, ?, ?>> sourceGroupingPre = usesAction.requiresCtx(usesNode,
                    GroupingNamespace.class, groupingName, FULL_DECLARATION);
            final Prerequisite<? extends StmtContext.Mutable<?, ?, ?>> targetNodePre = usesAction.mutatesCtx(
                    usesNode.getParentContext(), FULL_DECLARATION);

            usesAction.apply(new InferenceAction() {

                @Override
                public void apply() throws InferenceException {
                    StatementContextBase<?, ?, ?> targetNodeStmtCtx = (StatementContextBase<?, ?, ?>) targetNodePre.get();
                    StatementContextBase<?, ?, ?> sourceGrpStmtCtx = (StatementContextBase<?, ?, ?>) sourceGroupingPre.get();

                    try {
                        GroupingUtils.copyFromSourceToTarget(sourceGrpStmtCtx, targetNodeStmtCtx, usesNode);
                        GroupingUtils.resolveUsesNode(usesNode, targetNodeStmtCtx);
                    } catch (SourceException e) {
                        LOG.warn(e.getMessage(), e);
                        throw e;
                    }
                }

                @Override
                public void prerequisiteFailed(Collection<? extends Prerequisite<?>> failed) throws InferenceException {
                    if (failed.contains(sourceGroupingPre)) {
                        throw new InferenceException("Grouping " + groupingName + " was not resolved.", usesNode
                                .getStatementSourceReference());
                    }
                    throw new InferenceException("Unknown error occurred.", usesNode.getStatementSourceReference());
                }
            });
        }

        @Override
        public UsesStatement createDeclared(StmtContext<QName, UsesStatement, ?> ctx) {
            return new UsesStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName, UsesStatement> createEffective(
                StmtContext<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> ctx) {
            return new UsesEffectiveStatementImpl(ctx);
        }

    }

    @Override
    public QName getName() {
        return argument();
    }

    @Override
    public WhenStatement getWhenStatement() {
        return firstDeclared(WhenStatement.class);
    }

    @Override
    public Collection<? extends IfFeatureStatement> getIfFeatures() {
        return allDeclared(IfFeatureStatement.class);
    }

    @Override
    public StatusStatement getStatus() {
        return firstDeclared(StatusStatement.class);
    }

    @Override
    public DescriptionStatement getDescription() {
        return firstDeclared(DescriptionStatement.class);
    }

    @Override
    public ReferenceStatement getReference() {
        return firstDeclared(ReferenceStatement.class);
    }

    @Override
    public Collection<? extends AugmentStatement> getAugments() {
        return allDeclared(AugmentStatement.class);
    }

    @Override
    public Collection<? extends RefineStatement> getRefines() {
        return allDeclared(RefineStatement.class);
    }
}
