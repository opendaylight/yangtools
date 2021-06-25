/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class AugmentedTypeTest {
    @Test
    public void augmentedAbstractTopologyTest() {
        final EffectiveModelContext context = YangParserTestUtils.parseYangResources(AugmentedTypeTest.class,
            "/augment-test-models/abstract-topology@2013-02-08.yang",
            "/augment-test-models/augment-abstract-topology@2013-05-03.yang",
            "/augment-test-models/augment-network-link-attributes@2013-05-03.yang",
            "/augment-test-models/augment-topology-tunnels@2013-05-03.yang",
            "/augment-test-models/ietf-interfaces@2012-11-15.yang");
        assertNotNull("Schema Context is null", context);

        final List<GeneratedType> genTypes = DefaultBindingGenerator.generateFor(context);
        assertEquals(28, genTypes.size());

        GeneratedTransferObject gtInterfaceKey = null;
        GeneratedType gtInterface = null;
        GeneratedType gtTunnel = null;
        GeneratedTransferObject gtTunnelKey = null;
        GeneratedType gtNetworkLink2 = null;

        for (final GeneratedType type : genTypes) {
            if (!type.getPackageName().contains("augment._abstract.topology")) {
                continue;
            }

            if (type.getName().equals("InterfaceKey")) {
                gtInterfaceKey = (GeneratedTransferObject) type;
            } else if (type.getName().equals("Interface")) {
                gtInterface = type;
            } else if (type.getName().equals("Tunnel")) {
                gtTunnel = type;
            } else if (type.getName().equals("TunnelKey")) {
                gtTunnelKey = (GeneratedTransferObject) type;
            } else if (type.getName().equals("NetworkLink2")) {
                gtNetworkLink2 = type;
            }
        }

        // 'Interface
        assertNotNull("gtInterface is null", gtInterface);
        final List<MethodSignature> gtInterfaceMethods = gtInterface.getMethodDefinitions();
        assertNotNull("gtInterfaceMethods is null", gtInterfaceMethods);
        MethodSignature getIfcKeyMethod = null;
        for (final MethodSignature method : gtInterfaceMethods) {
            if (BindingMapping.IDENTIFIABLE_KEY_NAME.equals(method.getName())) {
                getIfcKeyMethod = method;
                break;
            }
        }
        assertNotNull("getIfcKeyMethod is null", getIfcKeyMethod);
        assertNotNull("getIfcKeyMethod.getReturnType() is null", getIfcKeyMethod.getReturnType());
        assertFalse("getIfcKeyMethod.getReturnType() should not be Void",
                getIfcKeyMethod.getReturnType().equals("java.lang.Void"));
        assertTrue("getIfcKeyMethod.getReturnType().getName() must be InterfaceKey", getIfcKeyMethod.getReturnType()
                .getName().equals("InterfaceKey"));

        MethodSignature getHigherLayerIfMethod = null;
        for (final MethodSignature method : gtInterfaceMethods) {
            if (method.getName().equals("getHigherLayerIf")) {
                getHigherLayerIfMethod = method;
                break;
            }
        }
        assertNotNull("getHigherLayerIf method is null", getHigherLayerIfMethod);
        assertNotNull("getHigherLayerIf method return type is null", getHigherLayerIfMethod.getReturnType());
        assertTrue("getHigherLayerIf method return type name must be List", getHigherLayerIfMethod.getReturnType()
                .getName().equals("List"));

        // 'InterfaceKey'
        assertNotNull("InterfaceKey is null", gtInterfaceKey);
        final List<GeneratedProperty> properties = gtInterfaceKey.getProperties();
        assertNotNull("properties is null", properties);
        GeneratedProperty gtInterfaceId = null;
        for (final GeneratedProperty property : properties) {
            if (property.getName().equals("interfaceId")) {
                gtInterfaceId = property;
                break;
            }
        }
        assertNotNull("interfaceId is null", gtInterfaceId);
        assertNotNull("interfaceId return type is null", gtInterfaceId.getReturnType());
        assertTrue("interfaceId return type name must be String",
                gtInterfaceId.getReturnType().getName().equals("String"));

        // 'Tunnel'
        assertNotNull("Tunnel is null", gtTunnel);
        final List<MethodSignature> tunnelMethods = gtTunnel.getMethodDefinitions();
        assertNotNull("Tunnel methods are null", tunnelMethods);
        MethodSignature getTunnelKeyMethod = null;
        for (MethodSignature method : tunnelMethods) {
            if (BindingMapping.IDENTIFIABLE_KEY_NAME.equals(method.getName())) {
                getTunnelKeyMethod = method;
                break;
            }
        }
        assertNotNull("getKey method of Tunnel is null", getTunnelKeyMethod);
        assertNotNull("getKey method return type is null", getTunnelKeyMethod.getReturnType());
        assertTrue("getKey method return type name must be TunnelKey", getTunnelKeyMethod.getReturnType().getName()
                .equals("TunnelKey"));

        // 'TunnelKey'
        assertNotNull("TunnelKey is null", gtTunnelKey);
        final List<GeneratedProperty> tunnelKeyProperties = gtTunnelKey.getProperties();
        assertNotNull("TunnelKey properties are null", tunnelKeyProperties);

        GeneratedProperty gtTunnelId = null;
        for (final GeneratedProperty property : tunnelKeyProperties) {
            if (property.getName().equals("tunnelId")) {
                gtTunnelId = property;
            }
        }
        assertNotNull("tunnelId is null", gtTunnelId);
        assertNotNull("tunnelId return type is null", gtTunnelId.getReturnType());
        assertTrue("tunnelId returnType name must be Integer", gtTunnelId.getReturnType().getName().equals("Integer"));

        // 'NetworkLink2'
        assertNotNull("NetworkLink2 is null", gtNetworkLink2);

        final List<MethodSignature> networkLink2Methods = gtNetworkLink2.getMethodDefinitions();
        assertNotNull("NetworkLink2 methods are null", networkLink2Methods);

        MethodSignature getIfcMethod = null;
        for (MethodSignature method : networkLink2Methods) {
            if (method.getName().equals("getInterface")) {
                getIfcMethod = method;
                break;
            }
        }

        assertNotNull("getInterface method is null", getIfcMethod);
        assertNotNull("getInterface method return type is null", getIfcMethod.getReturnType());
        assertTrue("getInterface method return type name must be String", getIfcMethod.getReturnType().getName()
                .equals("String"));
    }
}
