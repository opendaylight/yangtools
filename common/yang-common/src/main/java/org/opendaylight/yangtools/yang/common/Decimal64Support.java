/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
final class Decimal64Support extends AbstractCanonicalValueSupport<Decimal64> {
    static final CanonicalValueSupport<Decimal64> INSTANCE = new Decimal64Support();

    private Decimal64Support() {
        super(Decimal64.class);
    }

    @Override
    public ValidationResult<Decimal64> fromString(final String str) {
        // https://www.rfc-editor.org/rfc/rfc6020#section-9.3.1
        //
        // A decimal64 value is lexically represented as an optional sign ("+"
        // or "-"), followed by a sequence of decimal digits, optionally
        // followed by a period ('.') as a decimal indicator and a sequence of
        // decimal digits.  If no sign is specified, "+" is assumed.
        if (str.isEmpty()) {
            return CanonicalValueViolation.of(null, "Empty string is not a valid decimal64 representation");
        }

        // Deal with optional sign
        final boolean negative;
        int idx = switch (str.charAt(0)) {
            case '-' -> {
                negative = true;
                yield 1;
            }
            case '+' -> {
                negative = false;
                yield 1;
            }
            default -> {
                negative = false;
                yield 0;
            }
        };
        // Sanity check length
        if (idx == str.length()) {
            return CanonicalValueViolation.of(null, "Missing digits after sign");
        }

        // Character limit, used for caching and cutting trailing zeroes
        int limit = str.length() - 1;

        // Skip any leading zeroes, but leave at least one
        for (; idx < limit && str.charAt(idx) == '0'; idx++) {
            final char ch = str.charAt(idx + 1);
            if (ch < '0' || ch > '9') {
                break;
            }
        }

        // Integer part and its length
        int intLen = 0;
        long intPart = 0;

        for (; idx <= limit; idx++, intLen++) {
            final char ch = str.charAt(idx);
            if (ch == '.') {
                // Fractions are next
                break;
            }
            if (intLen == Decimal64.MAX_SCALE) {
                return CanonicalValueViolation.of(null,
                        "Integer part is longer than " + Decimal64.MAX_SCALE + " digits");
            }

            intPart = 10 * intPart + toInt(ch, idx);
        }

        if (idx > limit) {
            // No fraction digits, we are done
            return new ValidatedValue<>(new Decimal64((byte)1, intPart, 0, negative));
        }

        // Bump index to skip over period and check the remainder
        idx++;
        if (idx > limit) {
            return CanonicalValueViolation.of(null, "Value '" + str + "' is missing fraction digits");
        }

        // Trim trailing zeroes, if any
        while (idx < limit && str.charAt(limit) == '0') {
            limit--;
        }

        final int fracLimit = Decimal64.MAX_SCALE - intLen + 1;
        byte fracLen = 0;
        long fracPart = 0;
        for (; idx <= limit; idx++, fracLen++) {
            final char ch = str.charAt(idx);
            if (fracLen == fracLimit) {
                return CanonicalValueViolation.of(null, "Fraction part longer than " + fracLimit + " digits");
            }

            fracPart = 10 * fracPart + toInt(ch, idx);
        }

        return new ValidatedValue<>(new Decimal64(fracLen, intPart, fracPart, negative));
    }

    private static int toInt(final char ch, final int index) {
        if (ch < '0' || ch > '9') {
            throw new NumberFormatException("Illegal character at offset " + index);
        }
        return ch - '0';
    }
}
