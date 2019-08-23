/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.CharMatcher;
import com.google.common.collect.RangeSet;
import java.math.BigInteger;
import java.util.Optional;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNull;
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

    // FIXME: inline this
    private static final String INCORRECT_LEXICAL_REPRESENTATION =
            "Incorrect lexical representation of integer value: %s."
                    + "\nAn integer value can be defined as: "
                    + "\n  - a decimal number,"
                    + "\n  - a hexadecimal number (prefix 0x)," + "%n  - an octal number (prefix 0)."
                    + "\nSigned values are allowed. Spaces between digits are NOT allowed.";

    private final RangeSet<N> rangeConstraints;

    AbstractIntegerStringCodec(final T typeDefinition, final Optional<RangeConstraint<N>> constraint,
            final Class<N> outputClass) {
        super(requireNonNull(typeDefinition), outputClass);
        rangeConstraints = constraint.map(RangeConstraint::getAllowedRanges).orElse(null);
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

    public static @NonNull AbstractIntegerStringCodec<Short, Uint8TypeDefinition> from(final Uint8TypeDefinition type) {
        return new Uint8StringCodec(type);
    }

    public static AbstractIntegerStringCodec<Integer, Uint16TypeDefinition> from(final Uint16TypeDefinition type) {
        return new Uint16StringCodec(type);
    }

    public static @NonNull AbstractIntegerStringCodec<Long, Uint32TypeDefinition> from(
            final Uint32TypeDefinition type) {
        return new Uint32StringCodec(type);
    }

    public static @NonNull AbstractIntegerStringCodec<BigInteger, Uint64TypeDefinition> from(
            final Uint64TypeDefinition type) {
        return new Uint64StringCodec(type);
    }

    @Override
    public final N deserializeImpl(final String product) {
        final int base = provideBase(product);
        final String stringRepresentation = base != 16 ? product : X_MATCHER.removeFrom(product);
        final N deserialized = verifyNotNull(deserialize(stringRepresentation, base));
        if (rangeConstraints != null) {
            checkArgument(rangeConstraints.contains(deserialized), "Value '%s'  is not in required ranges %s",
                deserialized, rangeConstraints);
        }
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
    protected abstract @NonNull N deserialize(@NonNull String stringRepresentation, int radix);

    protected static <N extends Number & Comparable<N>> Optional<RangeConstraint<N>> extractRange(
            final RangeRestrictedTypeDefinition<?, N> type) {
        return type == null ? Optional.empty() : type.getRangeConstraint();
    }

    private static int provideBase(final String integer) {
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
}
