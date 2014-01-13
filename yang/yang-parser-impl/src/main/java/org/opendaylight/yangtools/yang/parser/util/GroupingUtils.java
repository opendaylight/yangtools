/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.RpcDefinitionBuilder;

public final class GroupingUtils {

    private GroupingUtils() {
    }

    /**
     * Search given modules for grouping by name defined in uses node.
     *
     * @param usesBuilder
     *            builder of uses statement
     * @param modules
     *            all loaded modules
     * @param module
     *            current module
     * @return grouping with given name if found, null otherwise
     */
    public static GroupingBuilder getTargetGroupingFromModules(final UsesNodeBuilder usesBuilder,
            final Map<String, TreeMap<Date, ModuleBuilder>> modules, final ModuleBuilder module) {
        final int line = usesBuilder.getLine();

        final String groupingString = usesBuilder.getGroupingPathAsString();
        String groupingPrefix;
        String groupingName;

        if (groupingString.contains(":")) {
            String[] splitted = groupingString.split(":");
            if (splitted.length != 2 || groupingString.contains("/")) {
                throw new YangParseException(module.getName(), line, "Invalid name of target grouping");
            }
            groupingPrefix = splitted[0];
            groupingName = splitted[1];
        } else {
            groupingPrefix = module.getPrefix();
            groupingName = groupingString;
        }

        ModuleBuilder dependentModule;
        if(groupingPrefix == null) {
            dependentModule = module;
        }
        if (groupingPrefix.equals(module.getPrefix())) {
            dependentModule = module;
        } else {
            dependentModule = ParserUtils.findModuleFromBuilders(modules, module, groupingPrefix, line);
        }

        if (dependentModule == null) {
            return null;
        }

        GroupingBuilder result;
        Set<GroupingBuilder> groupings = dependentModule.getGroupingBuilders();
        result = findGroupingBuilder(groupings, groupingName);
        if (result != null) {
            return result;
        }

        Builder parent = usesBuilder.getParent();

        while (parent != null) {
            if (parent instanceof DataNodeContainerBuilder) {
                groupings = ((DataNodeContainerBuilder) parent).getGroupingBuilders();
            } else if (parent instanceof RpcDefinitionBuilder) {
                groupings = ((RpcDefinitionBuilder) parent).getGroupings();
            }
            result = findGroupingBuilder(groupings, groupingName);
            if (result == null) {
                parent = parent.getParent();
            } else {
                break;
            }
        }

        if (result == null) {
            throw new YangParseException(module.getName(), line, "Referenced grouping '" + groupingName
                    + "' not found.");
        }
        return result;
    }

    /**
     * Search context for grouping by name defined in uses node.
     *
     * @param usesBuilder
     *            builder of uses statement
     * @param module
     *            current module
     * @param context
     *            SchemaContext containing already resolved modules
     * @return grouping with given name if found, null otherwise
     */
    public static GroupingDefinition getTargetGroupingFromContext(final UsesNodeBuilder usesBuilder,
            final ModuleBuilder module, final SchemaContext context) {
        final int line = usesBuilder.getLine();
        String groupingString = usesBuilder.getGroupingPathAsString();
        String groupingPrefix;
        String groupingName;

        if (groupingString.contains(":")) {
            String[] splitted = groupingString.split(":");
            if (splitted.length != 2 || groupingString.contains("/")) {
                throw new YangParseException(module.getName(), line, "Invalid name of target grouping");
            }
            groupingPrefix = splitted[0];
            groupingName = splitted[1];
        } else {
            groupingPrefix = module.getPrefix();
            groupingName = groupingString;
        }

        Module dependentModule = ParserUtils.findModuleFromContext(context, module, groupingPrefix, line);
        return findGroupingDefinition(dependentModule.getGroupings(), groupingName);
    }

    /**
     * Find grouping by name.
     *
     * @param groupings
     *            collection of grouping builders to search
     * @param name
     *            name of grouping
     * @return grouping with given name if present in collection, null otherwise
     */
    public static GroupingBuilder findGroupingBuilder(Set<GroupingBuilder> groupings, String name) {
        for (GroupingBuilder grouping : groupings) {
            if (grouping.getQName().getLocalName().equals(name)) {
                return grouping;
            }
        }
        return null;
    }

    /**
     * Find grouping by name.
     *
     * @param groupings
     *            collection of grouping definitions to search
     * @param name
     *            name of grouping
     * @return grouping with given name if present in collection, null otherwise
     */
    public static GroupingDefinition findGroupingDefinition(Set<GroupingDefinition> groupings, String name) {
        for (GroupingDefinition grouping : groupings) {
            if (grouping.getQName().getLocalName().equals(name)) {
                return grouping;
            }
        }
        return null;
    }

    /**
     * Perform refinement of uses target grouping nodes. Uses process has to be
     * already performed.
     *
     * @param usesNode
     *            uses node containing refine statements
     */
    public static void performRefine(UsesNodeBuilder usesNode) {
        for (RefineHolder refine : usesNode.getRefines()) {
            String refineTargetPath = refine.getName();

            String[] splitted = refineTargetPath.split("/");
            Builder currentNode = usesNode.getParent();
            for (String pathElement : splitted) {
                if (currentNode instanceof DataNodeContainerBuilder) {
                    currentNode = ((DataNodeContainerBuilder) currentNode).getDataChildByName(pathElement);
                } else if (currentNode instanceof ChoiceBuilder) {
                    currentNode = ((ChoiceBuilder) currentNode).getCaseNodeByName(pathElement);
                }
            }

            DataSchemaNodeBuilder nodeToRefine = (DataSchemaNodeBuilder) currentNode;
            if (nodeToRefine == null) {
                throw new YangParseException(refine.getModuleName(), refine.getLine(), "Refine target node '"
                        + refine.getName() + "' not found");
            }
            RefineUtils.performRefine(nodeToRefine, refine);
            usesNode.addRefineNode(nodeToRefine);
        }
    }

    public static class UsesComparator implements Comparator<UsesNodeBuilder> {
        @Override
        public int compare(UsesNodeBuilder o1, UsesNodeBuilder o2) {
            return getElementPosition(o2) - getElementPosition(o1);
        }
    }

    private static int getElementPosition(UsesNodeBuilder usesNode) {
        int i = 0;
        Builder parent = usesNode.getParent();
        while (!(parent instanceof ModuleBuilder)) {
            parent = parent.getParent();
            i++;
        }
        return i;
    }

}
