/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedBelongsTo;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedImport;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedInclude;

/**
 * DTO containing all the linkage information which needs to be supplied to a RootStatementContext. This info will be
 * used to construct linkage substatements like imports, includes, belongs-to etc...
 */
public record ResolvedSourceInfo(
        @NonNull SourceIdentifier sourceId,
        @NonNull QNameModule qnameModule,
        @NonNull List<ResolvedImport> imports,
        @NonNull List<ResolvedInclude> includes,
        // FIXME: should never be null
        @Nullable Unqualified prefix,
        @Nullable ResolvedBelongsTo belongsTo) {

    @NonNullByDefault
    public ResolvedSourceInfo {
        requireNonNull(sourceId);
        requireNonNull(qnameModule);
        imports = List.copyOf(imports);
        includes = List.copyOf(includes);
    }
}
