/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class AugmentedTypeTest {
    @Test
    void augmentedAbstractTopologyTest() {
        final var context = YangParserTestUtils.parseYangResources(AugmentedTypeTest.class,
            "/augment-test-models/abstract-topology@2013-02-08.yang",
            "/augment-test-models/augment-abstract-topology@2013-05-03.yang",
            "/augment-test-models/augment-network-link-attributes@2013-05-03.yang",
            "/augment-test-models/augment-topology-tunnels@2013-05-03.yang",
            "/augment-test-models/ietf-interfaces@2012-11-15.yang");

        final var genTypes = DefaultBindingGenerator.generateFor(context);
        assertEquals(31, genTypes.size());

        GeneratedTransferObject gtInterfaceKey = null;
        GeneratedType gtInterface = null;
        GeneratedType gtTunnel = null;
        GeneratedTransferObject gtTunnelKey = null;
        GeneratedType gtNetworkLink2 = null;

        for (var type : genTypes) {
            if (!type.getPackageName().contains("augment._abstract.topology")) {
                continue;
            }

            if (type.getName().equals("InterfaceKey")) {
                gtInterfaceKey = assertInstanceOf(GeneratedTransferObject.class, type);
            } else if (type.getName().equals("Interface")) {
                gtInterface = type;
            } else if (type.getName().equals("Tunnel")) {
                gtTunnel = type;
            } else if (type.getName().equals("TunnelKey")) {
                gtTunnelKey = assertInstanceOf(GeneratedTransferObject.class, type);
            } else if (type.getName().equals("NetworkLink2")) {
                gtNetworkLink2 = type;
            }
        }

        // 'Interface
        assertNotNull(gtInterface, "gtInterface is null");
        final var gtInterfaceMethods = gtInterface.getMethodDefinitions();
        assertNotNull(gtInterfaceMethods, "gtInterfaceMethods is null");
        MethodSignature getIfcKeyMethod = null;
        for (final MethodSignature method : gtInterfaceMethods) {
            if (Naming.KEY_AWARE_KEY_NAME.equals(method.getName())) {
                getIfcKeyMethod = method;
                break;
            }
        }
        assertNotNull(getIfcKeyMethod, "getIfcKeyMethod is null");
        var retType = assertInstanceOf(GeneratedTransferObject.class, getIfcKeyMethod.getReturnType());
        assertEquals(JavaTypeName.create(
            "org.opendaylight.yang.gen.v1.urn.model.augment._abstract.topology.rev130503.topology.interfaces",
            "InterfaceKey"), retType.getIdentifier());

        MethodSignature getHigherLayerIfMethod = null;
        for (var method : gtInterfaceMethods) {
            if (method.getName().equals("getHigherLayerIf")) {
                getHigherLayerIfMethod = method;
                break;
            }
        }
        assertNotNull(getHigherLayerIfMethod, "getHigherLayerIf method is null");
        assertEquals(Types.setTypeFor(Types.STRING), getHigherLayerIfMethod.getReturnType());

        // 'InterfaceKey'
        assertNotNull(gtInterfaceKey, "InterfaceKey is null");
        final var properties = gtInterfaceKey.getProperties();
        assertNotNull(properties, "properties is null");
        GeneratedProperty gtInterfaceId = null;
        for (var property : properties) {
            if (property.getName().equals("interfaceId")) {
                gtInterfaceId = property;
                break;
            }
        }
        assertNotNull(gtInterfaceId, "interfaceId is null");
        assertEquals(Types.STRING, gtInterfaceId.getReturnType());

        // 'Tunnel'
        assertNotNull(gtTunnel, "Tunnel is null");
        final var tunnelMethods = gtTunnel.getMethodDefinitions();
        assertNotNull(tunnelMethods, "Tunnel methods are null");
        MethodSignature getTunnelKeyMethod = null;
        for (var method : tunnelMethods) {
            if (Naming.KEY_AWARE_KEY_NAME.equals(method.getName())) {
                getTunnelKeyMethod = method;
                break;
            }
        }
        assertNotNull(getTunnelKeyMethod, "getKey method of Tunnel is null");

        retType = assertInstanceOf(GeneratedTransferObject.class, getTunnelKeyMethod.getReturnType());
        assertEquals(JavaTypeName.create("org.opendaylight.yang.gen.v1.urn.model.augment._abstract.topology.rev130503"
            + ".topology.network.links.network.link.tunnels", "TunnelKey"), retType.getIdentifier());

        // 'TunnelKey'
        assertNotNull(gtTunnelKey, "TunnelKey is null");
        final var tunnelKeyProperties = gtTunnelKey.getProperties();
        assertNotNull(tunnelKeyProperties, "TunnelKey properties are null");

        GeneratedProperty gtTunnelId = null;
        for (var property : tunnelKeyProperties) {
            if (property.getName().equals("tunnelId")) {
                gtTunnelId = property;
            }
        }
        assertNotNull(gtTunnelId, "tunnelId is null");
        assertEquals(Types.typeForClass(Integer.class), gtTunnelId.getReturnType());

        // 'NetworkLink2'
        assertNotNull(gtNetworkLink2, "NetworkLink2 is null");

        final var networkLink2Methods = gtNetworkLink2.getMethodDefinitions();
        assertNotNull(networkLink2Methods, "NetworkLink2 methods are null");

        MethodSignature getIfcMethod = null;
        for (var method : networkLink2Methods) {
            if (method.getName().equals("getInterface")) {
                getIfcMethod = method;
                break;
            }
        }

        assertNotNull(getIfcMethod, "getInterface method is null");
        assertEquals(Types.STRING, getIfcMethod.getReturnType());
    }
}
