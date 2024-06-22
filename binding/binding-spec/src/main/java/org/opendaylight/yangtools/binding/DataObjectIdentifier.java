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

        Simple(final Iterable<ExactDataObjectStep<?>> steps, final NodeStep<T> lastStep) {
            super(steps);
            this.lastStep = requireNonNull(lastStep);
        }

        @Override
        public NodeStep<T> lastStep() {
            return lastStep;
        }

        @Override
        public Builder<T> toBuilder() {
            return new SimpleBuilder<>(this);
        }

        @Override
        public DataObjectWildcard<T> toWildcard() {
            return new DataObjectWildcard<>(ImmutableList.copyOf(steps()), lastStep);
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

        @SuppressWarnings("unchecked")
        WithKey(final Iterable<ExactDataObjectStep<?>> steps, final KeyStep<?, ?> lastStep) {
            super(steps);
            this.lastStep = (KeyStep<K, T>) requireNonNull(lastStep);
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

        @Override
        public DataObjectWildcard.WithKey<T, K> toWildcard() {
            return new DataObjectWildcard.WithKey<>(ImmutableList.copyOf(steps()), lastStep);
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
        Builder(final Builder<?> prev, final ExactDataObjectStep<?> step) {
            super(prev, step);
        }

        Builder(final DataObjectIdentifier<T> base) {
            super(base.steps);
        }

        Builder(final ExactDataObjectStep<?> step) {
            super(step);
        }

        @Override
        public final <N extends DataObject & Augmentation<? super T>> Builder<N> augmentation(final Class<N> augment) {
            return append(new NodeStep<>(augment));
        }

        @Override
        public final <N extends ChildOf<? super T>> Builder<N> child(final Class<N> container) {
            return append(nodeStep(null, container));
        }

        @Override
        public final <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>> Builder<N> child(
                final Class<C> caze, final Class<N> container) {
            return append(nodeStep(caze, container));
        }

        @Override
        public final <N extends KeyAware<K> & ChildOf<? super T>, K extends Key<N>> WithKeyBuilder<N, K> child(
                final Class<N> listItem, final K listKey) {
            return append(new KeyStep<>(listItem, listKey));
        }

        @Override
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

        @Override
        abstract <X extends DataObject> SimpleBuilder<X> append(ExactDataObjectStep<?> step);

        @Override
        final <X extends DataObject> SimpleBuilder<X> append(final InexactDataObjectStep<?> step) {
            throw new IllegalArgumentException("unsupported step " + step);
        }

        @Override
        abstract <X extends DataObject & KeyAware<Y>, Y extends Key<X>> WithKeyBuilder<X, Y> append(KeyStep<?, ?> step);
    }

    public static final class WithKeyBuilder<T extends DataObject & KeyAware<K>, K extends Key<T>>
            extends Builder<T> {
        private @NonNull KeyStep<K, T> lastStep;

        @SuppressWarnings("unchecked")
        WithKeyBuilder(final KeyStep<?, ?> firstStep) {
            super(firstStep);
            lastStep = (KeyStep<K, T>) requireNonNull(firstStep);
        }

        WithKeyBuilder(final WithKey<T, K> base) {
            super(base);
            lastStep = base.lastStep();
        }

        @SuppressWarnings("unchecked")
        WithKeyBuilder(final SimpleBuilder<?> prev, final KeyStep<?, ?> lastStep) {
            super(prev, lastStep);
            this.lastStep = (KeyStep<K, T>) requireNonNull(lastStep);
        }

        /**
         * Build the instance identifier.
         *
         * @return Resulting {@link KeyedInstanceIdentifier}.
         */
        @Override
        public WithKey<T, K> build() {
            return new WithKey<>(buildSteps(), lastStep);
        }

        @Override
        <X extends DataObject> @NonNull SimpleBuilder<X> append(final ExactDataObjectStep<?> step) {
            return new SimpleBuilder<>(this, step);
        }

        @Override
        @SuppressWarnings("unchecked")
        <X extends DataObject & KeyAware<Y>, Y extends Key<X>> WithKeyBuilder<X, Y> append(final KeyStep<?, ?> step) {
            lastStep = (KeyStep<K, T>) requireNonNull(step);
            addStep(step);
            return (WithKeyBuilder<X, Y>) this;
        }
    }

    private static final class SimpleBuilder<T extends DataObject> extends Builder<T> {
        private @NonNull ExactDataObjectStep<T> lastStep;

        SimpleBuilder(final ExactDataObjectStep<T> step) {
            super(step);
            lastStep = requireNonNull(step);
        }

        SimpleBuilder(final DataObjectIdentifier<T> base) {
            super(base);
            lastStep = base.lastStep();
        }

        @SuppressWarnings("unchecked")
        private SimpleBuilder(final WithKeyBuilder<?, ?> prev, final ExactDataObjectStep<?> step) {
            super(prev, step);
            lastStep = (ExactDataObjectStep<T>) requireNonNull(step);
        }

        @Override
        public DataObjectIdentifier<T> build() {
            final var steps = buildSteps();
            return switch (lastStep) {
                case NodeStep<T> last -> new Simple<>(steps, last);
                case KeyStep<?, ?> last -> new WithKey(steps, last);
            };
        }

        @Override
        @SuppressWarnings("unchecked")
        <X extends DataObject> SimpleBuilder<X> append(final ExactDataObjectStep<?> step) {
            lastStep = (ExactDataObjectStep<T>) requireNonNull(step);
            addStep(step);
            return (SimpleBuilder<X>) this;
        }

        @Override
        <X extends DataObject & KeyAware<Y>, Y extends Key<X>> WithKeyBuilder<X, Y> append(final KeyStep<?, ?> item) {
            return new WithKeyBuilder<>(this, item);
        }
    }

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final @NonNull Iterable<ExactDataObjectStep<?>> steps;

    private DataObjectIdentifier(final Iterable<ExactDataObjectStep<?>> steps) {
        this.steps = requireNonNull(steps);
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
        return new SimpleBuilder<>(nodeStep(null, container));
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
        return new SimpleBuilder<>(nodeStep(caze, container));
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
        return new SimpleBuilder<>(nodeStep(null, container));
    }

    public static <R extends DataRoot & DataObject, C extends ChoiceIn<? super R> & DataObject,
            T extends ChildOf<? super C>>
            @NonNull Builder<T> builderOfInherited(final Class<R> root,
                final Class<C> caze, final Class<T> container) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new SimpleBuilder<>(nodeStep(caze, container));
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
    public final Iterable<ExactDataObjectStep<?>> steps() {
        return steps;
    }

    @Override
    public abstract @NonNull ExactDataObjectStep<T> lastStep();

    @Override
    public abstract @NonNull Builder<T> toBuilder();

    @Override
    @Deprecated
    public final DataObjectIdentifier<T> toIdentifier() {
        return this;
    }

    @Override
    @Deprecated
    public final DataObjectIdentifier<T> tryToIdentifier() {
        return this;
    }

    @Override
    final Class<?> contract() {
        return DataObjectIdentifier.class;
    }

    private static <T extends DataObject, C extends ChoiceIn<?> & DataObject> @NonNull NodeStep<T> nodeStep(
            final Class<C> caze, final Class<T> type) {
        if (KeyAware.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException("Missing key for " + type);
        }
        return new NodeStep<>(type, caze);
    }
}
