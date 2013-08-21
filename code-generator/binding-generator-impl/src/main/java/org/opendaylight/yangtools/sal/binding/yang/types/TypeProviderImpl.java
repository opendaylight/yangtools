/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.yang.types;

import static org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil.*;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringEscapeUtils;
import org.opendaylight.yangtools.binding.generator.util.TypeConstants;
import org.opendaylight.yangtools.binding.generator.util.Types;
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.EnumerationBuilderImpl;
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedTOBuilderImpl;
import org.opendaylight.yangtools.sal.binding.generator.spi.TypeProvider;
import org.opendaylight.yangtools.sal.binding.model.api.Enumeration;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.EnumBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.model.util.StringType;
import org.opendaylight.yangtools.yang.model.util.UnionType;
import org.opendaylight.yangtools.yang.parser.util.ModuleDependencySort;

public final class TypeProviderImpl implements TypeProvider {
    /**
     * Contains the schema data red from YANG files.
     */
    private final SchemaContext schemaContext;

    /**
     * The outter map maps module names to the map of the types for the module.
     * The inner map maps the name of the concrete type to the JAVA
     * <code>Type</code> (usually it is generated TO).
     */
    private Map<String, Map<String, Type>> genTypeDefsContextMap;

    /**
     * The map which maps schema paths to JAVA <code>Type</code>.
     */
    private final Map<SchemaPath, Type> referencedTypes;

    /**
     * Creates new instance of class <code>TypeProviderImpl</code>.
     *
     * @param schemaContext
     *            contains the schema data red from YANG files
     * @throws IllegalArgumentException
     *             if <code>schemaContext</code> equal null.
     */
    public TypeProviderImpl(final SchemaContext schemaContext) {
        if (schemaContext == null) {
            throw new IllegalArgumentException("Schema Context cannot be null!");
        }

        this.schemaContext = schemaContext;
        this.genTypeDefsContextMap = new HashMap<>();
        this.referencedTypes = new HashMap<>();
        resolveTypeDefsFromContext();
    }

    /**
     * Puts <code>refType</code> to map with key <code>refTypePath</code>
     *
     * @param refTypePath
     *            schema path used as the map key
     * @param refType
     *            type which represents the map value
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>refTypePath</code> equal null</li>
     *             <li>if <code>refType</code> equal null</li>
     *             </ul>
     *
     */
    public void putReferencedType(final SchemaPath refTypePath, final Type refType) {
        if (refTypePath == null) {
            throw new IllegalArgumentException("Path reference of " + "Enumeration Type Definition cannot be NULL!");
        }

        if (refType == null) {
            throw new IllegalArgumentException("Reference to Enumeration " + "Type cannot be NULL!");
        }
        referencedTypes.put(refTypePath, refType);
    }

    /**
     *
     * Converts basic YANG type <code>type</code> to JAVA <code>Type</code>.
     *
     * @param type
     *            string with YANG name of type
     * @returns JAVA <code>Type</code> for YANG type <code>type</code>
     * @see org.opendaylight.controller.yang.model.type.provider.TypeProvider#
     *      javaTypeForYangType(java.lang.String)
     */
    @Override
    public Type javaTypeForYangType(String type) {
        Type t = BaseYangTypes.BASE_YANG_TYPES_PROVIDER.javaTypeForYangType(type);
        return t;
    }

    /**
     * Converts schema definition type <code>typeDefinition</code> to JAVA
     * <code>Type</code>
     *
     * @param typeDefinition
     *            type definition which is converted to JAVA type
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>typeDefinition</code> equal null</li>
     *             <li>if Q name of <code>typeDefinition</code> equal null</li>
     *             <li>if name of <code>typeDefinition</code> equal null</li>
     *             </ul>
     */
    @Override
    public Type javaTypeForSchemaDefinitionType(final TypeDefinition<?> typeDefinition) {
        Type returnType = null;
        if (typeDefinition == null) {
            throw new IllegalArgumentException("Type Definition cannot be NULL!");
        }
        if (typeDefinition.getQName() == null) {
            throw new IllegalArgumentException(
                    "Type Definition cannot have non specified QName (QName cannot be NULL!)");
        }
        if (typeDefinition.getQName().getLocalName() == null) {
            throw new IllegalArgumentException("Type Definitions Local Name cannot be NULL!");
        }
        final String typedefName = typeDefinition.getQName().getLocalName();
        if (typeDefinition instanceof ExtendedType) {
            final TypeDefinition<?> baseTypeDef = baseTypeDefForExtendedType(typeDefinition);

            if (baseTypeDef instanceof LeafrefTypeDefinition) {
                final LeafrefTypeDefinition leafref = (LeafrefTypeDefinition) baseTypeDef;
                returnType = provideTypeForLeafref(leafref);
            } else if (baseTypeDef instanceof IdentityrefTypeDefinition) {
                final IdentityrefTypeDefinition idref = (IdentityrefTypeDefinition) baseTypeDef;
                returnType = provideTypeForIdentityref(idref);
            } else if (baseTypeDef instanceof EnumTypeDefinition) {
                final EnumTypeDefinition enumTypeDef = (EnumTypeDefinition) baseTypeDef;
                returnType = provideTypeForEnum(enumTypeDef, typedefName);
            } else {
                final Module module = findParentModuleForTypeDefinition(schemaContext, typeDefinition);
                if (module != null) {
                    final Map<String, Type> genTOs = genTypeDefsContextMap.get(module.getName());
                    if (genTOs != null) {
                        returnType = genTOs.get(typedefName);
                    }
                    if (returnType == null) {
                        returnType = BaseYangTypes.BASE_YANG_TYPES_PROVIDER
                                .javaTypeForSchemaDefinitionType(baseTypeDef);
                    }
                }
            }
        } else {
            if (typeDefinition instanceof LeafrefTypeDefinition) {
                final LeafrefTypeDefinition leafref = (LeafrefTypeDefinition) typeDefinition;
                returnType = provideTypeForLeafref(leafref);
            } else if (typeDefinition instanceof IdentityrefTypeDefinition) {
                final IdentityrefTypeDefinition idref = (IdentityrefTypeDefinition) typeDefinition;
                returnType = provideTypeForIdentityref(idref);
            } else {
                returnType = BaseYangTypes.BASE_YANG_TYPES_PROVIDER.javaTypeForSchemaDefinitionType(typeDefinition);
            }
        }
        // TODO: add throw exception when we will be able to resolve ALL yang
        // types!
        // if (returnType == null) {
        // throw new IllegalArgumentException("Type Provider can't resolve " +
        // "type for specified Type Definition " + typedefName);
        // }
        return returnType;
    }

