/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.import_;

import static org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase.SOURCE_LINKAGE;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.findFirstDeclaredSubstatement;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import java.util.Collection;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.ModuleQNameToPrefix;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceKeyCriterion;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

final class RevisionImport {
    private RevisionImport() {
        // Hidden on purpose
    }

    static void onLinkageDeclared(final Mutable<Unqualified, ImportStatement, ImportEffectiveStatement> stmt) {
        final ModelActionBuilder importAction = stmt.newInferenceAction(SOURCE_LINKAGE);
        final Prerequisite<StmtContext<?, ?, ?>> imported;
        final Unqualified moduleName = stmt.getArgument();
        final Revision revision = firstAttributeOf(stmt.declaredSubstatements(), RevisionDateStatement.class);
        if (revision == null) {
            imported = importAction.requiresCtx(stmt, ModuleNamespace.class,
                NamespaceKeyCriterion.latestRevisionModule(moduleName), SOURCE_LINKAGE);
        } else {
            imported = importAction.requiresCtx(stmt, ModuleNamespace.class, new SourceIdentifier(moduleName, revision),
                SOURCE_LINKAGE);
        }

        final Prerequisite<Mutable<?, ?, ?>> linkageTarget = importAction.mutatesCtx(stmt.getRoot(), SOURCE_LINKAGE);

        importAction.apply(new InferenceAction() {
            @Override
            public void apply(final InferenceContext ctx) {
                final StmtContext<?, ?, ?> importedModule = imported.resolve(ctx);

                final SourceIdentifier importedModuleIdentifier =
                    stmt.getFromNamespace(ModuleCtxToSourceIdentifier.class, importedModule);
                stmt.addToNs(ImportedVersionNamespace.class, Empty.value(), importedModuleIdentifier);

                final QNameModule mod = InferenceException.throwIfNull(stmt.getFromNamespace(
                    ModuleCtxToModuleQName.class, importedModule), stmt, "Failed to find module of %s", importedModule);

                linkageTarget.resolve(ctx).addToNs(ImportedModuleContext.class,
                    importedModuleIdentifier, importedModule);
                final String impPrefix = firstAttributeOf(stmt.declaredSubstatements(), PrefixStatement.class);
                stmt.addToNs(ImportPrefixToModuleCtx.class, impPrefix, importedModule);
                stmt.addToNs(ModuleQNameToPrefix.class, mod, impPrefix);
            }

            @Override
            public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                if (failed.contains(imported)) {
                    throw new InferenceException(stmt, "Imported module [%s] was not found.",
                        moduleName.getLocalName());
                }
            }
        });
    }

    static SourceIdentifier getImportedSourceIdentifier(final StmtContext<Unqualified, ImportStatement, ?> stmt) {
        final var revision = findFirstDeclaredSubstatement(stmt, RevisionDateStatement.class);
        return new SourceIdentifier(stmt.getArgument(), revision != null ? revision.getArgument() : null);
    }
}
