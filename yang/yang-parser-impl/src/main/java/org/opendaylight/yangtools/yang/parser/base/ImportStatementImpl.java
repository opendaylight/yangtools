package org.opendaylight.yangtools.yang.parser.base;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import java.net.URI;
import java.text.ParseException;
import java.util.Date;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleIdentifierImpl;
import org.opendaylight.yangtools.yang.parser.spi.ModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelInferenceActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelInferenceActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelInferenceActionBuilder.Prereq;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;

public class ImportStatementImpl extends AbstractDeclaredStatement<String> implements ImportStatement {

    public static class Definition extends StatementSupport<String, ImportStatement,EffectiveStatement<String, ImportStatement>> {

        public Definition() {
            super(Rfc6020Mapping.Import);
        }

        @Override
        public String parseArgumentValue(StmtContext<?,?,?> ctx, String value) {
            return value;
        }

        @Override
        public ImportStatement createDeclared(StmtContext<String, ImportStatement,?> ctx) {
            return new ImportStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, ImportStatement> createEffective(StmtContext<String, ImportStatement,EffectiveStatement<String, ImportStatement>>  ctx) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void onStatementDeclarationFinished(final Mutable<String, ImportStatement,EffectiveStatement<String, ImportStatement>> stmt) {
            final ModuleIdentifier impIdentifier = getImportedModuleIdentifier(stmt);
            ModelInferenceActionBuilder importAction = stmt.newInferenceAction();
            final Prereq<StmtContext<?,ModuleStatement,?>> importedModule;
            final Prereq<Mutable<?, ?,?>> linkageTarget;
            final Prereq<Mutable<?, ?,?>> prefixTarget;
            importedModule = importAction.requiresDeclaredCtx(stmt, ModuleNamespace.class, impIdentifier);
            linkageTarget = importAction.mutatesNamespace(stmt.getRootContext(),ImportedModuleContext.class);
            prefixTarget = importAction.mutatesNamespace(stmt.getRootContext(), PrefixToModule.class);
            importAction.apply(new InferenceAction() {

                @Override
                public void apply() throws InferenceException {
                    linkageTarget.get().addToNamespace(ImportedModuleContext.class, impIdentifier, importedModule.get());
                    // Add prefix
                    prefixTarget.get();
                }

                @Override
                public void preconditionsWasNotMet(Iterable<Prereq<?>> failed) throws InferenceException {
                    if(Iterables.contains(failed, importedModule)) {
                        throw new InferenceException("Imported module was not found.", stmt.getSourceReference());
                    }
                }
            });
        }

        private ModuleIdentifier getImportedModuleIdentifier(Mutable<String, ImportStatement,?> stmt) {
            String moduleName = stmt.getStatementArgument();
            RevisionDateStatement revisionStmt = stmt.firstDeclaredOptional(RevisionDateStatement.class);
            final Optional<Date> revision;
            if(revisionStmt != null) {
                try {
                    revision = Optional.of(SimpleDateFormatUtil.getRevisionFormat().parse(revisionStmt.argument()));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            } else {
                revision = Optional.absent();
            }
            return new ModuleIdentifierImpl(moduleName, Optional.<URI>absent(), revision);
        }

    }

    ImportStatementImpl(StmtContext<String, ImportStatement,?> context) {
        super(context);
    }

    @Override
    public String getModule() {
        return rawArgument();
    }

    @Override
    public PrefixStatement getPrefix() {
        return firstDeclared(PrefixStatement.class);
    }

    @Override
    public RevisionDateStatement getRevisionDate() {
        return firstDeclared(RevisionDateStatement.class);
    }
}
