/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangContextParser;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

public class AugmentRelativeXPathTest {

    private final static List<File> augmentModels = new ArrayList<>();
    private final static URL augmentFolderPath = AugmentedTypeTest.class
            .getResource("/augment-relative-xpath-models");

    @BeforeClass
    public static void loadTestResources() throws URISyntaxException {
        final File augFolder = new File(augmentFolderPath.toURI());

        for (final File fileEntry : augFolder.listFiles()) {
            if (fileEntry.isFile()) {
                augmentModels.add(fileEntry);
            }
        }
    }

    @Test
    public void AugmentationWithRelativeXPathTest() throws IOException {
        final YangContextParser parser = new YangParserImpl();
        final SchemaContext context = parser.parseFiles(augmentModels);

        assertNotNull("context is null", context);
        final BindingGenerator bindingGen = new BindingGeneratorImpl();
        final List<Type> genTypes = bindingGen.generateTypes(context);

        assertNotNull("genTypes is null", genTypes);
        assertFalse("genTypes is empty", genTypes.isEmpty());

        GeneratedTransferObject gtInterfaceKey = null;
        GeneratedType gtInterface = null;
        GeneratedType gtTunnel = null;
        GeneratedTransferObject gtTunnelKey = null;

        for (final Type type : genTypes) {
            if (type.getName().equals("InterfaceKey") && type.getPackageName().contains("augment._abstract.topology")) {
                gtInterfaceKey = (GeneratedTransferObject) type;
            } else if (type.getName().equals("Interface") && type.getPackageName().contains("augment._abstract.topology")) {
                gtInterface = (GeneratedType) type;
            } else if (type.getName().equals("Tunnel") && type.getPackageName().contains("augment._abstract.topology")) {
                gtTunnel = (GeneratedType) type;
            } else if (type.getName().equals("TunnelKey") && type.getPackageName().contains("augment._abstract.topology")) {
                gtTunnelKey = (GeneratedTransferObject) type;
            }
        }

        // 'Interface
        assertNotNull("gtInterface is null", gtInterface);
        final List<MethodSignature> gtInterfaceMethods = gtInterface.getMethodDefinitions();
        assertNotNull("gtInterfaceMethods is null", gtInterfaceMethods);
        MethodSignature getIfcKeyMethod = null;
        for (final MethodSignature method : gtInterfaceMethods) {
            if (method.getName().equals("getKey")) {
                getIfcKeyMethod = method;
                break;
            }
        }
        assertNotNull("getIfcKeyMethod is null", getIfcKeyMethod);
        assertNotNull("getIfcKeyMethod.getReturnType() is null", getIfcKeyMethod.getReturnType());
        assertFalse("getIfcKeyMethod.getReturnType() should not be Void",
                getIfcKeyMethod.getReturnType().equals("java.lang.Void"));
        assertTrue("getIfcKeyMethod.getReturnType().getName() must be InterfaceKey",
                getIfcKeyMethod.getReturnType().getName().equals("InterfaceKey"));

        // 'InterfaceKey'
        assertNotNull("gtInterfaceKey is null", gtInterfaceKey);
        final List<GeneratedProperty> properties = gtInterfaceKey.getProperties();
        assertNotNull("properties is null", properties);
        GeneratedProperty gtInterfaceId = null;
        for (final GeneratedProperty property : properties) {
            if (property.getName().equals("interfaceId")) {
                gtInterfaceId = property;
                break;
            }
        }
        assertNotNull("gtInterfaceId is null", gtInterfaceId);
        assertNotNull("gtInterfaceId.getReturnType() is null", gtInterfaceId.getReturnType());
        assertFalse("gtInterfaceId.getReturnType() should not be Void",
                gtInterfaceId.getReturnType().equals("java.lang.Void"));
        assertTrue("gtInterfaceId.getReturnType().getName() must be String",
                gtInterfaceId.getReturnType().getName().equals("String"));

        // 'Tunnel'
        assertNotNull("gtTunnel is null", gtTunnel);
        final List<MethodSignature> tunnelMethods = gtTunnel.getMethodDefinitions();
        assertNotNull("tunnelMethods is null", tunnelMethods);
        MethodSignature getTunnelKeyMethod = null;
        for (MethodSignature method : tunnelMethods) {
            if (method.getName().equals("getKey")) {
                getTunnelKeyMethod = method;
                break;
            }
        }
        assertNotNull("getTunnelKeyMethod is null", getTunnelKeyMethod);
        assertNotNull("getTunnelKeyMethod.getReturnType()",
                getTunnelKeyMethod.getReturnType());
        assertFalse("getTunnelKeyMethod.getReturnType() should not be Void",
                getTunnelKeyMethod.getReturnType().equals("java.lang.Void"));
        assertTrue("getTunnelKeyMethod.getReturnType().getName() must be TunnelKey",
                getTunnelKeyMethod.getReturnType().getName().equals("TunnelKey"));

        // 'TunnelKey'
        assertNotNull("gtTunnelKey is null", gtTunnelKey);
        final List<GeneratedProperty> tunnelKeyProperties = gtTunnelKey.getProperties();
        assertNotNull("tunnelKeyProperties is null", tunnelKeyProperties);

        GeneratedProperty gtTunnelId = null;
        for (final GeneratedProperty property : tunnelKeyProperties) {
            if (property.getName().equals("tunnelId")) {
                gtTunnelId = property;
            }
        }
        assertNotNull("gtTunnelId is null", gtTunnelId);
        assertNotNull("gtTunnelId.getReturnType() is null",
                gtTunnelId.getReturnType());
        assertFalse("gtTunnelId.getReturnType() should not be Void",
                gtTunnelId.getReturnType().equals("java.lang.Void"));
        assertTrue("gtTunnelId.getReturnType().getName() must be Uri",
                gtTunnelId.getReturnType().getName().equals("Uri"));
    }

}
