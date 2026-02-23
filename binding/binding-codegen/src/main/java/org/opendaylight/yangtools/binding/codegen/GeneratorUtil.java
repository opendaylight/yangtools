/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.WildcardType;
import org.opendaylight.yangtools.binding.model.api.YangSourceDefinition.Single;
import org.opendaylight.yangtools.binding.model.ri.TypeConstants;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

public final class GeneratorUtil {
    private static final ConcreteType PATTERN = Types.typeForClass(Pattern.class);

    private GeneratorUtil() {
        // Hidden on purpose
    }

    /**
     * Returns the map of imports. The map maps the type name to the package name. To the map are added packages
     * for <code>genType</code> and for all enclosed types, constants, methods (parameter types, return values),
     * implemented types.
     *
     * @param genType generated type for which the map of the imports is created
     * @return map of the necessary imports
     * @throws IllegalArgumentException if <code>genType</code> equals <code>null</code>
     */
    static Map<String, JavaTypeName> createImports(final GeneratedType genType) {
        if (genType == null) {
            throw new IllegalArgumentException("Generated Type cannot be NULL!");
        }
        final Map<String, JavaTypeName> imports = new LinkedHashMap<>();

        List<GeneratedType> childGeneratedTypes = genType.getEnclosedTypes();
        if (!childGeneratedTypes.isEmpty()) {
            for (GeneratedType genTypeChild : childGeneratedTypes) {
                imports.putAll(createImports(genTypeChild));
            }
        }

        // REGULAR EXPRESSION
        if (genType instanceof GeneratedTransferObject gto
                && isConstantInTO(TypeConstants.PATTERN_CONSTANT_NAME, gto)) {
            putTypeIntoImports(genType, PATTERN, imports);
        }

        // METHODS
        final var methods = genType.getMethodDefinitions();
        if (methods != null) {
            for (var method : methods) {
                putTypeIntoImports(genType, method.getReturnType(), imports);
                for (var methodParam : method.getParameters()) {
                    putTypeIntoImports(genType, methodParam.type(), imports);
                }
                for (var at : method.getAnnotations()) {
                    putTypeIntoImports(genType, at, imports);
                }
            }
        }

        // PROPERTIES
        if (genType instanceof GeneratedTransferObject gto) {
            final List<GeneratedProperty> properties = gto.getProperties();
            if (properties != null) {
                for (GeneratedProperty property : properties) {
                    final Type propertyType = property.getReturnType();
                    putTypeIntoImports(genType, propertyType, imports);
                }
            }
        }
        return imports;
    }

    /**
     * Evaluates if it is necessary to add the package name for <code>type</code> to the map of imports for
     * <code>parentGenType</code>. If it is so the package name is saved to the map <code>imports</code>.
     *
     * @param parentGenType generated type for which is the map of the necessary imports built
     * @param type JAVA <code>Type</code> for which is the necessary of the package import evaluated
     * @param imports map of the imports for <code>parentGenType</code>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if the <code>parentGenType</code> equals
     *             <code>null</code></li>
     *             <li>if the name of <code>parentGenType</code> equals
     *             <code>null</code></li>
     *             <li>if the name of the package of <code>parentGenType</code>
     *             equals <code>null</code></li>
     *             <li>if the <code>type</code> equals <code>null</code></li>
     *             <li>if the name of <code>type</code> equals <code>null</code>
     *             </li>
     *             <li>if the name of the package of <code>type</code> equals
     *             <code>null</code></li>
     *             </ul>
     */
    static void putTypeIntoImports(final GeneratedType parentGenType, final Type type,
                                   final Map<String, JavaTypeName> imports) {
        checkArgument(parentGenType != null, "Parent Generated Type parameter MUST be specified and cannot be "
                + "NULL!");
        checkArgument(parentGenType.simpleName() != null, "Parent Generated Type name cannot be NULL!");
        checkArgument(parentGenType.packageName() != null,
                "Parent Generated Type cannot have Package Name referenced as NULL!");
        checkArgument(type != null, "Type parameter MUST be specified and cannot be NULL!");

        checkArgument(type.simpleName() != null, "Type name cannot be NULL!");
        checkArgument(type.packageName() != null, "Type cannot have Package Name referenced as NULL!");

        final String typeName = type.simpleName();
        final String typePackageName = type.packageName();
        final String parentTypeName = parentGenType.simpleName();
        if (typeName.equals(parentTypeName) || typePackageName.startsWith("java.lang") || typePackageName.isEmpty()) {
            return;
        }
        if (!imports.containsKey(typeName)) {
            imports.put(typeName, type.name());
        }
        if (type instanceof ParameterizedType paramType) {
            for (var param : paramType.getActualTypeArguments()) {
                putTypeIntoImports(parentGenType, param, imports);
            }
        }
    }

