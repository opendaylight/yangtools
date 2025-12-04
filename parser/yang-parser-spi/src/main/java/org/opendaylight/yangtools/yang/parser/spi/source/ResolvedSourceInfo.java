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
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * DTO containing all the linkage information which needs to be supplied to a RootStatementContext. This info will be
 * used to construct the substatements like imports, includes, belongs-to etc...
 */
public record ResolvedSourceInfo(
    SourceIdentifier sourceId,
    QNameModule qnameModule,
    String prefix,
    StmtContext<?, ?, ?> root,
    Map<String, ResolvedSourceInfo> imports,
    List<ResolvedInclude> includes,
    ResolvedBelongsTo belongsTo) {

    public ResolvedSourceInfo {
        requireNonNull(sourceId);
        requireNonNull(qnameModule);
        requireNonNull(root);
        imports = ImmutableMap.copyOf(requireNonNull(imports));
        includes = ImmutableList.copyOf(requireNonNull(includes));

        // FIXME: this class probably needs to be specialized to Module/Submodule implementations.
//        requireNonNull(prefix); // submodules dont have prefixes
//        requireNonNull(belongsTo); // modules dont have belongs to
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
}