    /**
     * Seeks for identity reference <code>idref</code> the JAVA
     * <code>type</code>.<br />
     * <br />
     *
     * <i>Example:<br />
     * If identy which is referenced via <code>idref</code> has name <b>Idn</b>
     * then returning type is <b>{@code Class<? extends Idn>}</b></i>
     *
     * @param idref
     *            identityref type definition for which JAVA <code>Type</code>
     *            is sought
     * @return JAVA <code>Type</code> of the identity which is refrenced through
     *         <code>idref</code>
     */
    private Type provideTypeForIdentityref(IdentityrefTypeDefinition idref) {
        QName baseIdQName = idref.getIdentity();
        Module module = schemaContext.findModuleByNamespace(baseIdQName.getNamespace());
        IdentitySchemaNode identity = null;
        for (IdentitySchemaNode id : module.getIdentities()) {
            if (id.getQName().equals(baseIdQName)) {
                identity = id;
            }
        }
        if (identity == null) {
            throw new IllegalArgumentException("Target identity '" + baseIdQName + "' do not exists");
        }

        final String basePackageName = moduleNamespaceToPackageName(module);
        final String packageName = packageNameForGeneratedType(basePackageName, identity.getPath());
        final String genTypeName = parseToClassName(identity.getQName().getLocalName());

        Type baseType = Types.typeForClass(Class.class);
        Type paramType = Types.wildcardTypeFor(packageName, genTypeName);
        Type returnType = Types.parameterizedTypeFor(baseType, paramType);
        return returnType;
    }

    /**
     * Converts <code>typeDefinition</code> to concrete JAVA <code>Type</code>.
     *
     * @param typeDefinition
     *            type definition which should be converted to JAVA
     *            <code>Type</code>
     * @return JAVA <code>Type</code> which represents
     *         <code>typeDefinition</code>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>typeDefinition</code> equal null</li>
     *             <li>if Q name of <code>typeDefinition</code></li>
     *             <li>if name of <code>typeDefinition</code></li>
     *             </ul>
     */
    public Type generatedTypeForExtendedDefinitionType(final TypeDefinition<?> typeDefinition) {
        Type returnType = null;
        if (typeDefinition == null) {
            throw new IllegalArgumentException("Type Definition cannot be NULL!");
        }
        if (typeDefinition.getQName() == null) {
            throw new IllegalArgumentException(
                    "Type Definition cannot have non specified QName (QName cannot be NULL!)");
        }
        if (typeDefinition.getQName().getLocalName() == null) {
            throw new IllegalArgumentException("Type Definitions Local Name cannot be NULL!");
        }

        final String typedefName = typeDefinition.getQName().getLocalName();
        if (typeDefinition instanceof ExtendedType) {
            final TypeDefinition<?> baseTypeDef = baseTypeDefForExtendedType(typeDefinition);

            if (!(baseTypeDef instanceof LeafrefTypeDefinition) && !(baseTypeDef instanceof IdentityrefTypeDefinition)) {
                final Module module = findParentModuleForTypeDefinition(schemaContext, typeDefinition);

                if (module != null) {
                    final Map<String, Type> genTOs = genTypeDefsContextMap.get(module.getName());
                    if (genTOs != null) {
                        returnType = genTOs.get(typedefName);
                    }
                }
            }
        }
        return returnType;
    }

    /**
     * Gets base type definition for <code>extendTypeDef</code>. The method is
     * recursivelly called until non <code>ExtendedType</code> type is found.
     *
     * @param extendTypeDef
     *            type definition for which is the base type definition sought
     * @return type definition which is base type for <code>extendTypeDef</code>
     * @throws IllegalArgumentException
     *             if <code>extendTypeDef</code> equal null
     */
    private TypeDefinition<?> baseTypeDefForExtendedType(final TypeDefinition<?> extendTypeDef) {
        if (extendTypeDef == null) {
            throw new IllegalArgumentException("Type Definiition reference cannot be NULL!");
        }
        final TypeDefinition<?> baseTypeDef = extendTypeDef.getBaseType();
        if (baseTypeDef instanceof ExtendedType) {
            return baseTypeDefForExtendedType(baseTypeDef);
        } else {
            return baseTypeDef;
        }

    }

