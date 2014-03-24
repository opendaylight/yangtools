/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.*;

import com.google.common.base.Preconditions;

import java.util.Set;

/**
 * The Schema Context Util contains support methods for searching through Schema
 * Context modules for specified schema nodes via Schema Path or Revision Aware
 * XPath. The Schema Context Util is designed as mixin, so it is not
 * instantiable.
 *
 */
public class SchemaContextUtil {

    private SchemaContextUtil() {
    }

    /**
     * Method attempts to find DataSchemaNode in Schema Context via specified
     * Schema Path. The returned DataSchemaNode from method will be the node at
     * the end of the SchemaPath. If the DataSchemaNode is not present in the
     * Schema Context the method will return <code>null</code>. <br>
     * In case that Schema Context or Schema Path are not specified correctly
     * (i.e. contains <code>null</code> values) the method will return
     * IllegalArgumentException.
     *
     * @throws IllegalArgumentException
     *
     * @param context
     *            Schema Context
     * @param schemaPath
     *            Schema Path to search for
     * @return SchemaNode from the end of the Schema Path or <code>null</code>
     *         if the Node is not present.
     */
    public static SchemaNode findDataSchemaNode(SchemaContext context, SchemaPath schemaPath) {
        Preconditions.checkArgument(context != null, "Schema Context reference cannot be NULL");
        Preconditions.checkArgument(schemaPath != null, "Schema Path reference cannot be NULL");
        List<QName> prefixedPath = (schemaPath.getPath());
        if (prefixedPath != null) {
            return findNodeInSchemaContext(context, prefixedPath);
        }
        return null;
    }

    /**
     * Method attempts to find DataSchemaNode inside of provided Schema Context
     * and Yang Module accordingly to Non-conditional Revision Aware XPath. The
     * specified Module MUST be present in Schema Context otherwise the
     * operation would fail and return <code>null</code>. <br>
     * The Revision Aware XPath MUST be specified WITHOUT the conditional
     * statement (i.e. without [cond]) in path, because in this state the Schema
     * Context is completely unaware of data state and will be not able to
     * properly resolve XPath. If the XPath contains condition the method will
     * return IllegalArgumentException. <br>
     * In case that Schema Context or Module or Revision Aware XPath contains
     * <code>null</code> references the method will throw
     * IllegalArgumentException <br>
     * If the Revision Aware XPath is correct and desired Data Schema Node is
     * present in Yang module or in depending module in Schema Context the
     * method will return specified Data Schema Node, otherwise the operation
     * will fail and method will return <code>null</code>.
     *
     * @throws IllegalArgumentException
     *
     * @param context
     *            Schema Context
     * @param module
     *            Yang Module
     * @param nonCondXPath
     *            Non Conditional Revision Aware XPath
     * @return Returns Data Schema Node for specified Schema Context for given
     *         Non-conditional Revision Aware XPath, or <code>null</code> if the
     *         DataSchemaNode is not present in Schema Context.
     */
    public static SchemaNode findDataSchemaNode(SchemaContext context, Module module, RevisionAwareXPath nonCondXPath) {
        Preconditions.checkArgument(context != null, "Schema Context reference cannot be NULL");
        Preconditions.checkArgument(module != null, "Module reference cannot be NULL");
        Preconditions.checkArgument(nonCondXPath != null, "Non Conditional Revision Aware XPath cannot be NULL");

        String strXPath = nonCondXPath.toString();
        if (strXPath != null) {
            if (strXPath.contains("[")) {
                throw new IllegalArgumentException("Revision Aware XPath cannot contains condition");
            }
            if (nonCondXPath.isAbsolute()) {
                List<QName> qnamedPath = xpathToQNamePath(context, module, strXPath);
                if (qnamedPath != null) {
                    return findNodeInSchemaContext(context, qnamedPath);
                }
            }
        }
        return null;
    }

