/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationSchemaNode;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationSchemaNodeAwareSchemaContext;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Module;

/**
 * Simple subclass of {@link AbstractSchemaContext} which performs some amount of indexing to speed up common
 * SchemaContext operations. This implementation assumes input modules are consistent and does not perform
 * any extensive analysis to ensure the resulting object complies to SchemaContext interface.
 */
@Beta
public class SimpleSchemaContext extends AbstractSchemaContext implements AnnotationSchemaNodeAwareSchemaContext {
    private final ImmutableSetMultimap<URI, Module> namespaceToModules;
    private final ImmutableSetMultimap<String, Module> nameToModules;
    private final ImmutableMap<QNameModule, Module> moduleMap;
    private final ImmutableSet<Module> modules;
    private final ImmutableMap<QName, AnnotationSchemaNode> annotations;

    protected SimpleSchemaContext(final Set<Module> modules) {
        /*
         * Instead of doing this on each invocation of getModules(), pre-compute it once and keep it around -- better
         * than the set we got in.
         *
         * Note we are performing two sort operations: the dependency sort takes care of detecting multiple imports,
         * performing sorting as a side-effect, but we really want the modules sorted to comply with getModules().
         */
        final List<Module> sortedModules = new ArrayList<>(ModuleDependencySort.sort(modules));
        sortedModules.sort(NAME_REVISION_COMPARATOR);
        this.modules = ImmutableSet.copyOf(sortedModules);

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
        final Builder<QNameModule, Module> moduleMapBuilder = ImmutableMap.builder();
        for (Module m : modules) {
            nameMap.put(m.getName(), m);
            nsMap.put(m.getNamespace(), m);
            moduleMapBuilder.put(m.getQNameModule(), m);
        }

        namespaceToModules = ImmutableSetMultimap.copyOf(nsMap);
        nameToModules = ImmutableSetMultimap.copyOf(nameMap);
        moduleMap = moduleMapBuilder.build();
        annotations = ImmutableMap.copyOf(AnnotationSchemaNode.findAll(this));
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
    public final Optional<AnnotationSchemaNode> findAnnotation(final QName qname) {
        return Optional.ofNullable(annotations.get(requireNonNull(qname)));
    }

    @Override
    protected Map<QNameModule, Module> getModuleMap() {
        return moduleMap;
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
