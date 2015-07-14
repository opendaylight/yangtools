/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.parser.impl.util;

import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.getArgumentString;

import org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils;

import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import java.util.HashSet;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser.StatementContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser;
import com.google.common.collect.ImmutableSet;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Belongs_to_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Import_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Include_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Module_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Revision_date_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Revision_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Revision_stmtsContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Submodule_stmtContext;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;

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
    private final ImmutableSet<ModuleImport> submoduleIncludes;
    private final ImmutableSet<ModuleImport> moduleImports;
    private final ImmutableSet<ModuleImport> dependencies;

    YangModelDependencyInfo(final String name, final String formattedRevision,
            final ImmutableSet<ModuleImport> imports,
            final ImmutableSet<ModuleImport> includes) {
        this.name = name;
        this.formattedRevision = formattedRevision;
        this.revision = formattedRevision == null ? null : QName
                .parseRevision(formattedRevision);
        this.moduleImports = imports;
        this.submoduleIncludes = includes;
        this.dependencies = ImmutableSet.<ModuleImport> builder()
                .addAll(moduleImports).addAll(submoduleIncludes).build();
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((formattedRevision == null) ? 0 : formattedRevision
                        .hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        YangModelDependencyInfo other = (YangModelDependencyInfo) obj;
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

        if (tree instanceof YangStatementParser.StatementContext) {
            YangStatementParser.StatementContext rootStatement = (YangStatementParser.StatementContext) tree;
            return parseAST(rootStatement);
        }

        final Optional<Module_stmtContext> moduleCtx = ParserListenerUtils
                .getFirstContext(tree, Module_stmtContext.class);
        if (moduleCtx.isPresent()) {
            return parseModuleContext(moduleCtx.get());
        }

        final Optional<Submodule_stmtContext> submoduleCtx = ParserListenerUtils
                .getFirstContext(tree, Submodule_stmtContext.class);
        if (submoduleCtx.isPresent()) {
            return parseSubmoduleContext(submoduleCtx.get());
        }

        throw new YangSyntaxErrorException(name, 0, 0, "Unknown YANG text type");
    }

    private static YangModelDependencyInfo parseAST(
            YangStatementParser.StatementContext rootStatement) {
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
        StatementContext yangAST = new YangStatementSourceImpl(yangStream)
                .getYangAST();
        return parseAST(yangAST);
    }

    private static YangModelDependencyInfo parseModuleContext(
            final Module_stmtContext module) {
        String name = getArgumentString(module);
        String latestRevision = getLatestRevision(module.revision_stmts());
        ImmutableSet<ModuleImport> imports = parseImports(module
                .linkage_stmts().import_stmt());
        ImmutableSet<ModuleImport> includes = parseIncludes(module
                .linkage_stmts().include_stmt());

        return new ModuleDependencyInfo(name, latestRevision, imports, includes);
    }

    private static YangModelDependencyInfo parseModuleContext(
            final YangStatementParser.StatementContext module) {
        String name = Utils.stringFromStringContext(module.argument());
        String latestRevision = getLatestRevision(module);
        ImmutableSet<ModuleImport> imports = parseImports(module);
        ImmutableSet<ModuleImport> includes = parseIncludes(module);

        return new ModuleDependencyInfo(name, latestRevision, imports, includes);
    }

    private static ImmutableSet<ModuleImport> parseImports(
            final YangStatementParser.StatementContext module) {
        Set<ModuleImport> result = new HashSet<>();
        List<StatementContext> subStatements = module.statement();
        for (StatementContext subStatementContext : subStatements) {
            if (subStatementContext
                    .keyword()
                    .getText()
                    .equals(Rfc6020Mapping.IMPORT.getStatementName()
                            .getLocalName())) {
                String revisionDateStr = getRevisionDateString(subStatementContext);
                String importedModuleName = Utils
                        .stringFromStringContext(subStatementContext.argument());
                Date revisionDate = (revisionDateStr == null) ? null : QName
                        .parseRevision(revisionDateStr);
                result.add(new ModuleImportImpl(importedModuleName,
                        revisionDate));
            }
        }
        return ImmutableSet.copyOf(result);
    }

    private static ImmutableSet<ModuleImport> parseIncludes(
            final YangStatementParser.StatementContext module) {
        Set<ModuleImport> result = new HashSet<>();
        List<StatementContext> subStatements = module.statement();
        for (StatementContext subStatementContext : subStatements) {
            if (subStatementContext
                    .keyword()
                    .getText()
                    .equals(Rfc6020Mapping.INCLUDE.getStatementName()
                            .getLocalName())) {
                String revisionDateStr = getRevisionDateString(subStatementContext);
                String IncludeModuleName = Utils
                        .stringFromStringContext(subStatementContext.argument());
                Date revisionDate = (revisionDateStr == null) ? null : QName
                        .parseRevision(revisionDateStr);
                result.add(new ModuleImportImpl(IncludeModuleName, revisionDate));
            }
        }
        return ImmutableSet.copyOf(result);
    }

    private static String getRevisionDateString(StatementContext importStatement) {
        List<StatementContext> importSubStatements = importStatement
                .statement();
        String revisionDateStr = null;
        for (StatementContext importSubStatement : importSubStatements) {
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

    private static ImmutableSet<ModuleImport> parseImports(
            final List<Import_stmtContext> importStatements) {
        ImmutableSet.Builder<ModuleImport> builder = ImmutableSet.builder();
        for (Import_stmtContext importStmt : importStatements) {
            String moduleName = getArgumentString(importStmt);
            Date revision = getRevision(importStmt.revision_date_stmt());
            builder.add(new ModuleImportImpl(moduleName, revision));
        }
        return builder.build();
    }

    public static String getLatestRevision(
            final YangStatementParser.StatementContext module) {
        List<StatementContext> subStatements = module.statement();
        String latestRevision = null;
        for (StatementContext subStatementContext : subStatements) {
            if (subStatementContext
                    .keyword()
                    .getText()
                    .equals(Rfc6020Mapping.REVISION.getStatementName()
                            .getLocalName())) {
                String currentRevision = Utils
                        .stringFromStringContext(subStatementContext.argument());
                if (latestRevision == null
                        || latestRevision.compareTo(currentRevision) == -1) {
                    latestRevision = currentRevision;
                }
            }
        }
        return latestRevision;
    }

    public static String getLatestRevision(
            final Revision_stmtsContext revisionStmts) {
        List<Revision_stmtContext> revisions = revisionStmts
                .getRuleContexts(Revision_stmtContext.class);
        String latestRevision = null;
        for (Revision_stmtContext revisionStmt : revisions) {
            String currentRevision = getArgumentString(revisionStmt);
            if (latestRevision == null
                    || latestRevision.compareTo(currentRevision) == -1) {
                latestRevision = currentRevision;
            }
        }
        return latestRevision;
    }

    private static YangModelDependencyInfo parseSubmoduleContext(
            final YangStatementParser.StatementContext submodule) {
        String name = Utils.stringFromStringContext(submodule.argument());
        String belongsTo = parseBelongsTo(submodule);

        String latestRevision = getLatestRevision(submodule);
        ImmutableSet<ModuleImport> imports = parseImports(submodule);
        ImmutableSet<ModuleImport> includes = parseIncludes(submodule);

        return new SubmoduleDependencyInfo(name, latestRevision, belongsTo,
                imports, includes);
    }

    private static String parseBelongsTo(StatementContext submodule) {
        List<StatementContext> subStatements = submodule.statement();
        for (StatementContext subStatementContext : subStatements) {
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

    private static YangModelDependencyInfo parseSubmoduleContext(
            final Submodule_stmtContext submodule) {
        String name = getArgumentString(submodule);
        Belongs_to_stmtContext belongsToStmt = submodule
                .submodule_header_stmts().belongs_to_stmt(0);
        String belongsTo = getArgumentString(belongsToStmt);

        String latestRevision = getLatestRevision(submodule.revision_stmts());
        ImmutableSet<ModuleImport> imports = parseImports(submodule
                .linkage_stmts().import_stmt());
        ImmutableSet<ModuleImport> includes = parseIncludes(submodule
                .linkage_stmts().include_stmt());

        return new SubmoduleDependencyInfo(name, latestRevision, belongsTo,
                imports, includes);
    }

    private static ImmutableSet<ModuleImport> parseIncludes(
            final List<Include_stmtContext> importStatements) {
        ImmutableSet.Builder<ModuleImport> builder = ImmutableSet.builder();
        for (Include_stmtContext importStmt : importStatements) {
            String moduleName = getArgumentString(importStmt);
            Date revision = getRevision(importStmt.revision_date_stmt());
            builder.add(new ModuleImportImpl(moduleName, revision));
        }
        return builder.build();
    }

    private static Date getRevision(
            final Revision_date_stmtContext revisionDateStmt) {
        if (revisionDateStmt == null) {
            return null;
        }
        String formatedDate = getArgumentString(revisionDateStmt);
        return QName.parseRevision(formatedDate);
    }

    /**
     *
     * Dependency information for YANG module.
     *
     */
    public static final class ModuleDependencyInfo extends
            YangModelDependencyInfo {

        private ModuleDependencyInfo(final String name,
                final String latestRevision,
                final ImmutableSet<ModuleImport> imports,
                final ImmutableSet<ModuleImport> includes) {
            super(name, latestRevision, imports, includes);
        }

        @Override
        public String toString() {
            return "Module [name=" + getName() + ", revision=" + getRevision()
                    + ", dependencies=" + getDependencies() + "]";
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
        private final String name;

        public ModuleImportImpl(final String moduleName, final Date revision) {
            this.name = moduleName;
            this.revision = revision;
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
        public String getPrefix() {
            return null;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result
                    + ((revision == null) ? 0 : revision.hashCode());
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
            ModuleImportImpl other = (ModuleImportImpl) obj;
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
            return true;
        }

        @Override
        public String toString() {
            return "ModuleImportImpl [name=" + name + ", revision="
                    + QName.formattedRevision(revision) + "]";
        }
    }
}
