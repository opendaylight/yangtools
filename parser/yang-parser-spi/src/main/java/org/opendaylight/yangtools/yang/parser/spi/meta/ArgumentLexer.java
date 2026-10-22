/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;
import java.util.stream.IntStream;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

@NonNullByDefault
public abstract sealed class ArgumentLexer<A> {

    public abstract static sealed class OfNumber<A extends Number> extends ArgumentLexer<A> {
        OfNumber(final Class<A> argumentRepresentation) {
            super(argumentRepresentation);
        }
    }

    public static final class OfDecimal64 extends OfNumber<Decimal64> {
        private static final OfDecimal64[] INSTANCES = IntStream.rangeClosed(1, 18)
            .mapToObj(OfDecimal64::new)
            .toArray(OfDecimal64[]::new);

        private final byte fractionDigits;

        private OfDecimal64(final int fractionDigits) {
            super(Decimal64.class);
            this.fractionDigits = (byte) fractionDigits;
        }

        public static OfDecimal64 of(final int fractionDigits) {
            try {
                return INSTANCES[fractionDigits - 1];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("bad fraction digits " + fractionDigits, e);
            }
        }

        @Override
        public Decimal64 lexArgument(final String rawArgument) {
            return Decimal64.valueOf(rawArgument).scaleTo(fractionDigits);
        }
    }

    public static final class OfInteger<A extends Number> extends OfNumber<A> {
        public static final OfInteger<Byte> INT8 = new OfInteger<>(Byte.class, Byte::valueOf);
        public static final OfInteger<Short> INT16 = new OfInteger<>(Short.class, Short::valueOf);
        public static final OfInteger<Integer> INT32 = new OfInteger<>(Integer.class, Integer::valueOf);
        public static final OfInteger<Long> INT64 = new OfInteger<>(Long.class, Long::valueOf);
        public static final OfInteger<Uint8> UINT8 = new OfInteger<>(Uint8.class, Uint8::valueOf);
        public static final OfInteger<Uint16> UINT16 = new OfInteger<>(Uint16.class, Uint16::valueOf);
        public static final OfInteger<Uint32> UINT32 = new OfInteger<>(Uint32.class, Uint32::valueOf);
        public static final OfInteger<Uint64> UINT64 = new OfInteger<>(Uint64.class, Uint64::valueOf);

        private final Function<String, A> lexer;

        private OfInteger(final Class<A> argumentRepresentation, final Function<String, A> lexer) {
            super(argumentRepresentation);
            this.lexer = requireNonNull(lexer);
        }

        @Override
        public A lexArgument(final String rawArgument) {
            return lexer.apply(rawArgument);
        }
    }

    private final Class<A> argumentRepresentation;

    ArgumentLexer(final Class<A> argumentRepresentation) {
        this.argumentRepresentation = requireNonNull(argumentRepresentation);
    }

    public final Class<A> argumentRepresentation() {
        return argumentRepresentation;
    }

    public abstract A lexArgument(String rawArgument);
}
