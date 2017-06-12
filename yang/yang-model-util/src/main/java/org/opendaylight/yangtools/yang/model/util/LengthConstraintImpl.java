/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.Objects;
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

    private final Number min;
    private final Number max;

    private final String description;
    private final String reference;

    private final String errorAppTag;
    private final String errorMessage;

    LengthConstraintImpl(final Number min, final Number max, final Optional<String> description,
            final Optional<String> reference) {
        this(min, max, description, reference, "length-out-of-specified-bounds", "The argument is out of bounds <"
                + min + ", " + max + ">");
    }

    LengthConstraintImpl(final Number min, final Number max, final Optional<String> description,
            final Optional<String> reference, final String errorAppTag, final String errorMessage) {
        this.min = Preconditions.checkNotNull(min, "min must not be null.");
        this.max = Preconditions.checkNotNull(max, "max must not be null");
        this.description = description.orNull();
        this.reference = reference.orNull();
        this.errorAppTag = errorAppTag != null ? errorAppTag : "length-out-of-specified-bounds";
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
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LengthConstraintImpl other = (LengthConstraintImpl) obj;
        if (!Objects.equals(description, other.description)) {
            return false;
        }
        if (!Objects.equals(errorAppTag, other.errorAppTag)) {
            return false;
        }
        if (!Objects.equals(errorMessage, other.errorMessage)) {
            return false;
        }
        if (max != other.max) {
            return false;
        }
        if (min != other.min) {
            return false;
        }
        if (!Objects.equals(reference, other.reference)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "LengthConstraintImpl [min=" + min
            + ", max=" + max
            + ", description=" + description
            + ", errorAppTag=" + errorAppTag
            + ", reference=" + reference
            + ", errorMessage=" + errorMessage
            + "]";
    }
}
