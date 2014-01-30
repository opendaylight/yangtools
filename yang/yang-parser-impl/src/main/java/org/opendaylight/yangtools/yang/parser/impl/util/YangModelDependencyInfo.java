/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.parser.impl.util;

import static org.opendaylight.yangtools.yang.parser.util.ParserListenerUtils.getArgumentString;
import static org.opendaylight.yangtools.yang.parser.util.ParserListenerUtils.getFirstContext;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

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
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

public abstract class YangModelDependencyInfo {

    private final String name;
    private final String formattedRevision;
    private final Date revision;
    private final ImmutableSet<ModuleImport> submoduleIncludes;
    private final ImmutableSet<ModuleImport> moduleImports;
    private final ImmutableSet<ModuleImport> dependencies;

    public YangModelDependencyInfo(String name, String formattedRevision, ImmutableSet<ModuleImport> imports,
            ImmutableSet<ModuleImport> includes) {
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

    public ImmutableSet<ModuleImport> getDependencies() {
        return dependencies;
    }

    public String getName() {
        return name;
    }

    public String getFormattedRevision() {
        return formattedRevision;
    }

    public Date getRevision() {
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
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof YangModelDependencyInfo))
            return false;
        YangModelDependencyInfo other = (YangModelDependencyInfo) obj;
        if (formattedRevision == null) {
            if (other.formattedRevision != null)
                return false;
        } else if (!formattedRevision.equals(other.formattedRevision))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    public static YangModelDependencyInfo fromInputStream(InputStream yangStream) {
        YangContext yangContext = YangParserImpl.parseStreamWithoutErrorListeners(yangStream);

        Optional<Module_stmtContext> moduleCtx = getFirstContext(yangContext, Module_stmtContext.class);
        if (moduleCtx.isPresent()) {
            return fromModuleContext(moduleCtx.get());
        }
        Optional<Submodule_stmtContext> submoduleCtx = getFirstContext(yangContext, Submodule_stmtContext.class);
        if (submoduleCtx.isPresent()) {
            return fromSubmoduleContext(submoduleCtx.get());
        }
        throw new IllegalArgumentException("Supplied stream is not valid yang file.");
    }

    private static YangModelDependencyInfo fromModuleContext(Module_stmtContext module) {
        String name = getArgumentString(module);
        // String prefix =
        // getArgumentString(module.module_header_stmts().prefix_stmt(0));
        String namespace = getArgumentString(module.module_header_stmts().namespace_stmt(0));
        String latestRevision = getLatestRevision(module.revision_stmts());
        ImmutableSet<ModuleImport> imports = getImports(module.linkage_stmts().import_stmt());
        ImmutableSet<ModuleImport> includes = getIncludes(module.linkage_stmts().include_stmt());

        return new ModuleDependencyInfo(name, latestRevision, namespace, imports, includes);
    }

    private static ImmutableSet<ModuleImport> getImports(List<Import_stmtContext> importStatements) {
        ImmutableSet.Builder<ModuleImport> builder = ImmutableSet.builder();
        for (Import_stmtContext importStmt : importStatements) {
            String moduleName = getArgumentString(importStmt);
            Date revision = getRevision(importStmt.revision_date_stmt());
            builder.add(new ModuleImportImpl(moduleName, revision));
        }
        return builder.build();
    }

    private static String getLatestRevision(Revision_stmtsContext revision_stmts) {
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

    private static YangModelDependencyInfo fromSubmoduleContext(Submodule_stmtContext submodule) {
        String name = getArgumentString(submodule);
        Belongs_to_stmtContext belongsToStmt = submodule.submodule_header_stmts().belongs_to_stmt(0);
        String belongsTo = getArgumentString(belongsToStmt);

        String latestRevision = getLatestRevision(submodule.revision_stmts());
        ImmutableSet<ModuleImport> imports = getImports(submodule.linkage_stmts().import_stmt());
        ImmutableSet<ModuleImport> includes = getIncludes(submodule.linkage_stmts().include_stmt());

        return new SubmoduleDependencyInfo(name, latestRevision, belongsTo, imports, includes);
    }

    private static ImmutableSet<ModuleImport> getIncludes(List<Include_stmtContext> importStatements) {
        ImmutableSet.Builder<ModuleImport> builder = ImmutableSet.builder();
        for (Include_stmtContext importStmt : importStatements) {
            String moduleName = getArgumentString(importStmt);
            Date revision = getRevision(importStmt.revision_date_stmt());
            builder.add(new ModuleImportImpl(moduleName, revision));
        }
        return builder.build();
    }

    private static Date getRevision(Revision_date_stmtContext revision_date_stmt) {
        if (revision_date_stmt == null) {
            return null;
        }
        String formatedDate = getArgumentString(revision_date_stmt);
        return QName.parseRevision(formatedDate);
    }

    public static final class ModuleDependencyInfo extends YangModelDependencyInfo {

        private ModuleDependencyInfo(String name, String latestRevision, String namespace,
                ImmutableSet<ModuleImport> imports, ImmutableSet<ModuleImport> includes) {
            super(name, latestRevision, imports, includes);
        }

        @Override
        public String toString() {
            return "Module [name=" + getName() + ", revision=" + getRevision()
                    + ", dependencies=" + getDependencies() + "]";
        }

    }

    public static final class SubmoduleDependencyInfo extends YangModelDependencyInfo {

        private final String belongsTo;

        public String getParentModule() {
            return belongsTo;
        }

        private SubmoduleDependencyInfo(String name, String latestRevision, String belongsTo,
                ImmutableSet<ModuleImport> imports, ImmutableSet<ModuleImport> includes) {
            super(name, latestRevision, imports, includes);
            this.belongsTo = belongsTo;
        }

        @Override
        public String toString() {
            return "Submodule [name=" + getName() + ", revision=" + getRevision()
                    + ", dependencies=" + getDependencies() + "]";
        }

    }

    private static final class ModuleImportImpl implements ModuleImport {

        private Date revision;
        private String name;

        public ModuleImportImpl(String moduleName, Date revision) {
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
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ModuleImportImpl other = (ModuleImportImpl) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            if (revision == null) {
                if (other.revision != null)
                    return false;
            } else if (!revision.equals(other.revision))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "ModuleImportImpl [name=" + name + ", revision=" + QName.formattedRevision(revision) + "]";
        }


    }
}
