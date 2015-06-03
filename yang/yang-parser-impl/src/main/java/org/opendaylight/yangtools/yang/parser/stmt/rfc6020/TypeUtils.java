/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.LengthConstraintEffectiveImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.RangeConstraintEffectiveImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base.BinaryBaseType;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base.BitsBaseType;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base.BooleanBaseType;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base.Decimal64BaseType;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base.EmptyBaseType;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base.IdentityRefBaseType;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base.InstanceIdentifierBaseType;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base.Int16BaseType;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base.Int32BaseType;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base.Int64BaseType;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base.Int8BaseType;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base.LeafRefBaseType;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base.StringBaseType;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base.UInt16BaseType;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base.UInt32BaseType;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base.UInt64BaseType;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base.UInt8BaseType;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.base.YangBaseType;

public class TypeUtils {
    public static final String BOOLEAN = "boolean";
    public static final String DECIMAL64 = "decimal64";
    public static final String EMPTY = "empty";
    public static final String INT8 = "int8";
    public static final String INT16 = "int16";
    public static final String INT32 = "int32";
    public static final String INT64 = "int64";
    public static final String STRING = "string";
    public static final String UINT8 = "uint8";
    public static final String UINT16 = "uint16";
    public static final String UINT32 = "uint32";
    public static final String UINT64 = "uint64";

    public static final String BINARY = "binary";
    public static final String BITS = "bits";
    public static final String ENUMERATION = "enumeration";
    public static final String IDENTITY_REF = "identityref";
    public static final String INSTANCE_IDENTIFIER = "instance-identifier";
    public static final String LEAF_REF = "leafref";
    public static final String UNION = "union";

    public static final Map<String, YangBaseType> baseTypesMap = new HashMap<>();

    private TypeUtils() {
    }

    static {
        baseTypesMap.put(BOOLEAN, BooleanBaseType.getInstance());
        baseTypesMap.put(DECIMAL64, Decimal64BaseType.getInstance());
        baseTypesMap.put(EMPTY, EmptyBaseType.getInstance());
        baseTypesMap.put(INT8, Int8BaseType.getInstance());
        baseTypesMap.put(INT16, Int16BaseType.getInstance());
        baseTypesMap.put(INT32, Int32BaseType.getInstance());
        baseTypesMap.put(INT64, Int64BaseType.getInstance());
        baseTypesMap.put(STRING, StringBaseType.getInstance());
        baseTypesMap.put(UINT8, UInt8BaseType.getInstance());
        baseTypesMap.put(UINT16, UInt16BaseType.getInstance());
        baseTypesMap.put(UINT32, UInt32BaseType.getInstance());
        baseTypesMap.put(UINT64, UInt64BaseType.getInstance());
        baseTypesMap.put(BINARY, BinaryBaseType.getInstance());
        baseTypesMap.put(BITS, BitsBaseType.getInstance());
        // baseTypesMap.put(ENUMERATION, EnumBaseType.getInstance());
        baseTypesMap.put(IDENTITY_REF, IdentityRefBaseType.getInstance());
        baseTypesMap.put(INSTANCE_IDENTIFIER, InstanceIdentifierBaseType.getInstance());
        baseTypesMap.put(LEAF_REF, LeafRefBaseType.getInstance());
        // baseTypesMap.put(UNION, UnionBaseType.getInstance());
    }

    private static final Set<String> STATEMENT_BUILT_IN_TYPES = ImmutableSet.<String> builder().add(BOOLEAN)
            .add(DECIMAL64).add(EMPTY).add(INT8).add(INT16).add(INT32).add(INT64).add(STRING).add(UINT8).add(UINT16)
            .add(UINT32).add(UINT64).build();

    private static final Splitter PIPE_SPLITTER = Splitter.on("|").trimResults();
    private static final Splitter TWO_DOTS_SPLITTER = Splitter.on("..").trimResults();

    public static boolean isYangStatementBuiltInType(final String type) {
        return STATEMENT_BUILT_IN_TYPES.contains(type);
    }

