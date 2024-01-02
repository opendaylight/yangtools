/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName;
import org.opendaylight.yangtools.yang.ir.IRArgument;
import org.opendaylight.yangtools.yang.ir.IRKeyword;
import org.opendaylight.yangtools.yang.ir.IRKeyword.Unqualified;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportEffectiveStatement;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangIRSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.spi.meta.StringUnescaper;
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
public abstract class YangModelDependencyInfo {
    private static final String BELONGS_TO = YangStmtMapping.BELONGS_TO.getStatementName().getLocalName();
    private static final String IMPORT = YangStmtMapping.IMPORT.getStatementName().getLocalName();
    private static final String INCLUDE = YangStmtMapping.INCLUDE.getStatementName().getLocalName();
    private static final String MODULE = YangStmtMapping.MODULE.getStatementName().getLocalName();
    private static final String REVISION = YangStmtMapping.REVISION.getStatementName().getLocalName();
    private static final String REVISION_DATE = YangStmtMapping.REVISION_DATE.getStatementName().getLocalName();
    private static final String SUBMODULE = YangStmtMapping.SUBMODULE.getStatementName().getLocalName();

    private final String name;
    private final Revision revision;
    private final ImmutableSet<ModuleImport> submoduleIncludes;
    private final ImmutableSet<ModuleImport> moduleImports;
    private final ImmutableSet<ModuleImport> dependencies;

    YangModelDependencyInfo(final String name, final String formattedRevision, final ImmutableSet<ModuleImport> imports,
            final ImmutableSet<ModuleImport> includes) {
        this.name = name;
        revision = Revision.ofNullable(formattedRevision).orElse(null);
        moduleImports = imports;
        submoduleIncludes = includes;
        dependencies = ImmutableSet.<ModuleImport>builder()
                .addAll(moduleImports).addAll(submoduleIncludes).build();
    }

    /**
     * Returns immutable collection of all module imports. This collection contains both <code>import</code> statements
     * and <code>include</code> statements for submodules.
     *
     * @return Immutable collection of imports.
     */
    public ImmutableSet<ModuleImport> getDependencies() {
        return dependencies;
    }

    /**
     * Returns model name.
     *
     * @return model name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns formatted revision string.
     *
     * @return formatted revision string
     */
    public String getFormattedRevision() {
        return revision != null ? revision.toString() : null;
    }

    /**
     * Returns revision.
     *
     * @return revision, potentially null
     */
    public Optional<Revision> getRevision() {
        return Optional.ofNullable(revision);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(name);
        result = prime * result + Objects.hashCode(revision);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof YangModelDependencyInfo other
            && Objects.equals(name, other.name) && Objects.equals(revision, other.revision);
    }

    /**
     * Extracts {@link YangModelDependencyInfo} from an intermediate representation root statement of a YANG model.
     *
     * @param source Schema source
     * @return {@link YangModelDependencyInfo}
     * @throws IllegalArgumentException If the root statement is not a valid YANG module/submodule
     */
    public static @NonNull YangModelDependencyInfo forIR(final YangIRSchemaSource source) {
        return forIR(source.getRootStatement(), source.getIdentifier());
    }

