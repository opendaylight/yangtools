/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.concurrent.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.parser.util.ModuleDependencySort;

@Immutable
final class SchemaContextImpl implements SchemaContext {
    private static final Comparator<Module> REVISION_COMPARATOR = new Comparator<Module>() {
        @Override
        public int compare(final Module o1, final Module o2) {
            if (o2.getRevision() == null) {
                return -1;
            }

            return o2.getRevision().compareTo(o1.getRevision());
        }
    };

    private static final Supplier<TreeSet<Module>> MODULE_SET_SUPPLIER = new Supplier<TreeSet<Module>>() {
        @Override
        public TreeSet<Module> get() {
            return new TreeSet<>(REVISION_COMPARATOR);
        }
    };

    private final Map<ModuleIdentifier, String> identifiersToSources;
    private final SetMultimap<URI, Module> namespaceToModules;
    private final SetMultimap<String, Module> nameToModules;
    private final Set<Module> modules;

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

    @Override
    public Set<DataSchemaNode> getDataDefinitions() {
        final Set<DataSchemaNode> dataDefs = new HashSet<>();
        for (Module m : modules) {
            dataDefs.addAll(m.getChildNodes());
        }
        return dataDefs;
    }

    @Override
    public Set<Module> getModules() {
        return modules;
    }

    @Override
    public Set<NotificationDefinition> getNotifications() {
        final Set<NotificationDefinition> notifications = new HashSet<>();
        for (Module m : modules) {
            notifications.addAll(m.getNotifications());
        }
        return notifications;
    }

    @Override
    public Set<RpcDefinition> getOperations() {
        final Set<RpcDefinition> rpcs = new HashSet<>();
        for (Module m : modules) {
            rpcs.addAll(m.getRpcs());
        }
        return rpcs;
    }

    @Override
    public Set<ExtensionDefinition> getExtensions() {
        final Set<ExtensionDefinition> extensions = new HashSet<>();
        for (Module m : modules) {
            extensions.addAll(m.getExtensionSchemaNodes());
        }
        return extensions;
    }

    @Override
    public Module findModuleByName(final String name, final Date revision) {
        for (final Module module : nameToModules.get(name)) {
            if (revision == null || revision.equals(module.getRevision())) {
                return module;
            }
        }

        return null;
    }

    @Override
    public Set<Module> findModuleByNamespace(final URI namespace) {
        final Set<Module> ret = namespaceToModules.get(namespace);
        return ret == null ? Collections.<Module>emptySet() : ret;
    }

    @Override
    public Module findModuleByNamespaceAndRevision(final URI namespace, final Date revision) {
        if (namespace == null) {
            return null;
        }
        for (Module module : findModuleByNamespace(namespace)) {
            if (revision == null || revision.equals(module.getRevision())) {
                return module;
            }
        }
        return null;
    }

    @Override
    public boolean isAugmenting() {
        return false;
    }

    @Override
    public boolean isAddedByUses() {
        return false;
    }

    @Override
    public boolean isConfiguration() {
        return false;
    }

    @Override
    public ConstraintDefinition getConstraints() {
        return null;
    }

    @Override
    public QName getQName() {
        return SchemaContext.NAME;
    }

    @Override
    public SchemaPath getPath() {
        return SchemaPath.ROOT;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getReference() {
        return null;
    }

    @Override
    public Status getStatus() {
        return Status.CURRENT;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        final List<UnknownSchemaNode> result = new ArrayList<>();
        for (Module module : modules) {
            result.addAll(module.getUnknownSchemaNodes());
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public Set<TypeDefinition<?>> getTypeDefinitions() {
        final Set<TypeDefinition<?>> result = new LinkedHashSet<>();
        for (Module module : modules) {
            result.addAll(module.getTypeDefinitions());
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public Set<DataSchemaNode> getChildNodes() {
        final Set<DataSchemaNode> result = new LinkedHashSet<>();
        for (Module module : modules) {
            result.addAll(module.getChildNodes());
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public Set<GroupingDefinition> getGroupings() {
        final Set<GroupingDefinition> result = new LinkedHashSet<>();
        for (Module module : modules) {
            result.addAll(module.getGroupings());
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public DataSchemaNode getDataChildByName(final QName name) {
        for (Module module : modules) {
            final DataSchemaNode result = module.getDataChildByName(name);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public DataSchemaNode getDataChildByName(final String name) {
        for (Module module : modules) {
            final DataSchemaNode result = module.getDataChildByName(name);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public Set<UsesNode> getUses() {
        return Collections.emptySet();
    }

    @Override
    public boolean isPresenceContainer() {
        return false;
    }

    @Override
    public Set<AugmentationSchema> getAvailableAugmentations() {
        return Collections.emptySet();
    }

    //FIXME: should work for submodules too
    @Override
    public Set<ModuleIdentifier> getAllModuleIdentifiers() {
        return identifiersToSources.keySet();
    }

    @Override
    public Optional<String> getModuleSource(final ModuleIdentifier moduleIdentifier) {
        String maybeSource = identifiersToSources.get(moduleIdentifier);
        return Optional.fromNullable(maybeSource);
    }

    @Override
    public String toString() {
        return "SchemaContextImpl{" +
                "modules=" + modules +
                '}';
    }
}
