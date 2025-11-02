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
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;

/**
 * Contains the resolved information about a source. Such as the linkage details about imports, includes, belongsTo.
 */
final class ResolvedSource {
    private final QNameModule qNameModule;
    private final SourceSpecificContext context;
    private final ImmutableMap<String, ResolvedSource> imports;
    private final ImmutableSet<ResolvedSource> includes;
    // TODO: consider a different way to store this info. Map.Entry works for now.
    private final Map.Entry<String, QNameModule> belongsTo;

    ResolvedSource(final @NonNull SourceSpecificContext context, final QNameModule qNameModule,
        final Map.Entry<String, QNameModule> belongsTo, final Map<String, ResolvedSource> resolvedImports,
        final Set<ResolvedSource> resolvedIncludes) {
        this.context = requireNonNull(context);
        this.qNameModule = qNameModule;
        this.belongsTo = belongsTo;
        this.imports = ImmutableMap.copyOf(resolvedImports);
        this.includes = ImmutableSet.copyOf(resolvedIncludes);
    }

    public SourceSpecificContext getContext() {
        return context;
    }

    public Map.Entry<String, QNameModule> getBelongsTo() {
        return belongsTo;
    }

    public QNameModule getQNameModule() {
        return qNameModule;
    }

    public ImmutableMap<String, ResolvedSource> getImports() {
        return imports;
    }

    public ImmutableSet<ResolvedSource> getIncludes() {
        return includes;
    }

    public static Builder builder(final SourceSpecificContext sourceContext) {
        return new Builder(sourceContext);
    }

    static final class Builder {

        private final SourceInfo sourceInfo;
        private final SourceSpecificContext context;
        private final ImmutableMap.Builder<String, Builder> imports = new ImmutableMap.Builder<>();
        private final ImmutableSet.Builder<Builder> includes = new ImmutableSet.Builder<>();
        private Map.Entry<String, Builder> belongsTo;

        private ResolvedSource buildFinished;

        Builder(final SourceSpecificContext sourceContext) {
            this.context = sourceContext;
            this.sourceInfo = sourceContext.getSourceInfo();
        }

        public SourceSpecificContext getContext() {
            return context;
        }

        public Builder addImport(String prefix, final Builder importedModule) {
            ensureBuilderOpened();
            imports.put(prefix, importedModule);
            return this;
        }

        public Builder addInclude(final Builder includedSubmodule) {
            ensureBuilderOpened();
            includes.add(includedSubmodule);
            return this;
        }

        public Builder setBelongsTo(final String prefix, final Builder belongsToModule) {
            ensureBuilderOpened();
            this.belongsTo = Map.entry(prefix, belongsToModule);
            return this;
        }

        public ResolvedSource build(final Map<SourceSpecificContext, ResolvedSource> allResolved) {
            if (buildFinished != null) {
                return buildFinished;
            }

            final Map<String, ResolvedSource> resolvedImports = imports.build().entrySet().stream()
                .map(prefixedImport -> {
                    if (!allResolved.containsKey(prefixedImport.getValue().getContext())) {
                        throw new IllegalStateException(String.format("Unresolved import %s of module %s",
                            prefixedImport.getValue().getSourceId(), this.getSourceId()));
                    }
                    return Map.entry(prefixedImport.getKey(), allResolved.get(prefixedImport.getValue().getContext()));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            final Set<ResolvedSource> resolvedIncludes = includes.build().stream()
                .map(builder -> {
                    if (!allResolved.containsKey(builder.context)) {
                        throw new IllegalStateException(String.format(
                            "Unresolved include %s of module %s ", builder.getSourceId(), this.getSourceId())
                        );
                    }
                    return allResolved.get(builder.context);
                })
                .collect(Collectors.toSet());

            final var resolvedBelongsTo = belongsTo == null ? null :
                Map.entry(belongsTo.getKey(), belongsTo.getValue().resolveQnameModule());

            buildFinished = new ResolvedSource(context, resolveQnameModule(), resolvedBelongsTo, resolvedImports,
                resolvedIncludes);
            return buildFinished;
        }

        private Object getSourceId() {
            return this.sourceInfo.sourceId();
        }

        private QNameModule resolveQnameModule() {
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
}