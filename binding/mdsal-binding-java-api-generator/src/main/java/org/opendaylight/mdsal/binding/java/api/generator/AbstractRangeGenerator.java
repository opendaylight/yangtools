/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.ConcreteType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractRangeGenerator<T extends Number & Comparable<T>> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractRangeGenerator.class);
    private static final ImmutableMap<String, AbstractRangeGenerator<?>> GENERATORS;

    private static void addGenerator(final Builder<String, AbstractRangeGenerator<?>> builder,
            final AbstractRangeGenerator<?> generator) {
        builder.put(generator.getTypeClass().getCanonicalName(), generator);
    }

    static {
        final Builder<String, AbstractRangeGenerator<?>> b = ImmutableMap.builder();
        addGenerator(b, new ByteRangeGenerator());
        addGenerator(b, new ShortRangeGenerator());
        addGenerator(b, new IntegerRangeGenerator());
        addGenerator(b, new LongRangeGenerator());
        addGenerator(b, new BigDecimalRangeGenerator());
        addGenerator(b, new BigIntegerRangeGenerator());
        GENERATORS = b.build();
    }

    private final Class<T> type;

    protected AbstractRangeGenerator(final Class<T> typeClass) {
        this.type = requireNonNull(typeClass);
    }

    static AbstractRangeGenerator<?> forType(final @NonNull Type type) {
        final ConcreteType javaType = TypeUtils.getBaseYangType(type);
        return GENERATORS.get(javaType.getFullyQualifiedName());
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
     * Return the type's fully-qualified name.
     *
     * @return Fully-qualified name
     */
    protected final @NonNull String getTypeName() {
        return type.getName();
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
        final AbstractRangeGenerator<?> gen = GENERATORS.get(value.getClass().getName());
        final Number check = gen.convert(ret);
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
     * Format a value into a Java-compilable expression which results in the appropriate
     * type.
     *
     * @param value Number value
     * @return Java language string representation
     */
    protected abstract @NonNull String format(T value);

    /**
     * Generate the checker method source code.
     * @param checkerName Name of the checker method.
     * @param constraints Restrictions which need to be applied.
     * @return Method source code.
     */
    protected abstract @NonNull String generateRangeCheckerImplementation(@NonNull String checkerName,
            @NonNull RangeConstraint<?> constraints, Function<Class<?>, String> classImporter);

    private static String rangeCheckerName(final String member) {
        return "check" + member + "Range";
    }

    String generateRangeChecker(final @NonNull String member, final @NonNull RangeConstraint<?> constraints,
            final JavaFileTemplate template) {
        return generateRangeCheckerImplementation(rangeCheckerName(member), constraints, template::importedName);
    }

    String generateRangeCheckerCall(final @NonNull String member, final @NonNull String valueReference) {
        return rangeCheckerName(member) + '(' + valueReference + ");\n";
    }
}
