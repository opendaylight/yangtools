/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.ModuleLike;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.Submodule;
import org.opendaylight.yangtools.yang.model.spi.AbstractSchemaContext;

public final class FilteringSchemaContextProxy extends AbstractSchemaContext {
    private final ImmutableMap<QNameModule, Module> moduleMap;

    //collection to be filled with filtered modules
    private final ImmutableSet<Module> filteredModules;

    //collections to be filled in with filtered data
    private final ImmutableSetMultimap<XMLNamespace, Module> namespaceToModules;
    private final ImmutableSetMultimap<String, Module> nameToModules;

    /**
     * Filters SchemaContext for yang modules.
     *
     * @param delegate original SchemaContext
     * @param rootModules modules (yang schemas) to be available and all their dependencies (modules importing
     *                    rootModule and whole chain of their imports)
     * @param additionalModuleIds (additional) modules (yang schemas) to be available and whole chain of their imports
     */
    public FilteringSchemaContextProxy(final SchemaContext delegate, final Collection<ModuleId> rootModules,
            final Set<ModuleId> additionalModuleIds) {
        requireNonNull(rootModules, "Base modules cannot be null.");
        requireNonNull(additionalModuleIds, "Additional modules cannot be null.");

        final Builder<Module> filteredModulesBuilder = new Builder<>();

        // preparing map to get all modules with one name but difference in revision
        final TreeMultimap<String, Module> nameToModulesAll = TreeMultimap.create(String::compareTo,
            REVISION_COMPARATOR);

        nameToModulesAll.putAll(getStringModuleMap(delegate));

        // in case there is a particular dependency to view filteredModules/YANG models dependency is checked
        // for module name and imports
        processForRootModules(delegate, rootModules, filteredModulesBuilder);

        // adding additional modules
        processForAdditionalModules(delegate, additionalModuleIds, filteredModulesBuilder);

        filteredModulesBuilder.addAll(getImportedModules(
                Maps.uniqueIndex(delegate.getModules(), ModuleId.MODULE_TO_MODULE_ID::apply),
                filteredModulesBuilder.build(), nameToModulesAll));

        /**
         * Instead of doing this on each invocation of getModules(), pre-compute it once and keep it around.
         */
        final List<Module> sortedModules = new ArrayList<>(filteredModulesBuilder.build());
        sortedModules.sort(NAME_REVISION_COMPARATOR);
        filteredModules = ImmutableSet.copyOf(sortedModules);

        final SetMultimap<XMLNamespace, Module> nsMap = Multimaps.newSetMultimap(new TreeMap<>(),
            AbstractSchemaContext::createModuleSet);
        final SetMultimap<String, Module> nameMap = Multimaps.newSetMultimap(new TreeMap<>(),
            AbstractSchemaContext::createModuleSet);
        final ImmutableMap.Builder<QNameModule, Module> moduleMapBuilder = ImmutableMap.builder();
        for (final Module module : filteredModules) {
            nameMap.put(module.getName(), module);
            nsMap.put(module.getNamespace(), module);
            moduleMapBuilder.put(module.getQNameModule(), module);
        }

        namespaceToModules = ImmutableSetMultimap.copyOf(nsMap);
        nameToModules = ImmutableSetMultimap.copyOf(nameMap);
        moduleMap = moduleMapBuilder.build();
    }

    private static void processForAdditionalModules(final SchemaContext delegate,
            final Set<ModuleId> additionalModuleIds, final Builder<Module> filteredModulesBuilder) {
        filteredModulesBuilder.addAll(Collections2.filter(delegate.getModules(),
            module -> selectAdditionalModules(module, additionalModuleIds)));
    }

    private void processForRootModules(final SchemaContext delegate, final Collection<ModuleId> rootModules,
            final Builder<Module> filteredModulesBuilder) {
        filteredModulesBuilder.addAll(Collections2.filter(delegate.getModules(),
            module -> checkModuleDependency(module, rootModules)));
    }

