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
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;

/**
 * DTO containing all the linkage information which needs to be supplied to a RootStatementContext. This info will be
 * used to construct linkage substatements like imports, includes, belongs-to etc...
 */
public record ResolvedSourceInfo(
    @NonNull SourceIdentifier sourceId,
    @NonNull QNameModule qnameModule,
    @NonNull List<ResolvedImport> imports,
    @NonNull List<ResolvedInclude> includes,
    @Nullable String prefix,
    @Nullable ResolvedBelongsTo belongsTo) {

    public record ResolvedBelongsTo(@NonNull String prefix, @NonNull QNameModule parentModuleQname) {
        public ResolvedBelongsTo {
            requireNonNull(prefix);
            requireNonNull(parentModuleQname);
        }
    }

    public record ResolvedInclude(@NonNull SourceIdentifier sourceId, @NonNull QNameModule qname) {
        public ResolvedInclude {
            requireNonNull(sourceId);
            requireNonNull(qname);
        }
    }

    public record ResolvedImport(@NonNull String prefix, @NonNull SourceIdentifier sourceId,
            @NonNull QNameModule qname) {
        public ResolvedImport {
            requireNonNull(prefix);
            requireNonNull(sourceId);
            requireNonNull(qname);
        }

        public static ResolvedImport of(final @NonNull String prefix,
                final @NonNull ResolvedSourceInfo importedSource) {
            final var imported = requireNonNull(importedSource);
            return new ResolvedImport(requireNonNull(prefix), imported.sourceId, imported.qnameModule);
        }
    }

    public ResolvedSourceInfo {
        requireNonNull(sourceId);
        requireNonNull(qnameModule);
        imports = ImmutableList.copyOf(requireNonNull(imports));
        includes = ImmutableList.copyOf(requireNonNull(includes));
    }
}