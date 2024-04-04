/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.codec.YangInvalidValueException;
import org.opendaylight.yangtools.yang.data.spi.codec.DecimalStringCodec;
import org.opendaylight.yangtools.yang.data.spi.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1442Test {
    private static DecimalStringCodec codec;

    @BeforeAll
    static void beforeClass() {
        final var foo = YangParserTestUtils.parseYang("""
            module yt1442 {
              namespace yt1442;
              prefix yt1442;

              leaf foo {
                type decimal64 {
                  fraction-digits 2;
                  range 10.0..100.0 {
                    error-app-tag model-defined-app-tag;
                    error-message model-defined-message;
                  }
                }
              }
            }""")
            .getDataChildByName(QName.create("yt1442", "foo"));
        codec = assertInstanceOf(DecimalStringCodec.class,
            TypeDefinitionAwareCodec.from(assertInstanceOf(LeafSchemaNode.class, foo).getType()));
    }

    @Test
    void testTen() {
        final var ten = codec.deserialize("10.00");
        assertDecimal(1000, ten);
        assertEquals("10.0", codec.serialize(ten));
    }

    @Test
    void testFifty() {
        final var fifty = codec.deserialize("50.00");
        assertDecimal(5000, fifty);
        assertEquals("50.0", codec.serialize(fifty));
    }

    @Test
    void testHundred() {
        final var hundred = codec.deserialize("100.00");
        assertDecimal(10000, hundred);
        assertEquals("100.0", codec.serialize(hundred));
    }

    @Test
    void testNegativeOutOfRange() {
        assertYIVE("Value '-10.0' is not in required ranges [[10.0..100.0]]", "-10.00");
        assertYIVE("Value '-50.0' is not in required ranges [[10.0..100.0]]", "-50.00");
        assertYIVE("Value '-100.0' is not in required ranges [[10.0..100.0]]", "-100.00");
    }

    @Test
    void testPositiveOutOfRange() {
        assertYIVE("Value '9.99' is not in required ranges [[10.0..100.0]]", "9.99");
        assertYIVE("Value '100.01' is not in required ranges [[10.0..100.0]]", "100.01");
    }

    @Test
    void testTooLargeFractionPart() {
        final var ex = assertThrows(IllegalArgumentException.class, () -> codec.deserialize("100.001"));
        assertEquals("Value '100.001' does not match required fraction-digits", ex.getMessage());
        final var cause = assertInstanceOf(ArithmeticException.class, ex.getCause());
        assertEquals("Decreasing scale of 100.001 to 2 requires rounding", cause.getMessage());
    }

    @Test
    void testTooLargeIntegralPart() {
        final var ex = assertThrows(IllegalArgumentException.class, () -> codec.deserialize("92233720368547759.0"));
        assertEquals("Value '92233720368547759.0' does not match required fraction-digits", ex.getMessage());
        final var cause = assertInstanceOf(ArithmeticException.class, ex.getCause());
        assertEquals("Increasing scale of 92233720368547759.0 to 2 would overflow", cause.getMessage());
    }

    private static void assertDecimal(final long unscaledValue, final Object obj) {
        final var actual = assertInstanceOf(Decimal64.class, obj);
        assertEquals(2, actual.scale());
        assertEquals(unscaledValue, actual.unscaledValue());
    }

    private static void assertYIVE(final String expectedMessage, final @NonNull String input) {
        final var ex = assertThrows(YangInvalidValueException.class, () -> codec.deserialize(input));
        assertEquals(expectedMessage, ex.getMessage());

        final var errors = ex.getNetconfErrors();
        assertEquals(1, errors.size());
        final var error = errors.get(0);
        assertEquals(ErrorSeverity.ERROR, error.severity());
        assertEquals(ErrorType.APPLICATION, error.type());
        assertEquals(ErrorTag.INVALID_VALUE, error.tag());
        assertEquals("model-defined-app-tag", error.appTag());
        assertEquals("model-defined-message", error.message());
        assertEquals(List.of(), error.info());
    }
}
