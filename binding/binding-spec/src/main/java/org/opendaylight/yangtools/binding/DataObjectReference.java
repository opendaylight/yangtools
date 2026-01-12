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
import java.io.Serializable;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.impl.AbstractDataObjectReference;
import org.opendaylight.yangtools.binding.impl.AbstractDataObjectReferenceBuilder;
import org.opendaylight.yangtools.binding.impl.DataObjectIdentifierImpl;
import org.opendaylight.yangtools.binding.impl.DataObjectReferenceBuilder;
import org.opendaylight.yangtools.binding.impl.DataObjectReferenceBuilderWithKey;
import org.opendaylight.yangtools.binding.impl.DataObjectReferenceImpl;
import org.opendaylight.yangtools.binding.impl.DataObjectReferenceWithKey;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.KeyedBuilder;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

/**
 * A reference to a {@link DataObject} with semantics partially overlapping with to YANG {@code instance-identifier}.
 *
 * <p>While this indirection is not something defined in YANG, this class hierarchy arises naturally from the Binding
 * specification's Java footprint, which uses {@link DataObject} as the baseline self-sufficient addressable construct.
 * This means users can use a {@link KeyAware} class without specifying the corresponding key -- resulting in an
 * {@link InexactDataObjectStep}.
 *
 * <p>There are two kinds of a reference based on their treatment of such a {@link InexactDataObjectStep}:
 * <ol>
 *   <li>{@link DataObjectIdentifier}, which accepts only {@link ExactDataObjectStep}s and represents
 *       a {@link BindingInstanceIdentifier} pointing to a {@link DataObject}</li>
 *   <li>{@link DataObjectReference}, which accepts any {@link DataObjectStep} and represents path-based matching
 *       criteria for one or more {@link DataObjectIdentifier}s based on
 *       {@link InexactDataObjectStep#matches(DataObjectStep)}.
 * </ol>
 * An explicit conversion to {@link DataObjectIdentifier} can be attempted via {@link #toIdentifier()} method.
 *
 * <p>The legacy {@link InstanceIdentifier} is implements the second kind via its class hierarchy, but indicates its
 * compliance via {@link #isExact()} method. Any {@link DataObjectReference} can be converted into an
 * {@link InstanceIdentifier} via the {@link #toLegacy()} method.
 *
 * @param <T> type of {@link DataObject} held in the last step.
 */
