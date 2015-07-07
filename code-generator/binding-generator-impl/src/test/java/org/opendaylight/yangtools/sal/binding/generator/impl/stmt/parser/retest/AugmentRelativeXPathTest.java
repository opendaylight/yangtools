/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl.stmt.parser.retest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

import org.opendaylight.yangtools.sal.binding.generator.impl.BindingGeneratorImpl;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class AugmentRelativeXPathTest extends AbstractTypesTest {

    public AugmentRelativeXPathTest() {
        super(AugmentRelativeXPathTest.class.getResource("/augment-relative-xpath-models"));
    }

    @Test
    public void AugmentationWithRelativeXPathTest() throws IOException, SourceException, ReactorException {

        final SchemaContext context = RetestUtils.parseYangSources(testModels);

        assertNotNull("context is null", context);
        final BindingGenerator bindingGen = new BindingGeneratorImpl(true);
        final List<Type> genTypes = bindingGen.generateTypes(context);

        assertNotNull("genTypes is null", genTypes);
        assertFalse("genTypes is empty", genTypes.isEmpty());

        GeneratedTransferObject gtInterfaceKey = null;
        GeneratedType gtInterface = null;
        GeneratedType gtTunnel = null;
        GeneratedTransferObject gtTunnelKey = null;

        for (final Type type : genTypes) {
            if (type.getName().equals("InterfaceKey") && type.getPackageName().contains("augment._abstract.topology")) {
                gtInterfaceKey = (GeneratedTransferObject) type;
            } else if (type.getName().equals("Interface")
                    && type.getPackageName().contains("augment._abstract.topology")) {
                gtInterface = (GeneratedType) type;
            } else if (type.getName().equals("Tunnel") && type.getPackageName().contains("augment._abstract.topology")) {
                gtTunnel = (GeneratedType) type;
            } else if (type.getName().equals("TunnelKey")
                    && type.getPackageName().contains("augment._abstract.topology")) {
                gtTunnelKey = (GeneratedTransferObject) type;
            }
        }

        // 'Interface
        assertNotNull("Interface is null", gtInterface);
        final List<MethodSignature> gtInterfaceMethods = gtInterface.getMethodDefinitions();
        assertNotNull("Interface methods are null", gtInterfaceMethods);
        MethodSignature getIfcKeyMethod = null;
        for (final MethodSignature method : gtInterfaceMethods) {
            if (method.getName().equals("getKey")) {
                getIfcKeyMethod = method;
                break;
            }
        }
        assertNotNull("getKey method is null", getIfcKeyMethod);
        assertNotNull("getKey method return type is null", getIfcKeyMethod.getReturnType());
        assertTrue("getKey method return type name must be InterfaceKey", getIfcKeyMethod.getReturnType().getName()
                .equals("InterfaceKey"));

        // 'InterfaceKey'
        assertNotNull("InterfaceKey is null", gtInterfaceKey);
        final List<GeneratedProperty> properties = gtInterfaceKey.getProperties();
        assertNotNull("InterfaceKey properties are null", properties);
        GeneratedProperty gtInterfaceId = null;
        for (final GeneratedProperty property : properties) {
            if (property.getName().equals("interfaceId")) {
                gtInterfaceId = property;
                break;
            }
        }
        assertNotNull("interfaceId is null", gtInterfaceId);
        assertNotNull("interfaceId return type is null", gtInterfaceId.getReturnType());
        assertTrue("interfaceId return type name must be String",
                gtInterfaceId.getReturnType().getName().equals("String"));

        // 'Tunnel'
        assertNotNull("Tunnel is null", gtTunnel);
        final List<MethodSignature> tunnelMethods = gtTunnel.getMethodDefinitions();
        assertNotNull("Tunnel methods are null", tunnelMethods);
        MethodSignature getTunnelKeyMethod = null;
        for (MethodSignature method : tunnelMethods) {
            if (method.getName().equals("getKey")) {
                getTunnelKeyMethod = method;
                break;
            }
        }
        assertNotNull("getKey method is null", getTunnelKeyMethod);
        assertNotNull("getKey method return type", getTunnelKeyMethod.getReturnType());
        assertTrue("getKey method return type name must be TunnelKey", getTunnelKeyMethod.getReturnType().getName()
                .equals("TunnelKey"));

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
        assertNotNull("tunnelId return type is null", gtTunnelId.getReturnType());
        assertTrue("tunnelId return type name must be Uri", gtTunnelId.getReturnType().getName().equals("Uri"));
    }

}
