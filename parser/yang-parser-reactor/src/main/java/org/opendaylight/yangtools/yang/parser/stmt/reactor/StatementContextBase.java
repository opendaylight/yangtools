/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
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
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase.ExecutionOrder;
import org.opendaylight.yangtools.yang.parser.spi.meta.MutableStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceKeyCriterion;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementFactory;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport.CopyPolicy;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.UndeclaredStatementFactory;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.NamespaceAccess.KeyedValueAddedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core reactor statement implementation of {@link Mutable}.
 *
 * @param <A> Argument type
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
abstract class StatementContextBase<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends ReactorStmtCtx<A, D, E> implements CopyHistory {
    /**
     * Event listener when an item is added to model namespace.
     */
    interface OnNamespaceItemAdded {
        /**
         * Invoked whenever a new item is added to a namespace.
         */
        void namespaceItemAdded(StatementContextBase<?, ?, ?> context, ParserNamespace<?, ?> namespace, Object key,
            Object value);
    }

    /**
     * Event listener when a parsing {@link ModelProcessingPhase} is completed.
     */
    interface OnPhaseFinished {
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

    // Bottom 4 bits, encoding a CopyHistory, aight?
    private static final byte COPY_ORIGINAL              = 0x00;
    private static final byte COPY_LAST_TYPE_MASK        = 0x03;
    @Deprecated(since = "7.0.9", forRemoval = true)
    private static final byte COPY_ADDED_BY_USES         = 0x04;
    private static final byte COPY_ADDED_BY_AUGMENTATION = 0x08;

    // Top four bits, of which we define the topmost two to 0. We use the bottom two to encode last CopyType, aight?
    private static final int COPY_CHILD_TYPE_SHIFT       = 4;

    private static final CopyType @NonNull [] COPY_TYPE_VALUES = CopyType.values();

    static {
        final int copyTypes = COPY_TYPE_VALUES.length;
        // This implies CopyType.ordinal() is <= COPY_TYPE_MASK
        verify(copyTypes == COPY_LAST_TYPE_MASK + 1, "Unexpected %s CopyType values", copyTypes);
    }

    /**
     * 8 bits worth of instance storage. This is treated as a constant bit field with following structure:
     * <pre>
     *   <code>
     * |7|6|5|4|3|2|1|0|
     * |0 0|cct|a|u|lst|
     *   </code>
     * </pre>
     *
     * <p>
     * The four allocated fields are:
     * <ul>
     *   <li>{@code lst}, encoding the four states corresponding to {@link CopyHistory#getLastOperation()}</li>
     *   <li>{@code u}, encoding {@link #isAddedByUses()}</li>
     *   <li>{@code a}, encoding {@link #isAugmenting()}</li>
     *   <li>{@code cct} encoding {@link #childCopyType()}</li>
     * </ul>
     * We still have two unused bits.
     */
    private final byte bitsAight;

    // Note: this field can strictly be derived in InferredStatementContext, but it forms the basis of many of our
    //       operations, hence we want to keep it close by.
    private final @NonNull StatementDefinitionContext<A, D, E> definition;

    // TODO: consider keying by Byte equivalent of ExecutionOrder
    private Multimap<ModelProcessingPhase, OnPhaseFinished> phaseListeners = ImmutableMultimap.of();
    private Multimap<ModelProcessingPhase, ContextMutation> phaseMutation = ImmutableMultimap.of();

    private List<StmtContext<?, ?, ?>> effectOfStatement = ImmutableList.of();

    /**
     * {@link ModelProcessingPhase.ExecutionOrder} value of current {@link ModelProcessingPhase} of this statement.
     */
    private byte executionOrder;

    // TODO: we a single byte of alignment shadow left, we should think how we can use it to cache information we build
    //       during InferredStatementContext.tryToReusePrototype(). We usually end up being routed to
    //       copyAsChildOfImpl() -- which performs an eager instantiation and checks for changes afterwards. We should
    //       be able to capture how parent scope affects the copy in a few bits. If we can do that, than we can reap
    //       the benefits by just examining new parent context and old parent context contribution to the state. If
    //       their impact is the same, we can skip instantiation of statements and directly reuse them (individually,
    //       or as a complete file).
    //
    //       Whatever we end up tracking, we need to track two views of that -- for the statement itself
    //       (sans substatements) and a summary of substatements. I think it should be possible to get this working
    //       with 2x5bits -- we have up to 15 mutable bits available if we share the field with implicitDeclaredFlag.

