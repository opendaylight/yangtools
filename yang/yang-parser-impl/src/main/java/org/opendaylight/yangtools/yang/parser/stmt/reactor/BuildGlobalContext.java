/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace;

import java.util.Map.Entry;
import java.util.Collection;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceNotAvailableException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.SourceSpecificContext.PhaseCompletionProgress;

class BuildGlobalContext extends NamespaceStorageSupport implements NamespaceBehaviour.Registry {

    private static final List<ModelProcessingPhase> PHASE_EXECUTION_ORDER = ImmutableList.<ModelProcessingPhase>builder()
            .add(ModelProcessingPhase.SOURCE_LINKAGE)
            .add(ModelProcessingPhase.STATEMENT_DEFINITION)
            .add(ModelProcessingPhase.FULL_DECLARATION)
            .add(ModelProcessingPhase.EFFECTIVE_MODEL)
            .build();

    private final Map<QName,StatementDefinitionContext<?,?,?>> definitions = new HashMap<>();
    private final Map<Class<?>,NamespaceBehaviourWithListeners<?, ?, ?>> supportedNamespaces = new HashMap<>();


    private final Map<ModelProcessingPhase,StatementSupportBundle> supports;
    private final Set<SourceSpecificContext> sources = new HashSet<>();

    private ModelProcessingPhase currentPhase = ModelProcessingPhase.INIT;
    private ModelProcessingPhase finishedPhase = ModelProcessingPhase.INIT;

    public BuildGlobalContext(Map<ModelProcessingPhase, StatementSupportBundle> supports) {
        super();
        this.supports = supports;
    }

    public BuildGlobalContext(Map<ModelProcessingPhase, StatementSupportBundle> supports,  Map<ValidationBundleType,Collection<?>> supportedValidation) {
        super();
        this.supports = supports;

        Set<Entry<ValidationBundleType, Collection<?>>> validationBundles = supportedValidation.entrySet();
        for (Entry<ValidationBundleType, Collection<?>> validationBundle : validationBundles) {
            addToNs(ValidationBundlesNamespace.class, validationBundle.getKey(), validationBundle.getValue());
        }
    }

    public StatementSupportBundle getSupportsForPhase(ModelProcessingPhase currentPhase) {
        return supports.get(currentPhase);
    }

    public void addSource(@Nonnull StatementStreamSource source) {
        sources.add(new SourceSpecificContext(this,source));
    }

    @Override
    public StorageNodeType getStorageNodeType() {
        return StorageNodeType.GLOBAL;
    }

    @Override
    public NamespaceStorageNode getParentNamespaceStorage() {
        return null;
    }

