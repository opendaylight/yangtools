/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.import_;

import static org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase.SOURCE_PRE_LINKAGE;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.util.Collection;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateEffectiveStatement;
import org.opendaylight.yangtools.yang.model.repo.api.SemVerSourceIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.PreLinkageModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.ImportPrefixToSemVerSourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

abstract class AbstractImportStatementSupport
        extends BaseStringStatementSupport<ImportStatement, ImportEffectiveStatement> {
    AbstractImportStatementSupport() {
        super(YangStmtMapping.IMPORT);
    }

    @Override
    public final void onPreLinkageDeclared(final Mutable<String, ImportStatement, ImportEffectiveStatement> stmt) {
        /*
         * Add ModuleIdentifier of a module which is required by this module.
         * Based on this information, required modules are searched from library
         * sources.
         */
        stmt.addRequiredSource(RevisionImport.getImportedSourceIdentifier(stmt));

        final String moduleName = stmt.coerceStatementArgument();
        final ModelActionBuilder importAction = stmt.newInferenceAction(SOURCE_PRE_LINKAGE);
        final Prerequisite<StmtContext<?, ?, ?>> imported = importAction.requiresCtx(stmt,
                PreLinkageModuleNamespace.class, moduleName, SOURCE_PRE_LINKAGE);
        importAction.mutatesCtx(stmt.getRoot(), SOURCE_PRE_LINKAGE);

        importAction.apply(new InferenceAction() {
            @Override
            public void apply(final InferenceContext ctx) {
                final StmtContext<?, ?, ?> importedModuleContext = imported.resolve(ctx);
                Verify.verify(moduleName.equals(importedModuleContext.coerceRawStatementArgument()));
                final URI importedModuleNamespace = importedModuleContext.getFromNamespace(ModuleNameToNamespace.class,
                        moduleName);
                Verify.verifyNotNull(importedModuleNamespace);
                final String impPrefix = SourceException.throwIfNull(
                    firstAttributeOf(stmt.declaredSubstatements(), PrefixStatement.class),
                    stmt.getStatementSourceReference(), "Missing prefix statement");

                stmt.addToNs(ImpPrefixToNamespace.class, impPrefix, importedModuleNamespace);
            }

            @Override
            public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                InferenceException.throwIf(failed.contains(imported), stmt.getStatementSourceReference(),
                        "Imported module [%s] was not found.", moduleName);
            }
        });
    }

    @Override
    public final void onLinkageDeclared(final Mutable<String, ImportStatement, ImportEffectiveStatement> stmt) {
        if (stmt.isEnabledSemanticVersioning()) {
            SemanticVersionImport.onLinkageDeclared(stmt);
        } else {
            RevisionImport.onLinkageDeclared(stmt);
        }
    }

    @Override
    protected final ImportStatement createDeclared(final StmtContext<String, ImportStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new ImportStatementImpl(ctx, substatements);
    }

    @Override
    protected final ImportStatement createEmptyDeclared(final StmtContext<String, ImportStatement, ?> ctx) {
        throw new IllegalStateException("Unexpected empty declared import statement");
    }

    @Override
    protected final ImportEffectiveStatement createEffective(
            final StmtContext<String, ImportStatement, ImportEffectiveStatement> ctx, final ImportStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {

        final String prefix = declared.getPrefix().getValue();
        final SemVer semVer;
        final Revision revision;
        if (!ctx.isEnabledSemanticVersioning()) {
            final Optional<Revision> optRev = substatements.stream()
                    .filter(RevisionDateEffectiveStatement.class::isInstance)
                    .findFirst()
                    .map(stmt -> ((RevisionDateEffectiveStatement) stmt).argument());
            revision = optRev.isPresent() ? optRev.get() : getImportedRevision(ctx, declared.getModule(), prefix);
            semVer = null;
        } else {
            final SemVerSourceIdentifier importedModuleIdentifier = ctx.getFromNamespace(
                ImportPrefixToSemVerSourceIdentifier.class, prefix);
            revision = importedModuleIdentifier.getRevision().orElse(null);
            semVer = importedModuleIdentifier.getSemanticVersion().orElse(null);
        }

        return new ImportEffectiveStatementImpl(declared, substatements, revision, semVer);
    }

    @Override
    protected final ImportEffectiveStatement createEmptyEffective(
            final StmtContext<String, ImportStatement, ImportEffectiveStatement> ctx, final ImportStatement declared) {
        throw new IllegalStateException("Unexpected empty effective import statement");
    }

    private static Revision getImportedRevision(final StmtContext<String, ImportStatement, ?> ctx,
            final String moduleName, final String prefix) {
        // When 'revision-date' of an import is not specified in yang source, we need to find revision of imported
        // module.
        final QNameModule importedModule = StmtContextUtils.getModuleQNameByPrefix(ctx, prefix);
        SourceException.throwIfNull(importedModule, ctx.getStatementSourceReference(),
                "Unable to find import of module %s with prefix %s.", moduleName, prefix);
        return importedModule.getRevision().orElse(null);
    }
}
