/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
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
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.ImplicitParentAwareStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceKeyCriterion;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.SupportedFeaturesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.SupportedFeaturesNamespace.SupportedFeatures;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.NamespaceBehaviourWithListeners.KeyedValueAddedListener;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.NamespaceBehaviourWithListeners.PredicateValueAddedListener;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase.ContextMutation;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase.OnNamespaceItemAdded;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase.OnPhaseFinished;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractStmtContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends NamespaceStorageSupport implements Mutable<A, D, E> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractStmtContext.class);
    private static final Object NULL_SCHEMAPATH = new Object();

    private final CopyHistory copyHistory;

    private Multimap<ModelProcessingPhase, OnPhaseFinished> phaseListeners = ImmutableMultimap.of();
    private Multimap<ModelProcessingPhase, ContextMutation> phaseMutation = ImmutableMultimap.of();
    private @Nullable ModelProcessingPhase completedPhase;
    private List<StmtContext<?, ?, ?>> effectOfStatement = ImmutableList.of();
    private @Nullable D declaredInstance;
    private @Nullable E effectiveInstance;
    private boolean isSupportedToBuildEffective = true;

    // BooleanFields value
    private byte supportedByFeatures;

    AbstractStmtContext(final CopyHistory copyHistory) {
        this.copyHistory = requireNonNull(copyHistory);
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

    @Override
    public StatementDefinition getPublicDefinition() {
        return definition().getPublicView();
    }

    @Override
    public <K, KT extends K, N extends StatementNamespace<K, ?, ?>> void addContext(final Class<N> namespace,
            final KT key,final StmtContext<?, ?, ?> stmt) {
        addContextToNamespace(namespace, key, stmt);
    }

    @Override
    public ModelActionBuilder newInferenceAction(final ModelProcessingPhase phase) {
        return getRoot().getSourceContext().newInferenceAction(phase);
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
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
        final boolean ret = supportedFeatures == null || StmtContextUtils.checkFeatureSupport(this, supportedFeatures);
        supportedByFeatures = OptionalBoolean.of(ret);
        return ret;
    }

    @Override
    public final Mutable<?, ?, ?> childCopyOf(final StmtContext<?, ?, ?> stmt, final CopyType type,
            final QNameModule targetModule) {
        checkState(stmt.getCompletedPhase() == ModelProcessingPhase.EFFECTIVE_MODEL,
                "Attempted to copy statement %s which has completed phase %s", stmt, stmt.getCompletedPhase());
        checkArgument(stmt instanceof SubstatementContext || stmt instanceof CopiedStmtContext,
            "Unsupported statement %s", stmt);
        return childCopyOf((AbstractStmtContext<?, ?, ?>) stmt, type, targetModule);
    }

    private <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<X, Y>> Mutable<X, Y, Z> childCopyOf(
            final AbstractStmtContext<X, Y, Z> original, final CopyType type, final QNameModule targetModule) {
        final Optional<StatementSupport<?, ?, ?>> implicitParent = definition().getImplicitParentFor(
            original.getPublicDefinition());

        final AbstractStmtContext<X, Y, Z> result;
        final AbstractStmtContext<X, Y, Z> copy;

        if (implicitParent.isPresent()) {
            final StatementDefinitionContext<?, ?, ?> def = new StatementDefinitionContext<>(implicitParent.get());
            final SubstatementContext<X, Y, Z> implicit = new SubstatementContext(this, def,
                original.getStatementSourceReference(), original.rawStatementArgument(),
                original.getStatementArgument(), type);

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

            copy = new CopiedStmtContext<>(implicit, original, childCopyType, type, targetModule);
            implicit.addEffectiveSubstatement(copy);
            result = implicit;
        } else {
            result = copy = new CopiedStmtContext<>(this, original, type, type, targetModule);
        }

        original.definition().onStatementAdded(copy);
        return result;
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
    public Collection<? extends StmtContext<?, ?, ?>> declaredSubstatements() {
        return mutableDeclaredSubstatements();
    }

    @Override
    public Collection<? extends StmtContext<?, ?, ?>> effectiveSubstatements() {
        return mutableEffectiveSubstatements();
    }

    @Beta
    public final boolean hasImplicitParentSupport() {
        return definition().getFactory() instanceof ImplicitParentAwareStatementSupport;
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
    public final void addEffectiveSubstatement(final Mutable<?, ?, ?> substatement) {
        checkCompletedPhase();
        appendEffectiveStatement(substatement);
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
        if (statements.isEmpty()) {
            return;
        }

        statements.forEach(Objects::requireNonNull);
        checkCompletedPhase();
        appendEffectiveStatements(statements);
    }

    @Beta
    public final AbstractStmtContext<?, ?, ?> wrapWithImplicit(final AbstractStmtContext<?, ?, ?> original) {
        final Optional<StatementSupport<?, ?, ?>> optImplicit = definition().getImplicitParentFor(
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

    private void checkCompletedPhase() {
        final ModelProcessingPhase inProgressPhase = getRoot().getSourceContext().getInProgressPhase();
        checkState(inProgressPhase == ModelProcessingPhase.FULL_DECLARATION
                || inProgressPhase == ModelProcessingPhase.EFFECTIVE_MODEL,
                "Effective statement cannot be added in declared phase at: %s", getStatementSourceReference());
    }

    public abstract void removeStatementFromEffectiveSubstatements(StatementDefinition statementDef);

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
    public final void removeStatementFromEffectiveSubstatements(final StatementDefinition statementDef,
            final String statementArg) {
        if (statementArg == null) {
            removeStatementFromEffectiveSubstatements(statementDef);
        } else {
            doRemoveStatement(statementDef, statementArg);
        }
    }

    @Override
    public abstract AbstractStmtContext<?, ?, ?> getParentContext();

    /**
     * Returns the model root for this statement.
     *
     * @return root context of statement
     */
    @Override
    public abstract RootStatementContext<?, ?, ?> getRoot();

    @Override
    protected void checkLocalNamespaceAllowed(final Class<? extends IdentifierNamespace<?, ?>> type) {
        definition().checkNamespaceAllowed(type);
    }

    @Override
    protected <K, V, N extends IdentifierNamespace<K, V>> void onNamespaceElementAdded(final Class<N> type, final K key,
            final V value) {
        // definition().onNamespaceElementAdded(this, type, key, value);
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("definition", definition()).add("rawArgument", rawStatementArgument());
    }

    /**
     * Return the context in which this statement was defined.
     *
     * @return statement definition
     */
    protected abstract @NonNull StatementDefinitionContext<A, D, E> definition();

    protected abstract boolean isIgnoringIfFeatures();

    protected abstract boolean isIgnoringConfig();

    protected abstract boolean isParentSupportedByFeatures();

    protected abstract void appendEffectiveStatement(Mutable<?, ?, ?> substatement);

    protected abstract void appendEffectiveStatements(Collection<? extends Mutable<?, ?, ?>> statements);

    /**
     * Tries to execute current {@link ModelProcessingPhase} of source parsing.
     *
     * @param phase
     *            to be executed (completed)
     * @return if phase was successfully completed
     * @throws SourceException
     *             when an error occurred in source parsing
     */
    final boolean tryToCompletePhase(final ModelProcessingPhase phase) {
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

        for (final AbstractStmtContext<?, ?, ?> child : declared()) {
            finished &= child.tryToCompletePhase(phase);
        }
        for (final Mutable<?, ?, ?> child : effective()) {
            if (child instanceof AbstractStmtContext) {
                finished &= ((AbstractStmtContext<?, ?, ?>) child).tryToCompletePhase(phase);
            }
        }

        if (finished) {
            onPhaseCompleted(phase);
            return true;
        }
        return false;
    }

    // Guaranteed not to mutate
    abstract Collection<? extends AbstractStmtContext<?, ?, ?>> declared();

    // Guaranteed not to mutate
    abstract Collection<? extends Mutable<?, ?, ?>> effective();

    abstract void doRemoveStatement(StatementDefinition statementDef, @NonNull String statementArg);

    abstract AbstractStmtContext<A, D, E> reparent(AbstractStmtContext<?, ?, ?> newParent);

    /**
     * Adds {@link OnPhaseFinished} listener for a {@link ModelProcessingPhase} end. If the base has already completed
     * the listener is notified immediately.
     *
     * @param phase requested completion phase
     * @param listener listener to invoke
     * @throws NullPointerException if any of the arguments is null
     */
    final void addPhaseCompletedListener(final ModelProcessingPhase phase, final OnPhaseFinished listener) {
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
    final void addMutation(final ModelProcessingPhase phase, final ContextMutation mutation) {
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
                listener.namespaceItemAdded(AbstractStmtContext.this, type, key, value);
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
        listener.namespaceItemAdded(this, type, match.getKey(), match.getValue());
    }

    final <K, V, N extends IdentifierNamespace<K, V>> void waitForPhase(final Object value, final Class<N> type,
            final ModelProcessingPhase phase, final NamespaceKeyCriterion<K> criterion,
            final OnNamespaceItemAdded listener) {
        ((AbstractStmtContext<?, ? ,?>) value).addPhaseCompletedListener(phase,
            (context, phaseCompleted) -> {
                selectMatch(type, criterion, listener);
                return true;
            });
    }

    /**
     * Ends declared section of current node.
     */
    final void endDeclared(final ModelProcessingPhase phase) {
        definition().onDeclarationFinished(this, phase);
    }

    static void removeStatement(final Iterator<? extends StmtContext<?, ?, ?>> iterator,
            final StatementDefinition statementDef) {
        while (iterator.hasNext()) {
            final StmtContext<?, ?, ?> next = iterator.next();
            if (statementDef.equals(next.getPublicDefinition())) {
                iterator.remove();
            }
        }
    }

    static void removeStatement(final Iterator<? extends StmtContext<?, ?, ?>> iterator,
            final StatementDefinition statementDef, @NonNull final String statementArg) {
        while (iterator.hasNext()) {
            final StmtContext<?, ?, ?> next = iterator.next();
            if (statementDef.equals(next.getPublicDefinition()) && statementArg.equals(next.rawStatementArgument())) {
                iterator.remove();
            }
        }
    }

    /**
     * Config statements are not all that common which means we are performing a recursive search towards the root
     * every time {@link #isConfiguration()} is invoked. This is quite expensive because it causes a linear search
     * for the (usually non-existent) config statement.
     *
     * <p>
     * This field maintains a resolution cache, so once we have returned a result, we will keep on returning the same
     * result without performing any lookups.
     *
     * @param substatement A substatement
     * @param field OptionalBoolean field handle
     * @return true if this statement represents configuration
     */
    static boolean substatementIsConfiguration(final AbstractStmtContext<?, ?, ?> substatement, final VarHandle field) {
        if (substatement.isIgnoringConfig()) {
            return true;
        }

        final byte configuration = (byte) field.get(substatement);
        if (OptionalBoolean.isPresent(configuration)) {
            return OptionalBoolean.get(configuration);
        }

        final StmtContext<Boolean, ?, ?> configStatement = StmtContextUtils.findFirstSubstatement(substatement,
            ConfigStatement.class);
        final boolean parentIsConfig = substatement.coerceParentContext().isConfiguration();

        final boolean isConfig;
        if (configStatement != null) {
            isConfig = configStatement.coerceStatementArgument();

            // Validity check: if parent is config=false this cannot be a config=true
            InferenceException.throwIf(isConfig && !parentIsConfig, substatement.getStatementSourceReference(),
                    "Parent node has config=false, this node must not be specifed as config=true");
        } else {
            // If "config" statement is not specified, the default is the same as the parent's "config" value.
            isConfig = parentIsConfig;
        }

        // Resolved, make sure we cache this return
        field.set(substatement, OptionalBoolean.of(isConfig));
        return isConfig;
    }

    static boolean substatementIsIgnoringConfig(final AbstractStmtContext<?, ?, ?> substatement,
            final VarHandle field) {
        final byte ignoreConfig = (byte) field.get(substatement);
        if (OptionalBoolean.isPresent(ignoreConfig)) {
            return OptionalBoolean.get(ignoreConfig);
        }

        final boolean ret = substatement.definition().isIgnoringConfig()
                || substatement.getParentContext().isIgnoringConfig();
        field.set(substatement, OptionalBoolean.of(ret));
        return ret;
    }


    static boolean substatementIsIgnoringIfFeatures(final AbstractStmtContext<?, ?, ?> substatement,
            final VarHandle field) {
        final byte ignoreIfFeature = (byte) field.get(substatement);
        if (OptionalBoolean.isPresent(ignoreIfFeature)) {
            return OptionalBoolean.get(ignoreIfFeature);
        }

        final boolean ret = substatement.definition().isIgnoringIfFeatures()
                || substatement.getParentContext().isIgnoringIfFeatures();
        field.set(substatement, OptionalBoolean.of(ret));
        return ret;
    }

    static @NonNull Optional<SchemaPath> substatementSchemaPath(final AbstractStmtContext<?, ?, ?> substatement,
            final VarHandle field) {
        final Object existing = field.getAcquire(substatement);
        return existing != null ? unmaskSchemaPath(existing) : loadSchemaPath(substatement, field);
    }

    private static @NonNull Optional<SchemaPath> unmaskSchemaPath(final Object obj) {
        return obj instanceof SchemaPath ? Optional.of((SchemaPath) obj) : Optional.empty();
    }

    private static @NonNull Optional<SchemaPath> loadSchemaPath(final AbstractStmtContext<?, ?, ?> substatement,
            final VarHandle field) {
        final Object masked = maskedSchemaPath(substatement);
        final Object witness = field.compareAndExchangeRelease(substatement, null, masked);
        return unmaskSchemaPath(witness == null ? masked : witness);
    }

    private static @NonNull Object maskedSchemaPath(final AbstractStmtContext<?, ?, ?> substatement) {
        final AbstractStmtContext<?, ?, ?> parent = substatement.getParentContext();
        final Optional<SchemaPath> maybeParentPath = parent.getSchemaPath();
        verify(maybeParentPath.isPresent(), "Parent %s does not have a SchemaPath", parent);
        final SchemaPath parentPath = maybeParentPath.get();

        if (StmtContextUtils.isUnknownStatement(substatement)) {
            return parentPath.createChild(substatement.getPublicDefinition().getStatementName());
        }
        final Object argument = substatement.getStatementArgument();
        if (argument instanceof QName) {
            final QName qname = (QName) argument;
            if (StmtContextUtils.producesDeclared(substatement, UsesStatement.class)) {
                return parentPath;
            }

            return parentPath.createChild(qname);
        }
        if (argument instanceof String) {
            // FIXME: This may yield illegal argument exceptions
            final Optional<StmtContext<?, ?, ?>> originalCtx = substatement.getOriginalCtx();
            final QName qname = StmtContextUtils.qnameFromArgument(originalCtx.orElse(substatement), (String) argument);
            return parentPath.createChild(qname);
        }
        if (argument instanceof SchemaNodeIdentifier
                && (StmtContextUtils.producesDeclared(substatement, AugmentStatement.class)
                        || StmtContextUtils.producesDeclared(substatement, RefineStatement.class)
                        || StmtContextUtils.producesDeclared(substatement, DeviationStatement.class))) {

            return parentPath.createChild(((SchemaNodeIdentifier) argument).getPathFromRoot());
        }

        // FIXME: this does not look right, it should be NULL_SCHEMAPATH
        return maybeParentPath.orElse(null);
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

    private <K, V, N extends IdentifierNamespace<K, V>> NamespaceBehaviourWithListeners<K, V, N> getBehaviour(
            final Class<N> type) {
        final NamespaceBehaviour<K, V, N> behaviour = getBehaviourRegistry().getNamespaceBehaviour(type);
        checkArgument(behaviour instanceof NamespaceBehaviourWithListeners, "Namespace %s does not support listeners",
            type);

        return (NamespaceBehaviourWithListeners<K, V, N>) behaviour;
    }

    private static <T> Multimap<ModelProcessingPhase, T> newMultimap() {
        return Multimaps.newListMultimap(new EnumMap<>(ModelProcessingPhase.class), () -> new ArrayList<>(1));
    }
}
