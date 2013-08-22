/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import static org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil.*;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.opendaylight.yangtools.binding.generator.util.ReferencedTypeImpl;
import org.opendaylight.yangtools.binding.generator.util.Types;
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedTOBuilderImpl;
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedTypeBuilderImpl;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.generator.spi.TypeProvider;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.EnumBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.sal.binding.yang.types.GroupingDefinitionDependencySort;
import org.opendaylight.yangtools.sal.binding.yang.types.TypeProviderImpl;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
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
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.DataNodeIterator;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.model.util.UnionType;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;

public final class BindingGeneratorImpl implements BindingGenerator {

    /**
     * Outter key represents the package name. Outter value represents map of
     * all builders in the same package. Inner key represents the schema node
     * name (in JAVA class/interface name format). Inner value represents
     * instance of builder for schema node specified in key part.
     */
    private Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders;

    /**
     * Provide methods for converting YANG types to JAVA types.
     */
    private TypeProvider typeProvider;

    /**
     * Holds reference to schema context to resolve data of augmented elemnt
     * when creating augmentation builder
     */
    private SchemaContext schemaContext;

    /**
     * Each grouping which is converted from schema node to generated type is
     * added to this map with its Schema path as key to make it easier to get
     * reference to it. In schema nodes in <code>uses</code> attribute there is
     * only Schema Path but when building list of implemented interfaces for
     * Schema node the object of type <code>Type</code> is required. So in this
     * case is used this map.
     */
    private final Map<SchemaPath, GeneratedType> allGroupings = new HashMap<SchemaPath, GeneratedType>();

    /**
     * Constant with the concrete name of namespace.
     */
    private final static String YANG_EXT_NAMESPACE = "urn:opendaylight:yang:extension:yang-ext";

    /**
     * Constant with the concrete name of identifier.
     */
    private final static String AUGMENT_IDENTIFIER_NAME = "augment-identifier";

