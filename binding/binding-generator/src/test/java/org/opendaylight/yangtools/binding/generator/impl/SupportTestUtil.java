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

import java.util.List;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.DataContainerArchetype;
import org.opendaylight.yangtools.binding.model.api.GetterMethod;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;

final class SupportTestUtil {
    private SupportTestUtil() {
        // Hidden on purpose
    }

    static void containsMethods(final DataContainerArchetype genType, final NameTypePattern... searchedSignsWhat) {
        containsMethods(genType.getters(), searchedSignsWhat);
    }

    static void containsMethods(final List<GetterMethod> searchedSignsIn,
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
                if (searchedSignWhat.getName().equals(Naming.GETTER_PREFIX + searchedSignIn.suffix())) {
                    nameMatchFound = true;
                    typeNameFound = resolveFullNameOfReturnType(searchedSignIn.returnType());
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

    static void containsInterface(final String interfaceNameSearched, final DataContainerArchetype genType) {
        for (var caseCImplement : genType.partials()) {
            if (resolveFullNameOfReturnType(caseCImplement).equals(interfaceNameSearched)) {
                return;
            }
        }
        fail("Generated type " + genType.simpleName() + " doesn't implement interface " + interfaceNameSearched);
    }
}
