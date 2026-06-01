/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import static java.util.Objects.requireNonNull;

import java.util.HexFormat;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.BitsTypeObject;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * A helper class to build the result of {@link JavaContract#javaTS()}. Instances can be acquired through
 * {@link CodeHelpers#jcTSB(Class)} or {@link CodeHelpers#jcTSB(org.opendaylight.yangtools.binding.Augmentable)}.
 *
 * <p>Implementation is biased towards {@link JavaDataContainer}, but is usable in other contexts as well.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public final class JavaTSBuilder implements Mutable {
    private final StringBuilder sb = new StringBuilder();
    private final List<? extends Augmentation<?>> augmentations;

    private boolean needComma;

    JavaTSBuilder(final Class<?> clazz, final List<? extends Augmentation<?>> augmentations) {
        this.augmentations = requireNonNull(augmentations);
        sb.append(clazz.getSimpleName()).append('{');
    }

    /**
     * Append a {@link BitsTypeObject} component, i.e. the presence of a bit.
     *
     * @param bitName the name of the bit
     * @param present bit presence, if {@code false}, this method does nothing
     * @return this builder
     */
    public JavaTSBuilder bit(final String bitName, final boolean present) {
        if (present) {
            startProperty().append(requireNonNull(bitName));
        }
        return this;
    }

    /**
     * Append a named property component, e.g. a field.
     *
     * @param name property name
     * @param value property value, if {@code null}, this method does nothing
     * @return this builder
     */
    public JavaTSBuilder prop(final String name, final @Nullable Object value) {
        return value == null ? this : addProp(name, value);
    }

    /**
     * Append a named binary property component, e.g. a field.
     *
     * @param name property name
     * @param value property value, if {@code null}, this method does nothing
     * @return this builder
     */
    public JavaTSBuilder prop(final String name, final byte @Nullable [] value) {
        return value == null ? this : addProp(name, value);
    }

    JavaTSBuilder addProp(final String name, final Object value) {
        startProperty(name).append(value.toString());
        return this;
    }

    JavaTSBuilder addProp(final String name, final byte[] value) {
        HexFormat.of().formatHex(startProperty(name), value);
        return this;
    }

    /**
     * {@return the resulting {@link JavaContract#javaTS()} string}
     */
    public String build() {
        if (!augmentations.isEmpty()) {
            final var it = augmentations.iterator();
            startProperty("augmentation").append('[').append(it.next().toString());
            while (it.hasNext()) {
                sb.append(", ").append(it.next().toString());
            }
            sb.append(']');
        }
        return sb.append('}').toString();
    }

    // TODO: @DoNotCall/@InlineMe when we can leak Error Prone annotations
    @Override
    @Deprecated(forRemoval = true)
    public String toString() {
        return "JavaTSBuilder{buf=" + sb + "}";
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