public sealed interface DataObjectReference<T extends DataObject> extends Immutable, PathLike, Serializable
        permits DataObjectIdentifier, DataObjectReference.WithKey, AbstractDataObjectReference {
    /**
     * A builder of {@link DataObjectReference} objects.
     *
     * @param <T> type of {@link DataObject} held in the last step.
     */
    sealed interface Builder<T extends DataObject>
            permits Builder.WithKey, DataObjectIdentifier.Builder, AbstractDataObjectReferenceBuilder {
        /**
         * A builder of {@link DataObjectReference.WithKey} objects.
         *
         * @param <T> type of {@link EntryObject} held in the last step.
         * @param <K> {@link Key} type
         */
        sealed interface WithKey<T extends EntryObject<T, K>, K extends Key<T>> extends Builder<T>
                permits DataObjectIdentifier.Builder.WithKey, DataObjectReferenceBuilderWithKey, KeyedBuilder {
            @Override
            DataObjectReference.WithKey<T, K> build();
        }

        /**
         * Update this builder to build a reference to a specific augmentation of the data object this builder currently
         * points to.
         *
         * @param <A> augmentation type
         * @param augmentation augmentation class
         * @return this builder
         * @throws NullPointerException if {@code augmentation} is {@code null}
         */
        <A extends Augmentation<? super T>> @NonNull Builder<A> augmentation(@NonNull Class<A> augmentation);

        /**
         * Append the specified container as a child of the data object this build currently references. This method
         * should be used when you want to build an instance identifier by appending top-level elements.
         *
         * @param <N> Container type
         * @param container Container to append
         * @return this builder
         * @throws NullPointerException if {@code container} is null
         */
        <N extends ChildOf<? super T>> @NonNull Builder<N> child(@NonNull Class<N> container);

        /**
         * Append the specified container as a child of the data object this build currently references. This method
         * should be used when you want to build an reference by appending a container node to the identifier and the
         * {@code container} is defined in a {@code grouping} used in a {@code case} statement.
         *
         * @param <C> Case type
         * @param <N> Container type
         * @param caze Choice case class
         * @param container Container to append
         * @return this builder
         * @throws NullPointerException if {@code container} is null
         */
        <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>> @NonNull Builder<N> child(
                Class<C> caze, @NonNull Class<N> container);

        /**
         * Append the specified listItem as a child of the data object this build currently references. This method
         * should be used when you want to build a reference by appending a specific list element to the identifier.
         *
         * @param <N> List type
         * @param <K> Key type
         * @param listItem List to append
         * @param listKey List key
         * @return this builder
         * @throws NullPointerException if any argument is null
         */
        <N extends EntryObject<N, K> & ChildOf<? super T>, K extends Key<N>> @NonNull WithKey<N, K> child(
                @NonNull Class<@NonNull N> listItem, @NonNull K listKey);

        /**
         * Append the specified listItem as a child of the data object this build currently references. This
         * method should be used when you want to build a reference by appending a specific list element to the
         * identifier and the {@code list} is defined in a {@code grouping} used in a {@code case} statement.
         *
         * @param <C> Case type
         * @param <N> List type
         * @param <K> Key type
         * @param caze Choice case class
         * @param listItem List to append
         * @param listKey List key
         * @return this builder
         * @throws NullPointerException if any argument is null
         */
        <C extends ChoiceIn<? super T> & DataObject, K extends Key<N>, N extends EntryObject<N, K> & ChildOf<? super C>>
            @NonNull WithKey<N, K> child(@NonNull Class<C> caze, @NonNull Class<N> listItem, @NonNull K listKey);

        /**
         * Build the data object reference.
         *
         * @return resulting {@link DataObjectReference}.
         */
        @NonNull DataObjectReference<T> build();
    }

    /**
     * A {@link DataObjectReference} pointing to a {@link EntryObject}.
     *
     * @param <K> Key type
     * @param <T> EntryObject type
     */
    sealed interface WithKey<T extends EntryObject<T, K>, K extends Key<T>> extends DataObjectReference<T>, KeyAware<K>
            permits DataObjectIdentifier.WithKey, DataObjectReferenceWithKey, KeyedInstanceIdentifier {
        @Override
        KeyStep<K, T> lastStep();

        @Override
        Builder.WithKey<T, K> toBuilder();

        @Override
        DataObjectIdentifier.WithKey<T, K> toIdentifier();

        /**
         * Return a legacy {@link KeyedInstanceIdentifier} for this reference.
         *
         * @return A {@link KeyedInstanceIdentifier}.
         */
        @Override
        @SuppressWarnings("unchecked")
        default @NonNull KeyedInstanceIdentifier<T, K> toLegacy() {
            return (KeyedInstanceIdentifier<T, K>) InstanceIdentifier.<T>unsafeOf(ImmutableList.copyOf(steps()));
        }

        @Override
        default K key() {
            return lastStep().key();
        }

        /**
         * Return the key attached to this identifier. This method is equivalent to calling
         * {@link InstanceIdentifier#keyOf(InstanceIdentifier)}.
         *
         * @return Key associated with this instance identifier.
         * @deprecated Use {@link #key()} instead.
         */
        @Deprecated(since = "14.0.0")
        default @NonNull K getKey() {
            return key();
        }
    }

    /**
     * Create a new {@link Builder} initialized to produce a reference equal to this one.
     *
     * @return A builder instance
     * @deprecated Use {@link #toBuilder()} instead.
     */
    @Deprecated(since = "14.0.0")
    default @NonNull Builder<T> builder() {
        return toBuilder();
    }

    static <T extends ChildOf<? extends DataRoot<?>>> @NonNull Builder<T> builder(final @NonNull Class<T> container) {
        return new DataObjectReferenceBuilder<>(DataObjectStep.of(container));
    }

    static <C extends ChoiceIn<? extends DataRoot<?>> & DataObject, T extends ChildOf<? super C>>
            @NonNull Builder<T> builder(final @NonNull Class<C> caze, final @NonNull Class<T> container) {
        return new DataObjectReferenceBuilder<>(DataObjectStep.of(caze, container));
    }

    static <N extends EntryObject<N, K> & ChildOf<? extends DataRoot<?>>, K extends Key<N>>
            Builder.@NonNull WithKey<N, K> builder(final Class<N> listItem, final K listKey) {
        return new DataObjectReferenceBuilderWithKey<>(new KeyStep<>(listItem, listKey));
    }

    static <C extends ChoiceIn<? extends DataRoot<?>> & DataObject,
            N extends EntryObject<N, K> & ChildOf<? super C>, K extends Key<N>>
            Builder.@NonNull WithKey<N, K> builder(final @NonNull Class<C> caze, final @NonNull Class<N> listItem,
                final @NonNull K listKey) {
        return new DataObjectReferenceBuilderWithKey<>(new KeyStep<>(listItem, requireNonNull(caze), listKey));
    }

    static <R extends DataRoot<R>, T extends ChildOf<? super R>>
            @NonNull Builder<T> builderOfInherited(final @NonNull Class<R> root, final @NonNull Class<T> container) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new DataObjectReferenceBuilder<>(DataObjectStep.of(container));
    }

    static <R extends DataRoot<R>, C extends ChoiceIn<? super R> & DataObject, T extends ChildOf<? super C>>
            @NonNull Builder<T> builderOfInherited(final Class<R> root,
                final Class<C> caze, final Class<T> container) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new DataObjectReferenceBuilder<>(DataObjectStep.of(caze, container));
    }

    static <R extends DataRoot<R>, N extends EntryObject<N, K> & ChildOf<? super R>, K extends Key<N>>
            Builder.@NonNull WithKey<N, K> builderOfInherited(final @NonNull Class<R> root,
                final @NonNull Class<N> listItem, final @NonNull K listKey) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new DataObjectReferenceBuilderWithKey<>(new KeyStep<>(listItem, listKey));
    }

    static <R extends DataRoot<R>, C extends ChoiceIn<? super R> & DataObject,
            N extends EntryObject<N, K> & ChildOf<? super C>, K extends Key<N>>
                Builder.@NonNull WithKey<N, K> builderOfInherited(final Class<R> root,
                    final Class<C> caze, final Class<N> listItem, final K listKey) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new DataObjectReferenceBuilderWithKey<>(new KeyStep<>(listItem, requireNonNull(caze), listKey));
    }

    static @NonNull DataObjectReference<?> ofUnsafeSteps(final Iterable<? extends @NonNull DataObjectStep<?>> steps) {
        return ofUnsafeSteps(ImmutableList.copyOf(steps));
    }

    static @NonNull DataObjectReference<?> ofUnsafeSteps(final List<? extends @NonNull DataObjectStep<?>> steps) {
        return ofUnsafeSteps(ImmutableList.copyOf(steps));
    }

    @SuppressWarnings("unchecked")
    static @NonNull DataObjectReference<?> ofUnsafeSteps(
            final ImmutableList<? extends @NonNull DataObjectStep<?>> steps) {
        if (steps.stream().allMatch(ExactDataObjectStep.class::isInstance)) {
            return DataObjectIdentifierImpl.ofUnsafeSteps(
                (ImmutableList<? extends @NonNull ExactDataObjectStep<?>>) steps);
        }
        return DataObjectReferenceImpl.ofUnsafeSteps(steps);
    }

    /**
     * Return the steps of this reference. Returned {@link Iterable} does not support removals and contains one or more
     * non-null items.
     *
     * @return the steps of this reference
     */
    @NonNull Iterable<? extends @NonNull DataObjectStep<?>> steps();

    /**
     * Return the last step of this reference.
     *
     * @return the last step
     */
    @NonNull DataObjectStep<T> lastStep();

    /**
     * Create a new {@link Builder} initialized to produce a reference equal to this one.
     *
     * @return A builder instance
     */
    @NonNull Builder<T> toBuilder();

    /**
     * Return a {@link DataObjectIdentifier} view of this reference, if possible.
     *
     * @return A {@link DataObjectIdentifier}
     * @throws UnsupportedOperationException if this reference is not compatible with {@link DataObjectIdentifier}
     */
    @NonNull DataObjectIdentifier<T> toIdentifier();

    /**
     * Returns {@code true} if this reference is composed solely of {@link ExactDataObjectStep}s.
     *
     * @implNote
     *     The default implementation returns {@code false} to simplify implementation hierarchy.
     * @return {@code true} if this reference is composed solely of {@link ExactDataObjectStep}s
     */
    default boolean isExact() {
        return false;
    }

    /**
     * Return a legacy {@link InstanceIdentifier} for this reference.
     *
     * @return An {@link InstanceIdentifier}.
     */
    @Deprecated(since = "14.0.23", forRemoval = true)
    default @NonNull InstanceIdentifier<T> toLegacy() {
        return InstanceIdentifier.unsafeOf(ImmutableList.copyOf(steps()));
    }

    /**
     * Return the steps of this reference. Returned {@link Iterable} does not support removals and contains one or more
     * non-null ite,s.
     *
     * @return the steps of this reference
     * @deprecated Use {@link #steps()} instead.
     */
    @Deprecated(since = "14.0.0")
    default @NonNull Iterable<? extends @NonNull DataObjectStep<?>> getPathArguments() {
        return steps();
    }

    /**
     * Returns {@code true} if this reference contains an {@link InexactDataObjectStep}s.
     *
     * @implSpec
     *     The default implementation returns {@code true} to simplify implementation hierarchy.
     * @return {@code true} if this reference contains an {@link InexactDataObjectStep}
     * @deprecated Use negated result of {@link #isExact()} instead
     */
    @Deprecated(since = "14.0.0")
    default boolean isWildcarded() {
        return true;
    }
}
