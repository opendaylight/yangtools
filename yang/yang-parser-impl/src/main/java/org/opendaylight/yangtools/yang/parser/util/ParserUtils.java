/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationTargetBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingMember;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.AnyXmlBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceCaseBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ContainerSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.GroupingBuilderImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.IdentitySchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.LeafListSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.LeafSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ListSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.NotificationBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.RpcDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.TypeDefinitionBuilderImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnknownSchemaNodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.io.ByteSource;

public final class ParserUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ParserUtils.class);
    private static final Splitter SLASH_SPLITTER = Splitter.on('/');
    private static final Splitter COLON_SPLITTER = Splitter.on(':');

    private ParserUtils() {
    }

    public static Collection<ByteSource> streamsToByteSources(final Collection<InputStream> streams) throws IOException {
        Collection<ByteSource> result = new HashSet<>();
        for (InputStream stream : streams) {
            result.add(new ByteSourceImpl(stream));
        }
        return result;
    }

    public static ByteSource fileToByteSource(final File file) {
        return new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                return new NamedFileInputStream(file, file.getAbsolutePath());
            }
        };
    }

    public static Collection<ByteSource> filesToByteSources(final Collection<File> streams) throws FileNotFoundException {
        return Collections2.transform(streams, new Function<File, ByteSource>() {
            @Override
            public ByteSource apply(final File input) {
                return new ByteSource() {
                    @Override
                    public InputStream openStream() throws IOException {
                        return new NamedFileInputStream(input, input.getAbsolutePath());
                    }
                };
            }
        });
    }

    /**
     * Set string representation of source to ModuleBuilder.
     *
     * @param sourceToBuilder
     *            source to module mapping
     */
    public static void setSourceToBuilder(final Map<ByteSource, ModuleBuilder> sourceToBuilder) throws IOException {
        for (Map.Entry<ByteSource, ModuleBuilder> entry : sourceToBuilder.entrySet()) {
            ModuleBuilder builder = entry.getValue();
            ByteSource source = entry.getKey();

            String content = null;
            InputStream stream = null;
            try {
                stream = source.openStream();
                content = IOUtils.toString(stream);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        LOG.warn("Failed to close stream {}", stream);
                    }
                }
            }
            builder.setSource(content);
        }
    }

    /**
     * Create new SchemaPath from given path and qname.
     *
     * @param schemaPath
     *            base path
     * @param qname
     *            one or more qnames added to base path
     * @return new SchemaPath from given path and qname
     */
    public static SchemaPath createSchemaPath(final SchemaPath schemaPath, final QName... qname) {
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
    public static ModuleBuilder findModuleFromBuilders(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final ModuleBuilder module, final String prefix, final int line) {
        ModuleBuilder dependentModule = null;
        Date dependentModuleRevision = null;

        if (prefix == null) {
            dependentModule = module;
        } else if (prefix.equals(module.getPrefix())) {
            dependentModule = module;
        } else {
            ModuleImport dependentModuleImport = getModuleImport(module, prefix);
            if (dependentModuleImport == null) {
                throw new YangParseException(module.getName(), line, "No import found with prefix '" + prefix + "'.");
            }
            String dependentModuleName = dependentModuleImport.getModuleName();
            dependentModuleRevision = dependentModuleImport.getRevision();

            TreeMap<Date, ModuleBuilder> moduleBuildersByRevision = modules.get(dependentModuleName);
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
    public static Module findModuleFromContext(final SchemaContext context, final ModuleBuilder currentModule, final String prefix,
            final int line) {
        if (context == null) {
            throw new YangParseException(currentModule.getName(), line, "Cannot find module with prefix '" + prefix
                    + "'.");
        }
        TreeMap<Date, Module> modulesByRevision = new TreeMap<>();

        ModuleImport dependentModuleImport = ParserUtils.getModuleImport(currentModule, prefix);
        if (dependentModuleImport == null) {
            throw new YangParseException(currentModule.getName(), line, "No import found with prefix '" + prefix + "'.");
        }
        String dependentModuleName = dependentModuleImport.getModuleName();
        Date dependentModuleRevision = dependentModuleImport.getRevision();

        for (Module contextModule : context.getModules()) {
            if (contextModule.getName().equals(dependentModuleName)) {
                Date revision = contextModule.getRevision();
                if (revision == null) {
                    revision = new Date(0L);
                }
                modulesByRevision.put(revision, contextModule);
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
     *            XPath as String
     * @return SchemaPath from given String
     */
    public static SchemaPath parseXPathString(final String xpathString) {
        final boolean absolute = xpathString.indexOf('/') == 0;

        final List<QName> path = new ArrayList<QName>();
        for (String pathElement : SLASH_SPLITTER.split(xpathString)) {
            if (pathElement.length() > 0) {
                final Iterator<String> it = COLON_SPLITTER.split(pathElement).iterator();
                final String s = it.next();

                final QName name;
                if (it.hasNext()) {
                    name = new QName(null, null, s, it.next());
                } else {
                    name = new QName(null, null, null, s);
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
    public static void fillAugmentTarget(final AugmentationSchemaBuilder augment, final Builder target) {
        if (target instanceof DataNodeContainerBuilder) {
            fillAugmentTarget(augment, (DataNodeContainerBuilder) target);
        } else if (target instanceof ChoiceBuilder) {
            fillAugmentTarget(augment, (ChoiceBuilder) target);
        } else {
            throw new YangParseException(
                    augment.getModuleName(),
                    augment.getLine(),
                    "Error in augment parsing: The target node MUST be either a container, list, choice, case, input, output, or notification node.");
        }
    }

    /**
     * Add all augment's child nodes to given target.
     *
     * @param augment
     *            builder of augment statement
     * @param target
     *            augmentation target node
     */
    private static void fillAugmentTarget(final AugmentationSchemaBuilder augment, final DataNodeContainerBuilder target) {
        for (DataSchemaNodeBuilder child : augment.getChildNodeBuilders()) {
            DataSchemaNodeBuilder childCopy = CopyUtils.copy(child, target, false);
            if (augment.getParent() instanceof UsesNodeBuilder) {
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
    private static void fillAugmentTarget(final AugmentationSchemaBuilder augment, final ChoiceBuilder target) {
        for (DataSchemaNodeBuilder builder : augment.getChildNodeBuilders()) {
            DataSchemaNodeBuilder childCopy = CopyUtils.copy(builder, target, false);
            if (augment.getParent() instanceof UsesNodeBuilder) {
                setNodeAddedByUses(childCopy);
            }
            setNodeAugmenting(childCopy);
            target.addCase(childCopy);
        }
        for (UsesNodeBuilder usesNode : augment.getUsesNodeBuilders()) {
            if (usesNode != null) {
                throw new YangParseException(augment.getModuleName(), augment.getLine(),
                        "Error in augment parsing: cannot augment choice with nodes from grouping");
            }
        }
    }

    /**
     * Set augmenting flag to true for node and all its child nodes.
     *
     * @param node
     */
    private static void setNodeAugmenting(final DataSchemaNodeBuilder node) {
        node.setAugmenting(true);
        if (node instanceof DataNodeContainerBuilder) {
            DataNodeContainerBuilder dataNodeChild = (DataNodeContainerBuilder) node;
            for (DataSchemaNodeBuilder inner : dataNodeChild.getChildNodeBuilders()) {
                setNodeAugmenting(inner);
            }
        } else if (node instanceof ChoiceBuilder) {
            ChoiceBuilder choiceChild = (ChoiceBuilder) node;
            for (ChoiceCaseBuilder inner : choiceChild.getCases()) {
                setNodeAugmenting(inner);
            }
        }
    }

    /**
     * Set addedByUses flag to true for node and all its child nodes.
     *
     * @param node
     */
    public static void setNodeAddedByUses(final GroupingMember node) {
        node.setAddedByUses(true);
        if (node instanceof DataNodeContainerBuilder) {
            DataNodeContainerBuilder dataNodeChild = (DataNodeContainerBuilder) node;
            for (DataSchemaNodeBuilder inner : dataNodeChild.getChildNodeBuilders()) {
                setNodeAddedByUses(inner);
            }
        } else if (node instanceof ChoiceBuilder) {
            ChoiceBuilder choiceChild = (ChoiceBuilder) node;
            for (ChoiceCaseBuilder inner : choiceChild.getCases()) {
                setNodeAddedByUses(inner);
            }
        }
    }

    /**
     * Set config flag to new value.
     *
     * @param node
     *            node to update
     * @param config
     *            new config value
     */
    public static void setNodeConfig(final DataSchemaNodeBuilder node, final Boolean config) {
        if (node instanceof ContainerSchemaNodeBuilder || node instanceof LeafSchemaNodeBuilder
                || node instanceof LeafListSchemaNodeBuilder || node instanceof ListSchemaNodeBuilder
                || node instanceof ChoiceBuilder || node instanceof AnyXmlBuilder) {
            node.setConfiguration(config);
        }
        if (node instanceof DataNodeContainerBuilder) {
            DataNodeContainerBuilder dataNodeChild = (DataNodeContainerBuilder) node;
            for (DataSchemaNodeBuilder inner : dataNodeChild.getChildNodeBuilders()) {
                setNodeConfig(inner, config);
            }
        } else if (node instanceof ChoiceBuilder) {
            ChoiceBuilder choiceChild = (ChoiceBuilder) node;
            for (ChoiceCaseBuilder inner : choiceChild.getCases()) {
                setNodeConfig(inner, config);
            }
        }
    }

    public static DataSchemaNodeBuilder findSchemaNode(final List<QName> path, final SchemaNodeBuilder parentNode) {
        DataSchemaNodeBuilder node = null;
        SchemaNodeBuilder parent = parentNode;
        int i = 0;
        while (i < path.size()) {
            String name = path.get(i).getLocalName();
            if (parent instanceof DataNodeContainerBuilder) {
                node = ((DataNodeContainerBuilder) parent).getDataChildByName(name);
            } else if (parent instanceof ChoiceBuilder) {
                node = ((ChoiceBuilder) parent).getCaseNodeByName(name);
            } else if (parent instanceof RpcDefinitionBuilder) {
                if ("input".equals(name)) {
                    node = ((RpcDefinitionBuilder) parent).getInput();
                } else if ("output".equals(name)) {
                    node = ((RpcDefinitionBuilder) parent).getOutput();
                } else {
                    return null;
                }
            } else {
                return null;
            }

            if (i < path.size() - 1) {
                parent = node;
            }
            i = i + 1;
        }

        return node;
    }

    public static SchemaNodeBuilder findSchemaNodeInModule(final List<QName> pathToNode, final ModuleBuilder module) {
        List<QName> path = new ArrayList<>(pathToNode);
        QName first = path.remove(0);

        SchemaNodeBuilder node = module.getDataChildByName(first.getLocalName());
        if (node == null) {
            Set<NotificationBuilder> notifications = module.getAddedNotifications();
            for (NotificationBuilder notification : notifications) {
                if (notification.getQName().getLocalName().equals(first.getLocalName())) {
                    node = notification;
                }
            }
        }
        if (node == null) {
            Set<RpcDefinitionBuilder> rpcs = module.getAddedRpcs();
            for (RpcDefinitionBuilder rpc : rpcs) {
                if (rpc.getQName().getLocalName().equals(first.getLocalName())) {
                    node = rpc;
                }
            }
        }
        if (node == null) {
            return null;
        }

        if (!path.isEmpty()) {
            node = findSchemaNode(path, node);
        }

        return node;
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
    public static boolean processAugmentation(final AugmentationSchemaBuilder augment, final ModuleBuilder firstNodeParent) {
        List<QName> path = augment.getTargetPath().getPath();
        Builder targetNode = findSchemaNodeInModule(path, firstNodeParent);
        if (targetNode == null) {
            return false;
        }

        fillAugmentTarget(augment, targetNode);
        ((AugmentationTargetBuilder) targetNode).addAugmentation(augment);
        augment.setResolved(true);
        return true;
    }

    public static IdentitySchemaNodeBuilder findBaseIdentity(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final ModuleBuilder module, final String baseString, final int line) {

        // FIXME: optimize indexOf() away?
        if (baseString.indexOf(':') != -1) {
            final Iterator<String> it = COLON_SPLITTER.split(baseString).iterator();
            final String prefix = it.next();
            final String name = it.next();

            if (it.hasNext()) {
                throw new YangParseException(module.getName(), line, "Failed to parse identityref base: " + baseString);
            }

            ModuleBuilder dependentModule = findModuleFromBuilders(modules, module, prefix, line);
            if (dependentModule == null) {
                return null;
            }

            return findIdentity(dependentModule.getAddedIdentities(), name);
        } else {
            return findIdentity(module.getAddedIdentities(), baseString);
        }
    }

    public static IdentitySchemaNodeBuilder findIdentity(final Set<IdentitySchemaNodeBuilder> identities, final String name) {
        for (IdentitySchemaNodeBuilder identity : identities) {
            if (identity.getQName().getLocalName().equals(name)) {
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
    public static ModuleBuilder getParentModule(final Builder node) {
        if (node instanceof ModuleBuilder) {
            return (ModuleBuilder) node;
        }
        Builder parent = node.getParent();
        while (!(parent instanceof ModuleBuilder)) {
            parent = parent.getParent();
        }
        Preconditions.checkState(parent instanceof ModuleBuilder);
        ModuleBuilder parentModule = (ModuleBuilder) parent;
        if (parentModule.isSubmodule()) {
            parentModule = parentModule.getParent();
        }
        return parentModule;
    }

    public static Set<DataSchemaNodeBuilder> wrapChildNodes(final String moduleName, final int line, final Set<DataSchemaNode> nodes,
            final SchemaPath parentPath, final URI ns, final Date rev, final String pref) {
        Set<DataSchemaNodeBuilder> result = new HashSet<>();

        for (DataSchemaNode node : nodes) {
            QName qname = new QName(ns, rev, pref, node.getQName().getLocalName());
            DataSchemaNodeBuilder wrapped = wrapChildNode(moduleName, line, node, parentPath, qname);
            result.add(wrapped);
        }
        return result;
    }

    public static DataSchemaNodeBuilder wrapChildNode(final String moduleName, final int line, final DataSchemaNode node,
            final SchemaPath parentPath, final QName qname) {
        List<QName> path = new ArrayList<>(parentPath.getPath());
        path.add(qname);
        SchemaPath schemaPath = new SchemaPath(path, parentPath.isAbsolute());

        if (node instanceof AnyXmlSchemaNode) {
            return new AnyXmlBuilder(moduleName, line, qname, schemaPath, ((AnyXmlSchemaNode) node));
        } else if (node instanceof ChoiceNode) {
            return new ChoiceBuilder(moduleName, line, qname, schemaPath, ((ChoiceNode) node));
        } else if (node instanceof ContainerSchemaNode) {
            return new ContainerSchemaNodeBuilder(moduleName, line, qname, schemaPath, ((ContainerSchemaNode) node));
        } else if (node instanceof LeafSchemaNode) {
            return new LeafSchemaNodeBuilder(moduleName, line, qname, schemaPath, ((LeafSchemaNode) node));
        } else if (node instanceof LeafListSchemaNode) {
            return new LeafListSchemaNodeBuilder(moduleName, line, qname, schemaPath, ((LeafListSchemaNode) node));
        } else if (node instanceof ListSchemaNode) {
            return new ListSchemaNodeBuilder(moduleName, line, qname, schemaPath, ((ListSchemaNode) node));
        } else if (node instanceof ChoiceCaseNode) {
            return new ChoiceCaseBuilder(moduleName, line, qname, schemaPath, ((ChoiceCaseNode) node));
        } else {
            throw new YangParseException(moduleName, line, "Failed to copy node: Unknown type of DataSchemaNode: "
                    + node);
        }
    }

    public static Set<GroupingBuilder> wrapGroupings(final String moduleName, final int line, final Set<GroupingDefinition> nodes,
            final SchemaPath parentPath, final URI ns, final Date rev, final String pref) {
        Set<GroupingBuilder> result = new HashSet<>();
        for (GroupingDefinition node : nodes) {
            QName qname = new QName(ns, rev, pref, node.getQName().getLocalName());
            List<QName> path = new ArrayList<>(parentPath.getPath());
            path.add(qname);
            SchemaPath schemaPath = new SchemaPath(path, parentPath.isAbsolute());
            result.add(new GroupingBuilderImpl(moduleName, line, qname, schemaPath, node));
        }
        return result;
    }

    public static Set<TypeDefinitionBuilder> wrapTypedefs(final String moduleName, final int line, final DataNodeContainer dataNode,
            final SchemaPath parentPath, final URI ns, final Date rev, final String pref) {
        Set<TypeDefinition<?>> nodes = dataNode.getTypeDefinitions();
        Set<TypeDefinitionBuilder> result = new HashSet<>();
        for (TypeDefinition<?> node : nodes) {
            QName qname = new QName(ns, rev, pref, node.getQName().getLocalName());
            List<QName> path = new ArrayList<>(parentPath.getPath());
            path.add(qname);
            SchemaPath schemaPath = new SchemaPath(path, parentPath.isAbsolute());
            result.add(new TypeDefinitionBuilderImpl(moduleName, line, qname, schemaPath, ((ExtendedType) node)));
        }
        return result;
    }

    public static List<UnknownSchemaNodeBuilder> wrapUnknownNodes(final String moduleName, final int line,
            final List<UnknownSchemaNode> nodes, final SchemaPath parentPath, final URI ns, final Date rev, final String pref) {
        List<UnknownSchemaNodeBuilder> result = new ArrayList<>();
        for (UnknownSchemaNode node : nodes) {
            QName qname = new QName(ns, rev, pref, node.getQName().getLocalName());
            List<QName> path = new ArrayList<>(parentPath.getPath());
            path.add(qname);
            SchemaPath schemaPath = new SchemaPath(path, parentPath.isAbsolute());
            result.add(new UnknownSchemaNodeBuilder(moduleName, line, qname, schemaPath, node));
        }
        return result;
    }

    private static final class ByteSourceImpl extends ByteSource {
        private final String toString;
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();

        private ByteSourceImpl(InputStream input) throws IOException {
            toString = input.toString();
            IOUtils.copy(input, output);
        }

        @Override
        public InputStream openStream() throws IOException {
            return new NamedByteArrayInputStream(output.toByteArray(), toString);
        }
    }

}
