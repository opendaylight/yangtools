/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.module;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ForwardingObject;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.IncludedSubmoduleNameToModuleCtx;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

/**
 * A concentrator {@link StmtContext}, which makes it appear as though as all effective statements in submodules
 * are included in it.
 */
final class ModuleStmtContext extends ForwardingObject
        implements StmtContext<String, ModuleStatement, ModuleEffectiveStatement> {

    private final @NonNull StmtContext<String, ModuleStatement, ModuleEffectiveStatement> delegate;
    private final @NonNull ImmutableList<StmtContext<?, ?, ?>> effectiveSubstatements;
    private final @NonNull ImmutableSet<Module> submodules;

    private ModuleStmtContext(final StmtContext<String, ModuleStatement, ModuleEffectiveStatement> delegate,
            final Collection<StmtContext<?, ?, ?>> submodules) {
        this.delegate = requireNonNull(delegate);

        final List<StmtContext<?, ?, ?>> statements = new ArrayList<>(delegate.effectiveSubstatements());
        final Set<Module> subs = new LinkedHashSet<>(submodules.size());
        for (StmtContext<?, ?, ?> submoduleCtx : submodules) {
            final EffectiveStatement<?, ?> submodule = submoduleCtx.buildEffective();
            verify(submodule instanceof Module, "Submodule statement %s is not a Module", submodule);
            subs.add((Module) submodule);

            // FIXME: filter these: at least belongs-to, etc.
            statements.addAll(submoduleCtx.effectiveSubstatements());
        }

        this.effectiveSubstatements = ImmutableList.copyOf(statements);
        this.submodules = ImmutableSet.copyOf(subs);
    }

    static @NonNull ModuleStmtContext create(
            final StmtContext<String, ModuleStatement, ModuleEffectiveStatement> delegate) {
        final Map<String, StmtContext<?, ?, ?>> includedSubmodules = delegate.getAllFromCurrentStmtCtxNamespace(
            IncludedSubmoduleNameToModuleCtx.class);
        return new ModuleStmtContext(delegate, includedSubmodules == null || includedSubmodules.isEmpty()
                ? ImmutableList.of() : includedSubmodules.values());
    }

    @Override
    protected @NonNull StmtContext<String, ModuleStatement, ModuleEffectiveStatement> delegate() {
        return delegate;
    }

    ImmutableSet<Module> getSubmodules() {
        return submodules;
    }

    @Override
    public ImmutableList<StmtContext<?, ?, ?>> effectiveSubstatements() {
        return effectiveSubstatements;
    }

    @Override
    public ModuleEffectiveStatement buildEffective() {
        throw new UnsupportedOperationException("Attempted to instantiate proxy context " + this);
    }

    @Override
    public ModuleStatement buildDeclared() {
        return delegate.buildDeclared();
    }

    @Override
    public StatementSource getStatementSource() {
        return delegate.getStatementSource();
    }

    @Override
    public StatementSourceReference getStatementSourceReference() {
        return delegate.getStatementSourceReference();
    }

    @Override
    public StatementDefinition getPublicDefinition() {
        return delegate.getPublicDefinition();
    }

    @Override
    public StmtContext<?, ?, ?> getParentContext() {
        return delegate.getParentContext();
    }

    @Override
    public String rawStatementArgument() {
        return delegate.rawStatementArgument();
    }

    @Override
    public @Nullable String getStatementArgument() {
        return delegate.getStatementArgument();
    }

    @Override
    public @NonNull Optional<SchemaPath> getSchemaPath() {
        return delegate.getSchemaPath();
    }

    @Override
    public boolean isConfiguration() {
        return delegate.isConfiguration();
    }

    @Override
    public boolean isEnabledSemanticVersioning() {
        return delegate.isEnabledSemanticVersioning();
    }

    @Override
    public <K, V, T extends K, N extends IdentifierNamespace<K, V>> @NonNull V getFromNamespace(final Class<N> type,
            final T key) {
        return delegate.getFromNamespace(type, key);
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromNamespace(final Class<N> type) {
        return delegate.getAllFromNamespace(type);
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromCurrentStmtCtxNamespace(
            final Class<N> type) {
        return delegate.getAllFromCurrentStmtCtxNamespace(type);
    }

    @Override
    public StmtContext<?, ?, ?> getRoot() {
        return delegate.getRoot();
    }

    @Override
    public Collection<? extends StmtContext<?, ?, ?>> declaredSubstatements() {
        return delegate.declaredSubstatements();
    }

    @Override
    public boolean isSupportedToBuildEffective() {
        return delegate.isSupportedToBuildEffective();
    }

    @Override
    public boolean isSupportedByFeatures() {
        return delegate.isSupportedByFeatures();
    }

    @Override
    public Collection<? extends StmtContext<?, ?, ?>> getEffectOfStatement() {
        return delegate.getEffectOfStatement();
    }

    @Override
    public CopyHistory getCopyHistory() {
        return delegate.getCopyHistory();
    }

    @Override
    public Optional<StmtContext<?, ?, ?>> getOriginalCtx() {
        return delegate.getOriginalCtx();
    }

    @Override
    public Optional<? extends StmtContext<?, ?, ?>> getPreviousCopyCtx() {
        return delegate.getPreviousCopyCtx();
    }

    @Override
    public ModelProcessingPhase getCompletedPhase() {
        return delegate.getCompletedPhase();
    }

    @Override
    public @NonNull YangVersion getRootVersion() {
        return delegate.getRootVersion();
    }
}
