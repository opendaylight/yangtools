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
import com.google.common.collect.TreeBasedTable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.ir.IRArgument;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDeclaration;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.spi.meta.StatementDeclarations;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.MutableStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceNotAvailableException;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceStorage.GlobalStorage;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundles;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundles.ValidationBundleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class BuildGlobalContext extends AbstractNamespaceStorage implements GlobalStorage {
    private static final Logger LOG = LoggerFactory.getLogger(BuildGlobalContext.class);

    private static final ModelProcessingPhase[] PHASE_EXECUTION_ORDER = {
        ModelProcessingPhase.SOURCE_PRE_LINKAGE,
        ModelProcessingPhase.SOURCE_LINKAGE,
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
        for (var phase : PHASE_EXECUTION_ORDER) {
            startPhase(phase);
            loadPhaseStatements();
            completePhaseActions();
            endPhase(phase);
        }
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
//        addImportLinkages(involvedSources);
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private void loadPhaseStatementsFor(final Set<SourceSpecificContext> srcs) throws ReactorException {
        //TODO: we'll just start with STATEMENT_DECLARATION here.
        if (currentPhase.equals(ModelProcessingPhase.SOURCE_PRE_LINKAGE)) {
            for (var source : srcs) {
                fillSourceFromSourceInfo(source);
            }
        } else if (currentPhase.equals(ModelProcessingPhase.SOURCE_LINKAGE)) {
            return;
        } else {
            for (var source : srcs) {
                try {
                    source.loadStatements();
                } catch (RuntimeException e) {
                    throw propagateException(source, e);
                }
            }
        }
    }

    private void fillSourceFromSourceInfo(SourceSpecificContext source) throws ReactorException {

        final SourceInfo sourceInfo = source.getSourceInfo();
        if (sourceInfo == null) {
            try {
                source.loadStatements();
            } catch (RuntimeException e) {
                throw propagateException(source, e);
            }
            return;
        }
        final AtomicInteger childOffset = new AtomicInteger();
        final RootStatementContext<?,?,?> root = createRootStatement(source, sourceInfo);

        addSimpleStatement(source, root, sourceInfo, "yang-version", childOffset);
        addSimpleStatement(source, root, sourceInfo, "namespace", childOffset);
        addSimpleStatement(source, root, sourceInfo, "prefix", childOffset);
        addSimpleStatement(source, root, sourceInfo, "contact", childOffset);
        addSimpleStatement(source, root, sourceInfo, "description", childOffset);
        addSimpleStatement(source, root, sourceInfo, "organization", childOffset);
        addSimpleStatement(source, root, sourceInfo, "reference", childOffset);

        addRevisions(source, root, sourceInfo, childOffset);
        addImports(source, root, sourceInfo, childOffset);
        addIncludes(source, root, sourceInfo, childOffset);
        addBelongsTo(source, root, sourceInfo, childOffset);

//        fillSimpleNamespaces(source, root, sourceInfo);
    }

    private String stmtToArgumentString(IRStatement stmt) {
        return ((IRArgument.Single) stmt.argument()).string();
    }

    private StatementDeclaration.InText toStmtDef(IRStatement statement, SourceInfo info) {
        return StatementDeclarations.inText(info.sourceId().toYangFilename(),
            statement.startLine(), statement.startColumn() + 1);
    }

    private void addBelongsTo(SourceSpecificContext source, RootStatementContext<?, ?, ?> root, SourceInfo sourceInfo,
        AtomicInteger childOffset) {
        if (sourceInfo instanceof SourceInfo.Submodule submoduleInfo) {
            IRStatement belongsToIR = sourceInfo.rootStatement().statements().stream()
                .filter(stmt -> stmt.keyword().asStringDeclaration().equals("belongs-to"))
                .findFirst().get();
            AbstractResumedStatement<?, ?, ?> belongsToStmt = addSimpleStatement(source, root, sourceInfo, "belongs-to", childOffset);
            addSubstatement(belongsToStmt, belongsToIR.statements().get(0), source, sourceInfo, 0);
        }
    }

    private void addIncludes(SourceSpecificContext source, RootStatementContext<?, ?, ?> root, SourceInfo sourceInfo,
        AtomicInteger childOffset) {
        if (sourceInfo.includes().isEmpty()) {
            return;
        }

        final QName qNameInclude = QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE, "include");

        List<? extends IRStatement> includeIRStatements = sourceInfo.rootStatement().statements().stream()
            .filter(stmt -> stmt.keyword().asStringDeclaration().equals("include"))
            .collect(Collectors.toList());

        for (IRStatement includeIR : includeIRStatements) {
            AbstractResumedStatement<?, ?, ?> includeStatement = source.createDeclaredChild(root,
                childOffset.getAndIncrement(), qNameInclude, stmtToArgumentString(includeIR), toStmtDef(includeIR, sourceInfo));
            if (!includeIR.statements().isEmpty()) {
                Map<String, ? extends IRStatement> collect = includeIR.statements().stream()
                    .collect(Collectors.toMap(child -> child.keyword().asStringDeclaration(),
                        child -> child));
                if (collect.containsKey("revision-date")) {
                    addSubstatement(includeStatement, collect.get("revision-date"), source, sourceInfo, 0);
                }
            }
        }
    }

    private void addImports(SourceSpecificContext source, RootStatementContext<?, ?, ?> root, SourceInfo sourceInfo,
            AtomicInteger childOffset) {
        if (sourceInfo.imports().isEmpty()) {
            return;
        }

        final QName qNameImport = QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE, "import");

        List<? extends IRStatement> importIRStatements = sourceInfo.rootStatement().statements().stream()
            .filter(stmt -> stmt.keyword().asStringDeclaration().equals("import"))
            .collect(Collectors.toList());

        for (IRStatement importIR : importIRStatements) {
            AbstractResumedStatement<?, ?, ?> importStatement = source.createDeclaredChild(root,
                    childOffset.getAndIncrement(), qNameImport, stmtToArgumentString(importIR), toStmtDef(importIR, sourceInfo));
            if (!importIR.statements().isEmpty()) {
                Map<String, ? extends IRStatement> collect = importIR.statements().stream()
                    .collect(Collectors.toMap(child -> child.keyword().asStringDeclaration(),
                        child -> child));
                int importChildOffset = 0;
                if (collect.containsKey("prefix")) {
                    addSubstatement(importStatement, collect.get("prefix"), source, sourceInfo, importChildOffset++);
                }
                if (collect.containsKey("revision-date")) {
                    addSubstatement(importStatement, collect.get("revision-date"), source, sourceInfo, importChildOffset++);
                }
                if (collect.containsKey("description")) {
                    addSubstatement(importStatement, collect.get("description"), source, sourceInfo, importChildOffset++);
                }
                if (collect.containsKey("reference")) {
                    addSubstatement(importStatement, collect.get("reference"), source, sourceInfo, importChildOffset++);
                }

            }
        }
    }

    private AbstractResumedStatement<?, ?, ?> addSimpleStatement(SourceSpecificContext source, RootStatementContext<?, ?, ?> parent, SourceInfo sourceInfo, String statementName,
            AtomicInteger childOffset) {
        final QName qName = QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE, statementName);
        Optional<? extends IRStatement> statement = sourceInfo.rootStatement().statements().stream()
                                                         .filter(s -> s.keyword().asStringDeclaration()
                                                                              .equals(statementName)).findFirst();
        if (!statement.isPresent()) {
            return null;
        }
        getStatementDefContext(sourceInfo.yangVersion(), qName);
        return source.createDeclaredChild(parent, childOffset.getAndIncrement(), qName,
            stmtToArgumentString(statement.get()), toStmtDef(statement.get(), sourceInfo));
    }

    private void addRevisions(SourceSpecificContext source, RootStatementContext<?,?,?> root, SourceInfo sourceInfo,
            AtomicInteger childOffset) {
        final ImmutableSet<Revision> revisions = sourceInfo.revisions();
        if (revisions.isEmpty()) {
            return;
        }

        for (IRStatement statement : sourceInfo.rootStatement().statements()) {
            if (statement.keyword().asStringDeclaration().equals("revision")) {
                final QName qName = QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE, "revision");
                final String argument = ((IRArgument.Single) statement.argument()).string();

                AbstractResumedStatement<?, ?, ?> revisionRoot = source.createDeclaredChild(root,
                        childOffset.getAndIncrement(), qName, argument, toStmtDef(statement, sourceInfo));
                if (!statement.statements().isEmpty()) {
                    Map<String, ? extends IRStatement> collect = statement.statements().stream()
                        .collect(Collectors.toMap(child -> child.keyword().asStringDeclaration(),
                                child -> child));
                    int revisionChildOffset = 0;
                    if (collect.containsKey("description")) {
                        addSubstatement(revisionRoot, collect.get("description"), source, sourceInfo, revisionChildOffset++);
                    }
                    if (collect.containsKey("reference")) {
                        addSubstatement(revisionRoot, collect.get("reference"), source, sourceInfo, revisionChildOffset);
                    }
                }
            }
        }
    }

    // TODO: convenience method - will be probably removed when we get rid of the processing phases them selves
    private StatementDefinitionContext<?,?,?> getStatementDefContext(YangVersion version, QName qName) {
        StatementDefinitionContext<?,?,?> statementSupport = this.definitions.get(version, qName);
        if (statementSupport == null) {
            statementSupport = getSupportFor(ModelProcessingPhase.SOURCE_PRE_LINKAGE, version, qName);
        }

        if (statementSupport == null) {
            statementSupport = getSupportFor(ModelProcessingPhase.SOURCE_LINKAGE, version, qName);
        }
        return statementSupport;
    }

    // TODO: convenience method - will be probably removed when we get rid of the processing phases them selves
    private StatementDefinitionContext<?,?,?> getSupportFor(ModelProcessingPhase phase, YangVersion version, QName qName) {
        StatementSupportBundle supportBundle = supports.get(phase);
        StatementSupport<?, ?, ?> statementDefRaw = supportBundle.getStatementDefinition(version, qName);
        if (statementDefRaw != null) {
            StatementDefinitionContext<?, ?, ?> definitionContext = new StatementDefinitionContext<>(statementDefRaw);
            definitions.put(version, qName, definitionContext);
            return definitionContext;
        }
        return null;
    }



    private void addSubstatement(AbstractResumedStatement<?, ?, ?> parent, IRStatement substatement,
            SourceSpecificContext source, SourceInfo sourceInfo, int childOffset) {
        final QName qName = QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE, substatement.keyword().asStringDeclaration());
        StatementDefinitionContext<?, ?, ?> statementDefContext = getStatementDefContext(sourceInfo.yangVersion(), qName);
        parent.createSubstatement(childOffset, statementDefContext, toStmtDef(substatement, sourceInfo),
                ((IRArgument.Single) substatement.argument()).string());
    }

    private RootStatementContext<?, ?, ?> createRootStatement(SourceSpecificContext source, SourceInfo sourceInfo) {
        final QName qName = sourceInfo instanceof SourceInfo.Module ?
                              QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE, "module") :
                              QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE, "submodule");

        final AbstractResumedStatement<?, ?, ?> newRoot = source.createDeclaredChild(null, 0, qName,
                sourceInfo.sourceId().name().getLocalName(), toStmtDef(sourceInfo.rootStatement(), sourceInfo));
        newRoot.setRootIdentifier(sourceInfo.sourceId());
        return (RootStatementContext<?, ?, ?>) newRoot;
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
