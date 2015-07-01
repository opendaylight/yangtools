/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import org.opendaylight.yangtools.yang.parser.util.ModuleDependencySort;

import java.util.HashSet;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;

public class EffectiveSchemaContext extends AbstractEffectiveSchemaContext {

    private final Map<ModuleIdentifier, String> identifiersToSources;
    private final SetMultimap<URI, Module> namespaceToModules;
    private final SetMultimap<String, Module> nameToModules;
    private final Set<Module> modules;

    private final ImmutableList<DeclaredStatement<?>> rootDeclaredStatements;
    private final ImmutableList<EffectiveStatement<?, ?>> rootEffectiveStatements;

    public EffectiveSchemaContext(
            List<DeclaredStatement<?>> rootDeclaredStatements,
            List<EffectiveStatement<?, ?>> rootEffectiveStatements) {
        this.rootDeclaredStatements = ImmutableList
                .copyOf(rootDeclaredStatements);
        this.rootEffectiveStatements = ImmutableList
                .copyOf(rootEffectiveStatements);

        Set<Module> modulesInit = new HashSet<>();
        for (EffectiveStatement<?, ?> rootEffectiveStatement : rootEffectiveStatements) {
            if (rootEffectiveStatement instanceof ModuleEffectiveStatementImpl) {
                Module module = (Module) rootEffectiveStatement;
                modulesInit.add(module);
            }
        }

        Module[] moduleArray = new Module[modulesInit.size()];
        List<Module> sortedModuleList = ModuleDependencySort.sort(modulesInit.toArray(moduleArray));
        this.modules = ImmutableSet.copyOf(sortedModuleList);

        final SetMultimap<URI, Module> nsMap = Multimaps.newSetMultimap(
                new TreeMap<URI, Collection<Module>>(), MODULE_SET_SUPPLIER);
        final SetMultimap<String, Module> nameMap = Multimaps.newSetMultimap(
                new TreeMap<String, Collection<Module>>(), MODULE_SET_SUPPLIER);

        for (Module m : modulesInit) {
            nameMap.put(m.getName(), m);
            nsMap.put(m.getNamespace(), m);
        }

        namespaceToModules = ImmutableSetMultimap.copyOf(nsMap);
        nameToModules = ImmutableSetMultimap.copyOf(nameMap);

        // :TODO init identifiersToSources
        this.identifiersToSources = null;

    }

    public ImmutableList<DeclaredStatement<?>> getRootDeclaredStatements() {
        return rootDeclaredStatements;
    }

    public ImmutableList<EffectiveStatement<?, ?>> getRootEffectiveStatements() {
        return rootEffectiveStatements;
    }

    @Override
    protected Map<ModuleIdentifier, String> getIdentifiersToSources() {

        return identifiersToSources;
    }

    @Override
    public Set<Module> getModules() {

        return modules;
    }

    @Override
    protected SetMultimap<URI, Module> getNamespaceToModules() {

        return namespaceToModules;
    }

    @Override
    protected SetMultimap<String, Module> getNameToModules() {

        return nameToModules;
    }

    @Override
    public String toString() {

        return String.format("SchemaContextImpl{modules=%s}", modules);
    }
}
