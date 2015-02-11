package org.opendaylight.yangtools.yang.parser.base;

import com.google.common.collect.Iterables;
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
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelInferenceActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelInferenceActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelInferenceActionBuilder.Prereq;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

public class UsesStatementImpl extends AbstractDeclaredStatement<QName> implements UsesStatement {

    protected UsesStatementImpl(StmtContext<QName, UsesStatement, ?> context) {
        super(context);
    }

    public static class Definition extends
            StatementSupport<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> {

        public Definition() {
            super(Rfc6020Mapping.Uses);
        }

        @Override
        public QName parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return Utils.qNameFromArgument(ctx, value);
        }

        public void onStatementDeclared(Mutable<QName, UsesStatement, ?> usesNode) {
            ModelInferenceActionBuilder modifier = usesNode.newInferenceAction();
            final Prereq<?> targetPre;
            final Prereq<EffectiveStatement<QName, GroupingStatement>> sourcePre;
            final QName groupingName = usesNode.getStatementArgument();
            final StatementSourceReference usesSource = usesNode.getSourceReference();
            targetPre = modifier.mutatesEffectiveCtx(usesNode.getParentContext());
            sourcePre = modifier.requiresEffective(usesNode, GroupingNamespace.class, groupingName);

            modifier.apply(new InferenceAction() {

                @Override
                public void apply() throws InferenceException {
                    Mutable<?, ?, ?> targetCtx = (Mutable<?, ?, ?>) targetPre.get();
                    EffectiveStatement<QName, GroupingStatement> source = sourcePre.get();

                    throw new UnsupportedOperationException("Copy of not not yet implemented.");
                }

                @Override
                public void preconditionsWasNotMet(Iterable<Prereq<?>> failed) throws InferenceException {
                    // TODO Auto-generated method stub
                    if(Iterables.contains(failed, sourcePre)) {
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
    public Iterable<? extends IfFeatureStatement> getIfFeatures() {
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
    public Iterable<? extends AugmentStatement> getAugments() {
        return allDeclared(AugmentStatement.class);
    }

    @Override
    public Iterable<? extends RefineStatement> getRefines() {
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
    public Iterable<? extends DeclaredStatement<?>> declaredSubstatements() {
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
