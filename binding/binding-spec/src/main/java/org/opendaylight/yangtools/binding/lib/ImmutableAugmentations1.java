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
import java.util.SequencedMap;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.DataContainer;

/**
 * An empty {@link ImmutableAugmentations}.
 *
 * @param <C> the {@link Augmentable} {@link DataContainer} type
 * @since 16.1.0
 */
final class ImmutableAugmentations1<C extends DataContainer & Augmentable<C>, A extends Augmentation<C, A>>
        extends ImmutableAugmentations<C>
        implements SequencedMap<Class<? extends Augmentation<C, ?>>, Augmentation<C, ?>> {
    private final @NonNull Augmentation<C, ?> only;

    ImmutableAugmentations1(final Augmentation<C, A> only) {
        this.only = requireNonNull(only);
    }

    @Override
    Augmentations<C> withImpl(final Augmentation<C, ?> value) {
        if (value.implementedInterface().equals(only.implementedInterface())) {
            return new ImmutableAugmentations1<>(value);
        }
        final var ret = new HashAugmentations<C>();
        ret.set(only);
        ret.set(value);
        return ret;
    }

    @Override
    ImmutableAugmentations<C> withoutImpl(final Class<? extends Augmentation<C, ?>> key) {
        return key.equals(only.implementedInterface()) ? ImmutableAugmentations.of() : this;
    }

    @Override
    public boolean containsKey(final Object key) {
        return key.equals(only.implementedInterface());
    }

    @Override
    public boolean containsValue(final Object value) {
        return value.equals(only);
    }

    @Override
    public Set<Entry<Class<? extends Augmentation<C, ?>>, Augmentation<C, ?>>> entrySet() {
        return Set.of(firstEntry());
    }

    @Override
    public Set<Class<? extends Augmentation<C, ?>>> keySet() {
        return Set.of(keyOf(only));
    }

    @Override
    public Augmentation<C, ?> get(final Object key) {
        return key.equals(only.implementedInterface()) ? only : null;
    }

    @Override
    public Augmentation<C, ?> getOrDefault(final Object key, final Augmentation<C, ?> defaultValue) {
        return key.equals(only.implementedInterface()) ? only : defaultValue;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public ImmutableAugmentations1<C, A> reversed() {
        return this;
    }

    @Override
    public Entry<Class<? extends Augmentation<C, ?>>, Augmentation<C, ?>> firstEntry() {
        return entryOf(only);
    }

    @Override
    public Entry<Class<? extends Augmentation<C, ?>>, Augmentation<C, ?>> lastEntry() {
        return entryOf(only);
    }

    @Override
    public Entry<Class<? extends Augmentation<C, ?>>, Augmentation<C, ?>> pollFirstEntry() {
        throw uoe();
    }

    @Override
    public Entry<Class<? extends Augmentation<C, ?>>, Augmentation<C, ?>> pollLastEntry() {
        throw uoe();
    }

    @java.io.Serial
    private Object writeReplace() {
        return new IA1(only);
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
        return new NotSerializableException(ImmutableAugmentations1.class.getName());
    }
}
