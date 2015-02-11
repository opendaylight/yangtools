package org.opendaylight.yangtools.yang.parser.base;

import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.SchemaNodeIdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelInferenceActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelInferenceActionBuilder.Prereq;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

class AugmentStatementImpl extends AbstractDeclaredStatement<SchemaNodeIdentifier> implements AugmentStatement {

    AugmentStatementImpl(StmtContext<SchemaNodeIdentifier, AugmentStatement,?> ctx) {
        super(ctx);
    }

    public static class Definition extends StatementSupport<SchemaNodeIdentifier, AugmentStatement, EffectiveStatement<SchemaNodeIdentifier,AugmentStatement>> {

        public Definition() {
            super(Rfc6020Mapping.Augment);
        }

        @Override
        public SchemaNodeIdentifier parseArgumentValue(StmtContext<?,?,?> ctx, String value) {
            // FIXME Auto-generated method stub
            return null;
        }

        @Override
        public void onStatementDeclarationFinished(StmtContext.Mutable<SchemaNodeIdentifier, AugmentStatement,EffectiveStatement<SchemaNodeIdentifier,AugmentStatement>> stmt) {
            ModelInferenceActionBuilder modifier = stmt.newInferenceAction();
            SchemaNodeIdentifier targetPath = stmt.getStatementArgument();
            final Prereq targetPre = modifier.mutatesEffectiveCtx(stmt,
                    SchemaNodeIdentifierNamespace.class, targetPath);
            final Prereq<EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>> definitionPre = modifier.requiresEffective(stmt);

            modifier.apply(new Runnable() {

                @Override
                public void run() {
                    StmtContext<?, ?> targetCtx = targetPre.get();
                    AugmentStatement definition = definitionPre.get();
                    // FIXME: Add copy of data
                    // copySubstatementsFrom(definition,targetCtx);
                }

            });
        }

        @Override
        public AugmentStatement createDeclared(StmtContext<SchemaNodeIdentifier, AugmentStatement,?> ctx) {
            return new AugmentStatementImpl(ctx);
        }

        @Override
        public AugmentStatement createEffective(StmtContext<SchemaNodeIdentifier, AugmentStatement> ctx) {
            return new AugmentStatementImpl(ctx);
        }

    }

    @Override
    public AugmentStatement get() {
        return this;
    }

    @Override
    public String getTargetNode() {
        return rawArgument();
    }

}
