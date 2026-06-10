/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.LegacyArchetype;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;

final class SupportTestUtil {
    private SupportTestUtil() {
        // Hidden on purpose
    }

    static void containsMethods(final LegacyArchetype genType, final NameTypePattern... searchedSignsWhat) {
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

    private static String resolveFullNameOfReturnType(final Type type) {
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

    static void containsInterface(final String interfaceNameSearched, final LegacyArchetype genType) {
        for (var caseCImplement : genType.getImplements()) {
            if (resolveFullNameOfReturnType(caseCImplement).equals(interfaceNameSearched)) {
                return;
            }
        }
        fail("Generated type " + genType.simpleName() + " doesn't implement interface " + interfaceNameSearched);
    }

    @NonNullByDefault
    static void assertEntryObject(final LegacyArchetype type, final JavaTypeName expectedKeyType) {
        final var key = BindingTypes.extractEntryObjectKey(type);
        assertNotNull(key);
        assertEquals(expectedKeyType, key.name());
    }
}
