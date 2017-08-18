/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Preconditions;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

/**
 * {@link Immutable} implementation of {@link RangeConstraint}.
 *
 * <p>
 * Range constraint based on supplied parameters with additional behavior:
 * <ul>
 * <li>{@link RangeConstraint#getErrorAppTag()} returns
 * <code>range-out-of-specified-bounds</code>
 * <li>{@link RangeConstraint#getErrorMessage()} returns <code>The argument is
 * out of bounds &lt;<i>min</i>, <i>max</i> &gt;</code>
 * </ul>
 */
final class RangeConstraintImpl implements RangeConstraint, Immutable {
    private final Number min;
    private final Number max;

    private final String description;
    private final String reference;

    private final String errorAppTag;
    private final String errorMessage;

    RangeConstraintImpl(final Number min, final Number max, final Optional<String> description,
            final Optional<String> reference) {
        this(min, max, description, reference, "range-out-of-specified-bounds", "The argument is out of bounds <" + min
                + ", " + max + ">");
    }

    RangeConstraintImpl(final Number min, final Number max, final Optional<String> description,
            final Optional<String> reference, final String errorAppTag, final String errorMessage) {
        this.min = Preconditions.checkNotNull(min, "min must not be null.");
        this.max = Preconditions.checkNotNull(max, "max must not be null.");
        this.description = description.orElse(null);
        this.reference = reference.orElse(null);
        this.errorAppTag = errorAppTag != null ? errorAppTag : "range-out-of-specified-bounds";
        this.errorMessage = errorMessage != null ? errorMessage : "The argument is out of bounds <" + min + ", " + max
                + ">";
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
    public Number getMin() {
        return min;
    }

    @Override
    public Number getMax() {
        return max;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(description);
        result = prime * result + errorAppTag.hashCode();
        result = prime * result + errorMessage.hashCode();
        result = prime * result + max.hashCode();
        result = prime * result + min.hashCode();
        result = prime * result + Objects.hashCode(reference);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RangeConstraintImpl)) {
            return false;
        }
        final RangeConstraintImpl other = (RangeConstraintImpl) obj;
        return Objects.equals(description, other.description) && Objects.equals(max, other.max)
                && Objects.equals(min, other.min) && Objects.equals(reference, other.reference);
    }

    @Override
    public String toString() {
        return "RangeConstraintImpl [min=" + min + ", max=" + max + ", description=" + description
                + ", reference=" + reference + ", errorAppTag=" + errorAppTag + ", errorMessage=" + errorMessage + "]";
    }
}
