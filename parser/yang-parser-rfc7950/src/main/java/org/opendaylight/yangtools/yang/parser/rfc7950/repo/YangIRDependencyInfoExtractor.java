/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableSet;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName;
import org.opendaylight.yangtools.yang.ir.IRKeyword;
import org.opendaylight.yangtools.yang.ir.IRKeyword.Unqualified;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangModelDependencyInfo.ModuleDependencyInfo;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangModelDependencyInfo.SubmoduleDependencyInfo;
import org.opendaylight.yangtools.yang.parser.spi.source.ExplicitStatement;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

final class YangIRDependencyInfoExtractor {
    private static final String BELONGS_TO = YangStmtMapping.BELONGS_TO.getStatementName().getLocalName();
    private static final String IMPORT = YangStmtMapping.IMPORT.getStatementName().getLocalName();
    private static final String INCLUDE = YangStmtMapping.INCLUDE.getStatementName().getLocalName();
    private static final String MODULE = YangStmtMapping.MODULE.getStatementName().getLocalName();
    private static final String REVISION = YangStmtMapping.REVISION.getStatementName().getLocalName();
    private static final String REVISION_DATE = YangStmtMapping.REVISION_DATE.getStatementName().getLocalName();
    private static final String SUBMODULE = YangStmtMapping.SUBMODULE.getStatementName().getLocalName();
    private static final String YANG_VERSION = YangStmtMapping.YANG_VERSION.getStatementName().getLocalName();

    private YangIRDependencyInfoExtractor() {
        // Hidden on purpose
    }

    /**
     * Extracts {@link YangModelDependencyInfo} from an intermediate representation root statement of a YANG model.
     *
     * @param source Source identifier
     * @param rootStatement root statement
     * @return {@link YangModelDependencyInfo}
     * @throws IllegalArgumentException If the root statement is not a valid YANG module/submodule
     */
    static @NonNull YangModelDependencyInfo parseIR(final IRStatement rootStatement, final SourceIdentifier source) {
        final IRKeyword keyword = rootStatement.keyword();
        checkArgument(keyword instanceof Unqualified, "Invalid root statement %s", keyword);

        final String arg = keyword.identifier();
        if (MODULE.equals(arg)) {
            return parseModuleContext(rootStatement, source);
        }
        if (SUBMODULE.equals(arg)) {
            return parseSubmoduleContext(rootStatement, source);
        }
        throw new IllegalArgumentException("Root of parsed AST must be either module or submodule");
    }

    private static @NonNull YangModelDependencyInfo parseModuleContext(final IRStatement module,
            final SourceIdentifier source) {
        return new ModuleDependencyInfo(
            safeStringArgument(source, module, "module name"),
            latestRevision(module, source),
            imports(module, source),
            includes(module, source));
    }

    private static @NonNull YangModelDependencyInfo parseSubmoduleContext(final IRStatement submodule,
            final SourceIdentifier source) {
        return new SubmoduleDependencyInfo(
            safeStringArgument(source, submodule, "submodule name"),
            latestRevision(submodule, source),
            UnresolvedQName.Unqualified.of(parseBelongsTo(submodule, source)),
            imports(submodule, source),
            includes(submodule, source));
    }

    private static @Nullable String parseBelongsTo(final IRStatement submodule, final SourceIdentifier source) {
        return submodule.statements().stream()
            .filter(stmt -> isStatement(stmt, BELONGS_TO))
            .findFirst()
            .map(stmt -> safeStringArgument(source, stmt, "belongs-to module name"))
            .orElse(null);
    }

    private static ImmutableSet<ModuleImport> imports(final IRStatement module, final SourceIdentifier source) {
        return module.statements().stream()
            .filter(stmt -> isStatement(stmt, IMPORT))
            .map(stmt -> {
                final var revisionDateStr = revisionDateString(stmt, source);
                return new ModuleImportImpl(
                    UnresolvedQName.Unqualified.of(safeStringArgument(source, stmt, "imported module name")),
                    revisionDateStr != null ? Revision.of(revisionDateStr) : null);
            })
            .collect(ImmutableSet.toImmutableSet());
    }

    private static ImmutableSet<ModuleImport> includes(final IRStatement module, final SourceIdentifier source) {
        return module.statements().stream()
            .filter(stmt -> isStatement(stmt, INCLUDE))
            .map(stmt -> {
                final var revisionDateStr = revisionDateString(stmt, source);
                return new ModuleImportImpl(
                    UnresolvedQName.Unqualified.of(safeStringArgument(source, stmt, "included submodule name")),
                    revisionDateStr == null ? null : Revision.of(revisionDateStr));
            })
            .collect(ImmutableSet.toImmutableSet());
    }

    private static @Nullable String revisionDateString(final IRStatement rootStmt, final SourceIdentifier source) {
        return rootStmt.statements().stream()
            .filter(stmt -> isStatement(stmt, REVISION_DATE))
            .findFirst()
            .map(stmt -> safeStringArgument(source, stmt, "imported module revision-date"))
            .orElse(null);
    }

    static @Nullable String latestRevision(final IRStatement module, final SourceIdentifier source) {
        String latestRevision = null;
        for (var substatement : module.statements()) {
            if (isStatement(substatement, REVISION)) {
                final var currentRevision = safeStringArgument(source, substatement, "revision date");
                if (latestRevision == null || latestRevision.compareTo(currentRevision) < 0) {
                    latestRevision = currentRevision;
                }
            }
        }
        return latestRevision;
    }


    static String safeStringArgument(final SourceIdentifier source, final IRStatement stmt, final String desc) {
        final var ref = sourceRefOf(source, stmt);
        final var arg = stmt.argument();
        if (arg == null) {
            throw new IllegalArgumentException("Missing " + desc + " at " + ref);
        }

        // TODO: we probably need to understand yang version first....
        return ArgumentContextUtils.rfc6020().stringFromStringContext(arg, ref);
    }

    private static boolean isStatement(final IRStatement stmt, final String localName) {
        return stmt.keyword() instanceof Unqualified unqualified && localName.equals(unqualified.identifier());
    }

    private static StatementSourceReference sourceRefOf(final SourceIdentifier source, final IRStatement stmt) {
        return ExplicitStatement.atPosition(source.name().getLocalName(), stmt.startLine(), stmt.startColumn() + 1);
    }
}
