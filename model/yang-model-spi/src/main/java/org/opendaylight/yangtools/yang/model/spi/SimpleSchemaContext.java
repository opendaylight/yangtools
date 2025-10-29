/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationSchemaNode;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationSchemaNodeAwareSchemaContext;
import org.opendaylight.yangtools.yang.common.AnnotationName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.Module;

/**
 * Simple subclass of {@link AbstractSchemaContext} which performs some amount of indexing to speed up common
 * SchemaContext operations. This implementation assumes input modules are consistent and does not perform
 * any extensive analysis to ensure the resulting object complies to SchemaContext interface.
 */
@Beta
public class SimpleSchemaContext extends AbstractSchemaContext implements AnnotationSchemaNodeAwareSchemaContext {
    private final ImmutableSetMultimap<XMLNamespace, Module> namespaceToModules;
    private final ImmutableSetMultimap<String, Module> nameToModules;
    private final ImmutableMap<QNameModule, Module> moduleMap;
    private final ImmutableSet<Module> modules;
    private final ImmutableMap<AnnotationName, AnnotationSchemaNode> annotations;

    protected SimpleSchemaContext(final Collection<? extends @NonNull Module> modules) {
        /*
         * Instead of doing this on each invocation of getModules(), pre-compute it once and keep it around -- better
         * than the set we got in.
         *
         * Note we are performing two sort operations: the dependency sort takes care of detecting multiple imports,
         * performing sorting as a side-effect, but we really want the modules sorted to comply with getModules().
         */
        final var sortedModules = new ArrayList<>(ModuleDependencySort.sort(modules));
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
        final var nsMap = Multimaps.<XMLNamespace, Module>newSetMultimap(new TreeMap<>(),
            AbstractSchemaContext::createModuleSet);
        final var nameMap = Multimaps.<String, Module>newSetMultimap(new TreeMap<>(),
            AbstractSchemaContext::createModuleSet);
        final var moduleMapBuilder = ImmutableMap.<QNameModule, Module>builder();
        for (var module : modules) {
            nameMap.put(module.getName(), module);
            nsMap.put(module.getNamespace(), module);
            moduleMapBuilder.put(module.getQNameModule(), module);
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
    public static SimpleSchemaContext forModules(final Collection<? extends Module> modules) {
        return new SimpleSchemaContext(modules);
    }

    @Override
    public final Set<Module> getModules() {
        return modules;
    }

    @Override
    public final Optional<AnnotationSchemaNode> findAnnotation(final AnnotationName qname) {
        return Optional.ofNullable(annotations.get(requireNonNull(qname)));
    }

    @Override
    protected Map<QNameModule, Module> getModuleMap() {
        return moduleMap;
    }

    @Override
    protected final SetMultimap<XMLNamespace, Module> getNamespaceToModules() {
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