    private static BigInteger parseIntegerConstraintValue(final String value) {
        BigInteger result;

        if ("min".equals(value)) {
            result = RangeStatementImpl.YANG_MIN_NUM.toBigInteger();
        } else if ("max".equals(value)) {
            result = RangeStatementImpl.YANG_MAX_NUM.toBigInteger();
        } else {
            try {
                result = new BigInteger(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(String.format("Value %s is not a valid integer", value), e);
            }
        }
        return result;
    }

    private static BigDecimal parseDecimalConstraintValue(final String value) {
        BigDecimal result;

        if ("min".equals(value)) {
            result = RangeStatementImpl.YANG_MIN_NUM;
        } else if ("max".equals(value)) {
            result = RangeStatementImpl.YANG_MAX_NUM;
        } else {
            try {
                result = new BigDecimal(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(String.format("Value %s is not a valid decimal number", value), e);
            }
        }
        return result;
    }

    public static List<RangeConstraint> parseRangeListFromString(String rangeArgument) {

        Optional<String> description = Optional.absent();
        Optional<String> reference = Optional.absent();

        List<RangeConstraint> rangeConstraints = new ArrayList<>();

        for (final String singleRange : PIPE_SPLITTER.split(rangeArgument)) {
            final Iterator<String> boundaries = TWO_DOTS_SPLITTER.splitToList(singleRange).iterator();
            final BigDecimal min = parseDecimalConstraintValue(boundaries.next());

            final BigDecimal max;
            if (boundaries.hasNext()) {
                max = parseDecimalConstraintValue(boundaries.next());

                // if min larger than max then error
                if (min.compareTo(max) == 1) {
                    throw new IllegalArgumentException(String.format(
                            "Range constraint %s has descending order of boundaries; should be ascending", singleRange));
                }
                if (boundaries.hasNext()) {
                    throw new IllegalArgumentException("Wrong number of boundaries in range constraint " + singleRange);
                }
            } else {
                max = min;
            }

            // some of intervals overlapping
            if (rangeConstraints.size() > 1
                    && min.compareTo((BigDecimal) Iterables.getLast(rangeConstraints).getMax()) != 1) {
                throw new IllegalArgumentException(String.format("Some of the ranges in %s are not disjoint",
                        rangeArgument));
            }

            rangeConstraints.add(new RangeConstraintEffectiveImpl(min, max, description, reference));
        }

        return rangeConstraints;
    }

    public static List<LengthConstraint> parseLengthListFromString(String rangeArgument) {

        Optional<String> description = Optional.absent();
        Optional<String> reference = Optional.absent();

        List<LengthConstraint> rangeConstraints = new ArrayList<>();

        for (final String singleRange : PIPE_SPLITTER.split(rangeArgument)) {
            final Iterator<String> boundaries = TWO_DOTS_SPLITTER.splitToList(singleRange).iterator();
            final BigInteger min = parseIntegerConstraintValue(boundaries.next());

            final BigInteger max;
            if (boundaries.hasNext()) {
                max = parseIntegerConstraintValue(boundaries.next());

                // if min larger than max then error
                if (min.compareTo(max) == 1) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "Length constraint %s has descending order of boundaries; should be ascending",
                                    singleRange));
                }
                if (boundaries.hasNext()) {
                    throw new IllegalArgumentException("Wrong number of boundaries in length constraint " + singleRange);
                }
            } else {
                max = min;
            }

            // some of intervals overlapping
            if (rangeConstraints.size() > 1
                    && min.compareTo((BigInteger) Iterables.getLast(rangeConstraints).getMax()) != 1) {
                throw new IllegalArgumentException(String.format("Some of the length ranges in %s are not disjoint",
                        rangeArgument));
            }

            rangeConstraints.add(new LengthConstraintEffectiveImpl(min, max, description, reference));
        }

        return rangeConstraints;
    }

    public static boolean isYangBaseTypeString(String typeName) {
        return baseTypesMap.containsKey(typeName);
    }

    public static YangBaseType getYangBaseTypeFromString(String typeName) {
        return baseTypesMap.get(typeName);
    }
}
