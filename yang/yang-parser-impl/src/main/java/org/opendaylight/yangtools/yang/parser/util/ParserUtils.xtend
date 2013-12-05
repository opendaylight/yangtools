/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationTargetBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceBuilder.ChoiceNodeImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceCaseBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceCaseBuilder.ChoiceCaseNodeImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.ContainerSchemaNodeBuilder.ContainerSchemaNodeImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.IdentitySchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ListSchemaNodeBuilder.ListSchemaNodeImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.NotificationBuilder.NotificationDefinitionImpl;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget
import org.opendaylight.yangtools.yang.parser.builder.impl.RpcDefinitionBuilder
import java.net.URI
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingMember

public final class ParserUtils {

    private new() {
    }

    /**
     * Create new SchemaPath from given path and qname.
     *
     * @param schemaPath
     * @param qname
     * @return new SchemaPath from given path and qname
     */
    public static def SchemaPath createSchemaPath(SchemaPath schemaPath, QName... qname) {
        val path = new ArrayList<QName>(schemaPath.getPath());
        path.addAll(Arrays.asList(qname));
        return new SchemaPath(path, schemaPath.isAbsolute());
    }

    public static def SchemaPath correctSchemaPath(SchemaPath old, URI ns, Date revision, String prefix) {
        val List<QName> newPath = new ArrayList();
        for (name : old.path) {
            newPath.add(new QName(ns, revision, prefix, name.localName))
        }
        return new SchemaPath(newPath, old.absolute) 
    }

    /**
     * Get module import referenced by given prefix.
     *
     * @param builder
     *            module to search
     * @param prefix
     *            prefix associated with import
     * @return ModuleImport based on given prefix
     */
    public static def ModuleImport getModuleImport(ModuleBuilder builder, String prefix) {
        for (ModuleImport mi : builder.getModuleImports()) {
            if (mi.getPrefix().equals(prefix)) {
                return mi;

            }
        }
        return null;
    }

    /**
     * Find dependent module based on given prefix
     *
     * @param modules
     *            all available modules
     * @param module
     *            current module
     * @param prefix
     *            target module prefix
     * @param line
     *            current line in yang model
     * @return module builder if found, null otherwise
     */
    public static def ModuleBuilder findModuleFromBuilders(Map<String, TreeMap<Date, ModuleBuilder>> modules,
        ModuleBuilder module, String prefix, int line) {
        var ModuleBuilder dependentModule = null;
        var Date dependentModuleRevision = null;

        if (prefix.equals(module.getPrefix())) {
            dependentModule = module;
        } else {
            val ModuleImport dependentModuleImport = getModuleImport(module, prefix);
            if (dependentModuleImport === null) {
                throw new YangParseException(module.getName(), line, "No import found with prefix '" + prefix + "'.");
            }
            val String dependentModuleName = dependentModuleImport.getModuleName();
            dependentModuleRevision = dependentModuleImport.getRevision();

            val TreeMap<Date, ModuleBuilder> moduleBuildersByRevision = modules.get(dependentModuleName);
            if (moduleBuildersByRevision === null) {
                return null;
            }
            if (dependentModuleRevision === null) {
                dependentModule = moduleBuildersByRevision.lastEntry().getValue();
            } else {
                dependentModule = moduleBuildersByRevision.get(dependentModuleRevision);
            }
        }
        return dependentModule;
    }

    /**
     * Find module from context based on prefix.
     *
     * @param context
     *            schema context
     * @param currentModule
     *            current module
     * @param prefix
     *            current prefix used to reference dependent module
     * @param line
     *            current line in yang model
     * @return module based on given prefix if found in context, null otherwise
     */
    public static def Module findModuleFromContext(SchemaContext context, ModuleBuilder currentModule,
        String prefix, int line) {
        if (context === null) {
        	throw new YangParseException(currentModule.getName(), line, "Cannot find module with prefix '" + prefix + "'.");
        }
        val modulesByRevision = new TreeMap<Date, Module>();

        val dependentModuleImport = ParserUtils.getModuleImport(currentModule, prefix);
        if (dependentModuleImport === null) {
            throw new YangParseException(currentModule.getName(), line, "No import found with prefix '" + prefix + "'.");
        }
        val dependentModuleName = dependentModuleImport.getModuleName();
        val dependentModuleRevision = dependentModuleImport.getRevision();

        for (Module contextModule : context.getModules()) {
            if (contextModule.getName().equals(dependentModuleName)) {
                var revision = contextModule.getRevision();
                if (revision === null) {
                    revision = new Date(0L);
                }
                modulesByRevision.put(revision, contextModule);
            }
        }

        var Module result = null;
        if (dependentModuleRevision === null) {
            result = modulesByRevision.get(modulesByRevision.firstKey());
        } else {
            result = modulesByRevision.get(dependentModuleRevision);
        }
        return result;
    }