    /**
     * Method attempts to find DataSchemaNode inside of provided Schema Context
     * and Yang Module accordingly to Non-conditional relative Revision Aware
     * XPath. The specified Module MUST be present in Schema Context otherwise
     * the operation would fail and return <code>null</code>. <br>
     * The relative Revision Aware XPath MUST be specified WITHOUT the
     * conditional statement (i.e. without [cond]) in path, because in this
     * state the Schema Context is completely unaware of data state and will be
     * not able to properly resolve XPath. If the XPath contains condition the
     * method will return IllegalArgumentException. <br>
     * The Actual Schema Node MUST be specified correctly because from this
     * Schema Node will search starts. If the Actual Schema Node is not correct
     * the operation will simply fail, because it will be unable to find desired
     * DataSchemaNode. <br>
     * In case that Schema Context or Module or Actual Schema Node or relative
     * Revision Aware XPath contains <code>null</code> references the method
     * will throw IllegalArgumentException <br>
     * If the Revision Aware XPath doesn't have flag
     * <code>isAbsolute == false</code> the method will throw
     * IllegalArgumentException. <br>
     * If the relative Revision Aware XPath is correct and desired Data Schema
     * Node is present in Yang module or in depending module in Schema Context
     * the method will return specified Data Schema Node, otherwise the
     * operation will fail and method will return <code>null</code>.
     *
     * @throws IllegalArgumentException
     *
     * @param context
     *            Schema Context
     * @param module
     *            Yang Module
     * @param actualSchemaNode
     *            Actual Schema Node
     * @param relativeXPath
     *            Relative Non Conditional Revision Aware XPath
     * @return DataSchemaNode if is present in specified Schema Context for
     *         given relative Revision Aware XPath, otherwise will return
     *         <code>null</code>.
     */
    public static SchemaNode findDataSchemaNodeForRelativeXPath(SchemaContext context, Module module,
            SchemaNode actualSchemaNode, RevisionAwareXPath relativeXPath) {
        Preconditions.checkArgument(context != null, "Schema Context reference cannot be NULL");
        Preconditions.checkArgument(module != null, "Module reference cannot be NULL");
        Preconditions.checkArgument(actualSchemaNode != null, "Actual Schema Node reference cannot be NULL");
        Preconditions.checkArgument(relativeXPath != null, "Non Conditional Revision Aware XPath cannot be NULL");
        Preconditions.checkState(!relativeXPath.isAbsolute(),
                "Revision Aware XPath MUST be relative i.e. MUST contains ../, "
                        + "for non relative Revision Aware XPath use findDataSchemaNode method");

        SchemaPath actualNodePath = actualSchemaNode.getPath();
        if (actualNodePath != null) {
            List<QName> qnamePath = resolveRelativeXPath(context, module, relativeXPath, actualSchemaNode);

            if (qnamePath != null) {
                return findNodeInSchemaContext(context, qnamePath);
            }
        }
        return null;
    }

    /**
     * Returns parent Yang Module for specified Schema Context in which Schema
     * Node is declared. If the Schema Node is not present in Schema Context the
     * operation will return <code>null</code>. <br>
     * If Schema Context or Schema Node contains <code>null</code> references
     * the method will throw IllegalArgumentException
     *
     * @throws IllegalArgumentException
     *
     * @param context
     *            Schema Context
     * @param schemaNode
     *            Schema Node
     * @return Yang Module for specified Schema Context and Schema Node, if
     *         Schema Node is NOT present, the method will returns
     *         <code>null</code>
     */
    public static Module findParentModule(SchemaContext context, SchemaNode schemaNode) {
        Preconditions.checkArgument(context != null, "Schema Context reference cannot be NULL!");
        Preconditions.checkArgument(schemaNode != null, "Schema Node cannot be NULL!");
        Preconditions.checkState(schemaNode.getPath() != null, "Schema Path for Schema Node is not "
                + "set properly (Schema Path is NULL)");

        List<QName> qnamedPath = schemaNode.getPath().getPath();
        if (qnamedPath == null || qnamedPath.isEmpty()) {
            throw new IllegalStateException("Schema Path contains invalid state of path parts."
                    + "The Schema Path MUST contain at least ONE QName which defines namespace and Local name"
                    + "of path.");
        }
        QName qname = qnamedPath.get(qnamedPath.size() - 1);
        return context.findModuleByNamespaceAndRevision(qname.getNamespace(), qname.getRevision());
    }

