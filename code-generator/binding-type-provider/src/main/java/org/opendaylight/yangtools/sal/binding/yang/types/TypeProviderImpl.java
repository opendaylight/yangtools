/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.yang.types;

import static org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil.packageNameForGeneratedType;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.findDataSchemaNode;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.findDataSchemaNodeForRelativeXPath;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.findParentModule;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.io.BaseEncoding;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringEscapeUtils;
import org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil;
import org.opendaylight.yangtools.binding.generator.util.TypeConstants;
import org.opendaylight.yangtools.binding.generator.util.Types;
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.EnumerationBuilderImpl;
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedPropertyBuilderImpl;
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedTOBuilderImpl;
import org.opendaylight.yangtools.sal.binding.generator.spi.TypeProvider;
import org.opendaylight.yangtools.sal.binding.model.api.AccessModifier;
import org.opendaylight.yangtools.sal.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.sal.binding.model.api.Enumeration;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.Restrictions;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.EnumBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.DataNodeIterator;
import org.opendaylight.yangtools.yang.model.util.EnumerationType;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.model.util.Int16;
import org.opendaylight.yangtools.yang.model.util.Int32;
import org.opendaylight.yangtools.yang.model.util.Int64;
import org.opendaylight.yangtools.yang.model.util.Int8;
import org.opendaylight.yangtools.yang.model.util.StringType;
import org.opendaylight.yangtools.yang.model.util.Uint16;
import org.opendaylight.yangtools.yang.model.util.Uint32;
import org.opendaylight.yangtools.yang.model.util.Uint64;
import org.opendaylight.yangtools.yang.model.util.Uint8;
import org.opendaylight.yangtools.yang.model.util.UnionType;

public final class TypeProviderImpl implements TypeProvider {
    private static final Pattern NUMBERS_PATTERN = Pattern.compile("[0-9]+\\z");

    /**
     * Contains the schema data red from YANG files.
     */
    private final SchemaContext schemaContext;

    /**
     * Map<moduleName, Map<moduleDate, Map<typeName, type>>>
     */
    private final Map<String, Map<Date, Map<String, Type>>> genTypeDefsContextMap;

    /**
     * The map which maps schema paths to JAVA <code>Type</code>.
     */
    private final Map<SchemaPath, Type> referencedTypes;
    private final Map<Module, Set<Type>> additionalTypes;

    /**
     * Creates new instance of class <code>TypeProviderImpl</code>.
     *
     * @param schemaContext
     *            contains the schema data red from YANG files
     * @throws IllegalArgumentException
     *             if <code>schemaContext</code> equal null.
     */
    public TypeProviderImpl(final SchemaContext schemaContext) {
        Preconditions.checkArgument(schemaContext != null, "Schema Context cannot be null!");

        this.schemaContext = schemaContext;
        this.genTypeDefsContextMap = new HashMap<>();
        this.referencedTypes = new HashMap<>();
        this.additionalTypes = new HashMap<>();
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
        Preconditions.checkArgument(refTypePath != null,
                "Path reference of Enumeration Type Definition cannot be NULL!");
        Preconditions.checkArgument(refType != null, "Reference to Enumeration Type cannot be NULL!");
        referencedTypes.put(refTypePath, refType);
    }

    public Map<Module, Set<Type>> getAdditionalTypes() {
        return additionalTypes;
    }

    /**
     *
     * Converts basic YANG type <code>type</code> to JAVA <code>Type</code>.
     *
     * @param type
     *            string with YANG name of type
     * @return JAVA <code>Type</code> for YANG type <code>type</code>
     * @see TypeProvider#javaTypeForYangType(String)
     */
    @Override
    @Deprecated
    public Type javaTypeForYangType(final String type) {
        return BaseYangTypes.BASE_YANG_TYPES_PROVIDER.javaTypeForYangType(type);
    }