    // Copy constructor used by subclasses to implement reparent()
    StatementContextBase(final StatementContextBase<A, D, E> original) {
        super(original);
        this.bitsAight = original.bitsAight;
        this.definition = original.definition;
        this.executionOrder = original.executionOrder;
    }

    StatementContextBase(final StatementDefinitionContext<A, D, E> def) {
        this.definition = requireNonNull(def);
        this.bitsAight = COPY_ORIGINAL;
    }

    StatementContextBase(final StatementDefinitionContext<A, D, E> def, final CopyType copyType) {
        this.definition = requireNonNull(def);
        this.bitsAight = (byte) copyFlags(copyType);
    }

    StatementContextBase(final StatementContextBase<A, D, E> prototype, final CopyType copyType,
            final CopyType childCopyType) {
        this.definition = prototype.definition;
        this.bitsAight = (byte) (copyFlags(copyType)
            | prototype.bitsAight & ~COPY_LAST_TYPE_MASK | childCopyType.ordinal() << COPY_CHILD_TYPE_SHIFT);
    }

    private static int copyFlags(final CopyType copyType) {
        return historyFlags(copyType) | copyType.ordinal();
    }

    private static byte historyFlags(final CopyType copyType) {
        return switch (copyType) {
            case ADDED_BY_AUGMENTATION -> COPY_ADDED_BY_AUGMENTATION;
            case ADDED_BY_USES -> COPY_ADDED_BY_USES;
            case ADDED_BY_USES_AUGMENTATION -> COPY_ADDED_BY_AUGMENTATION | COPY_ADDED_BY_USES;
            case ORIGINAL -> COPY_ORIGINAL;
        };
    }

    @Override
    public final Collection<? extends StmtContext<?, ?, ?>> getEffectOfStatement() {
        return effectOfStatement;
    }

    @Override
    public final void addAsEffectOfStatement(final Collection<? extends StmtContext<?, ?, ?>> ctxs) {
        if (ctxs.isEmpty()) {
            return;
        }

        if (effectOfStatement.isEmpty()) {
            effectOfStatement = new ArrayList<>(ctxs.size());
        }
        effectOfStatement.addAll(ctxs);
    }

    //
    // CopyHistory integration
    //

    @Override
    public final CopyHistory history() {
        return this;
    }

    @Override
    @Deprecated(since = "7.0.9", forRemoval = true)
    public final boolean isAddedByUses() {
        return (bitsAight & COPY_ADDED_BY_USES) != 0;
    }

    @Override
    @Deprecated(since = "8.0.0")
    public final boolean isAugmenting() {
        return (bitsAight & COPY_ADDED_BY_AUGMENTATION) != 0;
    }

    @Override
    public final CopyType getLastOperation() {
        return COPY_TYPE_VALUES[bitsAight & COPY_LAST_TYPE_MASK];
    }

    // This method exists only for space optimization of InferredStatementContext
    final CopyType childCopyType() {
        return COPY_TYPE_VALUES[bitsAight >> COPY_CHILD_TYPE_SHIFT & COPY_LAST_TYPE_MASK];
    }

    //
    // Inference completion tracking
    //

    @Override
    final byte executionOrder() {
        return executionOrder;
    }

    // FIXME: this should be propagated through a correct constructor
    @Deprecated
    final void setCompletedPhase(final ModelProcessingPhase completedPhase) {
        executionOrder = completedPhase.executionOrder();
    }

    @Override
    public final <K, V> void addToNs(final ParserNamespace<K, V> type, final K key, final V value) {
        addToNamespace(type, key, value);
    }

    static final Collection<? extends Mutable<?, ?, ?>> mutableEffectiveSubstatements(
            final List<ReactorStmtCtx<?, ?, ?>> effective) {
        return effective instanceof ImmutableCollection ? effective : Collections.unmodifiableCollection(effective);
    }

