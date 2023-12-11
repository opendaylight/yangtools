/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.opendaylight.yangtools.yang.common.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangNetconfError;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.util.codec.NormalizedNodeParser;
import org.opendaylight.yangtools.yang.data.util.codec.NormalizedNodeParserException;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class NormalizedNodeParserTest {
    private static final EffectiveModelContext MODEL_CONTEXT = YangParserTestUtils.parseYang("""
            module foo {
              prefix foo;
              namespace foo;

              container foo {
                leaf str {
                  type string {
                    length 3;
                  }
                }
              }

              container baz {
                leaf uint {
                  type uint32;
                }
              }

              list xyzzy {
                key "one two";
                leaf one {
                  type boolean;
                }
                leaf two {
                  type string;
                }
              }
            }""");
    private static final NormalizedNodeParser PARSER = JSONCodecFactorySupplier.RFC7951.getShared(MODEL_CONTEXT);
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAR = QName.create("foo", "bar");
    private static final QName STR = QName.create("foo", "str");

    @Test
    void testParseData() throws Exception {
        assertEquals(Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(FOO))
            .withChild(ImmutableNodes.leafNode(STR, "str"))
            .build(),
            PARSER.parseData(Inference.ofDataTreePath(MODEL_CONTEXT, FOO), stream("""
                {
                  "foo:foo" : {
                    "str" : "str"
                  }
                }""")));
    }

    @Test
    void testParseDataBadType() throws Exception {
        final var error = assertError(() -> PARSER.parseData(Inference.ofDataTreePath(MODEL_CONTEXT, FOO), stream("""
            {
              "foo:foo" : {
                "str" : "too long"
              }
            }""")));
        assertEquals(ErrorType.APPLICATION, error.type());
        assertEquals(ErrorTag.INVALID_VALUE, error.tag());
    }

    @Test
    void testParseDataBadRootElement() throws Exception {
        final var error = assertError(() -> PARSER.parseData(Inference.ofDataTreePath(MODEL_CONTEXT, FOO), stream("""
            {
              "foo:baz" : {
                "uint" : 23
              }
            }""")));
        assertEquals(ErrorType.PROTOCOL, error.type());
        assertEquals(ErrorTag.MALFORMED_MESSAGE, error.tag());
        assertEquals("Payload name (foo)baz is different from identifier name (foo)foo", error.message());
    }

    private static @NonNull InputStream stream(final String str) {
        return new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
    }

    private static YangNetconfError assertError(final Executable executable) {
        final var ex = assertThrows(NormalizedNodeParserException.class, executable);
        final var errors = ex.getNetconfErrors();
        assertEquals(1, errors.size());
        final var error = errors.get(0);
        assertEquals(ErrorSeverity.ERROR, error.severity());
        return error;
    }
}
