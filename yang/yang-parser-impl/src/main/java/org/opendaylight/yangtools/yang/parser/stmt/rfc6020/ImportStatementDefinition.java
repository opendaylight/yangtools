/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase.SOURCE_LINKAGE;
import static org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase.SOURCE_PRE_LINKAGE;
import static org.opendaylight.yangtools.yang.parser.stmt.reactor.StmtContextUtils.firstAttributeOf;

import com.google.common.base.Verify;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
import org.opendaylight.yangtools.yang.model.util.ModuleIdentifierImpl;
import org.opendaylight.yangtools.yang.parser.spi.ModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.PreLinkageModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.SemanticVersionModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.SemanticVersionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToSemVerModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ImportedModuleContext;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleIdentifier;
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
    public void onPreLinkageDeclared(final Mutable<String, ImportStatement, EffectiveStatement<String, ImportStatement>> stmt) {
        /*
         * Add ModuleIdentifier of a module which is required by this module.
         * Based on this information, required modules are searched from library
         * sources.
         */
        stmt.addRequiredModule(RevisionImport.getImportedModuleIdentifier(stmt));

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
            final ModuleIdentifier impIdentifier = getImportedModuleIdentifier(stmt);
            final ModelActionBuilder importAction = stmt.newInferenceAction(SOURCE_LINKAGE);
            final Prerequisite<StmtContext<?, ?, ?>> imported = importAction.requiresCtx(stmt, ModuleNamespace.class,
                    impIdentifier, SOURCE_LINKAGE);
            final Prerequisite<Mutable<?, ?, ?>> linkageTarget = importAction
                    .mutatesCtx(stmt.getRoot(), SOURCE_LINKAGE);

            importAction.apply(new InferenceAction() {
                @Override
                public void apply(final InferenceContext ctx) {
                    StmtContext<?, ?, ?> importedModule = null;
                    ModuleIdentifier importedModuleIdentifier = null;
                    if (impIdentifier.getRevision() == SimpleDateFormatUtil.DEFAULT_DATE_IMP) {
                        final Entry<ModuleIdentifier, StmtContext<?, ModuleStatement, EffectiveStatement<String, ModuleStatement>>> recentModuleEntry = findRecentModule(
                                impIdentifier, stmt.getAllFromNamespace(ModuleNamespace.class));
                        if (recentModuleEntry != null) {
                            importedModuleIdentifier = recentModuleEntry.getKey();
                            importedModule = recentModuleEntry.getValue();
                        }
                    }

                    if (importedModule == null || importedModuleIdentifier == null) {
                        importedModule = imported.resolve(ctx);
                        importedModuleIdentifier = impIdentifier;
                    }

                    linkageTarget.resolve(ctx).addToNs(ImportedModuleContext.class, importedModuleIdentifier, importedModule);
                    final String impPrefix = firstAttributeOf(stmt.declaredSubstatements(), PrefixStatement.class);
                    stmt.addToNs(ImpPrefixToModuleIdentifier.class, impPrefix, importedModuleIdentifier);

                    final URI modNs = firstAttributeOf(importedModule.declaredSubstatements(), NamespaceStatement.class);
                    stmt.addToNs(URIStringToImpPrefix.class, modNs.toString(), impPrefix);
                }

                @Override
                public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                    if (failed.contains(imported)) {
                        throw new InferenceException(stmt.getStatementSourceReference(),
                                "Imported module [%s] was not found.", impIdentifier);
                    }
                }
            });

        }

        private static Entry<ModuleIdentifier, StmtContext<?, ModuleStatement, EffectiveStatement<String, ModuleStatement>>> findRecentModule(
                final ModuleIdentifier impIdentifier,
                final Map<ModuleIdentifier, StmtContext<?, ModuleStatement, EffectiveStatement<String, ModuleStatement>>> allModules) {

            ModuleIdentifier recentModuleIdentifier = impIdentifier;
            Entry<ModuleIdentifier, StmtContext<?, ModuleStatement, EffectiveStatement<String, ModuleStatement>>> recentModuleEntry = null;

            for (final Entry<ModuleIdentifier, StmtContext<?, ModuleStatement, EffectiveStatement<String, ModuleStatement>>> moduleEntry : allModules
                    .entrySet()) {
                final ModuleIdentifier id = moduleEntry.getKey();

                if (id.getName().equals(impIdentifier.getName())
                        && id.getRevision().compareTo(recentModuleIdentifier.getRevision()) > 0) {
                    recentModuleIdentifier = id;
                    recentModuleEntry = moduleEntry;
                }
            }

            return recentModuleEntry;
        }

        private static ModuleIdentifier getImportedModuleIdentifier(final StmtContext<String, ImportStatement, ?> stmt) {
            Date revision = firstAttributeOf(stmt.declaredSubstatements(), RevisionDateStatement.class);
            if (revision == null) {
                revision = SimpleDateFormatUtil.DEFAULT_DATE_IMP;
            }

            return ModuleIdentifierImpl.create(stmt.getStatementArgument(), Optional.empty(), Optional.of(revision));
        }
    }

    private static class SemanticVersionImport {
        private SemanticVersionImport() {
            throw new UnsupportedOperationException("Utility class");
        }

        private static void onLinkageDeclared(
                final Mutable<String, ImportStatement, EffectiveStatement<String, ImportStatement>> stmt) {
            final ModuleIdentifier impIdentifier = getImportedModuleIdentifier(stmt);
            final ModelActionBuilder importAction = stmt.newInferenceAction(SOURCE_LINKAGE);
            final Prerequisite<StmtContext<?, ?, ?>> imported = importAction.requiresCtx(stmt, ModuleNamespace.class,
                    impIdentifier, SOURCE_LINKAGE);
            final Prerequisite<Mutable<?, ?, ?>> linkageTarget = importAction
                    .mutatesCtx(stmt.getRoot(), SOURCE_LINKAGE);

            importAction.apply(new InferenceAction() {
                @Override
                public void apply(final InferenceContext ctx) {
                    final Entry<SemVer, StmtContext<?, ?, ?>> importedModuleEntry= findRecentCompatibleModuleEntry(
                            impIdentifier.getName(), stmt);

                    StmtContext<?, ?, ?> importedModule = null;
                    ModuleIdentifier importedModuleIdentifier = null;
                    ModuleIdentifier semVerModuleIdentifier = null;
                    if (importedModuleEntry != null) {
                        importedModule = importedModuleEntry.getValue();
                        importedModuleIdentifier = importedModule.getFromNamespace(ModuleCtxToModuleIdentifier.class, importedModule);
                        semVerModuleIdentifier = createSemVerModuleIdentifier(importedModuleIdentifier, importedModuleEntry.getKey());
                    } else {
                        throw new InferenceException(stmt.getStatementSourceReference(),
                                "Unable to find module compatible with requested import [%s(%s)].", impIdentifier
                                        .getName(), getRequestedImportVersion(stmt));
                    }

                    linkageTarget.resolve(ctx).addToNs(ImportedModuleContext.class, importedModuleIdentifier, importedModule);
                    final String impPrefix = firstAttributeOf(stmt.declaredSubstatements(), PrefixStatement.class);
                    stmt.addToNs(ImpPrefixToModuleIdentifier.class, impPrefix, importedModuleIdentifier);
                    stmt.addToNs(ImpPrefixToSemVerModuleIdentifier.class, impPrefix, semVerModuleIdentifier);

                    final URI modNs = firstAttributeOf(importedModule.declaredSubstatements(), NamespaceStatement.class);
                    stmt.addToNs(URIStringToImpPrefix.class, modNs.toString(), impPrefix);
                }

                @Override
                public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                    if (failed.contains(imported)) {
                        throw new InferenceException(stmt.getStatementSourceReference(),
                                "Unable to find module compatible with requested import [%s(%s)].", impIdentifier
                                        .getName(), getRequestedImportVersion(stmt));
                    }
                }
            });
        }

        private static SemVer getRequestedImportVersion(final StmtContext<?, ?, ?> impStmt) {
            SemVer requestedImportVersion = impStmt.getFromNamespace(SemanticVersionNamespace.class, impStmt);
            if (requestedImportVersion == null) {
                requestedImportVersion = Module.DEFAULT_SEMANTIC_VERSION;
            }
            return requestedImportVersion;
        }

        private static Entry<SemVer, StmtContext<?, ?, ?>> findRecentCompatibleModuleEntry(final String moduleName,
                final StmtContext<String, ImportStatement, EffectiveStatement<String, ImportStatement>> impStmt) {
            NavigableMap<SemVer, StmtContext<?, ?, ?>> allRelevantModulesMap = impStmt.getFromNamespace(
                    SemanticVersionModuleNamespace.class, moduleName);
            if (allRelevantModulesMap == null) {
                return null;
            }

            final SemVer requestedImportVersion = getRequestedImportVersion(impStmt);
            allRelevantModulesMap = allRelevantModulesMap.subMap(requestedImportVersion, true,
                    SemVer.create(requestedImportVersion.getMajor() + 1), false);
            if (!allRelevantModulesMap.isEmpty()) {
                return allRelevantModulesMap.lastEntry();
            }

            return null;
        }

        private static ModuleIdentifier getImportedModuleIdentifier(final StmtContext<String, ImportStatement, ?> impStmt) {
            return ModuleIdentifierImpl.create(impStmt.getStatementArgument(), Optional.empty(),
                    Optional.of(SimpleDateFormatUtil.DEFAULT_DATE_IMP));
        }

        private static ModuleIdentifier createSemVerModuleIdentifier(final ModuleIdentifier importedModuleIdentifier,
                final SemVer semVer) {
            return ModuleIdentifierImpl.create(importedModuleIdentifier.getName(),
                Optional.ofNullable(importedModuleIdentifier.getNamespace()),
                Optional.of(importedModuleIdentifier.getRevision()), semVer);
        }
    }
}
