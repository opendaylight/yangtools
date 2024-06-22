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
import org.eclipse.jdt.annotation.NonNull;

/**
 * A {@link BindingInstanceIdentifier} referencing some {@link DataObject}.
 */
public abstract sealed class DataObjectIdentifier<T extends DataObject>
        extends DataObjectReference<ExactDataObjectStep<?>, T>
        implements BindingInstanceIdentifier {
    /**
     * A simple {@link DataObjectIdentifier}.
     */
    public static final class Simple<T extends DataObject> extends DataObjectIdentifier<T> {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        private final @NonNull NodeStep<T> lastStep;

        private Simple(final ImmutableList<ExactDataObjectStep<?>> steps, final NodeStep<T> lastStep) {
            super(steps);
            this.lastStep = requireNonNull(lastStep);
        }

        @Override
        public NodeStep<T> lastStep() {
            return lastStep;
        }

        @Override
        public Builder<T> toBuilder() {
            return new RegularBuilder<>(this);
        }
    }

    /**
     * A {@link DataObjectIdentifier} to a {@link KeyAware} data object.
     */
    public static final class WithKey<T extends DataObject & KeyAware<K>, K extends Key<T>>
            extends DataObjectIdentifier<T> {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        private final @NonNull KeyStep<K, T> lastStep;

        private WithKey(final ImmutableList<ExactDataObjectStep<?>> steps, final KeyStep<K, T> lastStep) {
            super(steps);
            this.lastStep = requireNonNull(lastStep);
        }

        @Override
        public KeyStep<K, T> lastStep() {
            return lastStep;
        }

        public @NonNull K key() {
            return lastStep.key();
        }

        @Override
        public WithKeyBuilder<T, K> toBuilder() {
            return new WithKeyBuilder<>(this);
        }
    }

    // FIXME: we should have a 'WithPosition' specialization as well

    /**
     * A builder of {@link DataObjectIdentifier} objects.
     *
     * @param <T> target {@link DataObject} type
     */
    public abstract static sealed class Builder<T extends DataObject>
            extends DataObjectReferenceBuilder<ExactDataObjectStep<?>, T> {
        private final ImmutableList.Builder<ExactDataObjectStep<?>> pathBuilder;
        private final ImmutableList<ExactDataObjectStep<?>> basePath;

        Builder(final Builder<?> prev, final ExactDataObjectStep<?> item) {
            pathBuilder = prev.pathBuilder;
            basePath = prev.basePath;
            appendItem(item);
        }

        Builder(final DataObjectIdentifier<T> base) {
            pathBuilder = ImmutableList.builder();
            basePath = base.steps;
        }

        Builder(final ExactDataObjectStep<?> item) {
            pathBuilder = ImmutableList.builder();
            basePath = null;
            pathBuilder.add(item);
        }

        /**
         * Build an identifier which refers to a specific augmentation of the current InstanceIdentifier referenced by
         * the builder.
         *
         * @param container augmentation class
         * @param <N> augmentation type
         * @return this builder
         * @throws NullPointerException if {@code container} is null
         */
        public final <N extends DataObject & Augmentation<? super T>> Builder<N> augmentation(
                final Class<N> container) {
            return append(new NodeStep<>(container));
        }

        /**
         * Append the specified container as a child of the current InstanceIdentifier referenced by the builder. This
         * method should be used when you want to build an instance identifier by appending top-level elements, for
         * example
         * <pre>
         *     InstanceIdentifier.builder().child(Nodes.class).build();
         * </pre>
         *
         * <p>
         * NOTE :- The above example is only for illustration purposes InstanceIdentifier.builder() has been deprecated
         * and should not be used. Use InstanceIdentifier.builder(Nodes.class) instead
         *
         * @param container Container to append
         * @param <N> Container type
         * @return this builder
         * @throws NullPointerException if {@code container} is null
         */
        public final <N extends ChildOf<? super T>> Builder<N> child(final Class<N> container) {
            return append(nodeStep(null, container));
        }

        /**
         * Append the specified container as a child of the current InstanceIdentifier referenced by the builder. This
         * method should be used when you want to build an instance identifier by appending a container node to the
         * identifier and the {@code container} is defined in a {@code grouping} used in a {@code case} statement.
         *
         * @param caze Choice case class
         * @param container Container to append
         * @param <C> Case type
         * @param <N> Container type
         * @return this builder
         * @throws NullPointerException if {@code container} is null
         */
        public final <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>> Builder<N> child(
                final Class<C> caze, final Class<N> container) {
            return append(nodeStep(caze, container));
        }

        /**
         * Append the specified listItem as a child of the current InstanceIdentifier referenced by the builder. This
         * method should be used when you want to build an instance identifier by appending a specific list element to
         * the identifier.
         *
         * @param listItem List to append
         * @param listKey List key
         * @param <N> List type
         * @param <K> Key type
         * @return this builder
         * @throws NullPointerException if any argument is null
         */
        public final <N extends KeyAware<K> & ChildOf<? super T>, K extends Key<N>> WithKeyBuilder<N, K> child(
                final Class<@NonNull N> listItem, final K listKey) {
            return append(new KeyStep<>(listItem, listKey));
        }

        /**
         * Append the specified listItem as a child of the current InstanceIdentifier referenced by the builder. This
         * method should be used when you want to build an instance identifier by appending a specific list element to
         * the identifier and the {@code list} is defined in a {@code grouping} used in a {@code case} statement.
         *
         * @param caze Choice case class
         * @param listItem List to append
         * @param listKey List key
         * @param <C> Case type
         * @param <N> List type
         * @param <K> Key type
         * @return this builder
         * @throws NullPointerException if any argument is null
         */
        public final <C extends ChoiceIn<? super T> & DataObject, K extends Key<N>,
                N extends KeyAware<K> & ChildOf<? super C>> WithKeyBuilder<N, K> child(final Class<C> caze,
                    final Class<N> listItem, final K listKey) {
            return append(new KeyStep<>(listItem, requireNonNull(caze), listKey));
        }

        /**
         * Build the {@link DataObjectIdentifier}.
         *
         * @return Resulting {@link DataObjectIdentifier}.
         */
        @Override
        public abstract @NonNull DataObjectIdentifier<T> build();

        final ImmutableList<ExactDataObjectStep<?>> steps() {
            final var path = pathBuilder.build();
            return basePath == null ? path
                : ImmutableList.<ExactDataObjectStep<?>>builderWithExpectedSize(basePath.size() + path.size())
                    .addAll(basePath)
                    .addAll(path)
                    .build();
        }

        final void appendItem(final ExactDataObjectStep<?> item) {
            pathBuilder.add(item);
        }

        abstract <X extends DataObject> @NonNull RegularBuilder<X> append(ExactDataObjectStep<X> step);

        abstract <X extends DataObject & KeyAware<Y>, Y extends Key<X>> @NonNull WithKeyBuilder<X, Y> append(
            KeyStep<Y, X> step);
    }

    public static final class WithKeyBuilder<T extends DataObject & KeyAware<K>, K extends Key<T>>
            extends Builder<T> {
        private @NonNull KeyStep<K, T> lastStep;

        WithKeyBuilder(final KeyStep<K, T> firstStep) {
            super(firstStep);
            lastStep = requireNonNull(firstStep);
        }

        WithKeyBuilder(final WithKey<T, K> base) {
            super(base);
            lastStep = base.lastStep();
        }

        private WithKeyBuilder(final RegularBuilder<?> prev, final KeyStep<K, T> lastStep) {
            super(prev, lastStep);
            this.lastStep = requireNonNull(lastStep);
        }

        /**
         * Build the instance identifier.
         *
         * @return Resulting {@link KeyedInstanceIdentifier}.
         */
        @Override
        public WithKey<T, K> build() {
            return new WithKey<>(steps(), lastStep);
        }

        @Override
        <X extends DataObject> @NonNull RegularBuilder<X> append(final ExactDataObjectStep<X> step) {
            return new RegularBuilder<>(this, step);
        }

        @Override
        @SuppressWarnings("unchecked")
        <X extends DataObject & KeyAware<Y>, Y extends Key<X>> WithKeyBuilder<X, Y> append(final KeyStep<Y, X> step) {
            appendItem(step);
            lastStep = (KeyStep<K, T>) requireNonNull(step);
            return (WithKeyBuilder<X, Y>) this;
        }
    }

    private static final class RegularBuilder<T extends DataObject> extends Builder<T> {
        private @NonNull ExactDataObjectStep<T> lastStep;

        RegularBuilder(final ExactDataObjectStep<T> step) {
            super(step);
            lastStep = requireNonNull(step);
        }

        RegularBuilder(final DataObjectIdentifier<T> base) {
            super(base);
            lastStep = base.lastStep();
        }

        private RegularBuilder(final WithKeyBuilder<?, ?> prev, final ExactDataObjectStep<T> step) {
            super(prev, step);
            lastStep = requireNonNull(step);
        }

        @Override
        public DataObjectIdentifier<T> build() {
            final var steps = steps();
            return switch (lastStep) {
                case NodeStep<T> last -> new Simple<>(steps, last);
                case KeyStep<?, ?> last -> new WithKey(steps, last);
            };
        }

        @Override
        @SuppressWarnings("unchecked")
        <X extends DataObject> RegularBuilder<X> append(final ExactDataObjectStep<X> step) {
            appendItem(step);
            lastStep = (ExactDataObjectStep<T>) requireNonNull(step);
            return (RegularBuilder<X>) this;
        }

        @Override
        <X extends DataObject & KeyAware<Y>, Y extends Key<X>> WithKeyBuilder<X, Y> append(final KeyStep<Y, X> item) {
            return new WithKeyBuilder<>(this, item);
        }
    }

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final @NonNull ImmutableList<ExactDataObjectStep<?>> steps;

    private DataObjectIdentifier(final ImmutableList<ExactDataObjectStep<?>> steps) {
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("empty steps");
        }
        this.steps = steps;
    }

    public static final @NonNull DataObjectIdentifier<?> unsafeOf(final ImmutableList<ExactDataObjectStep<?>> steps) {
        return switch (steps.getLast()) {
            case NodeStep<?> lastStep -> new Simple<>(steps, lastStep);
            case KeyStep<?, ?> lastStep -> new WithKey<>(steps, lastStep);
        };
    }

    /**
     * Create a {@link Builder} for a specific type of InstanceIdentifier as specified by container.
     *
     * @param container Base container
     * @param <T> Type of the container
     * @return A new {@link Builder}
     * @throws NullPointerException if {@code container} is null
     */
    public static <T extends ChildOf<? extends DataRoot>> @NonNull Builder<T> builder(
            final Class<T> container) {
        return new RegularBuilder<>(nodeStep(null, container));
    }

    /**
     * Create a {@link Builder} for a specific type of {@link DataObjectIdentifier} as specified by container in
     * a {@code grouping} used in the {@code case} statement.
     *
     * @param caze Choice case class
     * @param container Base container
     * @param <C> Case type
     * @param <T> Type of the container
     * @return A new {@link Builder}
     * @throws NullPointerException if any argument is null
     */
    public static <C extends ChoiceIn<? extends DataRoot> & DataObject, T extends ChildOf<? super C>>
            @NonNull Builder<T> builder(final Class<C> caze, final Class<T> container) {
        return new RegularBuilder<>(nodeStep(caze, container));
    }

    /**
     * Create a {@link Builder} for a specific type of {@link DataObjectIdentifier} which represents {@link KeyStep}.
     *
     * @param listItem list item class
     * @param listKey key value
     * @param <N> List type
     * @param <K> List key
     * @return A new {@link Builder}
     * @throws NullPointerException if any argument is null
     */
    public static <N extends KeyAware<K> & ChildOf<? extends DataRoot>, K extends Key<N>>
            @NonNull WithKeyBuilder<N, K> builder(final Class<N> listItem, final K listKey) {
        return new WithKeyBuilder<>(new KeyStep<>(listItem, listKey));
    }

    /**
     * Create a {@link Builder} for a specific type of {@link DataObjectIdentifier} which represents a {@link KeyStep}
     * in a {@code grouping} used in the {@code case} statement.
     *
     * @param caze Choice case class
     * @param listItem list item class
     * @param listKey key value
     * @param <C> Case type
     * @param <N> List type
     * @param <K> List key
     * @return A new {@link Builder}
     * @throws NullPointerException if any argument is null
     */
    public static <C extends ChoiceIn<? extends DataRoot> & DataObject,
            N extends KeyAware<K> & ChildOf<? super C>, K extends Key<N>>
            @NonNull WithKeyBuilder<N, K> builder(final Class<C> caze, final Class<N> listItem, final K listKey) {
        return new WithKeyBuilder<>(new KeyStep<>(listItem, requireNonNull(caze), listKey));
    }

    public static <R extends DataRoot & DataObject, T extends ChildOf<? super R>>
            @NonNull Builder<T> builderOfInherited(final Class<R> root, final Class<T> container) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new RegularBuilder<>(nodeStep(null, container));
    }

    public static <R extends DataRoot & DataObject, C extends ChoiceIn<? super R> & DataObject,
            T extends ChildOf<? super C>>
            @NonNull Builder<T> builderOfInherited(final Class<R> root,
                final Class<C> caze, final Class<T> container) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new RegularBuilder<>(nodeStep(caze, container));
    }

    public static <R extends DataRoot & DataObject, N extends KeyAware<K> & ChildOf<? super R>, K extends Key<N>>
            @NonNull WithKeyBuilder<N, K> builderOfInherited(final Class<R> root,
                final Class<N> listItem, final K listKey) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new WithKeyBuilder<>(new KeyStep<>(listItem, listKey));
    }

    public static <R extends DataRoot & DataObject, C extends ChoiceIn<? super R> & DataObject,
            N extends KeyAware<K> & ChildOf<? super C>, K extends Key<N>>
            @NonNull WithKeyBuilder<N, K> builderOfInherited(final Class<R> root,
                final Class<C> caze, final Class<N> listItem, final K listKey) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new WithKeyBuilder<>(new KeyStep<>(listItem, requireNonNull(caze), listKey));
    }

    @Override
    public final ImmutableList<@NonNull ExactDataObjectStep<?>> steps() {
        return steps;
    }

    @Override
    public abstract @NonNull ExactDataObjectStep<T> lastStep();

    @Override
    public abstract @NonNull Builder<T> toBuilder();

    @Override
    final Object writeReplace() {
        return new ORv1(this);
    }

    private static <T extends DataObject, C extends ChoiceIn<?> & DataObject> @NonNull NodeStep<T> nodeStep(
            final Class<C> caze, final Class<T> type) {
        if (KeyAware.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException("Missing key for " + type);
        }
        return new NodeStep<>(type, caze);
    }
}
