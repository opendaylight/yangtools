/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.module;

import static com.google.common.base.Verify.verify;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnqualifiedQName;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.Submodule;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SemVerSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.spi.ModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.NamespaceToModule;
import org.opendaylight.yangtools.yang.parser.spi.PreLinkageModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SemanticVersionModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.SemanticVersionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.ImportPrefixToModuleCtx;
import org.opendaylight.yangtools.yang.parser.spi.source.IncludedSubmoduleNameToModuleCtx;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToSourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNamespaceForBelongsTo;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleQNameToModuleName;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

abstract class AbstractModuleStatementSupport
        extends BaseStatementSupport<UnqualifiedQName, ModuleStatement, ModuleEffectiveStatement> {
    AbstractModuleStatementSupport() {
        super(YangStmtMapping.MODULE);
    }

    @Override
    public final UnqualifiedQName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        try {
            return UnqualifiedQName.of(value);
        } catch (IllegalArgumentException e) {
            throw new SourceException(e.getMessage(), ctx.sourceReference(), e);
        }
    }

    @Override
    public final void onPreLinkageDeclared(
            final Mutable<UnqualifiedQName, ModuleStatement, ModuleEffectiveStatement> stmt) {
        final String moduleName = stmt.getRawArgument();

        final URI moduleNs = firstAttributeOf(stmt.declaredSubstatements(), NamespaceStatement.class);
        SourceException.throwIfNull(moduleNs, stmt.sourceReference(),
            "Namespace of the module [%s] is missing", moduleName);
        stmt.addToNs(ModuleNameToNamespace.class, moduleName, moduleNs);

        final String modulePrefix = firstAttributeOf(stmt.declaredSubstatements(), PrefixStatement.class);
        SourceException.throwIfNull(modulePrefix, stmt.sourceReference(),
            "Prefix of the module [%s] is missing", moduleName);
        stmt.addToNs(ImpPrefixToNamespace.class, modulePrefix, moduleNs);

        stmt.addContext(PreLinkageModuleNamespace.class, moduleName, stmt);

        final Optional<Revision> revisionDate = StmtContextUtils.getLatestRevision(stmt.declaredSubstatements());
        final QNameModule qNameModule = QNameModule.create(moduleNs, revisionDate.orElse(null)).intern();

        stmt.addToNs(ModuleCtxToModuleQName.class, stmt, qNameModule);
        stmt.setRootIdentifier(RevisionSourceIdentifier.create(stmt.getArgument().getLocalName(), revisionDate));
    }

    @Override
    public final void onLinkageDeclared(
            final Mutable<UnqualifiedQName, ModuleStatement, ModuleEffectiveStatement> stmt) {

        final Optional<URI> moduleNs = Optional.ofNullable(firstAttributeOf(stmt.declaredSubstatements(),
                NamespaceStatement.class));
        SourceException.throwIf(!moduleNs.isPresent(), stmt.sourceReference(),
            "Namespace of the module [%s] is missing", stmt.argument());

        final Optional<Revision> revisionDate = StmtContextUtils.getLatestRevision(stmt.declaredSubstatements());
        final QNameModule qNameModule = QNameModule.create(moduleNs.get(), revisionDate.orElse(null)).intern();
        final StmtContext<?, ModuleStatement, ModuleEffectiveStatement> possibleDuplicateModule =
                stmt.getFromNamespace(NamespaceToModule.class, qNameModule);
        if (possibleDuplicateModule != null && possibleDuplicateModule != stmt) {
            throw new SourceException(stmt.sourceReference(), "Module namespace collision: %s. At %s",
                    qNameModule.getNamespace(), possibleDuplicateModule.sourceReference());
        }

        final String moduleName = stmt.getRawArgument();
        final SourceIdentifier moduleIdentifier = RevisionSourceIdentifier.create(moduleName, revisionDate);

        stmt.addContext(ModuleNamespace.class, moduleIdentifier, stmt);
        stmt.addContext(ModuleNamespaceForBelongsTo.class, moduleIdentifier.getName(), stmt);
        stmt.addContext(NamespaceToModule.class, qNameModule, stmt);

        final String modulePrefix = firstAttributeOf(stmt.declaredSubstatements(), PrefixStatement.class);
        SourceException.throwIfNull(modulePrefix, stmt.sourceReference(),
            "Prefix of the module [%s] is missing", stmt.argument());

        stmt.addToNs(PrefixToModule.class, modulePrefix, qNameModule);
        stmt.addToNs(ModuleNameToModuleQName.class, moduleName, qNameModule);
        stmt.addToNs(ModuleCtxToModuleQName.class, stmt, qNameModule);
        stmt.addToNs(ModuleCtxToSourceIdentifier.class, stmt, moduleIdentifier);
        stmt.addToNs(ModuleQNameToModuleName.class, qNameModule, moduleName);
        stmt.addToNs(ImportPrefixToModuleCtx.class, modulePrefix, stmt);

        if (stmt.isEnabledSemanticVersioning()) {
            addToSemVerModuleNamespace(stmt, moduleIdentifier);
        }
    }

    @Override
    protected final ImmutableList<? extends EffectiveStatement<?, ?>> buildEffectiveSubstatements(
            final Current<UnqualifiedQName, ModuleStatement> stmt,
            final List<? extends StmtContext<?, ?, ?>> substatements) {
        final ImmutableList<? extends EffectiveStatement<?, ?>> local =
                super.buildEffectiveSubstatements(stmt, substatements);
        final Collection<StmtContext<?, ?, ?>> submodules = submoduleContexts(stmt);
        if (submodules.isEmpty()) {
            return local;
        }

        // Concatenate statements so they appear as if they were part of target module
        final List<EffectiveStatement<?, ?>> others = new ArrayList<>();
        for (StmtContext<?, ?, ?> submoduleCtx : submodules) {
            for (EffectiveStatement<?, ?> effective : submoduleCtx.buildEffective().effectiveSubstatements()) {
                if (effective instanceof SchemaNode || effective instanceof DataNodeContainer) {
                    others.add(effective);
                }
            }
        }

        return ImmutableList.<EffectiveStatement<?, ?>>builderWithExpectedSize(local.size() + others.size())
                .addAll(local)
                .addAll(others)
                .build();
    }

    @Override
    protected final ModuleStatement createDeclared(final StmtContext<UnqualifiedQName, ModuleStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new ModuleStatementImpl(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected final ModuleStatement createEmptyDeclared(final StmtContext<UnqualifiedQName, ModuleStatement, ?> ctx) {
        throw noNamespace(ctx);
    }

    @Override
    protected final ModuleEffectiveStatement createEffective(final Current<UnqualifiedQName, ModuleStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        if (substatements.isEmpty()) {
            throw noNamespace(stmt);
        }

        final List<Submodule> submodules = new ArrayList<>();
        for (StmtContext<?, ?, ?> submoduleCtx : submoduleContexts(stmt)) {
            final EffectiveStatement<?, ?> submodule = submoduleCtx.buildEffective();
            verify(submodule instanceof Submodule, "Submodule statement %s is not a Submodule", submodule);
            submodules.add((Submodule) submodule);
        }

        try {
            return new ModuleEffectiveStatementImpl(stmt, substatements, submodules);
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt.sourceReference(), e);
        }
    }

    private static Collection<StmtContext<?, ?, ?>> submoduleContexts(final Current<?, ?> stmt) {
        final Map<String, StmtContext<?, ?, ?>> submodules = stmt.getAllFromCurrentStmtCtxNamespace(
            IncludedSubmoduleNameToModuleCtx.class);
        return submodules == null ? List.of() : submodules.values();
    }

    private static SourceException noNamespace(final @NonNull CommonStmtCtx stmt) {
        return new SourceException("No namespace declared in module", stmt.sourceReference());
    }

    private static void addToSemVerModuleNamespace(
            final Mutable<UnqualifiedQName, ModuleStatement, ModuleEffectiveStatement> stmt,
            final SourceIdentifier moduleIdentifier) {
        final SemVerSourceIdentifier id = SemVerSourceIdentifier.create(stmt.getRawArgument(),
            stmt.getFromNamespace(SemanticVersionNamespace.class, stmt));
        stmt.addToNs(SemanticVersionModuleNamespace.class, id, stmt);
    }
}
