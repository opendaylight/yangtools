/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A replica of a different statement. It does not allow modification, but produces an effective statement from a
 * designated source.
 */
final class ReplicaStatementContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends ReactorStmtCtx<A, D, E> {
    private static final Logger LOG = LoggerFactory.getLogger(ReplicaStatementContext.class);

    private final StatementContextBase<?, ?, ?> parent;
    private final StatementContextBase<A, D, E> source;

    private final boolean haveRef;

    ReplicaStatementContext(final StatementContextBase<?, ?, ?> parent, final StatementContextBase<A, D, E> source) {
        super(source);
        this.parent = requireNonNull(parent);
        this.source = requireNonNull(source);
        if (source.isSupportedToBuildEffective()) {
            source.incRef();
            haveRef = true;
        } else {
            setIsSupportedToBuildEffective(false);
            haveRef = false;
        }
    }

    @Override
    E createEffective() {
        return source.buildEffective();
    }

    @Override
    public boolean isConfiguration() {
        return source.isConfiguration();
    }

    @Override
    public D buildDeclared() {
        return source.buildDeclared();
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
    public CopyHistory getCopyHistory() {
        return source.getCopyHistory();
    }

    @Override
    public ModelProcessingPhase getCompletedPhase() {
        return source.getCompletedPhase();
    }

    @Override
    public StatementDefinition publicDefinition() {
        return source.publicDefinition();
    }

    @Override
    StatementDefinitionContext<A, D, E> definition() {
        return source.definition();
    }

    @Override
    boolean hasEmptySubstatements() {
        return source.hasEmptySubstatements();
    }

    @Override
    boolean builtDeclared() {
        return source.builtDeclared();
    }

    @Override
    Iterable<StatementContextBase<?, ?, ?>> effectiveChildrenToComplete() {
        return ImmutableList.of();
    }

    @Override
    int sweepSubstatements() {
        if (haveRef) {
            source.decRef();
        }
        return 0;
    }

    @Override
    public Optional<StmtContext<A, D, E>> getPreviousCopyCtx() {
        throw new UnsupportedOperationException();
    }

    @Override
    Stream<? extends StmtContext<?, ?, ?>> streamDeclared() {
        throw new UnsupportedOperationException();
    }

    @Override
    Stream<? extends StmtContext<?, ?, ?>> streamEffective() {
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

    @Override
    public <K, V, T extends K, U extends V, N extends IdentifierNamespace<K, V>> void addToNs(final Class<@NonNull N> type,
        final T key, final U value) {
        // TODO Auto-generated method stub

    }

    @Override
    public Mutable<?, ?, ?> childCopyOf(final StmtContext<?, ?, ?> stmt, final CopyType type, @Nullable final QNameModule targetModule) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @NonNull Mutable<A, D, E> replicaAsChildOf(final Mutable<?, ?, ?> parent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @NonNull Optional<? extends Mutable<?, ?, ?>> copyAsChildOf(final Mutable<?, ?, ?> parent, final CopyType type,
        @Nullable final QNameModule targetModule) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @NonNull ModelActionBuilder newInferenceAction(@NonNull final ModelProcessingPhase phase) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <K, KT extends K, N extends StatementNamespace<K, ?, ?>> void addContext(final Class<@NonNull N> namespace, final KT key,
        final StmtContext<?, ?, ?> stmt) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addAsEffectOfStatement(final StmtContext<?, ?, ?> ctx) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addAsEffectOfStatement(final Collection<? extends StmtContext<?, ?, ?>> ctxs) {
        // TODO Auto-generated method stub

    }

    @Override
    public Collection<? extends StmtContext<?, ?, ?>> getEffectOfStatement() {
        // TODO Auto-generated method stub
        return null;
    }
}
