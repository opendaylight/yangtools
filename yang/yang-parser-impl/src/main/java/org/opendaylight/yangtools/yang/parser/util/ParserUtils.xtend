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
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationTargetBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceCaseBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.IdentitySchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.RpcDefinitionBuilder
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingMember
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode
import java.util.HashSet
import java.net.URI
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode
import org.opendaylight.yangtools.yang.parser.builder.impl.AnyXmlBuilder
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode
import org.opendaylight.yangtools.yang.parser.builder.impl.ContainerSchemaNodeBuilder
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode
import org.opendaylight.yangtools.yang.parser.builder.impl.ListSchemaNodeBuilder
import org.opendaylight.yangtools.yang.parser.builder.impl.LeafListSchemaNodeBuilder
import org.opendaylight.yangtools.yang.parser.builder.impl.LeafSchemaNodeBuilder
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder
import org.opendaylight.yangtools.yang.parser.builder.impl.GroupingBuilderImpl
import org.opendaylight.yangtools.yang.model.api.TypeDefinition
import org.opendaylight.yangtools.yang.parser.builder.impl.TypeDefinitionBuilderImpl
import org.opendaylight.yangtools.yang.model.util.ExtendedType
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder
import org.opendaylight.yangtools.yang.parser.builder.impl.UnknownSchemaNodeBuilder
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer
import com.google.common.base.Preconditions

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

        if(prefix == null) {
            dependentModule = module;
        } else if (prefix.equals(module.getPrefix())) {
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
            throw new YangParseException(currentModule.getName(), line,
                "Cannot find module with prefix '" + prefix + "'.");
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

    public static def dispatch fillAugmentTarget(AugmentationSchemaBuilder augment, Builder target) {
    }

    /**
     * Add all augment's child nodes to given target.
     *
     * @param augment
     *            builder of augment statement
     * @param target
     *            augmentation target node
     */
    public static def dispatch fillAugmentTarget(AugmentationSchemaBuilder augment, DataNodeContainerBuilder target) {
        for (DataSchemaNodeBuilder child : augment.getChildNodeBuilders()) {
            val childCopy = CopyUtils.copy(child, target, false);
            if (augment.parent instanceof UsesNodeBuilder) {
                setNodeAddedByUses(childCopy);
            }
            setNodeAugmenting(childCopy);
            try {
                target.addChildNode(childCopy);
            } catch (YangParseException e) {

                // more descriptive message
                throw new YangParseException(augment.getModuleName(), augment.getLine(),
                    "Failed to perform augmentation: " + e.getMessage());
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
    public static def dispatch fillAugmentTarget(AugmentationSchemaBuilder augment, ChoiceBuilder target) {
        for (DataSchemaNodeBuilder builder : augment.getChildNodeBuilders()) {
            val childCopy = CopyUtils.copy(builder, target, false);
            if (augment.parent instanceof UsesNodeBuilder) {
                setNodeAddedByUses(childCopy);
            }
            setNodeAugmenting(childCopy)
            target.addCase(childCopy);
        }
        for (UsesNodeBuilder usesNode : augment.getUsesNodeBuilders()) {
            if (usesNode !== null) {
                throw new YangParseException(augment.getModuleName(), augment.getLine(),
                    "Error in augment parsing: cannot augment choice with nodes from grouping");
            }
        }
    }

    /**
     * Set augmenting flag to true for node and all its child nodes.
     */
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

    /**
     * Set addedByUses flag to true for node and all its child nodes. 
     */
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

    public static def void setNodeConfig(DataSchemaNodeBuilder child, Boolean config) {
        if (child instanceof ContainerSchemaNodeBuilder || child instanceof LeafSchemaNodeBuilder ||
            child instanceof LeafListSchemaNodeBuilder || child instanceof ListSchemaNodeBuilder ||
            child instanceof ChoiceBuilder || child instanceof AnyXmlBuilder) {
            child.setConfiguration(config);
        }
        if (child instanceof DataNodeContainerBuilder) {
            val DataNodeContainerBuilder dataNodeChild = child as DataNodeContainerBuilder;
            for (inner : dataNodeChild.getChildNodeBuilders()) {
                setNodeConfig(inner, config);
            }
        } else if (child instanceof ChoiceBuilder) {
            val ChoiceBuilder choiceChild = child as ChoiceBuilder;
            for (inner : choiceChild.cases) {
                setNodeConfig(inner, config);
            }
        }
    }

    public static def DataSchemaNodeBuilder findSchemaNode(List<QName> path, SchemaNodeBuilder parentNode) {
        var DataSchemaNodeBuilder node
        var SchemaNodeBuilder parent = parentNode
        var int i = 0;
        while (i < path.size) {
            val String name = path.get(i).localName
            if (parent instanceof DataNodeContainerBuilder) {
                node = (parent as DataNodeContainerBuilder).getDataChildByName(name)
            } else if (parent instanceof ChoiceBuilder) {
                node = (parent as ChoiceBuilder).getCaseNodeByName(name)
            } else if (parent instanceof RpcDefinitionBuilder) {
                if ("input".equals(name)) {
                    node = (parent as RpcDefinitionBuilder).input
                } else if ("output".equals(name)) {
                    node = (parent as RpcDefinitionBuilder).output
                } else {
                    return null
                }
            } else {
                return null
            }

            if (i < path.size - 1) {
                parent = node
            }
            i = i + 1
        }

        return node
    }

    public static def SchemaNodeBuilder findSchemaNodeInModule(List<QName> pathToNode, ModuleBuilder module) {
        val List<QName> path = new ArrayList(pathToNode)
        val QName first = path.remove(0)

        var SchemaNodeBuilder node = module.getDataChildByName(first.localName)
        if (node == null) {
            val notifications = module.getAddedNotifications
            for (notification : notifications) {
                if (notification.QName.localName.equals(first.localName)) {
                    node = notification
                }
            }
        }
        if (node == null) {
            val rpcs = module.getAddedRpcs
            for (rpc : rpcs) {
                if (rpc.QName.localName.equals(first.localName)) {
                    node = rpc
                }
            }
        }
        if (node == null) {
            return null;
        }

        if (!path.empty) {
            node = findSchemaNode(path, node)
        }

        return node
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
    public static def boolean processAugmentation(AugmentationSchemaBuilder augment, ModuleBuilder firstNodeParent) {
        val path = augment.targetPath.path
        var Builder targetNode = findSchemaNodeInModule(path, firstNodeParent)
        if(targetNode === null) return false;

        if ((targetNode instanceof DataNodeContainerBuilder)) {
            val targetDataNodeContainer = targetNode as DataNodeContainerBuilder;
            augment.setTargetNodeSchemaPath(targetDataNodeContainer.getPath());
        } else if (targetNode instanceof ChoiceBuilder) {
            val targetChoiceBuilder = targetNode as ChoiceBuilder;
            augment.setTargetNodeSchemaPath(targetChoiceBuilder.getPath());
        } else {
            throw new YangParseException(augment.getModuleName(), augment.getLine(),
                "Error in augment parsing: The target node MUST be either a container, list, choice, case, input, output, or notification node.");
        }
        fillAugmentTarget(augment, targetNode);
        (targetNode as AugmentationTargetBuilder).addAugmentation(augment);
        augment.setResolved(true);
        return true;
    }

    public static def IdentitySchemaNodeBuilder findBaseIdentity(Map<String, TreeMap<Date, ModuleBuilder>> modules,
        ModuleBuilder module, String baseString, int line) {
        var IdentitySchemaNodeBuilder result = null;
        if (baseString.contains(":")) {
            val String[] splittedBase = baseString.split(":");
            if (splittedBase.length > 2) {
                throw new YangParseException(module.getName(), line,
                    "Failed to parse identityref base: " + baseString);
            }
            val prefix = splittedBase.get(0);
            val name = splittedBase.get(1);
            val dependentModule = findModuleFromBuilders(modules, module, prefix, line);
            if (dependentModule !== null) {
                result = findIdentity(dependentModule.getAddedIdentities, name);
            }
        } else {
            result = findIdentity(module.getAddedIdentities, baseString);
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
        Preconditions.checkState(parent instanceof ModuleBuilder)
        var parentModule = parent as ModuleBuilder
        if(parentModule.submodule) {
           parentModule = parentModule.parent; 
        }
        return parentModule;
    }

    public static def Set<DataSchemaNodeBuilder> wrapChildNodes(String moduleName, int line, Set<DataSchemaNode> nodes,
        SchemaPath parentPath, URI ns, Date rev, String pref) {
        val Set<DataSchemaNodeBuilder> result = new HashSet()

        for (DataSchemaNode node : nodes) {
            val qname = new QName(ns, rev, pref, node.QName.localName)
            val DataSchemaNodeBuilder wrapped = wrapChildNode(moduleName, line, node, parentPath, qname)
            result.add(wrapped)
        }
        return result
    }

    public static def DataSchemaNodeBuilder wrapChildNode(String moduleName, int line, DataSchemaNode node,
        SchemaPath parentPath, QName qname) {
        val List<QName> path = new ArrayList(parentPath.getPath())
        path.add(qname)
        val SchemaPath schemaPath = new SchemaPath(path, parentPath.isAbsolute())

        if (node instanceof AnyXmlSchemaNode) {
            return new AnyXmlBuilder(moduleName, line, qname, schemaPath, (node as AnyXmlSchemaNode));
        } else if (node instanceof ChoiceNode) {
            return new ChoiceBuilder(moduleName, line, qname, schemaPath, (node as ChoiceNode));
        } else if (node instanceof ContainerSchemaNode) {
            return new ContainerSchemaNodeBuilder(moduleName, line, qname, schemaPath, (node as ContainerSchemaNode));
        } else if (node instanceof LeafSchemaNode) {
            return new LeafSchemaNodeBuilder(moduleName, line, qname, schemaPath, (node as LeafSchemaNode));
        } else if (node instanceof LeafListSchemaNode) {
            return new LeafListSchemaNodeBuilder(moduleName, line, qname, schemaPath, (node as LeafListSchemaNode));
        } else if (node instanceof ListSchemaNode) {
            return new ListSchemaNodeBuilder(moduleName, line, qname, schemaPath, (node as ListSchemaNode));
        } else if (node instanceof ChoiceCaseNode) {
            return new ChoiceCaseBuilder(moduleName, line, qname, schemaPath, (node as ChoiceCaseNode));
        } else {
            throw new YangParseException(moduleName, line,
                "Failed to copy node: Unknown type of DataSchemaNode: " + node)
        }
    }

    public static def Set<GroupingBuilder> wrapGroupings(String moduleName, int line, Set<GroupingDefinition> nodes,
        SchemaPath parentPath, URI ns, Date rev, String pref) {
        val Set<GroupingBuilder> result = new HashSet()
        for (GroupingDefinition node : nodes) {
            val qname = new QName(ns, rev, pref, node.QName.localName)
            val List<QName> path = new ArrayList(parentPath.getPath())
            path.add(qname)
            val SchemaPath schemaPath = new SchemaPath(path, parentPath.isAbsolute())
            result.add(new GroupingBuilderImpl(moduleName, line, qname, schemaPath, node))
        }
        return result
    }

    public static def Set<TypeDefinitionBuilder> wrapTypedefs(String moduleName, int line, DataNodeContainer dataNode,
        SchemaPath parentPath, URI ns, Date rev, String pref) {
        val Set<TypeDefinition<?>> nodes = dataNode.typeDefinitions
        val Set<TypeDefinitionBuilder> result = new HashSet()
        for (TypeDefinition<?> node : nodes) {
            val qname = new QName(ns, rev, pref, node.QName.localName)
            val List<QName> path = new ArrayList(parentPath.getPath())
            path.add(qname)
            val SchemaPath schemaPath = new SchemaPath(path, parentPath.isAbsolute())
            result.add(new TypeDefinitionBuilderImpl(moduleName, line, qname, schemaPath, (node as ExtendedType)))
        }
        return result
    }

    public static def List<UnknownSchemaNodeBuilder> wrapUnknownNodes(String moduleName, int line,
        List<UnknownSchemaNode> nodes, SchemaPath parentPath, URI ns, Date rev, String pref) {
        val List<UnknownSchemaNodeBuilder> result = new ArrayList()
        for (UnknownSchemaNode node : nodes) {
            val qname = new QName(ns, rev, pref, node.QName.localName)
            val List<QName> path = new ArrayList(parentPath.getPath())
            path.add(qname)
            val SchemaPath schemaPath = new SchemaPath(path, parentPath.isAbsolute())
            result.add(new UnknownSchemaNodeBuilder(moduleName, line, qname, schemaPath, node))
        }
        return result
    }

}