    /**
     * Only parent constructor is invoked.
     */
    public BindingGeneratorImpl() {
        super();
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
     *             if param <code>context</code> is null
     * @throws IllegalStateException
     *             if <code>context</code> contain no modules
     */
    @Override
    public List<Type> generateTypes(final SchemaContext context) {
        Preconditions.checkArgument(context != null,"Schema Context reference cannot be NULL.");
        Preconditions.checkState(context.getModules() != null,"Schema Context does not contain defined modules.");
        final List<Type> generatedTypes = new ArrayList<>();
        schemaContext = context;
        typeProvider = new TypeProviderImpl(context);
        final Set<Module> modules = context.getModules();
        genTypeBuilders = new HashMap<>();
        for (final Module module : modules) {

            generatedTypes.addAll(allGroupingsToGenTypes(module));

            if (false == module.getChildNodes().isEmpty()) {
                generatedTypes.add(moduleToDataType(module));
            }
            generatedTypes.addAll(allTypeDefinitionsToGenTypes(module));
            generatedTypes.addAll(allContainersToGenTypes(module));
            generatedTypes.addAll(allListsToGenTypes(module));
            generatedTypes.addAll(allChoicesToGenTypes(module));
            generatedTypes.addAll(allAugmentsToGenTypes(module));
            generatedTypes.addAll(allRPCMethodsToGenType(module));
            generatedTypes.addAll(allNotificationsToGenType(module));
            generatedTypes.addAll(allIdentitiesToGenTypes(module, context));

        }
        return generatedTypes;
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
     *         set</li>.
     *         </ul>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if param <code>context</code> is null or</li>
     *             <li>if param <code>modules</code> is null</li>
     *             </ul>
     * @throws IllegalStateException
     *             if <code>context</code> contain no modules
     */
    @Override
    public List<Type> generateTypes(final SchemaContext context, final Set<Module> modules) {
        Preconditions.checkArgument(context != null,"Schema Context reference cannot be NULL.");
        Preconditions.checkState(context.getModules() != null,"Schema Context does not contain defined modules.");
        Preconditions.checkArgument(modules != null,"Sef of Modules cannot be NULL.");

        final List<Type> filteredGenTypes = new ArrayList<>();
        schemaContext = context;
        typeProvider = new TypeProviderImpl(context);
        final Set<Module> contextModules = context.getModules();
        genTypeBuilders = new HashMap<>();
        for (final Module contextModule : contextModules) {
            final List<Type> generatedTypes = new ArrayList<>();

            generatedTypes.addAll(allGroupingsToGenTypes(contextModule));
            if (false == contextModule.getChildNodes().isEmpty()) {
                generatedTypes.add(moduleToDataType(contextModule));
            }
            generatedTypes.addAll(allTypeDefinitionsToGenTypes(contextModule));
            generatedTypes.addAll(allContainersToGenTypes(contextModule));
            generatedTypes.addAll(allListsToGenTypes(contextModule));
            generatedTypes.addAll(allChoicesToGenTypes(contextModule));
            generatedTypes.addAll(allAugmentsToGenTypes(contextModule));
            generatedTypes.addAll(allRPCMethodsToGenType(contextModule));
            generatedTypes.addAll(allNotificationsToGenType(contextModule));
            generatedTypes.addAll(allIdentitiesToGenTypes(contextModule, context));

            if (modules.contains(contextModule)) {
                filteredGenTypes.addAll(generatedTypes);
            }
        }
        return filteredGenTypes;
    }

    /**
     * Converts all extended type definitions of module to the list of
     * <code>Type</code> objects.
     *
     * @param module
     *            module from which is obtained set of type definitions
     * @return list of <code>Type</code> which are generated from extended
     *         definition types (object of type <code>ExtendedType</code>)
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if module equals null</li>
     *             <li>if name of module equals null</li>
     *             <li>if type definitions of module equal null</li>
     *             </ul>
     *
     */
    private List<Type> allTypeDefinitionsToGenTypes(final Module module) {
        Preconditions.checkArgument(module != null,"Module reference cannot be NULL.");
        Preconditions.checkArgument(module.getName() != null,"Module name cannot be NULL.");
        Preconditions.checkArgument(module.getTypeDefinitions() != null,"Type Definitions for module " + module.getName() + " cannot be NULL.");

        final Set<TypeDefinition<?>> typeDefinitions = module.getTypeDefinitions();
        final List<Type> generatedTypes = new ArrayList<>();
        for (final TypeDefinition<?> typedef : typeDefinitions) {
            if (typedef != null) {
                final Type type = ((TypeProviderImpl) typeProvider).generatedTypeForExtendedDefinitionType(typedef);
                if ((type != null) && !generatedTypes.contains(type)) {
                    generatedTypes.add(type);
                }
            }
        }
        return generatedTypes;
    }

    /**
     * Converts all <b>containers</b> of the module to the list of
     * <code>Type</code> objects.
     *
     * @param module
     *            module from which is obtained DataNodeIterator to iterate over
     *            all containers
     * @return list of <code>Type</code> which are generated from containers
     *         (objects of type <code>ContainerSchemaNode</code>)
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if the module equals null</li>
     *             <li>if the name of module equals null</li>
     *             <li>if the set of child nodes equals null</li>
     *             </ul>
     *
     */
    private List<Type> allContainersToGenTypes(final Module module) {
        Preconditions.checkArgument(module != null,"Module reference cannot be NULL.");

        Preconditions.checkArgument(module.getName() != null,"Module name cannot be NULL.");

        if (module.getChildNodes() == null) {
            throw new IllegalArgumentException("Reference to Set of Child Nodes in module " + module.getName()
                    + " cannot be NULL.");
        }

        final List<Type> generatedTypes = new ArrayList<>();
        final DataNodeIterator it = new DataNodeIterator(module);
        final List<ContainerSchemaNode> schemaContainers = it.allContainers();
        final String basePackageName = moduleNamespaceToPackageName(module);
        for (final ContainerSchemaNode container : schemaContainers) {
            if (!container.isAddedByUses()) {
                generatedTypes.add(containerToGenType(basePackageName, container));
            }
        }
        return generatedTypes;
    }

    /**
     * Converts all <b>lists</b> of the module to the list of <code>Type</code>
     * objects.
     *
     * @param module
     *            module from which is obtained DataNodeIterator to iterate over
     *            all lists
     * @return list of <code>Type</code> which are generated from lists (objects
     *         of type <code>ListSchemaNode</code>)
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if the module equals null</li>
     *             <li>if the name of module equals null</li>
     *             <li>if the set of child nodes equals null</li>
     *             </ul>
     *
     */
    private List<Type> allListsToGenTypes(final Module module) {
        Preconditions.checkArgument(module != null,"Module reference cannot be NULL.");

        Preconditions.checkArgument(module.getName() != null,"Module name cannot be NULL.");

        if (module.getChildNodes() == null) {
            throw new IllegalArgumentException("Reference to Set of Child Nodes in module " + module.getName()
                    + " cannot be NULL.");
        }

        final List<Type> generatedTypes = new ArrayList<>();
        final DataNodeIterator it = new DataNodeIterator(module);
        final List<ListSchemaNode> schemaLists = it.allLists();
        final String basePackageName = moduleNamespaceToPackageName(module);
        if (schemaLists != null) {
            for (final ListSchemaNode list : schemaLists) {
                if (!list.isAddedByUses()) {
                    generatedTypes.addAll(listToGenType(basePackageName, list));
                }
            }
        }
        return generatedTypes;
    }

    /**
     * Converts all <b>choices</b> of the module to the list of
     * <code>Type</code> objects.
     *
     * @param module
     *            module from which is obtained DataNodeIterator to iterate over
     *            all choices
     * @return list of <code>Type</code> which are generated from choices
     *         (objects of type <code>ChoiceNode</code>)
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if the module equals null</li>
     *             <li>if the name of module equals null</li> *
     *             </ul>
     *
     */
    private List<GeneratedType> allChoicesToGenTypes(final Module module) {
        Preconditions.checkArgument(module != null,"Module reference cannot be NULL.");
        Preconditions.checkArgument(module.getName() != null,"Module name cannot be NULL.");

        final DataNodeIterator it = new DataNodeIterator(module);
        final List<ChoiceNode> choiceNodes = it.allChoices();
        final String basePackageName = moduleNamespaceToPackageName(module);

        final List<GeneratedType> generatedTypes = new ArrayList<>();
        for (final ChoiceNode choice : choiceNodes) {
            if ((choice != null) && !choice.isAddedByUses()) {
                generatedTypes.addAll(choiceToGeneratedType(basePackageName, choice));
            }
        }
        return generatedTypes;
    }

    /**
     * Converts all <b>augmentation</b> of the module to the list
     * <code>Type</code> objects.
     *
     * @param module
     *            module from which is obtained list of all augmentation objects
     *            to iterate over them
     * @return list of <code>Type</code> which are generated from augments
     *         (objects of type <code>AugmentationSchema</code>)
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if the module equals null</li>
     *             <li>if the name of module equals null</li>
     *             <li>if the set of child nodes equals null</li>
     *             </ul>
     *
     */
    private List<Type> allAugmentsToGenTypes(final Module module) {
        Preconditions.checkArgument(module != null,"Module reference cannot be NULL.");
        Preconditions.checkArgument(module.getName() != null,"Module name cannot be NULL.");
        if (module.getChildNodes() == null) {
            throw new IllegalArgumentException("Reference to Set of Augmentation Definitions in module "
                    + module.getName() + " cannot be NULL.");
        }

        final List<Type> generatedTypes = new ArrayList<>();
        final String basePackageName = moduleNamespaceToPackageName(module);
        final List<AugmentationSchema> augmentations = resolveAugmentations(module);
        for (final AugmentationSchema augment : augmentations) {
            generatedTypes.addAll(augmentationToGenTypes(basePackageName, augment));
        }
        return generatedTypes;
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
     *             <ul>
     *             <li>if the module equals null</li>
     *             <li>if the set of augmentation equals null</li>
     *             </ul>
     *
     */
    private List<AugmentationSchema> resolveAugmentations(final Module module) {
        Preconditions.checkArgument(module != null,"Module reference cannot be NULL.");
        Preconditions.checkState(module.getAugmentations() != null,"Augmentations Set cannot be NULL.");

        final Set<AugmentationSchema> augmentations = module.getAugmentations();
        final List<AugmentationSchema> sortedAugmentations = new ArrayList<>(augmentations);
        Collections.sort(sortedAugmentations, new Comparator<AugmentationSchema>() {

            @Override
            public int compare(AugmentationSchema augSchema1, AugmentationSchema augSchema2) {

                if (augSchema1.getTargetPath().getPath().size() > augSchema2.getTargetPath().getPath().size()) {
                    return 1;
                } else if (augSchema1.getTargetPath().getPath().size() < augSchema2.getTargetPath().getPath().size()) {
                    return -1;
                }
                return 0;

            }
        });

        return sortedAugmentations;
    }

    /**
     * Converts whole <b>module</b> to <code>GeneratedType</code> object.
     * Firstly is created the module builder object from which is finally
     * obtained reference to <code>GeneratedType</code> object.
     *
     * @param module
     *            module from which are obtained the module name, child nodes,
     *            uses and is derived package name
     * @return <code>GeneratedType</code> which is internal representation of
     *         the module
     * @throws IllegalArgumentException
     *             if the module equals null
     *
     */
    private GeneratedType moduleToDataType(final Module module) {
        Preconditions.checkArgument(module != null,"Module reference cannot be NULL.");

        final GeneratedTypeBuilder moduleDataTypeBuilder = moduleTypeBuilder(module, "Data");
        addImplementedInterfaceFromUses(module, moduleDataTypeBuilder);
        moduleDataTypeBuilder.addImplementsType(Types.typeForClass(DataRoot.class));

        final String basePackageName = moduleNamespaceToPackageName(module);
        if (moduleDataTypeBuilder != null) {
            final Set<DataSchemaNode> dataNodes = module.getChildNodes();
            resolveDataSchemaNodes(basePackageName, moduleDataTypeBuilder, dataNodes);
        }
        return moduleDataTypeBuilder.toInstance();
    }

    /**
     * Converts all <b>rpcs</b> inputs and outputs substatements of the module
     * to the list of <code>Type</code> objects. In addition are to containers
     * and lists which belong to input or output also part of returning list.
     *
     * @param module
     *            module from which is obtained set of all rpc objects to
     *            iterate over them
     * @return list of <code>Type</code> which are generated from rpcs inputs,
     *         outputs + container and lists which are part of inputs or outputs
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if the module equals null</li>
     *             <li>if the name of module equals null</li>
     *             <li>if the set of child nodes equals null</li>
     *             </ul>
     *
     */
    private List<Type> allRPCMethodsToGenType(final Module module) {
        Preconditions.checkArgument(module != null,"Module reference cannot be NULL.");

        Preconditions.checkArgument(module.getName() != null,"Module name cannot be NULL.");

        if (module.getChildNodes() == null) {
            throw new IllegalArgumentException("Reference to Set of RPC Method Definitions in module "
                    + module.getName() + " cannot be NULL.");
        }

        final String basePackageName = moduleNamespaceToPackageName(module);
        final Set<RpcDefinition> rpcDefinitions = module.getRpcs();

        if (rpcDefinitions.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Type> genRPCTypes = new ArrayList<>();
        final GeneratedTypeBuilder interfaceBuilder = moduleTypeBuilder(module, "Service");
        interfaceBuilder.addImplementsType(Types.typeForClass(RpcService.class));
        final Type future = Types.typeForClass(Future.class);
        for (final RpcDefinition rpc : rpcDefinitions) {
            if (rpc != null) {

                String rpcName = parseToClassName(rpc.getQName().getLocalName());
                String rpcMethodName = parseToValidParamName(rpcName);
                MethodSignatureBuilder method = interfaceBuilder.addMethod(rpcMethodName);

                final List<DataNodeIterator> rpcInOut = new ArrayList<>();

                ContainerSchemaNode input = rpc.getInput();
                ContainerSchemaNode output = rpc.getOutput();

                if (input != null) {
                    rpcInOut.add(new DataNodeIterator(input));
                    GeneratedTypeBuilder inType = addRawInterfaceDefinition(basePackageName, input, rpcName);
                    addImplementedInterfaceFromUses(input, inType);
                    inType.addImplementsType(Types.DATA_OBJECT);
                    resolveDataSchemaNodes(basePackageName, inType, input.getChildNodes());
                    Type inTypeInstance = inType.toInstance();
                    genRPCTypes.add(inTypeInstance);
                    method.addParameter(inTypeInstance, "input");
                }

                Type outTypeInstance = Types.typeForClass(Void.class);
                if (output != null) {
                    rpcInOut.add(new DataNodeIterator(output));
                    GeneratedTypeBuilder outType = addRawInterfaceDefinition(basePackageName, output, rpcName);
                    addImplementedInterfaceFromUses(output, outType);
                    outType.addImplementsType(Types.DATA_OBJECT);
                    resolveDataSchemaNodes(basePackageName, outType, output.getChildNodes());
                    outTypeInstance = outType.toInstance();
                    genRPCTypes.add(outTypeInstance);

                }

                final Type rpcRes = Types.parameterizedTypeFor(Types.typeForClass(RpcResult.class), outTypeInstance);
                method.setReturnType(Types.parameterizedTypeFor(future, rpcRes));
                for (DataNodeIterator it : rpcInOut) {
                    List<ContainerSchemaNode> nContainers = it.allContainers();
                    if ((nContainers != null) && !nContainers.isEmpty()) {
                        for (final ContainerSchemaNode container : nContainers) {
                            if (!container.isAddedByUses()) {
                                genRPCTypes.add(containerToGenType(basePackageName, container));
                            }
                        }
                    }
                    List<ListSchemaNode> nLists = it.allLists();
                    if ((nLists != null) && !nLists.isEmpty()) {
                        for (final ListSchemaNode list : nLists) {
                            if (!list.isAddedByUses()) {
                                genRPCTypes.addAll(listToGenType(basePackageName, list));
                            }
                        }
                    }
                }
            }
        }
        genRPCTypes.add(interfaceBuilder.toInstance());
        return genRPCTypes;
    }

    /**
     * Converts all <b>notifications</b> of the module to the list of
     * <code>Type</code> objects. In addition are to this list added containers
     * and lists which are part of this notification.
     *
     * @param module
     *            module from which is obtained set of all notification objects
     *            to iterate over them
     * @return list of <code>Type</code> which are generated from notification
     *         (object of type <code>NotificationDefinition</code>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if the module equals null</li>
     *             <li>if the name of module equals null</li>
     *             <li>if the set of child nodes equals null</li>
     *             </ul>
     *
     */
    private List<Type> allNotificationsToGenType(final Module module) {
        Preconditions.checkArgument(module != null,"Module reference cannot be NULL.");

        Preconditions.checkArgument(module.getName() != null,"Module name cannot be NULL.");

        if (module.getChildNodes() == null) {
            throw new IllegalArgumentException("Reference to Set of Notification Definitions in module "
                    + module.getName() + " cannot be NULL.");
        }

        final String basePackageName = moduleNamespaceToPackageName(module);
        final List<Type> genNotifyTypes = new ArrayList<>();
        final Set<NotificationDefinition> notifications = module.getNotifications();

        for (final NotificationDefinition notification : notifications) {
            if (notification != null) {
                DataNodeIterator it = new DataNodeIterator(notification);

                // Containers
                for (ContainerSchemaNode node : it.allContainers()) {
                    if (!node.isAddedByUses()) {
                        genNotifyTypes.add(containerToGenType(basePackageName, node));
                    }
                }
                // Lists
                for (ListSchemaNode node : it.allLists()) {
                    if (!node.isAddedByUses()) {
                        genNotifyTypes.addAll(listToGenType(basePackageName, node));
                    }
                }
                final GeneratedTypeBuilder notificationTypeBuilder = addDefaultInterfaceDefinition(basePackageName,
                        notification);
                notificationTypeBuilder.addImplementsType(Types
                        .typeForClass(org.opendaylight.yangtools.yang.binding.Notification.class));
                // Notification object
                resolveDataSchemaNodes(basePackageName, notificationTypeBuilder, notification.getChildNodes());
                genNotifyTypes.add(notificationTypeBuilder.toInstance());
            }
        }
        return genNotifyTypes;
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
     * @return list of <code>Type</code> which are generated from identities
     *         (object of type <code>IdentitySchemaNode</code>
     *
     */
    private List<Type> allIdentitiesToGenTypes(final Module module, final SchemaContext context) {
        List<Type> genTypes = new ArrayList<>();

        final Set<IdentitySchemaNode> schemaIdentities = module.getIdentities();

        final String basePackageName = moduleNamespaceToPackageName(module);

        if (schemaIdentities != null && !schemaIdentities.isEmpty()) {
            for (final IdentitySchemaNode identity : schemaIdentities) {
                genTypes.add(identityToGenType(basePackageName, identity, context));
            }
        }
        return genTypes;
    }

    /**
     * Converts the <b>identity</b> object to GeneratedType. Firstly it is
     * created transport object builder. If identity contains base identity then
     * reference to base identity is added to superior identity as its extend.
     * If identity doesn't contain base identity then only reference to abstract
     * class {@link org.opendaylight.yangtools.yang.model.api.BaseIdentity
     * BaseIdentity} is added
     *
     * @param basePackageName
     *            string contains the module package name
     * @param identity
     *            IdentitySchemaNode which contains data about identity
     * @param context
     *            SchemaContext which is used to get package and name
     *            information about base of identity
     *
     * @return GeneratedType which is generated from identity (object of type
     *         <code>IdentitySchemaNode</code>
     *
     */
    private GeneratedType identityToGenType(final String basePackageName, final IdentitySchemaNode identity,
            final SchemaContext context) {
        if (identity == null) {
            return null;
        }

        final String packageName = packageNameForGeneratedType(basePackageName, identity.getPath());
        final String genTypeName = parseToClassName(identity.getQName().getLocalName());
        final GeneratedTOBuilderImpl newType = new GeneratedTOBuilderImpl(packageName, genTypeName);

        IdentitySchemaNode baseIdentity = identity.getBaseIdentity();
        if (baseIdentity != null) {
            Module baseIdentityParentModule = SchemaContextUtil.findParentModule(context, baseIdentity);

            final String returnTypePkgName = moduleNamespaceToPackageName(baseIdentityParentModule);
            final String returnTypeName = parseToClassName(baseIdentity.getQName().getLocalName());

            GeneratedTransferObject gto = new GeneratedTOBuilderImpl(returnTypePkgName, returnTypeName).toInstance();
            newType.setExtendsType(gto);
        } else {
            newType.setExtendsType(Types.getBaseIdentityTO());
        }
        newType.setAbstract(true);
        return newType.toInstance();
    }

    /**
     * Converts all <b>groupings</b> of the module to the list of
     * <code>Type</code> objects. Firstly are groupings sorted according mutual
     * dependencies. At least dependend (indepedent) groupings are in the list
     * saved at first positions. For every grouping the record is added to map
     * {@link BindingGeneratorImpl#allGroupings allGroupings}
     *
     * @param module
     *            module from which is obtained set of all grouping objects to
     *            iterate over them
     * @return list of <code>Type</code> which are generated from groupings
     *         (object of type <code>GroupingDefinition</code>)
     *
     */
    private List<Type> allGroupingsToGenTypes(final Module module) {
        Preconditions.checkArgument(module != null,"Module parameter can not be null");
        final List<Type> genTypes = new ArrayList<>();
        final String basePackageName = moduleNamespaceToPackageName(module);
        final Set<GroupingDefinition> groupings = module.getGroupings();
        List<GroupingDefinition> groupingsSortedByDependencies;

        groupingsSortedByDependencies = GroupingDefinitionDependencySort.sort(groupings);

        for (final GroupingDefinition grouping : groupingsSortedByDependencies) {
            GeneratedType genType = groupingToGenType(basePackageName, grouping);
            genTypes.add(genType);
            SchemaPath schemaPath = grouping.getPath();
            allGroupings.put(schemaPath, genType);
        }
        return genTypes;
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
     * @return GeneratedType which is generated from grouping (object of type
     *         <code>GroupingDefinition</code>)
     */
    private GeneratedType groupingToGenType(final String basePackageName, GroupingDefinition grouping) {
        if (grouping == null) {
            return null;
        }

        final String packageName = packageNameForGeneratedType(basePackageName, grouping.getPath());
        final Set<DataSchemaNode> schemaNodes = grouping.getChildNodes();
        final GeneratedTypeBuilder typeBuilder = addDefaultInterfaceDefinition(packageName, grouping);

        resolveDataSchemaNodes(basePackageName, typeBuilder, schemaNodes);
        return typeBuilder.toInstance();
    }

    /**
     * Tries to find EnumTypeDefinition in <code>typeDefinition</code>. If base
     * type of <code>typeDefinition</code> is of the type ExtendedType then this
     * method is recursivelly called with this base type.
     *
     * @param typeDefinition
     *            TypeDefinition in which should be EnumTypeDefinition found as
     *            base type
     * @return EnumTypeDefinition if it is found inside
     *         <code>typeDefinition</code> or <code>null</code> in other case
     */
    private EnumTypeDefinition enumTypeDefFromExtendedType(final TypeDefinition<?> typeDefinition) {
        if (typeDefinition != null) {
            if (typeDefinition.getBaseType() instanceof EnumTypeDefinition) {
                return (EnumTypeDefinition) typeDefinition.getBaseType();
            } else if (typeDefinition.getBaseType() instanceof ExtendedType) {
                return enumTypeDefFromExtendedType(typeDefinition.getBaseType());
            }
        }
        return null;
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
     * @return enumeration builder which contais data from
     *         <code>enumTypeDef</code>
     */
    private EnumBuilder resolveInnerEnumFromTypeDefinition(final EnumTypeDefinition enumTypeDef, final String enumName,
            final GeneratedTypeBuilder typeBuilder) {
        if ((enumTypeDef != null) && (typeBuilder != null) && (enumTypeDef.getQName() != null)
                && (enumTypeDef.getQName().getLocalName() != null)) {

            final String enumerationName = parseToClassName(enumName);
            final EnumBuilder enumBuilder = typeBuilder.addEnumeration(enumerationName);
            enumBuilder.updateEnumPairsFromEnumTypeDef(enumTypeDef);

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
     *             if <code>module</code> equals null
     */
    private GeneratedTypeBuilder moduleTypeBuilder(final Module module, final String postfix) {
        Preconditions.checkArgument(module != null,"Module reference cannot be NULL.");
        String packageName = moduleNamespaceToPackageName(module);
        final String moduleName = parseToClassName(module.getName()) + postfix;

        return new GeneratedTypeBuilderImpl(packageName, moduleName);

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
     *            AugmentationSchema which is contains data about agumentation
     *            (target path, childs...)
     * @return list of <code>Type</code> objects which contains generated type
     *         for augmentation and for container, list and choice child nodes
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>augmentPackageName</code> equals null</li>
     *             <li>if <code>augSchema</code> equals null</li>
     *             <li>if target path of <code>augSchema</code> equals null</li>
     *             </ul>
     */
    private List<Type> augmentationToGenTypes(final String augmentPackageName, final AugmentationSchema augSchema) {
        Preconditions.checkArgument(augmentPackageName != null,"Package Name cannot be NULL.");
        Preconditions.checkArgument(augSchema != null,"Augmentation Schema cannot be NULL.");
        Preconditions.checkState(augSchema.getTargetPath() != null,"Augmentation Schema does not contain Target Path (Target Path is NULL).");

        final List<Type> genTypes = new ArrayList<>();

        // EVERY augmented interface will extends Augmentation<T> interface
        // and DataObject interface!!!
        final SchemaPath targetPath = augSchema.getTargetPath();
        final DataSchemaNode targetSchemaNode = findDataSchemaNode(schemaContext, targetPath);
        if ((targetSchemaNode != null) && (targetSchemaNode.getQName() != null)
                && (targetSchemaNode.getQName().getLocalName() != null)) {
            final Module targetModule = findParentModule(schemaContext, targetSchemaNode);
            final String targetBasePackage = moduleNamespaceToPackageName(targetModule);
            final String targetPackageName = packageNameForGeneratedType(targetBasePackage, targetSchemaNode.getPath());
            final String targetSchemaNodeName = targetSchemaNode.getQName().getLocalName();
            final Set<DataSchemaNode> augChildNodes = augSchema.getChildNodes();

            if (!(targetSchemaNode instanceof ChoiceNode)) {
                final GeneratedTypeBuilder augTypeBuilder = addRawAugmentGenTypeDefinition(augmentPackageName,
                        targetPackageName, targetSchemaNodeName, augSchema);
                final GeneratedType augType = augTypeBuilder.toInstance();
                genTypes.add(augType);
            } else {
                final Type refChoiceType = new ReferencedTypeImpl(targetPackageName,
                        parseToClassName(targetSchemaNodeName));
                final ChoiceNode choiceTarget = (ChoiceNode) targetSchemaNode;
                final Set<ChoiceCaseNode> choiceCaseNodes = choiceTarget.getCases();
                genTypes.addAll(generateTypesFromAugmentedChoiceCases(augmentPackageName, refChoiceType,
                        choiceCaseNodes));
            }
            genTypes.addAll(augmentationBodyToGenTypes(augmentPackageName, augChildNodes));
        }
        return genTypes;
    }

    /**
     * Returns a generated type builder for an augmentation.
     *
     * The name of the type builder is equal to the name of augmented node with
     * serial number as suffix.
     *
     * @param augmentPackageName
     *            string with contains the package name to which the augment
     *            belongs
     * @param targetPackageName
     *            string with the package name to which the augmented node
     *            belongs
     * @param targetSchemaNodeName
     *            string with the name of the augmented node
     * @param augSchema
     *            augmentation schema which contains data about the child nodes
     *            and uses of augment
     * @return generated type builder for augment
     */
    private GeneratedTypeBuilder addRawAugmentGenTypeDefinition(final String augmentPackageName,
            final String targetPackageName, final String targetSchemaNodeName, final AugmentationSchema augSchema) {
        final String targetTypeName = parseToClassName(targetSchemaNodeName);
        Map<String, GeneratedTypeBuilder> augmentBuilders = genTypeBuilders.get(augmentPackageName);
        if (augmentBuilders == null) {
            augmentBuilders = new HashMap<>();
            genTypeBuilders.put(augmentPackageName, augmentBuilders);
        }
        final String augIdentifier = getAugmentIdentifier(augSchema.getUnknownSchemaNodes());

        final String augTypeName = augIdentifier != null ? parseToClassName(augIdentifier) : augGenTypeName(
                augmentBuilders, targetTypeName);
        final Type targetTypeRef = new ReferencedTypeImpl(targetPackageName, targetTypeName);
        final Set<DataSchemaNode> augChildNodes = augSchema.getChildNodes();

        final GeneratedTypeBuilder augTypeBuilder = new GeneratedTypeBuilderImpl(augmentPackageName, augTypeName);

        augTypeBuilder.addImplementsType(Types.DATA_OBJECT);
        augTypeBuilder.addImplementsType(Types.augmentationTypeFor(targetTypeRef));
        addImplementedInterfaceFromUses(augSchema, augTypeBuilder);

        augSchemaNodeToMethods(augmentPackageName, augTypeBuilder, augChildNodes);
        augmentBuilders.put(augTypeName, augTypeBuilder);
        return augTypeBuilder;
    }

    /**
     * 
     * @param unknownSchemaNodes
     * @return
     */
    private String getAugmentIdentifier(List<UnknownSchemaNode> unknownSchemaNodes) {
        String ret = null;
        for (UnknownSchemaNode unknownSchemaNode : unknownSchemaNodes) {
            QName nodeType = unknownSchemaNode.getNodeType();
            if (AUGMENT_IDENTIFIER_NAME.equals(nodeType.getLocalName())
                    && YANG_EXT_NAMESPACE.equals(nodeType.getNamespace().toString())) {
                return unknownSchemaNode.getNodeParameter();
            }
        }
        return ret;
    }

    /**
     * Convert a container, list and choice subnodes (and recursivelly their
     * subnodes) of augment to generated types
     *
     * @param augBasePackageName
     *            string with the augment package name
     * @param augChildNodes
     *            set of data schema nodes which represents child nodes of the
     *            augment
     *
     * @return list of <code>Type</code> which represents container, list and
     *         choice subnodes of augment
     */
    private List<Type> augmentationBodyToGenTypes(final String augBasePackageName,
            final Set<DataSchemaNode> augChildNodes) {
        final List<Type> genTypes = new ArrayList<>();
        final List<DataNodeIterator> augSchemaIts = new ArrayList<>();
        for (final DataSchemaNode childNode : augChildNodes) {
            if (childNode instanceof DataNodeContainer) {
                augSchemaIts.add(new DataNodeIterator((DataNodeContainer) childNode));

                if (childNode instanceof ContainerSchemaNode) {
                    genTypes.add(containerToGenType(augBasePackageName, (ContainerSchemaNode) childNode));
                } else if (childNode instanceof ListSchemaNode) {
                    genTypes.addAll(listToGenType(augBasePackageName, (ListSchemaNode) childNode));
                }
            } else if (childNode instanceof ChoiceNode) {
                final ChoiceNode choice = (ChoiceNode) childNode;
                for (final ChoiceCaseNode caseNode : choice.getCases()) {
                    augSchemaIts.add(new DataNodeIterator(caseNode));
                }
                genTypes.addAll(choiceToGeneratedType(augBasePackageName, (ChoiceNode) childNode));
            }
        }

        for (final DataNodeIterator it : augSchemaIts) {
            final List<ContainerSchemaNode> augContainers = it.allContainers();
            final List<ListSchemaNode> augLists = it.allLists();
            final List<ChoiceNode> augChoices = it.allChoices();

            if (augContainers != null) {
                for (final ContainerSchemaNode container : augContainers) {
                    genTypes.add(containerToGenType(augBasePackageName, container));
                }
            }
            if (augLists != null) {
                for (final ListSchemaNode list : augLists) {
                    genTypes.addAll(listToGenType(augBasePackageName, list));
                }
            }
            if (augChoices != null) {
                for (final ChoiceNode choice : augChoices) {
                    genTypes.addAll(choiceToGeneratedType(augBasePackageName, choice));
                }
            }
        }
        return genTypes;
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
        String augTypeName = genTypeName;

        int index = 1;
        while ((builders != null) && builders.containsKey(genTypeName + index)) {
            index++;
        }
        augTypeName += index;
        return augTypeName;
    }

    /**
     * Converts <code>containerNode</code> to generated type. Firstly the
     * generated type builder is created. The subnodes of
     * <code>containerNode</code> are added as methods and the instance of
     * <code>GeneratedType</code> is returned.
     *
     * @param basePackageName
     *            string contains the module package name
     * @param containerNode
     *            container schema node with the data about childs nodes and
     *            schema paths
     * @return generated type for <code>containerNode</code>
     */
    private GeneratedType containerToGenType(final String basePackageName, ContainerSchemaNode containerNode) {
        if (containerNode == null) {
            return null;
        }

        final String packageName = packageNameForGeneratedType(basePackageName, containerNode.getPath());
        final Set<DataSchemaNode> schemaNodes = containerNode.getChildNodes();
        final GeneratedTypeBuilder typeBuilder = addDefaultInterfaceDefinition(packageName, containerNode);

        resolveDataSchemaNodes(basePackageName, typeBuilder, schemaNodes);
        return typeBuilder.toInstance();
    }

    /**
     * Adds the methods to <code>typeBuilder</code> which represent subnodes of
     * node for which <code>typeBuilder</code> was created.
     *
     * The subnodes aren't mapped to the methods if they are part of grouping or
     * augment (in this case are already part of them).
     *
     * @param basePackageName
     *            string contains the module package name
     * @param typeBuilder
     *            generated type builder which represents any node. The subnodes
     *            of this node are added to the <code>typeBuilder</code> as
     *            methods. The subnode can be of type leaf, leaf-list, list,
     *            container, choice.
     * @param schemaNodes
     *            set of data schema nodes which are the children of the node
     *            for which <code>typeBuilder</code> was created
     * @return generated type builder which is the same builder as input
     *         parameter. The getter methods (representing child nodes) could be
     *         added to it.
     */
    private GeneratedTypeBuilder resolveDataSchemaNodes(final String basePackageName,
            final GeneratedTypeBuilder typeBuilder, final Set<DataSchemaNode> schemaNodes) {
        if ((schemaNodes != null) && (typeBuilder != null)) {
            for (final DataSchemaNode schemaNode : schemaNodes) {
                if (schemaNode.isAugmenting() || schemaNode.isAddedByUses()) {
                    continue;
                }
                addSchemaNodeToBuilderAsMethod(basePackageName, schemaNode, typeBuilder);
            }
        }
        return typeBuilder;
    }

    /**
     * Adds the methods to <code>typeBuilder</code> what represents subnodes of
     * node for which <code>typeBuilder</code> was created.
     *
     * @param basePackageName
     *            string contains the module package name
     * @param typeBuilder
     *            generated type builder which represents any node. The subnodes
     *            of this node are added to the <code>typeBuilder</code> as
     *            methods. The subnode can be of type leaf, leaf-list, list,
     *            container, choice.
     * @param schemaNodes
     *            set of data schema nodes which are the children of the node
     *            for which <code>typeBuilder</code> was created
     * @return generated type builder which is the same object as the input
     *         parameter <code>typeBuilder</code>. The getter method could be
     *         added to it.
     */
    private GeneratedTypeBuilder augSchemaNodeToMethods(final String basePackageName,
            final GeneratedTypeBuilder typeBuilder, final Set<DataSchemaNode> schemaNodes) {
        if ((schemaNodes != null) && (typeBuilder != null)) {
            for (final DataSchemaNode schemaNode : schemaNodes) {
                if (schemaNode.isAugmenting()) {
                    addSchemaNodeToBuilderAsMethod(basePackageName, schemaNode, typeBuilder);
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
     * @param schemaNode
     *            data schema node which is added to <code>typeBuilder</code> as
     *            a method
     * @param typeBuilder
     *            generated type builder to which is <code>schemaNode</code>
     *            added as a method.
     */
    private void addSchemaNodeToBuilderAsMethod(final String basePackageName, final DataSchemaNode schemaNode,
            final GeneratedTypeBuilder typeBuilder) {
        if (schemaNode != null && typeBuilder != null) {
            if (schemaNode instanceof LeafSchemaNode) {
                resolveLeafSchemaNodeAsMethod(typeBuilder, (LeafSchemaNode) schemaNode);
            } else if (schemaNode instanceof LeafListSchemaNode) {
                resolveLeafListSchemaNode(typeBuilder, (LeafListSchemaNode) schemaNode);
            } else if (schemaNode instanceof ContainerSchemaNode) {
                resolveContainerSchemaNode(basePackageName, typeBuilder, (ContainerSchemaNode) schemaNode);
            } else if (schemaNode instanceof ListSchemaNode) {
                resolveListSchemaNode(basePackageName, typeBuilder, (ListSchemaNode) schemaNode);
            } else if (schemaNode instanceof ChoiceNode) {
                resolveChoiceSchemaNode(basePackageName, typeBuilder, (ChoiceNode) schemaNode);
            }
        }
    }

    /**
     * Creates a getter method for a choice node.
     *
     * Firstly generated type builder for choice is created or found in
     * {@link BindingGeneratorImpl#allGroupings allGroupings}. The package name
     * in the builder is created as concatenation of module package name and
     * names of all parent nodes. In the end the getter method for choice is
     * added to <code>typeBuilder</code> and return type is set to choice
     * builder.
     *
     * @param basePackageName
     *            string with the module package name
     * @param typeBuilder
     *            generated type builder to which is <code>choiceNode</code>
     *            added as getter method
     * @param choiceNode
     *            choice node which is mapped as a getter method
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>basePackageName</code> equals null</li>
     *             <li>if <code>typeBuilder</code> equals null</li>
     *             <li>if <code>choiceNode</code> equals null</li>
     *             </ul>
     *
     */
    private void resolveChoiceSchemaNode(final String basePackageName, final GeneratedTypeBuilder typeBuilder,
            final ChoiceNode choiceNode) {
        Preconditions.checkArgument(basePackageName != null,"Base Package Name cannot be NULL.");
        Preconditions.checkArgument(typeBuilder != null,"Generated Type Builder cannot be NULL.");
        Preconditions.checkArgument(choiceNode != null,"Choice Schema Node cannot be NULL.");

        final String choiceName = choiceNode.getQName().getLocalName();
        if (choiceName != null && !choiceNode.isAddedByUses()) {
            final String packageName = packageNameForGeneratedType(basePackageName, choiceNode.getPath());
            final GeneratedTypeBuilder choiceType = addDefaultInterfaceDefinition(packageName, choiceNode);
            constructGetter(typeBuilder, choiceName, choiceNode.getDescription(), choiceType);
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
     * @param basePackageName
     *            string with the module package name
     * @param choiceNode
     *            choice node which is mapped to generated type. Also child
     *            nodes - cases are mapped to generated types.
     * @return list of generated types which contains generated type for choice
     *         and generated types for all cases which aren't added do choice
     *         through <i>uses</i>.
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>basePackageName</code> equals null</li>
     *             <li>if <code>choiceNode</code> equals null</li>
     *             </ul>
     *
     */
    private List<GeneratedType> choiceToGeneratedType(final String basePackageName, final ChoiceNode choiceNode) {
        Preconditions.checkArgument(basePackageName != null,"Base Package Name cannot be NULL.");
        Preconditions.checkArgument(choiceNode != null,"Choice Schema Node cannot be NULL.");

        final List<GeneratedType> generatedTypes = new ArrayList<>();
        final String packageName = packageNameForGeneratedType(basePackageName, choiceNode.getPath());
        final GeneratedTypeBuilder choiceTypeBuilder = addRawInterfaceDefinition(packageName, choiceNode);
        choiceTypeBuilder.addImplementsType(Types.DATA_OBJECT);
        final GeneratedType choiceType = choiceTypeBuilder.toInstance();

        generatedTypes.add(choiceType);
        final Set<ChoiceCaseNode> caseNodes = choiceNode.getCases();
        if ((caseNodes != null) && !caseNodes.isEmpty()) {
            generatedTypes.addAll(generateTypesFromChoiceCases(basePackageName, choiceType, caseNodes));
        }
        return generatedTypes;
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
     * @param basePackageName
     *            string with the module package name
     * @param refChoiceType
     *            type which represents superior <i>case</i>
     * @param caseNodes
     *            set of choice case nodes which are mapped to generated types
     * @return list of generated types for <code>caseNodes</code>.
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>basePackageName</code> equals null</li>
     *             <li>if <code>refChoiceType</code> equals null</li>
     *             <li>if <code>caseNodes</code> equals null</li>
     *             </ul>
     *             *
     */
    private List<GeneratedType> generateTypesFromChoiceCases(final String basePackageName, final Type refChoiceType,
            final Set<ChoiceCaseNode> caseNodes) {
        Preconditions.checkArgument(basePackageName != null,"Base Package Name cannot be NULL.");
        Preconditions.checkArgument(refChoiceType != null,"Referenced Choice Type cannot be NULL.");
        Preconditions.checkArgument(caseNodes != null,"Set of Choice Case Nodes cannot be NULL.");

        final List<GeneratedType> generatedTypes = new ArrayList<>();
        for (final ChoiceCaseNode caseNode : caseNodes) {
            if (caseNode != null && !caseNode.isAddedByUses() && !caseNode.isAugmenting()) {
                final String packageName = packageNameForGeneratedType(basePackageName, caseNode.getPath());
                final GeneratedTypeBuilder caseTypeBuilder = addDefaultInterfaceDefinition(packageName, caseNode);
                caseTypeBuilder.addImplementsType(refChoiceType);

                final Set<DataSchemaNode> childNodes = caseNode.getChildNodes();
                if (childNodes != null) {
                    resolveDataSchemaNodes(basePackageName, caseTypeBuilder, childNodes);
                }
                generatedTypes.add(caseTypeBuilder.toInstance());
            }
        }

        return generatedTypes;
    }

    /**
     * Generates list of generated types for all the cases of a choice which are
     * added to the choice through the augment.
     *
     *
     * @param basePackageName
     *            string contains name of package to which augment belongs. If
     *            an augmented choice is from an other package (pcg1) than an
     *            augmenting choice (pcg2) then case's of the augmenting choice
     *            will belong to pcg2.
     * @param refChoiceType
     *            Type which represents the choice to which case belongs. Every
     *            case has to contain its choice in extend part.
     * @param caseNodes
     *            set of choice case nodes for which is checked if are/aren't
     *            added to choice through augmentation
     * @return list of generated types which represents augmented cases of
     *         choice <code>refChoiceType</code>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>basePackageName</code> equals null</li>
     *             <li>if <code>refChoiceType</code> equals null</li>
     *             <li>if <code>caseNodes</code> equals null</li>
     *             </ul>
     */
    private List<GeneratedType> generateTypesFromAugmentedChoiceCases(final String basePackageName,
            final Type refChoiceType, final Set<ChoiceCaseNode> caseNodes) {
        Preconditions.checkArgument(basePackageName != null,"Base Package Name cannot be NULL.");
        Preconditions.checkArgument(refChoiceType != null,"Referenced Choice Type cannot be NULL.");
        Preconditions.checkArgument(caseNodes != null,"Set of Choice Case Nodes cannot be NULL.");

        final List<GeneratedType> generatedTypes = new ArrayList<>();
        for (final ChoiceCaseNode caseNode : caseNodes) {
            if (caseNode != null && caseNode.isAugmenting()) {
                final String packageName = packageNameForGeneratedType(basePackageName, caseNode.getPath());
                final GeneratedTypeBuilder caseTypeBuilder = addDefaultInterfaceDefinition(packageName, caseNode);
                caseTypeBuilder.addImplementsType(refChoiceType);

                final Set<DataSchemaNode> childNodes = caseNode.getChildNodes();
                if (childNodes != null) {
                    resolveDataSchemaNodes(basePackageName, caseTypeBuilder, childNodes);
                }
                generatedTypes.add(caseTypeBuilder.toInstance());
            }
        }

        return generatedTypes;
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
     * @return boolean value
     *         <ul>
     *         <li>false - if <code>leaf</code> or <code>typeBuilder</code> are
     *         null</li>
     *         <li>true - in other cases</li>
     *         </ul>
     */
    private boolean resolveLeafSchemaNodeAsMethod(final GeneratedTypeBuilder typeBuilder, final LeafSchemaNode leaf) {
        if ((leaf != null) && (typeBuilder != null)) {
            final String leafName = leaf.getQName().getLocalName();
            String leafDesc = leaf.getDescription();
            if (leafDesc == null) {
                leafDesc = "";
            }

            final Module parentModule = findParentModule(schemaContext, leaf);
            if (leafName != null && !leaf.isAddedByUses()) {
                final TypeDefinition<?> typeDef = leaf.getType();

                Type returnType = null;
                if (typeDef instanceof EnumTypeDefinition) {
                    returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef);
                    final EnumTypeDefinition enumTypeDef = enumTypeDefFromExtendedType(typeDef);
                    final EnumBuilder enumBuilder = resolveInnerEnumFromTypeDefinition(enumTypeDef, leafName,
                            typeBuilder);

                    if (enumBuilder != null) {
                        returnType = new ReferencedTypeImpl(enumBuilder.getPackageName(), enumBuilder.getName());
                    }
                    ((TypeProviderImpl) typeProvider).putReferencedType(leaf.getPath(), returnType);
                } else if (typeDef instanceof UnionType) {
                    GeneratedTOBuilder genTOBuilder = addTOToTypeBuilder(typeDef, typeBuilder, leafName, parentModule);
                    if (genTOBuilder != null) {
                        returnType = new ReferencedTypeImpl(genTOBuilder.getPackageName(), genTOBuilder.getName());
                    }
                } else if (typeDef instanceof BitsTypeDefinition) {
                    GeneratedTOBuilder genTOBuilder = addTOToTypeBuilder(typeDef, typeBuilder, leafName, parentModule);
                    if (genTOBuilder != null) {
                        returnType = new ReferencedTypeImpl(genTOBuilder.getPackageName(), genTOBuilder.getName());
                    }
                } else {
                    returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef);
                }
                if (returnType != null) {
                    constructGetter(typeBuilder, leafName, leafDesc, returnType);
                    return true;
                }
            }
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
            boolean isReadOnly) {
        if ((leaf != null) && (toBuilder != null)) {
            final String leafName = leaf.getQName().getLocalName();
            String leafDesc = leaf.getDescription();
            if (leafDesc == null) {
                leafDesc = "";
            }

            if (leafName != null && !leaf.isAddedByUses()) {
                final TypeDefinition<?> typeDef = leaf.getType();

                // TODO: properly resolve enum types
                final Type returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef);

                if (returnType != null) {
                    final GeneratedPropertyBuilder propBuilder = toBuilder.addProperty(parseToClassName(leafName));

                    propBuilder.setReadOnly(isReadOnly);
                    propBuilder.setReturnType(returnType);
                    propBuilder.setComment(leafDesc);

                    toBuilder.addEqualsIdentity(propBuilder);
                    toBuilder.addHashIdentity(propBuilder);
                    toBuilder.addToStringProperty(propBuilder);

                    return true;
                }
            }
        }
        return false;
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
     * @return boolean value
     *         <ul>
     *         <li>true - if <code>node</code>, <code>typeBuilder</code>,
     *         nodeName equal null or <code>node</code> is added by <i>uses</i></li>
     *         <li>false - other cases</li>
     *         </ul>
     */
    private boolean resolveLeafListSchemaNode(final GeneratedTypeBuilder typeBuilder, final LeafListSchemaNode node) {
        if ((node != null) && (typeBuilder != null)) {
            final String nodeName = node.getQName().getLocalName();
            String nodeDesc = node.getDescription();
            if (nodeDesc == null) {
                nodeDesc = "";
            }

            if (nodeName != null && !node.isAddedByUses()) {
                final TypeDefinition<?> type = node.getType();
                final Type listType = Types.listTypeFor(typeProvider.javaTypeForSchemaDefinitionType(type));

                constructGetter(typeBuilder, nodeName, nodeDesc, listType);
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a getter method for a container node.
     *
     * Firstly generated type builder for container is created or found in
     * {@link BindingGeneratorImpl#allGroupings allGroupings}. The package name
     * in the builder is created as concatenation of module package name and
     * names of all parent nodes. In the end the getter method for container is
     * added to <code>typeBuilder</code> and return type is set to container
     * type builder.
     *
     * @param basePackageName
     *            string with the module package name
     * @param typeBuilder
     *            generated type builder to which is <code>containerNode</code>
     *            added as getter method
     * @param containerNode
     *            container schema node which is mapped as getter method to
     *            <code>typeBuilder</code>
     * @return boolean value
     *         <ul>
     *         <li>false - if <code>containerNode</code>,
     *         <code>typeBuilder</code>, container node name equal null or
     *         <code>containerNode</code> is added by uses</li>
     *         <li>true - other cases</li>
     *         </ul>
     */
    private boolean resolveContainerSchemaNode(final String basePackageName, final GeneratedTypeBuilder typeBuilder,
            final ContainerSchemaNode containerNode) {
        if ((containerNode != null) && (typeBuilder != null)) {
            final String nodeName = containerNode.getQName().getLocalName();

            if (nodeName != null && !containerNode.isAddedByUses()) {
                final String packageName = packageNameForGeneratedType(basePackageName, containerNode.getPath());

                final GeneratedTypeBuilder rawGenType = addDefaultInterfaceDefinition(packageName, containerNode);
                constructGetter(typeBuilder, nodeName, containerNode.getDescription(), rawGenType);

                return true;
            }
        }
        return false;
    }

    /**
     * Creates a getter method for a list node.
     *
     * Firstly generated type builder for list is created or found in
     * {@link BindingGeneratorImpl#allGroupings allGroupings}. The package name
     * in the builder is created as concatenation of module package name and
     * names of all parent nodes. In the end the getter method for list is added
     * to <code>typeBuilder</code> and return type is set to list type builder.
     *
     * @param basePackageName
     *            string with the module package name
     * @param typeBuilder
     *            generated type builder to which is <code></code> added as
     *            getter method
     * @param listNode
     *            list schema node which is mapped as getter method to
     *            <code>typeBuilder</code>
     * @return boolean value
     *         <ul>
     *         <li>false - if <code>listNode</code>, <code>typeBuilder</code>,
     *         list node name equal null or <code>listNode</code> is added by
     *         uses</li>
     *         <li>true - other cases</li>
     *         </ul>
     */
    private boolean resolveListSchemaNode(final String basePackageName, final GeneratedTypeBuilder typeBuilder,
            final ListSchemaNode listNode) {
        if ((listNode != null) && (typeBuilder != null)) {
            final String listName = listNode.getQName().getLocalName();

            if (listName != null && !listNode.isAddedByUses()) {
                final String packageName = packageNameForGeneratedType(basePackageName, listNode.getPath());
                final GeneratedTypeBuilder rawGenType = addDefaultInterfaceDefinition(packageName, listNode);
                constructGetter(typeBuilder, listName, listNode.getDescription(), Types.listTypeFor(rawGenType));
                return true;
            }
        }
        return false;
    }

    /**
     * Instantiates generated type builder with <code>packageName</code> and
     * <code>schemaNode</code>.
     *
     * The new builder always implements
     * {@link org.opendaylight.yangtools.yang.binding.DataObject DataObject}.<br />
     * If <code>schemaNode</code> is instance of GroupingDefinition it also
     * implements {@link org.opendaylight.yangtools.yang.binding.Augmentable
     * Augmentable}.<br />
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
     * @return generated type builder <code>schemaNode</code>
     */
    private GeneratedTypeBuilder addDefaultInterfaceDefinition(final String packageName, final SchemaNode schemaNode) {
        final GeneratedTypeBuilder builder = addRawInterfaceDefinition(packageName, schemaNode, "");
        builder.addImplementsType(Types.DATA_OBJECT);
        if (!(schemaNode instanceof GroupingDefinition)) {
            builder.addImplementsType(Types.augmentableTypeFor(builder));
        }

        if (schemaNode instanceof DataNodeContainer) {
            addImplementedInterfaceFromUses((DataNodeContainer) schemaNode, builder);
        }

        return builder;
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
     * @return generated type builder for <code>schemaNode</code>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>schemaNode</code> equals null</li>
     *             <li>if <code>packageName</code> equals null</li>
     *             <li>if Q name of schema node is null</li>
     *             <li>if schema node name is nul</li>
     *             </ul>
     *
     */
    private GeneratedTypeBuilder addRawInterfaceDefinition(final String packageName, final SchemaNode schemaNode,
            final String prefix) {
        Preconditions.checkArgument(schemaNode != null,"Data Schema Node cannot be NULL.");
        Preconditions.checkArgument(packageName != null,"Package Name for Generated Type cannot be NULL.");
        Preconditions.checkArgument(schemaNode.getQName() != null,"QName for Data Schema Node cannot be NULL.");
        final String schemaNodeName = schemaNode.getQName().getLocalName();
        Preconditions.checkArgument(schemaNodeName != null,"Local Name of QName for Data Schema Node cannot be NULL.");

        final String genTypeName;
        if (prefix == null) {
            genTypeName = parseToClassName(schemaNodeName);
        } else {
            genTypeName = prefix + parseToClassName(schemaNodeName);
        }

        final GeneratedTypeBuilder newType = new GeneratedTypeBuilderImpl(packageName, genTypeName);
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
     * Creates the name of the getter method from <code>methodName</code>.
     *
     * @param methodName
     *            string with the name of the getter method
     * @return string with the name of the getter method for
     *         <code>methodName</code> in JAVA method format
     */
    private String getterMethodName(final String methodName) {
        final StringBuilder method = new StringBuilder();
        method.append("get");
        method.append(parseToClassName(methodName));
        return method.toString();
    }

    /**
     * Creates the name of the setter method from <code>methodName</code>.
     *
     * @param methodName
     *            string with the name of the setter method
     * @return string with the name of the setter method for
     *         <code>methodName</code> in JAVA method format
     */
    private String setterMethodName(final String methodName) {
        final StringBuilder method = new StringBuilder();
        method.append("set");
        method.append(parseToClassName(methodName));
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
        final MethodSignatureBuilder getMethod = interfaceBuilder.addMethod(getterMethodName(schemaNodeName));

        getMethod.setComment(comment);
        getMethod.setReturnType(returnType);

        return getMethod;
    }

    /**
     * Creates a method signature builder as a part of
     * <code>interfaceBuilder</code> for <code>schemaNodeName</code>
     *
     * The method signature builder is created for the setter method of
     * <code>schemaNodeName</code>. Also <code>comment</code>
     * <code>parameterType</code> data are added to the builder. The return type
     * of the method is set to <code>void</code>.
     *
     * @param interfaceBuilder
     *            generated type builder for which the setter method should be
     *            created
     * @param schemaNodeName
     *            string with schema node name. The name will be the part of the
     *            setter method name.
     * @param comment
     *            string with comment for the setter method
     * @param parameterType
     *            type which represents the type of the setter method input
     *            parameter
     * @return method signature builder which represents the setter method of
     *         <code>interfaceBuilder</code>
     */
    private MethodSignatureBuilder constructSetter(final GeneratedTypeBuilder interfaceBuilder,
            final String schemaNodeName, final String comment, final Type parameterType) {
        final MethodSignatureBuilder setMethod = interfaceBuilder.addMethod(setterMethodName(schemaNodeName));

        setMethod.setComment(comment);
        setMethod.addParameter(parameterType, parseToValidParamName(schemaNodeName));
        setMethod.setReturnType(Types.voidType());

        return setMethod;
    }

    private List<Type> listToGenType(final String basePackageName, final ListSchemaNode list) {
        Preconditions.checkArgument(basePackageName != null,"Package Name for Generated Type cannot be NULL.");
        Preconditions.checkArgument(list != null,"List Schema Node cannot be NULL.");

        final String packageName = packageNameForGeneratedType(basePackageName, list.getPath());
        // final GeneratedTypeBuilder typeBuilder =
        // resolveListTypeBuilder(packageName, list);
        final GeneratedTypeBuilder typeBuilder = addDefaultInterfaceDefinition(packageName, list);

        final List<String> listKeys = listKeys(list);
        GeneratedTOBuilder genTOBuilder = resolveListKeyTOBuilder(packageName, list);

        if (genTOBuilder != null) {
            ParameterizedType identifierMarker = Types.parameterizedTypeFor(Types.typeForClass(Identifier.class),
                    typeBuilder);
            ParameterizedType identifiableMarker = Types.parameterizedTypeFor(Types.typeForClass(Identifiable.class),
                    genTOBuilder);
            genTOBuilder.addImplementsType(identifierMarker);
            typeBuilder.addImplementsType(identifiableMarker);
        }
        final Set<DataSchemaNode> schemaNodes = list.getChildNodes();

        for (final DataSchemaNode schemaNode : schemaNodes) {
            if (schemaNode.isAugmenting()) {
                continue;
            }
            addSchemaNodeToListBuilders(basePackageName, schemaNode, typeBuilder, genTOBuilder, listKeys);
        }
        return typeBuildersToGenTypes(typeBuilder, genTOBuilder);
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
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>schemaNode</code> equals null</li>
     *             <li>if <code>typeBuilder</code> equals null</li>
     *             </ul>
     */
    private void addSchemaNodeToListBuilders(final String basePackageName, final DataSchemaNode schemaNode,
            final GeneratedTypeBuilder typeBuilder, final GeneratedTOBuilder genTOBuilder, final List<String> listKeys) {
        Preconditions.checkArgument(schemaNode != null,"Data Schema Node cannot be NULL.");

        Preconditions.checkArgument(typeBuilder != null,"Generated Type Builder cannot be NULL.");

        if (schemaNode instanceof LeafSchemaNode) {
            final LeafSchemaNode leaf = (LeafSchemaNode) schemaNode;
            final String leafName = leaf.getQName().getLocalName();
            if (!listKeys.contains(leafName)) {
                resolveLeafSchemaNodeAsMethod(typeBuilder, leaf);
            } else {
                resolveLeafSchemaNodeAsProperty(genTOBuilder, leaf, true);
            }
        } else if (schemaNode instanceof LeafListSchemaNode) {
            resolveLeafListSchemaNode(typeBuilder, (LeafListSchemaNode) schemaNode);
        } else if (schemaNode instanceof ContainerSchemaNode) {
            resolveContainerSchemaNode(basePackageName, typeBuilder, (ContainerSchemaNode) schemaNode);
        } else if (schemaNode instanceof ListSchemaNode) {
            resolveListSchemaNode(basePackageName, typeBuilder, (ListSchemaNode) schemaNode);
        }
    }

    private List<Type> typeBuildersToGenTypes(final GeneratedTypeBuilder typeBuilder, GeneratedTOBuilder genTOBuilder) {
        final List<Type> genTypes = new ArrayList<>();
        Preconditions.checkArgument(typeBuilder != null,"Generated Type Builder cannot be NULL.");

        if (genTOBuilder != null) {
            final GeneratedTransferObject genTO = genTOBuilder.toInstance();
            constructGetter(typeBuilder, "key", "Returns Primary Key of Yang List Type", genTO);
            genTypes.add(genTO);
        }
        genTypes.add(typeBuilder.toInstance());
        return genTypes;
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

        if (list.getKeyDefinition() != null) {
            final List<QName> keyDefinitions = list.getKeyDefinition();

            for (final QName keyDefinition : keyDefinitions) {
                listKeys.add(keyDefinition.getLocalName());
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
            if (list != null) {
                final String listName = list.getQName().getLocalName() + "Key";
                genTOBuilder = schemaNodeToTransferObjectBuilder(packageName, listName);
            }
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
     * @return generated TO builder for <code>typeDef</code>
     */
    private GeneratedTOBuilder addTOToTypeBuilder(TypeDefinition<?> typeDef, GeneratedTypeBuilder typeBuilder,
            String leafName, Module parentModule) {
        final String classNameFromLeaf = parseToClassName(leafName);
        List<GeneratedTOBuilder> genTOBuilders = new ArrayList<>();
        final String packageName = typeBuilder.getFullyQualifiedName();
        if (typeDef instanceof UnionTypeDefinition) {
            genTOBuilders.addAll(((TypeProviderImpl) typeProvider).provideGeneratedTOBuildersForUnionTypeDef(
                    packageName, typeDef, classNameFromLeaf));
        } else if (typeDef instanceof BitsTypeDefinition) {
            genTOBuilders.add(((TypeProviderImpl) typeProvider).provideGeneratedTOBuilderForBitsTypeDefinition(
                    packageName, typeDef, classNameFromLeaf));
        }
        if (genTOBuilders != null && !genTOBuilders.isEmpty()) {
            for (GeneratedTOBuilder genTOBuilder : genTOBuilders) {
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
     * {@code dataNodeContainer}. For every <i>use</i> is obtained coresponding
     * generated type from {@link BindingGeneratorImpl#allGroupings
     * allGroupings} which is adde as <i>implements type</i> to
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
        for (UsesNode usesNode : dataNodeContainer.getUses()) {
            if (usesNode.getGroupingPath() != null) {
                GeneratedType genType = allGroupings.get(usesNode.getGroupingPath());
                if (genType == null) {
                    throw new IllegalStateException("Grouping " + usesNode.getGroupingPath() + "is not resolved for "
                            + builder.getName());
                }
                builder.addImplementsType(genType);
            }
        }
        return builder;
    }

}
