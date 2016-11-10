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
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EventListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
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
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.NamespaceBehaviourWithListeners.ValueAddedListener;

public abstract class StatementContextBase<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends NamespaceStorageSupport implements StmtContext.Mutable<A, D, E>, Identifiable<StatementIdentifier> {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private final class SubContextBuilder extends ContextBuilder {
        SubContextBuilder(final StatementDefinitionContext def, final StatementSourceReference sourceRef) {
            super(def, sourceRef);
        }

        @Override
        public StatementContextBase build() {
            StatementContextBase<?, ?, ?> potential = null;

            final StatementDefinition stmtDef = getDefinition().getPublicView();
            // FIXME: this is rather ugly. Rather than having an explicit blacklist, StatementDefinitionContext should
            //        give us information whether we should really bother with the substatements map.
            if (stmtDef != Rfc6020Mapping.AUGMENT && stmtDef != Rfc6020Mapping.DEVIATION
                    && stmtDef != Rfc6020Mapping.IMPORT && stmtDef != Rfc6020Mapping.TYPE) {
                potential = substatements.get(createIdentifier());
            }
            if (potential == null) {
                potential = new SubstatementContext(StatementContextBase.this, this);
                if (substatements.isEmpty()) {
                    substatements = new LinkedHashMap<>(1);
                }
                substatements.put(createIdentifier(), potential);
                getDefinition().onStatementAdded(potential);
            }
            potential.resetLists();
            switch (this.getStamementSource().getStatementSource()) {
            case DECLARATION:
                addDeclaredSubstatement(potential);
                break;
            case CONTEXT:
                addEffectiveSubstatement(potential);
                break;
            }
            return potential;
        }
    }

    /**
     * event listener when an item is added to model namespace
     */
    interface OnNamespaceItemAdded extends EventListener {
        /**
         * @throws SourceException
         */
        void namespaceItemAdded(StatementContextBase<?, ?, ?> context, Class<?> namespace, Object key, Object value);
    }

    /**
     * event listener when a parsing {@link ModelProcessingPhase} is completed
     */
    interface OnPhaseFinished extends EventListener {
        /**
         * @throws SourceException
         */
        boolean phaseFinished(StatementContextBase<?, ?, ?> context, ModelProcessingPhase phase);
    }

    /**
     * interface for all mutations within an {@link ModelActionBuilder.InferenceAction}
     */
    interface ContextMutation {

        boolean isFinished();
    }

    private final StatementDefinitionContext<A, D, E> definition;
    private final StatementIdentifier identifier;
    private final StatementSourceReference statementDeclSource;

    private Multimap<ModelProcessingPhase, OnPhaseFinished> phaseListeners = ImmutableMultimap.of();
    private Multimap<ModelProcessingPhase, ContextMutation> phaseMutation = ImmutableMultimap.of();
    private Map<StatementIdentifier, StatementContextBase<?, ?, ?>> substatements = ImmutableMap.of();
    private Collection<StatementContextBase<?, ?, ?>> declared = ImmutableList.of();
    private Collection<StatementContextBase<?, ?, ?>> effective = ImmutableList.of();
    private Collection<StatementContextBase<?, ?, ?>> effectOfStatement = ImmutableList.of();

    private SupportedByFeatures supportedByFeatures = SupportedByFeatures.UNDEFINED;
    private CopyHistory copyHistory = CopyHistory.original();
    private boolean isSupportedToBuildEffective = true;
    private ModelProcessingPhase completedPhase = null;
    private StatementContextBase<?, ?, ?> originalCtx;
    private D declaredInstance;
    private E effectiveInstance;
    private int order = 0;

    StatementContextBase(@Nonnull final ContextBuilder<A, D, E> builder) {
        this.definition = builder.getDefinition();
        this.identifier = builder.createIdentifier();
        this.statementDeclSource = builder.getStamementSource();
    }

    StatementContextBase(final StatementContextBase<A, D, E> original) {
        this.definition = Preconditions.checkNotNull(original.definition,
                "Statement context definition cannot be null copying from: %s", original.getStatementSourceReference());
        this.identifier = Preconditions.checkNotNull(original.identifier,
                "Statement context identifier cannot be null copying from: %s", original.getStatementSourceReference());
        this.statementDeclSource = Preconditions.checkNotNull(original.statementDeclSource,
                "Statement context statementDeclSource cannot be null copying from: %s",
                original.getStatementSourceReference());
    }

    @Override
    public Collection<StatementContextBase<?, ?, ?>> getEffectOfStatement() {
        return effectOfStatement;
    }

