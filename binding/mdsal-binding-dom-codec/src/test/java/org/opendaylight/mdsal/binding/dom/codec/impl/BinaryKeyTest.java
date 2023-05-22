/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer.NodeResult;
import org.opendaylight.yang.gen.v1.odl.test.binary.key.rev160101.BinaryList;
import org.opendaylight.yang.gen.v1.odl.test.binary.key.rev160101.BinaryListBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BinaryKeyTest extends AbstractBindingCodecTest {
    private final InstanceIdentifier<BinaryList> instanceIdentifier = InstanceIdentifier.create(BinaryList.class);

    @Test
    public void binaryKeyTest() {
        final byte[] binaryKey1 = {1, 1, 1};
        final byte[] binaryKey2 = {1};
        final BinaryList binaryList1 = new BinaryListBuilder()
                .setBinaryItem("first")
                .setBinaryKey(binaryKey1)
                .build();
        final BinaryList binaryList2 = new BinaryListBuilder()
                .setBinaryItem("second")
                .setBinaryKey(binaryKey2)
                .build();
        final BinaryList processedBinaryList1 = process(binaryList1);
        final BinaryList processedBinaryList2 = process(binaryList2);

        assertEquals(binaryList1, processedBinaryList1);
        assertEquals(binaryList1, binaryList1);
        assertEquals(processedBinaryList1, processedBinaryList1);

        assertNotEquals(binaryList1, processedBinaryList2);
        assertNotEquals(binaryList1, binaryList2);
        assertNotEquals(processedBinaryList1, processedBinaryList2);
    }

    private BinaryList process(final BinaryList binaryList) {
        final var entry = (NodeResult) codecContext.toNormalizedNode(instanceIdentifier, binaryList);
        return (BinaryList) codecContext.fromNormalizedNode(entry.path(), entry.node()).getValue();
    }
}
