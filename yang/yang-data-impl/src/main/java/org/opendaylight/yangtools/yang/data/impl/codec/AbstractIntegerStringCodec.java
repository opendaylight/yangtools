/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.base.CharMatcher;
import com.google.common.collect.RangeSet;
import java.math.BigInteger;
import java.util.Optional;
import java.util.regex.Pattern;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeRestrictedTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;

/**
 * Do not use this class outside of yangtools, its presence does not fall into the API stability contract.
 */
@Beta
public abstract class AbstractIntegerStringCodec<N extends Number & Comparable<N>,
        T extends RangeRestrictedTypeDefinition<T, N>> extends TypeDefinitionAwareCodec<N, T> {

    private static final Pattern INT_PATTERN = Pattern.compile("[+-]?[1-9][0-9]*$");
    private static final Pattern HEX_PATTERN = Pattern.compile("[+-]?0[xX][0-9a-fA-F]+");
    private static final Pattern OCT_PATTERN = Pattern.compile("[+-]?0[1-7][0-7]*$");

    // For up to two characters, this is very fast
    private static final CharMatcher X_MATCHER = CharMatcher.anyOf("xX");

    private static final String INCORRECT_LEXICAL_REPRESENTATION =
            "Incorrect lexical representation of integer value: %s."
                    + "\nAn integer value can be defined as: "
                    + "\n  - a decimal number,"
                    + "\n  - a hexadecimal number (prefix 0x)," + "%n  - an octal number (prefix 0)."
                    + "\nSigned values are allowed. Spaces between digits are NOT allowed.";

    private final RangeSet<N> rangeConstraints;

    AbstractIntegerStringCodec(final Optional<T> typeDefinition, final Optional<RangeConstraint<N>> constraint,
        final Class<N> outputClass) {
        super(typeDefinition, outputClass);
        rangeConstraints = constraint.map(RangeConstraint::getAllowedRanges).orElse(null);
    }

    public static AbstractIntegerStringCodec<Byte, Int8TypeDefinition> from(final Int8TypeDefinition type) {
        return new Int8StringCodec(Optional.of(type));
    }

    public static AbstractIntegerStringCodec<Short, Int16TypeDefinition> from(final Int16TypeDefinition type) {
        return new Int16StringCodec(Optional.of(type));
    }

    public static AbstractIntegerStringCodec<Integer, Int32TypeDefinition> from(final Int32TypeDefinition type) {
        return new Int32StringCodec(Optional.of(type));
    }

    public static AbstractIntegerStringCodec<Long, Int64TypeDefinition> from(final Int64TypeDefinition type) {
        return new Int64StringCodec(Optional.of(type));
    }

    public static AbstractIntegerStringCodec<Short, Uint8TypeDefinition> from(final Uint8TypeDefinition type) {
        return new Uint8StringCodec(Optional.of(type));
    }

    public static AbstractIntegerStringCodec<Integer, Uint16TypeDefinition> from(final Uint16TypeDefinition type) {
        return new Uint16StringCodec(Optional.of(type));
    }

    public static AbstractIntegerStringCodec<Long, Uint32TypeDefinition> from(final Uint32TypeDefinition type) {
        return new Uint32StringCodec(Optional.of(type));
    }

    public static AbstractIntegerStringCodec<BigInteger, Uint64TypeDefinition> from(final Uint64TypeDefinition type) {
        return new Uint64StringCodec(Optional.of(type));
    }

    @Override
    public final N deserialize(final String stringRepresentation) {
        final int base = provideBase(stringRepresentation);
        final N deserialized;
        if (base == 16) {
            deserialized = deserialize(normalizeHexadecimal(stringRepresentation),base);
        } else {
            deserialized = deserialize(stringRepresentation,base);
        }
        validate(deserialized);
        return deserialized;
    }

    /**
     * Deserializes value from supplied string representation is supplied radix. See
     * {@link Integer#parseInt(String, int)} for in-depth description about string and radix relationship.
     *
     * @param stringRepresentation String representation
     * @param radix numeric base.
     * @return Deserialized value.
     */
    abstract N deserialize(String stringRepresentation, int radix);

    private void validate(final N value) {
        if (rangeConstraints != null) {
            checkArgument(rangeConstraints.contains(value), "Value '%s'  is not in required ranges %s",
                value, rangeConstraints);
        }
    }

    protected static <N extends Number & Comparable<N>> Optional<RangeConstraint<N>> extractRange(
            final RangeRestrictedTypeDefinition<?, N> type) {
        return type == null ? Optional.empty() : type.getRangeConstraint();
    }

    private static int provideBase(final String integer) {
        checkArgument(integer != null, "String representing integer number cannot be NULL");

        if (integer.length() == 1 && integer.charAt(0) == '0') {
            return 10;
        } else if (INT_PATTERN.matcher(integer).matches()) {
            return 10;
        } else if (HEX_PATTERN.matcher(integer).matches()) {
            return 16;
        } else if (OCT_PATTERN.matcher(integer).matches()) {
            return 8;
        } else {
            throw new NumberFormatException(String.format(INCORRECT_LEXICAL_REPRESENTATION, integer));
        }
    }

    private static String normalizeHexadecimal(final String hexInt) {
        checkArgument(hexInt != null, "String representing integer number in Hexadecimal format cannot be NULL!");
        return X_MATCHER.removeFrom(hexInt);
    }
}
