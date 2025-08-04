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

import com.google.common.base.Verify;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.ir.IRArgument;
import org.opendaylight.yangtools.yang.ir.IRKeyword;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.StatementDeclarations;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.ExtendedSourceInfo;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.MutableStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceNotAvailableException;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceStorage.GlobalStorage;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinitionMap;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundles;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundles.ValidationBundleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class BuildGlobalContext extends AbstractNamespaceStorage implements GlobalStorage {
    private static final Logger LOG = LoggerFactory.getLogger(BuildGlobalContext.class);

    private static final ModelProcessingPhase[] PHASE_EXECUTION_ORDER = {
//        ModelProcessingPhase.SOURCE_PRE_LINKAGE,
//        ModelProcessingPhase.SOURCE_LINKAGE,
        ModelProcessingPhase.STATEMENT_DEFINITION,
        ModelProcessingPhase.FULL_DECLARATION,
        ModelProcessingPhase.EFFECTIVE_MODEL
    };

    private final Table<YangVersion, QName, StatementDefinitionContext<?, ?, ?>> definitions = HashBasedTable.create();
    private final Map<QName, StatementDefinitionContext<?, ?, ?>> modelDefinedStmtDefs = new HashMap<>();
    private final Map<ParserNamespace<?, ?>, BehaviourNamespaceAccess<?, ?>> supportedNamespaces = new HashMap<>();
    private final List<MutableStatement> mutableStatementsToSeal = new ArrayList<>();
    private final ImmutableMap<ModelProcessingPhase, StatementSupportBundle> supports;
    private final Set<SourceSpecificContext> sources = new HashSet<>();
    private final ImmutableSet<YangVersion> supportedVersions;

    private Set<SourceSpecificContext> libSources = new HashSet<>();
    private ModelProcessingPhase currentPhase = ModelProcessingPhase.INIT;
    private ModelProcessingPhase finishedPhase = ModelProcessingPhase.INIT;

    BuildGlobalContext(final ImmutableMap<ModelProcessingPhase, StatementSupportBundle> supports,
            final ImmutableMap<ValidationBundleType, Collection<?>> supportedValidation) {
        this.supports = requireNonNull(supports, "BuildGlobalContext#supports cannot be null");

        final var access = accessNamespace(ValidationBundles.NAMESPACE);
        for (var validationBundle : supportedValidation.entrySet()) {
            access.valueTo(this, validationBundle.getKey(), validationBundle.getValue());
        }

        supportedVersions = ImmutableSet.copyOf(
            verifyNotNull(supports.get(ModelProcessingPhase.INIT)).getSupportedVersions());
    }

    StatementSupportBundle getSupportsForPhase(final ModelProcessingPhase phase) {
        return supports.get(phase);
    }

    void addSource(final StatementStreamSource source) {
        sources.add(new SourceSpecificContext(this, source));
    }

    void addLibSource(final StatementStreamSource libSource) {
        checkState(currentPhase == ModelProcessingPhase.INIT,
                "Add library source is allowed in ModelProcessingPhase.INIT only");
        libSources.add(new SourceSpecificContext(this, libSource));
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

    @NonNull ReactorDeclaredModel build() throws ReactorException {
        executePhases();
        return transform();
    }

    @NonNull EffectiveSchemaContext buildEffective() throws ReactorException {
        executePhases();
        return transformEffective();
    }

    private void executePhases() throws ReactorException {
        // select only the sources and libs which should be involved in the next steps. Ignore libs which were
        // not referenced by anyone
        final Map<Unqualified, InvolvedSource> involvedSources = resolveInvolvedSources();

        // populate their PRE-LINKAGE and LINKAGE namespaces so that we can jump into STATEMENT-DEFINITION phase
        // seamlessly
        linkInvolvedSources(involvedSources);

        for (var phase : PHASE_EXECUTION_ORDER) {
            startPhase(phase);
            loadPhaseStatements();
            completePhaseActions();
            endPhase(phase);
        }
    }

    record InvolvedSource(
            SourceSpecificContext sourceContext,
            ExtendedSourceInfo sourceInfo) {
        public InvolvedSource {
            requireNonNull(sourceContext);
            requireNonNull(sourceInfo);
        }
    }

    private void linkInvolvedSources(final Map<Unqualified, InvolvedSource> involvedSources) {
        QNameToStatementDefinitionMap stmtDefs = new QNameToStatementDefinitionMap();
        stmtDefs.putAll(getSupportsForPhase(ModelProcessingPhase.SOURCE_LINKAGE).getCommonDefinitions());
        stmtDefs.putAll(getSupportsForPhase(ModelProcessingPhase.SOURCE_PRE_LINKAGE).getCommonDefinitions());

        for (Map.Entry<Unqualified, InvolvedSource> involvedSource : involvedSources.entrySet()) {
            stmtDefs.putAll(getSupportsForPhase(ModelProcessingPhase.SOURCE_LINKAGE)
                .getDefinitionsSpecificForVersion(involvedSource.getValue().sourceInfo.getSourceInfo().yangVersion()));
            stmtDefs.putAll(getSupportsForPhase(ModelProcessingPhase.SOURCE_PRE_LINKAGE)
                .getDefinitionsSpecificForVersion(involvedSource.getValue().sourceInfo.getSourceInfo().yangVersion()));
            involvedSource.getValue().sourceContext.startPhase(ModelProcessingPhase.SOURCE_PRE_LINKAGE);
            StmtContext.Mutable<Unqualified, ?, ?> rootStatement =
                    createRootStatement(involvedSource.getValue(), stmtDefs);

            //TODO: fill the pre-linkage and linkage namespaces of each root statement
        }
    }

    private StmtContext.Mutable<Unqualified, ?, ?> createRootStatement(final InvolvedSource source,
            QNameToStatementDefinitionMap qnameToStmtDefMap) {
        final YangIRSource irSource = source.sourceInfo.getIRSource();
        final IRArgument argumentCtx = irSource.statement().argument();
        //TODO: is ArgumentContextUtils necessary here? utils.stringFromStringContext(argumentCtx, ref);
        final String argument = argumentCtx == null ? null : ((IRArgument.Identifier) argumentCtx).string();
        final IRKeyword keyword = irSource.statement().keyword();
        final var ref = StatementDeclarations.inText(irSource.symbolicName(), irSource.statement().startLine(),
                irSource.statement().startColumn() + 1);
        final StatementDefinition moduleDef = qnameToStmtDefMap.get(QName.unsafeOf(
                YangConstants.RFC6020_YIN_MODULE, keyword.identifier()));
        QName moduleQname = moduleDef != null ? moduleDef.getStatementName() : null;
        //TODO: try assembling the components of the sourceContext.createDeclaredChild without the YangIRSource
        //TODO: add processing of Submodule as well.
        final var moduleStatement = (StmtContext.Mutable<Unqualified, ModuleStatement, ModuleEffectiveStatement>)
                source.sourceContext.createDeclaredChild(null, 0, moduleQname, argument.toString(), ref);
        return moduleStatement;
    }

    private Map<Unqualified, InvolvedSource> resolveInvolvedSources() {
        //TODO: should probably map by SourceIdentifier, not just Qname..
        final Map<Unqualified, InvolvedSource> involvedSources = new HashMap<>();
        final Map<Unqualified, SourceSpecificContext> namedLibSources = new HashMap<>();
        final Deque<Map.Entry<Unqualified, SourceSpecificContext>> importedToResolve = new ArrayDeque<>();

        for (SourceSpecificContext libSource : libSources) {
            namedLibSources.put(libSource.getInternalSourceId().name(), libSource);
        }

        for (SourceSpecificContext source : sources) {
            final ExtendedSourceInfo sourceInfo = source.getSourceInfo();
            involvedSources.put(sourceInfo.getSourceInfo().sourceId().name(), new InvolvedSource(source, sourceInfo));
            for (SourceDependency.Import anImport : sourceInfo.getSourceInfo().imports()) {
                final Unqualified importName = anImport.name();
                if (!involvedSources.containsKey(importName)) {
                    final SourceSpecificContext importedLibSource = namedLibSources.get(importName);
                    Verify.verifyNotNull(importedLibSource, "Imported source [%s] not found", importName.toString());
                    importedToResolve.add(Map.entry(importName, importedLibSource));
                }
            }
        }

        while (!importedToResolve.isEmpty()) {
            final Map.Entry<Unqualified, SourceSpecificContext> toResolve = importedToResolve.removeFirst();
            final ExtendedSourceInfo sourceInfo = toResolve.getValue().getSourceInfo();
            involvedSources.put(toResolve.getKey(), new InvolvedSource(toResolve.getValue(), sourceInfo));
            for (SourceDependency.Import anImport : sourceInfo.getSourceInfo().imports()) {
                final Unqualified importName = anImport.name();
                if (!involvedSources.containsKey(importName)) {
                    final SourceSpecificContext importedLibSource = namedLibSources.get(importName);
                    Verify.verifyNotNull(importedLibSource, "Imported source [%s] not found", importName.toString());
                    importedToResolve.add(Map.entry(importName, importedLibSource));
                }
            }
        }
        return involvedSources;
    }

    private @NonNull ReactorDeclaredModel transform() {
        checkState(finishedPhase == ModelProcessingPhase.EFFECTIVE_MODEL);
        final var rootStatements = new ArrayList<DeclaredStatement<?>>(sources.size());
        for (var source : sources) {
            rootStatements.add(source.declaredRoot());
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
    private @NonNull EffectiveSchemaContext transformEffective() throws ReactorException {
        checkState(finishedPhase == ModelProcessingPhase.EFFECTIVE_MODEL);
        final var rootStatements = new ArrayList<DeclaredStatement<?>>(sources.size());
        final var rootEffectiveStatements = new ArrayList<EffectiveStatement<?, ?>>(sources.size());

        for (var source : sources) {
            try {
                rootStatements.add(source.declaredRoot());
                rootEffectiveStatements.add(source.effectiveRoot());
            } catch (RuntimeException e) {
                throw propagateException(source, e);
            }
        }

        sealMutableStatements();
        return EffectiveSchemaContext.create(rootStatements, rootEffectiveStatements);
    }

    private void startPhase(final ModelProcessingPhase phase) {
        checkState(Objects.equals(finishedPhase, phase.getPreviousPhase()));
        startPhaseFor(phase, sources);
        startPhaseFor(phase, libSources);

        currentPhase = phase;
        LOG.debug("Global phase {} started", phase);
    }

    private static void startPhaseFor(final ModelProcessingPhase phase, final Set<SourceSpecificContext> sources) {
        for (var source : sources) {
            source.startPhase(phase);
        }
    }

    private void loadPhaseStatements() throws ReactorException {
        checkState(currentPhase != null);
        loadPhaseStatementsFor(sources);
        loadPhaseStatementsFor(libSources);
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private void loadPhaseStatementsFor(final Set<SourceSpecificContext> srcs) throws ReactorException {
        for (var source : srcs) {
            try {
                source.loadStatements();
            } catch (RuntimeException e) {
                throw propagateException(source, e);
            }
        }
    }

    private SomeModifiersUnresolvedException addSourceExceptions(final List<SourceSpecificContext> sourcesToProgress) {
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
    private void completePhaseActions() throws ReactorException {
        checkState(currentPhase != null);
        final var sourcesToProgress = new ArrayList<>(sources);
        if (!libSources.isEmpty()) {
            checkState(currentPhase == ModelProcessingPhase.SOURCE_PRE_LINKAGE,
                    "Yang library sources should be empty after ModelProcessingPhase.SOURCE_PRE_LINKAGE, "
                            + "but current phase was %s", currentPhase);
            sourcesToProgress.addAll(libSources);
        }

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

        if (!libSources.isEmpty()) {
            final var requiredLibs = getRequiredSourcesFromLib();
            sources.addAll(requiredLibs);
            libSources = ImmutableSet.of();
            /*
             * We want to report errors of relevant sources only, so any others can
             * be removed.
             */
            sourcesToProgress.retainAll(sources);
        }

        if (!sourcesToProgress.isEmpty()) {
            final var buildFailure = addSourceExceptions(sourcesToProgress);
            if (buildFailure != null) {
                throw buildFailure;
            }
        }
    }

    private Set<SourceSpecificContext> getRequiredSourcesFromLib() {
        checkState(currentPhase == ModelProcessingPhase.SOURCE_PRE_LINKAGE,
                "Required library sources can be collected only in ModelProcessingPhase.SOURCE_PRE_LINKAGE phase,"
                        + " but current phase was %s", currentPhase);
        final var libSourcesTable = TreeBasedTable.<Unqualified, Optional<Revision>, SourceSpecificContext>create(
            Unqualified::compareTo, Revision::compare);
        for (var libSource : libSources) {
            final var libSourceIdentifier = requireNonNull(libSource.getRootIdentifier());
            libSourcesTable.put(libSourceIdentifier.name(),
                Optional.ofNullable(libSourceIdentifier.revision()), libSource);
        }

        final var requiredLibs = new HashSet<SourceSpecificContext>();
        for (var source : sources) {
            collectRequiredSourcesFromLib(libSourcesTable, requiredLibs, source);
            removeConflictingLibSources(source, requiredLibs);
        }
        return requiredLibs;
    }

    private void collectRequiredSourcesFromLib(
            final TreeBasedTable<Unqualified, Optional<Revision>, SourceSpecificContext> libSourcesTable,
            final Set<SourceSpecificContext> requiredLibs, final SourceSpecificContext source) {
        for (var requiredSource : source.getRequiredSources()) {
            final var libSource = getRequiredLibSource(requiredSource, libSourcesTable);
            if (libSource != null && requiredLibs.add(libSource)) {
                collectRequiredSourcesFromLib(libSourcesTable, requiredLibs, libSource);
            }
        }
    }

    private static SourceSpecificContext getRequiredLibSource(final SourceIdentifier requiredSource,
            final TreeBasedTable<Unqualified, Optional<Revision>, SourceSpecificContext> libSourcesTable) {
        final var revision = requiredSource.revision();
        return revision != null ? libSourcesTable.get(requiredSource.name(), Optional.of(revision))
            : getLatestRevision(libSourcesTable.row(requiredSource.name()));
    }

    private static SourceSpecificContext getLatestRevision(
            final SortedMap<Optional<Revision>, SourceSpecificContext> sourceMap) {
        return sourceMap != null && !sourceMap.isEmpty() ? sourceMap.get(sourceMap.lastKey()) : null;
    }

    // removes required library sources which would cause namespace/name conflict with one of the main sources
    // later in the parsing process. this can happen if we add a parent module or a submodule as a main source
    // and the same parent module or submodule is added as one of the library sources.
    // such situation may occur when using the yang-system-test artifact - if a parent module/submodule is specified
    // as its argument and the same dir is specified as one of the library dirs through -p option).
    private static void removeConflictingLibSources(final SourceSpecificContext source,
            final Set<SourceSpecificContext> requiredLibs) {
        final var requiredLibsIter = requiredLibs.iterator();
        while (requiredLibsIter.hasNext()) {
            final var currentReqSource = requiredLibsIter.next();
            if (source.getRootIdentifier().equals(currentReqSource.getRootIdentifier())) {
                requiredLibsIter.remove();
            }
        }
    }

    private void endPhase(final ModelProcessingPhase phase) {
        checkState(currentPhase == phase);
        finishedPhase = currentPhase;
        LOG.debug("Global phase {} finished", phase);
    }

    Set<SourceSpecificContext> getSources() {
        return sources;
    }

    public Set<YangVersion> getSupportedVersions() {
        return supportedVersions;
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
