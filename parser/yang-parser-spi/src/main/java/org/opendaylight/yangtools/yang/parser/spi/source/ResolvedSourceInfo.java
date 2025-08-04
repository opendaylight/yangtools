/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * DTO containing all the linkage information which needs to be supplied to a RootStatementContext. This info will be
 * used to construct linkage substatements like imports, includes, belongs-to etc...
 */
public record ResolvedSourceInfo(
    @NonNull SourceIdentifier sourceId,
    @NonNull QNameModule qnameModule,
    @NonNull StmtContext<?, ?, ?> root,
    @NonNull List<ResolvedImport> imports,
    @NonNull List<ResolvedInclude> includes,
    @Nullable String prefix,
    @Nullable ResolvedBelongsTo belongsTo) {

    public record ResolvedBelongsTo(@NonNull String prefix, @NonNull QNameModule parentModuleQname,
            @NonNull StmtContext<?, ?, ?> parentRoot) {
        public ResolvedBelongsTo {
            requireNonNull(prefix);
            requireNonNull(parentModuleQname);
            requireNonNull(parentRoot);
        }
    }

    public record ResolvedInclude(@NonNull SourceIdentifier sourceId, @NonNull QNameModule qname,
            @NonNull StmtContext<?, ?, ?> root) {
        public ResolvedInclude {
            requireNonNull(sourceId);
            requireNonNull(qname);
            requireNonNull(root);

        }
    }

    public record ResolvedImport(@NonNull String prefix, @NonNull SourceIdentifier sourceId,
            @NonNull QNameModule qname, @NonNull StmtContext<?, ?, ?> root) {
        public ResolvedImport {
            requireNonNull(prefix);
            requireNonNull(sourceId);
            requireNonNull(qname);
            requireNonNull(root);
        }

        public static ResolvedImport of(final @NonNull String prefix,
                final @NonNull ResolvedSourceInfo importedSource) {
            final var imported = requireNonNull(importedSource);
            return new ResolvedImport(requireNonNull(prefix), imported.sourceId, imported.qnameModule, imported.root);
        }
    }

    public ResolvedSourceInfo {
        requireNonNull(sourceId);
        requireNonNull(qnameModule);
        requireNonNull(root);
        imports = ImmutableList.copyOf(requireNonNull(imports));
        includes = ImmutableList.copyOf(requireNonNull(includes));
    }

    /**
     * Returns a map of all modules accessible from this module (including itself).
     *
     * @return map of known modules mapped to their prefixes - including this module itself.
     */
    @NonNull public Map<String, QNameModule> mapAccessibleModulesToPrefixes() {
        final Map<String, QNameModule> allPrefixed = new HashMap<>();
        imports.forEach((imp) -> allPrefixed.put(imp.prefix, imp.qname));

        if (prefix != null) {
            allPrefixed.put(prefix, qnameModule);
        }

        return allPrefixed;
    }

    /**
     * Returns an {@link ResolvedImport} corresponding to the provided prefix.
     *
     * @param findPrefix prefix of the import statement
     * @return ResolvedImport defined with this prefix, or null.
     */
    @Nullable public ResolvedImport getImportByPrefix(final String findPrefix) {
        return imports.stream()
            .filter(imp -> imp.prefix().equals(findPrefix))
            .findFirst()
            .orElse(null);
    }
}