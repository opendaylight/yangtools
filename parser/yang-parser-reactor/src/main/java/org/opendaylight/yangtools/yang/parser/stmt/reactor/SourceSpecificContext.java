/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceException;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.stmt.NamespaceBinding;
import org.opendaylight.yangtools.yang.parser.source.StatementDefinitionResolver;
import org.opendaylight.yangtools.yang.parser.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.IdentifierBinding;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceKeyCriterion;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceStorage;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementDefinitions;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SourceSpecificContext implements NamespaceStorage, Mutable {
    enum PhaseCompletionProgress {
        NO_PROGRESS,
        PROGRESS,
        FINISHED
    }

    private static final class SupportedStatements extends NamespaceAccess<QName, StatementSupport<?, ?, ?>> {
        private final @NonNull ReactorStatementDefinitionResolver statementResolver;

        SupportedStatements(final @NonNull ReactorStatementDefinitionResolver statementResolver) {
            this.statementResolver = requireNonNull(statementResolver);
        }

        @Override
        ParserNamespace<QName, StatementSupport<?, ?, ?>> namespace() {
            return StatementSupport.NAMESPACE;
        }

        @Override
        StatementSupport<?, ?, ?> valueFrom(final NamespaceStorage storage, final QName key) {
            return statementResolver.lookupSupport(key);
        }

        @Override
        void valueTo(final NamespaceStorage storage, final QName key, final StatementSupport<?, ?, ?> value) {
            throw uoe();
        }

        @Override
        Map<QName, StatementSupport<?, ?, ?>> allFrom(final NamespaceStorage storage) {
            throw uoe();
        }

        @Override
        Entry<QName, StatementSupport<?, ?, ?>> entryFrom(final NamespaceStorage storage,
                final NamespaceKeyCriterion<QName> criterion) {
            throw uoe();
        }

        @Override
        void addListener(final QName key, final KeyedValueAddedListener<QName, StatementSupport<?, ?, ?>> listener) {
            throw uoe();
        }

        @Override
        void addListener(final PredicateValueAddedListener<QName, StatementSupport<?, ?, ?>> listener) {
            throw uoe();
        }

        private static UnsupportedOperationException uoe() {
            return new UnsupportedOperationException("StatementSupportNamespace is immutable");
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(SourceSpecificContext.class);

    // TODO: consider keying by Byte equivalent of ExecutionOrder
    private final Multimap<ModelProcessingPhase, ModifierImpl> modifiers = HashMultimap.create();
    private final @NonNull ReactorStatementDefinitionResolver statementResolver =
        new ReactorStatementDefinitionResolver();
    private final @NonNull SupportedStatements statementSupports = new SupportedStatements(statementResolver);
    private final @NonNull IdentifierBinding identifierBinding;
    private final @NonNull RootStatementContext<?, ?, ?> root;
    private final @NonNull BuildGlobalContext globalContext;
    private final @NonNull QNameModule definingModule;
    private final @NonNull SourceInfo sourceInfo;

    // Freed as soon as we complete ModelProcessingPhase.EFFECTIVE_MODEL
    private StatementStreamSource streamSource;

    /*
     * "imported" namespaces in this source -- this points to RootStatementContexts of
     * - modules imported via 'import' statement
     * - parent module, declared via 'belongs-to' statement
     */
    private Set<RootStatementContext<?, ?, ?>> importedNamespaces = Set.of();

    // TODO: consider using ExecutionOrder byte for these two
    private ModelProcessingPhase finishedPhase = ModelProcessingPhase.INIT;
    private ModelProcessingPhase inProgressPhase;

    // If not null, do not add anything to modifiers, but record it here.
    private List<Entry<ModelProcessingPhase, ModifierImpl>> delayedModifiers;

    @NonNullByDefault
    SourceSpecificContext(final BuildGlobalContext globalContext, final SourceInfo sourceInfo,
            final QNameModule definingModule, final NamespaceBinding namespaceBinding,
            final StatementStreamSource streamSource) {
        this.globalContext = requireNonNull(globalContext);
        this.sourceInfo = requireNonNull(sourceInfo);
        this.definingModule = requireNonNull(definingModule);
        identifierBinding = IdentifierBinding.of(namespaceBinding);
        this.streamSource = requireNonNull(streamSource);

        final var statement = streamSource.root();

        root = new RootStatementContext<>(sourceId().name(), definingModule, identifierBinding, this,
            globalContext.linkStatementDefinition(statement.definition(), yangVersion()),
            statement.sourceRef(), statement.rawArgument(), statement.size());
    }

    @NonNull BuildGlobalContext globalContext() {
        return globalContext;
    }

    @NonNull SourceIdentifier sourceId() {
        return sourceInfo.sourceId();
    }

    @NonNull YangVersion yangVersion() {
        return sourceInfo.yangVersion();
    }

    ModelProcessingPhase getInProgressPhase() {
        return inProgressPhase;
    }

    AbstractResumedStatement<?, ?, ?> createDeclaredChild(final AbstractResumedStatement<?, ?, ?> current,
            final int childId, final QName name, final String argument, final StatementSourceReference ref) {
        var def = globalContext.getStatementDefinition(sourceInfo.yangVersion(), name);
        if (def == null) {
            def = globalContext.getModelDefinedStatementDefinition(name);
            if (def == null) {
                final var extension = statementResolver.lookupSupport(name);
                if (extension != null) {
                    def = new StatementDefinitionContext<>(extension);
                    globalContext.putModelDefinedStatementDefinition(name, def);
                }
            }
        } else if (current != null) {
            def = current.definition().overrideDefinition(def);
        }

        if (def == null) {
            throw new InferenceException(ref, "Statement %s does not have type mapping defined.", name);
        }
        if (def.argumentDefinition() != null) {
            if (argument == null) {
                throw new SourceException(ref, "Statement %s requires an argument", name);
            }
        } else if (argument != null) {
            throw new SourceException(ref, "Statement %s does not take argument", name);
        }

        /*
         * If the current statement definition has argument specific
         * sub-definitions, get argument specific sub-definition based on given
         * argument (e.g. type statement need to be specialized based on its
         * argument).
         */
        if (def.hasArgumentSpecificSubDefinitions()) {
            def = def.getSubDefinitionSpecificForArgument(argument);
        }

        if (current != null) {
            return current.createSubstatement(childId, def, ref, argument);
        }

        final var rootStatement = root.definition().statementName();
        if (!rootStatement.equals(def.statementName())) {
            throw new VerifyException("inconsistent statement name of " + rootStatement);
        }
        final var rootArgument = root.getRawArgument();
        if (!rootArgument.equals(argument)) {
            throw new VerifyException("inconsistent statement argument of " + rootArgument);
        }
        return root;
    }

    @NonNull DeclaredStatement<?> declaredRoot() {
        return root.declared();
    }

    @NonNull EffectiveStatement<?, ?> effectiveRoot() {
        return root.buildEffective();
    }

    void startPhase(final ModelProcessingPhase phase) {
        final ModelProcessingPhase previousPhase = phase.getPreviousPhase();
        verify(Objects.equals(previousPhase, finishedPhase),
            "Phase sequencing violation: previous phase should be %s, source %s has %s", previousPhase, streamSource,
            finishedPhase);

        final Collection<ModifierImpl> previousModifiers = modifiers.get(previousPhase);
        checkState(previousModifiers.isEmpty(), "Previous phase %s has unresolved modifiers %s in source %s",
            previousPhase, previousModifiers, streamSource);

        inProgressPhase = phase;
        LOG.debug("Source {} started phase {}", streamSource, phase);
    }

    private boolean updateImportedNamespaces(final ParserNamespace<?, ?> type, final Object key) {
        if (!ParserNamespaces.IMPORTED_MODULE.equals(type)) {
            return false;
        }
        if (!(key instanceof RootStatementContext<?, ?, ?> context)) {
            throw new VerifyException("Unexpected imported key " + key);
        }

        if (importedNamespaces.isEmpty()) {
            importedNamespaces = LinkedHashSet.newLinkedHashSet(4);
        }
        importedNamespaces.add(context);
        return true;
    }

    /**
     * Set the {@link SourceLinkage} of this {@code module} source.
     *
     * @param importedModules the {@link SourceSpecificContext} accessible by being imported
     * @param includedSubmodules the {@link SourceSpecificContext} accessible by being included
     */
    @NonNullByDefault
    void setLinkage(final Map<Unqualified, SourceSpecificContext> importedModules,
            final Set<SourceSpecificContext> includedSubmodules) {
        if (!(sourceInfo instanceof SourceInfo.Module info)) {
            throw new VerifyException("cannot set linkage on non-module");
        }
        setLinkage(info.prefix(), this, importedModules, includedSubmodules);
    }

    /**
     * Set the {@link SourceLinkage} of this {@code submodule} source.
     *
     * @param importedModules the {@link SourceSpecificContext} accessible by being imported
     * @param includedSubmodules the {@link SourceSpecificContext} accessible by being included
     * @param belongsToPrefix the {@code prefix} of {@code belongs-to} statement
     * @param belongsToSource the {@link SourceSpecificContext} accessible by us belonging to its module
     */
    @NonNullByDefault
    void setLinkage(final Map<Unqualified, SourceSpecificContext> importedModules,
            final Set<SourceSpecificContext> includedSubmodules, final Unqualified belongsToPrefix,
            final SourceSpecificContext belongsToSource) {
        if (!(sourceInfo instanceof SourceInfo.Submodule info)) {
            throw new VerifyException("cannot set linkage on non-submodule");
        }
        if (!belongsToPrefix.equals(info.belongsTo().prefix())) {
            throw new VerifyException("inconsistent belongs-to prefix");
        }
        setLinkage(belongsToPrefix, belongsToSource, importedModules, includedSubmodules);
    }

    private void setLinkage(final @NonNull Unqualified localPrefix, final @NonNull SourceSpecificContext localModule,
            final @NonNull Map<Unqualified, SourceSpecificContext> importedModules,
            final @NonNull Set<SourceSpecificContext> includedSubmodules) {
        // FIXME: YANGTOOLS-1112: populate namespaces according to linkage
    }

    private @NonNull RootStatementContext<Unqualified, ModuleStatement, ModuleEffectiveStatement> rootAsModule() {
        return castRoot(root, ModuleStatement.DEF);
    }

    private static <A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
            @NonNull RootStatementContext<A, D, E> castRoot(final @NonNull RootStatementContext<?, ?, ?> root,
                final @NonNull StatementDefinition<A, D, E> def) {
        if (root.produces(def)) {
            @SuppressWarnings("unchecked")
            final var casted = (RootStatementContext<A, D, E>) root;
            return casted;
        }
        throw new VerifyException("unexpected root " + root);
    }

    @Override
    public <K, V> V putToLocalStorage(final ParserNamespace<K, V> type, final K key, final V value) {
        // RootStatementContext takes care of our namespaces, but intercept IMPORTED_MODULE stores
        return updateImportedNamespaces(type, key) ? null : root.putToLocalStorage(type, key, value);
    }

    @Override
    public <K, V> V putToLocalStorageIfAbsent(final ParserNamespace<K, V> type, final K key, final V value) {
        // RootStatementContext takes care of our namespaces, but intercept IMPORTED_MODULE stores
        return updateImportedNamespaces(type, key) ? null : root.putToLocalStorageIfAbsent(type, key, value);
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.SOURCE_LOCAL_SPECIAL;
    }

    @Override
    public <K, V> V getFromLocalStorage(final ParserNamespace<K, V> type, final K key) {
        final V potentialLocal = root.getFromLocalStorage(type, key);
        if (potentialLocal != null) {
            return potentialLocal;
        }

        for (final NamespaceStorage importedSource : importedNamespaces) {
            final V potential = importedSource.getFromLocalStorage(type, key);
            if (potential != null) {
                return potential;
            }
        }
        return null;
    }

    @Override
    public <K, V> Map<K, V> getAllFromLocalStorage(final ParserNamespace<K, V> type) {
        final Map<K, V> potentialLocal = root.getAllFromLocalStorage(type);
        if (potentialLocal != null) {
            return potentialLocal;
        }

        for (final NamespaceStorage importedSource : importedNamespaces) {
            final Map<K, V> potential = importedSource.getAllFromLocalStorage(type);

            if (potential != null) {
                return potential;
            }
        }
        return null;
    }

    <K, V> NamespaceAccess<K, V> accessNamespace(final ParserNamespace<K, V> type) {
        if (StatementSupport.NAMESPACE.equals(type)) {
            @SuppressWarnings("unchecked")
            final var ret = (NamespaceAccess<K, V>) statementSupports;
            return ret;
        }
        return globalContext.accessNamespace(type);
    }

    @Override
    public GlobalStorage getParentStorage() {
        return globalContext;
    }

    PhaseCompletionProgress tryToCompletePhase(final byte executionOrder) {
        final var phase = verifyNotNull(ModelProcessingPhase.ofExecutionOrder(executionOrder));
        final var currentPhaseModifiers = modifiers.get(phase);

        boolean hasProgressed = tryToProgress(currentPhaseModifiers);
        final boolean phaseCompleted = requireNonNull(root, "Malformed source. Valid root element is missing.")
                .tryToCompletePhase(executionOrder);

        hasProgressed |= tryToProgress(currentPhaseModifiers);

        // TODO: use executionOrder instead?
        if (phaseCompleted && currentPhaseModifiers.isEmpty()) {
            finishedPhase = phase;
            LOG.debug("Source {} finished phase {}", streamSource, phase);
            if (phase == ModelProcessingPhase.EFFECTIVE_MODEL) {
                // We have the effective model acquired, which is the final phase of source interaction.
                LOG.trace("Releasing source {}", streamSource);
                streamSource = null;
            }
            return PhaseCompletionProgress.FINISHED;
        }

        return hasProgressed ? PhaseCompletionProgress.PROGRESS : PhaseCompletionProgress.NO_PROGRESS;
    }

    private boolean tryToProgress(final Collection<ModifierImpl> currentPhaseModifiers) {
        boolean hasProgressed = false;

        // We are about to iterate over the modifiers and invoke callbacks. Those callbacks can end up circling back
        // and modifying the same collection. This asserts that modifiers should not be modified.
        delayedModifiers = List.of();

        // Try making forward progress ...
        final Iterator<ModifierImpl> modifier = currentPhaseModifiers.iterator();
        while (modifier.hasNext()) {
            if (modifier.next().tryApply()) {
                modifier.remove();
                hasProgressed = true;
            }
        }

        // We have finished iterating, if we have any delayed modifiers, put them back. This may seem as if we want
        // to retry the loop, but we do not have to, as we will be circling back anyway.
        //
        // The thing is, we are inherently single-threaded and therefore if we observe non-empty delayedModifiers, the
        // only way that could happen is through a callback, which in turn means we have made progress.
        if (!delayedModifiers.isEmpty()) {
            verify(hasProgressed, "Delayed modifiers encountered without making progress in %s", this);
            for (Entry<ModelProcessingPhase, ModifierImpl> entry : delayedModifiers) {
                modifiers.put(entry.getKey(), entry.getValue());
            }
        }
        delayedModifiers = null;

        return hasProgressed;
    }

    @NonNull ModelActionBuilder newInferenceAction(final @NonNull ModelProcessingPhase phase) {
        final ModifierImpl action = new ModifierImpl();

        if (delayedModifiers != null) {
            if (delayedModifiers.isEmpty()) {
                delayedModifiers = new ArrayList<>(2);
            }
            delayedModifiers.add(Map.entry(phase,action));
        } else {
            modifiers.put(phase, action);
        }

        return action;
    }

    @Override
    public String toString() {
        return "SourceSpecificContext [source=" + streamSource + ", current=" + inProgressPhase + ", finished="
                + finishedPhase + "]";
    }

    Optional<StatementSourceException> failModifiers(final ModelProcessingPhase identifier) {
        final var exceptions = new ArrayList<StatementSourceException>();
        for (var mod : modifiers.get(identifier)) {
            try {
                mod.failModifier();
            } catch (StatementSourceException e) {
                exceptions.add(e);
            }
        }

        return switch (exceptions.size()) {
            case 0 -> Optional.empty();
            case 1 -> Optional.of(exceptions.getFirst());
            default -> {
                final var ex = new InferenceException("Yang model processing phase " + identifier + " failed", root,
                    exceptions.getFirst());
                exceptions.listIterator(1).forEachRemaining(ex::addSuppressed);
                yield Optional.of(ex);
            }
        };
    }

    void loadStatements() {
        LOG.trace("Source {} loading statements for phase {}", streamSource, inProgressPhase);

        switch (inProgressPhase) {
            case SOURCE_LINKAGE ->
                streamSource.writeLinkage(new StatementContextWriter(this, inProgressPhase), stmtDef());
            case STATEMENT_DEFINITION ->
                streamSource.writeLinkageAndStatementDefinitions(new StatementContextWriter(this, inProgressPhase),
                    stmtDef());
            case FULL_DECLARATION ->
                streamSource.writeFull(new StatementContextWriter(this, inProgressPhase), stmtDef());
            default -> {
                // No-op
            }
        }
    }

    private StatementDefinitionResolver stmtDef() {
        // regular YANG statements and extension supports added
        final var supportsForPhase = globalContext.getSupportsForPhase(inProgressPhase);
        statementResolver.addSupports(supportsForPhase.getCommonDefinitions());
        statementResolver.addSupports(supportsForPhase.getDefinitionsSpecificForVersion(sourceInfo.yangVersion()));

        // No further actions needed
        if (inProgressPhase != ModelProcessingPhase.FULL_DECLARATION) {
            return statementResolver;
        }

        // We need to any and all extension statements which have been declared in the context
        final var extensions = globalContext.getNamespace(StatementDefinitions.NAMESPACE);
        if (extensions != null) {
            extensions.forEach((qname, support) -> {
                final var existing = statementResolver.tryAddSupport(qname, support);
                if (existing != null) {
                    LOG.debug("Source {} already defines statement {} as {}", streamSource, qname, existing);
                } else {
                    LOG.debug("Source {} defined statement {} as {}", streamSource, qname, support);
                }
            });
        }

        return statementResolver;
    }
}
