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
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
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
public final class TypeUtils {

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
        final ModuleBuilder dependentModuleBuilder = findDependentModuleBuilder(modules, module,
                unknownTypeQName.getPrefix(), line);

        resolvedType = findUnknownTypeDefinition(nodeToResolve, dependentModuleBuilder, modules, module);
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
                final TypeDefinitionBuilder newType = extendedTypeWithNewBase(null, type, extType, modules, module,
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
            resolvedType = findUnknownTypeDefinition(nodeToResolve, dependentModuleBuilder, modules, module);
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

                    final TypeDefinitionBuilder newType = extendedTypeWithNewBase(targetTypeBuilder, null, extType,
                            modules, builder, union.getLine());

                    union.setTypedef(newType);
                    toRemove.add(extType);
                }
            }
        }
        unionTypes.removeAll(toRemove);
    }

    public static void resolveTypeUnionWithContext(final UnionTypeBuilder union,
            final Map<String, TreeMap<Date, ModuleBuilder>> modules, final ModuleBuilder module,
            final SchemaContext context) {

        final List<TypeDefinition<?>> unionTypes = union.getTypes();
        final List<TypeDefinition<?>> toRemove = new ArrayList<TypeDefinition<?>>();
        for (TypeDefinition<?> unionType : unionTypes) {
            if (unionType instanceof UnknownType) {
                final UnknownType ut = (UnknownType) unionType;
                final QName utQName = ut.getQName();
                final ModuleBuilder dependentModuleBuilder = findDependentModuleBuilder(modules, module,
                        utQName.getPrefix(), union.getLine());

                if (dependentModuleBuilder == null) {
                    Module dependentModule = findModuleFromContext(context, module, utQName.getPrefix(),
                            union.getLine());
                    Set<TypeDefinition<?>> types = dependentModule.getTypeDefinitions();
                    TypeDefinition<?> type = findTypeByName(types, utQName.getLocalName());
                    union.setType(type);
                    toRemove.add(ut);
                } else {
                    final TypeDefinitionBuilder resolvedType = findTypeDefinitionBuilder(union, dependentModuleBuilder,
                            utQName.getLocalName(), module.getName(), union.getLine());
                    union.setTypedef(resolvedType);
                    toRemove.add(ut);
                }

            } else if (unionType instanceof ExtendedType) {
                final ExtendedType extType = (ExtendedType) unionType;
                TypeDefinition<?> extTypeBase = extType.getBaseType();
                if (extTypeBase instanceof UnknownType) {
                    final UnknownType ut = (UnknownType) extTypeBase;
                    final QName utQName = ut.getQName();
                    final ModuleBuilder dependentModuleBuilder = findDependentModuleBuilder(modules, module,
                            utQName.getPrefix(), union.getLine());

                    if (dependentModuleBuilder == null) {
                        final Module dependentModule = findModuleFromContext(context, module, utQName.getPrefix(),
                                union.getLine());
                        Set<TypeDefinition<?>> types = dependentModule.getTypeDefinitions();
                        TypeDefinition<?> type = findTypeByName(types, utQName.getLocalName());
                        final TypeDefinitionBuilder newType = extendedTypeWithNewBase(null, type, extType, modules,
                                module, 0);

                        union.setTypedef(newType);
                        toRemove.add(extType);
                    } else {
                        final TypeDefinitionBuilder targetTypeBuilder = findTypeDefinitionBuilder(union,
                                dependentModuleBuilder, utQName.getLocalName(), module.getName(), union.getLine());

                        final TypeDefinitionBuilder newType = extendedTypeWithNewBase(targetTypeBuilder, null, extType,
                                modules, module, union.getLine());

                        union.setTypedef(newType);
                        toRemove.add(extType);
                    }
                }
            }
        }
        unionTypes.removeAll(toRemove);
    }

    /**
     * Find type definition of type of unresolved node.
     * 
     * @param nodeToResolve
     *            node with unresolved type
     * @param dependentModuleBuilder
     *            module in which type definition is present
     * @param modules
     *            all loaded modules
     * @param module
     *            current module
     * @return TypeDefinitionBuilder of node type
     */
    private static TypeDefinitionBuilder findUnknownTypeDefinition(final TypeAwareBuilder nodeToResolve,
            final ModuleBuilder dependentModuleBuilder, final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final ModuleBuilder module) {
        final int line = nodeToResolve.getLine();
        final TypeDefinition<?> nodeToResolveType = nodeToResolve.getType();
        final QName unknownTypeQName = nodeToResolveType.getBaseType().getQName();
        TypeDefinitionBuilder resolvedType = null;
        final TypeDefinitionBuilder targetTypeBuilder = findTypeDefinitionBuilder(nodeToResolve,
                dependentModuleBuilder, unknownTypeQName.getLocalName(), module.getName(), line);

        if (nodeToResolveType instanceof ExtendedType) {
            final ExtendedType extType = (ExtendedType) nodeToResolveType;
            final TypeDefinitionBuilder newType = extendedTypeWithNewBase(targetTypeBuilder, null, extType, modules,
                    module, nodeToResolve.getLine());
            resolvedType = newType;
        } else {
            resolvedType = targetTypeBuilder;
        }

        // validate constraints
        final TypeConstraints constraints = findConstraintsFromTypeBuilder(nodeToResolve,
                new TypeConstraints(module.getName(), nodeToResolve.getLine()), modules, module, null);
        constraints.validateConstraints();

        return resolvedType;
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
     *            type from which constraints will be read
     * @param constraints
     *            constraints object to which constraints will be added
     */
    private static TypeConstraints mergeConstraints(final TypeDefinition<?> type, final TypeConstraints constraints) {
        if (type instanceof DecimalTypeDefinition) {
            constraints.addRanges(((DecimalTypeDefinition) type).getRangeConstraints());
            constraints.addFractionDigits(((DecimalTypeDefinition) type).getFractionDigits());
        } else if (type instanceof IntegerTypeDefinition) {
            constraints.addRanges(((IntegerTypeDefinition) type).getRangeConstraints());
        } else if (type instanceof StringTypeDefinition) {
            constraints.addPatterns(((StringTypeDefinition) type).getPatternConstraints());
            constraints.addLengths(((StringTypeDefinition) type).getLengthConstraints());
        } else if (type instanceof BinaryTypeDefinition) {
            constraints.addLengths(((BinaryTypeDefinition) type).getLengthConstraints());
        } else if (type instanceof ExtendedType) {
            constraints.addFractionDigits(((ExtendedType) type).getFractionDigits());
            constraints.addLengths(((ExtendedType) type).getLengthConstraints());
            constraints.addPatterns(((ExtendedType) type).getPatternConstraints());
            constraints.addRanges(((ExtendedType) type).getRangeConstraints());
        }
        return constraints;
    }

    /**
     * Create new type builder based on old type with new base type. Note: only
     * one of newBaseTypeBuilder or newBaseType can be specified.
     * 
     * @param newBaseTypeBuilder
     *            new base type builder or null
     * @param newBaseType
     *            new base type or null
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
    private static TypeDefinitionBuilder extendedTypeWithNewBase(final TypeDefinitionBuilder newBaseTypeBuilder,
            final TypeDefinition<?> newBaseType, final ExtendedType oldExtendedType,
            final Map<String, TreeMap<Date, ModuleBuilder>> modules, final ModuleBuilder module, final int line) {
        if ((newBaseTypeBuilder == null && newBaseType == null) || (newBaseTypeBuilder != null && newBaseType != null)) {
            throw new YangParseException(module.getName(), line,
                    "only one of newBaseTypeBuilder or newBaseType can be specified");
        }

        final TypeDefinitionBuilderImpl newType = new TypeDefinitionBuilderImpl(module.getModuleName(), line,
                oldExtendedType.getQName());
        final TypeConstraints tc = new TypeConstraints(module.getName(), line);
        TypeConstraints constraints = null;
        if (newBaseType == null) {
            tc.addFractionDigits(oldExtendedType.getFractionDigits());
            tc.addLengths(oldExtendedType.getLengthConstraints());
            tc.addPatterns(oldExtendedType.getPatternConstraints());
            tc.addRanges(oldExtendedType.getRangeConstraints());
            constraints = findConstraintsFromTypeBuilder(newBaseTypeBuilder, tc, modules, module, null);
            newType.setTypedef(newBaseTypeBuilder);
        } else {
            constraints = findConstraintsFromTypeDefinition(newBaseType, tc);
            newType.setType(newBaseType);
        }

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
            constraints.addLengths(extType.getLengthConstraints());
            constraints.addPatterns(extType.getPatternConstraints());
            constraints.addRanges(extType.getRangeConstraints());
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
                    return mergeConstraints(t, constraints);

                } else {
                    TypeDefinitionBuilder tdb = findTypeDefinitionBuilder(nodeToResolve, dependentModuleBuilder,
                            qname.getLocalName(), builder.getName(), nodeToResolve.getLine());
                    return findConstraintsFromTypeBuilder(tdb, constraints, modules, dependentModuleBuilder, context);
                }
            } else if (type instanceof ExtendedType) {
                mergeConstraints(type, constraints);

                TypeDefinition<?> base = ((ExtendedType) type).getBaseType();
                if (base instanceof UnknownType) {
                    ModuleBuilder dependentModule = ParserUtils.findDependentModuleBuilder(modules, builder, base
                            .getQName().getPrefix(), nodeToResolve.getLine());
                    TypeDefinitionBuilder tdb = findTypeDefinitionBuilder(nodeToResolve, dependentModule, base
                            .getQName().getLocalName(), builder.getName(), nodeToResolve.getLine());
                    return findConstraintsFromTypeBuilder(tdb, constraints, modules, dependentModule, context);
                } else {
                    // it has to be base yang type
                    return mergeConstraints(type, constraints);
                }
            } else {
                // it is base yang type
                return mergeConstraints(type, constraints);
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
