/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.ImplicitParentAwareStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.MutableStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceKeyCriterion;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementFactory;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport.CopyPolicy;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.ImplicitSubstatement;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.NamespaceBehaviourWithListeners.KeyedValueAddedListener;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.NamespaceBehaviourWithListeners.PredicateValueAddedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core reactor statement implementation of {@link Mutable}.
 *
 * @param <A> Argument type
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
public abstract class StatementContextBase<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends ReactorStmtCtx<A, D, E> {
    /**
     * Event listener when an item is added to model namespace.
     */
    interface OnNamespaceItemAdded extends EventListener {
        /**
         * Invoked whenever a new item is added to a namespace.
         */
        void namespaceItemAdded(StatementContextBase<?, ?, ?> context, Class<?> namespace, Object key, Object value);
    }

    /**
     * Event listener when a parsing {@link ModelProcessingPhase} is completed.
     */
    interface OnPhaseFinished extends EventListener {
        /**
         * Invoked whenever a processing phase has finished.
         */
        boolean phaseFinished(StatementContextBase<?, ?, ?> context, ModelProcessingPhase finishedPhase);
    }

    /**
     * Interface for all mutations within an {@link ModelActionBuilder.InferenceAction}.
     */
    interface ContextMutation {

        boolean isFinished();
    }

    private static final Logger LOG = LoggerFactory.getLogger(StatementContextBase.class);

    private final CopyHistory copyHistory;
    // Note: this field can strictly be derived in InferredStatementContext, but it forms the basis of many of our
    //       operations, hence we want to keep it close by.
    private final @NonNull StatementDefinitionContext<A, D, E> definition;

    private Multimap<ModelProcessingPhase, OnPhaseFinished> phaseListeners = ImmutableMultimap.of();
    private Multimap<ModelProcessingPhase, ContextMutation> phaseMutation = ImmutableMultimap.of();

    private List<StmtContext<?, ?, ?>> effectOfStatement = ImmutableList.of();

    private @Nullable ModelProcessingPhase completedPhase;

    // Copy constructor used by subclasses to implement reparent()
    StatementContextBase(final StatementContextBase<A, D, E> original) {
        super(original);
        this.copyHistory = original.copyHistory;
        this.definition = original.definition;
        this.completedPhase = original.completedPhase;
    }

    StatementContextBase(final StatementDefinitionContext<A, D, E> def) {
        this.definition = requireNonNull(def);
        this.copyHistory = CopyHistory.original();
    }

    StatementContextBase(final StatementDefinitionContext<A, D, E> def, final CopyHistory copyHistory) {
        this.definition = requireNonNull(def);
        this.copyHistory = requireNonNull(copyHistory);
    }

    @Override
    public Collection<? extends StmtContext<?, ?, ?>> getEffectOfStatement() {
        return effectOfStatement;
    }

    @Override
    public void addAsEffectOfStatement(final Collection<? extends StmtContext<?, ?, ?>> ctxs) {
        if (ctxs.isEmpty()) {
            return;
        }

        if (effectOfStatement.isEmpty()) {
            effectOfStatement = new ArrayList<>(ctxs.size());
        }
        effectOfStatement.addAll(ctxs);
    }

    @Override
    public final CopyHistory history() {
        return copyHistory;
    }

    @Override
    public final ModelProcessingPhase getCompletedPhase() {
        return completedPhase;
    }

    // FIXME: this should be propagated through a correct constructor
    @Deprecated
    final void setCompletedPhase(final ModelProcessingPhase completedPhase) {
        this.completedPhase = completedPhase;
    }

    @Override
    public final <K, V, T extends K, U extends V, N extends ParserNamespace<K, V>> void addToNs(
            final Class<@NonNull N> type, final T key, final U value) {
        addToNamespace(type, key, value);
    }

    static final Collection<? extends Mutable<?, ?, ?>> mutableEffectiveSubstatements(
            final List<ReactorStmtCtx<?, ?, ?>> effective) {
        return effective instanceof ImmutableCollection ? effective : Collections.unmodifiableCollection(effective);
    }

