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
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfoRef;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedBelongsTo;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedImport;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedInclude;

/**
 * DTO containing all the linkage information which needs to be supplied to a RootStatementContext. This info will be
 * used to construct linkage substatements like imports, includes, belongs-to etc...
 */
// FIXME: specialize for module/submodule
public record ResolvedSourceInfo(
        @NonNull SourceInfoRef infoRef,
        // TODO: rename to 'definingModule'?
        @NonNull QNameModule qnameModule,
        @NonNull List<ResolvedImport> imports,
        @NonNull List<ResolvedInclude> includes,
        @NonNull Unqualified prefix,
        @Nullable ResolvedBelongsTo belongsTo) {
    @NonNullByDefault
    public ResolvedSourceInfo {
        requireNonNull(infoRef);
        requireNonNull(qnameModule);
        imports = List.copyOf(imports);
        includes = List.copyOf(includes);
        requireNonNull(prefix);
    }

    static @NonNull ResolvedSourceInfo ofModule(final SourceInfoRef.@NonNull OfModule module,
            final @NonNull List<ResolvedImport> imports, final @NonNull List<ResolvedInclude> includes) {
        final var info = module.info();
        return new ResolvedSourceInfo(module, info.moduleName().getModule(), imports, includes, info.prefix(), null);
    }

    static @NonNull ResolvedSourceInfo ofSubmodule(final SourceInfoRef.@NonNull OfSubmodule submodule,
            final @NonNull ResolvedBelongsTo belongsTo, final @NonNull List<ResolvedImport> imports,
            final @NonNull List<ResolvedInclude> includes) {
        return new ResolvedSourceInfo(submodule, belongsTo.parentModuleQname(), imports, includes,
            belongsTo.dependency().prefix(), belongsTo);
    }
}
