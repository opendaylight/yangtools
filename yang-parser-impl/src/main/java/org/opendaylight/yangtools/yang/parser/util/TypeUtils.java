/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import static org.opendaylight.yangtools.yang.parser.util.ParserUtils.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
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
import org.opendaylight.yangtools.yang.model.util.UnknownType;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeAwareBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.IdentityrefTypeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.RpcDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.TypeDefinitionBuilderImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnionTypeBuilder;

/**
 * Utility class which contains helper methods for dealing with type operations.
 */
public class TypeUtils {

    private TypeUtils() {
    }

    /**
     * Resolve unknown type of node. It is assumed that type of node is either
     * UnknownType or ExtendedType with UnknownType as base type.
     *
     * @param nodeToResolve
     *            node with type to resolve
     * @param modules
     *            all loaded modules
     * @param module
     *            current module
     */
    public static void resolveType(final TypeAwareBuilder nodeToResolve,
            final Map<String, TreeMap<Date, ModuleBuilder>> modules, final ModuleBuilder module) {
        TypeDefinitionBuilder resolvedType = null;
        final int line = nodeToResolve.getLine();
        final TypeDefinition<?> nodeToResolveType = nodeToResolve.getType();
        final QName unknownTypeQName = nodeToResolveType.getBaseType().getQName();
        final ModuleBuilder dependentModule = findDependentModuleBuilder(modules, module, unknownTypeQName.getPrefix(),
                line);

        final TypeDefinitionBuilder targetTypeBuilder = findTypeDefinitionBuilder(nodeToResolve, dependentModule,
                unknownTypeQName.getLocalName(), module.getName(), line);

        if (nodeToResolveType instanceof ExtendedType) {
            final ExtendedType extType = (ExtendedType) nodeToResolveType;
            final TypeDefinitionBuilder newType = extendedTypeWithNewBaseTypeBuilder(targetTypeBuilder, extType,
                    modules, module, nodeToResolve.getLine());
            resolvedType = newType;
        } else {
            resolvedType = targetTypeBuilder;
        }

        // validate constraints
        final TypeConstraints constraints = findConstraintsFromTypeBuilder(nodeToResolve,
                new TypeConstraints(module.getName(), nodeToResolve.getLine()), modules, module, null);
        constraints.validateConstraints();

        nodeToResolve.setTypedef(resolvedType);
    }

    /**
     * Resolve unknown type of node. It is assumed that type of node is either
     * UnknownType or ExtendedType with UnknownType as base type.
     *
     * @param nodeToResolve
     *            node with type to resolve
     * @param modules
     *            all loaded modules
     * @param module
     *            current module
     * @param context
     *            SchemaContext containing already resolved modules
     */
    public static void resolveTypeWithContext(final TypeAwareBuilder nodeToResolve,
            final Map<String, TreeMap<Date, ModuleBuilder>> modules, final ModuleBuilder module,
            final SchemaContext context) {
        TypeDefinitionBuilder resolvedType = null;
        final int line = nodeToResolve.getLine();
        final TypeDefinition<?> nodeToResolveType = nodeToResolve.getType();
        final QName unknownTypeQName = nodeToResolveType.getBaseType().getQName();
        final ModuleBuilder dependentModuleBuilder = findDependentModuleBuilder(modules, module,
                unknownTypeQName.getPrefix(), line);

        if (dependentModuleBuilder == null) {
            final Module dependentModule = findModuleFromContext(context, module, unknownTypeQName.getPrefix(), line);
            final Set<TypeDefinition<?>> types = dependentModule.getTypeDefinitions();
            final TypeDefinition<?> type = findTypeByName(types, unknownTypeQName.getLocalName());

            if (nodeToResolveType instanceof ExtendedType) {
                final ExtendedType extType = (ExtendedType) nodeToResolveType;
                final TypeDefinitionBuilder newType = extendedTypeWithNewBaseType(type, extType, module,
                        nodeToResolve.getLine());

                nodeToResolve.setTypedef(newType);
            } else {
                if (nodeToResolve instanceof TypeDefinitionBuilder) {
                    TypeDefinitionBuilder tdb = (TypeDefinitionBuilder) nodeToResolve;
                    TypeConstraints tc = findConstraintsFromTypeBuilder(nodeToResolve,
                            new TypeConstraints(module.getName(), nodeToResolve.getLine()), modules, module, context);
                    tdb.setLengths(tc.getLength());
                    tdb.setPatterns(tc.getPatterns());
                    tdb.setRanges(tc.getRange());
                    tdb.setFractionDigits(tc.getFractionDigits());
                }
                nodeToResolve.setType(type);
            }

        } else {
            final TypeDefinitionBuilder targetTypeBuilder = findTypeDefinitionBuilder(nodeToResolve,
                    dependentModuleBuilder, unknownTypeQName.getLocalName(), module.getName(), line);

            if (nodeToResolveType instanceof ExtendedType) {
                final ExtendedType extType = (ExtendedType) nodeToResolveType;
                final TypeDefinitionBuilder newType = extendedTypeWithNewBaseTypeBuilder(targetTypeBuilder, extType,
                        modules, module, nodeToResolve.getLine());
                resolvedType = newType;
            } else {
                resolvedType = targetTypeBuilder;
            }

            // validate constraints
            final TypeConstraints constraints = findConstraintsFromTypeBuilder(nodeToResolve, new TypeConstraints(
                    module.getName(), nodeToResolve.getLine()), modules, module, context);
            constraints.validateConstraints();

            nodeToResolve.setTypedef(resolvedType);
        }
    }