    private static List<ReactorStmtCtx<?, ?, ?>> shrinkEffective(final List<ReactorStmtCtx<?, ?, ?>> effective) {
        return effective.isEmpty() ? ImmutableList.of() : effective;
    }

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

    @Override
    public final <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<X, Y>>
            Mutable<X, Y, Z> createUndeclaredSubstatement(final StatementSupport<X, Y, Z> support, final X arg) {
        requireNonNull(support);
        checkArgument(support instanceof UndeclaredStatementFactory, "Unsupported statement support %s", support);

        final var ret = new UndeclaredStmtCtx<>(this, support, arg);
        support.onStatementAdded(ret);
        return ret;
    }

    final List<ReactorStmtCtx<?, ?, ?>> addEffectiveSubstatement(final List<ReactorStmtCtx<?, ?, ?>> effective,
            final Mutable<?, ?, ?> substatement) {
        final ReactorStmtCtx<?, ?, ?> stmt = verifyStatement(substatement);
        final List<ReactorStmtCtx<?, ?, ?>> resized = beforeAddEffectiveStatement(effective, 1);
        ensureCompletedExecution(stmt);
        resized.add(stmt);
        return resized;
    }

    static final void afterAddEffectiveSubstatement(final Mutable<?, ?, ?> substatement) {
        // Undeclared statements still need to have 'onDeclarationFinished()' triggered
        if (substatement instanceof UndeclaredStmtCtx<?, ?, ?> undeclared) {
            finishDeclaration(undeclared);
        }
    }

    // Split out to keep generics working without a warning
    private static <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<X, Y>> void finishDeclaration(
            final @NonNull UndeclaredStmtCtx<X, Y, Z> substatement) {
        substatement.definition().onDeclarationFinished(substatement, ModelProcessingPhase.FULL_DECLARATION);
    }

    @Override
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
        final Collection<? extends ReactorStmtCtx<?, ?, ?>> casted =
            (Collection<? extends ReactorStmtCtx<?, ?, ?>>) statements;
        if (executionOrder != ExecutionOrder.NULL) {
            for (ReactorStmtCtx<?, ?, ?> stmt : casted) {
                ensureCompletedExecution(stmt, executionOrder);
            }
        }

        resized.addAll(casted);
        return resized;
    }

    abstract Iterator<ReactorStmtCtx<?, ?, ?>> effectiveChildrenToComplete();

    // Make sure target statement has transitioned at least to our phase (if we have one). This method is just before we
    // take allow a statement to become our substatement. This is needed to ensure that every statement tree does not
    // contain any statements which did not complete the same phase as the root statement.
    final void ensureCompletedExecution(final ReactorStmtCtx<?, ?, ?> stmt) {
        if (executionOrder != ExecutionOrder.NULL) {
            ensureCompletedExecution(stmt, executionOrder);
        }
    }

    private static void ensureCompletedExecution(final ReactorStmtCtx<?, ?, ?> stmt, final byte executionOrder) {
        verify(stmt.tryToCompletePhase(executionOrder), "Statement %s cannot complete phase %s", stmt, executionOrder);
    }

    // exposed for InferredStatementContext only
    static final ReactorStmtCtx<?, ?, ?> verifyStatement(final Mutable<?, ?, ?> stmt) {
        if (stmt instanceof ReactorStmtCtx<?, ?, ?> reactorStmt) {
            return reactorStmt;
        }
        throw new VerifyException("Unexpected statement " + stmt);
    }