    /**
     * Parse XPath string.
     *
     * @param xpathString
     *            as String
     * @return SchemaPath from given String
     */
    public static def SchemaPath parseXPathString(String xpathString) {
        val absolute = xpathString.startsWith("/");
        val String[] splittedPath = xpathString.split("/");
        val path = new ArrayList<QName>();
        var QName name;
        for (String pathElement : splittedPath) {
            if (pathElement.length() > 0) {
                val String[] splittedElement = pathElement.split(":");
                if (splittedElement.length == 1) {
                    name = new QName(null, null, null, splittedElement.get(0));
                } else {
                    name = new QName(null, null, splittedElement.get(0), splittedElement.get(1));
                }
                path.add(name);
            }
        }
        return new SchemaPath(path, absolute);
    }

    /**
     * Add all augment's child nodes to given target.
     *
     * @param augment
     *            builder of augment statement
     * @param target
     *            augmentation target node
     */
    public static def void fillAugmentTarget(AugmentationSchemaBuilder augment, DataNodeContainerBuilder target) {
        for (DataSchemaNodeBuilder child : augment.getChildNodeBuilders()) {
            val childCopy = CopyUtils.copy(child, target, false);
            if (augment.parent instanceof UsesNodeBuilder) {
                setNodeAddedByUses(childCopy);
            }
            setNodeAugmenting(childCopy);
            correctNodePath(child, target.getPath());
            correctNodePath(childCopy, target.getPath());
            try {
                target.addChildNode(childCopy);
            } catch (YangParseException e) {
                // more descriptive message
                throw new YangParseException(augment.getModuleName(), augment.getLine(),
                    "Failed to perform augmentation: " + e.getMessage());
            }
        }
    }

    private static def void setNodeAugmenting(DataSchemaNodeBuilder child) {
        child.setAugmenting(true);
        if (child instanceof DataNodeContainerBuilder) {
            val DataNodeContainerBuilder dataNodeChild = child as DataNodeContainerBuilder;
            for (inner : dataNodeChild.getChildNodeBuilders()) {
                setNodeAugmenting(inner);
            }
        } else if (child instanceof ChoiceBuilder) {
            val ChoiceBuilder choiceChild = child as ChoiceBuilder;
            for (inner : choiceChild.cases) {
                setNodeAugmenting(inner);
            }
        }
    }

    public static def void setNodeAddedByUses(GroupingMember child) {
        child.setAddedByUses(true);
        if (child instanceof DataNodeContainerBuilder) {
            val DataNodeContainerBuilder dataNodeChild = child as DataNodeContainerBuilder;
            for (inner : dataNodeChild.getChildNodeBuilders()) {
                setNodeAddedByUses(inner);
            }
        } else if (child instanceof ChoiceBuilder) {
            val ChoiceBuilder choiceChild = child as ChoiceBuilder;
            for (inner : choiceChild.cases) {
                setNodeAddedByUses(inner);
            }
        }
    }

    /**
     * Add all augment's child nodes to given target.
     *
     * @param augment
     *            builder of augment statement
     * @param target
     *            augmentation target choice node
     */
    public static def void fillAugmentTarget(AugmentationSchemaBuilder augment, ChoiceBuilder target) {
        for (DataSchemaNodeBuilder builder : augment.getChildNodeBuilders()) {
            val childCopy = CopyUtils.copy(builder, target, false);
            if (augment.parent instanceof UsesNodeBuilder) {
                setNodeAddedByUses(childCopy);
            }
            setNodeAugmenting(childCopy)
            correctNodePath(builder, target.getPath());
            correctNodePath(childCopy, target.getPath());
            target.addCase(childCopy);
        }
        for (UsesNodeBuilder usesNode : augment.getUsesNodes()) {
            if (usesNode !== null) {
                throw new YangParseException(augment.getModuleName(), augment.getLine(),
                    "Error in augment parsing: cannot augment choice with nodes from grouping");
            }
        }
    }

