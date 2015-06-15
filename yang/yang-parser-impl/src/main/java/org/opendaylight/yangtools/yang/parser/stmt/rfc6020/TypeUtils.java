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
import com.google.common.collect.Iterables;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.util.BinaryType;
import org.opendaylight.yangtools.yang.model.util.BooleanType;
import org.opendaylight.yangtools.yang.model.util.EmptyType;
import org.opendaylight.yangtools.yang.model.util.Int16;
import org.opendaylight.yangtools.yang.model.util.Int32;
import org.opendaylight.yangtools.yang.model.util.Int64;
import org.opendaylight.yangtools.yang.model.util.Int8;
import org.opendaylight.yangtools.yang.model.util.StringType;
import org.opendaylight.yangtools.yang.model.util.Uint16;
import org.opendaylight.yangtools.yang.model.util.Uint32;
import org.opendaylight.yangtools.yang.model.util.Uint64;
import org.opendaylight.yangtools.yang.model.util.Uint8;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.LengthConstraintEffectiveImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.RangeConstraintEffectiveImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.TypeDefinitionEffectiveBuilder;
import org.opendaylight.yangtools.yang.parser.util.UnknownBoundaryNumber;

public class TypeUtils {

    public static final String BINARY = "binary";
    public static final String BITS = "bits";
    public static final String BOOLEAN = "boolean";
    public static final String DECIMAL64 = "decimal64";
    public static final String EMPTY = "empty";
    public static final String ENUMERATION = "enumeration";
    public static final String IDENTITY_REF = "identityref";
    public static final String INSTANCE_IDENTIFIER = "instance-identifier";
    public static final String INT8 = "int8";
    public static final String INT16 = "int16";
    public static final String INT32 = "int32";
    public static final String INT64 = "int64";
    public static final String LEAF_REF = "leafref";
    public static final String STRING = "string";
    public static final String UINT8 = "uint8";
    public static final String UINT16 = "uint16";
    public static final String UINT32 = "uint32";
    public static final String UINT64 = "uint64";
    public static final String UNION = "union";

    public static final Set<String> BUILT_IN_TYPES = new HashSet<>();
    public static final Set<String> TYPE_BODY_STMTS = new HashSet<>();
    public static final Map<String, TypeDefinition> BASE_TYPES_MAP = new HashMap<>();

    static {

        BUILT_IN_TYPES.add(BINARY);
        BUILT_IN_TYPES.add(BITS);
        BUILT_IN_TYPES.add(BOOLEAN);
        BUILT_IN_TYPES.add(DECIMAL64);
        BUILT_IN_TYPES.add(EMPTY);
        BUILT_IN_TYPES.add(ENUMERATION);
        BUILT_IN_TYPES.add(IDENTITY_REF);
        BUILT_IN_TYPES.add(INSTANCE_IDENTIFIER);
        BUILT_IN_TYPES.add(INT8);
        BUILT_IN_TYPES.add(INT16);
        BUILT_IN_TYPES.add(INT32);
        BUILT_IN_TYPES.add(INT64);
        BUILT_IN_TYPES.add(LEAF_REF);
        BUILT_IN_TYPES.add(STRING);
        BUILT_IN_TYPES.add(UINT8);
        BUILT_IN_TYPES.add(UINT16);
        BUILT_IN_TYPES.add(UINT32);
        BUILT_IN_TYPES.add(UINT64);
        BUILT_IN_TYPES.add(UNION);

        TYPE_BODY_STMTS.add(DECIMAL64);
        TYPE_BODY_STMTS.add(ENUMERATION);
        TYPE_BODY_STMTS.add(LEAF_REF);
        TYPE_BODY_STMTS.add(IDENTITY_REF);
        TYPE_BODY_STMTS.add(INSTANCE_IDENTIFIER);
        TYPE_BODY_STMTS.add(BITS);
        TYPE_BODY_STMTS.add(UNION);
        TYPE_BODY_STMTS.add(BINARY);

        BASE_TYPES_MAP.put(BINARY, BinaryType.getInstance());
        BASE_TYPES_MAP.put(BOOLEAN, BooleanType.getInstance());
        BASE_TYPES_MAP.put(EMPTY, EmptyType.getInstance());
        BASE_TYPES_MAP.put(INT8, Int8.getInstance());
        BASE_TYPES_MAP.put(INT16, Int16.getInstance());
        BASE_TYPES_MAP.put(INT32, Int32.getInstance());
        BASE_TYPES_MAP.put(INT64, Int64.getInstance());
        BASE_TYPES_MAP.put(STRING, StringType.getInstance());
        BASE_TYPES_MAP.put(UINT8, Uint8.getInstance());
        BASE_TYPES_MAP.put(UINT16, Uint16.getInstance());
        BASE_TYPES_MAP.put(UINT32, Uint32.getInstance());
        BASE_TYPES_MAP.put(UINT64, Uint64.getInstance());
    }

