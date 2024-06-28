/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.bug8449.rev170516.Cont;
import org.opendaylight.yang.gen.v1.bug8449.rev170516.Cont.Ref;
import org.opendaylight.yang.gen.v1.bug8449.rev170516.ContBuilder;
import org.opendaylight.yang.gen.v1.bug8449.rev170516.ContInt32;
import org.opendaylight.yang.gen.v1.bug8449.rev170516.ContInt32.RefUnionInt32;
import org.opendaylight.yang.gen.v1.bug8449.rev170516.ContInt32Builder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class LeafrefSerializeDeserializeTest extends AbstractBindingCodecTest {
    @Test
    public void listReferenceTest() {
        final var contYII = YangInstanceIdentifier.builder().node(Cont.QNAME).build();
        final var fromYangInstanceIdentifier = codecContext.fromYangInstanceIdentifier(contYII);
        assertNotNull(fromYangInstanceIdentifier);

        final var BA_II_CONT = InstanceIdentifier.builder(Cont.class).build();
        final var refVal = new Ref("myvalue");
        final var data = new ContBuilder().setRef(refVal).build();
        final var normalizedNode = codecContext.toNormalizedDataObject(BA_II_CONT, data);
        assertNotNull(normalizedNode);

        final var fromNormalizedNode = codecContext.fromNormalizedNode(contYII, normalizedNode.node());
        assertNotNull(fromNormalizedNode);
        final var value = (Cont) fromNormalizedNode.getValue();
        assertEquals(refVal, value.getRef());
    }

    @Test
    public void uint32LeafrefTest() {
        final var contYII = YangInstanceIdentifier.builder().node(ContInt32.QNAME).build();
        final var fromYangInstanceIdentifier = codecContext.fromYangInstanceIdentifier(contYII);
        assertNotNull(fromYangInstanceIdentifier);

        final var BA_II_CONT = InstanceIdentifier.builder(ContInt32.class).build();
        final var refVal = new RefUnionInt32(Uint32.valueOf(5));
        final var data = new ContInt32Builder().setRefUnionInt32(refVal).build();
        final var normalizedNode = codecContext.toNormalizedDataObject(BA_II_CONT, data);
        assertNotNull(normalizedNode);

        final var fromNormalizedNode = codecContext.fromNormalizedNode(contYII, normalizedNode.node());
        assertNotNull(fromNormalizedNode);
        final var value = (ContInt32) fromNormalizedNode.getValue();
        assertEquals(refVal, value.getRefUnionInt32());
    }
}

