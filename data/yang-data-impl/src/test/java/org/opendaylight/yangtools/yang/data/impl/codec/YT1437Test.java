/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.spi.codec.DecimalStringCodec;
import org.opendaylight.yangtools.yang.data.spi.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1437Test {
    @Test
    void testDecimalFractionDigits() {
        final var module = YangParserTestUtils.parseYang("""
            module yt1437 {
              namespace yt1437;
              prefix yt1437;

              leaf foo {
                type decimal64 {
                  fraction-digits 2;
                  range "20.0..30.01 | 50";
                }
              }
            }""").findModule("yt1437").orElseThrow();
        final var foo = module.findDataChildByName(QName.create(module.getQNameModule(), "foo")).orElseThrow();

        final var codec = assertInstanceOf(DecimalStringCodec.class,
            TypeDefinitionAwareCodec.from(assertInstanceOf(LeafSchemaNode.class, foo).getType()));

        final var one = codec.deserialize("20.0");
        assertEquals(2, one.scale());
        assertEquals("20.0", codec.serialize(one));

        final var two = codec.deserialize("20.00");
        assertEquals(2, two.scale());
        assertEquals("20.0", codec.serialize(two));

        final var three = codec.deserialize("20.000");
        assertEquals(2, three.scale());
        assertEquals("20.0", codec.serialize(three));
    }
}
