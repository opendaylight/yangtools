/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.spi.source.MaterializedSourceRepresentation;
import org.opendaylight.yangtools.yang.model.spi.source.SourceTransformer;
import org.opendaylight.yangtools.yang.parser.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.MutableStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceNotAvailableException;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceStorage.GlobalStorage;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundles;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundles.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.SourceLinkageResolver.ResolvedSourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class BuildGlobalContext extends AbstractNamespaceStorage implements GlobalStorage {
    private static final Logger LOG = LoggerFactory.getLogger(BuildGlobalContext.class);

    private static final ModelProcessingPhase[] PHASE_EXECUTION_ORDER = {
        ModelProcessingPhase.STATEMENT_DEFINITION,
        ModelProcessingPhase.FULL_DECLARATION,
        ModelProcessingPhase.EFFECTIVE_MODEL
    };

    private final Table<YangVersion, QName, StatementDefinitionContext<?, ?, ?>> definitions = HashBasedTable.create();
    private final HashMap<QName, StatementDefinitionContext<?, ?, ?>> modelDefinedStmtDefs = new HashMap<>();
    private final HashMap<ParserNamespace<?, ?>, BehaviourNamespaceAccess<?, ?>> supportedNamespaces = new HashMap<>();
    private final ArrayList<MutableStatement> mutableStatementsToSeal = new ArrayList<>();
    private final ImmutableMap<ModelProcessingPhase, StatementSupportBundle> supports;
    private final HashSet<BuildSource<?>> sources = new HashSet<>();
    private final Set<BuildSource<?>> libSources = new HashSet<>();
    private final ImmutableSet<YangVersion> supportedVersions;

    private ModelProcessingPhase currentPhase = ModelProcessingPhase.INIT;
    private ModelProcessingPhase finishedPhase = ModelProcessingPhase.INIT;

    BuildGlobalContext(final ImmutableMap<ModelProcessingPhase, StatementSupportBundle> supports,
            final ImmutableMap<ValidationBundleType, Collection<?>> supportedValidation) {
        this.supports = requireNonNull(supports, "BuildGlobalContext#supports cannot be null");

        final var access = accessNamespace(ValidationBundles.NAMESPACE);
        for (var validationBundle : supportedValidation.entrySet()) {
            access.valueTo(this, validationBundle.getKey(), validationBundle.getValue());
        }

        supportedVersions = verifyNotNull(supports.get(ModelProcessingPhase.INIT)).getSupportedVersions();
    }

    StatementSupportBundle getSupportsForPhase(final ModelProcessingPhase phase) {
        return supports.get(phase);
    }

    @NonNullByDefault
    <S extends SourceRepresentation & MaterializedSourceRepresentation<S, ?>> void addSource(final S source,
            final StatementStreamSource.Factory<S> streamFactory) throws IOException, SourceSyntaxException {
        final var buildSource = new BuildSource<>(this, source, streamFactory);
        // eagerly initialize required sources
        buildSource.getSourceContext();
        sources.add(buildSource);
    }

    @NonNullByDefault
    public <I extends SourceRepresentation, O extends SourceRepresentation & MaterializedSourceRepresentation<O, ?>>
            void addLibSource(final SourceTransformer<I, O> transformer, final I source,
                final StatementStreamSource.Factory<O> streamFactory) {
        libSources.add(new BuildSource<>(this, transformer, source, streamFactory));
    }

    @NonNullByDefault
    <S extends SourceRepresentation & MaterializedSourceRepresentation<S, ?>> void addLibSource(final S source,
            final StatementStreamSource.Factory<S> streamFactory) {
        checkState(currentPhase == ModelProcessingPhase.INIT,
                "Add library source is allowed in ModelProcessingPhase.INIT only");
        // library sources are lazily initialized
        libSources.add(new BuildSource<>(this, source, streamFactory));
    }

    void setSupportedFeatures(final FeatureSet supportedFeatures) {
        addToNamespace(ParserNamespaces.SUPPORTED_FEATURES, Empty.value(), requireNonNull(supportedFeatures));
    }

    void setModulesDeviatedByModules(final SetMultimap<QNameModule, QNameModule> modulesDeviatedByModules) {
        addToNamespace(ParserNamespaces.MODULES_DEVIATED_BY, Empty.value(),
            ImmutableSetMultimap.copyOf(modulesDeviatedByModules));
    }

    @Override
    <K, V> BehaviourNamespaceAccess<K, V> accessNamespace(final ParserNamespace<K, V> namespace) {
        @SuppressWarnings("unchecked")
        final var existing = (BehaviourNamespaceAccess<K, V>) supportedNamespaces.get(namespace);
        if (existing != null) {
            return existing;
        }

        final var behaviour = verifyNotNull(supports.get(currentPhase), "No support for phase %s", currentPhase)
            .namespaceBehaviourOf(namespace);
        if (behaviour == null) {
            throw new NamespaceNotAvailableException(
                "Namespace " + namespace + " is not available in phase " + currentPhase);
        }

        final var created = new BehaviourNamespaceAccess<>(this, behaviour);
        supportedNamespaces.put(namespace, created);
        return created;
    }

    StatementDefinitionContext<?, ?, ?> getStatementDefinition(final YangVersion version, final QName name) {
        var potential = definitions.get(version, name);
        if (potential == null) {
            final var potentialRaw = verifyNotNull(supports.get(currentPhase)).getStatementDefinition(version, name);
            if (potentialRaw != null) {
                potential = new StatementDefinitionContext<>(potentialRaw);
                definitions.put(version, name, potential);
            }
        }
        return potential;
    }

    StatementDefinitionContext<?, ?, ?> getModelDefinedStatementDefinition(final QName name) {
        return modelDefinedStmtDefs.get(name);
    }

    void putModelDefinedStatementDefinition(final QName name, final StatementDefinitionContext<?, ?, ?> def) {
        modelDefinedStmtDefs.put(name, def);
    }

    @NonNull ReactorDeclaredModel build() throws ReactorException, SourceSyntaxException, IOException {
        final List<ResolvedSourceContext> resolvedSourceContexts = resolveSources();
        executePhases(resolvedSourceContexts);
        return transform(resolvedSourceContexts);
    }

    @NonNull EffectiveSchemaContext buildEffective() throws ReactorException, SourceSyntaxException, IOException {
        final List<ResolvedSourceContext> resolvedSourceContexts = resolveSources();
        executePhases(resolvedSourceContexts);
        return transformEffective(resolvedSourceContexts);
    }

    private @NonNull Collection<SourceSpecificContext> initializeContexts(
            final @NonNull Collection<BuildSource<?>> sourcesToInit) throws SourceSyntaxException, IOException {
        final var contexts = new HashSet<SourceSpecificContext>();
        for (var buildSource : sourcesToInit) {
            contexts.add(buildSource.getSourceContext());
        }
        return contexts;
    }

    private List<ResolvedSourceContext> resolveSources() throws ReactorException, SourceSyntaxException, IOException {
        this.currentPhase = ModelProcessingPhase.STATEMENT_DEFINITION;

        final var mainContexts = initializeContexts(sources);
        final var libContexts = initializeContexts(libSources);

        for (var source : mainContexts) {
            source.startPhase(ModelProcessingPhase.STATEMENT_DEFINITION);
            source.loadRootStatement();
        }

        for (var libSource : libContexts) {
            libSource.startPhase(ModelProcessingPhase.STATEMENT_DEFINITION);
            libSource.loadRootStatement();
        }

        final var linkageResolver = new SourceLinkageResolver(mainContexts, libContexts);
        final List<ResolvedSourceContext> resolvedSourceContexts = linkageResolver.resolveInvolvedSources();

        for (ResolvedSourceContext resolvedContext : resolvedSourceContexts) {
            SourceLinkageResolver.fillNamespaces(resolvedContext.resolvedSourceInfo());
        }
        return resolvedSourceContexts;
    }

    private void executePhases(final @NonNull List<ResolvedSourceContext> resolvedSources) throws ReactorException {
        for (var phase : PHASE_EXECUTION_ORDER) {
            startPhase(phase, resolvedSources);
            loadPhaseStatements(resolvedSources);
            completePhaseActions(resolvedSources);
            endPhase(phase);
        }
    }

    private @NonNull ReactorDeclaredModel transform(final @NonNull List<ResolvedSourceContext> resolvedSources) {
        checkState(finishedPhase == ModelProcessingPhase.EFFECTIVE_MODEL);
        final var rootStatements = new ArrayList<DeclaredStatement<?>>(resolvedSources.size());
        for (var source : resolvedSources) {
            rootStatements.add(source.context().declaredRoot());
        }
        return new ReactorDeclaredModel(rootStatements);
    }

    private @NonNull SomeModifiersUnresolvedException propagateException(final SourceSpecificContext source,
            final RuntimeException cause) throws SomeModifiersUnresolvedException {
        final var sourceId = source.identifySource();
        if (!(cause instanceof SourceException)) {
            /*
             * This should not be happening as all our processing should provide SourceExceptions.
             * We will wrap the exception to provide enough information to identify the problematic model,
             * but also emit a warning so the offending codepath will get fixed.
             */
            LOG.warn("Unexpected error processing source {}. Please file an issue with this model attached.",
                sourceId, cause);
        }

        throw new SomeModifiersUnresolvedException(currentPhase, sourceId, cause);
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private @NonNull EffectiveSchemaContext transformEffective(
            final @NonNull List<ResolvedSourceContext> resolvedSources) throws ReactorException {
        checkState(finishedPhase == ModelProcessingPhase.EFFECTIVE_MODEL);
        final var rootStatements = new ArrayList<DeclaredStatement<?>>(resolvedSources.size());
        final var rootEffectiveStatements = new ArrayList<EffectiveStatement<?, ?>>(resolvedSources.size());

        for (var source : resolvedSources) {
            final var context = source.context();
            try {
                rootStatements.add(context.declaredRoot());
                rootEffectiveStatements.add(context.effectiveRoot());
            } catch (RuntimeException e) {
                throw propagateException(context, e);
            }
        }

        sealMutableStatements();
        return EffectiveSchemaContext.create(rootStatements, rootEffectiveStatements);
    }

    private void startPhase(final ModelProcessingPhase phase, final @NonNull List<ResolvedSourceContext> resolved) {
        checkState(Objects.equals(finishedPhase, phase.getPreviousPhase()));
        startPhaseFor(phase, resolved);

        currentPhase = phase;
        LOG.debug("Global phase {} started", phase);
    }

    private static void startPhaseFor(final ModelProcessingPhase phase,
            final @NonNull List<ResolvedSourceContext> sources) {
        for (var source : sources) {
            source.context().startPhase(phase);
        }
    }

    private void loadPhaseStatements(final @NonNull List<ResolvedSourceContext> resolved) throws ReactorException {
        checkState(currentPhase != null);
        loadPhaseStatementsFor(resolved);
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private void loadPhaseStatementsFor(final @NonNull List<ResolvedSourceContext> srcs) throws ReactorException {
        for (var source : srcs) {
            try {
                source.context().loadStatements();
            } catch (RuntimeException e) {
                throw propagateException(source.context(), e);
            }
        }
    }

    private SomeModifiersUnresolvedException addSourceExceptions(
            final @NonNull List<SourceSpecificContext> sourcesToProgress) {
        boolean addedCause = false;
        SomeModifiersUnresolvedException buildFailure = null;
        for (var failedSource : sourcesToProgress) {
            final var optSourceEx = failedSource.failModifiers(currentPhase);
            if (optSourceEx.isEmpty()) {
                continue;
            }

            final var sourceEx = optSourceEx.orElseThrow();
            // Workaround for broken logging implementations which ignore
            // suppressed exceptions
            final var cause = sourceEx.getCause() != null ? sourceEx.getCause() : sourceEx;
            if (LOG.isDebugEnabled()) {
                LOG.error("Failed to parse YANG from source {}", failedSource, sourceEx);
            } else {
                LOG.error("Failed to parse YANG from source {}: {}", failedSource, cause.getMessage());
            }

            final var suppressed = sourceEx.getSuppressed();
            if (suppressed.length > 0) {
                LOG.error("{} additional errors reported:", suppressed.length);

                int count = 1;
                for (var supp : suppressed) {
                    LOG.error("Error {}: {}", count, supp.getMessage());
                    count++;
                }
            }

            if (!addedCause) {
                addedCause = true;
                final var sourceId = failedSource.identifySource();
                buildFailure = new SomeModifiersUnresolvedException(currentPhase, sourceId, sourceEx);
            } else {
                buildFailure.addSuppressed(sourceEx);
            }
        }
        return buildFailure;
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private void completePhaseActions(final @NonNull List<ResolvedSourceContext> resolved) throws ReactorException {
        checkState(currentPhase != null);
        final var sourcesToProgress = new ArrayList<>(resolved.stream().map(ResolvedSourceContext::context).toList());

        boolean progressing = true;
        while (progressing) {
            // We reset progressing to false.
            progressing = false;
            final var currentSource = sourcesToProgress.iterator();
            while (currentSource.hasNext()) {
                final var nextSourceCtx = currentSource.next();
                try {
                    final var sourceProgress = nextSourceCtx.tryToCompletePhase(currentPhase.executionOrder());
                    switch (sourceProgress) {
                        case null -> throw new NullPointerException();
                        case FINISHED -> {
                            currentSource.remove();
                            // we were able to make progress in computation
                            progressing = true;
                        }
                        case PROGRESS -> progressing = true;
                        case NO_PROGRESS -> {
                            // Noop
                        }
                    }
                } catch (RuntimeException e) {
                    throw propagateException(nextSourceCtx, e);
                }
            }
        }

        if (!sourcesToProgress.isEmpty()) {
            final var buildFailure = addSourceExceptions(sourcesToProgress);
            if (buildFailure != null) {
                throw buildFailure;
            }
        }
    }

    private void endPhase(final ModelProcessingPhase phase) {
        checkState(currentPhase == phase);
        finishedPhase = currentPhase;
        LOG.debug("Global phase {} finished", phase);
    }

    void addMutableStmtToSeal(final MutableStatement mutableStatement) {
        mutableStatementsToSeal.add(mutableStatement);
    }

    void sealMutableStatements() {
        for (final MutableStatement mutableStatement : mutableStatementsToSeal) {
            mutableStatement.seal();
        }
        mutableStatementsToSeal.clear();
    }
}
