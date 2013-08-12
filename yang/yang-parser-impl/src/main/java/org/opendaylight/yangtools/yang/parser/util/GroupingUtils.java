/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingMember;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.RpcDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnknownSchemaNodeBuilder;

public class GroupingUtils {

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
        final String groupingString = usesBuilder.getGroupingName();
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

        ModuleBuilder dependentModule = null;
        if (groupingPrefix.equals(module.getPrefix())) {
            dependentModule = module;
        } else {
            dependentModule = ParserUtils.findDependentModuleBuilder(modules, module, groupingPrefix, line);
        }

        if (dependentModule == null) {
            return null;
        }

        GroupingBuilder result = null;
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
        String groupingString = usesBuilder.getGroupingName();
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
     * Copy target grouping data to given uses node.
     * <p>
     * Copy all data-schema-nodes, groupings, typedefs and unknown nodes from
     * target grouping to uses node.
     * </p>
     *
     * @param usesNode
     * @param targetGrouping
     */
    public static void loadTargetGroupingData(final UsesNodeBuilder usesNode, final GroupingBuilder targetGrouping) {
        // child nodes
        Set<DataSchemaNodeBuilder> targetChildren = new HashSet<>();
        for (DataSchemaNodeBuilder targetChild : targetGrouping.getChildNodeBuilders()) {
            targetChildren.add(CopyUtils.copy(targetChild, usesNode.getParent(), true));
        }
        usesNode.setTargetChildren(targetChildren);

        // groupings
        Set<GroupingBuilder> targetGroupingGroupings = new HashSet<>();
        for (GroupingBuilder targetGroupingGrouping : targetGrouping.getGroupingBuilders()) {
            targetGroupingGroupings.add(CopyUtils.copy(targetGroupingGrouping, usesNode.getParent(), true));
        }
        usesNode.setTargetGroupings(targetGroupingGroupings);

        // typedefs
        Set<TypeDefinitionBuilder> targetGroupingTypedefs = new HashSet<>();
        for(TypeDefinitionBuilder targetGroupingTypedef : targetGrouping.getTypeDefinitionBuilders()) {
            targetGroupingTypedefs.add(CopyUtils.copy(targetGroupingTypedef, usesNode.getParent(), true));
        }
        usesNode.setTargetTypedefs(targetGroupingTypedefs);

        // unknown nodes
        List<UnknownSchemaNodeBuilder> targetGroupingUNs = new ArrayList<>();
        for(UnknownSchemaNodeBuilder targetGroupingUN : targetGrouping.getUnknownNodeBuilders()) {
            targetGroupingUNs.add(CopyUtils.copy(targetGroupingUN, usesNode.getParent(), true));
        }
        usesNode.setTargetUnknownNodes(targetGroupingUNs);

        usesNode.setLoadDone(true);
    }

    /**
     * Copy all data from target grouping which were added by uses.
     * <p>
     * Traverse uses statements in target grouping and copy all
     * data-schema-nodes, groupings, typedefs and unknown nodes to current uses
     * node.
     * </p>
     *
     * @param usesNode
     * @param targetGrouping
     */
    public static void loadTargetGroupingUses(final UsesNodeBuilder usesNode, final GroupingBuilder targetGrouping) {
        usesNode.getTargetGroupingUses().addAll(targetGrouping.getUsesNodes());
    }

    /**
     * Create copy of collection of given nodes with new schema path.
     *
     * @param nodes
     *            nodes to copy
     * @param parentPath
     *            schema path of parent node
     * @param namespace
     *            new namespace of node qname
     * @param revision
     *            new revision of node qname
     * @param prefix
     *            new prefix of node qname
     * @param moduleName
     *            current yang module name
     * @param line
     *            current line in yang module
     * @return collection of new nodes with corrected path
     */
    public static Set<DataSchemaNodeBuilder> copyUsesTargetNodesWithNewPath(UsesNodeBuilder usesNode, Builder parent) {
        Set<DataSchemaNodeBuilder> newNodes = new HashSet<>();

        for (DataSchemaNodeBuilder node : usesNode.getTargetChildren()) {
            if (node != null) {
                if (node instanceof GroupingMember) {
                    ((GroupingMember) node).setAddedByUses(true);
                }
                newNodes.add(node);
            }
        }

        return newNodes;
    }

    /**
     * Create copy of collection of given groupings with new schema path.
     *
     * @param groupings
     *            groupings to copy
     * @param parentPath
     *            schema path of parent node
     * @param namespace
     *            new namespace of node qname
     * @param revision
     *            new revision of node qname
     * @param prefix
     *            new prefix of node qname
     * @return collection of new groupings with corrected path
     */
    public static Set<GroupingBuilder> copyUsesTargetGroupingsWithNewPath(UsesNodeBuilder usesNode,
            SchemaPath parentPath, URI namespace, Date revision, String prefix) {
        Set<GroupingBuilder> newGroupings = new HashSet<>();
        for (GroupingBuilder node : usesNode.getTargetGroupings()) {
            if (node != null) {
                if (node instanceof GroupingMember) {
                    ((GroupingMember) node).setAddedByUses(true);
                }
                newGroupings.add(node);
            }
        }

        return newGroupings;
    }

    /**
     * Create copy of collection of given typedefs with new schema path.
     *
     * @param typedefs
     *            typedefs to copy
     * @param parentPath
     *            schema path of parent node
     * @param namespace
     *            new namespace of node qname
     * @param revision
     *            new revision of node qname
     * @param prefix
     *            new prefix of node qname
     * @return collection of new typedefs with corrected path
     */
    public static Set<TypeDefinitionBuilder> copyUsesTargetTypedefsWithNewPath(UsesNodeBuilder usesNode,
            SchemaPath parentPath, URI namespace, Date revision, String prefix) {
        Set<TypeDefinitionBuilder> newTypedefs = new HashSet<>();

        for (TypeDefinitionBuilder node : usesNode.getTargetTypedefs()) {
            if (node != null) {
                if (node instanceof GroupingMember) {
                    ((GroupingMember) node).setAddedByUses(true);
                }
                newTypedefs.add(node);
            }
        }

        return newTypedefs;
    }

    /**
     * Create copy of collection of given unknown nodes with new schema path.
     *
     * @param usesNode
     * @param parentPath
     *            schema path of parent node
     * @param namespace
     *            new namespace of node qname
     * @param revision
     *            new revision of node qname
     * @param prefix
     *            new prefix of node qname
     * @return collection of new unknownNodes with corrected path
     */
    public static List<UnknownSchemaNodeBuilder> copyUsesTargetUnknownNodesWithNewPath(UsesNodeBuilder usesNode,
            SchemaPath parentPath, URI namespace, Date revision, String prefix) {
        List<UnknownSchemaNodeBuilder> newUnknownNodes = new ArrayList<>();

        for (UnknownSchemaNodeBuilder node : usesNode.getTargetUnknownNodes()) {
            if (node != null) {
                node.setAddedByUses(true);
                newUnknownNodes.add(node);
            }
        }

        return newUnknownNodes;
    }

}
