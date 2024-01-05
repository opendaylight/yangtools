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
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1097Test {
    @Test
    void testBooleanStringUnion() {
        final var module = YangParserTestUtils.parseYang("""
            module yt1097 {
              namespace yt1097;
              prefix yt1097;

              leaf foo {
                type union {
                  type boolean;
                  type string;
                }
              }
            }""").findModule("yt1097").orElseThrow();
        final var foo = assertInstanceOf(LeafSchemaNode.class,
            module.dataChildByName(QName.create(module.getQNameModule(), "foo")));

        final var codec = assertInstanceOf(UnionStringCodec.class,
            UnionStringCodec.from(assertInstanceOf(UnionTypeDefinition.class, foo.getType())));
        assertDecoded(codec, Boolean.TRUE, "true");
        assertDecoded(codec, Boolean.FALSE, "false");
        assertDecoded(codec, "True");
        assertDecoded(codec, "TRUE");
        assertDecoded(codec, "False");
        assertDecoded(codec, "FALSE");
    }

    private static void assertDecoded(final TypeDefinitionAwareCodec<?, ?> codec, final String input) {
        assertDecoded(codec, input, input);
    }

    private static void assertDecoded(final TypeDefinitionAwareCodec<?, ?> codec, final Object expected,
            final String input) {
        assertEquals(expected, assertInstanceOf(expected.getClass(), codec.deserialize(input)));
    }
}
