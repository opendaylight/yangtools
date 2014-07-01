/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.model.util.UnknownType;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeAwareBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.util.TypeConstraints;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

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
        final TypeDefinition<?> nodeToResolveType = nodeToResolve.getType();
        final QName unknownTypeQName = nodeToResolveType.getBaseType().getQName();
        final ModuleBuilder dependentModuleBuilder = module.getImportedModule(unknownTypeQName.getPrefix());
        if (dependentModuleBuilder == null) {
            throw new YangParseException(module.getName(), nodeToResolve.getLine(), "No module found for import "
                    + unknownTypeQName.getPrefix());
        }
        TypeDefinitionBuilder resolvedType = findUnknownTypeDefinition(nodeToResolve, dependentModuleBuilder, modules,
                module);
        nodeToResolve.setTypedef(resolvedType);
    }

    /**
     * Resolve union type which contains one or more unresolved types.
     *
     * @param union
     *            union type builder to resolve
     * @param modules
     *            all loaded modules
     * @param module
     *            current module
     */
    public static void resolveTypeUnion(final UnionTypeBuilder union,
            final Map<String, TreeMap<Date, ModuleBuilder>> modules, final ModuleBuilder module) {
        final List<TypeDefinition<?>> unionTypes = union.getTypes();
        final List<TypeDefinition<?>> toRemove = new ArrayList<>();
        for (TypeDefinition<?> unionType : unionTypes) {
            if (unionType instanceof UnknownType) {
                resolveUnionUnknownType(union, (UnknownType) unionType, modules, module);
                toRemove.add(unionType);
            } else if (unionType instanceof ExtendedType && unionType.getBaseType() instanceof UnknownType) {
                resolveUnionUnknownType(union, (ExtendedType) unionType, modules, module);
                toRemove.add(unionType);
            }
        }
        unionTypes.removeAll(toRemove);
    }

    private static void resolveUnionUnknownType(final UnionTypeBuilder union, final UnknownType ut,
            final Map<String, TreeMap<Date, ModuleBuilder>> modules, final ModuleBuilder module) {
        final QName utQName = ut.getQName();
        final ModuleBuilder dependentModuleBuilder = module.getImportedModule(utQName.getPrefix());
        final TypeDefinitionBuilder resolvedType = findTypeDefinitionBuilder(union, dependentModuleBuilder,
                utQName.getLocalName(), module.getName(), union.getLine());
        union.setTypedef(resolvedType);
    }

    private static void resolveUnionUnknownType(final UnionTypeBuilder union, final ExtendedType extType,
            final Map<String, TreeMap<Date, ModuleBuilder>> modules, final ModuleBuilder module) {
        final int line = union.getLine();
        final TypeDefinition<?> extTypeBase = extType.getBaseType();
        final UnknownType ut = (UnknownType) extTypeBase;
        final QName utQName = ut.getQName();
        final ModuleBuilder dependentModuleBuilder = module.getImportedModule(utQName.getPrefix());
        final TypeDefinitionBuilder targetTypeBuilder = findTypeDefinitionBuilder(union, dependentModuleBuilder,
                utQName.getLocalName(), module.getName(), line);
        final TypeDefinitionBuilder newType = extendedTypeWithNewBase(targetTypeBuilder, null, extType, modules,
                module, line);
        union.setTypedef(newType);
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
        final TypeDefinitionBuilder targetTypeBuilder = findTypeDefinitionBuilder(nodeToResolve,
                dependentModuleBuilder, unknownTypeQName.getLocalName(), module.getName(), line);

        TypeDefinitionBuilder resolvedType;
        if (nodeToResolveType instanceof ExtendedType) {
            final ExtendedType extType = (ExtendedType) nodeToResolveType;
            resolvedType = extendedTypeWithNewBase(targetTypeBuilder, null, extType, modules, module,
                    nodeToResolve.getLine());
        } else {
            resolvedType = targetTypeBuilder;
        }

        // validate constraints
        final TypeConstraints constraints = findConstraintsFromTypeBuilder(nodeToResolve,
                new TypeConstraints(module.getName(), nodeToResolve.getLine()), modules, module);
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
        } else if (type instanceof UnsignedIntegerTypeDefinition) {
            constraints.addRanges(((UnsignedIntegerTypeDefinition) type).getRangeConstraints());
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
                oldExtendedType.getQName(), oldExtendedType.getPath());
        final TypeConstraints tc = new TypeConstraints(module.getName(), line);
        TypeConstraints constraints;
        if (newBaseType == null) {
            tc.addFractionDigits(oldExtendedType.getFractionDigits());
            tc.addLengths(oldExtendedType.getLengthConstraints());
            tc.addPatterns(oldExtendedType.getPatternConstraints());
            tc.addRanges(oldExtendedType.getRangeConstraints());
            constraints = findConstraintsFromTypeBuilder(newBaseTypeBuilder, tc, modules, module);
            newType.setTypedef(newBaseTypeBuilder);
        } else {
            constraints = findConstraintsFromTypeDefinition(newBaseType, tc);
            newType.setType(newBaseType);
        }

        newType.setDescription(oldExtendedType.getDescription());
        newType.setReference(oldExtendedType.getReference());
        newType.setStatus(oldExtendedType.getStatus());
        newType.setLengths(constraints.getLength());
        newType.setPatterns(constraints.getPatterns());
        newType.setRanges(constraints.getRange());
        newType.setFractionDigits(constraints.getFractionDigits());
        newType.setUnits(oldExtendedType.getUnits());
        newType.setDefaultValue(oldExtendedType.getDefaultValue());
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
            final ModuleBuilder builder) {

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
            return findConstraintsFromTypeBuilder(nodeToResolve.getTypedef(), constraints, modules, builder);
        } else {
            QName qname = type.getQName();
            if (type instanceof UnknownType) {
                ModuleBuilder dependentModuleBuilder = builder.getImportedModule(qname.getPrefix());
                TypeDefinitionBuilder tdb = findTypeDefinitionBuilder(nodeToResolve, dependentModuleBuilder,
                        qname.getLocalName(), builder.getName(), nodeToResolve.getLine());
                return findConstraintsFromTypeBuilder(tdb, constraints, modules, dependentModuleBuilder);
            } else if (type instanceof ExtendedType) {
                mergeConstraints(type, constraints);

                TypeDefinition<?> base = ((ExtendedType) type).getBaseType();
                if (base instanceof UnknownType) {
                    ModuleBuilder dependentModule = builder.getImportedModule(base.getQName().getPrefix());
                    TypeDefinitionBuilder tdb = findTypeDefinitionBuilder(nodeToResolve, dependentModule, base
                            .getQName().getLocalName(), builder.getName(), nodeToResolve.getLine());
                    return findConstraintsFromTypeBuilder(tdb, constraints, modules, dependentModule);
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
     * @param nodeToResolve
     *            node which contains unresolved type
     * @param dependentModule
     *            module which should contains referenced type
     * @param typeName
     *            name of type definition
     * @param currentModuleName
     *            name of current module
     * @param line
     *            current line in module
     * @return typeDefinitionBuilder
     */
    private static TypeDefinitionBuilder findTypeDefinitionBuilder(final TypeAwareBuilder nodeToResolve,
            final ModuleBuilder dependentModule, final String typeName, final String currentModuleName, final int line) {
        Set<TypeDefinitionBuilder> typedefs = dependentModule.getTypeDefinitionBuilders();
        TypeDefinitionBuilder result = findTypedefBuilderByName(typedefs, typeName);
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
