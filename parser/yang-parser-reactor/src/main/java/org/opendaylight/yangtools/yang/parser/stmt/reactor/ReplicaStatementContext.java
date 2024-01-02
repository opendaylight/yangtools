/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementFactory;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * A replica of a different statement. It does not allow modification, but produces an effective statement from a
 * designated source.
 */
final class ReplicaStatementContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends ReactorStmtCtx<A, D, E> {
    private final StatementContextBase<?, ?, ?> parent;
    private final ReactorStmtCtx<A, D, E> source;
    // We need to drop source's reference count when we are being swept.
    private final boolean haveSourceRef;

    ReplicaStatementContext(final StatementContextBase<?, ?, ?> parent, final ReactorStmtCtx<A, D, E> source) {
        super(source, null);
        this.parent = requireNonNull(parent);
        this.source = requireNonNull(source);
        if (source.isSupportedToBuildEffective()) {
            source.incRef();
            haveSourceRef = true;
        } else {
            haveSourceRef = false;
        }
    }

    @Override
    E createEffective() {
        return source.buildEffective();
    }

    @Override
    E createInferredEffective(final StatementFactory<A, D, E> factory, final InferredStatementContext<A, D, E> ctx,
            final Stream<? extends ReactorStmtCtx<?, ?, ?>> declared,
            final Stream<? extends ReactorStmtCtx<?, ?, ?>> effective) {
        return source.createInferredEffective(factory, ctx, declared, effective);
    }

    @Override
    ReactorStmtCtx<A, D, E> unmodifiedEffectiveSource() {
        return source.unmodifiedEffectiveSource();
    }

    @Override
    public EffectiveConfig effectiveConfig() {
        return source.effectiveConfig();
    }

    @Override
    public D declared() {
        return source.declared();
    }

    @Override
    public A argument() {
        return source.argument();
    }

    @Override
    public StatementSourceReference sourceReference() {
        return source.sourceReference();
    }

    @Override
    public String rawArgument() {
        return source.rawArgument();
    }

    @Override
    public Optional<StmtContext<A, D, E>> getOriginalCtx() {
        return source.getOriginalCtx();
    }

    @Override
    public Collection<? extends StatementContextBase<?, ?, ?>> mutableDeclaredSubstatements() {
        return source.mutableDeclaredSubstatements();
    }

    @Override
    public Collection<? extends Mutable<?, ?, ?>> mutableEffectiveSubstatements() {
        return source.mutableEffectiveSubstatements();
    }

    @Override
    byte executionOrder() {
        return source.executionOrder();
    }

    @Override
    public CopyHistory history() {
        return source.history();
    }

    @Override
    public Collection<? extends StmtContext<?, ?, ?>> getEffectOfStatement() {
        return List.of();
    }

    @Override
    ReplicaStatementContext<A, D, E> replicaAsChildOf(final StatementContextBase<?, ?, ?> newParent) {
        return source.replicaAsChildOf(newParent);
    }

    @Override
    public Optional<Mutable<A, D, E>> copyAsChildOf(final Mutable<?, ?, ?> newParent, final CopyType type,
            final QNameModule targetModule) {
        return source.copyAsChildOf(newParent, type, targetModule);
    }

    @Override
    ReactorStmtCtx<?, ?, ?> asEffectiveChildOf(final StatementContextBase<?, ?, ?> newParent, final CopyType type,
            final QNameModule targetModule) {
        final ReactorStmtCtx<?, ?, ?> ret = source.asEffectiveChildOf(newParent, type, targetModule);
        return ret == null ? null : this;
    }

    @Override
    StatementDefinitionContext<A, D, E> definition() {
        return source.definition();
    }

    @Override
    void markNoParentRef() {
        // No-op
    }

    @Override
    int sweepSubstatements() {
        if (haveSourceRef) {
            source.decRef();
        }
        return 0;
    }

    @Override
    @Deprecated
    public <K, V> void addToNs(final ParserNamespace<K, V> type, final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public Optional<StmtContext<A, D, E>> getPreviousCopyCtx() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void addAsEffectOfStatement(final Collection<? extends StmtContext<?, ?, ?>> ctxs) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public Mutable<?, ?, ?> childCopyOf(final StmtContext<?, ?, ?> stmt, final CopyType type,
            final QNameModule targetModule) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    boolean doTryToCompletePhase(final byte executionOrder) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<X, Y>>
            Mutable<X, Y, Z> createUndeclaredSubstatement(final StatementSupport<X, Y, Z> support, final X arg) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void addEffectiveSubstatement(final Mutable<?, ?, ?> substatement) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void addEffectiveSubstatements(final Collection<? extends Mutable<?, ?, ?>> statements) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void removeStatementFromEffectiveSubstatements(final StatementDefinition statementDef) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void removeStatementFromEffectiveSubstatements(final StatementDefinition statementDef,
            final String statementArg) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public boolean hasImplicitParentSupport() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public StmtContext<?, ?, ?> wrapWithImplicit(final StmtContext<?, ?, ?> original) {
        throw new UnsupportedOperationException();
    }

    /*
     * KEEP THINGS ORGANIZED!
     *
     * below methods exist in the same form in InferredStatementContext/SubstatementContext. If any adjustment is made
     * here, make sure it is properly updated there.
     */
    @Override
    public StatementContextBase<?, ?, ?> getParentContext() {
        return parent;
    }

    @Override
    public StatementContextBase<?, ?, ?> getParentStorage() {
        return parent;
    }

    @Override
    public RootStatementContext<?, ?, ?> getRoot() {
        return parent.getRoot();
    }

    @Override
    protected boolean isIgnoringIfFeatures() {
        return isIgnoringIfFeatures(parent);
    }

    @Override
    protected boolean isIgnoringConfig() {
        return isIgnoringConfig(parent);
    }

    @Override
    boolean computeSupportedByFeatures() {
        return source.isSupportedByFeatures();
    }

    @Override
    protected boolean isParentSupportedByFeatures() {
        return parent.isSupportedByFeatures();
    }
}
