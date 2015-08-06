/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.builder.impl;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteSource;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.util.ModuleImportImpl;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DocumentedNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.ExtensionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeAwareBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractDocumentedDataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.Comparators;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

/**
 * Builder of Module object. If this module is dependent on external
 * module/modules, these dependencies must be resolved before module is built,
 * otherwise result may not be valid.
 */
public class ModuleBuilder extends AbstractDocumentedDataNodeContainerBuilder implements DocumentedNodeBuilder {
    private static final QNameModule EMPTY_QNAME_MODULE = QNameModule.cachedReference(QNameModule.create(null, null));
    private static final String GROUPING_STR = "Grouping";
    private static final String TYPEDEF_STR = "typedef";
    private ModuleImpl instance;
    private final String name;
    private final String sourcePath;
    private static final SchemaPath SCHEMA_PATH = SchemaPath.create(Collections.<QName> emptyList(), true);
    private String prefix;
    private QNameModule qnameModule = EMPTY_QNAME_MODULE;

    private final boolean submodule;
    private String belongsTo;
    private ModuleBuilder parent;

    private final Deque<Builder> actualPath = new LinkedList<>();
    private final Set<TypeAwareBuilder> dirtyNodes = new HashSet<>();

    final Map<String, ModuleImport> imports = new HashMap<>();
    final Map<String, ModuleBuilder> importedModules = new HashMap<>();

    final Set<ModuleBuilder> addedSubmodules = new HashSet<>();
    final Set<Module> submodules = new HashSet<>();
    final Map<String, Date> includedModules = new HashMap<>();

    private final Set<AugmentationSchema> augments = new LinkedHashSet<>();
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

    private final List<UnknownSchemaNodeBuilder> allUnknownNodes = new ArrayList<>();

    private final List<ListSchemaNodeBuilder> allLists = new ArrayList<>();

    private String source;
    private String yangVersion;
    private String organization;
    private String contact;

    public ModuleBuilder(final String name, final String sourcePath) {
        this(name, false, sourcePath);
    }

    public ModuleBuilder(final String name, final boolean submodule, final String sourcePath) {
        super(name, 0, null);
        this.name = name;
        this.sourcePath = sourcePath;
        this.submodule = submodule;
        actualPath.push(this);//FIXME: this escapes constructor
    }

    public ModuleBuilder(final Module base) {
        super(base.getName(), 0, QName.create(base.getQNameModule(), base.getName()),
                SCHEMA_PATH, base);
        this.name = base.getName();
        this.sourcePath = base.getModuleSourcePath();

        submodule = false;
        yangVersion = base.getYangVersion();
        actualPath.push(this);//FIXME: this escapes constructor
        prefix = base.getPrefix();
        qnameModule = base.getQNameModule();

        augments.addAll(base.getAugmentations());
        rpcs.addAll(base.getRpcs());
        notifications.addAll(base.getNotifications());

        for (IdentitySchemaNode identityNode : base.getIdentities()) {
            addedIdentities.add(new IdentitySchemaNodeBuilder(name, identityNode));
        }

        features.addAll(base.getFeatures());
        deviations.addAll(base.getDeviations());
        extensions.addAll(base.getExtensionSchemaNodes());
        unknownNodes.addAll(base.getUnknownSchemaNodes());
        source = base.getSource();
    }

    @Override
    protected String getStatementName() {
        return "module";
    }

    /**
     * Build new Module object based on this builder.
     */
    @Override
    public Module build() {
        if(instance != null) {
            return instance;
        }

        buildChildren();

        // SUBMODULES
        for (ModuleBuilder submodule : addedSubmodules) {
            submodules.add(submodule.build());
        }

        // FEATURES
        for (FeatureBuilder fb : addedFeatures) {
            features.add(fb.build());
        }

        // NOTIFICATIONS
        for (NotificationBuilder entry : addedNotifications) {
            notifications.add(entry.build());
        }

        // AUGMENTATIONS
        for (AugmentationSchemaBuilder builder : augmentBuilders) {
            augments.add(builder.build());
        }

        // RPCs
        for (RpcDefinitionBuilder rpc : addedRpcs) {
            rpcs.add(rpc.build());
        }

        // DEVIATIONS
        for (DeviationBuilder entry : deviationBuilders) {
            deviations.add(entry.build());
        }

        // EXTENSIONS
        for (ExtensionBuilder eb : addedExtensions) {
            extensions.add(eb.build());
        }
        Collections.sort(extensions, Comparators.SCHEMA_NODE_COMP);


        // IDENTITIES
        for (IdentitySchemaNodeBuilder id : addedIdentities) {
            identities.add(id.build());
        }

        // UNKNOWN NODES
        for (UnknownSchemaNodeBuilder unb : addedUnknownNodes) {
            unknownNodes.add(unb.build());
        }
        Collections.sort(unknownNodes, Comparators.SCHEMA_NODE_COMP);

        instance = new ModuleImpl(name, sourcePath, this);
        return instance;
    }

