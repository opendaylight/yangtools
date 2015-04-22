/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import static org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils.findBaseIdentity;

import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
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
            final Map<URI, NavigableMap<Date, ModuleBuilder>> modules, final ModuleBuilder module) {
        QName unknownTypeQName = nodeToResolve.getTypeQName();
        final ModuleBuilder dependentModuleBuilder = BuilderUtils.findModule(unknownTypeQName, modules);
        if (dependentModuleBuilder == null) {
            throw new YangParseException(module.getName(), nodeToResolve.getLine(), "Type not found: "
                    + unknownTypeQName);
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
            final Map<URI, NavigableMap<Date, ModuleBuilder>> modules, final ModuleBuilder module) {
        // special handling for identityref types under union
        for (TypeDefinitionBuilder unionType : union.getTypedefs()) {
            if (unionType instanceof IdentityrefTypeBuilder) {
                IdentityrefTypeBuilder idref = (IdentityrefTypeBuilder) unionType;
                IdentitySchemaNodeBuilder identity = findBaseIdentity(module, idref.getBaseString(),
                        idref.getLine());
                if (identity == null) {
                    throw new YangParseException(module.getName(), idref.getLine(), "Failed to find base identity");
                }
                idref.setBaseIdentity(identity);
            }
        }
        for (QName unknownTypeQName : union.getBaseTypeQNames()) {
            final ModuleBuilder dependentModuleBuilder = BuilderUtils.findModule(unknownTypeQName, modules);
            if (dependentModuleBuilder == null) {
                throw new YangParseException(module.getName(), union.getLine(), "Type not found: " + unknownTypeQName);
            }

            final TypeDefinitionBuilder targetTypeBuilder = findTypeDefinitionBuilder(union, dependentModuleBuilder,
                    unknownTypeQName.getLocalName(), module.getName(), union.getLine());
            union.setTypedef(targetTypeBuilder);
        }
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
            final ModuleBuilder dependentModuleBuilder, final Map<URI, NavigableMap<Date, ModuleBuilder>> modules,
            final ModuleBuilder module) {
        final int line = nodeToResolve.getLine();
        final QName unknownTypeQName = nodeToResolve.getTypeQName();
        final TypeDefinitionBuilder targetTypeBuilder = findTypeDefinitionBuilder(nodeToResolve,
                dependentModuleBuilder, unknownTypeQName.getLocalName(), module.getName(), line);

        // validate constraints
        final TypeConstraints constraints = findConstraintsFromTypeBuilder(nodeToResolve,
                new TypeConstraints(module.getName(), nodeToResolve.getLine()), modules, module);
        constraints.validateConstraints();

        return targetTypeBuilder;
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

    private static TypeConstraints findConstraintsFromTypeBuilder(final TypeAwareBuilder nodeToResolve,
            final TypeConstraints constraints, final Map<URI, NavigableMap<Date, ModuleBuilder>> modules,
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
            final QName unknownTypeQName = nodeToResolve.getTypeQName();
            if (unknownTypeQName == null) {
                return constraints;
            }
            final ModuleBuilder dependentModuleBuilder = BuilderUtils.findModule(unknownTypeQName, modules);
            final TypeDefinitionBuilder targetTypeBuilder = findTypeDefinitionBuilder(nodeToResolve,
                    dependentModuleBuilder, unknownTypeQName.getLocalName(), builder.getName(), 0);
            return findConstraintsFromTypeBuilder(targetTypeBuilder, constraints, modules, dependentModuleBuilder);
        } else {
            if (type instanceof ExtendedType) {
                mergeConstraints(type, constraints);
                // it has to be base yang type
                return mergeConstraints(type, constraints);
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
