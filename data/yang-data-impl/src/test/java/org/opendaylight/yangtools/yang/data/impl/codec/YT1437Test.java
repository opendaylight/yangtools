/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT1437Test {
    private static final String YT_1437_YANG = """
            module yt1437 {
              namespace yt1437;
              prefix yt1437;
              leaf foo {
                type decimal64 {
                  fraction-digits 2;
                  range "20.0..30.01 | 50";
                }
              }
            }""";

    @Test
    public void testDecimalFractionDigits() {
        final var module = YangParserTestUtils.parseYang(YT_1437_YANG).findModule("yt1437").orElseThrow();
        final var foo = module.findDataChildByName(QName.create(module.getQNameModule(), "foo")).orElseThrow();
        assertThat(foo, instanceOf(LeafSchemaNode.class));

        final TypeDefinitionAwareCodec<?, ?> codec = TypeDefinitionAwareCodec.from(((LeafSchemaNode) foo).getType());
        assertThat(codec, instanceOf(DecimalStringCodec.class));
        final var cast = (DecimalStringCodec) codec;

        final var one = cast.deserialize("20.0");
        assertEquals(2, one.scale());
        assertEquals("20.0", cast.serialize(one));

        final var two = cast.deserialize("20.00");
        assertEquals(2, two.scale());
        assertEquals("20.0", cast.serialize(two));

        final var three = cast.deserialize("20.000");
        assertEquals(2, three.scale());
        assertEquals("20.0", cast.serialize(three));
    }
}
