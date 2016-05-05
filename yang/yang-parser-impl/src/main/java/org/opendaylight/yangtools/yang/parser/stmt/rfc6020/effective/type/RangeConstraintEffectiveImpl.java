/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.Objects;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

public class RangeConstraintEffectiveImpl implements RangeConstraint {

    public static final String DEFAULT_REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.2.4";
    private final Number min;
    private final Number max;

    private final String description;
    private final String reference;

    private final String errorAppTag;
    private final String errorMessage;

    public RangeConstraintEffectiveImpl(final Number min, final Number max, final Optional<String> description,
            final Optional<String> reference) {
        this(min, max, description.orNull(), reference.orNull(), "range-out-of-specified-bounds",
                "The argument is out of bounds <" + min + ", " + max + ">");
    }

    public RangeConstraintEffectiveImpl(final Number min, final Number max, final String description,
            final String reference, final String errorAppTag, final String errorMessage) {
        super();
        this.min = Preconditions.checkNotNull(min, "min must not be null.");
        this.max = Preconditions.checkNotNull(max, "max must not be null.");
        this.description = description;
        this.reference = reference;
        this.errorAppTag = Preconditions.checkNotNull(errorAppTag, "Error app tag must not be null.");
        this.errorMessage = Preconditions.checkNotNull(errorMessage, "Error message must not be null.");
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
        final RangeConstraintEffectiveImpl other = (RangeConstraintEffectiveImpl) obj;
        if (!Objects.equals(description, other.description)) {
            return false;
        }
        if (!Objects.equals(max, other.max)) {
            return false;
        }
        if (!Objects.equals(min, other.min)) {
            return false;
        }
        if (!Objects.equals(reference, other.reference)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return RangeConstraintEffectiveImpl.class.getSimpleName() + " [min=" + min + ", max=" + max + ", description="
                + description + ", reference=" + reference + ", errorAppTag=" + errorAppTag + ", errorMessage="
                + errorMessage + "]";
    }
}
