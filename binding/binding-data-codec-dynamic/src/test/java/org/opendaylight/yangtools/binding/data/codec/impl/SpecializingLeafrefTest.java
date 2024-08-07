/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.mdsal426.norev.BarCont;
import org.opendaylight.yang.gen.v1.mdsal426.norev.BarContBuilder;
import org.opendaylight.yang.gen.v1.mdsal426.norev.BooleanCont;
import org.opendaylight.yang.gen.v1.mdsal426.norev.BooleanContBuilder;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;

public class SpecializingLeafrefTest extends AbstractBindingCodecTest {
    private static final DataObjectIdentifier<BooleanCont> BOOLEAN_CONT_II =
        DataObjectIdentifier.builder(BooleanCont.class).build();

    private static final DataObjectIdentifier<BarCont> BAR_CONT_II =
        DataObjectIdentifier.builder(BarCont.class).build();

    @Test
    public void specifiedBooleanLeafTest() {
        final BooleanCont booleanCont  = new BooleanContBuilder().setIsFoo(true).build();

        final var res = codecContext.toNormalizedDataObject(BOOLEAN_CONT_II, booleanCont);

        final var booleanContBinding = (BooleanCont) codecContext.fromNormalizedNode(res.path(), res.node()).getValue();

        assertTrue(booleanContBinding.getIsFoo());
    }

    @Test
    public void specifiedCommonLeafTest() {
        final BarCont barCont  = new BarContBuilder().setLeaf2("foo").build();

        final var res = codecContext.toNormalizedDataObject(BAR_CONT_II, barCont);

        final var booleanContBinding = (BarCont) codecContext.fromNormalizedNode(res.path(), res.node()).getValue();

        assertEquals(booleanContBinding.getLeaf2(), "foo");
    }

    @Test
    public void specifiedLeafListTest() {
        final Set<String> testSet = Set.of("test");
        final BarCont barCont  = new BarContBuilder().setLeafList1(testSet).build();

        final var res = codecContext.toNormalizedDataObject(BAR_CONT_II, barCont);

        final var barContAfterConverting = (BarCont) codecContext.fromNormalizedNode(res.path(), res.node()).getValue();

        assertEquals(barContAfterConverting.getLeafList1(), testSet);
    }
}
