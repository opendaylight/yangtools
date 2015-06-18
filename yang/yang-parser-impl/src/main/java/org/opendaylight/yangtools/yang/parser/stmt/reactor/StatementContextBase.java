/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
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
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.AugmentStatementImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StatementContextBase<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends NamespaceStorageSupport implements StmtContext.Mutable<A, D, E>, Identifiable<StatementIdentifier> {

    /**
     * event listener when an item is added to model namespace
     */
    interface OnNamespaceItemAdded extends EventListener {

        void namespaceItemAdded(StatementContextBase<?, ?, ?> context, Class<?> namespace, Object key, Object value)
                throws SourceException;

    }

    /**
     * event listener when a parsing {@link ModelProcessingPhase} is completed
     */
    interface OnPhaseFinished extends EventListener {

        void phaseFinished(StatementContextBase<?, ?, ?> context, ModelProcessingPhase phase) throws SourceException;

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

    private Map<StatementIdentifier, StatementContextBase<?, ?, ?>> substatements = new LinkedHashMap<>();

    private Collection<StatementContextBase<?, ?, ?>> declared = new ArrayList<>();
    private Collection<StatementContextBase<?, ?, ?>> effective = new ArrayList<>();

    private ModelProcessingPhase completedPhase;

    private Multimap<ModelProcessingPhase, OnPhaseFinished> phaseListeners = HashMultimap.create();
    private Multimap<ModelProcessingPhase, ContextMutation> phaseMutation = HashMultimap.create();

    private D declaredInstance;
    private E effectiveInstance;

    StatementContextBase(@Nonnull ContextBuilder<A, D, E> builder) throws SourceException {
        this.definition = builder.getDefinition();
        this.identifier = builder.createIdentifier();
        this.statementDeclSource = builder.getStamementSource();
        this.completedPhase = null;
    }

    StatementContextBase(StatementContextBase<A, D, E> original) {
        this.definition = Preconditions
                .checkNotNull(original.definition, "Statement context definition cannot be null");
        this.identifier = Preconditions
                .checkNotNull(original.identifier, "Statement context identifier cannot be null");
        this.statementDeclSource = Preconditions.checkNotNull(original.statementDeclSource,
                "Statement context statementDeclSource cannot be null");
        this.completedPhase = null;
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

    /**
     * @return collection of declared substatements
     */
    @Override
    public Collection<StatementContextBase<?, ?, ?>> declaredSubstatements() {
        return Collections.unmodifiableCollection(declared);
    }

    /**
     * @return collection of effective substatements
     */
    @Override
    public Collection<StatementContextBase<?, ?, ?>> effectiveSubstatements() {
        return Collections.unmodifiableCollection(effective);
    }

    /**
     * adds effective statement to collection of substatements
     *
     * @throws IllegalStateException
     *             if added in declared phase
     * @throws NullPointerException
     *             if statement parameter is null
     */
    public void addEffectiveSubstatement(StatementContextBase<?, ?, ?> substatement) {

        final ModelProcessingPhase inProgressPhase = getRoot().getSourceContext().getInProgressPhase();
        Preconditions.checkState(inProgressPhase == ModelProcessingPhase.FULL_DECLARATION
                || inProgressPhase == ModelProcessingPhase.EFFECTIVE_MODEL,
                "Effective statement cannot be added in declared phase");

        effective.add(Preconditions.checkNotNull(substatement,
                "StatementContextBase effective substatement cannot be null"));
    }

    /**
     * adds declared statement to collection of substatements
     *
     * @throws IllegalStateException
     *             if added in effective phase
     * @throws NullPointerException
     *             if statement parameter is null
     */
    public void addDeclaredSubstatement(StatementContextBase<?, ?, ?> substatement) {

        final ModelProcessingPhase inProgressPhase = getRoot().getSourceContext().getInProgressPhase();
        Preconditions.checkState(inProgressPhase != ModelProcessingPhase.EFFECTIVE_MODEL,
                "Declared statement cannot be added in effective phase");

        declared.add(Preconditions.checkNotNull(substatement,
                "StatementContextBase declared substatement cannot be null"));
    }

    /**
     * builds new substatement from statement definition context and statement source reference
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ContextBuilder<?, ?, ?> substatementBuilder(StatementDefinitionContext<?, ?, ?> def,
            StatementSourceReference ref) {
        return new ContextBuilder(def, ref) {

            @Override
            public StatementContextBase build() throws SourceException {
                StatementContextBase<?, ?, ?> potential = substatements.get(createIdentifier());
                if (potential == null) {
                    potential = new SubstatementContext(StatementContextBase.this, this);
                    substatements.put(createIdentifier(), potential);
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
        };
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
        Preconditions.checkArgument(completedPhase == ModelProcessingPhase.EFFECTIVE_MODEL);
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
                "Declared statements list cannot be cleared in effective phase");

        declared.clear();
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
    boolean tryToCompletePhase(ModelProcessingPhase phase) throws SourceException {
        if (phase.equals(completedPhase)) {
            return true;
        }
        Iterator<ContextMutation> openMutations = phaseMutation.get(phase).iterator();
        boolean finished = true;
        while (openMutations.hasNext()) {
            ContextMutation current = openMutations.next();
            if (current.isFinished()) {
                openMutations.remove();
            } else {
                finished = false;
            }
        }
        for (StatementContextBase<?, ?, ?> child : declared) {
            finished &= child.tryToCompletePhase(phase);
        }
        for (StatementContextBase<?, ?, ?> child : effective) {
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
    private void onPhaseCompleted(ModelProcessingPhase phase) throws SourceException {
        completedPhase = phase;
        Iterator<OnPhaseFinished> listener = phaseListeners.get(completedPhase).iterator();
        while (listener.hasNext()) {
            listener.next().phaseFinished(this, phase);
            listener.remove();
        }
    }

    /**
     * Ends declared section of current node.
     *
     * @throws SourceException
     */
    void endDeclared(StatementSourceReference ref, ModelProcessingPhase phase) throws SourceException {

        final ModelProcessingPhase inProgressPhase = getRoot().getSourceContext().getInProgressPhase();
        Preconditions.checkState(inProgressPhase != ModelProcessingPhase.EFFECTIVE_MODEL,
                "Declared statement cannot be ended in effective phase");

        definition().onDeclarationFinished(this, phase);
    }

    /**
     * @return statement definition
     */
    protected final StatementDefinitionContext<A, D, E> definition() {
        return definition;
    }

    @Override
    protected void checkLocalNamespaceAllowed(Class<? extends IdentifierNamespace<?, ?>> type) {
        definition().checkNamespaceAllowed(type);
    }

    /**
     * occurs when an item is added to model namespace
     *
     * @throws SourceException
     */
    <K, V, N extends IdentifierNamespace<K, V>> void onNamespaceItemAddedAction(final Class<N> type, K key,
            final OnNamespaceItemAdded listener) throws SourceException {
        Object potential = getFromNamespace(type, key);
        if (potential != null) {
            listener.namespaceItemAdded(this, type, key, potential);
            return;
        }
        NamespaceBehaviour<K, V, N> behaviour = getBehaviourRegistry().getNamespaceBehaviour(type);
        if (behaviour instanceof NamespaceBehaviourWithListeners) {
            NamespaceBehaviourWithListeners<K, V, N> casted = (NamespaceBehaviourWithListeners<K, V, N>) behaviour;
            casted.addValueListener(key, new ValueAddedListener(this) {
                @Override
                void onValueAdded(Object key, Object value) {
                    try {
                        listener.namespaceItemAdded(StatementContextBase.this, type, key, value);
                    } catch (SourceException e) {
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
    public ModelActionBuilder newInferenceAction(ModelProcessingPhase phase) {
        return getRoot().getSourceContext().newInferenceAction(phase);
    }

    /**
     * adds {@link OnPhaseFinished} listener for a {@link ModelProcessingPhase} end
     *
     * @throws SourceException
     */
    void addPhaseCompletedListener(ModelProcessingPhase phase, OnPhaseFinished listener) throws SourceException {

        Preconditions.checkNotNull(phase, "Statement context processing phase cannot be null");
        Preconditions.checkNotNull(listener, "Statement context phase listener cannot be null");

        ModelProcessingPhase finishedPhase = completedPhase;
        while (finishedPhase != null) {
            if (phase.equals(finishedPhase)) {
                listener.phaseFinished(this, finishedPhase);
                return;
            }
            finishedPhase = finishedPhase.getPreviousPhase();
        }
        phaseListeners.put(phase, listener);
    }

    /**
     * adds {@link ContextMutation} to {@link ModelProcessingPhase}
     *
     * @throws IllegalStateException
     *             when the mutation was registered after phase was completed
     */
    void addMutation(ModelProcessingPhase phase, ContextMutation mutation) {
        ModelProcessingPhase finishedPhase = completedPhase;
        while (finishedPhase != null) {
            if (phase.equals(finishedPhase)) {
                throw new IllegalStateException("Mutation registered after phase was completed.");
            }
            finishedPhase = finishedPhase.getPreviousPhase();
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
    public <K, KT extends K, N extends StatementNamespace<K, ?, ?>> void addContext(Class<N> namespace, KT key,
            StmtContext<?, ?, ?> stmt) {
        addContextToNamespace(namespace, (K) key, stmt);
    }
}