    public static SchemaNode findNodeInSchemaContext(SchemaContext context, List<QName> path) {
        QName current = path.get(0);
        Module module = context.findModuleByNamespaceAndRevision(current.getNamespace(), current.getRevision());
        if (module == null)
            return null;
        return findNodeInModule(module, path);
    }

    public static GroupingDefinition findGrouping(SchemaContext context, Module module, List<QName> path) {
        QName first = path.get(0);
        Module m = context.findModuleByNamespace(first.getNamespace()).iterator().next();
        DataNodeContainer currentParent = m;
        for (QName qname : path) {
            boolean found = false;
            DataNodeContainer node = (DataNodeContainer) currentParent.getDataChildByName(qname.getLocalName());
            if (node == null) {
                Set<GroupingDefinition> groupings = currentParent.getGroupings();
                for (GroupingDefinition gr : groupings) {
                    if (gr.getQName().getLocalName().equals(qname.getLocalName())) {
                        currentParent = gr;
                        found = true;
                    }
                }
            } else {
                found = true;
                currentParent = node;
            }
            if (!found) {
                throw new IllegalArgumentException("Failed to find referenced grouping: " + path + "("
                        + qname.getLocalName() + ")");
            }
        }

        return (GroupingDefinition) currentParent;
    }

    private static SchemaNode findNodeInModule(Module module, List<QName> path) {
        QName current = path.get(0);
        SchemaNode node = module.getDataChildByName(current);
        if (node != null)
            return findNode((DataSchemaNode) node, nextLevel(path));
        node = getRpcByName(module, current);
        if (node != null)
            return findNodeInRpc((RpcDefinition) node, nextLevel(path));
        node = getNotificationByName(module, current);
        if (node != null)
            return findNodeInNotification((NotificationDefinition) node, nextLevel(path));
        node = getGroupingByName(module, current);
        if (node != null)
            return findNodeInGrouping((GroupingDefinition) node, nextLevel(path));
        return node;
    }

    private static SchemaNode findNodeInGrouping(GroupingDefinition grouping, List<QName> path) {
        if (path.isEmpty())
            return grouping;
        QName current = path.get(0);
        DataSchemaNode node = grouping.getDataChildByName(current);
        if (node != null)
            return findNode(node, nextLevel(path));
        return null;
    }

    private static SchemaNode findNodeInRpc(RpcDefinition rpc, List<QName> path) {
        if (path.isEmpty())
            return rpc;
        QName current = path.get(0);
        switch (current.getLocalName()) {
        case "input":
            return findNode(rpc.getInput(), nextLevel(path));
        case "output":
            return findNode(rpc.getOutput(), nextLevel(path));
        }
        return null;
    }

    private static SchemaNode findNodeInNotification(NotificationDefinition rpc, List<QName> path) {
        if (path.isEmpty())
            return rpc;
        QName current = path.get(0);
        DataSchemaNode node = rpc.getDataChildByName(current);
        if (node != null)
            return findNode(node, nextLevel(path));
        return null;
    }

    private static SchemaNode findNode(ChoiceNode parent, List<QName> path) {
        if (path.isEmpty())
            return parent;
        QName current = path.get(0);
        ChoiceCaseNode node = parent.getCaseNodeByName(current);
        if (node != null)
            return findNodeInCase(node, nextLevel(path));
        return null;
    }

    private static SchemaNode findNode(ContainerSchemaNode parent, List<QName> path) {
        if (path.isEmpty())
            return parent;
        QName current = path.get(0);
        DataSchemaNode node = parent.getDataChildByName(current);
        if (node != null)
            return findNode(node, nextLevel(path));
        return null;
    }

    private static SchemaNode findNode(ListSchemaNode parent, List<QName> path) {
        if (path.isEmpty())
            return parent;
        QName current = path.get(0);
        DataSchemaNode node = parent.getDataChildByName(current);
        if (node != null)
            return findNode(node, nextLevel(path));
        return null;
    }