    public static void resolveTypeUnion(final UnionTypeBuilder union,
            final Map<String, TreeMap<Date, ModuleBuilder>> modules, final ModuleBuilder builder) {

        final List<TypeDefinition<?>> unionTypes = union.getTypes();
        final List<TypeDefinition<?>> toRemove = new ArrayList<TypeDefinition<?>>();
        for (TypeDefinition<?> unionType : unionTypes) {
            if (unionType instanceof UnknownType) {
                final UnknownType ut = (UnknownType) unionType;
                final ModuleBuilder dependentModule = findDependentModuleBuilder(modules, builder, ut.getQName()
                        .getPrefix(), union.getLine());
                final TypeDefinitionBuilder resolvedType = findTypeDefinitionBuilder(union, dependentModule, ut
                        .getQName().getLocalName(), builder.getName(), union.getLine());
                union.setTypedef(resolvedType);
                toRemove.add(ut);
            } else if (unionType instanceof ExtendedType) {
                final ExtendedType extType = (ExtendedType) unionType;
                final TypeDefinition<?> extTypeBase = extType.getBaseType();
                if (extTypeBase instanceof UnknownType) {
                    final UnknownType ut = (UnknownType) extTypeBase;
                    final ModuleBuilder dependentModule = findDependentModuleBuilder(modules, builder, ut.getQName()
                            .getPrefix(), union.getLine());
                    final TypeDefinitionBuilder targetTypeBuilder = findTypeDefinitionBuilder(union, dependentModule,
                            ut.getQName().getLocalName(), builder.getName(), union.getLine());

                    final TypeDefinitionBuilder newType = extendedTypeWithNewBaseTypeBuilder(targetTypeBuilder,
                            extType, modules, builder, union.getLine());

                    union.setTypedef(newType);
                    toRemove.add(extType);
                }
            }
        }
        unionTypes.removeAll(toRemove);
    }

