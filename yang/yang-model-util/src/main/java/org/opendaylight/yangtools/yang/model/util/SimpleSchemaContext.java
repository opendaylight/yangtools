/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import java.net.URI;
import java.util.Set;
import java.util.TreeMap;
import org.opendaylight.yangtools.yang.model.api.Module;

/**
 * Simple subclass of {@link AbstractSchemaContext} which performs some amount of indexing to speed up common
 * SchemaContext operations. This implementation assumes input modules are consistent and does not perform
 * any extensive analysis to ensure the resulting object complies to SchemaContext interface.
 */
@Beta
public class SimpleSchemaContext extends AbstractSchemaContext {
    private final SetMultimap<URI, Module> namespaceToModules;
    private final SetMultimap<String, Module> nameToModules;
    private final Set<Module> modules;

    protected SimpleSchemaContext(final Set<Module> modules) {
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
        final SetMultimap<URI, Module> nsMap = Multimaps.newSetMultimap(new TreeMap<>(),
            AbstractSchemaContext::createModuleSet);
        final SetMultimap<String, Module> nameMap = Multimaps.newSetMultimap(new TreeMap<>(),
            AbstractSchemaContext::createModuleSet);
        for (Module m : modules) {
            nameMap.put(m.getName(), m);
            nsMap.put(m.getNamespace(), m);
        }

        namespaceToModules = ImmutableSetMultimap.copyOf(nsMap);
        nameToModules = ImmutableSetMultimap.copyOf(nameMap);
    }

    /**
     * Create a new instance from specified modules. Note that no module validation is done and hence the consistency
     * of the resulting SchemaContext is completely in hands of the caller.
     */
    public static SimpleSchemaContext forModules(final Set<Module> modules) {
        return new SimpleSchemaContext(modules);
    }

    @Override
    public final Set<Module> getModules() {
        return modules;
    }

    @Override
    protected final SetMultimap<URI, Module> getNamespaceToModules() {
        return namespaceToModules;
    }

    @Override
    protected final SetMultimap<String, Module> getNameToModules() {
        return nameToModules;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("modules", modules);
    }
}