    @Override
    public Type javaTypeForSchemaDefinitionType(final TypeDefinition<?> typeDefinition, final SchemaNode parentNode) {
        return javaTypeForSchemaDefinitionType(typeDefinition, parentNode, null);
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
     *             <li>if Qname of <code>typeDefinition</code> equal null</li>
     *             <li>if name of <code>typeDefinition</code> equal null</li>
     *             </ul>
     */
    @Override
    public Type javaTypeForSchemaDefinitionType(final TypeDefinition<?> typeDefinition, final SchemaNode parentNode,
            final Restrictions r) {
        Type returnType;
        Preconditions.checkArgument(typeDefinition != null, "Type Definition cannot be NULL!");
        Preconditions.checkArgument(typeDefinition.getQName() != null,
                "Type Definition cannot have non specified QName (QName cannot be NULL!)");
        String typedefName = typeDefinition.getQName().getLocalName();
        Preconditions.checkArgument(typedefName != null, "Type Definitions Local Name cannot be NULL!");

        if (typeDefinition instanceof ExtendedType) {
            returnType = javaTypeForExtendedType(typeDefinition);
            if (r != null && returnType instanceof GeneratedTransferObject) {
                GeneratedTransferObject gto = (GeneratedTransferObject) returnType;
                Module module = findParentModule(schemaContext, parentNode);
                String basePackageName = BindingMapping.getRootPackageName(module.getQNameModule());
                String packageName = packageNameForGeneratedType(basePackageName, typeDefinition.getPath());
                String genTOName = BindingMapping.getClassName(typedefName);
                String name = packageName + "." + genTOName;
                if (!(returnType.getFullyQualifiedName().equals(name))) {
                    returnType = shadedTOWithRestrictions(gto, r);
                }
            }
        } else {
            returnType = javaTypeForLeafrefOrIdentityRef(typeDefinition, parentNode);
            if (returnType == null) {
                returnType = BaseYangTypes.BASE_YANG_TYPES_PROVIDER.javaTypeForYangType(typeDefinition.getQName()
                        .getLocalName());
            }
        }
        return returnType;
    }

    private static GeneratedTransferObject shadedTOWithRestrictions(final GeneratedTransferObject gto, final Restrictions r) {
        GeneratedTOBuilder gtob = new GeneratedTOBuilderImpl(gto.getPackageName(), gto.getName());
        GeneratedTransferObject parent = gto.getSuperType();
        if (parent != null) {
            gtob.setExtendsType(parent);
        }
        gtob.setRestrictions(r);
        for (GeneratedProperty gp : gto.getProperties()) {
            GeneratedPropertyBuilder gpb = gtob.addProperty(gp.getName());
            gpb.setValue(gp.getValue());
            gpb.setReadOnly(gp.isReadOnly());
            gpb.setAccessModifier(gp.getAccessModifier());
            gpb.setReturnType(gp.getReturnType());
            gpb.setFinal(gp.isFinal());
            gpb.setStatic(gp.isStatic());
        }
        return gtob.toInstance();
    }

    /**
     * Returns JAVA <code>Type</code> for instances of the type
     * <code>LeafrefTypeDefinition</code> or
     * <code>IdentityrefTypeDefinition</code>.
     *
     * @param typeDefinition
     *            type definition which is converted to JAVA <code>Type</code>
     * @return JAVA <code>Type</code> instance for <code>typeDefinition</code>
     */
    private Type javaTypeForLeafrefOrIdentityRef(final TypeDefinition<?> typeDefinition, final SchemaNode parentNode) {
        if (typeDefinition instanceof LeafrefTypeDefinition) {
            final LeafrefTypeDefinition leafref = (LeafrefTypeDefinition) typeDefinition;
            return provideTypeForLeafref(leafref, parentNode);
        } else if (typeDefinition instanceof IdentityrefTypeDefinition) {
            final IdentityrefTypeDefinition idref = (IdentityrefTypeDefinition) typeDefinition;
            return provideTypeForIdentityref(idref);
        } else {
            return null;
        }
    }

    /**
     * Returns JAVA <code>Type</code> for instances of the type
     * <code>ExtendedType</code>.
     *
     * @param typeDefinition
     *            type definition which is converted to JAVA <code>Type</code>
     * @return JAVA <code>Type</code> instance for <code>typeDefinition</code>
     */
    private Type javaTypeForExtendedType(final TypeDefinition<?> typeDefinition) {
        final String typedefName = typeDefinition.getQName().getLocalName();
        final TypeDefinition<?> baseTypeDef = baseTypeDefForExtendedType(typeDefinition);
        Type returnType = javaTypeForLeafrefOrIdentityRef(baseTypeDef, typeDefinition);
        if (returnType == null) {
            if (baseTypeDef instanceof EnumTypeDefinition) {
                final EnumTypeDefinition enumTypeDef = (EnumTypeDefinition) baseTypeDef;
                returnType = provideTypeForEnum(enumTypeDef, typedefName, typeDefinition);
            } else {
                final Module module = findParentModule(schemaContext, typeDefinition);
                Restrictions r = BindingGeneratorUtil.getRestrictions(typeDefinition);
                if (module != null) {
                    final Map<Date, Map<String, Type>> modulesByDate = genTypeDefsContextMap.get(module.getName());
                    final Map<String, Type> genTOs = modulesByDate.get(module.getRevision());
                    if (genTOs != null) {
                        returnType = genTOs.get(typedefName);
                    }
                    if (returnType == null) {
                        returnType = BaseYangTypes.BASE_YANG_TYPES_PROVIDER.javaTypeForSchemaDefinitionType(
                                baseTypeDef, typeDefinition, r);
                    }
                }
            }
        }
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
    private Type provideTypeForIdentityref(final IdentityrefTypeDefinition idref) {
        QName baseIdQName = idref.getIdentity().getQName();
        Module module = schemaContext.findModuleByNamespaceAndRevision(baseIdQName.getNamespace(),
                baseIdQName.getRevision());
        IdentitySchemaNode identity = null;
        for (IdentitySchemaNode id : module.getIdentities()) {
            if (id.getQName().equals(baseIdQName)) {
                identity = id;
            }
        }
        Preconditions.checkArgument(identity != null, "Target identity '" + baseIdQName + "' do not exists");

        final String basePackageName = BindingMapping.getRootPackageName(module.getQNameModule());
        final String packageName = packageNameForGeneratedType(basePackageName, identity.getPath());
        final String genTypeName = BindingMapping.getClassName(identity.getQName());

        Type baseType = Types.typeForClass(Class.class);
        Type paramType = Types.wildcardTypeFor(packageName, genTypeName);
        return Types.parameterizedTypeFor(baseType, paramType);
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
    public Type generatedTypeForExtendedDefinitionType(final TypeDefinition<?> typeDefinition,
            final SchemaNode parentNode) {
        Type returnType = null;
        Preconditions.checkArgument(typeDefinition != null, "Type Definition cannot be NULL!");
        if (typeDefinition.getQName() == null) {
            throw new IllegalArgumentException(
                    "Type Definition cannot have non specified QName (QName cannot be NULL!)");
        }
        Preconditions.checkArgument(typeDefinition.getQName().getLocalName() != null,
                "Type Definitions Local Name cannot be NULL!");

        final String typedefName = typeDefinition.getQName().getLocalName();
        if (typeDefinition instanceof ExtendedType) {
            final TypeDefinition<?> baseTypeDef = baseTypeDefForExtendedType(typeDefinition);

            if (!(baseTypeDef instanceof LeafrefTypeDefinition) && !(baseTypeDef instanceof IdentityrefTypeDefinition)) {
                final Module module = findParentModule(schemaContext, parentNode);

                if (module != null) {
                    final Map<Date, Map<String, Type>> modulesByDate = genTypeDefsContextMap.get(module.getName());
                    final Map<String, Type> genTOs = modulesByDate.get(module.getRevision());
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
        Preconditions.checkArgument(extendTypeDef != null, "Type Definition reference cannot be NULL!");
        final TypeDefinition<?> baseTypeDef = extendTypeDef.getBaseType();
        if (baseTypeDef == null) {
            return extendTypeDef;
        } else if (baseTypeDef instanceof ExtendedType) {
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
    public Type provideTypeForLeafref(final LeafrefTypeDefinition leafrefType, final SchemaNode parentNode) {
        Type returnType = null;
        Preconditions.checkArgument(leafrefType != null, "Leafref Type Definition reference cannot be NULL!");

        Preconditions.checkArgument(leafrefType.getPathStatement() != null,
                "The Path Statement for Leafref Type Definition cannot be NULL!");

        final RevisionAwareXPath xpath = leafrefType.getPathStatement();
        final String strXPath = xpath.toString();

        if (strXPath != null) {
            if (strXPath.indexOf('[') == -1) {
                final Module module = findParentModule(schemaContext, parentNode);
                if (module != null) {
                    final SchemaNode dataNode;
                    if (xpath.isAbsolute()) {
                        dataNode = findDataSchemaNode(schemaContext, module, xpath);
                    } else {
                        dataNode = findDataSchemaNodeForRelativeXPath(schemaContext, module, parentNode, xpath);
                    }

                    if (leafContainsEnumDefinition(dataNode)) {
                        returnType = referencedTypes.get(dataNode.getPath());
                    } else if (leafListContainsEnumDefinition(dataNode)) {
                        returnType = Types.listTypeFor(referencedTypes.get(dataNode.getPath()));
                    } else {
                        returnType = resolveTypeFromDataSchemaNode(dataNode);
                    }
                }
            } else {
                returnType = Types.typeForClass(Object.class);
            }
        }
        if (returnType == null) {
            throw new IllegalArgumentException("Failed to find leafref target: " + strXPath);
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
    private static boolean leafContainsEnumDefinition(final SchemaNode dataNode) {
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
    private static boolean leafListContainsEnumDefinition(final SchemaNode dataNode) {
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
    private Enumeration provideTypeForEnum(final EnumTypeDefinition enumTypeDef, final String enumName,
            final SchemaNode parentNode) {
        Preconditions.checkArgument(enumTypeDef != null, "EnumTypeDefinition reference cannot be NULL!");
        Preconditions.checkArgument(enumTypeDef.getValues() != null,
                "EnumTypeDefinition MUST contain at least ONE value definition!");
        Preconditions.checkArgument(enumTypeDef.getQName() != null, "EnumTypeDefinition MUST contain NON-NULL QName!");
        Preconditions.checkArgument(enumTypeDef.getQName().getLocalName() != null,
                "Local Name in EnumTypeDefinition QName cannot be NULL!");

        final String enumerationName = BindingMapping.getClassName(enumName);

        Module module = findParentModule(schemaContext, parentNode);
        final String basePackageName = BindingMapping.getRootPackageName(module.getQNameModule());

        final EnumerationBuilderImpl enumBuilder = new EnumerationBuilderImpl(basePackageName, enumerationName);
        enumBuilder.setDescription(enumTypeDef.getDescription());
        enumBuilder.setReference(enumTypeDef.getReference());
        enumBuilder.setModuleName(module.getName());
        enumBuilder.setSchemaPath(enumTypeDef.getPath().getPathFromRoot());
        enumBuilder.updateEnumPairsFromEnumTypeDef(enumTypeDef);
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
    private static Enumeration addInnerEnumerationToTypeBuilder(final EnumTypeDefinition enumTypeDef, final String enumName,
            final GeneratedTypeBuilderBase<?> typeBuilder) {
        Preconditions.checkArgument(enumTypeDef != null, "EnumTypeDefinition reference cannot be NULL!");
        Preconditions.checkArgument(enumTypeDef.getValues() != null,
                "EnumTypeDefinition MUST contain at least ONE value definition!");
        Preconditions.checkArgument(enumTypeDef.getQName() != null, "EnumTypeDefinition MUST contain NON-NULL QName!");
        Preconditions.checkArgument(enumTypeDef.getQName().getLocalName() != null,
                "Local Name in EnumTypeDefinition QName cannot be NULL!");
        Preconditions.checkArgument(typeBuilder != null, "Generated Type Builder reference cannot be NULL!");

        final String enumerationName = BindingMapping.getClassName(enumName);

        final EnumBuilder enumBuilder = typeBuilder.addEnumeration(enumerationName);
        enumBuilder.setDescription(enumTypeDef.getDescription());
        enumBuilder.updateEnumPairsFromEnumTypeDef(enumTypeDef);
        return enumBuilder.toInstance(enumBuilder);
    }

    /**
     * Converts <code>dataNode</code> to JAVA <code>Type</code>.
     *
     * @param dataNode
     *            contains information about YANG type
     * @return JAVA <code>Type</code> representation of <code>dataNode</code>
     */
    private Type resolveTypeFromDataSchemaNode(final SchemaNode dataNode) {
        Type returnType = null;
        if (dataNode != null) {
            if (dataNode instanceof LeafSchemaNode) {
                final LeafSchemaNode leaf = (LeafSchemaNode) dataNode;
                returnType = javaTypeForSchemaDefinitionType(leaf.getType(), leaf);
            } else if (dataNode instanceof LeafListSchemaNode) {
                final LeafListSchemaNode leafList = (LeafListSchemaNode) dataNode;
                returnType = javaTypeForSchemaDefinitionType(leafList.getType(), leafList);
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
        Preconditions.checkArgument(modules != null, "Set of Modules cannot be NULL!");
        final Module[] modulesArray = new Module[modules.size()];
        int i = 0;
        for (Module modul : modules) {
            modulesArray[i++] = modul;
        }
        final List<Module> modulesSortedByDependency = org.opendaylight.yangtools.yang.parser.util.ModuleDependencySort
                .sort(modulesArray);

        for (final Module module : modulesSortedByDependency) {
            Map<Date, Map<String, Type>> dateTypeMap = genTypeDefsContextMap.get(module.getName());
            if (dateTypeMap == null) {
                dateTypeMap = new HashMap<>();
            }
            dateTypeMap.put(module.getRevision(), Collections.<String, Type>emptyMap());
            genTypeDefsContextMap.put(module.getName(), dateTypeMap);
        }

        for (final Module module : modulesSortedByDependency) {
            if (module == null) {
                continue;
            }
            final String basePackageName = BindingMapping.getRootPackageName(module.getQNameModule());

            final DataNodeIterator it = new DataNodeIterator(module);
            final List<TypeDefinition<?>> typeDefinitions = it.allTypedefs();
            final List<TypeDefinition<?>> listTypeDefinitions = sortTypeDefinitionAccordingDepth(typeDefinitions);

            if ((listTypeDefinitions != null) && (basePackageName != null)) {
                for (final TypeDefinition<?> typedef : listTypeDefinitions) {
                    typedefToGeneratedType(basePackageName, module, typedef);
                }
            }
        }
    }

    /**
     *
     * @param basePackageName
     *            string with name of package to which the module belongs
     * @param module
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
    private Type typedefToGeneratedType(final String basePackageName, final Module module,
            final TypeDefinition<?> typedef) {
        final String moduleName = module.getName();
        final Date moduleRevision = module.getRevision();
        if ((basePackageName != null) && (moduleName != null) && (typedef != null) && (typedef.getQName() != null)) {
            final String typedefName = typedef.getQName().getLocalName();
            final TypeDefinition<?> innerTypeDefinition = typedef.getBaseType();
            if (!(innerTypeDefinition instanceof LeafrefTypeDefinition)
                    && !(innerTypeDefinition instanceof IdentityrefTypeDefinition)) {
                Type returnType = null;
                if (innerTypeDefinition instanceof ExtendedType) {
                    ExtendedType innerExtendedType = (ExtendedType) innerTypeDefinition;
                    returnType = provideGeneratedTOFromExtendedType(typedef, innerExtendedType, basePackageName,
                            module.getName());
                } else if (innerTypeDefinition instanceof UnionTypeDefinition) {
                    final GeneratedTOBuilder genTOBuilder = provideGeneratedTOBuilderForUnionTypeDef(basePackageName,
                            (UnionTypeDefinition) innerTypeDefinition, typedefName, typedef);
                    genTOBuilder.setTypedef(true);
                    genTOBuilder.setIsUnion(true);
                    addUnitsToGenTO(genTOBuilder, typedef.getUnits());
                    makeSerializable((GeneratedTOBuilderImpl) genTOBuilder);
                    returnType = genTOBuilder.toInstance();
                    // union builder
                    GeneratedTOBuilder unionBuilder = new GeneratedTOBuilderImpl(genTOBuilder.getPackageName(),
                            genTOBuilder.getName() + "Builder");
                    unionBuilder.setIsUnionBuilder(true);
                    MethodSignatureBuilder method = unionBuilder.addMethod("getDefaultInstance");
                    method.setReturnType(returnType);
                    method.addParameter(Types.STRING, "defaultValue");
                    method.setAccessModifier(AccessModifier.PUBLIC);
                    method.setStatic(true);
                    Set<Type> types = additionalTypes.get(module);
                    if (types == null) {
                        types = Sets.<Type> newHashSet(unionBuilder.toInstance());
                        additionalTypes.put(module, types);
                    } else {
                        types.add(unionBuilder.toInstance());
                    }
                } else if (innerTypeDefinition instanceof EnumTypeDefinition) {
                    // enums are automatically Serializable
                    final EnumTypeDefinition enumTypeDef = (EnumTypeDefinition) innerTypeDefinition;
                    // TODO units for typedef enum
                    returnType = provideTypeForEnum(enumTypeDef, typedefName, typedef);
                } else if (innerTypeDefinition instanceof BitsTypeDefinition) {
                    final BitsTypeDefinition bitsTypeDefinition = (BitsTypeDefinition) innerTypeDefinition;
                    final GeneratedTOBuilder genTOBuilder = provideGeneratedTOBuilderForBitsTypeDefinition(
                            basePackageName, bitsTypeDefinition, typedefName, module.getName());
                    genTOBuilder.setTypedef(true);
                    addUnitsToGenTO(genTOBuilder, typedef.getUnits());
                    makeSerializable((GeneratedTOBuilderImpl) genTOBuilder);
                    returnType = genTOBuilder.toInstance();
                } else {
                    final Type javaType = BaseYangTypes.BASE_YANG_TYPES_PROVIDER.javaTypeForSchemaDefinitionType(
                            innerTypeDefinition, typedef);
                    returnType = wrapJavaTypeIntoTO(basePackageName, typedef, javaType, module.getName());
                }
                if (returnType != null) {
                    final Map<Date, Map<String, Type>> modulesByDate = genTypeDefsContextMap.get(moduleName);
                    Map<String, Type> typeMap = modulesByDate.get(moduleRevision);
                    if (typeMap != null) {
                        if (typeMap.isEmpty()) {
                            typeMap = new HashMap<>(4);
                            modulesByDate.put(moduleRevision, typeMap);
                        }
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
            final Type javaType, final String moduleName) {
        Preconditions.checkNotNull(javaType, "javaType cannot be null");
        final String propertyName = "value";

        final GeneratedTOBuilder genTOBuilder = typedefToTransferObject(basePackageName, typedef, moduleName);
        genTOBuilder.setRestrictions(BindingGeneratorUtil.getRestrictions(typedef));
        final GeneratedPropertyBuilder genPropBuilder = genTOBuilder.addProperty(propertyName);
        genPropBuilder.setReturnType(javaType);
        genTOBuilder.addEqualsIdentity(genPropBuilder);
        genTOBuilder.addHashIdentity(genPropBuilder);
        genTOBuilder.addToStringProperty(genPropBuilder);
        if (javaType instanceof ConcreteType && "String".equals(javaType.getName()) && typedef instanceof ExtendedType) {
            final List<String> regExps = resolveRegExpressionsFromTypedef((ExtendedType) typedef);
            addStringRegExAsConstant(genTOBuilder, regExps);
        }
        addUnitsToGenTO(genTOBuilder, typedef.getUnits());
        genTOBuilder.setTypedef(true);
        makeSerializable((GeneratedTOBuilderImpl) genTOBuilder);
        return genTOBuilder.toInstance();
    }

    /**
     * Converts output list of generated TO builders to one TO builder (first
     * from list) which contains the remaining builders as its enclosing TO.
     *
     * @param basePackageName
     *            string with name of package to which the module belongs
     * @param typedef
     *            type definition which should be of type
     *            <code>UnionTypeDefinition</code>
     * @param typeDefName
     *            string with name for generated TO
     * @return generated TO builder with the list of enclosed generated TO
     *         builders
     */
    public GeneratedTOBuilder provideGeneratedTOBuilderForUnionTypeDef(final String basePackageName,
            final UnionTypeDefinition typedef, final String typeDefName, final SchemaNode parentNode) {
        final List<GeneratedTOBuilder> genTOBuilders = provideGeneratedTOBuildersForUnionTypeDef(basePackageName,
                typedef, typeDefName, parentNode);
        GeneratedTOBuilder resultTOBuilder = null;
        if (genTOBuilders.isEmpty()) {
            throw new IllegalStateException("No GeneratedTOBuilder objects generated from union " + typedef);
        }

        resultTOBuilder = genTOBuilders.remove(0);
        for (GeneratedTOBuilder genTOBuilder : genTOBuilders) {
            resultTOBuilder.addEnclosingTransferObject(genTOBuilder);
        }

        final GeneratedPropertyBuilder genPropBuilder = resultTOBuilder.addProperty("value");
        genPropBuilder.setReturnType(Types.CHAR_ARRAY);
        resultTOBuilder.addEqualsIdentity(genPropBuilder);
        resultTOBuilder.addHashIdentity(genPropBuilder);
        resultTOBuilder.addToStringProperty(genPropBuilder);

        return resultTOBuilder;
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
     * @throws NullPointerException
     *             <ul>
     *             <li>if <code>basePackageName</code> is null</li>
     *             <li>if <code>typedef</code> is null</li>
     *             <li>if Qname of <code>typedef</code> is null</li>
     *             </ul>
     */
    public List<GeneratedTOBuilder> provideGeneratedTOBuildersForUnionTypeDef(final String basePackageName,
            final UnionTypeDefinition typedef, final String typeDefName, final SchemaNode parentNode) {
        Preconditions.checkNotNull(basePackageName, "Base Package Name cannot be NULL!");
        Preconditions.checkNotNull(typedef, "Type Definition cannot be NULL!");
        Preconditions.checkNotNull(typedef.getQName(), "Type definition QName cannot be NULL!");

        final List<GeneratedTOBuilder> generatedTOBuilders = new ArrayList<>();
        final List<TypeDefinition<?>> unionTypes = typedef.getTypes();
        final Module module = findParentModule(schemaContext, parentNode);

        final GeneratedTOBuilderImpl unionGenTOBuilder;
        if (typeDefName != null && !typeDefName.isEmpty()) {
            final String typeName = BindingMapping.getClassName(typeDefName);
            unionGenTOBuilder = new GeneratedTOBuilderImpl(basePackageName, typeName);
            unionGenTOBuilder.setDescription(typedef.getDescription());
            unionGenTOBuilder.setReference(typedef.getReference());
            unionGenTOBuilder.setSchemaPath(typedef.getPath().getPathFromRoot());
            unionGenTOBuilder.setModuleName(module.getName());
        } else {
            unionGenTOBuilder = typedefToTransferObject(basePackageName, typedef, module.getName());
        }

        generatedTOBuilders.add(unionGenTOBuilder);
        unionGenTOBuilder.setIsUnion(true);
        final List<String> regularExpressions = new ArrayList<String>();
        for (final TypeDefinition<?> unionType : unionTypes) {
            final String unionTypeName = unionType.getQName().getLocalName();
            if (unionType instanceof UnionType) {
                generatedTOBuilders.addAll(resolveUnionSubtypeAsUnion(unionGenTOBuilder, (UnionType) unionType,
                        basePackageName, parentNode));
            } else if (unionType instanceof ExtendedType) {
                resolveExtendedSubtypeAsUnion(unionGenTOBuilder, (ExtendedType) unionType, regularExpressions,
                        parentNode);
            } else if (unionType instanceof EnumTypeDefinition) {
                final Enumeration enumeration = addInnerEnumerationToTypeBuilder((EnumTypeDefinition) unionType,
                        unionTypeName, unionGenTOBuilder);
                updateUnionTypeAsProperty(unionGenTOBuilder, enumeration, unionTypeName);
            } else {
                final Type javaType = BaseYangTypes.BASE_YANG_TYPES_PROVIDER.javaTypeForSchemaDefinitionType(unionType,
                        parentNode);
                updateUnionTypeAsProperty(unionGenTOBuilder, javaType, unionTypeName);
            }
        }
        if (!regularExpressions.isEmpty()) {
            addStringRegExAsConstant(unionGenTOBuilder, regularExpressions);
        }

        storeGenTO(typedef, unionGenTOBuilder, parentNode);

        return generatedTOBuilders;
    }

    /**
     * Wraps code which handle case when union subtype is also of the type
     * <code>UnionType</code>.
     *
     * In this case the new generated TO is created for union subtype (recursive
     * call of method
     * {@link #provideGeneratedTOBuildersForUnionTypeDef(String, UnionTypeDefinition,
     * String, SchemaNode)}
     * provideGeneratedTOBuilderForUnionTypeDef} and in parent TO builder
     * <code>parentUnionGenTOBuilder</code> is created property which type is
     * equal to new generated TO.
     *
     * @param parentUnionGenTOBuilder
     *            generated TO builder to which is the property with the child
     *            union subtype added
     * @param basePackageName
     *            string with the name of the module package
     * @param unionSubtype
     *            type definition which represents union subtype
     * @return list of generated TO builders. The number of the builders can be
     *         bigger one due to recursive call of
     *         <code>provideGeneratedTOBuildersForUnionTypeDef</code> method.
     */
    private List<GeneratedTOBuilder> resolveUnionSubtypeAsUnion(final GeneratedTOBuilder parentUnionGenTOBuilder,
            final UnionTypeDefinition unionSubtype, final String basePackageName, final SchemaNode parentNode) {
        final String newTOBuilderName = provideAvailableNameForGenTOBuilder(parentUnionGenTOBuilder.getName());
        final List<GeneratedTOBuilder> subUnionGenTOBUilders = provideGeneratedTOBuildersForUnionTypeDef(
                basePackageName, unionSubtype, newTOBuilderName, parentNode);

        final GeneratedPropertyBuilder propertyBuilder;
        propertyBuilder = parentUnionGenTOBuilder.addProperty(BindingMapping.getPropertyName(newTOBuilderName));
        propertyBuilder.setReturnType(subUnionGenTOBUilders.get(0));
        parentUnionGenTOBuilder.addEqualsIdentity(propertyBuilder);
        parentUnionGenTOBuilder.addToStringProperty(propertyBuilder);

        return subUnionGenTOBUilders;
    }

    /**
     * Wraps code which handle case when union subtype is of the type
     * <code>ExtendedType</code>.
     *
     * If TO for this type already exists it is used for the creation of the
     * property in <code>parentUnionGenTOBuilder</code>. In other case the base
     * type is used for the property creation.
     *
     * @param parentUnionGenTOBuilder
     *            generated TO builder in which new property is created
     * @param unionSubtype
     *            type definition of the <code>ExtendedType</code> type which
     *            represents union subtype
     * @param regularExpressions
     *            list of strings with the regular expressions
     * @param parentNode
     *            parent Schema Node for Extended Subtype
     *
     */
    private void resolveExtendedSubtypeAsUnion(final GeneratedTOBuilder parentUnionGenTOBuilder,
            final ExtendedType unionSubtype, final List<String> regularExpressions, final SchemaNode parentNode) {
        final String unionTypeName = unionSubtype.getQName().getLocalName();
        final Type genTO = findGenTO(unionTypeName, unionSubtype);
        if (genTO != null) {
            updateUnionTypeAsProperty(parentUnionGenTOBuilder, genTO, genTO.getName());
        } else {
            final TypeDefinition<?> baseType = baseTypeDefForExtendedType(unionSubtype);
            if (unionTypeName.equals(baseType.getQName().getLocalName())) {
                final Type javaType = BaseYangTypes.BASE_YANG_TYPES_PROVIDER.javaTypeForSchemaDefinitionType(baseType,
                        parentNode);
                if (javaType != null) {
                    updateUnionTypeAsProperty(parentUnionGenTOBuilder, javaType, unionTypeName);
                }
            }
            if (baseType instanceof StringType) {
                regularExpressions.addAll(resolveRegExpressionsFromTypedef(unionSubtype));
            }
        }
    }

    /**
     * Searches for generated TO for <code>searchedTypeDef</code> type
     * definition in {@link #genTypeDefsContextMap genTypeDefsContextMap}
     *
     * @param searchedTypeName
     *            string with name of <code>searchedTypeDef</code>
     * @return generated TO for <code>searchedTypeDef</code> or
     *         <code>null</code> it it doesn't exist
     */
    private Type findGenTO(final String searchedTypeName, final SchemaNode parentNode) {
        final Module typeModule = findParentModule(schemaContext, parentNode);
        if (typeModule != null && typeModule.getName() != null) {
            final Map<Date, Map<String, Type>> modulesByDate = genTypeDefsContextMap.get(typeModule.getName());
            final Map<String, Type> genTOs = modulesByDate.get(typeModule.getRevision());
            if (genTOs != null) {
                return genTOs.get(searchedTypeName);
            }
        }
        return null;
    }

    /**
     * Stores generated TO created from <code>genTOBuilder</code> for
     * <code>newTypeDef</code> to {@link #genTypeDefsContextMap
     * genTypeDefsContextMap} if the module for <code>newTypeDef</code> exists
     *
     * @param newTypeDef
     *            type definition for which is <code>genTOBuilder</code> created
     * @param genTOBuilder
     *            generated TO builder which is converted to generated TO and
     *            stored
     */
    private void storeGenTO(final TypeDefinition<?> newTypeDef, final GeneratedTOBuilder genTOBuilder,
            final SchemaNode parentNode) {
        if (!(newTypeDef instanceof UnionType)) {

            final Module parentModule = findParentModule(schemaContext, parentNode);
            if (parentModule != null && parentModule.getName() != null) {
                Map<Date, Map<String, Type>> modulesByDate = genTypeDefsContextMap.get(parentModule.getName());
                Map<String, Type> genTOsMap = modulesByDate.get(parentModule.getRevision());
                genTOsMap.put(newTypeDef.getQName().getLocalName(), genTOBuilder.toInstance());
            }
        }
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
    private static void updateUnionTypeAsProperty(final GeneratedTOBuilder unionGenTransObject, final Type type,
            final String propertyName) {
        if (unionGenTransObject != null && type != null && !unionGenTransObject.containsProperty(propertyName)) {
            final GeneratedPropertyBuilder propBuilder = unionGenTransObject
                    .addProperty(BindingMapping.getPropertyName(propertyName));
            propBuilder.setReturnType(type);

            unionGenTransObject.addEqualsIdentity(propBuilder);
            unionGenTransObject.addHashIdentity(propBuilder);
            unionGenTransObject.addToStringProperty(propBuilder);
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
    private static GeneratedTOBuilderImpl typedefToTransferObject(final String basePackageName,
            final TypeDefinition<?> typedef, final String moduleName) {

        final String packageName = packageNameForGeneratedType(basePackageName, typedef.getPath());
        final String typeDefTOName = typedef.getQName().getLocalName();

        if ((packageName != null) && (typeDefTOName != null)) {
            final String genTOName = BindingMapping.getClassName(typeDefTOName);
            final GeneratedTOBuilderImpl newType = new GeneratedTOBuilderImpl(packageName, genTOName);

            newType.setDescription(typedef.getDescription());
            newType.setReference(typedef.getReference());
            newType.setSchemaPath(typedef.getPath().getPathFromRoot());
            newType.setModuleName(moduleName);

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
            final TypeDefinition<?> typeDef, final String typeDefName, final String moduleName) {

        Preconditions.checkArgument(typeDef != null, "typeDef cannot be NULL!");
        Preconditions.checkArgument(basePackageName != null, "Base Package Name cannot be NULL!");

        if (typeDef instanceof BitsTypeDefinition) {
            BitsTypeDefinition bitsTypeDefinition = (BitsTypeDefinition) typeDef;

            final String typeName = BindingMapping.getClassName(typeDefName);
            final GeneratedTOBuilderImpl genTOBuilder = new GeneratedTOBuilderImpl(basePackageName, typeName);

            genTOBuilder.setDescription(typeDef.getDescription());
            genTOBuilder.setReference(typeDef.getReference());
            genTOBuilder.setSchemaPath(typeDef.getPath().getPathFromRoot());
            genTOBuilder.setModuleName(moduleName);
            genTOBuilder.setBaseType(typeDef);

            final List<Bit> bitList = bitsTypeDefinition.getBits();
            GeneratedPropertyBuilder genPropertyBuilder;
            for (final Bit bit : bitList) {
                String name = bit.getName();
                genPropertyBuilder = genTOBuilder.addProperty(BindingMapping.getPropertyName(name));
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
    private List<String> resolveRegExpressionsFromTypedef(final ExtendedType typedef) {
        final List<String> regExps = new ArrayList<String>();
        Preconditions.checkArgument(typedef != null, "typedef can't be null");
        final TypeDefinition<?> strTypeDef = baseTypeDefForExtendedType(typedef);
        if (strTypeDef instanceof StringType) {
            final List<PatternConstraint> patternConstraints = typedef.getPatternConstraints();
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
    private static void addStringRegExAsConstant(final GeneratedTOBuilder genTOBuilder, final List<String> regularExpressions) {
        if (genTOBuilder == null) {
            throw new IllegalArgumentException("Generated transfer object builder can't be null");
        }
        if (regularExpressions == null) {
            throw new IllegalArgumentException("List of regular expressions can't be null");
        }
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
     * @param typedef
     *            Type Definition
     * @param innerExtendedType
     *            extended type which is part of some other extended type
     * @param basePackageName
     *            string with the package name of the module
     * @param moduleName
     *            Module Name
     * @return generated TO which extends generated TO for
     *         <code>innerExtendedType</code>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>extendedType</code> equals null</li>
     *             <li>if <code>basePackageName</code> equals null</li>
     *             <li>if <code>typedefName</code> equals null</li>
     *             </ul>
     */
    private GeneratedTransferObject provideGeneratedTOFromExtendedType(final TypeDefinition<?> typedef,
            final ExtendedType innerExtendedType, final String basePackageName, final String moduleName) {
        Preconditions.checkArgument(innerExtendedType != null, "Extended type cannot be NULL!");
        Preconditions.checkArgument(basePackageName != null, "String with base package name cannot be NULL!");

        final String typedefName = typedef.getQName().getLocalName();
        final String classTypedefName = BindingMapping.getClassName(typedefName);
        final String innerTypeDef = innerExtendedType.getQName().getLocalName();
        final GeneratedTOBuilderImpl genTOBuilder = new GeneratedTOBuilderImpl(basePackageName, classTypedefName);

        genTOBuilder.setDescription(typedef.getDescription());
        genTOBuilder.setReference(typedef.getReference());
        genTOBuilder.setSchemaPath(typedef.getPath().getPathFromRoot());
        genTOBuilder.setModuleName(moduleName);
        genTOBuilder.setTypedef(true);
        Restrictions r = BindingGeneratorUtil.getRestrictions(typedef);
        genTOBuilder.setRestrictions(r);

        if (baseTypeDefForExtendedType(innerExtendedType) instanceof UnionTypeDefinition) {
            genTOBuilder.setIsUnion(true);
        }

        Map<Date, Map<String, Type>> modulesByDate = null;
        Map<String, Type> typeMap = null;
        final Module parentModule = findParentModule(schemaContext, innerExtendedType);
        if (parentModule != null) {
            modulesByDate = genTypeDefsContextMap.get(parentModule.getName());
            typeMap = modulesByDate.get(parentModule.getRevision());
        }

        if (typeMap != null) {
            Type type = typeMap.get(innerTypeDef);
            if (type instanceof GeneratedTransferObject) {
                genTOBuilder.setExtendsType((GeneratedTransferObject) type);
            }
        }
        addUnitsToGenTO(genTOBuilder, typedef.getUnits());
        makeSerializable(genTOBuilder);

        return genTOBuilder.toInstance();
    }

    /**
     * Add {@link Serializable} to implemented interfaces of this TO. Also
     * compute and add serialVersionUID property.
     *
     * @param gto
     *            transfer object which needs to be serializable
     */
    private static void makeSerializable(final GeneratedTOBuilderImpl gto) {
        gto.addImplementsType(Types.typeForClass(Serializable.class));
        GeneratedPropertyBuilder prop = new GeneratedPropertyBuilderImpl("serialVersionUID");
        prop.setValue(Long.toString(BindingGeneratorUtil.computeDefaultSUID(gto)));
        gto.setSUID(prop);
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
            final Collection<TypeDefinition<?>> unsortedTypeDefinitions) {
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

        // SortedMap guarantees order corresponding to keys in ascending order
        for (List<TypeDefinition<?>> v : typeDefinitionsDepths.values()) {
            sortedTypeDefinition.addAll(v);
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
            return 1;
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
                childDepth = childDepth + getTypeDefinitionDepth(childTypeDefinition);
                if (childDepth > maxChildDepth) {
                    maxChildDepth = childDepth;
                }
            }
            return maxChildDepth;
        }
        return depth;
    }

    /**
     * Returns string which contains the same value as <code>name</code> but
     * integer suffix is incremented by one. If <code>name</code> contains no
     * number suffix then number 1 is added.
     *
     * @param name
     *            string with name of augmented node
     * @return string with the number suffix incremented by one (or 1 is added)
     */
    private static String provideAvailableNameForGenTOBuilder(final String name) {
        Matcher mtch = NUMBERS_PATTERN.matcher(name);
        if (mtch.find()) {
            final int newSuffix = Integer.valueOf(name.substring(mtch.start())) + 1;
            return name.substring(0, mtch.start()) + newSuffix;
        } else {
            return name + 1;
        }
    }

    public void addUnitsToGenTO(final GeneratedTOBuilder to, final String units) {
        if (units != null && !units.isEmpty()) {
            to.addConstant(Types.STRING, "_UNITS", "\"" + units + "\"");
            GeneratedPropertyBuilder prop = new GeneratedPropertyBuilderImpl("UNITS");
            prop.setReturnType(Types.STRING);
            to.addToStringProperty(prop);
        }
    }

    @Override
    public String getTypeDefaultConstruction(final LeafSchemaNode node) {
        return getTypeDefaultConstruction(node, node.getDefault());
    }

    public String getTypeDefaultConstruction(final LeafSchemaNode node, final String defaultValue) {
        TypeDefinition<?> type = node.getType();
        QName typeQName = type.getQName();
        TypeDefinition<?> base = baseTypeDefForExtendedType(type);
        Preconditions.checkNotNull(type, "Cannot provide default construction for null type of %s", node);
        Preconditions.checkNotNull(defaultValue, "Cannot provide default construction for null default statement of %s",
                node);

        StringBuilder sb = new StringBuilder();
        String result = null;
        if (base instanceof BinaryTypeDefinition) {
            result = binaryToDef(defaultValue);
        } else if (base instanceof BitsTypeDefinition) {
            String parentName;
            String className;
            Module parent = getParentModule(node);
            Iterator<QName> path = node.getPath().getPathFromRoot().iterator();
            path.next();
            if (!(path.hasNext())) {
                parentName = BindingMapping.getClassName(parent.getName()) + "Data";
                String basePackageName = BindingMapping.getRootPackageName(parent.getQNameModule());
                className = basePackageName + "." + parentName + "." + BindingMapping.getClassName(node.getQName());
            } else {
                String basePackageName = BindingMapping.getRootPackageName(parent.getQNameModule());
                String packageName = packageNameForGeneratedType(basePackageName, type.getPath());
                parentName = BindingMapping.getClassName(parent.getName());
                className = packageName + "." + parentName + "." + BindingMapping.getClassName(node.getQName());
            }
            result = bitsToDef((BitsTypeDefinition) base, className, defaultValue, type instanceof ExtendedType);
        } else if (base instanceof BooleanTypeDefinition) {
            result = typeToDef(Boolean.class, defaultValue);
        } else if (base instanceof DecimalTypeDefinition) {
            result = typeToDef(BigDecimal.class, defaultValue);
        } else if (base instanceof EmptyTypeDefinition) {
            result = typeToDef(Boolean.class, defaultValue);
        } else if (base instanceof EnumTypeDefinition) {
            char[] defValArray = defaultValue.toCharArray();
            char first = Character.toUpperCase(defaultValue.charAt(0));
            defValArray[0] = first;
            String newDefVal = new String(defValArray);
            String className;
            if (type instanceof ExtendedType) {
                Module m = getParentModule(type);
                String basePackageName = BindingMapping.getRootPackageName(m.getQNameModule());
                String packageName = packageNameForGeneratedType(basePackageName, type.getPath());
                className = packageName + "." + BindingMapping.getClassName(typeQName);
            } else {
                Module parentModule = getParentModule(node);
                String basePackageName = BindingMapping.getRootPackageName(parentModule.getQNameModule());
                String packageName = packageNameForGeneratedType(basePackageName, node.getPath());
                className = packageName + "." + BindingMapping.getClassName(node.getQName());
            }
            result = className + "." + newDefVal;
        } else if (base instanceof IdentityrefTypeDefinition) {
            throw new UnsupportedOperationException("Cannot get default construction for identityref type");
        } else if (base instanceof InstanceIdentifierTypeDefinition) {
            throw new UnsupportedOperationException("Cannot get default construction for instance-identifier type");
        } else if (base instanceof Int8) {
            result = typeToDef(Byte.class, defaultValue);
        } else if (base instanceof Int16) {
            result = typeToDef(Short.class, defaultValue);
        } else if (base instanceof Int32) {
            result = typeToDef(Integer.class, defaultValue);
        } else if (base instanceof Int64) {
            result = typeToDef(Long.class, defaultValue);
        } else if (base instanceof LeafrefTypeDefinition) {
            result = leafrefToDef(node, (LeafrefTypeDefinition) base, defaultValue);
        } else if (base instanceof StringTypeDefinition) {
            result = "\"" + defaultValue + "\"";
        } else if (base instanceof Uint8) {
            result = typeToDef(Short.class, defaultValue);
        } else if (base instanceof Uint16) {
            result = typeToDef(Integer.class, defaultValue);
        } else if (base instanceof Uint32) {
            result = typeToDef(Long.class, defaultValue);
        } else if (base instanceof Uint64) {
            result = typeToDef(BigInteger.class, defaultValue);
        } else if (base instanceof UnionTypeDefinition) {
            result = unionToDef(node);
        } else {
            result = "";
        }
        sb.append(result);

        if (type instanceof ExtendedType && !(base instanceof LeafrefTypeDefinition)
                && !(base instanceof EnumerationType) && !(base instanceof UnionTypeDefinition)) {
            Module m = getParentModule(type);
            String basePackageName = BindingMapping.getRootPackageName(m.getQNameModule());
            String packageName = packageNameForGeneratedType(basePackageName, type.getPath());
            String className = packageName + "." + BindingMapping.getClassName(typeQName);
            sb.insert(0, "new " + className + "(");
            sb.insert(sb.length(), ')');
        }

        return sb.toString();
    }

    private static String typeToDef(final Class<?> clazz, final String defaultValue) {
        return "new " + clazz.getName() + "(\"" + defaultValue + "\")";
    }

    private static String binaryToDef(final String defaultValue) {
        StringBuilder sb = new StringBuilder();
        BaseEncoding en = BaseEncoding.base64();
        byte[] encoded = en.decode(defaultValue);
        sb.append("new byte[] {");
        for (int i = 0; i < encoded.length; i++) {
            sb.append(encoded[i]);
            if (i != encoded.length - 1) {
                sb.append(", ");
            }
        }
        sb.append('}');
        return sb.toString();
    }

    private static String bitsToDef(final BitsTypeDefinition type, final String className, final String defaultValue,
            final boolean isExt) {
        List<Bit> bits = new ArrayList<>(type.getBits());
        Collections.sort(bits, new Comparator<Bit>() {
            @Override
            public int compare(final Bit o1, final Bit o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        StringBuilder sb = new StringBuilder();
        if (!isExt) {
            sb.append("new ");
            sb.append(className);
            sb.append('(');
        }
        for (int i = 0; i < bits.size(); i++) {
            if (bits.get(i).getName().equals(defaultValue)) {
                sb.append(true);
            } else {
                sb.append(false);
            }
            if (i != bits.size() - 1) {
                sb.append(", ");
            }
        }
        if (!isExt) {
            sb.append(')');
        }
        return sb.toString();
    }

    private Module getParentModule(final SchemaNode node) {
        QName qname = node.getPath().getPathFromRoot().iterator().next();
        URI namespace = qname.getNamespace();
        Date revision = qname.getRevision();
        return schemaContext.findModuleByNamespaceAndRevision(namespace, revision);
    }

    private String leafrefToDef(final LeafSchemaNode parentNode, final LeafrefTypeDefinition leafrefType, final String defaultValue) {
        Preconditions.checkArgument(leafrefType != null, "Leafref Type Definition reference cannot be NULL!");
        Preconditions.checkArgument(leafrefType.getPathStatement() != null,
                "The Path Statement for Leafref Type Definition cannot be NULL!");

        final RevisionAwareXPath xpath = leafrefType.getPathStatement();
        final String strXPath = xpath.toString();

        if (strXPath != null) {
            if (strXPath.indexOf('[') == -1) {
                final Module module = findParentModule(schemaContext, parentNode);
                if (module != null) {
                    final SchemaNode dataNode;
                    if (xpath.isAbsolute()) {
                        dataNode = findDataSchemaNode(schemaContext, module, xpath);
                    } else {
                        dataNode = findDataSchemaNodeForRelativeXPath(schemaContext, module, parentNode, xpath);
                    }
                    String result = getTypeDefaultConstruction((LeafSchemaNode) dataNode, defaultValue);
                    return result;
                }
            } else {
                return "new java.lang.Object()";
            }
        }

        return null;
    }

    private String unionToDef(final LeafSchemaNode node) {
        String parentName;
        String className;

        if (node.getType() instanceof ExtendedType) {
            ExtendedType type = (ExtendedType) node.getType();
            QName typeQName = type.getQName();
            Module module = null;
            Set<Module> modules = schemaContext.findModuleByNamespace(typeQName.getNamespace());
            if (modules.size() > 1) {
                for (Module m : modules) {
                    if (m.getRevision().equals(typeQName.getRevision())) {
                        module = m;
                        break;
                    }
                }
                if (module == null) {
                    List<Module> modulesList = new ArrayList<>(modules);
                    Collections.sort(modulesList, new Comparator<Module>() {
                        @Override
                        public int compare(final Module o1, final Module o2) {
                            return o1.getRevision().compareTo(o2.getRevision());
                        }
                    });
                    module = modulesList.get(0);
                }
            } else {
                module = modules.iterator().next();
            }

            String basePackageName = BindingMapping.getRootPackageName(module.getQNameModule());
            className = basePackageName + "." + BindingMapping.getClassName(typeQName);
        } else {
            Iterator<QName> path = node.getPath().getPathFromRoot().iterator();
            QName first = path.next();
            if (!(path.hasNext())) {
                URI namespace = first.getNamespace();
                Date revision = first.getRevision();
                Module parent = schemaContext.findModuleByNamespaceAndRevision(namespace, revision);
                parentName = BindingMapping.getClassName((parent).getName()) + "Data";
                String basePackageName = BindingMapping.getRootPackageName(parent.getQNameModule());
                className = basePackageName + "." + parentName + "." + BindingMapping.getClassName(node.getQName());
            } else {
                URI namespace = first.getNamespace();
                Date revision = first.getRevision();
                Module parentModule = schemaContext.findModuleByNamespaceAndRevision(namespace, revision);
                String basePackageName = BindingMapping.getRootPackageName(parentModule.getQNameModule());
                String packageName = packageNameForGeneratedType(basePackageName, node.getType().getPath());
                className = packageName + "." + BindingMapping.getClassName(node.getQName());
            }
        }
        return union(className, node.getDefault(), node);
    }

    private static String union(final String className, final String defaultValue, final LeafSchemaNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append("new ");
        sb.append(className);
        sb.append("(\"");
        sb.append(defaultValue);
        sb.append("\".toCharArray())");
        return sb.toString();
    }

    @Override
    public String getConstructorPropertyName(final SchemaNode node) {
        if (node instanceof TypeDefinition<?>) {
            return "value";
        } else {
            return "";
        }
    }

    @Override
    public String getParamNameFromType(final TypeDefinition<?> type) {
        return BindingMapping.getPropertyName(type.getQName().getLocalName());
    }

}
