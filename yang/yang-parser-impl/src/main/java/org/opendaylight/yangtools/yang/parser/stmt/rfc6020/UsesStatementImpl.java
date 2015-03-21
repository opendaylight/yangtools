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
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
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
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

public class UsesStatementImpl extends AbstractDeclaredStatement<QName> implements UsesStatement {

    protected UsesStatementImpl(StmtContext<QName, UsesStatement, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractStatementSupport<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> {

        public Definition() {
            super(Rfc6020Mapping.Uses);
        }

        @Override
        public QName parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return Utils.qNameFromArgument(ctx, value);
        }

        public void onStatementDeclared(Mutable<QName, UsesStatement, ?> usesNode) throws InferenceException {
            ModelActionBuilder modifier = usesNode.newInferenceAction(ModelProcessingPhase.EffectiveModel);
            final QName groupingName = usesNode.getStatementArgument();
            final StatementSourceReference usesSource = usesNode.getStatementSourceReference();
            final Prerequisite<?> targetPre = modifier.mutatesEffectiveCtx(usesNode.getParentContext());
            final Prerequisite<EffectiveStatement<QName, GroupingStatement>> sourcePre = modifier.requiresEffective(
                    usesNode, GroupingNamespace.class, groupingName);

            modifier.apply(new InferenceAction() {

                @Override
                public void apply() throws InferenceException {
                    Mutable<?, ?, ?> targetCtx = (Mutable<?, ?, ?>) targetPre.get();
                    EffectiveStatement<QName, GroupingStatement> source = sourcePre.get();

                    throw new UnsupportedOperationException("Copy of not not yet implemented.");
                }

                @Override
                public void prerequisiteFailed(Collection<? extends Prerequisite<?>> failed) throws InferenceException {
                    if(failed.contains(sourcePre)) {
                        throw new InferenceException("Grouping " + groupingName + "was not found.", usesSource);
                    }
                    throw new InferenceException("Unknown error occured.", usesSource);
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
            throw new UnsupportedOperationException("Not implemented yet.");
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

    @Override
    public QName argument() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String rawArgument() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<? extends DeclaredStatement<?>> declaredSubstatements() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StatementDefinition statementDefinition() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StatementSource getStatementSource() {
        // TODO Auto-generated method stub
        return null;
    }

}
