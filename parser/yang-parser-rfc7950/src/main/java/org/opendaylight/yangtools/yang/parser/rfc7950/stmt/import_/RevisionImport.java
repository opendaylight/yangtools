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
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
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
        final var importAction = stmt.newInferenceAction(SOURCE_LINKAGE);
        final var moduleName = stmt.getArgument();
        final var revision = firstAttributeOf(stmt.declaredSubstatements(), RevisionDateStatement.class);
        final Prerequisite<StmtContext<Unqualified, ModuleStatement, ModuleEffectiveStatement>> imported;
        if (revision == null) {
            imported = importAction.requiresCtx(stmt, ParserNamespaces.MODULE,
                NamespaceKeyCriterion.latestRevisionModule(moduleName), SOURCE_LINKAGE);
        } else {
            imported = importAction.requiresCtx(stmt, ParserNamespaces.MODULE,
                new SourceIdentifier(moduleName, revision), SOURCE_LINKAGE);
        }

        final var linkageTarget = importAction.mutatesCtx(stmt.getRoot(), SOURCE_LINKAGE);

        importAction.apply(new InferenceAction() {
            @Override
            public void apply(final InferenceContext ctx) {
                final var importedModule = imported.resolve(ctx);

                linkageTarget.resolve(ctx).addToNs(ParserNamespaces.IMPORTED_MODULE,
                    stmt.namespaceItem(ParserNamespaces.MODULECTX_TO_SOURCE, importedModule), importedModule);
                final var impPrefix = firstAttributeOf(stmt.declaredSubstatements(), PrefixStatement.class);
                stmt.addToNs(ParserNamespaces.IMPORT_PREFIX_TO_MODULECTX, impPrefix, importedModule);
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
        final var revision = findFirstDeclaredSubstatement(stmt, RevisionDateStatement.DEF);
        return new SourceIdentifier(stmt.getArgument(), revision != null ? revision.getArgument() : null);
    }
}
