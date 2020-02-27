/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi;

import java.util.Optional;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.util.PatternConstraintImpl;

/**
 * Utility class which provides factory methods to construct Constraints.
 *
 * <p>
 * Provides static factory methods which constructs instances of
 * <ul>
 * <li>{@link PatternConstraint} - {@link #newPatternConstraint(String, Optional, Optional)}
 * </ul>
 */
public final class BaseConstraints {
    private BaseConstraints() {
        // Hidden on purpose
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