    private TypeUtils() {
    }

    private static final Splitter PIPE_SPLITTER = Splitter.on("|").trimResults();
    private static final Splitter TWO_DOTS_SPLITTER = Splitter.on("..").trimResults();

    private static BigDecimal yangConstraintToBigDecimal(Number number) {
        if (number instanceof UnknownBoundaryNumber) {
            if (number.toString().equals("min")) {
                return RangeStatementImpl.YANG_MIN_NUM;
            } else {
                return RangeStatementImpl.YANG_MAX_NUM;
            }
        } else {
            return new BigDecimal(number.toString());
        }
    }

    private static int compareNumbers(Number n1, Number n2) {

        final BigDecimal num1 = yangConstraintToBigDecimal(n1);
        final BigDecimal num2 = yangConstraintToBigDecimal(n2);

        return new BigDecimal(num1.toString()).compareTo(new BigDecimal(num2.toString()));
    }

    private static Number parseIntegerConstraintValue(final String value) {
        Number result;

        if ("min".equals(value) || "max".equals(value)) {
            result = new UnknownBoundaryNumber(value);
        } else {
            try {
                result = new BigInteger(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(String.format("Value %s is not a valid integer", value), e);
            }
        }
        return result;
    }

    private static Number parseDecimalConstraintValue(final String value) {
        Number result;

        if ("min".equals(value) || "max".equals(value)) {
            result = new UnknownBoundaryNumber(value);
        } else {
            try {
                if (value.indexOf('.') != -1) {
                    result = new BigDecimal(value);
                } else {
                    result = new BigInteger(value);
                }
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
            final Number min = parseDecimalConstraintValue(boundaries.next());

            final Number max;
            if (boundaries.hasNext()) {
                max = parseDecimalConstraintValue(boundaries.next());

                // if min larger than max then error
                if (compareNumbers(min, max) == 1) {
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
            if (rangeConstraints.size() > 1 && compareNumbers(min, Iterables.getLast(rangeConstraints).getMax()) != 1) {
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
            final Number min = parseIntegerConstraintValue(boundaries.next());

            final Number max;
            if (boundaries.hasNext()) {
                max = parseIntegerConstraintValue(boundaries.next());

                // if min larger than max then error
                if (compareNumbers(min, max) == 1) {
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
            if (rangeConstraints.size() > 1 && compareNumbers(min, Iterables.getLast(rangeConstraints).getMax()) != 1) {
                throw new IllegalArgumentException(String.format("Some of the length ranges in %s are not disjoint",
                        rangeArgument));
            }

            rangeConstraints.add(new LengthConstraintEffectiveImpl(min, max, description, reference));
        }

        return rangeConstraints;
    }

    public static boolean isYangBaseTypeString(String typeName) {
        return BUILT_IN_TYPES.contains(typeName);
    }

    public static boolean isYangTypeBodyStmt(String typeName) {
        return TYPE_BODY_STMTS.contains(typeName);
    }

    public static TypeDefinition<?> getTypeFromEffectiveStatement(EffectiveStatement<?, ?> effectiveStatement) {
        if (effectiveStatement instanceof TypeDefinitionEffectiveBuilder) {
            TypeDefinitionEffectiveBuilder typeDefEffectiveBuilder = (TypeDefinitionEffectiveBuilder) effectiveStatement;
            return typeDefEffectiveBuilder.buildType();
        } else {
            final String typeName = ((TypeDefinition) effectiveStatement).getQName().getLocalName();
            return BASE_TYPES_MAP.get(typeName);
        }
    }

    public static TypeDefinition<?> getYangBaseTypeFromString(String typeName) {

        if (BASE_TYPES_MAP.containsKey(typeName)) {
            return BASE_TYPES_MAP.get(typeName);
        }

        return null;
    }

    public static void sortTypes(List<TypeDefinition<?>> typesInit) {
        Collections.sort(typesInit, new Comparator<TypeDefinition<?>>() {
            @Override
            public int compare(TypeDefinition<?> o1, TypeDefinition<?> o2) {
                if (isBuiltInType(o1) && !isBuiltInType(o2)) {
                    return -1;
                }
                if (!isBuiltInType(o1) && isBuiltInType(o2)) {
                    return 1;
                }
                return 0;
            }
        });
    }

    public static boolean isBuiltInType(TypeDefinition<?> o1) {
        return BUILT_IN_TYPES.contains(o1.getQName().getLocalName());
    }
}
