/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Link to the original definition of an {@link AbstractExplicitGenerator}.
 */
// FIXME: sealed when we have JDK17+
abstract class OriginalLink {
    static final class Complete extends OriginalLink {
        private final @NonNull AbstractExplicitGenerator<?> original;

        Complete(final AbstractExplicitGenerator<?> original) {
            this.original = requireNonNull(original);
        }

        @Override
        AbstractExplicitGenerator<?> previous() {
            return original;
        }

        @Override
        @NonNull AbstractExplicitGenerator<?> original() {
            return original;
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("original", original);
        }
    }

    static final class Partial extends OriginalLink {
        private final @NonNull AbstractExplicitGenerator<?> previous;
        private AbstractExplicitGenerator<?> original;

        Partial(final AbstractExplicitGenerator<?> previous) {
            this.previous = requireNonNull(previous);
        }

        @Override
        AbstractExplicitGenerator<?> previous() {
            return previous;
        }

        @Override
        AbstractExplicitGenerator<?> original() {
            if (original == null) {
                final var link = previous.originalLink();
                if (link instanceof Complete || link.previous() != previous) {
                    original = link.original();
                }
            }
            return original;
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("previous", previous).add("original", original);
        }
    }

    private OriginalLink() {
        // Hidden on purpose
    }

    abstract @NonNull AbstractExplicitGenerator<?> previous();

    abstract @Nullable AbstractExplicitGenerator<?> original();

    abstract ToStringHelper addToStringAttributes(ToStringHelper helper);

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }
}