    /**
     * Extracts {@link YangModelDependencyInfo} from an intermediate representation root statement of a YANG model.
     *
     * @param source Source identifier
     * @param rootStatement root statement
     * @return {@link YangModelDependencyInfo}
     * @throws IllegalArgumentException If the root statement is not a valid YANG module/submodule
     */
    static @NonNull YangModelDependencyInfo forIR(final IRStatement rootStatement,
            final SourceIdentifier source) {
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

    /**
     * Extracts {@link YangModelDependencyInfo} from a {@link YangTextSchemaSource}. This parsing does not
     * validate full YANG module, only parses header up to the revisions and imports.
     *
     * @param yangText {@link YangTextSchemaSource}
     * @return {@link YangModelDependencyInfo}
     * @throws YangSyntaxErrorException If the resource does not pass syntactic analysis
     * @throws IOException When the resource cannot be read
     */
    public static YangModelDependencyInfo forYangText(final YangTextSchemaSource yangText)
            throws IOException, YangSyntaxErrorException {
        final YangStatementStreamSource source = YangStatementStreamSource.create(yangText);
        return forIR(source.rootStatement(), source.getIdentifier());
    }

    private static @NonNull YangModelDependencyInfo parseModuleContext(final IRStatement module,
            final SourceIdentifier source) {
        final String name = safeStringArgument(source, module, "module name");
        final String latestRevision = getLatestRevision(module, source);
        final ImmutableSet<ModuleImport> imports = parseImports(module, source);
        final ImmutableSet<ModuleImport> includes = parseIncludes(module, source);

        return new ModuleDependencyInfo(name, latestRevision, imports, includes);
    }

    private static ImmutableSet<ModuleImport> parseImports(final IRStatement module,
            final SourceIdentifier source) {
        final Set<ModuleImport> result = new HashSet<>();
        for (final IRStatement substatement : module.statements()) {
            if (isBuiltin(substatement, IMPORT)) {
                final String importedModuleName = safeStringArgument(source, substatement, "imported module name");
                final String revisionDateStr = getRevisionDateString(substatement, source);
                result.add(new ModuleImportImpl(UnresolvedQName.Unqualified.of(importedModuleName),
                    revisionDateStr != null ? Revision.of(revisionDateStr) : null));
            }
        }
        return ImmutableSet.copyOf(result);
    }

    private static boolean isBuiltin(final IRStatement stmt, final String localName) {
        final IRKeyword keyword = stmt.keyword();
        return keyword instanceof Unqualified && localName.equals(keyword.identifier());
    }

    private static ImmutableSet<ModuleImport> parseIncludes(final IRStatement module, final SourceIdentifier source) {
        final Set<ModuleImport> result = new HashSet<>();
        for (final IRStatement substatement : module.statements()) {
            if (isBuiltin(substatement, INCLUDE)) {
                final String revisionDateStr = getRevisionDateString(substatement, source);
                final String includeModuleName = safeStringArgument(source, substatement, "included submodule name");
                result.add(new ModuleImportImpl(UnresolvedQName.Unqualified.of(includeModuleName),
                    revisionDateStr == null ? null : Revision.of(revisionDateStr)));
            }
        }
        return ImmutableSet.copyOf(result);
    }

    private static String getRevisionDateString(final IRStatement importStatement, final SourceIdentifier source) {
        String revisionDateStr = null;
        for (final IRStatement substatement : importStatement.statements()) {
            if (isBuiltin(substatement, REVISION_DATE)) {
                revisionDateStr = safeStringArgument(source, substatement, "imported module revision-date");
            }
        }
        return revisionDateStr;
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

    private static @NonNull YangModelDependencyInfo parseSubmoduleContext(final IRStatement submodule,
            final SourceIdentifier source) {
        final String name = safeStringArgument(source, submodule, "submodule name");
        final UnresolvedQName.Unqualified belongsTo = UnresolvedQName.Unqualified.of(parseBelongsTo(submodule, source));

        final String latestRevision = getLatestRevision(submodule, source);
        final ImmutableSet<ModuleImport> imports = parseImports(submodule, source);
        final ImmutableSet<ModuleImport> includes = parseIncludes(submodule, source);

        return new SubmoduleDependencyInfo(name, latestRevision, belongsTo, imports, includes);
    }

    private static String parseBelongsTo(final IRStatement submodule, final SourceIdentifier source) {
        for (final IRStatement substatement : submodule.statements()) {
            if (isBuiltin(substatement, BELONGS_TO)) {
                return safeStringArgument(source, substatement, "belongs-to module name");
            }
        }
        return null;
    }

    static String safeStringArgument(final SourceIdentifier source, final IRStatement stmt, final String desc) {
        final StatementSourceReference ref = getReference(source, stmt);
        final IRArgument arg = stmt.argument();
        if (arg == null) {
            throw new IllegalArgumentException("Missing " + desc + " at " + ref);
        }

        // TODO: we probably need to understand yang version first....
        return StringUnescaper.RFC6020.stringFromStringContext(arg, ref);
    }

    private static StatementSourceReference getReference(final SourceIdentifier source, final IRStatement stmt) {
        return ExplicitStatement.atPosition(source.name().getLocalName(), stmt.startLine(), stmt.startColumn() + 1);
    }

    /**
     * Dependency information for YANG module.
     */
    public static final class ModuleDependencyInfo extends YangModelDependencyInfo {
        ModuleDependencyInfo(final String name, final String latestRevision, final ImmutableSet<ModuleImport> imports,
                final ImmutableSet<ModuleImport> includes) {
            super(name, latestRevision, imports, includes);
        }

        @Override
        public String toString() {
            return "Module [name=" + getName() + ", revision=" + getRevision()
                + ", dependencies=" + getDependencies()
                + "]";
        }
    }

    /**
     * Dependency information for submodule, also provides name for parent module.
     */
    public static final class SubmoduleDependencyInfo extends YangModelDependencyInfo {
        private final UnresolvedQName.Unqualified belongsTo;

        private SubmoduleDependencyInfo(final String name, final String latestRevision,
                final UnresolvedQName.Unqualified belongsTo, final ImmutableSet<ModuleImport> imports,
                final ImmutableSet<ModuleImport> includes) {
            super(name, latestRevision, imports, includes);
            this.belongsTo = belongsTo;
        }

        /**
         * Returns name of parent module.
         *
         * @return The module this info belongs to
         */
        public UnresolvedQName.Unqualified getParentModule() {
            return belongsTo;
        }

        @Override
        public String toString() {
            return "Submodule [name=" + getName() + ", revision=" + getRevision()
                + ", dependencies=" + getDependencies()
                + "]";
        }
    }

    /**
     * Utility implementation of {@link ModuleImport} to be used by {@link YangModelDependencyInfo}.
     */
    // FIXME: this is a rather nasty misuse of APIs :(
    private static final class ModuleImportImpl implements ModuleImport {
        private final UnresolvedQName.@NonNull Unqualified moduleName;
        private final Revision revision;

        ModuleImportImpl(final UnresolvedQName.@NonNull Unqualified moduleName, final @Nullable Revision revision) {
            this.moduleName = requireNonNull(moduleName, "Module name must not be null.");
            this.revision = revision;
        }

        @Override
        public UnresolvedQName.Unqualified getModuleName() {
            return moduleName;
        }

        @Override
        public Optional<Revision> getRevision() {
            return Optional.ofNullable(revision);
        }

        @Override
        public String getPrefix() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<String> getDescription() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getReference() {
            return Optional.empty();
        }

        @Override
        public ImportEffectiveStatement asEffectiveStatement() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hashCode(moduleName);
            result = prime * result + Objects.hashCode(revision);
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            return this == obj || obj instanceof ModuleImportImpl other
                && moduleName.equals(other.moduleName) && Objects.equals(revision, other.revision);
        }

        @Override
        public String toString() {
            return "ModuleImportImpl [name=" + moduleName + ", revision=" + revision + "]";
        }
    }
}