    public static void resolveTypeUnionWithContext(final UnionTypeBuilder union,
            final Map<String, TreeMap<Date, ModuleBuilder>> modules, final ModuleBuilder builder,
            final SchemaContext context) {

        final List<TypeDefinition<?>> unionTypes = union.getTypes();
        final List<TypeDefinition<?>> toRemove = new ArrayList<TypeDefinition<?>>();
        for (TypeDefinition<?> unionType : unionTypes) {
            if (unionType instanceof UnknownType) {
                final UnknownType ut = (UnknownType) unionType;
                final QName utQName = ut.getQName();
                final ModuleBuilder dependentModuleBuilder = findDependentModuleBuilder(modules, builder,
                        utQName.getPrefix(), union.getLine());

                if (dependentModuleBuilder == null) {
                    Module dependentModule = findModuleFromContext(context, builder, utQName.getPrefix(),
                            union.getLine());
                    Set<TypeDefinition<?>> types = dependentModule.getTypeDefinitions();
                    TypeDefinition<?> type = findTypeByName(types, utQName.getLocalName());
                    union.setType(type);
                    toRemove.add(ut);
                } else {
                    final TypeDefinitionBuilder resolvedType = findTypeDefinitionBuilder(union, dependentModuleBuilder,
                            utQName.getLocalName(), builder.getName(), union.getLine());
                    union.setTypedef(resolvedType);
                    toRemove.add(ut);
                }

            } else if (unionType instanceof ExtendedType) {
                final ExtendedType extType = (ExtendedType) unionType;
                TypeDefinition<?> extTypeBase = extType.getBaseType();
                if (extTypeBase instanceof UnknownType) {
                    final UnknownType ut = (UnknownType) extTypeBase;
                    final QName utQName = ut.getQName();
                    final ModuleBuilder dependentModuleBuilder = findDependentModuleBuilder(modules, builder,
                            utQName.getPrefix(), union.getLine());

                    if (dependentModuleBuilder == null) {
                        final Module dependentModule = findModuleFromContext(context, builder, utQName.getPrefix(),
                                union.getLine());
                        Set<TypeDefinition<?>> types = dependentModule.getTypeDefinitions();
                        TypeDefinition<?> type = findTypeByName(types, utQName.getLocalName());
                        final TypeDefinitionBuilder newType = extendedTypeWithNewBaseType(type, extType, builder, 0);

                        union.setTypedef(newType);
                        toRemove.add(extType);
                    } else {
                        final TypeDefinitionBuilder targetTypeBuilder = findTypeDefinitionBuilder(union,
                                dependentModuleBuilder, utQName.getLocalName(), builder.getName(), union.getLine());

                        final TypeDefinitionBuilder newType = extendedTypeWithNewBaseTypeBuilder(targetTypeBuilder,
                                extType, modules, builder, union.getLine());

                        union.setTypedef(newType);
                        toRemove.add(extType);
                    }
                }
            }
        }
        unionTypes.removeAll(toRemove);
    }

    /**
     * Search types for type with given name.
     *
     * @param types
     *            types to search
     * @param name
     *            name of type
     * @return type with given name if present in collection, null otherwise
     */
    private static TypeDefinitionBuilder findTypedefBuilderByName(Set<TypeDefinitionBuilder> types, String name) {
        for (TypeDefinitionBuilder td : types) {
            if (td.getQName().getLocalName().equals(name)) {
                return td;
            }
        }
        return null;
    }

