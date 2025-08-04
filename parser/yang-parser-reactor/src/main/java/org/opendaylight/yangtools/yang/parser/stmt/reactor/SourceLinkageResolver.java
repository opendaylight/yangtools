/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import static org.opendaylight.yangtools.yang.model.api.source.SourceDependency.BelongsTo;
import static org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import static org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Referenced;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.DetailedRevision;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;

public class SourceLinkageResolver {


    private Set<SourceSpecificContext> mainSources = new HashSet<>();
    private Set<SourceSpecificContext> libSources = new HashSet<>();

    private final Map<SourceIdentifier, SourceInfo> allSources = new HashMap<>();
    private final Map<SourceIdentifier, SourceSpecificContext> allContexts = new HashMap<>();
    private final Map<Unqualified, List<SourceIdentifier>> allSourcesMapped = new HashMap<>();

    final Map<Unqualified, List<SourceIdentifier>> resolvedSourcesGrouped = new HashMap<>();
    final Map<SourceIdentifier, SourceSpecificContext> resolvedSources = new HashMap<>();

    private final BuildGlobalContext globalContext;

    public SourceLinkageResolver(final BuildGlobalContext globalContext) {
        this.globalContext = globalContext;
    }

    public void withMainSources(final @NonNull Collection<SourceSpecificContext> sources) {
        final ImmutableSet<SourceSpecificContext> newMainSources = ImmutableSet.copyOf(requireNonNull(sources));
        mainSources = newMainSources;
    }

    public void withLibSources(final @NonNull Collection<SourceSpecificContext> sources) {
        final ImmutableSet<SourceSpecificContext> newLibSources = ImmutableSet.copyOf(requireNonNull(sources));
        libSources = newLibSources;
    }

    private void resolveInvolvedSources() {
        if (mainSources.isEmpty()) {
            return;
        }

        for (SourceSpecificContext source : mainSources) {
            final SourceIdentifier sourceId = source.getInternalSourceId();
            final SourceInfo sourceInfo = source.getSourceInfo();
            allSources.putIfAbsent(sourceId, sourceInfo);
            allContexts.putIfAbsent(sourceId, source);
            List<SourceIdentifier> allOfQname = allSourcesMapped.get(sourceId.name());
            if (allOfQname != null) {
                allOfQname.add(sourceId);
            } else {
                allSourcesMapped.put(sourceId.name(),  new LinkedList<>(List.of(sourceId)));
            }
        }
        for (SourceSpecificContext libSource : libSources) {
            final SourceIdentifier sourceId = libSource.getInternalSourceId();
            final SourceInfo sourceInfo = libSource.getSourceInfo();
            allSources.putIfAbsent(sourceId, sourceInfo);
            allContexts.putIfAbsent(sourceId, libSource);
            List<SourceIdentifier> allOfQname = allSourcesMapped.get(sourceId.name());
            if (allOfQname != null) {
                allOfQname.add(sourceId);
            } else {
                allSourcesMapped.put(sourceId.name(),  new LinkedList<>(List.of(sourceId)));
            }
        }

        for (SourceSpecificContext mainSource : mainSources) {
            tryResolveImports(mainSource.getInternalSourceId());
        }
    }

