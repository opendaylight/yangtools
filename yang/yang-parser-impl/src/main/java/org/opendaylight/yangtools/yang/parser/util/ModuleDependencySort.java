/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import static java.util.Arrays.asList;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.util.TopologicalSort.Node;
import org.opendaylight.yangtools.yang.parser.util.TopologicalSort.NodeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a module dependency graph from provided {@link ModuleBuilder}s and
 * provides a {@link #sort(ModuleBuilder...)} method. It is topological sort and
 * returns modules in order in which they should be processed (e.g. if A imports
 * B, sort returns {B, A}).
 */
public final class ModuleDependencySort {

//    private static final Date DEFAULT_REVISION = new Date(0);
    private static final Date DEFAULT_REVISION = SimpleDateFormatUtil.DEFAULT_DATE_REV;
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleDependencySort.class);
    private static final Function<Node, Module> TOPOLOGY_FUNCTION = new Function<TopologicalSort.Node, Module>() {
        @Override
        public Module apply(final TopologicalSort.Node input) {
            if (input == null) {
                return null;
            }
            ModuleOrModuleBuilder moduleOrModuleBuilder = ((ModuleNodeImpl) input).getReference();
            return moduleOrModuleBuilder.getModule();
        }
    };

    /**
     * It is not desirable to instance this class
     */
    private ModuleDependencySort() {
    }


    /**
     * Extracts {@link ModuleBuilder} from a {@link ModuleNodeImpl}.
     */
    private static final Function<TopologicalSort.Node, ModuleBuilder> NODE_TO_MODULEBUILDER = new Function<TopologicalSort.Node, ModuleBuilder>() {
        @Override
        public ModuleBuilder apply(final TopologicalSort.Node input) {
            // Cast to ModuleBuilder from Node and return
            if (input == null) {
                return null;
            }
            ModuleOrModuleBuilder moduleOrModuleBuilder = ((ModuleNodeImpl) input).getReference();
            return moduleOrModuleBuilder.getModuleBuilder();
        }
    };

    /**
     * Topological sort of module builder dependency graph.
     *
     * @return Sorted list of Module builders. Modules can be further processed
     *         in returned order.
     */
    public static List<ModuleBuilder> sort(final ModuleBuilder... builders) {
        return sort(asList(builders));
    }

    public static List<ModuleBuilder> sort(final Collection<ModuleBuilder> builders) {
        List<TopologicalSort.Node> sorted = sortInternal(ModuleOrModuleBuilder.fromAll(
                Collections.<Module>emptySet(),builders));
        return Lists.transform(sorted, NODE_TO_MODULEBUILDER);
    }

    public static List<ModuleBuilder> sortWithContext(final SchemaContext context, final ModuleBuilder... builders) {
        List<ModuleOrModuleBuilder> all = ModuleOrModuleBuilder.fromAll(context.getModules(), asList(builders));

        List<TopologicalSort.Node> sorted = sortInternal(all);
        // Cast to ModuleBuilder from Node if possible and return
        return Lists.transform(sorted, new Function<TopologicalSort.Node, ModuleBuilder>() {

            @Override
            public ModuleBuilder apply(final TopologicalSort.Node input) {
                if (input == null) {
                    return null;
                }
                ModuleOrModuleBuilder moduleOrModuleBuilder = ((ModuleNodeImpl) input).getReference();
                if (moduleOrModuleBuilder.isModuleBuilder()) {
                    return moduleOrModuleBuilder.getModuleBuilder();
                } else {
                    return null;
                }
            }
        });
    }

    /**
     * Topological sort of module dependency graph.
     *
     * @return Sorted list of Modules. Modules can be further processed in
     *         returned order.
     */
    public static List<Module> sort(final Module... modules) {
        List<TopologicalSort.Node> sorted = sortInternal(ModuleOrModuleBuilder.fromAll(asList(modules),
                Collections.<ModuleBuilder>emptyList()));
        // Cast to Module from Node and return
        return Lists.transform(sorted, TOPOLOGY_FUNCTION);
    }

    private static List<TopologicalSort.Node> sortInternal(final Iterable<ModuleOrModuleBuilder> modules) {
        Map<String, Map<Date, ModuleNodeImpl>> moduleGraph = createModuleGraph(modules);

        Set<TopologicalSort.Node> nodes = Sets.newHashSet();
        for (Map<Date, ModuleNodeImpl> map : moduleGraph.values()) {
            for (ModuleNodeImpl node : map.values()) {
                nodes.add(node);
            }
        }

        return TopologicalSort.sort(nodes);
    }

    @VisibleForTesting
    static Map<String, Map<Date, ModuleNodeImpl>> createModuleGraph(final Iterable<ModuleOrModuleBuilder> builders) {
        Map<String, Map<Date, ModuleNodeImpl>> moduleGraph = Maps.newHashMap();

        processModules(moduleGraph, builders);
        processDependencies(moduleGraph, builders);

        return moduleGraph;
    }

    /**
     * Extract module:revision from module builders
     */
    private static void processDependencies(final Map<String, Map<Date, ModuleNodeImpl>> moduleGraph,
            final Iterable<ModuleOrModuleBuilder> mmbs) {
        Map<URI, ModuleOrModuleBuilder> allNS = new HashMap<>();

        // Create edges in graph
        for (ModuleOrModuleBuilder mmb : mmbs) {
            Map<String, Date> imported = Maps.newHashMap();

            String fromName;
            Date fromRevision;
            Collection<ModuleImport> imports;
            URI ns;

            if (mmb.isModule()) {
                Module module = mmb.getModule();
                fromName = module.getName();
                fromRevision = module.getRevision();
                imports = module.getImports();
                ns = module.getNamespace();
            } else {
                ModuleBuilder moduleBuilder = mmb.getModuleBuilder();
                fromName = moduleBuilder.getName();
                fromRevision = moduleBuilder.getRevision();
                imports = moduleBuilder.getImports().values();
                ns = moduleBuilder.getNamespace();
            }

            // check for existence of module with same namespace
            if (allNS.containsKey(ns)) {
                ModuleOrModuleBuilder mod = allNS.get(ns);
                String name = null;
                Date revision = null;
                if (mod.isModule()) {
                    name = mod.getModule().getName();
                    revision = mod.getModule().getRevision();
                } else if (mod.isModuleBuilder()) {
                    name = mod.getModuleBuilder().getName();
                    revision = mod.getModuleBuilder().getRevision();
                }
                if (!(fromName.equals(name))) {
                    LOGGER.warn(
                            "Error while sorting module [{}, {}]: module with same namespace ({}) already loaded: [{}, {}]",
                            fromName, fromRevision, ns, name, revision);
                }
            } else {
                allNS.put(ns, mmb);
            }

            // no need to check if other Type of object, check is performed in
            // process modules

            if (fromRevision == null) {
                fromRevision = DEFAULT_REVISION;
            }

            for (ModuleImport imprt : imports) {
                String toName = imprt.getModuleName();
                Date toRevision = imprt.getRevision() == null ? DEFAULT_REVISION : imprt.getRevision();

                ModuleNodeImpl from = moduleGraph.get(fromName).get(fromRevision);

                ModuleNodeImpl to = getModuleByNameAndRevision(moduleGraph, fromName, fromRevision, toName, toRevision);

                /*
                 * Check imports: If module is imported twice with different
                 * revisions then throw exception
                 */
                if (imported.get(toName) != null && !imported.get(toName).equals(toRevision)
                        && !imported.get(toName).equals(DEFAULT_REVISION) && !toRevision.equals(DEFAULT_REVISION)) {
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
            final Iterable<ModuleOrModuleBuilder> builders) {

        // Process nodes
        for (ModuleOrModuleBuilder momb : builders) {

            String name;
            Date rev;

            if (momb.isModule()) {
                name = momb.getModule().getName();
                rev = momb.getModule().getRevision();
            } else {
                name = momb.getModuleBuilder().getName();
                rev = momb.getModuleBuilder().getRevision();
            }

            if (rev == null) {
                rev = DEFAULT_REVISION;
            }

            if (moduleGraph.get(name) == null) {
                moduleGraph.put(name, Maps.<Date, ModuleNodeImpl> newHashMap());
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
        private final ModuleOrModuleBuilder originalObject;

        public ModuleNodeImpl(final String name, final Date revision, final ModuleOrModuleBuilder builder) {
            this.name = name;
            this.revision = revision;
            this.originalObject = builder;
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
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((revision == null) ? 0 : revision.hashCode());
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
            ModuleNodeImpl other = (ModuleNodeImpl) obj;
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

        public ModuleOrModuleBuilder getReference() {
            return originalObject;
        }

    }

}
class ModuleOrModuleBuilder {
    private final Optional<Module> maybeModule;
    private final Optional<ModuleBuilder> maybeModuleBuilder;

    ModuleOrModuleBuilder(final Module module) {
        maybeModule = Optional.of(module);
        maybeModuleBuilder = Optional.absent();
    }

    ModuleOrModuleBuilder(final ModuleBuilder moduleBuilder) {
        maybeModule = Optional.absent();
        maybeModuleBuilder = Optional.of(moduleBuilder);
    }
    boolean isModule(){
        return maybeModule.isPresent();
    }
    boolean isModuleBuilder(){
        return maybeModuleBuilder.isPresent();
    }
    Module getModule(){
        return maybeModule.get();
    }
    ModuleBuilder getModuleBuilder(){
        return maybeModuleBuilder.get();
    }

    static List<ModuleOrModuleBuilder> fromAll(final Collection<Module> modules, final Collection<ModuleBuilder> moduleBuilders) {
        List<ModuleOrModuleBuilder> result = new ArrayList<>(modules.size() + moduleBuilders.size());
        for(Module m: modules){
            result.add(new ModuleOrModuleBuilder(m));
        }
        for (ModuleBuilder mb : moduleBuilders) {
            result.add(new ModuleOrModuleBuilder(mb));
        }
        return result;
    }
}