    /**
     * Checks if the constant with the name <code>constName</code> is in the list of the constant definition for
     * <code>genTO</code>.
     *
     * @param constName string with the name of constant which is sought
     * @param genTO generated transfer object in which is <code>constName</code> sought
     * @return boolean value
     *         <ul>
     *         <li>true - if <code>constName</code> is in the list of the
     *         constant definition for <code>genTO</code></li>
     *         <li>false - in other cases</li>
     *         </ul>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>constName</code> equals <code>null</code></li>
     *             <li>if <code>genTO</code> equals <code>null</code></li>
     *             </ul>
     */
    static boolean isConstantInTO(final String constName, final GeneratedTransferObject genTO) {
        if (constName == null || genTO == null) {
            throw new IllegalArgumentException();
        }
        List<Constant> consts = genTO.getConstantDefinitions();
        for (Constant cons : consts) {
            if (cons.getName().equals(constName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates the map which maps the type name to package name and contains only package names for enclosed types
     * of <code>genType</code> and recursively their enclosed types.
     *
     * @param genType JAVA <code>Type</code> for which is the map created
     * @return map of the package names for all the enclosed types and recursively their enclosed types
     */
    static Map<String, String> createChildImports(final GeneratedType genType) {
        final var childImports = new LinkedHashMap<String, String>();
        for (var genTypeChild : genType.getEnclosedTypes()) {
            createChildImports(genTypeChild);
            childImports.put(genTypeChild.simpleName(), genTypeChild.packageName());
        }
        return childImports;
    }

    /**
     * Builds the string which contains either the full path to the type (package name with type) or only type name
     * if the package is among <code>imports</code>.
     *
     * @param parentGenType generated type which contains <code>type</code>
     * @param type JAVA <code>Type</code> for which is the string with type info generated
     * @param imports map of necessary imports for <code>parentGenType</code>
     * @return string with type name for <code>type</code> in the full format or in the short format
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if the <code>type</code> equals <code>null</code></li>
     *             <li>if the name of the <code>type</code> equals
     *             <code>null</code></li>
     *             <li>if the name of the package of the <code>type</code>
     *             equals <code>null</code></li>
     *             <li>if the <code>imports</code> equals <code>null</code></li>
     *             </ul>
     */
    static String getExplicitType(final GeneratedType parentGenType, final Type type,
                                  final Map<String, JavaTypeName> imports) {
        checkArgument(type != null, "Type parameter MUST be specified and cannot be NULL!");
        checkArgument(imports != null, "Imports Map cannot be NULL!");

        final var importedType = imports.get(type.simpleName());
        final var sb = new StringBuilder();
        if (type.name().equals(importedType)) {
            sb.append(type.simpleName());
            addActualTypeParameters(sb, type, parentGenType, imports);
            if (sb.toString().equals("Void")) {
                return "void";
            }
        } else {
            if (type.equals(Types.voidType())) {
                return "void";
            }
            sb.append(type.fullyQualifiedName());
            addActualTypeParameters(sb, type, parentGenType, imports);
        }
        return sb.toString();
    }

    /**
     * Adds actual type parameters from <code>type</code> to <code>builder</code> if <code>type</code> is
     * <code>ParametrizedType</code>.
     *
     * @param sb string builder which contains type name
     * @param type JAVA <code>Type</code> for which is the string with type info generated
     * @param parentGenType generated type which contains <code>type</code>
     * @param imports map of necessary imports for <code>parentGenType</code>
     * @return if <code>type</code> is of the type <code>ParametrizedType</code> <br />
     *         <li>then <code>builder</code> + actual <code>type</code>
     *         parameters</li> <li>else only <code>builder</code></li>
     */
    private static StringBuilder addActualTypeParameters(final StringBuilder sb, final Type type,
            final GeneratedType parentGenType, final Map<String, JavaTypeName> imports) {
        if (type instanceof ParameterizedType pType) {
            sb.append('<').append(getParameters(parentGenType, pType.getActualTypeArguments(), imports)).append('>');
        }
        return sb;
    }

    /**
     * Generates the string with all actual type parameters from <code>pTypes</code>.
     *
     * @param parentGenType generated type for which is the JAVA code generated
     * @param paramTypes array of <code>Type</code> instances = actual type parameters
     * @param availableImports map of imports for <code>parentGenType</code>
     * @return string with all actual type parameters from <code>pTypes</code>
     */
    private static String getParameters(final GeneratedType parentGenType, final List<Type> paramTypes,
                                        final Map<String, JavaTypeName> availableImports) {
        if (paramTypes == null || paramTypes.isEmpty()) {
            return "?";
        }
        final var sb = new StringBuilder();
        final var it = paramTypes.iterator();
        while (true) {
            final var type = it.next();

            if (Types.voidType().equals(type)) {
                sb.append("java.lang.Void");
            } else {
                if (type instanceof WildcardType) {
                    sb.append("? extends ");
                }
                sb.append(getExplicitType(parentGenType, type, availableImports));
            }
            if (!it.hasNext()) {
                return sb.toString();
            }
            sb.append(", ");
        }
    }

    /**
     * Selects from input list of properties only those which have read only attribute set to true.
     *
     * @param properties list of properties of generated transfer object
     * @return subset of <code>properties</code> which have read only attribute set to true
     */
    static @NonNull List<GeneratedProperty> resolveReadOnlyPropertiesFromTO(
            final @Nullable List<GeneratedProperty> properties) {
        return properties == null || properties.isEmpty() ? List.of()
            : properties.stream().filter(GeneratedProperty::isReadOnly).collect(Collectors.toUnmodifiableList());
    }

    /**
     * Returns the list of the read only properties of all extending generated transfer object from <code>genTO</code>
     * to highest parent generated transfer object.
     *
     * @param genTO generated transfer object for which is the list of read only properties generated
     * @return list of all read only properties from actual to highest parent generated transfer object. In case when
     *         extension exists the method is recursive called.
     */
    static @NonNull List<GeneratedProperty> getPropertiesOfAllParents(final @NonNull GeneratedTransferObject genTO) {
        final var superType = genTO.getSuperType();
        if (superType == null) {
            return List.of();
        }

        final var propertiesOfAllParents = new ArrayList<GeneratedProperty>();
        propertiesOfAllParents.addAll(resolveReadOnlyPropertiesFromTO(superType.getProperties()));
        propertiesOfAllParents.addAll(getPropertiesOfAllParents(superType));
        return propertiesOfAllParents;
    }

    /**
     * Check if the {@code type} represents non-presence container.
     *
     * @param type {@link GeneratedType} to be checked if represents container without presence statement.
     * @return {@code true} if specified {@code type} is a container without presence statement,
     *     {@code false} otherwise.
     */
    static boolean isNonPresenceContainer(final GeneratedType type) {
        final var definition = type.getYangSourceDefinition();
        return definition.isPresent()
            && definition.orElseThrow() instanceof Single single
            && single.getNode() instanceof ContainerSchemaNode container
            && !container.isPresenceContainer();
    }
}
