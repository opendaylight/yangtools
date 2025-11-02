/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yangtools.yang.model.api.meta.StatementDeclaration;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.spi.meta.StatementDeclarations;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.parser.antlr.YangTextParser;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;

public final class YangIRSourceInfoExtractor extends SourceInfoExtractor<IRStatement> {

    public YangIRSourceInfoExtractor(final IRStatement root, final SourceIdentifier rootIdentifier) {
        super(root, rootIdentifier);
    }

    public static @NonNull SourceInfo forIR(final YangIRSource source) {
        return forIR(source.statement(), source.sourceId());
    }

    /**
     * Extracts {@link SourceInfo} from an intermediate representation root statement of a YANG model.
     *
     * @param root root statement
     * @param rootIdentifier Source identifier, perhaps guessed from input name
     * @return {@link SourceInfo}
     * @throws IllegalArgumentException If the root statement is not a valid YANG module/submodule
     */
    public static SourceInfo forIR(final IRStatement root, final SourceIdentifier rootIdentifier) {
        return new YangIRSourceInfoExtractor(root, rootIdentifier).getSourceInfo();
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

    @Override
    String extractRootType() {
        final var keyword = root().keyword();
        if (!(keyword instanceof IRKeyword.Unqualified)) {
            throw new IllegalArgumentException("Invalid root statement " + keyword);
        }
        return keyword.identifier();
    }

    @Override
    Unqualified extractModulePrefix() {
        return root().statements().stream()
            .filter(stmt -> isStatement(stmt, PREFIX))
            .findFirst()
            .map(stmt -> Unqualified.of(
                safeStringArgument(rootId(), stmt, "prefix argument")))
            .orElseThrow(() -> new IllegalArgumentException("No prefix statement in " + refOf(rootId(), root())));
    }

    private Unqualified extractPrefix(final IRStatement parentStmt, final SourceIdentifier sourceId) {
        return parentStmt.statements().stream()
            .filter(stmt -> isStatement(stmt, PREFIX))
            .findFirst()
            .map(stmt -> Unqualified.of(safeStringArgument(sourceId, stmt, "prefix argument")))
            .orElseThrow(() -> new IllegalArgumentException("No prefix statement in " + refOf(sourceId, parentStmt)));
    }

    @Override
    XMLNamespace extractNamespace() {
        return root().statements().stream()
            .filter(stmt -> isStatement(stmt, NAMESPACE))
            .findFirst()
            .map(stmt -> XMLNamespace.of(safeStringArgument(rootId(), stmt, "namespace argument")))
            .orElseThrow(() -> new IllegalArgumentException("No namespace statement in " + refOf(rootId(), root())));
    }

    @Override
    SourceDependency.BelongsTo extractBelongsTo() {
        return root().statements().stream()
            .filter(stmt -> isStatement(stmt, BELONGS_TO))
            .findFirst()
            .map(stmt -> new SourceDependency.BelongsTo(Unqualified.of(
                safeStringArgument(rootId(), stmt, "belongs-to module name")), extractPrefix(stmt, rootId())))
            .orElseThrow(() -> new IllegalArgumentException("No belongs-to statement in " + refOf(rootId(), root())));
    }

    @Override
    Unqualified extractName() {
        return Unqualified.of(safeStringArgument(rootId(), root(), "module/submodule argument"));
    }

    @Override
    YangVersion extractYangVersion() {
        return root().statements().stream()
            .filter(stmt -> isStatement(stmt, YANG_VERSION))
            .findFirst()
            .map(stmt -> YangVersion.forString(safeStringArgument(rootId(), stmt, "yang-version argument")))
            .orElse(null);
    }

    @Override
    void extractRevisions(final SourceInfo.Builder<?, ?> builder) {
        root().statements().stream()
            .filter(stmt -> isStatement(stmt, REVISION))
            .map(stmt -> Revision.of(safeStringArgument(rootId(), stmt, "revision argument")))
            .forEach(builder::addRevision);
    }

    @Override
    void extractIncludes(final SourceInfo.Builder<?, ?> builder) {
        root().statements().stream()
            .filter(stmt -> isStatement(stmt, INCLUDE))
            .map(stmt -> new SourceDependency.Include(
                Unqualified.of(safeStringArgument(rootId(), stmt, "include argument")),
                extractRevisionDate(stmt, rootId())))
            .forEach(builder::addInclude);
    }

    @Override
    void extractImports(final SourceInfo.Builder<?, ?> builder) {
        root().statements().stream()
            .filter(stmt -> isStatement(stmt, IMPORT))
            .map(stmt -> new SourceDependency.Import(
                Unqualified.of(safeStringArgument(rootId(), stmt, "import argument")),
                extractPrefix(stmt, rootId()),
                extractRevisionDate(stmt, rootId())))
            .forEach(builder::addImport);
    }

    private @Nullable Revision extractRevisionDate(final IRStatement parentStmt,
        final SourceIdentifier sourceId) {
        return parentStmt.statements().stream()
            .filter(stmt -> isStatement(stmt, REVISION_DATE))
            .findFirst()
            .map(stmt -> Revision.of(safeStringArgument(sourceId, stmt, "revision date argument")))
            .orElse(null);
    }

    private boolean isStatement(final IRStatement stmt, final String name) {
        return stmt.keyword() instanceof IRKeyword.Unqualified keyword && name.equals(keyword.identifier());
    }

    private StatementDeclaration.InText refOf(final SourceIdentifier source, final IRStatement stmt) {
        return StatementDeclarations.inText(source.name().getLocalName(), stmt.startLine(), stmt.startColumn() + 1);
    }

    private @NonNull String safeStringArgument(final SourceIdentifier source, final IRStatement stmt,
        final String desc) {
        final var ref = refOf(source, stmt);
        final var arg = stmt.argument();
        if (arg == null) {
            throw new IllegalArgumentException("Missing " + desc + " at " + ref);
        }

        // TODO: we probably need to understand yang version first....
        return ArgumentContextUtils.rfc6020().stringFromStringContext(arg, ref);
    }
}
