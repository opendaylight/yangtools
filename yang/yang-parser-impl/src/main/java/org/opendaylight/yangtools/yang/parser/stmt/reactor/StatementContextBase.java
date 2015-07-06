/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import java.util.LinkedList;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
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
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.NamespaceBehaviourWithListeners.ValueAddedListener;

public abstract class StatementContextBase<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends NamespaceStorageSupport implements
        StmtContext.Mutable<A, D, E>, Identifiable<StatementIdentifier> {

    interface OnNamespaceItemAdded extends EventListener {

        void namespaceItemAdded(StatementContextBase<?, ?, ?> context,
                Class<?> namespace, Object key, Object value)
                throws SourceException;

    }

    interface OnPhaseFinished extends EventListener {

        boolean phaseFinished(StatementContextBase<?, ?, ?> context,
                ModelProcessingPhase phase) throws SourceException;

    }

    interface ContextMutation {

        boolean isFinished();

    }

    abstract static class ContextBuilder<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> {

        private final StatementDefinitionContext<A, D, E> definition;
        private final StatementSourceReference stmtRef;
        private String rawArg;
        private StatementSourceReference argRef;

        public ContextBuilder(StatementDefinitionContext<A, D, E> def,
                StatementSourceReference sourceRef) {
            this.definition = def;
            this.stmtRef = sourceRef;
        }

        public void setArgument(@Nonnull String argument,
                @Nonnull StatementSourceReference argumentSource) {
            Preconditions.checkArgument(definition.hasArgument(),
                    "Statement does not take argument.");
            this.rawArg = Preconditions.checkNotNull(argument);
            this.argRef = Preconditions.checkNotNull(argumentSource);
        }

        public String getRawArgument() {
            return rawArg;
        }

        public StatementSourceReference getStamementSource() {
            return stmtRef;
        }

        public StatementSourceReference getArgumentSource() {
            return argRef;
        }

        public StatementDefinitionContext<A, D, E> getDefinition() {
            return definition;
        }

        public StatementIdentifier getIdentifier() {
            return new StatementIdentifier(definition.getStatementName(),
                    rawArg);
        }

        public abstract StatementContextBase<A, D, E> build()
                throws SourceException;

    }

    private final StatementDefinitionContext<A, D, E> definition;
    private final StatementIdentifier identifier;
    private final StatementSourceReference statementDeclSource;
    private int order = 0;

    private Map<StatementIdentifier, StatementContextBase<?, ?, ?>> substatements = new LinkedHashMap<>();

    private Collection<StatementContextBase<?, ?, ?>> declared = new ArrayList<>();
    private Collection<StatementContextBase<?, ?, ?>> effective = new ArrayList<>();
    private Collection<StatementContextBase<?, ?, ?>> effectOfStatement = new ArrayList<>();

    public Collection<StatementContextBase<?, ?, ?>> getEffectOfStatement() {
        return effectOfStatement;
    }

    public void addAsEffectOfStatement(StatementContextBase<?, ?, ?> ctx) {
        effectOfStatement.add(ctx);
    }

    private ModelProcessingPhase completedPhase;

    private Multimap<ModelProcessingPhase, OnPhaseFinished> phaseListeners = HashMultimap
            .create();
    private Multimap<ModelProcessingPhase, ContextMutation> phaseMutation = HashMultimap
            .create();

    private D declaredInstance;
    private E effectiveInstance;

    private StatementContextBase<?, ?, ?> originalCtx;
    private List<TypeOfCopy> copyHistory;

    private boolean isSupportedToBuildEffective = true;

    @Override
    public boolean isSupportedToBuildEffective() {
        return isSupportedToBuildEffective;
    }

    @Override
    public void setIsSupportedToBuildEffective(boolean isSupportedToBuildEffective) {
        this.isSupportedToBuildEffective = isSupportedToBuildEffective;
    }

    @Override
    public List<TypeOfCopy> getCopyHistory() {
        return copyHistory;
    }

    @Override
    public void addToCopyHistory(TypeOfCopy typeOfCopy) {
        this.copyHistory.add(typeOfCopy);
    }

    @Override
    public void addAllToCopyHistory(List<TypeOfCopy> typeOfCopyList) {
        this.copyHistory.addAll(typeOfCopyList);
    }

    @Override
    public StatementContextBase<?, ?, ?> getOriginalCtx() {
        return originalCtx;
    }

    @Override
    public void setOriginalCtx(StatementContextBase<?, ?, ?> originalCtx) {
        this.originalCtx = originalCtx;
    }

    @Override
    public void setOrder(int order) {
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
    public void setCompletedPhase(ModelProcessingPhase completedPhase) {
        this.completedPhase = completedPhase;
    }

    StatementContextBase(@Nonnull ContextBuilder<A, D, E> builder)
            throws SourceException {
        this.definition = builder.getDefinition();
        this.identifier = builder.getIdentifier();
        this.statementDeclSource = builder.getStamementSource();
        this.completedPhase = null;
        initCopyHistory();
    }

    StatementContextBase(StatementContextBase<A, D, E> original) {
        this.definition = original.definition;
        this.identifier = original.identifier;
        this.statementDeclSource = original.statementDeclSource;
        this.completedPhase = null;
        initCopyHistory();
    }

    private void initCopyHistory() {
        this.copyHistory = new LinkedList<>();
        this.copyHistory.add(TypeOfCopy.ORIGINAL);
    }

    @Override
    public abstract StatementContextBase<?, ?, ?> getParentContext();

    @Override
    public abstract RootStatementContext<?, ?, ?> getRoot();

    @Override
    public StatementIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public StatementSource getStatementSource() {
        return statementDeclSource.getStatementSource();
    }

    @Override
    public StatementSourceReference getStatementSourceReference() {
        return statementDeclSource;
    }

    @Override
    public String rawStatementArgument() {
        return identifier.getArgument();
    }

    @Override
    public Collection<StatementContextBase<?, ?, ?>> declaredSubstatements() {
        return Collections.unmodifiableCollection(declared);
    }

    @Override
    public Collection<StatementContextBase<?, ?, ?>> effectiveSubstatements() {
        return effective;
    }

    public void addEffectiveSubstatement(
            StatementContextBase<?, ?, ?> substatement) {
        effective.add(substatement);
    }

    public void addDeclaredSubstatement(
            StatementContextBase<?, ?, ?> substatement) {
        declared.add(substatement);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ContextBuilder<?, ?, ?> substatementBuilder(
            StatementDefinitionContext<?, ?, ?> def,
            StatementSourceReference ref) {
        return new ContextBuilder(def, ref) {

            @Override
            public StatementContextBase build() throws SourceException {
                StatementContextBase<?, ?, ?> potential = null;

                if (getDefinition().getPublicView() != Rfc6020Mapping.AUGMENT) {
                    potential = substatements.get(getIdentifier());
                }
                if (potential == null) {
                    potential = new SubstatementContext(
                            StatementContextBase.this, this);
                    substatements.put(getIdentifier(), potential);
                }
                potential.resetLists();
                switch (this.getStamementSource().getStatementSource()) {
                case DECLARATION:
                    declared.add(potential);
                    break;
                case CONTEXT:
                    effective.add(potential);
                    break;
                }
                return potential;
            }
        };
    }

    @Override
    public StorageNodeType getStorageNodeType() {
        return StorageNodeType.STATEMENT_LOCAL;
    }

    @Override
    public D buildDeclared() {
        Preconditions
                .checkArgument(completedPhase == ModelProcessingPhase.FULL_DECLARATION
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

    void resetLists() {
        declared.clear();
    }

    boolean tryToCompletePhase(ModelProcessingPhase phase)
            throws SourceException {
        Iterator<ContextMutation> openMutations = phaseMutation.get(phase)
                .iterator();
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

    private void onPhaseCompleted(ModelProcessingPhase phase)
            throws SourceException {
        completedPhase = phase;
        Iterator<OnPhaseFinished> listener = phaseListeners.get(completedPhase)
                .iterator();
        while (listener.hasNext()) {
            OnPhaseFinished next = listener.next();
            if (next.phaseFinished(this, phase)) {
                listener.remove();
            }
        }
    }

    /**
     *
     * Ends declared section of current node.
     *
     * @param ref
     * @throws SourceException
     *
     */
    void endDeclared(StatementSourceReference ref, ModelProcessingPhase phase)
            throws SourceException {
        definition().onDeclarationFinished(this, phase);
    }

    protected final StatementDefinitionContext<A, D, E> definition() {
        return definition;
    }

    @Override
    protected void checkLocalNamespaceAllowed(
            Class<? extends IdentifierNamespace<?, ?>> type) {
        definition().checkNamespaceAllowed(type);
    }

    @Override
    protected <K, V, N extends IdentifierNamespace<K, V>> void onNamespaceElementAdded(
            Class<N> type, K key, V value) {
        // definition().onNamespaceElementAdded(this, type, key, value);
    }

    <K, V, N extends IdentifierNamespace<K, V>> void onNamespaceItemAddedAction(
            final Class<N> type, K key, final OnNamespaceItemAdded listener)
            throws SourceException {
        Object potential = getFromNamespace(type, key);
        if (potential != null) {
            listener.namespaceItemAdded(this, type, key, potential);
            return;
        }
        NamespaceBehaviour<K, V, N> behaviour = getBehaviourRegistry()
                .getNamespaceBehaviour(type);
        if (behaviour instanceof NamespaceBehaviourWithListeners) {
            NamespaceBehaviourWithListeners<K, V, N> casted = (NamespaceBehaviourWithListeners<K, V, N>) behaviour;
            casted.addValueListener(key, new ValueAddedListener(this) {
                @Override
                void onValueAdded(Object key, Object value) {
                    try {
                        listener.namespaceItemAdded(StatementContextBase.this,
                                type, key, value);
                    } catch (SourceException e) {
                        throw Throwables.propagate(e);
                    }
                }
            });
        }
    }

    @Override
    public StatementDefinition getPublicDefinition() {
        return definition().getPublicView();
    }

    @Override
    public ModelActionBuilder newInferenceAction(ModelProcessingPhase phase) {
        return getRoot().getSourceContext().newInferenceAction(phase);
    }

    void addPhaseCompletedListener(ModelProcessingPhase phase,
            OnPhaseFinished listener) throws SourceException {
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

    void addMutation(ModelProcessingPhase phase, ContextMutation mutation) {
        ModelProcessingPhase finishedPhase = completedPhase;
        while (finishedPhase != null) {
            if (phase.equals(finishedPhase)) {
                throw new IllegalStateException(
                        "Mutation registered after phase was completed.");
            }
            finishedPhase = finishedPhase.getPreviousPhase();
        }
        phaseMutation.put(phase, mutation);
    }

    @Override
    public <K, KT extends K, N extends StatementNamespace<K, ?, ?>> void addContext(
            Class<N> namespace, KT key, StmtContext<?, ?, ?> stmt) {
        addContextToNamespace(namespace, (K) key, stmt);
    }
}
