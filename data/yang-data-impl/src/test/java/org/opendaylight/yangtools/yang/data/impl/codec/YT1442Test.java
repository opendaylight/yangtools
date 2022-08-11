/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT1442Test {
    @Test
    public void testDecimalStringCodecRangeConstraints() {
        final var module = YangParserTestUtils.parseYangResource("/yt1442.yang").findModule("yt1442").orElseThrow();
        final var foo = module.findDataChildByName(QName.create(module.getQNameModule(), "foo")).orElseThrow();
        assertThat(foo, instanceOf(LeafSchemaNode.class));

        final TypeDefinitionAwareCodec<?, ?> codec = TypeDefinitionAwareCodec.from(((LeafSchemaNode) foo).getType());
        assertThat(codec, instanceOf(DecimalStringCodec.class));
        final var cast = (DecimalStringCodec) codec;

        //Checking numbers withing range
        final var ten = cast.deserialize("10.00");
        assertEquals("10.0", cast.serialize(ten));

        final var fifty = cast.deserialize("50.00");
        assertEquals("50.0", cast.serialize(fifty));

        final var hundred = cast.deserialize("100.00");
        assertEquals("100.0", cast.serialize(hundred));

        //Checking numbers just out of range
        assertThrows(IllegalArgumentException.class, () -> cast.deserialize("9.99"));
        assertThrows(IllegalArgumentException.class, () -> cast.deserialize("100.01"));

        //Now checking with wrong scale
        assertThrows(IllegalArgumentException.class, () -> cast.deserialize("100.001"));

        //Now checking withing range but negative
        assertThrows(IllegalArgumentException.class, () -> cast.deserialize("-10.00"));
        assertThrows(IllegalArgumentException.class, () -> cast.deserialize("-50.00"));
        assertThrows(IllegalArgumentException.class, () -> cast.deserialize("-100.00"));
    }
}
