/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import java.util.Optional;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

/**
 * Utility class which provides factory methods to construct Constraints.
 *
 * <p>
 * Provides static factory methods which constructs instances of
 * <ul>
 * <li>{@link LengthConstraint} - {@link #newLengthConstraint(Number, Number, Optional, Optional)}
 * <li>{@link RangeConstraint} - {@link #newRangeConstraint(Number, Number, Optional, Optional)}
 * <li>{@link PatternConstraint} - {@link #newPatternConstraint(String, Optional, Optional)}
 * </ul>
 */
public final class BaseConstraints {
    private BaseConstraints() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a {@link LengthConstraint}.
     *
     * <p>
     * Creates an instance of Length constraint based on supplied parameters
     * with additional behaviour:
     * <ul>
     * <li>{@link LengthConstraint#getErrorAppTag()} returns <code>length-out-of-specified-bounds</code>
     * <li>{@link LengthConstraint#getErrorMessage()} returns <code>The argument is out of bounds
     *     &lt;<i>min</i>, <i>max</i> &gt;</code>
     * </ul>
     *
     * @see LengthConstraint
     *
     * @param min  length-restricting lower bound value. The value MUST NOT be negative.
     * @param max length-restricting upper bound value. The value MUST NOT be negative.
     * @param description Description associated with constraint. {@link Optional#empty()} if description is undefined.
     * @param reference Reference associated with constraint. {@link Optional#empty()} if reference is undefined.
     * @return Instance of {@link LengthConstraint}
     */
    public static LengthConstraint newLengthConstraint(final Number min, final Number max,
            final Optional<String> description, final Optional<String> reference) {
        return new LengthConstraintImpl(min, max, description, reference);
    }

    /**
     * Creates a {@link LengthConstraint}.
     *
     * <p>
     * Creates an instance of Length constraint based on supplied parameters
     * with additional behaviour:
     * <ul>
     * <li>{@link LengthConstraint#getErrorAppTag()} returns <code>length-out-of-specified-bounds</code>
     * <li>{@link LengthConstraint#getErrorMessage()} returns <code>The argument is out of bounds
     *     &lt;<i>min</i>, <i>max</i> &gt;</code>
     * </ul>
     *
     * @see LengthConstraint
     *
     * @param min  length-restricting lower bound value. The value MUST NOT be negative.
     * @param max length-restricting upper bound value. The value MUST NOT be negative.
     * @param description Description associated with constraint. {@link Optional#empty()} if description is undefined.
     * @param reference Reference associated with constraint. {@link Optional#empty()} if reference is undefined.
     * @param errorAppTag error-app-tag associated with constraint.
     * @param errorMessage error message associated with constraint.
     * @return Instance of {@link LengthConstraint}
     */
    public static LengthConstraint newLengthConstraint(final Number min, final Number max,
            final Optional<String> description, final Optional<String> reference, final String errorAppTag,
            final String errorMessage) {
        return new LengthConstraintImpl(min, max, description, reference, errorAppTag, errorMessage);
    }

    /**
     * Creates a {@link RangeConstraint}.
     *
     * <p>
     * Creates an instance of Range constraint based on supplied parameters
     * with additional behaviour:
     * <ul>
     * <li>{@link RangeConstraint#getErrorAppTag()} returns <code>range-out-of-specified-bounds</code>
     * <li>{@link RangeConstraint#getErrorMessage()} returns <code>The argument is out of bounds
     *     &lt;<i>min</i>, <i>max</i> &gt;</code>
     * </ul>
     *
     * @see RangeConstraint
     *
     * @param <T> Type of constraint
     * @param min value-restricting lower bound value. The value MUST NOT Be null.
     * @param max value-restricting upper bound value. The value MUST NOT Be null.
     * @param description Description associated with constraint. {@link Optional#empty()} if description is undefined.
     * @param reference Reference associated with constraint. {@link Optional#empty()} if reference is undefined.
     * @return Instance of {@link RangeConstraint}
     */
    public static <T extends Number> RangeConstraint newRangeConstraint(final T min, final T max,
            final Optional<String> description, final Optional<String> reference) {
        return new RangeConstraintImpl(min, max, description, reference);
    }

    /**
     * Creates a {@link RangeConstraint}.
     *
     * <p>
     * Creates an instance of Range constraint based on supplied parameters
     * with additional behaviour:
     * <ul>
     * <li>{@link RangeConstraint#getErrorAppTag()} returns <code>range-out-of-specified-bounds</code>
     * <li>{@link RangeConstraint#getErrorMessage()} returns <code>The argument is out of bounds
     *     &lt;<i>min</i>, <i>max</i> &gt;</code>
     * </ul>
     *
     * @see RangeConstraint
     *
     * @param <T> Type of constraint
     * @param min value-restricting lower bound value. The value MUST NOT Be null.
     * @param max value-restricting upper bound value. The value MUST NOT Be null.
     * @param description Description associated with constraint. {@link Optional#empty()} if description is undefined.
     * @param reference Reference associated with constraint. {@link Optional#empty()} if reference is undefined.
     * @param errorAppTag error-app-tag associated with constraint.
     * @param errorMessage error message associated with constraint.
     * @return Instance of {@link RangeConstraint}
     */
    public static <T extends Number> RangeConstraint newRangeConstraint(final T min, final T max,
            final Optional<String> description, final Optional<String> reference, final String errorAppTag,
            final String errorMessage) {
        return new RangeConstraintImpl(min, max, description, reference, errorAppTag, errorMessage);
    }

    /**
     * Creates a {@link PatternConstraint}.
     *
     * <p>
     * Creates an instance of Pattern constraint based on supplied parameters
     * with additional behaviour:
     * <ul>
     * <li>{@link PatternConstraint#getErrorAppTag()} returns
     * <code>invalid-regular-expression</code>
     * </ul>
     *
     * @see PatternConstraint
     *
     * @param pattern
     *            Regular expression, MUST NOT BE null.
     * @param description
     *            Description associated with constraint.
     * @param reference
     *            Reference associated with constraint.
     * @return Instance of {@link PatternConstraint}
     */
    public static PatternConstraint newPatternConstraint(final String pattern, final Optional<String> description,
            final Optional<String> reference) {
        return new PatternConstraintImpl(pattern, description, reference);
    }

    /**
     * Creates a {@link PatternConstraint}.
     *
     * <p>
     * Creates an instance of Pattern constraint based on supplied parameters
     * with additional behaviour:
     * <ul>
     * <li>{@link PatternConstraint#getErrorAppTag()} returns
     * <code>invalid-regular-expression</code>
     * </ul>
     *
     * @see PatternConstraint
     *
     * @param pattern
     *            Regular expression, MUST NOT BE null.
     * @param description
     *            Description associated with constraint.
     * @param reference
     *            Reference associated with constraint.
     * @param errorAppTag
     *            error-app-tag associated with constraint.
     * @param errorMessage
     *            error message associated with constraint.
     * @param modifier
     *            Modifier of pattern constraint.
     * @return Instance of {@link PatternConstraint}
     */
    public static PatternConstraint newPatternConstraint(final String pattern, final Optional<String> description,
            final Optional<String> reference, final String errorAppTag, final String errorMessage,
            final Optional<ModifierKind> modifier) {
        return new PatternConstraintImpl(pattern, description, reference, errorAppTag, errorMessage, modifier);
    }
}
