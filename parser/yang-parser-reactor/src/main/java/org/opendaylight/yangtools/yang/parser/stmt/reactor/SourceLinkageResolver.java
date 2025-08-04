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
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
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

    private final Map<Unqualified, List<SourceIdentifier>> resolvedSourcesGrouped = new HashMap<>();
    private final Map<SourceIdentifier, ResolvedSource> resolvedSources = new HashMap<>();

    private final BuildGlobalContext globalContext;

    static final class ResolvedSource {
        private final SourceSpecificContext context;
        private RootStatementContext<?, ?, ?> rootStmt;
        private final List<SourceIdentifier> imports;
        private final List<SourceIdentifier> includes;
        private final SourceIdentifier belongsTo;

            ResolvedSource(SourceSpecificContext context, RootStatementContext<?, ?, ?> rootStmt,
                List<SourceIdentifier> imports,
                List<SourceIdentifier> includes, SourceIdentifier belongsTo) {
                this.context = requireNonNull(context);
                this.rootStmt = rootStmt;
                this.imports = imports;
                this.includes = includes;
                this.belongsTo = belongsTo;
            }

            ResolvedSource(final @NonNull SourceSpecificContext sourceContext) {
                this(sourceContext, null, new LinkedList<>(), new LinkedList<>(), null);
            }

        public SourceSpecificContext context() {
            return context;
        }

        public RootStatementContext<?, ?, ?> rootStmt() {
            return rootStmt;
        }

        public void setRootStmt(RootStatementContext<?, ?, ?> rootStmt) {
            this.rootStmt = rootStmt;
        }

        public List<SourceIdentifier> imports() {
            return imports;
        }

        public List<SourceIdentifier> includes() {
            return includes;
        }

        public SourceIdentifier belongsTo() {
            return belongsTo;
        }
    }

    SourceLinkageResolver(final BuildGlobalContext globalContext,
        final @NonNull Collection<SourceSpecificContext> withMainSources,
        final @NonNull Collection<SourceSpecificContext> withLibSources) {
        this.globalContext = requireNonNull(globalContext);
        this.mainSources.addAll(requireNonNull(withMainSources));
        this.libSources.addAll(requireNonNull(withLibSources));
    }

    Map<SourceIdentifier, ResolvedSource> resolveInvolvedSources() {
        if (mainSources.isEmpty()) {
            return Collections.emptyMap();
        }

        for (SourceSpecificContext source : mainSources) {
            final SourceInfo sourceInfo = source.getSourceInfo();
            final SourceIdentifier sourceId = sourceInfo.sourceId().value();

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

            final SourceInfo sourceInfo = libSource.getSourceInfo();
            final SourceIdentifier sourceId = sourceInfo.sourceId().value();
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
            tryResolveImports(mainSource.getSourceInfo().sourceId().value());
        }

        resolvedSources.forEach(this::tryResolveIncludes);
        resolvedSources.forEach(this::fillSourceFromSourceInfo);
        linkResolvedSources();

        return resolvedSources;
    }

    private void linkResolvedSources() {
        for (var resolvedSource : resolvedSources.entrySet()) {
            SourceIdentifier id = resolvedSource.getKey();
            SourceLinkageResolver.ResolvedSource resolvedInfo = resolvedSource.getValue();
            resolvedInfo.context();
        }
    }

    private void linkModuleNamespaces() {

    }

    private void tryResolveIncludes(SourceIdentifier id, ResolvedSource resolvedSource) {
        final Deque<SourceIdentifier> dependencyChain = new ArrayDeque<>();
        dependencyChain.addFirst(id);
        while (!dependencyChain.isEmpty()) {
            SourceIdentifier current = dependencyChain.pollFirst();
            Set<Include> includes = getIncludesOf(current);
            Set<SourceIdentifier> resolvedIncludes = new HashSet<>(includes.size());
            boolean allIncludesResolved = true;
            for (Include include : includes) {
                Unqualified includeName = include.name().value();
                List<SourceIdentifier> resolvedMatchingQName = resolvedSourcesGrouped.get(includeName);
                Optional<SourceIdentifier> foundResolvedMatch = resolvedMatchingQName.stream()
                    .filter(include::isSatisfiedBy).findFirst();
                if (foundResolvedMatch.isPresent()) {
                    resolvedIncludes.add(foundResolvedMatch.get());
                } else {
                    //if it's not resolved yet, find it among all libs and add it to the chain
                    List<SourceIdentifier> allMatchingQname = allSourcesMapped.get(includeName);
                    Optional<SourceIdentifier> foundMatching = allMatchingQname.stream().filter(include::isSatisfiedBy)
                        .findFirst();
                    if (foundMatching.isPresent()) {
                        dependencyChain.addLast(foundMatching.get());
                        allIncludesResolved = false;
                    } else {
                        throw new IllegalArgumentException(String.format("Missing dependency %s of source %s",
                            includeName, current));
                    }
                }
            }
            if (allIncludesResolved) {
                //TODO: make sure to store this information, which will later be added as the linkage namespace to the
                // RootStatementContext
                final ResolvedSource resolved = addResolvedSource(current);
                resolved.includes().addAll(resolvedIncludes);
            } else {
                //some includes were not resolved, so we'll have to check this later again.
                dependencyChain.addLast(current);
            }
        }
    }

    private @NonNull List<SourceIdentifier> findAmongResolved(final Unqualified name) {
        final List<SourceIdentifier> resolvedMatchingQName = resolvedSourcesGrouped.get(name);
        if (resolvedMatchingQName != null) {
            return resolvedMatchingQName;
        }
        return Collections.emptyList();
    }

    private @NonNull List<SourceIdentifier> findAmongAll(final Unqualified name) {
        final List<SourceIdentifier> resolvedMatchingQName = allSourcesMapped.get(name);
        if (resolvedMatchingQName != null) {
            return resolvedMatchingQName;
        }
        return Collections.emptyList();
    }

    private void tryResolveImports(SourceIdentifier id) {
        if (resolvedSources.containsKey(id)) {
            return;
        }
        final Deque<SourceIdentifier> dependencyChain = new ArrayDeque<>();
        dependencyChain.addFirst(id);
        while (!dependencyChain.isEmpty()) {
            SourceIdentifier current = dependencyChain.pollFirst();
            Set<Import> imports = getImportsOf(current);
            Set<SourceIdentifier> resolvedImports = new HashSet<>(imports.size());
            // check if it's already among resolved - we don't add it to the chain
            boolean allImportsResolved = true;
            for (Import anImport : imports) {
                Unqualified importName = anImport.name().value();
                List<SourceIdentifier> resolvedMatchingQName = findAmongResolved(importName);
                Optional<SourceIdentifier> foundResolvedMatch = resolvedMatchingQName.stream()
                    .filter(anImport::isSatisfiedBy).findFirst();
                if (foundResolvedMatch.isPresent()) {
                    resolvedImports.add(foundResolvedMatch.get());
                } else {
                    //if it's not resolved yet, find it among all libs and add it to the chain
                    List<SourceIdentifier> allMatchingQname = findAmongAll(importName);
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
                final ResolvedSource resolved = addResolvedSource(current);
                resolved.imports().addAll(resolvedImports);
            } else {
                //some imports were not resolved, so we'll have to check this later again.
                dependencyChain.addLast(current);
            }
        }
    }

    private ResolvedSource addResolvedSource(final SourceIdentifier id) {
        if (resolvedSources.containsKey(id)) {
            return resolvedSources.get(id);
        }
        final ResolvedSource newResolved = new ResolvedSource(allContexts.get(id));
        resolvedSources.put(id, newResolved);
        final List<SourceIdentifier> potentials = resolvedSourcesGrouped.get(id.name());
        if (potentials != null) {
            potentials.add(id);
        } else {
            resolvedSourcesGrouped.put(id.name(), new LinkedList<>(List.of(id)));
        }
        return newResolved;
    }

    private Set<Import> getImportsOf(final SourceIdentifier id) {
        final SourceInfo sourceInfo = allSources.get(id);
        return sourceInfo.imports();
    }

    private Set<Include> getIncludesOf(final SourceIdentifier id) {
        final SourceInfo sourceInfo = allSources.get(id);
        return sourceInfo.includes();
    }

    void fillSourceFromSourceInfo(SourceIdentifier id, ResolvedSource resolvedSource) throws RuntimeException {
        final SourceSpecificContext source = resolvedSource.context();
        final SourceInfo sourceInfo = source.getSourceInfo();
        final AtomicInteger childOffset = new AtomicInteger();
        final YangVersion version = sourceInfo.yangVersion().value();
        final RootStatementContext<?,?,?> root = createRootStatement(source, sourceInfo);
        resolvedSource.setRootStmt(root);

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