    private static List<ReactorStmtCtx<?, ?, ?>> shrinkEffective(final List<ReactorStmtCtx<?, ?, ?>> effective) {
        return effective.isEmpty() ? ImmutableList.of() : effective;
    }

    public abstract void removeStatementFromEffectiveSubstatements(StatementDefinition statementDef);

    static final List<ReactorStmtCtx<?, ?, ?>> removeStatementFromEffectiveSubstatements(
            final List<ReactorStmtCtx<?, ?, ?>> effective, final StatementDefinition statementDef) {
        if (effective.isEmpty()) {
            return effective;
        }

        final Iterator<? extends StmtContext<?, ?, ?>> iterator = effective.iterator();
        while (iterator.hasNext()) {
            final StmtContext<?, ?, ?> next = iterator.next();
            if (statementDef.equals(next.publicDefinition())) {
                iterator.remove();
            }
        }

        return shrinkEffective(effective);
    }

    /**
     * Removes a statement context from the effective substatements based on its statement definition (i.e statement
     * keyword) and raw (in String form) statement argument. The statement context is removed only if both statement
     * definition and statement argument match with one of the effective substatements' statement definition
     * and argument.
     *
     * <p>
     * If the statementArg parameter is null, the statement context is removed based only on its statement definition.
     *
     * @param statementDef statement definition of the statement context to remove
     * @param statementArg statement argument of the statement context to remove
     */
    public abstract void removeStatementFromEffectiveSubstatements(StatementDefinition statementDef,
            String statementArg);

    static final List<ReactorStmtCtx<?, ?, ?>> removeStatementFromEffectiveSubstatements(
            final List<ReactorStmtCtx<?, ?, ?>> effective, final StatementDefinition statementDef,
            final String statementArg) {
        if (statementArg == null) {
            return removeStatementFromEffectiveSubstatements(effective, statementDef);
        }

        if (effective.isEmpty()) {
            return effective;
        }

        final Iterator<ReactorStmtCtx<?, ?, ?>> iterator = effective.iterator();
        while (iterator.hasNext()) {
            final Mutable<?, ?, ?> next = iterator.next();
            if (statementDef.equals(next.publicDefinition()) && statementArg.equals(next.rawArgument())) {
                iterator.remove();
            }
        }

        return shrinkEffective(effective);
    }

    // YANG example: RPC/action statements always have 'input' and 'output' defined
    @Beta
    public <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<X, Y>> @NonNull Mutable<X, Y, Z>
            appendImplicitSubstatement(final StatementSupport<X, Y, Z> support, final String rawArg) {
        // FIXME: YANGTOOLS-652: This does not need to be a SubstatementContext, in can be a specialized
        //                       StatementContextBase subclass.
        final Mutable<X, Y, Z> ret = new SubstatementContext<>(this, new StatementDefinitionContext<>(support),
                ImplicitSubstatement.of(sourceReference()), rawArg);
        support.onStatementAdded(ret);
        addEffectiveSubstatement(ret);
        return ret;
    }

    /**
     * Adds an effective statement to collection of substatements.
     *
     * @param substatement substatement
     * @throws IllegalStateException if added in declared phase
     * @throws NullPointerException if statement parameter is null
     */
    public abstract void addEffectiveSubstatement(Mutable<?, ?, ?> substatement);

    final List<ReactorStmtCtx<?, ?, ?>> addEffectiveSubstatement(final List<ReactorStmtCtx<?, ?, ?>> effective,
            final Mutable<?, ?, ?> substatement) {
        verifyStatement(substatement);

        final List<ReactorStmtCtx<?, ?, ?>> resized = beforeAddEffectiveStatement(effective, 1);
        final ReactorStmtCtx<?, ?, ?> stmt = (ReactorStmtCtx<?, ?, ?>) substatement;
        final ModelProcessingPhase phase = completedPhase;
        if (phase != null) {
            ensureCompletedPhase(stmt, phase);
        }
        resized.add(stmt);
        return resized;
    }

