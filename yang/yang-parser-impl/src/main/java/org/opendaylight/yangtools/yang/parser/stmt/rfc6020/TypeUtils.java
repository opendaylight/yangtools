/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.util.Collections;

import java.util.Comparator;
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

    // FIXME: please clean these sets up and wipe out those not used
    private static final Set<String> PRIMITIVE_TYPES = ImmutableSet
            .<String> builder().add(BOOLEAN).add(DECIMAL64).add(EMPTY)
            .add(INT8).add(INT16).add(INT32).add(INT64).add(STRING).add(UINT8)
            .add(UINT16).add(UINT32).add(UINT64).build();
    public static final Set<String> BASE_TYPES = ImmutableSet
            .<String> builder().addAll(PRIMITIVE_TYPES).add(BINARY).add(BITS)
            .add(ENUMERATION).add(IDENTITY_REF).add(INSTANCE_IDENTIFIER)
            .add(LEAF_REF).add(UNION).build();

    public static final Set<String> TYPE_BODY_STMTS = ImmutableSet
            .<String> builder().add(DECIMAL64).add(ENUMERATION).add(LEAF_REF)
            .add(IDENTITY_REF).add(INSTANCE_IDENTIFIER).add(BITS).add(UNION)
            .add(BINARY).build();

    public static final Map<String, TypeDefinition> baseTypesMap = new HashMap<>();

    static {
        baseTypesMap.put(BOOLEAN, BooleanType.getInstance());
        baseTypesMap.put(EMPTY, EmptyType.getInstance());
        baseTypesMap.put(INT8, Int8.getInstance());
        baseTypesMap.put(INT16, Int16.getInstance());
        baseTypesMap.put(INT32, Int32.getInstance());
        baseTypesMap.put(INT64, Int64.getInstance());
        baseTypesMap.put(STRING, StringType.getInstance());
        baseTypesMap.put(UINT8, Uint8.getInstance());
        baseTypesMap.put(UINT16, Uint16.getInstance());
        baseTypesMap.put(UINT32, Uint32.getInstance());
        baseTypesMap.put(UINT64, Uint64.getInstance());
        baseTypesMap.put(BINARY, BinaryType.getInstance());
    }

    private TypeUtils() {
    }

    private static final Splitter PIPE_SPLITTER = Splitter.on("|")
            .trimResults();
    private static final Splitter TWO_DOTS_SPLITTER = Splitter.on("..")
            .trimResults();

    private static int compareNumbers(Number n1, Number n2) {
        return new BigDecimal(n1.toString()).compareTo(new BigDecimal(n2
                .toString()));
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
                throw new IllegalArgumentException(String.format(
                        "Value %s is not a valid integer", value), e);
            }
        }
        return result;
    }

    private static Number parseDecimalConstraintValue(final String value) {
        Number result;

        if ("min".equals(value)) {
            result = RangeStatementImpl.YANG_MIN_NUM;
        } else if ("max".equals(value)) {
            result = RangeStatementImpl.YANG_MAX_NUM;
        } else {
            try {
                if (value.indexOf('.') != -1) {
                    result = new BigDecimal(value);
                } else {
                    result = new BigInteger(value);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(String.format(
                        "Value %s is not a valid decimal number", value), e);
            }
        }
        return result;
    }

    public static List<RangeConstraint> parseRangeListFromString(
            String rangeArgument) {

        Optional<String> description = Optional.absent();
        Optional<String> reference = Optional.absent();

        List<RangeConstraint> rangeConstraints = new ArrayList<>();

        for (final String singleRange : PIPE_SPLITTER.split(rangeArgument)) {
            final Iterator<String> boundaries = TWO_DOTS_SPLITTER.splitToList(
                    singleRange).iterator();
            final Number min = parseDecimalConstraintValue(boundaries.next());

            final Number max;
            if (boundaries.hasNext()) {
                max = parseDecimalConstraintValue(boundaries.next());

                // if min larger than max then error
                if (compareNumbers(min, max) == 1) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "Range constraint %s has descending order of boundaries; should be ascending",
                                    singleRange));
                }
                if (boundaries.hasNext()) {
                    throw new IllegalArgumentException(
                            "Wrong number of boundaries in range constraint "
                                    + singleRange);
                }
            } else {
                max = min;
            }

            // some of intervals overlapping
            if (rangeConstraints.size() > 1
                    && compareNumbers(min, Iterables.getLast(rangeConstraints)
                            .getMax()) != 1) {
                throw new IllegalArgumentException(String.format(
                        "Some of the ranges in %s are not disjoint",
                        rangeArgument));
            }

            rangeConstraints.add(new RangeConstraintEffectiveImpl(min, max,
                    description, reference));
        }

        return rangeConstraints;
    }

    public static List<LengthConstraint> parseLengthListFromString(
            String rangeArgument) {

        Optional<String> description = Optional.absent();
        Optional<String> reference = Optional.absent();

        List<LengthConstraint> rangeConstraints = new ArrayList<>();

        for (final String singleRange : PIPE_SPLITTER.split(rangeArgument)) {
            final Iterator<String> boundaries = TWO_DOTS_SPLITTER.splitToList(
                    singleRange).iterator();
            final BigInteger min = parseIntegerConstraintValue(boundaries
                    .next());

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
                    throw new IllegalArgumentException(
                            "Wrong number of boundaries in length constraint "
                                    + singleRange);
                }
            } else {
                max = min;
            }

            // some of intervals overlapping
            if (rangeConstraints.size() > 1
                    && min.compareTo((BigInteger) Iterables.getLast(
                            rangeConstraints).getMax()) != 1) {
                throw new IllegalArgumentException(String.format(
                        "Some of the length ranges in %s are not disjoint",
                        rangeArgument));
            }

            rangeConstraints.add(new LengthConstraintEffectiveImpl(min, max,
                    description, reference));
        }

        return rangeConstraints;
    }

    public static boolean isYangPrimitiveTypeString(final String type) {
        return PRIMITIVE_TYPES.contains(type);
    }

    public static boolean isYangBaseTypeString(String typeName) {
        return BASE_TYPES.contains(typeName);
    }

    public static boolean isYangTypeBodyStmt(String typeName) {
        return TYPE_BODY_STMTS.contains(typeName);
    }

    public static TypeDefinition<?> getTypeFromEffectiveStatement(
            EffectiveStatement<?, ?> effectiveStatement) {
        if (effectiveStatement instanceof TypeDefinitionEffectiveBuilder) {
            TypeDefinitionEffectiveBuilder typeDefEffectiveBuilder = (TypeDefinitionEffectiveBuilder) effectiveStatement;
            return typeDefEffectiveBuilder.buildType();
        } else {
            final String typeName = ((TypeDefinition) effectiveStatement)
                    .getQName().getLocalName();
            return baseTypesMap.get(typeName);
        }
    }

    public static TypeDefinition<?> getYangBaseTypeFromString(String typeName) {

        if (baseTypesMap.containsKey(typeName)) {
            return baseTypesMap.get(typeName);
        }
        // else {
        // switch (typeName) {
        // case DECIMAL64:
        // return Decimal64BaseType.create(typedef.getPath(),
        // typedef.getFractionDigits());
        // case BITS:
        // // return BitsBaseType.create(typedef.getPath(),
        // // typedef.getBits());
        // break;
        // case ENUMERATION:
        // // return EnumBaseType.create(typedef.getPath(),
        // // typedef.getEnums(), typedef.getDefaultEnum());
        // break;
        // case IDENTITY_REF:
        // // return IdentityRefBaseType.getInstance(typedef.getPath(),
        // // typedef.getIdentity());
        // break;
        // case INSTANCE_IDENTIFIER:
        // // InstanceIdentifierBaseType.getInstance(???)
        // break;
        // case LEAF_REF:
        // // return LeafRefBaseType.create(typedef.getPath(),
        // // typedef.get??());
        // case UNION:
        // // return UnionBaseType.create(typedef.getPath(),
        // // typedef.getTypes());
        // break;
        // default:
        // break;
        // }
        // }

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
        return BASE_TYPES.contains(o1.getQName().getLocalName());
    }

    // public static TypeDefinition<?> getYangBaseTypeFromString(String
    // typeName, ExtendedTypeEffectiveStatementImpl extendedType) {
    //
    // if (baseTypesMap.containsKey(typeName)) {
    // return baseTypesMap.get(typeName);
    // }
    //
    // // else {
    // // switch (typeName) {
    // // case DECIMAL64:
    // // return Decimal64BaseType.create(extendedType.getPath(),
    // extendedType.getFractionDigits());
    // // case BITS:
    // // // return BitsBaseType.create(extendedType.getPath(),
    // // // extendedType.getBits());
    // // break;
    // // case ENUMERATION:
    // // // return EnumBaseType.create(extendedType.getPath(),
    // // // extendedType.getEnums(), extendedType.getDefaultEnum());
    // // break;
    // // case IDENTITY_REF:
    // // // return IdentityRefBaseType.getInstance(extendedType.getPath(),
    // // // extendedType.getIdentity());
    // // break;
    // // case INSTANCE_IDENTIFIER:
    // // // InstanceIdentifierBaseType.getInstance(???)
    // // break;
    // // case LEAF_REF:
    // // // return LeafRefBaseType.create(extendedType.getPath(),
    // // // extendedType.get??());
    // // case UNION:
    // // // return UnionBaseType.create(extendedType.getPath(),
    // // // extendedType.getTypes());
    // // break;
    // // default:
    // // break;
    // // }
    // // }
    //
    // return null;
    // }
}
