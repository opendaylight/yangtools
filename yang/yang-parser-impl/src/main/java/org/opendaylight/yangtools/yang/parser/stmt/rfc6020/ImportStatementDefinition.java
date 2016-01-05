/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase.SOURCE_LINKAGE;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;
import com.google.common.base.Optional;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleIdentifierImpl;
import org.opendaylight.yangtools.yang.parser.spi.ModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ImportEffectiveStatementImpl;

public class ImportStatementDefinition
        extends AbstractStatementSupport<String, ImportStatement, EffectiveStatement<String, ImportStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(Rfc6020Mapping.IMPORT)
            .add(Rfc6020Mapping.PREFIX, 1, 1).add(Rfc6020Mapping.REVISION_DATE, 0, 1).build();

    public ImportStatementDefinition() {
        super(Rfc6020Mapping.IMPORT);
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
    public void onFullDefinitionDeclared(
            final Mutable<String, ImportStatement, EffectiveStatement<String, ImportStatement>> stmt) {
        super.onFullDefinitionDeclared(stmt);
        SUBSTATEMENT_VALIDATOR.validate(stmt);
    }

    @Override
    public void onLinkageDeclared(
            final Mutable<String, ImportStatement, EffectiveStatement<String, ImportStatement>> stmt) {
        final ModuleIdentifier impIdentifier = getImportedModuleIdentifier(stmt);
        final ModelActionBuilder importAction = stmt.newInferenceAction(SOURCE_LINKAGE);
        final Prerequisite<StmtContext<?, ?, ?>> imported = importAction.requiresCtx(stmt, ModuleNamespace.class,
            impIdentifier, SOURCE_LINKAGE);
        final Prerequisite<Mutable<?, ?, ?>> linkageTarget = importAction.mutatesCtx(stmt.getRoot(), SOURCE_LINKAGE);

        importAction.apply(new InferenceAction() {
            @Override
            public void apply() {
                StmtContext<?, ?, ?> importedModule = null;
                ModuleIdentifier importedModuleIdentifier = null;
                if (impIdentifier.getRevision() == SimpleDateFormatUtil.DEFAULT_DATE_IMP) {
                    Entry<ModuleIdentifier, StmtContext<?, ModuleStatement, EffectiveStatement<String, ModuleStatement>>> recentModuleEntry =
                            findRecentModule(impIdentifier, stmt.getAllFromNamespace(ModuleNamespace.class));
                    if (recentModuleEntry != null) {
                        importedModuleIdentifier = recentModuleEntry.getKey();
                        importedModule = recentModuleEntry.getValue();
                    }
                }

                if (importedModule == null || importedModuleIdentifier == null) {
                    importedModule = imported.get();
                    importedModuleIdentifier = impIdentifier;
                }

                linkageTarget.get().addToNs(ImportedModuleContext.class, importedModuleIdentifier, importedModule);
                String impPrefix = firstAttributeOf(stmt.declaredSubstatements(), PrefixStatement.class);
                stmt.addToNs(ImpPrefixToModuleIdentifier.class, impPrefix, importedModuleIdentifier);

                final URI modNs = firstAttributeOf(importedModule.declaredSubstatements(), NamespaceStatement.class);
                stmt.addToNs(URIStringToImpPrefix.class, modNs.toString(), impPrefix);
            }

            private Entry<ModuleIdentifier, StmtContext<?, ModuleStatement, EffectiveStatement<String, ModuleStatement>>> findRecentModule(
                    final ModuleIdentifier impIdentifier,
                    final Map<ModuleIdentifier, StmtContext<?, ModuleStatement, EffectiveStatement<String, ModuleStatement>>> allModules) {

                ModuleIdentifier recentModuleIdentifier = impIdentifier;
                Entry<ModuleIdentifier, StmtContext<?, ModuleStatement, EffectiveStatement<String, ModuleStatement>>> recentModuleEntry = null;

                for (Entry<ModuleIdentifier, StmtContext<?, ModuleStatement, EffectiveStatement<String, ModuleStatement>>> moduleEntry : allModules.entrySet()) {
                    final ModuleIdentifier id = moduleEntry.getKey();

                    if (id.getName().equals(impIdentifier.getName())
                            && id.getRevision().compareTo(recentModuleIdentifier.getRevision()) > 0) {
                        recentModuleIdentifier = id;
                        recentModuleEntry = moduleEntry;
                    }
                }

                return recentModuleEntry;
            }

            @Override
            public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed)  {
                if (failed.contains(imported)) {
                    throw new InferenceException(stmt.getStatementSourceReference(),
                        "Imported module [%s] was not found.", impIdentifier);
                }
            }
        });


    }

    private static ModuleIdentifier getImportedModuleIdentifier(final Mutable<String, ImportStatement, ?> stmt) {
        Date revision = firstAttributeOf(stmt.declaredSubstatements(), RevisionDateStatement.class);
        if (revision == null) {
            revision = SimpleDateFormatUtil.DEFAULT_DATE_IMP;
        }

        return new ModuleIdentifierImpl(stmt.getStatementArgument(), Optional.<URI> absent(),
                Optional.<Date> of(revision));
    }

}