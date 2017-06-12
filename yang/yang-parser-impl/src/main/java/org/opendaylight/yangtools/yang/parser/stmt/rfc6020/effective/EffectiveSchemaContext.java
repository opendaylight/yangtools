/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.ModuleIdentifierImpl;
import org.opendaylight.yangtools.yang.parser.util.ModuleDependencySort;

public final class EffectiveSchemaContext extends AbstractEffectiveSchemaContext {

    private final SetMultimap<URI, Module> namespaceToModules;
    private final SetMultimap<String, Module> nameToModules;
    private final Set<Module> modules;

    private final List<DeclaredStatement<?>> rootDeclaredStatements;
    private final List<EffectiveStatement<?, ?>> rootEffectiveStatements;
    private final Set<ModuleIdentifier> moduleIdentifiers;

    public EffectiveSchemaContext(final List<DeclaredStatement<?>> rootDeclaredStatements,
            final List<EffectiveStatement<?, ?>> rootEffectiveStatements) {
        this.rootDeclaredStatements = ImmutableList.copyOf(rootDeclaredStatements);
        this.rootEffectiveStatements = ImmutableList.copyOf(rootEffectiveStatements);

        final Set<Module> modulesInit = new HashSet<>();
        for (EffectiveStatement<?, ?> rootEffectiveStatement : rootEffectiveStatements) {
            if (rootEffectiveStatement instanceof ModuleEffectiveStatementImpl) {
                Module module = (Module) rootEffectiveStatement;
                modulesInit.add(module);
            }
        }
        this.modules = ImmutableSet.copyOf(ModuleDependencySort.sort(modulesInit));

        final SetMultimap<URI, Module> nsMap = Multimaps.newSetMultimap(new TreeMap<>(), MODULE_SET_SUPPLIER);
        final SetMultimap<String, Module> nameMap = Multimaps.newSetMultimap(new TreeMap<>(), MODULE_SET_SUPPLIER);
        final Set<ModuleIdentifier> modIdBuilder = new HashSet<>();
        for (Module m : modulesInit) {
            nameMap.put(m.getName(), m);
            nsMap.put(m.getNamespace(), m);
            modIdBuilder.add(ModuleIdentifierImpl.create(m.getName(), Optional.of(m.getNamespace()),
                Optional.of(m.getRevision())));
            resolveSubmoduleIdentifiers(m.getSubmodules(), modIdBuilder);
        }

        namespaceToModules = ImmutableSetMultimap.copyOf(nsMap);
        nameToModules = ImmutableSetMultimap.copyOf(nameMap);
        moduleIdentifiers = ImmutableSet.copyOf(modIdBuilder);
    }

    public EffectiveSchemaContext(final Set<Module> modules) {

         /*
         * Instead of doing this on each invocation of getModules(), pre-compute
         * it once and keep it around -- better than the set we got in.
         */
        this.modules = ImmutableSet.copyOf(ModuleDependencySort.sort(modules));

         /*
         * The most common lookup is from Namespace->Module.
         *
         * RESTCONF performs lookups based on module name only, where it wants
         * to receive the latest revision
         *
         * Invest some quality time in building up lookup tables for both.
         */
        final SetMultimap<URI, Module> nsMap = Multimaps.newSetMultimap(
                new TreeMap<>(), MODULE_SET_SUPPLIER);
        final SetMultimap<String, Module> nameMap = Multimaps.newSetMultimap(
                new TreeMap<>(), MODULE_SET_SUPPLIER);

        Set<ModuleIdentifier> modIdBuilder = new HashSet<>();
        for (Module m : modules) {
            nameMap.put(m.getName(), m);
            nsMap.put(m.getNamespace(), m);
            modIdBuilder.add(ModuleIdentifierImpl.create(m.getName(), Optional.of(m.getNamespace()),
                Optional.of(m.getRevision())));
            resolveSubmoduleIdentifiers(m.getSubmodules(), modIdBuilder);
        }

        namespaceToModules = ImmutableSetMultimap.copyOf(nsMap);
        nameToModules = ImmutableSetMultimap.copyOf(nameMap);
        moduleIdentifiers = ImmutableSet.copyOf(modIdBuilder);

        rootDeclaredStatements = ImmutableList.of();
        rootEffectiveStatements = ImmutableList.of();
    }

    public static SchemaContext resolveSchemaContext(final Set<Module> modules) {
       return new EffectiveSchemaContext(modules);
    }

    private static void resolveSubmoduleIdentifiers(final Set<Module> submodules, final Set<ModuleIdentifier> modIdBuilder) {
        for (Module submodule : submodules) {
            modIdBuilder.add(ModuleIdentifierImpl.create(submodule.getName(),
                Optional.of(submodule.getNamespace()), Optional.of(submodule.getRevision())));
        }
    }

    public List<DeclaredStatement<?>> getRootDeclaredStatements() {
        return rootDeclaredStatements;
    }

    public List<EffectiveStatement<?, ?>> getRootEffectiveStatements() {
        return rootEffectiveStatements;
    }

    @Override
    protected Map<ModuleIdentifier, String> getIdentifiersToSources() {
        return ImmutableMap.of();
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
    public Set<ModuleIdentifier> getAllModuleIdentifiers() {
        return moduleIdentifiers;
    }

    @Override
    public String toString() {
        return String.format("EffectiveSchemaContext{modules=%s}", modules);
    }
}
