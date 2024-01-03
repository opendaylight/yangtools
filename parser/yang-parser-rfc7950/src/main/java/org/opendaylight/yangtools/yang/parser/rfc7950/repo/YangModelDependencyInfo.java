/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.ir.IRKeyword;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.ir.YangIRSchemaSource;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.ModuleSourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.Import;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.Include;
import org.opendaylight.yangtools.yang.model.spi.source.SubmoduleSourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.source.ExplicitStatement;

/**
 * Helper transfer object which holds basic and dependency information for YANG
 * model.
 *
 * <p>
 * There are two concrete implementations of this interface:
 * <ul>
 * <li>{@link ModuleDependencyInfo} - Dependency information for module</li>
 * <li>{@link SubmoduleDependencyInfo} - Dependency information for submodule</li>
 * </ul>
 *
 * @see ModuleDependencyInfo
 * @see SubmoduleDependencyInfo
 */
public abstract sealed class YangModelDependencyInfo {
    /**
     * Dependency information for a YANG module.
     */
    public static final class ModuleDependencyInfo extends YangModelDependencyInfo {
        private ModuleDependencyInfo(final String name, final Revision revision,
                final ImmutableSet<Import> imports, final ImmutableSet<Include> includes) {
            super(name, revision, imports, includes);
        }
    }

    /**
     * Dependency information for a YANG submodule, also provides name for parent module.
     */
    public static final class SubmoduleDependencyInfo extends YangModelDependencyInfo {
        private final @NonNull Unqualified belongsTo;

        private SubmoduleDependencyInfo(final String name, final Revision revision, final Unqualified belongsTo,
                final ImmutableSet<Import> imports, final ImmutableSet<Include> includes) {
            super(name, revision, imports, includes);
            this.belongsTo = requireNonNull(belongsTo);
        }

        /**
         * Returns name of parent module.
         *
         * @return The module this info belongs to
         */
        public @NonNull Unqualified getParentModule() {
            return belongsTo;
        }
    }

    public record Dependency(@NonNull Unqualified name, @Nullable Revision revision) {
        public Dependency {
            requireNonNull(name);
        }
    }

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

    private final @NonNull String name;
    private final @Nullable Revision revision;
    private final @NonNull ImmutableSet<SourceIdentifier> dependencies;

    YangModelDependencyInfo(final String name, final Revision revision, final ImmutableSet<Import> imports,
            final ImmutableSet<Include> includes) {
        this.name = requireNonNull(name);
        this.revision = revision;

        dependencies = Streams.concat(
            imports.stream().map(spec -> new SourceIdentifier(spec.name(), spec.revision())),
            includes.stream().map(spec -> new SourceIdentifier(spec.name(), spec.revision())))
            .collect(ImmutableSet.toImmutableSet());
    }

    /**
     * Returns model name.
     *
     * @return model name
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns formatted revision string.
     *
     * @return formatted revision string, or {@code null}
     */
    public final @Nullable String getFormattedRevision() {
        final var local = revision;
        return local != null ? local.toString() : null;
    }

    /**
     * Returns revision.
     *
     * @return revision, potentially null
     */
    public final Optional<Revision> getRevision() {
        return Optional.ofNullable(revision);
    }

    /**
     * Returns immutable collection of all source dependencies. This collection contains both {@code import} statements
     * and {@code include} statements..
     *
     * @return Immutable collection of dependencies..
     */
    public final ImmutableSet<SourceIdentifier> getDependencies() {
        return dependencies;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(name, revision);
    }

    @Override
    public final boolean equals(final Object obj) {
        return this == obj || obj instanceof YangModelDependencyInfo other
            && Objects.equals(name, other.name) && Objects.equals(revision, other.revision);
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
            .add("name", getName())
            .add("revision", getRevision())
            .add("dependencies", getDependencies())
            .toString();
    }

    /**
     * Extracts {@link YangModelDependencyInfo} from an intermediate representation root statement of a YANG model.
     *
     * @param source Schema source
     * @return {@link YangModelDependencyInfo}
     * @throws IllegalArgumentException If the root statement is not a valid YANG module/submodule
     */
    public static @NonNull YangModelDependencyInfo forIR(final YangIRSchemaSource source) {
        return forIR(source.getRootStatement(), source.sourceId());
    }

