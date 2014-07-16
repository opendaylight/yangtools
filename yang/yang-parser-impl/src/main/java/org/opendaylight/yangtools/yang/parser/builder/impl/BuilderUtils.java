/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.io.ByteSource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.io.IOUtils;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
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
import org.opendaylight.yangtools.yang.parser.util.NamedByteArrayInputStream;
import org.opendaylight.yangtools.yang.parser.util.NamedFileInputStream;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BuilderUtils {

    private static final Logger LOG = LoggerFactory.getLogger(BuilderUtils.class);
    private static final Splitter SLASH_SPLITTER = Splitter.on('/').omitEmptyStrings();
    private static final Splitter COLON_SPLITTER = Splitter.on(':');
    private static final Date NULL_DATE = new Date(0L);
    private static final String INPUT = "input";
    private static final String OUTPUT = "output";

    private BuilderUtils() {
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

    public static Collection<ByteSource> filesToByteSources(final Collection<File> streams)
            throws FileNotFoundException {
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
     * Create new SchemaPath from given path and qname.
     *
     * @param schemaPath
     *            base path
     * @param qname
     *            one or more qnames added to base path
     * @return new SchemaPath from given path and qname
     *
     * @deprecated Use {@link SchemaPath#createChild(QName...)} instead.
     */
    @Deprecated
    public static SchemaPath createSchemaPath(final SchemaPath schemaPath, final QName... qname) {
        return schemaPath.createChild(qname);
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
        ModuleBuilder dependentModule;
        Date dependentModuleRevision;

        if (prefix == null) {
            dependentModule = module;
        } else if (prefix.equals(module.getPrefix())) {
            dependentModule = module;
        } else {
            ModuleImport dependentModuleImport = module.getImport(prefix);
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
     *            prefix used to reference dependent module
     * @param line
     *            current line in yang model
     * @return module based on import with given prefix if found in context,
     *         null otherwise
     * @throws YangParseException
     *             if no import found with given prefix
     */
    public static Module findModuleFromContext(final SchemaContext context, final ModuleBuilder currentModule,
            final String prefix, final int line) {
        TreeMap<Date, Module> modulesByRevision = new TreeMap<>();

        ModuleImport dependentModuleImport = currentModule.getImport(prefix);
        if (dependentModuleImport == null) {
            throw new YangParseException(currentModule.getName(), line, "No import found with prefix '" + prefix + "'.");
        }
        String dependentModuleName = dependentModuleImport.getModuleName();
        Date dependentModuleRevision = dependentModuleImport.getRevision();

        for (Module contextModule : context.getModules()) {
            if (contextModule.getName().equals(dependentModuleName)) {
                Date revision = contextModule.getRevision();
                if (revision == null) {
                    revision = NULL_DATE;
                }
                modulesByRevision.put(revision, contextModule);
            }
        }

        Module result;
        if (dependentModuleRevision == null) {
            result = modulesByRevision.get(modulesByRevision.firstKey());
        } else {
            result = modulesByRevision.get(dependentModuleRevision);
        }
        if (result == null) {
            throw new YangParseException(currentModule.getName(), line, "Module not found for prefix " + prefix);
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
        final boolean absolute = !xpathString.isEmpty() && xpathString.charAt(0) == '/';

        final List<QName> path = new ArrayList<>();
        for (String pathElement : SLASH_SPLITTER.split(xpathString)) {
            final Iterator<String> it = COLON_SPLITTER.split(pathElement).iterator();
            final String s = it.next();

            final QName name;
            if (it.hasNext()) {
                name = QName.create(QNameModule.create(null, null), s, it.next());
            } else {
                name = QName.create(QNameModule.create(null, null), s);
            }
            path.add(name);
        }
        return SchemaPath.create(path, absolute);
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

    /**
     *
     * Find a builder for node in data namespace of YANG module.
     *
     * Search is performed on full QName equals, this means builders and schema
     * path MUST be resolved against imports and their namespaces.
     *
     * Search is done in data namespace, this means notification, rpc
     * definitions and top level data definitions are considered as top-level
     * items, from which it is possible to traverse.
     *
     *
     * @param schemaPath
     *            Schema Path to node
     * @param module
     *            ModuleBuilder to start lookup in
     * @return Node Builder if found, {@link Optional#absent()} otherwise.
     */
    public static Optional<SchemaNodeBuilder> findSchemaNodeInModule(final SchemaPath schemaPath,
            final ModuleBuilder module) {
        Iterator<QName> path = schemaPath.getPathFromRoot().iterator();
        Preconditions.checkArgument(path.hasNext(), "Schema Path must contain at least one element.");
        QName first = path.next();
        Optional<SchemaNodeBuilder> currentNode = getDataNamespaceChild(module, first);

        while (currentNode.isPresent() && path.hasNext()) {
            currentNode = findDataChild(currentNode.get(), path.next());
        }
        return currentNode;
    }

    private static Optional<SchemaNodeBuilder> findDataChild(final SchemaNodeBuilder parent, final QName child) {
        if (parent instanceof DataNodeContainerBuilder) {
            return castOptional(SchemaNodeBuilder.class,
                    findDataChildInDataNodeContainer((DataNodeContainerBuilder) parent, child));
        } else if (parent instanceof ChoiceBuilder) {
            return castOptional(SchemaNodeBuilder.class, findCaseInChoice((ChoiceBuilder) parent, child));
        } else if (parent instanceof RpcDefinitionBuilder) {
            return castOptional(SchemaNodeBuilder.class, findContainerInRpc((RpcDefinitionBuilder) parent, child));

        } else {
            LOG.trace("Child {} not found in node {}", child, parent);
            return Optional.absent();
        }
    }

    /**
     * Casts optional from one argument to other.
     *
     * @param cls
     *            Class to be checked
     * @param optional
     *            Original value
     * @return
     */
    private static <T> Optional<T> castOptional(final Class<T> cls, final Optional<?> optional) {
        if (optional.isPresent()) {
            Object value = optional.get();
            if (cls.isInstance(value)) {
                @SuppressWarnings("unchecked")
                // Actually checked by outer if
                T casted = (T) value;
                return Optional.of(casted);
            }
        }
        return Optional.absent();
    }

    /**
     *
     * Gets input / output container from {@link RpcDefinitionBuilder} if QName
     * is input/output.
     *
     *
     * @param parent
     *            RPC Definition builder
     * @param child
     *            Child QName
     * @return Optional of input/output if defined and QName is input/output.
     *         Otherwise {@link Optional#absent()}.
     */
    private static Optional<ContainerSchemaNodeBuilder> findContainerInRpc(final RpcDefinitionBuilder parent, final QName child) {
        if (INPUT.equals(child.getLocalName())) {
            return Optional.of(parent.getInput());
        } else if (OUTPUT.equals(child.getLocalName())) {
            return Optional.of(parent.getOutput());
        }
        LOG.trace("Child {} not found in node {}", child, parent);
        return Optional.absent();
    }

    /**
     * Finds case by QName in {@link ChoiceBuilder}
     *
     *
     * @param parent
     *            DataNodeContainer in which lookup should be performed
     * @param child
     *            QName of child
     * @return Optional of child if found.
     */

    private static Optional<ChoiceCaseBuilder> findCaseInChoice(final ChoiceBuilder parent, final QName child) {
        for (ChoiceCaseBuilder caze : parent.getCases()) {
            if (caze.getQName().equals(child)) {
                return Optional.of(caze);
            }
        }
        LOG.trace("Child {} not found in node {}", child, parent);
        return Optional.absent();
    }

    /**
     * Finds direct child by QName in {@link DataNodeContainerBuilder}
     *
     *
     * @param parent
     *            DataNodeContainer in which lookup should be performed
     * @param child
     *            QName of child
     * @return Optional of child if found.
     */
    private static Optional<DataSchemaNodeBuilder> findDataChildInDataNodeContainer(final DataNodeContainerBuilder parent,
            final QName child) {
        for (DataSchemaNodeBuilder childNode : parent.getChildNodeBuilders()) {
            if (childNode.getQName().equals(child)) {
                return Optional.of(childNode);
            }
        }
        LOG.trace("Child {} not found in node {}", child, parent);
        return Optional.absent();
    }

    /**
     *
     * Find a child builder for node in data namespace of YANG module.
     *
     * Search is performed on full QName equals, this means builders and schema
     * path MUST be resolved against imports and their namespaces.
     *
     * Search is done in data namespace, this means notification, rpc
     * definitions and top level data definitions are considered as top-level
     * items, from which it is possible to traverse.
     *
     *
     * @param child
     *            Child QName.
     * @param module
     *            ModuleBuilder to start lookup in
     * @return Node Builder if found, {@link Optional#absent()} otherwise.
     */
    private static Optional<SchemaNodeBuilder> getDataNamespaceChild(final ModuleBuilder module, final QName child) {
        /*
         * First we do lookup in data tree, if node is found we return it.
         */
        final Optional<SchemaNodeBuilder> dataTreeNode = getDataChildByQName(module, child);
        if (dataTreeNode.isPresent()) {
            return dataTreeNode;
        }

        /*
         * We lookup in notifications
         */
        Set<NotificationBuilder> notifications = module.getAddedNotifications();
        for (NotificationBuilder notification : notifications) {
            if (notification.getQName().equals(child)) {
                return Optional.<SchemaNodeBuilder> of(notification);
            }
        }

        /*
         * We lookup in RPCs
         */
        Set<RpcDefinitionBuilder> rpcs = module.getAddedRpcs();
        for (RpcDefinitionBuilder rpc : rpcs) {
            if (rpc.getQName().equals(child)) {
                return Optional.<SchemaNodeBuilder> of(rpc);
            }
        }
        LOG.trace("Child {} not found in data namespace of module {}", child, module);
        return Optional.absent();
    }

    private static Optional<SchemaNodeBuilder> getDataChildByQName(final DataNodeContainerBuilder builder, final QName child) {
        for (DataSchemaNodeBuilder childNode : builder.getChildNodeBuilders()) {
            if (childNode.getQName().equals(child)) {
                return Optional.<SchemaNodeBuilder> of(childNode);
            }
        }
        LOG.trace("Child {} not found in node {}", child, builder);
        return Optional.absent();
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
    public static boolean processAugmentation(final AugmentationSchemaBuilder augment,
            final ModuleBuilder firstNodeParent) {
        Optional<SchemaNodeBuilder> potentialTargetNode = findSchemaNodeInModule(augment.getTargetNodeSchemaPath(),
                firstNodeParent);
        if (!potentialTargetNode.isPresent()) {
            return false;
        }
        SchemaNodeBuilder targetNode = potentialTargetNode.get();
        fillAugmentTarget(augment, targetNode);
        Preconditions.checkState(targetNode instanceof AugmentationTargetBuilder,
                "Node refered by augmentation must be valid augmentation target");
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

            ModuleBuilder dependentModule = getModuleByPrefix(module, prefix);
            if (dependentModule == null) {
                return null;
            }

            return findIdentity(dependentModule.getAddedIdentities(), name);
        } else {
            return findIdentity(module.getAddedIdentities(), baseString);
        }
    }

    public static IdentitySchemaNodeBuilder findIdentity(final Set<IdentitySchemaNodeBuilder> identities,
            final String name) {
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
        ModuleBuilder parentModule = (ModuleBuilder) parent;
        if (parentModule.isSubmodule()) {
            parentModule = parentModule.getParent();
        }
        return parentModule;
    }

    public static Set<DataSchemaNodeBuilder> wrapChildNodes(final String moduleName, final int line,
            final Set<DataSchemaNode> nodes, final SchemaPath parentPath, final QName parentQName) {
        Set<DataSchemaNodeBuilder> result = new LinkedHashSet<>();

        for (DataSchemaNode node : nodes) {
            QName qname = QName.create(parentQName, node.getQName().getLocalName());
            DataSchemaNodeBuilder wrapped = wrapChildNode(moduleName, line, node, parentPath, qname);
            result.add(wrapped);
        }
        return result;
    }

    public static DataSchemaNodeBuilder wrapChildNode(final String moduleName, final int line,
            final DataSchemaNode node, final SchemaPath parentPath, final QName qname) {

        final SchemaPath schemaPath = parentPath.createChild(qname);

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

    public static Set<GroupingBuilder> wrapGroupings(final String moduleName, final int line,
            final Set<GroupingDefinition> nodes, final SchemaPath parentPath, final QName parentQName) {
        Set<GroupingBuilder> result = new HashSet<>();
        for (GroupingDefinition node : nodes) {
            QName qname = QName.create(parentQName, node.getQName().getLocalName());
            SchemaPath schemaPath = parentPath.createChild(qname);
            result.add(new GroupingBuilderImpl(moduleName, line, qname, schemaPath, node));
        }
        return result;
    }

    public static Set<TypeDefinitionBuilder> wrapTypedefs(final String moduleName, final int line,
            final DataNodeContainer dataNode, final SchemaPath parentPath, final QName parentQName) {
        Set<TypeDefinition<?>> nodes = dataNode.getTypeDefinitions();
        Set<TypeDefinitionBuilder> result = new HashSet<>();
        for (TypeDefinition<?> node : nodes) {
            QName qname = QName.create(parentQName, node.getQName().getLocalName());
            SchemaPath schemaPath = parentPath.createChild(qname);
            result.add(new TypeDefinitionBuilderImpl(moduleName, line, qname, schemaPath, ((ExtendedType) node)));
        }
        return result;
    }

    public static List<UnknownSchemaNodeBuilderImpl> wrapUnknownNodes(final String moduleName, final int line,
            final List<UnknownSchemaNode> nodes, final SchemaPath parentPath, final QName parentQName) {
        List<UnknownSchemaNodeBuilderImpl> result = new ArrayList<>();
        for (UnknownSchemaNode node : nodes) {
            QName qname = QName.create(parentQName, node.getQName().getLocalName());
            SchemaPath schemaPath = parentPath.createChild(qname);
            result.add(new UnknownSchemaNodeBuilderImpl(moduleName, line, qname, schemaPath, node));
        }
        return result;
    }

    private static final class ByteSourceImpl extends ByteSource {
        private final String toString;
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();

        private ByteSourceImpl(final InputStream input) throws IOException {
            toString = input.toString();
            IOUtils.copy(input, output);
        }

        @Override
        public InputStream openStream() throws IOException {
            return new NamedByteArrayInputStream(output.toByteArray(), toString);
        }
    }

    public static ModuleBuilder getModuleByPrefix(final ModuleBuilder module, final String prefix) {
        if (prefix == null || prefix.isEmpty() || prefix.equals(module.getPrefix())) {
            return module;
        } else {
            return module.getImportedModule(prefix);
        }
    }

}