    @Override
    public void addAsEffectOfStatement(final StatementContextBase<?, ?, ?> ctx) {
        if (effectOfStatement.isEmpty()) {
            effectOfStatement = new ArrayList<>(1);
        }
        effectOfStatement.add(ctx);
    }

    @Override
    public void addAsEffectOfStatement(final Collection<StatementContextBase<?, ?, ?>> ctxs) {
        if (ctxs.isEmpty()) {
            return;
        }

        if (effectOfStatement.isEmpty()) {
            effectOfStatement = new ArrayList<>(ctxs.size());
        }
        effectOfStatement.addAll(ctxs);
    }

    @Override
    public SupportedByFeatures getSupportedByFeatures() {
        return supportedByFeatures;
    }

    @Override
    public void setSupportedByFeatures(final boolean isSupported) {
        this.supportedByFeatures = isSupported ? SupportedByFeatures.SUPPORTED : SupportedByFeatures.NOT_SUPPORTED;
    }

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
    public void appendCopyHistory(final CopyType typeOfCopy, final CopyHistory toAppend) {
        copyHistory = copyHistory.append(typeOfCopy, toAppend);
    }

    @Override
    public StatementContextBase<?, ?, ?> getOriginalCtx() {
        return originalCtx;
    }

    @Override
    public void setOriginalCtx(final StatementContextBase<?, ?, ?> originalCtx) {
        this.originalCtx = originalCtx;
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

    /**
     * @return context of parent of statement
     */
    @Override
    public abstract StatementContextBase<?, ?, ?> getParentContext();

    /**
     * @return root context of statement
     */
    @Override
    public abstract RootStatementContext<?, ?, ?> getRoot();

    /**
     * @return statement identifier
     */
    @Override
    public StatementIdentifier getIdentifier() {
        return identifier;
    }

    /**
     * @return origin of statement
     */
    @Override
    public StatementSource getStatementSource() {
        return statementDeclSource.getStatementSource();
    }

    /**
     * @return reference of statement source
     */
    @Override
    public StatementSourceReference getStatementSourceReference() {
        return statementDeclSource;
    }

    /**
     * @return raw statement argument string
     */
    @Override
    public String rawStatementArgument() {
        return identifier.getArgument();
    }

    private static final <T> Collection<T> maybeWrap(final Collection<T> input) {
        if (input instanceof ImmutableCollection) {
            return input;
        }

        return Collections.unmodifiableCollection(input);
    }

    @Override
    public Collection<StatementContextBase<?, ?, ?>> declaredSubstatements() {
        return maybeWrap(declared);
    }

    @Override
    public Collection<StatementContextBase<?, ?, ?>> effectiveSubstatements() {
        return maybeWrap(effective);
    }

    public void removeStatementsFromEffectiveSubstatements(final Collection<StatementContextBase<?, ?, ?>> substatements) {
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

    public void removeStatementFromEffectiveSubstatements(final StatementDefinition refineSubstatementDef) {
        if (effective.isEmpty()) {
            return;
        }

        final Iterator<StatementContextBase<?, ?, ?>> iterator = effective.iterator();
        while (iterator.hasNext()) {
            final StatementContextBase<?, ?, ?> next = iterator.next();
            if (next.getPublicDefinition().equals(refineSubstatementDef)) {
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
    public void addEffectiveSubstatement(final StatementContextBase<?, ?, ?> substatement) {
        Preconditions.checkNotNull(substatement, "StatementContextBase effective substatement cannot be null at: %s",
            getStatementSourceReference());
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
    public void addEffectiveSubstatements(final Collection<StatementContextBase<?, ?, ?>> substatements) {
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
     * adds declared statement to collection of substatements
     *
     * @param substatement substatement
     * @throws IllegalStateException
     *             if added in effective phase
     * @throws NullPointerException
     *             if statement parameter is null
     */
    public void addDeclaredSubstatement(final StatementContextBase<?, ?, ?> substatement) {

        final ModelProcessingPhase inProgressPhase = getRoot().getSourceContext().getInProgressPhase();
        Preconditions.checkState(inProgressPhase != ModelProcessingPhase.EFFECTIVE_MODEL,
                "Declared statement cannot be added in effective phase at: %s", getStatementSourceReference());

        if (declared.isEmpty()) {
            declared = new ArrayList<>(1);
        }
        declared.add(Preconditions.checkNotNull(substatement,
                "StatementContextBase declared substatement cannot be null at: %s", getStatementSourceReference()));
    }

    /**
     * builds a new substatement from statement definition context and statement source reference
     *
     * @param def definition context
     * @param ref source reference
     *
     * @return instance of ContextBuilder
     */
    public ContextBuilder<?, ?, ?> substatementBuilder(final StatementDefinitionContext<?, ?, ?> def,
            final StatementSourceReference ref) {
        return new SubContextBuilder(def, ref);
    }

    /**
     * @return local namespace behaviour type {@link NamespaceBehaviour}
     */
    @Override
    public StorageNodeType getStorageNodeType() {
        return StorageNodeType.STATEMENT_LOCAL;
    }

    /**
     * builds {@link DeclaredStatement} for statement context
     */
    @Override
    public D buildDeclared() {
        Preconditions.checkArgument(completedPhase == ModelProcessingPhase.FULL_DECLARATION
                || completedPhase == ModelProcessingPhase.EFFECTIVE_MODEL);
        if (declaredInstance == null) {
            declaredInstance = definition().getFactory().createDeclared(this);
        }
        return declaredInstance;
    }

    /**
     * builds {@link EffectiveStatement} for statement context
     */
    @Override
    public E buildEffective() {
        if (effectiveInstance == null) {
            effectiveInstance = definition().getFactory().createEffective(this);
        }
        return effectiveInstance;
    }

    /**
     * clears collection of declared substatements
     *
     * @throws IllegalStateException
     *             if invoked in effective build phase
     */
    void resetLists() {

        final SourceSpecificContext sourceContext = getRoot().getSourceContext();
        Preconditions.checkState(sourceContext.getInProgressPhase() != ModelProcessingPhase.EFFECTIVE_MODEL,
                "Declared statements list cannot be cleared in effective phase at: %s", getStatementSourceReference());

        declared = ImmutableList.of();
    }

    /**
     * tries to execute current {@link ModelProcessingPhase} of source parsing
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

        for (final StatementContextBase<?, ?, ?> child : declared) {
            finished &= child.tryToCompletePhase(phase);
        }
        for (final StatementContextBase<?, ?, ?> child : effective) {
            finished &= child.tryToCompletePhase(phase);
        }

        if (finished) {
            onPhaseCompleted(phase);
            return true;
        }
        return false;
    }

    /**
     * occurs on end of {@link ModelProcessingPhase} of source parsing
     *
     * @param phase
     *            that was to be completed (finished)
     * @throws SourceException
     *             when an error occured in source parsing
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

    /**
     * occurs when an item is added to model namespace
     *
     * @throws SourceException instance of SourceException
     */
    @Override
    protected <K, V, N extends IdentifierNamespace<K, V>> void onNamespaceElementAdded(final Class<N> type, final K key, final V value) {
        // definition().onNamespaceElementAdded(this, type, key, value);
    }

    <K, V, N extends IdentifierNamespace<K, V>> void onNamespaceItemAddedAction(final Class<N> type, final K key,
            final OnNamespaceItemAdded listener) throws SourceException {
        final Object potential = getFromNamespace(type, key);
        if (potential != null) {
            listener.namespaceItemAdded(this, type, key, potential);
            return;
        }
        final NamespaceBehaviour<K, V, N> behaviour = getBehaviourRegistry().getNamespaceBehaviour(type);
        if (behaviour instanceof NamespaceBehaviourWithListeners) {
            final NamespaceBehaviourWithListeners<K, V, N> casted = (NamespaceBehaviourWithListeners<K, V, N>) behaviour;
            casted.addValueListener(new ValueAddedListener<K>(this, key) {
                @Override
                void onValueAdded(final Object key, final Object value) {
                    try {
                        listener.namespaceItemAdded(StatementContextBase.this, type, key, value);
                    } catch (final SourceException e) {
                        throw Throwables.propagate(e);
                    }
                }
            });
        }
    }

    /**
     * @see StatementSupport#getPublicView()
     */
    @Override
    public StatementDefinition getPublicDefinition() {
        return definition().getPublicView();
    }

    /**
     * @return new {@link ModelActionBuilder} for the phase
     */
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

    /**
     * adds statement to namespace map with the key
     *
     * @param namespace
     *            {@link StatementNamespace} child that determines namespace to be added to
     * @param key
     *            of type according to namespace class specification
     * @param stmt
     *            to be added to namespace map
     */
    @Override
    public <K, KT extends K, N extends StatementNamespace<K, ?, ?>> void addContext(final Class<N> namespace, final KT key,
            final StmtContext<?, ?, ?> stmt) {
        addContextToNamespace(namespace, key, stmt);
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("definition", definition).add("id", identifier);
    }
}
