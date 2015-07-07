/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl.stmt.parser.retest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.sal.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class SupportTestUtil {

    public static SchemaContext resolveSchemaContextFromFiles(final URI... yangFiles) throws IOException, SourceException, ReactorException {

        final List<File> inputFiles = new ArrayList<File>();
        for (int i = 0; i < yangFiles.length; ++i) {
            inputFiles.add(new File(yangFiles[i]));
        }

        return RetestUtils.parseYangSources(inputFiles);
    }

    public static void containsMethods(final GeneratedType genType, final NameTypePattern... searchedSignsWhat) {
        final List<MethodSignature> searchedSignsIn = genType.getMethodDefinitions();
        containsMethods(searchedSignsIn, searchedSignsWhat);
    }

    public static void containsMethods(final List<MethodSignature> searchedSignsIn,
            final NameTypePattern... searchedSignsWhat) {
        if (searchedSignsIn == null) {
            throw new IllegalArgumentException("List of method signatures in which should be searched can't be null");
        }
        if (searchedSignsWhat == null) {
            throw new IllegalArgumentException("Array of method signatures which should be searched can't be null");
        }

        for (NameTypePattern searchedSignWhat : searchedSignsWhat) {
            boolean nameMatchFound = false;
            String typeNameFound = "";
            for (MethodSignature searchedSignIn : searchedSignsIn) {
                if (searchedSignWhat.getName().equals(searchedSignIn.getName())) {
                    nameMatchFound = true;
                    typeNameFound = resolveFullNameOfReturnType(searchedSignIn.getReturnType());
                    if (searchedSignWhat.getType().equals(typeNameFound)) {
                        break;
                    }
                }
            }
            assertTrue("Method " + searchedSignWhat.getName() + " wasn't found.", nameMatchFound);
            assertEquals("Return type in method " + searchedSignWhat.getName() + " doesn't match expected type ",
                    searchedSignWhat.getType(), typeNameFound);

        }
    }

    public static void containsAttributes(final GeneratedTransferObject genTO, boolean equal, boolean hash,
            boolean toString, final NameTypePattern... searchedSignsWhat) {
        List<GeneratedProperty> searchedPropertiesIn = genTO.getProperties();
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

    public static void containsAttributes(final List<GeneratedProperty> searchedPropertiesIn, final String listType,
            final NameTypePattern... searchedPropertiesWhat) {

        for (NameTypePattern searchedPropertyWhat : searchedPropertiesWhat) {
            boolean nameMatchFound = false;
            String typeNameFound = "";
            for (GeneratedProperty searchedPropertyIn : searchedPropertiesIn) {
                if (searchedPropertyWhat.getName().equals(searchedPropertyIn.getName())) {
                    nameMatchFound = true;
                    typeNameFound = resolveFullNameOfReturnType(searchedPropertyIn.getReturnType());
                    if (searchedPropertyWhat.getType().equals(typeNameFound)) {
                        break;
                    }
                }
            }
            assertTrue("Property " + searchedPropertyWhat.getName() + " wasn't found in " + listType
                    + " property list.", nameMatchFound);
            assertEquals("The type of property " + searchedPropertyWhat.getName() + " in " + listType
                    + " property list doesn't match expected type.", searchedPropertyWhat.getType(), typeNameFound);

        }
    }

    public static String resolveFullNameOfReturnType(final Type type) {
        final StringBuilder nameBuilder = new StringBuilder();
        if (type instanceof ParameterizedType) {
            nameBuilder.append(type.getName() + "<");
            ParameterizedType parametrizedTypes = (ParameterizedType) type;
            for (Type parametrizedType : parametrizedTypes.getActualTypeArguments()) {
                nameBuilder.append(parametrizedType.getName() + ",");
            }
            if (nameBuilder.charAt(nameBuilder.length() - 1) == ',') {
                nameBuilder.deleteCharAt(nameBuilder.length() - 1);
            }
            nameBuilder.append(">");
        } else {
            nameBuilder.append(type.getName());
        }
        return nameBuilder.toString();
    }

    public static void containsInterface(String interfaceNameSearched, GeneratedType genType) {
        List<Type> caseCImplements = genType.getImplements();
        boolean interfaceFound = false;
        for (Type caseCImplement : caseCImplements) {
            String interfaceName = resolveFullNameOfReturnType(caseCImplement);
            if (interfaceName.equals(interfaceNameSearched)) {
                interfaceFound = true;
                break;
            }
        }
        assertTrue("Generated type " + genType.getName() + " doesn't implement interface " + interfaceNameSearched,
                interfaceFound);
    }

}