    /**
     * Create new schema path of node based on parent node schema path.
     *
     * @param node
     *            node to correct
     * @param parentSchemaPath
     *            schema path of node parent
     */
    static def void correctNodePath(SchemaNodeBuilder node, SchemaPath parentSchemaPath) {

        // set correct path
        val targetNodePath = new ArrayList<QName>(parentSchemaPath.getPath());
        targetNodePath.add(node.getQName());
        node.setPath(new SchemaPath(targetNodePath, true));

        // set correct path for all child nodes
        if (node instanceof DataNodeContainerBuilder) {
            val dataNodeContainer = node as DataNodeContainerBuilder;
            for (DataSchemaNodeBuilder child : dataNodeContainer.getChildNodeBuilders()) {
                correctNodePath(child, node.getPath());
            }
        }

        // set correct path for all cases
        if (node instanceof ChoiceBuilder) {
            val choiceBuilder = node as ChoiceBuilder;
            for (ChoiceCaseBuilder choiceCaseBuilder : choiceBuilder.getCases()) {
                correctNodePath(choiceCaseBuilder, node.getPath());
            }
        }
    }



    private static def Builder findNode(Builder firstNodeParent, List<QName> path, String moduleName, int line) {
        var currentName = "";
        var currentParent = firstNodeParent;

        val max = path.size();
        var i = 0;
        while(i < max) {
            var qname = path.get(i);

            currentName = qname.getLocalName();
            if (currentParent instanceof DataNodeContainerBuilder) {
                var dataNodeContainerParent = currentParent as DataNodeContainerBuilder;
                var SchemaNodeBuilder nodeFound = dataNodeContainerParent.getDataChildByName(currentName);
                // if not found, search in notifications
                if (nodeFound == null && currentParent instanceof ModuleBuilder) {
                    nodeFound = searchNotifications(currentParent as ModuleBuilder, currentName);
                }
                // if not found, search in rpcs
                if (nodeFound == null && currentParent instanceof ModuleBuilder) {
                    nodeFound = searchRpcs(currentParent as ModuleBuilder, currentName);
                }
                // if not found, search in uses
                if (nodeFound == null) {
                    var found = searchUses(dataNodeContainerParent, currentName);
                    if(found == null) {
                        return null;
                    } else {
                        currentParent = found;
                    }
                } else {
                    currentParent = nodeFound;
                }
            } else if (currentParent instanceof ChoiceBuilder) {
                val choiceParent = currentParent as ChoiceBuilder;
                currentParent = choiceParent.getCaseNodeByName(currentName);
            } else if (currentParent instanceof RpcDefinitionBuilder) {
                val rpc = currentParent as RpcDefinitionBuilder;
                if ("input".equals(currentName)) {
                    currentParent = rpc.input;
                } else if ("output".equals(currentName)) {
                    currentParent = rpc.output;
                }
            } else {
                throw new YangParseException(moduleName, line,
                        "Error in augment parsing: failed to find node " + currentName);
            }

            // if node in path not found, return null
            if (currentParent == null) {
                return null;
            }
            i = i + 1; 
        }
        return currentParent;
    }

    private static def searchNotifications(ModuleBuilder parent, String name) {
        for(notification : parent.notifications) {
            if(notification.getQName().localName.equals(name)) {
                return notification;
            }
        }
        return null;
    }

    private static def searchRpcs(ModuleBuilder parent, String name) {
        for(rpc : parent.rpcs) {
            if(rpc.getQName().localName.equals(name)) {
                return rpc;
            }
        }
        return null;
    }

    private static def searchUses(DataNodeContainerBuilder dataNodeContainerParent, String name) {
        var currentName = name;
        for (unb : dataNodeContainerParent.usesNodes) {
            var result = searchInUsesTarget(currentName, unb);
            if (result != null) {
                return result;
            }

            result = findNodeInUses(currentName, unb);
            if (result != null) {
                var copy = CopyUtils.copy(result, unb.getParent(), true);
                unb.getTargetChildren().add(copy);
                return copy;
            }
        }
        return null;
    }
    
    public static def getRpc(ModuleBuilder module,String name) {
        for(rpc : module.getRpcs()) {
            if(name == rpc.QName.localName) {
                return rpc;
            }
        }
        return null;
    }
    
    public static def getNotification(ModuleBuilder module,String name) {
        for(notification : module.getNotifications()) {
            if(name == notification.QName.localName) {
                return notification;
            }
        }
    }
    
    private static def nextLevel(List<QName> path){
        return path.subList(1,path.size)
    }
    
