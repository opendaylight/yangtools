/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import java.util.Map.Entry;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import org.antlr.v4.runtime.tree.ParseTree;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Belongs_to_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Module_header_stmtsContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Module_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Namespace_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Revision_stmtsContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Submodule_header_stmtsContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Submodule_stmtContext;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
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
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils;
import org.opendaylight.yangtools.yang.parser.impl.util.YangModelDependencyInfo;
import org.opendaylight.yangtools.yang.parser.util.NamedByteArrayInputStream;
import org.opendaylight.yangtools.yang.parser.util.NamedFileInputStream;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BuilderUtils {

    private static final Logger LOG = LoggerFactory.getLogger(BuilderUtils.class);
    private static final Splitter COLON_SPLITTER = Splitter.on(':');
    private static final Date NULL_DATE = new Date(0L);
    private static final String INPUT = "input";
    private static final String OUTPUT = "output";
    private static final String CHILD_NOT_FOUND_IN_NODE_STR = "Child {} not found in node {}";

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
    public static ModuleBuilder findModuleFromBuilders(final Map<String, NavigableMap<Date, ModuleBuilder>> modules,
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

            NavigableMap<Date, ModuleBuilder> moduleBuildersByRevision = modules.get(dependentModuleName);
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

    public static ModuleBuilder findModuleFromBuilders(ModuleImport imp, Iterable<ModuleBuilder> modules) {
        String name = imp.getModuleName();
        Date revision = imp.getRevision();
        NavigableMap<Date, ModuleBuilder> map = new TreeMap<>();
        for (ModuleBuilder module : modules) {
            if (module != null && module.getName().equals(name)) {
                map.put(module.getRevision(), module);
            }
        }
        if (map.isEmpty()) {
            return null;
        }
        if (revision == null) {
            return map.lastEntry().getValue();
        }
        return map.get(revision);
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
        NavigableMap<Date, Module> modulesByRevision = new TreeMap<>();

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
     * @param node grouping member node
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

    public static SchemaNodeBuilder findSchemaNode(final Iterable<QName> path, final SchemaNodeBuilder parentNode) {
        SchemaNodeBuilder node = null;
        SchemaNodeBuilder parent = parentNode;
        int size = Iterables.size(path);
        int i = 0;
        for (QName qname : path) {
            String name = qname.getLocalName();
            if (parent instanceof DataNodeContainerBuilder) {
                node = ((DataNodeContainerBuilder) parent).getDataChildByName(name);
                if (node == null) {
                    node = findUnknownNode(name, parent);
                }
            } else if (parent instanceof ChoiceBuilder) {
                node = ((ChoiceBuilder) parent).getCaseNodeByName(name);
                if (node == null) {
                    node = findUnknownNode(name, parent);
                }
            } else if (parent instanceof RpcDefinitionBuilder) {
                if (INPUT.equals(name)) {
                    node = ((RpcDefinitionBuilder) parent).getInput();
                } else if (OUTPUT.equals(name)) {
                    node = ((RpcDefinitionBuilder) parent).getOutput();
                } else {
                    if (node == null) {
                        node = findUnknownNode(name, parent);
                    }
                }
            } else {
                node = findUnknownNode(name, parent);
            }

            if (i < size - 1) {
                parent = node;
            }
            i = i + 1;
        }

        return node;
    }

    private static UnknownSchemaNodeBuilder findUnknownNode(final String name, final Builder parent) {
        for (UnknownSchemaNodeBuilder un : parent.getUnknownNodes()) {
            if (un.getQName().getLocalName().equals(name)) {
                return un;
            }
        }
        return null;
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
            SchemaNodeBuilder currentParent = currentNode.get();
            QName currentPath = path.next();
            currentNode = findDataChild(currentParent, currentPath);
            if (!currentNode.isPresent()) {
                for (SchemaNodeBuilder un : currentParent.getUnknownNodes()) {
                    if (un.getQName().equals(currentPath)) {
                        currentNode = Optional.of(un);
                    }
                }
            }
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
            LOG.trace(CHILD_NOT_FOUND_IN_NODE_STR, child, parent);
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
     * @return Optional object with type argument casted as cls
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

    // FIXME: if rpc does not define input or output, this method creates it
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
    private static Optional<ContainerSchemaNodeBuilder> findContainerInRpc(final RpcDefinitionBuilder parent,
            final QName child) {
        if (INPUT.equals(child.getLocalName())) {
            if (parent.getInput() == null) {
                QName qname = QName.create(parent.getQName().getModule(), INPUT);
                final ContainerSchemaNodeBuilder inputBuilder = new ContainerSchemaNodeBuilder(parent.getModuleName(),
                        parent.getLine(), qname, parent.getPath().createChild(qname));
                inputBuilder.setParent(parent);
                parent.setInput(inputBuilder);
                return Optional.of(inputBuilder);
            }
            return Optional.of(parent.getInput());
        } else if (OUTPUT.equals(child.getLocalName())) {
            if (parent.getOutput() == null) {
                QName qname = QName.create(parent.getQName().getModule(), OUTPUT);
                final ContainerSchemaNodeBuilder outputBuilder = new ContainerSchemaNodeBuilder(parent.getModuleName(),
                        parent.getLine(), qname, parent.getPath().createChild(qname));
                outputBuilder.setParent(parent);
                parent.setOutput(outputBuilder);
                return Optional.of(outputBuilder);
            }
            return Optional.of(parent.getOutput());
        }
        LOG.trace(CHILD_NOT_FOUND_IN_NODE_STR, child, parent);
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
        LOG.trace(CHILD_NOT_FOUND_IN_NODE_STR, child, parent);
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
        LOG.trace(CHILD_NOT_FOUND_IN_NODE_STR, child, parent);
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
        LOG.trace(CHILD_NOT_FOUND_IN_NODE_STR, child, builder);
        return Optional.absent();
    }

    /**
     * Find augment target node and perform augmentation.
     *
     * @param augment
     *            augment builder to process
     * @param firstNodeParent
     *            parent of first node in path
     * @return true if augmentation process succeed, false otherwise
     */
    public static boolean processAugmentation(final AugmentationSchemaBuilder augment,
            final ModuleBuilder firstNodeParent) {
        Optional<SchemaNodeBuilder> potentialTargetNode = findSchemaNodeInModule(augment.getTargetPath(),
                firstNodeParent);
        if (!potentialTargetNode.isPresent()) {
            return false;
        } else if (potentialTargetNode.get() instanceof UnknownSchemaNodeBuilder) {
            LOG.warn("Error in augment parsing: unsupported augment target: {}", potentialTargetNode.get());
            return true;
        }
        SchemaNodeBuilder targetNode = potentialTargetNode.get();
        fillAugmentTarget(augment, targetNode);
        Preconditions.checkState(targetNode instanceof AugmentationTargetBuilder,
                "Node refered by augmentation must be valid augmentation target");
        ((AugmentationTargetBuilder) targetNode).addAugmentation(augment);
        augment.setResolved(true);
        return true;
    }

    public static IdentitySchemaNodeBuilder findBaseIdentity(final ModuleBuilder module, final String baseString,
            final int line) {

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
     * @param node node
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
            final Collection<DataSchemaNode> nodes, final SchemaPath parentPath, final QName parentQName) {
        Set<DataSchemaNodeBuilder> result = new LinkedHashSet<>(nodes.size());

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
            return new AnyXmlBuilder(moduleName, line, qname, schemaPath, (AnyXmlSchemaNode) node);
        } else if (node instanceof ChoiceSchemaNode) {
            return new ChoiceBuilder(moduleName, line, qname, schemaPath, (ChoiceSchemaNode) node);
        } else if (node instanceof ContainerSchemaNode) {
            return new ContainerSchemaNodeBuilder(moduleName, line, qname, schemaPath, (ContainerSchemaNode) node);
        } else if (node instanceof LeafSchemaNode) {
            return new LeafSchemaNodeBuilder(moduleName, line, qname, schemaPath, (LeafSchemaNode) node);
        } else if (node instanceof LeafListSchemaNode) {
            return new LeafListSchemaNodeBuilder(moduleName, line, qname, schemaPath, (LeafListSchemaNode) node);
        } else if (node instanceof ListSchemaNode) {
            return new ListSchemaNodeBuilder(moduleName, line, qname, schemaPath, (ListSchemaNode) node);
        } else if (node instanceof ChoiceCaseNode) {
            return new ChoiceCaseBuilder(moduleName, line, qname, schemaPath, (ChoiceCaseNode) node);
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
            result.add(new TypeDefinitionBuilderImpl(moduleName, line, qname, schemaPath, (ExtendedType) node));
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
        private final byte[] data;

        private ByteSourceImpl(final InputStream input) throws IOException {
            toString = input.toString();
            data = ByteStreams.toByteArray(input);
        }

        @Override
        public InputStream openStream() throws IOException {
            return new NamedByteArrayInputStream(data, toString);
        }
    }

    public static ModuleBuilder getModuleByPrefix(final ModuleBuilder module, final String prefix) {
        if (prefix == null || prefix.isEmpty() || prefix.equals(module.getPrefix())) {
            return module;
        } else {
            return module.getImportedModule(prefix);
        }
    }

    public static ModuleBuilder findModule(final QName qname, final Map<URI, NavigableMap<Date, ModuleBuilder>> modules) {
        NavigableMap<Date, ModuleBuilder> map = modules.get(qname.getNamespace());
        if (map == null) {
            return null;
        }
        if (qname.getRevision() == null) {
            return map.lastEntry().getValue();
        }

        Entry<Date, ModuleBuilder> lastEntry = map.lastEntry();
        if (qname.getRevision().compareTo(lastEntry.getKey()) > 0) {
            /*
             * We are trying to find more recent revision of module than is in
             * the map. Most probably the yang models are not referenced
             * correctly and the revision of a base module or submodule has not
             * been updated along with revision of a referenced module or
             * submodule. However, we should return the most recent entry in the
             * map, otherwise the null pointer exception occurs (see Bug3799).
             */
            LOG.warn(String
                    .format("Attempt to find more recent revision of module than is available. "
                            + "The requested revision is [%s], but the most recent available revision of module is [%s]."
                            + " Most probably some of Yang models do not have updated revision or they are not "
                            + "referenced correctly.",
                            qname.getRevision(), lastEntry.getKey()));
            return lastEntry.getValue();
        }

        return map.get(qname.getRevision());
    }

    public static Map<String, NavigableMap<Date, URI>> createYangNamespaceContext(
            final Collection<? extends ParseTree> modules, final Optional<SchemaContext> context) {
        Map<String, NavigableMap<Date, URI>> namespaceContext = new HashMap<>();
        Set<Submodule_stmtContext> submodules = new HashSet<>();
        // first read ParseTree collection and separate modules and submodules
        for (ParseTree module : modules) {
            for (int i = 0; i < module.getChildCount(); i++) {
                ParseTree moduleTree = module.getChild(i);
                if (moduleTree instanceof Submodule_stmtContext) {
                    // put submodule context to separate collection
                    submodules.add((Submodule_stmtContext) moduleTree);
                } else if (moduleTree instanceof Module_stmtContext) {
                    // get name, revision and namespace from module
                    Module_stmtContext moduleCtx = (Module_stmtContext) moduleTree;
                    final String moduleName = ParserListenerUtils.stringFromNode(moduleCtx);
                    Date rev = null;
                    URI namespace = null;
                    for (int j = 0; j < moduleCtx.getChildCount(); j++) {
                        ParseTree moduleCtxChildTree = moduleCtx.getChild(j);
                        if (moduleCtxChildTree instanceof Revision_stmtsContext) {
                            String revisionDateStr = YangModelDependencyInfo
                                    .getLatestRevision((Revision_stmtsContext) moduleCtxChildTree);
                            if (revisionDateStr == null) {
                                rev = new Date(0);
                            } else {
                                rev = QName.parseRevision(revisionDateStr);
                            }
                        }
                        if (moduleCtxChildTree instanceof Module_header_stmtsContext) {
                            Module_header_stmtsContext headerCtx = (Module_header_stmtsContext) moduleCtxChildTree;
                            for (int k = 0; k < headerCtx.getChildCount(); k++) {
                                ParseTree ctx = headerCtx.getChild(k);
                                if (ctx instanceof Namespace_stmtContext) {
                                    final String namespaceStr = ParserListenerUtils.stringFromNode(ctx);
                                    namespace = URI.create(namespaceStr);
                                    break;
                                }
                            }
                        }
                    }
                    // update namespaceContext
                    NavigableMap<Date, URI> revToNs = namespaceContext.get(moduleName);
                    if (revToNs == null) {
                        revToNs = new TreeMap<>();
                        revToNs.put(rev, namespace);
                        namespaceContext.put(moduleName, revToNs);
                    }
                    revToNs.put(rev, namespace);
                }
            }
        }
        // after all ParseTree-s are parsed update namespaceContext with modules
        // from SchemaContext
        if (context.isPresent()) {
            for (Module module : context.get().getModules()) {
                NavigableMap<Date, URI> revToNs = namespaceContext.get(module.getName());
                if (revToNs == null) {
                    revToNs = new TreeMap<>();
                    revToNs.put(module.getRevision(), module.getNamespace());
                    namespaceContext.put(module.getName(), revToNs);
                }
                revToNs.put(module.getRevision(), module.getNamespace());
            }
        }
        // when all modules are processed, traverse submodules and update
        // namespaceContext with mapping for submodules
        for (Submodule_stmtContext submodule : submodules) {
            final String moduleName = ParserListenerUtils.stringFromNode(submodule);
            for (int i = 0; i < submodule.getChildCount(); i++) {
                ParseTree subHeaderCtx = submodule.getChild(i);
                if (subHeaderCtx instanceof Submodule_header_stmtsContext) {
                    for (int j = 0; j < subHeaderCtx.getChildCount(); j++) {
                        ParseTree belongsCtx = subHeaderCtx.getChild(j);
                        if (belongsCtx instanceof Belongs_to_stmtContext) {
                            final String belongsTo = ParserListenerUtils.stringFromNode(belongsCtx);
                            NavigableMap<Date, URI> ns = namespaceContext.get(belongsTo);
                            if (ns == null) {
                                throw new YangParseException(moduleName, submodule.getStart().getLine(), String.format(
                                        "Unresolved belongs-to statement: %s", belongsTo));
                            }
                            // submodule get namespace and revision from module
                            NavigableMap<Date, URI> subNs = new TreeMap<>();
                            subNs.put(ns.firstKey(), ns.firstEntry().getValue());
                            namespaceContext.put(moduleName, subNs);
                        }
                    }
                }
            }
        }
        return namespaceContext;
    }

}