    public String getModuleSourcePath() {
        return sourcePath;
    }

    @Override
    public ModuleBuilder getParent() {
        return parent;
    }

    public void setParent(final ModuleBuilder parent) {
        this.parent = parent;
    }

    @Override
    public void setParent(final Builder parent) {
        throw new YangParseException(name, 0, "Can not set parent to module");
    }

    @Override
    public SchemaPath getPath() {
        return SCHEMA_PATH;
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
        return qnameModule.getNamespace();
    }

    public QNameModule getQNameModule() {
        return qnameModule;
    }

    public void setQNameModule(final QNameModule qnameModule) {
        this.qnameModule = Preconditions.checkNotNull(qnameModule);
    }

    public void setNamespace(final URI namespace) {
        this.qnameModule = QNameModule.cachedReference(QNameModule.create(namespace, qnameModule.getRevision()));
    }

    public String getPrefix() {
        return prefix;
    }

    public Date getRevision() {
        return qnameModule.getRevision();
    }

    public ModuleImport getImport(final String prefix) {
        return imports.get(prefix);
    }

    public Map<String, ModuleImport> getImports() {
        return imports;
    }

    public ModuleBuilder getImportedModule(final String prefix) {
        return importedModules.get(prefix);
    }

    public void addImportedModule(final String prefix, final ModuleBuilder module) {
        checkPrefix(prefix);
        importedModules.put(prefix, module);
    }

    public Map<String, Date> getIncludedModules() {
        return includedModules;
    }

    public void addInclude(final String name, final Date revision) {
        includedModules.put(name, revision);
    }

    public void addSubmodule(final ModuleBuilder submodule) {
        addedSubmodules.add(submodule);
    }

    protected String getSource() {
        return source;
    }

    public boolean isSubmodule() {
        return submodule;
    }

    public String getBelongsTo() {
        return belongsTo;
    }

    public void setBelongsTo(final String belongsTo) {
        this.belongsTo = belongsTo;
    }

    public void markActualNodeDirty() {
        final TypeAwareBuilder nodeBuilder = (TypeAwareBuilder) getActualNode();
        dirtyNodes.add(nodeBuilder);
    }

    public void setRevision(final Date revision) {
        this.qnameModule = QNameModule.cachedReference(QNameModule.create(qnameModule.getNamespace(), revision));
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public void setYangVersion(final String yangVersion) {
        this.yangVersion = yangVersion;
    }

    public void setOrganization(final String organization) {
        this.organization = organization;
    }

    public void setContact(final String contact) {
        this.contact = contact;
    }

    public void addModuleImport(final String moduleName, final Date revision, final String prefix) {
        checkPrefix(prefix);
        checkNotSealed();
        final ModuleImport moduleImport = new ModuleImportImpl(moduleName, revision, prefix);
        imports.put(prefix, moduleImport);
    }

    private void checkPrefix(final String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            throw new IllegalArgumentException("Cannot add imported module with undefined prefix");
        }
        if (prefix.equals(this.prefix)) {
            throw new IllegalArgumentException("Cannot add imported module with prefix equals to module prefix");
        }
    }

    public ExtensionBuilder addExtension(final QName qname, final int line, final SchemaPath path) {
        checkNotSealed();
        Builder parentBuilder = getActualNode();
        if (!(parentBuilder.equals(this))) {
            throw new YangParseException(name, line, "extension can be defined only in module or submodule");
        }

        final String extName = qname.getLocalName();
        for (ExtensionBuilder addedExtension : addedExtensions) {
            if (addedExtension.getQName().getLocalName().equals(extName)) {
                raiseYangParserException("extension", "node", extName, line, addedExtension.getLine());
            }
        }
        final ExtensionBuilder builder = new ExtensionBuilderImpl(name, line, qname, path);
        builder.setParent(parentBuilder);
        addedExtensions.add(builder);
        return builder;
    }