    private List<ReactorStmtCtx<?, ?, ?>> beforeAddEffectiveStatement(final List<ReactorStmtCtx<?, ?, ?>> effective,
            final int toAdd) {
        // We cannot allow statement to be further mutated.
        // TODO: we really want to say 'not NULL and not at or after EFFECTIVE_MODEL here. This will matter if we have
        //       a phase after EFFECTIVE_MODEL
        verify(executionOrder != ExecutionOrder.EFFECTIVE_MODEL, "Cannot modify finished statement at %s",
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
        if (result instanceof MutableStatement mutable) {
            getRoot().addMutableStmtToSeal(mutable);
        }
        return result;
    }

    abstract @NonNull E createEffective(@NonNull StatementFactory<A, D, E> factory);

    /**
     * Return a stream of declared statements which can be built into an {@link EffectiveStatement}, as per
     * {@link StmtContext#buildEffective()} contract.
     *
     * @return Stream of supported declared statements.
     */
    // FIXME: we really want to unify this with streamEffective(), under its name
    abstract Stream<? extends @NonNull ReactorStmtCtx<?, ?, ?>> streamDeclared();

    /**
     * Return a stream of inferred statements which can be built into an {@link EffectiveStatement}, as per
     * {@link StmtContext#buildEffective()} contract.
     *
     * @return Stream of supported effective statements.
     */
    // FIXME: this method is currently a misnomer, but unifying with streamDeclared() would make this accurate again
    abstract @NonNull Stream<? extends @NonNull ReactorStmtCtx<?, ?, ?>> streamEffective();

    @Override
    final boolean doTryToCompletePhase(final byte targetOrder) {
        final boolean finished = phaseMutation.isEmpty() || runMutations(targetOrder);
        if (completeChildren(targetOrder) && finished) {
            onPhaseCompleted(targetOrder);
            return true;
        }
        return false;
    }

    private boolean completeChildren(final byte targetOrder) {
        boolean finished = true;
        for (final StatementContextBase<?, ?, ?> child : mutableDeclaredSubstatements()) {
            finished &= child.tryToCompletePhase(targetOrder);
        }
        final var it = effectiveChildrenToComplete();
        while (it.hasNext()) {
            finished &= it.next().tryToCompletePhase(targetOrder);
        }
        return finished;
    }

    private boolean runMutations(final byte targetOrder) {
        final ModelProcessingPhase phase = verifyNotNull(ModelProcessingPhase.ofExecutionOrder(targetOrder));
        final Collection<ContextMutation> openMutations = phaseMutation.get(phase);
        return openMutations.isEmpty() || runMutations(phase, openMutations);
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
     * Occurs on end of {@link ModelProcessingPhase} of source parsing. This method must not be called with
     * {@code executionOrder} equal to {@link ExecutionOrder#NULL}.
     *
     * @param phase that was to be completed (finished)
     * @throws SourceException when an error occurred in source parsing
     */
    private void onPhaseCompleted(final byte completedOrder) {
        executionOrder = completedOrder;
        if (completedOrder == ExecutionOrder.EFFECTIVE_MODEL) {
            // We have completed effective model, substatements are guaranteed not to change
            summarizeSubstatementPolicy();
        }

        final ModelProcessingPhase phase = verifyNotNull(ModelProcessingPhase.ofExecutionOrder(completedOrder));
        final Collection<OnPhaseFinished> listeners = phaseListeners.get(phase);
        if (!listeners.isEmpty()) {
            runPhaseListeners(phase, listeners);
        }
    }

    private void summarizeSubstatementPolicy() {
        if (definition().support().copyPolicy() == CopyPolicy.EXACT_REPLICA || noSensitiveSubstatements()) {
            setAllSubstatementsContextIndependent();
        }
    }

    /**
     * Determine whether any substatements are copy-sensitive as determined by {@link StatementSupport#copyPolicy()}.
     * Only {@link CopyPolicy#CONTEXT_INDEPENDENT}, {@link CopyPolicy#EXACT_REPLICA} and {@link CopyPolicy#IGNORE} are
     * copy-insensitive. Note that statements which are not {@link StmtContext#isSupportedToBuildEffective()} are all
     * considered copy-insensitive.
     *
     * <p>
     * Implementations are expected to call {@link #noSensitiveSubstatements()} to actually traverse substatement sets.
     *
     * @return True if no substatements require copy-sensitive handling
     */
    abstract boolean noSensitiveSubstatements();

    /**
     * Determine whether any of the provided substatements are context-sensitive for purposes of implementing
     * {@link #noSensitiveSubstatements()}.
     *
     * @param substatements Substatements to check
     * @return True if no substatements require context-sensitive handling
     */
    static boolean noSensitiveSubstatements(final Collection<? extends ReactorStmtCtx<?, ?, ?>> substatements) {
        for (ReactorStmtCtx<?, ?, ?> stmt : substatements) {
            if (stmt.isSupportedToBuildEffective()) {
                if (!stmt.allSubstatementsContextIndependent()) {
                    // This is a recursive property
                    return false;
                }

                switch (stmt.definition().support().copyPolicy()) {
                    case CONTEXT_INDEPENDENT:
                    case EXACT_REPLICA:
                    case IGNORE:
                        break;
                    default:
                        return false;
                }
            }
        }
        return true;
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

    @Override
    final StatementDefinitionContext<A, D, E> definition() {
        return definition;
    }

    final <K, V> void onNamespaceItemAddedAction(final ParserNamespace<K, V> namespace, final K key,
            final OnNamespaceItemAdded listener) {
        final var access = accessNamespace(namespace);
        final var potential = access.valueFrom(this, key);
        if (potential != null) {
            LOG.trace("Listener on {} key {} satisfied immediately", namespace, key);
            listener.namespaceItemAdded(this, namespace, key, potential);
            return;
        }

        access.addListener(key, new KeyedValueAddedListener<>(this) {
            @Override
            void onValueAdded(final K key, final V value) {
                listener.namespaceItemAdded(StatementContextBase.this, namespace, key, value);
            }
        });
    }

    final <K, V> void onNamespaceItemAddedAction(final ParserNamespace<K, V> namespace,
            final ModelProcessingPhase phase, final NamespaceKeyCriterion<K> criterion,
            final OnNamespaceItemAdded listener) {
        final var access = accessNamespace(namespace);
        final var entry = access.entryFrom(this, criterion);
        if (entry != null) {
            LOG.debug("Listener on {} criterion {} found a pre-existing match: {}", namespace, criterion, entry);
            waitForPhase(entry.getValue(), access, phase, criterion, listener);
            return;
        }

        access.addListener((key, value) -> {
            if (criterion.match(key)) {
                LOG.debug("Listener on {} criterion {} matched added key {}", namespace, criterion, key);
                waitForPhase(value, access, phase, criterion, listener);
                return true;
            }
            return false;
        });
    }

    private <K, V> void waitForPhase(final Object value, final NamespaceAccess<K, V> access,
            final ModelProcessingPhase phase, final NamespaceKeyCriterion<K> criterion,
            final OnNamespaceItemAdded listener) {
        ((StatementContextBase<?, ?, ?>) value).addPhaseCompletedListener(phase, (context, phaseCompleted) -> {
            final var match = access.entryFrom(this, criterion);
            if (match == null) {
                throw new IllegalStateException("Failed to find a match for criterion %s in namespace %s node %s"
                    .formatted(criterion, access.namespace(), this));
            }
            listener.namespaceItemAdded(this, access.namespace(), match.getKey(), match.getValue());
            return true;
        });
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
        requireNonNull(phase, "Statement context processing phase cannot be null");
        requireNonNull(listener, "Statement context phase listener cannot be null");

        ModelProcessingPhase finishedPhase = ModelProcessingPhase.ofExecutionOrder(executionOrder);
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
        checkState(executionOrder < phase.executionOrder(), "Mutation registered after phase was completed at: %s",
            sourceReference());

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
    public final Optional<Mutable<A, D, E>> copyAsChildOf(final Mutable<?, ?, ?> parent, final CopyType type,
            final QNameModule targetModule) {
        checkEffectiveModelCompleted(this);
        return Optional.ofNullable(copyAsChildOfImpl(parent, type, targetModule));
    }

    private @Nullable ReactorStmtCtx<A, D, E> copyAsChildOfImpl(final Mutable<?, ?, ?> parent, final CopyType type,
            final QNameModule targetModule) {
        final StatementSupport<A, D, E> support = definition.support();
        final CopyPolicy policy = support.copyPolicy();
        switch (policy) {
            case EXACT_REPLICA:
                return replicaAsChildOf(parent);
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
        if (!isSupportedToBuildEffective() || !isSupportedByFeatures()) {
            // Do not create effective copies, as they cannot be built anyway
            return null;
        }

        final var copy = copyAsChildOfImpl(parent, type, targetModule);
        if (copy == null) {
            // The statement fizzled, this should never happen, perhaps a verify()?
            return null;
        }

        parent.ensureCompletedExecution(copy);
        return canReuseCurrent(copy) ? this : copy;
    }

    private boolean canReuseCurrent(final @NonNull ReactorStmtCtx<A, D, E> copy) {
        // Defer to statement factory to see if we can reuse this object. If we can and have only context-independent
        // substatements we can reuse the object. More complex cases are handled indirectly via the copy.
        return definition.getFactory().canReuseCurrent(copy, this, buildEffective().effectiveSubstatements())
            && allSubstatementsContextIndependent();
    }

    @Override
    public final Mutable<?, ?, ?> childCopyOf(final StmtContext<?, ?, ?> stmt, final CopyType type,
            final QNameModule targetModule) {
        checkEffectiveModelCompleted(stmt);
        if (stmt instanceof StatementContextBase<?, ?, ?> base) {
            return childCopyOf(base, type, targetModule);
        } else if (stmt instanceof ReplicaStatementContext<?, ?, ?> replica) {
            return replica.replicaAsChildOf(this);
        } else {
            throw new IllegalArgumentException("Unsupported statement " + stmt);
        }
    }

    private <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<X, Y>> Mutable<X, Y, Z> childCopyOf(
            final StatementContextBase<X, Y, Z> original, final CopyType type, final QNameModule targetModule) {
        final var implicitParent = definition.getImplicitParentFor(this, original.publicDefinition());

        final StatementContextBase<X, Y, Z> result;
        final InferredStatementContext<X, Y, Z> copy;

        if (implicitParent.isPresent()) {
            result = new UndeclaredStmtCtx(this, implicitParent.orElseThrow(), original, type);

            final CopyType childCopyType = switch (type) {
                case ADDED_BY_AUGMENTATION -> CopyType.ORIGINAL;
                case ADDED_BY_USES_AUGMENTATION -> CopyType.ADDED_BY_USES;
                case ADDED_BY_USES, ORIGINAL -> type;
            };
            copy = new InferredStatementContext<>(result, original, childCopyType, type, targetModule);
            result.addEffectiveSubstatement(copy);
            result.definition.onStatementAdded(result);
        } else {
            result = copy = new InferredStatementContext<>(this, original, type, type, targetModule);
        }

        original.definition.onStatementAdded(copy);
        return result;
    }

    @Override
    final ReplicaStatementContext<A, D, E> replicaAsChildOf(final StatementContextBase<?, ?, ?> parent) {
        return new ReplicaStatementContext<>(parent, this);
    }

    private static void checkEffectiveModelCompleted(final StmtContext<?, ?, ?> stmt) {
        final ModelProcessingPhase phase = stmt.getCompletedPhase();
        checkState(phase == ModelProcessingPhase.EFFECTIVE_MODEL,
                "Attempted to copy statement %s which has completed phase %s", stmt, phase);
    }

    @Override
    public final boolean hasImplicitParentSupport() {
        return definition.getFactory() instanceof ImplicitParentAwareStatementSupport;
    }

    @Override
    public final StmtContext<?, ?, ?> wrapWithImplicit(final StmtContext<?, ?, ?> original) {
        final var optImplicit = definition.getImplicitParentFor(this, original.publicDefinition());
        if (optImplicit.isEmpty()) {
            return original;
        }
        if (original instanceof StatementContextBase<?, ?, ?> origBase) {
            final var result = new UndeclaredStmtCtx<>(origBase, optImplicit.orElseThrow());
            result.addEffectiveSubstatement(origBase.reparent(result));
            result.setCompletedPhase(original.getCompletedPhase());
            return result;
        }
        throw new IllegalArgumentException("Unsupported original " + original);
    }

    abstract StatementContextBase<A, D, E> reparent(StatementContextBase<?, ?, ?> newParent);

    /**
     * Indicate that the set of substatements is empty. This is a preferred shortcut to substatement stream filtering.
     *
     * @return True if {@link #allSubstatements()} and {@link #allSubstatementsStream()} would return an empty stream.
     */
    abstract boolean hasEmptySubstatements();
}
