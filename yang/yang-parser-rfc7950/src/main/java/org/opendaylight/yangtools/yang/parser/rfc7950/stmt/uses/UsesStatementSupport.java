/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.uses;

import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.parser.spi.GroupingNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;

public final class UsesStatementSupport extends
        AbstractQNameStatementSupport<UsesStatement, EffectiveStatement<QName, UsesStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .USES)
        .addAny(YangStmtMapping.AUGMENT)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.IF_FEATURE)
        .addAny(YangStmtMapping.REFINE)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .addOptional(YangStmtMapping.WHEN)
        .build();
    private static final UsesStatementSupport INSTANCE = new UsesStatementSupport();

    private UsesStatementSupport() {
        super(YangStmtMapping.USES);
    }

    public static UsesStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseNodeIdentifier(ctx, value);
    }

    @Override
    public void onFullDefinitionDeclared(
            final Mutable<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> usesNode) {
        if (!usesNode.isSupportedByFeatures()) {
            return;
        }
        super.onFullDefinitionDeclared(usesNode);

        final ModelActionBuilder usesAction = usesNode.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
        final QName groupingName = usesNode.getStatementArgument();

        final Prerequisite<StmtContext<?, ?, ?>> sourceGroupingPre = usesAction.requiresCtx(usesNode,
                GroupingNamespace.class, groupingName, ModelProcessingPhase.EFFECTIVE_MODEL);
        final Prerequisite<? extends StmtContext.Mutable<?, ?, ?>> targetNodePre = usesAction.mutatesEffectiveCtx(
                usesNode.getParentContext());

        usesAction.apply(new InferenceAction() {

            @Override
            public void apply(final InferenceContext ctx) {
                final StatementContextBase<?, ?, ?> targetNodeStmtCtx =
                        (StatementContextBase<?, ?, ?>) targetNodePre.resolve(ctx);
                final StatementContextBase<?, ?, ?> sourceGrpStmtCtx =
                        (StatementContextBase<?, ?, ?>) sourceGroupingPre.resolve(ctx);

                UsesStatementImpl.copyFromSourceToTarget(sourceGrpStmtCtx, targetNodeStmtCtx, usesNode);
                UsesStatementImpl.resolveUsesNode(usesNode, targetNodeStmtCtx);
                StmtContextUtils.validateIfFeatureAndWhenOnListKeys(usesNode);
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

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}