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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class GeneratedTypesLeafrefTest {
    @Test
    void testLeafrefResolving() {
        final var context = YangParserTestUtils.parseYangResources(GeneratedTypesLeafrefTest.class,
            "/leafref-test-models/abstract-topology@2013-02-08.yang", "/ietf-models/ietf-interfaces.yang",
            "/ietf-models/ietf-inet-types.yang", "/ietf-models/ietf-yang-types.yang");
        assertEquals(4, context.getModules().size());

        final var genTypes = DefaultBindingGenerator.generateFor(context);
        assertEquals(55, genTypes.size());

        GeneratedTransferObject gtIfcKey = null;
        GeneratedType gtIfc = null;
        GeneratedType gtNetworkLink = null;
        GeneratedType gtSource = null;
        GeneratedType gtDest = null;
        GeneratedType gtTunnel = null;
        GeneratedTransferObject gtTunnelKey = null;
        GeneratedType gtTopology = null;
        for (final Type type : genTypes) {
            String name = type.getName();
            if ("InterfaceKey".equals(name)
                    && "org.opendaylight.yang.gen.v1.urn.model._abstract.topology.rev130208.topology.interfaces".equals(
                        type.getPackageName())) {
                gtIfcKey = (GeneratedTransferObject) type;
            } else {
                switch (name) {
                    case "Interface":
                        gtIfc = (GeneratedType) type;
                        break;
                    case "NetworkLink":
                        gtNetworkLink = (GeneratedType) type;
                        break;
                    case "SourceNode":
                        gtSource = (GeneratedType) type;
                        break;
                    case "DestinationNode":
                        gtDest = (GeneratedType) type;
                        break;
                    case "Tunnel":
                        gtTunnel = (GeneratedType) type;
                        break;
                    case "TunnelKey":
                        gtTunnelKey = (GeneratedTransferObject) type;
                        break;
                    case "Topology":
                        gtTopology = (GeneratedType) type;
                        break;
                    case null:
                    default:
                        break;
                }
            }
        }

        assertNotNull(gtIfcKey);
        assertNotNull(gtIfc);
        assertNotNull(gtNetworkLink);
        assertNotNull(gtSource);
        assertNotNull(gtDest);
        assertNotNull(gtTunnel);
        assertNotNull(gtTunnelKey);
        assertNotNull(gtTopology);

        // Topology
        final var gtTopoMethods = gtTopology.getMethodDefinitions();
        assertNotNull(gtTopoMethods);
        MethodSignature condLeafref = null;
        for (var method : gtTopoMethods) {
            if (method.getName().equals("getCondLeafref")) {
                condLeafref = method;
            }
        }
        assertNotNull(condLeafref);
        Type condLeafRT = condLeafref.getReturnType();
        assertNotNull(condLeafRT);
        assertEquals("org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri",
            condLeafRT.getFullyQualifiedName());

        // InterfaceId
        final var gtIfcKeyProps = gtIfcKey.getProperties();
        assertNotNull(gtIfcKeyProps);
        GeneratedProperty ifcIdProp = null;
        for (var property : gtIfcKeyProps) {
            if (property.getName().equals("interfaceId")) {
                ifcIdProp = property;
            }
        }
        assertNotNull(ifcIdProp);
        Type ifcIdPropType = ifcIdProp.getReturnType();
        assertNotNull(ifcIdPropType);
        assertEquals("java.lang.String", ifcIdPropType.getFullyQualifiedName());

        // Interface
        final var gtIfcMethods = gtIfc.getMethodDefinitions();
        assertNotNull(gtIfcMethods);
        MethodSignature getIfcKey = null;
        MethodSignature getHigherLayerIf = null;
        for (var method : gtIfcMethods) {
            switch (method.getName()) {
                case Naming.KEY_AWARE_KEY_NAME:
                    getIfcKey = method;
                    break;
                case "getHigherLayerIf":
                    getHigherLayerIf = method;
                    break;
                default:
            }
        }
        assertNotNull(getIfcKey);
        final var getIfcKeyType = getIfcKey.getReturnType();
        assertNotNull(getIfcKeyType);
        assertNotSame("java.lang.Void", getIfcKeyType);
        assertEquals("InterfaceKey", getIfcKeyType.getName());

        assertNotNull(getHigherLayerIf);
        final var getHigherLayerIfType = getHigherLayerIf.getReturnType();
        assertNotNull(getHigherLayerIfType);
        assertNotSame("java.lang.Void", getHigherLayerIfType);
        assertEquals("Set", getHigherLayerIfType.getName());

        // NetworkLink
        final var gtNetworkLinkMethods = gtNetworkLink.getMethodDefinitions();
        assertNotNull(gtNetworkLinkMethods);
        MethodSignature getIfc = null;
        for (var method : gtNetworkLinkMethods) {
            if (method.getName().equals("getInterface")) {
                getIfc = method;
            }
        }
        assertNotNull(getIfc);
        final var getIfcType = getIfc.getReturnType();
        assertNotNull(getIfcType);
        assertNotSame("java.lang.Void", getIfcType);
        assertEquals("String", getIfcType.getName());

        // SourceNode
        final var gtSourceMethods = gtSource.getMethodDefinitions();
        assertNotNull(gtSourceMethods);
        MethodSignature getIdSource = null;
        for (var method : gtSourceMethods) {
            if (method.getName().equals("getId")) {
                getIdSource = method;
            }
        }
        assertNotNull(getIdSource);
        final var getIdType = getIdSource.getReturnType();
        assertNotNull(getIdType);
        assertNotSame("java.lang.Void", getIdType);
        assertEquals("Uri", getIdType.getName());

        // DestinationNode
        final var gtDestMethods = gtDest.getMethodDefinitions();
        assertNotNull(gtDestMethods);
        MethodSignature getIdDest = null;
        for (var method : gtDestMethods) {
            if (method.getName().equals("getId")) {
                getIdDest = method;
            }
        }
        assertNotNull(getIdDest);
        final var getIdDestType = getIdDest.getReturnType();
        assertNotNull(getIdDestType);
        assertNotSame("java.lang.Void", getIdDestType);
        assertEquals("Uri", getIdDestType.getName());

        // Tunnel
        final var gtTunnelMethods = gtTunnel.getMethodDefinitions();
        assertNotNull(gtTunnelMethods);
        MethodSignature getTunnelKey = null;
        for (MethodSignature method : gtTunnelMethods) {
            if (Naming.KEY_AWARE_KEY_NAME.equals(method.getName())) {
                getTunnelKey = method;
            }
        }
        assertNotNull(getTunnelKey);
        final var getTunnelKeyType = getTunnelKey.getReturnType();
        assertNotNull(getTunnelKeyType);
        assertNotSame("java.lang.Void", getTunnelKeyType);
        assertEquals("TunnelKey", getTunnelKeyType.getName());

        // TunnelKey
        final var gtTunnelKeyProps = gtTunnelKey.getProperties();
        assertNotNull(gtTunnelKeyProps);
        GeneratedProperty tunnelId = null;
        for (var property : gtTunnelKeyProps) {
            if (property.getName().equals("tunnelId")) {
                tunnelId = property;
            }
        }
        assertNotNull(tunnelId);
        final var tunnelIdType = tunnelId.getReturnType();
        assertNotNull(tunnelIdType);
        assertNotSame("java.lang.Void", tunnelIdType);
        assertEquals("Uri", tunnelIdType.getName());
    }

    @Test
    public void testLeafrefInvalidPathResolving() {
        final var context =  YangParserTestUtils.parseYangResource("/leafref-test-invalid-model/foo.yang");
        assertEquals(1, context.getModules().size());

        final var uoe = assertThrows(UnsupportedOperationException.class,
            () -> DefaultBindingGenerator.generateFor(context));
        assertEquals("Cannot ascertain type", uoe.getMessage());
        final var cause = assertInstanceOf(IllegalArgumentException.class, uoe.getCause());
        assertThat(cause.getMessage()).contains("Failed to find leafref target");
    }
}