    @Override
    public NamespaceBehaviour.Registry getBehaviourRegistry() {
        return this;
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> NamespaceBehaviourWithListeners<K, V, N> getNamespaceBehaviour(Class<N> type) {
        NamespaceBehaviourWithListeners<?, ?, ?> potential = supportedNamespaces.get(type);
        if (potential == null) {
            NamespaceBehaviour<K, V, N> potentialRaw = supports.get(currentPhase).getNamespaceBehaviour(type);
            if(potentialRaw != null) {
                potential = new NamespaceBehaviourWithListeners<>(potentialRaw);
                supportedNamespaces.put(type, potential);
            }
        }
        if (potential != null) {
            Preconditions.checkState(type.equals(potential.getIdentifier()));

            /*
             * Safe cast, previous checkState checks equivalence of key from
             * which type argument are derived
             */
            return (NamespaceBehaviourWithListeners<K, V, N>) potential;
        }
        throw new NamespaceNotAvailableException("Namespace " + type + " is not available in phase " + currentPhase);
    }

    public StatementDefinitionContext<?, ?, ?> getStatementDefinition(QName name) {
        StatementDefinitionContext<?, ?, ?> potential = definitions.get(name);
        if(potential == null) {
            StatementSupport<?, ?, ?> potentialRaw = supports.get(currentPhase).getStatementDefinition(name);
            if(potentialRaw != null) {
                potential = new StatementDefinitionContext<>(potentialRaw);
                definitions.put(name, potential);
            }
        }
        return potential;
    }

    public EffectiveModelContext build() throws SourceException, ReactorException {
        for(ModelProcessingPhase phase : PHASE_EXECUTION_ORDER) {
            startPhase(phase);
            loadPhaseStatements();
            completePhaseActions();
            endPhase(phase);
        }
        return transform();
    }

    private EffectiveModelContext transform() {
        Preconditions.checkState(finishedPhase == ModelProcessingPhase.EFFECTIVE_MODEL);
        List<DeclaredStatement<?>> rootStatements = new ArrayList<>();
        for(SourceSpecificContext source : sources) {
            DeclaredStatement<?> root = source.getRoot().buildDeclared();
            rootStatements.add(root);
        }
        return new EffectiveModelContext(rootStatements);
    }

    public EffectiveSchemaContext buildEffective() throws SourceException, ReactorException {
        for(ModelProcessingPhase phase : PHASE_EXECUTION_ORDER) {
            startPhase(phase);
            loadPhaseStatements();
            completePhaseActions();
            endPhase(phase);
        }
        return transformEffective();
    }

    private EffectiveSchemaContext transformEffective() {
        Preconditions.checkState(finishedPhase == ModelProcessingPhase.EFFECTIVE_MODEL);
        List<DeclaredStatement<?>> rootStatements = new ArrayList<>();
        List<EffectiveStatement<?,?>> rootEffectiveStatements = new ArrayList<>();

        for(SourceSpecificContext source : sources) {
            DeclaredStatement<?> root = source.getRoot().buildDeclared();
            rootStatements.add(root);

            EffectiveStatement<?,?> rootEffective = source.getRoot().buildEffective();
            rootEffectiveStatements.add(rootEffective);
        }

        return new EffectiveSchemaContext(rootStatements,rootEffectiveStatements);
    }

    private void startPhase(ModelProcessingPhase phase) {
        Preconditions.checkState(Objects.equals(finishedPhase, phase.getPreviousPhase()));
        for(SourceSpecificContext source : sources) {
            source.startPhase(phase);
        }
        currentPhase = phase;
    }

    private  void loadPhaseStatements() throws SourceException {
        Preconditions.checkState(currentPhase != null);
        for(SourceSpecificContext source : sources) {
            source.loadStatements();
        }
    }

    private  void completePhaseActions() throws ReactorException {
        Preconditions.checkState(currentPhase != null);
        List<SourceSpecificContext> sourcesToProgress = Lists.newArrayList(sources);
        try {
            boolean progressing = true;
            while(progressing) {
                // We reset progressing to false.
                progressing = false;
                Iterator<SourceSpecificContext> currentSource = sourcesToProgress.iterator();
                while(currentSource.hasNext()) {
                    SourceSpecificContext nextSourceCtx = currentSource.next();
                    PhaseCompletionProgress sourceProgress = nextSourceCtx.tryToCompletePhase(currentPhase);
                    switch (sourceProgress) {
                        case FINISHED:
                            currentSource.remove();
                            // Fallback to progress, since we were able to make progress in computation
                        case PROGRESS:
                            progressing = true;
                            break;
                        case NO_PROGRESS:
                            // Noop
                            break;
                        default:
                           throw new IllegalStateException("Unsupported phase progress " + sourceProgress);
                    }
                }
            }
        } catch (SourceException e) {
            throw Throwables.propagate(e);
        }
        if(!sourcesToProgress.isEmpty()) {
            SomeModifiersUnresolvedException buildFailure = new SomeModifiersUnresolvedException(currentPhase);
                for(SourceSpecificContext failedSource : sourcesToProgress) {
                    SourceException sourceEx = failedSource.failModifiers(currentPhase);
                    buildFailure.addSuppressed(sourceEx);
                }
                throw buildFailure;
        }
    }

    private  void endPhase(ModelProcessingPhase phase) {
        Preconditions.checkState(currentPhase == phase);
        finishedPhase = currentPhase;
    }

    public Set<SourceSpecificContext> getSources() {
        return sources;
    }

}
