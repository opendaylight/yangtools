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
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.util.OptionalBoolean;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceKeyCriterion;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.ImplicitSubstatement;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter.ResumedStatement;
import org.opendaylight.yangtools.yang.parser.spi.source.SupportedFeaturesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.SupportedFeaturesNamespace.SupportedFeatures;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.NamespaceBehaviourWithListeners.KeyedValueAddedListener;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.NamespaceBehaviourWithListeners.PredicateValueAddedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StatementContextBase<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends NamespaceStorageSupport implements Mutable<A, D, E>, ResumedStatement {
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

    private final @NonNull StatementDefinitionContext<A, D, E> definition;
    private final @NonNull StatementSourceReference statementDeclSource;
    private final StmtContext<?, ?, ?> originalCtx;
    private final CopyHistory copyHistory;
    private final String rawArgument;

    private Multimap<ModelProcessingPhase, OnPhaseFinished> phaseListeners = ImmutableMultimap.of();
    private Multimap<ModelProcessingPhase, ContextMutation> phaseMutation = ImmutableMultimap.of();
    private List<Mutable<?, ?, ?>> effective = ImmutableList.of();
    private List<StmtContext<?, ?, ?>> effectOfStatement = ImmutableList.of();
    private StatementMap substatements = StatementMap.empty();

    private boolean isSupportedToBuildEffective = true;
    private @Nullable ModelProcessingPhase completedPhase;
    private @Nullable D declaredInstance;
    private @Nullable E effectiveInstance;

    // BooleanFields value
    private byte supportedByFeatures;

    private boolean fullyDefined;

    StatementContextBase(final StatementDefinitionContext<A, D, E> def, final StatementSourceReference ref,
            final String rawArgument) {
        this.definition = requireNonNull(def);
        this.statementDeclSource = requireNonNull(ref);
        this.rawArgument = def.internArgument(rawArgument);
        this.copyHistory = CopyHistory.original();
        this.originalCtx = null;
    }

    StatementContextBase(final StatementDefinitionContext<A, D, E> def, final StatementSourceReference ref,
        final String rawArgument, final CopyType copyType) {
        this.definition = requireNonNull(def);
        this.statementDeclSource = requireNonNull(ref);
        this.rawArgument = rawArgument;
        this.copyHistory = CopyHistory.of(copyType, CopyHistory.original());
        this.originalCtx = null;
    }

    StatementContextBase(final StatementContextBase<A, D, E> original, final CopyType copyType) {
        this.definition = original.definition;
        this.statementDeclSource = original.statementDeclSource;
        this.rawArgument = original.rawArgument;
        this.copyHistory = CopyHistory.of(copyType, original.getCopyHistory());
        this.originalCtx = original.getOriginalCtx().orElse(original);
    }

    @Override
    public Collection<? extends StmtContext<?, ?, ?>> getEffectOfStatement() {
        return effectOfStatement;
    }

    @Override
    public void addAsEffectOfStatement(final StmtContext<?, ?, ?> ctx) {
        if (effectOfStatement.isEmpty()) {
            effectOfStatement = new ArrayList<>(1);
        }
        effectOfStatement.add(ctx);
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
    public boolean isSupportedByFeatures() {
        if (OptionalBoolean.isPresent(supportedByFeatures)) {
            return OptionalBoolean.get(supportedByFeatures);
        }

        if (isIgnoringIfFeatures()) {
            supportedByFeatures = OptionalBoolean.of(true);
            return true;
        }

        final boolean isParentSupported = isParentSupportedByFeatures();
        /*
         * If parent is not supported, then this context is also not supported.
         * So we do not need to check if-features statements of this context and
         * we can return false immediately.
         */
        if (!isParentSupported) {
            supportedByFeatures = OptionalBoolean.of(false);
            return false;
        }

        /*
         * If parent is supported, we need to check if-features statements of
         * this context.
         */
        // If the set of supported features has not been provided, all features are supported by default.
        final Set<QName> supportedFeatures = getFromNamespace(SupportedFeaturesNamespace.class,
                SupportedFeatures.SUPPORTED_FEATURES);
        final boolean ret = supportedFeatures == null ? true
                : StmtContextUtils.checkFeatureSupport(this, supportedFeatures);

        supportedByFeatures = OptionalBoolean.of(ret);
        return ret;
    }

    protected abstract boolean isParentSupportedByFeatures();

    protected abstract boolean isIgnoringIfFeatures();

    protected abstract boolean isIgnoringConfig();

    @Override
    public boolean isSupportedToBuildEffective() {
        return isSupportedToBuildEffective;
    }

    @Override
    public void setIsSupportedToBuildEffective(final boolean isSupportedToBuildEffective) {
        this.isSupportedToBuildEffective = isSupportedToBuildEffective;
    }

    @Override
    public CopyHistory getCopyHistory() {
        return copyHistory;
    }

    @Override
    public Optional<StmtContext<?, ?, ?>> getOriginalCtx() {
        return Optional.ofNullable(originalCtx);
    }

    @Override
    public ModelProcessingPhase getCompletedPhase() {
        return completedPhase;
    }

    @Override
    public void setCompletedPhase(final ModelProcessingPhase completedPhase) {
        this.completedPhase = completedPhase;
    }

    @Override
    public abstract StatementContextBase<?, ?, ?> getParentContext();

    /**
     * Returns the model root for this statement.
     *
     * @return root context of statement
     */
    @Override
    public abstract RootStatementContext<?, ?, ?> getRoot();

    /**
     * Returns the origin of the statement.
     *
     * @return origin of statement
     */
    @Override
    public StatementSource getStatementSource() {
        return statementDeclSource.getStatementSource();
    }

    /**
     * Returns a reference to statement source.
     *
     * @return reference of statement source
     */
    @Override
    public StatementSourceReference getStatementSourceReference() {
        return statementDeclSource;
    }

    @Override
    public final String rawStatementArgument() {
        return rawArgument;
    }

    @Override
    public Collection<? extends StmtContext<?, ?, ?>> declaredSubstatements() {
        return substatements.values();
    }

    @Override
    public Collection<? extends Mutable<?, ?, ?>> mutableDeclaredSubstatements() {
        return substatements.values();
    }

    @Override
    public Collection<? extends StmtContext<?, ?, ?>> effectiveSubstatements() {
        return mutableEffectiveSubstatements();
    }

    @Override
    public Collection<? extends Mutable<?, ?, ?>> mutableEffectiveSubstatements() {
        if (effective instanceof ImmutableCollection) {
            return effective;
        }

        return Collections.unmodifiableCollection(effective);
    }

    public void removeStatementsFromEffectiveSubstatements(
            final Collection<? extends StmtContext<?, ?, ?>> statements) {
        if (!effective.isEmpty()) {
            effective.removeAll(statements);
            shrinkEffective();
        }
    }

    private void shrinkEffective() {
        if (effective.isEmpty()) {
            effective = ImmutableList.of();
        }
    }

    public void removeStatementFromEffectiveSubstatements(final StatementDefinition statementDef) {
        if (effective.isEmpty()) {
            return;
        }

        final Iterator<? extends StmtContext<?, ?, ?>> iterator = effective.iterator();
        while (iterator.hasNext()) {
            final StmtContext<?, ?, ?> next = iterator.next();
            if (statementDef.equals(next.getPublicDefinition())) {
                iterator.remove();
            }
        }

        shrinkEffective();
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
    public void removeStatementFromEffectiveSubstatements(final StatementDefinition statementDef,
            final String statementArg) {
        if (statementArg == null) {
            removeStatementFromEffectiveSubstatements(statementDef);
        }

        if (effective.isEmpty()) {
            return;
        }

        final Iterator<Mutable<?, ?, ?>> iterator = effective.iterator();
        while (iterator.hasNext()) {
            final Mutable<?, ?, ?> next = iterator.next();
            if (statementDef.equals(next.getPublicDefinition()) && statementArg.equals(next.rawStatementArgument())) {
                iterator.remove();
            }
        }

        shrinkEffective();
    }

    /**
     * Adds an effective statement to collection of substatements.
     *
     * @param substatement substatement
     * @throws IllegalStateException
     *             if added in declared phase
     * @throws NullPointerException
     *             if statement parameter is null
     */
    public void addEffectiveSubstatement(final Mutable<?, ?, ?> substatement) {
        beforeAddEffectiveStatement(1);
        effective.add(substatement);
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
    public void addEffectiveSubstatements(final Collection<? extends Mutable<?, ?, ?>> statements) {
        if (statements.isEmpty()) {
            return;
        }

        statements.forEach(Objects::requireNonNull);
        beforeAddEffectiveStatement(statements.size());
        effective.addAll(statements);
    }

    private void beforeAddEffectiveStatement(final int toAdd) {
        final ModelProcessingPhase inProgressPhase = getRoot().getSourceContext().getInProgressPhase();
        checkState(inProgressPhase == ModelProcessingPhase.FULL_DECLARATION
                || inProgressPhase == ModelProcessingPhase.EFFECTIVE_MODEL,
                "Effective statement cannot be added in declared phase at: %s", getStatementSourceReference());

        if (effective.isEmpty()) {
            effective = new ArrayList<>(toAdd);
        }
    }

    /**
     * Create a new substatement at the specified offset.
     *
     * @param offset Substatement offset
     * @param def definition context
     * @param ref source reference
     * @param argument statement argument
     * @param <X> new substatement argument type
     * @param <Y> new substatement declared type
     * @param <Z> new substatement effective type
     * @return A new substatement
     */
    @SuppressWarnings("checkstyle:methodTypeParameterName")
    public final <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<X, Y>>
            StatementContextBase<X, Y, Z> createSubstatement(final int offset,
                    final StatementDefinitionContext<X, Y, Z> def, final StatementSourceReference ref,
                    final String argument) {
        final ModelProcessingPhase inProgressPhase = getRoot().getSourceContext().getInProgressPhase();
        checkState(inProgressPhase != ModelProcessingPhase.EFFECTIVE_MODEL,
                "Declared statement cannot be added in effective phase at: %s", getStatementSourceReference());

        final Optional<StatementSupport<?, ?, ?>> implicitParent = definition.getImplicitParentFor(def.getPublicView());
        if (implicitParent.isPresent()) {
            return createImplicitParent(offset, implicitParent.get(), ref, argument).createSubstatement(offset, def,
                    ref, argument);
        }

        final StatementContextBase<X, Y, Z> ret = new SubstatementContext<>(this, def, ref, argument);
        substatements = substatements.put(offset, ret);
        def.onStatementAdded(ret);
        return ret;
    }

    private StatementContextBase<?, ?, ?> createImplicitParent(final int offset,
            final StatementSupport<?, ?, ?> implicitParent, final StatementSourceReference ref, final String argument) {
        final StatementDefinitionContext<?, ?, ?> def = new StatementDefinitionContext<>(implicitParent);
        return createSubstatement(offset, def, ImplicitSubstatement.of(ref), argument);
    }

    public void appendImplicitStatement(final StatementSupport<?, ?, ?> statementToAdd) {
        createSubstatement(substatements.capacity(), new StatementDefinitionContext<>(statementToAdd),
                ImplicitSubstatement.of(getStatementSourceReference()), null);
    }

    /**
     * Lookup substatement by its offset in this statement.
     *
     * @param offset Substatement offset
     * @return Substatement, or null if substatement does not exist.
     */
    final StatementContextBase<?, ?, ?> lookupSubstatement(final int offset) {
        return substatements.get(offset);
    }

    final void setFullyDefined() {
        this.fullyDefined = true;
    }

    final void resizeSubstatements(final int expectedSize) {
        substatements = substatements.ensureCapacity(expectedSize);
    }

    final void walkChildren(final ModelProcessingPhase phase) {
        checkState(fullyDefined);
        substatements.values().forEach(stmt -> {
            stmt.walkChildren(phase);
            stmt.endDeclared(phase);
        });
    }

    @Override
    public D buildDeclared() {
        checkArgument(completedPhase == ModelProcessingPhase.FULL_DECLARATION
                || completedPhase == ModelProcessingPhase.EFFECTIVE_MODEL);
        if (declaredInstance == null) {
            declaredInstance = definition().getFactory().createDeclared(this);
        }
        return declaredInstance;
    }

    @Override
    public E buildEffective() {
        if (effectiveInstance == null) {
            effectiveInstance = definition().getFactory().createEffective(this);
        }
        return effectiveInstance;
    }

    /**
     * tries to execute current {@link ModelProcessingPhase} of source parsing.
     *
     * @param phase
     *            to be executed (completed)
     * @return if phase was successfully completed
     * @throws SourceException
     *             when an error occurred in source parsing
     */
    boolean tryToCompletePhase(final ModelProcessingPhase phase) {

        boolean finished = true;
        final Collection<ContextMutation> openMutations = phaseMutation.get(phase);
        if (!openMutations.isEmpty()) {
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
                if (phaseMutation.isEmpty()) {
                    phaseMutation = ImmutableMultimap.of();
                }
            }
        }

        for (final StatementContextBase<?, ?, ?> child : substatements.values()) {
            finished &= child.tryToCompletePhase(phase);
        }
        for (final Mutable<?, ?, ?> child : effective) {
            if (child instanceof StatementContextBase) {
                finished &= ((StatementContextBase<?, ?, ?>) child).tryToCompletePhase(phase);
            }
        }

        if (finished) {
            onPhaseCompleted(phase);
            return true;
        }
        return false;
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
        if (listeners.isEmpty()) {
            return;
        }

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
        definition().onDeclarationFinished(this, phase);
    }

    /**
     * Return the context in which this statement was defined.
     *
     * @return statement definition
     */
    protected final @NonNull StatementDefinitionContext<A, D, E> definition() {
        return definition;
    }

    @Override
    protected void checkLocalNamespaceAllowed(final Class<? extends IdentifierNamespace<?, ?>> type) {
        definition().checkNamespaceAllowed(type);
    }

    @Override
    protected <K, V, N extends IdentifierNamespace<K, V>> void onNamespaceElementAdded(final Class<N> type, final K key,
            final V value) {
        // definition().onNamespaceElementAdded(this, type, key, value);
    }

    final <K, V, N extends IdentifierNamespace<K, V>> void onNamespaceItemAddedAction(final Class<N> type, final K key,
            final OnNamespaceItemAdded listener) {
        final Object potential = getFromNamespace(type, key);
        if (potential != null) {
            LOG.trace("Listener on {} key {} satisfied immediately", type, key);
            listener.namespaceItemAdded(this, type, key, potential);
            return;
        }

        getBehaviour(type).addListener(new KeyedValueAddedListener<K>(this, key) {
            @Override
            void onValueAdded(final Object value) {
                listener.namespaceItemAdded(StatementContextBase.this, type, key, value);
            }
        });
    }

    final <K, V, N extends IdentifierNamespace<K, V>> void onNamespaceItemAddedAction(final Class<N> type,
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

    final <K, V, N extends IdentifierNamespace<K, V>> void selectMatch(final Class<N> type,
            final NamespaceKeyCriterion<K> criterion, final OnNamespaceItemAdded listener) {
        final Optional<Entry<K, V>> optMatch = getFromNamespace(type, criterion);
        checkState(optMatch.isPresent(), "Failed to find a match for criterion %s in namespace %s node %s", criterion,
            type, this);
        final Entry<K, V> match = optMatch.get();
        listener.namespaceItemAdded(StatementContextBase.this, type, match.getKey(), match.getValue());
    }

    final <K, V, N extends IdentifierNamespace<K, V>> void waitForPhase(final Object value, final Class<N> type,
            final ModelProcessingPhase phase, final NamespaceKeyCriterion<K> criterion,
            final OnNamespaceItemAdded listener) {
        ((StatementContextBase<?, ? ,?>) value).addPhaseCompletedListener(phase,
            (context, phaseCompleted) -> {
                selectMatch(type, criterion, listener);
                return true;
            });
    }

    private <K, V, N extends IdentifierNamespace<K, V>> NamespaceBehaviourWithListeners<K, V, N> getBehaviour(
            final Class<N> type) {
        final NamespaceBehaviour<K, V, N> behaviour = getBehaviourRegistry().getNamespaceBehaviour(type);
        checkArgument(behaviour instanceof NamespaceBehaviourWithListeners, "Namespace %s does not support listeners",
            type);

        return (NamespaceBehaviourWithListeners<K, V, N>) behaviour;
    }

    /**
     * See {@link StatementSupport#getPublicView()}.
     */
    @Override
    public StatementDefinition getPublicDefinition() {
        return definition().getPublicView();
    }

    @Override
    public ModelActionBuilder newInferenceAction(final ModelProcessingPhase phase) {
        return getRoot().getSourceContext().newInferenceAction(phase);
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
        checkNotNull(phase, "Statement context processing phase cannot be null at: %s", getStatementSourceReference());
        checkNotNull(listener, "Statement context phase listener cannot be null at: %s", getStatementSourceReference());

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
     * @throws IllegalStateException
     *             when the mutation was registered after phase was completed
     */
    void addMutation(final ModelProcessingPhase phase, final ContextMutation mutation) {
        ModelProcessingPhase finishedPhase = completedPhase;
        while (finishedPhase != null) {
            checkState(!phase.equals(finishedPhase), "Mutation registered after phase was completed at: %s",
                getStatementSourceReference());
            finishedPhase = finishedPhase.getPreviousPhase();
        }

        if (phaseMutation.isEmpty()) {
            phaseMutation = newMultimap();
        }
        phaseMutation.put(phase, mutation);
    }

    @Override
    public <K, KT extends K, N extends StatementNamespace<K, ?, ?>> void addContext(final Class<N> namespace,
            final KT key,final StmtContext<?, ?, ?> stmt) {
        addContextToNamespace(namespace, key, stmt);
    }

    @Override
    public <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<X, Y>> Mutable<X, Y, Z> childCopyOf(
            final StmtContext<X, Y, Z> stmt, final CopyType type, final QNameModule targetModule) {
        checkState(stmt.getCompletedPhase() == ModelProcessingPhase.EFFECTIVE_MODEL,
                "Attempted to copy statement %s which has completed phase %s", stmt, stmt.getCompletedPhase());

        checkArgument(stmt instanceof SubstatementContext, "Unsupported statement %s", stmt);

        final SubstatementContext<X, Y, Z> original = (SubstatementContext<X, Y, Z>)stmt;
        final Optional<StatementSupport<?, ?, ?>> implicitParent = definition.getImplicitParentFor(
            original.getPublicDefinition());

        final SubstatementContext<X, Y, Z> result;
        final SubstatementContext<X, Y, Z> copy;

        if (implicitParent.isPresent()) {
            final StatementDefinitionContext<?, ?, ?> def = new StatementDefinitionContext<>(implicitParent.get());
            result = new SubstatementContext(this, def, original.getSourceReference(),
                original.rawStatementArgument(), original.getStatementArgument(), type);

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

            copy = new SubstatementContext<>(original, result, childCopyType, targetModule);
            result.addEffectiveSubstatement(copy);
            original.definition().onStatementAdded(copy);
        } else {
            result = copy = new SubstatementContext<>(original, this, type, targetModule);
            original.definition().onStatementAdded(copy);
        }

        original.copyTo(copy, type, targetModule);
        return result;
    }

    @Override
    public @NonNull StatementDefinition getDefinition() {
        return getPublicDefinition();
    }

    @Override
    public @NonNull StatementSourceReference getSourceReference() {
        return getStatementSourceReference();
    }

    @Override
    public boolean isFullyDefined() {
        return fullyDefined;
    }

    final void copyTo(final StatementContextBase<?, ?, ?> target, final CopyType typeOfCopy,
            @Nullable final QNameModule targetModule) {
        final Collection<Mutable<?, ?, ?>> buffer = new ArrayList<>(substatements.size() + effective.size());

        for (final Mutable<?, ?, ?> stmtContext : substatements.values()) {
            if (stmtContext.isSupportedByFeatures()) {
                copySubstatement(stmtContext, target, typeOfCopy, targetModule, buffer);
            }
        }

        for (final Mutable<?, ?, ?> stmtContext : effective) {
            copySubstatement(stmtContext, target, typeOfCopy, targetModule, buffer);
        }

        target.addEffectiveSubstatements(buffer);
    }

    private void copySubstatement(final Mutable<?, ?, ?> stmtContext,  final Mutable<?, ?, ?> target,
            final CopyType typeOfCopy, final QNameModule newQNameModule, final Collection<Mutable<?, ?, ?>> buffer) {
        if (needToCopyByUses(stmtContext)) {
            final Mutable<?, ?, ?> copy = target.childCopyOf(stmtContext, typeOfCopy, newQNameModule);
            LOG.debug("Copying substatement {} for {} as {}", stmtContext, this, copy);
            buffer.add(copy);
        } else if (isReusedByUses(stmtContext)) {
            LOG.debug("Reusing substatement {} for {}", stmtContext, this);
            buffer.add(stmtContext);
        } else {
            LOG.debug("Skipping statement {}", stmtContext);
        }
    }

    // FIXME: revise this, as it seems to be wrong
    private static final Set<YangStmtMapping> NOCOPY_FROM_GROUPING_SET = ImmutableSet.of(
        YangStmtMapping.DESCRIPTION,
        YangStmtMapping.REFERENCE,
        YangStmtMapping.STATUS);
    private static final Set<YangStmtMapping> REUSED_DEF_SET = ImmutableSet.of(
        YangStmtMapping.TYPE,
        YangStmtMapping.TYPEDEF,
        YangStmtMapping.USES);

    private static boolean needToCopyByUses(final StmtContext<?, ?, ?> stmtContext) {
        final StatementDefinition def = stmtContext.getPublicDefinition();
        if (REUSED_DEF_SET.contains(def)) {
            LOG.debug("Will reuse {} statement {}", def, stmtContext);
            return false;
        }
        if (NOCOPY_FROM_GROUPING_SET.contains(def)) {
            return !YangStmtMapping.GROUPING.equals(stmtContext.coerceParentContext().getPublicDefinition());
        }

        LOG.debug("Will copy {} statement {}", def, stmtContext);
        return true;
    }

    private static boolean isReusedByUses(final StmtContext<?, ?, ?> stmtContext) {
        return REUSED_DEF_SET.contains(stmtContext.getPublicDefinition());
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("definition", definition).add("rawArgument", rawArgument);
    }
}