    public ContainerSchemaNodeBuilder addContainerNode(final int line, final QName qname, final SchemaPath schemaPath) {
        checkNotSealed();
        final ContainerSchemaNodeBuilder builder = new ContainerSchemaNodeBuilder(name, line, qname, schemaPath);

        Builder parentBuilder = getActualNode();
        builder.setParent(parentBuilder);
        addChildToParent(parentBuilder, builder, qname.getLocalName());

        return builder;
    }

    public ListSchemaNodeBuilder addListNode(final int line, final QName qname, final SchemaPath schemaPath) {
        checkNotSealed();
        final ListSchemaNodeBuilder builder = new ListSchemaNodeBuilder(name, line, qname, schemaPath);

        Builder parentBuilder = getActualNode();
        builder.setParent(parentBuilder);
        addChildToParent(parentBuilder, builder, qname.getLocalName());
        allLists.add(builder);

        return builder;
    }

    public LeafSchemaNodeBuilder addLeafNode(final int line, final QName qname, final SchemaPath schemaPath) {
        checkNotSealed();
        final LeafSchemaNodeBuilder builder = new LeafSchemaNodeBuilder(name, line, qname, schemaPath);

        Builder parentBuilder = getActualNode();
        builder.setParent(parentBuilder);
        addChildToParent(parentBuilder, builder, qname.getLocalName());

        return builder;
    }

    public LeafListSchemaNodeBuilder addLeafListNode(final int line, final QName qname, final SchemaPath schemaPath) {
        checkNotSealed();
        final LeafListSchemaNodeBuilder builder = new LeafListSchemaNodeBuilder(name, line, qname, schemaPath);

        Builder parentBuilder = getActualNode();
        builder.setParent(parentBuilder);
        addChildToParent(parentBuilder, builder, qname.getLocalName());

        return builder;
    }

