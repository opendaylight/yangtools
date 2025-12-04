/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.parser.spi.source.ResolvedSourceInfo;

/**
 * Constructs a {@link ResolvedSourceInfo} of a Source containing the linkage details about imports, includes,
 * belongsTo.
 */
final class ResolvedSourceBuilder {

    private final SourceInfo sourceInfo;
    private final SourceSpecificContext context;
    private final ImmutableMap.Builder<String, ResolvedSourceBuilder> imports = new ImmutableMap.Builder<>();
    private final ImmutableSet.Builder<ResolvedSourceBuilder> includes = new ImmutableSet.Builder<>();
    private Map.Entry<String, ResolvedSourceBuilder> belongsTo = null;

    private ResolvedSourceInfo buildFinished;

    ResolvedSourceBuilder(final SourceSpecificContext sourceContext) {
        this.context = sourceContext;
        this.sourceInfo = sourceContext.getSourceInfo();
    }

    public SourceSpecificContext getContext() {
        return context;
    }

    public YangVersion getYangVersion() {
        return this.sourceInfo.yangVersion();
    }

    public ResolvedSourceBuilder addImport(final String prefix, final ResolvedSourceBuilder importedModule) {
        ensureBuilderOpened();
        imports.put(prefix, importedModule);
        return this;
    }

    public ResolvedSourceBuilder addInclude(final ResolvedSourceBuilder includedSubmodule) {
        ensureBuilderOpened();
        includes.add(includedSubmodule);
        return this;
    }

    public ResolvedSourceBuilder setBelongsTo(final String prefix, final ResolvedSourceBuilder belongsToModule) {
        ensureBuilderOpened();
        this.belongsTo = Map.entry(prefix, belongsToModule);
        return this;
    }

    public ResolvedSourceInfo build(final Map<SourceSpecificContext, ResolvedSourceInfo> allResolved) {
        if (buildFinished != null) {
            return buildFinished;
        }

        final Map<String, ResolvedSourceInfo> resolvedImports = imports.build().entrySet().stream()
            .map(prefixedImport -> {
                final SourceSpecificContext impContext = prefixedImport.getValue().getContext();
                if (!allResolved.containsKey(impContext)) {
                    throw new IllegalStateException(String.format("Unresolved import %s of module %s",
                        prefixedImport.getValue().getSourceId(), this.getSourceId()));
                }
                return Map.entry(prefixedImport.getKey(), allResolved.get(impContext));
            })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        final var resolvedBelongsTo = belongsTo == null ? null :
           new ResolvedSourceInfo.ResolvedBelongsTo(belongsTo.getKey(), belongsTo.getValue().resolveQnameModule(),
               belongsTo.getValue().getContext().getRoot());

        final String prefix = sourceInfo instanceof SourceInfo.Module
            ? ((SourceInfo.Module) sourceInfo).prefix().getLocalName() : null;

        final List<ResolvedSourceInfo.ResolvedInclude> resolvedIncludes = includes.build()
            .stream()
            .map(builder -> new ResolvedSourceInfo.ResolvedInclude(builder.getSourceId(), builder.resolveQnameModule(),
                builder.context.getRoot()))
            .toList();

        buildFinished = new ResolvedSourceInfo(sourceInfo.sourceId(), resolveQnameModule(),
            prefix, context.getRoot(), resolvedImports, resolvedIncludes, resolvedBelongsTo);
        return buildFinished;
    }

    private SourceIdentifier getSourceId() {
        return this.sourceInfo.sourceId();
    }

    QNameModule resolveQnameModule() {
        if (sourceInfo instanceof SourceInfo.Module moduleInfo) {
            return moduleInfo.resolveModuleQName();
        } else {
            // Submodule's QNameModule is composed of parents Namespace + its own Revision (or null, if absent)
            verifyNotNull(belongsTo,  "Cannot resolve QNameModule of a submodule %s. Missing belongs-to",
                sourceInfo.sourceId());
            final Revision latestRevision = sourceInfo.revisions().isEmpty() ? null : sourceInfo.revisions()
                .stream()
                .max(Comparator.naturalOrder())
                .orElseThrow();
            return QNameModule.ofRevision(belongsTo.getValue().resolveQnameModule().namespace(), latestRevision);
        }
    }

    private void ensureBuilderOpened() {
        verify(this.buildFinished == null, "Builder for source %s was already closed",
            sourceInfo.sourceId());
    }
}