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
import org.opendaylight.yangtools.binding.model.ContainerObjectArchetype;
import org.opendaylight.yangtools.binding.model.EntryObjectArchetype;
import org.opendaylight.yangtools.binding.model.GetterMethod;
import org.opendaylight.yangtools.binding.model.KeyArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;
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

        KeyArchetype gtIfcKey = null;
        EntryObjectArchetype gtIfc = null;
        EntryObjectArchetype gtNetworkLink = null;
        ContainerObjectArchetype gtSource = null;
        ContainerObjectArchetype gtDest = null;
        EntryObjectArchetype gtTunnel = null;
        KeyArchetype gtTunnelKey = null;
        ContainerObjectArchetype gtTopology = null;

        for (var type : genTypes) {
            String name = type.simpleName();
            if ("InterfaceKey".equals(name)
                    && "org.opendaylight.yang.gen.v1.urn.model._abstract.topology.rev130208.topology.interfaces".equals(
                        type.packageName())) {
                gtIfcKey = assertInstanceOf(KeyArchetype.class, type);
            } else {
                switch (name) {
                    case "Interface" ->
                        gtIfc = assertInstanceOf(EntryObjectArchetype.class, type);
                    case "NetworkLink" ->
                        gtNetworkLink = assertInstanceOf(EntryObjectArchetype.class, type);
                    case "SourceNode" ->
                        gtSource = assertInstanceOf(ContainerObjectArchetype.class, type);
                    case "DestinationNode" ->
                        gtDest = assertInstanceOf(ContainerObjectArchetype.class, type);
                    case "Tunnel" ->
                        gtTunnel = assertInstanceOf(EntryObjectArchetype.class, type);
                    case "TunnelKey" ->
                        gtTunnelKey = assertInstanceOf(KeyArchetype.class, type);
                    case "Topology" ->
                        gtTopology = assertInstanceOf(ContainerObjectArchetype.class, type);
                    default -> {
                        // no-op
                    }
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
        final var gtTopoMethods = gtTopology.getters();
        assertNotNull(gtTopoMethods);
        GetterMethod condLeafref = null;
        for (var method : gtTopoMethods) {
            if (method.suffix().equals("CondLeafref")) {
                condLeafref = method;
            }
        }
        assertNotNull(condLeafref);
        Type condLeafRT = condLeafref.returnType();
        assertNotNull(condLeafRT);
        assertEquals("org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri",
            condLeafRT.canonicalName());

        // InterfaceId
        final var gtIfcKeyProps = gtIfcKey.methods();
        assertNotNull(gtIfcKeyProps);
        var ifcIdProp = gtIfcKeyProps.get("interface-id");
        assertNotNull(ifcIdProp);
        Type ifcIdPropType = ifcIdProp.returnType();
        assertNotNull(ifcIdPropType);
        assertEquals("java.lang.String", ifcIdPropType.canonicalName());

        // Interface
        assertEquals(TypeName.of(
            "org.opendaylight.yang.gen.v1.urn.model._abstract.topology.rev130208.topology.interfaces", "InterfaceKey"),
            gtIfc.keyName());
        final var gtIfcMethods = gtIfc.getters();
        assertNotNull(gtIfcMethods);
        GetterMethod getHigherLayerIf = null;
        for (var method : gtIfcMethods) {
            switch (method.suffix()) {
                case "HigherLayerIf" -> getHigherLayerIf = method;
                default -> {
                    // no-op
                }
            }
        }

        assertNotNull(getHigherLayerIf);
        final var getHigherLayerIfType = getHigherLayerIf.returnType();
        assertNotNull(getHigherLayerIfType);
        assertNotSame("java.lang.Void", getHigherLayerIfType);
        assertEquals("Set", getHigherLayerIfType.simpleName());

        // NetworkLink
        final var gtNetworkLinkMethods = gtNetworkLink.getters();
        assertNotNull(gtNetworkLinkMethods);
        GetterMethod getIfc = null;
        for (var method : gtNetworkLinkMethods) {
            if (method.suffix().equals("Interface")) {
                getIfc = method;
            }
        }
        assertNotNull(getIfc);
        final var getIfcType = getIfc.returnType();
        assertNotNull(getIfcType);
        assertNotSame("java.lang.Void", getIfcType);
        assertEquals("String", getIfcType.simpleName());

        // SourceNode
        final var gtSourceMethods = gtSource.getters();
        assertNotNull(gtSourceMethods);
        GetterMethod getIdSource = null;
        for (var method : gtSourceMethods) {
            if (method.suffix().equals("Id")) {
                getIdSource = method;
            }
        }
        assertNotNull(getIdSource);
        final var getIdType = getIdSource.returnType();
        assertNotNull(getIdType);
        assertNotSame("java.lang.Void", getIdType);
        assertEquals("Uri", getIdType.simpleName());

        // DestinationNode
        final var gtDestMethods = gtDest.getters();
        assertNotNull(gtDestMethods);
        GetterMethod getIdDest = null;
        for (var method : gtDestMethods) {
            if (method.suffix().equals("Id")) {
                getIdDest = method;
            }
        }
        assertNotNull(getIdDest);
        final var getIdDestType = getIdDest.returnType();
        assertNotNull(getIdDestType);
        assertNotSame("java.lang.Void", getIdDestType);
        assertEquals("Uri", getIdDestType.simpleName());

        // Tunnel
        assertEquals(TypeName.of("""
            org.opendaylight.yang.gen.v1.urn.model._abstract.topology.rev130208.topology.network.links.network.link.\
            tunnels""", "TunnelKey"), gtTunnel.keyName());
        assertThat(gtTunnel.getters()).hasSize(1);

        // TunnelKey
        final var gtTunnelKeyProps = gtTunnelKey.methods();
        assertNotNull(gtTunnelKeyProps);
        var tunnelId = gtTunnelKeyProps.get("tunnel-id");
        assertNotNull(tunnelId);
        final var tunnelIdType = tunnelId.returnType();
        assertNotNull(tunnelIdType);
        assertNotSame("java.lang.Void", tunnelIdType);
        assertEquals("Uri", tunnelIdType.simpleName());
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
