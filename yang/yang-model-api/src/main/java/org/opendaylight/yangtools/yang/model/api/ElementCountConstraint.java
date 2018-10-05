/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Contains method which returns various data constraints for a list-like YANG element
 * (e.g. min or max number of elements).
 */
@Beta
public abstract class ElementCountConstraint {
    private static final class Min extends ElementCountConstraint {
        private final int minElements;

        Min(final int minElements) {
            checkArgument(minElements >= 0);
            this.minElements = minElements;
        }

        @Override
        public Integer getMinElements() {
            return minElements;
        }

        @Override
        public Integer getMaxElements() {
            return null;
        }
    }

    private static final class Max extends ElementCountConstraint {
        private final int maxElements;

        Max(final int maxElements) {
            checkArgument(maxElements >= 0);
            this.maxElements = maxElements;
        }

        @Override
        public Integer getMinElements() {
            return null;
        }

        @Override
        public Integer getMaxElements() {
            return maxElements;
        }
    }

    private static final class MinMax extends ElementCountConstraint {
        private final int minElements;
        private final int maxElements;

        MinMax(final int minElements, final int maxElements) {
            checkArgument(minElements >= 0);
            checkArgument(maxElements >= 0);
            checkArgument(minElements <= maxElements);
            this.minElements = minElements;
            this.maxElements = maxElements;
        }

        @Override
        public Integer getMinElements() {
            return minElements;
        }

        @Override
        public Integer getMaxElements() {
            return maxElements;
        }
    }

    ElementCountConstraint() {
        // Hidden on purpose
    }

    public static ElementCountConstraint atLeast(final int minElements) {
        return new Min(minElements);
    }

    public static ElementCountConstraint atMost(final int maxElements) {
        return new Max(maxElements);
    }

    public static ElementCountConstraint inRange(final int minElements, final int maxElements) {
        return new MinMax(minElements, maxElements);
    }

    public static Optional<ElementCountConstraint> forNullable(final @Nullable Integer minElements,
            final @Nullable Integer maxElements) {
        if (minElements == null) {
            return maxElements != null ? Optional.of(new Max(maxElements)) : Optional.empty();
        }

        return Optional.of(maxElements != null ? new MinMax(minElements, maxElements) : new Min(minElements));
    }

    /**
     * Returns the minimum required number of data elements for node where this
     * constraint is specified.
     *
     * <p>
     * The returning value equals to value of the argument of the
     * <b>min-elements</b> YANG substatement.
     * It is used with YANG statements leaf-list, list, deviate.
     *
     * @return integer with minimal number of elements, or null if no minimum is defined
     */
    public abstract @Nullable Integer getMinElements();

    /**
     * Returns the maximum admissible number of data elements for node where
     * this constraint is specified.
     *
     * <p>
     * The returning value equals to value of the argument of the
     * <b>max-elements</b> YANG substatement.
     * It is used with YANG statements leaf-list, list, deviate.
     *
     * @return integer with maximum number of elements, or null if no maximum is defined
     */
    public abstract @Nullable Integer getMaxElements();

    @Override
    public final int hashCode() {
        return Objects.hash(getMinElements(), getMaxElements());
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ElementCountConstraint)) {
            return false;
        }
        final ElementCountConstraint other = (ElementCountConstraint) obj;
        return Objects.equals(getMinElements(), other.getMinElements())
                && Objects.equals(getMaxElements(), other.getMaxElements());
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(ElementCountConstraint.class).omitNullValues()
                .add("minElements", getMinElements())
                .add("maxElements", getMaxElements()).toString();
    }
}