    /**
     * Extracts {@link YangModelDependencyInfo} from an intermediate representation root statement of a YANG model.
     *
     * @param sourceId Source identifier
     * @param rootStatement root statement
     * @return {@link YangModelDependencyInfo}
     * @throws IllegalArgumentException If the root statement is not a valid YANG module/submodule
     */
    static @NonNull YangModelDependencyInfo forIR(final IRStatement rootStatement,
            final SourceIdentifier sourceId) {
        final var keyword = rootStatement.keyword();
        if (!(keyword instanceof IRKeyword.Unqualified)) {
            throw new IllegalArgumentException("Invalid root statement " + keyword);
        }

        final String arg = keyword.identifier();
        if (MODULE.equals(arg)) {
            return forSourceInfo(moduleForIR(rootStatement, sourceId));
        }
        if (SUBMODULE.equals(arg)) {
            return forSourceInfo(submmoduleForIR(rootStatement, sourceId));
        }
        throw new IllegalArgumentException("Root of parsed AST must be either module or submodule");
    }

    public static @NonNull YangModelDependencyInfo forSourceInfo(final SourceInfo info) {
        if (info instanceof ModuleSourceInfo module) {
            return forSourceInfo(module);
        } else if (info instanceof SubmoduleSourceInfo submodule) {
            return forSourceInfo(submodule);
        } else {
            throw new IllegalArgumentException("Unhandled source info " + requireNonNull(info));
        }
    }

    public static @NonNull ModuleDependencyInfo forSourceInfo(final @NonNull ModuleSourceInfo info) {
        return new ModuleDependencyInfo(info.name().getLocalName(), latestRevision(info.revisions()), info.imports(),
            info.includes());
    }

    public static @NonNull SubmoduleDependencyInfo forSourceInfo(final @NonNull SubmoduleSourceInfo info) {
        return new SubmoduleDependencyInfo(info.name().getLocalName(), latestRevision(info.revisions()),
            info.belongsTo(), info.imports(), info.includes());
    }

    /**
     * Extracts {@link YangModelDependencyInfo} from a {@link YangTextSource}. This parsing does not validate full YANG
     * module, only parses header up to the revisions and imports.
     *
     * @param yangText {@link YangTextSource}
     * @return {@link YangModelDependencyInfo}
     * @throws YangSyntaxErrorException If the resource does not pass syntactic analysis
     * @throws IOException When the resource cannot be read
     */
    public static YangModelDependencyInfo forYangText(final YangTextSource yangText)
            throws IOException, YangSyntaxErrorException {
        final var source = YangStatementStreamSource.create(yangText);
        return forIR(source.rootStatement(), source.getIdentifier());
    }

    private static @NonNull ModuleSourceInfo moduleForIR(final IRStatement root, final SourceIdentifier sourceId) {
        return new ModuleSourceInfo(Unqualified.of(safeStringArgument(sourceId, root, "module name")),
            extractYangVersion(root, sourceId), extractNamespace(root, sourceId), extractPrefix(root, sourceId),
            extractRevisions(root, sourceId), extractImports(root, sourceId), extractIncludes(root, sourceId));
    }

    private static @NonNull SubmoduleSourceInfo submmoduleForIR(final IRStatement root,
            final SourceIdentifier sourceId) {
        return new SubmoduleSourceInfo(Unqualified.of(safeStringArgument(sourceId, root, "submodule name")),
            extractYangVersion(root, sourceId), extractBelongsTo(root, sourceId),
            extractRevisions(root, sourceId), extractImports(root, sourceId), extractIncludes(root, sourceId));
    }

    private static @Nullable Revision latestRevision(final Collection<Revision> revision) {
        return revision.stream().sorted(Comparator.reverseOrder()).findFirst().orElse(null);
    }

    private static YangVersion extractYangVersion(final IRStatement root, final SourceIdentifier sourceId) {
        return root.statements().stream()
            .filter(stmt -> isBuiltin(stmt, YANG_VERSION))
            .findFirst()
            .map(stmt -> safeStringArgument(sourceId, stmt, "yang-version argument"))
            .map(YangVersion::forString)
            .orElse(YangVersion.VERSION_1);
    }

