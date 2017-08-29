/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnresolvedNumber;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRange;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.QNameCacheNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
* util class for manipulating YANG base and extended types implementation
*/
public final class TypeUtils {

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

    private static final Map<String, String> BUILT_IN_TYPES;

    static {
        final Builder<String, String> b = ImmutableMap.builder();

        b.put(BINARY, BINARY);
        b.put(BITS, BITS);
        b.put(BOOLEAN, BOOLEAN);
        b.put(DECIMAL64, DECIMAL64);
        b.put(EMPTY, EMPTY);
        b.put(ENUMERATION, ENUMERATION);
        b.put(IDENTITY_REF,IDENTITY_REF);
        b.put(INSTANCE_IDENTIFIER, INSTANCE_IDENTIFIER);
        b.put(INT8, INT8);
        b.put(INT16, INT16);
        b.put(INT32, INT32);
        b.put(INT64, INT64);
        b.put(LEAF_REF, LEAF_REF);
        b.put(STRING, STRING);
        b.put(UINT8, UINT8);
        b.put(UINT16, UINT16);
        b.put(UINT32, UINT32);
        b.put(UINT64, UINT64);
        b.put(UNION, UNION);

        BUILT_IN_TYPES = b.build();
    }

    private static final Set<String> TYPE_BODY_STMTS = ImmutableSet.of(
        DECIMAL64, ENUMERATION, LEAF_REF, IDENTITY_REF, BITS, UNION);
    private static final Splitter PIPE_SPLITTER = Splitter.on('|').trimResults();
    private static final Splitter TWO_DOTS_SPLITTER = Splitter.on("..").trimResults();

    private TypeUtils() {
    }

    private static BigDecimal yangConstraintToBigDecimal(final Number number) {
        if (UnresolvedNumber.max().equals(number)) {
            return RangeStatementImpl.YANG_MAX_NUM;
        }
        if (UnresolvedNumber.min().equals(number)) {
            return RangeStatementImpl.YANG_MIN_NUM;
        }

        return new BigDecimal(number.toString());
    }

    private static int compareNumbers(final Number n1, final Number n2) {

        final BigDecimal num1 = yangConstraintToBigDecimal(n1);
        final BigDecimal num2 = yangConstraintToBigDecimal(n2);

        return new BigDecimal(num1.toString()).compareTo(new BigDecimal(num2.toString()));
    }

    private static Number parseIntegerConstraintValue(final StmtContext<?, ?, ?> ctx, final String value) {
        if ("max".equals(value)) {
            return UnresolvedNumber.max();
        }
        if ("min".equals(value)) {
            return UnresolvedNumber.min();
        }

        try {
            return new BigInteger(value);
        } catch (final NumberFormatException e) {
            throw new SourceException(ctx.getStatementSourceReference(), e, "Value %s is not a valid integer", value);
        }
    }

    private static Number parseDecimalConstraintValue(final StmtContext<?, ?, ?> ctx, final String value) {
        if ("max".equals(value)) {
            return UnresolvedNumber.max();
        }
        if ("min".equals(value)) {
            return UnresolvedNumber.min();
        }

        try {
            return value.indexOf('.') != -1 ? new BigDecimal(value) : new BigInteger(value);
        } catch (final NumberFormatException e) {
            throw new SourceException(String.format("Value %s is not a valid decimal number", value),
                    ctx.getStatementSourceReference(), e);
        }
    }

    public static List<ValueRange> parseRangeListFromString(final StmtContext<?, ?, ?> ctx,
            final String rangeArgument) {
        final List<ValueRange> ranges = new ArrayList<>();

        for (final String singleRange : PIPE_SPLITTER.split(rangeArgument)) {
            final Iterator<String> boundaries = TWO_DOTS_SPLITTER.split(singleRange).iterator();
            final Number min = parseDecimalConstraintValue(ctx, boundaries.next());

            final Number max;
            if (boundaries.hasNext()) {
                max = parseDecimalConstraintValue(ctx, boundaries.next());

                // if min larger than max then error
                SourceException.throwIf(compareNumbers(min, max) == 1, ctx.getStatementSourceReference(),
                        "Range constraint %s has descending order of boundaries; should be ascending", singleRange);
                SourceException.throwIf(boundaries.hasNext(), ctx.getStatementSourceReference(),
                    "Wrong number of boundaries in range constraint %s", singleRange);
            } else {
                max = min;
            }

            // some of intervals overlapping
            InferenceException.throwIf(ranges.size() > 1
                && compareNumbers(min, Iterables.getLast(ranges).upperBound()) != 1,
                ctx.getStatementSourceReference(),  "Some of the value ranges in %s are not disjoint",
                rangeArgument);
            ranges.add(ValueRange.of(min, max));
        }

        return ranges;
    }

    public static List<ValueRange> parseLengthListFromString(final StmtContext<?, ?, ?> ctx,
            final String lengthArgument) {
        final List<ValueRange> ranges = new ArrayList<>();

        for (final String singleRange : PIPE_SPLITTER.split(lengthArgument)) {
            final Iterator<String> boundaries = TWO_DOTS_SPLITTER.split(singleRange).iterator();
            final Number min = parseIntegerConstraintValue(ctx, boundaries.next());

            final Number max;
            if (boundaries.hasNext()) {
                max = parseIntegerConstraintValue(ctx, boundaries.next());

                // if min larger than max then error
                SourceException.throwIf(compareNumbers(min, max) == 1, ctx.getStatementSourceReference(),
                        "Length constraint %s has descending order of boundaries; should be ascending.", singleRange);
                SourceException.throwIf(boundaries.hasNext(), ctx.getStatementSourceReference(),
                        "Wrong number of boundaries in length constraint %s.", singleRange);
            } else {
                max = min;
            }

            // some of intervals overlapping
            InferenceException.throwIf(ranges.size() > 1
                && compareNumbers(min, Iterables.getLast(ranges).upperBound()) != 1,
                        ctx.getStatementSourceReference(),  "Some of the length ranges in %s are not disjoint",
                        lengthArgument);
            ranges.add(ValueRange.of(min, max));
        }

        return ranges;
    }