    /**
     * Adds an effective statement to collection of substatements.
     *
     * @param statements substatements
     * @throws IllegalStateException
     *             if added in declared phase
     * @throws NullPointerException
     *             if statement parameter is null
     */
    public final void addEffectiveSubstatements(final Collection<? extends Mutable<?, ?, ?>> statements) {
        if (!statements.isEmpty()) {
            statements.forEach(StatementContextBase::verifyStatement);
            addEffectiveSubstatementsImpl(statements);
        }
    }

    abstract void addEffectiveSubstatementsImpl(Collection<? extends Mutable<?, ?, ?>> statements);

    final List<ReactorStmtCtx<?, ?, ?>> addEffectiveSubstatementsImpl(final List<ReactorStmtCtx<?, ?, ?>> effective,
            final Collection<? extends Mutable<?, ?, ?>> statements) {
        final List<ReactorStmtCtx<?, ?, ?>> resized = beforeAddEffectiveStatement(effective, statements.size());
        final Collection<? extends StatementContextBase<?, ?, ?>> casted =
            (Collection<? extends StatementContextBase<?, ?, ?>>) statements;
        final ModelProcessingPhase phase = completedPhase;
        if (phase != null) {
            for (StatementContextBase<?, ?, ?> stmt : casted) {
                ensureCompletedPhase(stmt, phase);
            }
        }

        resized.addAll(casted);
        return resized;
    }

    abstract Iterable<ReactorStmtCtx<?, ?, ?>> effectiveChildrenToComplete();

    // exposed for InferredStatementContext only
    final void ensureCompletedPhase(final Mutable<?, ?, ?> stmt) {
        verifyStatement(stmt);
        final ModelProcessingPhase phase = completedPhase;
        if (phase != null) {
            ensureCompletedPhase((ReactorStmtCtx<?, ?, ?>) stmt, phase);
        }
    }

    // Make sure target statement has transitioned at least to specified phase. This method is just before we take
    // allow a statement to become our substatement. This is needed to ensure that every statement tree does not contain
    // any statements which did not complete the same phase as the root statement.
    private static void ensureCompletedPhase(final ReactorStmtCtx<?, ?, ?> stmt, final ModelProcessingPhase phase) {
        verify(stmt.tryToCompletePhase(phase), "Statement %s cannot complete phase %s", stmt, phase);
    }

    private static void verifyStatement(final Mutable<?, ?, ?> stmt) {
        verify(stmt instanceof ReactorStmtCtx, "Unexpected statement %s", stmt);
    }

    private List<ReactorStmtCtx<?, ?, ?>> beforeAddEffectiveStatement(final List<ReactorStmtCtx<?, ?, ?>> effective,
            final int toAdd) {
        // We cannot allow statement to be further mutated
        verify(completedPhase != ModelProcessingPhase.EFFECTIVE_MODEL, "Cannot modify finished statement at %s",
            sourceReference());
        return beforeAddEffectiveStatementUnsafe(effective, toAdd);
    }

    final List<ReactorStmtCtx<?, ?, ?>> beforeAddEffectiveStatementUnsafe(final List<ReactorStmtCtx<?, ?, ?>> effective,
            final int toAdd) {
        final ModelProcessingPhase inProgressPhase = getRoot().getSourceContext().getInProgressPhase();
        checkState(inProgressPhase == ModelProcessingPhase.FULL_DECLARATION
                || inProgressPhase == ModelProcessingPhase.EFFECTIVE_MODEL,
                "Effective statement cannot be added in declared phase at: %s", sourceReference());

        return effective.isEmpty() ? new ArrayList<>(toAdd) : effective;
    }

    @Override
    final E createEffective() {
        final E result = createEffective(definition.getFactory());
        if (result instanceof MutableStatement) {
            getRoot().addMutableStmtToSeal((MutableStatement) result);
        }
        return result;
    }

