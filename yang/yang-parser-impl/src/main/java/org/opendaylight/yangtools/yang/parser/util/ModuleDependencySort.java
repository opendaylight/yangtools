/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.parser.util.TopologicalSort.Node;
import org.opendaylight.yangtools.yang.parser.util.TopologicalSort.NodeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a module dependency graph from provided {@link Module}s and
 * provides a {@link #sort(Module...)} method. It is topological sort and
 * returns modules in order in which they should be processed (e.g. if A imports
 * B, sort returns {B, A}).
 */
public final class ModuleDependencySort {

    private static final Date DEFAULT_REVISION = SimpleDateFormatUtil.DEFAULT_DATE_REV;
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleDependencySort.class);
    private static final Function<Node, Module> TOPOLOGY_FUNCTION = input -> {
        if (input == null) {
            return null;
        }
        return ((ModuleNodeImpl) input).getReference();
    };

    /**
     * It is not desirable to instance this class
     */
    private ModuleDependencySort() {
    }

    /**
     * Topological sort of module dependency graph.
     *
     * @param modules YANG modules
     * @return Sorted list of Modules. Modules can be further processed in
     *         returned order.
     */
    public static List<Module> sort(final Module... modules) {
        final List<TopologicalSort.Node> sorted = sortInternal(Arrays.asList(modules));
        // Cast to Module from Node and return
        return Lists.transform(sorted, TOPOLOGY_FUNCTION);
    }

    private static List<TopologicalSort.Node> sortInternal(final Iterable<Module> modules) {
        final Map<String, Map<Date, ModuleNodeImpl>> moduleGraph = createModuleGraph(modules);

        final Set<TopologicalSort.Node> nodes = Sets.newHashSet();
        for (final Map<Date, ModuleNodeImpl> map : moduleGraph.values()) {
            for (final ModuleNodeImpl node : map.values()) {
                nodes.add(node);
            }
        }

        return TopologicalSort.sort(nodes);
    }

    @VisibleForTesting
    static Map<String, Map<Date, ModuleNodeImpl>> createModuleGraph(final Iterable<Module> builders) {
        final Map<String, Map<Date, ModuleNodeImpl>> moduleGraph = Maps.newHashMap();

        processModules(moduleGraph, builders);
        processDependencies(moduleGraph, builders);

        return moduleGraph;
    }

    /**
     * Extract module:revision from module builders
     */
    private static void processDependencies(final Map<String, Map<Date, ModuleNodeImpl>> moduleGraph,
            final Iterable<Module> mmbs) {
        final Map<URI, Module> allNS = new HashMap<>();

        // Create edges in graph
        for (final Module module : mmbs) {
            final Map<String, Date> imported = Maps.newHashMap();

            String fromName;
            Date fromRevision;
            Collection<ModuleImport> imports;
            URI ns;

            fromName = module.getName();
            fromRevision = module.getRevision();
            imports = module.getImports();
            ns = module.getNamespace();

            // check for existence of module with same namespace
            if (allNS.containsKey(ns)) {
                final Module mod = allNS.get(ns);
                final String name = mod.getName();
                final Date revision = mod.getRevision();
                if (!(fromName.equals(name))) {
                    LOGGER.warn(
                            "Error while sorting module [{}, {}]: module with same namespace ({}) already loaded: [{}, {}]",
                            fromName, fromRevision, ns, name, revision);
                }
            } else {
                allNS.put(ns, module);
            }

            // no need to check if other Type of object, check is performed in
            // process modules

            if (fromRevision == null) {
                fromRevision = DEFAULT_REVISION;
            }

            for (final ModuleImport imprt : imports) {
                final String toName = imprt.getModuleName();
                final Date toRevision = imprt.getRevision() == null ? DEFAULT_REVISION : imprt.getRevision();

                final ModuleNodeImpl from = moduleGraph.get(fromName).get(fromRevision);

                final ModuleNodeImpl to = getModuleByNameAndRevision(moduleGraph, fromName, fromRevision, toName, toRevision);

                /*
                 * If it is a yang 1 module, check imports: If module is imported twice with different
                 * revisions then throw exception
                 */
                if (YangVersion.VERSION_1.toString().equals(module.getYangVersion()) && imported.get(toName) != null
                        && !imported.get(toName).equals(toRevision) && !imported.get(toName).equals(DEFAULT_REVISION)
                        && !toRevision.equals(DEFAULT_REVISION)) {
                    ex(String.format("Module:%s imported twice with different revisions:%s, %s", toName,
                            formatRevDate(imported.get(toName)), formatRevDate(toRevision)));
                }

                imported.put(toName, toRevision);

                from.addEdge(to);
            }
        }
    }

    /**
     * Get imported module by its name and revision from moduleGraph
     */
    private static ModuleNodeImpl getModuleByNameAndRevision(final Map<String, Map<Date, ModuleNodeImpl>> moduleGraph,
            final String fromName, final Date fromRevision, final String toName, final Date toRevision) {
        ModuleNodeImpl to = null;

        if (moduleGraph.get(toName) == null || !moduleGraph.get(toName).containsKey(toRevision)) {
            // If revision is not specified in import, but module exists
            // with different revisions, take first
            if (moduleGraph.get(toName) != null && !moduleGraph.get(toName).isEmpty()
                    && toRevision.equals(DEFAULT_REVISION)) {
                to = moduleGraph.get(toName).values().iterator().next();
                LOGGER.trace(String
                        .format("Import:%s:%s by module:%s:%s does not specify revision, using:%s:%s for module dependency sort",
                                toName, formatRevDate(toRevision), fromName, formatRevDate(fromRevision), to.getName(),
                                formatRevDate(to.getRevision())));
            } else {
                LOGGER.warn(String.format("Not existing module imported:%s:%s by:%s:%s", toName,
                        formatRevDate(toRevision), fromName, formatRevDate(fromRevision)));
                LOGGER.warn("Available models: {}", moduleGraph);
                ex(String.format("Not existing module imported:%s:%s by:%s:%s", toName, formatRevDate(toRevision),
                        fromName, formatRevDate(fromRevision)));
            }
        } else {
            to = moduleGraph.get(toName).get(toRevision);
        }
        return to;
    }

    private static void ex(final String message) {
        throw new YangValidationException(message);
    }

    /**
     * Extract dependencies from module builders or modules to fill dependency
     * graph
     */
    private static void processModules(final Map<String, Map<Date, ModuleNodeImpl>> moduleGraph,
            final Iterable<Module> modules) {

        // Process nodes
        for (final Module momb : modules) {

            final String name = momb.getName();
            Date rev = momb.getRevision();
            if (rev == null) {
                rev = DEFAULT_REVISION;
            }

            if (moduleGraph.get(name) == null) {
                moduleGraph.put(name, Maps.newHashMap());
            }

            if (moduleGraph.get(name).get(rev) != null) {
                ex(String.format("Module:%s with revision:%s declared twice", name, formatRevDate(rev)));
            }

            moduleGraph.get(name).put(rev, new ModuleNodeImpl(name, rev, momb));
        }
    }

    private static String formatRevDate(final Date rev) {
        return rev.equals(DEFAULT_REVISION) ? "default" : SimpleDateFormatUtil.getRevisionFormat().format(rev);
    }

    @VisibleForTesting
    static class ModuleNodeImpl extends NodeImpl {
        private final String name;
        private final Date revision;
        private final Module originalObject;

        public ModuleNodeImpl(final String name, final Date revision, final Module module) {
            this.name = name;
            this.revision = revision;
            this.originalObject = module;
        }

        public String getName() {
            return name;
        }

        public Date getRevision() {
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
