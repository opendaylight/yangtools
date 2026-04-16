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
import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.BindingObject;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * A helper class to build the result of {@link JavaContract#javaTS()}.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public final class JavaTSBuilder implements Mutable {
    private final StringBuilder sb = new StringBuilder();
    private final Collection<? extends Augmentation<?>> augmentations;

    private boolean needComma;

    private JavaTSBuilder(final Collection<? extends Augmentation<?>> augmentations, final Class<?> clazz) {
        this.augmentations = requireNonNull(augmentations);
        sb.append(clazz.getSimpleName()).append('{');
    }

    JavaTSBuilder(final Class<? extends BindingObject> clazz) {
        this(List.of(), clazz);
    }

    <T extends Augmentable<T> & DataContainer> JavaTSBuilder(final T augmentable) {
        this(augmentable.augmentations().values(), augmentable.implementedInterface());
    }

    public JavaTSBuilder bit(final String name, final boolean present) {
        if (present) {
            startProperty().append(requireNonNull(name));
        }
        return this;
    }

    public JavaTSBuilder prop(final String name, final @Nullable Object value) {
        if (value != null) {
            startProperty(name).append(value.toString());
        }
        return this;
    }

    public JavaTSBuilder prop(final String name, final byte @Nullable [] value) {
        if (value != null) {
            startProperty(name).append(Arrays.toString(value));
        }
        return this;
    }

    public String build() {
        if (!augmentations.isEmpty()) {
            // TODO: append (sorted?) augmentations without the intermediate string
            startProperty("augmentation").append(augmentations.toString());
        }
        return sb.append('}').toString();
    }

    @Override
    @Deprecated(forRemoval = true)
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