    @NonNull E createEffective(final StatementFactory<A, D, E> factory) {
        return createEffective(factory, this);
    }

    // Creates EffectiveStatement through full materialization
    static <A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> @NonNull E createEffective(
            final StatementFactory<A, D, E> factory, final StatementContextBase<A, D, E> ctx) {
        return factory.createEffective(ctx, ctx.streamDeclared(), ctx.streamEffective());
    }

    abstract Stream<? extends StmtContext<?, ?, ?>> streamDeclared();

    abstract Stream<? extends StmtContext<?, ?, ?>> streamEffective();

    @Override
    final boolean doTryToCompletePhase(final ModelProcessingPhase phase) {
        final boolean finished = phaseMutation.isEmpty() ? true : runMutations(phase);
        if (completeChildren(phase) && finished) {
            onPhaseCompleted(phase);
            return true;
        }
        return false;
    }

    private boolean completeChildren(final ModelProcessingPhase phase) {
        boolean finished = true;
        for (final StatementContextBase<?, ?, ?> child : mutableDeclaredSubstatements()) {
            finished &= child.tryToCompletePhase(phase);
        }
        for (final ReactorStmtCtx<?, ?, ?> child : effectiveChildrenToComplete()) {
            finished &= child.tryToCompletePhase(phase);
        }
        return finished;
    }

    private boolean runMutations(final ModelProcessingPhase phase) {
        final Collection<ContextMutation> openMutations = phaseMutation.get(phase);
        return openMutations.isEmpty() ? true : runMutations(phase, openMutations);
    }

    private boolean runMutations(final ModelProcessingPhase phase, final Collection<ContextMutation> openMutations) {
        boolean finished = true;
        final Iterator<ContextMutation> it = openMutations.iterator();
        while (it.hasNext()) {
            final ContextMutation current = it.next();
            if (current.isFinished()) {
                it.remove();
            } else {
                finished = false;
            }
        }

        if (openMutations.isEmpty()) {
            phaseMutation.removeAll(phase);
            cleanupPhaseMutation();
        }
        return finished;
    }

    private void cleanupPhaseMutation() {
        if (phaseMutation.isEmpty()) {
            phaseMutation = ImmutableMultimap.of();
        }
    }

    /**
     * Occurs on end of {@link ModelProcessingPhase} of source parsing.
     *
     * @param phase
     *            that was to be completed (finished)
     * @throws SourceException
     *             when an error occurred in source parsing
     */
    private void onPhaseCompleted(final ModelProcessingPhase phase) {
        completedPhase = phase;

        final Collection<OnPhaseFinished> listeners = phaseListeners.get(phase);
        if (!listeners.isEmpty()) {
            runPhaseListeners(phase, listeners);
        }
    }

    private void runPhaseListeners(final ModelProcessingPhase phase, final Collection<OnPhaseFinished> listeners) {
        final Iterator<OnPhaseFinished> listener = listeners.iterator();
        while (listener.hasNext()) {
            final OnPhaseFinished next = listener.next();
            if (next.phaseFinished(this, phase)) {
                listener.remove();
            }
        }

        if (listeners.isEmpty()) {
            phaseListeners.removeAll(phase);
            if (phaseListeners.isEmpty()) {
                phaseListeners = ImmutableMultimap.of();
            }
        }
    }

    /**
     * Ends declared section of current node.
     */
    void endDeclared(final ModelProcessingPhase phase) {
        definition.onDeclarationFinished(this, phase);
    }

    @Override
    final StatementDefinitionContext<A, D, E> definition() {
        return definition;
    }

    final <K, V, N extends ParserNamespace<K, V>> void onNamespaceItemAddedAction(final Class<N> type, final K key,
            final OnNamespaceItemAdded listener) {
        final Object potential = getFromNamespace(type, key);
        if (potential != null) {
            LOG.trace("Listener on {} key {} satisfied immediately", type, key);
            listener.namespaceItemAdded(this, type, key, potential);
            return;
        }

        getBehaviour(type).addListener(new KeyedValueAddedListener<>(this, key) {
            @Override
            void onValueAdded(final Object value) {
                listener.namespaceItemAdded(StatementContextBase.this, type, key, value);
            }
        });
    }

