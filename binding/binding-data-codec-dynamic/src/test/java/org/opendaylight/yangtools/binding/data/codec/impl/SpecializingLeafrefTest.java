/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.mdsal426.norev.BarCont;
import org.opendaylight.yang.gen.v1.mdsal426.norev.BarContBuilder;
import org.opendaylight.yang.gen.v1.mdsal426.norev.BooleanCont;
import org.opendaylight.yang.gen.v1.mdsal426.norev.BooleanContBuilder;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;

@NonNullByDefault
class SpecializingLeafrefTest extends AbstractBindingCodecTest {
    private static final DataObjectIdentifier<BooleanCont> BOOLEAN_CONT_II =
        DataObjectIdentifier.builder(BooleanCont.class).build();
    private static final DataObjectIdentifier<BarCont> BAR_CONT_II =
        DataObjectIdentifier.builder(BarCont.class).build();

    @Test
    void specifiedBooleanLeafTest() {
        final var booleanCont  = new BooleanContBuilder().setIsFoo(true).build();

        final var res = codecContext.toNormalizedDataObject(BOOLEAN_CONT_II, booleanCont);
        final var entry = codecContext.fromNormalizedNode(res.path(), res.node());
        assertNotNull(entry);

        final var booleanContBinding = assertInstanceOf(BooleanCont.class, entry.getValue());
        assertEquals(Boolean.TRUE, booleanContBinding.getIsFoo());
    }

    @Test
    void specifiedCommonLeafTest() {
        final var barCont  = new BarContBuilder().setLeaf2("foo").build();

        final var res = codecContext.toNormalizedDataObject(BAR_CONT_II, barCont);
        final var entry = codecContext.fromNormalizedNode(res.path(), res.node());
        assertNotNull(entry);

        final var booleanContBinding = assertInstanceOf(BarCont.class, entry.getValue());
        assertEquals(booleanContBinding.getLeaf2(), "foo");
    }

    @Test
    public void specifiedLeafListTest() {
        final var testSet = Set.of("test");
        final var barCont  = new BarContBuilder().setLeafList1(testSet).build();

        final var res = codecContext.toNormalizedDataObject(BAR_CONT_II, barCont);
        final var entry = codecContext.fromNormalizedNode(res.path(), res.node());
        assertNotNull(entry);

        final var barContAfterConverting = assertInstanceOf(BarCont.class, entry.getValue());
        assertEquals(barContAfterConverting.getLeafList1(), testSet);
    }
}
