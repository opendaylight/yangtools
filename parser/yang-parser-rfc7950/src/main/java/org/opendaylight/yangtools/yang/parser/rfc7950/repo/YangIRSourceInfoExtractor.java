/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.ir.IRKeyword;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDeclaration;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.BelongsTo;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.meta.StatementDeclarations;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSchemaSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;

/**
 * Utility class for extract {@link SourceInfo} from a {@link YangIRSchemaSource}.
 */
public final class YangIRSourceInfoExtractor {
    private static final String BELONGS_TO = YangStmtMapping.BELONGS_TO.getStatementName().getLocalName();
    private static final String IMPORT = YangStmtMapping.IMPORT.getStatementName().getLocalName();
    private static final String INCLUDE = YangStmtMapping.INCLUDE.getStatementName().getLocalName();
    private static final String MODULE = YangStmtMapping.MODULE.getStatementName().getLocalName();
    private static final String NAMESPACE = YangStmtMapping.NAMESPACE.getStatementName().getLocalName();
    private static final String PREFIX = YangStmtMapping.PREFIX.getStatementName().getLocalName();
    private static final String REVISION = YangStmtMapping.REVISION.getStatementName().getLocalName();
    private static final String REVISION_DATE = YangStmtMapping.REVISION_DATE.getStatementName().getLocalName();
    private static final String SUBMODULE = YangStmtMapping.SUBMODULE.getStatementName().getLocalName();
    private static final String YANG_VERSION = YangStmtMapping.YANG_VERSION.getStatementName().getLocalName();

    private YangIRSourceInfoExtractor() {
        // Hidden on purpose
    }

    /**
     * Extracts {@link SourceInfo} from an intermediate representation root statement of a YANG model.
     *
     * @param source Schema source
     * @return {@link SourceInfo}
     * @throws IllegalArgumentException If the root statement is not a valid YANG module/submodule
     */
    public static @NonNull SourceInfo forIR(final YangIRSchemaSource source) {
        return forIR(source.rootStatement(), source.sourceId());
    }

    /**
     * Extracts {@link SourceInfo} from an intermediate representation root statement of a YANG model.
     *
     * @param sourceId Source identifier, perhaps guessed from input name
     * @param rootStatement root statement
     * @return {@link SourceInfo}
     * @throws IllegalArgumentException If the root statement is not a valid YANG module/submodule
     */
    public static @NonNull SourceInfo forIR(final IRStatement rootStatement, final SourceIdentifier sourceId) {
        final var keyword = rootStatement.keyword();
        if (!(keyword instanceof IRKeyword.Unqualified)) {
            throw new IllegalArgumentException("Invalid root statement " + keyword);
        }

        final String arg = keyword.identifier();
        if (MODULE.equals(arg)) {
            return moduleForIR(rootStatement, sourceId);
        }
        if (SUBMODULE.equals(arg)) {
            return submmoduleForIR(rootStatement, sourceId);
        }
        throw new IllegalArgumentException("Root of parsed AST must be either module or submodule");
    }

    /**
     * Extracts {@link SourceInfo} from a {@link YangTextSource}. This parsing does not validate full YANG module, only
     * parses header up to the revisions and imports.
     *
     * @param yangText {@link YangTextSource}
     * @return {@link SourceInfo}
     * @throws YangSyntaxErrorException If the resource does not pass syntactic analysis
     * @throws IOException When the resource cannot be read
     */
    public static SourceInfo forYangText(final YangTextSource yangText)
            throws IOException, YangSyntaxErrorException {
        final var source = YangStatementStreamSource.create(yangText);
        return forIR(source.rootStatement(), source.getIdentifier());
    }

    private static SourceInfo.@NonNull Module moduleForIR(final IRStatement root, final SourceIdentifier sourceId) {
        final var builder = SourceInfo.Module.builder();
        fill(builder, root, sourceId);
        return builder
            .setNamespace(root.statements().stream()
                .filter(stmt -> isStatement(stmt, NAMESPACE))
                .findFirst()
                .map(stmt -> safeStringArgument(sourceId, stmt, "namespace argument"))
                .map(XMLNamespace::of)
                .orElseThrow(() -> new IllegalArgumentException("No namespace statement in " + refOf(sourceId, root))))
            .setPrefix(extractPrefix(root, sourceId))
            .build();
    }

