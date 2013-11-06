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
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.model.util.UnionType;
import static com.google.common.base.Preconditions.*;
import static extension org.opendaylight.yangtools.binding.generator.util.Types.*;
import static org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil.*;
import static org.opendaylight.yangtools.binding.generator.util.BindingTypes.*;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.*;
import org.opendaylight.yangtools.yang.parser.util.ModuleDependencySort
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.model.api.UsesNode
import org.opendaylight.yangtools.yang.binding.annotations.RoutingContext
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.AnnotationTypeBuilder
import org.opendaylight.yangtools.yang.model.api.ModuleImport
import org.opendaylight.yangtools.yang.binding.DataContainer
import java.util.Iterator
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget
import java.util.Collection
import org.opendaylight.yangtools.yang.model.api.YangNode
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition
import org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil
import org.opendaylight.yangtools.sal.binding.model.api.Restrictions

public class BindingGeneratorImpl implements BindingGenerator {

    private final Map<Module, ModuleContext> genCtx = new HashMap()

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
        checkArgument(context !== null, "Schema Context reference cannot be NULL.");
        checkState(context.modules !== null, "Schema Context does not contain defined modules.");
        schemaContext = context;
        typeProvider = new TypeProviderImpl(context);
        val Set<Module> modules = context.modules;
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
        checkArgument(context !== null, "Schema Context reference cannot be NULL.");
        checkState(context.modules !== null, "Schema Context does not contain defined modules.");
        checkArgument(modules !== null, "Set of Modules cannot be NULL.");

        schemaContext = context;
        typeProvider = new TypeProviderImpl(context);
        val contextModules = ModuleDependencySort.sort(context.modules);
        genTypeBuilders = new HashMap();

        for (contextModule : contextModules) {
            moduleToGenTypes(contextModule, context);
        }
        for (contextModule : contextModules) {
            allAugmentsToGenTypes(contextModule);
        }

        val List<Type> filteredGenTypes = new ArrayList();
        for (Module m : modules) {
            filteredGenTypes.addAll(genCtx.get(m).generatedTypes);

        }
        genCtx.clear;

