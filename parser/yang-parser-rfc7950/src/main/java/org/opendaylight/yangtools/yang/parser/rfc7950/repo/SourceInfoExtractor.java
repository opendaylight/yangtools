/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.BelongsTo;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;

public abstract sealed class SourceInfoExtractor<T> permits YangIRSourceInfoExtractor, YinSourceInfoExtractor {

    static final String BELONGS_TO = YangStmtMapping.BELONGS_TO.getStatementName().getLocalName();
    static final String IMPORT = YangStmtMapping.IMPORT.getStatementName().getLocalName();
    static final String INCLUDE = YangStmtMapping.INCLUDE.getStatementName().getLocalName();
    static final String MODULE = YangStmtMapping.MODULE.getStatementName().getLocalName();
    static final String NAMESPACE = YangStmtMapping.NAMESPACE.getStatementName().getLocalName();
    static final String PREFIX = YangStmtMapping.PREFIX.getStatementName().getLocalName();
    static final String REVISION = YangStmtMapping.REVISION.getStatementName().getLocalName();
    static final String REVISION_DATE = YangStmtMapping.REVISION_DATE.getStatementName().getLocalName();
    static final String SUBMODULE = YangStmtMapping.SUBMODULE.getStatementName().getLocalName();
    static final String YANG_VERSION = YangStmtMapping.YANG_VERSION.getStatementName().getLocalName();

    private final @NonNull T root;
    private final @NonNull SourceIdentifier rootIdentifier;

    abstract @NonNull String extractRootType();

    abstract @NonNull Unqualified extractModulePrefix();

    abstract @NonNull XMLNamespace extractNamespace();

    abstract @NonNull BelongsTo extractBelongsTo();

    abstract @NonNull Unqualified extractName();

    abstract @Nullable YangVersion extractYangVersion();

    abstract void extractRevisions(SourceInfo.Builder<?, ?> builder);

    abstract void extractIncludes(SourceInfo.Builder<?, ?> builder);

    abstract void extractImports(SourceInfo.Builder<?, ?> builder);

    @NonNullByDefault
    SourceInfoExtractor(final T root, final SourceIdentifier rootIdentifier) {
        this.root = requireNonNull(root);
        this.rootIdentifier = requireNonNull(rootIdentifier);
    }

    T root() {
        return root;
    }

    SourceIdentifier rootId() {
        return rootIdentifier;
    }

    public final @NonNull SourceInfo getSourceInfo() {
        final String rootType = extractRootType();
        if (rootType.equals(MODULE)) {
            return extractModule();
        }

        if (rootType.equals(SUBMODULE)) {
            return extractSubmodule();
        }
        throw new IllegalArgumentException("Root of YING must be either module or submodule");
    }

    private SourceInfo.@NonNull Module extractModule() {
        final var builder = SourceInfo.Module.builder();
        fillCommon(builder);
        return builder
            .setPrefix(extractModulePrefix())
            .setNamespace(extractNamespace())
            .build();
    }

    private SourceInfo.@NonNull Submodule extractSubmodule() {
        final var builder = SourceInfo.Submodule.builder();
        fillCommon(builder);
        return builder
            .setBelongsTo(extractBelongsTo())
            .build();
    }

    private void fillCommon(final SourceInfo.Builder<?, ?> builder) {
        builder.setName(extractName());
        final var yangVersion = extractYangVersion();
        if (yangVersion != null) {
            builder.setYangVersion(yangVersion);
        }
        extractRevisions(builder);
        extractIncludes(builder);
        extractImports(builder);
    }
}
