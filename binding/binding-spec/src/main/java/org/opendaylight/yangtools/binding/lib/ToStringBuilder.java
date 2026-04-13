/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.Arrays;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * A helper class to build a {@link Object#toString()} string for binding constructs.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public final class ToStringBuilder implements Mutable {
    private final StringBuilder sb = new StringBuilder();

    private boolean needComma;

    ToStringBuilder(final String className) {
        sb.append(requireNonNull(className)).append('{');
    }

    public ToStringBuilder appendAugmentations(final Augmentable<?> augmentable) {
        final var augmentations = augmentable.augmentations();
        if (!augmentations.isEmpty()) {
            startProperty(Naming.AUGMENTATION_FIELD).append(augmentations.values().toString());
        }
        return this;
    }

    public ToStringBuilder appendBit(final String name, final boolean present) {
        if (present) {
            startProperty().append(requireNonNull(name));
        }
        return this;
    }

    public ToStringBuilder appendProperty(final String name, final @Nullable Object value) {
        if (value != null) {
            startProperty(name).append(value.toString());
        }
        return this;
    }

    public ToStringBuilder appendProperty(final String name, final byte @Nullable [] value) {
        if (value != null) {
            startProperty(name).append(Arrays.toString(value));
        }
        return this;
    }

    public String build() {
        return sb.append('}').toString();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("string", sb.toString()).toString();
    }

    private StringBuilder startProperty() {
        if (needComma) {
            return sb.append(", ");
        }
        needComma = true;
        return sb;
    }

    private StringBuilder startProperty(final String name) {
        return startProperty().append(requireNonNull(name)).append('=');
    }
}
