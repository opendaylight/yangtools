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
import org.opendaylight.yangtools.yang.model.spi.meta.StatementDeclarations;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorMalformedArgumentException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorMissingArgumentException;

/**
 * Utility class for extract {@link SourceInfo} from a {@link YangIRSource}.
 */
abstract sealed class YangIRSourceInfoExtractor implements SourceInfo.Extractor {
    static final class ForModule extends YangIRSourceInfoExtractor {
        private static final String NAMESPACE = YangStmtMapping.NAMESPACE.getStatementName().getLocalName();

        @NonNullByDefault
        ForModule(final SourceIdentifier sourceId, final IRStatement root) {
            super(sourceId, root);
        }

        @Override
        public SourceInfo.Module extractSourceInfo() throws ExtractorException {
            final var builder = SourceInfo.Module.builder();
            fillBuilder(builder);
            return builder
                .setNamespace(root.statements().stream()
                    .filter(stmt -> isStatement(stmt, NAMESPACE))
                    .findFirst()
                    .map(stmt -> safeStringArgument(stmt, "namespace argument"))
                    .map(XMLNamespace::of)
                    .orElseThrow(() -> new IllegalArgumentException("No namespace statement in " + refOf(root))))
                .setPrefix(extractPrefix(root))
                .build();
        }
    }

    static final class ForSubmodule extends YangIRSourceInfoExtractor {
        private static final String BELONGS_TO = YangStmtMapping.BELONGS_TO.getStatementName().getLocalName();

        @NonNullByDefault
        ForSubmodule(final SourceIdentifier sourceId, final IRStatement root) {
            super(sourceId, root);
        }

        @Override
        public SourceInfo.Submodule extractSourceInfo() throws ExtractorException {
            final var builder = SourceInfo.Submodule.builder();
            fillBuilder(builder);
            return builder
                .setBelongsTo(root.statements().stream()
                    .filter(stmt -> isStatement(stmt, BELONGS_TO))
                    .findFirst()
                    .map(stmt -> new BelongsTo(Unqualified.of(safeStringArgument(stmt, "belongs-to module name")),
                        extractPrefix(stmt)))
                    .orElseThrow(() -> new IllegalArgumentException("No belongs-to statement in " + refOf(root))))
                .build();
        }
    }

    private static final String IMPORT = YangStmtMapping.IMPORT.getStatementName().getLocalName();
    private static final String INCLUDE = YangStmtMapping.INCLUDE.getStatementName().getLocalName();
    private static final String PREFIX = YangStmtMapping.PREFIX.getStatementName().getLocalName();
    private static final String REVISION = YangStmtMapping.REVISION.getStatementName().getLocalName();
    private static final String REVISION_DATE = YangStmtMapping.REVISION_DATE.getStatementName().getLocalName();
    private static final String YANG_VERSION = YangStmtMapping.YANG_VERSION.getStatementName().getLocalName();

    private final @NonNull SourceIdentifier sourceId;
    final @NonNull IRStatement root;

    @NonNullByDefault
    YangIRSourceInfoExtractor(final SourceIdentifier sourceId, final IRStatement root) {
        this.sourceId = requireNonNull(sourceId);
        this.root = requireNonNull(root);
    }

    final void fillBuilder(final SourceInfo.Builder<?, ?> builder) throws ExtractorException {
        builder.setName(Unqualified.of(safeStringArgument(root)));

        root.statements().stream()
            .filter(stmt -> isStatement(stmt, YANG_VERSION))
            .findFirst()
            .map(stmt -> {
                try {
                    return safeStringArgument(stmt);
                } catch (ExtractorException e) {
                    throw e.toUnchecked();
                }
            })
            .map(YangVersion::forString)
            .ifPresent(builder::setYangVersion);

        for (var stmt : root.statements()) {
            if (isStatement(stmt, IMPORT)) {
                builder.addImport(new Import(Unqualified.of(safeStringArgument(stmt)),
                    extractPrefix(stmt), extractRevisionDate(stmt)));
            } else if (isStatement(stmt, INCLUDE)) {
                builder.addInclude(new Include(Unqualified.of(safeStringArgument(stmt)),
                    extractRevisionDate(stmt)));
            } else if (isStatement(stmt, REVISION)) {
                builder.addRevision(Revision.of(safeStringArgument(stmt)));
            }
        }
    }

    final @NonNull Unqualified extractPrefix(final IRStatement stmt) throws ExtractorException {
        for (var subStmt : stmt.statements()) {
            if (isStatement(subStmt, PREFIX)) {
                return Unqualified.of(safeStringArgument(subStmt));
            }
        }
        throw new ExtractorMissingArgumentException(refOf(stmt), "prefix");
    }

    private @Nullable Revision extractRevisionDate(final IRStatement stmt) throws ExtractorException {
        for (var subStmt : stmt.statements()) {
            if (isStatement(subStmt, REVISION_DATE)) {
                return Revision.of(safeStringArgument(subStmt));
            }
        }
        return null;
    }

    private static boolean isStatement(final IRStatement stmt, final String name) {
        return stmt.keyword() instanceof IRKeyword.Unqualified keyword && name.equals(keyword.identifier());
    }

    final @NonNull String safeStringArgument(final IRStatement stmt) throws ExtractorException {
        final var arg = stmt.argument();
        if (arg == null) {
            throw new ExtractorMissingArgumentException(refOf(stmt), stmt.keyword().asStringDeclaration());
        }

        try {
            // TODO: we probably need to understand yang version first....
            return arg.asString(StringEscaping.RFC6020);
        } catch (ParseException e) {
            throw new ExtractorMalformedArgumentException(refOf(stmt), stmt.keyword().asStringDeclaration(), e);
        }
    }

    final StatementDeclaration.@NonNull InText refOf(final IRStatement stmt) {
        return StatementDeclarations.inText(sourceId.name().getLocalName(), stmt.startLine(), stmt.startColumn() + 1);
    }
}
