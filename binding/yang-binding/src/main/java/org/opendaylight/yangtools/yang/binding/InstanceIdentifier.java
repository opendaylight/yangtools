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

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.HierarchicalIdentifier;
import org.opendaylight.yangtools.util.HashCodeBuilder;

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
public sealed class InstanceIdentifier<T extends DataObject>
        implements HierarchicalIdentifier<InstanceIdentifier<? extends DataObject>>
        permits KeyedInstanceIdentifier {
    @java.io.Serial
    private static final long serialVersionUID = 3L;

    /*
     * Protected to differentiate internal and external access. Internal access is required never to modify
     * the contents. References passed to outside entities have to be wrapped in an unmodifiable view.
     */
    final Iterable<DataObjectStep<?>> pathArguments;

    private final @NonNull Class<T> targetType;
    private final boolean wildcarded;
    private final int hash;

    InstanceIdentifier(final Class<T> type, final Iterable<DataObjectStep<?>> pathArguments, final boolean wildcarded,
            final int hash) {
        this.pathArguments = requireNonNull(pathArguments);
        targetType = requireNonNull(type);
        this.wildcarded = wildcarded;
        this.hash = hash;
    }

    /**
     * Return the type of data which this InstanceIdentifier identifies.
     *
     * @return Target type
     */
    public final @NonNull Class<T> getTargetType() {
        return targetType;
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
        verify(target.equals(targetType), "Cannot adapt %s to %s", this, target);
        return (InstanceIdentifier<N>) this;
    }

    /**
     * Return the path argument chain which makes up this instance identifier.
     *
     * @return Path argument chain. Immutable and does not contain nulls.
     */
    public final @NonNull Iterable<DataObjectStep<?>> getPathArguments() {
        return Iterables.unmodifiableIterable(pathArguments);
    }

    /**
     * Check whether an instance identifier contains any wildcards. A wildcard is an path argument which has a null key.
     *
     * @return true if any of the path arguments has a null key.
     */
    public final boolean isWildcarded() {
        return wildcarded;
    }

    @Override
    public final int hashCode() {
        return hash;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final var other = (InstanceIdentifier<?>) obj;
        if (pathArguments == other.pathArguments) {
            return true;
        }

        /*
         * We could now just go and compare the pathArguments, but that can be potentially expensive. Let's try to avoid
         * that by checking various things that we have cached from pathArguments and trying to prove the identifiers
         * are *not* equal.
         */
        return hash == other.hash && wildcarded == other.wildcarded && targetType == other.targetType
            && keyEquals(other)
            // Everything checks out so far, so we have to do a full equals
            && Iterables.elementsEqual(pathArguments, other.pathArguments);
    }

    boolean keyEquals(final InstanceIdentifier<?> other) {
        return true;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    /**
     * Add class-specific toString attributes.
     *
     * @param toStringHelper ToStringHelper instance
     * @return ToStringHelper instance which was passed in
     */
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("targetType", targetType).add("path", Iterables.toString(pathArguments));
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
     *
     * <p>
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
        for (var step : pathArguments) {
            if (type.equals(step.type())) {
                @SuppressWarnings("unchecked")
                final var ret = (InstanceIdentifier<I>) internalCreate(Iterables.limit(pathArguments, count));
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
        for (var step : pathArguments) {
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
    public final boolean contains(final InstanceIdentifier<? extends DataObject> other) {
        requireNonNull(other, "other should not be null");

        final var oit = other.pathArguments.iterator();
        for (var step : pathArguments) {
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

        final var otherSteps = other.pathArguments.iterator();
        for (var step : pathArguments) {
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
        return trustedCreate(arg, Iterables.concat(pathArguments, Collections.singleton(arg)),
            HashCodeBuilder.nextHashCode(hash, arg), wildcarded);
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
        return childIdentifier(createStep(container));
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
        return childIdentifier(createStep(caze, container));
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
        N extends KeyAware<K> & ChildOf<? super C>> @NonNull KeyedInstanceIdentifier<N, K> child(
                final Class<@NonNull C> caze, final Class<@NonNull N> listItem, final K listKey) {
        return (KeyedInstanceIdentifier<N, K>) childIdentifier(new KeyStep<>(listItem, requireNonNull(caze), listKey));
    }

    /**
     * Create an InstanceIdentifier for a child augmentation. This method is a more efficient equivalent to
     * {@code builder().augmentation(container).build()}.
     *
     * @param container Container to append
     * @param <N> Container type
     * @return An InstanceIdentifier.
     * @throws NullPointerException if {@code container} is null
     */
    public final <N extends DataObject & Augmentation<? super T>> @NonNull InstanceIdentifier<N> augmentation(
            final Class<@NonNull N> container) {
        return childIdentifier(new NodeStep<>(container));
    }

    @java.io.Serial
    Object writeReplace() throws ObjectStreamException {
        return new IIv4<>(this);
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

    private void throwNSE() throws NotSerializableException {
        throw new NotSerializableException(getClass().getName());
    }

    /**
     * Create a builder rooted at this key.
     *
     * @return A builder instance
     */
    // FIXME: rename this method to 'toBuilder()'
    public @NonNull Builder<T> builder() {
        return new RegularBuilder<>(this);
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
        return new RegularBuilder<>(createStep(container));
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
        return new RegularBuilder<>(createStep(caze, container));
    }

    /**
     * Create a {@link Builder} for a specific type of InstanceIdentifier which represents an {@link IdentifiableItem}.
     *
     * @param listItem list item class
     * @param listKey key value
     * @param <N> List type
     * @param <K> List key
     * @return A new {@link Builder}
     * @throws NullPointerException if any argument is null
     */
    public static <N extends KeyAware<K> & ChildOf<? extends DataRoot>,
            K extends Key<N>> @NonNull KeyedBuilder<N, K> builder(final Class<N> listItem,
                    final K listKey) {
        return new KeyedBuilder<>(new KeyStep<>(listItem, listKey));
    }

    /**
     * Create a {@link Builder} for a specific type of InstanceIdentifier which represents an {@link IdentifiableItem}
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
    public static <C extends ChoiceIn<? extends DataRoot> & DataObject,
            N extends KeyAware<K> & ChildOf<? super C>, K extends Key<N>>
            @NonNull KeyedBuilder<N, K> builder(final Class<C> caze, final Class<N> listItem,
                    final K listKey) {
        return new KeyedBuilder<>(new KeyStep<>(listItem, requireNonNull(caze), listKey));
    }

    public static <R extends DataRoot & DataObject, T extends ChildOf<? super R>>
            @NonNull Builder<T> builderOfInherited(final Class<R> root, final Class<T> container) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new RegularBuilder<>(createStep(container));
    }

    public static <R extends DataRoot & DataObject, C extends ChoiceIn<? super R> & DataObject,
            T extends ChildOf<? super C>>
            @NonNull Builder<T> builderOfInherited(final Class<R> root,
                final Class<C> caze, final Class<T> container) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new RegularBuilder<>(createStep(caze, container));
    }

    public static <R extends DataRoot & DataObject, N extends KeyAware<K> & ChildOf<? super R>,
            K extends Key<N>>
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
    private static @NonNull InstanceIdentifier<?> internalCreate(final Iterable<DataObjectStep<?>> pathArguments) {
        final var it = requireNonNull(pathArguments, "pathArguments may not be null").iterator();
        checkArgument(it.hasNext(), "pathArguments may not be empty");

        final var hashBuilder = new HashCodeBuilder<DataObjectStep<?>>();
        boolean wildcard = false;
        DataObjectStep<?> arg;

        do {
            arg = it.next();
            // Non-null is implied by our callers
            final var type = verifyNotNull(arg).type();
            checkArgument(ChildOf.class.isAssignableFrom(type) || Augmentation.class.isAssignableFrom(type),
                "%s is not a valid path argument", type);

            hashBuilder.addArgument(arg);

            if (!(arg instanceof ExactDataObjectStep)) {
                wildcard = true;
            }
        } while (it.hasNext());

        return trustedCreate(arg, pathArguments, hashBuilder.build(), wildcard);
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
    public static <T extends DataObject> @NonNull InstanceIdentifier<T> unsafeOf(
            final List<? extends DataObjectStep<?>> pathArguments) {
        return (InstanceIdentifier<T>) internalCreate(ImmutableList.copyOf(pathArguments));
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
    public static <T extends ChildOf<? extends DataRoot>> @NonNull InstanceIdentifier<T> create(
            final Class<@NonNull T> type) {
        return (InstanceIdentifier<T>) internalCreate(ImmutableList.of(createStep(type)));
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
            final InstanceIdentifier<N> id) {
        requireNonNull(id);
        checkArgument(id instanceof KeyedInstanceIdentifier, "%s does not have a key", id);

        @SuppressWarnings("unchecked")
        final K ret = ((KeyedInstanceIdentifier<N, K>)id).getKey();
        return ret;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    static <N extends DataObject> @NonNull InstanceIdentifier<N> trustedCreate(final DataObjectStep<?> lastStep,
            final Iterable<DataObjectStep<?>> pathArguments, final int hash, final boolean wildcarded) {
        if (lastStep instanceof NodeStep) {
            return new InstanceIdentifier(lastStep.type(), pathArguments, wildcarded, hash);
        } else if (lastStep instanceof KeyStep<?, ?> predicate) {
            return new KeyedInstanceIdentifier(predicate, pathArguments, wildcarded, hash);
        } else if (lastStep instanceof KeylessStep) {
            return new InstanceIdentifier(lastStep.type(), pathArguments, true, hash);
        } else {
            throw new IllegalStateException("Unhandled step " + lastStep);
        }
    }

    @Deprecated(since = "13.0.0", forRemoval = true)
    private abstract static sealed class AbstractPathArgument<T extends DataObject>
            implements Comparable<AbstractPathArgument<?>>, Serializable {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        private final @NonNull Class<T> type;

        AbstractPathArgument(final Class<T> type) {
            this.type = requireNonNull(type, "Type may not be null.");
        }

        /**
         * Return the data object type backing this PathArgument.
         *
         * @return Data object type.
         */
        final @NonNull Class<T> type() {
            return type;
        }

        /**
         * Return an optional enclosing case type. This is used only when {@link #type()} references a node defined
         * in a {@code grouping} which is reference inside a {@code case} statement in order to safely reference the
         * node.
         *
         * @return case class or {@code null}
         */
        Class<? extends DataObject> caseType() {
            return null;
        }

        @Nullable Object key() {
            return null;
        }

        @Override
        public final int hashCode() {
            return Objects.hash(type, caseType(), key());
        }

        @Override
        public final boolean equals(final Object obj) {
            return this == obj || obj instanceof AbstractPathArgument<?> other && type.equals(other.type)
                && Objects.equals(key(), other.key()) && Objects.equals(caseType(), other.caseType());
        }

        @Override
        public final int compareTo(final AbstractPathArgument<?> arg) {
            final int cmp = compareClasses(type, arg.type());
            if (cmp != 0) {
                return cmp;
            }
            final var caseType = caseType();
            final var argCaseType = arg.caseType();
            if (caseType == null) {
                return argCaseType == null ? 1 : -1;
            }
            return argCaseType == null ? 1 : compareClasses(caseType, argCaseType);
        }

        private static int compareClasses(final Class<?> first, final Class<?> second) {
            return first.getCanonicalName().compareTo(second.getCanonicalName());
        }

        @java.io.Serial
        final Object readResolve() throws ObjectStreamException {
            return toStep();
        }

        abstract DataObjectStep<?> toStep();
    }

    /**
     * An Item represents an object that probably is only one of it's kind. For example a Nodes object is only one of
     * a kind. In YANG terms this would probably represent a container.
     *
     * @param <T> Item type
     */
    @Deprecated(since = "13.0.0", forRemoval = true)
    private static sealed class Item<T extends DataObject> extends AbstractPathArgument<T> {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        Item(final Class<T> type) {
            super(type);
        }

        @Override
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final DataObjectStep<?> toStep() {
            return createStep((Class) caseType(), type());
        }

        @Override
        public String toString() {
            return type().getName();
        }
    }

    /**
     * An IdentifiableItem represents a object that is usually present in a collection and can be identified uniquely
     * by a key. In YANG terms this would probably represent an item in a list.
     *
     * @param <I> An object that is identifiable by an identifier
     * @param <T> The identifier of the object
     */
    @Deprecated(since = "13.0.0", forRemoval = true)
    private static sealed class IdentifiableItem<I extends KeyAware<T> & DataObject, T extends Key<I>>
            extends AbstractPathArgument<I> {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        private final @NonNull T key;

        IdentifiableItem(final Class<I> type, final T key) {
            super(type);
            this.key = requireNonNull(key, "Key may not be null.");
        }

        /**
         * Return the data object type backing this PathArgument.
         *
         * @return Data object type.
         */
        @Override
        final @NonNull T key() {
            return key;
        }

        @Override
        final KeyStep<?, ?> toStep() {
            return new KeyStep<>(type(), caseType(), key);
        }

        @Override
        public String toString() {
            return type().getName() + "[key=" + key + "]";
        }
    }

    @Deprecated(since = "13.0.0", forRemoval = true)
    private static final class CaseItem<C extends ChoiceIn<?> & DataObject, T extends ChildOf<? super C>>
            extends Item<T> {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        private final Class<C> caseType;

        CaseItem(final Class<C> caseType, final Class<T> type) {
            super(type);
            this.caseType = requireNonNull(caseType);
        }

        @Override
        Class<C> caseType() {
            return caseType;
        }
    }

    @Deprecated(since = "13.0.0", forRemoval = true)
    private static final class CaseIdentifiableItem<C extends ChoiceIn<?> & DataObject,
            T extends ChildOf<? super C> & KeyAware<K>, K extends Key<T>> extends IdentifiableItem<T, K> {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        private final Class<C> caseType;

        CaseIdentifiableItem(final Class<C> caseType, final Class<T> type, final K key) {
            super(type, key);
            this.caseType = requireNonNull(caseType);
        }

        @Override
        Class<C> caseType() {
            return caseType;
        }
    }

    /**
     * A builder of {@link InstanceIdentifier} objects.
     *
     * @param <T> Instance identifier target type
     */
    public abstract static sealed class Builder<T extends DataObject> {
        private final ImmutableList.Builder<DataObjectStep<?>> pathBuilder;
        private final HashCodeBuilder<DataObjectStep<?>> hashBuilder;
        private final Iterable<? extends DataObjectStep<?>> basePath;

        private boolean wildcard;

        Builder(final Builder<?> prev, final DataObjectStep<?> item) {
            pathBuilder = prev.pathBuilder;
            hashBuilder = prev.hashBuilder;
            basePath = prev.basePath;
            wildcard = prev.wildcard;
            appendItem(item);
        }

        Builder(final InstanceIdentifier<T> identifier) {
            pathBuilder = ImmutableList.builder();
            hashBuilder = new HashCodeBuilder<>(identifier.hashCode());
            wildcard = identifier.isWildcarded();
            basePath = identifier.pathArguments;
        }

        Builder(final DataObjectStep<?> item, final boolean wildcard) {
            pathBuilder = ImmutableList.builder();
            hashBuilder = new HashCodeBuilder<>();
            basePath = null;
            hashBuilder.addArgument(item);
            pathBuilder.add(item);
            this.wildcard = wildcard;
        }

        final boolean wildcard() {
            return wildcard;
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
            return append(createStep(container));
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
            return append(createStep(caze, container));
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
        public final <N extends KeyAware<K> & ChildOf<? super T>, K extends Key<N>> KeyedBuilder<N, K> child(
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
                N extends KeyAware<K> & ChildOf<? super C>> KeyedBuilder<N, K> child(final Class<C> caze,
                    final Class<N> listItem, final K listKey) {
            return append(new KeyStep<>(listItem, requireNonNull(caze), listKey));
        }

        /**
         * Build the instance identifier.
         *
         * @return Resulting {@link InstanceIdentifier}.
         */
        public abstract @NonNull InstanceIdentifier<T> build();

        @Override
        public final int hashCode() {
            return hashBuilder.build();
        }

        @Override
        public final boolean equals(final Object obj) {
            return this == obj || obj instanceof Builder<?> other
                && wildcard == other.wildcard && hashCode() == other.hashCode()
                && Iterables.elementsEqual(pathArguments(), other.pathArguments());
        }

        final Iterable<DataObjectStep<?>> pathArguments() {
            final var args = pathBuilder.build();
            return basePath == null ? args : Iterables.concat(basePath, args);
        }

        final void appendItem(final DataObjectStep<?> item) {
            hashBuilder.addArgument(item);
            pathBuilder.add(item);
            if (!(item instanceof ExactDataObjectStep)) {
                wildcard = true;
            }
        }

        abstract <X extends DataObject> @NonNull RegularBuilder<X> append(DataObjectStep<X> step);

        abstract <X extends DataObject & KeyAware<Y>, Y extends Key<X>> @NonNull KeyedBuilder<X, Y> append(
            KeyStep<Y, X> step);
    }

    public static final class KeyedBuilder<T extends DataObject & KeyAware<K>, K extends Key<T>>
            extends Builder<T> {
        private @NonNull KeyStep<K, T> lastStep;

        KeyedBuilder(final KeyStep<K, T> firstStep) {
            super(firstStep, false);
            lastStep = requireNonNull(firstStep);
        }

        KeyedBuilder(final KeyedInstanceIdentifier<T, K> identifier) {
            super(identifier);
            lastStep = identifier.lastStep();
        }

        private KeyedBuilder(final RegularBuilder<?> prev, final KeyStep<K, T> lastStep) {
            super(prev, lastStep);
            this.lastStep = requireNonNull(lastStep);
        }

        /**
         * Build the instance identifier.
         *
         * @return Resulting {@link KeyedInstanceIdentifier}.
         */
        @Override
        public @NonNull KeyedInstanceIdentifier<T, K> build() {
            return new KeyedInstanceIdentifier<>(lastStep, pathArguments(), wildcard(), hashCode());
        }

        @Override
        <X extends DataObject> @NonNull RegularBuilder<X> append(final DataObjectStep<X> step) {
            return new RegularBuilder<>(this, step);
        }

        @Override
        @SuppressWarnings("unchecked")
        <X extends DataObject & KeyAware<Y>, Y extends Key<X>> KeyedBuilder<X, Y> append(final KeyStep<Y, X> step) {
            appendItem(step);
            lastStep = (KeyStep<K, T>) requireNonNull(step);
            return (KeyedBuilder<X, Y>) this;
        }
    }

    private static final class RegularBuilder<T extends DataObject> extends Builder<T> {
        private @NonNull Class<T> type;

        RegularBuilder(final DataObjectStep<T> item) {
            super(item, !(item instanceof ExactDataObjectStep));
            type = item.type();
        }

        RegularBuilder(final InstanceIdentifier<T> identifier) {
            super(identifier);
            type = identifier.getTargetType();
        }

        private RegularBuilder(final KeyedBuilder<?, ?> prev, final DataObjectStep<T> item) {
            super(prev, item);
            type = item.type();
        }

        @Override
        public InstanceIdentifier<T> build() {
            return new InstanceIdentifier<>(type, pathArguments(), wildcard(), hashCode());
        }

        @Override
        @SuppressWarnings({ "rawtypes", "unchecked" })
        <X extends DataObject> RegularBuilder<X> append(final DataObjectStep<X> step) {
            appendItem(step);
            type = (Class) step.type();
            return (RegularBuilder<X>) this;
        }

        @Override
        <X extends DataObject & KeyAware<Y>, Y extends Key<X>> KeyedBuilder<X, Y> append(
                final KeyStep<Y, X> item) {
            return new KeyedBuilder<>(this, item);
        }
    }
}
