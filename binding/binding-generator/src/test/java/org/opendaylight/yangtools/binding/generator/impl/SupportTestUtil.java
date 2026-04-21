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
import static org.junit.jupiter.api.Assertions.fail;
import static org.opendaylight.yangtools.binding.model.ri.Types.typeForClass;

import java.util.List;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
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
            sb.append(type.simpleName()).append('<');
            for (var parameterizedType : parameterizedTypes.getActualTypeArguments()) {
                sb.append(parameterizedType.simpleName()).append(',');
            }
            if (sb.charAt(sb.length() - 1) == ',') {
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.append(">");
        } else {
            sb.append(type.simpleName());
        }
        return sb.toString();
    }

    static void containsInterface(final String interfaceNameSearched, final GeneratedType genType) {
        for (var caseCImplement : genType.getImplements()) {
            if (resolveFullNameOfReturnType(caseCImplement).equals(interfaceNameSearched)) {
                return;
            }
        }
        fail("Generated type " + genType.simpleName() + " doesn't implement interface " + interfaceNameSearched);
    }

    static void assertEntryObject(final GeneratedType type, final JavaTypeName expectedKeyType) {
        final var eo = typeForClass(EntryObject.class);

        for (var iface : type.getImplements()) {
            if (iface instanceof ParameterizedType ptype && eo.equals(ptype.getRawType())) {
                final var args = ptype.getActualTypeArguments();
                assertEquals(2, args.size());
                assertEquals(expectedKeyType, args.getLast().name());
                return;
            }
        }
        fail(type + " does not implement ");
    }
}
