/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.parser.builder.api.AbstractDataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeAwareBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.util.Comparators;
import org.opendaylight.yangtools.yang.parser.util.ModuleImportImpl;
import org.opendaylight.yangtools.yang.parser.util.RefineHolder;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Builder of Module object. If this module is dependent on external
 * module/modules, these dependencies must be resolved before module is built,
 * otherwise result may not be valid.
 */
public class ModuleBuilder extends AbstractDataNodeContainerBuilder {

    private final ModuleImpl instance;
    private final String name;
    private final String sourcePath;
    private final SchemaPath schemaPath;
    private URI namespace;
    private String prefix;
    private Date revision;

    private final boolean submodule;
    private String belongsTo;
    private ModuleBuilder parent;

    public ModuleBuilder getParent() {
        return parent;
    }

    public void setParent(ModuleBuilder parent) {
        this.parent = parent;
    }

    private final Deque<Builder> actualPath = new LinkedList<>();
    private final Set<TypeAwareBuilder> dirtyNodes = new HashSet<>();

    private final Set<ModuleImport> imports = new HashSet<ModuleImport>();

    private final Set<AugmentationSchema> augments = new HashSet<>();
    private final List<AugmentationSchemaBuilder> augmentBuilders = new ArrayList<>();
    private final List<AugmentationSchemaBuilder> allAugments = new ArrayList<>();

    private final List<GroupingBuilder> allGroupings = new ArrayList<>();

    private final List<UsesNodeBuilder> allUsesNodes = new ArrayList<>();

    private final Set<RpcDefinition> rpcs = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
    private final Set<RpcDefinitionBuilder> addedRpcs = new HashSet<>();

    private final Set<NotificationDefinition> notifications = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
    private final Set<NotificationBuilder> addedNotifications = new HashSet<>();

    private final Set<IdentitySchemaNode> identities = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
    private final Set<IdentitySchemaNodeBuilder> addedIdentities = new HashSet<>();

    private final Set<FeatureDefinition> features = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
    private final Set<FeatureBuilder> addedFeatures = new HashSet<>();

    private final Set<Deviation> deviations = new HashSet<>();
    private final Set<DeviationBuilder> deviationBuilders = new HashSet<>();

    private final List<ExtensionDefinition> extensions = new ArrayList<>();
    private final List<ExtensionBuilder> addedExtensions = new ArrayList<>();

    private final List<UnknownSchemaNodeBuilder> allUnknownNodes = new ArrayList<UnknownSchemaNodeBuilder>();

    private final List<ListSchemaNodeBuilder> allLists = new ArrayList<ListSchemaNodeBuilder>();

    private String source;

    public ModuleBuilder(final String name, final String sourcePath) {
        this(name, false, sourcePath);
    }

    public ModuleBuilder(final String name, final boolean submodule, final String sourcePath) {
        super(name, 0, null);
        this.name = name;
        this.sourcePath = sourcePath;
        schemaPath = new SchemaPath(Collections.<QName> emptyList(), true);
        this.submodule = submodule;
        instance = new ModuleImpl(name, sourcePath);
        actualPath.push(this);
    }

    public ModuleBuilder(Module base) {
        super(base.getName(), 0, null);
        this.name = base.getName();
        this.sourcePath = base.getModuleSourcePath();
        schemaPath = new SchemaPath(Collections.<QName> emptyList(), true);
        submodule = false;
        instance = new ModuleImpl(base.getName(), base.getModuleSourcePath());
        actualPath.push(this);
        namespace = base.getNamespace();
        prefix = base.getPrefix();
        revision = base.getRevision();

        for (DataSchemaNode childNode : base.getChildNodes()) {
            childNodes.add(childNode);
        }

        typedefs.addAll(base.getTypeDefinitions());
        groupings.addAll(base.getGroupings());
        usesNodes.addAll(base.getUses());
        augments.addAll(base.getAugmentations());
        rpcs.addAll(base.getRpcs());
        notifications.addAll(base.getNotifications());
        identities.addAll(base.getIdentities());
        features.addAll(base.getFeatures());
        deviations.addAll(base.getDeviations());
        extensions.addAll(base.getExtensionSchemaNodes());
        unknownNodes.addAll(base.getUnknownSchemaNodes());
    }

