/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangContextParser;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

public class GeneratedTypesLeafrefTest {

    private SchemaContext resolveSchemaContextFromFiles(final URI... yangFiles) throws IOException {
        final YangContextParser parser = new YangParserImpl();

        final List<File> inputFiles = new ArrayList<File>();
        for (int i = 0; i < yangFiles.length; ++i) {
            inputFiles.add(new File(yangFiles[i]));
        }

        return parser.parseFiles(inputFiles);
    }

    @Test
    public void testLeafrefResolving() throws URISyntaxException, IOException {
        final URI topologyPath = getClass().getResource("/leafref-test-models/abstract-topology@2013-02-08.yang")
                .toURI();
        final URI interfacesPath = getClass().getResource("/leafref-test-models/ietf-interfaces@2012-11-15.yang")
                .toURI();
        final URI inetTypesPath = getClass().getResource("/leafref-test-models/ietf-inet-types@2010-09-24.yang")
                .toURI();
        final URI yangTypesPath = getClass().getResource("/leafref-test-models/ietf-yang-types@2010-09-24.yang")
                .toURI();

        assertNotNull(topologyPath);
        assertNotNull(interfacesPath);
        assertNotNull(inetTypesPath);
        assertNotNull(yangTypesPath);

        final SchemaContext context = resolveSchemaContextFromFiles(topologyPath, interfacesPath, inetTypesPath,
                yangTypesPath);
        assertNotNull(context);
        assertEquals(4, context.getModules().size());

        final BindingGenerator bindingGen = new BindingGeneratorImpl();
        final List<Type> genTypes = bindingGen.generateTypes(context);

        assertEquals(54, genTypes.size());
        assertNotNull(genTypes);

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
            if ("InterfaceKey".equals(name)) {
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
        assertEquals("java.lang.Object", condLeafRT.getFullyQualifiedName());

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
            if (method.getName().equals("getKey")) {
                getIfcKey = method;
            } else if (method.getName().equals("getHigherLayerIf")) {
                getHigherLayerIf = method;
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
        assertEquals("List", getHigherLayerIfType.getName());

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
            if (method.getName().equals("getKey")) {
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
    public void testLeafrefInvalidPathResolving() throws URISyntaxException, IOException {
        final URI resource = getClass().getResource("/leafref-test-invalid-model/foo.yang").toURI();
        assertNotNull(resource);

        final SchemaContext context = resolveSchemaContextFromFiles(resource);
        assertNotNull(context);
        assertEquals(1, context.getModules().size());

        final BindingGenerator bindingGen = new BindingGeneratorImpl();
        try {
            bindingGen.generateTypes(context);
            fail("Expected IllegalArgumentException caused by invalid leafref path");
        } catch (IllegalArgumentException e) {
            String expected = "Failed to find leafref target: ../id";
            assertEquals(expected, e.getMessage());
        }
    }

}