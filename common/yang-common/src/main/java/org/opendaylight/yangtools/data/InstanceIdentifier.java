/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.data;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;

/**
 *
 */
@NonNullByDefault
public abstract sealed class InstanceIdentifier implements ScalarValue {

    public abstract List<Step> steps();

    public static final InstanceIdentifier of(final List<Step> steps) {
        return switch (steps.size()) {
            case 0 -> throw new IllegalArgumentException("empty steps");
            case 1 -> new SingleStep(steps.getFirst());
            default -> new MultipleSteps(steps);
        };
    }

    /**
     * A single step in {@link InstanceIdentifier}.
     */
    public sealed interface Step permits QName, ItemStep {
        // Nothing else
    }

    /**
     * A {@link Step} identifying a {@code list} or {@code leaf-list} item.
     */
    public sealed interface ItemStep extends Step {

        QName qname();
    }

    public record KeyStep(QName qname, Map<QName, Object> predicates) implements ItemStep {
        public KeyStep {
            requireNonNull(qname);
            if (predicates.isEmpty()) {
                throw new IllegalArgumentException("empty steps");
            }
        }
    }

    public record PositionStep(QName qname, int position) implements ItemStep {
        public PositionStep {
            requireNonNull(qname);
            if (position < 1) {
                throw new IllegalArgumentException("non-positive position " + position);
            }
        }
    }

    public record ValueStep(QName qname, Object value) implements ItemStep {
        public ValueStep {
            requireNonNull(qname);
            requireNonNull(value);
        }
    }

    private static final class MultipleSteps extends InstanceIdentifier {
        private final List<Step> steps;

        MultipleSteps(final List<Step> steps) {
            this.steps = requireNonNull(steps);
        }

        @Override
        public List<Step> steps() {
            return steps;
        }
    }

    private static final class SingleStep extends InstanceIdentifier {
        private final Step step;

        SingleStep(final Step step) {
            this.step = requireNonNull(step);
        }

        @Override
        public List<Step> steps() {
            return List.of(step);
        }
    }
}