    /**
     * Find type by name.
     *
     * @param types
     *            collection of types
     * @param typeName
     *            type name
     * @return type with given name if it is present in collection, null
     *         otherwise
     */
    private static TypeDefinition<?> findTypeByName(Set<TypeDefinition<?>> types, String typeName) {
        for (TypeDefinition<?> type : types) {
            if (type.getQName().getLocalName().equals(typeName)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Pull restriction from type and add them to constraints.
     *
     * @param type
     * @param constraints
     */
    private static void mergeConstraints(final TypeDefinition<?> type, final TypeConstraints constraints) {
        if (type instanceof DecimalTypeDefinition) {
            constraints.addRanges(((DecimalTypeDefinition) type).getRangeStatements());
            constraints.addFractionDigits(((DecimalTypeDefinition) type).getFractionDigits());
        } else if (type instanceof IntegerTypeDefinition) {
            constraints.addRanges(((IntegerTypeDefinition) type).getRangeStatements());
        } else if (type instanceof StringTypeDefinition) {
            constraints.addPatterns(((StringTypeDefinition) type).getPatterns());
            constraints.addLengths(((StringTypeDefinition) type).getLengthStatements());
        } else if (type instanceof BinaryTypeDefinition) {
            constraints.addLengths(((BinaryTypeDefinition) type).getLengthConstraints());
        }
    }

    /**
     * Create new ExtendedType based on given type and with schema path.
     *
     * @param newPath
     *            schema path for new type
     * @param oldType
     *            type based
     * @return
     */
    static ExtendedType createNewExtendedType(final ExtendedType oldType, final SchemaPath newPath) {
        QName qname = oldType.getQName();
        TypeDefinition<?> baseType = oldType.getBaseType();
        String desc = oldType.getDescription();
        String ref = oldType.getReference();
        ExtendedType.Builder builder = new ExtendedType.Builder(qname, baseType, desc, ref, newPath);
        builder.status(oldType.getStatus());
        builder.lengths(oldType.getLengths());
        builder.patterns(oldType.getPatterns());
        builder.ranges(oldType.getRanges());
        builder.fractionDigits(oldType.getFractionDigits());
        builder.unknownSchemaNodes(oldType.getUnknownSchemaNodes());
        return builder.build();
    }

    static StringTypeDefinition createNewStringType(final SchemaPath schemaPath, final QName nodeQName,
            final StringTypeDefinition nodeType) {
        final List<QName> path = schemaPath.getPath();
        final List<QName> newPath = new ArrayList<QName>(path);
        newPath.add(nodeQName);
        newPath.add(nodeType.getQName());
        final SchemaPath newSchemaPath = new SchemaPath(newPath, schemaPath.isAbsolute());
        return new StringType(newSchemaPath);
    }

    static IntegerTypeDefinition createNewIntType(final SchemaPath schemaPath, final QName nodeQName,
            final IntegerTypeDefinition type) {
        final QName typeQName = type.getQName();
        final SchemaPath newSchemaPath = createSchemaPath(schemaPath, nodeQName, typeQName);
        final String localName = typeQName.getLocalName();

        if ("int8".equals(localName)) {
            return new Int8(newSchemaPath);
        } else if ("int16".equals(localName)) {
            return new Int16(newSchemaPath);
        } else if ("int32".equals(localName)) {
            return new Int32(newSchemaPath);
        } else if ("int64".equals(localName)) {
            return new Int64(newSchemaPath);
        } else {
            return null;
        }
    }

    static UnsignedIntegerTypeDefinition createNewUintType(final SchemaPath schemaPath, final QName nodeQName,
            final UnsignedIntegerTypeDefinition type) {
        final QName typeQName = type.getQName();
        final SchemaPath newSchemaPath = createSchemaPath(schemaPath, nodeQName, typeQName);
        final String localName = typeQName.getLocalName();

        if ("uint8".equals(localName)) {
            return new Uint8(newSchemaPath);
        } else if ("uint16".equals(localName)) {
            return new Uint16(newSchemaPath);
        } else if ("uint32".equals(localName)) {
            return new Uint32(newSchemaPath);
        } else if ("uint64".equals(localName)) {
            return new Uint64(newSchemaPath);
        } else {
            return null;
        }
    }

    /**
     * Create new type builder based on old type with new base type.
     *
     * @param newBaseType
     *            new base type builder
     * @param oldExtendedType
     *            old type
     * @param modules
     *            all loaded modules
     * @param module
     *            current module
     * @param line
     *            current line in module
     * @return new type builder based on old type with new base type
     */
    private static TypeDefinitionBuilder extendedTypeWithNewBaseTypeBuilder(final TypeDefinitionBuilder newBaseType,
            final ExtendedType oldExtendedType, final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final ModuleBuilder module, final int line) {
        final TypeConstraints tc = new TypeConstraints(module.getName(), line);
        tc.addFractionDigits(oldExtendedType.getFractionDigits());
        tc.addLengths(oldExtendedType.getLengths());
        tc.addPatterns(oldExtendedType.getPatterns());
        tc.addRanges(oldExtendedType.getRanges());

        final TypeConstraints constraints = findConstraintsFromTypeBuilder(newBaseType, tc, modules, module, null);
        final TypeDefinitionBuilderImpl newType = new TypeDefinitionBuilderImpl(module.getModuleName(), line,
                oldExtendedType.getQName());
        newType.setTypedef(newBaseType);
        newType.setPath(oldExtendedType.getPath());
        newType.setDescription(oldExtendedType.getDescription());
        newType.setReference(oldExtendedType.getReference());
        newType.setStatus(oldExtendedType.getStatus());
        newType.setLengths(constraints.getLength());
        newType.setPatterns(constraints.getPatterns());
        newType.setRanges(constraints.getRange());
        newType.setFractionDigits(constraints.getFractionDigits());
        newType.setUnits(oldExtendedType.getUnits());
        newType.setDefaultValue(oldExtendedType.getDefaultValue());
        newType.setUnknownNodes(oldExtendedType.getUnknownSchemaNodes());
        return newType;
    }

    /**
     * Create new type builder based on old type with new base type.
     *
     * @param newBaseType
     *            new base type
     * @param oldExtendedType
     *            old type
     * @param modules
     *            all loaded modules
     * @param module
     *            current module
     * @param line
     *            current line in module
     * @return new type builder based on old type with new base type
     */
    private static TypeDefinitionBuilder extendedTypeWithNewBaseType(final TypeDefinition<?> newBaseType,
            final ExtendedType oldExtendedType, final ModuleBuilder module, final int line) {
        final TypeConstraints tc = new TypeConstraints(module.getName(), line);

        final TypeConstraints constraints = findConstraintsFromTypeDefinition(newBaseType, tc);
        final TypeDefinitionBuilderImpl newType = new TypeDefinitionBuilderImpl(module.getModuleName(), line,
                oldExtendedType.getQName());
        newType.setType(newBaseType);
        newType.setPath(oldExtendedType.getPath());
        newType.setDescription(oldExtendedType.getDescription());
        newType.setReference(oldExtendedType.getReference());
        newType.setStatus(oldExtendedType.getStatus());
        newType.setLengths(constraints.getLength());
        newType.setPatterns(constraints.getPatterns());
        newType.setRanges(constraints.getRange());
        newType.setFractionDigits(constraints.getFractionDigits());
        newType.setUnits(oldExtendedType.getUnits());
        newType.setDefaultValue(oldExtendedType.getDefaultValue());
        newType.setUnknownNodes(oldExtendedType.getUnknownSchemaNodes());
        return newType;
    }

    /**
     * Pull restrictions from type and add them to constraints.
     *
     * @param typeToResolve
     *            type from which constraints will be read
     * @param constraints
     *            constraints object to which constraints will be added
     * @return constraints contstraints object containing constraints from given
     *         type
     */
    private static TypeConstraints findConstraintsFromTypeDefinition(final TypeDefinition<?> typeToResolve,
            final TypeConstraints constraints) {
        // union type cannot be restricted
        if (typeToResolve instanceof UnionTypeDefinition) {
            return constraints;
        }
        if (typeToResolve instanceof ExtendedType) {
            ExtendedType extType = (ExtendedType) typeToResolve;
            constraints.addFractionDigits(extType.getFractionDigits());
            constraints.addLengths(extType.getLengths());
            constraints.addPatterns(extType.getPatterns());
            constraints.addRanges(extType.getRanges());
            return findConstraintsFromTypeDefinition(extType.getBaseType(), constraints);
        } else {
            mergeConstraints(typeToResolve, constraints);
            return constraints;
        }
    }

    private static TypeConstraints findConstraintsFromTypeBuilder(final TypeAwareBuilder nodeToResolve,
            final TypeConstraints constraints, final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final ModuleBuilder builder, final SchemaContext context) {

        // union and identityref types cannot be restricted
        if (nodeToResolve instanceof UnionTypeBuilder || nodeToResolve instanceof IdentityrefTypeBuilder) {
            return constraints;
        }

        if (nodeToResolve instanceof TypeDefinitionBuilder) {
            TypeDefinitionBuilder typedefToResolve = (TypeDefinitionBuilder) nodeToResolve;
            constraints.addFractionDigits(typedefToResolve.getFractionDigits());
            constraints.addLengths(typedefToResolve.getLengths());
            constraints.addPatterns(typedefToResolve.getPatterns());
            constraints.addRanges(typedefToResolve.getRanges());
        }

        TypeDefinition<?> type = nodeToResolve.getType();
        if (type == null) {
            return findConstraintsFromTypeBuilder(nodeToResolve.getTypedef(), constraints, modules, builder, context);
        } else {
            QName qname = type.getQName();
            if (type instanceof UnknownType) {
                ModuleBuilder dependentModuleBuilder = ParserUtils.findDependentModuleBuilder(modules, builder,
                        qname.getPrefix(), nodeToResolve.getLine());
                if (dependentModuleBuilder == null) {
                    if (context == null) {
                        throw new YangParseException(builder.getName(), nodeToResolve.getLine(),
                                "Failed to resolved type constraints.");
                    }
                    Module dm = ParserUtils.findModuleFromContext(context, builder, qname.getPrefix(),
                            nodeToResolve.getLine());
                    TypeDefinition<?> t = findTypeByName(dm.getTypeDefinitions(), qname.getLocalName());
                    if (t instanceof ExtendedType) {
                        ExtendedType extType = (ExtendedType) t;
                        constraints.addFractionDigits(extType.getFractionDigits());
                        constraints.addLengths(extType.getLengths());
                        constraints.addPatterns(extType.getPatterns());
                        constraints.addRanges(extType.getRanges());
                        return constraints;
                    } else {
                        mergeConstraints(t, constraints);
                        return constraints;
                    }
                } else {
                    TypeDefinitionBuilder tdb = findTypeDefinitionBuilder(nodeToResolve, dependentModuleBuilder,
                            qname.getLocalName(), builder.getName(), nodeToResolve.getLine());
                    return findConstraintsFromTypeBuilder(tdb, constraints, modules, dependentModuleBuilder, context);
                }
            } else if (type instanceof ExtendedType) {
                ExtendedType extType = (ExtendedType) type;
                constraints.addFractionDigits(extType.getFractionDigits());
                constraints.addLengths(extType.getLengths());
                constraints.addPatterns(extType.getPatterns());
                constraints.addRanges(extType.getRanges());

                TypeDefinition<?> base = extType.getBaseType();
                if (base instanceof UnknownType) {
                    ModuleBuilder dependentModule = ParserUtils.findDependentModuleBuilder(modules, builder, base
                            .getQName().getPrefix(), nodeToResolve.getLine());
                    TypeDefinitionBuilder tdb = findTypeDefinitionBuilder(nodeToResolve, dependentModule, base
                            .getQName().getLocalName(), builder.getName(), nodeToResolve.getLine());
                    return findConstraintsFromTypeBuilder(tdb, constraints, modules, dependentModule, context);
                } else {
                    // it has to be base yang type
                    mergeConstraints(type, constraints);
                    return constraints;
                }
            } else {
                // it is base yang type
                mergeConstraints(type, constraints);
                return constraints;
            }
        }
    }

    /**
     * Search for type definition builder by name.
     *
     * @param dirtyNodeSchemaPath
     *            schema path of node which contains unresolved type
     * @param dependentModule
     *            module which should contains referenced type
     * @param typeName
     *            name of type definition
     * @param currentModuleName
     *            name of current module
     * @param line
     *            current line in module
     * @return
     */
    private static TypeDefinitionBuilder findTypeDefinitionBuilder(final TypeAwareBuilder nodeToResolve,
            final ModuleBuilder dependentModule, final String typeName, final String currentModuleName, final int line) {

        TypeDefinitionBuilder result = null;

        Set<TypeDefinitionBuilder> typedefs = dependentModule.getTypeDefinitionBuilders();
        result = findTypedefBuilderByName(typedefs, typeName);
        if (result != null) {
            return result;
        }

        Builder parent = nodeToResolve.getParent();
        while (parent != null) {
            if (parent instanceof DataNodeContainerBuilder) {
                typedefs = ((DataNodeContainerBuilder) parent).getTypeDefinitionBuilders();
            } else if (parent instanceof RpcDefinitionBuilder) {
                typedefs = ((RpcDefinitionBuilder) parent).getTypeDefinitions();
            }
            result = findTypedefBuilderByName(typedefs, typeName);
            if (result == null) {
                parent = parent.getParent();
            } else {
                break;
            }
        }

        if (result == null) {
            throw new YangParseException(currentModuleName, line, "Referenced type '" + typeName + "' not found.");
        }
        return result;
    }

}
