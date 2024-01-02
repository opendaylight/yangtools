/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ForwardingObject;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.UndeclaredCurrent;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class ForwardingUndeclaredCurrent<A, D extends DeclaredStatement<A>> extends ForwardingObject
        implements UndeclaredCurrent<A, D> {
    private final Current<A, D> delegate;

    ForwardingUndeclaredCurrent(final Current<A, D> delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public @NonNull QName moduleName() {
        return delegate.moduleName();
    }

    @Override
    public QName argumentAsTypeQName() {
        return delegate.argumentAsTypeQName();
    }

    @Override
    @Deprecated
    public <E extends EffectiveStatement<A, D>> StmtContext<A, D, E> caerbannog() {
        return delegate.caerbannog();
    }

    @Override
    public EffectiveConfig effectiveConfig() {
        return delegate.effectiveConfig();
    }

    @Override
    public QNameModule effectiveNamespace() {
        return delegate.effectiveNamespace();
    }

    @Override
    public Parent effectiveParent() {
        return delegate.effectiveParent();
    }

    @Override
    public StatementDefinition publicDefinition() {
        return delegate.publicDefinition();
    }

    @Override
    public StatementSourceReference sourceReference() {
        return delegate.sourceReference();
    }

    @Override
    public CopyHistory history() {
        return delegate.history();
    }

    @Override
    public <K, V> Map<K, V> namespace(final ParserNamespace<K, V> namespace) {
        return delegate.namespace(namespace);
    }

    @Override
    public <K, V> V namespaceItem(final ParserNamespace<K, V> namespace, final K key) {
        return delegate.namespaceItem(namespace, key);
    }

    @Override
    public <K, V> Map<K, V> localNamespacePortion(final ParserNamespace<K, V> namespace) {
        return delegate.localNamespacePortion(namespace);
    }

    @Override
    public A argument() {
        return delegate.argument();
    }

    @Override
    public YangVersion yangVersion() {
        return delegate.yangVersion();
    }

    @Override
    public <X, Z extends EffectiveStatement<X, ?>> Optional<X> findSubstatementArgument(final Class<Z> type) {
        return delegate.findSubstatementArgument(type);
    }

    @Override
    public boolean hasSubstatement(final Class<? extends EffectiveStatement<?, ?>> type) {
        return delegate.hasSubstatement(type);
    }

    @Override
    protected Current<A, D> delegate() {
        return delegate;
    }
}