    private void tryResolveImports(SourceIdentifier id) {
        final Deque<SourceIdentifier> dependencyChain = new ArrayDeque<>();
        dependencyChain.addFirst(id);
        while (!dependencyChain.isEmpty()) {
            SourceIdentifier current = dependencyChain.pollFirst();
            Set<Import> imports = getDependenciesOf(current);
            // check if it's already among resolved - we don't add it to the chain
            boolean allImportsResolved = true;
            for (Import anImport : imports) {
                Unqualified importName = anImport.name().value();
                List<SourceIdentifier> resolvedMatchingQName = resolvedSourcesGrouped.get(anImport.name().value());
                Optional<SourceIdentifier> foundResolvedMatch = resolvedMatchingQName.stream()
                    .filter(anImport::isSatisfiedBy).findFirst();
                if (foundResolvedMatch.isEmpty()) {
                    //if it's not resolved yet, find it among all libs and add it to the chain
                    List<SourceIdentifier> allMatchingQname = allSourcesMapped.get(anImport.name().value());
                    Optional<SourceIdentifier> foundMatching = allMatchingQname.stream().filter(anImport::isSatisfiedBy)
                        .findFirst();
                    if (foundMatching.isPresent()) {
                        dependencyChain.addLast(foundMatching.get());
                        allImportsResolved = false;
                    } else {
                        throw new IllegalArgumentException(String.format("Missing dependency %s of source %s",
                            importName, current));
                    }
                }
            }
            if (allImportsResolved) {
                //TODO: make sure to store this information, which will later be added as the linkage namespace to the
                // RootStatementContext
                addResolvedSource(id);
            } else {
                //some imports were not resolved, so we'll have to check this later again.
                dependencyChain.addLast(id);
            }
        }
    }

    private void addResolvedSource(SourceIdentifier id) {
        resolvedSources.putIfAbsent(id, allContexts.get(id));
        final List<SourceIdentifier> potentials = resolvedSourcesGrouped.get(id.name());
        if (potentials != null) {
            potentials.add(id);
        } else {
            resolvedSourcesGrouped.put(id.name(), new LinkedList<>(List.of(id)));
        }
    }

    private Set<Import> getDependenciesOf(final SourceIdentifier parent) {
        final SourceInfo sourceInfo = allSources.get(parent);
        return sourceInfo.imports();
    }


//    private void linkInvolvedSources(final Map<Unqualified, InvolvedSource> involvedSources) {
//        QNameToStatementDefinitionMap stmtDefs = new QNameToStatementDefinitionMap();
////        stmtDefs.putAll(getSupportsForPhase(ModelProcessingPhase.SOURCE_LINKAGE).getCommonDefinitions());
//        stmtDefs.putAll(getSupportsForPhase(ModelProcessingPhase.SOURCE_PRE_LINKAGE).getCommonDefinitions());
//
//        for (Map.Entry<Unqualified, InvolvedSource> involvedSource : involvedSources.entrySet()) {
////            involvedSource.getValue().sourceContext.startPhase(ModelProcessingPhase.SOURCE_PRE_LINKAGE);
////            involvedSource.getValue().sourceContext.end(ModelProcessingPhase.SOURCE_PRE_LINKAGE);
//
//            // add linkage as well
////            stmtDefs.putAll(getSupportsForPhase(ModelProcessingPhase.SOURCE_LINKAGE)
////                .getDefinitionsSpecificForVersion(involvedSource.getValue().sourceInfo.getSourceInfo().yangVersion()));
//            stmtDefs.putAll(getSupportsForPhase(ModelProcessingPhase.SOURCE_PRE_LINKAGE)
//                .getDefinitionsSpecificForVersion(involvedSource.getValue().sourceInfo.getSourceInfo().yangVersion()));
//            involvedSource.getValue().sourceContext.startPhase(ModelProcessingPhase.SOURCE_LINKAGE);
//            StmtContext.Mutable<Unqualified, ?, ?> rootStatement =
//                createRootStatement(involvedSource.getValue(), stmtDefs);
//
//            //TODO: fill the pre-linkage and linkage namespaces of each root statement
//
//            if (involvedSource.getValue().sourceInfo.getSourceInfo() instanceof SourceInfo.Module moduleInfo) {
//                rootStatement.addToNs(ParserNamespaces.MODULE_NAME_TO_NAMESPACE, involvedSource.getKey(), moduleInfo.namespace());
//                rootStatement.addToNs(ParserNamespaces.IMP_PREFIX_TO_NAMESPACE, moduleInfo.prefix().getPrefix(), moduleInfo.namespace());
//                rootStatement.addToNs(ParserNamespaces.PRELINKAGE_MODULE, rootStatement.argument(),
//                    (StmtContext.Mutable<Unqualified, ModuleStatement, ModuleEffectiveStatement>)rootStatement);
//
//                final var revisionDate = moduleInfo.revisions().iterator().next();
//                final var qNameModule = QNameModule.ofRevision(moduleInfo.namespace(), revisionDate).intern();
//                rootStatement.addToNs(ParserNamespaces.MODULECTX_TO_QNAME, rootStatement, qNameModule);
//            }
//        }
//    }

