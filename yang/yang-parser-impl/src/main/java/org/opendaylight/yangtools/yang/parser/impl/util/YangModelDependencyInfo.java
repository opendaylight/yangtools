/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.parser.impl.util;

import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.getArgumentString;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.getFirstContext;

import com.google.common.base.Optional;
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
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.YangContext;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

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
            final ImmutableSet<ModuleImport> imports, final ImmutableSet<ModuleImport> includes) {
        this.name = name;
        this.formattedRevision = formattedRevision;
        this.revision = QName.parseRevision(formattedRevision);
        this.moduleImports = imports;
        this.submoduleIncludes = includes;
        this.dependencies = ImmutableSet.<ModuleImport> builder() //
                .addAll(moduleImports) //
                .addAll(submoduleIncludes) //
                .build();
    }

    /**
     * Returns immutable collection of all module imports.
     *
     * This collection contains both <code>import</code> statements
     * and <code>include</code> statements for submodules.
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
        result = prime * result + ((formattedRevision == null) ? 0 : formattedRevision.hashCode());
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
     * Extracts {@link YangModelDependencyInfo} from an abstract syntax tree
     * of a YANG model.
     *
     * @param tree Abstract syntax tree
     * @return {@link YangModelDependencyInfo}
     * @throws YangSyntaxErrorException
     *             If the AST is not a valid YANG module/submodule
     */
    public static YangModelDependencyInfo fromAST(final String name, final ParserRuleContext tree) throws YangSyntaxErrorException {
        final Optional<Module_stmtContext> moduleCtx = getFirstContext(tree, Module_stmtContext.class);
        if (moduleCtx.isPresent()) {
            return parseModuleContext(moduleCtx.get());
        }

        final Optional<Submodule_stmtContext> submoduleCtx = getFirstContext(tree, Submodule_stmtContext.class);
        if (submoduleCtx.isPresent()) {
            return parseSubmoduleContext(submoduleCtx.get());
        }

        throw new YangSyntaxErrorException(name, 0, 0, "Unknown YANG text type");
    }

    /**
     * Extracts {@link YangModelDependencyInfo} from input stream
     * containing YANG model.
     *
     * This parsing does not validate full YANG module, only
     * parses header up to the revisions and imports.
     *
     * @param yangStream
     *            Opened Input stream containing text source of YANG model
     * @return {@link YangModelDependencyInfo}
     * @throws IllegalArgumentException
     *             If input stream is not valid YANG stream
     */
    public static YangModelDependencyInfo fromInputStream(final InputStream yangStream) {
        YangContext yangContext = YangParserImpl.parseStreamWithoutErrorListeners(yangStream);

        Optional<Module_stmtContext> moduleCtx = getFirstContext(yangContext, Module_stmtContext.class);
        if (moduleCtx.isPresent()) {
            return parseModuleContext(moduleCtx.get());
        }
        Optional<Submodule_stmtContext> submoduleCtx = getFirstContext(yangContext, Submodule_stmtContext.class);
        if (submoduleCtx.isPresent()) {
            return parseSubmoduleContext(submoduleCtx.get());
        }
        throw new IllegalArgumentException("Supplied stream is not valid yang file.");
    }

    private static YangModelDependencyInfo parseModuleContext(final Module_stmtContext module) {
        String name = getArgumentString(module);
        // String prefix =
        // getArgumentString(module.module_header_stmts().prefix_stmt(0));
        String namespace = getArgumentString(module.module_header_stmts().namespace_stmt(0));
        String latestRevision = getLatestRevision(module.revision_stmts());
        ImmutableSet<ModuleImport> imports = parseImports(module.linkage_stmts().import_stmt());
        ImmutableSet<ModuleImport> includes = parseIncludes(module.linkage_stmts().include_stmt());

        return new ModuleDependencyInfo(name, latestRevision, namespace, imports, includes);
    }

    private static ImmutableSet<ModuleImport> parseImports(final List<Import_stmtContext> importStatements) {
        ImmutableSet.Builder<ModuleImport> builder = ImmutableSet.builder();
        for (Import_stmtContext importStmt : importStatements) {
            String moduleName = getArgumentString(importStmt);
            Date revision = getRevision(importStmt.revision_date_stmt());
            builder.add(new ModuleImportImpl(moduleName, revision));
        }
        return builder.build();
    }

    private static String getLatestRevision(final Revision_stmtsContext revision_stmts) {
        List<Revision_stmtContext> revisions = revision_stmts.getRuleContexts(Revision_stmtContext.class);
        String latestRevision = null;
        for (Revision_stmtContext revisionStmt : revisions) {
            String currentRevision = getArgumentString(revisionStmt);
            if (latestRevision == null || latestRevision.compareTo(currentRevision) == -1) {
                latestRevision = currentRevision;
            }
        }
        return latestRevision;
    }

    private static YangModelDependencyInfo parseSubmoduleContext(final Submodule_stmtContext submodule) {
        String name = getArgumentString(submodule);
        Belongs_to_stmtContext belongsToStmt = submodule.submodule_header_stmts().belongs_to_stmt(0);
        String belongsTo = getArgumentString(belongsToStmt);

        String latestRevision = getLatestRevision(submodule.revision_stmts());
        ImmutableSet<ModuleImport> imports = parseImports(submodule.linkage_stmts().import_stmt());
        ImmutableSet<ModuleImport> includes = parseIncludes(submodule.linkage_stmts().include_stmt());

        return new SubmoduleDependencyInfo(name, latestRevision, belongsTo, imports, includes);
    }

    private static ImmutableSet<ModuleImport> parseIncludes(final List<Include_stmtContext> importStatements) {
        ImmutableSet.Builder<ModuleImport> builder = ImmutableSet.builder();
        for (Include_stmtContext importStmt : importStatements) {
            String moduleName = getArgumentString(importStmt);
            Date revision = getRevision(importStmt.revision_date_stmt());
            builder.add(new ModuleImportImpl(moduleName, revision));
        }
        return builder.build();
    }

    private static Date getRevision(final Revision_date_stmtContext revision_date_stmt) {
        if (revision_date_stmt == null) {
            return null;
        }
        String formatedDate = getArgumentString(revision_date_stmt);
        return QName.parseRevision(formatedDate);
    }

    /**
     *
     * Dependency information for YANG module.
     *
     */
    public static final class ModuleDependencyInfo extends YangModelDependencyInfo {

        private ModuleDependencyInfo(final String name, final String latestRevision, final String namespace,
                final ImmutableSet<ModuleImport> imports, final ImmutableSet<ModuleImport> includes) {
            super(name, latestRevision, imports, includes);
        }

        @Override
        public String toString() {
            return "Module [name=" + getName() + ", revision=" + getRevision() + ", dependencies=" + getDependencies()
                    + "]";
        }
    }

    /**
     *
     * Dependency information for submodule, also provides name
     * for parent module.
     *
     */
    public static final class SubmoduleDependencyInfo extends YangModelDependencyInfo {

        private final String belongsTo;

        /**
         * Returns name of parent module.
         *
         */
        public String getParentModule() {
            return belongsTo;
        }

        private SubmoduleDependencyInfo(final String name, final String latestRevision, final String belongsTo,
                final ImmutableSet<ModuleImport> imports, final ImmutableSet<ModuleImport> includes) {
            super(name, latestRevision, imports, includes);
            this.belongsTo = belongsTo;
        }

        @Override
        public String toString() {
            return "Submodule [name=" + getName() + ", revision=" + getRevision() + ", dependencies="
                    + getDependencies() + "]";
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
            result = prime * result + ((revision == null) ? 0 : revision.hashCode());
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
            return "ModuleImportImpl [name=" + name + ", revision=" + QName.formattedRevision(revision) + "]";
        }
    }
}
