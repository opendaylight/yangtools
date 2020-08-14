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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigStatements;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRArgument;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRKeyword;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRStatement;
import org.opendaylight.yangtools.yang.parser.spi.source.DeclarationInTextSource;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

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

    private static final String OPENCONFIG_VERSION = OpenConfigStatements.OPENCONFIG_VERSION.getStatementName()
            .getLocalName();
    private static final Splitter COLON_SPLITTER = Splitter.on(":").omitEmptyStrings().trimResults();

    private final String name;
    private final Revision revision;
    private final SemVer semVer;
    private final ImmutableSet<ModuleImport> submoduleIncludes;
    private final ImmutableSet<ModuleImport> moduleImports;
    private final ImmutableSet<ModuleImport> dependencies;

    YangModelDependencyInfo(final String name, final String formattedRevision,
            final ImmutableSet<ModuleImport> imports,
            final ImmutableSet<ModuleImport> includes) {
        this(name, formattedRevision, imports, includes, Optional.empty());
    }

    YangModelDependencyInfo(final String name, final String formattedRevision,
            final ImmutableSet<ModuleImport> imports,
            final ImmutableSet<ModuleImport> includes,
            final Optional<SemVer> semVer) {
        this.name = name;
        this.revision = Revision.ofNullable(formattedRevision).orElse(null);
        this.moduleImports = imports;
        this.submoduleIncludes = includes;
        this.dependencies = ImmutableSet.<ModuleImport>builder()
                .addAll(moduleImports).addAll(submoduleIncludes).build();
        this.semVer = semVer.orElse(null);
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

    /**
     * Returns semantic version of module.
     *
     * @return semantic version
     */
    public Optional<SemVer> getSemanticVersion() {
        return Optional.ofNullable(semVer);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(name);
        result = prime * result + Objects.hashCode(revision);
        result = prime * result + Objects.hashCode(semVer);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof YangModelDependencyInfo)) {
            return false;
        }
        final YangModelDependencyInfo other = (YangModelDependencyInfo) obj;
        return Objects.equals(name, other.name) && Objects.equals(revision, other.revision)
                && Objects.equals(semVer, other.semVer);
    }

    /**
     * Extracts {@link YangModelDependencyInfo} from an abstract syntax tree of a YANG model.
     *
     * @param source Source identifier
     * @param rootStatement root statement
     * @return {@link YangModelDependencyInfo}
     * @throws IllegalArgumentException If the AST is not a valid YANG module/submodule
     */
    static @NonNull YangModelDependencyInfo parseAST(final IRStatement rootStatement,
            final SourceIdentifier source) {
        final String keyWordText = rootStatement.keyword().toString();
        if (MODULE.equals(keyWordText)) {
            return parseModuleContext(rootStatement, source);
        }
        if (SUBMODULE.equals(keyWordText)) {
            return parseSubmoduleContext(rootStatement, source);
        }
        throw new IllegalArgumentException("Root of parsed AST must be either module or submodule");
    }

    /**
     * Extracts {@link YangModelDependencyInfo} from input stream containing a YANG model. This parsing does not
     * validate full YANG module, only parses header up to the revisions and imports.
     *
     * @param refClass Base search class
     * @param resourceName resource name, relative to refClass
     * @return {@link YangModelDependencyInfo}
     * @throws YangSyntaxErrorException If the resource does not pass syntactic analysis
     * @throws IOException When the resource cannot be read
     * @throws IllegalArgumentException
     *             If input stream is not valid YANG stream
     */
    @VisibleForTesting
    public static YangModelDependencyInfo forResource(final Class<?> refClass, final String resourceName)
            throws IOException, YangSyntaxErrorException {
        final YangStatementStreamSource source = YangStatementStreamSource.create(
            YangTextSchemaSource.forResource(refClass, resourceName));
        return parseAST(source.rootStatement(), source.getIdentifier());
    }

    private static @NonNull YangModelDependencyInfo parseModuleContext(final IRStatement module,
            final SourceIdentifier source) {
        final String name = safeStringArgument(source, module, "module name");
        final String latestRevision = getLatestRevision(module, source);
        final Optional<SemVer> semVer = Optional.ofNullable(findSemanticVersion(module, source));
        final ImmutableSet<ModuleImport> imports = parseImports(module, source);
        final ImmutableSet<ModuleImport> includes = parseIncludes(module, source);

        return new ModuleDependencyInfo(name, latestRevision, imports, includes, semVer);
    }

    private static ImmutableSet<ModuleImport> parseImports(final IRStatement module,
            final SourceIdentifier source) {
        final Set<ModuleImport> result = new HashSet<>();
        for (final IRStatement substatement : module.statements()) {
            if (isBuiltin(substatement, IMPORT)) {
                final String importedModuleName = safeStringArgument(source, substatement, "imported module name");
                final String revisionDateStr = getRevisionDateString(substatement, source);
                final Revision revisionDate = Revision.ofNullable(revisionDateStr).orElse(null);
                final SemVer importSemVer = findSemanticVersion(substatement, source);
                result.add(new ModuleImportImpl(importedModuleName, revisionDate, importSemVer));
            }
        }
        return ImmutableSet.copyOf(result);
    }

    private static SemVer findSemanticVersion(final IRStatement statement, final SourceIdentifier source) {
        String semVerString = null;
        for (final IRStatement substatement : statement.statements()) {
            // FIXME: this should also check we are using a prefix
            if (OPENCONFIG_VERSION.equals(substatement.keyword().identifier())) {
                semVerString = safeStringArgument(source,  substatement, "version string");
                break;
            }
        }

        return Strings.isNullOrEmpty(semVerString) ? null : SemVer.valueOf(semVerString);
    }

    private static boolean isBuiltin(final IRStatement stmt, final String localName) {
        final IRKeyword keyword = stmt.keyword();
        return keyword.prefix() == null && localName.equals(keyword.identifier());
    }

    private static ImmutableSet<ModuleImport> parseIncludes(final IRStatement module, final SourceIdentifier source) {
        final Set<ModuleImport> result = new HashSet<>();
        for (final IRStatement substatement : module.statements()) {
            if (isBuiltin(substatement, INCLUDE)) {
                final String revisionDateStr = getRevisionDateString(substatement, source);
                final String IncludeModuleName = safeStringArgument(source, substatement, "included submodule name");
                final Revision revisionDate = Revision.ofNullable(revisionDateStr).orElse(null);
                result.add(new ModuleImportImpl(IncludeModuleName, revisionDate));
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
        final String belongsTo = parseBelongsTo(submodule, source);

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

    private static String safeStringArgument(final SourceIdentifier source, final IRStatement stmt,
            final String desc) {
        final StatementSourceReference ref = getReference(source, stmt);
        final IRArgument arg = stmt.argument();
        checkArgument(arg != null, "Missing %s at %s", desc, ref);
        // TODO: we probably need to understand yang version first....
        return ArgumentContextUtils.rfc6020().stringFromStringContext(arg, ref);
    }

    private static StatementSourceReference getReference(final SourceIdentifier source,
            final IRStatement stmt) {
        return DeclarationInTextSource.atPosition(source.getName(), stmt.startLine(), stmt.startColumn());
    }

    /**
     * Dependency information for YANG module.
     */
    public static final class ModuleDependencyInfo extends YangModelDependencyInfo {
        ModuleDependencyInfo(final String name, final String latestRevision, final ImmutableSet<ModuleImport> imports,
                final ImmutableSet<ModuleImport> includes, final Optional<SemVer> semVer) {
            super(name, latestRevision, imports, includes, semVer);
        }

        @Override
        public String toString() {
            return "Module [name=" + getName() + ", revision=" + getRevision()
                + ", semanticVersion=" + getSemanticVersion().orElse(null)
                + ", dependencies=" + getDependencies()
                + "]";
        }
    }

    /**
     * Dependency information for submodule, also provides name for parent module.
     */
    public static final class SubmoduleDependencyInfo extends YangModelDependencyInfo {
        private final String belongsTo;

        private SubmoduleDependencyInfo(final String name, final String latestRevision, final String belongsTo,
                final ImmutableSet<ModuleImport> imports, final ImmutableSet<ModuleImport> includes) {
            super(name, latestRevision, imports, includes);
            this.belongsTo = belongsTo;
        }

        /**
         * Returns name of parent module.
         */
        public String getParentModule() {
            return belongsTo;
        }

        @Override
        public String toString() {
            return "Submodule [name=" + getName() + ", revision="
                    + getRevision() + ", dependencies=" + getDependencies()
                    + "]";
        }
    }

    /**
     * Utility implementation of {@link ModuleImport} to be used by {@link YangModelDependencyInfo}.
     */
    private static final class ModuleImportImpl implements ModuleImport {

        private final Revision revision;
        private final SemVer semVer;
        private final String name;

        ModuleImportImpl(final @NonNull String moduleName, final @Nullable Revision revision) {
            this(moduleName, revision, null);
        }

        ModuleImportImpl(final @NonNull String moduleName, final @Nullable Revision revision,
                final @Nullable SemVer semVer) {
            this.name = requireNonNull(moduleName, "Module name must not be null.");
            this.revision = revision;
            this.semVer = semVer;
        }

        @Override
        public String getModuleName() {
            return name;
        }

        @Override
        public Optional<Revision> getRevision() {
            return Optional.ofNullable(revision);
        }

        @Override
        public Optional<SemVer> getSemanticVersion() {
            return Optional.ofNullable(semVer);
        }

        @Override
        public String getPrefix() {
            return null;
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
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hashCode(name);
            result = prime * result + Objects.hashCode(revision);
            result = prime * result + Objects.hashCode(semVer);
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ModuleImportImpl)) {
                return false;
            }
            final ModuleImportImpl other = (ModuleImportImpl) obj;
            return name.equals(other.name) && Objects.equals(revision, other.revision)
                    && Objects.equals(getSemanticVersion(), other.getSemanticVersion());
        }

        @Override
        public String toString() {
            return "ModuleImportImpl [name=" + name + ", revision="
                    + QName.formattedRevision(Optional.ofNullable(revision)) + ", semanticVersion=" + semVer + "]";
        }
    }
}
