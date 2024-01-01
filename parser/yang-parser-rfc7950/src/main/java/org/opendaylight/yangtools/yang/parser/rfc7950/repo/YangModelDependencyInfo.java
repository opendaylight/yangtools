/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.repo.api.YangIRSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;

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
        dependencies = ImmutableSet.<ModuleImport>builder().addAll(moduleImports).addAll(submoduleIncludes).build();
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
     * Returns revision.
     *
     * @return revision, potentially null
     */
    public Optional<Revision> getRevision() {
        return Optional.ofNullable(revision);
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
     * Returns immutable collection of all module imports. This collection contains both <code>import</code> statements
     * and <code>include</code> statements for submodules.
     *
     * @return Immutable collection of imports.
     */
    public ImmutableSet<ModuleImport> getDependencies() {
        return dependencies;
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
        return YangIRDependencyInfoExtractor.parseIR(source.getRootStatement(), source.getIdentifier());
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
        final var source = YangStatementStreamSource.create(yangText);
        return YangIRDependencyInfoExtractor.parseIR(source.rootStatement(), source.getIdentifier());
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

        SubmoduleDependencyInfo(final String name, final String latestRevision,
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
}
