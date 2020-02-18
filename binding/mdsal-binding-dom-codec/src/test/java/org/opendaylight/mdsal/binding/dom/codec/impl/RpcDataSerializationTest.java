/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.GetTopOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.GetTopOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.PutTopInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.PutTopInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class RpcDataSerializationTest extends AbstractBindingCodecTest {

    private static final QName PUT_TOP = QName.create(PutTopInput.QNAME, "put-top");
    private static final QName GET_TOP = QName.create(GetTopOutput.QNAME, "get-top");

    private static final SchemaPath PUT_TOP_INPUT = SchemaPath.create(true, PUT_TOP, PutTopInput.QNAME);
    private static final SchemaPath GET_TOP_OUTPUT = SchemaPath.create(true, GET_TOP, GetTopOutput.QNAME);

    private static final TopLevelListKey LIST_KEY = new TopLevelListKey("test");

    @Test
    public void testRpcInputToNormalized() {
        final PutTopInput bindingOriginal = new PutTopInputBuilder()
                .setTopLevelList(ImmutableMap.of(LIST_KEY, new TopLevelListBuilder().withKey(LIST_KEY).build()))
                .build();
        final ContainerNode dom = registry.toNormalizedNodeRpcData(bindingOriginal);
        assertNotNull(dom);
        assertEquals(PutTopInput.QNAME, dom.getIdentifier().getNodeType());

        final DataObject bindingDeserialized = registry.fromNormalizedNodeRpcData(PUT_TOP_INPUT, dom);
        assertEquals(bindingOriginal, bindingDeserialized);
    }

    @Test
    public void testRpcOutputToNormalized() {
        final GetTopOutput bindingOriginal = new GetTopOutputBuilder()
                .setTopLevelList(ImmutableMap.of(LIST_KEY, new TopLevelListBuilder().withKey(LIST_KEY).build()))
                .build();
        final ContainerNode dom = registry.toNormalizedNodeRpcData(bindingOriginal);
        assertNotNull(dom);
        assertEquals(GetTopOutput.QNAME, dom.getIdentifier().getNodeType());

        final DataObject bindingDeserialized = registry.fromNormalizedNodeRpcData(GET_TOP_OUTPUT, dom);
        assertEquals(bindingOriginal, bindingDeserialized);
    }
}