    private static SchemaNode findNode(DataSchemaNode parent, List<QName> path) {
        SchemaNode result = null;
        if (path.isEmpty()) {
            result = parent;
        } else {
            if (parent instanceof ContainerSchemaNode) {
                result = findNode((ContainerSchemaNode) parent, path);
            } else if (parent instanceof ListSchemaNode) {
                result = findNode((ListSchemaNode) parent, path);
            } else if (parent instanceof ChoiceNode) {
                result = findNode((ChoiceNode) parent, path);
            } else {
                throw new IllegalArgumentException("Path nesting violation");
            }
        }
        return result;
    }

    public static SchemaNode findNodeInCase(ChoiceCaseNode parent, List<QName> path) {
        if (path.isEmpty())
            return parent;
        QName current = path.get(0);
        DataSchemaNode node = parent.getDataChildByName(current);
        if (node != null)
            return findNode(node, nextLevel(path));
        return null;
    }

    public static RpcDefinition getRpcByName(Module module, QName name) {
        for (RpcDefinition rpc : module.getRpcs()) {
            if (rpc.getQName().equals(name)) {
                return rpc;
            }
        }
        return null;
    }

    private static List<QName> nextLevel(List<QName> path) {
        return path.subList(1, path.size());
    }

    public static NotificationDefinition getNotificationByName(Module module, QName name) {
        for (NotificationDefinition notification : module.getNotifications()) {
            if (notification.getQName().equals(name)) {
                return notification;
            }
        }
        return null;
    }

    public static GroupingDefinition getGroupingByName(Module module, QName name) {
        for (GroupingDefinition grouping : module.getGroupings()) {
            if (grouping.getQName().equals(name)) {
                return grouping;
            }
        }
        return null;
    }

    /**
     * Utility method which search for original node defined in grouping.
     *
     * @param node
     * @return
     */
    public static DataSchemaNode findOriginal(DataSchemaNode node, SchemaContext ctx) {
        DataSchemaNode result = findCorrectTargetFromGrouping(node, ctx);
        if (result == null) {
            result = findCorrectTargetFromAugment(node, ctx);
            if (result != null) {
                if (result.isAddedByUses()) {
                    result = findOriginal(result, ctx);
                }
            }
        }
        return result;
    }

    private static DataSchemaNode findCorrectTargetFromGrouping(DataSchemaNode node, SchemaContext ctx) {
        if (node.getPath().getPath().size() == 1) {
            // uses is under module statement
            Module m = findParentModule(ctx, node);
            DataSchemaNode result = null;
            for (UsesNode u : m.getUses()) {
                SchemaNode targetGrouping = findNodeInSchemaContext(ctx, u.getGroupingPath().getPath());
                if (!(targetGrouping instanceof GroupingDefinition)) {
                    throw new IllegalArgumentException("Failed to generate code for augment in " + u);
                }
                GroupingDefinition gr = (GroupingDefinition) targetGrouping;
                result = gr.getDataChildByName(node.getQName().getLocalName());
            }
            if (result == null) {
                throw new IllegalArgumentException("Failed to generate code for augment");
            }
            return result;
        } else {
            DataSchemaNode result = null;
            QName currentName = node.getQName();
            List<QName> tmpPath = new ArrayList<>();
            Object parent = null;

            SchemaPath sp = node.getPath();
            List<QName> names = sp.getPath();
            List<QName> newNames = new ArrayList<>(names);
            newNames.remove(newNames.size() - 1);
            SchemaPath newSp = new SchemaPath(newNames, sp.isAbsolute());
            parent = findDataSchemaNode(ctx, newSp);

            do {
                tmpPath.add(currentName);
                if (parent instanceof DataNodeContainer) {
                    DataNodeContainer dataNodeParent = (DataNodeContainer) parent;
                    for (UsesNode u : dataNodeParent.getUses()) {
                        if (result == null) {
                            result = getResultFromUses(u, currentName.getLocalName(), ctx);
                        }
                    }
                }
                if (result == null) {
                    currentName = ((SchemaNode) parent).getQName();
                    if (parent instanceof SchemaNode) {
                        SchemaPath nodeSp = ((SchemaNode) parent).getPath();
                        List<QName> nodeNames = nodeSp.getPath();
                        List<QName> nodeNewNames = new ArrayList<>(nodeNames);
                        nodeNewNames.remove(nodeNewNames.size() - 1);
                        if (nodeNewNames.isEmpty()) {
                            parent = getParentModule((SchemaNode) parent, ctx);
                        } else {
                            SchemaPath nodeNewSp = new SchemaPath(nodeNewNames, nodeSp.isAbsolute());
                            parent = findDataSchemaNode(ctx, nodeNewSp);
                        }
                    } else {
                        throw new IllegalArgumentException("Failed to generate code for augment");
                    }
                }
            } while (result == null && !(parent instanceof Module));

            if (result != null) {
                result = getTargetNode(tmpPath, result, ctx);
            }
            return result;
        }
    }

