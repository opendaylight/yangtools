/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

// FIXME: YANGTOOLS-1185: integrate this into StatementContextBase
final class BaseCurrentEffectiveStmtCtx<A, D extends DeclaredStatement<A>> implements EffectiveStmtCtx.Current<A, D> {
    static final Object NULL_OBJ = new Object();

    private final @NonNull StatementContextBase<A, D, ?> delegate;

    private @Nullable Object parent;

    BaseCurrentEffectiveStmtCtx(final StatementContextBase<A, D, ?> delegate) {
        this.delegate = requireNonNull(delegate);
        this.parent = null;
    }

    BaseCurrentEffectiveStmtCtx(final StatementContextBase<A, D, ?> delegate, final Parent parent) {
        this.delegate = requireNonNull(delegate);
        this.parent = requireNonNull(parent);
    }

    @Override
    public StatementSource source() {
        return delegate.source();
    }

    @Override
    public StatementSourceReference sourceReference() {
        return delegate.sourceReference();
    }

    @Override
    public CommonStmtCtx root() {
        return delegate.getRoot();
    }

    @Override
    public CopyHistory history() {
        return delegate.history();
    }

    @Override
    public D declared() {
        return delegate.declared();
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> localNamespace(final Class<@NonNull N> nsType) {
        return delegate.localNamespace(nsType);
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> @Nullable Map<K, V> namespace(final Class<@NonNull N> nsType) {
        return delegate.namespace(nsType);
    }

    @Override
    public <K, V, T extends K, N extends IdentifierNamespace<K, V>> V namespaceItem(final Class<@NonNull N> type,
            final T key) {
        return delegate.namespaceItem(type, key);
    }

    @Override
    public boolean effectiveConfig() {
        return delegate.isConfiguration();
    }

    @Override
    @Deprecated
    public Optional<SchemaPath> schemaPath() {
        return delegate.schemaPath();
    }

    @Override
    public StatementDefinition publicDefinition() {
        return delegate.publicDefinition();
    }

    @Override
    public Parent effectiveParent() {
        final Object local = parent;
        if (local instanceof Parent) {
            return (Parent) local;
        } else if (NULL_OBJ.equals(local)) {
            return null;
        } else {
            return loadParent();
        }
    }

    // FIXME: YANGTOOLS-1185: this should be rendered unnecessary
    private Parent loadParent() {
        final StatementContextBase<?, ?, ?> parentDelegate = delegate.getParentContext();
        if (parentDelegate == null) {
            parent = NULL_OBJ;
            return null;
        }

        final Parent ret = new BaseCurrentEffectiveStmtCtx<>(parentDelegate);
        parent = ret;
        return ret;
    }

    @Override
    public A argument() {
        return delegate.argument();
    }

    @Override
    public String rawArgument() {
        return delegate.rawArgument();
    }

    @Override
    public EffectiveStatement<?, ?> original() {
        return delegate.getOriginalCtx().map(StmtContext::buildEffective).orElse(null);
    }

    @Override
    public YangVersion yangVersion() {
        return delegate.yangVersion();
    }

    @Override
    @Deprecated
    @SuppressWarnings("unchecked")
    public <Z extends EffectiveStatement<A, D>> StmtContext<A, D, Z> caerbannog() {
        return (StmtContext<A, D, Z>) delegate;
    }
}