    record InvolvedSource(
        SourceSpecificContext sourceContext,
        SourceInfo sourceInfo) {
        public InvolvedSource {
            requireNonNull(sourceContext);
            requireNonNull(sourceInfo);
        }
    }

//    Map<Unqualified, InvolvedSource> resolveInvolvedSources(final Set<SourceSpecificContext> sources,
//        final Set<SourceSpecificContext> libSources) {
//
//        //TODO: should probably map by SourceIdentifier, not just Qname..
//        final Map<Unqualified, InvolvedSource> involvedSources = new HashMap<>();
//        final Map<SourceIdentifier, SourceSpecificContext> yinSources = new HashMap<>();
//        final Map<SourceIdentifier, SourceSpecificContext> namedLibSources = new HashMap<>();
//        final Deque<Map.Entry<Unqualified, SourceSpecificContext>> importedToResolve = new ArrayDeque<>();
//
//        for (SourceSpecificContext libSource : libSources) {
//            if (libSource.getSourceInfo() != null) {
//                namedLibSources.put(libSource.getInternalSourceId(), libSource);
//            } else {
//                yinSources.put(libSource.getInternalSourceId(), libSource);
//            }
//        }
//
//        for (SourceSpecificContext source : sources) {
//            final SourceInfo sourceInfo = source.getSourceInfo();
//            if (sourceInfo != null) {
//                involvedSources.put(sourceInfo.sourceId().name(), new InvolvedSource(source, sourceInfo));
//                for (SourceDependency.Import anImport : sourceInfo.imports()) {
//                    final Unqualified importName = anImport.name();
//                    if (!involvedSources.containsKey(importName)) {
//                        final SourceSpecificContext importedLibSource = namedLibSources.get(importName);
//                        Verify.verifyNotNull(importedLibSource, "Imported source [%s] not found", importName.toString());
//                        importedToResolve.add(Map.entry(importName, importedLibSource));
//                    }
//                }
//            }
//
//        }
//
//        while (!importedToResolve.isEmpty()) {
//            final Map.Entry<Unqualified, SourceSpecificContext> toResolve = importedToResolve.removeFirst();
//            final ExtendedSourceInfo sourceInfo = toResolve.getValue().getSourceInfo();
//            involvedSources.put(toResolve.getKey(), new InvolvedSource(toResolve.getValue(), sourceInfo));
//            for (SourceDependency.Import anImport : sourceInfo.getSourceInfo().imports()) {
//                final Unqualified importName = anImport.name();
//                if (!involvedSources.containsKey(importName)) {
//                    final SourceSpecificContext importedLibSource = namedLibSources.get(importName);
//                    Verify.verifyNotNull(importedLibSource, "Imported source [%s] not found", importName.toString());
//                    importedToResolve.add(Map.entry(importName, importedLibSource));
//                }
//            }
//        }
//        return involvedSources;
//    }

    void fillSourceFromSourceInfo(SourceSpecificContext source) throws RuntimeException {
        final SourceInfo sourceInfo = source.getSourceInfo();
        final AtomicInteger childOffset = new AtomicInteger();
        final YangVersion version = sourceInfo.yangVersion().value();
        final RootStatementContext<?,?,?> root = createRootStatement(source, sourceInfo);

        addYangVersion(root, sourceInfo, childOffset);
        addNamespace(root, sourceInfo, childOffset);
        addModulePrefix(root, sourceInfo, childOffset);
        addAdditionalStringStmt(root, version, "contact", sourceInfo.contact(), childOffset);
        addAdditionalStringStmt(root, version, "organization", sourceInfo.description(), childOffset);
        addAdditionalStringStmt(root, version, "description", sourceInfo.organization(), childOffset);
        addAdditionalStringStmt(root, version, "reference", sourceInfo.reference(), childOffset);


        addRevisions(source, root, sourceInfo, childOffset);
        addImports(source, root, sourceInfo, childOffset);
        addIncludes(source, root, sourceInfo, childOffset);
        addBelongsTo(source, root, sourceInfo, childOffset);
    }

