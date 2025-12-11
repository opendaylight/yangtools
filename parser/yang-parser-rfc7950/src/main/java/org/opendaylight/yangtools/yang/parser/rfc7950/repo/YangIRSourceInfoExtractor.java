/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
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
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDeclaration;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.BelongsTo;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.spi.meta.StatementDeclarations;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.parser.antlr.YangTextParser;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Utility class for extract {@link SourceInfo} from a {@link YangIRSource}.
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

    private final @NonNull IRStatement root;
    private final @NonNull SourceIdentifier sourceId;

    @NonNullByDefault
    private YangIRSourceInfoExtractor(final IRStatement root, final SourceIdentifier sourceId) {
        this.root = requireNonNull(root);
        this.sourceId = requireNonNull(sourceId);
    }

    /**
     * Extracts {@link SourceInfo} from an intermediate representation root statement of a YANG model.
     *
     * @param source Schema source
     * @return {@link SourceInfo}
     * @throws IllegalArgumentException If the root statement is not a valid YANG module/submodule
     */
    public static @NonNull SourceInfo forIR(final YangIRSource source) {
        return forIR(source.statement(), source.sourceId());
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

        final var extractor = new YangIRSourceInfoExtractor(rootStatement, sourceId);
        final String arg = keyword.identifier();
        if (MODULE.equals(arg)) {
            return extractor.moduleForIR();
        }
        if (SUBMODULE.equals(arg)) {
            return extractor.submmoduleForIR();
        }
        throw new IllegalArgumentException("Root of parsed AST must be either module or submodule");
    }

    /**
     * Extracts {@link SourceInfo} from a {@link YangTextSource}. This parsing does not validate full YANG module, only
     * parses header up to the revisions and imports.
     *
     * @param yangText {@link YangTextSource}
     * @return {@link SourceInfo}
     * @throws IOException When the resource cannot be read
     * @throws YangSyntaxErrorException If the resource does not pass syntactic analysis
     */
    public static SourceInfo forYangText(final YangTextSource yangText) throws IOException, YangSyntaxErrorException {
        final var sourceId = yangText.sourceId();
        return forIR(YangTextParser.parseToIR(yangText), sourceId);
    }

    private SourceInfo.@NonNull Module moduleForIR() {
        final var builder = SourceInfo.Module.builder();
        fill(builder);
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

    private SourceInfo.@NonNull Submodule submmoduleForIR() {
        final var builder = SourceInfo.Submodule.builder();
        fill(builder);
        return builder
            .setBelongsTo(root.statements().stream()
                .filter(stmt -> isStatement(stmt, BELONGS_TO))
                .findFirst()
                .map(stmt -> new BelongsTo(Unqualified.of(safeStringArgument(stmt, "belongs-to module name")),
                    extractPrefix(stmt)))
                .orElseThrow(() -> new IllegalArgumentException("No belongs-to statement in " + refOf(root))))
            .build();
    }

    private void fill(final SourceInfo.Builder<?, ?> builder) {
        builder.setName(Unqualified.of(safeStringArgument(root, "module/submodule argument")));

        root.statements().stream()
            .filter(stmt -> isStatement(stmt, YANG_VERSION))
            .findFirst()
            .map(stmt -> safeStringArgument(stmt, "yang-version argument"))
            .map(YangVersion::forString)
            .ifPresent(builder::setYangVersion);

        root.statements().stream()
            .filter(stmt -> isStatement(stmt, REVISION))
            .map(stmt -> Revision.of(safeStringArgument(stmt, "revision argument")))
            .forEach(builder::addRevision);

        root.statements().stream()
            .filter(stmt -> isStatement(stmt, IMPORT))
            .map(stmt -> new Import(Unqualified.of(safeStringArgument(stmt, "import argument")),
                extractPrefix(stmt), extractRevisionDate(stmt)))
            .forEach(builder::addImport);

        root.statements().stream()
            .filter(stmt -> isStatement(stmt, INCLUDE))
            .map(stmt -> new Include(Unqualified.of(safeStringArgument(stmt, "include argument")),
                extractRevisionDate(stmt)))
            .forEach(builder::addInclude);
    }

    private @NonNull Unqualified extractPrefix(final IRStatement stmt) {
        return stmt.statements().stream()
            .filter(subStmt -> isStatement(subStmt, PREFIX))
            .findFirst()
            .map(subStmt -> Unqualified.of(safeStringArgument(subStmt, "prefix argument")))
            .orElseThrow(() -> new IllegalArgumentException("No prefix statement in " + refOf(stmt)));
    }

    private @Nullable Revision extractRevisionDate(final IRStatement stmt) {
        return stmt.statements().stream()
            .filter(subStmt -> isStatement(subStmt, REVISION_DATE))
            .findFirst()
            .map(subStmt -> Revision.of(safeStringArgument(subStmt, "revision date argument")))
            .orElse(null);
    }

    private static boolean isStatement(final IRStatement stmt, final String name) {
        return stmt.keyword() instanceof IRKeyword.Unqualified keyword && name.equals(keyword.identifier());
    }

    private @NonNull String safeStringArgument(final IRStatement stmt, final String desc) {
        final var arg = stmt.argument();
        if (arg == null) {
            throw new IllegalArgumentException("Missing " + desc + " at " + refOf(stmt));
        }

        try {
            // TODO: we probably need to understand yang version first....
            return ArgumentContextUtils.RFC6020.stringFromStringContext(arg);
        } catch (ParseException e) {
            throw new SourceException(e.getMessage(), refOf(stmt), e);
        }
    }

    private StatementDeclaration.InText refOf(final IRStatement stmt) {
        return StatementDeclarations.inText(sourceId.name().getLocalName(), stmt.startLine(), stmt.startColumn() + 1);
    }
}
