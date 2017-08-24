/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.RangeSet;
import java.util.Objects;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;

/**
 * {@link Immutable} implementation of {@link LengthConstraint}.
 * Length constraint based on supplied parameters with additional behaviour:
 *
 * <ul>
 * <li>{@link LengthConstraint#getErrorAppTag()} returns
 * <code>length-out-of-specified-bounds</code>
 * <li>{@link LengthConstraint#getErrorMessage()} returns <code>The argument is
 * out of bounds &lt;<i>min</i>, <i>max</i> &gt;</code>
 * </ul>
 */
final class LengthConstraintImpl implements LengthConstraint, Immutable {
    private final RangeSet<Integer> ranges;
    private final String description;
    private final String reference;
    private final String errorAppTag;
    private final String errorMessage;

    LengthConstraintImpl(final RangeSet<Integer> ranges, final @Nullable String description,
            final @Nullable String reference) {
        this(ranges, description, reference, null, null);
    }

    LengthConstraintImpl(final RangeSet<Integer> ranges, final @Nullable String description,
            final @Nullable String reference, final String errorAppTag, final String errorMessage) {
        this.ranges = ImmutableRangeSet.copyOf(ranges);
        this.description = description;
        this.reference = reference;
        this.errorAppTag = errorAppTag != null ? errorAppTag : "length-out-of-specified-bounds";
        this.errorMessage = errorMessage != null ? errorMessage : "The argument is out of bounds" + ranges;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getErrorAppTag() {
        return errorAppTag;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public RangeSet<Integer> getAllowedRanges() {
        return ranges;
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, errorAppTag, errorMessage, ranges, reference);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LengthConstraintImpl other = (LengthConstraintImpl) obj;
        return Objects.equals(description, other.description) && Objects.equals(errorAppTag, other.errorAppTag)
            && Objects.equals(errorMessage, other.errorMessage) && ranges.equals(other.ranges)
            && Objects.equals(reference, other.reference);
    }

    @Override
    public String toString() {
        return "LengthConstraintImpl [ranges=" + ranges
            + ", description=" + description
            + ", errorAppTag=" + errorAppTag
            + ", reference=" + reference
            + ", errorMessage=" + errorMessage
            + "]";
    }
}