    final <K, V, N extends ParserNamespace<K, V>> void onNamespaceItemAddedAction(final Class<N> type,
            final ModelProcessingPhase phase, final NamespaceKeyCriterion<K> criterion,
            final OnNamespaceItemAdded listener) {
        final Optional<Entry<K, V>> existing = getFromNamespace(type, criterion);
        if (existing.isPresent()) {
            final Entry<K, V> entry = existing.get();
            LOG.debug("Listener on {} criterion {} found a pre-existing match: {}", type, criterion, entry);
            waitForPhase(entry.getValue(), type, phase, criterion, listener);
            return;
        }

        final NamespaceBehaviourWithListeners<K, V, N> behaviour = getBehaviour(type);
        behaviour.addListener(new PredicateValueAddedListener<K, V>(this) {
            @Override
            boolean onValueAdded(final K key, final V value) {
                if (criterion.match(key)) {
                    LOG.debug("Listener on {} criterion {} matched added key {}", type, criterion, key);
                    waitForPhase(value, type, phase, criterion, listener);
                    return true;
                }

                return false;
            }
        });
    }

    final <K, V, N extends ParserNamespace<K, V>> void selectMatch(final Class<N> type,
            final NamespaceKeyCriterion<K> criterion, final OnNamespaceItemAdded listener) {
        final Optional<Entry<K, V>> optMatch = getFromNamespace(type, criterion);
        checkState(optMatch.isPresent(), "Failed to find a match for criterion %s in namespace %s node %s", criterion,
            type, this);
        final Entry<K, V> match = optMatch.get();
        listener.namespaceItemAdded(StatementContextBase.this, type, match.getKey(), match.getValue());
    }

    final <K, V, N extends ParserNamespace<K, V>> void waitForPhase(final Object value, final Class<N> type,
            final ModelProcessingPhase phase, final NamespaceKeyCriterion<K> criterion,
            final OnNamespaceItemAdded listener) {
        ((StatementContextBase<?, ? ,?>) value).addPhaseCompletedListener(phase,
            (context, phaseCompleted) -> {
                selectMatch(type, criterion, listener);
                return true;
            });
    }

    private <K, V, N extends ParserNamespace<K, V>> NamespaceBehaviourWithListeners<K, V, N> getBehaviour(
            final Class<N> type) {
        final NamespaceBehaviour<K, V, N> behaviour = getBehaviourRegistry().getNamespaceBehaviour(type);
        checkArgument(behaviour instanceof NamespaceBehaviourWithListeners, "Namespace %s does not support listeners",
            type);

        return (NamespaceBehaviourWithListeners<K, V, N>) behaviour;
    }

    private static <T> Multimap<ModelProcessingPhase, T> newMultimap() {
        return Multimaps.newListMultimap(new EnumMap<>(ModelProcessingPhase.class), () -> new ArrayList<>(1));
    }

    /**
     * Adds {@link OnPhaseFinished} listener for a {@link ModelProcessingPhase} end. If the base has already completed
     * the listener is notified immediately.
     *
     * @param phase requested completion phase
     * @param listener listener to invoke
     * @throws NullPointerException if any of the arguments is null
     */
    void addPhaseCompletedListener(final ModelProcessingPhase phase, final OnPhaseFinished listener) {
        checkNotNull(phase, "Statement context processing phase cannot be null at: %s", sourceReference());
        checkNotNull(listener, "Statement context phase listener cannot be null at: %s", sourceReference());

        ModelProcessingPhase finishedPhase = completedPhase;
        while (finishedPhase != null) {
            if (phase.equals(finishedPhase)) {
                listener.phaseFinished(this, finishedPhase);
                return;
            }
            finishedPhase = finishedPhase.getPreviousPhase();
        }
        if (phaseListeners.isEmpty()) {
            phaseListeners = newMultimap();
        }

        phaseListeners.put(phase, listener);
    }

