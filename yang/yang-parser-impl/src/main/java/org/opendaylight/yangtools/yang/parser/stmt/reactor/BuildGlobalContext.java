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
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.DerivedNamespaceBehaviour;
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
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.SourceSpecificContext.PhaseCompletionProgress;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.RecursiveObjectLeaker;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BuildGlobalContext extends NamespaceStorageSupport implements NamespaceBehaviour.Registry {
    private static final Logger LOG = LoggerFactory.getLogger(BuildGlobalContext.class);

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

    public BuildGlobalContext(final Map<ModelProcessingPhase, StatementSupportBundle> supports) {
        super();
        this.supports = Preconditions.checkNotNull(supports, "BuildGlobalContext#supports cannot be null");
    }

    public BuildGlobalContext(final Map<ModelProcessingPhase, StatementSupportBundle> supports, final Map<ValidationBundleType,Collection<?>> supportedValidation) {
        super();
        this.supports = Preconditions.checkNotNull(supports, "BuildGlobalContext#supports cannot be null");

        for (Entry<ValidationBundleType, Collection<?>> validationBundle : supportedValidation.entrySet()) {
            addToNs(ValidationBundlesNamespace.class, validationBundle.getKey(), validationBundle.getValue());
        }
    }

    public StatementSupportBundle getSupportsForPhase(final ModelProcessingPhase currentPhase) {
        return supports.get(currentPhase);
    }

    public void addSource(@Nonnull final StatementStreamSource source) {
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
    public <K, V, N extends IdentifierNamespace<K, V>> NamespaceBehaviourWithListeners<K, V, N> getNamespaceBehaviour(final Class<N> type) {
        NamespaceBehaviourWithListeners<?, ?, ?> potential = supportedNamespaces.get(type);
        if (potential == null) {
            NamespaceBehaviour<K, V, N> potentialRaw = supports.get(currentPhase).getNamespaceBehaviour(type);
            if (potentialRaw != null) {
                potential = createNamespaceContext(potentialRaw);
                supportedNamespaces.put(type, potential);
            } else {
                throw new NamespaceNotAvailableException(
                        "Namespace " + type + " is not available in phase " + currentPhase);
            }
        }

        Verify.verify(type.equals(potential.getIdentifier()));
        /*
         * Safe cast, previous checkState checks equivalence of key from which type argument are
         * derived
         */
        return (NamespaceBehaviourWithListeners<K, V, N>) potential;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <K, V, N extends IdentifierNamespace<K, V>> NamespaceBehaviourWithListeners<K, V, N> createNamespaceContext(
            final NamespaceBehaviour<K, V, N> potentialRaw) {
        if (potentialRaw instanceof DerivedNamespaceBehaviour) {
            VirtualNamespaceContext derivedContext =
                    new VirtualNamespaceContext((DerivedNamespaceBehaviour) potentialRaw);
            getNamespaceBehaviour(((DerivedNamespaceBehaviour) potentialRaw).getDerivedFrom())
                    .addDerivedNamespace(derivedContext);
            return derivedContext;
        }
        return new SimpleNamespaceContext<>(potentialRaw);
    }

    public StatementDefinitionContext<?, ?, ?> getStatementDefinition(final QName name) {
        StatementDefinitionContext<?, ?, ?> potential = definitions.get(name);
        if (potential == null) {
            StatementSupport<?, ?, ?> potentialRaw = supports.get(currentPhase).getStatementDefinition(name);
            if (potentialRaw != null) {
                potential = new StatementDefinitionContext<>(potentialRaw);
                definitions.put(name, potential);
            }
        }
        return potential;
    }

    public EffectiveModelContext build() throws SourceException, ReactorException {
        for (ModelProcessingPhase phase : PHASE_EXECUTION_ORDER) {
            startPhase(phase);
            loadPhaseStatements();
            completePhaseActions();
            endPhase(phase);
        }
        return transform();
    }

    private EffectiveModelContext transform() {
        Preconditions.checkState(finishedPhase == ModelProcessingPhase.EFFECTIVE_MODEL);
        List<DeclaredStatement<?>> rootStatements = new ArrayList<>(sources.size());
        for (SourceSpecificContext source : sources) {
            rootStatements.add(source.getRoot().buildDeclared());
        }
        return new EffectiveModelContext(rootStatements);
    }

    public EffectiveSchemaContext buildEffective() throws SourceException, ReactorException {
        for (ModelProcessingPhase phase : PHASE_EXECUTION_ORDER) {
            startPhase(phase);
            loadPhaseStatements();
            completePhaseActions();
            endPhase(phase);
        }
        return transformEffective();
    }

    private EffectiveSchemaContext transformEffective() {
        Preconditions.checkState(finishedPhase == ModelProcessingPhase.EFFECTIVE_MODEL);
        List<DeclaredStatement<?>> rootStatements = new ArrayList<>(sources.size());
        List<EffectiveStatement<?,?>> rootEffectiveStatements = new ArrayList<>(sources.size());

        try {
            for (SourceSpecificContext source : sources) {
                final RootStatementContext<?, ?, ?> root = source.getRoot();
                rootStatements.add(root.buildDeclared());
                rootEffectiveStatements.add(root.buildEffective());
            }
        } finally {
            RecursiveObjectLeaker.cleanup();
        }

        return new EffectiveSchemaContext(rootStatements, rootEffectiveStatements);
    }

    private void startPhase(final ModelProcessingPhase phase) {
        Preconditions.checkState(Objects.equals(finishedPhase, phase.getPreviousPhase()));
        for (SourceSpecificContext source : sources) {
            source.startPhase(phase);
        }
        currentPhase = phase;
    }

    private  void loadPhaseStatements() throws SourceException {
        Preconditions.checkState(currentPhase != null);
        for (SourceSpecificContext source : sources) {
            source.loadStatements();
        }
    }

    private SomeModifiersUnresolvedException addSourceExceptions(final SomeModifiersUnresolvedException buildFailure,
            final List<SourceSpecificContext> sourcesToProgress) {
        boolean addedCause = false;
        for (SourceSpecificContext failedSource : sourcesToProgress) {
            final SourceException sourceEx = failedSource.failModifiers(currentPhase);

            // Workaround for broken logging implementations which ignore suppressed exceptions
            Throwable cause = sourceEx.getCause() != null ? sourceEx.getCause() : sourceEx;
            if (LOG.isDebugEnabled()) {
                LOG.error("Failed to parse YANG from source {}", failedSource, sourceEx);
            } else {
                LOG.error("Failed to parse YANG from source {}: {}", failedSource, cause.getMessage());
            }

            final Throwable[] suppressed = sourceEx.getSuppressed();
            if (suppressed.length > 0) {
                LOG.error("{} additional errors reported:", suppressed.length);

                int i = 1;
                for (Throwable t : suppressed) {
                    // FIXME: this should be configured in the appender, really
                    if (LOG.isDebugEnabled()) {
                        LOG.error("Error {}: {}", i, t.getMessage(), t);
                    } else {
                        LOG.error("Error {}: {}", i, t.getMessage());
                    }

                    i++;
                }
            }

            if (!addedCause) {
                addedCause = true;
                buildFailure.initCause(sourceEx);
            } else {
                buildFailure.addSuppressed(sourceEx);
            }
        }
        return buildFailure;
    }

    private  void completePhaseActions() throws ReactorException {
        Preconditions.checkState(currentPhase != null);
        List<SourceSpecificContext> sourcesToProgress = Lists.newArrayList(sources);
        try {
            boolean progressing = true;
            while (progressing) {
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
        if (!sourcesToProgress.isEmpty()) {
            SomeModifiersUnresolvedException buildFailure = new SomeModifiersUnresolvedException(currentPhase);
            buildFailure = addSourceExceptions(buildFailure, sourcesToProgress);
            throw buildFailure;
        }
    }

    private  void endPhase(final ModelProcessingPhase phase) {
        Preconditions.checkState(currentPhase == phase);
        finishedPhase = currentPhase;
    }

    public Set<SourceSpecificContext> getSources() {
        return sources;
    }
}
