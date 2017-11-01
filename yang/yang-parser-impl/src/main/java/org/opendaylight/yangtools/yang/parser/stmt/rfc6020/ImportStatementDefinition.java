/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase.SOURCE_LINKAGE;
import static org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase.SOURCE_PRE_LINKAGE;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.findFirstDeclaredSubstatement;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Verify;
import java.net.URI;
import java.util.Collection;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SemVerSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.ModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.PreLinkageModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceKeyCriterion;
import org.opendaylight.yangtools.yang.parser.spi.meta.SemanticVersionModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.SemanticVersionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.ImportPrefixToModuleCtx;
import org.opendaylight.yangtools.yang.parser.spi.source.ImportPrefixToSemVerSourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ImportedModuleContext;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToSourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ImportEffectiveStatementImpl;

public class ImportStatementDefinition extends
        AbstractStatementSupport<String, ImportStatement, EffectiveStatement<String, ImportStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator
            .builder(YangStmtMapping.IMPORT)
            .addMandatory(YangStmtMapping.PREFIX)
            .addOptional(YangStmtMapping.REVISION_DATE)
            .addOptional(SupportedExtensionsMapping.OPENCONFIG_VERSION)
            .build();

    public ImportStatementDefinition() {
        super(YangStmtMapping.IMPORT);
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public ImportStatement createDeclared(final StmtContext<String, ImportStatement, ?> ctx) {
        return new ImportStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<String, ImportStatement> createEffective(
            final StmtContext<String, ImportStatement, EffectiveStatement<String, ImportStatement>> ctx) {
        return new ImportEffectiveStatementImpl(ctx);
    }

    @Override
    public void onPreLinkageDeclared(final Mutable<String, ImportStatement,
            EffectiveStatement<String, ImportStatement>> stmt) {
        /*
         * Add ModuleIdentifier of a module which is required by this module.
         * Based on this information, required modules are searched from library
         * sources.
         */
        stmt.addRequiredSource(RevisionImport.getImportedSourceIdentifier(stmt));

        final String moduleName = stmt.getStatementArgument();
        final ModelActionBuilder importAction = stmt.newInferenceAction(SOURCE_PRE_LINKAGE);
        final Prerequisite<StmtContext<?, ?, ?>> imported = importAction.requiresCtx(stmt,
                PreLinkageModuleNamespace.class, moduleName, SOURCE_PRE_LINKAGE);
        final Prerequisite<Mutable<?, ?, ?>> linkageTarget = importAction
                .mutatesCtx(stmt.getRoot(), SOURCE_PRE_LINKAGE);

        importAction.apply(new InferenceAction() {
            @Override
            public void apply(final InferenceContext ctx) {
                final StmtContext<?, ?, ?> importedModuleContext = imported.resolve(ctx);
                Verify.verify(moduleName.equals(importedModuleContext.getStatementArgument()));
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
    public void onLinkageDeclared(
            final Mutable<String, ImportStatement, EffectiveStatement<String, ImportStatement>> stmt) {
        if (stmt.isEnabledSemanticVersioning()) {
            SemanticVersionImport.onLinkageDeclared(stmt);
        } else {
            RevisionImport.onLinkageDeclared(stmt);
        }
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    private static class RevisionImport {

        private RevisionImport() {
            throw new UnsupportedOperationException("Utility class");
        }

        private static void onLinkageDeclared(
                final Mutable<String, ImportStatement, EffectiveStatement<String, ImportStatement>> stmt) {
            final ModelActionBuilder importAction = stmt.newInferenceAction(SOURCE_LINKAGE);
            final Prerequisite<StmtContext<?, ?, ?>> imported;
            final String moduleName = stmt.getStatementArgument();
            final Revision revision = firstAttributeOf(stmt.declaredSubstatements(), RevisionDateStatement.class);
            if (revision == null) {
                imported = importAction.requiresCtx(stmt, ModuleNamespace.class,
                    NamespaceKeyCriterion.latestRevisionModule(moduleName), SOURCE_LINKAGE);
            } else {
                imported = importAction.requiresCtx(stmt, ModuleNamespace.class,
                    RevisionSourceIdentifier.create(moduleName, Optional.of(revision)), SOURCE_LINKAGE);
            }

            final Prerequisite<Mutable<?, ?, ?>> linkageTarget = importAction.mutatesCtx(stmt.getRoot(),
                SOURCE_LINKAGE);

            importAction.apply(new InferenceAction() {
                @Override
                public void apply(final InferenceContext ctx) {
                    final StmtContext<?, ?, ?> importedModule = imported.resolve(ctx);

                    linkageTarget.resolve(ctx).addToNs(ImportedModuleContext.class,
                        stmt.getFromNamespace(ModuleCtxToSourceIdentifier.class, importedModule), importedModule);
                    final String impPrefix = firstAttributeOf(stmt.declaredSubstatements(), PrefixStatement.class);
                    final URI modNs = firstAttributeOf(importedModule.declaredSubstatements(),
                        NamespaceStatement.class);
                    stmt.addToNs(ImportPrefixToModuleCtx.class, impPrefix, importedModule);
                    stmt.addToNs(URIStringToImpPrefix.class, modNs.toString(), impPrefix);
                }

                @Override
                public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                    if (failed.contains(imported)) {
                        throw new InferenceException(stmt.getStatementSourceReference(),
                                "Imported module [%s] was not found.", moduleName);
                    }
                }
            });
        }

        static SourceIdentifier getImportedSourceIdentifier(final StmtContext<String, ImportStatement, ?> stmt) {
            final StmtContext<Revision, ?, ?> revision = findFirstDeclaredSubstatement(stmt,
                RevisionDateStatement.class);
            return revision == null ? RevisionSourceIdentifier.create(stmt.getStatementArgument())
                    : RevisionSourceIdentifier.create(stmt.getStatementArgument(), revision.getStatementArgument());
        }
    }

    private static class SemanticVersionImport {

        private abstract static class CompatibleCriterion extends NamespaceKeyCriterion<SemVerSourceIdentifier> {
            private final String moduleName;

            CompatibleCriterion(final String moduleName) {
                this.moduleName = requireNonNull(moduleName);
            }

            @Override
            public boolean match(final SemVerSourceIdentifier key) {
                return moduleName.equals(key.getName());
            }

            @Override
            protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
                return toStringHelper.add("moduleName", moduleName);
            }
        }

        private static final class NoVerCompatibleCriterion extends CompatibleCriterion {
            NoVerCompatibleCriterion(final String moduleName) {
                super(moduleName);
            }

            @Override
            public SemVerSourceIdentifier select(final SemVerSourceIdentifier first,
                    final SemVerSourceIdentifier second) {
                // TODO Auto-generated method stub
                return null;
            }
        }

        private static final class SemVerCompatibleCriterion extends CompatibleCriterion {
            private final SemVer semVer;

            SemVerCompatibleCriterion(final String moduleName, final SemVer semVer) {
                super(moduleName);
                this.semVer = requireNonNull(semVer);
            }

            @Override
            public boolean match(final SemVerSourceIdentifier key) {
                if (!super.match(key)) {
                    return false;
                }
                final Optional<SemVer> optKeyVer = key.getSemanticVersion();
                if (!optKeyVer.isPresent()) {
                    return false;
                }

                final SemVer keyVer = optKeyVer.get();
                if (semVer.getMajor() != keyVer.getMajor()) {
                    return false;
                }
                if (semVer.getMinor() > keyVer.getMinor()) {
                    return false;
                }
                return semVer.getMinor() < keyVer.getMinor() || semVer.getPatch() <= keyVer.getPatch();
            }

            @Override
            public SemVerSourceIdentifier select(final SemVerSourceIdentifier first,
                    final SemVerSourceIdentifier second) {
                return first.getSemanticVersion().get().compareTo(second.getSemanticVersion().get()) >= 0 ? first
                        : second;
            }

            @Override
            protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
                return super.addToStringAttributes(toStringHelper).add("version", semVer);
            }
        }


        private SemanticVersionImport() {
            throw new UnsupportedOperationException("Utility class");
        }

        private static void onLinkageDeclared(
                final Mutable<String, ImportStatement, EffectiveStatement<String, ImportStatement>> stmt) {
            final ModelActionBuilder importAction = stmt.newInferenceAction(SOURCE_LINKAGE);
            final String moduleName = stmt.getStatementArgument();
            final SemVer semanticVersion = stmt.getFromNamespace(SemanticVersionNamespace.class, stmt);
            final CompatibleCriterion criterion = semanticVersion == null ? new NoVerCompatibleCriterion(moduleName)
                    : new SemVerCompatibleCriterion(moduleName, semanticVersion);

            final Prerequisite<StmtContext<?, ?, ?>> imported = importAction.requiresCtx(stmt,
                SemanticVersionModuleNamespace.class, criterion, SOURCE_LINKAGE);
            final Prerequisite<Mutable<?, ?, ?>> linkageTarget = importAction.mutatesCtx(stmt.getRoot(),
                SOURCE_LINKAGE);

            importAction.apply(new InferenceAction() {
                @Override
                public void apply(final InferenceContext ctx) {
                    final StmtContext<?, ?, ?> importedModule = imported.resolve(ctx);
                    final SemVer importedVersion = stmt.getFromNamespace(SemanticVersionNamespace.class, stmt);
                    final SourceIdentifier importedModuleIdentifier = importedModule.getFromNamespace(
                        ModuleCtxToSourceIdentifier.class, importedModule);
                    final SemVerSourceIdentifier semVerModuleIdentifier = createSemVerModuleIdentifier(
                        importedModuleIdentifier, importedVersion);

                    linkageTarget.resolve(ctx).addToNs(ImportedModuleContext.class, importedModuleIdentifier,
                        importedModule);
                    final String impPrefix = firstAttributeOf(stmt.declaredSubstatements(), PrefixStatement.class);
                    stmt.addToNs(ImportPrefixToModuleCtx.class, impPrefix, importedModule);
                    stmt.addToNs(ImportPrefixToSemVerSourceIdentifier.class, impPrefix, semVerModuleIdentifier);

                    final URI modNs = firstAttributeOf(importedModule.declaredSubstatements(),
                        NamespaceStatement.class);
                    stmt.addToNs(URIStringToImpPrefix.class, modNs.toString(), impPrefix);
                }

                @Override
                public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                    if (failed.contains(imported)) {
                        throw new InferenceException(stmt.getStatementSourceReference(),
                                "Unable to find module compatible with requested import [%s(%s)].", moduleName,
                                getRequestedImportVersionString(stmt));
                    }
                }
            });
        }

        private static Optional<SemVer> getRequestedImportVersion(final StmtContext<?, ?, ?> stmt) {
            return Optional.ofNullable(stmt.getFromNamespace(SemanticVersionNamespace.class, stmt));
        }

        private static String getRequestedImportVersionString(final StmtContext<?, ?, ?> stmt) {
            return getRequestedImportVersion(stmt).map(SemVer::toString).orElse("<any>");
        }

        private static SemVerSourceIdentifier createSemVerModuleIdentifier(
                final SourceIdentifier importedModuleIdentifier, final SemVer semVer) {
            return SemVerSourceIdentifier.create(importedModuleIdentifier.getName(),
                importedModuleIdentifier.getRevision(), semVer);
        }
    }
}
