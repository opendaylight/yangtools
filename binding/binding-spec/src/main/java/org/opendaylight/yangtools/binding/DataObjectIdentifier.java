/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.impl.AbstractDataObjectIdentifierBuilder;
import org.opendaylight.yangtools.binding.impl.DataObjectIdentifierBuilder;
import org.opendaylight.yangtools.binding.impl.DataObjectIdentifierBuilderWithKey;
import org.opendaylight.yangtools.binding.impl.DataObjectIdentifierImpl;
import org.opendaylight.yangtools.binding.impl.DataObjectIdentifierWithKey;

/**
 * A {@link DataObjectReference} matching at most one {@link DataObject}, consistent with YANG
 * {@code instance-identifier} addressing as captured by {@link BindingInstanceIdentifier}.
 *
 * @param <T> type of {@link DataObject} held in the last step.
 */
public sealed interface DataObjectIdentifier<T extends DataObject>
        extends DataObjectReference<T>, BindingInstanceIdentifier
        permits DataObjectIdentifier.WithKey, DataObjectIdentifierImpl {
    /**
     * A builder of {@link DataObjectReference} objects.
     *
     * @param <T> type of {@link DataObject} held in the last step.
     */
    sealed interface Builder<T extends DataObject> extends DataObjectReference.Builder<T>
            permits Builder.WithKey, AbstractDataObjectIdentifierBuilder {
        /**
         * A builder of {@link DataObjectReference.WithKey} objects.
         *
         * @param <T> type of {@link EntryObject} held in the last step.
         * @param <K> {@link Key} type
         */
        non-sealed interface WithKey<T extends EntryObject<T, K>, K extends Key<T>>
                extends Builder<T>, DataObjectReference.Builder.WithKey<T, K>
                /* permits DataObjectReferenceBuilderWithKey, KeyedBuilder */ {
            @Override
            DataObjectIdentifier.WithKey<T, K> build();

            @Override
            DataObjectReference.Builder.WithKey<T, K> toReferenceBuilder();
        }

        @Override
        <A extends Augmentation<? super T>> Builder<A> augmentation(Class<A> augmentation);

        @Override
        <N extends ChildOf<? super T>> Builder<N> child(Class<N> container);

        @Override
        <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>> Builder<N> child(
            Class<C> caze, Class<N> container);

        @Override
        <C extends ChoiceIn<? super T> & DataObject, K extends Key<N>, N extends EntryObject<N, K> & ChildOf<? super C>>
            WithKey<N, K> child(Class<C> caze, Class<N> listItem, K listKey);

        @Override
        <N extends EntryObject<N, K> & ChildOf<? super T>, K extends Key<N>> WithKey<N, K> child(
            Class<@NonNull N> listItem, K listKey);

        @Override
        DataObjectIdentifier<T> build();

        /**
         * Returns a {@link DataObjectReference.Builder} equivalent of this builder, allowing
         * {@link InexactDataObjectStep}s to be added, resulting in a {@link DataObjectReference}.
         *
         * @return A {@link DataObjectReference.Builder}
         */
        DataObjectReference.Builder<T> toReferenceBuilder();
    }

    /**
     * A {@link DataObjectIdentifier} pointing to an {@link EntryObject}.
     *
     * @param <K> Key type
     * @param <T> EntryObject type
     */
    sealed interface WithKey<T extends EntryObject<T, K>, K extends Key<T>>
            extends DataObjectIdentifier<T>, DataObjectReference.WithKey<T, K>
            permits DataObjectIdentifierWithKey {
        @Override
        KeyStep<K, T> lastStep();

        @Override
        DataObjectIdentifier.Builder.WithKey<T, K> toBuilder();
    }

    static @NonNull DataObjectIdentifier<?> ofUnsafeSteps(
            final Iterable<? extends @NonNull ExactDataObjectStep<?>> steps) {
        return ofUnsafeSteps(ImmutableList.copyOf(steps));
    }

    static @NonNull DataObjectIdentifier<?> ofUnsafeSteps(
            final List<? extends @NonNull ExactDataObjectStep<?>> steps) {
        return ofUnsafeSteps(ImmutableList.copyOf(steps));
    }

    static @NonNull DataObjectIdentifier<?> ofUnsafeSteps(
            final ImmutableList<? extends @NonNull ExactDataObjectStep<?>> steps) {
        return DataObjectIdentifierImpl.ofUnsafeSteps(steps);
    }

    static <T extends ChildOf<? extends DataRoot<?>>> @NonNull Builder<T> builder(
            final @NonNull Class<T> container) {
        return new DataObjectIdentifierBuilder<>(DataObjectStep.of(container));
    }

    static <C extends ChoiceIn<? extends DataRoot<?>> & DataObject, T extends ChildOf<? super C>>
            @NonNull Builder<T> builder(final @NonNull Class<C> caze, final @NonNull Class<T> container) {
        return new DataObjectIdentifierBuilder<>(DataObjectStep.of(caze, container));
    }

    static <N extends EntryObject<N, K> & ChildOf<? extends DataRoot<?>>, K extends Key<N>>
            Builder.@NonNull WithKey<N, K> builder(final Class<N> listItem, final K listKey) {
        return new DataObjectIdentifierBuilderWithKey<>(new KeyStep<>(listItem, listKey));
    }

    static <C extends ChoiceIn<? extends DataRoot<?>> & DataObject,
            N extends EntryObject<N, K> & ChildOf<? super C>, K extends Key<N>>
            Builder.@NonNull WithKey<N, K> builder(final @NonNull Class<C> caze, final @NonNull Class<N> listItem,
                    final @NonNull K listKey) {
        return new DataObjectIdentifierBuilderWithKey<>(new KeyStep<>(listItem, requireNonNull(caze), listKey));
    }

    static <R extends DataRoot<R>, T extends ChildOf<? super R>>
            @NonNull Builder<T> builderOfInherited(final @NonNull Class<R> root, final @NonNull Class<T> container) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new DataObjectIdentifierBuilder<>(DataObjectStep.of(container));
    }

    static <R extends DataRoot<R>, C extends ChoiceIn<? super R> & DataObject, T extends ChildOf<? super C>>
            @NonNull Builder<T> builderOfInherited(final Class<R> root,
                final Class<C> caze, final Class<T> container) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new DataObjectIdentifierBuilder<>(DataObjectStep.of(caze, container));
    }

    static <R extends DataRoot<R>, N extends EntryObject<N, K> & ChildOf<? super R>, K extends Key<N>>
            Builder.@NonNull WithKey<N, K> builderOfInherited(final @NonNull Class<R> root,
                final @NonNull Class<N> listItem, final @NonNull K listKey) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new DataObjectIdentifierBuilderWithKey<>(new KeyStep<>(listItem, listKey));
    }

    static <R extends DataRoot<R>, C extends ChoiceIn<? super R> & DataObject,
            N extends EntryObject<N, K> & ChildOf<? super C>, K extends Key<N>>
            Builder.@NonNull WithKey<N, K> builderOfInherited(final Class<R> root,
                final Class<C> caze, final Class<N> listItem, final K listKey) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new DataObjectIdentifierBuilderWithKey<>(new KeyStep<>(listItem, requireNonNull(caze), listKey));
    }

    @Override
    Iterable<? extends @NonNull ExactDataObjectStep<?>> steps();

    @Override
    default boolean isExact() {
        return true;
    }

    @Override
    Builder<T> toBuilder();

    @Override
    @Deprecated(since = "14.0.0")
    default boolean isWildcarded() {
        return false;
    }
}
