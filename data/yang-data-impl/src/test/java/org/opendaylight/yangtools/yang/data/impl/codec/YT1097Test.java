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
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT1097Test {
    private static final String YT_1097_YANG = """
        module yt1097 {
          namespace yt1097;
          prefix yt1097;
          leaf foo {
            type union {
              type boolean;
              type string;
            }
          }
        }""";

    @Test
    public void testBooleanStringUnion() {
        final Module module = YangParserTestUtils.parseYang(YT_1097_YANG).findModule("yt1097").orElseThrow();
        final DataSchemaNode foo = module.findDataChildByName(QName.create(module.getQNameModule(), "foo"))
                .orElseThrow();
        assertThat(foo, instanceOf(LeafSchemaNode.class));

        final TypeDefinitionAwareCodec<?, ?> codec = TypeDefinitionAwareCodec.from(((LeafSchemaNode) foo).getType());
        assertThat(codec, instanceOf(UnionStringCodec.class));

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
        final Object result = codec.deserialize(input);
        assertThat(result, instanceOf(expected.getClass()));
        assertEquals(expected, result);
    }
}