    /**
     * Find augment target node and perform augmentation.
     *
     * @param augment
     * @param firstNodeParent
     *            parent of first node in path
     * @param path
     *            path to augment target
     * @return true if augmentation process succeed, false otherwise
     */
    public static def boolean processAugmentation(AugmentationSchemaBuilder augment, Builder firstNodeParent,
        List<QName> path) {

            // traverse augment target path and try to reach target node
            val targetNode = findNode(firstNodeParent,path,augment.moduleName,augment.line);
            if (targetNode === null) return false;
            
            if ((targetNode instanceof DataNodeContainerBuilder)) {
            	val targetDataNodeContainer = targetNode as DataNodeContainerBuilder;
            	augment.setTargetNodeSchemaPath(targetDataNodeContainer.getPath());
                fillAugmentTarget(augment, targetDataNodeContainer);
            } else if (targetNode instanceof ChoiceBuilder) {
            	val targetChoiceBuilder = targetNode as ChoiceBuilder;
            	augment.setTargetNodeSchemaPath(targetChoiceBuilder.getPath());
                fillAugmentTarget(augment, targetChoiceBuilder);
            } else {
                throw new YangParseException(augment.getModuleName(), augment.getLine(),
                    "Error in augment parsing: The target node MUST be either a container, list, choice, case, input, output, or notification node.");
            }
            (targetNode as AugmentationTargetBuilder).addAugmentation(augment);
            augment.setResolved(true);
            return true;
        }

    private static def DataSchemaNodeBuilder searchInUsesTarget(String localName, UsesNodeBuilder uses) {
        for(child : uses.targetChildren) {
            if (child.getQName().getLocalName().equals(localName)) {
                return child;
            }
        }
    } 

