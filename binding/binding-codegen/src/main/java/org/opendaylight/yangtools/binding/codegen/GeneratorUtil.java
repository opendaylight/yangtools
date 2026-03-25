/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Map;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;

public final class GeneratorUtil {
    private GeneratorUtil() {
        // Hidden on purpose
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

    static boolean strictTypeEquals(final Type type1, final Type type2) {
        if (!type1.equals(type2)) {
            return false;
        }
        if (type1 instanceof ParameterizedType param1) {
            return type2 instanceof ParameterizedType param2
                && param1.getActualTypeArguments().equals(param2.getActualTypeArguments());
        }
        return !(type2 instanceof ParameterizedType);
    }
}
