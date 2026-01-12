/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.binding.ChoiceIn;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.ExactDataObjectStep;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyStep;
import org.opendaylight.yangtools.binding.KeylessStep;
import org.opendaylight.yangtools.binding.NodeStep;
import org.opendaylight.yangtools.binding.impl.AbstractDataObjectReference;
import org.opendaylight.yangtools.binding.impl.AbstractDataObjectReferenceBuilder;
import org.opendaylight.yangtools.binding.impl.DataObjectIdentifierImpl;
import org.opendaylight.yangtools.binding.impl.DataObjectReferenceImpl;
import org.opendaylight.yangtools.concepts.HierarchicalIdentifier;

/**
 * This instance identifier uniquely identifies a specific DataObject in the data tree modeled by YANG.
 *
 * <p>For Example let's say you were trying to refer to a node in inventory which was modeled in YANG as follows,
 * <pre>code{
 *   module opendaylight-inventory {
 *     ....
 *
 *     container nodes {
 *       list node {
 *         key "id";
 *         ext:context-instance "node-context";
 *
 *         uses node;
 *       }
 *     }
 *   }
 * }</pre>
 *
 * <p>You can create an instance identifier as follows to get to a node with id "openflow:1": {@code
 * InstanceIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(new NodeId("openflow:1")).build();
 * }
 *
 * <p>This would be the same as using a path like so, "/nodes/node/openflow:1" to refer to the openflow:1 node
 *
 * @deprecated Use {@link DataObjectIdentifier} for the {@link #isExact()} case and {@link DataObjectReference} for the
 *             {@link #isWildcarded()} case.
 */
