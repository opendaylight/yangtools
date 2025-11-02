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
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;

/**
 * Contains the resolved information about a source. Such as the linkage details about imports, includes, belongsTo.
 */
final class ResolvedSource {

    record ResolvedBelongsTo(String prefix, QNameModule parentModuleQname) {
        ResolvedBelongsTo {
            requireNonNull(prefix);
            requireNonNull(parentModuleQname);
        }
    }

    private final SourceIdentifier sourceId;
    private final QNameModule qnameModule;
    private final String prefix;
    private final YangVersion yangVersion;
    private final SourceSpecificContext context;
    private final ImmutableMap<String, ResolvedSource> imports;
    private final ImmutableMap<SourceIdentifier, QNameModule> includes;
    private final ResolvedBelongsTo belongsTo;

    ResolvedSource(final @NonNull SourceSpecificContext context, final SourceIdentifier sourceId,
        final QNameModule qnameModule, final YangVersion yangVersion, final String prefix,
        final ResolvedBelongsTo belongsTo, final Map<String, ResolvedSource> resolvedImports,
        final Map<SourceIdentifier, QNameModule> resolvedIncludes) {
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

    public ResolvedBelongsTo getBelongsTo() {
        return belongsTo;
    }

    public QNameModule getQNameModule() {
        return qnameModule;
    }

    public ImmutableMap<String, ResolvedSource> getImports() {
        return imports;
    }

    public ImmutableMap<SourceIdentifier, QNameModule> getIncludes() {
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
        private final ImmutableSet.Builder<Builder> includes = new ImmutableSet.Builder<>();
        private ResolvedBelongsTo belongsTo = null;

        private ResolvedSource buildFinished;

        Builder(final SourceSpecificContext sourceContext) {
            this.context = sourceContext;
            this.sourceInfo = sourceContext.getSourceInfo();
        }

        public SourceSpecificContext getContext() {
            return context;
        }

        public YangVersion getYangVersion() {
            return this.sourceInfo.yangVersion();
        }

        private SourceIdentifier getSourceId() {
            return this.sourceInfo.sourceId();
        }

        public Builder addImport(final String prefix, final Builder importedModule) {
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
            this.belongsTo = new ResolvedBelongsTo(prefix, belongsToModule.resolveQnameModule());
            return this;
        }

        public ResolvedSource build(final Map<SourceSpecificContext, ResolvedSource> allResolved) {
            if (buildFinished != null) {
                return buildFinished;
            }

            final Map<String, ResolvedSource> resolvedImports = imports.build().entrySet().stream()
                .map(prefixedImport -> {
                    final SourceSpecificContext impContext = prefixedImport.getValue().getContext();
                    if (!allResolved.containsKey(impContext)) {
                        throw new IllegalStateException(String.format("Unresolved import %s of module %s",
                            prefixedImport.getValue().getSourceId(), this.getSourceId()));
                    }
                    return Map.entry(prefixedImport.getKey(), allResolved.get(impContext));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            final String prefix = sourceInfo instanceof SourceInfo.Module
                ? ((SourceInfo.Module) sourceInfo).prefix().getLocalName() : null;

            final Map<SourceIdentifier, QNameModule> resolvedIncludes = includes.build()
                .stream()
                .map(builder -> Map.entry(builder.getSourceId(), builder.resolveQnameModule()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            buildFinished = new ResolvedSource(context, sourceInfo.sourceId(), resolveQnameModule(),
                sourceInfo.yangVersion(), prefix, belongsTo, resolvedImports, resolvedIncludes);
            return buildFinished;
        }

        QNameModule resolveQnameModule() {
            if (sourceInfo instanceof SourceInfo.Module moduleInfo) {
                return QNameModule.ofRevision(moduleInfo.namespace(), latestRevision());
            } else {
                // Submodule's QNameModule is composed of parents Namespace + its own Revision (or null, if absent)
                verifyNotNull(belongsTo,  "Cannot resolve QNameModule of a submodule %s. Missing belongs-to",
                    sourceInfo.sourceId());
                return QNameModule.ofRevision(belongsTo.parentModuleQname().namespace(), latestRevision());
            }
        }

        private Revision latestRevision() {
            return sourceInfo.revisions().isEmpty() ? null : sourceInfo.revisions().iterator().next();
        }

        private void ensureBuilderOpened() {
            verify(this.buildFinished == null, "Builder for source %s was already closed",
                sourceInfo.sourceId());
        }
    }
}