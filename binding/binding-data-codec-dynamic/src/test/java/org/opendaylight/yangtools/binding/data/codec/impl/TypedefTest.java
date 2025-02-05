/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.bug8903.rev170829.DefaultPolicy;
import org.opendaylight.yang.gen.v1.bug8903.rev170829.DefaultPolicyBuilder;
import org.opendaylight.yang.gen.v1.bug8903.rev170829.PolicyLoggingFlag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.typedef.empty.rev170829.TestCont;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.typedef.empty.rev170829.TestContBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.typedef.empty.rev170829.TypedefEmpty;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.Empty;

class TypedefTest extends AbstractBindingCodecTest {
    private static final DataObjectIdentifier<DefaultPolicy> BA_DEFAULT_POLICY =
        DataObjectIdentifier.builder(DefaultPolicy.class).build();
    private static final DataObjectIdentifier<TestCont> BA_TEST_CONT =
        DataObjectIdentifier.builder(TestCont.class).build();

    @Test
    void testTypedef() {
        final var binding = new DefaultPolicyBuilder()
                .setAction(true)
                .setAction2(new PolicyLoggingFlag(false))
                .setAction3(true)
                .build();
        final var dom = codecContext.toNormalizedDataObject(BA_DEFAULT_POLICY, binding);
        final var read = codecContext.fromNormalizedNode(dom.path(),dom.node());

        assertEquals(binding, read.getValue());
    }

    @Test
    void testTypedefEmptyType() {
        final var binding = new TestContBuilder()
                .setEmptyLeaf(Empty.value())
                .setEmptyLeaf2(new TypedefEmpty(Empty.value()))
                .setEmptyLeaf3(Empty.value())
                .build();
        final var dom = codecContext.toNormalizedDataObject(BA_TEST_CONT, binding);
        final var read = codecContext.fromNormalizedNode(dom.path(),dom.node());

        assertEquals(binding, read.getValue());
    }
}