        return filteredGenTypes;
    }

    private def void moduleToGenTypes(Module m, SchemaContext context) {
        genCtx.put(m, new ModuleContext)
        allTypeDefinitionsToGenTypes(m)
        groupingsToGenTypes(m, m.groupings)
        rpcMethodsToGenType(m)
        allIdentitiesToGenTypes(m, context)
        notificationsToGenType(m)

        if (!m.childNodes.isEmpty()) {
            val moduleType = moduleToDataType(m)
            genCtx.get(m).addModuleNode(moduleType)
            val basePackageName = moduleNamespaceToPackageName(m);
            resolveDataSchemaNodes(m, basePackageName, moduleType, moduleType, m.childNodes)
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
     *             <li>if module equals null</li>
     *             <li>if name of module equals null</li>
     *             <li>if type definitions of module equal null</li>
     *             </ul>
     *
     */
    private def void allTypeDefinitionsToGenTypes(Module module) {
        checkArgument(module !== null, "Module reference cannot be NULL.");
        checkArgument(module.name !== null, "Module name cannot be NULL.");
        val it = new DataNodeIterator(module);
        val List<TypeDefinition<?>> typeDefinitions = it.allTypedefs;
        checkState(typeDefinitions !== null, '''Type Definitions for module «module.name» cannot be NULL.''');

        for (TypeDefinition<?> typedef : typeDefinitions) {
            if (typedef !== null) {
                val type = (typeProvider as TypeProviderImpl).generatedTypeForExtendedDefinitionType(typedef, typedef);
                if (type !== null) {
                    genCtx.get(module).addTypedefType(typedef.path, type)
                }
            }
        }
    }

    private def void containerToGenType(Module module, String basePackageName, GeneratedTypeBuilder parent,
        GeneratedTypeBuilder childOf, ContainerSchemaNode node) {
        if (node.augmenting || node.addedByUses) {
            return
        }
        val packageName = packageNameForGeneratedType(basePackageName, node.path)
        val genType = addDefaultInterfaceDefinition(packageName, node, childOf)
        constructGetter(parent, node.QName.localName, node.description, genType)
        genCtx.get(module).addChildNodeType(node.path, genType)
        resolveDataSchemaNodes(module, basePackageName, genType, genType, node.childNodes)
        groupingsToGenTypes(module, node.groupings)
        processUsesAugments(node, module)
    }

    private def void listToGenType(Module module, String basePackageName, GeneratedTypeBuilder parent,
        GeneratedTypeBuilder childOf, ListSchemaNode node) {
        if (node.augmenting || node.addedByUses) {
            return
        }
        val packageName = packageNameForGeneratedType(basePackageName, (node).path)
        val genType = addDefaultInterfaceDefinition(packageName, node, childOf)
        constructGetter(parent, node.QName.localName, node.description, Types.listTypeFor(genType))
        genCtx.get(module).addChildNodeType(node.path, genType)
        groupingsToGenTypes(module, node.groupings)
        processUsesAugments(node, module)

        val List<String> listKeys = listKeys(node);
        val genTOBuilder = resolveListKeyTOBuilder(packageName, node);

        if (genTOBuilder !== null) {
            val identifierMarker = IDENTIFIER.parameterizedTypeFor(genType);
            val identifiableMarker = IDENTIFIABLE.parameterizedTypeFor(genTOBuilder);
            genTOBuilder.addImplementsType(identifierMarker);
            genType.addImplementsType(identifiableMarker);
        }

        for (schemaNode : node.childNodes) {
            if (!schemaNode.augmenting) {
                addSchemaNodeToListBuilders(basePackageName, schemaNode, genType, genTOBuilder, listKeys, module);
            }
        }

        typeBuildersToGenTypes(module, genType, genTOBuilder);
    }

    private def void processUsesAugments(DataNodeContainer node, Module module) {
        val basePackageName = moduleNamespaceToPackageName(module);
        for (usesNode : node.uses) {
            for (augment : usesNode.augmentations) {
                augmentationToGenTypes(basePackageName, augment, module, usesNode);
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
     *             <li>if the module equals null</li>
     *             <li>if the name of module equals null</li>
     *             <li>if the set of child nodes equals null</li>
     *             </ul>
     *
     */
    private def void allAugmentsToGenTypes(Module module) {
        checkArgument(module !== null, "Module reference cannot be NULL.");
        checkArgument(module.name !== null, "Module name cannot be NULL.");
        if (module.childNodes === null) {
            throw new IllegalArgumentException(
                "Reference to Set of Augmentation Definitions in module " + module.name + " cannot be NULL.");
        }

        val basePackageName = moduleNamespaceToPackageName(module);
        val List<AugmentationSchema> augmentations = resolveAugmentations(module);
        for (augment : augmentations) {
            augmentationToGenTypes(basePackageName, augment, module, null);
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
     *             <ul>
     *             <li>if the module equals null</li>
     *             <li>if the set of augmentation equals null</li>
     *             </ul>
     *
     */
    private def List<AugmentationSchema> resolveAugmentations(Module module) {
        checkArgument(module !== null, "Module reference cannot be NULL.");
        checkState(module.augmentations !== null, "Augmentations Set cannot be NULL.");

        val Set<AugmentationSchema> augmentations = module.augmentations;
        val List<AugmentationSchema> sortedAugmentations = new ArrayList(augmentations);
        Collections.sort(sortedAugmentations,
            [ augSchema1, augSchema2 |
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
    private def GeneratedTypeBuilder moduleToDataType(Module module) {
        checkArgument(module !== null, "Module reference cannot be NULL.");

        val moduleDataTypeBuilder = moduleTypeBuilder(module, "Data");
        addImplementedInterfaceFromUses(module, moduleDataTypeBuilder);
        moduleDataTypeBuilder.addImplementsType(DATA_ROOT);
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
     *             <li>if the module equals null</li>
     *             <li>if the name of module equals null</li>
     *             <li>if the set of child nodes equals null</li>
     *             </ul>
     *
     */
    private def void rpcMethodsToGenType(Module module) {
        checkArgument(module !== null, "Module reference cannot be NULL.");
        checkArgument(module.name !== null, "Module name cannot be NULL.");
        checkArgument(module.childNodes !== null,
            "Reference to Set of RPC Method Definitions in module " + module.name + " cannot be NULL.");

        val basePackageName = moduleNamespaceToPackageName(module);
        val Set<RpcDefinition> rpcDefinitions = module.rpcs;
        if (rpcDefinitions.isEmpty()) {
            return;
        }

        val interfaceBuilder = moduleTypeBuilder(module, "Service");
        interfaceBuilder.addImplementsType(Types.typeForClass(RpcService));
        for (rpc : rpcDefinitions) {
            if (rpc !== null) {
                val rpcName = parseToClassName(rpc.QName.localName);
                val rpcMethodName = parseToValidParamName(rpcName);
                val method = interfaceBuilder.addMethod(rpcMethodName);
                val input = rpc.input;
                val output = rpc.output;

                if (input !== null) {
                    val inType = addRawInterfaceDefinition(basePackageName, input, rpcName);
                    addImplementedInterfaceFromUses(input, inType);
                    inType.addImplementsType(DATA_OBJECT);
                    inType.addImplementsType(augmentable(inType));
                    resolveDataSchemaNodes(module, basePackageName, inType, inType, input.childNodes);
                    genCtx.get(module).addChildNodeType(input.path, inType)
                    val inTypeInstance = inType.toInstance();
                    method.addParameter(inTypeInstance, "input");
                }

                var Type outTypeInstance = VOID;
                if (output !== null) {
                    val outType = addRawInterfaceDefinition(basePackageName, output, rpcName);
                    addImplementedInterfaceFromUses(output, outType);
                    outType.addImplementsType(DATA_OBJECT);
                    outType.addImplementsType(augmentable(outType));
                    resolveDataSchemaNodes(module, basePackageName, outType, outType, output.childNodes);
                    genCtx.get(module).addChildNodeType(output.path, outType)
                    outTypeInstance = outType.toInstance();
                }

                val rpcRes = Types.parameterizedTypeFor(Types.typeForClass(RpcResult), outTypeInstance);
                method.setReturnType(Types.parameterizedTypeFor(FUTURE, rpcRes));
            }
        }

        genCtx.get(module).addTopLevelNodeType(interfaceBuilder)
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
     *             <li>if the set of child nodes equals null</li>
     *             </ul>
     *
     */
    private def void notificationsToGenType(Module module) {
        checkArgument(module !== null, "Module reference cannot be NULL.");
        checkArgument(module.name !== null, "Module name cannot be NULL.");

        if (module.childNodes === null) {
            throw new IllegalArgumentException(
                "Reference to Set of Notification Definitions in module " + module.name + " cannot be NULL.");
        }
        val notifications = module.notifications;
        if(notifications.empty) return;

        val listenerInterface = moduleTypeBuilder(module, "Listener");
        listenerInterface.addImplementsType(BindingTypes.NOTIFICATION_LISTENER);
        val basePackageName = moduleNamespaceToPackageName(module);

        for (notification : notifications) {
            if (notification !== null) {
                processUsesAugments(notification, module);

                val notificationInterface = addDefaultInterfaceDefinition(basePackageName, notification,
                    BindingTypes.DATA_OBJECT);
                notificationInterface.addImplementsType(NOTIFICATION);
                genCtx.get(module).addChildNodeType(notification.path, notificationInterface)

                // Notification object
                resolveDataSchemaNodes(module, basePackageName, notificationInterface, notificationInterface,
                    notification.childNodes);

                listenerInterface.addMethod("on" + notificationInterface.name) //
                .setAccessModifier(AccessModifier.PUBLIC).addParameter(notificationInterface, "notification").
                    setReturnType(Types.VOID);
            }
        }

        genCtx.get(module).addTopLevelNodeType(listenerInterface)
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
    private def void allIdentitiesToGenTypes(Module module, SchemaContext context) {
        val Set<IdentitySchemaNode> schemaIdentities = module.identities;
        val basePackageName = moduleNamespaceToPackageName(module);

        if (schemaIdentities !== null && !schemaIdentities.isEmpty()) {
            for (identity : schemaIdentities) {
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
     * @param module current module
     * @param basePackageName
     *            string contains the module package name
     * @param identity
     *            IdentitySchemaNode which contains data about identity
     * @param context
     *            SchemaContext which is used to get package and name
     *            information about base of identity
     *
     */
    private def void identityToGenType(Module module, String basePackageName, IdentitySchemaNode identity,
        SchemaContext context) {
        if (identity === null) {
            return;
        }
        val packageName = packageNameForGeneratedType(basePackageName, identity.path);
        val genTypeName = parseToClassName(identity.QName.localName);
        val newType = new GeneratedTOBuilderImpl(packageName, genTypeName);
        val baseIdentity = identity.baseIdentity;
        if (baseIdentity === null) {
            newType.setExtendsType(Types.baseIdentityTO);
        } else {
            val baseIdentityParentModule = SchemaContextUtil.findParentModule(context, baseIdentity);
            val returnTypePkgName = moduleNamespaceToPackageName(baseIdentityParentModule);
            val returnTypeName = parseToClassName(baseIdentity.QName.localName);
            val gto = new GeneratedTOBuilderImpl(returnTypePkgName, returnTypeName).toInstance();
            newType.setExtendsType(gto);
        }
        newType.setAbstract(true);
        genCtx.get(module).addIdentityType(newType)
    }

    /**
     * Converts all <b>groupings</b> of the module to the list of
     * <code>Type</code> objects. Firstly are groupings sorted according mutual
     * dependencies. At least dependend (indepedent) groupings are in the list
     * saved at first positions. For every grouping the record is added to map
     * {@link BindingGeneratorImpl#allGroupings allGroupings}
     *
     * @param module
     *            current module
     * @param collection of groupings from which types will be generated
     *
     */
    private def void groupingsToGenTypes(Module module, Collection<GroupingDefinition> groupings) {
        val basePackageName = moduleNamespaceToPackageName(module);
        val List<GroupingDefinition> groupingsSortedByDependencies = new GroupingDefinitionDependencySort().sort(
            groupings);
        for (grouping : groupingsSortedByDependencies) {
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
     * @param module current module
     * @return GeneratedType which is generated from grouping (object of type
     *         <code>GroupingDefinition</code>)
     */
    private def void groupingToGenType(String basePackageName, GroupingDefinition grouping, Module module) {
        val packageName = packageNameForGeneratedType(basePackageName, grouping.path);
        val genType = addDefaultInterfaceDefinition(packageName, grouping);
        genCtx.get(module).addGroupingType(grouping.path, genType)
        resolveDataSchemaNodes(module, basePackageName, genType, genType, grouping.childNodes);
        groupingsToGenTypes(module, grouping.groupings);
        processUsesAugments(grouping, module);
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
    private def EnumTypeDefinition enumTypeDefFromExtendedType(TypeDefinition<?> typeDefinition) {
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
    private def EnumBuilder resolveInnerEnumFromTypeDefinition(EnumTypeDefinition enumTypeDef, String enumName,
        GeneratedTypeBuilder typeBuilder) {
        if ((enumTypeDef !== null) && (typeBuilder !== null) && (enumTypeDef.QName !== null) &&
            (enumTypeDef.QName.localName !== null)) {
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
    private def GeneratedTypeBuilder moduleTypeBuilder(Module module, String postfix) {
        checkArgument(module !== null, "Module reference cannot be NULL.");
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
     * @param module current module
     * @param parentUsesNode parent uses node of this augment (can be null if this augment is not defined under uses statement)
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>augmentPackageName</code> equals null</li>
     *             <li>if <code>augSchema</code> equals null</li>
     *             <li>if target path of <code>augSchema</code> equals null</li>
     *             </ul>
     */
    private def void augmentationToGenTypes(String augmentPackageName, AugmentationSchema augSchema, Module module,
        UsesNode parentUsesNode) {
        checkArgument(augmentPackageName !== null, "Package Name cannot be NULL.");
        checkArgument(augSchema !== null, "Augmentation Schema cannot be NULL.");
        checkState(augSchema.targetPath !== null,
            "Augmentation Schema does not contain Target Path (Target Path is NULL).");

        processUsesAugments(augSchema, module);

        // EVERY augmented interface will extends Augmentation<T> interface
        // and DataObject interface
        val targetPath = augSchema.targetPath;
        var targetSchemaNode = findDataSchemaNode(schemaContext, targetPath);
        if (targetSchemaNode instanceof DataSchemaNode && (targetSchemaNode as DataSchemaNode).isAddedByUses()) {
            if (parentUsesNode == null) {
                targetSchemaNode = findOriginal(targetSchemaNode as DataSchemaNode);
            } else {
                targetSchemaNode = findOriginalTargetFromGrouping(targetSchemaNode.QName.localName, parentUsesNode);
            }
            if (targetSchemaNode == null) {
                throw new NullPointerException(
                    "Failed to find target node from grouping for augmentation " + augSchema + " in module " +
                        module.name);
            }
        }

        if (targetSchemaNode !== null) {
            var targetTypeBuilder = findChildNodeByPath(targetSchemaNode.path)
            if (targetTypeBuilder === null) {
                targetTypeBuilder = findCaseByPath(targetSchemaNode.path)
            }
            if (targetTypeBuilder === null) {
                throw new NullPointerException("Target type not yet generated: " + targetSchemaNode);
            }
            if (!(targetSchemaNode instanceof ChoiceNode)) {
                var packageName = augmentPackageName;
                if (parentUsesNode != null) {
                    packageName = packageNameForGeneratedType(augmentPackageName, augSchema.targetPath);
                }
                val augTypeBuilder = addRawAugmentGenTypeDefinition(module, packageName, augmentPackageName,
                    targetTypeBuilder.toInstance, augSchema);
                genCtx.get(module).addAugmentType(augTypeBuilder)
            } else {
                generateTypesFromAugmentedChoiceCases(module, augmentPackageName, targetTypeBuilder.toInstance,
                    targetSchemaNode as ChoiceNode, augSchema.childNodes);
            }
        }
    }

    /**
     * Utility method which search for original node defined in grouping.
     */
    private def DataSchemaNode findOriginal(DataSchemaNode node) {
        var DataSchemaNode result = findCorrectTargetFromGrouping(node);
        if (result == null) {
            result = findCorrectTargetFromAugment(node);
            if (result != null) {
                if (result.addedByUses) {
                    result = findOriginal(result);
                }
            }
        }
        return result;
    }

    private def DataSchemaNode findCorrectTargetFromAugment(DataSchemaNode node) {
        if (!node.augmenting) {
            return null;
        }

        var String currentName = node.QName.localName;
        var tmpPath = new ArrayList<String>();
        var YangNode parent = node;
        var AugmentationSchema augment = null;
        do {
            parent = (parent as DataSchemaNode).parent;
            if (parent instanceof AugmentationTarget) {
                tmpPath.add(currentName);
                augment = findNodeInAugment((parent as AugmentationTarget).availableAugmentations, currentName);
                if (augment == null) {
                    currentName = (parent as DataSchemaNode).QName.localName;
                }
            }
        } while ((parent as DataSchemaNode).augmenting && augment == null);

        if (augment == null) {
            return null;
        } else {
            Collections.reverse(tmpPath);
            var Object actualParent = augment;
            var DataSchemaNode result = null;
            for (name : tmpPath) {
                if (actualParent instanceof DataNodeContainer) {
                    result = (actualParent as DataNodeContainer).getDataChildByName(name);
                    actualParent = (actualParent as DataNodeContainer).getDataChildByName(name);
                } else {
                    if (actualParent instanceof ChoiceNode) {
                        result = (actualParent as ChoiceNode).getCaseNodeByName(name);
                        actualParent = (actualParent as ChoiceNode).getCaseNodeByName(name);
                    }
                }
            }

            if (result.addedByUses) {
                result = findCorrectTargetFromGrouping(result);
            }

            return result;
        }
    }

    private def AugmentationSchema findNodeInAugment(Collection<AugmentationSchema> augments, String name) {
        for (augment : augments) {
            if (augment.getDataChildByName(name) != null) {
                return augment;
            }
        }
        return null;
    }

    private def DataSchemaNode findCorrectTargetFromGrouping(DataSchemaNode node) {
        if (node.path.path.size == 1) {

            // uses is under module statement
            val Module m = findParentModule(schemaContext, node);
            var DataSchemaNode result = null;
            for (u : m.uses) {
                var SchemaNode targetGrouping = findNodeInSchemaContext(schemaContext, u.groupingPath.path);
                if (!(targetGrouping instanceof GroupingDefinition)) {
                    throw new IllegalArgumentException("Failed to generate code for augment in " + u);
                }
                var gr = targetGrouping as GroupingDefinition;
                result = gr.getDataChildByName(node.QName.localName);
            }
            if (result == null) {
                throw new IllegalArgumentException("Failed to generate code for augment");
            }
            return result;
        } else {
            var DataSchemaNode result = null;
            var String currentName = node.QName.localName;
            var tmpPath = new ArrayList<String>();
            var YangNode parent = node.parent;
            do {
                tmpPath.add(currentName);
                val dataNodeParent = parent as DataNodeContainer;
                for (u : dataNodeParent.uses) {
                    var SchemaNode targetGrouping = findNodeInSchemaContext(schemaContext, u.groupingPath.path);
                    if (!(targetGrouping instanceof GroupingDefinition)) {
                        throw new IllegalArgumentException("Failed to generate code for augment in " + u);
                    }
                    var gr = targetGrouping as GroupingDefinition;
                    result = gr.getDataChildByName(currentName);
                }
                if (result == null) {
                    currentName = (parent as SchemaNode).QName.localName;
                    if (parent instanceof DataSchemaNode) {
                        parent = (parent as DataSchemaNode).parent;
                    } else {
                        parent = (parent as DataNodeContainer).parent;
                    }
                }
            } while (result == null && !(parent instanceof Module));

            if (result != null) {
                if (tmpPath.size == 1) {
                    return result;
                } else {
                    var DataSchemaNode newParent = result;
                    Collections.reverse(tmpPath);
                    tmpPath.remove(0);
                    for (name : tmpPath) {
                        newParent = (newParent as DataNodeContainer).getDataChildByName(name);
                    }
                    return newParent;
                }
            }

            return result;
        }
    }

    /**
     * Convenient method to find node added by uses statement.
     */
    private def DataSchemaNode findOriginalTargetFromGrouping(String targetSchemaNodeName, UsesNode parentUsesNode) {
        var SchemaNode targetGrouping = findNodeInSchemaContext(schemaContext, parentUsesNode.groupingPath.path);
        if (!(targetGrouping instanceof GroupingDefinition)) {
            throw new IllegalArgumentException("Failed to generate code for augment in " + parentUsesNode);
        }

        var grouping = targetGrouping as GroupingDefinition;
        var result = grouping.getDataChildByName(targetSchemaNodeName);
        if (result == null) {
            return null;
        }
        var boolean fromUses = result.addedByUses;

        var Iterator<UsesNode> groupingUses = grouping.uses.iterator;
        while (fromUses) {
            if (groupingUses.hasNext()) {
                grouping = findNodeInSchemaContext(schemaContext, groupingUses.next().groupingPath.path) as GroupingDefinition;
                result = grouping.getDataChildByName(targetSchemaNodeName);
                fromUses = result.addedByUses;
            } else {
                throw new NullPointerException("Failed to generate code for augment in " + parentUsesNode);
            }
        }

        return result;
    }

    /**
     * Returns a generated type builder for an augmentation.
     *
     * The name of the type builder is equal to the name of augmented node with
     * serial number as suffix.
     *
     * @param module current module
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
    private def GeneratedTypeBuilder addRawAugmentGenTypeDefinition(Module module, String augmentPackageName,
        String basePackageName, Type targetTypeRef, AugmentationSchema augSchema) {
        var Map<String, GeneratedTypeBuilder> augmentBuilders = genTypeBuilders.get(augmentPackageName);
        if (augmentBuilders === null) {
            augmentBuilders = new HashMap();
            genTypeBuilders.put(augmentPackageName, augmentBuilders);
        }
        val augIdentifier = getAugmentIdentifier(augSchema.unknownSchemaNodes);

        val augTypeName = if (augIdentifier !== null) {
                parseToClassName(augIdentifier)
            } else {
                augGenTypeName(augmentBuilders, targetTypeRef.name);
            }

        val augTypeBuilder = new GeneratedTypeBuilderImpl(augmentPackageName, augTypeName);

        augTypeBuilder.addImplementsType(DATA_OBJECT);
        augTypeBuilder.addImplementsType(Types.augmentationTypeFor(targetTypeRef));
        addImplementedInterfaceFromUses(augSchema, augTypeBuilder);

        augSchemaNodeToMethods(module, basePackageName, augTypeBuilder, augTypeBuilder, augSchema.childNodes);
        augmentBuilders.put(augTypeName, augTypeBuilder);
        return augTypeBuilder;
    }

    /**
     *
     * @param unknownSchemaNodes
     * @return nodeParameter of UnknownSchemaNode
     */
    private def String getAugmentIdentifier(List<UnknownSchemaNode> unknownSchemaNodes) {
        for (unknownSchemaNode : unknownSchemaNodes) {
            val nodeType = unknownSchemaNode.nodeType;
            if (AUGMENT_IDENTIFIER_NAME.equals(nodeType.localName) &&
                YANG_EXT_NAMESPACE.equals(nodeType.namespace.toString())) {
                return unknownSchemaNode.nodeParameter;
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
    private def String augGenTypeName(Map<String, GeneratedTypeBuilder> builders, String genTypeName) {
        var index = 1;
        while ((builders !== null) && builders.containsKey(genTypeName + index)) {
            index = index + 1;
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
     * @param module current module
     * @param basePackageName
     *            string contains the module package name
     * @param parent
     *            generated type builder which represents any node. The subnodes
     *            of this node are added to the <code>typeBuilder</code> as
     *            methods. The subnode can be of type leaf, leaf-list, list,
     *            container, choice.
     * @param childOf parent type
     * @param schemaNodes
     *            set of data schema nodes which are the children of the node
     *            for which <code>typeBuilder</code> was created
     * @return generated type builder which is the same builder as input
     *         parameter. The getter methods (representing child nodes) could be
     *         added to it.
     */
    private def GeneratedTypeBuilder resolveDataSchemaNodes(Module module, String basePackageName,
        GeneratedTypeBuilder parent, GeneratedTypeBuilder childOf, Set<DataSchemaNode> schemaNodes) {
        if ((schemaNodes !== null) && (parent !== null)) {
            for (schemaNode : schemaNodes) {
                if (!schemaNode.augmenting && !schemaNode.addedByUses) {
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
     * @param module current module
     * @param basePackageName
     *            string contains the module package name
     * @param typeBuilder
     *            generated type builder which represents any node. The subnodes
     *            of this node are added to the <code>typeBuilder</code> as
     *            methods. The subnode can be of type leaf, leaf-list, list,
     *            container, choice.
     * @param childOf parent type
     * @param schemaNodes
     *            set of data schema nodes which are the children of the node
     *            for which <code>typeBuilder</code> was created
     * @return generated type builder which is the same object as the input
     *         parameter <code>typeBuilder</code>. The getter method could be
     *         added to it.
     */
    private def GeneratedTypeBuilder augSchemaNodeToMethods(Module module, String basePackageName,
        GeneratedTypeBuilder typeBuilder, GeneratedTypeBuilder childOf, Set<DataSchemaNode> schemaNodes) {
        if ((schemaNodes !== null) && (typeBuilder !== null)) {
            for (schemaNode : schemaNodes) {
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
     * @param childOf parent type
     * @param module current module
     */
    private def void addSchemaNodeToBuilderAsMethod(String basePackageName, DataSchemaNode node,
        GeneratedTypeBuilder typeBuilder, GeneratedTypeBuilder childOf, Module module) {
        if (node !== null && typeBuilder !== null) {
            switch (node) {
                case node instanceof LeafSchemaNode:
                    resolveLeafSchemaNodeAsMethod(typeBuilder, node as LeafSchemaNode)
                case node instanceof LeafListSchemaNode:
                    resolveLeafListSchemaNode(typeBuilder, node as LeafListSchemaNode)
                case node instanceof ContainerSchemaNode:
                    containerToGenType(module, basePackageName, typeBuilder, childOf, node as ContainerSchemaNode)
                case node instanceof ListSchemaNode:
                    listToGenType(module, basePackageName, typeBuilder, childOf, node as ListSchemaNode)
                case node instanceof ChoiceNode:
                    choiceToGeneratedType(module, basePackageName, typeBuilder, node as ChoiceNode)
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
     * @param module current module
     * @param basePackageName
     *            string with the module package name
     * @param parent parent type
     * @param childOf concrete parent for case child nodes
     * @param choiceNode
     *            choice node which is mapped to generated type. Also child
     *            nodes - cases are mapped to generated types.
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>basePackageName</code> equals null</li>
     *             <li>if <code>choiceNode</code> equals null</li>
     *             </ul>
     *
     */
    private def void choiceToGeneratedType(Module module, String basePackageName, GeneratedTypeBuilder parent,
        ChoiceNode choiceNode) {
        checkArgument(basePackageName !== null, "Base Package Name cannot be NULL.");
        checkArgument(choiceNode !== null, "Choice Schema Node cannot be NULL.");

        val packageName = packageNameForGeneratedType(basePackageName, choiceNode.path);
        val choiceTypeBuilder = addRawInterfaceDefinition(packageName, choiceNode);
        constructGetter(parent, choiceNode.QName.localName, choiceNode.description, choiceTypeBuilder);
        choiceTypeBuilder.addImplementsType(DataContainer.typeForClass);
        genCtx.get(module).addChildNodeType(choiceNode.path, choiceTypeBuilder)
        generateTypesFromChoiceCases(module, basePackageName, parent, choiceTypeBuilder.toInstance, choiceNode);
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
    private def void generateTypesFromChoiceCases(Module module, String basePackageName,
        GeneratedTypeBuilder choiceParent, Type refChoiceType, ChoiceNode choiceNode) {
        checkArgument(basePackageName !== null, "Base Package Name cannot be NULL.");
        checkArgument(refChoiceType !== null, "Referenced Choice Type cannot be NULL.");
        checkArgument(choiceNode !== null, "ChoiceNode cannot be NULL.");

        val Set<ChoiceCaseNode> caseNodes = choiceNode.cases;
        if (caseNodes == null) {
            return
        }

        for (caseNode : caseNodes) {
            if (caseNode !== null && !caseNode.isAddedByUses() && !caseNode.isAugmenting()) {
                val packageName = packageNameForGeneratedType(basePackageName, caseNode.path);
                val caseTypeBuilder = addDefaultInterfaceDefinition(packageName, caseNode);
                caseTypeBuilder.addImplementsType(refChoiceType);
                genCtx.get(module).addCaseType(caseNode.path, caseTypeBuilder)
                val Set<DataSchemaNode> caseChildNodes = caseNode.childNodes;
                if (caseChildNodes !== null) {
                    val parentNode = choiceNode.parent;
                    var SchemaNode parent;
                    if (parentNode instanceof AugmentationSchema) {
                        val augSchema = parentNode as AugmentationSchema;
                        val targetPath = augSchema.targetPath;
                        var targetSchemaNode = findDataSchemaNode(schemaContext, targetPath);
                        if (targetSchemaNode instanceof DataSchemaNode &&
                            (targetSchemaNode as DataSchemaNode).isAddedByUses()) {
                            targetSchemaNode = findOriginal(targetSchemaNode as DataSchemaNode);
                            if (targetSchemaNode == null) {
                                throw new NullPointerException(
                                    "Failed to find target node from grouping for augmentation " + augSchema +
                                        " in module " + module.name);
                            }
                        }
                        parent = targetSchemaNode as SchemaNode
                    } else {
                        parent = choiceNode.parent as SchemaNode;
                    }
                    var GeneratedTypeBuilder childOfType = findChildNodeByPath(parent.path)
                    resolveDataSchemaNodes(module, basePackageName, caseTypeBuilder, childOfType, caseChildNodes);
                }
            }

            processUsesAugments(caseNode, module);
        }
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
    private def void generateTypesFromAugmentedChoiceCases(Module module, String basePackageName, Type targetType,
        ChoiceNode targetNode, Set<DataSchemaNode> augmentedNodes) {
        checkArgument(basePackageName !== null, "Base Package Name cannot be NULL.");
        checkArgument(targetType !== null, "Referenced Choice Type cannot be NULL.");
        checkArgument(augmentedNodes !== null, "Set of Choice Case Nodes cannot be NULL.");

        for (caseNode : augmentedNodes) {
            if (caseNode !== null) {
                val packageName = packageNameForGeneratedType(basePackageName, caseNode.path);
                val caseTypeBuilder = addDefaultInterfaceDefinition(packageName, caseNode);
                caseTypeBuilder.addImplementsType(targetType);

                val SchemaNode parent = targetNode.parent as SchemaNode;
                var GeneratedTypeBuilder childOfType = null;
                if (parent instanceof Module) {
                    childOfType = genCtx.get(parent as Module).moduleNode
                } else if (parent instanceof ChoiceCaseNode) {
                    childOfType = findCaseByPath(parent.path)
                } else if (parent instanceof DataSchemaNode || parent instanceof NotificationDefinition) {
                    childOfType = findChildNodeByPath(parent.path)
                } else if (parent instanceof GroupingDefinition) {
                    childOfType = findGroupingByPath(parent.path);
                }

                if (childOfType == null) {
                    throw new IllegalArgumentException("Failed to find parent type of choice " + targetNode);
                }

                if (caseNode instanceof DataNodeContainer) {
                    val DataNodeContainer dataNodeCase = caseNode as DataNodeContainer;
                    val Set<DataSchemaNode> childNodes = dataNodeCase.childNodes;
                    if (childNodes !== null) {
                        resolveDataSchemaNodes(module, basePackageName, caseTypeBuilder, childOfType, childNodes);
                    }
                } else {
                    val ChoiceCaseNode node = targetNode.getCaseNodeByName(caseNode.getQName().getLocalName());
                    val Set<DataSchemaNode> childNodes = node.childNodes;
                    if (childNodes !== null) {
                        resolveDataSchemaNodes(module, basePackageName, caseTypeBuilder, childOfType, childNodes);
                    }
                }

                genCtx.get(module).addCaseType(caseNode.path, caseTypeBuilder)
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
     * @return boolean value
     *         <ul>
     *         <li>false - if <code>leaf</code> or <code>typeBuilder</code> are
     *         null</li>
     *         <li>true - in other cases</li>
     *         </ul>
     */
    private def boolean resolveLeafSchemaNodeAsMethod(GeneratedTypeBuilder typeBuilder, LeafSchemaNode leaf) {
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
                    val enumTypeDef = typeDef as EnumTypeDefinition;
                    val enumBuilder = resolveInnerEnumFromTypeDefinition(enumTypeDef, leafName, typeBuilder);

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
                    val Restrictions restrictions = BindingGeneratorUtil.getRestrictions(typeDef);
                    returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, leaf, restrictions);
                }
                if (returnType !== null) {
                    val MethodSignatureBuilder getter = constructGetter(typeBuilder, leafName, leafDesc, returnType);
                    processContextRefExtension(leaf, getter, parentModule);
                    return true;
                }
            }
        }
        return false;
    }

    private def void processContextRefExtension(LeafSchemaNode leaf, MethodSignatureBuilder getter, Module module) {
        for (node : leaf.unknownSchemaNodes) {
            val nodeType = node.nodeType;
            if ("context-reference".equals(nodeType.localName)) {
                val nodeParam = node.nodeParameter;
                var IdentitySchemaNode identity = null;
                var String basePackageName = null;
                val String[] splittedElement = nodeParam.split(":");
                if (splittedElement.length == 1) {
                    identity = findIdentityByName(module.identities, splittedElement.get(0));
                    basePackageName = moduleNamespaceToPackageName(module);
                } else if (splittedElement.length == 2) {
                    var prefix = splittedElement.get(0);
                    val Module dependentModule = findModuleFromImports(module.imports, prefix)
                    if (dependentModule == null) {
                        throw new IllegalArgumentException(
                            "Failed to process context-reference: unknown prefix " + prefix);
                    }
                    identity = findIdentityByName(dependentModule.identities, splittedElement.get(1));
                    basePackageName = moduleNamespaceToPackageName(dependentModule);
                } else {
                    throw new IllegalArgumentException(
                        "Failed to process context-reference: unknown identity " + nodeParam);
                }
                if (identity == null) {
                    throw new IllegalArgumentException(
                        "Failed to process context-reference: unknown identity " + nodeParam);
                }

                val Class<RoutingContext> clazz = typeof(RoutingContext);
                val AnnotationTypeBuilder rc = getter.addAnnotation(clazz.package.name, clazz.simpleName);
                val packageName = packageNameForGeneratedType(basePackageName, identity.path);
                val genTypeName = parseToClassName(identity.QName.localName);
                rc.addParameter("value", packageName + "." + genTypeName + ".class");
            }
        }
    }

    private def IdentitySchemaNode findIdentityByName(Set<IdentitySchemaNode> identities, String name) {
        for (id : identities) {
            if (id.QName.localName.equals(name)) {
                return id;
            }
        }
        return null;
    }

    private def Module findModuleFromImports(Set<ModuleImport> imports, String prefix) {
        for (imp : imports) {
            if (imp.prefix.equals(prefix)) {
                return schemaContext.findModuleByName(imp.moduleName, imp.revision);
            }
        }
        return null;
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
    private def boolean resolveLeafSchemaNodeAsProperty(GeneratedTOBuilder toBuilder, LeafSchemaNode leaf,
        boolean isReadOnly) {
        if ((leaf !== null) && (toBuilder !== null)) {
            val leafName = leaf.QName.localName;
            var String leafDesc = leaf.description;
            if (leafDesc === null) {
                leafDesc = "";
            }

            if (leafName !== null) {
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
    private def boolean resolveLeafListSchemaNode(GeneratedTypeBuilder typeBuilder, LeafListSchemaNode node) {
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

    private def GeneratedTypeBuilder addDefaultInterfaceDefinition(String packageName, SchemaNode schemaNode) {
        return addDefaultInterfaceDefinition(packageName, schemaNode, null);
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
     * @param parent parent type (can be null)
     * @return generated type builder <code>schemaNode</code>
     */
    private def GeneratedTypeBuilder addDefaultInterfaceDefinition(String packageName, SchemaNode schemaNode,
        Type parent) {
        val builder = addRawInterfaceDefinition(packageName, schemaNode, "");
        if (parent === null) {
            builder.addImplementsType(DATA_OBJECT);
        } else {
            builder.addImplementsType(BindingTypes.childOf(parent));
        }
        if (!(schemaNode instanceof GroupingDefinition)) {
            builder.addImplementsType(augmentable(builder));
        }

        if (schemaNode instanceof DataNodeContainer) {
            addImplementedInterfaceFromUses(schemaNode as DataNodeContainer, builder);
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
    private def GeneratedTypeBuilder addRawInterfaceDefinition(String packageName, SchemaNode schemaNode) {
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
     * @param prefix return type name prefix
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
    private def GeneratedTypeBuilder addRawInterfaceDefinition(String packageName, SchemaNode schemaNode,
        String prefix) {
        checkArgument(schemaNode !== null, "Data Schema Node cannot be NULL.");
        checkArgument(packageName !== null, "Package Name for Generated Type cannot be NULL.");
        checkArgument(schemaNode.QName !== null, "QName for Data Schema Node cannot be NULL.");
        val schemaNodeName = schemaNode.QName.localName;
        checkArgument(schemaNodeName !== null, "Local Name of QName for Data Schema Node cannot be NULL.");

        var String genTypeName;
        if (prefix === null) {
            genTypeName = parseToClassName(schemaNodeName);
        } else {
            genTypeName = prefix + parseToClassName(schemaNodeName);
        }

        //FIXME: Validation of name conflict
        val newType = new GeneratedTypeBuilderImpl(packageName, genTypeName);
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
     * @param returnType return type
     * @return string with the name of the getter method for
     *         <code>methodName</code> in JAVA method format
     */
    private def String getterMethodName(String methodName, Type returnType) {
        val method = new StringBuilder();
        if (BOOLEAN.equals(returnType)) {
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
    private def MethodSignatureBuilder constructGetter(GeneratedTypeBuilder interfaceBuilder, String schemaNodeName,
        String comment, Type returnType) {
        val getMethod = interfaceBuilder.addMethod(getterMethodName(schemaNodeName, returnType));
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
     * @param module current module
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>schemaNode</code> equals null</li>
     *             <li>if <code>typeBuilder</code> equals null</li>
     *             </ul>
     */
    private def void addSchemaNodeToListBuilders(String basePackageName, DataSchemaNode schemaNode,
        GeneratedTypeBuilder typeBuilder, GeneratedTOBuilder genTOBuilder, List<String> listKeys, Module module) {
        checkArgument(schemaNode !== null, "Data Schema Node cannot be NULL.");
        checkArgument(typeBuilder !== null, "Generated Type Builder cannot be NULL.");

        if (schemaNode instanceof LeafSchemaNode) {
            val leaf = schemaNode as LeafSchemaNode;
            val leafName = leaf.QName.localName;
            if (!listKeys.contains(leafName)) {
                resolveLeafSchemaNodeAsMethod(typeBuilder, leaf);
            } else {
                resolveLeafSchemaNodeAsProperty(genTOBuilder, leaf, true);
            }
        } else if (!schemaNode.addedByUses) {
            if (schemaNode instanceof LeafListSchemaNode) {
                resolveLeafListSchemaNode(typeBuilder, schemaNode as LeafListSchemaNode);
            } else if (schemaNode instanceof ContainerSchemaNode) {
                containerToGenType(module, basePackageName, typeBuilder, typeBuilder, schemaNode as ContainerSchemaNode);
            } else if (schemaNode instanceof ChoiceNode) {
                choiceToGeneratedType(module, basePackageName, typeBuilder, schemaNode as ChoiceNode);
            } else if (schemaNode instanceof ListSchemaNode) {
                listToGenType(module, basePackageName, typeBuilder, typeBuilder, schemaNode as ListSchemaNode);
            }
        }
    }

    private def typeBuildersToGenTypes(Module module, GeneratedTypeBuilder typeBuilder, GeneratedTOBuilder genTOBuilder) {
        checkArgument(typeBuilder !== null, "Generated Type Builder cannot be NULL.");

        if (genTOBuilder !== null) {
            val genTO = genTOBuilder.toInstance();
            constructGetter(typeBuilder, "key", "Returns Primary Key of Yang List Type", genTO);
            genCtx.get(module).addGeneratedTOBuilder(genTOBuilder)
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
    private def listKeys(ListSchemaNode list) {
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
    private def GeneratedTOBuilder resolveListKeyTOBuilder(String packageName, ListSchemaNode list) {
        var GeneratedTOBuilder genTOBuilder = null;
        if ((list.keyDefinition !== null) && (!list.keyDefinition.isEmpty())) {
            if (list !== null) {
                val listName = list.QName.localName + "Key";
                val String genTOName = parseToClassName(listName);
                genTOBuilder = new GeneratedTOBuilderImpl(packageName, genTOName);
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
     * @param leaf
     * @param parentModule
     * @return generated TO builder for <code>typeDef</code>
     */
    private def GeneratedTOBuilder addTOToTypeBuilder(TypeDefinition<?> typeDef, GeneratedTypeBuilder typeBuilder,
        String leafName, LeafSchemaNode leaf, Module parentModule) {
        val classNameFromLeaf = parseToClassName(leafName);
        val List<GeneratedTOBuilder> genTOBuilders = new ArrayList();
        val packageName = typeBuilder.fullyQualifiedName;
        if (typeDef instanceof UnionTypeDefinition) {
            genTOBuilders.addAll(
                (typeProvider as TypeProviderImpl).
                    provideGeneratedTOBuildersForUnionTypeDef(packageName, (typeDef as UnionTypeDefinition),
                        classNameFromLeaf, leaf));
        } else if (typeDef instanceof BitsTypeDefinition) {
            genTOBuilders.add(
                ((typeProvider as TypeProviderImpl) ).
                    provideGeneratedTOBuilderForBitsTypeDefinition(packageName, typeDef, classNameFromLeaf));
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
    private def addImplementedInterfaceFromUses(DataNodeContainer dataNodeContainer, GeneratedTypeBuilder builder) {
        for (usesNode : dataNodeContainer.uses) {
            if (usesNode.groupingPath !== null) {
                val genType = findGroupingByPath(usesNode.groupingPath).toInstance
                if (genType === null) {
                    throw new IllegalStateException(
                        "Grouping " + usesNode.groupingPath + "is not resolved for " + builder.name);
                }
                builder.addImplementsType(genType);
            }
        }
        return builder;
    }

    private def GeneratedTypeBuilder findChildNodeByPath(SchemaPath path) {
        for (ctx : genCtx.values) {
            var result = ctx.getChildNode(path)
            if (result !== null) {
                return result
            }
        }
        return null
    }

    private def GeneratedTypeBuilder findGroupingByPath(SchemaPath path) {
        for (ctx : genCtx.values) {
            var result = ctx.getGrouping(path)
            if (result !== null) {
                return result
            }
        }
        return null
    }

    private def GeneratedTypeBuilder findCaseByPath(SchemaPath path) {
        for (ctx : genCtx.values) {
            var result = ctx.getCase(path)
            if (result !== null) {
                return result
            }
        }
        return null
    }

}
