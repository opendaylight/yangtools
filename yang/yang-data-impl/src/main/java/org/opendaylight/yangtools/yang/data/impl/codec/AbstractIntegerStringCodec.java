/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.opendaylight.yangtools.yang.model.util.BaseTypes.INT16_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.INT32_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.INT64_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.INT8_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.UINT16_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.UINT32_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.UINT64_QNAME;
import static org.opendaylight.yangtools.yang.model.util.BaseTypes.UINT8_QNAME;

import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;

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


    private final List<Range<N>> rangeConstraints;

    protected AbstractIntegerStringCodec(final Optional<T> typeDefinition, final List<RangeConstraint> constraints , final Class<N> outputClass) {
        super(typeDefinition, outputClass);
        if(constraints.isEmpty()) {
            rangeConstraints = Collections.emptyList();
        } else {
            final ArrayList<Range<N>> builder = new ArrayList<>(constraints.size());
            for(final RangeConstraint yangConstraint : constraints) {
                builder.add(createRange(yangConstraint.getMin(),yangConstraint.getMax()));
            }
            rangeConstraints = builder;
        }

    }

    static TypeDefinitionAwareCodec<?, IntegerTypeDefinition> from(final IntegerTypeDefinition type) {
        final Optional<IntegerTypeDefinition> typeOptional = Optional.of(type);
        IntegerTypeDefinition baseType = type;
        while(baseType.getBaseType() != null) {
            baseType = baseType.getBaseType();
        }
        if (INT8_QNAME.equals(baseType.getQName())) {
            return new Int8StringCodec(typeOptional);
        } else if (INT16_QNAME.equals(baseType.getQName())) {
            return new Int16StringCodec(typeOptional);
        } else if (INT32_QNAME.equals(baseType.getQName())) {
            return new Int32StringCodec(typeOptional);
        } else if (INT64_QNAME.equals(baseType.getQName())) {
            return new Int64StringCodec(typeOptional);
        }
        throw new IllegalArgumentException("Unsupported base type: " + baseType.getQName());
    }

    static TypeDefinitionAwareCodec<?, UnsignedIntegerTypeDefinition> from(final UnsignedIntegerTypeDefinition type) {
        final Optional<UnsignedIntegerTypeDefinition> typeOptional = Optional.of(type);
        UnsignedIntegerTypeDefinition baseType = type;
        while(baseType.getBaseType() != null) {
            baseType = baseType.getBaseType();
        }
        if (UINT8_QNAME.equals(baseType.getQName())) {
            return new Uint8StringCodec(typeOptional);
        } else if (UINT16_QNAME.equals(baseType.getQName())) {
            return new Uint16StringCodec(typeOptional);
        } else if (UINT32_QNAME.equals(baseType.getQName())) {
            return new Uint32StringCodec(typeOptional);
        } else if (UINT64_QNAME.equals(baseType.getQName())) {
            return new Uint64StringCodec(typeOptional);
        }
        throw new IllegalArgumentException("Unsupported base type: " + baseType.getQName());
    }

    private Range<N> createRange(final Number yangMin, final Number yangMax) {
        final N min = convertValue(yangMin);
        final N max = convertValue(yangMax);
        return Range.closed(min, max);
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


    private final void validate(final N value) {
        if(rangeConstraints.isEmpty()) {
            return;
        }
        for(final Range<N> constraint : rangeConstraints) {
            if(constraint.contains(value)) {
                return;
            }
        }
        // FIXME: Provide better error report.
        throw new IllegalArgumentException("Value '" + value + "'  is not in required range " + rangeConstraints);
    }

    protected abstract N deserialize(String stringRepresentation, int base);

    protected abstract N convertValue(Number value);


    protected static List<RangeConstraint> extractRange(final IntegerTypeDefinition type) {
        if(type == null) {
            return Collections.emptyList();
        }
        return type.getRangeConstraints();
    }

    protected static List<RangeConstraint> extractRange(final UnsignedIntegerTypeDefinition type) {
        if(type == null) {
            return Collections.emptyList();
        }
        return type.getRangeConstraints();
    }

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
}
