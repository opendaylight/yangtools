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
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingMember;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceCaseBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.RpcDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnknownSchemaNodeBuilder;

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
     * Add nodes defined in uses target grouping to uses parent.
     *
     * @param usesNode
     *            yang uses node which will be instantiated in current location
     */
    public static void updateUsesParent(UsesNodeBuilder usesNode) {
        DataNodeContainerBuilder parent = usesNode.getParent();
        ModuleBuilder module = ParserUtils.getParentModule(parent);
        URI ns = module.getNamespace();
        Date rev = module.getRevision();
        String prefix = module.getPrefix();

        SchemaPath parentPath = parent.getPath();
        if (parent instanceof AugmentationSchemaBuilder) {
            parentPath = ((AugmentationSchemaBuilder) parent).getTargetNodeSchemaPath();
        }

        // child nodes
        for (DataSchemaNodeBuilder child : usesNode.getTargetChildren()) {
            if (child instanceof GroupingMember) {
                ((GroupingMember) child).setAddedByUses(true);
            }

            if (child instanceof GroupingMember) {
                GroupingMember gm = (GroupingMember) child;
                if (gm.isAddedByUses()) {
                    if (usesNode.isAugmenting()) {
                        child.setAugmenting(true);
                    }
                    if (usesNode.isAugmenting()
                            && !(usesNode.getParentAugment().getParent() instanceof UsesNodeBuilder)) {
                        AugmentationSchemaBuilder parentAugment = usesNode.getParentAugment();
                        ModuleBuilder m = ParserUtils.getParentModule(parentAugment);
                        correctNodePathForUsesNodes(child, parentPath, m);
                    } else {
                        child.setQName(new QName(ns, rev, prefix, child.getQName().getLocalName()));
                        correctNodePathForUsesNodes(child, parentPath, module);
                    }
                }
            } else {
                throw new YangParseException(module.getName(), usesNode.getLine(),
                        "Failed to process uses node: unresolved child node");
            }

            parent.addChildNode(child);
        }

        // groupings
        for (GroupingBuilder gb : usesNode.getTargetGroupings()) {
            gb.setAddedByUses(true);
            gb.setQName(new QName(ns, rev, prefix, gb.getQName().getLocalName()));
            correctNodePathForUsesNodes(gb, parentPath, module);
            parent.addGrouping(gb);
        }

        // typedefs
        for (TypeDefinitionBuilder tdb : usesNode.getTargetTypedefs()) {
            tdb.setAddedByUses(true);
            tdb.setQName(new QName(ns, rev, prefix, tdb.getQName().getLocalName()));
            correctNodePathForUsesNodes(tdb, parentPath, module);
            parent.addTypedef(tdb);
        }

        // unknown nodes
        for (UnknownSchemaNodeBuilder un : usesNode.getTargetUnknownNodes()) {
            un.setAddedByUses(true);
            un.setQName(new QName(ns, rev, prefix, un.getQName().getLocalName()));
            correctNodePathForUsesNodes(un, parentPath, module);
            parent.addUnknownNodeBuilder(un);
        }
    }

    /**
     * Read data defined in target grouping builder, make a copy and add them to
     * uses node builder.
     *
     * @param usesNode
     *            uses node builder
     */
    public static void collectUsesData(UsesNodeBuilder usesNode) {
        collectTargetChildNodes(usesNode);
        collectTargetTypedefs(usesNode);
        collectTargetGroupings(usesNode);
        collectTargetUnknownNodes(usesNode);
        usesNode.setDataCollected(true);
    }

    /**
     * Read child nodes defined in target grouping and make a copy of them.
     *
     * @param usesNode
     *            uses node for which data will be collected
     */
    private static void collectTargetChildNodes(UsesNodeBuilder usesNode) {
        final GroupingBuilder target = usesNode.getGroupingBuilder();
        final Set<DataSchemaNodeBuilder> collection = new HashSet<>();
        addChildNodeToCollection(usesNode, collection, target.getChildNodeBuilders(), usesNode.getParent());

        for (UsesNodeBuilder targetUses : target.getUsesNodes()) {
            Set<DataSchemaNodeBuilder> targetUsesChildNodes = collectTargetUsesChildNodes(targetUses,
                    usesNode.getParent());
            addChildNodeToCollection(usesNode, collection, targetUsesChildNodes, usesNode.getParent());
        }
        usesNode.getTargetChildren().addAll(collection);
    }

    private static Set<DataSchemaNodeBuilder> collectTargetUsesChildNodes(UsesNodeBuilder usesNode,
            DataNodeContainerBuilder parent) {
        final GroupingBuilder target = usesNode.getGroupingBuilder();
        final Set<DataSchemaNodeBuilder> collection = new HashSet<>(usesNode.getTargetChildren());
        addChildNodeToCollection(usesNode, collection, target.getChildNodeBuilders(), parent);

        for (UsesNodeBuilder targetUses : target.getUsesNodes()) {
            Set<DataSchemaNodeBuilder> targetUsesChildNodes = collectTargetUsesChildNodes(targetUses, parent);
            addChildNodeToCollection(usesNode, collection, targetUsesChildNodes, parent);
        }
        return collection;
    }

    private static void addChildNodeToCollection(UsesNodeBuilder usesNode, Set<DataSchemaNodeBuilder> collection,
            Set<DataSchemaNodeBuilder> allNodes, Builder parent) {
        for (DataSchemaNodeBuilder childNode : allNodes) {
            boolean exists = false;
            for (DataSchemaNodeBuilder usesChildNode : usesNode.getTargetChildren()) {
                if (usesChildNode.getQName().getLocalName().equals(childNode.getQName().getLocalName())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                DataSchemaNodeBuilder copy = CopyUtils.copy(childNode, parent, true);
                collection.add(copy);
            }
        }
    }

    /**
     * Read typedefs defined in target grouping and make a copy of them.
     *
     * @param usesNode
     *            uses node for which data will be collected
     */
    private static void collectTargetTypedefs(UsesNodeBuilder usesNode) {
        final GroupingBuilder target = usesNode.getGroupingBuilder();
        Set<TypeDefinitionBuilder> collection = new HashSet<>();
        addTypedefToCollection(usesNode, collection, target.getTypeDefinitionBuilders(), usesNode.getParent());

        for (UsesNodeBuilder targetUses : target.getUsesNodes()) {
            Set<TypeDefinitionBuilder> targetUsesTypedefs = collectTargetUsesTypedefs(targetUses, usesNode.getParent());
            addTypedefToCollection(usesNode, collection, targetUsesTypedefs, usesNode.getParent());
        }
        usesNode.getTargetTypedefs().addAll(collection);
    }

    private static Set<TypeDefinitionBuilder> collectTargetUsesTypedefs(UsesNodeBuilder usesNode,
            DataNodeContainerBuilder parent) {
        final GroupingBuilder target = usesNode.getGroupingBuilder();
        Set<TypeDefinitionBuilder> collection = new HashSet<>(usesNode.getTargetTypedefs());
        addTypedefToCollection(usesNode, collection, target.getTypeDefinitionBuilders(), parent);

        for (UsesNodeBuilder targetUses : target.getUsesNodes()) {
            Set<TypeDefinitionBuilder> targetUsesTypedefs = collectTargetUsesTypedefs(targetUses, parent);
            addTypedefToCollection(usesNode, collection, targetUsesTypedefs, parent);
        }
        return collection;
    }

    private static void addTypedefToCollection(UsesNodeBuilder usesNode, Set<TypeDefinitionBuilder> collection,
            Set<TypeDefinitionBuilder> allTypedefs, Builder parent) {
        for (TypeDefinitionBuilder childNode : allTypedefs) {
            boolean exists = false;
            for (TypeDefinitionBuilder usesTypedef : usesNode.getTargetTypedefs()) {
                if (usesTypedef.getQName().getLocalName().equals(childNode.getQName().getLocalName())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                TypeDefinitionBuilder copy = CopyUtils.copy(childNode, parent, true);
                collection.add(copy);
            }
        }
    }

    /**
     * Read groupings defined in target grouping and make a copy of them.
     *
     * @param usesNode
     *            uses node for which data will be collected
     */
    private static void collectTargetGroupings(UsesNodeBuilder usesNode) {
        final GroupingBuilder target = usesNode.getGroupingBuilder();
        Set<GroupingBuilder> collection = new HashSet<>();
        addGroupingToCollection(usesNode, collection, target.getGroupingBuilders(), usesNode.getParent());

        for (UsesNodeBuilder targetUses : target.getUsesNodes()) {
            Set<GroupingBuilder> targetUsesGrouping = collectTargetGroupings(targetUses, usesNode.getParent());
            addGroupingToCollection(usesNode, collection, targetUsesGrouping, usesNode.getParent());
        }
        usesNode.getTargetGroupings().addAll(collection);
    }

    private static Set<GroupingBuilder> collectTargetGroupings(UsesNodeBuilder usesNode, DataNodeContainerBuilder parent) {
        final GroupingBuilder target = usesNode.getGroupingBuilder();
        Set<GroupingBuilder> collection = new HashSet<>(usesNode.getTargetGroupings());
        addGroupingToCollection(usesNode, collection, target.getGroupingBuilders(), parent);

        for (UsesNodeBuilder targetUses : target.getUsesNodes()) {
            Set<GroupingBuilder> targetUsesGroupings = collectTargetGroupings(targetUses, parent);
            addGroupingToCollection(usesNode, collection, targetUsesGroupings, parent);
        }
        return collection;
    }

    private static void addGroupingToCollection(UsesNodeBuilder usesNode, Set<GroupingBuilder> collection,
            Set<GroupingBuilder> allGroupings, Builder parent) {
        for (GroupingBuilder childNode : allGroupings) {
            boolean exists = false;
            for (GroupingBuilder usesGrouping : usesNode.getTargetGroupings()) {
                if (usesGrouping.getQName().getLocalName().equals(childNode.getQName().getLocalName())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                GroupingBuilder copy = CopyUtils.copy(childNode, parent, true);
                collection.add(copy);
            }
        }
    }

    /**
     * Read unknown nodes defined in target grouping and make a copy of them.
     *
     * @param usesNode
     *            uses node for which data will be collected
     */
    private static void collectTargetUnknownNodes(UsesNodeBuilder usesNode) {
        final GroupingBuilder target = usesNode.getGroupingBuilder();
        final List<UnknownSchemaNodeBuilder> collection = new ArrayList<>();
        addUnknownNodeToCollection(usesNode, collection, target.getUnknownNodeBuilders(), usesNode.getParent());

        for (UsesNodeBuilder targetUses : target.getUsesNodes()) {
            List<UnknownSchemaNodeBuilder> targetUsesUnknownNodes = collectTargetUnknownNodes(targetUses,
                    usesNode.getParent());
            addUnknownNodeToCollection(usesNode, collection, targetUsesUnknownNodes, usesNode.getParent());
        }
        usesNode.getTargetUnknownNodes().addAll(collection);
    }

    private static List<UnknownSchemaNodeBuilder> collectTargetUnknownNodes(UsesNodeBuilder usesNode,
            DataNodeContainerBuilder parent) {
        final GroupingBuilder target = usesNode.getGroupingBuilder();
        List<UnknownSchemaNodeBuilder> collection = new ArrayList<>(usesNode.getTargetUnknownNodes());
        addUnknownNodeToCollection(usesNode, collection, target.getUnknownNodeBuilders(), parent);

        for (UsesNodeBuilder targetUses : target.getUsesNodes()) {
            List<UnknownSchemaNodeBuilder> targetUsesUnknownNodes = collectTargetUnknownNodes(targetUses, parent);
            addUnknownNodeToCollection(usesNode, collection, targetUsesUnknownNodes, parent);
        }
        return collection;
    }

    private static void addUnknownNodeToCollection(UsesNodeBuilder usesNode, List<UnknownSchemaNodeBuilder> collection,
            List<UnknownSchemaNodeBuilder> allUnknownNodes, Builder parent) {
        for (UnknownSchemaNodeBuilder childNode : allUnknownNodes) {
            boolean exists = false;
            for (UnknownSchemaNodeBuilder usesUnknownNode : usesNode.getTargetUnknownNodes()) {
                if (usesUnknownNode.getQName().getLocalName().equals(childNode.getQName().getLocalName())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                UnknownSchemaNodeBuilder copy = CopyUtils.copy(childNode, parent, true);
                collection.add(copy);
            }
        }
    }

    /**
     * Read data defined in target grouping definition, make a copy and add them
     * to uses node builder.
     *
     * @param usesNode
     *            uses node builder
     */
    public static void collectUsesDataFromContext(UsesNodeBuilder usesNode) {
        DataNodeContainerBuilder parent = usesNode.getParent();
        URI namespace = parent.getQName().getNamespace();
        Date revision = parent.getQName().getRevision();
        String prefix = parent.getQName().getPrefix();
        String moduleName = parent.getModuleName();
        int line = parent.getLine();

        // child nodes
        copyGroupingNodesToUsesNode(usesNode, namespace, revision, prefix, moduleName, line);

        // groupings
        final Set<GroupingBuilder> newGroupings = new HashSet<>();
        for (GroupingDefinition g : usesNode.getGroupingDefinition().getGroupings()) {
            QName newQName = new QName(namespace, revision, prefix, g.getQName().getLocalName());
            GroupingBuilder newGrouping = CopyUtils.createGrouping(g, newQName, moduleName, line);
            newGrouping.setAddedByUses(true);
            newGroupings.add(newGrouping);
        }
        usesNode.getTargetGroupings().addAll(newGroupings);

        // typedefs
        final Set<TypeDefinitionBuilder> newTypedefs = new HashSet<>();
        for (TypeDefinition<?> td : usesNode.getGroupingDefinition().getTypeDefinitions()) {
            QName newQName = new QName(namespace, revision, prefix, td.getQName().getLocalName());
            TypeDefinitionBuilder newType = CopyUtils.createTypedef((ExtendedType) td, newQName, moduleName, line);
            newType.setAddedByUses(true);
            newTypedefs.add(newType);
        }
        usesNode.getTargetTypedefs().addAll(newTypedefs);

        // unknown nodes
        final List<UnknownSchemaNodeBuilder> newUnknownNodes = new ArrayList<>();
        for (UnknownSchemaNode un : usesNode.getGroupingDefinition().getUnknownSchemaNodes()) {
            QName newQName = new QName(namespace, revision, prefix, un.getQName().getLocalName());
            UnknownSchemaNodeBuilder newNode = CopyUtils.createUnknownSchemaNode(un, newQName, moduleName, line);
            newNode.setAddedByUses(true);
            newUnknownNodes.add(newNode);
        }
        usesNode.getTargetUnknownNodes().addAll(newUnknownNodes);

        usesNode.setDataCollected(true);
    }

    /**
     * Read data defined in target grouping definition, make a copy and add them
     * to uses node builder.
     *
     * @param usesNode
     *            used node builder to which are copied nodes from its
     *            <code>GroupingDefinition</code>
     * @param namespace
     *            URI with parent namespace
     * @param revision
     *            date with parent revision date
     * @param prefix
     *            string with parent prefix
     * @param moduleName
     *            string with parent module name
     * @param lineNumber
     *            number with YANG file row where is the parent defined
     */
    private static void copyGroupingNodesToUsesNode(final UsesNodeBuilder usesNode, final URI namespace,
            final Date revision, final String prefix, final String moduleName, final int lineNumber) {
        final Set<DataSchemaNodeBuilder> newChildren = new HashSet<>();
        for (DataSchemaNode child : usesNode.getGroupingDefinition().getChildNodes()) {
            if (child != null) {
                DataSchemaNodeBuilder newChild = null;
                QName newQName = new QName(namespace, revision, prefix, child.getQName().getLocalName());
                if (child instanceof AnyXmlSchemaNode) {
                    newChild = CopyUtils.createAnyXml((AnyXmlSchemaNode) child, newQName, moduleName, lineNumber);
                } else if (child instanceof ChoiceNode) {
                    newChild = CopyUtils.createChoice((ChoiceNode) child, newQName, moduleName, lineNumber);
                } else if (child instanceof ContainerSchemaNode) {
                    newChild = CopyUtils.createContainer((ContainerSchemaNode) child, newQName, moduleName, lineNumber);
                } else if (child instanceof LeafListSchemaNode) {
                    newChild = CopyUtils.createLeafList((LeafListSchemaNode) child, newQName, moduleName, lineNumber);
                } else if (child instanceof LeafSchemaNode) {
                    newChild = CopyUtils.createLeafBuilder((LeafSchemaNode) child, newQName, moduleName, lineNumber);
                } else if (child instanceof ListSchemaNode) {
                    newChild = CopyUtils.createList((ListSchemaNode) child, newQName, moduleName, lineNumber);
                }

                if (newChild == null) {
                    throw new YangParseException(moduleName, lineNumber,
                            "Unknown member of target grouping while resolving uses node.");
                }

                ((GroupingMember) newChild).setAddedByUses(true);
                newChildren.add(newChild);
            }
        }
        usesNode.getTargetChildren().addAll(newChildren);

    }

    /**
     * Correct schema path of nodes added by uses statement.
     *
     * @param node
     *            node added by uses statement
     * @param parentSchemaPath
     *            schema path of parent node
     * @param parentModule
     *            current parent node module
     */
    private static void correctNodePathForUsesNodes(final SchemaNodeBuilder node, final SchemaPath parentSchemaPath,
            final ModuleBuilder parentModule) {
        // set correct path
        List<QName> targetNodePath = new ArrayList<>(parentSchemaPath.getPath());
        targetNodePath.add(new QName(parentModule.getNamespace(), parentModule.getRevision(), parentModule.getPrefix(),
                node.getQName().getLocalName()));
        node.setPath(new SchemaPath(targetNodePath, true));

        // set correct path for all child nodes
        if (node instanceof DataNodeContainerBuilder) {
            DataNodeContainerBuilder dataNodeContainer = (DataNodeContainerBuilder) node;
            for (DataSchemaNodeBuilder child : dataNodeContainer.getChildNodeBuilders()) {
                correctNodePathForUsesNodes(child, node.getPath(), parentModule);
            }
        }

        // set correct path for all cases
        if (node instanceof ChoiceBuilder) {
            ChoiceBuilder choiceBuilder = (ChoiceBuilder) node;
            for (ChoiceCaseBuilder choiceCaseBuilder : choiceBuilder.getCases()) {
                correctNodePathForUsesNodes(choiceCaseBuilder, node.getPath(), parentModule);
            }
        }
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

}