    private static DataSchemaNode findCorrectTargetFromAugment(DataSchemaNode node, SchemaContext ctx) {
        if (!node.isAugmenting()) {
            return null;
        }

        QName currentName = node.getQName();
        Object currentNode = node;
        Object parent = node;
        List<QName> tmpPath = new ArrayList<QName>();
        List<SchemaNode> tmpTree = new ArrayList<SchemaNode>();

        AugmentationSchema augment = null;
        do {
            SchemaPath sp = ((SchemaNode) parent).getPath();
            List<QName> names = sp.getPath();
            List<QName> newNames = new ArrayList<>(names);
            newNames.remove(newNames.size() - 1);
            SchemaPath newSp = new SchemaPath(newNames, sp.isAbsolute());
            parent = findDataSchemaNode(ctx, newSp);
            if (parent instanceof AugmentationTarget) {
                tmpPath.add(currentName);
                tmpTree.add((SchemaNode) currentNode);
                augment = findNodeInAugment(((AugmentationTarget) parent).getAvailableAugmentations(), currentName);
                if (augment == null) {
                    currentName = ((DataSchemaNode) parent).getQName();
                    currentNode = parent;
                }
            }
        } while (((DataSchemaNode) parent).isAugmenting() && augment == null);

        if (augment == null) {
            return null;
        } else {
            Collections.reverse(tmpPath);
            Collections.reverse(tmpTree);
            Object actualParent = augment;
            DataSchemaNode result = null;
            for (QName name : tmpPath) {
                if (actualParent instanceof DataNodeContainer) {
                    result = ((DataNodeContainer) actualParent).getDataChildByName(name.getLocalName());
                    actualParent = ((DataNodeContainer) actualParent).getDataChildByName(name.getLocalName());
                } else {
                    if (actualParent instanceof ChoiceNode) {
                        result = ((ChoiceNode) actualParent).getCaseNodeByName(name.getLocalName());
                        actualParent = ((ChoiceNode) actualParent).getCaseNodeByName(name.getLocalName());
                    }
                }
            }

            if (result.isAddedByUses()) {
                result = findCorrectTargetFromAugmentGrouping(result, augment, tmpTree, ctx);
            }

            return result;
        }
    }

    private static DataSchemaNode getResultFromUses(UsesNode u, String currentName, SchemaContext ctx) {
        SchemaNode targetGrouping = findNodeInSchemaContext(ctx, u.getGroupingPath().getPath());
        if (!(targetGrouping instanceof GroupingDefinition)) {
            throw new IllegalArgumentException("Failed to generate code for augment in " + u);
        }
        GroupingDefinition gr = (GroupingDefinition) targetGrouping;
        return gr.getDataChildByName(currentName);
    }

    private static Module getParentModule(SchemaNode node, SchemaContext ctx) {
        QName qname = node.getPath().getPath().get(0);
        URI namespace = qname.getNamespace();
        Date revision = qname.getRevision();
        return ctx.findModuleByNamespaceAndRevision(namespace, revision);
    }

    private static DataSchemaNode getTargetNode(List<QName> tmpPath, DataSchemaNode node, SchemaContext ctx) {
        DataSchemaNode result = node;
        if (tmpPath.size() == 1) {
            if (result != null && result.isAddedByUses()) {
                result = findOriginal(result, ctx);
            }
            return result;
        } else {
            DataSchemaNode newParent = result;
            Collections.reverse(tmpPath);

            tmpPath.remove(0);
            for (QName name : tmpPath) {
                // searching by local name is must, because node has different
                // namespace in its original location
                if (newParent == null) {
                    break;
                }
                if (newParent instanceof DataNodeContainer) {
                    newParent = ((DataNodeContainer) newParent).getDataChildByName(name.getLocalName());
                } else {
                    newParent = ((ChoiceNode) newParent).getCaseNodeByName(name.getLocalName());
                }
            }
            if (newParent != null && newParent.isAddedByUses()) {
                newParent = findOriginal(newParent, ctx);
            }
            return newParent;
        }
    }

