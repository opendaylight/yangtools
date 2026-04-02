/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.Decimal64Type;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractRangeGenerator<T extends Number & Comparable<T>> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractRangeGenerator.class);
    private static final Map<String, AbstractRangeGenerator<?>> GENERATORS =
        Stream.of(new ByteRangeGenerator(), new ShortRangeGenerator(), new IntegerRangeGenerator(),
            new LongRangeGenerator(), new Uint8RangeGenerator(), new Uint16RangeGenerator(), new Uint32RangeGenerator(),
            new Uint64RangeGenerator())
        .collect(Collectors.toUnmodifiableMap(gen -> gen.getTypeClass().getCanonicalName(), Function.identity()));

    private final @NonNull Class<T> type;

    protected AbstractRangeGenerator(final Class<T> typeClass) {
        type = requireNonNull(typeClass);
    }

    static @NonNull AbstractRangeGenerator<?> forType(final @NonNull Type type) {
        if (type instanceof Decimal64Type decimal64) {
            return new Decimal64RangeGenerator(decimal64);
        }

        final var javaType = TypeUtils.getBaseYangType(type);
        return forName(javaType.canonicalName());
    }

    private static @NonNull AbstractRangeGenerator<?> forName(final String fqcn) {
        return verifyNotNull(GENERATORS.get(fqcn), "Unhandled type %s", fqcn);
    }

    /**
     * Return the type's class.
     *
     * @return A class object
     */
    protected final @NonNull Class<T> getTypeClass() {
        return type;
    }

    /**
     * Return the value in the native type from a particular Number instance.
     *
     * @param value Value as a Number
     * @return Value in native format.
     */
    protected final @NonNull T getValue(final Number value) {
        if (type.isInstance(value)) {
            return type.cast(value);
        }

        LOG.debug("Converting value {} from {} to {}", value, value.getClass(), type);
        final T ret = convert(value);

        // Check if the conversion lost any precision by performing conversion the other way around
        final var gen = value instanceof Decimal64 decimal64 ? new Decimal64RangeGenerator(decimal64)
            : forName(value.getClass().getName());
        final var check = gen.convert(ret);
        if (!value.equals(check)) {
            LOG.warn("Number class conversion from {} to {} truncated value {} to {}", value.getClass(), type, value,
                ret);
        }

        return ret;
    }

    // FIXME: Once BUG-3399 is fixed, we should never need this
    @Deprecated
    protected abstract T convert(Number value);

    /**
     * Generate the checker method source code.
     *
     * @param checkerName Name of the checker method.
     * @param constraints Restrictions which need to be applied.
     * @return Method source code.
     */
    abstract @NonNull BlockBuilder generateRangeCheckerImplementation(@NonNull String checkerName,
            @NonNull RangeConstraint<?> constraints, @NonNull GeneratedClass javaClass);

    // FIXME: appendCheckerName(BlockBuilder bb)
    private static @NonNull String rangeCheckerName(final String member) {
        return "check" + member + "Range";
    }

    @NonNull BlockBuilder generateRangeChecker(final @NonNull String member,
            final @NonNull RangeConstraint<?> constraints, final @NonNull GeneratedClass javaClass) {
        return generateRangeCheckerImplementation(rangeCheckerName(member), constraints, javaClass);
    }

    @NonNullByDefault
    final void appendCheckerCall(final BlockBuilder bb, final String member, final String valueReference) {
        bb.str(rangeCheckerName(member)).str("(").str(valueReference).frg(primitiveRef()).eol(");");
    }

    @Nullable BlockFragment primitiveRef() {
        return null;
    }
}
