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
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * DTO containing all the linkage information which needs to be supplied to a RootStatementContext. This info will be
 * used to construct linkage substatements like imports, includes, belongs-to etc...
 */
public record ResolvedSourceInfo(
    SourceIdentifier sourceId,
    QNameModule qnameModule,
    String prefix,
    StmtContext<?, ?, ?> root,
    List<ResolvedImport> imports,
    List<ResolvedInclude> includes,
    ResolvedBelongsTo belongsTo) {

    public ResolvedSourceInfo {
        requireNonNull(sourceId);
        requireNonNull(qnameModule);
        requireNonNull(root);
        imports = ImmutableList.copyOf(requireNonNull(imports));
        includes = ImmutableList.copyOf(requireNonNull(includes));
    }

    public Map<String, QNameModule> getImportsPrefixToQNameIncludingSelf() {
        final Map<String, QNameModule> allPrefixed = new HashMap<>();
        imports.forEach((imp) -> allPrefixed.put(imp.prefix, imp.importedModule.qnameModule));

        if (prefix != null) {
            allPrefixed.put(prefix, qnameModule);
        }

        return allPrefixed;
    }

    public ResolvedSourceInfo getImportByPrefix(final String findPrefix) {
        return imports.stream()
            .filter(imp -> imp.prefix().equals(findPrefix))
            .map(imp -> imp.importedModule)
            .findFirst()
            .orElse(null);
    }

    public record ResolvedBelongsTo(String prefix,
        QNameModule parentModuleQname,
        StmtContext<?, ?, ?> parentRoot) {
        public ResolvedBelongsTo {
            requireNonNull(prefix);
            requireNonNull(parentModuleQname);
            requireNonNull(parentRoot);
        }
    }

    public record ResolvedInclude(SourceIdentifier includeId,
        QNameModule includeModuleQname,
        StmtContext<?, ?, ?> rootContext) {
        public ResolvedInclude {
            requireNonNull(includeId);
            requireNonNull(includeModuleQname);
            requireNonNull(rootContext);
        }
    }

    public record ResolvedImport(String prefix,
        ResolvedSourceInfo importedModule) {
        public ResolvedImport {
            requireNonNull(prefix);
            requireNonNull(importedModule);
        }
    }
}