    private static SourceInfo.@NonNull Submodule submmoduleForIR(final IRStatement root,
            final SourceIdentifier sourceId) {
        final var builder = SourceInfo.Submodule.builder();
        fill(builder, root, sourceId);
        return builder
            .setBelongsTo(root.statements().stream()
                .filter(stmt -> isStatement(stmt, BELONGS_TO))
                .findFirst()
                .map(stmt -> new BelongsTo(Unqualified.of(safeStringArgument(sourceId, stmt, "belongs-to module name")),
                    extractPrefix(stmt, sourceId)))
                .orElseThrow(() -> new IllegalArgumentException("No belongs-to statement in " + refOf(sourceId, root))))
            .build();
    }

    private static void fill(final SourceInfo.Builder<?, ?> builder, final IRStatement root,
            final SourceIdentifier sourceId) {
        builder.setName(Unqualified.of(safeStringArgument(sourceId, root, "module/submodule argument")));

        root.statements().stream()
            .filter(stmt -> isStatement(stmt, YANG_VERSION))
            .findFirst()
            .map(stmt -> safeStringArgument(sourceId, stmt, "yang-version argument"))
            .map(YangVersion::forString)
            .ifPresent(builder::setYangVersion);

        root.statements().stream()
            .filter(stmt -> isStatement(stmt, REVISION))
            .map(stmt -> Revision.of(safeStringArgument(sourceId, stmt, "revision argument")))
            .forEach(builder::addRevision);

        root.statements().stream()
            .filter(stmt -> isStatement(stmt, IMPORT))
            .map(stmt -> new Import(Unqualified.of(safeStringArgument(sourceId, stmt, "import argument")),
                extractPrefix(stmt, sourceId), extractRevisionDate(stmt, sourceId)))
            .forEach(builder::addImport);

        root.statements().stream()
            .filter(stmt -> isStatement(stmt, INCLUDE))
            .map(stmt -> new Include(Unqualified.of(safeStringArgument(sourceId, stmt, "include argument")),
                extractRevisionDate(stmt, sourceId)))
            .forEach(builder::addInclude);
    }

    private static @NonNull Unqualified extractPrefix(final IRStatement root, final SourceIdentifier sourceId) {
        return root.statements().stream()
            .filter(stmt -> isStatement(stmt, PREFIX))
            .findFirst()
            .map(stmt -> Unqualified.of(safeStringArgument(sourceId, stmt, "prefix argument")))
            .orElseThrow(() -> new IllegalArgumentException("No prefix statement in " + refOf(sourceId, root)));
    }

    private static @Nullable Revision extractRevisionDate(final IRStatement root, final SourceIdentifier sourceId) {
        return root.statements().stream()
            .filter(stmt -> isStatement(stmt, REVISION_DATE))
            .findFirst()
            .map(stmt -> Revision.of(safeStringArgument(sourceId, stmt, "revision date argument")))
            .orElse(null);
    }

    private static boolean isStatement(final IRStatement stmt, final String name) {
        return stmt.keyword() instanceof IRKeyword.Unqualified keyword && name.equals(keyword.identifier());
    }

    private static @NonNull String safeStringArgument(final SourceIdentifier source, final IRStatement stmt,
            final String desc) {
        final var ref = refOf(source, stmt);
        final var arg = stmt.argument();
        if (arg == null) {
            throw new IllegalArgumentException("Missing " + desc + " at " + ref);
        }

        // TODO: we probably need to understand yang version first....
        return ArgumentContextUtils.rfc6020().stringFromStringContext(arg, ref);
    }

    private static StatementDeclaration.InText refOf(final SourceIdentifier source, final IRStatement stmt) {
        return StatementDeclarations.inText(source.name().getLocalName(), stmt.startLine(), stmt.startColumn() + 1);
    }
}
