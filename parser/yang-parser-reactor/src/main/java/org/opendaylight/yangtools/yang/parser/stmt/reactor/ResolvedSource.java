/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
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
    private final ImmutableMap<String, QNameModule> imports;
    private final ImmutableSet<QNameModule> includes;
    // TODO: consider a different way to store this info. Map.Entry works for now.
    private final Map.Entry<String, QNameModule> belongsTo;

    ResolvedSource(final @NonNull SourceSpecificContext context, final QNameModule qNameModule,
        final Map.Entry<String, QNameModule> belongsTo, final Map<String, QNameModule> resolvedImports,
        final Set<QNameModule> resolvedIncludes) {
        this.context = requireNonNull(context);
        this.qNameModule = qNameModule;
        this.belongsTo = belongsTo;
        this.imports = ImmutableMap.copyOf(resolvedImports);
        this.includes = ImmutableSet.copyOf(resolvedIncludes);
    }

    public SourceSpecificContext context() {
        return context;
    }

    public Map.Entry<String, QNameModule> getBelongsTo() {
        return belongsTo;
    }

    public QNameModule getQNameModule() {

        return qNameModule;
    }

    public ImmutableMap<String, QNameModule> getImports() {
        return imports;
    }

    public ImmutableSet<QNameModule> getIncludes() {
        return includes;
    }

    public static Builder builder() {
        return new Builder();
    }

    static final class Builder {

        private SourceInfo sourceInfo;
        private SourceSpecificContext context;
        private final ImmutableMap.Builder<String, QNameModule> imports = new ImmutableMap.Builder<>();
        private final ImmutableSet.Builder<QNameModule> includes = new ImmutableSet.Builder<>();
        private Map.Entry<String, QNameModule> belongsTo;

        Builder() {
            
        }

        public SourceInfo sourceInfo() {
            return sourceInfo;
        }

        public Builder setSourceInfo(SourceInfo sourceInfo) {
            this.sourceInfo = sourceInfo;
            return this;
        }

        public Builder setContext(SourceSpecificContext context) {
            this.context = context;
            return this;
        }

        public Builder addImport(String prefix, final QNameModule importedModule) {
            imports.put(prefix, importedModule);
            return this;
        }

        public Builder addInclude(final QNameModule includedSubmodule) {
            includes.add(includedSubmodule);
            return this;
        }

        public Builder setBelongsTo(final String prefix, final QNameModule belongsToModule) {
            this.belongsTo = Map.entry(prefix, belongsToModule);
            return this;
        }

        public ResolvedSource build() {
            return new ResolvedSource(context, resolveQnameModule(), belongsTo, imports.build(), includes.build());
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
                return QNameModule.ofRevision(belongsTo.getValue().namespace(), latestRevision);
            }
        }
    }
}