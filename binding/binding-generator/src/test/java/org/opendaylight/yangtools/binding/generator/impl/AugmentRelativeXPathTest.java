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
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class AugmentRelativeXPathTest {
    @Test
    void testAugmentationWithRelativeXPath() {
        final var genTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResourceDirectory("/augment-relative-xpath-models"));
        assertNotNull(genTypes);
        assertEquals(27, genTypes.size());

        GeneratedTransferObject gtInterfaceKey = null;
        GeneratedType gtInterface = null;
        GeneratedType gtTunnel = null;
        GeneratedTransferObject gtTunnelKey = null;

        for (var type : genTypes) {
            if (!type.getPackageName().contains("augment._abstract.topology")) {
                continue;
            }

            if (type.getName().equals("InterfaceKey")) {
                gtInterfaceKey = assertInstanceOf(GeneratedTransferObject.class, type);

                final var properties = gtInterfaceKey.getProperties();
                assertNotNull(properties, "InterfaceKey properties are null");
                assertEquals(1, properties.size());

                final var property = properties.getFirst();
                assertEquals("interfaceId", property.getName());
                assertNotNull(property.getReturnType(), "interfaceId return type is null");
                assertEquals(JavaTypeName.create(String.class), property.getReturnType().getIdentifier());
            } else if (type.getName().equals("Interface")) {
                gtInterface = type;

                final var gtInterfaceMethods = gtInterface.getMethodDefinitions();
                assertNotNull(gtInterfaceMethods, "Interface methods are null");
                assertEquals(9, gtInterfaceMethods.size());

                MethodSignature getIfcKeyMethod = null;
                for (var method : gtInterfaceMethods) {
                    if (Naming.KEY_AWARE_KEY_NAME.equals(method.getName())) {
                        getIfcKeyMethod = method;
                        break;
                    }
                }
                assertNotNull(getIfcKeyMethod, "getKey method is null");
                assertNotNull(getIfcKeyMethod.getReturnType(), "getKey method return type is null");
                assertEquals(JavaTypeName.create(
                    "org.opendaylight.yang.gen.v1.urn.model.augment._abstract.topology.rev130503.topology.interfaces",
                    "InterfaceKey"),
                    getIfcKeyMethod.getReturnType().getIdentifier());
            } else if (type.getName().equals("Tunnel")) {
                gtTunnel = type;

                final var tunnelMethods = gtTunnel.getMethodDefinitions();
                assertNotNull(tunnelMethods, "Tunnel methods are null");
                assertEquals(7, tunnelMethods.size());

                MethodSignature getTunnelKeyMethod = null;
                for (var method : tunnelMethods) {
                    if (Naming.KEY_AWARE_KEY_NAME.equals(method.getName())) {
                        getTunnelKeyMethod = method;
                        break;
                    }
                }
                assertNotNull(getTunnelKeyMethod, "getKey method is null");
                assertNotNull(getTunnelKeyMethod.getReturnType(), "getKey method return type");
                assertEquals(JavaTypeName.create("org.opendaylight.yang.gen.v1.urn.model.augment._abstract.topology"
                    + ".rev130503.topology.network.links.network.link.tunnels", "TunnelKey"),
                    getTunnelKeyMethod.getReturnType().getIdentifier());
            } else if (type.getName().equals("TunnelKey")) {
                gtTunnelKey = assertInstanceOf(GeneratedTransferObject.class, type);

                final var properties = gtTunnelKey.getProperties();
                assertNotNull(properties, "TunnelKey properties are null");
                assertEquals(1, properties.size());

                final var property = properties.getFirst();
                assertEquals("tunnelId", property.getName());
                assertNotNull(property.getReturnType(), "tunnelId return type is null");
                assertEquals(
                    JavaTypeName.create("org.opendaylight.yang.gen.v1.urn.model._abstract.topology.rev130208", "Uri"),
                    property.getReturnType().getIdentifier());
            }
        }

        assertNotNull(gtInterface, "Interface is null");
        assertNotNull(gtInterfaceKey, "InterfaceKey is null");
        assertNotNull(gtTunnel, "Tunnel is null");
        assertNotNull(gtTunnelKey, "TunnelKey is null");
    }
}
