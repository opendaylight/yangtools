/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.import_;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase.SOURCE_PRE_LINKAGE;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigStatements;
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
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.ImportPrefixToSemVerSourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class ImportStatementSupport
        extends BaseStringStatementSupport<ImportStatement, ImportEffectiveStatement> {
    private static final @NonNull ImportStatementSupport RFC6020_INSTANCE = new ImportStatementSupport(
        SubstatementValidator.builder(YangStmtMapping.IMPORT)
            .addMandatory(YangStmtMapping.PREFIX)
            .addOptional(YangStmtMapping.REVISION_DATE)
            .addOptional(OpenConfigStatements.OPENCONFIG_VERSION)
            .build());
    private static final @NonNull ImportStatementSupport RFC7950_INSTANCE = new ImportStatementSupport(
        SubstatementValidator.builder(YangStmtMapping.IMPORT)
            .addMandatory(YangStmtMapping.PREFIX)
            .addOptional(YangStmtMapping.REVISION_DATE)
            .addOptional(OpenConfigStatements.OPENCONFIG_VERSION)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addOptional(YangStmtMapping.REFERENCE)
            .build());

    private final SubstatementValidator validator;

    private ImportStatementSupport(final SubstatementValidator validator) {
        super(YangStmtMapping.IMPORT, CopyPolicy.REJECT);
        this.validator = requireNonNull(validator);
    }

    public static @NonNull ImportStatementSupport rfc6020Instance() {
        return RFC6020_INSTANCE;
    }

    public static @NonNull ImportStatementSupport rfc7950Instance() {
        return RFC7950_INSTANCE;
    }

    @Override
    public void onPreLinkageDeclared(final Mutable<String, ImportStatement, ImportEffectiveStatement> stmt) {
        /*
         * Add ModuleIdentifier of a module which is required by this module.
         * Based on this information, required modules are searched from library
         * sources.
         */
        stmt.addRequiredSource(RevisionImport.getImportedSourceIdentifier(stmt));

        final String moduleName = stmt.getArgument();
        final ModelActionBuilder importAction = stmt.newInferenceAction(SOURCE_PRE_LINKAGE);
        final Prerequisite<StmtContext<?, ?, ?>> imported = importAction.requiresCtx(stmt,
                PreLinkageModuleNamespace.class, moduleName, SOURCE_PRE_LINKAGE);
        importAction.mutatesCtx(stmt.getRoot(), SOURCE_PRE_LINKAGE);

        importAction.apply(new InferenceAction() {
            @Override
            public void apply(final InferenceContext ctx) {
                final StmtContext<?, ?, ?> importedModuleContext = imported.resolve(ctx);
                verify(moduleName.equals(importedModuleContext.getRawArgument()));
                final URI importedModuleNamespace = verifyNotNull(
                    importedModuleContext.getFromNamespace(ModuleNameToNamespace.class, moduleName));
                final String impPrefix = SourceException.throwIfNull(
                    firstAttributeOf(stmt.declaredSubstatements(), PrefixStatement.class), stmt,
                    "Missing prefix statement");

                stmt.addToNs(ImpPrefixToNamespace.class, impPrefix, importedModuleNamespace);
            }

            @Override
            public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                InferenceException.throwIf(failed.contains(imported), stmt, "Imported module [%s] was not found.",
                    moduleName);
            }
        });
    }

    @Override
    public void onLinkageDeclared(final Mutable<String, ImportStatement, ImportEffectiveStatement> stmt) {
        if (stmt.isEnabledSemanticVersioning()) {
            SemanticVersionImport.onLinkageDeclared(stmt);
        } else {
            RevisionImport.onLinkageDeclared(stmt);
        }
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }

    @Override
    protected ImportStatement createDeclared(final StmtContext<String, ImportStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new ImportStatementImpl(ctx.getRawArgument(), substatements);
    }

    @Override
    protected ImportStatement createEmptyDeclared(final StmtContext<String, ImportStatement, ?> ctx) {
        throw new IllegalStateException("Unexpected empty declared import statement");
    }

    @Override
    protected ImportEffectiveStatement createEffective(final Current<String, ImportStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        checkState(!substatements.isEmpty(), "Unexpected empty effective import statement");

        final ImportStatement declared = stmt.declared();
        final String prefix = declared.getPrefix().getValue();
        final SemVer semVer;
        final Revision revision;
        final var rabbit = stmt.caerbannog();
        if (!rabbit.isEnabledSemanticVersioning()) {
            final Optional<Revision> optRev = substatements.stream()
                    .filter(RevisionDateEffectiveStatement.class::isInstance)
                    .findFirst()
                    .map(subStmt -> ((RevisionDateEffectiveStatement) subStmt).argument());
            revision = optRev.isPresent() ? optRev.get() : getImportedRevision(rabbit, declared.getModule(), prefix);
            semVer = null;
        } else {
            final SemVerSourceIdentifier importedModuleIdentifier = stmt.getFromNamespace(
                    ImportPrefixToSemVerSourceIdentifier.class, prefix);
            revision = importedModuleIdentifier.getRevision().orElse(null);
            semVer = importedModuleIdentifier.getSemanticVersion().orElse(null);
        }

        return new ImportEffectiveStatementImpl(declared, substatements, revision, semVer);
    }

    private static Revision getImportedRevision(final StmtContext<String, ImportStatement, ?> ctx,
            final String moduleName, final String prefix) {
        // When 'revision-date' of an import is not specified in yang source, we need to find revision of imported
        // module.
        final QNameModule importedModule = SourceException.throwIfNull(
            StmtContextUtils.getModuleQNameByPrefix(ctx, prefix), ctx,
            "Unable to find import of module %s with prefix %s.", moduleName, prefix);
        return importedModule.getRevision().orElse(null);
    }
}
