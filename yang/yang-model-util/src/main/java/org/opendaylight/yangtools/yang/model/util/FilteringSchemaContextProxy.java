/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.html
 */

package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.google.common.collect.ImmutableSet.Builder;
import java.net.URI;
import java.util.*;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

@Immutable
public final class FilteringSchemaContextProxy extends AbstractSchemaContext {

    public static final Function<Module, ModuleId> MODULE_TO_MODULE_ID = new Function<Module, ModuleId>() {
        @Override
        public ModuleId apply(Module input) {
            return new ModuleId(input.getName(), input.getRevision());
        }
    };

    private final Set<Module> filteredModules;
    private final ListMultimap <String, Module> nameToModulesAll;

    //collections to be filled in with filtered data
    private final Map<ModuleIdentifier, String> identifiersToSources;
    private final SetMultimap<URI, Module> namespaceToModules;
    private final SetMultimap<String, Module> nameToModules;

    /**
     *
     * Filters yang modules
     *
     * @param delegate original SchemaContext
     * @param rootModules modules (yang schemas) to be available and all their dependencies (modules importing rootModule and whole chain of their imports)
     * @param additionalModuleIds (additional) modules (yang schemas) to be available and whole chain of their imports
     *
     * @return filtered SchemaContext
     */


    public FilteringSchemaContextProxy(final SchemaContext delegate, final Collection<ModuleId> rootModules, final Collection<ModuleId> additionalModuleIds) {

        Preconditions.checkArgument(!(rootModules==null),"Base modules cannot be null." );
        Preconditions.checkArgument(!(additionalModuleIds==null),"Additional modules cannot be null." );
        Preconditions.checkArgument(!(rootModules.isEmpty() && additionalModuleIds.isEmpty()), "Arguments are empty. Nothing to do.");

        final Builder<Module> filteredModulesBuilder = new Builder<>();

         /*
         * The most common lookup is from Namespace->Module.delegate.getModules()
         *
         * RESTCONF performs lookups based on module name only, where it wants
         * to receive the latest revision
         *
         * Invest some quality time in building up lookup tables for both.
         */
        final SetMultimap<URI, Module> nsMap = Multimaps.newSetMultimap(
                new TreeMap<URI, Collection<Module>>(), MODULE_SET_SUPPLIER);
        final SetMultimap<String, Module> nameMap = Multimaps.newSetMultimap(
                new TreeMap<String, Collection<Module>>(), MODULE_SET_SUPPLIER);

        ImmutableMap.Builder<ModuleIdentifier, String> identifiersToSourcesBuilder = ImmutableMap.builder();

        //preparing map to get all modules with one name but difference in revision
         nameToModulesAll = getStringModuleMap(delegate);

        //in case there is a particular dependancy to view filteredModules/yang models
        //dependancy is checked for module name and imports
        if(!rootModules.isEmpty()) {
            filteredModulesBuilder.addAll(Collections2.filter(delegate.getModules(), new Predicate<Module>() {
                @Override
                public boolean apply(@Nullable Module module) {
                    return checkModuleDependency(module, rootModules);
                }
            }));
        }

        if(!additionalModuleIds.isEmpty()) {
            filteredModulesBuilder.addAll(Collections2.filter(delegate.getModules(), new Predicate<Module>() {
                @Override
                public boolean apply(@Nullable Module module) {
                    return selectAdditionalModules(module, additionalModuleIds);
                }
            }));
        }

        filteredModulesBuilder.addAll(getImportedModules(
                Maps.uniqueIndex(delegate.getModules(), MODULE_TO_MODULE_ID), filteredModulesBuilder.build()));

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

    private ListMultimap<String, Module> getStringModuleMap(SchemaContext delegate) {
        return Multimaps.index(delegate.getModules(), new Function<Module, String>() {
            @Override
            public String apply(Module input) {
                return input.getName();
            }
        });
    }

    //dealing with imported module other than root and directly importing root
    private Collection<Module> getImportedModules(Map<ModuleId, Module> allModules, Set<Module> baseModules) {

        List<Module> relatedModules = Lists.newLinkedList();

        for (Module module : baseModules) {
            for (ModuleImport moduleImport : module.getImports()) {

                Date revisionDate;

                if(moduleImport.getRevision()==null) {

                    revisionDate = getLatestRevision(nameToModulesAll.get(moduleImport.getModuleName()));

                } else {

                    revisionDate = moduleImport.getRevision();
                }

                ModuleId key = new ModuleId(moduleImport.getModuleName(),revisionDate);
                Module importedModule = allModules.get(key);

                Preconditions.checkArgument(importedModule != null,  "Invalid schema, cannot find imported module: %s from module: %s, %s, modules:%s", key, module.getQNameModule(), module.getName() );
                relatedModules.add(importedModule);

                //calling imports recursive
                relatedModules.addAll(getImportedModules(allModules, Collections.singleton(importedModule)));

            }
        }

        return relatedModules;
    }

    private Date getLatestRevision(List<Module> mods) {
        return Collections.max(mods, new Comparator<Module>() {

            @Override
            public int compare(Module m1, Module m2) {

                return m1.getRevision().compareTo(m2.getRevision());
            }
        }).getRevision();
    }

    @Override
    protected Map<ModuleIdentifier, String> getIdentifiersToSources() {
        return identifiersToSources;
    }

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

    public boolean selectAdditionalModules(Module module, Collection<ModuleId> additionalModules){

        for(ModuleId additionalModule : additionalModules){

            String additionalModuleName = additionalModule.getName();
            Date revision = additionalModule.getRev();

            if(module.getName().equals(additionalModuleName) && module.getRevision().equals(revision)){
                return true;
            }
        }

        return false;
    };

    //check for any dependency regarding given string
    public boolean checkModuleDependency(Module module, Collection<ModuleId> moduleIds) {

        for (ModuleId moduleId : moduleIds) {

            String moduleDependencyName = moduleId.getName();
            Date revision = moduleId.getRev();

            //main module or submodule, revision of root module can be null
            if(module.getName().equals(moduleDependencyName) && (revision==null||revision.equals(module.getRevision()))) {
                return true;
            }

            //handling/checking imports regarding root modules
            for (ModuleImport moduleImport : module.getImports()) {

                if(moduleImport.getModuleName().equals(moduleDependencyName)) {

                    if(revision.equals(moduleImport.getRevision())) {

                        return true;

                    //the case when root module revision = null, selecting dependant module with highest revision but it's very rare
                    } else if (revision==null){

                        List mods = nameToModulesAll.get(moduleImport.getModuleName());

                        Date maxDate = getLatestRevision(mods);

                        if(maxDate.equals(moduleImport.getRevision())){
                            return true;
                        }

                    }
                }

            }

            //submodules handling
            for (Module moduleSub : module.getSubmodules()) {
                return checkModuleDependency(moduleSub, moduleIds);
            }
        }

        return false;
    };

    @Override
    public String toString() {
        return String.format("SchemaContextProxyImpl{filteredModules=%s}", filteredModules);
    }

    public static final class ModuleId {
        private final String name;
        private final Date rev;

        public ModuleId(String name, Date rev) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "No module dependency name given. Nothing to do.");
            this.name = name;
            this.rev = Preconditions.checkNotNull(rev, "No revision date given. Nothing to do.");
        }

        public String getName() {
            return name;
        }

        public Date getRev() {
            return rev;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ModuleId moduleId = (ModuleId) o;

            if (name != null ? !name.equals(moduleId.name) : moduleId.name != null) return false;
            if (rev != null ? !rev.equals(moduleId.rev) : moduleId.rev != null) return false;

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
