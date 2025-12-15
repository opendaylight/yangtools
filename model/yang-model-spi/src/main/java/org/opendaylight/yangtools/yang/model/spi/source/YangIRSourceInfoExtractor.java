/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2025 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static java.util.Objects.requireNonNull;

import java.text.ParseException;
import java.time.format.DateTimeParseException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.ir.IRKeyword;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.ir.StringEscaping;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDeclaration;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.BelongsTo;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorMalformedArgumentException;

/**
 * Utility class for extract {@link SourceInfo} from a {@link YangIRSource}.
 */
abstract sealed class YangIRSourceInfoExtractor implements SourceInfo.Extractor {
    static final class ForModule extends YangIRSourceInfoExtractor {
        private static final @NonNull String NAMESPACE = YangStmtMapping.NAMESPACE.getStatementName().getLocalName();

        @NonNullByDefault
        ForModule(final SourceIdentifier sourceId, final IRStatement root) {
            super(sourceId, root);
        }

        @Override
        public SourceInfo.Module extractSourceInfo() throws ExtractorException {
            final var builder = SourceInfo.Module.builder();
            fillBuilder(builder);
            return builder
                .setNamespace(extractNamespace())
                .setPrefix(extractPrefix(root))
                .build();
        }

        private @NonNull XMLNamespace extractNamespace() throws ExtractorException {
            for (var stmt : root.statements()) {
                if (isStatement(stmt, NAMESPACE)) {
                    final var arg = stringArgument(stmt);
                    try {
                        return XMLNamespace.of(arg);
                    } catch (IllegalArgumentException e) {
                        throw newInvalidArgument(stmt, e);
                    }
                }
            }
            throw newMissingSubstatement(root, NAMESPACE);
        }
    }

    static final class ForSubmodule extends YangIRSourceInfoExtractor {
        private static final @NonNull String BELONGS_TO = YangStmtMapping.BELONGS_TO.getStatementName().getLocalName();

        @NonNullByDefault
        ForSubmodule(final SourceIdentifier sourceId, final IRStatement root) {
            super(sourceId, root);
        }

        @Override
        public SourceInfo.Submodule extractSourceInfo() throws ExtractorException {
            final var builder = SourceInfo.Submodule.builder();
            fillBuilder(builder);
            return builder
                .setBelongsTo(extractBelongsTo())
                .build();
        }

        private @NonNull BelongsTo extractBelongsTo() throws ExtractorException {
            for (var stmt : root.statements()) {
                if (isStatement(stmt, BELONGS_TO)) {
                    return new BelongsTo(unqualifiedArgument(stmt), extractPrefix(stmt));
                }
            }
            throw newMissingSubstatement(root, BELONGS_TO);
        }
    }

    private static final @NonNull String IMPORT = YangStmtMapping.IMPORT.getStatementName().getLocalName();
    private static final @NonNull String INCLUDE = YangStmtMapping.INCLUDE.getStatementName().getLocalName();
    private static final @NonNull String PREFIX = YangStmtMapping.PREFIX.getStatementName().getLocalName();
    private static final @NonNull String REVISION = YangStmtMapping.REVISION.getStatementName().getLocalName();
    private static final @NonNull String REVISION_DATE =
        YangStmtMapping.REVISION_DATE.getStatementName().getLocalName();
    private static final @NonNull String YANG_VERSION = YangStmtMapping.YANG_VERSION.getStatementName().getLocalName();

    private final @NonNull SourceIdentifier sourceId;
    final @NonNull IRStatement root;

    @NonNullByDefault
    YangIRSourceInfoExtractor(final SourceIdentifier sourceId, final IRStatement root) {
        this.sourceId = requireNonNull(sourceId);
        this.root = requireNonNull(root);
    }

    final void fillBuilder(final SourceInfo.Builder<?, ?> builder) throws ExtractorException {
        builder
            .setYangVersion(extractYangVersion())
            .setName(unqualifiedArgument(root));

        for (var stmt : root.statements()) {
            if (isStatement(stmt, IMPORT)) {
                builder.addImport(new Import(unqualifiedArgument(stmt), extractPrefix(stmt),
                    extractRevisionDate(stmt)));
            } else if (isStatement(stmt, INCLUDE)) {
                builder.addInclude(new Include(unqualifiedArgument(stmt), extractRevisionDate(stmt)));
            } else if (isStatement(stmt, REVISION)) {
                builder.addRevision(revisionArgument(stmt));
            }
        }
    }

    private @NonNull YangVersion extractYangVersion() throws ExtractorException {
        for (var stmt : root.statements()) {
            if (isStatement(stmt, YANG_VERSION)) {
                final var arg = stringArgument(stmt);
                try {
                    return YangVersion.ofString(arg);
                } catch (IllegalArgumentException e) {
                    throw newInvalidArgument(stmt, e);
                }
            }
        }
        return YangVersion.VERSION_1;
    }

    final @NonNull Unqualified extractPrefix(final IRStatement stmt) throws ExtractorException {
        for (var subStmt : stmt.statements()) {
            if (isStatement(subStmt, PREFIX)) {
                return unqualifiedArgument(subStmt);
            }
        }
        throw newMissingSubstatement(stmt, PREFIX);
    }

    private @Nullable Revision extractRevisionDate(final IRStatement stmt) throws ExtractorException {
        for (var subStmt : stmt.statements()) {
            if (isStatement(subStmt, REVISION_DATE)) {
                return revisionArgument(subStmt);
            }
        }
        return null;
    }

    private static boolean isStatement(final IRStatement stmt, final String name) {
        return stmt.keyword() instanceof IRKeyword.Unqualified keyword && name.equals(keyword.identifier());
    }

    @NonNullByDefault
    final Revision revisionArgument(final IRStatement stmt) throws ExtractorException {
        final var arg = stringArgument(stmt);
        try {
            return Revision.of(arg);
        } catch (DateTimeParseException e) {
            throw newInvalidArgument(stmt, e);
        }
    }

    @NonNullByDefault
    final String stringArgument(final IRStatement stmt) throws ExtractorException {
        final var arg = stmt.argument();
        if (arg == null) {
            throw new ExtractorException(refOf(stmt), "Missing argument to " + stmt.keyword().asStringDeclaration());
        }

        try {
            // TODO: we probably need to understand yang version first....
            return arg.asString(StringEscaping.RFC6020);
        } catch (ParseException e) {
            throw new ExtractorMalformedArgumentException(refOf(stmt), stmt.keyword().asStringDeclaration(), e);
        }
    }

    @NonNullByDefault
    final Unqualified unqualifiedArgument(final IRStatement stmt) throws ExtractorException {
        final var arg = stringArgument(stmt);
        try {
            return Unqualified.of(arg);
        } catch (IllegalArgumentException e) {
            throw newInvalidArgument(stmt, e);
        }
    }

    @NonNullByDefault
    final ExtractorException newInvalidArgument(final IRStatement stmt, final Exception cause) {
        return new ExtractorException(refOf(stmt),
            "Invalid argument to " + stmt.keyword().asStringDeclaration() + ": " + cause.getMessage(), cause);
    }

    @NonNullByDefault
    final ExtractorException newMissingSubstatement(final IRStatement parent, final String keyword) {
        return new ExtractorException(refOf(parent), "Missing " + keyword + " substatement");
    }

    @NonNullByDefault
    final StatementDeclaration.InText refOf(final IRStatement stmt) {
        return YangIRSource.refOf(sourceId, stmt);
    }
}
