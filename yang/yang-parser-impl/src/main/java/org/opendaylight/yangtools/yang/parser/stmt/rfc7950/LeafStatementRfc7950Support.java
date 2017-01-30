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
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.*;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.LeafStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.ListStatementImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class LeafStatementRfc7950Support extends LeafStatementImpl.Definition {

    private static final Logger LOG = LoggerFactory.getLogger(UsesStatementRfc7950Support.class);

    @Override
    public void onFullDefinitionDeclared(
            final StmtContext.Mutable<QName,LeafStatement,EffectiveStatement<QName,LeafStatement>> leaf) {
        super.onFullDefinitionDeclared(leaf);


        if (!StmtContextUtils.areFeaturesSupported(leaf)) {
            return;
        }
        if (StmtContextUtils.isInExtensionBody(leaf)) {
            return;
        }

        final ModelActionBuilder leafAction = leaf.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);

        leafAction.apply(new ModelActionBuilder.InferenceAction() {

            @Override
            public void apply() {
                try {
                    disallowWhenAndIfFeatureOnKeys(leaf);
                } catch (final SourceException e) {
                    LOG.warn(e.getMessage(), e);
                    throw e;
                }
            }

            @Override
            public void prerequisiteFailed(final Collection<? extends ModelActionBuilder.Prerequisite<?>> failed) {
                throw new InferenceException("Unknown error occurred.", leaf.getStatementSourceReference());
            }
        });
    }

    private void disallowWhenAndIfFeatureOnKeys(
            StmtContext.Mutable<QName, LeafStatement, EffectiveStatement<QName, LeafStatement>> leaf) {
        final DeclaredStatement<?> parentCtx = leaf.getParentContext().buildDeclared();
        final String leafName = leaf.getStatementArgument().getLocalName();

        if (parentCtx instanceof ListStatementImpl) {
            final List<String> keyNames = ((ListStatementImpl) parentCtx).getKey()
                    .argument().stream()
                    .map(qName -> new String(qName.getLastComponent().getLocalName()))
                    .collect(Collectors.toList());

            if (keyNames == null || keyNames.isEmpty() || !keyNames.contains(leafName)) {
                return;
            }

            leaf.declaredSubstatements().forEach(leafSubstatement -> {
                if (keyNames.contains(leafName) &&
                        (leafSubstatement instanceof IfFeatureStatement
                        || leafSubstatement instanceof WhenStatement)) {
                    throw new InferenceException("RFC7950: Substatements if-feature and when are not allowed on " +
                            "list keys.", leaf.getStatementSourceReference());
                }
            });
        }
    }
}
