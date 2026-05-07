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
import org.opendaylight.yangtools.binding.model.EntryObjectArchetype;
import org.opendaylight.yangtools.binding.model.KeyArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class AugmentRelativeXPathTest {
    @Test
    void testAugmentationWithRelativeXPath() {
        final var genTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResourceDirectory("/augment-relative-xpath-models"));
        assertNotNull(genTypes);
        assertEquals(27, genTypes.size());

        KeyArchetype gtInterfaceKey = null;
        EntryObjectArchetype gtInterface = null;
        EntryObjectArchetype gtTunnel = null;
        KeyArchetype gtTunnelKey = null;

        for (var type : genTypes) {
            if (!type.packageName().toString().contains("augment._abstract.topology")) {
                continue;
            }

            if (type.simpleName().equals("InterfaceKey")) {
                gtInterfaceKey = assertInstanceOf(KeyArchetype.class, type);

                final var methods = gtInterfaceKey.methods();
                assertNotNull(methods, "InterfaceKey properties are null");
                assertEquals(1, methods.size());

                final var property = methods.entrySet().iterator().next();
                assertEquals("interface-id", property.getKey());
                final var returnType = property.getValue().returnType();
                assertNotNull(returnType, "interfaceId return type is null");
                assertEquals(TypeName.ofClass(String.class), returnType.name());
            } else if (type.simpleName().equals("Interface")) {
                gtInterface = assertInstanceOf(EntryObjectArchetype.class, type);

                final var gtInterfaceGetters = gtInterface.getters();
                assertNotNull(gtInterfaceGetters, "Interface methods are null");
                assertEquals(2, gtInterfaceGetters.size());

                assertEquals("""
                    org.opendaylight.yang.gen.v1.urn.model.augment._abstract.topology.rev130503.topology.interfaces.\
                    InterfaceKey""", gtInterface.keyName().toString());
            } else if (type.simpleName().equals("Tunnel")) {
                gtTunnel = assertInstanceOf(EntryObjectArchetype.class, type);

                final var tunnelGetters = gtTunnel.getters();
                assertNotNull(tunnelGetters, "Tunnel methods are null");
                assertEquals(1, tunnelGetters.size());

                assertEquals("""
                    org.opendaylight.yang.gen.v1.urn.model.augment._abstract.topology.rev130503.topology.network.links.\
                    network.link.tunnels.TunnelKey""", gtTunnel.keyName().toString());
            } else if (type.simpleName().equals("TunnelKey")) {
                gtTunnelKey = assertInstanceOf(KeyArchetype.class, type);

                final var methods = gtTunnelKey.methods();
                assertNotNull(methods, "TunnelKey properties are null");
                assertEquals(1, methods.size());

                final var property = methods.entrySet().iterator().next();
                assertEquals("tunnel-id", property.getKey());
                final var returnType = property.getValue().returnType();
                assertNotNull(returnType, "tunnelId return type is null");
                assertEquals("org.opendaylight.yang.gen.v1.urn.model._abstract.topology.rev130208.Uri",
                    returnType.name().toString());
            }
        }

        assertNotNull(gtInterface, "Interface is null");
        assertNotNull(gtInterfaceKey, "InterfaceKey is null");
        assertNotNull(gtTunnel, "Tunnel is null");
        assertNotNull(gtTunnelKey, "TunnelKey is null");
    }
}
