/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import static org.opendaylight.yangtools.yang.parser.util.ParserUtils.createSchemaPath;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
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
     * Add nodes defined in uses target grouping to uses parent.
     *
     * @param usesNode
     */
    public static void updateUsesParent(UsesNodeBuilder usesNode) {
        DataNodeContainerBuilder parent = usesNode.getParent();

        // child nodes
        for (DataSchemaNodeBuilder child : usesNode.getTargetChildren()) {
            if (child instanceof GroupingMember) {
                ((GroupingMember) child).setAddedByUses(true);
            }
            parent.addChildNode(child);
        }

        // groupings
        for (GroupingBuilder gb : usesNode.getTargetGroupings()) {
            gb.setAddedByUses(true);
            parent.addGrouping(gb);
        }

        // typedefs
        for (TypeDefinitionBuilder tdb : usesNode.getTargetTypedefs()) {
            tdb.setAddedByUses(true);
            parent.addTypedef(tdb);
        }

        // unknown nodes
        for (UnknownSchemaNodeBuilder un : usesNode.getTargetUnknownNodes()) {
            un.setAddedByUses(true);
            parent.addUnknownNodeBuilder(un);
        }
    }

    public static void collectUsesData(UsesNodeBuilder usesNode) {
        usesNode.setTargetChildren(collectUsesChildNodes(usesNode));
        usesNode.setTargetTypedefs(collectUsesTypedefs(usesNode));
        usesNode.setTargetGroupings(collectUsesGroupings(usesNode));
        usesNode.setTargetUnknownNodes(collectUsesUnknownNodes(usesNode));
        usesNode.setDataCollected(true);
    }

    private static Set<DataSchemaNodeBuilder> collectUsesChildNodes(UsesNodeBuilder usesNode) {
        final GroupingBuilder target = usesNode.getGroupingBuilder();
        Set<DataSchemaNodeBuilder> childNodes = target.getChildNodeBuilders();
        Set<DataSchemaNodeBuilder> copies = new HashSet<>();
        for (DataSchemaNodeBuilder childNode : childNodes) {
            copies.add(CopyUtils.copy(childNode, usesNode.getParent(), true));
        }
        for (UsesNodeBuilder targetUses : target.getUsesNodes()) {
            copies.addAll(collectUsesChildNodes(targetUses));
        }
        return copies;
    }

    private static Set<TypeDefinitionBuilder> collectUsesTypedefs(UsesNodeBuilder usesNode) {
        final GroupingBuilder target = usesNode.getGroupingBuilder();
        Set<TypeDefinitionBuilder> typedefs = target.getTypeDefinitionBuilders();
        Set<TypeDefinitionBuilder> copies = new HashSet<>();
        for (TypeDefinitionBuilder typedef : typedefs) {
            copies.add(CopyUtils.copy(typedef, usesNode.getParent(), true));
        }
        for (UsesNodeBuilder targetUses : target.getUsesNodes()) {
            copies.addAll(collectUsesTypedefs(targetUses));
        }
        return copies;
    }

    private static Set<GroupingBuilder> collectUsesGroupings(UsesNodeBuilder usesNode) {
        final GroupingBuilder target = usesNode.getGroupingBuilder();
        Set<GroupingBuilder> groupings = target.getGroupingBuilders();
        Set<GroupingBuilder> copies = new HashSet<>();
        for (GroupingBuilder grouping : groupings) {
            copies.add(CopyUtils.copy(grouping, usesNode.getParent(), true));
        }
        for (UsesNodeBuilder targetUses : target.getUsesNodes()) {
            copies.addAll(collectUsesGroupings(targetUses));
        }
        return copies;
    }

    private static List<UnknownSchemaNodeBuilder> collectUsesUnknownNodes(UsesNodeBuilder usesNode) {
        final GroupingBuilder target = usesNode.getGroupingBuilder();
        List<UnknownSchemaNodeBuilder> unknownNodes = target.getUnknownNodeBuilders();
        List<UnknownSchemaNodeBuilder> copies = new ArrayList<>();
        for (UnknownSchemaNodeBuilder unknownNode : unknownNodes) {
            copies.add(CopyUtils.copy(unknownNode, usesNode.getParent(), true));
        }
        for (UsesNodeBuilder targetUses : target.getUsesNodes()) {
            copies.addAll(collectUsesUnknownNodes(targetUses));
        }
        return copies;
    }

    public static void collectUsesDataFromContext(UsesNodeBuilder usesNode) {
        DataNodeContainerBuilder parent = usesNode.getParent();
        URI namespace = parent.getQName().getNamespace();
        Date revision = parent.getQName().getRevision();
        String prefix = parent.getQName().getPrefix();
        String  moduleName = parent.getModuleName();
        int line = parent.getLine();

        // child nodes
        final Set<DataSchemaNodeBuilder> newChildren = new HashSet<>();
        for (DataSchemaNode child : usesNode.getGroupingDefinition().getChildNodes()) {
            if (child != null) {
                DataSchemaNodeBuilder newChild = null;
                QName newQName = new QName(namespace, revision, prefix, child.getQName().getLocalName());
                if (child instanceof AnyXmlSchemaNode) {
                    newChild = CopyUtils.createAnyXml((AnyXmlSchemaNode) child, newQName, moduleName, line);
                } else if (child instanceof ChoiceNode) {
                    newChild = CopyUtils.createChoice((ChoiceNode) child, newQName, moduleName, line);
                } else if (child instanceof ContainerSchemaNode) {
                    newChild = CopyUtils.createContainer((ContainerSchemaNode) child, newQName, moduleName, line);
                } else if (child instanceof LeafListSchemaNode) {
                    newChild = CopyUtils.createLeafList((LeafListSchemaNode) child, newQName, moduleName, line);
                } else if (child instanceof LeafSchemaNode) {
                    newChild = CopyUtils.createLeafBuilder((LeafSchemaNode) child, newQName, moduleName, line);
                } else if (child instanceof ListSchemaNode) {
                    newChild = CopyUtils.createList((ListSchemaNode) child, newQName, moduleName, line);
                }

                if (newChild == null) {
                    throw new YangParseException(moduleName, line,
                            "Unknown member of target grouping while resolving uses node.");
                }
                if (newChild instanceof GroupingMember) {
                    ((GroupingMember) newChild).setAddedByUses(true);
                }

                newChild.setPath(createSchemaPath(parent.getPath(), newQName));
                newChildren.add(newChild);
            }
        }
        usesNode.setTargetChildren(newChildren);

        // groupings
        final Set<GroupingBuilder> newGroupings = new HashSet<>();
        for (GroupingDefinition g : usesNode.getGroupingDefinition().getGroupings()) {
            QName newQName = new QName(namespace, revision, prefix, g.getQName().getLocalName());
            GroupingBuilder newGrouping = CopyUtils.createGrouping(g, newQName, moduleName, line);
            newGrouping.setAddedByUses(true);
            newGrouping.setPath(createSchemaPath(parent.getPath(), newQName));
            newGroupings.add(newGrouping);
        }
        usesNode.setTargetGroupings(newGroupings);

        // typedefs
        final Set<TypeDefinitionBuilder> newTypedefs = new HashSet<>();
        for (TypeDefinition<?> td : usesNode.getGroupingDefinition().getTypeDefinitions()) {
            QName newQName = new QName(namespace, revision, prefix, td.getQName().getLocalName());
            TypeDefinitionBuilder newType = CopyUtils.createTypedef((ExtendedType) td, newQName, moduleName, line);
            newType.setAddedByUses(true);
            newType.setPath(createSchemaPath(parent.getPath(), newQName));
            newTypedefs.add(newType);
        }
        usesNode.setTargetTypedefs(newTypedefs);

        // unknown nodes
        final List<UnknownSchemaNodeBuilder> newUnknownNodes = new ArrayList<>();
        for (UnknownSchemaNode un : usesNode.getGroupingDefinition().getUnknownSchemaNodes()) {
            QName newQName = new QName(namespace, revision, prefix, un.getQName().getLocalName());
            UnknownSchemaNodeBuilder newNode = CopyUtils.createUnknownSchemaNode(un, newQName, moduleName, line);
            newNode.setAddedByUses(true);
            newNode.setPath(createSchemaPath(parent.getPath(), newQName));
            newUnknownNodes.add(newNode);
        }
        usesNode.setTargetUnknownNodes(newUnknownNodes);

        usesNode.setDataCollected(true);
    }

    public static void fixUsesNodesPath(UsesNodeBuilder usesNode) {
        DataNodeContainerBuilder parent = usesNode.getParent();

        // child nodes
        Set<DataSchemaNodeBuilder> currentChildNodes = parent.getChildNodeBuilders();
        for (DataSchemaNodeBuilder child : currentChildNodes) {
            if (child instanceof GroupingMember) {
                GroupingMember gm = (GroupingMember) child;
                if (gm.isAddedByUses()) {
                    ParserUtils.correctNodePath(child, parent.getPath());
                }
            }
        }

        // groupings
        Set<GroupingBuilder> currentGroupings = parent.getGroupingBuilders();
        for (GroupingBuilder child : currentGroupings) {
            if (child.isAddedByUses()) {
                ParserUtils.correctNodePath(child, parent.getPath());
            }

        }

        // typedefs
        Set<TypeDefinitionBuilder> currentTypedefs = parent.getTypeDefinitionBuilders();
        for (TypeDefinitionBuilder child : currentTypedefs) {
            if (child.isAddedByUses()) {
                ParserUtils.correctNodePath(child, parent.getPath());
            }

        }

        // unknown nodes
        List<UnknownSchemaNodeBuilder> currentUN = parent.getUnknownNodeBuilders();
        for (UnknownSchemaNodeBuilder un : currentUN) {
            if (un.isAddedByUses()) {
                ParserUtils.correctNodePath(un, parent.getPath());
            }
        }
    }

    /**
     * Perform refinement of uses target grouping nodes. Uses process has to be
     * already performed.
     *
     * @param usesNode
     */
    public static void performRefine(UsesNodeBuilder usesNode) {
        for (RefineHolder refine : usesNode.getRefines()) {
            DataSchemaNodeBuilder nodeToRefine = null;
            for (DataSchemaNodeBuilder dataNode : usesNode.getParent().getChildNodeBuilders()) {
                if (refine.getName().equals(dataNode.getQName().getLocalName())) {
                    nodeToRefine = dataNode;
                    break;
                }
            }
            if (nodeToRefine == null) {
                throw new YangParseException(refine.getModuleName(), refine.getLine(), "Refine target node '"
                        + refine.getName() + "' not found");
            }
            RefineUtils.performRefine(nodeToRefine, refine);
            usesNode.addRefineNode(nodeToRefine);
        }
    }

}