    private static Multimap<String, ? extends Module> getStringModuleMap(final SchemaContext delegate) {
        return Multimaps.index(delegate.getModules(), Module::getName);
    }

    //dealing with imported module other than root and directly importing root
    private static Collection<Module> getImportedModules(final Map<ModuleId, ? extends @NonNull Module> allModules,
            final Collection<? extends @NonNull Module> baseModules,
            final TreeMultimap<String, Module> nameToModulesAll) {

        List<Module> relatedModules = new LinkedList<>();

        for (Module module : baseModules) {
            for (ModuleImport moduleImport : module.getImports()) {
                Optional<Revision> revisionDate = moduleImport.getRevision();
                if (revisionDate.isEmpty()) {
                    revisionDate = nameToModulesAll.get(moduleImport.getModuleName().getLocalName()).first()
                        .getRevision();
                }

                ModuleId key = new ModuleId(moduleImport.getModuleName(), revisionDate);
                Module importedModule = allModules.get(key);

                Preconditions.checkArgument(importedModule != null,
                        "Invalid schema, cannot find imported module: %s from module: %s, %s, modules:%s", key,
                        module.getQNameModule(), module.getName(), allModules);
                relatedModules.add(importedModule);

                //calling imports recursive
                relatedModules.addAll(getImportedModules(allModules, Collections.singleton(importedModule),
                            nameToModulesAll));
            }
        }

        return relatedModules;
    }

    @Override
    public Set<Module> getModules() {
        return filteredModules;
    }

    @Override
    protected Map<QNameModule, Module> getModuleMap() {
        return moduleMap;
    }

    @Override
    protected SetMultimap<XMLNamespace, Module> getNamespaceToModules() {
        return namespaceToModules;
    }

    @Override
    protected SetMultimap<String, Module> getNameToModules() {
        return nameToModules;
    }

    private static boolean selectAdditionalModules(final Module module, final Set<ModuleId> additionalModules) {
        return additionalModules.contains(new ModuleId(module.getName(), module.getRevision()));
    }

    //check for any dependency regarding given string
    private boolean checkModuleDependency(final ModuleLike module, final Collection<ModuleId> rootModules) {
        for (ModuleId rootModule : rootModules) {
            if (rootModule.equals(new ModuleId(module.getName(), module.getRevision()))) {
                return true;
            }

            //handling/checking imports regarding root modules
            for (ModuleImport moduleImport : module.getImports()) {
                if (moduleImport.getModuleName().equals(rootModule.getName())) {
                    return moduleImport.getRevision().isEmpty()
                            || moduleImport.getRevision().equals(rootModule.getRev());
                }
            }

            //submodules handling
            for (Submodule moduleSub : module.getSubmodules()) {
                return checkModuleDependency(moduleSub, rootModules);
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return String.format("SchemaContextProxyImpl{filteredModules=%s}", filteredModules);
    }

    public static final class ModuleId {
        private final Unqualified name;
        private final Revision rev;

        public ModuleId(final String name, final Optional<Revision> rev) {
            this(Unqualified.of(name), rev);
        }

        public ModuleId(final Unqualified name, final Optional<Revision> rev) {
            this.name = requireNonNull(name);
            this.rev = rev.orElseThrow(() -> new IllegalArgumentException("No revision date given. Nothing to do."));
        }

        public Unqualified getName() {
            return name;
        }

        public Optional<Revision> getRev() {
            return Optional.ofNullable(rev);
        }

        public static final Function<Module, ModuleId> MODULE_TO_MODULE_ID = input -> new ModuleId(input.getName(),
            input.getRevision());

        @Override
        public boolean equals(final Object obj) {
            return this == obj || obj instanceof ModuleId other && name.equals(other.name)
                && Objects.equals(rev, other.rev);
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (rev != null ? rev.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return String.format("ModuleId{name='%s', rev=%s}",name,rev);
        }
    }
}