    public GroupingBuilder addGrouping(final int line, final QName qname, final SchemaPath path) {
        checkNotSealed();
        final GroupingBuilder builder = new GroupingBuilderImpl(name, line, qname, path);

        Builder parentBuilder = getActualNode();
        builder.setParent(parentBuilder);

        String groupingName = qname.getLocalName();
        if (parentBuilder.equals(this)) {
            for (GroupingBuilder addedGrouping : getGroupingBuilders()) {
                if (addedGrouping.getQName().getLocalName().equals(groupingName)) {
                    raiseYangParserException("", GROUPING_STR, groupingName, line, addedGrouping.getLine());
                }
            }
            addGrouping(builder);
        } else {
            if (parentBuilder instanceof DataNodeContainerBuilder) {
                DataNodeContainerBuilder parentNode = (DataNodeContainerBuilder) parentBuilder;
                for (GroupingBuilder addedGrouping : parentNode.getGroupingBuilders()) {
                    if (addedGrouping.getQName().getLocalName().equals(groupingName)) {
                        raiseYangParserException("", GROUPING_STR, groupingName, line, addedGrouping.getLine());
                    }
                }
                parentNode.addGrouping(builder);
            } else if (parentBuilder instanceof RpcDefinitionBuilder) {
                RpcDefinitionBuilder parentNode = (RpcDefinitionBuilder) parentBuilder;
                for (GroupingBuilder child : parentNode.getGroupings()) {
                    if (child.getQName().getLocalName().equals(groupingName)) {
                        raiseYangParserException("", GROUPING_STR, groupingName, line, child.getLine());
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

    public AugmentationSchemaBuilder addAugment(final int line, final String augmentTargetStr,
            final SchemaPath targetPath, final int order) {
        checkNotSealed();
        final AugmentationSchemaBuilder builder = new AugmentationSchemaBuilderImpl(name, line, augmentTargetStr,
                targetPath, order);

        Builder parentBuilder = getActualNode();
        builder.setParent(parentBuilder);

        if (parentBuilder.equals(this)) {
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
            if (parentBuilder instanceof UsesNodeBuilder) {
                if (augmentTargetStr.startsWith("/")) {
                    throw new YangParseException(name, line,
                            "If 'augment' statement is a substatement to the 'uses' statement, it cannot contain absolute path ("
                                    + augmentTargetStr + ")");
                }
                ((UsesNodeBuilder) parentBuilder).addAugment(builder);
            } else {
                throw new YangParseException(name, line, "Augment can be declared only under module or uses statement.");
            }
        }
        allAugments.add(builder);

        return builder;
    }

    public UsesNodeBuilder addUsesNode(final int line, final SchemaPath grouping) {
        checkNotSealed();
        final UsesNodeBuilder usesBuilder = new UsesNodeBuilderImpl(name, line, grouping);

        Builder parentBuilder = getActualNode();
        usesBuilder.setParent(parentBuilder);

        if (parentBuilder.equals(this)) {
            addUsesNode(usesBuilder);
        } else {
            if (!(parentBuilder instanceof DataNodeContainerBuilder)) {
                throw new YangParseException(name, line, "Unresolved parent of uses '" + grouping + "'.");
            }
            ((DataNodeContainerBuilder) parentBuilder).addUsesNode(usesBuilder);
        }
        if (parentBuilder instanceof AugmentationSchemaBuilder) {
            usesBuilder.setAugmenting(true);
        }

        allUsesNodes.add(usesBuilder);
        return usesBuilder;
    }

    public void addRefine(final RefineHolderImpl refine) {
        checkNotSealed();
        final Builder parentBuilder = getActualNode();
        if (!(parentBuilder instanceof UsesNodeBuilder)) {
            throw new YangParseException(name, refine.getLine(), "refine can be defined only in uses statement");
        }
        ((UsesNodeBuilder) parentBuilder).addRefine(refine);
        refine.setParent(parentBuilder);
    }

    public RpcDefinitionBuilder addRpc(final int line, final QName qname, final SchemaPath path) {
        checkNotSealed();
        Builder parentBuilder = getActualNode();
        if (!(parentBuilder.equals(this))) {
            throw new YangParseException(name, line, "rpc can be defined only in module or submodule");
        }

        final RpcDefinitionBuilder rpcBuilder = new RpcDefinitionBuilder(name, line, qname, path);
        rpcBuilder.setParent(parentBuilder);

        String rpcName = qname.getLocalName();
        checkNotConflictingInDataNamespace(rpcName, line);
        addedRpcs.add(rpcBuilder);
        return rpcBuilder;
    }

    private void checkNotConflictingInDataNamespace(final String rpcName, final int line) {
        for (DataSchemaNodeBuilder addedChild : getChildNodeBuilders()) {
            if (addedChild.getQName().getLocalName().equals(rpcName)) {
                raiseYangParserException("rpc", "node", rpcName, line, addedChild.getLine());
            }
        }
        for (RpcDefinitionBuilder rpc : addedRpcs) {
            if (rpc.getQName().getLocalName().equals(rpcName)) {
                raiseYangParserException("", "rpc", rpcName, line, rpc.getLine());
            }
        }
        for (NotificationBuilder addedNotification : addedNotifications) {
            if (addedNotification.getQName().getLocalName().equals(rpcName)) {
                raiseYangParserException("rpc", "notification", rpcName, line, addedNotification.getLine());
            }
        }
    }

    public ContainerSchemaNodeBuilder addRpcInput(final int line, final QName qname, final SchemaPath schemaPath) {
        checkNotSealed();
        final Builder parentBuilder = getActualNode();
        if (!(parentBuilder instanceof RpcDefinitionBuilder)) {
            throw new YangParseException(name, line, "input can be defined only in rpc statement");
        }
        final RpcDefinitionBuilder rpc = (RpcDefinitionBuilder) parentBuilder;

        final ContainerSchemaNodeBuilder inputBuilder = new ContainerSchemaNodeBuilder(name, line, qname, schemaPath);
        inputBuilder.setParent(rpc);

        rpc.setInput(inputBuilder);
        return inputBuilder;
    }

    public ContainerSchemaNodeBuilder addRpcOutput(final SchemaPath schemaPath, final QName qname, final int line) {
        checkNotSealed();
        final Builder parentBuilder = getActualNode();
        if (!(parentBuilder instanceof RpcDefinitionBuilder)) {
            throw new YangParseException(name, line, "output can be defined only in rpc statement");
        }
        final RpcDefinitionBuilder rpc = (RpcDefinitionBuilder) parentBuilder;

        final ContainerSchemaNodeBuilder outputBuilder = new ContainerSchemaNodeBuilder(name, line, qname, schemaPath);
        outputBuilder.setParent(rpc);

        rpc.setOutput(outputBuilder);
        return outputBuilder;
    }

    public void addNotification(final NotificationDefinition notification) {
        checkNotSealed();
        notifications.add(notification);
    }

    public NotificationBuilder addNotification(final int line, final QName qname, final SchemaPath path) {
        checkNotSealed();
        final Builder parentBuilder = getActualNode();
        if (!(parentBuilder.equals(this))) {
            throw new YangParseException(name, line, "notification can be defined only in module or submodule");
        }

        String notificationName = qname.getLocalName();
        checkNotConflictingInDataNamespace(notificationName, line);

        final NotificationBuilder builder = new NotificationBuilder(name, line, qname, path);
        builder.setParent(parentBuilder);
        addedNotifications.add(builder);

        return builder;
    }

    public FeatureBuilder addFeature(final int line, final QName qname, final SchemaPath path) {
        Builder parentBuilder = getActualNode();
        if (!(parentBuilder.equals(this))) {
            throw new YangParseException(name, line, "feature can be defined only in module or submodule");
        }

        final FeatureBuilder builder = new FeatureBuilder(name, line, qname, path);
        builder.setParent(parentBuilder);

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

        Builder parentBuilder = getActualNode();
        builder.setParent(parentBuilder);
        addChildToParent(parentBuilder, builder, qname.getLocalName());

        return builder;
    }

    public ChoiceCaseBuilder addCase(final int line, final QName qname, final SchemaPath path) {
        Builder parentBuilder = getActualNode();
        if (parentBuilder == null || parentBuilder.equals(this)) {
            throw new YangParseException(name, line, "'case' parent not found");
        }

        final ChoiceCaseBuilder builder = new ChoiceCaseBuilder(name, line, qname, path);
        builder.setParent(parentBuilder);

        if (parentBuilder instanceof ChoiceBuilder) {
            ((ChoiceBuilder) parentBuilder).addCase(builder);
        } else if (parentBuilder instanceof AugmentationSchemaBuilder) {
            ((AugmentationSchemaBuilder) parentBuilder).addChildNode(builder);
        } else {
            throw new YangParseException(name, line, "Unresolved parent of 'case' " + qname.getLocalName());
        }

        return builder;
    }

    public AnyXmlBuilder addAnyXml(final int line, final QName qname, final SchemaPath schemaPath) {
        final AnyXmlBuilder builder = new AnyXmlBuilder(name, line, qname, schemaPath);

        Builder parentBuilder = getActualNode();
        builder.setParent(parentBuilder);
        addChildToParent(parentBuilder, builder, qname.getLocalName());

        return builder;
    }

    @Override
    public void addTypedef(final TypeDefinitionBuilder typedefBuilder) {
        String nodeName = typedefBuilder.getQName().getLocalName();
        for (TypeDefinitionBuilder tdb : getTypeDefinitionBuilders()) {
            if (tdb.getQName().getLocalName().equals(nodeName)) {
                raiseYangParserException("", TYPEDEF_STR, nodeName, typedefBuilder.getLine(), tdb.getLine());
            }
        }
        super.addTypedef(typedefBuilder);
    }

    public TypeDefinitionBuilderImpl addTypedef(final int line, final QName qname, final SchemaPath path) {
        final TypeDefinitionBuilderImpl builder = new TypeDefinitionBuilderImpl(name, line, qname, path);

        Builder parentBuilder = getActualNode();
        builder.setParent(parentBuilder);

        String typedefName = qname.getLocalName();
        if (parentBuilder.equals(this)) {
            addTypedef(builder);
        } else {
            if (parentBuilder instanceof DataNodeContainerBuilder) {
                DataNodeContainerBuilder parentNode = (DataNodeContainerBuilder) parentBuilder;
                for (TypeDefinitionBuilder child : parentNode.getTypeDefinitionBuilders()) {
                    if (child.getQName().getLocalName().equals(typedefName)) {
                        raiseYangParserException("", TYPEDEF_STR, typedefName, line, child.getLine());
                    }
                }
                parentNode.addTypedef(builder);
            } else if (parentBuilder instanceof RpcDefinitionBuilder) {
                RpcDefinitionBuilder rpcParent = (RpcDefinitionBuilder) parentBuilder;
                for (TypeDefinitionBuilder tdb : rpcParent.getTypeDefinitions()) {
                    if (tdb.getQName().getLocalName().equals(builder.getQName().getLocalName())) {
                        raiseYangParserException("", TYPEDEF_STR, typedefName, line, tdb.getLine());
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
        Builder parentBuilder = getActualNode();
        if (!(parentBuilder instanceof TypeAwareBuilder)) {
            throw new YangParseException("Failed to set type '" + type.getQName().getLocalName()
                    + "'. Invalid parent node: " + parentBuilder);
        }
        ((TypeAwareBuilder) parentBuilder).setType(type);
    }

    public UnionTypeBuilder addUnionType(final int line, final QNameModule module) {
        final Builder parentBuilder = getActualNode();
        if (parentBuilder == null) {
            throw new YangParseException(name, line, "Unresolved parent of union type");
        } else {
            final UnionTypeBuilder union = new UnionTypeBuilder(name, line);
            if (parentBuilder instanceof TypeAwareBuilder) {
                ((TypeAwareBuilder) parentBuilder).setTypedef(union);
                return union;
            } else {
                throw new YangParseException(name, line, "Invalid parent of union type.");
            }
        }
    }

    public void addIdentityrefType(final int line, final SchemaPath schemaPath, final String baseString) {
        final IdentityrefTypeBuilder identityref = new IdentityrefTypeBuilder(name, line, baseString, schemaPath);

        final Builder parentBuilder = getActualNode();
        if (parentBuilder == null) {
            throw new YangParseException(name, line, "Unresolved parent of identityref type.");
        } else {
            if (parentBuilder instanceof TypeAwareBuilder) {
                final TypeAwareBuilder typeParent = (TypeAwareBuilder) parentBuilder;
                typeParent.setTypedef(identityref);
                dirtyNodes.add(typeParent);
            } else {
                throw new YangParseException(name, line, "Invalid parent of identityref type.");
            }
        }
    }

    public DeviationBuilder addDeviation(final int line, final SchemaPath targetPath) {
        Builder parentBuilder = getActualNode();
        if (!(parentBuilder.equals(this))) {
            throw new YangParseException(name, line, "deviation can be defined only in module or submodule");
        }

        final DeviationBuilder builder = new DeviationBuilder(name, line, targetPath);
        builder.setParent(parentBuilder);
        deviationBuilders.add(builder);
        return builder;
    }

    public IdentitySchemaNodeBuilder addIdentity(final QName qname, final int line, final SchemaPath path) {
        Builder parentBuilder = getActualNode();
        if (!(parentBuilder.equals(this))) {
            throw new YangParseException(name, line, "identity can be defined only in module or submodule");
        }
        String identityName = qname.getLocalName();
        for (IdentitySchemaNodeBuilder idBuilder : addedIdentities) {
            if (idBuilder.getQName().equals(qname)) {
                raiseYangParserException("", "identity", identityName, line, idBuilder.getLine());
            }
        }

        final IdentitySchemaNodeBuilder builder = new IdentitySchemaNodeBuilder(name, line, qname, path);
        builder.setParent(parentBuilder);
        addedIdentities.add(builder);
        return builder;
    }

    @Override
    public void addUnknownNodeBuilder(final UnknownSchemaNodeBuilder builder) {
        addedUnknownNodes.add(builder);
        allUnknownNodes.add(builder);
    }

    public UnknownSchemaNodeBuilderImpl addUnknownSchemaNode(final int line, final QName qname, final SchemaPath path) {
        final Builder parentBuilder = getActualNode();
        final UnknownSchemaNodeBuilderImpl builder = new UnknownSchemaNodeBuilderImpl(name, line, qname, path);
        builder.setParent(parentBuilder);
        allUnknownNodes.add(builder);

        if (parentBuilder.equals(this)) {
            addedUnknownNodes.add(builder);
        } else {
            if (parentBuilder instanceof SchemaNodeBuilder) {
                parentBuilder.addUnknownNodeBuilder(builder);
            } else if (parentBuilder instanceof DataNodeContainerBuilder) {
                parentBuilder.addUnknownNodeBuilder(builder);
            } else if (parentBuilder instanceof RefineHolderImpl) {
                parentBuilder.addUnknownNodeBuilder(builder);
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

    public void setSource(final ByteSource byteSource) throws IOException {
        setSource(byteSource.asCharSource(Charsets.UTF_8).read());
    }

    public void setSource(final String source) {
        this.source = source;
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

    public String getYangVersion() {
        return yangVersion;
    }

    public String getContact() {
        return contact;
    }

    public String getOrganization() {
        return organization;
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
        for (DataSchemaNodeBuilder childNode : getChildNodeBuilders()) {
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
        addChildNode(child);
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
        throw new YangParseException(getModuleName(), currentLine, msg);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + qnameModule.hashCode();
        result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());

        return result;
    }

    @Override
    public boolean equals(final Object obj) {
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
        if (!qnameModule.equals(other.qnameModule)) {
            return false;
        }
        if (prefix == null) {
            if (other.prefix != null) {
                return false;
            }
        } else if (!prefix.equals(other.prefix)) {
            return false;
        }
        return true;
    }

    public List<UnknownSchemaNode> getExtensionInstances() {
        return unknownNodes;
    }
}
