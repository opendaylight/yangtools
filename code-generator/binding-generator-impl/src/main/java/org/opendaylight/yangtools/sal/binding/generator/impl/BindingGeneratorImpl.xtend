
/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.binding.generator.util.BindingTypes;
import org.opendaylight.yangtools.binding.generator.util.ReferencedTypeImpl;
import org.opendaylight.yangtools.binding.generator.util.Types;
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedTOBuilderImpl;
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedTypeBuilderImpl;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.generator.spi.TypeProvider;
import org.opendaylight.yangtools.sal.binding.model.api.AccessModifier;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.EnumBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.sal.binding.yang.types.GroupingDefinitionDependencySort;
import org.opendaylight.yangtools.sal.binding.yang.types.TypeProviderImpl;
import org.opendaylight.yangtools.yang.binding.RpcService;
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
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.DataNodeIterator;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.model.util.UnionType;
import static com.google.common.base.Preconditions.*;
import static extension org.opendaylight.yangtools.binding.generator.util.Types.*;
import static org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil.*;
import static org.opendaylight.yangtools.binding.generator.util.BindingTypes.*;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.*;


public class BindingGeneratorImpl implements BindingGenerator {

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
    private var TypeProvider typeProvider;

    /**
     * Holds reference to schema context to resolve data of augmented elemnt
     * when creating augmentation builder
     */
    private var SchemaContext schemaContext;

    /**
     * Each grouping which is converted from schema node to generated type is
     * added to this map with its Schema path as key to make it easier to get
     * reference to it. In schema nodes in <code>uses</code> attribute there is
     * only Schema Path but when building list of implemented interfaces for
     * Schema node the object of type <code>Type</code> is required. So in this
     * case is used this map.
     */
    private val allGroupings = new HashMap<SchemaPath, GeneratedType>();
    
    
    private val yangToJavaMapping = new HashMap<SchemaPath, Type>();

    /**
     * Constant with the concrete name of namespace.
     */
    private val static String YANG_EXT_NAMESPACE = "urn:opendaylight:yang:extension:yang-ext";

