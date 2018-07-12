/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.test;

import static org.junit.Assert.assertEquals;

import java.util.Map.Entry;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.bug8903.rev170829.DefaultPolicy;
import org.opendaylight.yang.gen.v1.bug8903.rev170829.DefaultPolicyBuilder;
import org.opendaylight.yang.gen.v1.bug8903.rev170829.PolicyLoggingFlag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.typedef.empty.rev170829.TestCont;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.typedef.empty.rev170829.TestContBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.typedef.empty.rev170829.TypedefEmpty;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;


public class TypedefTest extends AbstractBindingCodecTest {

    private static final InstanceIdentifier<DefaultPolicy> BA_DEFAULT_POLICY =
            InstanceIdentifier.builder(DefaultPolicy.class).build();
    private static final InstanceIdentifier<TestCont> BA_TEST_CONT =
            InstanceIdentifier.builder(TestCont.class).build();

    @Test
    public void testTypedef() {
        DefaultPolicy binding = new DefaultPolicyBuilder()
                .setAction(true)
                .setAction2(new PolicyLoggingFlag(false))
                .setAction3(true)
                .build();
        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> dom =
                registry.toNormalizedNode(BA_DEFAULT_POLICY, binding);
        final Entry<InstanceIdentifier<?>, DataObject> readed =
                registry.fromNormalizedNode(dom.getKey(),dom.getValue());

        assertEquals(binding,readed.getValue());

    }

    @Test
    public void testTypedefEmptyType() {
        TestCont binding = new TestContBuilder()
                .setEmptyLeaf(true)
                .setEmptyLeaf2(new TypedefEmpty(true))
                .setEmptyLeaf3(true)
                .build();
        final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> dom =
                registry.toNormalizedNode(BA_TEST_CONT, binding);
        final Entry<InstanceIdentifier<?>, DataObject> readed =
                registry.fromNormalizedNode(dom.getKey(),dom.getValue());

        assertEquals(binding,readed.getValue());

    }
}
