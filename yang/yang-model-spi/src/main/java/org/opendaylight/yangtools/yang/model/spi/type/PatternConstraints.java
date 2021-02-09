/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.type;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;

/**
 * Utility class which provides factory methods to construct {@link PatternConstraint}s.
 */
@Beta
@NonNullByDefault
// FIXME: 7.0.0: this seems to be unused, consider its future?
public final class PatternConstraints {
    private PatternConstraints() {
        // Hidden on purpose
    }

    /**
     * Creates a {@link PatternConstraint}.
     *
     * <p>
     * Creates an instance of Pattern constraint based on supplied parameters with additional behaviour:
     * <ul>
     *   <li>{@link PatternConstraint#getErrorAppTag()} returns {@code invalid-regular-expression}</li>
     * </ul>
     *
     * @see PatternConstraint
     * @param pattern Regular expression, MUST NOT BE null.
     * @param description Description associated with constraint.
     * @param reference Reference associated with constraint.
     * @return Instance of {@link PatternConstraint}
     * @throws NullPointerException if {@code pattern} is null
     */
    public static PatternConstraint newPatternConstraint(final String pattern, final @Nullable String description,
            final @Nullable String reference) {
        return new PatternConstraintImpl(pattern, description, reference);
    }

    /**
     * Creates a {@link PatternConstraint}.
     *
     * <p>
     * Creates an instance of Pattern constraint based on supplied parameters with additional behaviour:
     * <ul>
     *   <li>{@link PatternConstraint#getErrorAppTag()} returns {@code invalid-regular-expression}
     * </ul>
     *
     * @see PatternConstraint
     * @param pattern Regular expression, MUST NOT BE null.
     * @param description Description associated with constraint.
     * @param reference Reference associated with constraint.
     * @param errorAppTag {@code error-app-tag} associated with constraint.
     * @param errorMessage {@code error-message} associated with constraint.
     * @param modifier Modifier of pattern constraint.
     * @return Instance of {@link PatternConstraint}
     * @throws NullPointerException if {@code pattern}, {@code errorAppTag} or {@code errorMessage} is null
     */
    public static PatternConstraint newPatternConstraint(final String pattern, final @Nullable String description,
            final @Nullable String reference, final String errorAppTag, final String errorMessage,
            final @Nullable ModifierKind modifier) {
        return new PatternConstraintImpl(pattern, description, reference, errorAppTag, errorMessage, modifier);
    }
}
