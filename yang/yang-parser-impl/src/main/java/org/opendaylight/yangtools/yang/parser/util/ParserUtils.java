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
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceBuilder.ChoiceNodeImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceCaseBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceCaseBuilder.ChoiceCaseNodeImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.ContainerSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ContainerSchemaNodeBuilder.ContainerSchemaNodeImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.IdentityrefTypeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ListSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ListSchemaNodeBuilder.ListSchemaNodeImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.NotificationBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.NotificationBuilder.NotificationDefinitionImpl;

public final class ParserUtils {

    private ParserUtils() {
    }

    /**
     * Create new SchemaPath from given path and qname.
     *
     * @param schemaPath
     * @param qname
     * @return
     */
    public static SchemaPath createSchemaPath(SchemaPath schemaPath, QName... qname) {
        List<QName> path = new ArrayList<>(schemaPath.getPath());
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
    public static ModuleImport getModuleImport(final ModuleBuilder builder, final String prefix) {
        ModuleImport moduleImport = null;
        for (ModuleImport mi : builder.getModuleImports()) {
            if (mi.getPrefix().equals(prefix)) {
                moduleImport = mi;
                break;
            }
        }
        return moduleImport;
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
    public static ModuleBuilder findDependentModuleBuilder(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final ModuleBuilder module, final String prefix, final int line) {
        ModuleBuilder dependentModule = null;
        Date dependentModuleRevision = null;

        if (prefix.equals(module.getPrefix())) {
            dependentModule = module;
        } else {
            final ModuleImport dependentModuleImport = getModuleImport(module, prefix);
            if (dependentModuleImport == null) {
                throw new YangParseException(module.getName(), line, "No import found with prefix '" + prefix + "'.");
            }
            final String dependentModuleName = dependentModuleImport.getModuleName();
            dependentModuleRevision = dependentModuleImport.getRevision();

            final TreeMap<Date, ModuleBuilder> moduleBuildersByRevision = modules.get(dependentModuleName);
            if (moduleBuildersByRevision == null) {
                return null;
            }
            if (dependentModuleRevision == null) {
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
    public static Module findModuleFromContext(final SchemaContext context, final ModuleBuilder currentModule,
            final String prefix, final int line) {
        TreeMap<Date, Module> modulesByRevision = new TreeMap<Date, Module>();

        final ModuleImport dependentModuleImport = ParserUtils.getModuleImport(currentModule, prefix);
        if (dependentModuleImport == null) {
            throw new YangParseException(currentModule.getName(), line, "No import found with prefix '" + prefix + "'.");
        }
        final String dependentModuleName = dependentModuleImport.getModuleName();
        final Date dependentModuleRevision = dependentModuleImport.getRevision();

        for (Module contextModule : context.getModules()) {
            if (contextModule.getName().equals(dependentModuleName)) {
                Date revision = contextModule.getRevision();
                if (revision == null) {
                    revision = new Date(0L);
                }
                modulesByRevision.put(revision, contextModule);
                break;
            }
        }

        Module result = null;
        if (dependentModuleRevision == null) {
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
    public static SchemaPath parseXPathString(final String xpathString) {
        final boolean absolute = xpathString.startsWith("/");
        final String[] splittedPath = xpathString.split("/");
        final List<QName> path = new ArrayList<QName>();
        QName name;
        for (String pathElement : splittedPath) {
            if (pathElement.length() > 0) {
                final String[] splittedElement = pathElement.split(":");
                if (splittedElement.length == 1) {
                    name = new QName(null, null, null, splittedElement[0]);
                } else {
                    name = new QName(null, null, splittedElement[0], splittedElement[1]);
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
    public static void fillAugmentTarget(final AugmentationSchemaBuilder augment, final DataNodeContainerBuilder target) {
        for (DataSchemaNodeBuilder child : augment.getChildNodeBuilders()) {
            DataSchemaNodeBuilder childCopy = CopyUtils.copy(child, target, false);
            childCopy.setAugmenting(true);
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
        for (UsesNodeBuilder usesNode : augment.getUsesNodes()) {
            UsesNodeBuilder copy = CopyUtils.copyUses(usesNode, target);
            target.addUsesNode(copy);
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
    public static void fillAugmentTarget(final AugmentationSchemaBuilder augment, final ChoiceBuilder target) {
        for (DataSchemaNodeBuilder builder : augment.getChildNodeBuilders()) {
            DataSchemaNodeBuilder childCopy = CopyUtils.copy(builder, target, false);
            childCopy.setAugmenting(true);
            correctNodePath(builder, target.getPath());
            correctNodePath(childCopy, target.getPath());
            target.addCase(childCopy);
        }
        for (UsesNodeBuilder usesNode : augment.getUsesNodes()) {
            if (usesNode != null) {
                throw new YangParseException(augment.getModuleName(), augment.getLine(),
                        "Error in augment parsing: cannot augment uses to choice");
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
    static void correctNodePath(final SchemaNodeBuilder node, final SchemaPath parentSchemaPath) {
        // set correct path
        List<QName> targetNodePath = new ArrayList<QName>(parentSchemaPath.getPath());
        targetNodePath.add(node.getQName());
        node.setPath(new SchemaPath(targetNodePath, true));

        // set correct path for all child nodes
        if (node instanceof DataNodeContainerBuilder) {
            DataNodeContainerBuilder dataNodeContainer = (DataNodeContainerBuilder) node;
            for (DataSchemaNodeBuilder child : dataNodeContainer.getChildNodeBuilders()) {
                correctNodePath(child, node.getPath());
            }
        }

        // set correct path for all cases
        if (node instanceof ChoiceBuilder) {
            ChoiceBuilder choiceBuilder = (ChoiceBuilder) node;
            for (ChoiceCaseBuilder choiceCaseBuilder : choiceBuilder.getCases()) {
                correctNodePath(choiceCaseBuilder, node.getPath());
            }
        }
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
    public static boolean processAugmentation(final AugmentationSchemaBuilder augment, final Builder firstNodeParent,
            final List<QName> path) {
        // traverse augment target path and try to reach target node
        String currentName = null;
        Builder currentParent = firstNodeParent;

        for (int i = 0; i < path.size(); i++) {
            QName qname = path.get(i);

            currentName = qname.getLocalName();
            if (currentParent instanceof DataNodeContainerBuilder) {
                DataSchemaNodeBuilder nodeFound = ((DataNodeContainerBuilder) currentParent)
                        .getDataChildByName(currentName);
                // if not found as regular child, search in uses
                if (nodeFound == null) {
                    boolean found = false;
                    for (UsesNodeBuilder unb : ((DataNodeContainerBuilder) currentParent).getUsesNodes()) {
                        DataSchemaNodeBuilder result = findNodeInUses(currentName, unb);
                        if (result != null) {
                            currentParent = result;
                            found = true;
                            break;
                        }
                    }
                    // if not found even in uses nodes, return false
                    if (!found) {
                        return false;
                    }
                } else {
                    currentParent = nodeFound;
                }
            } else if (currentParent instanceof ChoiceBuilder) {
                currentParent = ((ChoiceBuilder) currentParent).getCaseNodeByName(currentName);
            } else {
                throw new YangParseException(augment.getModuleName(), augment.getLine(),
                        "Error in augment parsing: failed to find node " + currentName);
            }

            // if node in path not found, return false
            if (currentParent == null) {
                return false;
            }
        }
        if (!(currentParent instanceof DataSchemaNodeBuilder)) {
            throw new YangParseException(
                    augment.getModuleName(),
                    augment.getLine(),
                    "Error in augment parsing: The target node MUST be either a container, list, choice, case, input, output, or notification node.");
        }

        if (currentParent instanceof ChoiceBuilder) {
            fillAugmentTarget(augment, (ChoiceBuilder) currentParent);
        } else {
            fillAugmentTarget(augment, (DataNodeContainerBuilder) currentParent);
        }
        ((AugmentationTargetBuilder) currentParent).addAugmentation(augment);
        SchemaPath oldPath = ((DataSchemaNodeBuilder) currentParent).getPath();
        augment.setTargetPath(new SchemaPath(oldPath.getPath(), oldPath.isAbsolute()));
        augment.setResolved(true);

        return true;
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
    private static DataSchemaNodeBuilder findNodeInUses(String localName, UsesNodeBuilder uses) {
        GroupingBuilder target = uses.getGroupingBuilder();
        for (DataSchemaNodeBuilder child : target.getChildNodeBuilders()) {
            if (child.getQName().getLocalName().equals(localName)) {
                return child;
            }
        }
        for (UsesNodeBuilder usesNode : target.getUsesNodes()) {
            DataSchemaNodeBuilder result = findNodeInUses(localName, usesNode);
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
    public static boolean processAugmentationOnContext(final AugmentationSchemaBuilder augment, final List<QName> path,
            final ModuleBuilder module, final String prefix, final SchemaContext context) {
        final int line = augment.getLine();
        final Module dependentModule = findModuleFromContext(context, module, prefix, line);
        if (dependentModule == null) {
            throw new YangParseException(module.getName(), line,
                    "Error in augment parsing: failed to find module with prefix " + prefix + ".");
        }

        String currentName = path.get(0).getLocalName();
        SchemaNode currentParent = dependentModule.getDataChildByName(currentName);
        if (currentParent == null) {
            Set<NotificationDefinition> notifications = dependentModule.getNotifications();
            for (NotificationDefinition ntf : notifications) {
                if (ntf.getQName().getLocalName().equals(currentName)) {
                    currentParent = ntf;
                    break;
                }
            }
        }
        if (currentParent == null) {
            throw new YangParseException(module.getName(), line, "Error in augment parsing: failed to find node "
                    + currentName + ".");
        }

        for (int i = 1; i < path.size(); i++) {
            currentName = path.get(i).getLocalName();
            if (currentParent instanceof DataNodeContainer) {
                currentParent = ((DataNodeContainer) currentParent).getDataChildByName(currentName);
            } else if (currentParent instanceof ChoiceNode) {
                currentParent = ((ChoiceNode) currentParent).getCaseNodeByName(currentName);
            } else {
                throw new YangParseException(augment.getModuleName(), line,
                        "Error in augment parsing: failed to find node " + currentName);
            }
            // if node in path not found, return false
            if (currentParent == null) {
                throw new YangParseException(module.getName(), line, "Error in augment parsing: failed to find node "
                        + currentName + ".");
            }
        }

        if (currentParent instanceof ContainerSchemaNodeImpl) {
            // includes container, input and output statement
            ContainerSchemaNodeImpl c = (ContainerSchemaNodeImpl) currentParent;
            ContainerSchemaNodeBuilder cb = c.toBuilder();
            fillAugmentTarget(augment, cb);
            ((AugmentationTargetBuilder) cb).addAugmentation(augment);
            SchemaPath oldPath = cb.getPath();
            cb.rebuild();
            augment.setTargetPath(new SchemaPath(oldPath.getPath(), oldPath.isAbsolute()));
            augment.setResolved(true);
        } else if (currentParent instanceof ListSchemaNodeImpl) {
            ListSchemaNodeImpl l = (ListSchemaNodeImpl) currentParent;
            ListSchemaNodeBuilder lb = l.toBuilder();
            fillAugmentTarget(augment, lb);
            ((AugmentationTargetBuilder) lb).addAugmentation(augment);
            SchemaPath oldPath = lb.getPath();
            lb.rebuild();
            augment.setTargetPath(new SchemaPath(oldPath.getPath(), oldPath.isAbsolute()));
            augment.setResolved(true);
        } else if (currentParent instanceof ChoiceNodeImpl) {
            ChoiceNodeImpl ch = (ChoiceNodeImpl) currentParent;
            ChoiceBuilder chb = ch.toBuilder();
            fillAugmentTarget(augment, chb);
            ((AugmentationTargetBuilder) chb).addAugmentation(augment);
            SchemaPath oldPath = chb.getPath();
            chb.rebuild();
            augment.setTargetPath(new SchemaPath(oldPath.getPath(), oldPath.isAbsolute()));
            augment.setResolved(true);
        } else if (currentParent instanceof ChoiceCaseNodeImpl) {
            ChoiceCaseNodeImpl chc = (ChoiceCaseNodeImpl) currentParent;
            ChoiceCaseBuilder chcb = chc.toBuilder();
            fillAugmentTarget(augment, chcb);
            ((AugmentationTargetBuilder) chcb).addAugmentation(augment);
            SchemaPath oldPath = chcb.getPath();
            chcb.rebuild();
            augment.setTargetPath(new SchemaPath(oldPath.getPath(), oldPath.isAbsolute()));
            augment.setResolved(true);
        } else if (currentParent instanceof NotificationDefinitionImpl) {
            NotificationDefinitionImpl nd = (NotificationDefinitionImpl) currentParent;
            NotificationBuilder nb = nd.toBuilder();
            fillAugmentTarget(augment, nb);
            ((AugmentationTargetBuilder) nb).addAugmentation(augment);
            SchemaPath oldPath = nb.getPath();
            nb.rebuild();
            augment.setTargetPath(new SchemaPath(oldPath.getPath(), oldPath.isAbsolute()));
            augment.setResolved(true);
        } else {
            throw new YangParseException(module.getName(), line, "Target of type " + currentParent.getClass()
                    + " cannot be augmented.");
        }

        return true;
    }

    public static QName findFullQName(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final ModuleBuilder module, final IdentityrefTypeBuilder idref) {
        QName result = null;
        String baseString = idref.getBaseString();
        if (baseString.contains(":")) {
            String[] splittedBase = baseString.split(":");
            if (splittedBase.length > 2) {
                throw new YangParseException(module.getName(), idref.getLine(), "Failed to parse identityref base: "
                        + baseString);
            }
            String prefix = splittedBase[0];
            String name = splittedBase[1];
            ModuleBuilder dependentModule = findDependentModuleBuilder(modules, module, prefix, idref.getLine());
            result = new QName(dependentModule.getNamespace(), dependentModule.getRevision(), prefix, name);
        } else {
            result = new QName(module.getNamespace(), module.getRevision(), module.getPrefix(), baseString);
        }
        return result;
    }

    /**
     * Get module in which this node is defined.
     *
     * @param node
     * @return builder of module where this node is defined
     */
    public static ModuleBuilder getParentModule(Builder node) {
        if (node instanceof ModuleBuilder) {
            return (ModuleBuilder) node;
        }

        Builder parent = node.getParent();
        while (!(parent instanceof ModuleBuilder)) {
            parent = parent.getParent();
        }
        return (ModuleBuilder) parent;
    }

}