        /**
     * Find node with given name in uses target.
     *
     * @param localName
     *            name of node to find
     * @param uses
     *            uses node which target grouping should be searched
     * @return node with given name if found, null otherwise
     */
    private static def DataSchemaNodeBuilder findNodeInUses(String localName, UsesNodeBuilder uses) {
        val target = uses.groupingBuilder;
        for (child : target.childNodeBuilders) {
            if (child.getQName().getLocalName().equals(localName)) {
                return child;
            }
        }
        for (usesNode : target.usesNodes) {
            val result = findNodeInUses(localName, usesNode);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

        /**
     * Find augment target node in given context and perform augmentation.
     *
     * @param augment
     * @param path
     *            path to augment target
     * @param module
     *            current module
     * @param prefix
     *            current prefix of target module
     * @param context
     *            SchemaContext containing already resolved modules
     * @return true if augment process succeed, false otherwise
     */
        public static def boolean processAugmentationOnContext(AugmentationSchemaBuilder augment, List<QName> path,
            ModuleBuilder module, String prefix, SchemaContext context) {
            val int line = augment.getLine();
            val Module dependentModule = findModuleFromContext(context, module, prefix, line);
            if (dependentModule === null) {
                throw new YangParseException(module.getName(), line,
                    "Error in augment parsing: failed to find module with prefix " + prefix + ".");
            }

            var currentName = path.get(0).getLocalName();
            var SchemaNode currentParent = dependentModule.getDataChildByName(currentName);
            if (currentParent === null) {
                val notifications = dependentModule.getNotifications();
                for (NotificationDefinition ntf : notifications) {
                    if (ntf.getQName().getLocalName().equals(currentName)) {
                        currentParent = ntf;
                    }
                }
            }
            if (currentParent === null) {
                throw new YangParseException(module.getName(), line,
                    "Error in augment parsing: failed to find node " + currentName + ".");
            }

            for (qname : path.nextLevel) {
                currentName = qname.getLocalName();
                if (currentParent instanceof DataNodeContainer) {
                    currentParent = (currentParent as DataNodeContainer).getDataChildByName(currentName);
                } else if (currentParent instanceof ChoiceNode) {
                    currentParent = (currentParent as ChoiceNode).getCaseNodeByName(currentName);
                } else {
                    throw new YangParseException(augment.getModuleName(), line,
                        "Error in augment parsing: failed to find node " + currentName);
                }

                // if node in path not found, return false
                if (currentParent === null) {
                    throw new YangParseException(module.getName(), line,
                        "Error in augment parsing: failed to find node " + currentName + ".");
                }
            }

            val oldPath = currentParent.path;

            if (!(currentParent instanceof AugmentationTarget)) {
                throw new YangParseException(module.getName(), line,
                    "Target of type " + currentParent.class + " cannot be augmented.");
            }

            switch (currentParent) {
                case (currentParent instanceof ContainerSchemaNodeImpl): {

                    // includes container, input and output statement
                    val c = currentParent as ContainerSchemaNodeImpl;
                    val cb = c.toBuilder();
                    fillAugmentTarget(augment, cb);
                    (cb as AugmentationTargetBuilder ).addAugmentation(augment);
                    cb.rebuild();
                }
                case (currentParent instanceof ListSchemaNodeImpl): {
                    val l = currentParent as ListSchemaNodeImpl;
                    val lb = l.toBuilder();
                    fillAugmentTarget(augment, lb);
                    (lb as AugmentationTargetBuilder ).addAugmentation(augment);
                    lb.rebuild();
                    augment.setTargetNodeSchemaPath(new SchemaPath(oldPath.getPath(), oldPath.isAbsolute()));
                    augment.setResolved(true);
                }
                case (currentParent instanceof ChoiceNodeImpl): {
                    val ch = currentParent as ChoiceNodeImpl;
                    val chb = ch.toBuilder();
                    fillAugmentTarget(augment, chb);
                    (chb as AugmentationTargetBuilder ).addAugmentation(augment);
                    chb.rebuild();
                    augment.setTargetNodeSchemaPath(new SchemaPath(oldPath.getPath(), oldPath.isAbsolute()));
                    augment.setResolved(true);
                }
                case (currentParent instanceof ChoiceCaseNodeImpl): {
                    val chc = currentParent as ChoiceCaseNodeImpl;
                    val chcb = chc.toBuilder();
                    fillAugmentTarget(augment, chcb);
                    (chcb as AugmentationTargetBuilder ).addAugmentation(augment);
                    chcb.rebuild();
                    augment.setTargetNodeSchemaPath(new SchemaPath(oldPath.getPath(), oldPath.isAbsolute()));
                    augment.setResolved(true);
                }
                case (currentParent instanceof NotificationDefinitionImpl): {
                    val nd = currentParent as NotificationDefinitionImpl;
                    val nb = nd.toBuilder();
                    fillAugmentTarget(augment, nb);
                    (nb as AugmentationTargetBuilder ).addAugmentation(augment);
                    nb.rebuild();
                    augment.setTargetNodeSchemaPath(new SchemaPath(oldPath.getPath(), oldPath.isAbsolute()));
                    augment.setResolved(true);
                }
            }
            augment.setTargetNodeSchemaPath(new SchemaPath(oldPath.getPath(), oldPath.isAbsolute()));
            augment.setResolved(true);
            return true;
        }

    public static def IdentitySchemaNodeBuilder findBaseIdentity(Map<String, TreeMap<Date, ModuleBuilder>> modules,
        ModuleBuilder module, String baseString, int line) {
        var IdentitySchemaNodeBuilder result = null;
        if (baseString.contains(":")) {
            val String[] splittedBase = baseString.split(":");
            if (splittedBase.length > 2) {
                throw new YangParseException(module.getName(), line, "Failed to parse identityref base: " +
                    baseString);
            }
            val prefix = splittedBase.get(0);
            val name = splittedBase.get(1);
            val dependentModule = findModuleFromBuilders(modules, module, prefix, line);
            if (dependentModule !== null) {
                result = findIdentity(dependentModule.identities, name);
            }
        } else {
            result = findIdentity(module.identities, baseString);
        }
        return result;
    }

    public static def IdentitySchemaNode findBaseIdentityFromContext(Map<String, TreeMap<Date, ModuleBuilder>> modules,
            ModuleBuilder module, String baseString, int line, SchemaContext context) {
        var IdentitySchemaNode result = null;

        val String[] splittedBase = baseString.split(":");
        if (splittedBase.length > 2) {
            throw new YangParseException(module.getName(), line, "Failed to parse identityref base: " + baseString);
        }
        val prefix = splittedBase.get(0);
        val name = splittedBase.get(1);
        val dependentModule = findModuleFromContext(context, module, prefix, line);
        result = findIdentityNode(dependentModule.identities, name);

        if (result == null) {
            throw new YangParseException(module.name, line, "Failed to find base identity");
        }
        return result;
    }

    private static def IdentitySchemaNodeBuilder findIdentity(Set<IdentitySchemaNodeBuilder> identities, String name) {
        for (identity : identities) {
            if (identity.QName.localName.equals(name)) {
                return identity;
            }
        }
        return null;
    }

    private static def IdentitySchemaNode findIdentityNode(Set<IdentitySchemaNode> identities, String name) {
        for (identity : identities) {
            if (identity.QName.localName.equals(name)) {
                return identity;
            }
        }
        return null;
    }

        /**
     * Get module in which this node is defined.
     *
     * @param node
     * @return builder of module where this node is defined
     */
        public static def ModuleBuilder getParentModule(Builder node) {
            if (node instanceof ModuleBuilder) {
                return node as ModuleBuilder;
            }
            var parent = node.getParent();
            while (!(parent instanceof ModuleBuilder)) {
                parent = parent.getParent();
            }
            return parent as ModuleBuilder;
        }

}

