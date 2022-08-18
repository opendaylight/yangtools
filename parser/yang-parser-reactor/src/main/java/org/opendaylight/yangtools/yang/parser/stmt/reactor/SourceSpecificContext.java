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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementDefinitions;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixResolver;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinitionMap;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SourceSpecificContext implements NamespaceStorageNode, NamespaceBehaviour.Registry, Mutable {
    enum PhaseCompletionProgress {
        NO_PROGRESS,
        PROGRESS,
        FINISHED
    }

    private static final class SupportedStatements extends NamespaceBehaviour<QName, StatementSupport<?, ?, ?>,
                ParserNamespace<QName, StatementSupport<?, ?, ?>>> {
        private final QNameToStatementDefinitionMap statementDefinitions;

        SupportedStatements(final QNameToStatementDefinitionMap statementDefinitions) {
            super(StatementSupport.NAMESPACE);
            this.statementDefinitions = requireNonNull(statementDefinitions);
        }

        @Override
        public StatementSupport<?, ?, ?> getFrom(final NamespaceStorageNode storage, final QName key) {
            return statementDefinitions.getSupport(key);
        }

        @Override
        public Map<QName, StatementSupport<?, ?, ?>> getAllFrom(final NamespaceStorageNode storage) {
            throw uoe();
        }

        @Override
        public void addTo(final NamespaceStorageNode storage, final QName key, final StatementSupport<?, ?, ?> value) {
            throw uoe();
        }

        private static UnsupportedOperationException uoe() {
            return new UnsupportedOperationException("StatementSupportNamespace is immutable");
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(SourceSpecificContext.class);

    // TODO: consider keying by Byte equivalent of ExecutionOrder
    private final Multimap<ModelProcessingPhase, ModifierImpl> modifiers = HashMultimap.create();
    private final QNameToStatementDefinitionMap qnameToStmtDefMap = new QNameToStatementDefinitionMap();
    private final SupportedStatements statementSupports = new SupportedStatements(qnameToStmtDefMap);
    private final HashMapPrefixResolver prefixToModuleMap = new HashMapPrefixResolver();
    private final @NonNull BuildGlobalContext globalContext;

    // Freed as soon as we complete ModelProcessingPhase.EFFECTIVE_MODEL
    private StatementStreamSource source;

    /*
     * "imported" namespaces in this source -- this points to RootStatementContexts of
     * - modules imported via 'import' statement
     * - parent module, declared via 'belongs-to' statement
     */
    private List<RootStatementContext<?, ?, ?>> importedNamespaces = ImmutableList.of();
    private RootStatementContext<?, ?, ?> root;
    // TODO: consider using ExecutionOrder byte for these two
    private ModelProcessingPhase finishedPhase = ModelProcessingPhase.INIT;
    private ModelProcessingPhase inProgressPhase;

    // If not null, do not add anything to modifiers, but record it here.
    private List<Entry<ModelProcessingPhase, ModifierImpl>> delayedModifiers;

    SourceSpecificContext(final BuildGlobalContext globalContext, final StatementStreamSource source) {
        this.globalContext = requireNonNull(globalContext);
        this.source = requireNonNull(source);
    }

    @NonNull BuildGlobalContext globalContext() {
        return globalContext;
    }

    ModelProcessingPhase getInProgressPhase() {
        return inProgressPhase;
    }

    AbstractResumedStatement<?, ?, ?> createDeclaredChild(final AbstractResumedStatement<?, ?, ?> current,
            final int childId, final QName name, final String argument, final StatementSourceReference ref) {
        StatementDefinitionContext<?, ?, ?> def = globalContext.getStatementDefinition(getRootVersion(), name);
        if (def == null) {
            def = globalContext.getModelDefinedStatementDefinition(name);
            if (def == null) {
                final StatementSupport<?, ?, ?> extension = qnameToStmtDefMap.getSupport(name);
                if (extension != null) {
                    def = new StatementDefinitionContext<>(extension);
                    globalContext.putModelDefinedStatementDefinition(name, def);
                }
            }
        } else if (current != null) {
            def = current.definition().overrideDefinition(def);
        }

        if (InferenceException.throwIfNull(def, ref, "Statement %s does not have type mapping defined.", name)
                .getArgumentDefinition().isPresent()) {
            SourceException.throwIfNull(argument, ref, "Statement %s requires an argument", name);
        } else {
            SourceException.throwIf(argument != null, ref, "Statement %s does not take argument", name);
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

        /*
         * If root is null or root version is other than default,
         * we need to create new root.
         */
        if (root == null) {
            root = new RootStatementContext<>(this, def, ref, argument);
        } else if (!RootStatementContext.DEFAULT_VERSION.equals(root.yangVersion())
                && inProgressPhase == ModelProcessingPhase.SOURCE_LINKAGE) {
            root = new RootStatementContext<>(this, def, ref, argument, root.yangVersion(),
                    root.getRootIdentifier());
        } else {
            final QName rootStatement = root.definition().getStatementName();
            final String rootArgument = root.rawArgument();

            checkState(Objects.equals(def.getStatementName(), rootStatement) && Objects.equals(argument, rootArgument),
                "Root statement was already defined as '%s %s'.", rootStatement, rootArgument);
        }
        return root;
    }

    RootStatementContext<?, ?, ?> getRoot() {
        return root;
    }

    /**
     * Return version of root statement context.
     *
     * @return version of root statement context
     */
    YangVersion getRootVersion() {
        return root != null ? root.yangVersion() : RootStatementContext.DEFAULT_VERSION;
    }

    void startPhase(final ModelProcessingPhase phase) {
        final ModelProcessingPhase previousPhase = phase.getPreviousPhase();
        verify(Objects.equals(previousPhase, finishedPhase),
            "Phase sequencing violation: previous phase should be %s, source %s has %s", previousPhase, source,
            finishedPhase);

        final Collection<ModifierImpl> previousModifiers = modifiers.get(previousPhase);
        checkState(previousModifiers.isEmpty(), "Previous phase %s has unresolved modifiers %s in source %s",
            previousPhase, previousModifiers, source);

        inProgressPhase = phase;
        LOG.debug("Source {} started phase {}", source, phase);
    }

    private void updateImportedNamespaces(final ParserNamespace<?, ?> type, final Object value) {
        if (SourceParserNamespaces.BELONGSTO_PREFIX_TO_MODULECTX.equals(type)
            || SourceParserNamespaces.IMPORTED_MODULE.equals(type)) {
            verify(value instanceof RootStatementContext, "Unexpected imported value %s", value);

            if (importedNamespaces.isEmpty()) {
                importedNamespaces = new ArrayList<>(1);
            }
            importedNamespaces.add((RootStatementContext<?, ?, ?>) value);
        }
    }

    @Override
    public <K, V, N extends ParserNamespace<K, V>> V putToLocalStorage(final N type, final K key, final V value) {
        // RootStatementContext takes care of IncludedModuleContext and the rest...
        final V ret = getRoot().putToLocalStorage(type, key, value);
        // FIXME: what about duplicates?
        updateImportedNamespaces(type, value);
        return ret;
    }

    @Override
    public <K, V, N extends ParserNamespace<K, V>> V putToLocalStorageIfAbsent(final N type, final K key,
           final V value) {
        // RootStatementContext takes care of IncludedModuleContext and the rest...
        final V ret = getRoot().putToLocalStorageIfAbsent(type, key, value);
        if (ret == null) {
            updateImportedNamespaces(type, value);
        }
        return ret;
    }

    @Override
    public StorageNodeType getStorageNodeType() {
        return StorageNodeType.SOURCE_LOCAL_SPECIAL;
    }

    @Override
    public <K, V, N extends ParserNamespace<K, V>> V getFromLocalStorage(final N type, final K key) {
        final V potentialLocal = getRoot().getFromLocalStorage(type, key);
        if (potentialLocal != null) {
            return potentialLocal;
        }

        for (final NamespaceStorageNode importedSource : importedNamespaces) {
            final V potential = importedSource.getFromLocalStorage(type, key);
            if (potential != null) {
                return potential;
            }
        }
        return null;
    }

    @Override
    public <K, V, N extends ParserNamespace<K, V>> Map<K, V> getAllFromLocalStorage(final N type) {
        final Map<K, V> potentialLocal = getRoot().getAllFromLocalStorage(type);
        if (potentialLocal != null) {
            return potentialLocal;
        }

        for (final NamespaceStorageNode importedSource : importedNamespaces) {
            final Map<K, V> potential = importedSource.getAllFromLocalStorage(type);

            if (potential != null) {
                return potential;
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V, N extends ParserNamespace<K, V>> NamespaceBehaviour<K, V, N> getNamespaceBehaviour(final N type) {
        if (StatementSupport.NAMESPACE.equals(type)) {
            return (NamespaceBehaviour<K, V, N>) statementSupports;
        }
        return globalContext.getNamespaceBehaviour(type);
    }

    @Override
    public BuildGlobalContext getParentNamespaceStorage() {
        return globalContext;
    }

    PhaseCompletionProgress tryToCompletePhase(final byte executionOrder) {
        final ModelProcessingPhase phase = verifyNotNull(ModelProcessingPhase.ofExecutionOrder(executionOrder));
        final Collection<ModifierImpl> currentPhaseModifiers = modifiers.get(phase);

        boolean hasProgressed = tryToProgress(currentPhaseModifiers);
        final boolean phaseCompleted = requireNonNull(root, "Malformed source. Valid root element is missing.")
                .tryToCompletePhase(executionOrder);

        hasProgressed |= tryToProgress(currentPhaseModifiers);

        // TODO: use executionOrder instead?
        if (phaseCompleted && currentPhaseModifiers.isEmpty()) {
            finishedPhase = phase;
            LOG.debug("Source {} finished phase {}", source, phase);
            if (phase == ModelProcessingPhase.EFFECTIVE_MODEL) {
                // We have the effective model acquired, which is the final phase of source interaction.
                LOG.trace("Releasing source {}", source);
                source = null;
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
        return "SourceSpecificContext [source=" + source + ", current=" + inProgressPhase + ", finished="
                + finishedPhase + "]";
    }

    Optional<SourceException> failModifiers(final ModelProcessingPhase identifier) {
        final List<SourceException> exceptions = new ArrayList<>();
        for (final ModifierImpl mod : modifiers.get(identifier)) {
            try {
                mod.failModifier();
            } catch (final SourceException e) {
                exceptions.add(e);
            }
        }

        return switch (exceptions.size()) {
            case 0 -> Optional.empty();
            case 1 -> Optional.of(exceptions.get(0));
            default -> {
                final String message = String.format("Yang model processing phase %s failed", identifier);
                final InferenceException ex = new InferenceException(message, root, exceptions.get(0));
                exceptions.listIterator(1).forEachRemaining(ex::addSuppressed);
                yield Optional.of(ex);
            }
        };
    }

    void loadStatements() {
        LOG.trace("Source {} loading statements for phase {}", source, inProgressPhase);

        switch (inProgressPhase) {
            case SOURCE_PRE_LINKAGE:
                source.writePreLinkage(new StatementContextWriter(this, inProgressPhase), stmtDef());
                break;
            case SOURCE_LINKAGE:
                source.writeLinkage(new StatementContextWriter(this, inProgressPhase), stmtDef(), preLinkagePrefixes(),
                    getRootVersion());
                break;
            case STATEMENT_DEFINITION:
                source.writeLinkageAndStatementDefinitions(new StatementContextWriter(this, inProgressPhase), stmtDef(),
                    prefixes(), getRootVersion());
                break;
            case FULL_DECLARATION:
                source.writeFull(new StatementContextWriter(this, inProgressPhase), stmtDef(), prefixes(),
                    getRootVersion());
                break;
            default:
                break;
        }
    }

    private PrefixResolver preLinkagePrefixes() {
        final HashMapPrefixResolver preLinkagePrefixes = new HashMapPrefixResolver();
        final var prefixToNamespaceMap = getAllFromLocalStorage(SourceParserNamespaces.IMP_PREFIX_TO_NAMESPACE);
        if (prefixToNamespaceMap == null) {
            //:FIXME if it is a submodule without any import, the map is null. Handle also submodules and includes...
            return null;
        }

        prefixToNamespaceMap.forEach((key, value) -> preLinkagePrefixes.put(key, QNameModule.create(value)));
        return preLinkagePrefixes;
    }

    private PrefixResolver prefixes() {
        final var allImports = root.getAllFromNamespace(SourceParserNamespaces.IMPORT_PREFIX_TO_MODULECTX);
        if (allImports != null) {
            allImports.forEach((key, value) ->
                prefixToModuleMap.put(key, root.getFromNamespace(SourceParserNamespaces.MODULECTX_TO_QNAME, value)));
        }

        final var allBelongsTo = root.getAllFromNamespace(SourceParserNamespaces.BELONGSTO_PREFIX_TO_MODULECTX);
        if (allBelongsTo != null) {
            allBelongsTo.forEach((key, value) ->
                prefixToModuleMap.put(key, root.getFromNamespace(SourceParserNamespaces.MODULECTX_TO_QNAME, value)));
        }

        return prefixToModuleMap;
    }

    private QNameToStatementDefinition stmtDef() {
        // regular YANG statements and extension supports added
        final StatementSupportBundle supportsForPhase = globalContext.getSupportsForPhase(inProgressPhase);
        qnameToStmtDefMap.putAll(supportsForPhase.getCommonDefinitions());
        qnameToStmtDefMap.putAll(supportsForPhase.getDefinitionsSpecificForVersion(getRootVersion()));

        // No further actions needed
        if (inProgressPhase != ModelProcessingPhase.FULL_DECLARATION) {
            return qnameToStmtDefMap;
        }

        // We need to any and all extension statements which have been declared in the context
        final Map<QName, StatementSupport<?, ?, ?>> extensions = globalContext.getNamespace(
            StatementDefinitions.NAMESPACE);
        if (extensions != null) {
            extensions.forEach((qname, support) -> {
                final StatementSupport<?, ?, ?> existing = qnameToStmtDefMap.putIfAbsent(qname, support);
                if (existing != null) {
                    LOG.debug("Source {} already defines statement {} as {}", source, qname, existing);
                } else {
                    LOG.debug("Source {} defined statement {} as {}", source, qname, support);
                }
            });
        }

        return qnameToStmtDefMap;
    }

    Collection<SourceIdentifier> getRequiredSources() {
        return root.getRequiredSources();
    }

    SourceIdentifier getRootIdentifier() {
        return root.getRootIdentifier();
    }
}
