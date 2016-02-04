/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.util.Uint16;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
/**
 * Test suite for increasing of test coverage of RpcDefinitionBuilder implementation.
 *
 * @see org.opendaylight.yangtools.yang.parser.builder.impl.RpcDefinitionBuilder
 *
 * @author Lukas Sedlak &lt;lsedlak@cisco.com&gt;
 *
 * @deprecated Pre-Beryllium implementation, scheduled for removal.
 */
@Deprecated
public class RpcDefinitionBuilderTest extends AbstractBuilderTest {

    @Test
    public void testBuild() {
        final QName rpcQName = QName.create(module.getNamespace(), module.getRevision(), "send-message");
        SchemaPath rpcPath = SchemaPath.create(true, rpcQName);
        final RpcDefinitionBuilder rpcBuilder = new RpcDefinitionBuilder(module.getModuleName(), 10, rpcQName, rpcPath);

        final QName testTypedef = QName.create(module.getNamespace(), module.getRevision(), "test-typedef");
        final SchemaPath testTypedefPath = SchemaPath.create(true, rpcQName, testTypedef);
        final TypeDefinitionBuilder typedefBuilder = new TypeDefinitionBuilderImpl(module.getModuleName(),
            12, testTypedef, testTypedefPath);
        typedefBuilder.setType(Uint16.getInstance());

        rpcBuilder.addTypedef(typedefBuilder);

        final QName testGroup = QName.create(module.getNamespace(), module.getRevision(), "test-group");
        final SchemaPath groupPath = SchemaPath.create(true, rpcQName, testGroup);
        final GroupingBuilder groupBuilder = new GroupingBuilderImpl(module.getModuleName(), 15, testGroup, groupPath);

        rpcBuilder.addGrouping(groupBuilder);

        final QName unknownNode = QName.create(module.getNamespace(), module.getRevision(), "unknown-ext-use");
        final SchemaPath unknownNodePath = SchemaPath.create(true, rpcQName, unknownNode);
        final UnknownSchemaNodeBuilder unknownNodeBuilder =  new UnknownSchemaNodeBuilderImpl(module.getModuleName(),
            25, unknownNode, unknownNodePath);

        rpcBuilder.addUnknownNodeBuilder(unknownNodeBuilder);

        final RpcDefinitionBuilder rpcBuilder2 = new RpcDefinitionBuilder(module.getModuleName(), 10, rpcQName, rpcPath);
        final RpcDefinitionBuilder rpcBuilder3 = new RpcDefinitionBuilder(module.getModuleName(), 10, unknownNode, rpcPath);
        final RpcDefinitionBuilder rpcBuilder4 = new RpcDefinitionBuilder(module.getModuleName(), 10, rpcQName, unknownNodePath);

        assertEquals(rpcBuilder, rpcBuilder2);
        assertNotEquals(rpcBuilder, null);
        assertNotEquals(rpcBuilder, rpcBuilder3);
        assertNotEquals(rpcBuilder, rpcBuilder4);

        assertEquals(rpcBuilder.toString(), "rpc send-message");

        final RpcDefinition instance = rpcBuilder.build();

        assertNotNull(instance);

        final RpcDefinition sameInstance = rpcBuilder.build();
        assertTrue(sameInstance == instance);
        assertEquals(sameInstance, instance);

        assertEquals(instance.getPath(), rpcPath);
        assertEquals(instance.getStatus(), Status.CURRENT);
        assertNull(instance.getOutput());
        assertFalse(instance.getTypeDefinitions().isEmpty());
        assertFalse(instance.getGroupings().isEmpty());
        assertFalse(instance.getUnknownSchemaNodes().isEmpty());

        assertEquals(instance.getTypeDefinitions().size(), 1);
        assertEquals(instance.getGroupings().size(), 1);
        assertEquals(instance.getUnknownSchemaNodes().size(), 1);

        RpcDefinition instance2 = rpcBuilder2.build();
        RpcDefinition instance3 = rpcBuilder3.build();
        RpcDefinition instance4 = rpcBuilder4.build();

        assertEquals(instance, instance2);
        assertNotEquals(instance, null);
        assertNotEquals(instance, instance3);
        assertNotEquals(instance, instance4);

        assertEquals(instance.toString(), "RpcDefinitionImpl[qname=(urn:opendaylight.rpc:def:test-model?" +
            "revision=2014-01-06)send-message, path=AbsoluteSchemaPath{path=[(urn:opendaylight.rpc:def:test-model?" +
            "revision=2014-01-06)send-message]}, input=null, output=null]");
    }
}
