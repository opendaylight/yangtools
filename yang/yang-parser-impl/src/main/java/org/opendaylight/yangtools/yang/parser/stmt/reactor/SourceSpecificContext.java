/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Multimap;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.ImportedNamespaceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementDefinitionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleIdentifierToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModuleMap;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinitionMap;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.BitsSpecificationImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Decimal64SpecificationImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.EnumSpecificationImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.IdentityRefSpecificationImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.InstanceIdentifierSpecificationImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.LeafrefSpecificationImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.ModelDefinedStatementDefinition;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.UnionSpecificationImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.UnknownStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceSpecificContext implements NamespaceStorageNode, NamespaceBehaviour.Registry, Mutable {

    public enum PhaseCompletionProgress {
        NO_PROGRESS,
        PROGRESS,
        FINISHED
    }

    private static final Logger LOG = LoggerFactory.getLogger(SourceSpecificContext.class);
    private static final Map<String, StatementSupport<?, ?, ?>> BUILTIN_TYPE_SUPPORTS =
            ImmutableMap.<String, StatementSupport<?, ?, ?>>builder()
            .put(TypeUtils.DECIMAL64, new Decimal64SpecificationImpl.Definition())
            .put(TypeUtils.UNION, new UnionSpecificationImpl.Definition())
            .put(TypeUtils.ENUMERATION, new EnumSpecificationImpl.Definition())
            .put(TypeUtils.LEAF_REF, new LeafrefSpecificationImpl.Definition())
            .put(TypeUtils.BITS, new BitsSpecificationImpl.Definition())
            .put(TypeUtils.IDENTITY_REF, new IdentityRefSpecificationImpl.Definition())
            .put(TypeUtils.INSTANCE_IDENTIFIER, new InstanceIdentifierSpecificationImpl.Definition())
            .build();

    private final QNameToStatementDefinitionMap qNameToStmtDefMap = new QNameToStatementDefinitionMap();
    private final Multimap<ModelProcessingPhase, ModifierImpl> modifiers = HashMultimap.create();
    private final PrefixToModuleMap prefixToModuleMap = new PrefixToModuleMap();
    private final BuildGlobalContext currentContext;
    private final StatementStreamSource source;

    private Map<QName, StatementSupport<?, ?, ?>> definedExtensions = ImmutableMap.of();
    private Collection<NamespaceStorageNode> importedNamespaces = ImmutableList.of();
    private ModelProcessingPhase finishedPhase = ModelProcessingPhase.INIT;
    private ModelProcessingPhase inProgressPhase;
    private RootStatementContext<?, ?, ?> root;

    SourceSpecificContext(final BuildGlobalContext currentContext, final StatementStreamSource source) {
        this.currentContext = Preconditions.checkNotNull(currentContext);
        this.source = Preconditions.checkNotNull(source);
    }

    boolean isEnabledSemanticVersioning(){
        return currentContext.isEnabledSemanticVersioning();
    }

    ModelProcessingPhase getInProgressPhase() {
        return inProgressPhase;
    }

    ContextBuilder<?, ?, ?> createDeclaredChild(final StatementContextBase<?, ?, ?> current, final QName name,
                                                final StatementSourceReference ref) {
        StatementDefinitionContext<?, ?, ?> def = currentContext.getStatementDefinition(name);

        if (def == null) {
            final StatementSupport<?, ?, ?> extension = definedExtensions.get(name);
            if (extension != null) {
                def = new StatementDefinitionContext<>(extension);
            } else {
                // type-body-stmts
                def = resolveTypeBodyStmts(name.getLocalName());
            }
        } else if (current != null && current.definition().getRepresentingClass().equals(UnknownStatementImpl.class)) {
            /*
             * This code wraps statements encountered inside an extension so they do not get confused with regular
             * statements.
             *
             * FIXME: BUG-7037: re-evaluate whether this is really needed, as this is a very expensive way of making
             *        this work. We really should be peeking into the extension definition to find these nodes,
             *        as otherwise we are not reusing definitions nor support for these nodes.
             */
            final QName qName = Utils.qNameFromArgument(current, name.getLocalName());
            def = new StatementDefinitionContext<>(new UnknownStatementImpl.Definition(
                new ModelDefinedStatementDefinition(qName)));
        }

        Preconditions.checkArgument(def != null, "Statement %s does not have type mapping defined.", name);
        if (current == null) {
            return createDeclaredRoot(def, ref);
        }
        return current.substatementBuilder(def, ref);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ContextBuilder<?, ?, ?> createDeclaredRoot(final StatementDefinitionContext<?, ?, ?> def,
                                                       final StatementSourceReference ref) {
        return new ContextBuilder(def, ref) {

            @Override
            public StatementContextBase build() {
                if (root == null) {
                    root = new RootStatementContext(this, SourceSpecificContext.this);
                } else {
                    Preconditions.checkState(root.getIdentifier().equals(createIdentifier()),
                            "Root statement was already defined as %s.", root.getIdentifier());
                }
                root.resetLists();
                return root;
            }

        };
    }

    RootStatementContext<?, ?, ?> getRoot() {
        return root;
    }

    DeclaredStatement<?> buildDeclared() {
        return root.buildDeclared();
    }

    EffectiveStatement<?, ?> buildEffective() {
        return root.buildEffective();
    }

    void startPhase(final ModelProcessingPhase phase) {
        @Nullable final ModelProcessingPhase previousPhase = phase.getPreviousPhase();
        Preconditions.checkState(Objects.equals(previousPhase, finishedPhase));
        Preconditions.checkState(modifiers.get(previousPhase).isEmpty());
        inProgressPhase = phase;
        LOG.debug("Source {} started phase {}", source, phase);
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> void addToLocalStorage(final Class<N> type, final K key,
           final V value) {
        if (ImportedNamespaceContext.class.isAssignableFrom(type)) {
            if (importedNamespaces.isEmpty()) {
                importedNamespaces = new ArrayList<>(1);
            }
            importedNamespaces.add((NamespaceStorageNode) value);
        }
        getRoot().addToLocalStorage(type, key, value);
    }

    @Override
    public StorageNodeType getStorageNodeType() {
        return StorageNodeType.SOURCE_LOCAL_SPECIAL;
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> V getFromLocalStorage(final Class<N> type, final K key) {
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

    @Nullable
    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromLocalStorage(final Class<N> type) {
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
    public <K, V, N extends IdentifierNamespace<K, V>> NamespaceBehaviour<K, V, N> getNamespaceBehaviour(
            final Class<N> type) {
        return currentContext.getNamespaceBehaviour(type);
    }

    @Override
    public NamespaceStorageNode getParentNamespaceStorage() {
        return currentContext;
    }

    PhaseCompletionProgress tryToCompletePhase(final ModelProcessingPhase phase) throws SourceException {
        final Collection<ModifierImpl> currentPhaseModifiers = modifiers.get(phase);

        boolean hasProgressed = tryToProgress(currentPhaseModifiers);

        Preconditions.checkNotNull(this.root, "Malformed source. Valid root element is missing.");
        final boolean phaseCompleted = root.tryToCompletePhase(phase);

        hasProgressed |= tryToProgress(currentPhaseModifiers);

        if (phaseCompleted && currentPhaseModifiers.isEmpty()) {
            finishedPhase = phase;
            LOG.debug("Source {} finished phase {}", source, phase);
            return PhaseCompletionProgress.FINISHED;

        }

        return hasProgressed ? PhaseCompletionProgress.PROGRESS : PhaseCompletionProgress.NO_PROGRESS;
    }

    private static boolean tryToProgress(final Collection<ModifierImpl> currentPhaseModifiers) {
        boolean hasProgressed = false;

        final Iterator<ModifierImpl> modifier = currentPhaseModifiers.iterator();
        while (modifier.hasNext()) {
            if (modifier.next().tryApply()) {
                modifier.remove();
                hasProgressed = true;
            }
        }

        return hasProgressed;
    }

    ModelActionBuilder newInferenceAction(final ModelProcessingPhase phase) {
        final ModifierImpl action = new ModifierImpl(phase);
        modifiers.put(phase, action);
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

        if (exceptions.isEmpty()) {
            return Optional.empty();
        }

        final String message = String.format("Yang model processing phase %s failed", identifier);
        final InferenceException e = new InferenceException(message, root.getStatementSourceReference(),
            exceptions.get(0));
        exceptions.listIterator(1).forEachRemaining(e::addSuppressed);

        return Optional.of(e);
    }

    void loadStatements() throws SourceException {
        LOG.trace("Source {} loading statements for phase {}", source, inProgressPhase);

        switch (inProgressPhase) {
            case SOURCE_PRE_LINKAGE:
                source.writePreLinkage(new StatementContextWriter(this, inProgressPhase), stmtDef());
                break;
            case SOURCE_LINKAGE:
                source.writeLinkage(new StatementContextWriter(this, inProgressPhase), stmtDef(), preLinkagePrefixes());
                break;
            case STATEMENT_DEFINITION:
                source.writeLinkageAndStatementDefinitions(new StatementContextWriter(this, inProgressPhase), stmtDef(), prefixes());
                break;
            case FULL_DECLARATION:
                source.writeFull(new StatementContextWriter(this, inProgressPhase), stmtDef(), prefixes());
                break;
            default:
                break;
        }
    }

    private static StatementDefinitionContext<?, ?, ?> resolveTypeBodyStmts(final String typeArgument) {
        final StatementSupport<?, ?, ?> support = BUILTIN_TYPE_SUPPORTS.get(typeArgument);
        return support == null ? null : new StatementDefinitionContext<>(support);
    }

    private PrefixToModule preLinkagePrefixes() {
        final PrefixToModuleMap preLinkagePrefixes = new PrefixToModuleMap(true);
        final Map<String, URI> prefixToNamespaceMap = getAllFromLocalStorage(ImpPrefixToNamespace.class);
        if (prefixToNamespaceMap == null) {
            //:FIXME if it is a submodule without any import, the map is null. Handle also submodules and includes...
            return null;
        }

        prefixToNamespaceMap.forEach((key, value) -> preLinkagePrefixes.put(key, QNameModule.create(value, null)));
        return preLinkagePrefixes;
    }

    private PrefixToModule prefixes() {
        final Map<String, ModuleIdentifier> allPrefixes = getRoot().getAllFromNamespace(ImpPrefixToModuleIdentifier
                .class);
        final Map<String, ModuleIdentifier> belongsToPrefixes = getRoot().getAllFromNamespace
                (BelongsToPrefixToModuleIdentifier.class);
        if (belongsToPrefixes != null) {
            allPrefixes.putAll(belongsToPrefixes);
        }

        allPrefixes.forEach((key, value) ->
            prefixToModuleMap.put(key, getRoot().getFromNamespace(ModuleIdentifierToModuleQName.class, value)));

        return prefixToModuleMap;
    }

    private static <K, V> void addSupports(final Builder<K, V> builder, @Nullable final Map<K, V> supports) {
        if (supports != null) {
            builder.putAll(supports);
        }
    }

    private QNameToStatementDefinition stmtDef() {
        // regular YANG statements and extension supports added
        qNameToStmtDefMap.putAll(currentContext.getSupportsForPhase(inProgressPhase).getDefinitions());

        // We need to any and all extension statements which have been declared in the context
        if (inProgressPhase == ModelProcessingPhase.FULL_DECLARATION) {
            // We maintain this map for createDeclaredChild(), which calls out to global context first,
            // hence there is no point in performing double lookups.
            final Builder<QName, StatementSupport<?, ?, ?>> b = ImmutableMap.builder();
            addSupports(b, getRoot().getAllFromLocalStorage(StatementDefinitionNamespace.class));
            for (NamespaceStorageNode namespace : importedNamespaces) {
                addSupports(b, namespace.getAllFromLocalStorage(StatementDefinitionNamespace.class));
            }

            definedExtensions = b.build();
            definedExtensions.forEach((qname, support) -> {
                final StatementSupport<?, ?, ?> existing = qNameToStmtDefMap.putIfAbsent(qname, support);
                if (existing != null) {
                    LOG.debug("Source {} already defines statement {} as {}", source, qname, existing);
                } else {
                    LOG.debug("Source {} defined statement {} as {}", source, qname, support);
                }
            });
        }

        return qNameToStmtDefMap;
    }
}