    private static AugmentationSchema findNodeInAugment(Collection<AugmentationSchema> augments, QName name) {
        for (AugmentationSchema augment : augments) {
            DataSchemaNode node = augment.getDataChildByName(name);
            if (node != null) {
                return augment;
            }
        }
        return null;
    }

    private static DataSchemaNode findCorrectTargetFromAugmentGrouping(DataSchemaNode node,
            AugmentationSchema parentNode, List<SchemaNode> dataTree, SchemaContext ctx) {

        DataSchemaNode result = null;
        QName currentName = node.getQName();
        List<QName> tmpPath = new ArrayList<>();
        tmpPath.add(currentName);
        int i = 1;
        Object parent = null;

        do {
            if (dataTree.size() < 2 || dataTree.size() == i) {
                parent = parentNode;
            } else {
                parent = dataTree.get(dataTree.size() - (i + 1));
                tmpPath.add(((SchemaNode) parent).getQName());
            }

            if (parent instanceof DataNodeContainer) {
                DataNodeContainer dataNodeParent = (DataNodeContainer) parent;
                for (UsesNode u : dataNodeParent.getUses()) {
                    if (result == null) {
                        result = getResultFromUses(u, currentName.getLocalName(), ctx);
                    }
                }
            }

            if (result == null) {
                i = i + 1;
                currentName = ((SchemaNode) parent).getQName();
            }
        } while (result == null);

        if (result != null) {
            result = getTargetNode(tmpPath, result, ctx);
        }
        return result;
    }

    /**
     * Transforms string representation of XPath to Queue of QNames. The XPath
     * is split by "/" and for each part of XPath is assigned correct module in
     * Schema Path. <br>
     * If Schema Context, Parent Module or XPath string contains
     * <code>null</code> values, the method will throws IllegalArgumentException
     *
     * @throws IllegalArgumentException
     *
     * @param context
     *            Schema Context
     * @param parentModule
     *            Parent Module
     * @param xpath
     *            XPath String
     * @return return a list of QName
     */
    private static List<QName> xpathToQNamePath(SchemaContext context, Module parentModule, String xpath) {
        Preconditions.checkArgument(context != null, "Schema Context reference cannot be NULL");
        Preconditions.checkArgument(parentModule != null, "Parent Module reference cannot be NULL");
        Preconditions.checkArgument(xpath != null, "XPath string reference cannot be NULL");

        List<QName> path = new LinkedList<QName>();
        String[] prefixedPath = xpath.split("/");
        for (String pathComponent : prefixedPath) {
            if (!pathComponent.isEmpty()) {
                path.add(stringPathPartToQName(context, parentModule, pathComponent));
            }
        }
        return path;
    }

    /**
     * Transforms part of Prefixed Path as java String to QName. <br>
     * If the string contains module prefix separated by ":" (i.e.
     * mod:container) this module is provided from from Parent Module list of
     * imports. If the Prefixed module is present in Schema Context the QName
     * can be constructed. <br>
     * If the Prefixed Path Part does not contains prefix the Parent's Module
     * namespace is taken for construction of QName. <br>
     * If Schema Context, Parent Module or Prefixed Path Part refers to
     * <code>null</code> the method will throw IllegalArgumentException
     *
     * @throws IllegalArgumentException
     *
     * @param context
     *            Schema Context
     * @param parentModule
     *            Parent Module
     * @param prefixedPathPart
     *            Prefixed Path Part string
     * @return QName from prefixed Path Part String.
     */
    private static QName stringPathPartToQName(SchemaContext context, Module parentModule, String prefixedPathPart) {
        Preconditions.checkArgument(context != null, "Schema Context reference cannot be NULL");
        Preconditions.checkArgument(parentModule != null, "Parent Module reference cannot be NULL");
        Preconditions.checkArgument(prefixedPathPart != null, "Prefixed Path Part cannot be NULL!");

        if (prefixedPathPart.contains(":")) {
            String[] prefixedName = prefixedPathPart.split(":");
            Module module = resolveModuleForPrefix(context, parentModule, prefixedName[0]);
            if (module == null) {
                throw new IllegalArgumentException("Failed to resolve xpath: no module found for prefix "
                        + prefixedName[0] + " in module " + parentModule.getName());
            } else {
                return new QName(module.getNamespace(), module.getRevision(), prefixedName[1]);
            }
        } else {
            return new QName(parentModule.getNamespace(), parentModule.getRevision(), prefixedPathPart);
        }
    }

