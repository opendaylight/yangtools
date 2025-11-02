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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;

/**
 * Contains the resolved information about a source. Such as the linkage details about imports, includes, belongsTo.
 */
final class ResolvedSource {
    private final SourceIdentifier sourceId;
    private final QNameModule qnameModule;
    private final String prefix;
    private final YangVersion yangVersion;
    private final SourceSpecificContext context;
    private final ImmutableMap<String, ResolvedSource> imports;

    // includes mapped to their name
    private final ImmutableMap<ResolvedSource, Unqualified> includes;

    // TODO: consider a different way to store this info. Map.Entry works for now.
    private final Map.Entry<String, QNameModule> belongsTo;

    ResolvedSource(final @NonNull SourceSpecificContext context, final SourceIdentifier sourceId,
        final QNameModule qnameModule, final YangVersion yangVersion, final String prefix,
        final Map.Entry<String, QNameModule> belongsTo, final Map<String, ResolvedSource> resolvedImports,
        final Map<ResolvedSource, Unqualified> resolvedIncludes) {
        this.context = requireNonNull(context);
        this.sourceId = sourceId;
        this.qnameModule = qnameModule;
        this.yangVersion = yangVersion;
        this.prefix = prefix;
        this.belongsTo = belongsTo;
        this.imports = ImmutableMap.copyOf(resolvedImports);
        this.includes = ImmutableMap.copyOf(resolvedIncludes);
    }

    public String getPrefix() {
        return prefix;
    }

    public SourceSpecificContext getContext() {
        return context;
    }

    public SourceIdentifier getSourceId() {
        return sourceId;
    }

    public Map.Entry<String, QNameModule> getBelongsTo() {
        return belongsTo;
    }

    public QNameModule getQNameModule() {
        return qnameModule;
    }

    public ImmutableMap<String, ResolvedSource> getImports() {
        return imports;
    }

    public ImmutableMap<ResolvedSource, @NonNull Unqualified> getIncludes() {
        return includes;
    }

    public static Builder builder(final SourceSpecificContext sourceContext) {
        return new Builder(sourceContext);
    }

    public YangVersion getYangVersion() {
        return this.yangVersion;
    }

    static final class Builder {

        private final SourceInfo sourceInfo;
        private final SourceSpecificContext context;
        private final ImmutableMap.Builder<String, Builder> imports = new ImmutableMap.Builder<>();
        private final ImmutableMap.Builder<Builder, Unqualified> includes = new ImmutableMap.Builder<>();
        private Map.Entry<String, Builder> belongsTo = null;

        private ResolvedSource buildFinished;

        Builder(final SourceSpecificContext sourceContext) {
            this.context = sourceContext;
            this.sourceInfo = sourceContext.getSourceInfo();
        }

        public SourceSpecificContext getContext() {
            return context;
        }

        public Builder addImport(final String prefix, final Builder importedModule) {
            ensureBuilderOpened();
            imports.put(prefix, importedModule);
            return this;
        }

        public Builder addInclude(final Builder includedSubmodule, final Unqualified submoduleName) {
            ensureBuilderOpened();
            includes.put(includedSubmodule, submoduleName);
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

            final Map<ResolvedSource, Unqualified> resolvedIncludes = new LinkedHashMap<>();
            for (Map.Entry<Builder, Unqualified> anInclude : includes.build().entrySet()) {
                final SourceSpecificContext includeContext = anInclude.getKey().getContext();
                if (!allResolved.containsKey(includeContext)) {
                    throw new IllegalStateException(String.format(
                        "Unresolved include %s of module %s ", anInclude.getKey().getSourceId(), this.getSourceId())
                    );
                }
                resolvedIncludes.put(allResolved.get(includeContext), anInclude.getValue());
            }

            final var resolvedBelongsTo = belongsTo == null ? null :
                Map.entry(belongsTo.getKey(), belongsTo.getValue().resolveQnameModule());

            final String prefix = sourceInfo instanceof SourceInfo.Module
                ? ((SourceInfo.Module) sourceInfo).prefix().getLocalName() : null;

            buildFinished = new ResolvedSource(context, sourceInfo.sourceId(), resolveQnameModule(),
                sourceInfo.yangVersion(), prefix, resolvedBelongsTo, resolvedImports, resolvedIncludes);
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