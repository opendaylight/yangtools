/*
 * Copyright (c) 2017 OpenDaylight.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.parser.spi.GroupingNamespace;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.*;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.GroupingStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.LeafStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.ListStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.UsesStatementImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UsesStatementRfc7950Support extends UsesStatementImpl.Definition {

    private static final Logger LOG = LoggerFactory.getLogger(UsesStatementRfc7950Support.class);

    @Override
    public void onFullDefinitionDeclared(
            final StmtContext.Mutable<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> usesNode) {
        if (!StmtContextUtils.areFeaturesSupported(usesNode)) {
            return;
        }
        final SubstatementValidator validator = getSubstatementValidator();
        if (validator != null) {
            validator.validate(usesNode);
        }

        if (StmtContextUtils.isInExtensionBody(usesNode)) {
            return;
        }

        final ModelActionBuilder usesAction = usesNode.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
        final QName groupingName = usesNode.getStatementArgument();

        final ModelActionBuilder.Prerequisite<StmtContext<?, ?, ?>> sourceGroupingPre = usesAction.requiresCtx(usesNode,
                GroupingNamespace.class, groupingName, ModelProcessingPhase.EFFECTIVE_MODEL);
        final ModelActionBuilder.Prerequisite<? extends StmtContext.Mutable<?, ?, ?>> targetNodePre = usesAction.mutatesEffectiveCtx(
                usesNode.getParentContext());

        usesAction.apply(new ModelActionBuilder.InferenceAction() {

            @Override
            public void apply() {
                final StatementContextBase<?, ?, ?> targetNodeStmtCtx = (StatementContextBase<?, ?, ?>) targetNodePre.get();
                final StatementContextBase<?, ?, ?> sourceGrpStmtCtx = (StatementContextBase<?, ?, ?>) sourceGroupingPre.get();

                StatementContextBase<?, ?, ?> test = targetNodeStmtCtx.getParentContext();

                try {
                    disallowWhenAndIfFeatureOnKeys(usesNode);
                    UsesStatementImpl.copyFromSourceToTarget(sourceGrpStmtCtx, targetNodeStmtCtx, usesNode);
                    UsesStatementImpl.resolveUsesNode(usesNode, targetNodeStmtCtx);
                } catch (final SourceException e) {
                    LOG.warn(e.getMessage(), e);
                    throw e;
                }
            }

            @Override
            public void prerequisiteFailed(final Collection<? extends ModelActionBuilder.Prerequisite<?>> failed) {
                InferenceException.throwIf(failed.contains(sourceGroupingPre),
                        usesNode.getStatementSourceReference(), "Grouping '%s' was not resolved.", groupingName);
                throw new InferenceException("Unknown error occurred.", usesNode.getStatementSourceReference());
            }
        });
    }

    private void disallowWhenAndIfFeatureOnKeys(
            Mutable<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> usesNode) {
        final DeclaredStatement<?> parentCtx = usesNode.getParentContext().buildDeclared();

        if (parentCtx instanceof ListStatementImpl) {
            final List<String> keyNames = ((ListStatementImpl) parentCtx).getKey()
                    .argument().stream()
                    .map(qName -> new String(qName.getLastComponent().getLocalName()))
                    .collect(Collectors.toList());

            if (keyNames == null || keyNames.isEmpty()) {
                return;
            }

            // TO-DO: 1) Is getParentContext() the right way to get the grouping associated with uses statement?
            // 2) buildDeclared() won't give effective model, fix the logic to cover cases when key leaf is defined in
            // a grouping used by the current grouping.
            final DeclaredStatement<?> ctx = usesNode.getParentContext().getParentContext().buildDeclared();
            ctx.declaredSubstatements().forEach(declaredSubstatement -> {
                if (declaredSubstatement instanceof GroupingStatementImpl &&
                        ((GroupingStatementImpl) declaredSubstatement).getName()
                                .getLocalName().equals(usesNode.getStatementArgument())) {
                    ((GroupingStatementImpl) declaredSubstatement).declaredSubstatements()
                            .forEach(groupingSubstatement -> {
                        if (groupingSubstatement instanceof LeafStatementImpl &&
                                keyNames.contains(((LeafStatementImpl) groupingSubstatement).getName().getLocalName())) {
                            final Collection<? extends IfFeatureStatement> ifFeatures =
                                    ((LeafStatementImpl) declaredSubstatement).getIfFeatures();
                            if ((ifFeatures != null && !ifFeatures.isEmpty()) ||
                                    ((LeafStatementImpl) declaredSubstatement).getWhenStatement() != null) {
                                throw new InferenceException("RFC7950: Substatements if-feature and when are not allowed on " +
                                        "list keys.", usesNode.getStatementSourceReference());
                            }
                        }
                    });
                }
            });
        }
    }
}
