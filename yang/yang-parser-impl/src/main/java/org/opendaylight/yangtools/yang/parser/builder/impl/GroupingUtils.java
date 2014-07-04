/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import com.google.common.base.Splitter;

import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
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
import org.opendaylight.yangtools.yang.parser.builder.api.RefineBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public final class GroupingUtils {
    private static final Splitter COLON_SPLITTER = Splitter.on(':');
    private static final Splitter SLASH_SPLITTER = Splitter.on('/');

    private GroupingUtils() {
    }

    /**
     * Common string splitter. Given a string representation of a grouping's name, it creates a prefix/name
     * pair and returns it.
     *
     * @param groupingString Grouping string reference
     * @param module Module which we are processing
     * @param line Module line which we are processing
     * @return An array of two strings, first one is the module prefix, the second is the grouping name.
     */
    private static String[] getPrefixAndName(final String groupingString, final ModuleBuilder module, final int line) {
        final String[] ret = new String[2];

        if (groupingString.indexOf(':') != -1) {
            if (groupingString.indexOf('/') != -1) {
                throw new YangParseException(module.getName(), line, "Invalid name of target grouping");
            }

            final Iterator<String> split = COLON_SPLITTER.split(groupingString).iterator();
            ret[0] = split.next();
            ret[1] = split.next();
            if (split.hasNext()) {
                throw new YangParseException(module.getName(), line, "Invalid name of target grouping");
            }
        } else {
            ret[0] = module.getPrefix();
            ret[1] = groupingString;
        }

        return ret;
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

        final String[] split = getPrefixAndName(usesBuilder.getGroupingPathAsString(), module, line);
        final String groupingPrefix = split[0];
        final String groupingName = split[1];
        final ModuleBuilder dependentModule;

        if (groupingPrefix == null) {
            dependentModule = module;
        } else if (groupingPrefix.equals(module.getPrefix())) {
            dependentModule = module;
        } else {
            dependentModule = BuilderUtils.findModuleFromBuilders(modules, module, groupingPrefix, line);
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
        final String[] split = getPrefixAndName(usesBuilder.getGroupingPathAsString(), module, line);
        Module dependentModule = BuilderUtils.findModuleFromContext(context, module, split[0], line);
        return findGroupingDefinition(dependentModule.getGroupings(), split[1]);
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
    private static GroupingBuilder findGroupingBuilder(final Set<GroupingBuilder> groupings, final String name) {
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
    private static GroupingDefinition findGroupingDefinition(final Set<GroupingDefinition> groupings, final String name) {
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
    public static void performRefine(final UsesNodeBuilder usesNode) {
        for (RefineBuilder refine : usesNode.getRefines()) {
            String refineTargetPath = refine.getTargetPathString();

            Builder currentNode = usesNode.getParent();
            for (String pathElement : SLASH_SPLITTER.split(refineTargetPath)) {
                if (currentNode instanceof DataNodeContainerBuilder) {
                    currentNode = ((DataNodeContainerBuilder) currentNode).getDataChildByName(pathElement);
                } else if (currentNode instanceof ChoiceBuilder) {
                    currentNode = ((ChoiceBuilder) currentNode).getCaseNodeByName(pathElement);
                }
            }

            DataSchemaNodeBuilder nodeToRefine = (DataSchemaNodeBuilder) currentNode;
            if (nodeToRefine == null) {
                throw new YangParseException(refine.getModuleName(), refine.getLine(), "Refine target node '"
                        + refine.getTargetPathString() + "' not found");
            }
            RefineUtils.performRefine(nodeToRefine, refine);
            usesNode.addRefineNode(nodeToRefine);
        }
    }

    public static class UsesComparator implements Comparator<UsesNodeBuilder> {
        @Override
        public int compare(final UsesNodeBuilder o1, final UsesNodeBuilder o2) {
            return getElementPosition(o2) - getElementPosition(o1);
        }
    }

    private static int getElementPosition(final UsesNodeBuilder usesNode) {
        int i = 0;
        Builder parent = usesNode.getParent();
        while (!(parent instanceof ModuleBuilder)) {
            parent = parent.getParent();
            i++;
        }
        return i;
    }

}
