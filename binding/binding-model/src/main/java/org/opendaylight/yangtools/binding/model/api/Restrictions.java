/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

// FIXME: MDSAL-85: specialize for supported types
@Beta
public final class Restrictions {
    private static final @NonNull Restrictions EMPTY = new Restrictions(null, null, ImmutableList.of());

    private final LengthConstraint lengthConstraint;
    // FIXME: MDSAL-85: this is applicable only to int/uint/decimal types and it needs to be captured in the type
    //                  itself
    private final RangeConstraint<?> rangeConstraint;
    private final ImmutableList<PatternConstraint> patternConstraints;

    private Restrictions(final LengthConstraint lengthConstraint, final RangeConstraint<?> rangeConstraint,
            final List<PatternConstraint> patternConstraints) {
        this.lengthConstraint = lengthConstraint;
        this.rangeConstraint = rangeConstraint;
        this.patternConstraints = ImmutableList.copyOf(patternConstraints);
    }

    public static @NonNull Restrictions empty() {
        return EMPTY;
    }

    public static @NonNull Restrictions of(final @Nullable LengthConstraint lengthConstraint) {
        return lengthConstraint == null ? EMPTY : new Restrictions(lengthConstraint, null, ImmutableList.of());
    }

    public static @NonNull Restrictions of(final @Nullable RangeConstraint<?> rangeConstraint) {
        return rangeConstraint == null ? EMPTY : new Restrictions(null, rangeConstraint, ImmutableList.of());
    }

    public static @NonNull Restrictions of(final List<PatternConstraint> patternConstraints,
            final @Nullable LengthConstraint lengthConstraint) {
        return patternConstraints.isEmpty() && lengthConstraint == null ? EMPTY
            : new Restrictions(lengthConstraint, null, patternConstraints);
    }

    public Optional<LengthConstraint> getLengthConstraint() {
        return Optional.ofNullable(lengthConstraint);
    }

    public List<PatternConstraint> getPatternConstraints() {
        return patternConstraints;
    }

    public Optional<RangeConstraint<?>> getRangeConstraint() {
        return Optional.ofNullable(rangeConstraint);
    }

    public boolean isEmpty() {
        return lengthConstraint == null && rangeConstraint == null && patternConstraints.isEmpty();
    }
}