    /**
     * Adds a {@link ContextMutation} to a {@link ModelProcessingPhase}.
     *
     * @throws IllegalStateException when the mutation was registered after phase was completed
     */
    final void addMutation(final ModelProcessingPhase phase, final ContextMutation mutation) {
        ModelProcessingPhase finishedPhase = completedPhase;
        while (finishedPhase != null) {
            checkState(!phase.equals(finishedPhase), "Mutation registered after phase was completed at: %s",
                sourceReference());
            finishedPhase = finishedPhase.getPreviousPhase();
        }

        if (phaseMutation.isEmpty()) {
            phaseMutation = newMultimap();
        }
        phaseMutation.put(phase, mutation);
    }

    final void removeMutation(final ModelProcessingPhase phase, final ContextMutation mutation) {
        if (!phaseMutation.isEmpty()) {
            phaseMutation.remove(phase, mutation);
            cleanupPhaseMutation();
        }
    }

    @Override
    public <K, KT extends K, N extends StatementNamespace<K, ?, ?>> void addContext(final Class<@NonNull N> namespace,
            final KT key,final StmtContext<?, ?, ?> stmt) {
        addContextToNamespace(namespace, key, stmt);
    }

    @Override
    public Optional<? extends Mutable<?, ?, ?>> copyAsChildOf(final Mutable<?, ?, ?> parent, final CopyType type,
            final QNameModule targetModule) {
        checkEffectiveModelCompleted(this);
        return Optional.ofNullable(copyAsChildOfImpl(parent, type, targetModule));
    }

    private ReactorStmtCtx<A, D, E> copyAsChildOfImpl(final Mutable<?, ?, ?> parent, final CopyType type,
            final QNameModule targetModule) {
        final StatementSupport<A, D, E> support = definition.support();
        final CopyPolicy policy = support.copyPolicy();
        switch (policy) {
            case CONTEXT_INDEPENDENT:
                if (allSubstatementsContextIndependent()) {
                    return replicaAsChildOf(parent);
                }

                // fall through
            case DECLARED_COPY:
                // FIXME: ugly cast
                return (ReactorStmtCtx<A, D, E>) parent.childCopyOf(this, type, targetModule);
            case IGNORE:
                return null;
            case REJECT:
                throw new IllegalStateException("Statement " + support.getPublicView() + " should never be copied");
            default:
                throw new IllegalStateException("Unhandled policy " + policy);
        }
    }

    @Override
    final ReactorStmtCtx<?, ?, ?> asEffectiveChildOf(final StatementContextBase<?, ?, ?> parent, final CopyType type,
            final QNameModule targetModule) {
        final ReactorStmtCtx<A, D, E> copy = copyAsChildOfImpl(parent, type, targetModule);
        if (copy == null) {
            // The statement fizzled, this should never happen, perhaps a verify()?
            return null;
        }

        parent.ensureCompletedPhase(copy);
        return canReuseCurrent(copy) ? replicaAsChildOf(parent) : copy;
    }

    private boolean canReuseCurrent(final ReactorStmtCtx<A, D, E> copy) {
        // Defer to statement factory to see if we can reuse this object. If we can and have only context-independent
        // substatements we can reuse the object. More complex cases are handled indirectly via the copy.
        return definition.getFactory().canReuseCurrent(copy, this, buildEffective().effectiveSubstatements())
            && allSubstatementsContextIndependent();
    }

    // FIXME: YANGTOOLS-1195: we really want to compute (and cache) the summary for substatements.
    //
    // For now we just check if there are any substatements, but we really want to ask:
    //
    //   Are all substatements (recursively) CONTEXT_INDEPENDENT as well?
    //
    // Which is something we want to compute once and store. This needs to be implemented.
    private boolean allSubstatementsContextIndependent() {
        return hasEmptySubstatements();
    }

