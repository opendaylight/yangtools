/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;

/**
 * Dedicated type for YANG's 'type decimal64' type.
 *
 * @author Robert Varga
 */
@Beta
public final class Decimal64 extends Number implements Comparable<Decimal64> {
    private static final long serialVersionUID = 1L;

    private static final long SCALE[];
    static {
        final long[] s = new long[18];
        long m = 10;
        for (int i = 0; i < s.length; ++i) {
            s[i] = m;
            m *= 10;
        }

        SCALE = s;
    }

    private final byte fractionDigits;
    private final long value;

    private Decimal64(final byte fractionDigits, final long value) {
        checkArgument(fractionDigits >= 1 && fractionDigits <= 18);
        this.fractionDigits = fractionDigits;
        this.value = value;
    }

    /**
     * Attempt to parse a String into a Decimal64. This method uses minimum fraction digits required to hold
     * the entire value.
     *
     * @param value
     * @return
     * @throws NullPointerException if value is null.
     * @throws NumberFormatException if the string does not contain a parsable decimal64.
     */
    public static Decimal64 parse(final String value) {
        // https://tools.ietf.org/html/rfc6020#section-9.3.1
        //
        // A decimal64 value is lexically represented as an optional sign ("+"
        // or "-"), followed by a sequence of decimal digits, optionally
        // followed by a period ('.') as a decimal indicator and a sequence of
        // decimal digits.  If no sign is specified, "+" is assumed.
        if (value.isEmpty()) {
            throw new NumberFormatException("Empty string is not a valid decimal64 representation");
        }

        // Deal with optional sign, adjusting parsing start
        final boolean negative;
        final int start;
        switch (value.charAt(0)) {
            case '-':
                negative = true;
                start = 1;
                break;
            case '+':
                negative = false;
                start = 1;
                break;
            default:
                negative = false;
                start = 0;
        }

        // Sanity check length
        if (start != 0 && value.length() == start) {
            throw new NumberFormatException("Missing digits after sign");
        }

        final int period = value.indexOf('.', start);
        return period != -1 ? parseFractional(negative, value.substring(start, period), value.substring(period + 1))
                : parseIntegral(negative, value.substring(start));
    }

    private static Decimal64 parseFractional(final boolean negative, final String intPart, final String fracPart) {
        if (fracPart.indexOf('.') != -1) {
            throw new NumberFormatException("Multiple period characters present");
        }
        if (intPart.isEmpty()) {
            throw new NumberFormatException("Empty integral part");
        }
        if (fracPart.isEmpty()) {
            throw new NumberFormatException("Empty fraction part");
        }

        // TODO Auto-generated method stub
        return null;
    }


    private static Decimal64 parseIntegral(final boolean negative, final String intPart) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int compareTo(final Decimal64 o) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int intValue() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long longValue() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float floatValue() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double doubleValue() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String toString() {
        // https://tools.ietf.org/html/rfc6020#section-9.3.2
        //
        // The canonical form of a positive decimal64 does not include the sign
        // "+".  The decimal point is required.  Leading and trailing zeros are
        // prohibited, subject to the rule that there MUST be at least one digit
        // before and after the decimal point.  The value zero is represented as
        // "0.0".

        final StringBuilder sb = new StringBuilder(21);
        sb.append(value / SCALE[fractionDigits - 1]);
        sb.append('.');
        sb.append(value % SCALE[fractionDigits - 1]);
        return sb.toString();
    }
}
