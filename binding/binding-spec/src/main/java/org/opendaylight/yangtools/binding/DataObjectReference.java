/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * A reference to a {@link DataObject} with semantics partially overlapping with to YANG {@code instance-identifier}.
 *
 * <p>
 * While this indirection is not something defined in YANG, this class hierarchy arises naturally from the Binding
 * specification's Java footprint, which uses {@link DataObject} as the baseline self-sufficient addressable construct.
 * This means users can use a {@link KeyAware} class without specifying the corresponding key -- resulting in an
 * {@link InexactDataObjectStep}.
 *
 * <p>
 * There are two kinds of a reference based on their treatment of such a {@link InexactDataObjectStep}:
 * <ul>
 *   <li>{@link DataObjectIdentifier}, which accepts only {@link ExactDataObjectStep}s and represents
 *       a {@link BindingInstanceIdentifier} pointing to a {@link DataObject}</li>
 *   <li>{@link DataObjectWildcard}, which accepts any {@link DataObjectStep} and represents path-based matching
 *       criteria for one or more {@link DataObjectIdentifier}s based on
 *       {@link InexactDataObjectStep#matches(DataObjectStep)}.
 * </ul>
 */
public abstract sealed class DataObjectReference<S extends DataObjectStep<?>, T extends DataObject>
        implements Immutable, Serializable
        permits DataObjectIdentifier, DataObjectWildcard {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    /*
     * Protected to differentiate internal and external access. Internal access is required never to modify
     * the contents. References passed to outside entities have to be wrapped in an unmodifiable view.
     */
    final @NonNull Iterable<@NonNull S> steps;

    private final @NonNull DataObjectStep<T> lastStep;

    DataObjectReference(final Iterable<@NonNull S> steps, final DataObjectStep<T> lastStep) {
        this.steps = requireNonNull(steps);
        this.lastStep = requireNonNull(lastStep);
    }

    /**
     * Return the steps of this reference. Returned {@link Iterable} does not support removals and contains one or more
     * non-null ite,s.
     *
     * @return the steps of this reference
     */
    public abstract @NonNull Iterable<@NonNull S> steps();

    public @NonNull DataObjectStep<T> lastStep() {
        return lastStep;
    }

    /**
     * Create a {@link DataObjectReferenceBuilder} producing equivalent reference.
     *
     * @return A builder instance
     */
    public abstract @NonNull DataObjectReferenceBuilder<S, T> toBuilder();

    public abstract @NonNull DataObjectWildcard<T> toWildcard();

    // throws UnsupportedOperationException
    public abstract @NonNull DataObjectIdentifier<T> toIdentifier();

    public abstract @Nullable DataObjectIdentifier<T> tryToIdentifier();

    /**
     * Create an InstanceIdentifier for a child augmentation. This method is a more efficient equivalent to
     * {@code builder().augmentation(container).build()}.
     *
     * @param <A> Augmentation type
     * @param augment Augmentation to append
     * @return A DataObjectReference
     * @throws NullPointerException if {@code augment} is null
     */
    public abstract <A extends DataObject & Augmentation<? super T>>
        @NonNull DataObjectReference<S, A> withAugmentation(@NonNull Class<A> augment);

    /**
     * Create an InstanceIdentifier for a child container. This method is a more efficient equivalent to
     * {@code builder().child(container).build()}.
     *
     * @param container Container to append
     * @param <N> Container type
     * @return An InstanceIdentifier.
     * @throws NullPointerException if {@code container} is null
     */
    public abstract <N extends ChildOf<? super T>> @NonNull DataObjectReference<S, N> withChild(
        @NonNull Class<@NonNull N> container);

    /**
     * Create an InstanceIdentifier for a child list item. This method is a more efficient equivalent to
     * {@code builder().child(listItem, listKey).build()}.
     *
     * @param listItem List to append
     * @param listKey List key
     * @param <N> List type
     * @param <K> Key type
     * @return An InstanceIdentifier.
     * @throws NullPointerException if any argument is null
     */
    public abstract <N extends KeyAware<K> & ChildOf<? super T>, K extends Key<N>>
        @NonNull DataObjectReference<S, N> withChild(@NonNull Class<N> listItem, @NonNull K listKey);

    abstract <X extends DataObject> @NonNull DataObjectReference<S, X> withChild(NodeStep<X> step);

    static final <T extends DataObject> @NonNull DataObjectReference<?, ?> ofSteps(
            final ImmutableList<? extends DataObjectStep<?>> steps) {
        return steps.stream().anyMatch(InexactDataObjectStep.class::isInstance) ? DataObjectWildcard.unsafeOf(steps)
            : DataObjectIdentifier.unsafeOf((ImmutableList<ExactDataObjectStep<?>>) steps);
    }

    @Override
    public final int hashCode() {
        int hashCode = 1;
        for (var element : steps()) {
            hashCode = hashCode * 31 + element.hashCode();
        }
        return hashCode;
    }

    @Override
    public final boolean equals(final Object obj) {
        return this == obj || obj != null && getClass().equals(obj.getClass())
            && Iterables.elementsEqual(steps(), ((DataObjectReference<?, ?>) obj).steps());
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(contract())).toString();
    }

    /**
     * Add class-specific toString attributes.
     *
     * @param toStringHelper ToStringHelper instance
     * @return ToStringHelper instance which was passed in
     */
    ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        // FIXME: add steps
        return toStringHelper;
    }

    abstract @NonNull Class<?> contract();

    @java.io.Serial
    final Object writeReplace() throws ObjectStreamException {
        return writeReplaceImpl();
    }

    abstract @NonNull ORv1 writeReplaceImpl();

    @java.io.Serial
    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        throwNSE();
    }

    @java.io.Serial
    private void readObjectNoData() throws ObjectStreamException {
        throwNSE();
    }

    @java.io.Serial
    private void writeObject(final ObjectOutputStream stream) throws IOException {
        throwNSE();
    }

    final void throwNSE() throws NotSerializableException {
        throw new NotSerializableException(getClass().getName());
    }
}
