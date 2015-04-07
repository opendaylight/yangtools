/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

abstract class AbstractIntegerStringCodec<N extends Number & Comparable<N>, T extends TypeDefinition<T>> extends TypeDefinitionAwareCodec<N, T>{

    private static final Pattern intPattern = Pattern.compile("[+-]?[1-9][0-9]*$");
    private static final Pattern hexPattern = Pattern.compile("[+-]?0[xX][0-9a-fA-F]+");
    private static final Pattern octalPattern = Pattern.compile("[+-]?0[1-7][0-7]*$");

    // For up to two characters, this is very fast
    private static final CharMatcher X_MATCHER = CharMatcher.anyOf("xX");

    private static final String INCORRECT_LEXICAL_REPRESENTATION = "Incorrect lexical representation of integer value: %s."
            + "\nAn integer value can be defined as: "
            + "\n  - a decimal number,"
            + "\n  - a hexadecimal number (prefix 0x)," + "%n  - an octal number (prefix 0)."
            + "\nSigned values are allowed. Spaces between digits are NOT allowed.";

    private static final int provideBase(final String integer) {
        if (integer == null) {
            throw new IllegalArgumentException("String representing integer number cannot be NULL");
        }

        if ((integer.length() == 1) && (integer.charAt(0) == '0')) {
            return 10;
        }

        final Matcher intMatcher = intPattern.matcher(integer);
        if (intMatcher.matches()) {
            return 10;
        }
        final Matcher hexMatcher = hexPattern.matcher(integer);
        if (hexMatcher.matches()) {
            return 16;
        }
        final Matcher octMatcher = octalPattern.matcher(integer);
        if (octMatcher.matches()) {
            return 8;
        }
        final String formatedMessage =
                String.format(INCORRECT_LEXICAL_REPRESENTATION, integer);
        throw new NumberFormatException(formatedMessage);
    }

    private static String normalizeHexadecimal(final String hexInt) {
        if (hexInt == null) {
            throw new IllegalArgumentException(
                    "String representing integer number in Hexadecimal format cannot be NULL!");
        }

        return X_MATCHER.removeFrom(hexInt);
    }

    protected AbstractIntegerStringCodec(final Optional<T> typeDefinition, final Class<N> outputClass) {
        super(typeDefinition, outputClass);

    }

    @Override
    public final N deserialize(final String stringRepresentation) {
        final int base = provideBase(stringRepresentation);
        if (base == 16) {
            return deserialize(normalizeHexadecimal(stringRepresentation),base);
        }
        return deserialize(stringRepresentation,base);
    }

    protected abstract N deserialize(String stringRepresentation, int base);

}
