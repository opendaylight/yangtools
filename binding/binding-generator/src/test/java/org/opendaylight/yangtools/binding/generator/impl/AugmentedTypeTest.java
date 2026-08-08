/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.AugmentationArchetype;
import org.opendaylight.yangtools.binding.model.EntryObjectArchetype;
import org.opendaylight.yangtools.binding.model.GetterMethod;
import org.opendaylight.yangtools.binding.model.KeyArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.ri.BaseYangTypes;
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

        KeyArchetype gtInterfaceKey = null;
        EntryObjectArchetype gtInterface = null;
        EntryObjectArchetype gtTunnel = null;
        KeyArchetype gtTunnelKey = null;
        AugmentationArchetype gtNetworkLink2 = null;

        for (var type : genTypes) {
            if (!type.packageName().contains("augment._abstract.topology")) {
                continue;
            }

            if (type.simpleName().equals("InterfaceKey")) {
                gtInterfaceKey = assertInstanceOf(KeyArchetype.class, type);
            } else if (type.simpleName().equals("Interface")) {
                gtInterface = assertInstanceOf(EntryObjectArchetype.class, type);
            } else if (type.simpleName().equals("Tunnel")) {
                gtTunnel = assertInstanceOf(EntryObjectArchetype.class, type);
            } else if (type.simpleName().equals("TunnelKey")) {
                gtTunnelKey = assertInstanceOf(KeyArchetype.class, type);
            } else if (type.simpleName().equals("NetworkLink2")) {
                gtNetworkLink2 = assertInstanceOf(AugmentationArchetype.class, type);
            }
        }

        // 'Interface
        assertNotNull(gtInterface, "gtInterface is null");
        assertEquals(TypeName.of(
            "org.opendaylight.yang.gen.v1.urn.model.augment._abstract.topology.rev130503.topology.interfaces",
            "InterfaceKey"), gtInterface.keyName());

        GetterMethod getHigherLayerIfMethod = null;
        for (var method : gtInterface.getters()) {
            if (method.suffix().equals("HigherLayerIf")) {
                getHigherLayerIfMethod = method;
                break;
            }
        }
        assertNotNull(getHigherLayerIfMethod, "getHigherLayerIf method is null");
        assertEquals(Types.setTypeFor(BaseYangTypes.STRING_TYPE), getHigherLayerIfMethod.returnType());

        // 'InterfaceKey'
        assertNotNull(gtInterfaceKey, "InterfaceKey is null");
        final var methods = gtInterfaceKey.methods();
        assertNotNull(methods, "properties is null");
        final var gtInterfaceId = methods.get("interface-id");
        assertNotNull(gtInterfaceId, "interfaceId is null");
        assertEquals(BaseYangTypes.STRING_TYPE, gtInterfaceId.returnType());

        // 'Tunnel'
        assertNotNull(gtTunnel, "Tunnel is null");
        assertEquals(TypeName.of("""
            org.opendaylight.yang.gen.v1.urn.model.augment._abstract.topology.rev130503.topology.network.links.network.\
            link.tunnels""", "TunnelKey"), gtTunnel.keyName());
        assertThat(gtTunnel.getters()).hasSize(2);

        // 'TunnelKey'
        assertNotNull(gtTunnelKey, "TunnelKey is null");
        final var tunnelKeyProperties = gtTunnelKey.methods();
        assertNotNull(tunnelKeyProperties, "TunnelKey properties are null");

        final var gtTunnelId = tunnelKeyProperties.get("tunnel-id");
        assertNotNull(gtTunnelId, "tunnelId is null");
        assertEquals("TunnelId", gtTunnelId.suffix());
        assertEquals(BaseYangTypes.INT32_TYPE, gtTunnelId.returnType());

        // 'NetworkLink2'
        assertNotNull(gtNetworkLink2, "NetworkLink2 is null");

        final var networkLink2Methods = gtNetworkLink2.getters();
        assertNotNull(networkLink2Methods, "NetworkLink2 methods are null");

        GetterMethod getIfcMethod = null;
        for (var method : networkLink2Methods) {
            if (method.suffix().equals("Interface")) {
                getIfcMethod = method;
                break;
            }
        }

        assertNotNull(getIfcMethod, "getInterface method is null");
        assertEquals(BaseYangTypes.STRING_TYPE, getIfcMethod.returnType());
    }
}
