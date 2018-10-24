/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.import_;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase.SOURCE_LINKAGE;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.net.URI;
import java.util.Collection;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.repo.api.SemVerSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.URIStringToImportPrefix;
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
import org.opendaylight.yangtools.yang.parser.spi.source.ImportPrefixToModuleCtx;
import org.opendaylight.yangtools.yang.parser.spi.source.ImportPrefixToSemVerSourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ImportedModuleContext;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToSourceIdentifier;

final class SemanticVersionImport {

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

    private static final class NoVerCompatibleCriterion extends SemanticVersionImport.CompatibleCriterion {
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

    private static final class SemVerCompatibleCriterion extends SemanticVersionImport.CompatibleCriterion {
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

    static void onLinkageDeclared(
            final Mutable<String, ImportStatement, EffectiveStatement<String, ImportStatement>> stmt) {
        final ModelActionBuilder importAction = stmt.newInferenceAction(SOURCE_LINKAGE);
        final String moduleName = stmt.coerceStatementArgument();
        final SemVer semanticVersion = stmt.getFromNamespace(SemanticVersionNamespace.class, stmt);
        final SemanticVersionImport.CompatibleCriterion criterion = semanticVersion == null
                ? new NoVerCompatibleCriterion(moduleName)
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
                stmt.addToNs(URIStringToImportPrefix.class, modNs.toString(), impPrefix);
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