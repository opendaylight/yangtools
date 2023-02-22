/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThrows;

import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class GeneratedTypesLeafrefTest {

    @Test
    public void testLeafrefResolving() {
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
            } else if ("Interface".equals(name)) {
                gtIfc = (GeneratedType) type;
            } else if ("NetworkLink".equals(name)) {
                gtNetworkLink = (GeneratedType) type;
            } else if ("SourceNode".equals(name)) {
                gtSource = (GeneratedType) type;
            } else if ("DestinationNode".equals(name)) {
                gtDest = (GeneratedType) type;
            } else if ("Tunnel".equals(name)) {
                gtTunnel = (GeneratedType) type;
            } else if ("TunnelKey".equals(name)) {
                gtTunnelKey = (GeneratedTransferObject) type;
            } else if ("Topology".equals(name)) {
                gtTopology = (GeneratedType) type;
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
        final List<MethodSignature> gtTopoMethods = gtTopology.getMethodDefinitions();
        assertNotNull(gtTopoMethods);
        MethodSignature condLeafref = null;
        for (final MethodSignature method : gtTopoMethods) {
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
        final List<GeneratedProperty> gtIfcKeyProps = gtIfcKey.getProperties();
        assertNotNull(gtIfcKeyProps);
        GeneratedProperty ifcIdProp = null;
        for (final GeneratedProperty property : gtIfcKeyProps) {
            if (property.getName().equals("interfaceId")) {
                ifcIdProp = property;
            }
        }
        assertNotNull(ifcIdProp);
        Type ifcIdPropType = ifcIdProp.getReturnType();
        assertNotNull(ifcIdPropType);
        assertEquals("java.lang.String", ifcIdPropType.getFullyQualifiedName());

        // Interface
        final List<MethodSignature> gtIfcMethods = gtIfc.getMethodDefinitions();
        assertNotNull(gtIfcMethods);
        MethodSignature getIfcKey = null;
        MethodSignature getHigherLayerIf = null;
        for (final MethodSignature method : gtIfcMethods) {
            switch (method.getName()) {
                case Naming.IDENTIFIABLE_KEY_NAME:
                    getIfcKey = method;
                    break;
                case "getHigherLayerIf":
                    getHigherLayerIf = method;
                    break;
                default:
            }
        }
        assertNotNull(getIfcKey);
        Type getIfcKeyType = getIfcKey.getReturnType();
        assertNotNull(getIfcKeyType);
        assertNotSame("java.lang.Void", getIfcKeyType);
        assertEquals("InterfaceKey", getIfcKeyType.getName());

        assertNotNull(getHigherLayerIf);
        Type getHigherLayerIfType = getHigherLayerIf.getReturnType();
        assertNotNull(getHigherLayerIfType);
        assertNotSame("java.lang.Void", getHigherLayerIfType);
        assertEquals("Set", getHigherLayerIfType.getName());

        // NetworkLink
        final List<MethodSignature> gtNetworkLinkMethods = gtNetworkLink.getMethodDefinitions();
        assertNotNull(gtNetworkLinkMethods);
        MethodSignature getIfc = null;
        for (MethodSignature method : gtNetworkLinkMethods) {
            if (method.getName().equals("getInterface")) {
                getIfc = method;
            }
        }
        assertNotNull(getIfc);
        Type getIfcType = getIfc.getReturnType();
        assertNotNull(getIfcType);
        assertNotSame("java.lang.Void", getIfcType);
        assertEquals("String", getIfcType.getName());

        // SourceNode
        final List<MethodSignature> gtSourceMethods = gtSource.getMethodDefinitions();
        assertNotNull(gtSourceMethods);
        MethodSignature getIdSource = null;
        for (MethodSignature method : gtSourceMethods) {
            if (method.getName().equals("getId")) {
                getIdSource = method;
            }
        }
        assertNotNull(getIdSource);
        Type getIdType = getIdSource.getReturnType();
        assertNotNull(getIdType);
        assertNotSame("java.lang.Void", getIdType);
        assertEquals("Uri", getIdType.getName());

        // DestinationNode
        final List<MethodSignature> gtDestMethods = gtDest.getMethodDefinitions();
        assertNotNull(gtDestMethods);
        MethodSignature getIdDest = null;
        for (MethodSignature method : gtDestMethods) {
            if (method.getName().equals("getId")) {
                getIdDest = method;
            }
        }
        assertNotNull(getIdDest);
        Type getIdDestType = getIdDest.getReturnType();
        assertNotNull(getIdDestType);
        assertNotSame("java.lang.Void", getIdDestType);
        assertEquals("Uri", getIdDestType.getName());

        // Tunnel
        final List<MethodSignature> gtTunnelMethods = gtTunnel.getMethodDefinitions();
        assertNotNull(gtTunnelMethods);
        MethodSignature getTunnelKey = null;
        for (MethodSignature method : gtTunnelMethods) {
            if (Naming.IDENTIFIABLE_KEY_NAME.equals(method.getName())) {
                getTunnelKey = method;
            }
        }
        assertNotNull(getTunnelKey);
        Type getTunnelKeyType = getTunnelKey.getReturnType();
        assertNotNull(getTunnelKeyType);
        assertNotSame("java.lang.Void", getTunnelKeyType);
        assertEquals("TunnelKey", getTunnelKeyType.getName());

        // TunnelKey
        final List<GeneratedProperty> gtTunnelKeyProps = gtTunnelKey.getProperties();
        assertNotNull(gtTunnelKeyProps);
        GeneratedProperty tunnelId = null;
        for (final GeneratedProperty property : gtTunnelKeyProps) {
            if (property.getName().equals("tunnelId")) {
                tunnelId = property;
            }
        }
        assertNotNull(tunnelId);
        Type tunnelIdType = tunnelId.getReturnType();
        assertNotNull(tunnelIdType);
        assertNotSame("java.lang.Void", tunnelIdType);
        assertEquals("Uri", tunnelIdType.getName());
    }

    @Test
    public void testLeafrefInvalidPathResolving() {
        final EffectiveModelContext context =  YangParserTestUtils.parseYangResource(
            "/leafref-test-invalid-model/foo.yang");
        assertEquals(1, context.getModules().size());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> DefaultBindingGenerator.generateFor(context));
        assertThat(ex.getMessage(), containsString("Failed to find leafref target"));
    }
}