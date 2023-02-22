/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class AugmentRelativeXPathTest {
    @Test
    public void testAugmentationWithRelativeXPath() {
        final List<GeneratedType> genTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResourceDirectory("/augment-relative-xpath-models"));
        assertNotNull("genTypes is null", genTypes);
        assertEquals(27, genTypes.size());

        GeneratedTransferObject gtInterfaceKey = null;
        GeneratedType gtInterface = null;
        GeneratedType gtTunnel = null;
        GeneratedTransferObject gtTunnelKey = null;

        for (final GeneratedType type : genTypes) {
            if (!type.getPackageName().contains("augment._abstract.topology")) {
                continue;
            }

            if (type.getName().equals("InterfaceKey")) {
                gtInterfaceKey = (GeneratedTransferObject) type;

                final List<GeneratedProperty> properties = gtInterfaceKey.getProperties();
                assertNotNull("InterfaceKey properties are null", properties);
                assertEquals(1, properties.size());

                final GeneratedProperty property = properties.get(0);
                assertEquals("interfaceId", property.getName());
                assertNotNull("interfaceId return type is null", property.getReturnType());
                assertEquals(JavaTypeName.create(String.class), property.getReturnType().getIdentifier());
            } else if (type.getName().equals("Interface")) {
                gtInterface = type;

                final List<MethodSignature> gtInterfaceMethods = gtInterface.getMethodDefinitions();
                assertNotNull("Interface methods are null", gtInterfaceMethods);
                assertEquals(9, gtInterfaceMethods.size());

                MethodSignature getIfcKeyMethod = null;
                for (final MethodSignature method : gtInterfaceMethods) {
                    if (Naming.IDENTIFIABLE_KEY_NAME.equals(method.getName())) {
                        getIfcKeyMethod = method;
                        break;
                    }
                }
                assertNotNull("getKey method is null", getIfcKeyMethod);
                assertNotNull("getKey method return type is null", getIfcKeyMethod.getReturnType());
                assertEquals(JavaTypeName.create(
                    "org.opendaylight.yang.gen.v1.urn.model.augment._abstract.topology.rev130503.topology.interfaces",
                    "InterfaceKey"),
                    getIfcKeyMethod.getReturnType().getIdentifier());
            } else if (type.getName().equals("Tunnel")) {
                gtTunnel = type;

                final List<MethodSignature> tunnelMethods = gtTunnel.getMethodDefinitions();
                assertNotNull("Tunnel methods are null", tunnelMethods);
                assertEquals(7, tunnelMethods.size());

                MethodSignature getTunnelKeyMethod = null;
                for (MethodSignature method : tunnelMethods) {
                    if (Naming.IDENTIFIABLE_KEY_NAME.equals(method.getName())) {
                        getTunnelKeyMethod = method;
                        break;
                    }
                }
                assertNotNull("getKey method is null", getTunnelKeyMethod);
                assertNotNull("getKey method return type", getTunnelKeyMethod.getReturnType());
                assertEquals(JavaTypeName.create("org.opendaylight.yang.gen.v1.urn.model.augment._abstract.topology"
                    + ".rev130503.topology.network.links.network.link.tunnels", "TunnelKey"),
                    getTunnelKeyMethod.getReturnType().getIdentifier());
            } else if (type.getName().equals("TunnelKey")) {
                assertThat(type, instanceOf(GeneratedTransferObject.class));

                gtTunnelKey = (GeneratedTransferObject) type;

                final List<GeneratedProperty> properties = gtTunnelKey.getProperties();
                assertNotNull("TunnelKey properties are null", properties);
                assertEquals(1, properties.size());

                final GeneratedProperty property = properties.get(0);
                assertEquals("tunnelId", property.getName());
                assertNotNull("tunnelId return type is null", property.getReturnType());
                assertEquals(
                    JavaTypeName.create("org.opendaylight.yang.gen.v1.urn.model._abstract.topology.rev130208", "Uri"),
                    property.getReturnType().getIdentifier());
            }
        }

        assertNotNull("Interface is null", gtInterface);
        assertNotNull("InterfaceKey is null", gtInterfaceKey);
        assertNotNull("Tunnel is null", gtTunnel);
        assertNotNull("TunnelKey is null", gtTunnelKey);
    }
}
