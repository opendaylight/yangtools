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
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Link to the original definition of an {@link AbstractExplicitGenerator}.
 */
// FIXME: sealed when we have JDK17+
abstract class OriginalLink<T extends EffectiveStatement<?, ?>> {
    private static final class Complete<T extends EffectiveStatement<?, ?>> extends OriginalLink<T> {
        private final @NonNull AbstractExplicitGenerator<T> original;

        Complete(final AbstractExplicitGenerator<T> original) {
            this.original = requireNonNull(original);
        }

        @Override
        AbstractExplicitGenerator<T> previous() {
            return original;
        }

        @Override
        @NonNull AbstractExplicitGenerator<T> original() {
            return original;
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("original", original);
        }
    }

    private static final class Partial<T extends EffectiveStatement<?, ?>> extends OriginalLink<T> {
        private final @NonNull AbstractExplicitGenerator<T> previous;
        private AbstractExplicitGenerator<T> original;

        Partial(final AbstractExplicitGenerator<T> previous) {
            this.previous = requireNonNull(previous);
        }

        @Override
        AbstractExplicitGenerator<T> previous() {
            return previous;
        }

        @Override
        AbstractExplicitGenerator<T> original() {
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

    static <T extends EffectiveStatement<?, ?>> @NonNull OriginalLink<T> complete(
            final AbstractExplicitGenerator<T> original) {
        return new Complete<>(original);
    }

    static <T extends EffectiveStatement<?, ?>> @NonNull OriginalLink<T> partial(
            final AbstractExplicitGenerator<T> previous) {
        return new Partial<>(previous);
    }

    abstract @NonNull AbstractExplicitGenerator<T> previous();

    abstract @Nullable AbstractExplicitGenerator<T> original();

    abstract ToStringHelper addToStringAttributes(ToStringHelper helper);

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }
}
