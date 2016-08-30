/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.antlr.v4.runtime.ParserRuleContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser.StatementContext;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.SupportedExtensionsMapping;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;

/**
 * Helper transfer object which holds basic and dependency information for YANG
 * model.
 *
 *
 *
 * There are two concrete implementations of this interface:
 * <ul>
 * <li>{@link ModuleDependencyInfo} - Dependency information for module</li>
 * <li>{@link SubmoduleDependencyInfo} - Dependency information for submodule</li>
 * </ul>
 *
 * @see ModuleDependencyInfo
 * @see SubmoduleDependencyInfo
 *
 */

public abstract class YangModelDependencyInfo {

    private final String name;
    private final String formattedRevision;
    private final Date revision;
    private final Optional<SemVer> semVer;
    private final ImmutableSet<ModuleImport> submoduleIncludes;
    private final ImmutableSet<ModuleImport> moduleImports;
    private final ImmutableSet<ModuleImport> dependencies;

    YangModelDependencyInfo(final String name, final String formattedRevision,
            final ImmutableSet<ModuleImport> imports,
            final ImmutableSet<ModuleImport> includes) {
        this(name, formattedRevision, imports, includes, Optional.absent());
    }

    YangModelDependencyInfo(final String name, final String formattedRevision,
            final ImmutableSet<ModuleImport> imports,
            final ImmutableSet<ModuleImport> includes,
            final Optional<SemVer> semVer) {
        this.name = name;
        this.formattedRevision = formattedRevision;
        this.revision = formattedRevision == null ? null : QName
                .parseRevision(formattedRevision);
        this.moduleImports = imports;
        this.submoduleIncludes = includes;
        this.dependencies = ImmutableSet.<ModuleImport> builder()
                .addAll(moduleImports).addAll(submoduleIncludes).build();
        this.semVer = semVer;
    }

    /**
     * Returns immutable collection of all module imports.
     *
     * This collection contains both <code>import</code> statements and
     * <code>include</code> statements for submodules.
     *
     * @return Immutable collection of imports.
     */
    public ImmutableSet<ModuleImport> getDependencies() {
        return dependencies;
    }

    /**
     * Returns model name
     *
     * @return model name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns formatted revision string
     *
     * @return formatted revision string
     */
    public String getFormattedRevision() {
        return formattedRevision;
    }

    /**
     * Returns revision
     *
     * @return revision
     */
    Date getRevision() {
        return revision;
    }

