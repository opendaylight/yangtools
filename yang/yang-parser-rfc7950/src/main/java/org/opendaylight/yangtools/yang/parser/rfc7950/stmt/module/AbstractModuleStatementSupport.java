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
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
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
import org.opendaylight.yangtools.yang.parser.spi.ModuleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.NamespaceToModule;
import org.opendaylight.yangtools.yang.parser.spi.PreLinkageModuleNamespace;
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
        extends BaseStatementSupport<String, ModuleStatement, ModuleEffectiveStatement> {
    AbstractModuleStatementSupport() {
        super(YangStmtMapping.MODULE);
    }

    @Override
    public final String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public final void onPreLinkageDeclared(final Mutable<String, ModuleStatement, ModuleEffectiveStatement> stmt) {
        final String moduleName = stmt.getStatementArgument();

        final URI moduleNs = firstAttributeOf(stmt.declaredSubstatements(), NamespaceStatement.class);
        SourceException.throwIfNull(moduleNs, stmt.getStatementSourceReference(),
            "Namespace of the module [%s] is missing", stmt.getStatementArgument());
        stmt.addToNs(ModuleNameToNamespace.class, moduleName, moduleNs);

        final String modulePrefix = firstAttributeOf(stmt.declaredSubstatements(), PrefixStatement.class);
        SourceException.throwIfNull(modulePrefix, stmt.getStatementSourceReference(),
            "Prefix of the module [%s] is missing", stmt.getStatementArgument());
        stmt.addToNs(ImpPrefixToNamespace.class, modulePrefix, moduleNs);

        stmt.addContext(PreLinkageModuleNamespace.class, moduleName, stmt);

        final Optional<Revision> revisionDate = StmtContextUtils.getLatestRevision(stmt.declaredSubstatements());
        final QNameModule qNameModule = QNameModule.create(moduleNs, revisionDate.orElse(null)).intern();

        stmt.addToNs(ModuleCtxToModuleQName.class, stmt, qNameModule);
        stmt.setRootIdentifier(RevisionSourceIdentifier.create(stmt.getStatementArgument(), revisionDate));
    }

    @Override
    public final void onLinkageDeclared(final Mutable<String, ModuleStatement, ModuleEffectiveStatement> stmt) {

        final Optional<URI> moduleNs = Optional.ofNullable(firstAttributeOf(stmt.declaredSubstatements(),
                NamespaceStatement.class));
        SourceException.throwIf(!moduleNs.isPresent(), stmt.getStatementSourceReference(),
            "Namespace of the module [%s] is missing", stmt.getStatementArgument());

        final Optional<Revision> revisionDate = StmtContextUtils.getLatestRevision(stmt.declaredSubstatements());
        final QNameModule qNameModule = QNameModule.create(moduleNs.get(), revisionDate.orElse(null)).intern();
        final StmtContext<?, ModuleStatement, ModuleEffectiveStatement> possibleDuplicateModule =
                stmt.getFromNamespace(NamespaceToModule.class, qNameModule);
        if (possibleDuplicateModule != null && possibleDuplicateModule != stmt) {
            throw new SourceException(stmt.getStatementSourceReference(), "Module namespace collision: %s. At %s",
                    qNameModule.getNamespace(), possibleDuplicateModule.getStatementSourceReference());
        }

        final SourceIdentifier moduleIdentifier = RevisionSourceIdentifier.create(stmt.getStatementArgument(),
                revisionDate);

        stmt.addContext(ModuleNamespace.class, moduleIdentifier, stmt);
        stmt.addContext(ModuleNamespaceForBelongsTo.class, moduleIdentifier.getName(), stmt);
        stmt.addContext(NamespaceToModule.class, qNameModule, stmt);

        final String modulePrefix = firstAttributeOf(stmt.declaredSubstatements(), PrefixStatement.class);
        SourceException.throwIfNull(modulePrefix, stmt.getStatementSourceReference(),
            "Prefix of the module [%s] is missing", stmt.getStatementArgument());

        stmt.addToNs(PrefixToModule.class, modulePrefix, qNameModule);
        stmt.addToNs(ModuleNameToModuleQName.class, stmt.getStatementArgument(), qNameModule);
        stmt.addToNs(ModuleCtxToModuleQName.class, stmt, qNameModule);
        stmt.addToNs(ModuleCtxToSourceIdentifier.class, stmt, moduleIdentifier);
        stmt.addToNs(ModuleQNameToModuleName.class, qNameModule, stmt.getStatementArgument());
        stmt.addToNs(ImportPrefixToModuleCtx.class, modulePrefix, stmt);

        if (stmt.isEnabledSemanticVersioning()) {
            addToSemVerModuleNamespace(stmt, moduleIdentifier);
        }
    }

    @Override
    protected final ImmutableList<? extends EffectiveStatement<?, ?>> buildEffectiveSubstatements(
            final StmtContext<String, ModuleStatement, ModuleEffectiveStatement> ctx,
            final List<? extends StmtContext<?, ?, ?>> substatements) {
        final ImmutableList<? extends EffectiveStatement<?, ?>> local =
                super.buildEffectiveSubstatements(ctx, substatements);
        final Collection<StmtContext<?, ?, ?>> submodules = submoduleContexts(ctx);
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
    protected final ModuleStatement createDeclared(final StmtContext<String, ModuleStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new ModuleStatementImpl(ctx.coerceRawStatementArgument(), substatements);
    }

    @Override
    protected final ModuleStatement createEmptyDeclared(final StmtContext<String, ModuleStatement, ?> ctx) {
        throw noNamespace(ctx);
    }

    @Override
    protected final ModuleEffectiveStatement createEffective(
            final StmtContext<String, ModuleStatement, ModuleEffectiveStatement> ctx,
            final ModuleStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final List<Submodule> submodules = new ArrayList<>();
        for (StmtContext<?, ?, ?> submoduleCtx : submoduleContexts(ctx)) {
            final EffectiveStatement<?, ?> submodule = submoduleCtx.buildEffective();
            verify(submodule instanceof Submodule, "Submodule statement %s is not a Submodule", submodule);
            submodules.add((Submodule) submodule);
        }

        return new ModuleEffectiveStatementImpl(ctx, declared, substatements, submodules);
    }

    @Override
    protected final ModuleEffectiveStatement createEmptyEffective(
            final StmtContext<String, ModuleStatement, ModuleEffectiveStatement> ctx, final ModuleStatement declared) {
        throw noNamespace(ctx);
    }

    private static Collection<StmtContext<?, ?, ?>> submoduleContexts(final StmtContext<?, ?, ?> ctx) {
        final Map<String, StmtContext<?, ?, ?>> submodules = ctx.getAllFromCurrentStmtCtxNamespace(
            IncludedSubmoduleNameToModuleCtx.class);
        return submodules == null ? List.of() : submodules.values();
    }

    private static SourceException noNamespace(final StmtContext<?, ?, ?> ctx) {
        return new SourceException("No namespace declared in module", ctx.getStatementSourceReference());
    }

    private static void addToSemVerModuleNamespace(
            final Mutable<String, ModuleStatement, ModuleEffectiveStatement> stmt,
            final SourceIdentifier moduleIdentifier) {
        final String moduleName = stmt.coerceStatementArgument();
        final SemVer moduleSemVer = stmt.getFromNamespace(SemanticVersionNamespace.class, stmt);
        final SemVerSourceIdentifier id = SemVerSourceIdentifier.create(moduleName, moduleSemVer);
        stmt.addToNs(SemanticVersionModuleNamespace.class, id, stmt);
    }
}