    /**
     * Converts <code>leafrefType</code> to JAVA <code>Type</code>.
     *
     * The path of <code>leafrefType</code> is followed to find referenced node
     * and its <code>Type</code> is returned.
     *
     * @param leafrefType
     *            leafref type definition for which is the type sought
     * @return JAVA <code>Type</code> of data schema node which is referenced in
     *         <code>leafrefType</code>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>leafrefType</code> equal null</li>
     *             <li>if path statement of <code>leafrefType</code> equal null</li>
     *             </ul>
     *
     */
    public Type provideTypeForLeafref(final LeafrefTypeDefinition leafrefType) {
        Type returnType = null;
        if (leafrefType == null) {
            throw new IllegalArgumentException("Leafref Type Definition reference cannot be NULL!");
        }

        if (leafrefType.getPathStatement() == null) {
            throw new IllegalArgumentException("The Path Statement for Leafref Type Definition cannot be NULL!");
        }

        final RevisionAwareXPath xpath = leafrefType.getPathStatement();
        final String strXPath = xpath.toString();

        if (strXPath != null) {
            if (strXPath.contains("[")) {
                returnType = Types.typeForClass(Object.class);
            } else {
                final Module module = findParentModuleForTypeDefinition(schemaContext, leafrefType);
                if (module != null) {
                    final DataSchemaNode dataNode;
                    if (xpath.isAbsolute()) {
                        dataNode = findDataSchemaNode(schemaContext, module, xpath);
                    } else {
                        dataNode = findDataSchemaNodeForRelativeXPath(schemaContext, module, leafrefType, xpath);
                    }

                    if (leafContainsEnumDefinition(dataNode)) {
                        returnType = referencedTypes.get(dataNode.getPath());
                    } else if (leafListContainsEnumDefinition(dataNode)) {
                        returnType = Types.listTypeFor(referencedTypes.get(dataNode.getPath()));
                    } else {
                        returnType = resolveTypeFromDataSchemaNode(dataNode);
                    }
                }
            }
        }
        return returnType;
    }

