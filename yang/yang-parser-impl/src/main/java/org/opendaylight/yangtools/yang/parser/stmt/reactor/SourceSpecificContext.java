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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.ExtensionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.ImportedNamespaceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModuleMap;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinitionMap;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase.ContextBuilder;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.UnknownStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

public class SourceSpecificContext implements NamespaceStorageNode, NamespaceBehaviour.Registry, Mutable {

    public enum PhaseCompletionProgress {
        NO_PROGRESS,
        PROGRESS,
        FINISHED
    }

    private final StatementStreamSource source;
    private final BuildGlobalContext currentContext;
    private final Collection<NamespaceStorageNode> importedNamespaces = new ArrayList<>();
    private final Multimap<ModelProcessingPhase, ModifierImpl> modifiers = HashMultimap.create();

    private RootStatementContext<?,?, ?> root;

    private ModelProcessingPhase inProgressPhase;
    private ModelProcessingPhase finishedPhase;
    private final QNameToStatementDefinitionMap qNameToStmtDefMap = new QNameToStatementDefinitionMap();
    private final PrefixToModuleMap prefixToModuleMap = new PrefixToModuleMap();


    SourceSpecificContext(final BuildGlobalContext currentContext,final StatementStreamSource source) {
        this.source = source;
        this.currentContext = currentContext;
    }

    StatementDefinitionContext<?,?,?> getDefinition(final QName name) {
        return currentContext.getStatementDefinition(name);
    }

