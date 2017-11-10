/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.Beta;

/**
 * Simple DTO holder of constraints placed on the number of elements inside a {@link ListSchemaNode}
 * or a {@link LeafListSchemaNode}, as expressed by min-elements and max-elements. This is an effective model view
 * of these constraints and is clipped to constraints imposed by {@link java.util.Collection#size()}.
 *
 * @author Robert Varga
 */
@Beta
public final class ElementCountConstraint {
    private static final ElementCountConstraint UNCONSTRAINED = new ElementCountConstraint(0, Integer.MAX_VALUE);

    // FIXME: this may need to update to longs once http://cr.openjdk.java.net/~jrose/pres/201207-Arrays-2.pdf
    //        or similar lands in a JRE we support.
    private final int minElements;
    private final int maxElements;

    ElementCountConstraint(final int minElements, final int maxElements) {
        this.minElements = minElements;
        this.maxElements = maxElements;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the minimum required number of data elements for node where this constraint is specified.
     *
     * <p>
     * The returning value equals to value of the argument of the <b>min-elements</b> YANG substatement.
     *
     * @return integer with minimal number of elements, clipped to 0 if not present.
     */
    public int getMinElements() {
        return minElements;
    }

    /**
     * Returns the maximum admissible number of data elements for node where this constraint is specified.
     *
     * <p>
     * The returned value equals to value of the argument of the <b>max-elements</b> YANG substatement, clipped to
     * the range of Integer.
     *
     * @return integer with maximum number of elements, clipped to {@link Integer#MAX_VALUE} if not present or larger.
     */
    public int getMaxElements() {
        return maxElements;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(minElements) * 31 + Integer.hashCode(maxElements);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ElementCountConstraint)) {
            return false;
        }

        final ElementCountConstraint other = (ElementCountConstraint) obj;
        return minElements == other.minElements && maxElements == other.maxElements;
    }

    public static final class Builder implements org.opendaylight.yangtools.concepts.Builder<ElementCountConstraint> {
        private int minElements = 0;
        private int maxElements = Integer.MAX_VALUE;

        Builder() {
            // Hidden on purpose
        }

        public Builder setMinElements(final int minElements) {
            checkArgument(minElements >= 0, "Minimum elements needs to be non-negative");
            this.minElements = minElements;
            return this;
        }

        public Builder setMaxElements(final int maxElements) {
            checkArgument(maxElements >= 0, "Maximum elements needs to be non-negative");
            this.maxElements = maxElements;
            return this;
        }

        @Override
        public ElementCountConstraint build() {
            checkState(minElements <= maxElements, "Inconsistent minimum %s and maximum %s elements", minElements,
                    maxElements);
            return minElements == 0 && maxElements == Integer.MAX_VALUE ? UNCONSTRAINED
                    : new ElementCountConstraint(minElements, maxElements);
        }
    }
}
