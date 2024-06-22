/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects.ToStringHelper;
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
import org.opendaylight.yangtools.concepts.HierarchicalIdentifier;

/**
 * This instance identifier uniquely identifies a specific DataObject in the data tree modeled by YANG.
 *
 * <p>
 * For Example let's say you were trying to refer to a node in inventory which was modeled in YANG as follows,
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
 * <p>
 * You can create an instance identifier as follows to get to a node with id "openflow:1": {@code
 * InstanceIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(new NodeId("openflow:1")).build();
 * }
 *
 * <p>
 * This would be the same as using a path like so, "/nodes/node/openflow:1" to refer to the openflow:1 node
 */
public abstract sealed class DataObjectWildcard<T extends DataObject> extends DataObjectReference<DataObjectStep<?>, T>
        implements HierarchicalIdentifier<DataObjectWildcard<? extends DataObject>> {
    /**
     * A simple {@link DataObjectWildcard}.
     *
     * @param <T> target {@link DataObject} type
     */
    public static final class Simple<T extends DataObject> extends DataObjectWildcard<T> {
        Simple(final Iterable<@NonNull DataObjectStep<?>> steps, final DataObjectStep<T> lastStep) {
            super(steps, lastStep);
        }

        @Override
        public SimpleBuilder<T> toBuilder() {
            return new SimpleBuilder<>(this);
        }
    }

    /**
     * An {@link DataObjectWildcard}, which has a list key attached at its last path element.
     *
     * @param <T> target {@link KeyAware} {@link DataObject} type
     * @param <K> target {@link Key} type
     */
    public static final class WithKey<T extends KeyAware<K> & DataObject, K extends Key<T>>
            extends DataObjectWildcard<T> {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        WithKey(final Iterable<@NonNull DataObjectStep<?>> steps, final KeyStep<K, T> lastStep) {
            super(steps, lastStep);
        }

        @Override
        @SuppressWarnings("unchecked")
        public KeyStep<K, T> lastStep() {
            return (KeyStep<K, T>) super.lastStep();
        }

        /**
         * Return the key attached to this identifier. This method is equivalent to calling
         * {@link DataObjectWildcard#keyOf(DataObjectWildcard)}.
         *
         * @return Key associated with this instance identifier.
         */
        public @NonNull K getKey() {
            return lastStep().key();
        }

        @Override
        public KeyedBuilder<T, K> toBuilder() {
            return new KeyedBuilder<>(this);
        }

        @Override
        public DataObjectIdentifier.WithKey<T, K> toIdentifier() {
            return new DataObjectIdentifier.WithKey<>(getExactSteps(), lastStep());
        }

        @Override
        public DataObjectIdentifier.WithKey<T, K> tryToIdentifier() {
            final var steps = tryExactSteps();
            return steps == null ? null : new DataObjectIdentifier.WithKey<>(getExactSteps(), lastStep());
        }

        @Override
        boolean keyEquals(final DataObjectWildcard<?> other) {
            return getKey().equals(((WithKey<?, ?>) other).getKey());
        }
    }

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    DataObjectWildcard(final Iterable<@NonNull DataObjectStep<?>> steps, final DataObjectStep<T> lastStep) {
        super(steps, lastStep);
    }

    @Override
    public final Iterable<@NonNull DataObjectStep<?>> steps() {
        return steps;
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
    public final <N extends DataObject> @NonNull DataObjectWildcard<N> verifyTarget(final Class<@NonNull N> target) {
        verify(target.equals(getTargetType()), "Cannot adapt %s to %s", this, target);
        return (DataObjectWildcard<N>) this;
    }

    boolean keyEquals(final DataObjectWildcard<?> other) {
        return true;
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("targetType", getTargetType()).add("path", Iterables.toString(steps()));
    }

    /**
     * Return an instance identifier trimmed at the first occurrence of a specific component type.
     *
     * <p>
     * For example let's say an instance identifier was built like so,
     * <pre>
     *      identifier = InstanceIdentifier.builder(Nodes.class).child(Node.class,
     *                   new NodeKey(new NodeId("openflow:1")).build();
     * </pre>
     *
     * <p>
     * And you wanted to obtain the Instance identifier which represented Nodes you would do it like so,
     * <pre>
     *      identifier.firstIdentifierOf(Nodes.class)
     * </pre>
     *
     * @param type component type
     * @return trimmed instance identifier, or null if the component type
     *         is not present.
     */
    public final <I extends DataObject> @Nullable DataObjectWildcard<I> firstIdentifierOf(
            final Class<@NonNull I> type) {
        int count = 1;
        final var steps = steps();
        for (var step : steps) {
            if (type.equals(step.type())) {
                @SuppressWarnings("unchecked")
                final var ret = (DataObjectWildcard<I>) internalCreate(Iterables.limit(steps, count));
                return ret;
            }

            ++count;
        }

        return null;
    }

    /**
     * Return the key associated with the first component of specified type in
     * an identifier.
     *
     * @param listItem component type
     * @return key associated with the component, or null if the component type
     *         is not present.
     */
    public final <N extends KeyAware<K> & DataObject, K extends Key<N>> @Nullable K firstKeyOf(
            final Class<@NonNull N> listItem) {
        for (var step : steps()) {
            if (step instanceof KeyStep<?, ?> keyPredicate && listItem.equals(step.type())) {
                @SuppressWarnings("unchecked")
                final var ret = (K) keyPredicate.key();
                return ret;
            }
        }
        return null;
    }

    /**
     * Check whether an identifier is contained in this identifier. This is a strict subtree check, which requires all
     * PathArguments to match exactly.
     *
     * <p>
     * The contains method checks if the other identifier is fully contained within the current identifier. It does this
     * by looking at only the types of the path arguments and not by comparing the path arguments themselves.
     *
     * <p>
     * To illustrate here is an example which explains the working of this API. Let's say you have two instance
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
    public final boolean contains(final DataObjectWildcard<? extends DataObject> other) {
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
     * to {@link #contains(DataObjectWildcard)}, with the exception that a wildcards are assumed to match their
     * non-wildcarded PathArgument counterpart.
     *
     * @param other Identifier which should be checked for inclusion.
     * @return true if this identifier contains the other object
     */
    public final boolean containsWildcarded(final DataObjectReference<?, ?> other) {
        final var otherSteps = other.steps().iterator();
        for (var step : steps()) {
            if (!otherSteps.hasNext()) {
                return false;
            }

            final var otherStep = otherSteps.next();
            switch (step) {
                case null -> throw new NullPointerException("null otherStep");
                case ExactDataObjectStep<?> exact -> {
                    if (!step.equals(otherStep)) {
                        return false;
                    }
                }
                case InexactDataObjectStep<?> inexact -> {
                    if (!inexact.matches(requireNonNull(otherStep))) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    @Override
    final <X extends DataObject> @NonNull DataObjectWildcard<X> withChild(final NodeStep<X> step) {
        return withChild(step);
    }

    private <N extends DataObject> @NonNull DataObjectWildcard<N> withChild(final DataObjectStep<N> arg) {
        return trustedCreate(arg, Iterables.concat(steps(), ImmutableList.of(arg)));
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
    public final <N extends ChildOf<? super T>> @NonNull DataObjectWildcard<N> child(
            final Class<@NonNull N> container) {
        return withChild(createStep(container));
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
    public final <N extends KeyAware<K> & ChildOf<? super T>, K extends Key<N>>
            @NonNull WithKey<N, K> child(final Class<@NonNull N> listItem, final K listKey) {
        return (WithKey<N, K>) withChild(new KeyStep<>(listItem, listKey));
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
            @NonNull DataObjectWildcard<N> child(final Class<@NonNull C> caze, final Class<@NonNull N> container) {
        return withChild(createStep(caze, container));
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
        N extends KeyAware<K> & ChildOf<? super C>> @NonNull WithKey<N, K> child(
                final Class<@NonNull C> caze, final Class<@NonNull N> listItem, final K listKey) {
        return (WithKey<N, K>) withChild(new KeyStep<>(listItem, requireNonNull(caze), listKey));
    }

    @Override
    public final <N extends DataObject & Augmentation<? super T>> DataObjectWildcard<N> withAugmentation(
            final Class<N> augment) {
        return withChild(new NodeStep<>(augment));
    }

    /**
     * Create an InstanceIdentifier for a child augmentation. This method is a more efficient equivalent to
     * {@code builder().augmentation(container).build()}.
     *
     * @param <A> Augmentation type
     * @param augment Augmentation to append
     * @return A DataObjectReference
     * @throws NullPointerException if {@code augment} is null
     * @deprecated Provided for migration, please use {@link #withAugmentation(Class)} instead.
     */
    @Deprecated
    public final <N extends DataObject & Augmentation<? super T>> @NonNull DataObjectWildcard<N> augmentation(
            final @NonNull Class<N> augment) {
        return withAugmentation(augment);
    }

    @Override
    final Class<?> contract() {
        return DataObjectWildcard.class;
    }

    @Override
    final OWv1 writeReplaceImpl() {
        return new OWv1(this);
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
    public abstract Builder<T> toBuilder();

    @Override
    @Deprecated
    public final DataObjectWildcard<T> toWildcard() {
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public DataObjectIdentifier<T> toIdentifier() {
        return (DataObjectIdentifier<T>) DataObjectIdentifier.ofSteps(getExactSteps());
    }

    @Override
    @SuppressWarnings("unchecked")
    public DataObjectIdentifier<T> tryToIdentifier() {
        final var exactSteps = tryExactSteps();
        return exactSteps == null ? null : (DataObjectIdentifier<T>) DataObjectIdentifier.ofSteps(exactSteps);
    }

    final @Nullable ImmutableList<ExactDataObjectStep<?>> tryExactSteps() {
        final var local = steps();
        for (var step : local) {
            if (step instanceof InexactDataObjectStep) {
                return null;
            }
        }
        return unsafeExactSteps(local);
    }

    final @NonNull ImmutableList<ExactDataObjectStep<?>> getExactSteps() {
        final var local = steps();
        local.forEach(step -> {
            if (step instanceof InexactDataObjectStep inexact) {
                throw new UnsupportedOperationException("unsupported step" + inexact);
            }
        });
        return unsafeExactSteps(local);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static @NonNull ImmutableList<ExactDataObjectStep<?>> unsafeExactSteps(final Iterable steps) {
        return ImmutableList.copyOf(steps);
    }

    /**
     * Create a {@link Builder} for a specific type of InstanceIdentifier as specified by container.
     *
     * @param container Base container
     * @param <T> Type of the container
     * @return A new {@link Builder}
     * @throws NullPointerException if {@code container} is null
     */
    public static <T extends ChildOf<? extends DataRoot>> @NonNull Builder<T> builder(final Class<T> container) {
        return new SimpleBuilder<>(createStep(container));
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
    public static <C extends ChoiceIn<? extends DataRoot> & DataObject, T extends ChildOf<? super C>>
            @NonNull Builder<T> builder(final Class<C> caze, final Class<T> container) {
        return new SimpleBuilder<>(createStep(caze, container));
    }

    /**
     * Create a {@link Builder} for a specific type of {@link DataObjectWildcard} which represents an {@link KeyStep}.
     *
     * @param listItem list item class
     * @param listKey key value
     * @param <N> List type
     * @param <K> List key
     * @return A new {@link Builder}
     * @throws NullPointerException if any argument is null
     */
    public static <N extends KeyAware<K> & ChildOf<? extends DataRoot>,  K extends Key<N>>
            @NonNull KeyedBuilder<N, K> builder(final Class<N> listItem, final K listKey) {
        return new KeyedBuilder<>(new KeyStep<>(listItem, listKey));
    }

    /**
     * Create a {@link Builder} for a specific type of {@link DataObjectWildcard} which represents an {@link KeyStep}
     *  in a {@code grouping} used in the {@code case} statement.
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
    public static <C extends ChoiceIn<? extends DataRoot> & DataObject,  N extends KeyAware<K> & ChildOf<? super C>,
            K extends Key<N>>
            @NonNull KeyedBuilder<N, K> builder(final Class<C> caze, final Class<N> listItem, final K listKey) {
        return new KeyedBuilder<>(new KeyStep<>(listItem, requireNonNull(caze), listKey));
    }

    public static <R extends DataRoot & DataObject, T extends ChildOf<? super R>>
            @NonNull Builder<T> builderOfInherited(final Class<R> root, final Class<T> container) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new SimpleBuilder<>(createStep(container));
    }

    public static <R extends DataRoot & DataObject, C extends ChoiceIn<? super R> & DataObject,
            T extends ChildOf<? super C>>
            @NonNull Builder<T> builderOfInherited(final Class<R> root,
                final Class<C> caze, final Class<T> container) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new SimpleBuilder<>(createStep(caze, container));
    }

    public static <R extends DataRoot & DataObject, N extends KeyAware<K> & ChildOf<? super R>, K extends Key<N>>
            @NonNull KeyedBuilder<N, K> builderOfInherited(final Class<R> root,
                final Class<N> listItem, final K listKey) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new KeyedBuilder<>(new KeyStep<>(listItem, listKey));
    }

    public static <R extends DataRoot & DataObject, C extends ChoiceIn<? super R> & DataObject,
            N extends KeyAware<K> & ChildOf<? super C>, K extends Key<N>>
            @NonNull KeyedBuilder<N, K> builderOfInherited(final Class<R> root,
                final Class<C> caze, final Class<N> listItem, final K listKey) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new KeyedBuilder<>(new KeyStep<>(listItem, requireNonNull(caze), listKey));
    }

    @Beta
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T extends DataObject, C extends ChoiceIn<?> & DataObject> @NonNull DataObjectStep<T> createStep(
            final Class<C> caze, final Class<T> type) {
        return KeyAware.class.isAssignableFrom(type) ? new KeylessStep(type, caze) : new NodeStep<>(type, caze);
    }

    @Beta
    public static <T extends DataObject> @NonNull DataObjectStep<T> createStep(final Class<T> type) {
        return createStep(null, type);
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
    private static @NonNull DataObjectWildcard<?> internalCreate(final Iterable<DataObjectStep<?>> pathArguments) {
        final var it = requireNonNull(pathArguments, "pathArguments may not be null").iterator();
        checkArgument(it.hasNext(), "pathArguments may not be empty");

        DataObjectStep<?> arg;

        do {
            arg = it.next();
            // Non-null is implied by our callers
            final var type = verifyNotNull(arg).type();
            checkArgument(ChildOf.class.isAssignableFrom(type) || Augmentation.class.isAssignableFrom(type),
                "%s is not a valid path argument", type);
        } while (it.hasNext());

        return trustedCreate(arg, pathArguments);
    }

    /**
     * Create an instance identifier for a sequence of {@link DataObjectStep} steps. The steps are required to be formed
     * of classes extending either {@link ChildOf} or {@link Augmentation} contracts. This method does not check whether
     * or not the sequence is structurally sound, for example that an {@link Augmentation} follows an
     * {@link Augmentable} step. Furthermore the compile-time indicated generic type of the returned object does not
     * necessarily match the contained state.
     *
     * <p>
     * Failure to observe precautions to validate the list's contents may yield an object which mey be rejected at
     * run-time or lead to undefined behaviour.
     *
     * @param pathArguments The path to a specific node in the data tree
     * @return InstanceIdentifier instance
     * @throws NullPointerException if {@code pathArguments} is, or contains an item which is, {@code null}
     * @throws IllegalArgumentException if {@code pathArguments} is empty or contains an item which does not represent
     *                                  a valid addressing step.
     */
    @SuppressWarnings("unchecked")
    public static <T extends DataObject> @NonNull DataObjectWildcard<T> unsafeOf(
            final List<? extends DataObjectStep<?>> pathArguments) {
        return (DataObjectWildcard<T>) internalCreate(ImmutableList.copyOf(pathArguments));
    }

    /**
     * Create an instance identifier for a very specific object type.
     *
     * <p>
     * For example
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
    public static <T extends ChildOf<? extends DataRoot>> @NonNull DataObjectWildcard<T> create(
            final Class<@NonNull T> type) {
        return (DataObjectWildcard<T>) internalCreate(ImmutableList.of(createStep(type)));
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
    public static <N extends KeyAware<K> & DataObject, K extends Key<N>> K keyOf(
            final DataObjectWildcard<N> id) {
        requireNonNull(id);
        checkArgument(id instanceof WithKey, "%s does not have a key", id);

        @SuppressWarnings("unchecked")
        final K ret = ((WithKey<N, K>)id).getKey();
        return ret;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    static <N extends DataObject> @NonNull DataObjectWildcard<N> trustedCreate(final DataObjectStep<?> lastStep,
            final Iterable<DataObjectStep<?>> pathArguments) {
        return switch (lastStep) {
            case NodeStep<?> node -> new Simple(pathArguments, lastStep);
            case KeyStep<?, ?> key -> new WithKey(pathArguments, key);
            case KeylessStep keyless -> new Simple(pathArguments, lastStep);
        };
    }

    /**
     * A builder of {@link DataObjectWildcard} objects.
     *
     * @param <T> Instance identifier target type
     */
    public abstract static sealed class Builder<T extends DataObject>
            extends DataObjectReferenceBuilder<DataObjectStep<?>, T> {
        Builder(final Builder<?> prev, final DataObjectStep<?> step) {
            super(prev, step);
        }

        Builder(final DataObjectWildcard<T> base) {
            super(base);
        }

        Builder(final DataObjectStep<?> step) {
            super(step);
        }

        @Override
        public final <N extends DataObject & Augmentation<? super T>> Builder<N> augmentation(final Class<N> augment) {
            return append(new NodeStep<>(augment));
        }

        @Override
        public final <N extends ChildOf<? super T>> Builder<N> child(final Class<N> container) {
            return append(createStep(container));
        }

        @Override
        public final <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>> Builder<N> child(
                final Class<C> caze, final Class<N> container) {
            return append(createStep(caze, container));
        }

        @Override
        public final <N extends KeyAware<K> & ChildOf<? super T>, K extends Key<N>> KeyedBuilder<N, K> child(
                final Class<@NonNull N> listItem, final K listKey) {
            return append(new KeyStep<>(listItem, listKey));
        }

        @Override
        public final <C extends ChoiceIn<? super T> & DataObject, K extends Key<N>,
                N extends KeyAware<K> & ChildOf<? super C>> KeyedBuilder<N, K> child(final Class<C> caze,
                    final Class<N> listItem, final K listKey) {
            return append(new KeyStep<>(listItem, requireNonNull(caze), listKey));
        }

        /**
         * Build the instance identifier.
         *
         * @return Resulting {@link DataObjectWildcard}.
         */
        @Override
        public abstract @NonNull DataObjectWildcard<T> build();

        @Override
        final <X extends DataObject> SimpleBuilder<X> append(final ExactDataObjectStep<?> step) {
            return append((DataObjectStep<?>) step);
        }

        @Override
        final <X extends DataObject> SimpleBuilder<X> append(final InexactDataObjectStep<?> step) {
            return append((DataObjectStep<?>) step);
        }

        @Override
        abstract <X extends DataObject & KeyAware<Y>, Y extends Key<X>> KeyedBuilder<X, Y> append(KeyStep<?, ?> step);

        abstract <X extends DataObject> @NonNull SimpleBuilder<X> append(@NonNull DataObjectStep<?> step);
    }

    private static final class SimpleBuilder<T extends DataObject> extends Builder<T> {
        private @NonNull DataObjectStep<T> lastStep;

        @SuppressWarnings("unchecked")
        SimpleBuilder(final DataObjectStep<?> step) {
            super(step);
            lastStep = (DataObjectStep<T>) requireNonNull(step);
        }

        SimpleBuilder(final DataObjectWildcard<T> identifier) {
            super(identifier);
            lastStep = identifier.lastStep();
        }

        @SuppressWarnings("unchecked")
        SimpleBuilder(final KeyedBuilder<?, ?> prev, final DataObjectStep<?> step) {
            super(prev, step);
            lastStep = (DataObjectStep<T>) requireNonNull(step);
        }

        @Override
        public DataObjectWildcard<T> build() {
            return new Simple<>(buildSteps(), lastStep);
        }

        @Override
        @SuppressWarnings("unchecked")
        <X extends DataObject> SimpleBuilder<X> append(final DataObjectStep<?> step) {
            lastStep = (DataObjectStep<T>) requireNonNull(step);
            addStep(step);
            return (SimpleBuilder<X>) this;
        }

        @Override
        <X extends DataObject & KeyAware<Y>, Y extends Key<X>> KeyedBuilder<X, Y> append(final KeyStep<?, ?> item) {
            return new KeyedBuilder<>(this, item);
        }
    }

    public static final class KeyedBuilder<T extends DataObject & KeyAware<K>, K extends Key<T>>
            extends Builder<T> {
        private @NonNull KeyStep<K, T> lastStep;

        KeyedBuilder(final KeyStep<K, T> firstStep) {
            super(firstStep);
            lastStep = requireNonNull(firstStep);
        }

        KeyedBuilder(final WithKey<T, K> identifier) {
            super(identifier);
            lastStep = identifier.lastStep();
        }

        @SuppressWarnings("unchecked")
        KeyedBuilder(final SimpleBuilder<?> prev, final KeyStep<?, ?> lastStep) {
            super(prev, lastStep);
            this.lastStep = (KeyStep<K, T>) requireNonNull(lastStep);
        }

        /**
         * Build the instance identifier.
         *
         * @return Resulting {@link WithKey}.
         */
        @Override
        public @NonNull WithKey<T, K> build() {
            return new WithKey<>(buildSteps(), lastStep);
        }

        @Override
        <X extends DataObject> SimpleBuilder<X> append(final DataObjectStep<?> step) {
            return new SimpleBuilder<>(this, step);
        }

        @Override
        @SuppressWarnings("unchecked")
        <X extends DataObject & KeyAware<Y>, Y extends Key<X>> KeyedBuilder<X, Y> append(final KeyStep<?, ?> step) {
            lastStep = (KeyStep<K, T>) requireNonNull(step);
            addStep(step);
            return (KeyedBuilder<X, Y>) this;
        }
    }
}
