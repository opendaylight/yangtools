/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import com.google.common.annotations.Beta;
import java.util.AbstractMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * An immutable {@link Augmentations}.
 *
 * @param <C> the {@link Augmentable} {@link DataContainer} type
 * @since 16.0.1
 */
@Beta
public abstract sealed class ImmutableAugmentations<C extends DataContainer & Augmentable<C>>
        extends AbstractMap<Class<? extends Augmentation<C, ?>>, Augmentation<C, ?>>
        implements Augmentations<C>, Immutable permits ImmutableAugmentations0, ImmutableAugmentations1 {
    /**
     * {@return an empty ImmutableAugmentations}
     *
     * @param <C> the {@link Augmentable} {@link DataContainer} type
     * @return an ImmutableAugmentations instance
     */
    @SuppressWarnings("unchecked")
    public static final <C extends DataContainer & Augmentable<C>> @NonNull ImmutableAugmentations<C> of() {
        return (ImmutableAugmentations<C>) ImmutableAugmentations0.INSTANCE;
    }

    /**
     * {@return an ImmutableAugmentations containing one augmentation}
     *
     * @param <C> the {@link Augmentable} {@link DataContainer} type
     * @param augmentation the augmentation
     * @return an ImmutableAugmentations instance
     * @throws NullPointerException if augmentation is {@code null}
     */
    public static final <C extends DataContainer & Augmentable<C>> @NonNull ImmutableAugmentations<C> of(
            final Augmentation<C, ?> augmentation) {
        return new ImmutableAugmentations1<>(augmentation);
    }

    /**
     * Add an {@link Augmentation}, potentially replacing a previous augmentation of the same type.
     *
     * @param value the {@link Augmentation}
     * @return a replacement instance
     * @throws ClassCastException if value is not consistent with its implemented interface
     * @throws NullPointerException if value is {@code null}
     * @see MutableAugmentations#set(Augmentation)
     */
    public final @NonNull Augmentations<C> with(final Augmentation<C, ?> value) {
        // implemented interface must be a subclass of Augmentation
        @SuppressWarnings("unchecked")
        final var implementedInterface = (Class<? extends Augmentation<C, ?>>) value.implementedInterface()
            .asSubclass(Augmentation.class);
        // implemented interface must actually be implemented
        return withImpl(implementedInterface.cast(value));
    }

    abstract @NonNull Augmentations<C> withImpl(Augmentation<C, ?> value);

    /**
     * Remove the {@link Augmentation} of specified type.
     *
     * @param key the {@link Augmentation} type
     * @return a replacement instance
     * @throws ClassCastException if type is not a subclass of {@link Augmentation}
     * @throws NullPointerException if type is {@code null}
     * @see MutableAugmentations#unset(Class)
     */
    public final @NonNull Augmentations<C> without(final Class<? extends Augmentation<C, ?>> key) {
        @SuppressWarnings("unchecked")
        final var casted = (Class<? extends Augmentation<C, ?>>) key.asSubclass(Augmentation.class);
        return withoutImpl(casted);
    }

    abstract @NonNull Augmentations<C> withoutImpl(Class<? extends Augmentation<C, ?>> key);

    static final <C extends DataContainer & Augmentable<C>>
            Entry<Class<? extends Augmentation<C, ?>>, Augmentation<C, ?>> entryOf(final Augmentation<C, ?> value) {
        return Map.entry(keyOf(value), value);
    }

    static final <C extends DataContainer & Augmentable<C>> Class<? extends Augmentation<C, ?>> keyOf(
            final Augmentation<C, ?> value) {
        @SuppressWarnings("unchecked")
        final var key = (@NonNull Class<? extends Augmentation<C, ?>>) value.implementedInterface()
            .asSubclass(Augmentation.class);
        return key;
    }

    @Override
    public final void clear() {
        throw uoe();
    }

    @Override
    public final Augmentation<C, ?> compute(final Class<? extends Augmentation<C, ?>> key,
            final BiFunction<? super Class<? extends Augmentation<C, ?>>,
                             ? super Augmentation<C, ?>,
                             ? extends Augmentation<C, ?>> remappingFunction) {
        throw uoe();
    }

    @Override
    public final Augmentation<C, ?> computeIfAbsent(final Class<? extends Augmentation<C, ?>> key,
            final Function<? super Class<? extends Augmentation<C, ?>>, ? extends Augmentation<C, ?>> mappingFunction) {
        throw uoe();
    }

    @Override
    public final Augmentation<C, ?> computeIfPresent(final Class<? extends Augmentation<C, ?>> key,
            final BiFunction<? super Class<? extends Augmentation<C, ?>>,
                             ? super Augmentation<C, ?>,
                             ? extends Augmentation<C, ?>> remappingFunction) {
        throw uoe();
    }

    @Override
    public final Augmentation<C, ?> merge(final Class<? extends Augmentation<C, ?>> key, final Augmentation<C, ?> value,
            final BiFunction<? super Augmentation<C, ?>,
                             ? super Augmentation<C, ?>,
                             ? extends Augmentation<C, ?>> remappingFunction) {
        throw uoe();
    }

    @Override
    public final Augmentation<C, ?> put(final Class<? extends Augmentation<C, ?>> key,
            final Augmentation<C, ?> value) {
        throw uoe();
    }

    @Override
    public final void putAll(
            final Map<? extends Class<? extends Augmentation<C, ?>>, ? extends Augmentation<C, ?>> map) {
        throw uoe();
    }

    @Override
    public final Augmentation<C, ?> putIfAbsent(final Class<? extends Augmentation<C, ?>> key,
            final Augmentation<C, ?> value) {
        throw uoe();
    }

    @Override
    public final Augmentation<C, ?> remove(final Object key) {
        throw uoe();
    }

    @Override
    public final boolean remove(final Object key, final Object value) {
        throw uoe();
    }

    @Override
    public final Augmentation<C, ?> replace(final Class<? extends Augmentation<C, ?>> key,
            final Augmentation<C, ?> value) {
        throw uoe();
    }

    @Override
    public final boolean replace(final Class<? extends Augmentation<C, ?>> key, final Augmentation<C, ?> oldValue,
            final Augmentation<C, ?> newValue) {
        throw uoe();
    }

    @Override
    public final void replaceAll(final BiFunction<? super Class<? extends Augmentation<C, ?>>,
                                                  ? super Augmentation<C, ?>,
                                                  ? extends Augmentation<C, ?>> function) {
        throw uoe();
    }

    static final @NonNull UnsupportedOperationException uoe() {
        return new UnsupportedOperationException();
    }
}