    /**
     * Constant with the concrete name of identifier.
     */
    private val static String AUGMENT_IDENTIFIER_NAME = "augment-identifier";

    
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
    override generateTypes(SchemaContext context) {
        checkArgument(context !== null,"Schema Context reference cannot be NULL.");
        checkState(context.modules !== null,"Schema Context does not contain defined modules.");
        val List<Type> generatedTypes = new ArrayList();
        schemaContext = context;
        typeProvider = new TypeProviderImpl(context);
        val Set<Module> modules = context.modules;
        genTypeBuilders = new HashMap();
        for (module : modules) {

            generatedTypes.addAll(allGroupingsToGenTypes(module));

            if (false == module.childNodes.isEmpty()) {
                generatedTypes.add(moduleToDataType(module));
            }
            generatedTypes.addAll(allTypeDefinitionsToGenTypes(module));
            generatedTypes.addAll(allContainersToGenTypes(module));
            generatedTypes.addAll(allListsToGenTypes(module));
            generatedTypes.addAll(allChoicesToGenTypes(module));
            generatedTypes.addAll(allRPCMethodsToGenType(module));
            generatedTypes.addAll(allNotificationsToGenType(module));
            generatedTypes.addAll(allIdentitiesToGenTypes(module, context));
        }
        for (module : modules) {
            generatedTypes.addAll(allAugmentsToGenTypes(module));
            
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
    override generateTypes(SchemaContext context, Set<Module> modules) {
        checkArgument(context !== null,"Schema Context reference cannot be NULL.");
        checkState(context.modules !== null,"Schema Context does not contain defined modules.");
        checkArgument(modules !== null,"Set of Modules cannot be NULL.");

        val List<Type> filteredGenTypes = new ArrayList();
        schemaContext = context;
        typeProvider = new TypeProviderImpl(context);
        val Set<Module> contextModules = context.modules;
        genTypeBuilders = new HashMap();
        
        for (contextModule : contextModules) {
            val List<Type> generatedTypes = new ArrayList();

            generatedTypes.addAll(allGroupingsToGenTypes(contextModule));
            if (false == contextModule.childNodes.isEmpty()) {
                generatedTypes.add(moduleToDataType(contextModule));
            }
            generatedTypes.addAll(allTypeDefinitionsToGenTypes(contextModule));
            generatedTypes.addAll(allContainersToGenTypes(contextModule));
            generatedTypes.addAll(allListsToGenTypes(contextModule));
            generatedTypes.addAll(allChoicesToGenTypes(contextModule));
            generatedTypes.addAll(allRPCMethodsToGenType(contextModule));
            generatedTypes.addAll(allNotificationsToGenType(contextModule));
            generatedTypes.addAll(allIdentitiesToGenTypes(contextModule, context));
            
            if (modules.contains(contextModule)) {
                filteredGenTypes.addAll(generatedTypes);
            }
        }
        for (contextModule : contextModules) {
            val generatedTypes = (allAugmentsToGenTypes(contextModule));
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
    private def List<Type> allTypeDefinitionsToGenTypes( Module module) {
        checkArgument(module !== null,"Module reference cannot be NULL.");
        checkArgument(module.name !== null,"Module name cannot be NULL.");
        val Set<TypeDefinition<?>> typeDefinitions = module.typeDefinitions;
        checkState(typeDefinitions !== null,'''Type Definitions for module «module.name» cannot be NULL.''');

        
        val List<Type> generatedTypes = new ArrayList();
        for ( TypeDefinition<?> typedef : typeDefinitions) {
            if (typedef !== null) {
                val type = (typeProvider as TypeProviderImpl).generatedTypeForExtendedDefinitionType(typedef, typedef);
                if ((type !== null) && !generatedTypes.contains(type)) {
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
    private def List<Type> allContainersToGenTypes( Module module) {
        checkArgument(module !== null,"Module reference cannot be NULL.");

        checkArgument(module.name !== null,"Module name cannot be NULL.");

        if (module.childNodes === null) {
            throw new IllegalArgumentException("Reference to Set of Child Nodes in module " + module.name
                    + " cannot be NULL.");
        }

        val List<Type> generatedTypes = new ArrayList();
        val it = new DataNodeIterator(module);
        val List<ContainerSchemaNode> schemaContainers = it.allContainers();
        val basePackageName = moduleNamespaceToPackageName(module);
        for (container : schemaContainers) {
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
    private def List<Type> allListsToGenTypes( Module module) {
        checkArgument(module !== null,"Module reference cannot be NULL.");
        checkArgument(module.name !== null,"Module name cannot be NULL.");

        if (module.childNodes === null) {
            throw new IllegalArgumentException("Reference to Set of Child Nodes in module " + module.name
                    + " cannot be NULL.");
        }

        val List<Type> generatedTypes = new ArrayList();
        val it = new DataNodeIterator(module);
        val List<ListSchemaNode> schemaLists = it.allLists();
        val basePackageName = moduleNamespaceToPackageName(module);
        if (schemaLists !== null) {
            for (list : schemaLists) {
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
    private def List<GeneratedType> allChoicesToGenTypes( Module module) {
        checkArgument(module !== null,"Module reference cannot be NULL.");
        checkArgument(module.name !== null,"Module name cannot be NULL.");

        val it = new DataNodeIterator(module);
        val choiceNodes = it.allChoices();
        val basePackageName = moduleNamespaceToPackageName(module);

        val List<GeneratedType> generatedTypes = new ArrayList();
        for (choice : choiceNodes) {
            if ((choice !== null) && !choice.isAddedByUses()) {
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
    private def List<Type> allAugmentsToGenTypes( Module module) {
        checkArgument(module !== null,"Module reference cannot be NULL.");
        checkArgument(module.name !== null,"Module name cannot be NULL.");
        if (module.childNodes === null) {
            throw new IllegalArgumentException("Reference to Set of Augmentation Definitions in module "
                    + module.name + " cannot be NULL.");
        }

        val List<Type> generatedTypes = new ArrayList();
        val basePackageName = moduleNamespaceToPackageName(module);
        val List<AugmentationSchema> augmentations = resolveAugmentations(module);
        for (augment : augmentations) {
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
    private def List<AugmentationSchema> resolveAugmentations( Module module) {
        checkArgument(module !== null,"Module reference cannot be NULL.");
        checkState(module.augmentations !== null,"Augmentations Set cannot be NULL.");

        val Set<AugmentationSchema> augmentations = module.augmentations;
        val List<AugmentationSchema> sortedAugmentations = new ArrayList(augmentations);
        Collections.sort(sortedAugmentations, [augSchema1, augSchema2 |

                if (augSchema1.targetPath.path.size() > augSchema2.targetPath.path.size()) {
                    return 1;
                } else if (augSchema1.targetPath.path.size() < augSchema2.targetPath.path.size()) {
                    return -1;
                }
                return 0;
           ]);
        return sortedAugmentations;
    }

    /**
     * Converts whole <b>module</b> to <code>GeneratedType</code> object.
     * Firstly is created the module builder object from which is vally
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
    private def GeneratedType moduleToDataType( Module module) {
        checkArgument(module !== null,"Module reference cannot be NULL.");

        val moduleDataTypeBuilder = moduleTypeBuilder(module, "Data");
        addImplementedInterfaceFromUses(module, moduleDataTypeBuilder);
        moduleDataTypeBuilder.addImplementsType(DATA_ROOT);

        val basePackageName = moduleNamespaceToPackageName(module);
        if (moduleDataTypeBuilder !== null) {
            val Set<DataSchemaNode> dataNodes = module.childNodes;
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
    private def List<Type> allRPCMethodsToGenType( Module module) {
        checkArgument(module !== null,"Module reference cannot be NULL.");

        checkArgument(module.name !== null,"Module name cannot be NULL.");

        if (module.childNodes === null) {
            throw new IllegalArgumentException("Reference to Set of RPC Method Definitions in module "
                    + module.name + " cannot be NULL.");
        }

        val basePackageName = moduleNamespaceToPackageName(module);
        val Set<RpcDefinition> rpcDefinitions = module.rpcs;

        if (rpcDefinitions.isEmpty()) {
            return Collections.emptyList();
        }

        val List<Type> genRPCTypes = new ArrayList();
        val interfaceBuilder = moduleTypeBuilder(module, "Service");
        interfaceBuilder.addImplementsType(Types.typeForClass(RpcService));
        for (rpc : rpcDefinitions) {
            if (rpc !== null) {

                val rpcName = parseToClassName(rpc.QName.localName);
                val rpcMethodName = parseToValidParamName(rpcName);
                val method = interfaceBuilder.addMethod(rpcMethodName);

                val rpcInOut = new ArrayList();

                val input = rpc.input;
                val output = rpc.output;

                if (input !== null) {
                    rpcInOut.add(new DataNodeIterator(input));
                    val inType = addRawInterfaceDefinition(basePackageName, input, rpcName);
                    addImplementedInterfaceFromUses(input, inType);
                    inType.addImplementsType(DATA_OBJECT);
                    inType.addImplementsType(augmentable(inType));
                    resolveDataSchemaNodes(basePackageName, inType, input.childNodes);
                    val inTypeInstance = inType.toInstance();
                    genRPCTypes.add(inTypeInstance);
                    method.addParameter(inTypeInstance, "input");
                }

                var Type outTypeInstance = VOID;
                if (output !== null) {
                    rpcInOut.add(new DataNodeIterator(output));
                    val outType = addRawInterfaceDefinition(basePackageName, output, rpcName);
                    addImplementedInterfaceFromUses(output, outType);
                    outType.addImplementsType(DATA_OBJECT);
                    outType.addImplementsType(augmentable(outType));

                    resolveDataSchemaNodes(basePackageName, outType, output.childNodes);
                    outTypeInstance = outType.toInstance();
                    genRPCTypes.add(outTypeInstance);

                }

                val rpcRes = Types.parameterizedTypeFor(Types.typeForClass(RpcResult), outTypeInstance);
                method.setReturnType(Types.parameterizedTypeFor(FUTURE, rpcRes));
                for (iter : rpcInOut) {
                    val List<ContainerSchemaNode> nContainers = iter.allContainers();
                    if ((nContainers !== null) && !nContainers.isEmpty()) {
                        for (container : nContainers) {
                            if (!container.isAddedByUses()) {
                                genRPCTypes.add(containerToGenType(basePackageName, container));
                            }
                        }
                    }
                    val List<ListSchemaNode> nLists = iter.allLists();
                    if ((nLists !== null) && !nLists.isEmpty()) {
                        for (list : nLists) {
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
    private def List<Type> allNotificationsToGenType( Module module) {
        checkArgument(module !== null,"Module reference cannot be NULL.");

        checkArgument(module.name !== null,"Module name cannot be NULL.");

        if (module.childNodes === null) {
            throw new IllegalArgumentException("Reference to Set of Notification Definitions in module "
                    + module.name + " cannot be NULL.");
        }
        val notifications = module.notifications;
        if(notifications.isEmpty()) return Collections.emptyList();
        
        val listenerInterface = moduleTypeBuilder(module, "Listener");
        listenerInterface.addImplementsType(BindingTypes.NOTIFICATION_LISTENER);
        
        
        
        val basePackageName = moduleNamespaceToPackageName(module);
        val List<Type> generatedTypes = new ArrayList();
        
        
        for ( notification : notifications) {
            if (notification !== null) {
                val iter = new DataNodeIterator(notification);

                // Containers
                for (node : iter.allContainers()) {
                    if (!node.isAddedByUses()) {
                        generatedTypes.add(containerToGenType(basePackageName, node));
                    }
                }
                // Lists
                for (node : iter.allLists()) {
                    if (!node.isAddedByUses()) {
                        generatedTypes.addAll(listToGenType(basePackageName, node));
                    }
                }
                val notificationInterface = addDefaultInterfaceDefinition(basePackageName,
                        notification);
                notificationInterface.addImplementsType(NOTIFICATION);
                // Notification object
                resolveDataSchemaNodes(basePackageName, notificationInterface, notification.childNodes);
                
                listenerInterface.addMethod("on"+notificationInterface.name) //
                    .setAccessModifier(AccessModifier.PUBLIC)
                    .addParameter(notificationInterface, "notification")
                    .setReturnType(Types.VOID);
                
                generatedTypes.add(notificationInterface.toInstance());
            }
        }
        generatedTypes.add(listenerInterface.toInstance());
        return generatedTypes;
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
    private def List<Type> allIdentitiesToGenTypes( Module module, SchemaContext context) {
        val List<Type> genTypes = new ArrayList();

        val Set<IdentitySchemaNode> schemaIdentities = module.identities;

        val basePackageName = moduleNamespaceToPackageName(module);

        if (schemaIdentities !== null && !schemaIdentities.isEmpty()) {
            for (identity : schemaIdentities) {
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
    private def GeneratedType identityToGenType(String basePackageName, IdentitySchemaNode identity,
            SchemaContext context) {
        if (identity === null) {
            return null;
        }

        val packageName = packageNameForGeneratedType(basePackageName, identity.path);
        val genTypeName = parseToClassName(identity.QName.localName);
        val newType = new GeneratedTOBuilderImpl(packageName, genTypeName);

        val baseIdentity = identity.baseIdentity;
        if (baseIdentity !== null) {
            val baseIdentityParentModule = SchemaContextUtil.findParentModule(context, baseIdentity);

            val returnTypePkgName = moduleNamespaceToPackageName(baseIdentityParentModule);
            val returnTypeName = parseToClassName(baseIdentity.QName.localName);

            val gto = new GeneratedTOBuilderImpl(returnTypePkgName, returnTypeName).toInstance();
            newType.setExtendsType(gto);
        } else {
            newType.setExtendsType(Types.baseIdentityTO);
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
    private def List<Type> allGroupingsToGenTypes( Module module) {
        checkArgument(module !== null,"Module parameter can not be null");
        val List<Type> genTypes = new ArrayList();
        val basePackageName = moduleNamespaceToPackageName(module);
        val Set<GroupingDefinition> groupings = module.groupings;
        val List<GroupingDefinition> groupingsSortedByDependencies = new GroupingDefinitionDependencySort().sort(groupings);

        for (grouping : groupingsSortedByDependencies) {
            val genType = groupingToGenType(basePackageName, grouping);
            genTypes.add(genType);
            val schemaPath = grouping.path;
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
    private def GeneratedType groupingToGenType( String basePackageName, GroupingDefinition grouping) {
        if (grouping === null) {
            return null;
        }

        val packageName = packageNameForGeneratedType(basePackageName, grouping.path);
        val Set<DataSchemaNode> schemaNodes = grouping.childNodes;
        val typeBuilder = addDefaultInterfaceDefinition(packageName, grouping);

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
    private def EnumTypeDefinition enumTypeDefFromExtendedType( TypeDefinition<?> typeDefinition) {
        if (typeDefinition !== null) {
            if (typeDefinition.baseType instanceof EnumTypeDefinition) {
                return typeDefinition.baseType as EnumTypeDefinition;
            } else if (typeDefinition.baseType instanceof ExtendedType) {
                return enumTypeDefFromExtendedType(typeDefinition.baseType);
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
    private def EnumBuilder resolveInnerEnumFromTypeDefinition( EnumTypeDefinition enumTypeDef, String enumName,
            GeneratedTypeBuilder typeBuilder) {
        if ((enumTypeDef !== null) && (typeBuilder !== null) && (enumTypeDef.QName !== null)
                && (enumTypeDef.QName.localName !== null)) {

            val enumerationName = parseToClassName(enumName);
            val enumBuilder = typeBuilder.addEnumeration(enumerationName);
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
    private def GeneratedTypeBuilder moduleTypeBuilder( Module module, String postfix) {
        checkArgument(module !== null,"Module reference cannot be NULL.");
        val packageName = moduleNamespaceToPackageName(module);
        val moduleName = parseToClassName(module.name) + postfix;

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
    private def List<Type> augmentationToGenTypes(String augmentPackageName, AugmentationSchema augSchema) {
        checkArgument(augmentPackageName !== null,"Package Name cannot be NULL.");
        checkArgument(augSchema !== null,"Augmentation Schema cannot be NULL.");
        checkState(augSchema.targetPath !== null,"Augmentation Schema does not contain Target Path (Target Path is NULL).");
        val List<Type> genTypes = new ArrayList();
        // EVERY augmented interface will extends Augmentation<T> interface
        // and DataObject interface!!!
        val targetPath = augSchema.targetPath;
        val targetSchemaNode = findDataSchemaNode(schemaContext, targetPath);
        var targetType = yangToJavaMapping.get(targetSchemaNode.path);
        if(targetType == null) {
            // FIXME: augmentation should be added as last, all types should already be generated
            // and have assigned Java Types,
            val targetModule = findParentModule(schemaContext, targetSchemaNode);
            val targetBasePackage = moduleNamespaceToPackageName(targetModule);
            val typePackage = packageNameForGeneratedType(targetBasePackage, targetSchemaNode.getPath());
            val targetSchemaNodeName = targetSchemaNode.getQName().getLocalName();
            val typeName = parseToClassName(targetSchemaNodeName);
            targetType = new ReferencedTypeImpl(typePackage,typeName);
        }
        if (targetSchemaNode !== null) {
            val augChildNodes = augSchema.childNodes;
            if (!(targetSchemaNode instanceof ChoiceNode)) {
                val augTypeBuilder = addRawAugmentGenTypeDefinition(augmentPackageName,
                        targetType, augSchema);
                val augType = augTypeBuilder.toInstance();
                genTypes.add(augType);
            } else {
                val choiceTarget = targetSchemaNode as ChoiceNode;
                val choiceCaseNodes = choiceTarget.cases;
                genTypes.addAll(generateTypesFromAugmentedChoiceCases(augmentPackageName, targetType,
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
    private def GeneratedTypeBuilder addRawAugmentGenTypeDefinition( String augmentPackageName,
            Type targetTypeRef, AugmentationSchema augSchema) {
        var Map<String, GeneratedTypeBuilder> augmentBuilders = genTypeBuilders.get(augmentPackageName);
        if (augmentBuilders === null) {
            augmentBuilders = new HashMap();
            genTypeBuilders.put(augmentPackageName, augmentBuilders);
        }
        val augIdentifier = getAugmentIdentifier(augSchema.unknownSchemaNodes);

        val augTypeName = if (augIdentifier !== null ) { 
            parseToClassName(augIdentifier)
        } else {
            augGenTypeName(augmentBuilders, targetTypeRef.name);
        }
        val Set<DataSchemaNode> augChildNodes = augSchema.childNodes;

        val augTypeBuilder = new GeneratedTypeBuilderImpl(augmentPackageName, augTypeName);

        augTypeBuilder.addImplementsType(DATA_OBJECT);
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
    private def String getAugmentIdentifier(List<UnknownSchemaNode> unknownSchemaNodes) {
        for (unknownSchemaNode : unknownSchemaNodes) {
            val nodeType = unknownSchemaNode.nodeType;
            if (AUGMENT_IDENTIFIER_NAME.equals(nodeType.localName)
                    && YANG_EXT_NAMESPACE.equals(nodeType.namespace.toString())) {
                return unknownSchemaNode.nodeParameter;
            }
        }
        return null;
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
    private def List<Type> augmentationBodyToGenTypes( String augBasePackageName,
            Set<DataSchemaNode> augChildNodes) {
        val List<Type> genTypes = new ArrayList();
        val List<DataNodeIterator> augSchemaIts = new ArrayList();
        for (childNode : augChildNodes) {
            if (childNode instanceof DataNodeContainer) {
                augSchemaIts.add(new DataNodeIterator(childNode as DataNodeContainer));

                if (childNode instanceof ContainerSchemaNode) {
                    genTypes.add(containerToGenType(augBasePackageName, childNode as ContainerSchemaNode));
                } else if (childNode instanceof ListSchemaNode) {
                    genTypes.addAll(listToGenType(augBasePackageName, childNode as ListSchemaNode));
                }
            } else if (childNode instanceof ChoiceNode) {
                val choice = childNode as ChoiceNode;
                for (caseNode : choice.cases) {
                    augSchemaIts.add(new DataNodeIterator(caseNode));
                }
                genTypes.addAll(choiceToGeneratedType(augBasePackageName, childNode as ChoiceNode));
            }
        }

        for (it : augSchemaIts) {
            val List<ContainerSchemaNode> augContainers = it.allContainers();
            val List<ListSchemaNode> augLists = it.allLists();
            val List<ChoiceNode> augChoices = it.allChoices();

            if (augContainers !== null) {
                for (container : augContainers) {
                    genTypes.add(containerToGenType(augBasePackageName, container));
                }
            }
            if (augLists !== null) {
                for (list : augLists) {
                    genTypes.addAll(listToGenType(augBasePackageName, list));
                }
            }
            if (augChoices !== null) {
                for (choice : augChoices) {
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
    private def String augGenTypeName( Map<String, GeneratedTypeBuilder> builders, String genTypeName) {
        var index = 1;
        while ((builders !== null) && builders.containsKey(genTypeName + index)) {
            index = index + 1;
        }
        return genTypeName + index;
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
    private def GeneratedType containerToGenType( String basePackageName, ContainerSchemaNode containerNode) {
        if (containerNode === null) {
            return null;
        }

        val packageName = packageNameForGeneratedType(basePackageName, containerNode.path);
        val schemaNodes = containerNode.childNodes;
        val typeBuilder = addDefaultInterfaceDefinition(packageName, containerNode);

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
    private def GeneratedTypeBuilder resolveDataSchemaNodes( String basePackageName,
            GeneratedTypeBuilder typeBuilder, Set<DataSchemaNode> schemaNodes) {
        if ((schemaNodes !== null) && (typeBuilder !== null)) {
            for (schemaNode : schemaNodes) {
                if (!schemaNode.isAugmenting() && !schemaNode.isAddedByUses()) {
                    addSchemaNodeToBuilderAsMethod(basePackageName, schemaNode, typeBuilder);
                }
                
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
    private def GeneratedTypeBuilder augSchemaNodeToMethods( String basePackageName,
             GeneratedTypeBuilder typeBuilder,  Set<DataSchemaNode> schemaNodes) {
        if ((schemaNodes !== null) && (typeBuilder !== null)) {
            for (schemaNode : schemaNodes) {
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
    private def void addSchemaNodeToBuilderAsMethod( String basePackageName,  DataSchemaNode node,
             GeneratedTypeBuilder typeBuilder) {
        if (node !== null && typeBuilder !== null) {
            switch(node) {
                case node instanceof LeafSchemaNode: resolveLeafSchemaNodeAsMethod(typeBuilder, node as LeafSchemaNode)
                case node instanceof LeafListSchemaNode: resolveLeafListSchemaNode(typeBuilder, node as LeafListSchemaNode)
                case node instanceof ContainerSchemaNode: resolveContainerSchemaNode(basePackageName, typeBuilder, node as ContainerSchemaNode)
                case node instanceof ListSchemaNode: resolveListSchemaNode(basePackageName, typeBuilder, node as ListSchemaNode)
                case node instanceof ChoiceNode: resolveChoiceSchemaNode(basePackageName, typeBuilder, node as ChoiceNode)
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
    private def void resolveChoiceSchemaNode( String basePackageName,  GeneratedTypeBuilder typeBuilder,
             ChoiceNode choiceNode) {
        checkArgument(basePackageName !== null,"Base Package Name cannot be NULL.");
        checkArgument(typeBuilder !== null,"Generated Type Builder cannot be NULL.");
        checkArgument(choiceNode !== null,"Choice Schema Node cannot be NULL.");

        val choiceName = choiceNode.QName.localName;
        if (choiceName !== null && !choiceNode.isAddedByUses()) {
            val packageName = packageNameForGeneratedType(basePackageName, choiceNode.path);
            val choiceType = addDefaultInterfaceDefinition(packageName, choiceNode);
            constructGetter(typeBuilder, choiceName, choiceNode.description, choiceType);
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
    private def List<GeneratedType> choiceToGeneratedType( String basePackageName,  ChoiceNode choiceNode) {
        checkArgument(basePackageName !== null,"Base Package Name cannot be NULL.");
        checkArgument(choiceNode !== null,"Choice Schema Node cannot be NULL.");

        val List<GeneratedType> generatedTypes = new ArrayList();
        val packageName = packageNameForGeneratedType(basePackageName, choiceNode.path);
        val choiceTypeBuilder = addRawInterfaceDefinition(packageName, choiceNode);
        //choiceTypeBuilder.addImplementsType(DATA_OBJECT);
        val choiceType = choiceTypeBuilder.toInstance();

        generatedTypes.add(choiceType);
        val Set<ChoiceCaseNode> caseNodes = choiceNode.cases;
        if ((caseNodes !== null) && !caseNodes.isEmpty()) {
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
    private def List<GeneratedType> generateTypesFromChoiceCases( String basePackageName,  Type refChoiceType,
             Set<ChoiceCaseNode> caseNodes) {
        checkArgument(basePackageName !== null,"Base Package Name cannot be NULL.");
        checkArgument(refChoiceType !== null,"Referenced Choice Type cannot be NULL.");
        checkArgument(caseNodes !== null,"Set of Choice Case Nodes cannot be NULL.");

        val List<GeneratedType> generatedTypes = new ArrayList();
        for (caseNode : caseNodes) {
            if (caseNode !== null && !caseNode.isAddedByUses() && !caseNode.isAugmenting()) {
                val packageName = packageNameForGeneratedType(basePackageName, caseNode.path);
                val caseTypeBuilder = addDefaultInterfaceDefinition(packageName, caseNode);
                caseTypeBuilder.addImplementsType(refChoiceType);

                val Set<DataSchemaNode> childNodes = caseNode.childNodes;
                if (childNodes !== null) {
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
    private def List<GeneratedType> generateTypesFromAugmentedChoiceCases( String basePackageName,
             Type refChoiceType,  Set<ChoiceCaseNode> caseNodes) {
        checkArgument(basePackageName !== null,"Base Package Name cannot be NULL.");
        checkArgument(refChoiceType !== null,"Referenced Choice Type cannot be NULL.");
        checkArgument(caseNodes !== null,"Set of Choice Case Nodes cannot be NULL.");

        val List<GeneratedType> generatedTypes = new ArrayList();
        for (caseNode : caseNodes) {
            if (caseNode !== null && caseNode.isAugmenting()) {
                val packageName = packageNameForGeneratedType(basePackageName, caseNode.path);
                val caseTypeBuilder = addDefaultInterfaceDefinition(packageName, caseNode);
                caseTypeBuilder.addImplementsType(refChoiceType);

                val Set<DataSchemaNode> childNodes = caseNode.childNodes;
                if (childNodes !== null) {
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
    private def boolean resolveLeafSchemaNodeAsMethod( GeneratedTypeBuilder typeBuilder,  LeafSchemaNode leaf) {
        if ((leaf !== null) && (typeBuilder !== null)) {
            val leafName = leaf.QName.localName;
            var String leafDesc = leaf.description;
            if (leafDesc === null) {
                leafDesc = "";
            }

            val parentModule = findParentModule(schemaContext, leaf);
            if (leafName !== null && !leaf.isAddedByUses()) {
                val TypeDefinition<?> typeDef = leaf.type;

                var Type returnType = null;
                if (typeDef instanceof EnumTypeDefinition) {
                    returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, leaf);
                    val enumTypeDef = enumTypeDefFromExtendedType(typeDef);
                    val enumBuilder = resolveInnerEnumFromTypeDefinition(enumTypeDef, leafName,
                            typeBuilder);

                    if (enumBuilder !== null) {
                        returnType = new ReferencedTypeImpl(enumBuilder.packageName, enumBuilder.name);
                    }
                    (typeProvider as TypeProviderImpl).putReferencedType(leaf.path, returnType);
                } else if (typeDef instanceof UnionType) {
                    val genTOBuilder = addTOToTypeBuilder(typeDef, typeBuilder, leafName, leaf, parentModule);
                    if (genTOBuilder !== null) {
                        returnType = new ReferencedTypeImpl(genTOBuilder.packageName, genTOBuilder.name);
                    }
                } else if (typeDef instanceof BitsTypeDefinition) {
                    val genTOBuilder = addTOToTypeBuilder(typeDef, typeBuilder, leafName, leaf, parentModule);
                    if (genTOBuilder !== null) {
                        returnType = new ReferencedTypeImpl(genTOBuilder.packageName, genTOBuilder.name);
                    }
                } else {
                    returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, leaf);
                }
                if (returnType !== null) {
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
    private def boolean resolveLeafSchemaNodeAsProperty( GeneratedTOBuilder toBuilder, LeafSchemaNode leaf,
            boolean isReadOnly) {
        if ((leaf !== null) && (toBuilder !== null)) {
            val leafName = leaf.QName.localName;
            var String leafDesc = leaf.description;
            if (leafDesc === null) {
                leafDesc = "";
            }

            if (leafName !== null && !leaf.isAddedByUses()) {
                val TypeDefinition<?> typeDef = leaf.type;

                // TODO: properly resolve enum types
                val returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, leaf);

                if (returnType !== null) {
                    val propBuilder = toBuilder.addProperty(parseToClassName(leafName));

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
    private def boolean resolveLeafListSchemaNode( GeneratedTypeBuilder typeBuilder, LeafListSchemaNode node) {
        if ((node !== null) && (typeBuilder !== null)) {
            val nodeName = node.QName.localName;
            var String nodeDesc = node.description;
            if (nodeDesc === null) {
                nodeDesc = "";
            }

            if (nodeName !== null && !node.isAddedByUses()) {
                val TypeDefinition<?> type = node.type;
                val listType = Types.listTypeFor(typeProvider.javaTypeForSchemaDefinitionType(type, node));

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
    private def boolean resolveContainerSchemaNode( String basePackageName, GeneratedTypeBuilder typeBuilder,
            ContainerSchemaNode containerNode) {
        if ((containerNode !== null) && (typeBuilder !== null)) {
            val nodeName = containerNode.QName.localName;

            if (nodeName !== null && !containerNode.isAddedByUses()) {
                val packageName = packageNameForGeneratedType(basePackageName, containerNode.path);

                val rawGenType = addDefaultInterfaceDefinition(packageName, containerNode);
                constructGetter(typeBuilder, nodeName, containerNode.description, rawGenType);

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
    private def boolean resolveListSchemaNode( String basePackageName, GeneratedTypeBuilder typeBuilder,
            ListSchemaNode listNode) {
        if ((listNode !== null) && (typeBuilder !== null)) {
            val listName = listNode.QName.localName;

            if (listName !== null && !listNode.isAddedByUses()) {
                val packageName = packageNameForGeneratedType(basePackageName, listNode.path);
                val rawGenType = addDefaultInterfaceDefinition(packageName, listNode);
                constructGetter(typeBuilder, listName, listNode.description, Types.listTypeFor(rawGenType));
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
    private def GeneratedTypeBuilder addDefaultInterfaceDefinition( String packageName, SchemaNode schemaNode) {
        val builder = addRawInterfaceDefinition(packageName, schemaNode, "");
        builder.addImplementsType(DATA_OBJECT);
        if (!(schemaNode instanceof GroupingDefinition)) {
            builder.addImplementsType(augmentable(builder));
        }

        if (schemaNode instanceof DataNodeContainer) {
            addImplementedInterfaceFromUses( schemaNode as DataNodeContainer, builder);
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
    private def GeneratedTypeBuilder addRawInterfaceDefinition( String packageName,  SchemaNode schemaNode) {
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
    private def GeneratedTypeBuilder addRawInterfaceDefinition( String packageName,  SchemaNode schemaNode,
             String prefix) {
        checkArgument(schemaNode !== null,"Data Schema Node cannot be NULL.");
        checkArgument(packageName !== null,"Package Name for Generated Type cannot be NULL.");
        checkArgument(schemaNode.QName !== null,"QName for Data Schema Node cannot be NULL.");
        val schemaNodeName = schemaNode.QName.localName;
        checkArgument(schemaNodeName !== null,"Local Name of QName for Data Schema Node cannot be NULL.");

        var String genTypeName;
        if (prefix === null) {
            genTypeName = parseToClassName(schemaNodeName);
        } else {
            genTypeName = prefix + parseToClassName(schemaNodeName);
        }
        //FIXME: Validation of name conflict
        val newType = new GeneratedTypeBuilderImpl(packageName, genTypeName);
        yangToJavaMapping.put(schemaNode.path,newType);
        if (!genTypeBuilders.containsKey(packageName)) {
            val Map<String, GeneratedTypeBuilder> builders = new HashMap();
            builders.put(genTypeName, newType);
            genTypeBuilders.put(packageName, builders);
        } else {
            val Map<String, GeneratedTypeBuilder> builders = genTypeBuilders.get(packageName);
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
    private def String getterMethodName( String methodName,Type returnType) {
        val method = new StringBuilder();
        if(BOOLEAN.equals(returnType)) {
            method.append("is");
        } else {
            method.append("get");
        }
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
    private def MethodSignatureBuilder constructGetter( GeneratedTypeBuilder interfaceBuilder,
             String schemaNodeName,  String comment,  Type returnType) {

        val getMethod = interfaceBuilder.addMethod(getterMethodName(schemaNodeName,returnType));

        getMethod.setComment(comment);
        getMethod.setReturnType(returnType);

        return getMethod;
    }

    private def listToGenType( String basePackageName,  ListSchemaNode list) {
        checkArgument(basePackageName !== null,"Package Name for Generated Type cannot be NULL.");
        checkArgument(list !== null,"List Schema Node cannot be NULL.");

        val packageName = packageNameForGeneratedType(basePackageName, list.path);
        // val typeBuilder =
        // resolveListTypeBuilder(packageName, list);
        val typeBuilder = addDefaultInterfaceDefinition(packageName, list);

        val List<String> listKeys = listKeys(list);
        val genTOBuilder = resolveListKeyTOBuilder(packageName, list);

        if (genTOBuilder !== null) {
            val identifierMarker = IDENTIFIER.parameterizedTypeFor(typeBuilder);
            val identifiableMarker = IDENTIFIABLE.parameterizedTypeFor(genTOBuilder);
            genTOBuilder.addImplementsType(identifierMarker);
            typeBuilder.addImplementsType(identifiableMarker);
        }
        val schemaNodes = list.childNodes;

        for (schemaNode : schemaNodes) {
            if (!schemaNode.isAugmenting()) {
                addSchemaNodeToListBuilders(basePackageName, schemaNode, typeBuilder, genTOBuilder, listKeys);
            }
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
    private def void addSchemaNodeToListBuilders( String basePackageName,  DataSchemaNode schemaNode,
             GeneratedTypeBuilder typeBuilder,  GeneratedTOBuilder genTOBuilder,  List<String> listKeys) {
        checkArgument(schemaNode !== null,"Data Schema Node cannot be NULL.");

        checkArgument(typeBuilder !== null,"Generated Type Builder cannot be NULL.");

        if (schemaNode instanceof LeafSchemaNode) {
            val leaf = schemaNode as LeafSchemaNode;
            val leafName = leaf.QName.localName;
            if (!listKeys.contains(leafName)) {
                resolveLeafSchemaNodeAsMethod(typeBuilder, leaf);
            } else {
                resolveLeafSchemaNodeAsProperty(genTOBuilder, leaf, true);
            }
        } else if (schemaNode instanceof LeafListSchemaNode) {
            resolveLeafListSchemaNode(typeBuilder,  schemaNode as LeafListSchemaNode);
        } else if (schemaNode instanceof ContainerSchemaNode) {
            resolveContainerSchemaNode(basePackageName, typeBuilder, schemaNode as ContainerSchemaNode);
        } else if (schemaNode instanceof ListSchemaNode) {
            resolveListSchemaNode(basePackageName, typeBuilder, schemaNode as ListSchemaNode);
        }
    }

    private def typeBuildersToGenTypes( GeneratedTypeBuilder typeBuilder, GeneratedTOBuilder genTOBuilder) {
        val List<Type> genTypes = new ArrayList();
        checkArgument(typeBuilder !== null,"Generated Type Builder cannot be NULL.");

        if (genTOBuilder !== null) {
            val genTO = genTOBuilder.toInstance();
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
    private def listKeys( ListSchemaNode list) {
        val List<String> listKeys = new ArrayList();

        if (list.keyDefinition !== null) {
            val keyDefinitions = list.keyDefinition;
            for (keyDefinition : keyDefinitions) {
                listKeys.add(keyDefinition.localName);
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
    private def GeneratedTOBuilder resolveListKeyTOBuilder( String packageName,  ListSchemaNode list) {
        var GeneratedTOBuilder genTOBuilder = null;
        if ((list.keyDefinition !== null) && (!list.keyDefinition.isEmpty())) {
            if (list !== null) {
                val listName = list.QName.localName + "Key";
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
    private def GeneratedTOBuilder addTOToTypeBuilder(TypeDefinition<?> typeDef, GeneratedTypeBuilder typeBuilder,
            String leafName, LeafSchemaNode leaf, Module parentModule) {
        val classNameFromLeaf = parseToClassName(leafName);
        val List<GeneratedTOBuilder> genTOBuilders = new ArrayList();
        val packageName = typeBuilder.fullyQualifiedName;
        if (typeDef instanceof UnionTypeDefinition) {
            genTOBuilders.addAll((typeProvider as TypeProviderImpl).provideGeneratedTOBuildersForUnionTypeDef(
                    packageName, typeDef, classNameFromLeaf, leaf));
        } else if (typeDef instanceof BitsTypeDefinition) {
            genTOBuilders.add(((typeProvider as TypeProviderImpl) ).provideGeneratedTOBuilderForBitsTypeDefinition(
                    packageName, typeDef, classNameFromLeaf));
        }
        if (genTOBuilders !== null && !genTOBuilders.isEmpty()) {
            for (genTOBuilder : genTOBuilders) {
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
    private def addImplementedInterfaceFromUses( DataNodeContainer dataNodeContainer,
            GeneratedTypeBuilder builder) {
        for (usesNode : dataNodeContainer.uses) {
            if (usesNode.groupingPath !== null) {
                val genType = allGroupings.get(usesNode.groupingPath);
                if (genType === null) {
                    throw new IllegalStateException("Grouping " + usesNode.groupingPath + "is not resolved for "
                            + builder.name);
                }
                builder.addImplementsType(genType);
            }
        }
        return builder;
    }
}