@Deprecated(since = "14.0.0", forRemoval = true)
public sealed class InstanceIdentifier<T extends DataObject> extends AbstractDataObjectReference<T, DataObjectStep<?>>
        implements HierarchicalIdentifier<InstanceIdentifier<? extends DataObject>>
        permits KeyedInstanceIdentifier {
    @java.io.Serial
    private static final long serialVersionUID = 3L;

    private final boolean wildcarded;

    InstanceIdentifier(final Iterable<? extends @NonNull DataObjectStep<?>> steps, final boolean wildcarded) {
        super(steps);
        this.wildcarded = wildcarded;
    }

    /**
     * Return the type of data which this InstanceIdentifier identifies.
     *
     * @return Target type
     */
    public final @NonNull Class<T> getTargetType() {
        return lastStep().type();
    }

    /**
     * Perform a safe target type adaptation of this instance identifier to target type. This method is useful when
     * dealing with type-squashed instances.
     *
     * @return Path argument with target type
     * @throws VerifyException if this instance identifier cannot be adapted to target type
     * @throws NullPointerException if {@code target} is null
     */
    @SuppressWarnings("unchecked")
    public final <N extends DataObject> @NonNull InstanceIdentifier<N> verifyTarget(final Class<@NonNull N> target) {
        verify(target.equals(getTargetType()), "Cannot adapt %s to %s", this, target);
        return (InstanceIdentifier<N>) this;
    }

    @Override
    public final boolean isExact() {
        return !wildcarded;
    }

    @Override
    @Deprecated(since = "14.0.0", forRemoval = true)
    public final boolean isWildcarded() {
        return wildcarded;
    }

    @Override
    protected final Class<?> contract() {
        return wildcarded ? super.contract() : DataObjectIdentifier.class;
    }

    /**
     * Return an instance identifier trimmed at the first occurrence of a specific component type.
     *
     * <p>For example let's say an instance identifier was built like so,
     * <pre>
     *      identifier = InstanceIdentifier.builder(Nodes.class).child(Node.class,
     *                   new NodeKey(new NodeId("openflow:1")).build();
     * </pre>
     *
     * <p>And you wanted to obtain the Instance identifier which represented Nodes you would do it like so,
     * <pre>
     *      identifier.firstIdentifierOf(Nodes.class)
     * </pre>
     *
     * @param type component type
     * @return trimmed instance identifier, or null if the component type
     *         is not present.
     */
    public final <I extends DataObject> @Nullable InstanceIdentifier<I> firstIdentifierOf(
            final Class<@NonNull I> type) {
        int count = 1;
        for (var step : steps()) {
            if (type.equals(step.type())) {
                @SuppressWarnings("unchecked")
                final var ret = (InstanceIdentifier<I>) internalCreate(Iterables.limit(steps(), count));
                return ret;
            }

            ++count;
        }

        return null;
    }

    @Override
    public final <E extends EntryObject<E, K>, K extends Key<E>> @Nullable K firstKeyOf(
            final Class<@NonNull E> listItem) {
        return super.firstKeyOf(listItem);
    }

    /**
     * Check whether an identifier is contained in this identifier. This is a strict subtree check, which requires all
     * PathArguments to match exactly.
     *
     * <p>The contains method checks if the other identifier is fully contained within the current identifier. It does
     * this by looking at only the types of the path arguments and not by comparing the path arguments themselves.
     *
     * <p>To illustrate here is an example which explains the working of this API. Let's say you have two instance
     * identifiers as follows:
     * {@code
     * this = /nodes/node/openflow:1
     * other = /nodes/node/openflow:2
     * }
     * then this.contains(other) will return false.
     *
     * @param other Potentially-container instance identifier
     * @return True if the specified identifier is contained in this identifier.
     */
    @Override
    public final boolean contains(final InstanceIdentifier<? extends DataObject> other) {
        requireNonNull(other, "other should not be null");

        final var oit = other.steps().iterator();
        for (var step : steps()) {
            if (!oit.hasNext()) {
                return false;
            }
            if (!step.equals(oit.next())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check whether this instance identifier contains the other identifier after wildcard expansion. This is similar
     * to {@link #contains(InstanceIdentifier)}, with the exception that a wildcards are assumed to match the their
     * non-wildcarded PathArgument counterpart.
     *
     * @param other Identifier which should be checked for inclusion.
     * @return true if this identifier contains the other object
     */
    public final boolean containsWildcarded(final InstanceIdentifier<?> other) {
        requireNonNull(other, "other should not be null");

        final var otherSteps = other.steps().iterator();
        for (var step : steps()) {
            if (!otherSteps.hasNext()) {
                return false;
            }

            final var otherStep = otherSteps.next();
            if (step instanceof ExactDataObjectStep) {
                if (!step.equals(otherStep)) {
                    return false;
                }
            } else if (step instanceof KeylessStep<?> keyless) {
                if (!keyless.matches(otherStep)) {
                    return false;
                }
            } else {
                throw new IllegalStateException("Unhandled step " + step);
            }
        }

        return true;
    }

    private <N extends DataObject> @NonNull InstanceIdentifier<N> childIdentifier(final DataObjectStep<N> arg) {
        return trustedCreate(arg, concat(steps(), arg), wildcarded);
    }

    /**
     * Create an InstanceIdentifier for a child container. This method is a more efficient equivalent to
     * {@code builder().child(container).build()}.
     *
     * @param container Container to append
     * @param <N> Container type
     * @return An InstanceIdentifier.
     * @throws NullPointerException if {@code container} is null
     */
    public final <N extends ChildOf<? super T>> @NonNull InstanceIdentifier<N> child(
            final Class<@NonNull N> container) {
        return childIdentifier(DataObjectStep.of(container));
    }

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
    @SuppressWarnings("unchecked")
    public final <N extends EntryObject<N, K> & ChildOf<? super T>, K extends Key<N>>
            @NonNull KeyedInstanceIdentifier<N, K> child(final Class<@NonNull N> listItem, final K listKey) {
        return (KeyedInstanceIdentifier<N, K>) childIdentifier(new KeyStep<>(listItem, listKey));
    }

    /**
     * Create an InstanceIdentifier for a child container. This method is a more efficient equivalent to
     * {@code builder().child(caze, container).build()}.
     *
     * @param caze Choice case class
     * @param container Container to append
     * @param <C> Case type
     * @param <N> Container type
     * @return An InstanceIdentifier.
     * @throws NullPointerException if any argument is null
     */
    // FIXME: add a proper caller
    public final <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>>
            @NonNull InstanceIdentifier<N> child(final Class<@NonNull C> caze, final Class<@NonNull N> container) {
        return childIdentifier(DataObjectStep.of(caze, container));
    }

    /**
     * Create an InstanceIdentifier for a child list item. This method is a more efficient equivalent to
     * {@code builder().child(caze, listItem, listKey).build()}.
     *
     * @param caze Choice case class
     * @param listItem List to append
     * @param listKey List key
     * @param <C> Case type
     * @param <N> List type
     * @param <K> Key type
     * @return An InstanceIdentifier.
     * @throws NullPointerException if any argument is null
     */
    // FIXME: add a proper caller
    @SuppressWarnings("unchecked")
    public final <C extends ChoiceIn<? super T> & DataObject, K extends Key<N>,
        N extends EntryObject<N, K> & ChildOf<? super C>> @NonNull KeyedInstanceIdentifier<N, K> child(
                final Class<@NonNull C> caze, final Class<@NonNull N> listItem, final K listKey) {
        return (KeyedInstanceIdentifier<N, K>) childIdentifier(new KeyStep<>(listItem, requireNonNull(caze), listKey));
    }

    /**
     * Create an InstanceIdentifier for a child augmentation. This method is a more efficient equivalent to
     * {@code builder().augmentation(container).build()}.
     *
     * @param augmentation Container to append
     * @param <A> Container type
     * @return An InstanceIdentifier.
     * @throws NullPointerException if {@code container} is null
     */
    public final <A extends Augmentation<? super T>> @NonNull InstanceIdentifier<A> augmentation(
            final Class<@NonNull A> augmentation) {
        return childIdentifier(new NodeStep<>(augmentation));
    }

    @Override
    protected Object toSerialForm() {
        return new IIv5(this);
    }

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

    @Override
    public Builder<T> toBuilder() {
        return new RegularBuilder<>(this);
    }

    @Override
    public DataObjectIdentifier<T> toIdentifier() {
        return toReference().toIdentifier();
    }

    @Override
    public InstanceIdentifier<T> toLegacy() {
        return this;
    }

    /**
     * Convert this {@link InstanceIdentifier} into its corresponding {@link DataObjectReference}.
     *
     * @return A non-InstanceIdentifier {@link DataObjectReference}
     */
    public @NonNull DataObjectReference<T> toReference() {
        final var steps = steps();
        return wildcarded ? new DataObjectReferenceImpl<>(steps) : new DataObjectIdentifierImpl<>(null, steps);
    }

    @Override
    @Deprecated(since = "14.0.0")
    public Builder<T> builder() {
        return toBuilder();
    }

    /**
     * Create a {@link Builder} for a specific type of InstanceIdentifier as specified by container.
     *
     * @param container Base container
     * @param <T> Type of the container
     * @return A new {@link Builder}
     * @throws NullPointerException if {@code container} is null
     */
    public static <T extends ChildOf<? extends DataRoot<?>>> @NonNull Builder<T> builder(
            final @NonNull Class<T> container) {
        return new RegularBuilder<>(DataObjectStep.of(container));
    }

    /**
     * Create a {@link Builder} for a specific type of InstanceIdentifier as specified by container in
     * a {@code grouping} used in the {@code case} statement.
     *
     * @param caze Choice case class
     * @param container Base container
     * @param <C> Case type
     * @param <T> Type of the container
     * @return A new {@link Builder}
     * @throws NullPointerException if any argument is null
     */
    public static <C extends ChoiceIn<? extends DataRoot<?>> & DataObject, T extends ChildOf<? super C>>
            @NonNull Builder<T> builder(final @NonNull Class<C> caze, final @NonNull Class<T> container) {
        return new RegularBuilder<>(DataObjectStep.of(caze, container));
    }

    /**
     * Create a {@link Builder} for a specific type of InstanceIdentifier which represents an {@link EntryObject}.
     *
     * @param listItem list item class
     * @param listKey key value
     * @param <N> List type
     * @param <K> List key
     * @return A new {@link Builder}
     * @throws NullPointerException if any argument is null
     */
    public static <N extends EntryObject<N, K> & ChildOf<? extends DataRoot<?>>, K extends Key<N>>
            @NonNull KeyedBuilder<N, K> builder(final Class<N> listItem, final K listKey) {
        return new KeyedBuilder<>(new KeyStep<>(listItem, listKey));
    }

    /**
     * Create a {@link Builder} for a specific type of InstanceIdentifier which represents an {@link EntryObject} in a
     * {@code grouping} used in the {@code case} statement.
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
    public static <C extends ChoiceIn<? extends DataRoot<?>> & DataObject,
            N extends EntryObject<N, K> & ChildOf<? super C>, K extends Key<N>>
            @NonNull KeyedBuilder<N, K> builder(final @NonNull Class<C> caze, final @NonNull Class<N> listItem,
                    final @NonNull K listKey) {
        return new KeyedBuilder<>(new KeyStep<>(listItem, requireNonNull(caze), listKey));
    }

    public static <R extends DataRoot<R>, T extends ChildOf<? super R>>
            @NonNull Builder<T> builderOfInherited(final @NonNull Class<R> root, final @NonNull Class<T> container) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new RegularBuilder<>(DataObjectStep.of(container));
    }

    public static <R extends DataRoot<R>, C extends ChoiceIn<? super R> & DataObject, T extends ChildOf<? super C>>
            @NonNull Builder<T> builderOfInherited(final Class<R> root,
                final Class<C> caze, final Class<T> container) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new RegularBuilder<>(DataObjectStep.of(caze, container));
    }

    public static <R extends DataRoot<R>, N extends EntryObject<N, K> & ChildOf<? super R>, K extends Key<N>>
            @NonNull KeyedBuilder<N, K> builderOfInherited(final @NonNull Class<R> root,
                final @NonNull Class<N> listItem, final @NonNull K listKey) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new KeyedBuilder<>(new KeyStep<>(listItem, listKey));
    }

    public static <R extends DataRoot<R>, C extends ChoiceIn<? super R> & DataObject,
            N extends EntryObject<N, K> & ChildOf<? super C>, K extends Key<N>>
            @NonNull KeyedBuilder<N, K> builderOfInherited(final Class<R> root,
                final Class<C> caze, final Class<N> listItem, final K listKey) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new KeyedBuilder<>(new KeyStep<>(listItem, requireNonNull(caze), listKey));
    }

    /**
     * Create an instance identifier for a very specific object type. This method implements {@link #create(Iterable)}
     * semantics, except it is used by internal callers, which have assured that the argument is an immutable Iterable.
     *
     * @param pathArguments The path to a specific node in the data tree
     * @return InstanceIdentifier instance
     * @throws IllegalArgumentException if pathArguments is empty or contains a null element.
     * @throws NullPointerException if {@code pathArguments} is null
     */
    private static @NonNull InstanceIdentifier<?> internalCreate(
            final Iterable<? extends DataObjectStep<?>> pathArguments) {
        final var it = requireNonNull(pathArguments, "pathArguments may not be null").iterator();
        checkArgument(it.hasNext(), "pathArguments may not be empty");

        boolean wildcard = false;
        DataObjectStep<?> arg;

        do {
            arg = it.next();
            // Non-null is implied by our callers
            final var type = verifyNotNull(arg).type();
            checkArgument(ChildOf.class.isAssignableFrom(type) || Augmentation.class.isAssignableFrom(type),
                "%s is not a valid path argument", type);

            if (!(arg instanceof ExactDataObjectStep)) {
                wildcard = true;
            }
        } while (it.hasNext());

        return trustedCreate(arg, pathArguments, wildcard);
    }

    /**
     * Create an instance identifier for a sequence of {@link DataObjectStep} steps. The steps are required to be formed
     * of classes extending either {@link ChildOf} or {@link Augmentation} contracts. This method does not check whether
     * or not the sequence is structurally sound, for example that an {@link Augmentation} follows an
     * {@link Augmentable} step. Furthermore the compile-time indicated generic type of the returned object does not
     * necessarily match the contained state.
     *
     * <p>Failure to observe precautions to validate the list's contents may yield an object which mey be rejected at
     * run-time or lead to undefined behaviour.
     *
     * @param pathArguments The path to a specific node in the data tree
     * @return InstanceIdentifier instance
     * @throws NullPointerException if {@code pathArguments} is, or contains an item which is, {@code null}
     * @throws IllegalArgumentException if {@code pathArguments} is empty or contains an item which does not represent
     *                                  a valid addressing step.
     */
    @SuppressWarnings("unchecked")
    public static <T extends DataObject> @NonNull InstanceIdentifier<T> unsafeOf(
            final List<? extends DataObjectStep<?>> pathArguments) {
        return (InstanceIdentifier<T>) internalCreate(ImmutableList.copyOf(pathArguments));
    }

    /**
     * Create an instance identifier for a very specific object type.
     *
     * <p>For example
     * <pre>
     *      new InstanceIdentifier(Nodes.class)
     * </pre>
     * would create an InstanceIdentifier for an object of type Nodes
     *
     * @param type The type of the object which this instance identifier represents
     * @return InstanceIdentifier instance
     */
    // FIXME: considering removing in favor of always going through a builder
    @SuppressWarnings("unchecked")
    public static <T extends ChildOf<? extends DataRoot<?>>> @NonNull InstanceIdentifier<T> create(
            final Class<@NonNull T> type) {
        return (InstanceIdentifier<T>) internalCreate(ImmutableList.of(DataObjectStep.of(type)));
    }

    /**
     * Return the key associated with the last component of the specified identifier.
     *
     * @param id instance identifier
     * @return key associated with the last component
     * @throws IllegalArgumentException if the supplied identifier type cannot have a key.
     * @throws NullPointerException if id is null.
     */
    // FIXME: reconsider naming and design of this method
    public static <N extends EntryObject<N, K>, K extends Key<N>> K keyOf(final InstanceIdentifier<N> id) {
        requireNonNull(id);
        checkArgument(id instanceof KeyedInstanceIdentifier, "%s does not have a key", id);

        @SuppressWarnings("unchecked")
        final K ret = ((KeyedInstanceIdentifier<N, K>)id).key();
        return ret;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    static <N extends DataObject> @NonNull InstanceIdentifier<N> trustedCreate(final DataObjectStep<?> lastStep,
            final Iterable<? extends DataObjectStep<?>> pathArguments, final boolean wildcarded) {
        return switch (lastStep) {
            case NodeStep<?> cast -> new InstanceIdentifier(pathArguments, wildcarded);
            case KeyStep<?, ?> cast -> new KeyedInstanceIdentifier(pathArguments, wildcarded);
            case KeylessStep<?> cast -> new InstanceIdentifier(pathArguments, true);
        };
    }

    /**
     * A builder of {@link InstanceIdentifier} objects.
     *
     * @param <T> Instance identifier target type
     */
    @Deprecated(since = "14.0.23", forRemoval = true)
    public abstract static sealed class Builder<T extends DataObject> extends AbstractDataObjectReferenceBuilder<T> {
        Builder(final Builder<?> prev) {
            super(prev);
        }

        Builder(final InstanceIdentifier<T> identifier) {
            super(identifier);
        }

        Builder(final DataObjectStep<?> item) {
            super(item);
        }

        Builder(final ExactDataObjectStep<?> item) {
            super(item);
        }

        @Override
        public final <A extends Augmentation<? super T>> Builder<A> augmentation(final Class<A> augmentation) {
            return append(new NodeStep<>(augmentation));
        }

        @Override
        public final <N extends ChildOf<? super T>> Builder<N> child(final Class<N> container) {
            return append(DataObjectStep.of(container));
        }

        @Override
        public final <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>> Builder<N> child(
                final Class<C> caze, final Class<N> container) {
            return append(DataObjectStep.of(caze, container));
        }

        @Override
        public final <N extends EntryObject<N, K> & ChildOf<? super T>, K extends Key<N>> KeyedBuilder<N, K> child(
                final Class<@NonNull N> listItem, final K listKey) {
            return append(new KeyStep<>(listItem, listKey));
        }

        @Override
        public final <C extends ChoiceIn<? super T> & DataObject, K extends Key<N>,
                N extends EntryObject<N, K> & ChildOf<? super C>> KeyedBuilder<N, K> child(final Class<C> caze,
                    final Class<N> listItem, final K listKey) {
            return append(new KeyStep<>(listItem, requireNonNull(caze), listKey));
        }

        @Override
        public abstract @NonNull InstanceIdentifier<T> build();

        @Override
        protected abstract <X extends DataObject> @NonNull RegularBuilder<X> append(DataObjectStep<X> step);

        @Override
        protected abstract <X extends EntryObject<X, Y>, Y extends Key<X>> @NonNull KeyedBuilder<X, Y> append(
            KeyStep<Y, X> step);
    }

    @Deprecated(since = "14.0.23", forRemoval = true)
    public static final class KeyedBuilder<T extends EntryObject<T, K>, K extends Key<T>> extends Builder<T>
            implements DataObjectReference.Builder.WithKey<T, K> {
        KeyedBuilder(final KeyStep<K, T> firstStep) {
            super(firstStep);
        }

        KeyedBuilder(final KeyedInstanceIdentifier<T, K> identifier) {
            super(identifier);
        }

        private KeyedBuilder(final RegularBuilder<?> prev) {
            super(prev);
        }

        /**
         * Build the instance identifier.
         *
         * @return Resulting {@link KeyedInstanceIdentifier}.
         */
        @Override
        public @NonNull KeyedInstanceIdentifier<T, K> build() {
            return new KeyedInstanceIdentifier<>(buildSteps(), wildcard());
        }

        @Override
        protected <X extends DataObject> @NonNull RegularBuilder<X> append(final DataObjectStep<X> step) {
            return new RegularBuilder<X>(this).append(step);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected <X extends EntryObject<X, Y>, Y extends Key<X>> KeyedBuilder<X, Y> append(final KeyStep<Y, X> step) {
            appendItem(step);
            return (KeyedBuilder<X, Y>) this;
        }
    }

    @Deprecated(since = "14.0.23", forRemoval = true)
    private static final class RegularBuilder<T extends DataObject> extends Builder<T> {
        RegularBuilder(final DataObjectStep<T> item) {
            super(item);
        }

        RegularBuilder(final InstanceIdentifier<T> identifier) {
            super(identifier);
        }

        private RegularBuilder(final KeyedBuilder<?, ?> prev) {
            super(prev);
        }

        @Override
        public InstanceIdentifier<T> build() {
            return new InstanceIdentifier<>(buildSteps(), wildcard());
        }

        @Override
        @SuppressWarnings("unchecked")
        protected <X extends DataObject> RegularBuilder<X> append(final DataObjectStep<X> step) {
            appendItem(step);
            return (RegularBuilder<X>) this;
        }

        @Override
        protected <X extends EntryObject<X, Y>, Y extends Key<X>> KeyedBuilder<X, Y> append(
                final KeyStep<Y, X> item) {
            return new KeyedBuilder<>(this).append(item);
        }
    }
}
