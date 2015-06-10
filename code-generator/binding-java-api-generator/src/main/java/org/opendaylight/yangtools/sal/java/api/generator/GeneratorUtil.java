/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator;

import static com.google.common.base.Preconditions.checkArgument;
import static org.opendaylight.yangtools.sal.java.api.generator.Constants.COMMA;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.opendaylight.yangtools.binding.generator.util.TypeConstants;
import org.opendaylight.yangtools.binding.generator.util.Types;
import org.opendaylight.yangtools.sal.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.sal.binding.model.api.Constant;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.sal.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.WildcardType;

public final class GeneratorUtil {
    private static final Comparator<GeneratedProperty> EQUALS_COMPLEXITY_COMPARATOR = new Comparator<GeneratedProperty>() {
        @Override
        public int compare(final GeneratedProperty o1, final GeneratedProperty o2) {
            final EqualsComplexity ec1 = EqualsComplexity.forProperty(o1);
            final EqualsComplexity ec2 = EqualsComplexity.forProperty(o2);

            return ec1.compareTo(ec2);
        }
    };

    /**
     * It doesn't have the sense to create the instances of this class.
     */
    private GeneratorUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the map of imports. The map maps the type name to the package
     * name. To the map are added packages for <code>genType</code> and for all
     * enclosed types, constants, methods (parameter types, return values),
     * implemented types.
     *
     * @param genType
     *            generated type for which the map of the imports is created
     * @return map of the necessary imports
     * @throws IllegalArgumentException
     *             if <code>genType</code> equals <code>null</code>
     */
    public static Map<String, String> createImports(final GeneratedType genType) {
        if (genType == null) {
            throw new IllegalArgumentException("Generated Type cannot be NULL!");
        }
        final Map<String, String> imports = new LinkedHashMap<>();

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
            putTypeIntoImports(genType, Types.typeForClass(java.util.Arrays.class), imports);
            putTypeIntoImports(genType, Types.typeForClass(java.util.ArrayList.class), imports);
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
     * Evaluates if it is necessary to add the package name for
     * <code>type</code> to the map of imports for <code>parentGenType</code>.
     * If it is so the package name is saved to the map <code>imports</code>.
     *
     * @param parentGenType
     *            generated type for which is the map of the necessary imports
     *            built
     * @param type
     *            JAVA <code>Type</code> for which is the necessary of the
     *            package import evaluated
     * @param imports
     *            map of the imports for <code>parentGenType</code>
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
    public static void putTypeIntoImports(final GeneratedType parentGenType, final Type type,
            final Map<String, String> imports) {
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
            imports.put(typeName, typePackageName);
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
     * Checks if the constant with the name <code>constName</code> is in the
     * list of the constant definition for <code>genTO</code>.
     *
     * @param constName
     *            string with the name of constant which is sought
     * @param genTO
     *            generated transfer object in which is <code>constName</code>
     *            sought
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
    public static boolean isConstantInTO(final String constName, final GeneratedTransferObject genTO) {
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
     * Creates the map which maps the type name to package name and contains
     * only package names for enclosed types of <code>genType</code> and
     * recursivelly their enclosed types.
     *
     * @param genType
     *            JAVA <code>Type</code> for which is the map created
     * @return map of the package names for all the enclosed types and
     *         recursivelly their enclosed types
     */
    public static Map<String, String> createChildImports(final GeneratedType genType) {
        Map<String, String> childImports = new LinkedHashMap<>();
        List<GeneratedType> childGeneratedTypes = genType.getEnclosedTypes();
        if (!childGeneratedTypes.isEmpty()) {
            for (GeneratedType genTypeChild : childGeneratedTypes) {
                createChildImports(genTypeChild);
                childImports.put(genTypeChild.getName(), genTypeChild.getPackageName());
            }
        }
        return childImports;
    }

    /**
     * Builds the string which contains either the full path to the type
     * (package name with type) or only type name if the package is among
     * <code>imports</code>.
     *
     * @param parentGenType
     *            generated type which contains <code>type</code>
     * @param type
     *            JAVA <code>Type</code> for which is the string with type info
     *            generated
     * @param imports
     *            map of necessary imports for <code>parentGenType</code>
     * @return string with type name for <code>type</code> in the full format or
     *         in the short format
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
    public static String getExplicitType(final GeneratedType parentGenType, final Type type,
            final Map<String, String> imports) {

        checkArgument(type != null, "Type parameter MUST be specified and cannot be NULL!");
        checkArgument(type.getName() != null, "Type name cannot be NULL!");
        checkArgument(type.getPackageName() != null, "Type cannot have Package Name referenced as NULL!");
        checkArgument(imports != null, "Imports Map cannot be NULL!");

        final String typePackageName = type.getPackageName();
        final String typeName = type.getName();
        final String importedPackageName = imports.get(typeName);
        final StringBuilder builder;
        if (typePackageName.equals(importedPackageName)) {
            builder = new StringBuilder(type.getName());
            addActualTypeParameters(builder, type, parentGenType, imports);
            if (builder.toString().equals("Void")) {
                return "void";
            }
        } else {
            builder = new StringBuilder();
                if (!typePackageName.isEmpty()) {
                    builder.append(typePackageName + Constants.DOT + type.getName());
                } else {
                    builder.append(type.getName());
                }
            if (type.equals(Types.voidType())) {
                return "void";
            }
            addActualTypeParameters(builder, type, parentGenType, imports);
        }
        return builder.toString();

    }

    /**
     * Adds actual type parameters from <code>type</code> to
     * <code>builder</code> if <code>type</code> is
     * <code>ParametrizedType</code>.
     *
     * @param builder
     *            string builder which contains type name
     * @param type
     *            JAVA <code>Type</code> for which is the string with type info
     *            generated
     * @param parentGenType
     *            generated type which contains <code>type</code>
     * @param imports
     *            map of necessary imports for <code>parentGenType</code>
     * @return if <code>type</code> is of the type <code>ParametrizedType</code> <br />
     *         <li>then <code>builder</code> + actual <code>type</code>
     *         parameters</li> <li>else only <code>builder</code></li>
     */
    private static StringBuilder addActualTypeParameters(final StringBuilder builder, final Type type,
            final GeneratedType parentGenType, final Map<String, String> imports) {
        if (type instanceof ParameterizedType) {
            final ParameterizedType pType = (ParameterizedType) type;
            final Type[] pTypes = pType.getActualTypeArguments();
            builder.append("<");
            builder.append(getParameters(parentGenType, pTypes, imports));
            builder.append(">");
        }
        return builder;
    }

    /**
     * Generates the string with all actual type parameters from
     * <code>pTypes</code>
     *
     * @param parentGenType
     *            generated type for which is the JAVA code generated
     * @param pTypes
     *            array of <code>Type</code> instances = actual type parameters
     * @param availableImports
     *            map of imports for <code>parentGenType</code>
     * @return string with all actual type parameters from <code>pTypes</code>
     */
    private static String getParameters(final GeneratedType parentGenType, final Type[] pTypes,
            final Map<String, String> availableImports) {
        if (pTypes == null || pTypes.length == 0) {
            return "?";
        }
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < pTypes.length; i++) {
            final Type t = pTypes[i];

            String separator = COMMA;
            if (i == (pTypes.length - 1)) {
                separator = "";
            }

            String wildcardParam = "";
            if (t.equals(Types.voidType())) {
                builder.append("java.lang.Void" + separator);
                continue;
            } else {

                if (t instanceof WildcardType) {
                    wildcardParam = "? extends ";
                }

                builder.append(wildcardParam + getExplicitType(parentGenType, t, availableImports) + separator);
            }
        }
        return builder.toString();
    }

    /**
     * Returns the reference to highest (top parent) Generated Transfer Object.
     *
     * @param childTransportObject
     *            is generated transfer object which can be extended by other
     *            generated transfer object
     * @return in first case that <code>childTransportObject</code> isn't
     *         extended then <code>childTransportObject</code> is returned. In
     *         second case the method is recursive called until first case.
     * @throws IllegalArgumentException
     *             if <code>childTransportObject</code> equals <code>null</code>
     */
    public static GeneratedTransferObject getTopParrentTransportObject(final GeneratedTransferObject childTransportObject) {
        if (childTransportObject == null) {
            throw new IllegalArgumentException("Parameter childTransportObject can't be null.");
        }
        if (childTransportObject.getSuperType() == null) {
            return childTransportObject;
        } else {
            return getTopParrentTransportObject(childTransportObject.getSuperType());
        }
    }

    /**
     * Selects from input list of properties only those which have read only
     * attribute set to true.
     *
     * @param properties
     *            list of properties of generated transfer object
     * @return subset of <code>properties</code> which have read only attribute
     *         set to true
     */
    public static List<GeneratedProperty> resolveReadOnlyPropertiesFromTO(final List<GeneratedProperty> properties) {
        List<GeneratedProperty> readOnlyProperties = new ArrayList<GeneratedProperty>();
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
     * Returns the list of the read only properties of all extending generated
     * transfer object from <code>genTO</code> to highest parent generated
     * transfer object
     *
     * @param genTO
     *            generated transfer object for which is the list of read only
     *            properties generated
     * @return list of all read only properties from actual to highest parent
     *         generated transfer object. In case when extension exists the
     *         method is recursive called.
     */
    public static List<GeneratedProperty> getPropertiesOfAllParents(final GeneratedTransferObject genTO) {
        List<GeneratedProperty> propertiesOfAllParents = new ArrayList<GeneratedProperty>();
        if (genTO.getSuperType() != null) {
            final List<GeneratedProperty> allPropertiesOfTO = genTO.getSuperType().getProperties();
            List<GeneratedProperty> readOnlyPropertiesOfTO = resolveReadOnlyPropertiesFromTO(allPropertiesOfTO);
            propertiesOfAllParents.addAll(readOnlyPropertiesOfTO);
            propertiesOfAllParents.addAll(getPropertiesOfAllParents(genTO.getSuperType()));
        }
        return propertiesOfAllParents;
    }

    /**
     * Order a collection of properties according the complexity of their {@link Object#equals(Object)}
     * complexity.
     *
     * @param properties Properties which need to be ordered.
     * @return Reordered properties. Note that the order is not guaranteed to be consistent in face
     *         of duplicates with same complexity.
     */
    static Collection<GeneratedProperty> propertiesInEqualityOrder(final Collection<GeneratedProperty> properties) {
        final List<GeneratedProperty> ret = new ArrayList<>(properties);
        Collections.sort(properties, EQUALS_COMPLEXITY_COMPARATOR);
        return ret;
    }
}