    private static @NonNull XMLNamespace extractNamespace(final IRStatement root, final SourceIdentifier sourceId) {
        return root.statements().stream()
            .filter(stmt -> isBuiltin(stmt, NAMESPACE))
            .findFirst()
            .map(stmt -> safeStringArgument(sourceId, stmt, "namespace argument"))
            .map(XMLNamespace::of)
            .orElseThrow(() -> new IllegalArgumentException("No namespace statement in " + refOf(sourceId, root)));
    }

    private static @NonNull String extractPrefix(final IRStatement root, final SourceIdentifier sourceId) {
        return root.statements().stream()
            .filter(stmt -> isBuiltin(stmt, PREFIX))
            .findFirst()
            .map(stmt -> safeStringArgument(sourceId, stmt, "prefix argument"))
            .orElseThrow(() -> new IllegalArgumentException("No prefix statement in " + refOf(sourceId, root)));
    }

    private static @NonNull Unqualified extractBelongsTo(final IRStatement root, final SourceIdentifier sourceId) {
        return root.statements().stream()
            .filter(stmt -> isBuiltin(stmt, BELONGS_TO))
            .findFirst()
            .map(stmt -> Unqualified.of(safeStringArgument(sourceId, stmt, "belongs-to module name")))
            .orElseThrow(() -> new IllegalArgumentException("No belongs-to statement in " + refOf(sourceId, root)));
    }

    private static @NonNull ImmutableSet<Revision> extractRevisions(final IRStatement root,
            final SourceIdentifier sourceId) {
        return root.statements().stream()
            .filter(stmt -> isBuiltin(stmt, REVISION))
            .map(stmt -> Revision.of(safeStringArgument(sourceId, stmt, "revision argument")))
            .collect(ImmutableSet.toImmutableSet());
    }

    private static @Nullable Revision extractRevisionDate(final IRStatement root, final SourceIdentifier sourceId) {
        return root.statements().stream()
            .filter(stmt -> isBuiltin(stmt, REVISION_DATE))
            .findFirst()
            .map(stmt -> Revision.of(safeStringArgument(sourceId, stmt, "revision date argument")))
            .orElse(null);
    }

    private static @NonNull ImmutableSet<Import> extractImports(final IRStatement root,
            final SourceIdentifier sourceId) {
        return root.statements().stream()
            .filter(stmt -> isBuiltin(stmt, IMPORT))
            .map(stmt -> new Import(Unqualified.of(safeStringArgument(sourceId, stmt, "imported module name")),
                extractPrefix(stmt, sourceId), extractRevisionDate(stmt, sourceId)))
            .collect(ImmutableSet.toImmutableSet());
    }

    private static @NonNull ImmutableSet<Include> extractIncludes(final IRStatement root,
            final SourceIdentifier sourceId) {
        return root.statements().stream()
            .filter(stmt -> isBuiltin(stmt, INCLUDE))
            .map(stmt -> new Include(Unqualified.of(safeStringArgument(sourceId, stmt, "included submodule name")),
                extractRevisionDate(stmt, sourceId)))
            .collect(ImmutableSet.toImmutableSet());
    }

    private static boolean isBuiltin(final IRStatement stmt, final String localName) {
        return stmt.keyword() instanceof IRKeyword.Unqualified keyword && localName.equals(keyword.identifier());
    }

    public static String getLatestRevision(final IRStatement module, final SourceIdentifier source) {
        String latestRevision = null;
        for (final IRStatement substatement : module.statements()) {
            if (isBuiltin(substatement, REVISION)) {
                final String currentRevision = safeStringArgument(source, substatement, "revision date");
                if (latestRevision == null || latestRevision.compareTo(currentRevision) < 0) {
                    latestRevision = currentRevision;
                }
            }
        }
        return latestRevision;
    }

    static @NonNull String safeStringArgument(final SourceIdentifier source, final IRStatement stmt,
            final String desc) {
        final var ref = refOf(source, stmt);
        final var arg = stmt.argument();
        if (arg == null) {
            throw new IllegalArgumentException("Missing " + desc + " at " + ref);
        }

        // TODO: we probably need to understand yang version first....
        return ArgumentContextUtils.rfc6020().stringFromStringContext(arg, ref);
    }

    private static StatementSourceReference refOf(final SourceIdentifier source, final IRStatement stmt) {
        return ExplicitStatement.atPosition(source.name().getLocalName(), stmt.startLine(), stmt.startColumn() + 1);
    }
}
