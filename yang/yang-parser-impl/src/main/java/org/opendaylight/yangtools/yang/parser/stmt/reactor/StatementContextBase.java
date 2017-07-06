/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;
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
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
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
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.SupportedFeaturesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.SupportedFeaturesNamespace.SupportedFeatures;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.NamespaceBehaviourWithListeners.ValueAddedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StatementContextBase<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends NamespaceStorageSupport implements StmtContext.Mutable<A, D, E> {
    /**
     * event listener when an item is added to model namespace.
     */
    interface OnNamespaceItemAdded extends EventListener {
        /**
         * @throws SourceException
         */
        void namespaceItemAdded(StatementContextBase<?, ?, ?> context, Class<?> namespace, Object key, Object value);
    }

    /**
     * event listener when a parsing {@link ModelProcessingPhase} is completed.
     */
    interface OnPhaseFinished extends EventListener {
        /**
         * @throws SourceException
         */
        boolean phaseFinished(StatementContextBase<?, ?, ?> context, ModelProcessingPhase phase);
    }

    /**
     * interface for all mutations within an {@link ModelActionBuilder.InferenceAction}.
     */
    interface ContextMutation {

        boolean isFinished();
    }

    private static final Logger LOG = LoggerFactory.getLogger(StatementContextBase.class);

    private final StatementDefinitionContext<A, D, E> definition;
    private final StatementSourceReference statementDeclSource;
    private final StmtContext<?, ?, ?> originalCtx;
    private final CopyHistory copyHistory;
    private final String rawArgument;

    private Multimap<ModelProcessingPhase, OnPhaseFinished> phaseListeners = ImmutableMultimap.of();
    private Multimap<ModelProcessingPhase, ContextMutation> phaseMutation = ImmutableMultimap.of();
    private Collection<Mutable<?, ?, ?>> effective = ImmutableList.of();
    private Collection<StmtContext<?, ?, ?>> effectOfStatement = ImmutableList.of();
    private StatementMap substatements = StatementMap.empty();

    private Boolean supportedByFeatures = null;
    private boolean isSupportedToBuildEffective = true;
    private ModelProcessingPhase completedPhase = null;
    private D declaredInstance;
    private E effectiveInstance;
    private int order = 0;

    StatementContextBase(final StatementDefinitionContext<A, D, E> def, final StatementSourceReference ref,
            final String rawArgument) {
        this.definition = Preconditions.checkNotNull(def);
        this.statementDeclSource = Preconditions.checkNotNull(ref);
        this.rawArgument = def.internArgument(rawArgument);
        this.copyHistory = CopyHistory.original();
        this.originalCtx = null;
    }

    StatementContextBase(final StatementContextBase<A, D, E> original, final CopyType copyType) {
        this.definition = Preconditions.checkNotNull(original.definition,
                "Statement context definition cannot be null copying from: %s", original.getStatementSourceReference());
        this.statementDeclSource = Preconditions.checkNotNull(original.statementDeclSource,
                "Statement context statementDeclSource cannot be null copying from: %s",
                original.getStatementSourceReference());
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
        if(isIgnoringIfFeatures()) {
            return true;
        }

        if (supportedByFeatures == null) {
            final Set<QName> supportedFeatures = getFromNamespace(SupportedFeaturesNamespace.class,
                SupportedFeatures.SUPPORTED_FEATURES);
            // If the set of supported features has not been provided, all features are supported by default.
            supportedByFeatures = supportedFeatures == null ? Boolean.TRUE
                    : StmtContextUtils.checkFeatureSupport(this, supportedFeatures);
        }

        return supportedByFeatures.booleanValue();
    }

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
    public void setOrder(final int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
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
     * @return root context of statement
     */
    @Nonnull
    @Override
    public abstract RootStatementContext<?, ?, ?> getRoot();

    /**
     * @return origin of statement
     */
    @Nonnull
    @Override
    public StatementSource getStatementSource() {
        return statementDeclSource.getStatementSource();
    }

    /**
     * @return reference of statement source
     */
    @Nonnull
    @Override
    public StatementSourceReference getStatementSourceReference() {
        return statementDeclSource;
    }

    @Override
    public final String rawStatementArgument() {
        return rawArgument;
    }

    @Nonnull
    @Override
    public Collection<? extends StmtContext<?, ?, ?>> declaredSubstatements() {
        return substatements.values();
    }

    @Nonnull
    @Override
    public Collection<? extends Mutable<?, ?, ?>> mutableDeclaredSubstatements() {
        return substatements.values();
    }

    @Override
    public Collection<? extends StmtContext<?, ?, ?>> effectiveSubstatements() {
        return mutableEffectiveSubstatements();
    }

    @Nonnull
    @Override
    public Collection<? extends Mutable<?, ?, ?>> mutableEffectiveSubstatements() {
        if (effective instanceof ImmutableCollection) {
            return effective;
        }

        return Collections.unmodifiableCollection(effective);
    }

    public void removeStatementsFromEffectiveSubstatements(final Collection<? extends StmtContext<?, ?, ?>> substatements) {
        if (!effective.isEmpty()) {
            effective.removeAll(substatements);
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
     * Removes a statement context from the effective substatements
     * based on its statement definition (i.e statement keyword) and raw (in String form) statement argument.
     * The statement context is removed only if both statement definition and statement argument match with
     * one of the effective substatements' statement definition and argument.
     *
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
     * adds effective statement to collection of substatements
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
     * adds effective statement to collection of substatements
     *
     * @param substatements substatements
     * @throws IllegalStateException
     *             if added in declared phase
     * @throws NullPointerException
     *             if statement parameter is null
     */
    public void addEffectiveSubstatements(final Collection<? extends Mutable<?, ?, ?>> substatements) {
        if (substatements.isEmpty()) {
            return;
        }

        substatements.forEach(Preconditions::checkNotNull);
        beforeAddEffectiveStatement(substatements.size());
        effective.addAll(substatements);
    }

    private void beforeAddEffectiveStatement(final int toAdd) {
        final ModelProcessingPhase inProgressPhase = getRoot().getSourceContext().getInProgressPhase();
        Preconditions.checkState(inProgressPhase == ModelProcessingPhase.FULL_DECLARATION
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
     * @return A new substatement
     */
    public final <CA, CD extends DeclaredStatement<CA>, CE extends EffectiveStatement<CA, CD>> StatementContextBase<CA, CD, CE> createSubstatement(
            final int offset, final StatementDefinitionContext<CA, CD, CE> def, final StatementSourceReference ref,
            final String argument) {
        final ModelProcessingPhase inProgressPhase = getRoot().getSourceContext().getInProgressPhase();
        Preconditions.checkState(inProgressPhase != ModelProcessingPhase.EFFECTIVE_MODEL,
                "Declared statement cannot be added in effective phase at: %s", getStatementSourceReference());

        final Optional<StatementContextBase<?, ?, ?>> implicitStatement = definition.beforeSubStatementCreated(this,
            offset, def, ref, argument);
        if (implicitStatement.isPresent()) {
            return implicitStatement.get().createSubstatement(offset, def, ref, argument);
        }

        final StatementContextBase<CA, CD, CE> ret = new SubstatementContext<>(this, def, ref, argument);
        substatements = substatements.put(offset, ret);
        def.onStatementAdded(ret);
        return ret;
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

    @Override
    public D buildDeclared() {
        Preconditions.checkArgument(completedPhase == ModelProcessingPhase.FULL_DECLARATION
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
     *             when an error occured in source parsing
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
     *
     * @param ref
     * @throws SourceException
     */
    void endDeclared(final StatementSourceReference ref, final ModelProcessingPhase phase) {
        definition().onDeclarationFinished(this, phase);
    }

    /**
     * @return statement definition
     */
    protected final StatementDefinitionContext<A, D, E> definition() {
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

    <K, V, N extends IdentifierNamespace<K, V>> void onNamespaceItemAddedAction(final Class<N> type, final K key,
            final OnNamespaceItemAdded listener) throws SourceException {
        final Object potential = getFromNamespace(type, key);
        if (potential != null) {
            LOG.trace("Listener on {} key {} satisfied immediately", type, key);
            listener.namespaceItemAdded(this, type, key, potential);
            return;
        }

        final NamespaceBehaviour<K, V, N> behaviour = getBehaviourRegistry().getNamespaceBehaviour(type);
        Preconditions.checkArgument(behaviour instanceof NamespaceBehaviourWithListeners,
            "Namespace {} does not support listeners", type);

        final NamespaceBehaviourWithListeners<K, V, N> casted = (NamespaceBehaviourWithListeners<K, V, N>) behaviour;
        casted.addValueListener(new ValueAddedListener<K>(this, key) {
            @Override
            void onValueAdded(final Object key, final Object value) {
                listener.namespaceItemAdded(StatementContextBase.this, type, key, value);
            }
        });
    }

    /**
     * See {@link StatementSupport#getPublicView()}.
     */
    @Nonnull
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
     * adds {@link OnPhaseFinished} listener for a {@link ModelProcessingPhase} end
     *
     * @throws SourceException
     */
    void addPhaseCompletedListener(final ModelProcessingPhase phase, final OnPhaseFinished listener) {

        Preconditions.checkNotNull(phase, "Statement context processing phase cannot be null at: %s",
                getStatementSourceReference());
        Preconditions.checkNotNull(listener, "Statement context phase listener cannot be null at: %s",
                getStatementSourceReference());

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
     * adds {@link ContextMutation} to {@link ModelProcessingPhase}
     *
     * @throws IllegalStateException
     *             when the mutation was registered after phase was completed
     */
    void addMutation(final ModelProcessingPhase phase, final ContextMutation mutation) {
        ModelProcessingPhase finishedPhase = completedPhase;
        while (finishedPhase != null) {
            if (phase.equals(finishedPhase)) {
                throw new IllegalStateException("Mutation registered after phase was completed at: "  +
                        getStatementSourceReference());
            }
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
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("definition", definition).add("rawArgument", rawArgument);
    }
}