    /**
     * Build new Module object based on this builder.
     */
    @Override
    public Module build() {
        instance.setPrefix(prefix);
        instance.setRevision(revision);
        instance.setImports(imports);
        instance.setNamespace(namespace);

        // TYPEDEFS
        for (TypeDefinitionBuilder tdb : addedTypedefs) {
            typedefs.add(tdb.build());
        }
        instance.setTypeDefinitions(typedefs);

        // CHILD NODES
        for (DataSchemaNodeBuilder child : addedChildNodes) {
            childNodes.add(child.build());
        }
        instance.addChildNodes(childNodes);

        // GROUPINGS
        for (GroupingBuilder gb : addedGroupings) {
            groupings.add(gb.build());
        }
        instance.setGroupings(groupings);

        // USES
        for (UsesNodeBuilder unb : addedUsesNodes) {
            usesNodes.add(unb.build());
        }
        instance.setUses(usesNodes);

        // FEATURES
        for (FeatureBuilder fb : addedFeatures) {
            features.add(fb.build());
        }
        instance.setFeatures(features);

        // NOTIFICATIONS
        for (NotificationBuilder entry : addedNotifications) {
            notifications.add(entry.build());
        }
        instance.setNotifications(notifications);

        // AUGMENTATIONS
        for (AugmentationSchemaBuilder builder : augmentBuilders) {
            augments.add(builder.build());
        }
        instance.setAugmentations(augments);

        // RPCs
        for (RpcDefinitionBuilder rpc : addedRpcs) {
            rpcs.add(rpc.build());
        }
        instance.setRpcs(rpcs);

        // DEVIATIONS
        for (DeviationBuilder entry : deviationBuilders) {
            deviations.add(entry.build());
        }
        instance.setDeviations(deviations);

        // EXTENSIONS
        for (ExtensionBuilder eb : addedExtensions) {
            extensions.add(eb.build());
        }
        Collections.sort(extensions, Comparators.SCHEMA_NODE_COMP);
        instance.setExtensionSchemaNodes(extensions);

        // IDENTITIES
        for (IdentitySchemaNodeBuilder id : addedIdentities) {
            identities.add(id.build());
        }
        instance.setIdentities(identities);

        // UNKNOWN NODES
        for (UnknownSchemaNodeBuilder unb : addedUnknownNodes) {
            unknownNodes.add(unb.build());
        }
        Collections.sort(unknownNodes, Comparators.SCHEMA_NODE_COMP);
        instance.setUnknownSchemaNodes(unknownNodes);

        instance.setSource(source);

        return instance;
    }

    public String getModuleSourcePath() {
        return sourcePath;
    }

    @Override
    public void setParent(Builder parent) {
        throw new YangParseException(name, 0, "Can not set parent to module");
    }

    @Override
    public SchemaPath getPath() {
        return schemaPath;
    }

    @Override
    public Set<TypeDefinitionBuilder> getTypeDefinitionBuilders() {
        return addedTypedefs;
    }

    public void enterNode(final Builder node) {
        actualPath.push(node);
    }

    public void exitNode() {
        actualPath.pop();
    }

    public Builder getActualNode() {
        if (actualPath.isEmpty()) {
            return null;
        } else {
            return actualPath.peekFirst();
        }
    }

    public Builder getActualParent() {
        if (actualPath.size() < 2) {
            return null;
        } else {
            Builder builderChild = actualPath.removeFirst();
            Builder builderParent = actualPath.peekFirst();
            actualPath.addFirst(builderChild);
            return builderParent;
        }
    }

    public Set<TypeAwareBuilder> getDirtyNodes() {
        return dirtyNodes;
    }

    public Set<AugmentationSchema> getAugments() {
        return augments;
    }

    public List<AugmentationSchemaBuilder> getAugmentBuilders() {
        return augmentBuilders;
    }

    public List<AugmentationSchemaBuilder> getAllAugments() {
        return allAugments;
    }

    public Set<IdentitySchemaNode> getIdentities() {
        return identities;
    }

    public Set<IdentitySchemaNodeBuilder> getAddedIdentities() {
        return addedIdentities;
    }

    public Set<FeatureDefinition> getFeatures() {
        return features;
    }

    public Set<FeatureBuilder> getAddedFeatures() {
        return addedFeatures;
    }

    public List<GroupingBuilder> getAllGroupings() {
        return allGroupings;
    }

    public List<UsesNodeBuilder> getAllUsesNodes() {
        return allUsesNodes;
    }

    public Set<Deviation> getDeviations() {
        return deviations;
    }

    public Set<DeviationBuilder> getDeviationBuilders() {
        return deviationBuilders;
    }

    public List<ExtensionDefinition> getExtensions() {
        return extensions;
    }

    public List<ExtensionBuilder> getAddedExtensions() {
        return addedExtensions;
    }

    public List<UnknownSchemaNodeBuilder> getAllUnknownNodes() {
        return allUnknownNodes;
    }

    public List<ListSchemaNodeBuilder> getAllLists() {
        return allLists;
    }

    public String getName() {
        return name;
    }

    public URI getNamespace() {
        return namespace;
    }

    public void setNamespace(final URI namespace) {
        this.namespace = namespace;
    }

    public String getPrefix() {
        return prefix;
    }

    public Date getRevision() {
        return revision;
    }

    public boolean isSubmodule() {
        return submodule;
    }

    public String getBelongsTo() {
        return belongsTo;
    }

    public void setBelongsTo(String belongsTo) {
        this.belongsTo = belongsTo;
    }

    public void markActualNodeDirty() {
        final TypeAwareBuilder nodeBuilder = (TypeAwareBuilder) getActualNode();
        dirtyNodes.add(nodeBuilder);
    }

