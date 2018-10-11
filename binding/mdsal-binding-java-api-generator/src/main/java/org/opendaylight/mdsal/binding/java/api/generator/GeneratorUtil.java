/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static com.google.common.base.Preconditions.checkArgument;
import static org.opendaylight.mdsal.binding.java.api.generator.Constants.COMMA;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.opendaylight.mdsal.binding.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.model.api.Constant;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.WildcardType;
import org.opendaylight.mdsal.binding.model.util.TypeConstants;
import org.opendaylight.mdsal.binding.model.util.Types;

public final class GeneratorUtil {
    private GeneratorUtil() {
        throw new UnsupportedOperationException();
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
        if (genType instanceof GeneratedTransferObject
                && isConstantInTO(TypeConstants.PATTERN_CONSTANT_NAME, (GeneratedTransferObject) genType)) {
            putTypeIntoImports(genType, Types.typeForClass(java.util.regex.Pattern.class), imports);
        }

        final List<MethodSignature> methods = genType.getMethodDefinitions();
        // METHODS
        if (methods != null) {
            for (final MethodSignature method : methods) {
                final Type methodReturnType = method.getReturnType();
                putTypeIntoImports(genType, methodReturnType, imports);
                for (final MethodSignature.Parameter methodParam : method.getParameters()) {
                    putTypeIntoImports(genType, methodParam.getType(), imports);
                }
                for (final AnnotationType at : method.getAnnotations()) {
                    putTypeIntoImports(genType, at, imports);
                }
            }
        }

        // PROPERTIES
        if (genType instanceof GeneratedTransferObject) {
            final GeneratedTransferObject genTO = (GeneratedTransferObject) genType;
            final List<GeneratedProperty> properties = genTO.getProperties();
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
        checkArgument(parentGenType.getName() != null, "Parent Generated Type name cannot be NULL!");
        checkArgument(parentGenType.getPackageName() != null,
                "Parent Generated Type cannot have Package Name referenced as NULL!");
        checkArgument(type != null, "Type parameter MUST be specified and cannot be NULL!");

        checkArgument(type.getName() != null, "Type name cannot be NULL!");
        checkArgument(type.getPackageName() != null, "Type cannot have Package Name referenced as NULL!");

        final String typeName = type.getName();
        final String typePackageName = type.getPackageName();
        final String parentTypeName = parentGenType.getName();
        if (typeName.equals(parentTypeName) || typePackageName.startsWith("java.lang") || typePackageName.isEmpty()) {
            return;
        }
        if (!imports.containsKey(typeName)) {
            imports.put(typeName, type.getIdentifier());
        }
        if (type instanceof ParameterizedType) {
            final ParameterizedType paramType = (ParameterizedType) type;
            final Type[] params = paramType.getActualTypeArguments();
            if (params != null) {
                for (Type param : params) {
                    putTypeIntoImports(parentGenType, param, imports);
                }
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
        Map<String, String> childImports = new LinkedHashMap<>();
        for (GeneratedType genTypeChild : genType.getEnclosedTypes()) {
            createChildImports(genTypeChild);
            childImports.put(genTypeChild.getName(), genTypeChild.getPackageName());
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

        final JavaTypeName importedType = imports.get(type.getName());
        final StringBuilder builder = new StringBuilder();
        if (type.getIdentifier().equals(importedType)) {
            builder.append(type.getName());
            addActualTypeParameters(builder, type, parentGenType, imports);
            if (builder.toString().equals("Void")) {
                return "void";
            }
        } else {
            if (type.equals(Types.voidType())) {
                return "void";
            }
            builder.append(type.getFullyQualifiedName());
            addActualTypeParameters(builder, type, parentGenType, imports);
        }
        return builder.toString();
    }

    /**
     * Adds actual type parameters from <code>type</code> to <code>builder</code> if <code>type</code> is
     * <code>ParametrizedType</code>.
     *
     * @param builder string builder which contains type name
     * @param type JAVA <code>Type</code> for which is the string with type info generated
     * @param parentGenType generated type which contains <code>type</code>
     * @param imports map of necessary imports for <code>parentGenType</code>
     * @return if <code>type</code> is of the type <code>ParametrizedType</code> <br />
     *         <li>then <code>builder</code> + actual <code>type</code>
     *         parameters</li> <li>else only <code>builder</code></li>
     */
    private static StringBuilder addActualTypeParameters(final StringBuilder builder, final Type type,
            final GeneratedType parentGenType, final Map<String, JavaTypeName> imports) {
        if (type instanceof ParameterizedType) {
            final ParameterizedType pType = (ParameterizedType) type;
            final Type[] pTypes = pType.getActualTypeArguments();
            builder.append('<').append(getParameters(parentGenType, pTypes, imports)).append('>');
        }
        return builder;
    }

    /**
     * Generates the string with all actual type parameters from <code>pTypes</code>.
     *
     * @param parentGenType generated type for which is the JAVA code generated
     * @param paramTypes array of <code>Type</code> instances = actual type parameters
     * @param availableImports map of imports for <code>parentGenType</code>
     * @return string with all actual type parameters from <code>pTypes</code>
     */
    private static String getParameters(final GeneratedType parentGenType, final Type[] paramTypes,
                                        final Map<String, JavaTypeName> availableImports) {

        if (paramTypes == null || paramTypes.length == 0) {
            return "?";
        }
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < paramTypes.length; i++) {
            final Type t = paramTypes[i];

            String separator = COMMA;
            if (i == paramTypes.length - 1) {
                separator = "";
            }

            if (Types.voidType().equals(t)) {
                builder.append("java.lang.Void").append(separator);
            } else {
                if (t instanceof WildcardType) {
                    builder.append("? extends ");
                }
                builder.append(getExplicitType(parentGenType, t, availableImports)).append(separator);
            }
        }
        return builder.toString();
    }

    /**
     * Returns the reference to highest (top parent) Generated Transfer Object.
     *
     * @param childTransportObject is generated transfer object which can be extended by other generated transfer object
     * @return in first case that <code>childTransportObject</code> is not extended then
     *         <code>childTransportObject</code> is returned. In second case the method is recursive called until first
     *         case.
     * @throws IllegalArgumentException if <code>childTransportObject</code> equals <code>null</code>
     */
    static GeneratedTransferObject getTopParentTransportObject(final GeneratedTransferObject childTransportObject) {
        if (childTransportObject == null) {
            throw new IllegalArgumentException("Parameter childTransportObject can't be null.");
        }
        if (childTransportObject.getSuperType() == null) {
            return childTransportObject;
        }

        return getTopParentTransportObject(childTransportObject.getSuperType());
    }

    /**
     * Selects from input list of properties only those which have read only attribute set to true.
     *
     * @param properties list of properties of generated transfer object
     * @return subset of <code>properties</code> which have read only attribute set to true
     */
    static List<GeneratedProperty> resolveReadOnlyPropertiesFromTO(final List<GeneratedProperty> properties) {
        List<GeneratedProperty> readOnlyProperties = new ArrayList<>();
        if (properties != null) {
            for (final GeneratedProperty property : properties) {
                if (property.isReadOnly()) {
                    readOnlyProperties.add(property);
                }
            }
        }
        return readOnlyProperties;
    }

    /**
     * Returns the list of the read only properties of all extending generated transfer object from <code>genTO</code>
     * to highest parent generated transfer object.
     *
     * @param genTO generated transfer object for which is the list of read only properties generated
     * @return list of all read only properties from actual to highest parent generated transfer object. In case when
     *         extension exists the method is recursive called.
     */
    static List<GeneratedProperty> getPropertiesOfAllParents(final GeneratedTransferObject genTO) {
        List<GeneratedProperty> propertiesOfAllParents = new ArrayList<>();
        if (genTO.getSuperType() != null) {
            final List<GeneratedProperty> allPropertiesOfTO = genTO.getSuperType().getProperties();
            List<GeneratedProperty> readOnlyPropertiesOfTO = resolveReadOnlyPropertiesFromTO(allPropertiesOfTO);
            propertiesOfAllParents.addAll(readOnlyPropertiesOfTO);
            propertiesOfAllParents.addAll(getPropertiesOfAllParents(genTO.getSuperType()));
        }
        return propertiesOfAllParents;
    }
}