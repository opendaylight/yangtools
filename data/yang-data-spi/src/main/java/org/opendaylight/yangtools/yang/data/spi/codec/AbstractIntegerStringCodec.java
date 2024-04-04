/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.codec;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import com.google.common.base.CharMatcher;
import com.google.common.collect.RangeSet;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.data.api.codec.YangInvalidValueException;
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

    private final RangeConstraint<N> rangeConstraint;

    AbstractIntegerStringCodec(final Class<N> outputClass, final T typeDefinition) {
        super(outputClass, typeDefinition);
        rangeConstraint = typeDefinition.getRangeConstraint().orElse(null);
    }

    public static @NonNull AbstractIntegerStringCodec<Byte, Int8TypeDefinition> from(final Int8TypeDefinition type) {
        return new Int8StringCodec(type);
    }

    public static @NonNull AbstractIntegerStringCodec<Short, Int16TypeDefinition> from(final Int16TypeDefinition type) {
        return new Int16StringCodec(type);
    }

    public static @NonNull AbstractIntegerStringCodec<Integer, Int32TypeDefinition> from(
            final Int32TypeDefinition type) {
        return new Int32StringCodec(type);
    }

    public static @NonNull AbstractIntegerStringCodec<Long, Int64TypeDefinition> from(final Int64TypeDefinition type) {
        return new Int64StringCodec(type);
    }

    public static @NonNull AbstractIntegerStringCodec<Uint8, Uint8TypeDefinition> from(final Uint8TypeDefinition type) {
        return new Uint8StringCodec(type);
    }

    public static @NonNull AbstractIntegerStringCodec<Uint16, Uint16TypeDefinition> from(
            final Uint16TypeDefinition type) {
        return new Uint16StringCodec(type);
    }

    public static @NonNull AbstractIntegerStringCodec<Uint32, Uint32TypeDefinition> from(
            final Uint32TypeDefinition type) {
        return new Uint32StringCodec(type);
    }

    public static @NonNull AbstractIntegerStringCodec<Uint64, Uint64TypeDefinition> from(
            final Uint64TypeDefinition type) {
        return new Uint64StringCodec(type);
    }

    @Override
    protected final N deserializeImpl(final String product) {
        final int base = provideBase(product);
        final String stringRepresentation = base != 16 ? product : X_MATCHER.removeFrom(product);
        final N deserialized = verifyNotNull(deserialize(stringRepresentation, base));
        if (rangeConstraint != null) {
            final RangeSet<N> ranges = rangeConstraint.getAllowedRanges();
            if (!ranges.contains(deserialized)) {
                throw new YangInvalidValueException(ErrorType.APPLICATION, rangeConstraint,
                    "Value '" + deserialized + "'  is not in required ranges " + ranges);
            }
        }
        return deserialized;
    }

    @Override
    protected final String serializeImpl(final N input) {
        return input.toString();
    }

    /**
     * Deserializes value from supplied string representation is supplied radix. See
     * {@link Integer#parseInt(String, int)} for in-depth description about string and radix relationship.
     *
     * @param stringRepresentation String representation
     * @param radix numeric base.
     * @return Deserialized value.
     */
    protected abstract @NonNull N deserialize(@NonNull String stringRepresentation, int radix);

    private static int provideBase(final String integer) {
        if (integer.length() == 1 && integer.charAt(0) == '0' || INT_PATTERN.matcher(integer).matches()) {
            return 10;
        } else if (HEX_PATTERN.matcher(integer).matches()) {
            return 16;
        } else if (OCT_PATTERN.matcher(integer).matches()) {
            return 8;
        } else {
            throw new NumberFormatException("Incorrect lexical representation of integer value: " + integer + ".\n"
                        + "An integer value can be defined as:\n"
                        + "  - a decimal number,\n"
                        + "  - a hexadecimal number (prefix 0x)," + "%n  - an octal number (prefix 0).\n"
                        + "Signed values are allowed. Spaces between digits are NOT allowed.");
        }
    }
}
