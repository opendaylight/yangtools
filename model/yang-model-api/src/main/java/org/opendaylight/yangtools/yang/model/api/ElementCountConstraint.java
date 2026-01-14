/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.stmt.ElementCountMatcher;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsArgument;

/**
 * Contains method which returns various data constraints for a list-like YANG element
 * (e.g. min or max number of elements).
 */
@Beta
public abstract sealed class ElementCountConstraint {
    private static final class Min extends ElementCountConstraint {
        private final MinElementsArgument minElements;

        Min(final MinElementsArgument minElements) {
            this.minElements = requireNonNull(minElements);
        }

        @Override
        public MinElementsArgument getMinElements() {
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
            // FIXME: RFC7950 states this needs to be a positive value
            if (maxElements < 0) {
                throw new IllegalArgumentException("maximum elements " + maxElements + " is not non-negative");
            }
            this.maxElements = maxElements;
        }

        @Override
        public MinElementsArgument getMinElements() {
            return null;
        }

        @Override
        public Integer getMaxElements() {
            return maxElements;
        }
    }

    private static final class MinMax extends ElementCountConstraint {
        private final MinElementsArgument minElements;
        private final int maxElements;

        MinMax(final MinElementsArgument minElements, final int maxElements) {
            // FIXME: RFC7950 states this needs to be a positive value
            if (maxElements < 0) {
                throw new IllegalArgumentException("maximum elements " + maxElements + " is not non-negative");
            }
            if (minElements.match(maxElements) instanceof ElementCountMatcher.Failure) {
                throw new IllegalArgumentException("minimum elements " + minElements
                    + " is not less than or equal to maximum elements " + maxElements);
            }

            this.minElements = minElements;
            this.maxElements = maxElements;
        }

        @Override
        public MinElementsArgument getMinElements() {
            return minElements;
        }

        @Override
        public Integer getMaxElements() {
            return maxElements;
        }
    }

    private ElementCountConstraint() {
        // Hidden on purpose
    }

    public static @NonNull ElementCountConstraint atLeast(final MinElementsArgument minElements) {
        return new Min(minElements);
    }

    public static @NonNull ElementCountConstraint atMost(final int maxElements) {
        return new Max(maxElements);
    }

    public static @NonNull ElementCountConstraint inRange(final MinElementsArgument minElements,
            final int maxElements) {
        return new MinMax(minElements, maxElements);
    }

    public static @NonNull Optional<ElementCountConstraint> forNullable(final @Nullable MinElementsArgument minElements,
            final @Nullable Integer maxElements) {
        if (minElements == null) {
            return maxElements != null ? Optional.of(new Max(maxElements)) : Optional.empty();
        }

        return Optional.of(maxElements != null ? new MinMax(minElements, maxElements) : new Min(minElements));
    }

    /**
     * Returns the minimum required number of data elements for node where this constraint is specified.
     *
     * <p>The returning value equals to value of the argument of the <b>min-elements</b> YANG substatement. It is used
     * with YANG statements leaf-list, list, deviate.
     *
     * @return integer with minimal number of elements, or null if no minimum is defined
     */
    public abstract @Nullable MinElementsArgument getMinElements();

    /**
     * Returns the maximum admissible number of data elements for node where
     * this constraint is specified.
     *
     * <p>The returning value equals to value of the argument of the <b>max-elements</b> YANG substatement. It is used
     * with YANG statements leaf-list, list, deviate.
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
        return this == obj || obj instanceof ElementCountConstraint other
            && Objects.equals(getMinElements(), other.getMinElements())
            && Objects.equals(getMaxElements(), other.getMaxElements());
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(ElementCountConstraint.class).omitNullValues()
                .add("minElements", getMinElements())
                .add("maxElements", getMaxElements())
                .toString();
    }
}