    /**
     * Checks if <code>dataNode</code> is <code>LeafSchemaNode</code> and if it
     * so then checks if it is of type <code>EnumTypeDefinition</code>.
     *
     * @param dataNode
     *            data schema node for which is checked if it is leaf and if it
     *            is of enum type
     * @return boolean value
     *         <ul>
     *         <li>true - if <code>dataNode</code> is leaf of type enumeration</li>
     *         <li>false - other cases</li>
     *         </ul>
     */
    private boolean leafContainsEnumDefinition(final DataSchemaNode dataNode) {
        if (dataNode instanceof LeafSchemaNode) {
            final LeafSchemaNode leaf = (LeafSchemaNode) dataNode;
            if (leaf.getType() instanceof EnumTypeDefinition) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if <code>dataNode</code> is <code>LeafListSchemaNode</code> and if
     * it so then checks if it is of type <code>EnumTypeDefinition</code>.
     *
     * @param dataNode
     *            data schema node for which is checked if it is leaflist and if
     *            it is of enum type
     * @return boolean value
     *         <ul>
     *         <li>true - if <code>dataNode</code> is leaflist of type
     *         enumeration</li>
     *         <li>false - other cases</li>
     *         </ul>
     */
    private boolean leafListContainsEnumDefinition(final DataSchemaNode dataNode) {
        if (dataNode instanceof LeafListSchemaNode) {
            final LeafListSchemaNode leafList = (LeafListSchemaNode) dataNode;
            if (leafList.getType() instanceof EnumTypeDefinition) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts <code>enumTypeDef</code> to
     * {@link org.opendaylight.yangtools.sal.binding.model.api.Enumeration
     * enumeration}.
     *
     * @param enumTypeDef
     *            enumeration type definition which is converted to enumeration
     * @param enumName
     *            string with name which is used as the enumeration name
     * @return enumeration type which is built with data (name, enum values)
     *         from <code>enumTypeDef</code>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>enumTypeDef</code> equals null</li>
     *             <li>if enum values of <code>enumTypeDef</code> equal null</li>
     *             <li>if Q name of <code>enumTypeDef</code> equal null</li>
     *             <li>if name of <code>enumTypeDef</code> equal null</li>
     *             </ul>
     */
    private Enumeration provideTypeForEnum(final EnumTypeDefinition enumTypeDef, final String enumName) {
        if (enumTypeDef == null) {
            throw new IllegalArgumentException("EnumTypeDefinition reference cannot be NULL!");
        }
        if (enumTypeDef.getValues() == null) {
            throw new IllegalArgumentException("EnumTypeDefinition MUST contain at least ONE value definition!");
        }
        if (enumTypeDef.getQName() == null) {
            throw new IllegalArgumentException("EnumTypeDefinition MUST contain NON-NULL QName!");
        }
        if (enumTypeDef.getQName().getLocalName() == null) {
            throw new IllegalArgumentException("Local Name in EnumTypeDefinition QName cannot be NULL!");
        }

        final String enumerationName = parseToClassName(enumName);

        Module module = findParentModuleForTypeDefinition(schemaContext, enumTypeDef);
        final String basePackageName = moduleNamespaceToPackageName(module);

        final EnumBuilder enumBuilder = new EnumerationBuilderImpl(basePackageName, enumerationName);
        updateEnumPairsFromEnumTypeDef(enumTypeDef, enumBuilder);
        return enumBuilder.toInstance(null);
    }

    /**
     * Adds enumeration to <code>typeBuilder</code>. The enumeration data are
     * taken from <code>enumTypeDef</code>.
     *
     * @param enumTypeDef
     *            enumeration type definition is source of enumeration data for
     *            <code>typeBuilder</code>
     * @param enumName
     *            string with the name of enumeration
     * @param typeBuilder
     *            generated type builder to which is enumeration added
     * @return enumeration type which contains enumeration data form
     *         <code>enumTypeDef</code>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>enumTypeDef</code> equals null</li>
     *             <li>if enum values of <code>enumTypeDef</code> equal null</li>
     *             <li>if Q name of <code>enumTypeDef</code> equal null</li>
     *             <li>if name of <code>enumTypeDef</code> equal null</li>
     *             <li>if name of <code>typeBuilder</code> equal null</li>
     *             </ul>
     *
     */
    private Enumeration addInnerEnumerationToTypeBuilder(final EnumTypeDefinition enumTypeDef, final String enumName,
            final GeneratedTypeBuilder typeBuilder) {
        if (enumTypeDef == null) {
            throw new IllegalArgumentException("EnumTypeDefinition reference cannot be NULL!");
        }
        if (enumTypeDef.getValues() == null) {
            throw new IllegalArgumentException("EnumTypeDefinition MUST contain at least ONE value definition!");
        }
        if (enumTypeDef.getQName() == null) {
            throw new IllegalArgumentException("EnumTypeDefinition MUST contain NON-NULL QName!");
        }
        if (enumTypeDef.getQName().getLocalName() == null) {
            throw new IllegalArgumentException("Local Name in EnumTypeDefinition QName cannot be NULL!");
        }
        if (typeBuilder == null) {
            throw new IllegalArgumentException("Generated Type Builder reference cannot be NULL!");
        }

        final String enumerationName = parseToClassName(enumName);

        final EnumBuilder enumBuilder = typeBuilder.addEnumeration(enumerationName);
        updateEnumPairsFromEnumTypeDef(enumTypeDef, enumBuilder);
        return enumBuilder.toInstance(enumBuilder);
    }

    /**
     * Updates <code>enumBuilder</code> with data from <code>enumTypeDef</code>.
     * Specifically this data represents list of value-name pairs.
     *
     * @param enumTypeDef
     *            enum type definition as source of enum data for
     *            <code>enumBuilder</code>
     * @param enumBuilder
     *            enum builder to which are saved enum data from
     *            <code>enumTypeDef</code>
     */
    private void updateEnumPairsFromEnumTypeDef(final EnumTypeDefinition enumTypeDef, final EnumBuilder enumBuilder) {
        if (enumBuilder != null) {
            final List<EnumPair> enums = enumTypeDef.getValues();
            if (enums != null) {
                int listIndex = 0;
                for (final EnumPair enumPair : enums) {
                    if (enumPair != null) {
                        final String enumPairName = parseToClassName(enumPair.getName());
                        Integer enumPairValue = enumPair.getValue();

                        if (enumPairValue == null) {
                            enumPairValue = listIndex;
                        }
                        enumBuilder.addValue(enumPairName, enumPairValue);
                        listIndex++;
                    }
                }
            }
        }
    }

    /**
     * Converts <code>dataNode</code> to JAVA <code>Type</code>.
     *
     * @param dataNode
     *            contains information about YANG type
     * @return JAVA <code>Type</code> representation of <code>dataNode</code>
     */
    private Type resolveTypeFromDataSchemaNode(final DataSchemaNode dataNode) {
        Type returnType = null;
        if (dataNode != null) {
            if (dataNode instanceof LeafSchemaNode) {
                final LeafSchemaNode leaf = (LeafSchemaNode) dataNode;
                returnType = javaTypeForSchemaDefinitionType(leaf.getType());
            } else if (dataNode instanceof LeafListSchemaNode) {
                final LeafListSchemaNode leafList = (LeafListSchemaNode) dataNode;
                returnType = javaTypeForSchemaDefinitionType(leafList.getType());
            }
        }
        return returnType;
    }

    /**
     * Passes through all modules and through all its type definitions and
     * convert it to generated types.
     *
     * The modules are firstly sorted by mutual dependencies. The modules are
     * sequentially passed. All type definitions of a module are at the
     * beginning sorted so that type definition with less amount of references
     * to other type definition are processed first.<br />
     * For each module is created mapping record in the map
     * {@link TypeProviderImpl#genTypeDefsContextMap genTypeDefsContextMap}
     * which map current module name to the map which maps type names to
     * returned types (generated types).
     *
     */
    private void resolveTypeDefsFromContext() {
        final Set<Module> modules = schemaContext.getModules();
        if (modules == null) {
            throw new IllegalArgumentException("Sef of Modules cannot be NULL!");
        }
        final Module[] modulesArray = new Module[modules.size()];
        int i = 0;
        for (Module modul : modules) {
            modulesArray[i++] = modul;
        }
        final List<Module> modulesSortedByDependency = ModuleDependencySort.sort(modulesArray);

        for (final Module module : modulesSortedByDependency) {
            if (module == null) {
                continue;
            }
            final String moduleName = module.getName();
            final String basePackageName = moduleNamespaceToPackageName(module);

            final Set<TypeDefinition<?>> typeDefinitions = module.getTypeDefinitions();
            final List<TypeDefinition<?>> listTypeDefinitions = sortTypeDefinitionAccordingDepth(typeDefinitions);

            final Map<String, Type> typeMap = new HashMap<>();
            genTypeDefsContextMap.put(moduleName, typeMap);

            if ((listTypeDefinitions != null) && (basePackageName != null)) {
                for (final TypeDefinition<?> typedef : listTypeDefinitions) {
                    typedefToGeneratedType(basePackageName, moduleName, typedef);
                }
            }
        }
    }

    /**
     *
     * @param basePackageName
     *            string with name of package to which the module belongs
     * @param moduleName
     *            string with the name of the module for to which the
     *            <code>typedef</code> belongs
     * @param typedef
     *            type definition of the node for which should be creted JAVA
     *            <code>Type</code> (usually generated TO)
     * @return JAVA <code>Type</code> representation of <code>typedef</code> or
     *         <code>null</code> value if <code>basePackageName</code> or
     *         <code>modulName</code> or <code>typedef</code> or Q name of
     *         <code>typedef</code> equals <code>null</code>
     */
    private Type typedefToGeneratedType(final String basePackageName, final String moduleName,
            final TypeDefinition<?> typedef) {
        if ((basePackageName != null) && (moduleName != null) && (typedef != null) && (typedef.getQName() != null)) {

            final String typedefName = typedef.getQName().getLocalName();
            final TypeDefinition<?> innerTypeDefinition = typedef.getBaseType();
            if (!(innerTypeDefinition instanceof LeafrefTypeDefinition)
                    && !(innerTypeDefinition instanceof IdentityrefTypeDefinition)) {
                Type returnType = null;
                if (innerTypeDefinition instanceof ExtendedType) {
                    ExtendedType innerExtendedType = (ExtendedType) innerTypeDefinition;
                    returnType = provideGeneratedTOFromExtendedType(innerExtendedType, basePackageName, typedefName);
                } else if (innerTypeDefinition instanceof UnionTypeDefinition) {
                    final Module parentModule = findParentModuleForTypeDefinition(schemaContext, typedef);
                    final GeneratedTOBuilder genTOBuilder = provideGeneratedTOBuilderForUnionTypeDefinition(
                            basePackageName, typedef, typedefName, parentModule);
                    returnType = genTOBuilder.toInstance();
                } else if (innerTypeDefinition instanceof EnumTypeDefinition) {
                    final EnumTypeDefinition enumTypeDef = (EnumTypeDefinition) innerTypeDefinition;
                    returnType = provideTypeForEnum(enumTypeDef, typedefName);

                } else if (innerTypeDefinition instanceof BitsTypeDefinition) {
                    final BitsTypeDefinition bitsTypeDefinition = (BitsTypeDefinition) innerTypeDefinition;
                    final GeneratedTOBuilder genTOBuilder = provideGeneratedTOBuilderForBitsTypeDefinition(
                            basePackageName, bitsTypeDefinition, typedefName);
                    returnType = genTOBuilder.toInstance();

                } else {
                    final Type javaType = BaseYangTypes.BASE_YANG_TYPES_PROVIDER
                            .javaTypeForSchemaDefinitionType(innerTypeDefinition);

                    returnType = wrapJavaTypeIntoTO(basePackageName, typedef, javaType);
                }
                if (returnType != null) {
                    final Map<String, Type> typeMap = genTypeDefsContextMap.get(moduleName);
                    if (typeMap != null) {
                        typeMap.put(typedefName, returnType);
                    }
                    return returnType;
                }
            }
        }
        return null;
    }

    /**
     * Wraps base YANG type to generated TO.
     *
     * @param basePackageName
     *            string with name of package to which the module belongs
     * @param typedef
     *            type definition which is converted to the TO
     * @param javaType
     *            JAVA <code>Type</code> to which is <code>typedef</code> mapped
     * @return generated transfer object which represent<code>javaType</code>
     */
    private GeneratedTransferObject wrapJavaTypeIntoTO(final String basePackageName, final TypeDefinition<?> typedef,
            final Type javaType) {
        if (javaType != null) {
            final String typedefName = typedef.getQName().getLocalName();
            final String propertyName = parseToValidParamName(typedefName);

            final GeneratedTOBuilder genTOBuilder = typedefToTransferObject(basePackageName, typedef);

            final GeneratedPropertyBuilder genPropBuilder = genTOBuilder.addProperty(propertyName);

            genPropBuilder.setReturnType(javaType);
            genTOBuilder.addEqualsIdentity(genPropBuilder);
            genTOBuilder.addHashIdentity(genPropBuilder);
            genTOBuilder.addToStringProperty(genPropBuilder);
            if (javaType == BaseYangTypes.STRING_TYPE) {
                if (typedef instanceof ExtendedType) {
                    final List<String> regExps = resolveRegExpressionsFromTypedef((ExtendedType) typedef);
                    addStringRegExAsConstant(genTOBuilder, regExps);
                }
            }
            return genTOBuilder.toInstance();
        }
        return null;
    }

    /**
     * Converts <code>typedef</code> to generated TO with
     * <code>typeDefName</code>. Every union type from <code>typedef</code> is
     * added to generated TO builder as property.
     *
     * @param basePackageName
     *            string with name of package to which the module belongs
     * @param typedef
     *            type definition which should be of type
     *            <code>UnionTypeDefinition</code>
     * @param typeDefName
     *            string with name for generated TO
     * @return generated TO builder which represents <code>typedef</code>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>basePackageName</code> equals null</li>
     *             <li>if <code>typedef</code> equals null</li>
     *             <li>if Q name of <code>typedef</code> equals null</li>
     *             </ul>
     */
    public GeneratedTOBuilder provideGeneratedTOBuilderForUnionTypeDefinition(final String basePackageName,
            final TypeDefinition<?> typedef, final String typeDefName, final Module parentModule) {
        if (basePackageName == null) {
            throw new IllegalArgumentException("Base Package Name cannot be NULL!");
        }
        if (typedef == null) {
            throw new IllegalArgumentException("Type Definition cannot be NULL!");
        }
        if (typedef.getQName() == null) {
            throw new IllegalArgumentException(
                    "Type Definition cannot have non specified QName (QName cannot be NULL!)");
        }

        final TypeDefinition<?> baseTypeDefinition = typedef.getBaseType();
        if ((baseTypeDefinition != null) && (baseTypeDefinition instanceof UnionTypeDefinition)) {
            final UnionTypeDefinition unionTypeDef = (UnionTypeDefinition) baseTypeDefinition;
            final List<TypeDefinition<?>> unionTypes = unionTypeDef.getTypes();

            final GeneratedTOBuilder unionGenTransObject;
            if (typeDefName != null && !typeDefName.isEmpty()) {
                final String typeName = parseToClassName(typeDefName);
                unionGenTransObject = new GeneratedTOBuilderImpl(basePackageName, typeName);
            } else {
                unionGenTransObject = typedefToTransferObject(basePackageName, typedef);
            }
            unionGenTransObject.setIsUnion(true);

            final List<String> regularExpressions = new ArrayList<String>();
            for (final TypeDefinition<?> unionType : unionTypes) {
                final String typeName = unionType.getQName().getLocalName();
                if (unionType instanceof ExtendedType) {
                    final Module unionTypeModule = findParentModuleForTypeDefinition(schemaContext, unionType);
                    if (unionTypeModule != null && unionTypeModule.getName() != null) {
                        final Map<String, Type> innerGenTOs = genTypeDefsContextMap.get(unionTypeModule.getName());
                        Type genTransferObject = null;
                        if (innerGenTOs != null) {
                            genTransferObject = innerGenTOs.get(typeName);
                        }
                        if (genTransferObject != null) {
                            updateUnionTypeAsProperty(unionGenTransObject, genTransferObject,
                                    genTransferObject.getName());
                        } else {
                            final TypeDefinition<?> baseType = baseTypeDefForExtendedType(unionType);
                            if (typeName.equals(baseType.getQName().getLocalName())) {
                                final Type javaType = BaseYangTypes.BASE_YANG_TYPES_PROVIDER
                                        .javaTypeForSchemaDefinitionType(baseType);
                                if (javaType != null) {
                                    updateUnionTypeAsProperty(unionGenTransObject, javaType, typeName);
                                }
                            }
                            if (baseType instanceof StringType) {
                                regularExpressions.addAll(resolveRegExpressionsFromTypedef((ExtendedType) unionType));
                            }
                        }
                    }
                } else if (unionType instanceof EnumTypeDefinition) {
                    final Enumeration enumeration = addInnerEnumerationToTypeBuilder((EnumTypeDefinition) unionType,
                            typeName, unionGenTransObject);
                    updateUnionTypeAsProperty(unionGenTransObject, enumeration, typeName);
                } else {
                    final Type javaType = BaseYangTypes.BASE_YANG_TYPES_PROVIDER
                            .javaTypeForSchemaDefinitionType(unionType);
                    if (javaType != null) {
                        updateUnionTypeAsProperty(unionGenTransObject, javaType, typeName);
                    }
                }
            }
            if (!regularExpressions.isEmpty()) {
                addStringRegExAsConstant(unionGenTransObject, regularExpressions);
            }

            Map<String, Type> genTOsMap = null;
            // final Module parentModule =
            // findParentModuleForTypeDefinition(schemaContext, typedef);
            if (parentModule != null && parentModule.getName() != null) {
                genTOsMap = genTypeDefsContextMap.get(parentModule.getName());
                genTOsMap.put(typedef.getQName().getLocalName(), unionGenTransObject.toInstance());
            }

            return unionGenTransObject;
        }
        return null;
    }

    /**
     * Adds a new property with the name <code>propertyName</code> and with type
     * <code>type</code> to <code>unonGenTransObject</code>.
     *
     * @param unionGenTransObject
     *            generated TO to which should be property added
     * @param type
     *            JAVA <code>type</code> of the property which should be added
     *            to <code>unionGentransObject</code>
     * @param propertyName
     *            string with name of property which should be added to
     *            <code>unionGentransObject</code>
     */
    private void updateUnionTypeAsProperty(final GeneratedTOBuilder unionGenTransObject, final Type type,
            final String propertyName) {
        if (unionGenTransObject != null && type != null) {
            if (!unionGenTransObject.containsProperty(propertyName)) {
                final GeneratedPropertyBuilder propBuilder = unionGenTransObject
                        .addProperty(parseToValidParamName(propertyName));
                propBuilder.setReturnType(type);

                unionGenTransObject.addEqualsIdentity(propBuilder);
                unionGenTransObject.addHashIdentity(propBuilder);
                unionGenTransObject.addToStringProperty(propBuilder);
            }
        }
    }

    /**
     * Converts <code>typedef</code> to the generated TO builder.
     *
     * @param basePackageName
     *            string with name of package to which the module belongs
     * @param typedef
     *            type definition from which is the generated TO builder created
     * @return generated TO builder which contains data from
     *         <code>typedef</code> and <code>basePackageName</code>
     */
    private GeneratedTOBuilder typedefToTransferObject(final String basePackageName, final TypeDefinition<?> typedef) {

        final String packageName = packageNameForGeneratedType(basePackageName, typedef.getPath());
        final String typeDefTOName = typedef.getQName().getLocalName();

        if ((packageName != null) && (typedef != null) && (typeDefTOName != null)) {
            final String genTOName = parseToClassName(typeDefTOName);
            final GeneratedTOBuilder newType = new GeneratedTOBuilderImpl(packageName, genTOName);

            return newType;
        }
        return null;
    }

    /**
     * Converts <code>typeDef</code> which should be of the type
     * <code>BitsTypeDefinition</code> to <code>GeneratedTOBuilder</code>.
     *
     * All the bits of the typeDef are added to returning generated TO as
     * properties.
     *
     * @param basePackageName
     *            string with name of package to which the module belongs
     * @param typeDef
     *            type definition from which is the generated TO builder created
     * @param typeDefName
     *            string with the name for generated TO builder
     * @return generated TO builder which represents <code>typeDef</code>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>typeDef</code> equals null</li>
     *             <li>if <code>basePackageName</code> equals null</li>
     *             </ul>
     */
    public GeneratedTOBuilder provideGeneratedTOBuilderForBitsTypeDefinition(final String basePackageName,
            final TypeDefinition<?> typeDef, String typeDefName) {

        if (typeDef == null) {
            throw new IllegalArgumentException("typeDef cannot be NULL!");
        }
        if (basePackageName == null) {
            throw new IllegalArgumentException("Base Package Name cannot be NULL!");
        }

        if (typeDef instanceof BitsTypeDefinition) {
            BitsTypeDefinition bitsTypeDefinition = (BitsTypeDefinition) typeDef;

            final String typeName = parseToClassName(typeDefName);
            final GeneratedTOBuilder genTOBuilder = new GeneratedTOBuilderImpl(basePackageName, typeName);

            final List<Bit> bitList = bitsTypeDefinition.getBits();
            GeneratedPropertyBuilder genPropertyBuilder;
            for (final Bit bit : bitList) {
                String name = bit.getName();
                genPropertyBuilder = genTOBuilder.addProperty(parseToValidParamName(name));
                genPropertyBuilder.setReadOnly(true);
                genPropertyBuilder.setReturnType(BaseYangTypes.BOOLEAN_TYPE);

                genTOBuilder.addEqualsIdentity(genPropertyBuilder);
                genTOBuilder.addHashIdentity(genPropertyBuilder);
                genTOBuilder.addToStringProperty(genPropertyBuilder);
            }

            return genTOBuilder;
        }
        return null;
    }

    /**
     * Converts the pattern constraints from <code>typedef</code> to the list of
     * the strings which represents these constraints.
     *
     * @param typedef
     *            extended type in which are the pattern constraints sought
     * @return list of strings which represents the constraint patterns
     * @throws IllegalArgumentException
     *             if <code>typedef</code> equals null
     *
     */
    private List<String> resolveRegExpressionsFromTypedef(ExtendedType typedef) {
        final List<String> regExps = new ArrayList<String>();
        if (typedef == null) {
            throw new IllegalArgumentException("typedef can't be null");
        }
        final TypeDefinition<?> strTypeDef = baseTypeDefForExtendedType(typedef);
        if (strTypeDef instanceof StringType) {
            final List<PatternConstraint> patternConstraints = typedef.getPatterns();
            if (!patternConstraints.isEmpty()) {
                String regEx;
                String modifiedRegEx;
                for (PatternConstraint patternConstraint : patternConstraints) {
                    regEx = patternConstraint.getRegularExpression();
                    modifiedRegEx = StringEscapeUtils.escapeJava(regEx);
                    regExps.add(modifiedRegEx);
                }
            }
        }
        return regExps;
    }

    /**
     *
     * Adds to the <code>genTOBuilder</code> the constant which contains regular
     * expressions from the <code>regularExpressions</code>
     *
     * @param genTOBuilder
     *            generated TO builder to which are
     *            <code>regular expressions</code> added
     * @param regularExpressions
     *            list of string which represent regular expressions
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>genTOBuilder</code> equals null</li>
     *             <li>if <code>regularExpressions</code> equals null</li>
     *             </ul>
     */
    private void addStringRegExAsConstant(GeneratedTOBuilder genTOBuilder, List<String> regularExpressions) {
        if (genTOBuilder == null)
            throw new IllegalArgumentException("genTOBuilder can't be null");
        if (regularExpressions == null)
            throw new IllegalArgumentException("regularExpressions can't be null");

        if (!regularExpressions.isEmpty()) {
            genTOBuilder.addConstant(Types.listTypeFor(BaseYangTypes.STRING_TYPE), TypeConstants.PATTERN_CONSTANT_NAME,
                    regularExpressions);
        }
    }

    /**
     * Creates generated TO with data about inner extended type
     * <code>innerExtendedType</code>, about the package name
     * <code>typedefName</code> and about the generated TO name
     * <code>typedefName</code>.
     *
     * It is supposed that <code>innerExtendedType</code> is already present in
     * {@link TypeProviderImpl#genTypeDefsContextMap genTypeDefsContextMap} to
     * be possible set it as extended type for the returning generated TO.
     *
     * @param innerExtendedType
     *            extended type which is part of some other extended type
     * @param basePackageName
     *            string with the package name of the module
     * @param typedefName
     *            string with the name for the generated TO
     * @return generated TO which extends generated TO for
     *         <code>innerExtendedType</code>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>extendedType</code> equals null</li>
     *             <li>if <code>basePackageName</code> equals null</li>
     *             <li>if <code>typedefName</code> equals null</li>
     *             </ul>
     */
    private GeneratedTransferObject provideGeneratedTOFromExtendedType(final ExtendedType innerExtendedType,
            final String basePackageName, final String typedefName) {

        if (innerExtendedType == null) {
            throw new IllegalArgumentException("Extended type cannot be NULL!");
        }
        if (basePackageName == null) {
            throw new IllegalArgumentException("String with base package name cannot be NULL!");
        }
        if (typedefName == null) {
            throw new IllegalArgumentException("String with type definition name cannot be NULL!");
        }

        final String classTypedefName = parseToClassName(typedefName);
        final String innerTypeDef = innerExtendedType.getQName().getLocalName();
        final GeneratedTOBuilder genTOBuilder = new GeneratedTOBuilderImpl(basePackageName, classTypedefName);

        Map<String, Type> typeMap = null;
        final Module parentModule = findParentModuleForTypeDefinition(schemaContext, innerExtendedType);
        if (parentModule != null) {
            typeMap = genTypeDefsContextMap.get(parentModule.getName());
        }

        if (typeMap != null) {
            Type type = typeMap.get(innerTypeDef);
            if (type instanceof GeneratedTransferObject) {
                genTOBuilder.setExtendsType((GeneratedTransferObject) type);
            }
        }

        return genTOBuilder.toInstance();
    }

    /**
     * Finds out for each type definition how many immersion (depth) is
     * necessary to get to the base type. Every type definition is inserted to
     * the map which key is depth and value is list of type definitions with
     * equal depth. In next step are lists from this map concatenated to one
     * list in ascending order according to their depth. All type definitions
     * are in the list behind all type definitions on which depends.
     *
     * @param unsortedTypeDefinitions
     *            list of type definitions which should be sorted by depth
     * @return list of type definitions sorted according their each other
     *         dependencies (type definitions which are depend on other type
     *         definitions are in list behind them).
     */
    private List<TypeDefinition<?>> sortTypeDefinitionAccordingDepth(
            final Set<TypeDefinition<?>> unsortedTypeDefinitions) {
        List<TypeDefinition<?>> sortedTypeDefinition = new ArrayList<>();

        Map<Integer, List<TypeDefinition<?>>> typeDefinitionsDepths = new TreeMap<>();
        for (TypeDefinition<?> unsortedTypeDefinition : unsortedTypeDefinitions) {
            final int depth = getTypeDefinitionDepth(unsortedTypeDefinition);
            List<TypeDefinition<?>> typeDefinitionsConcreteDepth = typeDefinitionsDepths.get(depth);
            if (typeDefinitionsConcreteDepth == null) {
                typeDefinitionsConcreteDepth = new ArrayList<TypeDefinition<?>>();
                typeDefinitionsDepths.put(depth, typeDefinitionsConcreteDepth);
            }
            typeDefinitionsConcreteDepth.add(unsortedTypeDefinition);
        }

        Set<Integer> depths = typeDefinitionsDepths.keySet(); // keys are in
                                                              // ascending order
        for (Integer depth : depths) {
            sortedTypeDefinition.addAll(typeDefinitionsDepths.get(depth));
        }

        return sortedTypeDefinition;
    }

    /**
     * Returns how many immersion is necessary to get from the type definition
     * to the base type.
     *
     * @param typeDefinition
     *            type definition for which is depth sought.
     * @return number of immersions which are necessary to get from the type
     *         definition to the base type
     */
    private int getTypeDefinitionDepth(final TypeDefinition<?> typeDefinition) {
        if (typeDefinition == null) {
            throw new IllegalArgumentException("Type definition can't be null");
        }
        int depth = 1;
        TypeDefinition<?> baseType = typeDefinition.getBaseType();

        if (baseType instanceof ExtendedType) {
            depth = depth + getTypeDefinitionDepth(typeDefinition.getBaseType());
        } else if (baseType instanceof UnionType) {
            List<TypeDefinition<?>> childTypeDefinitions = ((UnionType) baseType).getTypes();
            int maxChildDepth = 0;
            int childDepth = 1;
            for (TypeDefinition<?> childTypeDefinition : childTypeDefinitions) {
                childDepth = childDepth + getTypeDefinitionDepth(childTypeDefinition.getBaseType());
                if (childDepth > maxChildDepth) {
                    maxChildDepth = childDepth;
                }
            }
            return maxChildDepth;
        }
        return depth;
    }

}
