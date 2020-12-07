/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;

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
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

final class AdaptedPrototypeStatementContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        implements Current<A, D> {

    private final @NonNull StatementContextBase<A, D, E> delegate;
    private final @NonNull StatementContextBase<?, ?, ?> parent;
    private final @NonNull CopyHistory copyHistory;
    private final @NonNull EffectiveConfig effectiveConfig;
    private final A argument;
    @Deprecated
    private final Optional<SchemaPath> schemaPath;

    AdaptedPrototypeStatementContext(final StatementContextBase<?, ?, ?> parent, final EffectiveConfig effectiveConfig,
                                     final A argument, final StatementContextBase<A, D, E> prototype,
                                     final CopyHistory copyHistory, final Optional<SchemaPath> schemaPath) {
        this.parent = parent;
        this.argument = argument;
        this.effectiveConfig = effectiveConfig;
        this.delegate = prototype;
        this.copyHistory = copyHistory;
        this.schemaPath = schemaPath;
    }

    @Override
    public @NonNull CommonStmtCtx root() {
        return delegate.getRoot();
    }

    @Override
    public @Nullable EffectiveStatement<?, ?> original() {
        return delegate.original();
    }

    @Override
    public @NonNull StmtContext caerbannog() {
        return delegate;
    }

    @Override
    public D declared() {
        return delegate.declared();
    }

    @Override
    public A argument() {
        return argument;
    }

    @Override
    public @NonNull YangVersion yangVersion() {
        return delegate.yangVersion();
    }

    @Override
    public boolean hasSubstatement(@NonNull Class type) {
        return delegate.hasSubstatement(type);
    }

    @Override
    public @NonNull Optional findSubstatementArgument(@NonNull Class type) {
        return delegate.findSubstatementArgument(type);
    }

    @Override
    public @Nullable Parent effectiveParent() {
        return parent;
    }

    @Override
    public @NonNull EffectiveConfig effectiveConfig() {
        return effectiveConfig;
    }

    @Override
    @Deprecated
    public @NonNull Optional<SchemaPath> schemaPath() {
        return schemaPath;
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> namespace(Class<@NonNull N> nsType) {
        return delegate.namespace(nsType);
    }

    @Override
    public <K, V, T extends K, N extends IdentifierNamespace<K, V>> V namespaceItem(Class<@NonNull N> nsType, T key) {
        return delegate.namespaceItem(nsType, key);
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> localNamespace(Class<@NonNull N> nsType) {
        return delegate.localNamespace(nsType);
    }

    @Override
    public @NonNull CopyHistory history() {
        return copyHistory;
    }

    @Override
    public @NonNull StatementDefinition publicDefinition() {
        return delegate.publicDefinition();
    }

    @Override
    public @NonNull StatementSourceReference sourceReference() {
        return delegate.sourceReference();
    }

    @Override
    public @Nullable String rawArgument() {
        return delegate.rawArgument();
    }
}
