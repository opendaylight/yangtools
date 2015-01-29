/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import com.google.common.collect.*;
import java.net.URI;
import java.util.*;
import javax.annotation.concurrent.Immutable;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.util.AbstractSchemaContext;
import org.opendaylight.yangtools.yang.parser.util.ModuleDependencySort;

@Immutable
final class SchemaContextImpl extends AbstractSchemaContext {

    protected final Map<ModuleIdentifier, String> identifiersToSources;
    protected  final SetMultimap<URI, Module> namespaceToModules;
    protected  final SetMultimap<String, Module> nameToModules;
    protected  final Set<Module> modules;

    SchemaContextImpl(final Set<Module> modules, final Map<ModuleIdentifier, String> identifiersToSources) {
        this.identifiersToSources = ImmutableMap.copyOf(identifiersToSources);

         /*
         * Instead of doing this on each invocation of getModules(), pre-compute
         * it once and keep it around -- better than the set we got in.
         */
        this.modules = ImmutableSet.copyOf(ModuleDependencySort.sort(modules.toArray(new Module[modules.size()])));

         /*
         * The most common lookup is from Namespace->Module.
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

        for (Module m : modules) {
            nameMap.put(m.getName(), m);
            nsMap.put(m.getNamespace(), m);
        }

        namespaceToModules = ImmutableSetMultimap.copyOf(nsMap);
        nameToModules = ImmutableSetMultimap.copyOf(nameMap);

    }

    public Map<ModuleIdentifier, String> getIdentifiersToSources(){

        return identifiersToSources;
    }

    public Set<Module> getModules(){

        return modules;
    }

    public SetMultimap<URI, Module> getNamespaceToModules() {

        return namespaceToModules;
    }

    public SetMultimap<String, Module> getNameToModules() {

        return nameToModules;
    }

    @Override
    public String toString() {
        return "SchemaContextImpl{" +
                "modules=" + modules +
                '}';
    }
}