    /**
     * Returns semantic version of module
     *
     * @return semantic version
     */
    public Optional<SemVer> getSemanticVersion() {
        return semVer;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(formattedRevision);
        result = prime * result + Objects.hashCode(name);
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
        if (formattedRevision == null) {
            if (other.formattedRevision != null) {
                return false;
            }
        } else if (!formattedRevision.equals(other.formattedRevision)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if(!Objects.equals(semVer, other.semVer)) {
            return false;
        }

        return true;
    }

    /**
     * Extracts {@link YangModelDependencyInfo} from an abstract syntax tree of
     * a YANG model.
     *
     * @param tree
     *            Abstract syntax tree
     * @return {@link YangModelDependencyInfo}
     * @throws YangSyntaxErrorException
     *             If the AST is not a valid YANG module/submodule
     */
    public static YangModelDependencyInfo fromAST(final String name,
            final ParserRuleContext tree) throws YangSyntaxErrorException {

        if (tree instanceof StatementContext) {
            final StatementContext rootStatement = (StatementContext) tree;
            return parseAST(rootStatement);
        }

        throw new YangSyntaxErrorException(name, 0, 0, "Unknown YANG text type");
    }

    private static YangModelDependencyInfo parseAST(final StatementContext rootStatement) {
        if (rootStatement
                .keyword()
                .getText()
                .equals(Rfc6020Mapping.MODULE.getStatementName().getLocalName())) {
            return parseModuleContext(rootStatement);
        } else if (rootStatement
                .keyword()
                .getText()
                .equals(Rfc6020Mapping.SUBMODULE.getStatementName()
                        .getLocalName())) {
            return parseSubmoduleContext(rootStatement);
        }

        throw new IllegalArgumentException(
                "Root of parsed AST must be either module or submodule");
    }

    /**
     * Extracts {@link YangModelDependencyInfo} from input stream containing
     * YANG model.
     *
     * This parsing does not validate full YANG module, only parses header up to
     * the revisions and imports.
     *
     * @param yangStream
     *            Opened Input stream containing text source of YANG model
     * @return {@link YangModelDependencyInfo}
     * @throws IllegalArgumentException
     *             If input stream is not valid YANG stream
     */
    public static YangModelDependencyInfo fromInputStream(
            final InputStream yangStream) {
        final StatementContext yangAST = new YangStatementSourceImpl(yangStream)
                .getYangAST();
        return parseAST(yangAST);
    }

    private static YangModelDependencyInfo parseModuleContext(final StatementContext module) {
        final String name = Utils.stringFromStringContext(module.argument());
        final String latestRevision = getLatestRevision(module);
        final Optional<SemVer> semVer = Optional.fromNullable(getSemanticVersion(module));
        final ImmutableSet<ModuleImport> imports = parseImports(module);
        final ImmutableSet<ModuleImport> includes = parseIncludes(module);

        return new ModuleDependencyInfo(name, latestRevision, imports, includes, semVer);
    }

    private static ImmutableSet<ModuleImport> parseImports(final StatementContext module) {
        final Set<ModuleImport> result = new HashSet<>();
        final List<StatementContext> subStatements = module.statement();
        for (final StatementContext subStatementContext : subStatements) {
            if (subStatementContext
                    .keyword()
                    .getText()
                    .equals(Rfc6020Mapping.IMPORT.getStatementName()
                            .getLocalName())) {
                final String revisionDateStr = getRevisionDateString(subStatementContext);
                final String importedModuleName = Utils
                        .stringFromStringContext(subStatementContext.argument());
                final Date revisionDate = (revisionDateStr == null) ? null : QName
                        .parseRevision(revisionDateStr);
                final Optional<SemVer> importSemVer = Optional.fromNullable(getSemanticVersion(subStatementContext));
                result.add(new ModuleImportImpl(importedModuleName,
                        revisionDate, importSemVer));
            }
        }
        return ImmutableSet.copyOf(result);
    }

    private static SemVer getSemanticVersion(final StatementContext statement) {
        final List<StatementContext> subStatements = statement.statement();
        String semVerString = null;
        final String semVerStmtName = SupportedExtensionsMapping.SEMANTIC_VERSION.getStatementName().getLocalName();
        for (final StatementContext subStatement : subStatements) {
            final String subStatementName = Utils.trimPrefix(subStatement.keyword().getText());
            if (semVerStmtName.equals(subStatementName)) {
                semVerString = Utils.stringFromStringContext(subStatement.argument());
                break;
            }
        }

        if (Strings.isNullOrEmpty(semVerString)) {
            return null;
        }

        return SemVer.valueOf(semVerString);
    }

    private static ImmutableSet<ModuleImport> parseIncludes(final StatementContext module) {
        final Set<ModuleImport> result = new HashSet<>();
        final List<StatementContext> subStatements = module.statement();
        for (final StatementContext subStatementContext : subStatements) {
            if (subStatementContext
                    .keyword()
                    .getText()
                    .equals(Rfc6020Mapping.INCLUDE.getStatementName()
                            .getLocalName())) {
                final String revisionDateStr = getRevisionDateString(subStatementContext);
                final String IncludeModuleName = Utils
                        .stringFromStringContext(subStatementContext.argument());
                final Date revisionDate = (revisionDateStr == null) ? null : QName
                        .parseRevision(revisionDateStr);
                result.add(new ModuleImportImpl(IncludeModuleName, revisionDate));
            }
        }
        return ImmutableSet.copyOf(result);
    }

    private static String getRevisionDateString(final StatementContext importStatement) {
        final List<StatementContext> importSubStatements = importStatement.statement();
        String revisionDateStr = null;
        for (final StatementContext importSubStatement : importSubStatements) {
            if (importSubStatement
                    .keyword()
                    .getText()
                    .equals(Rfc6020Mapping.REVISION_DATE.getStatementName()
                            .getLocalName())) {
                revisionDateStr = Utils
                        .stringFromStringContext(importSubStatement.argument());
            }
        }
        return revisionDateStr;
    }

    public static String getLatestRevision(final StatementContext module) {
        final List<StatementContext> subStatements = module.statement();
        String latestRevision = null;
        for (final StatementContext subStatementContext : subStatements) {
            if (subStatementContext
                    .keyword()
                    .getText()
                    .equals(Rfc6020Mapping.REVISION.getStatementName()
                            .getLocalName())) {
                final String currentRevision = Utils
                        .stringFromStringContext(subStatementContext.argument());
                if (latestRevision == null
                        || latestRevision.compareTo(currentRevision) == -1) {
                    latestRevision = currentRevision;
                }
            }
        }
        return latestRevision;
    }

    private static YangModelDependencyInfo parseSubmoduleContext(final StatementContext submodule) {
        final String name = Utils.stringFromStringContext(submodule.argument());
        final String belongsTo = parseBelongsTo(submodule);

        final String latestRevision = getLatestRevision(submodule);
        final ImmutableSet<ModuleImport> imports = parseImports(submodule);
        final ImmutableSet<ModuleImport> includes = parseIncludes(submodule);

        return new SubmoduleDependencyInfo(name, latestRevision, belongsTo,
                imports, includes);
    }

    private static String parseBelongsTo(final StatementContext submodule) {
        final List<StatementContext> subStatements = submodule.statement();
        for (final StatementContext subStatementContext : subStatements) {
            if (subStatementContext
                    .keyword()
                    .getText()
                    .equals(Rfc6020Mapping.BELONGS_TO.getStatementName()
                            .getLocalName())) {
                return Utils.stringFromStringContext(subStatementContext
                        .argument());
            }
        }
        return null;
    }

    /**
     *
     * Dependency information for YANG module.
     *
     */
    public static class ModuleDependencyInfo extends
            YangModelDependencyInfo {

        private ModuleDependencyInfo(final String name,
                final String latestRevision,
                final ImmutableSet<ModuleImport> imports,
                final ImmutableSet<ModuleImport> includes) {
            super(name, latestRevision, imports, includes);
        }

        private ModuleDependencyInfo(final String name,
                final String latestRevision,
                final ImmutableSet<ModuleImport> imports,
                final ImmutableSet<ModuleImport> includes,
                final Optional<SemVer> semVer) {
            super(name, latestRevision, imports, includes, semVer);
        }

        @Override
        public String toString() {
            return "Module [name=" + getName() + ", revision=" + getRevision() + ", semanticVersion="
                    + getSemanticVersion().or(Module.DEFAULT_SEMANTIC_VERSION) + ", dependencies=" + getDependencies()
                    + "]";
        }
    }

    /**
     *
     * Dependency information for submodule, also provides name for parent
     * module.
     *
     */
    public static final class SubmoduleDependencyInfo extends
            YangModelDependencyInfo {

        private final String belongsTo;

        private SubmoduleDependencyInfo(final String name,
                final String latestRevision, final String belongsTo,
                final ImmutableSet<ModuleImport> imports,
                final ImmutableSet<ModuleImport> includes) {
            super(name, latestRevision, imports, includes);
            this.belongsTo = belongsTo;
        }

        /**
         * Returns name of parent module.
         *
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
     * Utility implementation of {@link ModuleImport} to be used by
     * {@link YangModelDependencyInfo}.
     *
     */
    private static final class ModuleImportImpl implements ModuleImport {

        private final Date revision;
        private final SemVer semVer;
        private final String name;

        public ModuleImportImpl(final String moduleName, final Date revision) {
            this(moduleName, revision, Optional.absent());
        }

        public ModuleImportImpl(final String moduleName, final Date revision, final Optional<SemVer> semVer) {
            this.name = Preconditions.checkNotNull(moduleName, "Module name must not be null.");
            this.revision = revision;
            this.semVer = semVer.or(Module.DEFAULT_SEMANTIC_VERSION);
        }

        @Override
        public String getModuleName() {
            return this.name;
        }

        @Override
        public Date getRevision() {
            return this.revision;
        }

        @Override
        public SemVer getSemanticVersion() {
            return this.semVer;
        }

        @Override
        public String getPrefix() {
            return null;
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
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ModuleImportImpl other = (ModuleImportImpl) obj;
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            if (revision == null) {
                if (other.revision != null) {
                    return false;
                }
            } else if (!revision.equals(other.revision)) {
                return false;
            }

            if (!Objects.equals(getSemanticVersion(), other.getSemanticVersion())) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "ModuleImportImpl [name=" + name + ", revision="
                    + QName.formattedRevision(revision) + ", semanticVersion=" + getSemanticVersion() + "]";
        }
    }
}
