/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractRangeGenerator<T extends Number & Comparable<T>> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractRangeGenerator.class);
    private static final Map<String, AbstractRangeGenerator<?>> GENERATORS;

    private static void addGenerator(final Builder<String, AbstractRangeGenerator<?>> b, final AbstractRangeGenerator<?> generator) {
        b.put(generator.getTypeClass().getCanonicalName(), generator);
    }

    static {
        final Builder<String, AbstractRangeGenerator<?>> b = ImmutableMap.<String, AbstractRangeGenerator<?>> builder();
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
        this.type = Preconditions.checkNotNull(typeClass);
    }

    static AbstractRangeGenerator<?> getInstance(final String canonicalName) {
        return GENERATORS.get(canonicalName);
    }

    /**
     * Return the type's class.
     *
     * @return A class object
     */
    protected final @Nonnull Class<T> getTypeClass() {
        return type;
    }

    /**
     * Return the type's fully-qualified name.
     *
     * @return Fully-qualified name
     */
    protected final @Nonnull String getTypeName() {
        return type.getName();
    }

    protected final @Nonnull T getValue(final Number value) {
        if (type.isInstance(value)) {
            return type.cast(value);
        }

        LOG.info("Number class conversion from {} to {} may lose precision of {}", value.getClass(), type, value);
        return convert(value);
    }

    // FIXME: Once BUG-3399 is fixed, we should never need this
    @Deprecated
    protected abstract T convert(final Number value);

    /**
     * Format a value into a Java-compilable expression which results in the appropriate
     * type.
     * @param number Number value
     * @return Java language string representation
     */
    protected abstract @Nonnull String format(final T number);

    /**
     * Generate the checker method source code.
     * @param checkerName Name of the checker method.
     * @param restrictions Restrictions which need to be applied.
     * @return Method source code.
     */
    protected abstract @Nonnull String generateRangeCheckerImplementation(@Nonnull final String checkerName, @Nonnull final Collection<RangeConstraint> restrictions);

    private static String rangeCheckerName(final String member) {
        final StringBuilder sb = new StringBuilder("check");
        if (member != null) {
            sb.append(member);
        }
        return sb.append("Range").toString();
    }

    String generateRangeChecker(@Nullable final String member, @Nonnull final Collection<RangeConstraint> restrictions) {
        Preconditions.checkArgument(!restrictions.isEmpty(), "Restrictions may not be empty");
        return generateRangeCheckerImplementation(rangeCheckerName(member), restrictions);
    }

    String generateRangeCheckerCall(@Nullable final String member, @Nonnull final String valueReference) {
        return rangeCheckerName(member) + '(' + valueReference + ");\n";
    }
}
