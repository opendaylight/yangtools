package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

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
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Referenced;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.meta.StatementDeclarations;
import org.opendaylight.yangtools.yang.model.spi.source.DetailedRevision;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;

public final class YangSourceInfoExtr extends SourceInfoExtractor<IRStatement> {
    @Override
    String extractRootType(final IRStatement root) {
        final var keyword = root.keyword();
        if (!(keyword instanceof IRKeyword.Unqualified)) {
            throw new IllegalArgumentException("Invalid root statement " + keyword);
        }
        return keyword.identifier();
    }

    @Override
    Referenced<Unqualified> extractPrefix(final IRStatement root, final SourceIdentifier rootId) {
        return root.statements().stream()
            .filter(stmt -> isStatement(stmt, PREFIX))
            .findFirst()
            .map(stmt -> new Referenced<>(Unqualified.of(
                safeStringArgument(rootId, stmt, "prefix argument")), refOf(rootId, stmt)))
            .orElseThrow(() -> new IllegalArgumentException("No prefix statement in " + refOf(rootId, root)));
    }

    @Override
    Referenced<XMLNamespace> extractNamespace(final IRStatement root, final SourceIdentifier rootId) {
        return root.statements().stream()
            .filter(stmt -> isStatement(stmt, NAMESPACE))
            .findFirst()
            .map(stmt -> new Referenced<>(safeStringArgument(rootId, stmt, "namespace argument"),
                refOf(rootId, stmt)))
            .map(referenced -> new Referenced<>(XMLNamespace.of(referenced.value()),
                referenced.reference()))
            .orElseThrow(() -> new IllegalArgumentException("No namespace statement in " + refOf(rootId, root)));
    }

    @Override
    SourceDependency.BelongsTo extractBelongsTo(final IRStatement root, final SourceIdentifier rootId) {
        return root.statements().stream()
            .filter(stmt -> isStatement(stmt, BELONGS_TO))
            .findFirst()
            .map(stmt -> new SourceDependency.BelongsTo(new Referenced<>(Unqualified.of(
                safeStringArgument(rootId, stmt, "belongs-to module name")), refOf(rootId, stmt)),
                extractPrefix(stmt, rootId)))
            .orElseThrow(() -> new IllegalArgumentException("No belongs-to statement in " + refOf(rootId, root)));
    }

    @Override
    Referenced<Unqualified> extractName(final IRStatement root, final SourceIdentifier rootId) {
        return new Referenced<>(Unqualified.of(safeStringArgument(rootId, root, "module/submodule argument")),
            refOf(rootId, root));
    }

    @Override
    Referenced<YangVersion> extractYangVersion(final IRStatement root, final SourceIdentifier rootId) {
        return root.statements().stream()
            .filter(stmt -> isStatement(stmt, YANG_VERSION))
            .findFirst()
            .map(stmt -> new Referenced<>(safeStringArgument(rootId, stmt, "yang-version argument"),
                refOf(rootId, stmt)))
            .map(refStr -> new Referenced<>(YangVersion.forString(refStr.value()), refStr.reference()))
            .orElse(null);
    }

    @Override
    void fillRevisions(final SourceInfo.Builder<?, ?> builder, final IRStatement root, final SourceIdentifier rootId) {
        root.statements().stream()
            .filter(stmt -> isStatement(stmt, REVISION))
            .map(stmt -> new DetailedRevision(
                new Referenced<>(Revision.of(safeStringArgument(rootId, stmt, "revision argument")),
                    refOf(rootId, stmt)),
                extractSimple(stmt, rootId, "description"),
                extractSimple(stmt, rootId, "reference")))
            .forEach(builder::addRevision);
    }

    @Override
    void fillIncludes(final SourceInfo.Builder<?, ?> builder, final IRStatement root, final SourceIdentifier rootId) {
        root.statements().stream()
            .filter(stmt -> isStatement(stmt, INCLUDE))
            .map(stmt -> new SourceDependency.Include(
                new Referenced<>(Unqualified.of(safeStringArgument(rootId, stmt, "include argument")),
                    refOf(rootId, stmt)),
                extractRevisionDate(stmt, rootId),
                extractSimple(stmt, rootId, "description"),
                extractSimple(stmt, rootId, "reference") ))
            .forEach(builder::addInclude);
    }

    @Override
    void fillImports(final SourceInfo.Builder<?, ?> builder, final IRStatement root, final SourceIdentifier rootId) {
        root.statements().stream()
            .filter(stmt -> isStatement(stmt, IMPORT))
            .map(stmt -> new SourceDependency.Import(
                new Referenced<>(Unqualified.of(safeStringArgument(rootId, stmt, "import argument")),
                    refOf(rootId, stmt)),
                extractPrefix(stmt, rootId),
                extractRevisionDate(stmt, rootId),
                extractSimple(stmt, rootId, "description"),
                extractSimple(stmt, rootId, "reference")))
            .forEach(builder::addImport);
    }

    private static boolean isStatement(final IRStatement stmt, final String name) {
        return stmt.keyword() instanceof IRKeyword.Unqualified keyword && name.equals(keyword.identifier());
    }

    private static StatementDeclaration.InText refOf(final SourceIdentifier source, final IRStatement stmt) {
        return StatementDeclarations.inText(source.name().getLocalName(), stmt.startLine(), stmt.startColumn() + 1);
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

    private static @Nullable Referenced<String> extractSimple(final IRStatement root, final SourceIdentifier sourceId,
        final String nodeName) {
        return root.statements().stream()
            .filter(stmt -> isStatement(stmt, nodeName))
            .findFirst()
            .map(stmt -> new Referenced<>(safeStringArgument(sourceId, stmt, nodeName),
                refOf(sourceId, stmt)))
            .orElse(null);
    }

    //TODO: should be probably promoted to the superclass
    private static @Nullable Referenced<Revision> extractRevisionDate(final IRStatement root,
        final SourceIdentifier sourceId) {
        return root.statements().stream()
            .filter(stmt -> isStatement(stmt, REVISION_DATE))
            .findFirst()
            .map(stmt -> new Referenced<>(Revision.of(safeStringArgument(sourceId, stmt, "revision date argument")),
                refOf(sourceId, stmt)))
            .orElse(null);
    }
}
