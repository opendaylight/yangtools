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
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.ImplicitParentAwareStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.MutableStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.Registry;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceKeyCriterion;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.ImplicitSubstatement;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.SupportedFeaturesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.SupportedFeaturesNamespace.SupportedFeatures;
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
        extends NamespaceStorageSupport implements Mutable<A, D, E> {
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

    // Flag bit assignments
    private static final int IS_SUPPORTED_BY_FEATURES    = 0x01;
    private static final int HAVE_SUPPORTED_BY_FEATURES  = 0x02;
    private static final int IS_IGNORE_IF_FEATURE        = 0x04;
    private static final int HAVE_IGNORE_IF_FEATURE      = 0x08;
    // Note: these four are related
    private static final int IS_IGNORE_CONFIG            = 0x10;
    private static final int HAVE_IGNORE_CONFIG          = 0x20;
    private static final int IS_CONFIGURATION            = 0x40;
    private static final int HAVE_CONFIGURATION          = 0x80;

    // Have-and-set flag constants, also used as masks
    private static final int SET_SUPPORTED_BY_FEATURES = HAVE_SUPPORTED_BY_FEATURES | IS_SUPPORTED_BY_FEATURES;
    private static final int SET_CONFIGURATION = HAVE_CONFIGURATION | IS_CONFIGURATION;
    // Note: implies SET_CONFIGURATION, allowing fewer bit operations to be performed
    private static final int SET_IGNORE_CONFIG = HAVE_IGNORE_CONFIG | IS_IGNORE_CONFIG | SET_CONFIGURATION;
    private static final int SET_IGNORE_IF_FEATURE = HAVE_IGNORE_IF_FEATURE | IS_IGNORE_IF_FEATURE;

    private final CopyHistory copyHistory;
    // Note: this field can strictly be derived in InferredStatementContext, but it forms the basis of many of our
    //       operations, hence we want to keep it close by.
    private final @NonNull StatementDefinitionContext<A, D, E> definition;

    private Multimap<ModelProcessingPhase, OnPhaseFinished> phaseListeners = ImmutableMultimap.of();
    private Multimap<ModelProcessingPhase, ContextMutation> phaseMutation = ImmutableMultimap.of();
    private List<Mutable<?, ?, ?>> effective = ImmutableList.of();
    private List<StmtContext<?, ?, ?>> effectOfStatement = ImmutableList.of();

    private @Nullable ModelProcessingPhase completedPhase;
    private @Nullable D declaredInstance;
    private @Nullable E effectiveInstance;

    // Master flag controlling whether this context can yield an effective statement
    // FIXME: investigate the mechanics that are being supported by this, as it would be beneficial if we can get rid
    //        of this flag -- eliminating the initial alignment shadow used by below gap-filler fields.
    private boolean isSupportedToBuildEffective = true;

    // Flag for use with AbstractResumedStatement. This is hiding in the alignment shadow created by above boolean
    private boolean fullyDefined;

    // Flags for use with SubstatementContext. These are hiding in the alignment shadow created by above boolean and
    // hence improve memory layout.
    private byte flags;

    // SchemaPath cache for use with SubstatementContext and InferredStatementContext. This hurts RootStatementContext
    // a bit in terms of size -- but those are only a few and SchemaPath is on its way out anyway.
    private volatile SchemaPath schemaPath;

    StatementContextBase(final StatementContextBase<A, D, E> original) {
        this.copyHistory = original.copyHistory;
        this.definition = original.definition;
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
        final int fl = flags & SET_SUPPORTED_BY_FEATURES;
        if (fl != 0) {
            return fl == SET_SUPPORTED_BY_FEATURES;
        }
        if (isIgnoringIfFeatures()) {
            flags |= SET_SUPPORTED_BY_FEATURES;
            return true;
        }

        /*
         * If parent is supported, we need to check if-features statements of this context.
         */
        if (isParentSupportedByFeatures()) {
            // If the set of supported features has not been provided, all features are supported by default.
            final Set<QName> supportedFeatures = getFromNamespace(SupportedFeaturesNamespace.class,
                    SupportedFeatures.SUPPORTED_FEATURES);
            if (supportedFeatures == null || StmtContextUtils.checkFeatureSupport(this, supportedFeatures)) {
                flags |= SET_SUPPORTED_BY_FEATURES;
                return true;
            }
        }

        // Either parent is not supported or this statement is not supported
        flags |= HAVE_SUPPORTED_BY_FEATURES;
        return false;
    }

    protected abstract boolean isParentSupportedByFeatures();

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

    @Override
    public final @NonNull Registry getBehaviourRegistry() {
        return getRoot().getBehaviourRegistryImpl();
    }

    @Override
    public final YangVersion getRootVersion() {
        return getRoot().getRootVersionImpl();
    }

    @Override
    public final void setRootVersion(final YangVersion version) {
        getRoot().setRootVersionImpl(version);
    }

    @Override
    public final void addMutableStmtToSeal(final MutableStatement mutableStatement) {
        getRoot().addMutableStmtToSealImpl(mutableStatement);
    }

    @Override
    public final void addRequiredSource(final SourceIdentifier dependency) {
        getRoot().addRequiredSourceImpl(dependency);
    }

    @Override
    public final void setRootIdentifier(final SourceIdentifier identifier) {
        getRoot().setRootIdentifierImpl(identifier);
    }

    @Override
    public final boolean isEnabledSemanticVersioning() {
        return getRoot().isEnabledSemanticVersioningImpl();
    }

    @Override
    public StatementSource getStatementSource() {
        return getStatementSourceReference().getStatementSource();
    }

    @Override
    public abstract Collection<? extends StatementContextBase<?, ?, ?>> mutableDeclaredSubstatements();

    @Override
    public Collection<? extends Mutable<?, ?, ?>> mutableEffectiveSubstatements() {
        if (effective instanceof ImmutableCollection) {
            return effective;
        }

        return Collections.unmodifiableCollection(effective);
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

    // YANG example: RPC/action statements always have 'input' and 'output' defined
    @Beta
    public <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<X, Y>> @NonNull Mutable<X, Y, Z>
            appendImplicitSubstatement(final StatementSupport<X, Y, Z> support, final String rawArg) {
        // FIXME: YANGTOOLS-652: This does not need to be a SubstatementContext, in can be a specialized
        //                       StatementContextBase subclass.
        final Mutable<X, Y, Z> ret = new SubstatementContext<>(this, new StatementDefinitionContext<>(support),
                ImplicitSubstatement.of(getStatementSourceReference()), rawArg);
        support.onStatementAdded(ret);
        addEffectiveSubstatement(ret);
        return ret;
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

    // Exists only due to memory optimization
    final boolean fullyDefined() {
        return fullyDefined;
    }

    // Exists only due to memory optimization, should live in AbstractResumedStatement
    final void setFullyDefined() {
        fullyDefined = true;
    }

    @Override
    public D buildDeclared() {
        checkArgument(completedPhase == ModelProcessingPhase.FULL_DECLARATION
                || completedPhase == ModelProcessingPhase.EFFECTIVE_MODEL);
        if (declaredInstance == null) {
            declaredInstance = definition.getFactory().createDeclared(this);
        }
        return declaredInstance;
    }

    @Override
    public E buildEffective() {
        if (effectiveInstance == null) {
            effectiveInstance = definition.getFactory().createEffective(this);
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

        for (final StatementContextBase<?, ?, ?> child : mutableDeclaredSubstatements()) {
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
        definition.onDeclarationFinished(this, phase);
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
        definition.checkNamespaceAllowed(type);
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

        getBehaviour(type).addListener(new KeyedValueAddedListener<>(this, key) {
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

    @Override
    public StatementDefinition getPublicDefinition() {
        return definition.getPublicView();
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
    public final Mutable<?, ?, ?> childCopyOf(final StmtContext<?, ?, ?> stmt, final CopyType type,
            final QNameModule targetModule) {
        checkState(stmt.getCompletedPhase() == ModelProcessingPhase.EFFECTIVE_MODEL,
                "Attempted to copy statement %s which has completed phase %s", stmt, stmt.getCompletedPhase());
        checkArgument(stmt instanceof StatementContextBase, "Unsupported statement %s", stmt);
        return childCopyOf((StatementContextBase<?, ?, ?>) stmt, type, targetModule);
    }

    private <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<X, Y>> Mutable<X, Y, Z> childCopyOf(
            final StatementContextBase<X, Y, Z> original, final CopyType type, final QNameModule targetModule) {
        final Optional<StatementSupport<?, ?, ?>> implicitParent = definition.getImplicitParentFor(
            original.getPublicDefinition());

        final StatementContextBase<X, Y, Z> result;
        final InferredStatementContext<X, Y, Z> copy;

        if (implicitParent.isPresent()) {
            final StatementDefinitionContext<?, ?, ?> def = new StatementDefinitionContext<>(implicitParent.get());
            result = new SubstatementContext(this, def, original.getStatementSourceReference(),
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

            copy = new InferredStatementContext<>(result, original, childCopyType, type, targetModule);
            result.addEffectiveSubstatement(copy);
        } else {
            result = copy = new InferredStatementContext<>(this, original, type, type, targetModule);
        }

        original.definition.onStatementAdded(copy);
        return result;
    }

    @Beta
    public final boolean hasImplicitParentSupport() {
        return definition.getFactory() instanceof ImplicitParentAwareStatementSupport;
    }

    @Beta
    public final StatementContextBase<?, ?, ?> wrapWithImplicit(final StatementContextBase<?, ?, ?> original) {
        final Optional<StatementSupport<?, ?, ?>> optImplicit = definition.getImplicitParentFor(
            original.getPublicDefinition());
        if (optImplicit.isEmpty()) {
            return original;
        }

        final StatementDefinitionContext<?, ?, ?> def = new StatementDefinitionContext<>(optImplicit.get());
        final CopyType type = original.getCopyHistory().getLastOperation();
        final SubstatementContext<?, ?, ?> result = new SubstatementContext(original.getParentContext(), def,
            original.getStatementSourceReference(), original.rawStatementArgument(), original.getStatementArgument(),
            type);

        result.addEffectiveSubstatement(original.reparent(result));
        result.setCompletedPhase(original.getCompletedPhase());
        return result;
    }

    abstract StatementContextBase<A, D, E> reparent(StatementContextBase<?, ?, ?> newParent);

    /**
     * Config statements are not all that common which means we are performing a recursive search towards the root
     * every time {@link #isConfiguration()} is invoked. This is quite expensive because it causes a linear search
     * for the (usually non-existent) config statement.
     *
     * <p>
     * This method maintains a resolution cache, so once we have returned a result, we will keep on returning the same
     * result without performing any lookups, solely to support {@link SubstatementContext#isConfiguration()}.
     *
     * <p>
     * Note: use of this method implies that {@link #isIgnoringConfig()} is realized with
     *       {@link #isIgnoringConfig(StatementContextBase)}.
     */
    final boolean isConfiguration(final StatementContextBase<?, ?, ?> parent) {
        final int fl = flags & SET_CONFIGURATION;
        if (fl != 0) {
            return fl == SET_CONFIGURATION;
        }
        if (isIgnoringConfig(parent)) {
            // Note: SET_CONFIGURATION has been stored in flags
            return true;
        }

        final StmtContext<Boolean, ?, ?> configStatement = StmtContextUtils.findFirstSubstatement(this,
            ConfigStatement.class);
        final boolean isConfig;
        if (configStatement != null) {
            isConfig = configStatement.coerceStatementArgument();
            if (isConfig) {
                // Validity check: if parent is config=false this cannot be a config=true
                InferenceException.throwIf(!parent.isConfiguration(), getStatementSourceReference(),
                        "Parent node has config=false, this node must not be specifed as config=true");
            }
        } else {
            // If "config" statement is not specified, the default is the same as the parent's "config" value.
            isConfig = parent.isConfiguration();
        }

        // Resolved, make sure we cache this return
        flags |= isConfig ? SET_CONFIGURATION : HAVE_CONFIGURATION;
        return isConfig;
    }

    protected abstract boolean isIgnoringConfig();

    /**
     * This method maintains a resolution cache for ignore config, so once we have returned a result, we will
     * keep on returning the same result without performing any lookups. Exists only to support
     * {@link SubstatementContext#isIgnoringConfig()}.
     *
     * <p>
     * Note: use of this method implies that {@link #isConfiguration()} is realized with
     *       {@link #isConfiguration(StatementContextBase)}.
     */
    final boolean isIgnoringConfig(final StatementContextBase<?, ?, ?> parent) {
        final int fl = flags & SET_IGNORE_CONFIG;
        if (fl != 0) {
            return fl == SET_IGNORE_CONFIG;
        }
        if (definition.isIgnoringConfig() || parent.isIgnoringConfig()) {
            flags |= SET_IGNORE_CONFIG;
            return true;
        }

        flags |= HAVE_IGNORE_CONFIG;
        return false;
    }

    protected abstract boolean isIgnoringIfFeatures();

    /**
     * This method maintains a resolution cache for ignore if-feature, so once we have returned a result, we will
     * keep on returning the same result without performing any lookups. Exists only to support
     * {@link SubstatementContext#isIgnoringIfFeatures()}.
     */
    final boolean isIgnoringIfFeatures(final StatementContextBase<?, ?, ?> parent) {
        final int fl = flags & SET_IGNORE_IF_FEATURE;
        if (fl != 0) {
            return fl == SET_IGNORE_IF_FEATURE;
        }
        if (definition.isIgnoringIfFeatures() || parent.isIgnoringIfFeatures()) {
            flags |= SET_IGNORE_IF_FEATURE;
            return true;
        }

        flags |= HAVE_IGNORE_IF_FEATURE;
        return false;
    }

    // Exists only to support SubstatementContext/InferredStatementContext
    final @NonNull Optional<SchemaPath> substatementGetSchemaPath() {
        SchemaPath local = schemaPath;
        if (local == null) {
            synchronized (this) {
                local = schemaPath;
                if (local == null) {
                    local = createSchemaPath(coerceParentContext());
                    schemaPath = local;
                }
            }
        }

        return Optional.ofNullable(local);
    }

    private SchemaPath createSchemaPath(final Mutable<?, ?, ?> parent) {
        final Optional<SchemaPath> maybeParentPath = parent.getSchemaPath();
        verify(maybeParentPath.isPresent(), "Parent %s does not have a SchemaPath", parent);
        final SchemaPath parentPath = maybeParentPath.get();

        if (StmtContextUtils.isUnknownStatement(this)) {
            return parentPath.createChild(getPublicDefinition().getStatementName());
        }
        final Object argument = getStatementArgument();
        if (argument instanceof QName) {
            final QName qname = (QName) argument;
            if (StmtContextUtils.producesDeclared(this, UsesStatement.class)) {
                return maybeParentPath.orElse(null);
            }

            return parentPath.createChild(qname);
        }
        if (argument instanceof String) {
            // FIXME: This may yield illegal argument exceptions
            final Optional<StmtContext<?, ?, ?>> originalCtx = getOriginalCtx();
            final QName qname = StmtContextUtils.qnameFromArgument(originalCtx.orElse(this), (String) argument);
            return parentPath.createChild(qname);
        }
        if (argument instanceof SchemaNodeIdentifier
                && (StmtContextUtils.producesDeclared(this, AugmentStatement.class)
                        || StmtContextUtils.producesDeclared(this, RefineStatement.class)
                        || StmtContextUtils.producesDeclared(this, DeviationStatement.class))) {

            return parentPath.createChild(((SchemaNodeIdentifier) argument).getPathFromRoot());
        }

        // FIXME: this does not look right
        return maybeParentPath.orElse(null);
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("definition", definition).add("rawArgument", rawStatementArgument());
    }
}