    public static boolean isYangTypeBodyStmtString(final String typeName) {
        return TYPE_BODY_STMTS.contains(typeName);
    }

    public static boolean isYangBuiltInTypeString(final String typeName) {
        return BUILT_IN_TYPES.containsKey(typeName);
    }


    public static SchemaPath typeEffectiveSchemaPath(final StmtContext<?, ?, ?> stmtCtx) {
        final SchemaPath path = stmtCtx.getSchemaPath().get();
        final SchemaPath parent = path.getParent();
        final QName parentQName = parent.getLastComponent();
        Preconditions.checkArgument(parentQName != null, "Path %s has an empty parent", path);

        final QName qname = stmtCtx.getFromNamespace(QNameCacheNamespace.class,
            QName.create(parentQName, path.getLastComponent().getLocalName()));
        return parent.createChild(qname);
    }

    /**
     * Checks whether supplied type has any of specified default values marked
     * with an if-feature. This method creates mutable copy of supplied set of
     * default values.
     *
     * @param yangVersion
     *            yang version
     * @param typeStmt
     *            type statement which should be checked
     * @param defaultValues
     *            set of default values which should be checked. The method
     *            creates mutable copy of this set
     *
     * @return true if any of specified default values is marked with an
     *         if-feature, otherwise false
     */
    public static boolean hasDefaultValueMarkedWithIfFeature(final YangVersion yangVersion,
            final TypeEffectiveStatement<?> typeStmt, final Set<String> defaultValues) {
        return !defaultValues.isEmpty() && yangVersion == YangVersion.VERSION_1_1
                && isRelevantForIfFeatureCheck(typeStmt)
                && isAnyDefaultValueMarkedWithIfFeature(typeStmt, new HashSet<>(defaultValues));
    }

    /**
     * Checks whether supplied type has specified default value marked with an
     * if-feature. This method creates mutable set of supplied default value.
     *
     * @param yangVersion
     *            yang version
     * @param typeStmt
     *            type statement which should be checked
     * @param defaultValue
     *            default value to be checked
     *
     * @return true if specified default value is marked with an if-feature,
     *         otherwise false
     */
    public static boolean hasDefaultValueMarkedWithIfFeature(final YangVersion yangVersion,
            final TypeEffectiveStatement<?> typeStmt, final String defaultValue) {
        final HashSet<String> defaultValues = new HashSet<>();
        defaultValues.add(defaultValue);
        return !Strings.isNullOrEmpty(defaultValue) && yangVersion == YangVersion.VERSION_1_1
                && isRelevantForIfFeatureCheck(typeStmt)
                && isAnyDefaultValueMarkedWithIfFeature(typeStmt, defaultValues);
    }

    static String findBuiltinString(final String rawArgument) {
        return BUILT_IN_TYPES.get(rawArgument);
    }

    private static boolean isRelevantForIfFeatureCheck(final TypeEffectiveStatement<?> typeStmt) {
        final TypeDefinition<?> typeDefinition = typeStmt.getTypeDefinition();
        return typeDefinition instanceof EnumTypeDefinition || typeDefinition instanceof BitsTypeDefinition
                || typeDefinition instanceof UnionTypeDefinition;
    }

    private static boolean isAnyDefaultValueMarkedWithIfFeature(final TypeEffectiveStatement<?> typeStmt,
            final Set<String> defaultValues) {
        final Iterator<? extends EffectiveStatement<?, ?>> iter = typeStmt.effectiveSubstatements().iterator();
        while (iter.hasNext() && !defaultValues.isEmpty()) {
            final EffectiveStatement<?, ?> effectiveSubstatement = iter.next();
            if (YangStmtMapping.BIT.equals(effectiveSubstatement.statementDefinition())) {
                final QName bitQName = (QName) effectiveSubstatement.argument();
                if (defaultValues.remove(bitQName.getLocalName()) && containsIfFeature(effectiveSubstatement)) {
                    return true;
                }
            } else if (YangStmtMapping.ENUM.equals(effectiveSubstatement.statementDefinition())
                    && defaultValues.remove(effectiveSubstatement.argument())
                    && containsIfFeature(effectiveSubstatement)) {
                return true;
            } else if (effectiveSubstatement instanceof TypeEffectiveStatement && isAnyDefaultValueMarkedWithIfFeature(
                    (TypeEffectiveStatement<?>) effectiveSubstatement, defaultValues)) {
                return true;
            }
        }

        return false;
    }

    private static boolean containsIfFeature(final EffectiveStatement<?, ?> effectiveStatement) {
        for (final EffectiveStatement<?, ?> effectiveSubstatement : effectiveStatement.effectiveSubstatements()) {
            if (YangStmtMapping.IF_FEATURE.equals(effectiveSubstatement.statementDefinition())) {
                return true;
            }
        }
        return false;
    }
}
