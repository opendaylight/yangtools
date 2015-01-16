/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil.computeDefaultSUID;
import static org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil.packageNameForGeneratedType;
import static org.opendaylight.yangtools.binding.generator.util.BindingTypes.DATA_OBJECT;
import static org.opendaylight.yangtools.binding.generator.util.BindingTypes.DATA_ROOT;
import static org.opendaylight.yangtools.binding.generator.util.BindingTypes.IDENTIFIABLE;
import static org.opendaylight.yangtools.binding.generator.util.BindingTypes.IDENTIFIER;
import static org.opendaylight.yangtools.binding.generator.util.BindingTypes.NOTIFICATION;
import static org.opendaylight.yangtools.binding.generator.util.BindingTypes.augmentable;
import static org.opendaylight.yangtools.binding.generator.util.Types.BOOLEAN;
import static org.opendaylight.yangtools.binding.generator.util.Types.FUTURE;
import static org.opendaylight.yangtools.binding.generator.util.Types.VOID;
import static org.opendaylight.yangtools.binding.generator.util.Types.typeForClass;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.findDataSchemaNode;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.findNodeInSchemaContext;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.findParentModule;
import java.util.regex.Pattern;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.sal.binding.generator.impl.YangTemplate;
import org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil;
import org.opendaylight.yangtools.binding.generator.util.BindingTypes;
import org.opendaylight.yangtools.binding.generator.util.ReferencedTypeImpl;
import org.opendaylight.yangtools.binding.generator.util.Types;
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedPropertyBuilderImpl;
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedTOBuilderImpl;
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedTypeBuilderImpl;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.generator.spi.TypeProvider;
import org.opendaylight.yangtools.sal.binding.model.api.AccessModifier;
import org.opendaylight.yangtools.sal.binding.model.api.Constant;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.sal.binding.model.api.Restrictions;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.EnumBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.sal.binding.yang.types.GroupingDefinitionDependencySort;
import org.opendaylight.yangtools.sal.binding.yang.types.TypeProviderImpl;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.binding.annotations.RoutingContext;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.DataNodeIterator;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.model.util.SchemaNodeUtils;
import org.opendaylight.yangtools.yang.model.util.UnionType;
import org.opendaylight.yangtools.yang.parser.builder.util.Comparators;
import org.opendaylight.yangtools.yang.parser.util.ModuleDependencySort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindingGeneratorImpl implements BindingGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(BindingGeneratorImpl.class);
    private static final Splitter COLON_SPLITTER = Splitter.on(':');
    private static final Splitter BSDOT_SPLITTER = Splitter.on("\\.");
    private static final char NEW_LINE = '\n';
    private static final String QNAME_FQCN = QName.class.getName();

    /**
     * Constant with the concrete name of identifier.
     */
    private static final String AUGMENT_IDENTIFIER_NAME = "augment-identifier";

    /**
     * Constant with the concrete name of namespace.
     */
    private static final String YANG_EXT_NAMESPACE = "urn:opendaylight:yang:extension:yang-ext";

    private static final Pattern UNICODE_CHAR_PATTERN = Pattern.compile("\\\\+u");

    private final Map<Module, ModuleContext> genCtx = new HashMap<>();

    /**
     * When set to true, generated classes will include javadoc comments which
     * are useful for users.
     */
    private final boolean verboseClassComments;

    /**
     * Outer key represents the package name. Outer value represents map of all
     * builders in the same package. Inner key represents the schema node name
     * (in JAVA class/interface name format). Inner value represents instance of
     * builder for schema node specified in key part.
     */
    private Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders;

    /**
     * Provide methods for converting YANG types to JAVA types.
     */
    private TypeProvider typeProvider;

    /**
     * Holds reference to schema context to resolve data of augmented element
     * when creating augmentation builder
     */
    private SchemaContext schemaContext;

    /**
     * Create a new binding generator with verboe comments.
     *
     * @deprecated Use {@link #BindingGeneratorImpl(boolean)} instead.
     */
    @Deprecated
    public BindingGeneratorImpl() {
        this(true);
    }

    /**
     * Create a new binding generator.
     *
     * @param verboseClassComments generate verbose comments
     */
    public BindingGeneratorImpl(final boolean verboseClassComments) {
        this.verboseClassComments = verboseClassComments;
    }

    /**
     * Resolves generated types from <code>context</code> schema nodes of all
     * modules.
     *
     * Generated types are created for modules, groupings, types, containers,
     * lists, choices, augments, rpcs, notification, identities.
     *
     * @param context
     *            schema context which contains data about all schema nodes
     *            saved in modules
     * @return list of types (usually <code>GeneratedType</code>
     *         <code>GeneratedTransferObject</code>which are generated from
     *         <code>context</code> data.
     * @throws IllegalArgumentException
     *             if arg <code>context</code> is null
     * @throws IllegalStateException
     *             if <code>context</code> contain no modules
     */
    @Override
    public List<Type> generateTypes(final SchemaContext context) {
        checkArgument(context != null, "Schema Context reference cannot be NULL.");
        checkState(context.getModules() != null, "Schema Context does not contain defined modules.");
        schemaContext = context;
        typeProvider = new TypeProviderImpl(context);
        final Set<Module> modules = context.getModules();
        return generateTypes(context, modules);
    }

    /**
     * Resolves generated types from <code>context</code> schema nodes only for
     * modules specified in <code>modules</code>
     *
     * Generated types are created for modules, groupings, types, containers,
     * lists, choices, augments, rpcs, notification, identities.
     *
     * @param context
     *            schema context which contains data about all schema nodes
     *            saved in modules
     * @param modules
     *            set of modules for which schema nodes should be generated
     *            types
     * @return list of types (usually <code>GeneratedType</code> or
     *         <code>GeneratedTransferObject</code>) which:
     *         <ul>
     *         <li>are generated from <code>context</code> schema nodes and</li>
     *         <li>are also part of some of the module in <code>modules</code>
     *         set.</li>
     *         </ul>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if arg <code>context</code> is null or</li>
     *             <li>if arg <code>modules</code> is null</li>
     *             </ul>
     * @throws IllegalStateException
     *             if <code>context</code> contain no modules
     */
    @Override
    public List<Type> generateTypes(final SchemaContext context, final Set<Module> modules) {
        checkArgument(context != null, "Schema Context reference cannot be NULL.");
        checkState(context.getModules() != null, "Schema Context does not contain defined modules.");
        checkArgument(modules != null, "Set of Modules cannot be NULL.");

        schemaContext = context;
        typeProvider = new TypeProviderImpl(context);
        final Module[] modulesArray = new Module[context.getModules().size()];
        context.getModules().toArray(modulesArray);
        final List<Module> contextModules = ModuleDependencySort.sort(modulesArray);
        genTypeBuilders = new HashMap<>();

        for (final Module contextModule : contextModules) {
            moduleToGenTypes(contextModule, context);
        }
        for (final Module contextModule : contextModules) {
            allAugmentsToGenTypes(contextModule);
        }

        final List<Type> filteredGenTypes = new ArrayList<>();
        for (final Module m : modules) {
            final ModuleContext ctx = checkNotNull(genCtx.get(m), "Module context not found for module %s", m);
            filteredGenTypes.addAll(ctx.getGeneratedTypes());
            final Set<Type> additionalTypes = ((TypeProviderImpl) typeProvider).getAdditionalTypes().get(m);
            if (additionalTypes != null) {
                filteredGenTypes.addAll(additionalTypes);
            }
        }

        return filteredGenTypes;
    }

    private void moduleToGenTypes(final Module m, final SchemaContext context) {
        genCtx.put(m, new ModuleContext());
        allTypeDefinitionsToGenTypes(m);
        groupingsToGenTypes(m, m.getGroupings());
        rpcMethodsToGenType(m);
        allIdentitiesToGenTypes(m, context);
        notificationsToGenType(m);

        if (!m.getChildNodes().isEmpty()) {
            final GeneratedTypeBuilder moduleType = moduleToDataType(m);
            genCtx.get(m).addModuleNode(moduleType);
            final String basePackageName = BindingMapping.getRootPackageName(m.getQNameModule());
            resolveDataSchemaNodes(m, basePackageName, moduleType, moduleType, m.getChildNodes());
        }
    }

    /**
     * Converts all extended type definitions of module to the list of
     * <code>Type</code> objects.
     *
     * @param module
     *            module from which is obtained set of type definitions
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if module is null</li>
     *             <li>if name of module is null</li>
     *             </ul>
     * @throws IllegalStateException
     *             if set of type definitions from module is null
     */
    private void allTypeDefinitionsToGenTypes(final Module module) {
        checkArgument(module != null, "Module reference cannot be NULL.");
        checkArgument(module.getName() != null, "Module name cannot be NULL.");
        final DataNodeIterator it = new DataNodeIterator(module);
        final List<TypeDefinition<?>> typeDefinitions = it.allTypedefs();
        checkState(typeDefinitions != null, "Type Definitions for module «module.name» cannot be NULL.");

        for (final TypeDefinition<?> typedef : typeDefinitions) {
            if (typedef != null) {
                final Type type = ((TypeProviderImpl) typeProvider).generatedTypeForExtendedDefinitionType(typedef,
                        typedef);
                if (type != null) {
                    final ModuleContext ctx = genCtx.get(module);
                    ctx.addTypedefType(typedef.getPath(), type);
                    ctx.addTypeToSchema(type,typedef);
                }
            }
        }
    }

    private GeneratedTypeBuilder processDataSchemaNode(final Module module, final String basePackageName,
            final GeneratedTypeBuilder childOf, final DataSchemaNode node) {
        if (node.isAugmenting() || node.isAddedByUses()) {
            return null;
        }
        final String packageName = packageNameForGeneratedType(basePackageName, node.getPath());
        final GeneratedTypeBuilder genType = addDefaultInterfaceDefinition(packageName, node, childOf, module);
        genType.addComment(node.getDescription());
        genType.setDescription(createDescription(node, genType.getFullyQualifiedName()));
        genType.setModuleName(module.getName());
        genType.setReference(node.getReference());
        genType.setSchemaPath(node.getPath().getPathFromRoot());
        if (node instanceof DataNodeContainer) {
            genCtx.get(module).addChildNodeType(node, genType);
            groupingsToGenTypes(module, ((DataNodeContainer) node).getGroupings());
            processUsesAugments((DataNodeContainer) node, module);
        }
        return genType;
    }

    private void containerToGenType(final Module module, final String basePackageName,
            final GeneratedTypeBuilder parent, final GeneratedTypeBuilder childOf, final ContainerSchemaNode node) {
        final GeneratedTypeBuilder genType = processDataSchemaNode(module, basePackageName, childOf, node);
        if (genType != null) {
            constructGetter(parent, node.getQName().getLocalName(), node.getDescription(), genType);
            resolveDataSchemaNodes(module, basePackageName, genType, genType, node.getChildNodes());
        }
    }

    private void listToGenType(final Module module, final String basePackageName, final GeneratedTypeBuilder parent,
            final GeneratedTypeBuilder childOf, final ListSchemaNode node) {
        final GeneratedTypeBuilder genType = processDataSchemaNode(module, basePackageName, childOf, node);
        if (genType != null) {
            constructGetter(parent, node.getQName().getLocalName(), node.getDescription(), Types.listTypeFor(genType));

            final List<String> listKeys = listKeys(node);
            final String packageName = packageNameForGeneratedType(basePackageName, node.getPath());
            final GeneratedTOBuilder genTOBuilder = resolveListKeyTOBuilder(packageName, node);
            if (genTOBuilder != null) {
                final Type identifierMarker = Types.parameterizedTypeFor(IDENTIFIER, genType);
                final Type identifiableMarker = Types.parameterizedTypeFor(IDENTIFIABLE, genTOBuilder);
                genTOBuilder.addImplementsType(identifierMarker);
                genType.addImplementsType(identifiableMarker);
            }

            for (final DataSchemaNode schemaNode : node.getChildNodes()) {
                if (!schemaNode.isAugmenting()) {
                    addSchemaNodeToListBuilders(basePackageName, schemaNode, genType, genTOBuilder, listKeys, module);
                }
            }

            // serialVersionUID
            if (genTOBuilder != null) {
                final GeneratedPropertyBuilder prop = new GeneratedPropertyBuilderImpl("serialVersionUID");
                prop.setValue(Long.toString(computeDefaultSUID(genTOBuilder)));
                genTOBuilder.setSUID(prop);
            }

            typeBuildersToGenTypes(module, genType, genTOBuilder);
        }
    }

    private void processUsesAugments(final DataNodeContainer node, final Module module) {
        final String basePackageName = BindingMapping.getRootPackageName(module.getQNameModule());
        for (final UsesNode usesNode : node.getUses()) {
            for (final AugmentationSchema augment : usesNode.getAugmentations()) {
                usesAugmentationToGenTypes(basePackageName, augment, module, usesNode, node);
                processUsesAugments(augment, module);
            }
        }
    }

    /**
     * Converts all <b>augmentation</b> of the module to the list
     * <code>Type</code> objects.
     *
     * @param module
     *            module from which is obtained list of all augmentation objects
     *            to iterate over them
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if the module is null</li>
     *             <li>if the name of module is null</li>
     *             </ul>
     * @throws IllegalStateException
     *             if set of augmentations from module is null
     */
    private void allAugmentsToGenTypes(final Module module) {
        checkArgument(module != null, "Module reference cannot be NULL.");
        checkArgument(module.getName() != null, "Module name cannot be NULL.");
        checkState(module.getAugmentations() != null, "Augmentations Set cannot be NULL.");

        final String basePackageName = BindingMapping.getRootPackageName(module.getQNameModule());
        final List<AugmentationSchema> augmentations = resolveAugmentations(module);
        for (final AugmentationSchema augment : augmentations) {
            augmentationToGenTypes(basePackageName, augment, module);
        }
    }

    /**
     * Returns list of <code>AugmentationSchema</code> objects. The objects are
     * sorted according to the length of their target path from the shortest to
     * the longest.
     *
     * @param module
     *            module from which is obtained list of all augmentation objects
     * @return list of sorted <code>AugmentationSchema</code> objects obtained
     *         from <code>module</code>
     * @throws IllegalArgumentException
     *             if module is null
     * @throws IllegalStateException
     *             if set of module augmentations is null
     */
    private List<AugmentationSchema> resolveAugmentations(final Module module) {
        checkArgument(module != null, "Module reference cannot be NULL.");
        checkState(module.getAugmentations() != null, "Augmentations Set cannot be NULL.");

        final Set<AugmentationSchema> augmentations = module.getAugmentations();
        final List<AugmentationSchema> sortedAugmentations = new ArrayList<>(augmentations);
        Collections.sort(sortedAugmentations, Comparators.AUGMENT_COMP);

        return sortedAugmentations;
    }

    /**
     * Create GeneratedTypeBuilder object from module argument.
     *
     * @param module
     *            Module object from which builder will be created
     * @return <code>GeneratedTypeBuilder</code> which is internal
     *         representation of the module
     * @throws IllegalArgumentException
     *             if module is null
     */
    private GeneratedTypeBuilder moduleToDataType(final Module module) {
        checkArgument(module != null, "Module reference cannot be NULL.");

        final GeneratedTypeBuilder moduleDataTypeBuilder = moduleTypeBuilder(module, "Data");
        addImplementedInterfaceFromUses(module, moduleDataTypeBuilder);
        moduleDataTypeBuilder.addImplementsType(DATA_ROOT);
        moduleDataTypeBuilder.addComment(module.getDescription());
        moduleDataTypeBuilder.setDescription(createDescription(module));
        moduleDataTypeBuilder.setReference(module.getReference());
        return moduleDataTypeBuilder;
    }

    /**
     * Converts all <b>rpcs</b> inputs and outputs substatements of the module
     * to the list of <code>Type</code> objects. In addition are to containers
     * and lists which belong to input or output also part of returning list.
     *
     * @param module
     *            module from which is obtained set of all rpc objects to
     *            iterate over them
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if the module is null</li>
     *             <li>if the name of module is null</li>
     *             </ul>
     * @throws IllegalStateException
     *             if set of rpcs from module is null
     */
    private void rpcMethodsToGenType(final Module module) {
        checkArgument(module != null, "Module reference cannot be NULL.");
        checkArgument(module.getName() != null, "Module name cannot be NULL.");
        final Set<RpcDefinition> rpcDefinitions = module.getRpcs();
        checkState(rpcDefinitions != null, "Set of rpcs from module " + module.getName() + " cannot be NULL.");
        if (rpcDefinitions.isEmpty()) {
            return;
        }

        final String basePackageName = BindingMapping.getRootPackageName(module.getQNameModule());
        final GeneratedTypeBuilder interfaceBuilder = moduleTypeBuilder(module, "Service");
        interfaceBuilder.addImplementsType(Types.typeForClass(RpcService.class));
        interfaceBuilder.setDescription(createDescription(rpcDefinitions, module.getName(), module.getModuleSourcePath()));

        for (final RpcDefinition rpc : rpcDefinitions) {
            if (rpc != null) {
                final String rpcName = BindingMapping.getClassName(rpc.getQName());
                final String rpcMethodName = BindingMapping.getPropertyName(rpcName);
                final String rpcComment = rpc.getDescription();
                final MethodSignatureBuilder method = interfaceBuilder.addMethod(rpcMethodName);
                final ContainerSchemaNode input = rpc.getInput();
                final ContainerSchemaNode output = rpc.getOutput();

                if (input != null) {
                    final GeneratedTypeBuilder inType = addRawInterfaceDefinition(basePackageName, input, rpcName);
                    addImplementedInterfaceFromUses(input, inType);
                    inType.addImplementsType(DATA_OBJECT);
                    inType.addImplementsType(augmentable(inType));
                    resolveDataSchemaNodes(module, basePackageName, inType, inType, input.getChildNodes());
                    genCtx.get(module).addChildNodeType(input, inType);
                    final GeneratedType inTypeInstance = inType.toInstance();
                    method.addParameter(inTypeInstance, "input");
                }

                Type outTypeInstance = VOID;
                if (output != null) {
                    final GeneratedTypeBuilder outType = addRawInterfaceDefinition(basePackageName, output, rpcName);
                    addImplementedInterfaceFromUses(output, outType);
                    outType.addImplementsType(DATA_OBJECT);
                    outType.addImplementsType(augmentable(outType));
                    resolveDataSchemaNodes(module, basePackageName, outType, outType, output.getChildNodes());
                    genCtx.get(module).addChildNodeType(output, outType);
                    outTypeInstance = outType.toInstance();
                }

                final Type rpcRes = Types.parameterizedTypeFor(Types.typeForClass(RpcResult.class), outTypeInstance);
                method.setComment(rpcComment);
                method.setReturnType(Types.parameterizedTypeFor(FUTURE, rpcRes));
            }
        }

        genCtx.get(module).addTopLevelNodeType(interfaceBuilder);
    }

    /**
     * Converts all <b>notifications</b> of the module to the list of
     * <code>Type</code> objects. In addition are to this list added containers
     * and lists which are part of this notification.
     *
     * @param module
     *            module from which is obtained set of all notification objects
     *            to iterate over them
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if the module equals null</li>
     *             <li>if the name of module equals null</li>
     *             </ul>
     * @throws IllegalStateException
     *             if set of notifications from module is null
     */
    private void notificationsToGenType(final Module module) {
        checkArgument(module != null, "Module reference cannot be NULL.");
        checkArgument(module.getName() != null, "Module name cannot be NULL.");
        final Set<NotificationDefinition> notifications = module.getNotifications();
        checkState(notifications != null, "Set of notification from module " + module.getName() + " cannot be NULL.");
        if (notifications.isEmpty()) {
            return;
        }

        final GeneratedTypeBuilder listenerInterface = moduleTypeBuilder(module, "Listener");
        listenerInterface.addImplementsType(BindingTypes.NOTIFICATION_LISTENER);
        final String basePackageName = BindingMapping.getRootPackageName(module.getQNameModule());



        for (final NotificationDefinition notification : notifications) {
            if (notification != null) {
                processUsesAugments(notification, module);

                final GeneratedTypeBuilder notificationInterface = addDefaultInterfaceDefinition(basePackageName,
                        notification, null, module);
                notificationInterface.addImplementsType(NOTIFICATION);
                genCtx.get(module).addChildNodeType(notification, notificationInterface);

                // Notification object
                resolveDataSchemaNodes(module, basePackageName, notificationInterface, notificationInterface,
                        notification.getChildNodes());

                listenerInterface.addMethod("on" + notificationInterface.getName())
                .setAccessModifier(AccessModifier.PUBLIC).addParameter(notificationInterface, "notification")
                .setComment(notification.getDescription()).setReturnType(Types.VOID);
            }
        }
        listenerInterface.setDescription(createDescription(notifications, module.getName(), module.getModuleSourcePath()));

        genCtx.get(module).addTopLevelNodeType(listenerInterface);
    }

    /**
     * Converts all <b>identities</b> of the module to the list of
     * <code>Type</code> objects.
     *
     * @param module
     *            module from which is obtained set of all identity objects to
     *            iterate over them
     * @param context
     *            schema context only used as input parameter for method
     *            {@link identityToGenType}
     *
     */
    private void allIdentitiesToGenTypes(final Module module, final SchemaContext context) {
        final Set<IdentitySchemaNode> schemaIdentities = module.getIdentities();
        final String basePackageName = BindingMapping.getRootPackageName(module.getQNameModule());

        if (schemaIdentities != null && !schemaIdentities.isEmpty()) {
            for (final IdentitySchemaNode identity : schemaIdentities) {
                identityToGenType(module, basePackageName, identity, context);
            }
        }
    }

    /**
     * Converts the <b>identity</b> object to GeneratedType. Firstly it is
     * created transport object builder. If identity contains base identity then
     * reference to base identity is added to superior identity as its extend.
     * If identity doesn't contain base identity then only reference to abstract
     * class {@link org.opendaylight.yangtools.yang.model.api.BaseIdentity
     * BaseIdentity} is added
     *
     * @param module
     *            current module
     * @param basePackageName
     *            string contains the module package name
     * @param identity
     *            IdentitySchemaNode which contains data about identity
     * @param context
     *            SchemaContext which is used to get package and name
     *            information about base of identity
     *
     */
    private void identityToGenType(final Module module, final String basePackageName,
            final IdentitySchemaNode identity, final SchemaContext context) {
        if (identity == null) {
            return;
        }
        final String packageName = packageNameForGeneratedType(basePackageName, identity.getPath());
        final String genTypeName = BindingMapping.getClassName(identity.getQName());
        final GeneratedTOBuilderImpl newType = new GeneratedTOBuilderImpl(packageName, genTypeName);
        final IdentitySchemaNode baseIdentity = identity.getBaseIdentity();
        if (baseIdentity == null) {
            final GeneratedTOBuilderImpl gto = new GeneratedTOBuilderImpl(BaseIdentity.class.getPackage().getName(),
                    BaseIdentity.class.getSimpleName());
            newType.setExtendsType(gto.toInstance());
        } else {
            final Module baseIdentityParentModule = SchemaContextUtil.findParentModule(context, baseIdentity);
            final String returnTypePkgName = BindingMapping.getRootPackageName(baseIdentityParentModule
                    .getQNameModule());
            final String returnTypeName = BindingMapping.getClassName(baseIdentity.getQName());
            final GeneratedTransferObject gto = new GeneratedTOBuilderImpl(returnTypePkgName, returnTypeName)
            .toInstance();
            newType.setExtendsType(gto);
        }
        newType.setAbstract(true);
        newType.addComment(identity.getDescription());
        newType.setDescription(createDescription(identity, newType.getFullyQualifiedName()));
        newType.setReference(identity.getReference());
        newType.setModuleName(module.getName());
        newType.setSchemaPath(identity.getPath().getPathFromRoot());

        final QName qname = identity.getQName();
        qnameConstant(newType, BindingMapping.QNAME_STATIC_FIELD_NAME, qname);

        genCtx.get(module).addIdentityType(identity.getQName(), newType);
    }

    private static Constant qnameConstant(final GeneratedTypeBuilderBase<?> toBuilder, final String constantName,
            final QName name) {
        final StringBuilder sb = new StringBuilder(QNAME_FQCN);
        sb.append(".cachedReference(");
        sb.append(QNAME_FQCN);
        sb.append(".create(");
        sb.append('"');
        sb.append(name.getNamespace());
        sb.append("\",\"");
        sb.append(name.getFormattedRevision());
        sb.append("\",\"");
        sb.append(name.getLocalName());
        sb.append("\"))");

        return toBuilder.addConstant(typeForClass(QName.class), constantName, sb.toString());
    }

    /**
     * Converts all <b>groupings</b> of the module to the list of
     * <code>Type</code> objects. Firstly are groupings sorted according mutual
     * dependencies. At least dependent (independent) groupings are in the list
     * saved at first positions. For every grouping the record is added to map
     * {@link BindingGeneratorImpl#allGroupings allGroupings}
     *
     * @param module
     *            current module
     * @param collection
     *            of groupings from which types will be generated
     *
     */
    private void groupingsToGenTypes(final Module module, final Collection<GroupingDefinition> groupings) {
        final String basePackageName = BindingMapping.getRootPackageName(module.getQNameModule());
        final List<GroupingDefinition> groupingsSortedByDependencies = new GroupingDefinitionDependencySort()
        .sort(groupings);
        for (final GroupingDefinition grouping : groupingsSortedByDependencies) {
            groupingToGenType(basePackageName, grouping, module);
        }
    }

    /**
     * Converts individual grouping to GeneratedType. Firstly generated type
     * builder is created and every child node of grouping is resolved to the
     * method.
     *
     * @param basePackageName
     *            string contains the module package name
     * @param grouping
     *            GroupingDefinition which contains data about grouping
     * @param module
     *            current module
     * @return GeneratedType which is generated from grouping (object of type
     *         <code>GroupingDefinition</code>)
     */
    private void groupingToGenType(final String basePackageName, final GroupingDefinition grouping, final Module module) {
        final String packageName = packageNameForGeneratedType(basePackageName, grouping.getPath());
        final GeneratedTypeBuilder genType = addDefaultInterfaceDefinition(packageName, grouping, module);
        genCtx.get(module).addGroupingType(grouping.getPath(), genType);
        resolveDataSchemaNodes(module, basePackageName, genType, genType, grouping.getChildNodes());
        groupingsToGenTypes(module, grouping.getGroupings());
        processUsesAugments(grouping, module);
    }

    /**
     * Adds enumeration builder created from <code>enumTypeDef</code> to
     * <code>typeBuilder</code>.
     *
     * Each <code>enumTypeDef</code> item is added to builder with its name and
     * value.
     *
     * @param enumTypeDef
     *            EnumTypeDefinition contains enum data
     * @param enumName
     *            string contains name which will be assigned to enumeration
     *            builder
     * @param typeBuilder
     *            GeneratedTypeBuilder to which will be enum builder assigned
     * @param module
     *            Module in which type should be generated
     * @return enumeration builder which contains data from
     *         <code>enumTypeDef</code>
     */
    private EnumBuilder resolveInnerEnumFromTypeDefinition(final EnumTypeDefinition enumTypeDef, final QName enumName,
            final GeneratedTypeBuilder typeBuilder, Module module) {
        if ((enumTypeDef != null) && (typeBuilder != null) && (enumTypeDef.getQName() != null)
                && (enumTypeDef.getQName().getLocalName() != null)) {
            final String enumerationName = BindingMapping.getClassName(enumName);
            final EnumBuilder enumBuilder = typeBuilder.addEnumeration(enumerationName);
            enumBuilder.setDescription(enumTypeDef.getDescription());
            enumBuilder.updateEnumPairsFromEnumTypeDef(enumTypeDef);
            ModuleContext ctx = genCtx.get(module);
            ctx.addInnerTypedefType(enumTypeDef.getPath(), enumBuilder);
            return enumBuilder;
        }
        return null;
    }

    /**
     * Generates type builder for <code>module</code>.
     *
     * @param module
     *            Module which is source of package name for generated type
     *            builder
     * @param postfix
     *            string which is added to the module class name representation
     *            as suffix
     * @return instance of GeneratedTypeBuilder which represents
     *         <code>module</code>.
     * @throws IllegalArgumentException
     *             if <code>module</code> is null
     */
    private GeneratedTypeBuilder moduleTypeBuilder(final Module module, final String postfix) {
        checkArgument(module != null, "Module reference cannot be NULL.");
        final String packageName = BindingMapping.getRootPackageName(module.getQNameModule());
        final String moduleName = BindingMapping.getClassName(module.getName()) + postfix;

        final GeneratedTypeBuilderImpl moduleBuilder = new GeneratedTypeBuilderImpl(packageName, moduleName);
        moduleBuilder.setDescription(createDescription(module));
        moduleBuilder.setReference(module.getReference());
        moduleBuilder.setModuleName(moduleName);

        return moduleBuilder;
    }

    /**
     * Converts <code>augSchema</code> to list of <code>Type</code> which
     * contains generated type for augmentation. In addition there are also
     * generated types for all containers, list and choices which are child of
     * <code>augSchema</code> node or a generated types for cases are added if
     * augmented node is choice.
     *
     * @param augmentPackageName
     *            string with the name of the package to which the augmentation
     *            belongs
     * @param augSchema
     *            AugmentationSchema which is contains data about augmentation
     *            (target path, childs...)
     * @param module
     *            current module
     * @param parentUsesNode
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>augmentPackageName</code> equals null</li>
     *             <li>if <code>augSchema</code> equals null</li>
     *             </ul>
     * @throws IllegalStateException
     *             if augment target path is null
     */
    private void augmentationToGenTypes(final String augmentPackageName, final AugmentationSchema augSchema,
            final Module module) {
        checkArgument(augmentPackageName != null, "Package Name cannot be NULL.");
        checkArgument(augSchema != null, "Augmentation Schema cannot be NULL.");
        checkState(augSchema.getTargetPath() != null,
                "Augmentation Schema does not contain Target Path (Target Path is NULL).");

        processUsesAugments(augSchema, module);
        final SchemaPath targetPath = augSchema.getTargetPath();
        SchemaNode targetSchemaNode = null;

        targetSchemaNode = findDataSchemaNode(schemaContext, targetPath);
        if (targetSchemaNode instanceof DataSchemaNode && ((DataSchemaNode) targetSchemaNode).isAddedByUses()) {
            if (targetSchemaNode instanceof DerivableSchemaNode) {
                targetSchemaNode = ((DerivableSchemaNode) targetSchemaNode).getOriginal().orNull();
            }
            if (targetSchemaNode == null) {
                throw new IllegalStateException("Failed to find target node from grouping in augmentation " + augSchema
                        + " in module " + module.getName());
            }
        }
        if (targetSchemaNode == null) {
            throw new IllegalArgumentException("augment target not found: " + targetPath);
        }

        GeneratedTypeBuilder targetTypeBuilder = findChildNodeByPath(targetSchemaNode.getPath());
        if (targetTypeBuilder == null) {
            targetTypeBuilder = findCaseByPath(targetSchemaNode.getPath());
        }
        if (targetTypeBuilder == null) {
            throw new NullPointerException("Target type not yet generated: " + targetSchemaNode);
        }

        if (!(targetSchemaNode instanceof ChoiceSchemaNode)) {
            final String packageName = augmentPackageName;
            final Type targetType = new ReferencedTypeImpl(targetTypeBuilder.getPackageName(),
                    targetTypeBuilder.getName());
            addRawAugmentGenTypeDefinition(module, packageName, augmentPackageName, targetType, augSchema);

        } else {
            generateTypesFromAugmentedChoiceCases(module, augmentPackageName, targetTypeBuilder.toInstance(),
                    (ChoiceSchemaNode) targetSchemaNode, augSchema.getChildNodes());
        }
    }

    private void usesAugmentationToGenTypes(final String augmentPackageName, final AugmentationSchema augSchema,
            final Module module, final UsesNode usesNode, final DataNodeContainer usesNodeParent) {
        checkArgument(augmentPackageName != null, "Package Name cannot be NULL.");
        checkArgument(augSchema != null, "Augmentation Schema cannot be NULL.");
        checkState(augSchema.getTargetPath() != null,
                "Augmentation Schema does not contain Target Path (Target Path is NULL).");

        processUsesAugments(augSchema, module);
        final SchemaPath targetPath = augSchema.getTargetPath();
        final SchemaNode targetSchemaNode = findOriginalTargetFromGrouping(targetPath, usesNode);
        if (targetSchemaNode == null) {
            throw new IllegalArgumentException("augment target not found: " + targetPath);
        }

        GeneratedTypeBuilder targetTypeBuilder = findChildNodeByPath(targetSchemaNode.getPath());
        if (targetTypeBuilder == null) {
            targetTypeBuilder = findCaseByPath(targetSchemaNode.getPath());
        }
        if (targetTypeBuilder == null) {
            throw new NullPointerException("Target type not yet generated: " + targetSchemaNode);
        }

        if (!(targetSchemaNode instanceof ChoiceSchemaNode)) {
            String packageName = augmentPackageName;
            if (usesNodeParent instanceof SchemaNode) {
                packageName = packageNameForGeneratedType(augmentPackageName, ((SchemaNode) usesNodeParent).getPath(),
                        true);
            }
            addRawAugmentGenTypeDefinition(module, packageName, augmentPackageName, targetTypeBuilder.toInstance(),
                    augSchema);
        } else {
            generateTypesFromAugmentedChoiceCases(module, augmentPackageName, targetTypeBuilder.toInstance(),
                    (ChoiceSchemaNode) targetSchemaNode, augSchema.getChildNodes());
        }
    }

    /**
     * Convenient method to find node added by uses statement.
     *
     * @param targetPath
     *            node path
     * @param parentUsesNode
     *            parent of uses node
     * @return node from its original location in grouping
     */
    private DataSchemaNode findOriginalTargetFromGrouping(final SchemaPath targetPath, final UsesNode parentUsesNode) {
        final SchemaNode targetGrouping = findNodeInSchemaContext(schemaContext, parentUsesNode.getGroupingPath()
                .getPathFromRoot());
        if (!(targetGrouping instanceof GroupingDefinition)) {
            throw new IllegalArgumentException("Failed to generate code for augment in " + parentUsesNode);
        }

        final GroupingDefinition grouping = (GroupingDefinition) targetGrouping;
        SchemaNode result = grouping;
        for (final QName node : targetPath.getPathFromRoot()) {
            // finding by local name is valid, grouping cannot contain nodes
            // with same name and different namespace
            if (result instanceof DataNodeContainer) {
                result = ((DataNodeContainer) result).getDataChildByName(node.getLocalName());
            } else if (result instanceof ChoiceSchemaNode) {
                result = ((ChoiceSchemaNode) result).getCaseNodeByName(node.getLocalName());
            }
        }
        if (result == null) {
            return null;
        }

        boolean fromUses = ((DataSchemaNode) result).isAddedByUses();
        final Iterator<UsesNode> groupingUses = grouping.getUses().iterator();
        while (groupingUses.hasNext() && fromUses) {
            result = findOriginalTargetFromGrouping(targetPath, groupingUses.next());
            if (result != null) {
                fromUses = ((DataSchemaNode) result).isAddedByUses();
            }
        }
        if (fromUses) {
            // this indicates invalid yang and thus possible bug in code because
            // invalid yang should be already spotted by parser
            throw new IllegalStateException("Failed to generate code for augment in " + parentUsesNode);
        }

        return (DataSchemaNode) result;
    }

    /**
     * Returns a generated type builder for an augmentation.
     *
     * The name of the type builder is equal to the name of augmented node with
     * serial number as suffix.
     *
     * @param module
     *            current module
     * @param augmentPackageName
     *            string with contains the package name to which the augment
     *            belongs
     * @param basePackageName
     *            string with the package name to which the augmented node
     *            belongs
     * @param targetTypeRef
     *            target type
     * @param augSchema
     *            augmentation schema which contains data about the child nodes
     *            and uses of augment
     * @return generated type builder for augment
     */
    private GeneratedTypeBuilder addRawAugmentGenTypeDefinition(final Module module, final String augmentPackageName,
            final String basePackageName, final Type targetTypeRef, final AugmentationSchema augSchema) {
        Map<String, GeneratedTypeBuilder> augmentBuilders = genTypeBuilders.get(augmentPackageName);
        if (augmentBuilders == null) {
            augmentBuilders = new HashMap<>();
            genTypeBuilders.put(augmentPackageName, augmentBuilders);
        }
        final String augIdentifier = getAugmentIdentifier(augSchema.getUnknownSchemaNodes());

        String augTypeName;
        if (augIdentifier != null) {
            augTypeName = BindingMapping.getClassName(augIdentifier);
        } else {
            augTypeName = augGenTypeName(augmentBuilders, targetTypeRef.getName());
        }

        final GeneratedTypeBuilder augTypeBuilder = new GeneratedTypeBuilderImpl(augmentPackageName, augTypeName);

        augTypeBuilder.addImplementsType(DATA_OBJECT);
        augTypeBuilder.addImplementsType(Types.augmentationTypeFor(targetTypeRef));
        addImplementedInterfaceFromUses(augSchema, augTypeBuilder);

        augSchemaNodeToMethods(module, basePackageName, augTypeBuilder, augTypeBuilder, augSchema.getChildNodes());
        augmentBuilders.put(augTypeName, augTypeBuilder);

        if(!augSchema.getChildNodes().isEmpty()) {
            genCtx.get(module).addTargetToAugmentation(targetTypeRef, augTypeBuilder);
            genCtx.get(module).addTypeToAugmentation(augTypeBuilder, augSchema);

        }
        genCtx.get(module).addAugmentType(augTypeBuilder);
        return augTypeBuilder;
    }

    /**
     *
     * @param unknownSchemaNodes
     * @return nodeParameter of UnknownSchemaNode
     */
    private String getAugmentIdentifier(final List<UnknownSchemaNode> unknownSchemaNodes) {
        for (final UnknownSchemaNode unknownSchemaNode : unknownSchemaNodes) {
            final QName nodeType = unknownSchemaNode.getNodeType();
            if (AUGMENT_IDENTIFIER_NAME.equals(nodeType.getLocalName())
                    && YANG_EXT_NAMESPACE.equals(nodeType.getNamespace().toString())) {
                return unknownSchemaNode.getNodeParameter();
            }
        }
        return null;
    }

    /**
     * Returns first unique name for the augment generated type builder. The
     * generated type builder name for augment consists from name of augmented
     * node and serial number of its augmentation.
     *
     * @param builders
     *            map of builders which were created in the package to which the
     *            augmentation belongs
     * @param genTypeName
     *            string with name of augmented node
     * @return string with unique name for augmentation builder
     */
    private String augGenTypeName(final Map<String, GeneratedTypeBuilder> builders, final String genTypeName) {
        int index = 1;
        if (builders != null) {
            while (builders.containsKey(genTypeName + index)) {
                index = index + 1;
            }
        }
        return genTypeName + index;
    }

    /**
     * Adds the methods to <code>typeBuilder</code> which represent subnodes of
     * node for which <code>typeBuilder</code> was created.
     *
     * The subnodes aren't mapped to the methods if they are part of grouping or
     * augment (in this case are already part of them).
     *
     * @param module
     *            current module
     * @param basePackageName
     *            string contains the module package name
     * @param parent
     *            generated type builder which represents any node. The subnodes
     *            of this node are added to the <code>typeBuilder</code> as
     *            methods. The subnode can be of type leaf, leaf-list, list,
     *            container, choice.
     * @param childOf
     *            parent type
     * @param schemaNodes
     *            set of data schema nodes which are the children of the node
     *            for which <code>typeBuilder</code> was created
     * @return generated type builder which is the same builder as input
     *         parameter. The getter methods (representing child nodes) could be
     *         added to it.
     */
    private GeneratedTypeBuilder resolveDataSchemaNodes(final Module module, final String basePackageName,
            final GeneratedTypeBuilder parent, final GeneratedTypeBuilder childOf, final Iterable<DataSchemaNode> schemaNodes) {
        if (schemaNodes != null && parent != null) {
            for (final DataSchemaNode schemaNode : schemaNodes) {
                if (!schemaNode.isAugmenting() && !schemaNode.isAddedByUses()) {
                    addSchemaNodeToBuilderAsMethod(basePackageName, schemaNode, parent, childOf, module);
                }
            }
        }
        return parent;
    }

    /**
     * Adds the methods to <code>typeBuilder</code> what represents subnodes of
     * node for which <code>typeBuilder</code> was created.
     *
     * @param module
     *            current module
     * @param basePackageName
     *            string contains the module package name
     * @param typeBuilder
     *            generated type builder which represents any node. The subnodes
     *            of this node are added to the <code>typeBuilder</code> as
     *            methods. The subnode can be of type leaf, leaf-list, list,
     *            container, choice.
     * @param childOf
     *            parent type
     * @param schemaNodes
     *            set of data schema nodes which are the children of the node
     *            for which <code>typeBuilder</code> was created
     * @return generated type builder which is the same object as the input
     *         parameter <code>typeBuilder</code>. The getter method could be
     *         added to it.
     */
    private GeneratedTypeBuilder augSchemaNodeToMethods(final Module module, final String basePackageName,
            final GeneratedTypeBuilder typeBuilder, final GeneratedTypeBuilder childOf,
            final Iterable<DataSchemaNode> schemaNodes) {
        if ((schemaNodes != null) && (typeBuilder != null)) {
            for (final DataSchemaNode schemaNode : schemaNodes) {
                if (!schemaNode.isAugmenting()) {
                    addSchemaNodeToBuilderAsMethod(basePackageName, schemaNode, typeBuilder, childOf, module);
                }
            }
        }
        return typeBuilder;
    }

    /**
     * Adds to <code>typeBuilder</code> a method which is derived from
     * <code>schemaNode</code>.
     *
     * @param basePackageName
     *            string with the module package name
     * @param node
     *            data schema node which is added to <code>typeBuilder</code> as
     *            a method
     * @param typeBuilder
     *            generated type builder to which is <code>schemaNode</code>
     *            added as a method.
     * @param childOf
     *            parent type
     * @param module
     *            current module
     */
    private void addSchemaNodeToBuilderAsMethod(final String basePackageName, final DataSchemaNode node,
            final GeneratedTypeBuilder typeBuilder, final GeneratedTypeBuilder childOf, final Module module) {
        if (node != null && typeBuilder != null) {
            if (node instanceof LeafSchemaNode) {
                resolveLeafSchemaNodeAsMethod(typeBuilder, (LeafSchemaNode) node, module);
            } else if (node instanceof LeafListSchemaNode) {
                resolveLeafListSchemaNode(typeBuilder, (LeafListSchemaNode) node,module);
            } else if (node instanceof ContainerSchemaNode) {
                containerToGenType(module, basePackageName, typeBuilder, childOf, (ContainerSchemaNode) node);
            } else if (node instanceof ListSchemaNode) {
                listToGenType(module, basePackageName, typeBuilder, childOf, (ListSchemaNode) node);
            } else if (node instanceof ChoiceSchemaNode) {
                choiceToGeneratedType(module, basePackageName, typeBuilder, (ChoiceSchemaNode) node);
            } else {
                // TODO: anyxml not yet supported
                LOG.debug("Unable to add schema node {} as method in {}: unsupported type of node.", node.getClass(),
                        typeBuilder.getFullyQualifiedName());
            }
        }
    }

    /**
     * Converts <code>choiceNode</code> to the list of generated types for
     * choice and its cases.
     *
     * The package names for choice and for its cases are created as
     * concatenation of the module package (<code>basePackageName</code>) and
     * names of all parents node.
     *
     * @param module
     *            current module
     * @param basePackageName
     *            string with the module package name
     * @param parent
     *            parent type
     * @param choiceNode
     *            choice node which is mapped to generated type. Also child
     *            nodes - cases are mapped to generated types.
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>basePackageName</code> is null</li>
     *             <li>if <code>choiceNode</code> is null</li>
     *             </ul>
     */
    private void choiceToGeneratedType(final Module module, final String basePackageName,
            final GeneratedTypeBuilder parent, final ChoiceSchemaNode choiceNode) {
        checkArgument(basePackageName != null, "Base Package Name cannot be NULL.");
        checkArgument(choiceNode != null, "Choice Schema Node cannot be NULL.");

        if (!choiceNode.isAddedByUses()) {
            final String packageName = packageNameForGeneratedType(basePackageName, choiceNode.getPath());
            final GeneratedTypeBuilder choiceTypeBuilder = addRawInterfaceDefinition(packageName, choiceNode);
            constructGetter(parent, choiceNode.getQName().getLocalName(), choiceNode.getDescription(),
                    choiceTypeBuilder);
            choiceTypeBuilder.addImplementsType(typeForClass(DataContainer.class));
            genCtx.get(module).addChildNodeType(choiceNode, choiceTypeBuilder);
            generateTypesFromChoiceCases(module, basePackageName, choiceTypeBuilder.toInstance(), choiceNode);
        }
    }

    /**
     * Converts <code>caseNodes</code> set to list of corresponding generated
     * types.
     *
     * For every <i>case</i> which isn't added through augment or <i>uses</i> is
     * created generated type builder. The package names for the builder is
     * created as concatenation of the module package (
     * <code>basePackageName</code>) and names of all parents nodes of the
     * concrete <i>case</i>. There is also relation "<i>implements type</i>"
     * between every case builder and <i>choice</i> type
     *
     * @param module
     *            current module
     * @param basePackageName
     *            string with the module package name
     * @param refChoiceType
     *            type which represents superior <i>case</i>
     * @param choiceNode
     *            choice case node which is mapped to generated type
     * @return list of generated types for <code>caseNodes</code>.
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>basePackageName</code> equals null</li>
     *             <li>if <code>refChoiceType</code> equals null</li>
     *             <li>if <code>caseNodes</code> equals null</li>
     *             </ul>
     */
    private void generateTypesFromChoiceCases(final Module module, final String basePackageName,
            final Type refChoiceType, final ChoiceSchemaNode choiceNode) {
        checkArgument(basePackageName != null, "Base Package Name cannot be NULL.");
        checkArgument(refChoiceType != null, "Referenced Choice Type cannot be NULL.");
        checkArgument(choiceNode != null, "ChoiceNode cannot be NULL.");

        final Set<ChoiceCaseNode> caseNodes = choiceNode.getCases();
        if (caseNodes == null) {
            return;
        }

        for (final ChoiceCaseNode caseNode : caseNodes) {
            if (caseNode != null && !caseNode.isAddedByUses() && !caseNode.isAugmenting()) {
                final String packageName = packageNameForGeneratedType(basePackageName, caseNode.getPath());
                final GeneratedTypeBuilder caseTypeBuilder = addDefaultInterfaceDefinition(packageName, caseNode, module);
                caseTypeBuilder.addImplementsType(refChoiceType);
                genCtx.get(module).addCaseType(caseNode.getPath(), caseTypeBuilder);
                genCtx.get(module).addChoiceToCaseMapping(refChoiceType, caseTypeBuilder, caseNode);
                final Iterable<DataSchemaNode> caseChildNodes = caseNode.getChildNodes();
                if (caseChildNodes != null) {
                    final SchemaPath choiceNodeParentPath = choiceNode.getPath().getParent();

                    if (!Iterables.isEmpty(choiceNodeParentPath.getPathFromRoot())) {
                        SchemaNode parent = findDataSchemaNode(schemaContext, choiceNodeParentPath);

                        if (parent instanceof AugmentationSchema) {
                            final AugmentationSchema augSchema = (AugmentationSchema) parent;
                            final SchemaPath targetPath = augSchema.getTargetPath();
                            SchemaNode targetSchemaNode = findDataSchemaNode(schemaContext, targetPath);
                            if (targetSchemaNode instanceof DataSchemaNode
                                    && ((DataSchemaNode) targetSchemaNode).isAddedByUses()) {
                                if (targetSchemaNode instanceof DerivableSchemaNode) {
                                    targetSchemaNode = ((DerivableSchemaNode) targetSchemaNode).getOriginal().orNull();
                                }
                                if (targetSchemaNode == null) {
                                    throw new IllegalStateException(
                                            "Failed to find target node from grouping for augmentation " + augSchema
                                                    + " in module " + module.getName());
                                }
                            }
                            parent = targetSchemaNode;
                        }

                        Preconditions.checkState(parent != null, "Could not find Choice node parent %s",
                                choiceNodeParentPath);
                        GeneratedTypeBuilder childOfType = findChildNodeByPath(parent.getPath());
                        if (childOfType == null) {
                            childOfType = findGroupingByPath(parent.getPath());
                        }
                        resolveDataSchemaNodes(module, basePackageName, caseTypeBuilder, childOfType, caseChildNodes);
                    } else
                        resolveDataSchemaNodes(module, basePackageName, caseTypeBuilder, moduleToDataType(module),
                                caseChildNodes);
               }
            }
            processUsesAugments(caseNode, module);
        }
    }

    /**
     * Generates list of generated types for all the cases of a choice which are
     * added to the choice through the augment.
     *
     * @param module
     *            current module
     * @param basePackageName
     *            string contains name of package to which augment belongs. If
     *            an augmented choice is from an other package (pcg1) than an
     *            augmenting choice (pcg2) then case's of the augmenting choice
     *            will belong to pcg2.
     * @param targetType
     *            Type which represents target choice
     * @param targetNode
     *            node which represents target choice
     * @param augmentedNodes
     *            set of choice case nodes for which is checked if are/aren't
     *            added to choice through augmentation
     * @return list of generated types which represents augmented cases of
     *         choice <code>refChoiceType</code>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>basePackageName</code> is null</li>
     *             <li>if <code>targetType</code> is null</li>
     *             <li>if <code>augmentedNodes</code> is null</li>
     *             </ul>
     */
    private void generateTypesFromAugmentedChoiceCases(final Module module, final String basePackageName,
            final Type targetType, final ChoiceSchemaNode targetNode, final Iterable<DataSchemaNode> augmentedNodes) {
        checkArgument(basePackageName != null, "Base Package Name cannot be NULL.");
        checkArgument(targetType != null, "Referenced Choice Type cannot be NULL.");
        checkArgument(augmentedNodes != null, "Set of Choice Case Nodes cannot be NULL.");

        for (final DataSchemaNode caseNode : augmentedNodes) {
            if (caseNode != null) {
                final String packageName = packageNameForGeneratedType(basePackageName, caseNode.getPath());
                final GeneratedTypeBuilder caseTypeBuilder = addDefaultInterfaceDefinition(packageName, caseNode, module);
                caseTypeBuilder.addImplementsType(targetType);

                SchemaNode parent = null;
                final SchemaPath nodeSp = targetNode.getPath();
                parent = findDataSchemaNode(schemaContext, nodeSp.getParent());

                GeneratedTypeBuilder childOfType = null;
                if (parent instanceof Module) {
                    childOfType = genCtx.get(parent).getModuleNode();
                } else if (parent instanceof ChoiceCaseNode) {
                    childOfType = findCaseByPath(parent.getPath());
                } else if (parent instanceof DataSchemaNode || parent instanceof NotificationDefinition) {
                    childOfType = findChildNodeByPath(parent.getPath());
                } else if (parent instanceof GroupingDefinition) {
                    childOfType = findGroupingByPath(parent.getPath());
                }

                if (childOfType == null) {
                    throw new IllegalArgumentException("Failed to find parent type of choice " + targetNode);
                }

                ChoiceCaseNode node = null;
                if (caseNode instanceof ChoiceCaseNode) {
                    node = (ChoiceCaseNode) caseNode;
                } else {
                    node = targetNode.getCaseNodeByName(caseNode.getQName().getLocalName());
                }
                final Iterable<DataSchemaNode> childNodes = node.getChildNodes();
                if (childNodes != null) {
                    resolveDataSchemaNodes(module, basePackageName, caseTypeBuilder, childOfType, childNodes);
                }
                genCtx.get(module).addCaseType(caseNode.getPath(), caseTypeBuilder);
                genCtx.get(module).addChoiceToCaseMapping(targetType, caseTypeBuilder, node);
            }
        }
    }

    /**
     * Converts <code>leaf</code> to the getter method which is added to
     * <code>typeBuilder</code>.
     *
     * @param typeBuilder
     *            generated type builder to which is added getter method as
     *            <code>leaf</code> mapping
     * @param leaf
     *            leaf schema node which is mapped as getter method which is
     *            added to <code>typeBuilder</code>
     * @param module
     *            Module in which type was defined
     * @return boolean value
     *         <ul>
     *         <li>false - if <code>leaf</code> or <code>typeBuilder</code> are
     *         null</li>
     *         <li>true - in other cases</li>
     *         </ul>
     */
    private Type resolveLeafSchemaNodeAsMethod(final GeneratedTypeBuilder typeBuilder, final LeafSchemaNode leaf, Module module) {
        Type returnType = null;
        if ((leaf != null) && (typeBuilder != null)) {
            final String leafName = leaf.getQName().getLocalName();
            String leafDesc = leaf.getDescription();
            if (leafDesc == null) {
                leafDesc = "";
            }

            final Module parentModule = findParentModule(schemaContext, leaf);
            if (leafName != null && !leaf.isAddedByUses()) {
                final TypeDefinition<?> typeDef = leaf.getType();

                GeneratedTOBuilder genTOBuilder;
                if (typeDef instanceof EnumTypeDefinition) {
                    returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, leaf);
                    final EnumTypeDefinition enumTypeDef = (EnumTypeDefinition) typeDef;
                    final EnumBuilder enumBuilder = resolveInnerEnumFromTypeDefinition(enumTypeDef, leaf.getQName(),
                            typeBuilder,module);

                    if (enumBuilder != null) {
                        returnType = enumBuilder.toInstance(typeBuilder);
                    }
                    ((TypeProviderImpl) typeProvider).putReferencedType(leaf.getPath(), returnType);
                } else if (typeDef instanceof UnionType) {
                    genTOBuilder = addTOToTypeBuilder(typeDef, typeBuilder, leaf, parentModule);
                    if (genTOBuilder != null) {
                        returnType = createReturnTypeForUnion(genTOBuilder, typeDef, typeBuilder, parentModule);
                    }
                } else if (typeDef instanceof BitsTypeDefinition) {
                    genTOBuilder = addTOToTypeBuilder(typeDef, typeBuilder, leaf, parentModule);
                    if (genTOBuilder != null) {
                        returnType = genTOBuilder.toInstance();
                    }
                } else {
                    final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(typeDef);
                    returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, leaf, restrictions);
                }
                if (returnType != null) {
                    final MethodSignatureBuilder getter = constructGetter(typeBuilder, leafName, leafDesc, returnType);
                    processContextRefExtension(leaf, getter, parentModule);
                }
            }
        }
        return returnType;
    }

    private void processContextRefExtension(final LeafSchemaNode leaf, final MethodSignatureBuilder getter,
            final Module module) {
        for (final UnknownSchemaNode node : leaf.getUnknownSchemaNodes()) {
            final QName nodeType = node.getNodeType();
            if ("context-reference".equals(nodeType.getLocalName())) {
                final String nodeParam = node.getNodeParameter();
                IdentitySchemaNode identity = null;
                String basePackageName = null;
                final Iterable<String> splittedElement = COLON_SPLITTER.split(nodeParam);
                final Iterator<String> iterator = splittedElement.iterator();
                final int length = Iterables.size(splittedElement);
                if (length == 1) {
                    identity = findIdentityByName(module.getIdentities(), iterator.next());
                    basePackageName = BindingMapping.getRootPackageName(module.getQNameModule());
                } else if (length == 2) {
                    final String prefix = iterator.next();
                    final Module dependentModule = findModuleFromImports(module.getImports(), prefix);
                    if (dependentModule == null) {
                        throw new IllegalArgumentException("Failed to process context-reference: unknown prefix "
                                + prefix);
                    }
                    identity = findIdentityByName(dependentModule.getIdentities(), iterator.next());
                    basePackageName = BindingMapping.getRootPackageName(dependentModule.getQNameModule());
                } else {
                    throw new IllegalArgumentException("Failed to process context-reference: unknown identity "
                            + nodeParam);
                }
                if (identity == null) {
                    throw new IllegalArgumentException("Failed to process context-reference: unknown identity "
                            + nodeParam);
                }

                final Class<RoutingContext> clazz = RoutingContext.class;
                final AnnotationTypeBuilder rc = getter.addAnnotation(clazz.getPackage().getName(),
                        clazz.getSimpleName());
                final String packageName = packageNameForGeneratedType(basePackageName, identity.getPath());
                final String genTypeName = BindingMapping.getClassName(identity.getQName().getLocalName());
                rc.addParameter("value", packageName + "." + genTypeName + ".class");
            }
        }
    }

    private IdentitySchemaNode findIdentityByName(final Set<IdentitySchemaNode> identities, final String name) {
        for (final IdentitySchemaNode id : identities) {
            if (id.getQName().getLocalName().equals(name)) {
                return id;
            }
        }
        return null;
    }

    private Module findModuleFromImports(final Set<ModuleImport> imports, final String prefix) {
        for (final ModuleImport imp : imports) {
            if (imp.getPrefix().equals(prefix)) {
                return schemaContext.findModuleByName(imp.getModuleName(), imp.getRevision());
            }
        }
        return null;
    }

    private boolean resolveLeafSchemaNodeAsProperty(final GeneratedTOBuilder toBuilder, final LeafSchemaNode leaf,
            final boolean isReadOnly, final Module module) {
        if ((leaf != null) && (toBuilder != null)) {
            final String leafName = leaf.getQName().getLocalName();
            String leafDesc = leaf.getDescription();
            if (leafDesc == null) {
                leafDesc = "";
            }
            Type returnType = null;
            final TypeDefinition<?> typeDef = leaf.getType();
            if (typeDef instanceof UnionTypeDefinition) {
                // GeneratedType for this type definition should be already
                // created
                final QName qname = typeDef.getQName();
                final Module unionModule = schemaContext.findModuleByNamespaceAndRevision(qname.getNamespace(),
                        qname.getRevision());
                final ModuleContext mc = genCtx.get(unionModule);
                returnType = mc.getTypedefs().get(typeDef.getPath());
            } else if (typeDef instanceof EnumTypeDefinition && BaseTypes.ENUMERATION_QNAME.equals(typeDef.getQName())) {
                // Annonymous enumeration (already generated, since it is inherited via uses).
                LeafSchemaNode originalLeaf = (LeafSchemaNode) SchemaNodeUtils.getRootOriginalIfPossible(leaf);
                QName qname = originalLeaf.getQName();
                final Module enumModule =  schemaContext.findModuleByNamespaceAndRevision(qname.getNamespace(),
                        qname.getRevision());
                returnType = genCtx.get(enumModule).getInnerType(originalLeaf.getType().getPath());
            } else {
                returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, leaf);
            }
            return resolveLeafSchemaNodeAsProperty(toBuilder, leaf, returnType, isReadOnly);
        }
        return false;
    }

    /**
     * Converts <code>leaf</code> schema node to property of generated TO
     * builder.
     *
     * @param toBuilder
     *            generated TO builder to which is <code>leaf</code> added as
     *            property
     * @param leaf
     *            leaf schema node which is added to <code>toBuilder</code> as
     *            property
     * @param returnType
     *            property type
     * @param isReadOnly
     *            boolean value which says if leaf property is|isn't read only
     * @return boolean value
     *         <ul>
     *         <li>false - if <code>leaf</code>, <code>toBuilder</code> or leaf
     *         name equals null or if leaf is added by <i>uses</i>.</li>
     *         <li>true - other cases</li>
     *         </ul>
     */
    private boolean resolveLeafSchemaNodeAsProperty(final GeneratedTOBuilder toBuilder, final LeafSchemaNode leaf,
            final Type returnType, final boolean isReadOnly) {
        if (returnType == null) {
            return false;
        }
        final String leafName = leaf.getQName().getLocalName();
        final String leafDesc = leaf.getDescription();
        final GeneratedPropertyBuilder propBuilder = toBuilder.addProperty(BindingMapping.getPropertyName(leafName));
        propBuilder.setReadOnly(isReadOnly);
        propBuilder.setReturnType(returnType);
        propBuilder.setComment(leafDesc);
        toBuilder.addEqualsIdentity(propBuilder);
        toBuilder.addHashIdentity(propBuilder);
        toBuilder.addToStringProperty(propBuilder);
        return true;
    }

    /**
     * Converts <code>node</code> leaf list schema node to getter method of
     * <code>typeBuilder</code>.
     *
     * @param typeBuilder
     *            generated type builder to which is <code>node</code> added as
     *            getter method
     * @param node
     *            leaf list schema node which is added to
     *            <code>typeBuilder</code> as getter method
     * @param module
     * @return boolean value
     *         <ul>
     *         <li>true - if <code>node</code>, <code>typeBuilder</code>,
     *         nodeName equal null or <code>node</code> is added by <i>uses</i></li>
     *         <li>false - other cases</li>
     *         </ul>
     */
    private boolean resolveLeafListSchemaNode(final GeneratedTypeBuilder typeBuilder, final LeafListSchemaNode node, Module module) {
        if ((node != null) && (typeBuilder != null)) {
            final QName nodeName = node.getQName();

            if (nodeName != null && !node.isAddedByUses()) {
                final TypeDefinition<?> typeDef = node.getType();
                final Module parentModule = findParentModule(schemaContext, node);

                Type returnType = null;
                if (typeDef instanceof EnumTypeDefinition) {
                    returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, node);
                    final EnumTypeDefinition enumTypeDef = (EnumTypeDefinition) typeDef;
                    final EnumBuilder enumBuilder = resolveInnerEnumFromTypeDefinition(enumTypeDef, nodeName,
                            typeBuilder,module);
                    returnType = new ReferencedTypeImpl(enumBuilder.getPackageName(), enumBuilder.getName());
                    ((TypeProviderImpl) typeProvider).putReferencedType(node.getPath(), returnType);
                } else if (typeDef instanceof UnionType) {
                    final GeneratedTOBuilder genTOBuilder = addTOToTypeBuilder(typeDef, typeBuilder, node, parentModule);
                    if (genTOBuilder != null) {
                        returnType = createReturnTypeForUnion(genTOBuilder, typeDef, typeBuilder, parentModule);
                    }
                } else if (typeDef instanceof BitsTypeDefinition) {
                    final GeneratedTOBuilder genTOBuilder = addTOToTypeBuilder(typeDef, typeBuilder, node, parentModule);
                    returnType = genTOBuilder.toInstance();
                } else {
                    final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(typeDef);
                    returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, node, restrictions);
                }

                final ParameterizedType listType = Types.listTypeFor(returnType);
                constructGetter(typeBuilder, nodeName.getLocalName(), node.getDescription(), listType);
                return true;
            }
        }
        return false;
    }

    private Type createReturnTypeForUnion(final GeneratedTOBuilder genTOBuilder, final TypeDefinition<?> typeDef,
            final GeneratedTypeBuilder typeBuilder, final Module parentModule) {
        final GeneratedTOBuilderImpl returnType = new GeneratedTOBuilderImpl(genTOBuilder.getPackageName(),
                genTOBuilder.getName());

        returnType.setDescription(typeDef.getDescription());
        returnType.setReference(typeDef.getReference());
        returnType.setSchemaPath(typeDef.getPath().getPathFromRoot());
        returnType.setModuleName(parentModule.getName());

        genTOBuilder.setTypedef(true);
        genTOBuilder.setIsUnion(true);
        ((TypeProviderImpl) typeProvider).addUnitsToGenTO(genTOBuilder, typeDef.getUnits());



        final GeneratedTOBuilder unionBuilder = createUnionBuilder(genTOBuilder,typeBuilder);


        final MethodSignatureBuilder method = unionBuilder.addMethod("getDefaultInstance");
        method.setReturnType(returnType);
        method.addParameter(Types.STRING, "defaultValue");
        method.setAccessModifier(AccessModifier.PUBLIC);
        method.setStatic(true);

        final Set<Type> types = ((TypeProviderImpl) typeProvider).getAdditionalTypes().get(parentModule);
        if (types == null) {
            ((TypeProviderImpl) typeProvider).getAdditionalTypes().put(parentModule,
                    Sets.<Type> newHashSet(unionBuilder.toInstance()));
        } else {
            types.add(unionBuilder.toInstance());
        }
        return returnType.toInstance();
    }

    private GeneratedTOBuilder createUnionBuilder(final GeneratedTOBuilder genTOBuilder, final GeneratedTypeBuilder typeBuilder) {

        final String outerCls = Types.getOuterClassName(genTOBuilder);
        final StringBuilder name;
        if(outerCls != null) {
            name = new StringBuilder(outerCls);
        } else {
            name = new StringBuilder();
        }
        name.append(genTOBuilder.getName());
        name.append("Builder");
        final GeneratedTOBuilderImpl unionBuilder = new GeneratedTOBuilderImpl(typeBuilder.getPackageName(),name.toString());
        unionBuilder.setIsUnionBuilder(true);
        return unionBuilder;
    }

    private GeneratedTypeBuilder addDefaultInterfaceDefinition(final String packageName, final SchemaNode schemaNode,
            final Module module) {
        return addDefaultInterfaceDefinition(packageName, schemaNode, null, module);
    }

    /**
     * Instantiates generated type builder with <code>packageName</code> and
     * <code>schemaNode</code>.
     *
     * The new builder always implements
     * {@link org.opendaylight.yangtools.yang.binding.DataObject DataObject}.<br>
     * If <code>schemaNode</code> is instance of GroupingDefinition it also
     * implements {@link org.opendaylight.yangtools.yang.binding.Augmentable
     * Augmentable}.<br>
     * If <code>schemaNode</code> is instance of
     * {@link org.opendaylight.yangtools.yang.model.api.DataNodeContainer
     * DataNodeContainer} it can also implement nodes which are specified in
     * <i>uses</i>.
     *
     * @param packageName
     *            string with the name of the package to which
     *            <code>schemaNode</code> belongs.
     * @param schemaNode
     *            schema node for which is created generated type builder
     * @param parent
     *            parent type (can be null)
     * @return generated type builder <code>schemaNode</code>
     */
    private GeneratedTypeBuilder addDefaultInterfaceDefinition(final String packageName, final SchemaNode schemaNode,
            final Type parent, final Module module) {
        final GeneratedTypeBuilder it = addRawInterfaceDefinition(packageName, schemaNode, "");
        if (parent == null) {
            it.addImplementsType(DATA_OBJECT);
        } else {
            it.addImplementsType(BindingTypes.childOf(parent));
        }
        if (!(schemaNode instanceof GroupingDefinition)) {
            it.addImplementsType(augmentable(it));
        }

        if (schemaNode instanceof DataNodeContainer) {
            groupingsToGenTypes(module, ((DataNodeContainer) schemaNode).getGroupings());
            addImplementedInterfaceFromUses((DataNodeContainer) schemaNode, it);
        }

        return it;
    }

    /**
     * Wraps the calling of the same overloaded method.
     *
     * @param packageName
     *            string with the package name to which returning generated type
     *            builder belongs
     * @param schemaNode
     *            schema node which provide data about the schema node name
     * @return generated type builder for <code>schemaNode</code>
     */
    private GeneratedTypeBuilder addRawInterfaceDefinition(final String packageName, final SchemaNode schemaNode) {
        return addRawInterfaceDefinition(packageName, schemaNode, "");
    }

    /**
     * Returns reference to generated type builder for specified
     * <code>schemaNode</code> with <code>packageName</code>.
     *
     * Firstly the generated type builder is searched in
     * {@link BindingGeneratorImpl#genTypeBuilders genTypeBuilders}. If it isn't
     * found it is created and added to <code>genTypeBuilders</code>.
     *
     * @param packageName
     *            string with the package name to which returning generated type
     *            builder belongs
     * @param schemaNode
     *            schema node which provide data about the schema node name
     * @param prefix
     *            return type name prefix
     * @return generated type builder for <code>schemaNode</code>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>schemaNode</code> is null</li>
     *             <li>if <code>packageName</code> is null</li>
     *             <li>if QName of schema node is null</li>
     *             <li>if schemaNode name is null</li>
     *             </ul>
     *
     */
    private GeneratedTypeBuilder addRawInterfaceDefinition(final String packageName, final SchemaNode schemaNode,
            final String prefix) {
        checkArgument(schemaNode != null, "Data Schema Node cannot be NULL.");
        checkArgument(packageName != null, "Package Name for Generated Type cannot be NULL.");
        checkArgument(schemaNode.getQName() != null, "QName for Data Schema Node cannot be NULL.");
        final String schemaNodeName = schemaNode.getQName().getLocalName();
        checkArgument(schemaNodeName != null, "Local Name of QName for Data Schema Node cannot be NULL.");

        String genTypeName;
        if (prefix == null) {
            genTypeName = BindingMapping.getClassName(schemaNodeName);
        } else {
            genTypeName = prefix + BindingMapping.getClassName(schemaNodeName);
        }

        // FIXME: Validation of name conflict
        final GeneratedTypeBuilderImpl newType = new GeneratedTypeBuilderImpl(packageName, genTypeName);
        final Module module = findParentModule(schemaContext, schemaNode);
        qnameConstant(newType, BindingMapping.QNAME_STATIC_FIELD_NAME, schemaNode.getQName());
        newType.addComment(schemaNode.getDescription());
        newType.setDescription(createDescription(schemaNode, newType.getFullyQualifiedName()));
        newType.setReference(schemaNode.getReference());
        newType.setSchemaPath(schemaNode.getPath().getPathFromRoot());
        newType.setModuleName(module.getName());

        if (!genTypeBuilders.containsKey(packageName)) {
            final Map<String, GeneratedTypeBuilder> builders = new HashMap<>();
            builders.put(genTypeName, newType);
            genTypeBuilders.put(packageName, builders);
        } else {
            final Map<String, GeneratedTypeBuilder> builders = genTypeBuilders.get(packageName);
            if (!builders.containsKey(genTypeName)) {
                builders.put(genTypeName, newType);
            }
        }
        return newType;
    }

    /**
     * Creates the name of the getter method name from <code>localName</code>.
     *
     * @param localName
     *            string with the name of the getter method
     * @param returnType
     *            return type
     * @return string with the name of the getter method for
     *         <code>methodName</code> in JAVA method format
     */
    public static String getterMethodName(final String localName, final Type returnType) {
        final StringBuilder method = new StringBuilder();
        if (BOOLEAN.equals(returnType)) {
            method.append("is");
        } else {
            method.append("get");
        }
        final String name = BindingMapping.toFirstUpper(BindingMapping.getPropertyName(localName));
        method.append(name);
        return method.toString();
    }

    /**
     * Created a method signature builder as part of
     * <code>interfaceBuilder</code>.
     *
     * The method signature builder is created for the getter method of
     * <code>schemaNodeName</code>. Also <code>comment</code> and
     * <code>returnType</code> information are added to the builder.
     *
     * @param interfaceBuilder
     *            generated type builder for which the getter method should be
     *            created
     * @param schemaNodeName
     *            string with schema node name. The name will be the part of the
     *            getter method name.
     * @param comment
     *            string with comment for the getter method
     * @param returnType
     *            type which represents the return type of the getter method
     * @return method signature builder which represents the getter method of
     *         <code>interfaceBuilder</code>
     */
    private MethodSignatureBuilder constructGetter(final GeneratedTypeBuilder interfaceBuilder,
            final String schemaNodeName, final String comment, final Type returnType) {
        final MethodSignatureBuilder getMethod = interfaceBuilder
                .addMethod(getterMethodName(schemaNodeName, returnType));
        getMethod.setComment(comment);
        getMethod.setReturnType(returnType);
        return getMethod;
    }

    /**
     * Adds <code>schemaNode</code> to <code>typeBuilder</code> as getter method
     * or to <code>genTOBuilder</code> as property.
     *
     * @param basePackageName
     *            string contains the module package name
     * @param schemaNode
     *            data schema node which should be added as getter method to
     *            <code>typeBuilder</code> or as a property to
     *            <code>genTOBuilder</code> if is part of the list key
     * @param typeBuilder
     *            generated type builder for the list schema node
     * @param genTOBuilder
     *            generated TO builder for the list keys
     * @param listKeys
     *            list of string which contains names of the list keys
     * @param module
     *            current module
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>schemaNode</code> equals null</li>
     *             <li>if <code>typeBuilder</code> equals null</li>
     *             </ul>
     */
    private void addSchemaNodeToListBuilders(final String basePackageName, final DataSchemaNode schemaNode,
            final GeneratedTypeBuilder typeBuilder, final GeneratedTOBuilder genTOBuilder, final List<String> listKeys,
            final Module module) {
        checkArgument(schemaNode != null, "Data Schema Node cannot be NULL.");
        checkArgument(typeBuilder != null, "Generated Type Builder cannot be NULL.");

        if (schemaNode instanceof LeafSchemaNode) {
            final LeafSchemaNode leaf = (LeafSchemaNode) schemaNode;
            final String leafName = leaf.getQName().getLocalName();
            final Type type = resolveLeafSchemaNodeAsMethod(typeBuilder, leaf,module);
            if (listKeys.contains(leafName)) {
                if (type == null) {
                    resolveLeafSchemaNodeAsProperty(genTOBuilder, leaf, true, module);
                } else {
                    resolveLeafSchemaNodeAsProperty(genTOBuilder, leaf, type, true);
                }
            }
        } else if (!schemaNode.isAddedByUses()) {
            if (schemaNode instanceof LeafListSchemaNode) {
                resolveLeafListSchemaNode(typeBuilder, (LeafListSchemaNode) schemaNode, module);
            } else if (schemaNode instanceof ContainerSchemaNode) {
                containerToGenType(module, basePackageName, typeBuilder, typeBuilder, (ContainerSchemaNode) schemaNode);
            } else if (schemaNode instanceof ChoiceSchemaNode) {
                choiceToGeneratedType(module, basePackageName, typeBuilder, (ChoiceSchemaNode) schemaNode);
            } else if (schemaNode instanceof ListSchemaNode) {
                listToGenType(module, basePackageName, typeBuilder, typeBuilder, (ListSchemaNode) schemaNode);
            }
        }
    }

    private void typeBuildersToGenTypes(final Module module, final GeneratedTypeBuilder typeBuilder,
            final GeneratedTOBuilder genTOBuilder) {
        checkArgument(typeBuilder != null, "Generated Type Builder cannot be NULL.");

        if (genTOBuilder != null) {
            final GeneratedTransferObject genTO = genTOBuilder.toInstance();
            constructGetter(typeBuilder, "key", "Returns Primary Key of Yang List Type", genTO);
            genCtx.get(module).addGeneratedTOBuilder(genTOBuilder);
        }
    }

    /**
     * Selects the names of the list keys from <code>list</code> and returns
     * them as the list of the strings
     *
     * @param list
     *            of string with names of the list keys
     * @return list of string which represents names of the list keys. If the
     *         <code>list</code> contains no keys then the empty list is
     *         returned.
     */
    private List<String> listKeys(final ListSchemaNode list) {
        final List<String> listKeys = new ArrayList<>();

        final List<QName> keyDefinition = list.getKeyDefinition();
        if (keyDefinition != null) {
            for (final QName keyDef : keyDefinition) {
                listKeys.add(keyDef.getLocalName());
            }
        }
        return listKeys;
    }

    /**
     * Generates for the <code>list</code> which contains any list keys special
     * generated TO builder.
     *
     * @param packageName
     *            string with package name to which the list belongs
     * @param list
     *            list schema node which is source of data about the list name
     * @return generated TO builder which represents the keys of the
     *         <code>list</code> or null if <code>list</code> is null or list of
     *         key definitions is null or empty.
     */
    private GeneratedTOBuilder resolveListKeyTOBuilder(final String packageName, final ListSchemaNode list) {
        GeneratedTOBuilder genTOBuilder = null;
        if ((list.getKeyDefinition() != null) && (!list.getKeyDefinition().isEmpty())) {
            final String listName = list.getQName().getLocalName() + "Key";
            final String genTOName = BindingMapping.getClassName(listName);
            genTOBuilder = new GeneratedTOBuilderImpl(packageName, genTOName);
        }
        return genTOBuilder;
    }

    /**
     * Builds generated TO builders for <code>typeDef</code> of type
     * {@link org.opendaylight.yangtools.yang.model.util.UnionType UnionType} or
     * {@link org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition
     * BitsTypeDefinition} which are also added to <code>typeBuilder</code> as
     * enclosing transfer object.
     *
     * If more then one generated TO builder is created for enclosing then all
     * of the generated TO builders are added to <code>typeBuilder</code> as
     * enclosing transfer objects.
     *
     * @param typeDef
     *            type definition which can be of type <code>UnionType</code> or
     *            <code>BitsTypeDefinition</code>
     * @param typeBuilder
     *            generated type builder to which is added generated TO created
     *            from <code>typeDef</code>
     * @param leafName
     *            string with name for generated TO builder
     * @param leaf
     * @return generated TO builder for <code>typeDef</code>
     */
    private GeneratedTOBuilder addTOToTypeBuilder(final TypeDefinition<?> typeDef,
            final GeneratedTypeBuilder typeBuilder, final DataSchemaNode leaf, final Module parentModule) {
        final String classNameFromLeaf = BindingMapping.getClassName(leaf.getQName());
        final List<GeneratedTOBuilder> genTOBuilders = new ArrayList<>();
        final String packageName = typeBuilder.getFullyQualifiedName();
        if (typeDef instanceof UnionTypeDefinition) {
            final List<GeneratedTOBuilder> types = ((TypeProviderImpl) typeProvider)
                    .provideGeneratedTOBuildersForUnionTypeDef(packageName, ((UnionTypeDefinition) typeDef),
                            classNameFromLeaf, leaf);
            genTOBuilders.addAll(types);

            GeneratedTOBuilder resultTOBuilder = null;
            if (types.isEmpty()) {
                throw new IllegalStateException("No GeneratedTOBuilder objects generated from union " + typeDef);
            }
            resultTOBuilder = types.remove(0);
            for (final GeneratedTOBuilder genTOBuilder : types) {
                resultTOBuilder.addEnclosingTransferObject(genTOBuilder);
            }

            final GeneratedPropertyBuilder genPropBuilder = resultTOBuilder.addProperty("value");
            genPropBuilder.setReturnType(Types.CHAR_ARRAY);
            resultTOBuilder.addEqualsIdentity(genPropBuilder);
            resultTOBuilder.addHashIdentity(genPropBuilder);
            resultTOBuilder.addToStringProperty(genPropBuilder);

        } else if (typeDef instanceof BitsTypeDefinition) {
            genTOBuilders.add((((TypeProviderImpl) typeProvider)).provideGeneratedTOBuilderForBitsTypeDefinition(
                    packageName, typeDef, classNameFromLeaf, parentModule.getName()));
        }
        if (genTOBuilders != null && !genTOBuilders.isEmpty()) {
            for (final GeneratedTOBuilder genTOBuilder : genTOBuilders) {
                typeBuilder.addEnclosingTransferObject(genTOBuilder);
            }
            return genTOBuilders.get(0);
        }
        return null;

    }

    /**
     * Adds the implemented types to type builder.
     *
     * The method passes through the list of <i>uses</i> in
     * {@code dataNodeContainer}. For every <i>use</i> is obtained corresponding
     * generated type from {@link BindingGeneratorImpl#allGroupings
     * allGroupings} which is added as <i>implements type</i> to
     * <code>builder</code>
     *
     * @param dataNodeContainer
     *            element which contains the list of used YANG groupings
     * @param builder
     *            builder to which are added implemented types according to
     *            <code>dataNodeContainer</code>
     * @return generated type builder with all implemented types
     */
    private GeneratedTypeBuilder addImplementedInterfaceFromUses(final DataNodeContainer dataNodeContainer,
            final GeneratedTypeBuilder builder) {
        for (final UsesNode usesNode : dataNodeContainer.getUses()) {
            if (usesNode.getGroupingPath() != null) {
                final GeneratedType genType = findGroupingByPath(usesNode.getGroupingPath()).toInstance();
                if (genType == null) {
                    throw new IllegalStateException("Grouping " + usesNode.getGroupingPath() + "is not resolved for "
                            + builder.getName());
                }

                builder.addImplementsType(genType);
                /*
                builder.addComment(genType.getDescription());
                builder.setDescription(genType.getDescription());
                builder.setModuleName(genType.getModuleName());
                builder.setReference(genType.getReference());
                builder.setSchemaPath(genType.getSchemaPath());
                */
            }
        }
        return builder;
    }

    private boolean isNullOrEmpty(final Collection<?> list) {
        return (list == null || list.isEmpty() ? true : false);
    }

    private String createDescription(final Set<? extends SchemaNode> schemaNodes, final String moduleName, final String moduleSourcePath) {
        final StringBuilder sb = new StringBuilder();

        if (!isNullOrEmpty(schemaNodes)) {
            final SchemaNode node = schemaNodes.iterator().next();

            if (node instanceof RpcDefinition) {
                sb.append("Interface for implementing the following YANG RPCs defined in module <b>" + moduleName + "</b>");
            } else if (node instanceof NotificationDefinition) {
                sb.append("Interface for receiving the following YANG notifications defined in module <b>" + moduleName + "</b>");
            }
        }
        sb.append(NEW_LINE);
        sb.append("<br>(Source path: <i>");
        sb.append(moduleSourcePath);
        sb.append("</i>):");
        sb.append(NEW_LINE);

        if (verboseClassComments) {
            sb.append("<pre>");
            sb.append(NEW_LINE);
            sb.append(YangTemplate.generateYangSnipet(schemaNodes));
            sb.append("</pre>");
            sb.append(NEW_LINE);
        }

        return replaceAllIllegalChars(sb);
    }

    private String createDescription(final SchemaNode schemaNode, final String fullyQualifiedName) {
        final StringBuilder sb = new StringBuilder();
        final String formattedDescription = YangTemplate.formatToParagraph(schemaNode.getDescription(), 0);

        if (!isNullOrEmpty(formattedDescription)) {
            sb.append(formattedDescription);
            sb.append(NEW_LINE);
        }

        if (verboseClassComments) {
            final Module module = findParentModule(schemaContext, schemaNode);
            final StringBuilder linkToBuilderClass = new StringBuilder();
            final String[] namespace = Iterables.toArray(BSDOT_SPLITTER.split(fullyQualifiedName), String.class);
            final String className = namespace[namespace.length - 1];

            if (hasBuilderClass(schemaNode)) {
                linkToBuilderClass.append(className);
                linkToBuilderClass.append("Builder");
            }

            sb.append("<p>");
            sb.append("This class represents the following YANG schema fragment defined in module <b>");
            sb.append(module.getName());
            sb.append("</b>");
            sb.append(NEW_LINE);
            sb.append("<br>(Source path: <i>");
            sb.append(module.getModuleSourcePath());
            sb.append("</i>):");
            sb.append(NEW_LINE);
            sb.append("<pre>");
            sb.append(NEW_LINE);
            sb.append(YangTemplate.generateYangSnipet(schemaNode));
            sb.append("</pre>");
            sb.append(NEW_LINE);
            sb.append("The schema path to identify an instance is");
            sb.append(NEW_LINE);
            sb.append("<i>");
            sb.append(YangTemplate.formatSchemaPath(module.getName(), schemaNode.getPath().getPathFromRoot()));
            sb.append("</i>");
            sb.append(NEW_LINE);

            if (hasBuilderClass(schemaNode)) {
                sb.append(NEW_LINE);
                sb.append("<p>To create instances of this class use " + "{@link " + linkToBuilderClass + "}.");
                sb.append(NEW_LINE);
                sb.append("@see ");
                sb.append(linkToBuilderClass);
                sb.append(NEW_LINE);
                if (schemaNode instanceof ListSchemaNode) {
                    final List<QName> keyDef = ((ListSchemaNode)schemaNode).getKeyDefinition();
                    if (keyDef != null && !keyDef.isEmpty()) {
                        sb.append("@see ");
                        sb.append(className);
                        sb.append("Key");
                    }
		    sb.append(NEW_LINE);
                }
            }
        }

        return replaceAllIllegalChars(sb);
    }

    private boolean hasBuilderClass(final SchemaNode schemaNode) {
        if (schemaNode instanceof ContainerSchemaNode || schemaNode instanceof ListSchemaNode ||
                schemaNode instanceof RpcDefinition || schemaNode instanceof NotificationDefinition) {
            return true;
        }
        return false;
    }

    private boolean isNullOrEmpty(final String string) {
        return (string == null || string.isEmpty() ? true : false);
    }

    private String createDescription(final Module module) {
        final StringBuilder sb = new StringBuilder();
        final String formattedDescription = YangTemplate.formatToParagraph(module.getDescription(), 0);

        if (!isNullOrEmpty(formattedDescription)) {
            sb.append(formattedDescription);
            sb.append(NEW_LINE);
        }

        if (verboseClassComments) {
            sb.append("<p>");
            sb.append("This class represents the following YANG schema fragment defined in module <b>");
            sb.append(module.getName());
            sb.append("</b>");
            sb.append(NEW_LINE);
            sb.append("<br>Source path: <i>");
            sb.append(module.getModuleSourcePath());
            sb.append("</i>):");
            sb.append(NEW_LINE);
            sb.append("<pre>");
            sb.append(NEW_LINE);
            sb.append(YangTemplate.generateYangSnipet(module));
            sb.append("</pre>");
        }

        return replaceAllIllegalChars(sb);
    }

    private GeneratedTypeBuilder findChildNodeByPath(final SchemaPath path) {
        for (final ModuleContext ctx : genCtx.values()) {
            final GeneratedTypeBuilder result = ctx.getChildNode(path);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private GeneratedTypeBuilder findGroupingByPath(final SchemaPath path) {
        for (final ModuleContext ctx : genCtx.values()) {
            final GeneratedTypeBuilder result = ctx.getGrouping(path);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private GeneratedTypeBuilder findCaseByPath(final SchemaPath path) {
        for (final ModuleContext ctx : genCtx.values()) {
            final GeneratedTypeBuilder result = ctx.getCase(path);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public Map<Module, ModuleContext> getModuleContexts() {
        return genCtx;
    }

    @VisibleForTesting
    static String replaceAllIllegalChars(StringBuilder stringBuilder){
        return UNICODE_CHAR_PATTERN.matcher(stringBuilder).replaceAll("\\\\\\\\u");
    }

}
