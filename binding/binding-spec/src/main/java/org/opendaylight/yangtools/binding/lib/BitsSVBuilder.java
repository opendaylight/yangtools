/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.BitsTypeObject;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * A helper class to build the result of {@link BitsTypeObject#stringValue()}. Instances can be acquired through
 * {@link CodeHelpers#btoSVB()}.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public abstract sealed class BitsSVBuilder {
    static final class Empty extends BitsSVBuilder implements Immutable {
        static final Empty INSTANCE = new Empty();

        private Empty() {
            // hidden on purpose
        }

        @Override
        NonEmpty append(final String bitName) {
            return new NonEmpty(bitName);
        }

        @Override
        public String build() {
            return "";
        }
    }

    private static final class NonEmpty extends BitsSVBuilder implements Mutable {
        private final StringBuilder sb = new StringBuilder();

        NonEmpty(final String bitName) {
            sb.append(bitName);
        }

        @Override
        NonEmpty append(final String bitName) {
            sb.append(' ').append(bitName);
            return this;
        }

        @Override
        public String build() {
            return sb.toString();
        }
    }

    /**
     * Append a bit name it if is present.
     *
     * @param bitName the name of the bit
     * @param present bit presence, if {@code false}, this method does nothing
     * @return this builder
     */
    public final BitsSVBuilder bit(final String bitName, final boolean present) {
        if (bitName.isEmpty()) {
            throw new IllegalArgumentException("empty bit name");
        }
        return present ? append(bitName) : this;
    }

    abstract NonEmpty append(String bitName);

    /**
     * {@return the resulting {@link BitsTypeObject#stringValue()} string}
     */
    public abstract String build();

    // TODO: @DoNotCall/@InlineMe when we can leak Error Prone annotations
    @Override
    @Deprecated(forRemoval = true)
    public final String toString() {
        return MoreObjects.toStringHelper(BitsSVBuilder.class).add("sb", build()).toString();
    }
}
