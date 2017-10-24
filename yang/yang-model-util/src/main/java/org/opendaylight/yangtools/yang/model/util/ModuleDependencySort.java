/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.annotations.Beta;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yangtools.util.TopologicalSort;
import org.opendaylight.yangtools.util.TopologicalSort.Node;
import org.opendaylight.yangtools.util.TopologicalSort.NodeImpl;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
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
    public static List<Module> sort(final Collection<Module> modules) {
        return sort((Iterable<Module>)modules);
    }

    /**
     * Topological sort of module dependency graph.
     *
     * @param modules YANG modules
     * @return Sorted list of Modules. Modules can be further processed in
     *         returned order.
     * @throws IllegalArgumentException when provided modules are not consistent.
     *
     * @deprecated Use {@link #sort(Collection)} instead.
     */
    @Deprecated
    public static List<Module> sort(final Iterable<Module> modules) {
        final List<Node> sorted = sortInternal(modules);
        // Cast to Module from Node and return
        return Lists.transform(sorted, input -> input == null ? null : ((ModuleNodeImpl) input).getReference());
    }

    private static List<Node> sortInternal(final Iterable<Module> modules) {
        final Table<String, Optional<Date>, ModuleNodeImpl> moduleGraph = createModuleGraph(modules);
        return TopologicalSort.sort(new HashSet<>(moduleGraph.values()));
    }

    private static Table<String, Optional<Date>, ModuleNodeImpl> createModuleGraph(final Iterable<Module> builders) {
        final Table<String, Optional<Date>, ModuleNodeImpl> moduleGraph = HashBasedTable.create();

        processModules(moduleGraph, builders);
        processDependencies(moduleGraph, builders);

        return moduleGraph;
    }

    /**
     * Extract module:revision from modules.
     */
    private static void processDependencies(final Table<String, Optional<Date>, ModuleNodeImpl> moduleGraph,
            final Iterable<Module> mmbs) {
        final Map<URI, Module> allNS = new HashMap<>();

        // Create edges in graph
        for (final Module module : mmbs) {
            final Map<String, Date> imported = new HashMap<>();
            final String fromName = module.getName();
            final URI ns = module.getNamespace();
            final Date fromRevision = module.getRevision();

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
            for (final ModuleImport imprt : module.getImports()) {
                final String toName = imprt.getModuleName();
                final Date toRevision = imprt.getRevision();

                final ModuleNodeImpl from = moduleGraph.get(fromName, Optional.ofNullable(fromRevision));
                final ModuleNodeImpl to = getModuleByNameAndRevision(moduleGraph, fromName, fromRevision, toName,
                    toRevision);

                /*
                 * If it is an yang 1 module, check imports: If module is imported twice with different
                 * revisions then throw exception
                 */
                if (module.getYangVersion() == YangVersion.VERSION_1) {
                    final Date impRevision = imported.get(toName);
                    if (impRevision != null && !impRevision.equals(toRevision) && toRevision != null) {
                        throw new IllegalArgumentException(String.format(
                            "Module:%s imported twice with different revisions:%s, %s", toName,
                            formatRevDate(impRevision), formatRevDate(toRevision)));
                    }
                }

                imported.put(toName, toRevision);

                from.addEdge(to);
            }
        }
    }

    /**
     * Get imported module by its name and revision from moduleGraph.
     */
    private static ModuleNodeImpl getModuleByNameAndRevision(
            final Table<String, Optional<Date>, ModuleNodeImpl> moduleGraph,
            final String fromName, final Date fromRevision, final String toName, final Date toRevision) {

        final ModuleNodeImpl exact = moduleGraph.get(toName, Optional.ofNullable(toRevision));
        if (exact != null) {
            return exact;
        }

        // If revision is not specified in import, but module exists with different revisions, take first one
        if (toRevision == null) {
            final Map<Optional<Date>, ModuleNodeImpl> modulerevs = moduleGraph.row(toName);

            if (!modulerevs.isEmpty()) {
                final ModuleNodeImpl first = modulerevs.values().iterator().next();
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Import:{}:{} by module:{}:{} does not specify revision, using:{}:{}"
                            + " for module dependency sort", toName, formatRevDate(toRevision), fromName,
                            formatRevDate(fromRevision), first.getName(), formatRevDate(first.getRevision()));
                }
                return first;
            }
        }

        LOG.warn("Not existing module imported:{}:{} by:{}:{}", toName, formatRevDate(toRevision), fromName,
            formatRevDate(fromRevision));
        LOG.warn("Available models: {}", moduleGraph);
        throw new IllegalArgumentException(String.format("Not existing module imported:%s:%s by:%s:%s", toName,
            formatRevDate(toRevision), fromName, formatRevDate(fromRevision)));
    }

    /**
     * Extract dependencies from modules to fill dependency graph.
     */
    private static void processModules(final Table<String, Optional<Date>, ModuleNodeImpl> moduleGraph,
            final Iterable<Module> modules) {

        // Process nodes
        for (final Module momb : modules) {

            final String name = momb.getName();
            final Optional<Date> rev = Optional.ofNullable(momb.getRevision());

            final Map<Optional<Date>, ModuleNodeImpl> revs = moduleGraph.row(name);
            if (revs.containsKey(rev)) {
                throw new IllegalArgumentException(String.format("Module:%s with revision:%s declared twice", name,
                    formatRevDate(rev.orElse(null))));
            }

            revs.put(rev, new ModuleNodeImpl(name, rev.orElse(null), momb));
        }
    }

    private static String formatRevDate(final Date rev) {
        return rev == null ? "default" : SimpleDateFormatUtil.getRevisionFormat().format(rev);
    }

    private static final class ModuleNodeImpl extends NodeImpl {
        private final String name;
        private final Date revision;
        private final Module originalObject;

        ModuleNodeImpl(final String name, final Date revision, final Module module) {
            this.name = name;
            this.revision = revision;
            this.originalObject = module;
        }

        String getName() {
            return name;
        }

        Date getRevision() {
            return revision;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hashCode(name);
            result = prime * result + Objects.hashCode(revision);
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ModuleNodeImpl other = (ModuleNodeImpl) obj;
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            if (revision == null) {
                if (other.revision != null) {
                    return false;
                }
            } else if (!revision.equals(other.revision)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Module [name=" + name + ", revision=" + formatRevDate(revision) + "]";
        }

        public Module getReference() {
            return originalObject;
        }
    }
}