    public void setRevision(final Date revision) {
        this.revision = revision;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public void setYangVersion(final String yangVersion) {
        instance.setYangVersion(yangVersion);
    }

    public void setDescription(final String description) {
        instance.setDescription(description);
    }

    public void setReference(final String reference) {
        instance.setReference(reference);
    }

    public void setOrganization(final String organization) {
        instance.setOrganization(organization);
    }

    public void setContact(final String contact) {
        instance.setContact(contact);
    }

    public boolean addModuleImport(final String moduleName, final Date revision, final String prefix) {
        final ModuleImport moduleImport = createModuleImport(moduleName, revision, prefix);
        return imports.add(moduleImport);
    }

    public Set<ModuleImport> getModuleImports() {
        return imports;
    }

    public ExtensionBuilder addExtension(final QName qname, final int line, final SchemaPath path) {
        Builder parent = getActualNode();
        if (!(parent.equals(this))) {
            throw new YangParseException(name, line, "extension can be defined only in module or submodule");
        }

        final String extName = qname.getLocalName();
        for (ExtensionBuilder addedExtension : addedExtensions) {
            if (addedExtension.getQName().getLocalName().equals(extName)) {
                raiseYangParserException("extension", "node", extName, line, addedExtension.getLine());
            }
        }
        final ExtensionBuilder builder = new ExtensionBuilder(name, line, qname, path);
        builder.setParent(parent);
        addedExtensions.add(builder);
        return builder;
    }

    public ContainerSchemaNodeBuilder addContainerNode(final int line, final QName qname, final SchemaPath schemaPath) {
        final ContainerSchemaNodeBuilder builder = new ContainerSchemaNodeBuilder(name, line, qname, schemaPath);

        Builder parent = getActualNode();
        builder.setParent(parent);
        addChildToParent(parent, builder, qname.getLocalName());

        return builder;
    }

    public ListSchemaNodeBuilder addListNode(final int line, final QName qname, final SchemaPath schemaPath) {
        final ListSchemaNodeBuilder builder = new ListSchemaNodeBuilder(name, line, qname, schemaPath);

        Builder parent = getActualNode();
        builder.setParent(parent);
        addChildToParent(parent, builder, qname.getLocalName());
        allLists.add(builder);

        return builder;
    }

    public LeafSchemaNodeBuilder addLeafNode(final int line, final QName qname, final SchemaPath schemaPath) {
        final LeafSchemaNodeBuilder builder = new LeafSchemaNodeBuilder(name, line, qname, schemaPath);

        Builder parent = getActualNode();
        builder.setParent(parent);
        addChildToParent(parent, builder, qname.getLocalName());

        return builder;
    }

    public LeafListSchemaNodeBuilder addLeafListNode(final int line, final QName qname, final SchemaPath schemaPath) {
        final LeafListSchemaNodeBuilder builder = new LeafListSchemaNodeBuilder(name, line, qname, schemaPath);

        Builder parent = getActualNode();
        builder.setParent(parent);
        addChildToParent(parent, builder, qname.getLocalName());

        return builder;
    }

    public GroupingBuilder addGrouping(final int line, final QName qname, final SchemaPath path) {
        final GroupingBuilder builder = new GroupingBuilderImpl(name, line, qname, path);

        Builder parent = getActualNode();
        builder.setParent(parent);

        String groupingName = qname.getLocalName();
        if (parent.equals(this)) {
            for (GroupingBuilder addedGrouping : addedGroupings) {
                if (addedGrouping.getQName().getLocalName().equals(groupingName)) {
                    raiseYangParserException("", "Grouping", groupingName, line, addedGrouping.getLine());
                }
            }
            addedGroupings.add(builder);
        } else {
            if (parent instanceof DataNodeContainerBuilder) {
                DataNodeContainerBuilder parentNode = (DataNodeContainerBuilder) parent;
                for (GroupingBuilder addedGrouping : parentNode.getGroupingBuilders()) {
                    if (addedGrouping.getQName().getLocalName().equals(groupingName)) {
                        raiseYangParserException("", "Grouping", groupingName, line, addedGrouping.getLine());
                    }
                }
                parentNode.addGrouping(builder);
            } else if (parent instanceof RpcDefinitionBuilder) {
                RpcDefinitionBuilder parentNode = (RpcDefinitionBuilder) parent;
                for (GroupingBuilder child : parentNode.getGroupings()) {
                    if (child.getQName().getLocalName().equals(groupingName)) {
                        raiseYangParserException("", "Grouping", groupingName, line, child.getLine());
                    }
                }
                parentNode.addGrouping(builder);
            } else {
                throw new YangParseException(name, line, "Unresolved parent of grouping " + groupingName);
            }
        }

        allGroupings.add(builder);
        return builder;
    }

    public AugmentationSchemaBuilder addAugment(final int line, final String augmentTargetStr) {
        final AugmentationSchemaBuilder builder = new AugmentationSchemaBuilderImpl(name, line, augmentTargetStr);

        Builder parent = getActualNode();
        builder.setParent(parent);

        if (parent.equals(this)) {
            // augment can be declared only under 'module' ...
            if (!(augmentTargetStr.startsWith("/"))) {
                throw new YangParseException(
                        name,
                        line,
                        "If the 'augment' statement is on the top level in a module, the absolute form of a schema node identifier MUST be used.");
            }
            augmentBuilders.add(builder);
        } else {
            // ... or 'uses' statement
            if (parent instanceof UsesNodeBuilder) {
                if (augmentTargetStr.startsWith("/")) {
                    throw new YangParseException(name, line,
                            "If 'augment' statement is a substatement to the 'uses' statement, it cannot contain absolute path ("
                                    + augmentTargetStr + ")");
                }
                ((UsesNodeBuilder) parent).addAugment(builder);
            } else {
                throw new YangParseException(name, line, "Augment can be declared only under module or uses statement.");
            }
        }
        allAugments.add(builder);

        return builder;
    }

    @Override
    public void addUsesNode(UsesNodeBuilder usesBuilder) {
        addedUsesNodes.add(usesBuilder);
        allUsesNodes.add(usesBuilder);
    }

    public UsesNodeBuilder addUsesNode(final int line, final String groupingPathStr) {
        final UsesNodeBuilder usesBuilder = new UsesNodeBuilderImpl(name, line, groupingPathStr);

        Builder parent = getActualNode();
        usesBuilder.setParent(parent);

        if (parent.equals(this)) {
            addedUsesNodes.add(usesBuilder);
        } else {
            if (!(parent instanceof DataNodeContainerBuilder)) {
                throw new YangParseException(name, line, "Unresolved parent of uses '" + groupingPathStr + "'.");
            }
            ((DataNodeContainerBuilder) parent).addUsesNode(usesBuilder);
        }
        if (parent instanceof AugmentationSchemaBuilder) {
            usesBuilder.setAugmenting(true);
        }

        allUsesNodes.add(usesBuilder);
        return usesBuilder;
    }

    public void addRefine(final RefineHolder refine) {
        final Builder parent = getActualNode();
        if (!(parent instanceof UsesNodeBuilder)) {
            throw new YangParseException(name, refine.getLine(), "refine can be defined only in uses statement");
        }
        ((UsesNodeBuilder) parent).addRefine(refine);
        refine.setParent(parent);
    }

    public RpcDefinitionBuilder addRpc(final int line, final QName qname, final SchemaPath path) {
        Builder parent = getActualNode();
        if (!(parent.equals(this))) {
            throw new YangParseException(name, line, "rpc can be defined only in module or submodule");
        }

        final RpcDefinitionBuilder rpcBuilder = new RpcDefinitionBuilder(name, line, qname, path);
        rpcBuilder.setParent(parent);

        String rpcName = qname.getLocalName();
        for (RpcDefinitionBuilder rpc : addedRpcs) {
            if (rpc.getQName().getLocalName().equals(rpcName)) {
                raiseYangParserException("", "rpc", rpcName, line, rpc.getLine());
            }
        }
        for (DataSchemaNodeBuilder addedChild : addedChildNodes) {
            if (addedChild.getQName().getLocalName().equals(rpcName)) {
                raiseYangParserException("rpc", "node", rpcName, line, addedChild.getLine());
            }
        }
        for (NotificationBuilder addedNotification : addedNotifications) {
            if (addedNotification.getQName().getLocalName().equals(rpcName)) {
                raiseYangParserException("rpc", "notification", rpcName, line, addedNotification.getLine());
            }
        }
        addedRpcs.add(rpcBuilder);
        return rpcBuilder;
    }

    public ContainerSchemaNodeBuilder addRpcInput(final int line, final QName qname, final SchemaPath schemaPath) {
        final Builder parent = getActualNode();
        if (!(parent instanceof RpcDefinitionBuilder)) {
            throw new YangParseException(name, line, "input can be defined only in rpc statement");
        }
        final RpcDefinitionBuilder rpc = (RpcDefinitionBuilder) parent;

        final ContainerSchemaNodeBuilder inputBuilder = new ContainerSchemaNodeBuilder(name, line, qname, schemaPath);
        inputBuilder.setParent(rpc);

        rpc.setInput(inputBuilder);
        return inputBuilder;
    }

    public ContainerSchemaNodeBuilder addRpcOutput(final SchemaPath schemaPath, final QName qname, final int line) {
        final Builder parent = getActualNode();
        if (!(parent instanceof RpcDefinitionBuilder)) {
            throw new YangParseException(name, line, "output can be defined only in rpc statement");
        }
        final RpcDefinitionBuilder rpc = (RpcDefinitionBuilder) parent;

        final ContainerSchemaNodeBuilder outputBuilder = new ContainerSchemaNodeBuilder(name, line, qname, schemaPath);
        outputBuilder.setParent(rpc);

        rpc.setOutput(outputBuilder);
        return outputBuilder;
    }

    public void addNotification(NotificationDefinition notification) {
        notifications.add(notification);
    }

    public NotificationBuilder addNotification(final int line, final QName qname, final SchemaPath path) {
        final Builder parent = getActualNode();
        if (!(parent.equals(this))) {
            throw new YangParseException(name, line, "notification can be defined only in module or submodule");
        }

        String notificationName = qname.getLocalName();
        for (NotificationBuilder nb : addedNotifications) {
            if (nb.getQName().equals(qname)) {
                raiseYangParserException("", "notification", notificationName, line, nb.getLine());
            }
        }
        for (RpcDefinitionBuilder rpc : addedRpcs) {
            if (rpc.getQName().getLocalName().equals(notificationName)) {
                raiseYangParserException("notification", "rpc", notificationName, line, rpc.getLine());
            }
        }
        for (DataSchemaNodeBuilder addedChild : addedChildNodes) {
            if (addedChild.getQName().getLocalName().equals(notificationName)) {
                raiseYangParserException("notification", "node", notificationName, line, addedChild.getLine());
            }
        }

        final NotificationBuilder builder = new NotificationBuilder(name, line, qname, path);
        builder.setParent(parent);
        addedNotifications.add(builder);

        return builder;
    }

    public FeatureBuilder addFeature(final int line, final QName qname, final SchemaPath path) {
        Builder parent = getActualNode();
        if (!(parent.equals(this))) {
            throw new YangParseException(name, line, "feature can be defined only in module or submodule");
        }

        final FeatureBuilder builder = new FeatureBuilder(name, line, qname, path);
        builder.setParent(parent);

        String featureName = qname.getLocalName();
        for (FeatureBuilder addedFeature : addedFeatures) {
            if (addedFeature.getQName().getLocalName().equals(featureName)) {
                raiseYangParserException("", "feature", featureName, line, addedFeature.getLine());
            }
        }
        addedFeatures.add(builder);
        return builder;
    }

    public ChoiceBuilder addChoice(final int line, final QName qname, final SchemaPath path) {
        final ChoiceBuilder builder = new ChoiceBuilder(name, line, qname, path);

        Builder parent = getActualNode();
        builder.setParent(parent);
        addChildToParent(parent, builder, qname.getLocalName());

        return builder;
    }

    public ChoiceCaseBuilder addCase(final int line, final QName qname, final SchemaPath path) {
        Builder parent = getActualNode();
        if (parent == null || parent.equals(this)) {
            throw new YangParseException(name, line, "'case' parent not found");
        }

        final ChoiceCaseBuilder builder = new ChoiceCaseBuilder(name, line, qname, path);
        builder.setParent(parent);

        if (parent instanceof ChoiceBuilder) {
            ((ChoiceBuilder) parent).addCase(builder);
        } else if (parent instanceof AugmentationSchemaBuilder) {
            ((AugmentationSchemaBuilder) parent).addChildNode(builder);
        } else {
            throw new YangParseException(name, line, "Unresolved parent of 'case' " + qname.getLocalName());
        }

        return builder;
    }

    public AnyXmlBuilder addAnyXml(final int line, final QName qname, final SchemaPath schemaPath) {
        final AnyXmlBuilder builder = new AnyXmlBuilder(name, line, qname, schemaPath);

        Builder parent = getActualNode();
        builder.setParent(parent);
        addChildToParent(parent, builder, qname.getLocalName());

        return builder;
    }

    @Override
    public void addTypedef(TypeDefinitionBuilder typedefBuilder) {
        String nodeName = typedefBuilder.getQName().getLocalName();
        for (TypeDefinitionBuilder tdb : addedTypedefs) {
            if (tdb.getQName().getLocalName().equals(nodeName)) {
                raiseYangParserException("", "typedef", nodeName, typedefBuilder.getLine(), tdb.getLine());
            }
        }
        addedTypedefs.add(typedefBuilder);
    }

    public TypeDefinitionBuilderImpl addTypedef(final int line, final QName qname, final SchemaPath path) {
        final TypeDefinitionBuilderImpl builder = new TypeDefinitionBuilderImpl(name, line, qname, path);

        Builder parent = getActualNode();
        builder.setParent(parent);

        String typedefName = qname.getLocalName();
        if (parent.equals(this)) {
            for (TypeDefinitionBuilder tdb : addedTypedefs) {
                if (tdb.getQName().getLocalName().equals(typedefName)) {
                    raiseYangParserException("", "typedef", typedefName, line, tdb.getLine());
                }
            }
            addedTypedefs.add(builder);
        } else {
            if (parent instanceof DataNodeContainerBuilder) {
                DataNodeContainerBuilder parentNode = (DataNodeContainerBuilder) parent;
                for (TypeDefinitionBuilder child : parentNode.getTypeDefinitionBuilders()) {
                    if (child.getQName().getLocalName().equals(typedefName)) {
                        raiseYangParserException("", "typedef", typedefName, line, child.getLine());
                    }
                }
                parentNode.addTypedef(builder);
            } else if (parent instanceof RpcDefinitionBuilder) {
                RpcDefinitionBuilder rpcParent = (RpcDefinitionBuilder) parent;
                for (TypeDefinitionBuilder tdb : rpcParent.getTypeDefinitions()) {
                    if (tdb.getQName().getLocalName().equals(builder.getQName().getLocalName())) {
                        raiseYangParserException("", "typedef", typedefName, line, tdb.getLine());
                    }
                }
                rpcParent.addTypedef(builder);
            } else {
                throw new YangParseException(name, line, "Unresolved parent of typedef " + typedefName);
            }
        }

        return builder;
    }

    public void setType(final TypeDefinition<?> type) {
        Builder parent = getActualNode();
        if (!(parent instanceof TypeAwareBuilder)) {
            throw new YangParseException("Failed to set type '" + type.getQName().getLocalName()
                    + "'. Invalid parent node: " + parent);
        }
        ((TypeAwareBuilder) parent).setType(type);
    }

    public UnionTypeBuilder addUnionType(final int line, final URI namespace, final Date revision) {
        final Builder parent = getActualNode();
        if (parent == null) {
            throw new YangParseException(name, line, "Unresolved parent of union type");
        } else {
            final UnionTypeBuilder union = new UnionTypeBuilder(name, line);
            if (parent instanceof TypeAwareBuilder) {
                ((TypeAwareBuilder) parent).setTypedef(union);
                return union;
            } else {
                throw new YangParseException(name, line, "Invalid parent of union type.");
            }
        }
    }

    public void addIdentityrefType(final int line, final SchemaPath schemaPath, final String baseString) {
        final IdentityrefTypeBuilder identityref = new IdentityrefTypeBuilder(name, line, baseString, schemaPath);

        final Builder parent = getActualNode();
        if (parent == null) {
            throw new YangParseException(name, line, "Unresolved parent of identityref type.");
        } else {
            if (parent instanceof TypeAwareBuilder) {
                final TypeAwareBuilder typeParent = (TypeAwareBuilder) parent;
                typeParent.setTypedef(identityref);
                dirtyNodes.add(typeParent);
            } else {
                throw new YangParseException(name, line, "Invalid parent of identityref type.");
            }
        }
    }

    public DeviationBuilder addDeviation(final int line, final String targetPath) {
        Builder parent = getActualNode();
        if (!(parent.equals(this))) {
            throw new YangParseException(name, line, "deviation can be defined only in module or submodule");
        }

        final DeviationBuilder builder = new DeviationBuilder(name, line, targetPath);
        builder.setParent(parent);
        deviationBuilders.add(builder);
        return builder;
    }

    public IdentitySchemaNodeBuilder addIdentity(final QName qname, final int line, final SchemaPath path) {
        Builder parent = getActualNode();
        if (!(parent.equals(this))) {
            throw new YangParseException(name, line, "identity can be defined only in module or submodule");
        }
        String identityName = qname.getLocalName();
        for (IdentitySchemaNodeBuilder idBuilder : addedIdentities) {
            if (idBuilder.getQName().equals(qname)) {
                raiseYangParserException("", "identity", identityName, line, idBuilder.getLine());
            }
        }

        final IdentitySchemaNodeBuilder builder = new IdentitySchemaNodeBuilder(name, line, qname, path);
        builder.setParent(parent);
        addedIdentities.add(builder);
        return builder;
    }

    @Override
    public void addUnknownNodeBuilder(final UnknownSchemaNodeBuilder builder) {
        addedUnknownNodes.add(builder);
        allUnknownNodes.add(builder);
    }

    public UnknownSchemaNodeBuilder addUnknownSchemaNode(final int line, final QName qname, final SchemaPath path) {
        final Builder parent = getActualNode();
        final UnknownSchemaNodeBuilder builder = new UnknownSchemaNodeBuilder(name, line, qname, path);
        builder.setParent(parent);
        allUnknownNodes.add(builder);

        if (parent.equals(this)) {
            addedUnknownNodes.add(builder);
        } else {
            if (parent instanceof SchemaNodeBuilder) {
                ((SchemaNodeBuilder) parent).addUnknownNodeBuilder(builder);
            } else if (parent instanceof DataNodeContainerBuilder) {
                ((DataNodeContainerBuilder) parent).addUnknownNodeBuilder(builder);
            } else if (parent instanceof RefineHolder) {
                ((RefineHolder) parent).addUnknownNodeBuilder(builder);
            } else {
                throw new YangParseException(name, line, "Unresolved parent of unknown node '" + qname.getLocalName()
                        + "'");
            }
        }

        return builder;
    }

    public Set<RpcDefinition> getRpcs() {
        return rpcs;
    }

    public Set<RpcDefinitionBuilder> getAddedRpcs() {
        return addedRpcs;
    }

    public Set<NotificationDefinition> getNotifications() {
        return notifications;
    }

    public Set<NotificationBuilder> getAddedNotifications() {
        return addedNotifications;
    }

    @Override
    public String toString() {
        return "module " + name;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public static final class ModuleImpl implements Module {
        private URI namespace;
        private final String name;
        private final String sourcePath;
        private Date revision;
        private String prefix;
        private String yangVersion;
        private String description;
        private String reference;
        private String organization;
        private String contact;
        private final Set<ModuleImport> imports = new HashSet<>();
        private final Set<FeatureDefinition> features = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
        private final Set<TypeDefinition<?>> typeDefinitions = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
        private final Set<NotificationDefinition> notifications = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
        private final Set<AugmentationSchema> augmentations = new HashSet<>();
        private final Set<RpcDefinition> rpcs = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
        private final Set<Deviation> deviations = new HashSet<>();
        private final Set<DataSchemaNode> childNodes = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
        private final Set<GroupingDefinition> groupings = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
        private final Set<UsesNode> uses = new HashSet<>();
        private final List<ExtensionDefinition> extensionNodes = new ArrayList<>();
        private final Set<IdentitySchemaNode> identities = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
        private final List<UnknownSchemaNode> unknownNodes = new ArrayList<>();
        private String source;

        private ModuleImpl(String name, String sourcePath) {
            this.name = name;
            this.sourcePath = sourcePath;
        }

        @Override
        public String getModuleSourcePath() {
            return sourcePath;
        }

        @Override
        public URI getNamespace() {
            return namespace;
        }

        private void setNamespace(URI namespace) {
            this.namespace = namespace;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Date getRevision() {
            return revision;
        }

        private void setRevision(Date revision) {
            this.revision = revision;
        }

        @Override
        public String getPrefix() {
            return prefix;
        }

        private void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public String getYangVersion() {
            return yangVersion;
        }

        private void setYangVersion(String yangVersion) {
            this.yangVersion = yangVersion;
        }

        @Override
        public String getDescription() {
            return description;
        }

        private void setDescription(String description) {
            this.description = description;
        }

        @Override
        public String getReference() {
            return reference;
        }

        private void setReference(String reference) {
            this.reference = reference;
        }

        @Override
        public String getOrganization() {
            return organization;
        }

        private void setOrganization(String organization) {
            this.organization = organization;
        }

        @Override
        public String getContact() {
            return contact;
        }

        private void setContact(String contact) {
            this.contact = contact;
        }

        @Override
        public Set<ModuleImport> getImports() {
            return imports;
        }

        private void setImports(Set<ModuleImport> imports) {
            if (imports != null) {
                this.imports.addAll(imports);
            }
        }

        @Override
        public Set<FeatureDefinition> getFeatures() {
            return features;
        }

        private void setFeatures(Set<FeatureDefinition> features) {
            if (features != null) {
                this.features.addAll(features);
            }
        }

        @Override
        public Set<TypeDefinition<?>> getTypeDefinitions() {
            return typeDefinitions;
        }

        private void setTypeDefinitions(Set<TypeDefinition<?>> typeDefinitions) {
            if (typeDefinitions != null) {
                this.typeDefinitions.addAll(typeDefinitions);
            }
        }

        @Override
        public Set<NotificationDefinition> getNotifications() {
            return notifications;
        }

        private void setNotifications(Set<NotificationDefinition> notifications) {
            if (notifications != null) {
                this.notifications.addAll(notifications);
            }
        }

        @Override
        public Set<AugmentationSchema> getAugmentations() {
            return augmentations;
        }

        private void setAugmentations(Set<AugmentationSchema> augmentations) {
            if (augmentations != null) {
                this.augmentations.addAll(augmentations);
            }
        }

        @Override
        public Set<RpcDefinition> getRpcs() {
            return rpcs;
        }

        private void setRpcs(Set<RpcDefinition> rpcs) {
            if (rpcs != null) {
                this.rpcs.addAll(rpcs);
            }
        }

        @Override
        public Set<Deviation> getDeviations() {
            return deviations;
        }

        private void setDeviations(Set<Deviation> deviations) {
            if (deviations != null) {
                this.deviations.addAll(deviations);
            }
        }

        @Override
        public Set<DataSchemaNode> getChildNodes() {
            return Collections.unmodifiableSet(childNodes);
        }

        private void addChildNodes(Set<DataSchemaNode> childNodes) {
            if (childNodes != null) {
                this.childNodes.addAll(childNodes);
            }
        }

        @Override
        public Set<GroupingDefinition> getGroupings() {
            return groupings;
        }

        private void setGroupings(Set<GroupingDefinition> groupings) {
            if (groupings != null) {
                this.groupings.addAll(groupings);
            }
        }

        @Override
        public Set<UsesNode> getUses() {
            return uses;
        }

        private void setUses(Set<UsesNode> uses) {
            if (uses != null) {
                this.uses.addAll(uses);
            }
        }

        @Override
        public List<ExtensionDefinition> getExtensionSchemaNodes() {
            Collections.sort(extensionNodes, Comparators.SCHEMA_NODE_COMP);
            return extensionNodes;
        }

        private void setExtensionSchemaNodes(final List<ExtensionDefinition> extensionNodes) {
            if (extensionNodes != null) {
                this.extensionNodes.addAll(extensionNodes);
            }
        }

        @Override
        public Set<IdentitySchemaNode> getIdentities() {
            return identities;
        }

        private void setIdentities(final Set<IdentitySchemaNode> identities) {
            if (identities != null) {
                this.identities.addAll(identities);
            }
        }

        @Override
        public List<UnknownSchemaNode> getUnknownSchemaNodes() {
            return unknownNodes;
        }

        private void setUnknownSchemaNodes(final List<UnknownSchemaNode> unknownNodes) {
            if (unknownNodes != null) {
                this.unknownNodes.addAll(unknownNodes);
            }
        }

        @Override
        public DataSchemaNode getDataChildByName(QName name) {
            return getChildNode(childNodes, name);
        }

        @Override
        public DataSchemaNode getDataChildByName(String name) {
            return getChildNode(childNodes, name);
        }

        void setSource(String source){
            this.source = source;
        }

        public String getSource() {
            return source;
        }

        // FIXME: prefix should not be taken into consideration, perhaps namespace too
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((revision == null) ? 0 : revision.hashCode());
            result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
            result = prime * result + ((yangVersion == null) ? 0 : yangVersion.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ModuleImpl other = (ModuleImpl) obj;
            if (namespace == null) {
                if (other.namespace != null) {
                    return false;
                }
            } else if (!namespace.equals(other.namespace)) {
                return false;
            }
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            if (revision == null) {
                if (other.revision != null) {
                    return false;
                }
            } else if (!revision.equals(other.revision)) {
                return false;
            }
            if (prefix == null) {
                if (other.prefix != null) {
                    return false;
                }
            } else if (!prefix.equals(other.prefix)) {
                return false;
            }
            if (yangVersion == null) {
                if (other.yangVersion != null) {
                    return false;
                }
            } else if (!yangVersion.equals(other.yangVersion)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(ModuleImpl.class.getSimpleName());
            sb.append("[");
            sb.append("name=" + name);
            sb.append(", namespace=" + namespace);
            sb.append(", revision=" + revision);
            sb.append(", prefix=" + prefix);
            sb.append(", yangVersion=" + yangVersion);
            sb.append("]");
            return sb.toString();
        }
    }

    /**
     * Add child to parent. Method checks for duplicates and add given child
     * node to parent. If node with same name is found, throws exception. If
     * parent is null, child node will be added directly to module.
     *
     * @param parent
     * @param child
     * @param childName
     */
    private void addChildToParent(final Builder parent, final DataSchemaNodeBuilder child, final String childName) {
        final int lineNum = child.getLine();
        if (parent.equals(this)) {
            addChildToModule(child, childName, lineNum);
        } else {
            addChildToSubnodeOfModule(parent, child, childName, lineNum);
        }
    }

    /**
     * Adds child node <code>child</code> to the set of nodes child nodes.
     *
     * The method reduces the complexity of the method
     * {@link #addChildToParent(Builder, DataSchemaNodeBuilder, String)
     * addChildToParent}.
     *
     * @param child
     *            data schema node builder for child node
     * @param childName
     *            string with name of child node
     * @param lineNum
     *            line number in YANG file where is the node with the name equal
     *            to <code>childName</code> is defined
     */
    private void addChildToModule(final DataSchemaNodeBuilder child, final String childName, final int lineNum) {
        // if parent == null => node is defined under module
        // All leafs, leaf-lists, lists, containers, choices, rpcs,
        // notifications, and anyxmls defined within a parent node or at the
        // top level of the module or its submodules share the same
        // identifier namespace.
        for (DataSchemaNodeBuilder childNode : addedChildNodes) {
            if (childNode.getQName().getLocalName().equals(childName)) {
                raiseYangParserException("'" + child + "'", "node", childName, lineNum, childNode.getLine());
            }
        }
        for (RpcDefinitionBuilder rpc : addedRpcs) {
            if (rpc.getQName().getLocalName().equals(childName)) {
                raiseYangParserException("'" + child + "'", "rpc", childName, lineNum, rpc.getLine());
            }
        }
        for (NotificationBuilder notification : addedNotifications) {
            if (notification.getQName().getLocalName().equals(childName)) {
                raiseYangParserException("'" + child + "'", "notification", childName, lineNum, notification.getLine());
            }
        }
        addedChildNodes.add(child);
    }

    /**
     * Adds child node <code>child</code> to the group of child nodes of the
     * <code>parent</code>
     *
     * The method reduces the complexity of the method
     * {@link #addChildToParent(Builder, DataSchemaNodeBuilder, String)
     * addChildToParent}. *
     *
     * @param parent
     *            builder of node which is parent for <code>child</code>
     * @param child
     *            data schema node builder for child node
     * @param childName
     *            string with name of child node
     * @param lineNum
     *            line number in YANG file where is the node with the name equal
     *            to <code>childName</code> is defined
     */
    private void addChildToSubnodeOfModule(final Builder parent, final DataSchemaNodeBuilder child,
            final String childName, final int lineNum) {
        // no need for checking rpc and notification because they can be
        // defined only under module or submodule
        if (parent instanceof DataNodeContainerBuilder) {
            DataNodeContainerBuilder parentNode = (DataNodeContainerBuilder) parent;
            for (DataSchemaNodeBuilder childNode : parentNode.getChildNodeBuilders()) {
                if (childNode.getQName().getLocalName().equals(childName)) {
                    raiseYangParserException("'" + child + "'", "node", childName, lineNum, childNode.getLine());
                }
            }
            parentNode.addChildNode(child);
        } else if (parent instanceof ChoiceBuilder) {
            ChoiceBuilder parentNode = (ChoiceBuilder) parent;
            for (ChoiceCaseBuilder caseBuilder : parentNode.getCases()) {
                if (caseBuilder.getQName().getLocalName().equals(childName)) {
                    raiseYangParserException("'" + child + "'", "node", childName, lineNum, caseBuilder.getLine());
                }
            }
            parentNode.addCase(child);
        } else {
            throw new YangParseException(name, lineNum, "Unresolved parent of node '" + childName + "'.");
        }
    }

    private ModuleImport createModuleImport(final String moduleName, final Date revision, final String prefix) {
        final ModuleImport moduleImport = new ModuleImportImpl(moduleName, revision, prefix);
        return moduleImport;
    }

    private void raiseYangParserException(final String cantAddType, final String type, final String name,
            final int currentLine, final int duplicateLine) {

        StringBuilder msgPrefix = new StringBuilder("");
        if (cantAddType != null && !cantAddType.isEmpty()) {
            msgPrefix.append("Can not add ");
            msgPrefix.append(cantAddType);
            msgPrefix.append(": ");
        }

        String msg = String.format("%s%s with same name '%s' already declared at line %d.", msgPrefix, type, name,
                duplicateLine);
        throw new YangParseException(moduleName, currentLine, msg);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
        result = prime * result + ((revision == null) ? 0 : revision.hashCode());
        result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ModuleBuilder other = (ModuleBuilder) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (namespace == null) {
            if (other.namespace != null) {
                return false;
            }
        } else if (!namespace.equals(other.namespace)) {
            return false;
        }
        if (prefix == null) {
            if (other.prefix != null) {
                return false;
            }
        } else if (!prefix.equals(other.prefix)) {
            return false;
        }
        if (revision == null) {
            if (other.revision != null) {
                return false;
            }
        } else if (!revision.equals(other.revision)) {
            return false;
        }
        return true;
    }


}