    ContextBuilder<?, ?, ?> createDeclaredChild(final StatementContextBase<?, ?, ?> current, final QName name, final StatementSourceReference ref) {
        StatementDefinitionContext<?,?,?> def = getDefinition(name);

        //extensions
        if (def == null) {
            if (Utils.isValidStatementDefinition(prefixToModuleMap, qNameToStmtDefMap, name)) {
                def = new StatementDefinitionContext<>(new UnknownStatementImpl.Definition(qNameToStmtDefMap.get(Utils.trimPrefix(name))));
            }
        }

        Preconditions.checkArgument(def != null, "Statement %s does not have type mapping defined.", name);
        if(current == null) {
            return createDeclaredRoot(def,ref);
        }
        return current.substatementBuilder(def, ref);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private ContextBuilder<?,?, ?> createDeclaredRoot(final StatementDefinitionContext<?,?,?> def, final StatementSourceReference ref) {
        return new ContextBuilder(def,ref) {

            @Override
            public StatementContextBase build() throws SourceException {
                if(root == null) {
                    root = new RootStatementContext(this, SourceSpecificContext.this);
                } else {
                    Preconditions.checkState(root.getIdentifier().equals(getIdentifier()), "Root statement was already defined as %s.", root.getIdentifier());
                }
                root.resetLists();
                return root;
            }

        };
    }

    RootStatementContext<?,?,?> getRoot() {
        return root;
    }

    DeclaredStatement<?> buildDeclared() {
        return root.buildDeclared();
    }

    EffectiveStatement<?,?> buildEffective() {
        return root.buildEffective();
    }

    void startPhase(final ModelProcessingPhase phase) {
        @Nullable ModelProcessingPhase previousPhase = phase.getPreviousPhase();
        Preconditions.checkState(Objects.equals(previousPhase, finishedPhase));
        Preconditions.checkState(modifiers.get(previousPhase).isEmpty());
        inProgressPhase = phase;
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> void addToLocalStorage(final Class<N> type, final K key, final V value) {
        if(ImportedNamespaceContext.class.isAssignableFrom(type)) {
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
        if(potentialLocal != null) {
            return potentialLocal;
        }
        for(NamespaceStorageNode importedSource : importedNamespaces) {
            V potential = importedSource.getFromLocalStorage(type, key);
            if(potential != null) {
                return potential;
            }
        }
        return null;
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> NamespaceBehaviour<K, V, N> getNamespaceBehaviour(final Class<N> type) {
        return currentContext.getNamespaceBehaviour(type);
    }

    @Override
    public NamespaceStorageNode getParentNamespaceStorage() {
        return currentContext;
    }

    PhaseCompletionProgress tryToCompletePhase(final ModelProcessingPhase phase) throws SourceException {
        Collection<ModifierImpl> currentPhaseModifiers = modifiers.get(phase);

        boolean hasProgressed = hasProgress(currentPhaseModifiers);

        boolean phaseCompleted = root.tryToCompletePhase(phase);

        hasProgressed = (hasProgress(currentPhaseModifiers) | hasProgressed);

        if(phaseCompleted && (currentPhaseModifiers.isEmpty())) {
            finishedPhase = phase;
            return PhaseCompletionProgress.FINISHED;

        }
        if(hasProgressed) {
            return PhaseCompletionProgress.PROGRESS;
        }
        return PhaseCompletionProgress.NO_PROGRESS;
    }


    private static boolean hasProgress(final Collection<ModifierImpl> currentPhaseModifiers) {

        Iterator<ModifierImpl> modifier = currentPhaseModifiers.iterator();
        boolean hasProgressed = false;
        while(modifier.hasNext()) {
            if(modifier.next().isApplied()) {
                modifier.remove();
                hasProgressed = true;
            }
        }

        return hasProgressed;

    }

    ModelActionBuilder newInferenceAction(final ModelProcessingPhase phase) {
        ModifierImpl action = new ModifierImpl(phase);
        modifiers.put(phase, action);
        return action;
    }

    @Override
    public String toString() {
        return "SourceSpecificContext [source=" + source + ", current=" + inProgressPhase + ", finished="
                + finishedPhase + "]";
    }

    SourceException failModifiers(final ModelProcessingPhase identifier) {
        InferenceException sourceEx = new InferenceException("Fail to infer source relationships", root.getStatementSourceReference());


        for(ModifierImpl mod : modifiers.get(identifier)) {
            try {
                mod.failModifier();
            } catch (SourceException e) {
                sourceEx.addSuppressed(e);
            }
        }
        return sourceEx;
    }

    void loadStatements() throws SourceException {
        switch (inProgressPhase) {
            case SOURCE_LINKAGE:
            source.writeLinkage(new StatementContextWriter(this, inProgressPhase),stmtDef());
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

    private PrefixToModule prefixes() {
        Map<String, QNameModule> prefixes = (Map<String, QNameModule>) currentContext.getAllFromNamespace(PrefixToModule.class);
        for (Map.Entry<String, QNameModule> prefix : prefixes.entrySet()) {
            prefixToModuleMap.put(prefix.getKey(), prefix.getValue());
        }
        return prefixToModuleMap;
    }

    private QNameToStatementDefinition stmtDef() {
        //regular YANG statements added
        ImmutableMap<QName, StatementSupport<?, ?, ?>> definitions = currentContext.getSupportsForPhase(
                inProgressPhase).getDefinitions();
        for (Map.Entry<QName, StatementSupport<?,?,?>> entry : definitions.entrySet()) {
            qNameToStmtDefMap.put(entry.getKey(), entry.getValue());
        }

        //extensions added
        if (inProgressPhase.equals(ModelProcessingPhase.FULL_DECLARATION)) {
            Map<QName, SubstatementContext<?, ?, ?>> extensions = (Map<QName, SubstatementContext<?, ?, ?>>) currentContext.getAllFromNamespace(ExtensionNamespace.class);
            if (extensions != null) {
                for (Map.Entry<QName, SubstatementContext<?, ?, ?>> extension : extensions.entrySet()) {
                    qNameToStmtDefMap.put(new QName(YangConstants.RFC6020_YIN_NAMESPACE, extension.getKey().getLocalName()), (StatementDefinition) extension.getValue().definition().getFactory());
                }
            }
        }
        return qNameToStmtDefMap;
    }
}
