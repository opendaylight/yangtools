/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.model.api.Enumeration;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangContextParser;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

public class GenEnumResolvingTest {

    private SchemaContext resolveSchemaContextFromFiles(final URI... yangFiles) throws IOException {
        final YangContextParser parser = new YangParserImpl();

        final List<File> inputFiles = new ArrayList<File>();
        for (int i = 0; i < yangFiles.length; ++i) {
            inputFiles.add(new File(yangFiles[i]));
        }

        return parser.parseFiles(inputFiles);
    }

    @Test
    public void testLeafEnumResolving() throws URISyntaxException, IOException {
        final URI ietfInterfacesPath = getClass().getResource("/enum-test-models/ietf-interfaces@2012-11-15.yang")
                .toURI();
        final URI ifTypePath = getClass().getResource("/enum-test-models/iana-if-type@2012-06-05.yang").toURI();
        final URI yangTypesPath = getClass().getResource("/enum-test-models/ietf-yang-types@2010-09-24.yang").toURI();

        final SchemaContext context = resolveSchemaContextFromFiles(ietfInterfacesPath, ifTypePath, yangTypesPath);
        assertTrue(context != null);

        final BindingGenerator bindingGen = new BindingGeneratorImpl();
        final List<Type> genTypes = bindingGen.generateTypes(context);
        assertTrue(genTypes != null);

        assertEquals("Expected count of all Generated Types", 20, genTypes.size());

        GeneratedType genInterface = null;
        for (final Type type : genTypes) {
            if (type instanceof GeneratedType) {
                if (type.getName().equals("Interface")) {
                    genInterface = (GeneratedType) type;
                }
            }
        }
        assertNotNull("Generated Type Interface is not present in list of Generated Types", genInterface);

        Enumeration linkUpDownTrapEnable = null;
        Enumeration operStatus = null;
        final List<Enumeration> enums = genInterface.getEnumerations();
        assertNotNull("Generated Type Interface cannot contain NULL reference to Enumeration types!", enums);
        assertEquals("Generated Type Interface MUST contain 2 Enumeration Types", 2, enums.size());
        for (final Enumeration e : enums) {
            if (e.getName().equals("LinkUpDownTrapEnable")) {
                linkUpDownTrapEnable = e;
            } else if (e.getName().equals("OperStatus")) {
                operStatus = e;
            }
        }

        assertNotNull("Expected Enum LinkUpDownTrapEnable, but was NULL!", linkUpDownTrapEnable);
        assertNotNull("Expected Enum OperStatus, but was NULL!", operStatus);

        assertNotNull("Enum LinkUpDownTrapEnable MUST contain Values definition not NULL reference!",
                linkUpDownTrapEnable.getValues());
        assertNotNull("Enum OperStatus MUST contain Values definition not NULL reference!", operStatus.getValues());
        assertEquals("Enum LinkUpDownTrapEnable MUST contain 2 values!", 2, linkUpDownTrapEnable.getValues().size());
        assertEquals("Enum OperStatus MUST contain 7 values!", 7, operStatus.getValues().size());

        final List<MethodSignature> methods = genInterface.getMethodDefinitions();

        assertNotNull("Generated Interface cannot contain NULL reference for Method Signature Definitions!", methods);

        assertEquals("Expected count of method signature definitions is 15", 15, methods.size());
        Enumeration ianaIfType = null;
        for (final MethodSignature method : methods) {
            if (method.getName().equals("getType")) {
                if (method.getReturnType() instanceof Enumeration) {
                    ianaIfType = (Enumeration) method.getReturnType();
                }
            }
        }

        assertNotNull("Method getType MUST return Enumeration Type not NULL reference!", ianaIfType);
        assertEquals("Enumeration getType MUST contain 272 values!", 272, ianaIfType.getValues().size());
    }

    @Test
    public void testTypedefEnumResolving() throws URISyntaxException, IOException {
        final URI ianaIfTypePath = getClass().getResource("/leafref-test-models/iana-if-type@2012-06-05.yang").toURI();

        final SchemaContext context = resolveSchemaContextFromFiles(ianaIfTypePath);
        assertTrue(context != null);
        final BindingGenerator bindingGen = new BindingGeneratorImpl();
        final List<Type> genTypes = bindingGen.generateTypes(context);
        assertTrue(genTypes != null);
        assertEquals(1, genTypes.size());

        final Type type = genTypes.get(0);
        assertTrue(type instanceof Enumeration);

        final Enumeration enumer = (Enumeration) type;
        assertEquals("Enumeration type MUST contain 272 values!", 272, enumer.getValues().size());
    }

    @Test
    public void testLeafrefEnumResolving() throws URISyntaxException, IOException {
        final URI ietfInterfacesPath = getClass().getResource("/enum-test-models/ietf-interfaces@2012-11-15.yang")
                .toURI();
        final URI ifTypePath = getClass().getResource("/enum-test-models/iana-if-type@2012-06-05.yang").toURI();
        final URI yangTypesPath = getClass().getResource("/enum-test-models/ietf-yang-types@2010-09-24.yang").toURI();
        final URI topologyPath = getClass().getResource("/enum-test-models/abstract-topology@2013-02-08.yang").toURI();
        final URI inetTypesPath = getClass().getResource("/enum-test-models/ietf-inet-types@2010-09-24.yang").toURI();
        final SchemaContext context = resolveSchemaContextFromFiles(ietfInterfacesPath, ifTypePath, yangTypesPath,
                topologyPath, inetTypesPath);

        assertNotNull(context);
        final BindingGenerator bindingGen = new BindingGeneratorImpl();
        final List<Type> genTypes = bindingGen.generateTypes(context);
        assertNotNull(genTypes);
        assertTrue(!genTypes.isEmpty());

        GeneratedType genInterface = null;
        for (final Type type : genTypes) {
            if (type instanceof GeneratedType) {
                if (type.getPackageName().equals(
                        "org.opendaylight.yang.gen.v1.urn.model._abstract.topology.rev130208.topology.interfaces")
                        && type.getName().equals("Interface")) {
                    genInterface = (GeneratedType) type;
                }
            }
        }
        assertNotNull("Generated Type Interface is not present in list of Generated Types", genInterface);

        Type linkUpDownTrapEnable = null;
        Type operStatus = null;
        final List<MethodSignature> methods = genInterface.getMethodDefinitions();
        assertNotNull("Generated Type Interface cannot contain NULL reference to Enumeration types!", methods);
        assertEquals("Generated Type Interface MUST contain 5 Methods ", 5, methods.size());
        for (final MethodSignature method : methods) {
            if (method.getName().equals("getLinkUpDownTrapEnable")) {
                linkUpDownTrapEnable = method.getReturnType();
            } else if (method.getName().equals("getOperStatus")) {
                operStatus = method.getReturnType();
            }
        }

        assertNotNull("Expected Referenced Enum LinkUpDownTrapEnable, but was NULL!", linkUpDownTrapEnable);
        assertTrue("Expected LinkUpDownTrapEnable of type Enumeration", linkUpDownTrapEnable instanceof Enumeration);
        assertEquals(linkUpDownTrapEnable.getPackageName(),
                "org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev121115.interfaces.Interface");

        assertNotNull("Expected Referenced Enum OperStatus, but was NULL!", operStatus);
        assertTrue("Expected OperStatus of type Enumeration", operStatus instanceof Enumeration);
        assertEquals(operStatus.getPackageName(),
                "org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev121115.interfaces.Interface");
    }

}