    /**
     * Method will attempt to resolve and provide Module reference for specified
     * module prefix. Each Yang module could contains multiple imports which
     * MUST be associated with corresponding module prefix. The method simply
     * looks into module imports and returns the module that is bounded with
     * specified prefix. If the prefix is not present in module or the prefixed
     * module is not present in specified Schema Context, the method will return
     * <code>null</code>. <br>
     * If String prefix is the same as prefix of the specified Module the
     * reference to this module is returned. <br>
     * If Schema Context, Module or Prefix are referring to <code>null</code>
     * the method will return IllegalArgumentException
     *
     * @throws IllegalArgumentException
     *
     * @param context
     *            Schema Context
     * @param module
     *            Yang Module
     * @param prefix
     *            Module Prefix
     * @return Module for given prefix in specified Schema Context if is
     *         present, otherwise returns <code>null</code>
     */
    private static Module resolveModuleForPrefix(SchemaContext context, Module module, String prefix) {
        Preconditions.checkArgument(context != null, "Schema Context reference cannot be NULL");
        Preconditions.checkArgument(module != null, "Module reference cannot be NULL");
        Preconditions.checkArgument(prefix != null, "Prefix string cannot be NULL");

        if (prefix.equals(module.getPrefix())) {
            return module;
        }

        Set<ModuleImport> imports = module.getImports();
        for (ModuleImport mi : imports) {
            if (prefix.equals(mi.getPrefix())) {
                return context.findModuleByName(mi.getModuleName(), mi.getRevision());
            }
        }
        return null;
    }

    /**
     * @throws IllegalArgumentException
     *
     * @param context
     *            Schema Context
     * @param module
     *            Yang Module
     * @param relativeXPath
     *            Non conditional Revision Aware Relative XPath
     * @param leafrefSchemaPath
     *            Schema Path for Leafref
     * @return list of QName
     */
    private static List<QName> resolveRelativeXPath(SchemaContext context, Module module,
            RevisionAwareXPath relativeXPath, SchemaNode leafrefParentNode) {
        Preconditions.checkArgument(context != null, "Schema Context reference cannot be NULL");
        Preconditions.checkArgument(module != null, "Module reference cannot be NULL");
        Preconditions.checkArgument(relativeXPath != null, "Non Conditional Revision Aware XPath cannot be NULL");
        Preconditions.checkState(!relativeXPath.isAbsolute(),
                "Revision Aware XPath MUST be relative i.e. MUST contains ../, "
                        + "for non relative Revision Aware XPath use findDataSchemaNode method");
        Preconditions.checkState(leafrefParentNode.getPath() != null,
                "Schema Path reference for Leafref cannot be NULL");

        List<QName> absolutePath = new LinkedList<QName>();
        String strXPath = relativeXPath.toString();
        String[] xpaths = strXPath.split("/");

        int colCount = 0;
        while (xpaths[colCount].contains("..")) {
            colCount = colCount + 1;
        }
        List<QName> path = leafrefParentNode.getPath().getPath();
        if (path != null) {
            int lenght = path.size() - colCount;
            absolutePath.addAll(path.subList(0, lenght));
            List<String> xpathsList = Arrays.asList(xpaths);
            List<String> sublistedXPath = xpathsList.subList(colCount, xpaths.length);
            List<QName> sublist = new ArrayList<>();
            for (String pathPart : sublistedXPath) {
                sublist.add(stringPathPartToQName(context, module, pathPart));
            }
            absolutePath.addAll(sublist);
        }

        return absolutePath;
    }
}
