/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;

final class SupportTestUtil {
    private SupportTestUtil() {
        // Hidden on purpose
    }

    static void containsMethods(final GeneratedType genType, final NameTypePattern... searchedSignsWhat) {
        containsMethods(genType.getMethodDefinitions(), searchedSignsWhat);
    }

    static void containsMethods(final List<MethodSignature> searchedSignsIn,
            final NameTypePattern... searchedSignsWhat) {
        if (searchedSignsIn == null) {
            throw new IllegalArgumentException("List of method signatures in which should be searched can't be null");
        }
        if (searchedSignsWhat == null) {
            throw new IllegalArgumentException("Array of method signatures which should be searched can't be null");
        }

        for (var searchedSignWhat : searchedSignsWhat) {
            boolean nameMatchFound = false;
            String typeNameFound = "";
            for (var searchedSignIn : searchedSignsIn) {
                if (searchedSignWhat.getName().equals(searchedSignIn.getName())) {
                    nameMatchFound = true;
                    typeNameFound = resolveFullNameOfReturnType(searchedSignIn.getReturnType());
                    if (searchedSignWhat.getType().equals(typeNameFound)) {
                        break;
                    }
                }
            }
            assertTrue(nameMatchFound, "Method " + searchedSignWhat.getName() + " wasn't found.");
            assertEquals(searchedSignWhat.getType(), typeNameFound,
                "Return type in method " + searchedSignWhat.getName() + " doesn't match expected type ");

        }
    }

    static void containsAttributes(final GeneratedTransferObject genTO, final boolean equal, final boolean hash,
            final boolean toString, final NameTypePattern... searchedSignsWhat) {
        var searchedPropertiesIn = genTO.getProperties();
        containsAttributes(searchedPropertiesIn, "", searchedSignsWhat);
        if (equal) {
            searchedPropertiesIn = genTO.getEqualsIdentifiers();
            containsAttributes(searchedPropertiesIn, "equal", searchedSignsWhat);
        }
        if (hash) {
            searchedPropertiesIn = genTO.getHashCodeIdentifiers();
            containsAttributes(searchedPropertiesIn, "hash", searchedSignsWhat);
        }
        if (toString) {
            searchedPropertiesIn = genTO.getToStringIdentifiers();
            containsAttributes(searchedPropertiesIn, "toString", searchedSignsWhat);
        }
    }

    static void containsAttributes(final List<GeneratedProperty> searchedPropertiesIn, final String listType,
            final NameTypePattern... searchedPropertiesWhat) {

        for (var searchedPropertyWhat : searchedPropertiesWhat) {
            boolean nameMatchFound = false;
            String typeNameFound = "";
            for (var searchedPropertyIn : searchedPropertiesIn) {
                if (searchedPropertyWhat.getName().equals(searchedPropertyIn.getName())) {
                    nameMatchFound = true;
                    typeNameFound = resolveFullNameOfReturnType(searchedPropertyIn.getReturnType());
                    if (searchedPropertyWhat.getType().equals(typeNameFound)) {
                        break;
                    }
                }
            }
            assertTrue(nameMatchFound,
                "Property " + searchedPropertyWhat.getName() + " wasn't found in " + listType + " property list.");
            assertEquals(searchedPropertyWhat.getType(), typeNameFound,
                "The type of property " + searchedPropertyWhat.getName() + " in " + listType
                + " property list doesn't match expected type.");
        }
    }

    static String resolveFullNameOfReturnType(final Type type) {
        final var sb = new StringBuilder();
        if (type instanceof ParameterizedType parameterizedTypes) {
            sb.append(type.getName()).append('<');
            for (var parameterizedType : parameterizedTypes.getActualTypeArguments()) {
                sb.append(parameterizedType.getName()).append(',');
            }
            if (sb.charAt(sb.length() - 1) == ',') {
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.append(">");
        } else {
            sb.append(type.getName());
        }
        return sb.toString();
    }

    static void containsInterface(final String interfaceNameSearched, final GeneratedType genType) {
        final var caseCImplements = genType.getImplements();
        boolean interfaceFound = false;
        for (var caseCImplement : caseCImplements) {
            if (resolveFullNameOfReturnType(caseCImplement).equals(interfaceNameSearched)) {
                interfaceFound = true;
                break;
            }
        }
        assertTrue(interfaceFound,
            "Generated type " + genType.getName() + " doesn't implement interface " + interfaceNameSearched);
    }
}
