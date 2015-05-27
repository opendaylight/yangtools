/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

/**
 * Utility class which provides factory methods to construct Constraints.
 *
 * Provides static factory methods which constructs instances of
 * <ul>
 * <li>{@link LengthConstraint} - {@link #lengthConstraint(Number, Number, String, String)}
 * <li>{@link RangeConstraint} - {@link #rangeConstraint(Number, Number, String, String)}
 * <li>{@link PatternConstraint} - {@link #patternConstraint(String, String, String)}
 * </ul>
 */
public final class BaseConstraints {

    private BaseConstraints() {
    }


    /**
     * Creates a {@link LengthConstraint}.
     *
     * Creates an instance of Length constraint based on supplied parameters
     * with additional behaviour:
     *
     * <ul>
     * <li>{@link LengthConstraint#getErrorAppTag()} returns <code>length-out-of-specified-bounds</code>
     * <li>{@link LengthConstraint#getErrorMessage()} returns <code>The argument is out of bounds &lt;<i>min</i>, <i>max</i> &gt;</code>
     * </ul>
     *
     * @see LengthConstraint
     *
     * @param min  length-restricting lower bound value. The value MUST NOT be negative.
     * @param max length-restricting upper bound value. The value MUST NOT be negative.
     * @param description Description associated with constraint. {@link Optional#absent()} if description is undefined.
     * @param reference Reference associated with constraint. {@link Optional#absent()} if reference is undefined.
     * @return Instance of {@link LengthConstraint}
     */
    public static LengthConstraint newLengthConstraint(final Number min, final Number max, final Optional<String> description,
            final Optional<String> reference) {
        return new LengthConstraintImpl(min, max, description, reference);
    }

    /**
     * Creates a {@link RangeConstraint}.
     *
     * Creates an instance of Range constraint based on supplied parameters
     * with additional behaviour:
     *
     * <ul>
     * <li>{@link RangeConstraint#getErrorAppTag()} returns <code>range-out-of-specified-bounds</code>
     * <li>{@link RangeConstraint#getErrorMessage()} returns <code>The argument is out of bounds &lt;<i>min</i>, <i>max</i> &gt;</code>
     * </ul>
     *
     *
     * @see RangeConstraint
     *
     * @param <T> Type of constraint
     * @param min value-restricting lower bound value. The value MUST NOT Be null.
     * @param max value-restricting upper bound value. The value MUST NOT Be null.
     * @param description Description associated with constraint. {@link Optional#absent()} if description is undefined.
     * @param reference Reference associated with constraint. {@link Optional#absent()} if reference is undefined.
     * @return Instance of {@link RangeConstraint}
     */
    public static <T extends Number> RangeConstraint newRangeConstraint(final T min, final T max, final Optional<String> description,
            final Optional<String> reference) {
        return new RangeConstraintImpl(min, max, description, reference);
    }


    /**
     * Creates a {@link PatternConstraint}.
     *
     * Creates an instance of Pattern constraint based on supplied parameters
     * with additional behaviour:
     *
     * <ul>
     * <li>{@link PatternConstraint#getErrorAppTag()} returns <code>invalid-regular-expression</code>
     * </ul>
     *
     * @see PatternConstraint
     *
     * @param pattern Regular expression, MUST NOT BE null.
     * @param description Description associated with constraint.
     * @param reference Reference associated with constraint.
     * @return Instance of {@link PatternConstraint}
     */
    public static PatternConstraint newPatternConstraint(final String pattern, final Optional<String> description,
            final Optional<String> reference) {
        return new PatternConstraintImpl(pattern, description, reference);
    }


    /**
     * Creates a {@link LengthConstraint}.
     *
     * Creates an instance of Length constraint based on supplied parameters
     * with additional behaviour:
     *
     * <ul>
     * <li>{@link LengthConstraint#getErrorAppTag()} returns <code>length-out-of-specified-bounds</code>
     * <li>{@link LengthConstraint#getErrorMessage()} returns <code>The argument is out of bounds &lt;<i>min</i>, <i>max</i> &gt;</code>
     * </ul>
     *
     * @see LengthConstraint
     *
     * @param min  length-restricting lower bound value. The value MUST NOT be negative.
     * @param max length-restricting upper bound value. The value MUST NOT be negative.
     * @param description Description associated with constraint.
     * @param reference Reference associated with constraint.
     * @return Instance of {@link LengthConstraint}
     * @deprecated Use {@link #newLengthConstraint(Number, Number, Optional, Optional)} instead.
     */
    @Deprecated
    public static LengthConstraint lengthConstraint(final Number min, final Number max, final String description,
            final String reference) {
        return newLengthConstraint(min, max, Optional.fromNullable(description), Optional.fromNullable(reference));
    }

    /**
     * Creates a {@link RangeConstraint}.
     *
     * Creates an instance of Range constraint based on supplied parameters
     * with additional behaviour:
     *
     * <ul>
     * <li>{@link RangeConstraint#getErrorAppTag()} returns <code>range-out-of-specified-bounds</code>
     * <li>{@link RangeConstraint#getErrorMessage()} returns <code>The argument is out of bounds &lt;<i>min</i>, <i>max</i> &gt;</code>
     * </ul>
     *
     *
     * @see RangeConstraint
     *
     * @param min value-restricting lower bound value. The value MUST NOT Be null.
     * @param max value-restricting upper bound value. The value MUST NOT Be null.
     * @param description Description associated with constraint.
     * @param reference Reference associated with constraint.
     * @return Instance of {@link RangeConstraint}
     * @deprecated Use {@link #newRangeConstraint(Number, Number, Optional, Optional)} instead.
     */
    @Deprecated
    public static RangeConstraint rangeConstraint(final Number min, final Number max, final String description,
            final String reference) {
        return newRangeConstraint(min, max, Optional.fromNullable(description), Optional.fromNullable(reference));
    }

    /**
     * Creates a {@link PatternConstraint}.
     *
     * Creates an instance of Range constraint based on supplied parameters
     * with additional behaviour:
     *
     * <ul>
     * <li>{@link PatternConstraint#getErrorAppTag()} returns <code>invalid-regular-expression</code>
     * </ul>
     *
     *
     * @see PatternConstraint
     *
     * @param pattern Regular expression, MUST NOT
     * @param description Description associated with constraint.
     * @param reference Reference associated with constraint.
     * @return Instance of {@link PatternConstraint}
     * @deprecated Use {@link #newPatternConstraint(String, Optional, Optional)} Instead.
     */
    @Deprecated
    public static PatternConstraint patternConstraint(final String pattern, final String description,
            final String reference) {
        return newPatternConstraint(pattern, Optional.fromNullable(description), Optional.fromNullable(reference));
    }
}
