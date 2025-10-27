/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Link to the original definition of an {@link AbstractExplicitGenerator}.
 */
abstract sealed class OriginalLink<T extends EffectiveStatement<?, ?>, R extends RuntimeType> {
    private static final class Complete<T extends EffectiveStatement<?, ?>, R extends RuntimeType>
            extends OriginalLink<T, R> {
        private final @NonNull AbstractExplicitGenerator<T, R> original;

        Complete(final AbstractExplicitGenerator<T, R> original) {
            this.original = requireNonNull(original);
        }

        @Override
        AbstractExplicitGenerator<T, R> previous() {
            return original;
        }

        @Override
        @NonNull AbstractExplicitGenerator<T, R> original() {
            return original;
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("original", original);
        }
    }

    private static final class Partial<T extends EffectiveStatement<?, ?>, R extends RuntimeType>
            extends OriginalLink<T, R> {
        private final @NonNull AbstractExplicitGenerator<T, R> previous;
        private AbstractExplicitGenerator<T, R> original;

        Partial(final AbstractExplicitGenerator<T, R> previous) {
            this.previous = requireNonNull(previous);
        }

        @Override
        AbstractExplicitGenerator<T, R> previous() {
            return previous;
        }

        @Override
        AbstractExplicitGenerator<T, R> original() {
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

    static <T extends EffectiveStatement<?, ?>, R extends RuntimeType> @NonNull OriginalLink<T, R> complete(
            final AbstractExplicitGenerator<T, R> original) {
        return new Complete<>(original);
    }

    static <T extends EffectiveStatement<?, ?>, R extends RuntimeType> @NonNull OriginalLink<T, R> partial(
            final AbstractExplicitGenerator<T, R> previous) {
        return new Partial<>(previous);
    }

    abstract @NonNull AbstractExplicitGenerator<T, R> previous();

    abstract @Nullable AbstractExplicitGenerator<T, R> original();

    abstract ToStringHelper addToStringAttributes(ToStringHelper helper);

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }
}
