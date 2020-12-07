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
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

/**
 * A replica of a different statement. It does not allow modification, but produces an effective statement from a
 * designated source.
 */
final class ReplicaStatementContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends ReactorStmtCtx<A, D, E> {
    private final StatementContextBase<?, ?, ?> parent;
    private final ReactorStmtCtx<A, D, E> source;

    ReplicaStatementContext(final StatementContextBase<?, ?, ?> parent, final ReactorStmtCtx<A, D, E> source) {
        super(source);
        this.parent = requireNonNull(parent);
        this.source = requireNonNull(source);
        if (source.isSupportedToBuildEffective()) {
            source.incRef();
            setFullyDefined();
        } else {
            setIsSupportedToBuildEffective(false);
        }
    }

    @Override
    E createEffective() {
        return source.buildEffective();
    }

    @Override
    public Current<A, D> withParent(final Parent parent, final CopyType copyType, final QNameModule targetModule) {
        return source.withParent(parent, copyType, targetModule);
    }

    @Override
    ReactorStmtCtx<A, D, E> asEffectiveChildOf(final StatementContextBase<?, ?, ?> parent, final CopyType type,
            final QNameModule targetModule) {
        return source.asEffectiveChildOf(parent, type, targetModule);
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
    public ModelProcessingPhase getCompletedPhase() {
        return source.getCompletedPhase();
    }

    @Override
    public CopyHistory history() {
        return source.history();
    }

    @Override
    public Mutable<A, D, E> replicaAsChildOf(final Mutable<?, ?, ?> newParent) {
        return source.replicaAsChildOf(newParent);
    }

    @Override
    public Optional<ReactorStmtCtx<A, D, E>> copyAsChildOf(final Mutable<?, ?, ?> newParent, final CopyType type,
            final QNameModule targetModule) {
        return source.copyAsChildOf(newParent, type, targetModule);
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
        if (fullyDefined()) {
            source.decRef();
        }
        return 0;
    }

    @Override
    public <K, V, T extends K, U extends V, N extends ParserNamespace<K, V>> void addToNs(final Class<@NonNull N> type,
            final T key, final U value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<StmtContext<A, D, E>> getPreviousCopyCtx() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <K, KT extends K, N extends StatementNamespace<K, ?, ?>> void addContext(final Class<@NonNull N> namespace,
            final KT key, final StmtContext<?, ?, ?> stmt) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addAsEffectOfStatement(final StmtContext<?, ?, ?> ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addAsEffectOfStatement(final Collection<? extends StmtContext<?, ?, ?>> ctxs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<? extends StmtContext<?, ?, ?>> getEffectOfStatement() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<X, Y>>
            @NonNull Mutable<X, Y, Z> childCopyOf(final StmtContext<X, Y, Z> stmt, final CopyType type,
                final QNameModule targetModule) {
        throw new UnsupportedOperationException();
    }

    @Override boolean doTryToCompletePhase(final ModelProcessingPhase phase) {
        throw new UnsupportedOperationException();
    }

    /*
     * KEEP THINGS ORGANIZED!
     *
     * below methods exist in the same form in InferredStatementContext/SubstatementContext. If any adjustment is made
     * here, make sure it is properly updated there.
     */
    @Override
    @Deprecated
    public Optional<SchemaPath> schemaPath() {
        return substatementGetSchemaPath();
    }

    @Override
    public StatementContextBase<?, ?, ?> getParentContext() {
        return parent;
    }

    @Override
    public StorageNodeType getStorageNodeType() {
        return StorageNodeType.STATEMENT_LOCAL;
    }

    @Override
    public StatementContextBase<?, ?, ?> getParentNamespaceStorage() {
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
    protected boolean isParentSupportedByFeatures() {
        return parent.isSupportedByFeatures();
    }
}
