/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import javax.annotation.concurrent.Immutable;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

@Immutable
public final class FilteringSchemaContextProxy extends AbstractSchemaContext {

    //collection to be filled with filtered modules
    private final Set<Module> filteredModules;

    //collections to be filled in with filtered data
    private final Map<ModuleIdentifier, String> identifiersToSources;
    private final SetMultimap<URI, Module> namespaceToModules;
    private final SetMultimap<String, Module> nameToModules;

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
        Preconditions.checkNotNull(rootModules, "Base modules cannot be null.");
        Preconditions.checkNotNull(additionalModuleIds, "Additional modules cannot be null.");

        final Builder<Module> filteredModulesBuilder = new Builder<>();
        final SetMultimap<URI, Module> nsMap = Multimaps.newSetMultimap(new TreeMap<>(), MODULE_SET_SUPPLIER);
        final SetMultimap<String, Module> nameMap = Multimaps.newSetMultimap(new TreeMap<>(), MODULE_SET_SUPPLIER);

        ImmutableMap.Builder<ModuleIdentifier, String> identifiersToSourcesBuilder = ImmutableMap.builder();

        //preparing map to get all modules with one name but difference in revision
        final TreeMultimap<String, Module> nameToModulesAll = getStringModuleTreeMultimap();

        nameToModulesAll.putAll(getStringModuleMap(delegate));

        //in case there is a particular dependancy to view filteredModules/yang models
        //dependancy is checked for module name and imports
        processForRootModules(delegate, rootModules, filteredModulesBuilder);

        //adding additional modules
        processForAdditionalModules(delegate, additionalModuleIds, filteredModulesBuilder);

        filteredModulesBuilder.addAll(getImportedModules(
                Maps.uniqueIndex(delegate.getModules(), ModuleId.MODULE_TO_MODULE_ID::apply),
                filteredModulesBuilder.build(), nameToModulesAll));

        /**
         * Instead of doing this on each invocation of getModules(), pre-compute
         * it once and keep it around -- better than the set we got in.
         */
        this.filteredModules = filteredModulesBuilder.build();

        for (final Module module :filteredModules) {
            nameMap.put(module.getName(), module);
            nsMap.put(module.getNamespace(), module);
            identifiersToSourcesBuilder.put(module, module.getSource());
        }

        namespaceToModules = ImmutableSetMultimap.copyOf(nsMap);
        nameToModules = ImmutableSetMultimap.copyOf(nameMap);
        identifiersToSources = identifiersToSourcesBuilder.build();
    }

    private static TreeMultimap<String, Module> getStringModuleTreeMultimap() {
        return TreeMultimap.create(String::compareTo, REVISION_COMPARATOR);
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

    private static Multimap<String, Module> getStringModuleMap(final SchemaContext delegate) {
        return Multimaps.index(delegate.getModules(), Module::getName);
    }

    //dealing with imported module other than root and directly importing root
    private static Collection<Module> getImportedModules(final Map<ModuleId, Module> allModules,
            final Set<Module> baseModules, final TreeMultimap<String, Module> nameToModulesAll) {

        List<Module> relatedModules = Lists.newLinkedList();

        for (Module module : baseModules) {
            for (ModuleImport moduleImport : module.getImports()) {

                Date revisionDate = moduleImport.getRevision() == null
                    ? nameToModulesAll.get(moduleImport.getModuleName()).first().getRevision()
                    : moduleImport.getRevision();

                ModuleId key = new ModuleId(moduleImport.getModuleName(),revisionDate);
                Module importedModule = allModules.get(key);

                Preconditions.checkArgument(importedModule != null,
                        "Invalid schema, cannot find imported module: %s from module: %s, %s, modules:%s", key,
                        module.getQNameModule(), module.getName());
                relatedModules.add(importedModule);

                //calling imports recursive
                relatedModules.addAll(getImportedModules(allModules, Collections.singleton(importedModule),
                            nameToModulesAll));
            }
        }

        return relatedModules;
    }

    @Override
    protected Map<ModuleIdentifier, String> getIdentifiersToSources() {
        return identifiersToSources;
    }

    @Override
    public Set<Module> getModules() {
        return filteredModules;
    }

    @Override
    protected SetMultimap<URI, Module> getNamespaceToModules() {
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
    private boolean checkModuleDependency(final Module module, final Collection<ModuleId> rootModules) {

        for (ModuleId rootModule : rootModules) {

            if (rootModule.equals(new ModuleId(module.getName(), module.getRevision()))) {
                return true;
            }

            //handling/checking imports regarding root modules
            for (ModuleImport moduleImport : module.getImports()) {

                if (moduleImport.getModuleName().equals(rootModule.getName())) {

                    if (moduleImport.getRevision() != null && !moduleImport.getRevision().equals(rootModule.getRev())) {
                        return false;
                    }

                    return true;
                }
            }

            //submodules handling
            for (Module moduleSub : module.getSubmodules()) {
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
        private final String name;
        private final Date rev;

        public ModuleId(final String name, final Date rev) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(name),
                    "No module dependency name given. Nothing to do.");
            this.name = name;
            this.rev = Preconditions.checkNotNull(rev, "No revision date given. Nothing to do.");
        }

        public String getName() {
            return name;
        }

        public Date getRev() {
            return rev;
        }

        public static final Function<Module, ModuleId> MODULE_TO_MODULE_ID = input -> new ModuleId(input.getName(),
            input.getRevision());

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ModuleId)) {
                return false;
            }

            ModuleId moduleId = (ModuleId) obj;
            if (name != null ? !name.equals(moduleId.name) : moduleId.name != null) {
                return false;
            }
            if (rev != null ? !rev.equals(moduleId.rev) : moduleId.rev != null) {
                return false;
            }

            return true;
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
