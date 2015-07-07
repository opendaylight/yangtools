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

import java.util.Set;

import java.util.Map.Entry;
import java.util.Map;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import com.google.common.base.Optional;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleIdentifierImpl;
import org.opendaylight.yangtools.yang.parser.spi.ModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ImportEffectiveStatementImpl;

public class ImportStatementDefinition
        extends
        AbstractStatementSupport<String, ImportStatement, EffectiveStatement<String, ImportStatement>> {

    public ImportStatementDefinition() {
        super(Rfc6020Mapping.IMPORT);
    }

    @Override
    public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
        return value;
    }

    @Override
    public ImportStatement createDeclared(
            StmtContext<String, ImportStatement, ?> ctx) {
        return new ImportStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<String, ImportStatement> createEffective(
            StmtContext<String, ImportStatement, EffectiveStatement<String, ImportStatement>> ctx) {
        return new ImportEffectiveStatementImpl(ctx);
    }

    @Override
    public void onLinkageDeclared(
            final Mutable<String, ImportStatement, EffectiveStatement<String, ImportStatement>> stmt)
            throws SourceException {
        final ModuleIdentifier impIdentifier = getImportedModuleIdentifier(stmt);
        ModelActionBuilder importAction = stmt
                .newInferenceAction(SOURCE_LINKAGE);
        final Prerequisite<StmtContext<?, ?, ?>> imported;
        final Prerequisite<Mutable<?, ?, ?>> linkageTarget;
        imported = importAction.requiresCtx(stmt, ModuleNamespace.class,
                impIdentifier, SOURCE_LINKAGE);
        linkageTarget = importAction.mutatesCtx(stmt.getRoot(), SOURCE_LINKAGE);

        importAction.apply(new InferenceAction() {

            @Override
            public void apply() throws InferenceException {
                StmtContext<?, ?, ?> importedModule = null;
                ModuleIdentifier importedModuleIdentifier = null;
                if (impIdentifier.getRevision() == SimpleDateFormatUtil.DEFAULT_DATE_IMP) {
                    Entry<ModuleIdentifier, StmtContext<?, ModuleStatement, EffectiveStatement<String, ModuleStatement>>> recentModuleEntry = findRecentModule(
                            impIdentifier,
                            stmt.getAllFromNamespace(ModuleNamespace.class));
                    if (recentModuleEntry != null) {
                        importedModuleIdentifier = recentModuleEntry.getKey();
                        importedModule = recentModuleEntry.getValue();
                    }
                }

                if(importedModule == null || importedModuleIdentifier == null) {
                    importedModule = imported.get();
                    importedModuleIdentifier = impIdentifier;
                }

                linkageTarget.get().addToNs(ImportedModuleContext.class,
                        importedModuleIdentifier, importedModule);
                String impPrefix = firstAttributeOf(stmt.declaredSubstatements(),
                        PrefixStatement.class);
                stmt.addToNs(ImpPrefixToModuleIdentifier.class, impPrefix,
                        importedModuleIdentifier);
            }

            private Entry<ModuleIdentifier, StmtContext<?, ModuleStatement, EffectiveStatement<String, ModuleStatement>>> findRecentModule(
                    ModuleIdentifier impIdentifier,
                    Map<ModuleIdentifier, StmtContext<?, ModuleStatement, EffectiveStatement<String, ModuleStatement>>> allModules) {

                ModuleIdentifier recentModuleIdentifier = impIdentifier;
                Entry<ModuleIdentifier, StmtContext<?, ModuleStatement, EffectiveStatement<String, ModuleStatement>>> recentModuleEntry = null;

                Set<Entry<ModuleIdentifier, StmtContext<?, ModuleStatement, EffectiveStatement<String, ModuleStatement>>>> moduleEntrySet = allModules
                        .entrySet();
                for (Entry<ModuleIdentifier, StmtContext<?, ModuleStatement, EffectiveStatement<String, ModuleStatement>>> moduleEntry : moduleEntrySet) {
                    if (moduleEntry.getKey().getName()
                            .equals(impIdentifier.getName())
                            && moduleEntry
                                    .getKey()
                                    .getRevision()
                                    .compareTo(
                                            recentModuleIdentifier
                                                    .getRevision()) > 0) {
                        recentModuleIdentifier = moduleEntry.getKey();
                        recentModuleEntry = moduleEntry;
                    }
                }

                return recentModuleEntry;
            }

            @Override
            public void prerequisiteFailed(
                    Collection<? extends Prerequisite<?>> failed)
                    throws InferenceException {
                if (failed.contains(imported)) {
                    throw new InferenceException(String.format(
                            "Imported module [%s] was not found.",
                            impIdentifier), stmt.getStatementSourceReference());
                }
            }
        });
    }

    private static ModuleIdentifier getImportedModuleIdentifier(
            Mutable<String, ImportStatement, ?> stmt) throws SourceException {

        String moduleName = stmt.getStatementArgument();
        Date revision = firstAttributeOf(stmt.declaredSubstatements(),
                RevisionDateStatement.class);
        if (revision == null) {
            revision = SimpleDateFormatUtil.DEFAULT_DATE_IMP;
        }

        return new ModuleIdentifierImpl(moduleName, Optional.<URI> absent(),
                Optional.<Date> of(revision));
    }

}