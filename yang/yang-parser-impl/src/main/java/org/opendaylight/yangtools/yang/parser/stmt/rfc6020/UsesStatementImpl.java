/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

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
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UsesEffectiveStatementImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UsesStatementImpl extends AbstractDeclaredStatement<QName> implements UsesStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .USES)
            .addAny(Rfc6020Mapping.AUGMENT)
            .addOptional(Rfc6020Mapping.DESCRIPTION)
            .addAny(Rfc6020Mapping.IF_FEATURE)
            .addAny(Rfc6020Mapping.REFINE)
            .addOptional(Rfc6020Mapping.REFERENCE)
            .addOptional(Rfc6020Mapping.STATUS)
            .addOptional(Rfc6020Mapping.WHEN)
            .build();

    private static final Logger LOG = LoggerFactory.getLogger(UsesStatementImpl.class);

    protected UsesStatementImpl(final StmtContext<QName, UsesStatement, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractStatementSupport<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> {

        public Definition() {
            super(Rfc6020Mapping.USES);
        }

        @Override
        public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return Utils.qNameFromArgument(ctx, value);
        }

        @Override
        public void onFullDefinitionDeclared(
                final StmtContext.Mutable<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> usesNode) {
            if (!StmtContextUtils.areFeaturesSupported(usesNode)) {
                return;
            }

            SUBSTATEMENT_VALIDATOR.validate(usesNode);

            if (StmtContextUtils.isInExtensionBody(usesNode)) {
                return;
            }

            ModelActionBuilder usesAction = usesNode.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
            final QName groupingName = usesNode.getStatementArgument();

            final Prerequisite<StmtContext<?, ?, ?>> sourceGroupingPre = usesAction.requiresCtx(usesNode,
                    GroupingNamespace.class, groupingName, ModelProcessingPhase.EFFECTIVE_MODEL);
            final Prerequisite<? extends StmtContext.Mutable<?, ?, ?>> targetNodePre = usesAction.mutatesEffectiveCtx(
                    usesNode.getParentContext());

            usesAction.apply(new InferenceAction() {

                @Override
                public void apply() {
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
                public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                    InferenceException.throwIf(failed.contains(sourceGroupingPre),
                            usesNode.getStatementSourceReference(), "Grouping '%s' was not resolved.", groupingName);
                    throw new InferenceException("Unknown error occurred.", usesNode.getStatementSourceReference());
                }
            });
        }

        @Override
        public UsesStatement createDeclared(final StmtContext<QName, UsesStatement, ?> ctx) {
            return new UsesStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName, UsesStatement> createEffective(
                final StmtContext<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> ctx) {
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