    @Override
    public final Mutable<?, ?, ?> childCopyOf(final StmtContext<?, ?, ?> stmt, final CopyType type,
            final QNameModule targetModule) {
        checkEffectiveModelCompleted(stmt);
        checkArgument(stmt instanceof StatementContextBase, "Unsupported statement %s", stmt);
        return childCopyOf((StatementContextBase<?, ?, ?>) stmt, type, targetModule);
    }

    private <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<X, Y>> Mutable<X, Y, Z> childCopyOf(
            final StatementContextBase<X, Y, Z> original, final CopyType type, final QNameModule targetModule) {
        final Optional<StatementSupport<?, ?, ?>> implicitParent = definition.getImplicitParentFor(
            original.publicDefinition());

        final StatementContextBase<X, Y, Z> result;
        final InferredStatementContext<X, Y, Z> copy;

        if (implicitParent.isPresent()) {
            final StatementDefinitionContext<?, ?, ?> def = new StatementDefinitionContext<>(implicitParent.get());
            result = new SubstatementContext(this, def, original.sourceReference(), original.rawArgument(),
                original.argument(), type);

            final CopyType childCopyType;
            switch (type) {
                case ADDED_BY_AUGMENTATION:
                    childCopyType = CopyType.ORIGINAL;
                    break;
                case ADDED_BY_USES_AUGMENTATION:
                    childCopyType = CopyType.ADDED_BY_USES;
                    break;
                case ADDED_BY_USES:
                case ORIGINAL:
                default:
                    childCopyType = type;
            }

            copy = new InferredStatementContext<>(result, original, childCopyType, type, targetModule);
            result.addEffectiveSubstatement(copy);
        } else {
            result = copy = new InferredStatementContext<>(this, original, type, type, targetModule);
        }

        original.definition.onStatementAdded(copy);
        return result;
    }

    @Override
    public final ReactorStmtCtx<A, D, E> replicaAsChildOf(final Mutable<?, ?, ?> parent) {
        checkArgument(parent instanceof StatementContextBase, "Unsupported parent %s", parent);
        return replicaAsChildOf((StatementContextBase<?, ?, ?>) parent);
    }

    final @NonNull ReplicaStatementContext<A, D, E> replicaAsChildOf(final StatementContextBase<?, ?, ?> stmt) {
        return new ReplicaStatementContext<>(stmt, this);
    }

    private static void checkEffectiveModelCompleted(final StmtContext<?, ?, ?> stmt) {
        final ModelProcessingPhase phase = stmt.getCompletedPhase();
        checkState(phase == ModelProcessingPhase.EFFECTIVE_MODEL,
                "Attempted to copy statement %s which has completed phase %s", stmt, phase);
    }

    @Beta
    public final boolean hasImplicitParentSupport() {
        return definition.getFactory() instanceof ImplicitParentAwareStatementSupport;
    }

    @Beta
    public final StatementContextBase<?, ?, ?> wrapWithImplicit(final StatementContextBase<?, ?, ?> original) {
        final Optional<StatementSupport<?, ?, ?>> optImplicit = definition.getImplicitParentFor(
            original.publicDefinition());
        if (optImplicit.isEmpty()) {
            return original;
        }

        final StatementDefinitionContext<?, ?, ?> def = new StatementDefinitionContext<>(optImplicit.get());
        final CopyType type = original.history().getLastOperation();
        final SubstatementContext<?, ?, ?> result = new SubstatementContext(original.getParentContext(), def,
            original.sourceReference(), original.rawArgument(), original.argument(), type);

        result.addEffectiveSubstatement(original.reparent(result));
        result.setCompletedPhase(original.getCompletedPhase());
        return result;
    }

    abstract StatementContextBase<A, D, E> reparent(StatementContextBase<?, ?, ?> newParent);

    /**
     * Indicate that the set of substatements is empty. This is a preferred shortcut to substatement stream filtering.
     *
     * @return True if {@link #allSubstatements()} and {@link #allSubstatementsStream()} would return an empty stream.
     */
    abstract boolean hasEmptySubstatements();
}
