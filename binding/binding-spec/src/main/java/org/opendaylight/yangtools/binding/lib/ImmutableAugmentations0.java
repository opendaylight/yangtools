/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.util.Objects;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.DataContainer;

/**
 * An empty {@link ImmutableAugmentations}.
 *
 * @param <C> the {@link Augmentable} {@link DataContainer} type
 * @since 16.0.1
 */
final class ImmutableAugmentations0<C extends DataContainer & Augmentable<C>> extends ImmutableAugmentations<C> {
    static final @NonNull ImmutableAugmentations0<?> INSTANCE = new ImmutableAugmentations0<>();

    private ImmutableAugmentations0() {
        // hidden on purpose
    }

    @Override
    HashAugmentations<C> withImpl(final Augmentation<C, ?> value) {
        final var ret = new HashAugmentations<C>();
        ret.set(value);
        return ret;
    }

    @Override
    ImmutableAugmentations0<C> withoutImpl(final Class<? extends Augmentation<C, ?>> key) {
        return this;
    }

    @Override
    public boolean containsKey(final Object key) {
        requireNonNull(key);
        return false;
    }

    @Override
    public boolean containsValue(final Object value) {
        Objects.requireNonNull(value);
        return false;
    }

    @Override
    public Set<Entry<Class<? extends Augmentation<C, ?>>, Augmentation<C, ?>>> entrySet() {
        return Set.of();
    }

    @Override
    public Set<Class<? extends Augmentation<C, ?>>> keySet() {
        return Set.of();
    }

    @Override
    public Augmentation<C, ?> get(final Object key) {
        requireNonNull(key);
        return null;
    }

    @Override
    public Augmentation<C, ?> getOrDefault(final Object key, final Augmentation<C, ?> defaultValue) {
        requireNonNull(key);
        return defaultValue;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public int size() {
        return 0;
    }

    @java.io.Serial
    @SuppressWarnings("static-method")
    private Object writeReplace() {
        return new IA0();
    }

    @java.io.Serial
    @SuppressWarnings("static-method")
    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        throw nse();
    }

    @java.io.Serial
    @SuppressWarnings("static-method")
    private void readObjectNoData() throws ObjectStreamException {
        throw nse();
    }

    @java.io.Serial
    @SuppressWarnings("static-method")
    private void writeObject(final ObjectOutputStream stream) throws IOException {
        throw nse();
    }

    private static NotSerializableException nse() {
        return new NotSerializableException(ImmutableAugmentations0.class.getName());
    }
}
