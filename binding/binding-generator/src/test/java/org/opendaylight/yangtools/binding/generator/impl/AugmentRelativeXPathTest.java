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
import static org.opendaylight.yangtools.binding.generator.impl.SupportTestUtil.assertEntryObject;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.KeyArchetype;
import org.opendaylight.yangtools.binding.model.api.LegacyArchetype;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class AugmentRelativeXPathTest {
    @Test
    void testAugmentationWithRelativeXPath() {
        final var genTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResourceDirectory("/augment-relative-xpath-models"));
        assertNotNull(genTypes);
        assertEquals(27, genTypes.size());

        KeyArchetype gtInterfaceKey = null;
        LegacyArchetype<?> gtInterface = null;
        LegacyArchetype<?> gtTunnel = null;
        KeyArchetype gtTunnelKey = null;

        for (var type : genTypes) {
            if (!type.packageName().contains("augment._abstract.topology")) {
                continue;
            }

            if (type.simpleName().equals("InterfaceKey")) {
                gtInterfaceKey = assertInstanceOf(KeyArchetype.class, type);

                final var properties = gtInterfaceKey.getProperties();
                assertNotNull(properties, "InterfaceKey properties are null");
                assertEquals(1, properties.size());

                final var property = properties.getFirst();
                assertEquals("interfaceId", property.getName());
                assertNotNull(property.getReturnType(), "interfaceId return type is null");
                assertEquals(JavaTypeName.create(String.class), property.getReturnType().name());
            } else if (type.simpleName().equals("Interface")) {
                gtInterface = assertInstanceOf(LegacyArchetype.class, type);

                final var gtInterfaceMethods = gtInterface.getMethodDefinitions();
                assertNotNull(gtInterfaceMethods, "Interface methods are null");
                assertEquals(5, gtInterfaceMethods.size());

                assertEntryObject(gtInterface, JavaTypeName.create(
                    "org.opendaylight.yang.gen.v1.urn.model.augment._abstract.topology.rev130503.topology.interfaces",
                    "InterfaceKey"));
            } else if (type.simpleName().equals("Tunnel")) {
                gtTunnel = assertInstanceOf(LegacyArchetype.class, type);

                final var tunnelMethods = gtTunnel.getMethodDefinitions();
                assertNotNull(tunnelMethods, "Tunnel methods are null");
                assertEquals(3, tunnelMethods.size());

                assertEntryObject(gtTunnel, JavaTypeName.create("""
                    org.opendaylight.yang.gen.v1.urn.model.augment._abstract.topology.rev130503.topology.network.links.\
                    network.link.tunnels""", "TunnelKey"));
            } else if (type.simpleName().equals("TunnelKey")) {
                gtTunnelKey = assertInstanceOf(KeyArchetype.class, type);

                final var properties = gtTunnelKey.getProperties();
                assertNotNull(properties, "TunnelKey properties are null");
                assertEquals(1, properties.size());

                final var property = properties.getFirst();
                assertEquals("tunnelId", property.getName());
                assertNotNull(property.getReturnType(), "tunnelId return type is null");
                assertEquals(
                    JavaTypeName.create("org.opendaylight.yang.gen.v1.urn.model._abstract.topology.rev130208", "Uri"),
                    property.getReturnType().name());
            }
        }

        assertNotNull(gtInterface, "Interface is null");
        assertNotNull(gtInterfaceKey, "InterfaceKey is null");
        assertNotNull(gtTunnel, "Tunnel is null");
        assertNotNull(gtTunnelKey, "TunnelKey is null");
    }
}
