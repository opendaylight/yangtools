/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A Builder of {@link DataObjectReference}s.
 */
public abstract sealed class DataObjectReferenceBuilder<S extends DataObjectStep<?>, T extends DataObject>
        permits DataObjectIdentifier.Builder, InstanceIdentifier.Builder {
    private final ImmutableList.Builder<S> pathBuilder;

    private Iterable<S> prefix;

    private DataObjectReferenceBuilder(final Iterable<S> prefix, final ImmutableList.Builder<S> pathBuilder,
            final S step) {
        this.prefix = prefix;
        this.pathBuilder = pathBuilder;
        addStep(step);
    }

    DataObjectReferenceBuilder(final Iterable<S> prefix) {
        this.prefix = requireNonNull(prefix);
        pathBuilder = ImmutableList.builder();
    }

    DataObjectReferenceBuilder(final S firstStep) {
        this(null, ImmutableList.builder(), firstStep);
    }

    DataObjectReferenceBuilder(final DataObjectReferenceBuilder<S, ?> prev, final S step) {
        this(prev.prefix, prev.pathBuilder, step);
    }

    /**
     * Update this builder to build a reference that refers to an augmentation referenced by this builder's current
     * state.
     *
     * @param <N> augmentation type
     * @param augment augmentation class
     * @return this builder
     * @throws NullPointerException if {@code augment} is null
     */
    public abstract <N extends DataObject & Augmentation<? super T>>
        @NonNull DataObjectReferenceBuilder<S, N> augmentation(@NonNull Class<N> augment);

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
     * @param <N> Container type
     * @param container Container to append
     * @return this builder
     * @throws NullPointerException if {@code container} is null
     */
    public abstract <N extends ChildOf<? super T>> @NonNull DataObjectReferenceBuilder<S, N> child(
        @NonNull Class<N> container);

    /**
     * Append the specified container as a child of the current InstanceIdentifier referenced by the builder. This
     * method should be used when you want to build an instance identifier by appending a container node to the
     * identifier and the {@code container} is defined in a {@code grouping} used in a {@code case} statement.
     *
     * @param <C> Case type
     * @param <N> Container type
     * @param caze Choice case class
     * @param container Container to append
     * @return this builder
     * @throws NullPointerException if {@code container} is null
     */
    public abstract <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>>
        @NonNull DataObjectReferenceBuilder<S, N> child(@NonNull Class<C> caze, @NonNull Class<N> container);

    /**
     * Append the specified listItem as a child of the current InstanceIdentifier referenced by the builder. This
     * method should be used when you want to build an instance identifier by appending a specific list element to
     * the identifier.
     *
     * @param <N> List type
     * @param <K> Key type
     * @param listItem List to append
     * @param listKey List key
     * @return this builder
     * @throws NullPointerException if any argument is null
     */
    // FIXME: capture KeyAware builder
    public abstract <N extends KeyAware<K> & ChildOf<? super T>, K extends Key<N>>
        @NonNull DataObjectReferenceBuilder<S, N> child(@NonNull Class<@NonNull N> listItem, @NonNull K listKey);

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
    public abstract <C extends ChoiceIn<? super T> & DataObject, K extends Key<N>,
            N extends KeyAware<K> & ChildOf<? super C>>
        @NonNull DataObjectReferenceBuilder<S, N> child(@NonNull Class<C> caze, @NonNull Class<N> listItem,
            @NonNull K listKey);

    /**
     * Build the {@link DataObjectReference}.
     *
     * @return Resulting {@link DataObjectReference}.
     */
    public abstract DataObjectReference<S, T> build();

    abstract <X extends DataObject> @NonNull DataObjectReferenceBuilder<S, X> append(
        @NonNull ExactDataObjectStep<?> step);

    abstract <X extends DataObject> @NonNull DataObjectReferenceBuilder<S, X> append(
        @NonNull InexactDataObjectStep<?> step);

    abstract <X extends DataObject & KeyAware<Y>, Y extends Key<X>> @NonNull DataObjectReferenceBuilder<S, X> append(
        @NonNull KeyStep<?, ?> step);

    final void addStep(final S step) {
        final var local = prefix;
        if (local != null) {
            prefix = null;
            pathBuilder.addAll(local);
        }
        pathBuilder.add(step);
    }

    final Iterable<S> buildSteps() {
        final var path = pathBuilder.build();
        return path.isEmpty() ? verifyNotNull(prefix) : path;
    }
}
