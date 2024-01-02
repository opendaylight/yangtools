/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.ir.YangIRSchemaSource;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.source.ModuleSourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.SubmoduleSourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.Import;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.Include;
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
     * @param source Source identifier
     * @param rootStatement root statement
     * @return {@link YangModelDependencyInfo}
     * @throws IllegalArgumentException If the root statement is not a valid YANG module/submodule
     */
    static @NonNull YangModelDependencyInfo forIR(final IRStatement rootStatement, final SourceIdentifier source) {
        return forIR(new YangIRSchemaSource(source, rootStatement, null));
    }

    /**
     * Extracts {@link YangModelDependencyInfo} from an intermediate representation root statement of a YANG model.
     *
     * @param source Schema source
     * @return {@link YangModelDependencyInfo}
     * @throws IllegalArgumentException If the root statement is not a valid YANG module/submodule
     */
    public static @NonNull YangModelDependencyInfo forIR(final YangIRSchemaSource source) {
        final var info = source.extractInfo();
        if (info instanceof ModuleSourceInfo module) {
            return new ModuleDependencyInfo(module.name().getLocalName(), latestRevision(module.revisions()),
                convertImports(module.imports()), convertIncludes(module.includes()));
        } else if (info instanceof SubmoduleSourceInfo submodule) {
            return new SubmoduleDependencyInfo(submodule.name().getLocalName(), latestRevision(submodule.revisions()),
                submodule.belongsTo(), convertImports(submodule.imports()), convertIncludes(submodule.includes()));
        } else {
            throw new IllegalArgumentException("Unsupported info " + info);
        }
    }

    private static @Nullable String latestRevision(final List<@NonNull Revision> revisions) {
        return revisions.stream().sorted().findFirst().map(Revision::toString).orElse(null);
    }

    private static @NonNull ImmutableSet<ModuleImport> convertImports(final ImmutableSet<@NonNull Import> imports) {
        return imports.stream()
            .map(entry -> new ModuleImportImpl(entry.name(), entry.revision()))
            .collect(ImmutableSet.toImmutableSet());
    }

    private static @NonNull ImmutableSet<ModuleImport> convertIncludes(final ImmutableSet<@NonNull Include> includes) {
        return includes.stream()
            .map(entry -> new ModuleImportImpl(entry.name(), entry.revision()))
            .collect(ImmutableSet.toImmutableSet());
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
            return "Module [name=" + getName() + ", revision=" + getRevision() + ", dependencies=" + getDependencies()
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
        private final @NonNull Unqualified moduleName;
        private final Revision revision;

        ModuleImportImpl(final @NonNull Unqualified moduleName, final @Nullable Revision revision) {
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