    private void addAdditionalStringStmt(
        AbstractResumedStatement<?, ?, ?> parent, YangVersion version, String stmtName, Referenced<String> stmt,
        AtomicInteger childOffset) {
        if (stmt == null) {
            return;
        }
        final QName qName = QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE, stmtName);
        final StatementDefinitionContext<?, ?, ?> def = globalContext.getStatementDefContext(version, qName);
        parent.createSubstatement(childOffset.getAndIncrement(), def, stmt.reference(), stmt.value());
    }

    private void addModulePrefix(RootStatementContext<?,?,?> root, SourceInfo sourceInfo, AtomicInteger childOffset) {
        if (sourceInfo instanceof SourceInfo.Module moduleInfo) {
            addPrefix(root, moduleInfo.prefix(), moduleInfo.yangVersion().value(), childOffset);
        }
    }


    private void addNamespace(RootStatementContext<?,?,?> root, SourceInfo sourceInfo, AtomicInteger childOffset) {
        if (sourceInfo instanceof SourceInfo.Module moduleInfo) {
            addSimpleStatement(root, sourceInfo.yangVersion().value(), "namespace",
                moduleInfo.namespace().value().toString(), moduleInfo.namespace().reference(),
                childOffset.getAndIncrement());
        }
    }

    private void addYangVersion(RootStatementContext<?,?,?> root, SourceInfo sourceInfo, AtomicInteger childOffset) {
        final Referenced<YangVersion> version = sourceInfo.yangVersion();
        if (version.value().reference() != null) {
            addSimpleStatement(root, version.value(), "yang-version",
                version.value().toString(), version.reference(), childOffset.getAndIncrement());

        }
    }

    private void addSimpleStatement(
        AbstractResumedStatement<?, ?, ?> parent, YangVersion version, String statementName, String argument,
        StatementSourceReference ref, int childOffset) {
        final QName qName = QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE, statementName);
        final StatementDefinitionContext<?, ?, ?> def = globalContext.getStatementDefContext(version, qName);
        parent.createSubstatement(childOffset, def, ref, argument);
    }

    private void addRevisions(SourceSpecificContext source, RootStatementContext<?,?,?> root, SourceInfo sourceInfo,
        AtomicInteger childOffset) {
        final ImmutableSet<DetailedRevision> revisions = sourceInfo.revisions();
        if (revisions.isEmpty()) {
            return;
        }

        final QName qName = QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE, "revision");
        AtomicInteger offset = new AtomicInteger();
        for (DetailedRevision revision : sourceInfo.revisions()) {
            AbstractResumedStatement<?, ?, ?> revisionStmt = source.createDeclaredChild(root,
                childOffset.getAndIncrement(), qName, revision.getRevision().value().toString(),
                revision.getRevision().reference());
            if (revision.getDescription() != null) {
                addAdditionalStringStmt(revisionStmt, sourceInfo.yangVersion().value(), "description",
                    revision.getDescription(), offset);
            }
            if (revision.getReference() != null) {
                addAdditionalStringStmt(revisionStmt, sourceInfo.yangVersion().value(), "reference",
                    revision.getReference(), offset);
            }
        }
    }

    private RootStatementContext<?, ?, ?> createRootStatement(SourceSpecificContext source, SourceInfo sourceInfo) {
        final QName qName = sourceInfo instanceof SourceInfo.Module ?
            QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE, "module") :
            QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE, "submodule");

        final AbstractResumedStatement<?, ?, ?> newRoot = source.createDeclaredChild(null, 0, qName,
            sourceInfo.sourceId().value().name().getLocalName(), sourceInfo.sourceId().reference());
        newRoot.setRootIdentifier(sourceInfo.sourceId().value());
        return (RootStatementContext<?, ?, ?>) newRoot;
    }

    private void addBelongsTo(SourceSpecificContext source, RootStatementContext<?, ?, ?> root, SourceInfo sourceInfo,
        AtomicInteger childOffset) {
        if (sourceInfo instanceof SourceInfo.Submodule subInfo) {
            final BelongsTo belongsTo = subInfo.belongsTo();
            final QName qName = QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE, "belongs-to");
            //TODO: do we need to call this for sure?
            //globalContext.getStatementDefContext(sourceInfo.yangVersion().value(), qName);

            final AbstractResumedStatement<?, ?, ?> belongsToStmt = source.createDeclaredChild(root,
                childOffset.getAndIncrement(), qName, belongsTo.name().value().getLocalName(), belongsTo.name().reference());
            addPrefix(belongsToStmt, belongsTo.prefix(), sourceInfo.yangVersion().value(), new AtomicInteger());
        }
    }

    private void addPrefix(final AbstractResumedStatement<?, ?, ?> parent, final Referenced<Unqualified> prefix, final YangVersion version, AtomicInteger childOffset) {
        final QName qName = QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE, "prefix");
        final StatementDefinitionContext<?, ?, ?> def = globalContext.getStatementDefContext(version, qName);
        parent.createSubstatement(childOffset.getAndIncrement(), def, prefix.reference(),
            prefix.value().getLocalName());
    }

    private void addRevisionDate(final AbstractResumedStatement<?, ?, ?> parent, final Referenced<Revision> revision,
        final YangVersion version, AtomicInteger childOffset) {
        final QName qName = QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE, "revision-date");
        StatementDefinitionContext<?, ?, ?> def = globalContext.getStatementDefContext(version, qName);
        parent.createSubstatement(childOffset.getAndIncrement(), def, revision.reference(),
            revision.value().toString());
    }

    private void addIncludes(SourceSpecificContext source, RootStatementContext<?, ?, ?> root, SourceInfo sourceInfo,
        AtomicInteger childOffset) {
        if (sourceInfo.includes().isEmpty()) {
            return;
        }

        final QName qname = QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE, "include");
        final YangVersion version = sourceInfo.yangVersion().value();
        for (Include include : sourceInfo.includes()) {
            AbstractResumedStatement<?, ?, ?> includeStatement = source.createDeclaredChild(root,
                childOffset.getAndIncrement(), qname, include.name().value().getLocalName(), include.name().reference());
            AtomicInteger offset = new AtomicInteger();
            if (include.revision() != null) {
                addRevisionDate(includeStatement, include.revision(), version, offset);
            }
            if (include.description() != null) {
                addAdditionalStringStmt(includeStatement, version, "description", include.description(), offset);
            }
            if (include.reference() != null) {
                addAdditionalStringStmt(includeStatement, version, "reference", include.description(), offset);
            }
        }
    }

    private void addImports(SourceSpecificContext source, RootStatementContext<?, ?, ?> root, SourceInfo sourceInfo,
        AtomicInteger childOffset) {
        if (sourceInfo.imports().isEmpty()) {
            return;
        }

        final QName qNameImport = QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE, "import");
        final YangVersion version = sourceInfo.yangVersion().value();
        for (Import anImport : sourceInfo.imports()) {
            AbstractResumedStatement<?, ?, ?> importStatement = source.createDeclaredChild(root,
                childOffset.getAndIncrement(), qNameImport, anImport.name().value().getLocalName(),
                anImport.name().reference());

            AtomicInteger importChildOffset = new AtomicInteger();

            if (anImport.prefix() != null) {
                addPrefix(importStatement, anImport.prefix(), version, importChildOffset);
            }
            if (anImport.revision() != null) {
                addRevisionDate(importStatement, anImport.revision(), version, importChildOffset);
            }
            if (anImport.description() != null) {
                addAdditionalStringStmt(importStatement, version, "description", anImport.description(),
                    importChildOffset);
            }
            if (anImport.reference() != null) {
                addAdditionalStringStmt(importStatement, version, "reference", anImport.reference(),
                    importChildOffset);
            }
        }
    }
}
