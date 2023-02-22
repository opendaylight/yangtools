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
import org.opendaylight.mdsal.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
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
        assertEquals(31, genTypes.size());

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
            if (Naming.IDENTIFIABLE_KEY_NAME.equals(method.getName())) {
                getIfcKeyMethod = method;
                break;
            }
        }
        assertNotNull("getIfcKeyMethod is null", getIfcKeyMethod);
        assertThat(getIfcKeyMethod.getReturnType(), instanceOf(GeneratedTransferObject.class));
        assertEquals(JavaTypeName.create(
            "org.opendaylight.yang.gen.v1.urn.model.augment._abstract.topology.rev130503.topology.interfaces",
            "InterfaceKey"), getIfcKeyMethod.getReturnType().getIdentifier());

        MethodSignature getHigherLayerIfMethod = null;
        for (final MethodSignature method : gtInterfaceMethods) {
            if (method.getName().equals("getHigherLayerIf")) {
                getHigherLayerIfMethod = method;
                break;
            }
        }
        assertNotNull("getHigherLayerIf method is null", getHigherLayerIfMethod);
        assertEquals(Types.setTypeFor(Types.STRING), getHigherLayerIfMethod.getReturnType());

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
        assertEquals(Types.STRING, gtInterfaceId.getReturnType());

        // 'Tunnel'
        assertNotNull("Tunnel is null", gtTunnel);
        final List<MethodSignature> tunnelMethods = gtTunnel.getMethodDefinitions();
        assertNotNull("Tunnel methods are null", tunnelMethods);
        MethodSignature getTunnelKeyMethod = null;
        for (MethodSignature method : tunnelMethods) {
            if (Naming.IDENTIFIABLE_KEY_NAME.equals(method.getName())) {
                getTunnelKeyMethod = method;
                break;
            }
        }
        assertNotNull("getKey method of Tunnel is null", getTunnelKeyMethod);

        var retType = getTunnelKeyMethod.getReturnType();
        assertThat(retType, instanceOf(GeneratedTransferObject.class));
        assertEquals(JavaTypeName.create("org.opendaylight.yang.gen.v1.urn.model.augment._abstract.topology.rev130503"
            + ".topology.network.links.network.link.tunnels", "TunnelKey"), retType.getIdentifier());

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
        assertEquals(Types.typeForClass(Integer.class), gtTunnelId.getReturnType());

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
        assertEquals(Types.STRING, getIfcMethod.getReturnType());
    }
}
