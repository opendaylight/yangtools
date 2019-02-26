/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.yangtools.util.TopologicalSort;
import org.opendaylight.yangtools.util.TopologicalSort.Node;
import org.opendaylight.yangtools.util.TopologicalSort.NodeImpl;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a module dependency graph from provided {@link Module}s and provides a {@link #sort(Collection)} method.
 * It is topological sort and returns modules in order in which they should be processed (e.g. if A imports B, sort
 * returns {B, A}).
 */
@Beta
public final class ModuleDependencySort {
    private static final Logger LOG = LoggerFactory.getLogger(ModuleDependencySort.class);

    /**
     * It is not desirable to instance this class.
     */
    private ModuleDependencySort() {
        throw new UnsupportedOperationException();
    }

    /**
     * Topological sort of module dependency graph.
     *
     * @param modules YANG modules
     * @return Sorted list of Modules. Modules can be further processed in returned order.
     * @throws IllegalArgumentException when provided modules are not consistent.
     */
    public static List<Module> sort(final Module... modules) {
        return sort(Arrays.asList(modules));
    }

    /**
     * Topological sort of module dependency graph.
     *
     * @param modules YANG modules
     * @return Sorted list of Modules. Modules can be further processed in returned order.
     * @throws IllegalArgumentException when provided modules are not consistent.
     */
    public static List<Module> sort(final Collection<Module> modules) {
        final List<Node> sorted = sortInternal(modules);
        // Cast to Module from Node and return
        return Lists.transform(sorted, input -> input == null ? null : ((ModuleNodeImpl) input).getModule());
    }

    private static List<Node> sortInternal(final Collection<Module> modules) {
        final Table<URI, Optional<Revision>, ModuleNodeImpl> moduleGraph = createModuleGraph(modules);
        return TopologicalSort.sort(new HashSet<>(moduleGraph.values()));
    }

    private static Table<URI, Optional<Revision>, ModuleNodeImpl> createModuleGraph(
            final Collection<Module> builders) {
        final Table<URI, Optional<Revision>, ModuleNodeImpl> moduleGraph = HashBasedTable.create();

        processModules(moduleGraph, builders);
        processDependencies(moduleGraph, builders);

        return moduleGraph;
    }

    /**
     * Extract module:revision from modules.
     */
    private static void processDependencies(final Table<URI, Optional<Revision>, ModuleNodeImpl> moduleGraph,
            final Collection<Module> mmbs) {
        final Map<URI, Module> allNS = new HashMap<>();

        // Create edges in graph
        for (final Module module : mmbs) {
            final Map<URI, Optional<Revision>> imported = new HashMap<>();
            final String fromName = module.getName();
            final QNameModule revNamespace = module.getQNameModule();
            final URI ns = revNamespace.getNamespace();
            final Optional<Revision> fromRevision = revNamespace.getRevision();

            // check for existence of module with same namespace
            final Module prev = allNS.putIfAbsent(ns, module);
            if (prev != null) {
                final String name = prev.getName();
                if (!fromName.equals(name)) {
                    LOG.warn("Error while sorting module [{}, {}]: module with same namespace ({}) already loaded:"
                        + " [{}, {}]", fromName, fromRevision, ns, name, prev.getRevision());
                }
            }

            // no need to check if other Type of object, check is performed in process modules
            for (final QNameModule imprt : allImports(module)) {
                final URI toNs = imprt.getNamespace();
                final Optional<Revision> toRevision = imprt.getRevision();

                final ModuleNodeImpl from = moduleGraph.get(ns, fromRevision);
                final ModuleNodeImpl to = getModuleByNameAndRevision(moduleGraph, fromName, fromRevision, toNs,
                    toRevision);

                /*
                 * If it is an yang 1 module, check imports: If module is imported twice with different
                 * revisions then throw exception
                 */
                if (module.getYangVersion() == YangVersion.VERSION_1) {
                    final Optional<Revision> impRevision = imported.get(toNs);
                    if (impRevision != null && impRevision.isPresent() && !impRevision.equals(toRevision)
                            && toRevision.isPresent()) {
                        throw new IllegalArgumentException(String.format(
                            "Module:%s imported twice with different revisions:%s, %s", to.getName(),
                            formatRevDate(impRevision), formatRevDate(toRevision)));
                    }
                }

                imported.put(toNs, toRevision);
                from.addEdge(to);
            }
        }
    }

    private static Collection<QNameModule> allImports(final Module mod) {
        final Set<QNameModule> ret = new LinkedHashSet<>();
        addAllImports(ret, mod);
        for (Module sub : mod.getSubmodules()) {
            addAllImports(ret, sub);
        }
        return ret;
    }

    private static void addAllImports(final Set<QNameModule> set, final Module mod) {
        for (String prefix : mod.getBoundPrefixes()) {
            if (!prefix.equals(mod.getPrefix())) {
                set.add(mod.findModuleForPrefix(prefix).get());
            }
        }
    }

    /**
     * Get imported module by its name and revision from moduleGraph.
     */
    private static ModuleNodeImpl getModuleByNameAndRevision(
            final Table<URI, Optional<Revision>, ModuleNodeImpl> moduleGraph, final String fromName,
            final Optional<Revision> fromRevision, final URI toNs, final Optional<Revision> toRevision) {

        final ModuleNodeImpl exact = moduleGraph.get(toNs, toRevision);
        if (exact != null) {
            return exact;
        }

        // If revision is not specified in import, but module exists with different revisions, take first one
        if (!toRevision.isPresent()) {
            final Map<Optional<Revision>, ModuleNodeImpl> modulerevs = moduleGraph.row(toNs);

            if (!modulerevs.isEmpty()) {
                final ModuleNodeImpl first = modulerevs.values().iterator().next();
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Import:{}:{} by module:{}:{} does not specify revision, using:{}:{}"
                            + " for module dependency sort", toNs, formatRevDate(toRevision), fromName,
                            formatRevDate(fromRevision), first.getName(), formatRevDate(first.getRevision()));
                }
                return first;
            }
        }

        LOG.warn("Not existing module imported:{}:{} by:{}:{}", toNs, formatRevDate(toRevision), fromName,
            formatRevDate(fromRevision));
        LOG.warn("Available models: {}", moduleGraph);
        throw new IllegalArgumentException(String.format("Not existing module imported:%s:%s by:%s:%s", toNs,
            formatRevDate(toRevision), fromName, formatRevDate(fromRevision)));
    }

    /**
     * Extract dependencies from modules to fill dependency graph.
     */
    private static void processModules(final Table<URI, Optional<Revision>, ModuleNodeImpl> moduleGraph,
            final Iterable<Module> modules) {

        // Process nodes
        for (final Module momb : modules) {
            final QNameModule mod = momb.getQNameModule();
            final Optional<Revision> rev = mod.getRevision();
            final Map<Optional<Revision>, ModuleNodeImpl> revs = moduleGraph.row(mod.getNamespace());
            checkArgument(!revs.containsKey(rev), "Namespace %s is declared in twice (module %s)", mod, momb.getName());
            revs.put(rev, new ModuleNodeImpl(momb));
        }
    }

    private static String formatRevDate(final Optional<Revision> rev) {
        return rev.map(Revision::toString).orElse("default");
    }

    private static final class ModuleNodeImpl extends NodeImpl {
        private final Module module;

        ModuleNodeImpl(final Module module) {
            this.module = requireNonNull(module);
        }

        Module getModule() {
            return module;
        }

        String getName() {
            return module.getName();
        }

        Optional<Revision> getRevision() {
            return module.getRevision();
        }

        @Override
        public int hashCode() {
            return Objects.hash(module.getName(), module.getRevision());
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ModuleNodeImpl)) {
                return false;
            }
            final ModuleNodeImpl other = (ModuleNodeImpl) obj;
            return getName().equals(other.getName()) && getRevision().equals(other.getRevision());
        }

        @Override
        public String toString() {
            return "Module [name=" + getName() + ", revision=" + formatRevDate(getRevision()) + "]";
        }
    }
}
