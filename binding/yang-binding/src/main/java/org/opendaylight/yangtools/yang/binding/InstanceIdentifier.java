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

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.HierarchicalIdentifier;
import org.opendaylight.yangtools.util.HashCodeBuilder;

/**
 * This instance identifier uniquely identifies a specific DataObject in the data tree modeled by YANG.
 *
 * <p>
 * For Example let's say you were trying to refer to a node in inventory which was modeled in YANG as follows,
 *
 * <p>
 * <pre>
 * module opendaylight-inventory {
 *      ....
 *
 *      container nodes {
 *        list node {
 *            key "id";
 *            ext:context-instance "node-context";
 *
 *            uses node;
 *        }
 *    }
 *
 * }
 * </pre>
 *
 * <p>
 * You can create an instance identifier as follows to get to a node with id "openflow:1": {@code
 * InstanceIdentifierBuilder.builder(Nodes.class).child(Node.class, new NodeKey(new NodeId("openflow:1")).build();
 * }
 *
 * <p>
 * This would be the same as using a path like so, "/nodes/node/openflow:1" to refer to the openflow:1 node
 */
public class InstanceIdentifier<T extends DataObject>
        implements HierarchicalIdentifier<InstanceIdentifier<? extends DataObject>> {
    @Serial
    private static final long serialVersionUID = 3L;

    /*
     * Protected to differentiate internal and external access. Internal access is required never to modify
     * the contents. References passed to outside entities have to be wrapped in an unmodifiable view.
     */
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "Handled through Externalizable proxy")
    final Iterable<PathArgument> pathArguments;

    private final @NonNull Class<T> targetType;
    private final boolean wildcarded;
    private final int hash;

    InstanceIdentifier(final Class<T> type, final Iterable<PathArgument> pathArguments, final boolean wildcarded,
            final int hash) {
        this.pathArguments = requireNonNull(pathArguments);
        this.targetType = requireNonNull(type);
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
    public final @NonNull Iterable<PathArgument> getPathArguments() {
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

        final InstanceIdentifier<?> other = (InstanceIdentifier<?>) obj;
        if (pathArguments == other.pathArguments) {
            return true;
        }

        /*
         * We could now just go and compare the pathArguments, but that
         * can be potentially expensive. Let's try to avoid that by
         * checking various things that we have cached from pathArguments
         * and trying to prove the identifiers are *not* equal.
         */
        if (hash != other.hash) {
            return false;
        }
        if (wildcarded != other.wildcarded) {
            return false;
        }
        if (targetType != other.targetType) {
            return false;
        }
        if (fastNonEqual(other)) {
            return false;
        }

        // Everything checks out so far, so we have to do a full equals
        return Iterables.elementsEqual(pathArguments, other.pathArguments);
    }

    /**
     * Perform class-specific fast checks for non-equality. This allows subclasses to avoid iterating over the
     * pathArguments by performing quick checks on their specific fields.
     *
     * @param other The other identifier, guaranteed to be the same class
     * @return true if the other identifier cannot be equal to this one.
     */
    protected boolean fastNonEqual(final InstanceIdentifier<?> other) {
        return false;
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
     *      identifier = InstanceIdentifierBuilder.builder(Nodes.class).child(Node.class,
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
        for (final PathArgument a : pathArguments) {
            if (type.equals(a.getType())) {
                @SuppressWarnings("unchecked")
                final InstanceIdentifier<I> ret = (InstanceIdentifier<I>) internalCreate(
                        Iterables.limit(pathArguments, count));
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
    public final <N extends Identifiable<K> & DataObject, K extends Identifier<N>> @Nullable K firstKeyOf(
            final Class<@NonNull N> listItem) {
        for (final PathArgument i : pathArguments) {
            if (listItem.equals(i.getType())) {
                @SuppressWarnings("unchecked")
                final K ret = ((IdentifiableItem<N, K>)i).getKey();
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

        final Iterator<?> lit = pathArguments.iterator();
        final Iterator<?> oit = other.pathArguments.iterator();

        while (lit.hasNext()) {
            if (!oit.hasNext()) {
                return false;
            }

            if (!lit.next().equals(oit.next())) {
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

        final Iterator<PathArgument> lit = pathArguments.iterator();
        final Iterator<PathArgument> oit = other.pathArguments.iterator();

        while (lit.hasNext()) {
            if (!oit.hasNext()) {
                return false;
            }

            final PathArgument la = lit.next();
            final PathArgument oa = oit.next();

            if (!la.getType().equals(oa.getType())) {
                return false;
            }
            if (la instanceof IdentifiableItem<?, ?> && oa instanceof IdentifiableItem<?, ?> && !la.equals(oa)) {
                return false;
            }
        }

        return true;
    }

    private <N extends DataObject> @NonNull InstanceIdentifier<N> childIdentifier(final AbstractPathArgument<N> arg) {
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
        return childIdentifier(Item.of(container));
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
    public final <N extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<N>>
            @NonNull KeyedInstanceIdentifier<N, K> child(final Class<@NonNull N> listItem, final K listKey) {
        return (KeyedInstanceIdentifier<N, K>) childIdentifier(IdentifiableItem.of(listItem, listKey));
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
        return childIdentifier(Item.of(caze, container));
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
    public final <C extends ChoiceIn<? super T> & DataObject, K extends Identifier<N>,
        N extends Identifiable<K> & ChildOf<? super C>> @NonNull KeyedInstanceIdentifier<N, K> child(
                final Class<@NonNull C> caze, final Class<@NonNull N> listItem, final K listKey) {
        return (KeyedInstanceIdentifier<N, K>) childIdentifier(IdentifiableItem.of(caze, listItem, listKey));
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
        return childIdentifier(Item.of(container));
    }

    /**
     * Create a builder rooted at this key.
     *
     * @return A builder instance
     */
    // FIXME: rename this method to 'toBuilder()'
    public @NonNull InstanceIdentifierBuilder<T> builder() {
        return new InstanceIdentifierBuilderImpl<>(Item.of(targetType), pathArguments, hash, wildcarded);
    }

    /**
     * Create an InstanceIdentifierBuilder for a specific type of InstanceIdentifier as specified by container.
     *
     * @param container Base container
     * @param <T> Type of the container
     * @return A new {@link InstanceIdentifierBuilder}
     * @throws NullPointerException if {@code container} is null
     */
    public static <T extends ChildOf<? extends DataRoot>> @NonNull InstanceIdentifierBuilder<T> builder(
            final Class<T> container) {
        return new InstanceIdentifierBuilderImpl<T>().addWildNode(Item.of(container));
    }

    /**
     * Create an InstanceIdentifierBuilder for a specific type of InstanceIdentifier as specified by container in
     * a {@code grouping} used in the {@code case} statement.
     *
     * @param caze Choice case class
     * @param container Base container
     * @param <C> Case type
     * @param <T> Type of the container
     * @return A new {@link InstanceIdentifierBuilder}
     * @throws NullPointerException if any argument is null
     */
    public static <C extends ChoiceIn<? extends DataRoot> & DataObject, T extends ChildOf<? super C>>
            @NonNull InstanceIdentifierBuilder<T> builder(final Class<C> caze, final Class<T> container) {
        return new InstanceIdentifierBuilderImpl<T>().addWildNode(Item.of(caze, container));
    }

    /**
     * Create an InstanceIdentifierBuilder for a specific type of InstanceIdentifier which represents an
     * {@link IdentifiableItem}.
     *
     * @param listItem list item class
     * @param listKey key value
     * @param <N> List type
     * @param <K> List key
     * @return A new {@link InstanceIdentifierBuilder}
     * @throws NullPointerException if any argument is null
     */
    public static <N extends Identifiable<K> & ChildOf<? extends DataRoot>,
            K extends Identifier<N>> @NonNull InstanceIdentifierBuilder<N> builder(final Class<N> listItem,
                    final K listKey) {
        return new InstanceIdentifierBuilderImpl<N>().addNode(IdentifiableItem.of(listItem, listKey));
    }

    /**
     * Create an InstanceIdentifierBuilder for a specific type of InstanceIdentifier which represents an
     * {@link IdentifiableItem} in a {@code grouping} used in the {@code case} statement.
     *
     * @param caze Choice case class
     * @param listItem list item class
     * @param listKey key value
     * @param <C> Case type
     * @param <N> List type
     * @param <K> List key
     * @return A new {@link InstanceIdentifierBuilder}
     * @throws NullPointerException if any argument is null
     */
    public static <C extends ChoiceIn<? extends DataRoot> & DataObject,
            N extends Identifiable<K> & ChildOf<? super C>, K extends Identifier<N>>
            @NonNull InstanceIdentifierBuilder<N> builder(final Class<C> caze, final Class<N> listItem,
                    final K listKey) {
        return new InstanceIdentifierBuilderImpl<N>().addNode(IdentifiableItem.of(caze, listItem, listKey));
    }

    public static <R extends DataRoot & DataObject, T extends ChildOf<? super R>>
            @NonNull InstanceIdentifierBuilder<T> builderOfInherited(final Class<R> root, final Class<T> container) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new InstanceIdentifierBuilderImpl<T>().addWildNode(Item.of(container));
    }

    public static <R extends DataRoot & DataObject, C extends ChoiceIn<? super R> & DataObject,
            T extends ChildOf<? super C>>
            @NonNull InstanceIdentifierBuilder<T> builderOfInherited(final Class<R> root,
                final Class<C> caze, final Class<T> container) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new InstanceIdentifierBuilderImpl<T>().addWildNode(Item.of(caze, container));
    }

    public static <R extends DataRoot & DataObject, N extends Identifiable<K> & ChildOf<? super R>,
            K extends Identifier<N>>
            @NonNull InstanceIdentifierBuilder<N> builderOfInherited(final Class<R> root,
                final Class<N> listItem, final K listKey) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new InstanceIdentifierBuilderImpl<N>().addNode(IdentifiableItem.of(listItem, listKey));
    }

    public static <R extends DataRoot & DataObject, C extends ChoiceIn<? super R> & DataObject,
            N extends Identifiable<K> & ChildOf<? super C>, K extends Identifier<N>>
            @NonNull InstanceIdentifierBuilder<N> builderOfInherited(final Class<R> root,
                final Class<C> caze, final Class<N> listItem, final K listKey) {
        // FIXME: we are losing root identity, hence namespaces may not work correctly
        return new InstanceIdentifierBuilderImpl<N>().addNode(IdentifiableItem.of(caze, listItem, listKey));
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
    private static @NonNull InstanceIdentifier<?> internalCreate(final Iterable<PathArgument> pathArguments) {
        final var it = requireNonNull(pathArguments, "pathArguments may not be null").iterator();
        checkArgument(it.hasNext(), "pathArguments may not be empty");

        final HashCodeBuilder<PathArgument> hashBuilder = new HashCodeBuilder<>();
        boolean wildcard = false;
        PathArgument arg;

        do {
            arg = it.next();
            // Non-null is implied by our callers
            final var type = verifyNotNull(arg).getType();
            checkArgument(ChildOf.class.isAssignableFrom(type) || Augmentation.class.isAssignableFrom(type),
                "%s is not a valid path argument", type);

            hashBuilder.addArgument(arg);

            if (Identifiable.class.isAssignableFrom(type) && !(arg instanceof IdentifiableItem)) {
                wildcard = true;
            }
        } while (it.hasNext());

        return trustedCreate(arg, pathArguments, hashBuilder.build(), wildcard);
    }

    /**
     * Create an instance identifier for a sequence of {@link PathArgument} steps. The steps are required to be formed
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
            final List<? extends PathArgument> pathArguments) {
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
        return (InstanceIdentifier<T>) internalCreate(ImmutableList.of(Item.of(type)));
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
    public static <N extends Identifiable<K> & DataObject, K extends Identifier<N>> K keyOf(
            final InstanceIdentifier<N> id) {
        requireNonNull(id);
        checkArgument(id instanceof KeyedInstanceIdentifier, "%s does not have a key", id);

        @SuppressWarnings("unchecked")
        final K ret = ((KeyedInstanceIdentifier<N, K>)id).getKey();
        return ret;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    static <N extends DataObject> @NonNull InstanceIdentifier<N> trustedCreate(final PathArgument arg,
            final Iterable<PathArgument> pathArguments, final int hash, final boolean wildcarded) {
        if (arg instanceof IdentifiableItem<?, ?> identifiable) {
            return new KeyedInstanceIdentifier(arg.getType(), pathArguments, wildcarded, hash, identifiable.getKey());
        }

        final var type = arg.getType();
        return new InstanceIdentifier(type, pathArguments, wildcarded || Identifiable.class.isAssignableFrom(type),
            hash);
    }

    /**
     * Path argument of {@link InstanceIdentifier}. Interface which implementations are used as path components of the
     * path in overall data tree.
     */
    public interface PathArgument extends Comparable<PathArgument> {
        /**
         * Return the data object type backing this PathArgument.
         *
         * @return Data object type.
         */
        @NonNull Class<? extends DataObject> getType();

        /**
         * Return an optional enclosing case type. This is used only when {@link #getType()} references a node defined
         * in a {@code grouping} which is reference inside a {@code case} statement in order to safely reference the
         * node.
         *
         * @return Optional case class.
         */
        default Optional<? extends Class<? extends DataObject>> getCaseType() {
            return Optional.empty();
        }
    }

    private abstract static class AbstractPathArgument<T extends DataObject> implements PathArgument, Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private final @NonNull Class<T> type;

        AbstractPathArgument(final Class<T> type) {
            this.type = requireNonNull(type, "Type may not be null.");
        }

        @Override
        public final Class<T> getType() {
            return type;
        }

        Object getKey() {
            return null;
        }

        @Override
        public final int hashCode() {
            return Objects.hash(type, getCaseType(), getKey());
        }

        @Override
        public final boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AbstractPathArgument)) {
                return false;
            }
            final AbstractPathArgument<?> other = (AbstractPathArgument<?>) obj;
            return type.equals(other.type) && Objects.equals(getKey(), other.getKey())
                    && getCaseType().equals(other.getCaseType());
        }

        @Override
        public final int compareTo(final PathArgument arg) {
            final int cmp = compareClasses(type, arg.getType());
            if (cmp != 0) {
                return cmp;
            }
            final Optional<? extends Class<?>> caseType = getCaseType();
            if (!caseType.isPresent()) {
                return arg.getCaseType().isPresent() ? -1 : 1;
            }
            final Optional<? extends Class<?>> argCaseType = getCaseType();
            return argCaseType.isPresent() ? compareClasses(caseType.get(), argCaseType.get()) : 1;
        }

        private static int compareClasses(final Class<?> first, final Class<?> second) {
            return first.getCanonicalName().compareTo(second.getCanonicalName());
        }
    }

    /**
     * An Item represents an object that probably is only one of it's kind. For example a Nodes object is only one of
     * a kind. In YANG terms this would probably represent a container.
     *
     * @param <T> Item type
     */
    public static class Item<T extends DataObject> extends AbstractPathArgument<T> {
        @Serial
        private static final long serialVersionUID = 1L;

        Item(final Class<T> type) {
            super(type);
        }

        /**
         * Return a PathArgument instance backed by the specified class.
         *
         * @param type Backing class
         * @param <T> Item type
         * @return A new PathArgument
         * @throws NullPointerException if {@code} is null.
         */
        public static <T extends DataObject> @NonNull Item<T> of(final Class<T> type) {
            return new Item<>(type);
        }

        /**
         * Return a PathArgument instance backed by the specified class, which in turn is defined in a {@code grouping}
         * used in a corresponding {@code case} statement.
         *
         * @param caseType defining case class
         * @param type Backing class
         * @param <C> Case type
         * @param <T> Item type
         * @return A new PathArgument
         * @throws NullPointerException if any argument is null.
         */
        public static <C extends ChoiceIn<?> & DataObject, T extends ChildOf<? super C>> @NonNull Item<T> of(
                final Class<C> caseType, final Class<T> type) {
            return new CaseItem<>(caseType, type);
        }

        @Override
        public String toString() {
            return getType().getName();
        }
    }

    /**
     * An IdentifiableItem represents a object that is usually present in a collection and can be identified uniquely
     * by a key. In YANG terms this would probably represent an item in a list.
     *
     * @param <I> An object that is identifiable by an identifier
     * @param <T> The identifier of the object
     */
    public static class IdentifiableItem<I extends Identifiable<T> & DataObject, T extends Identifier<I>>
            extends AbstractPathArgument<I> {
        @Serial
        private static final long serialVersionUID = 1L;

        private final @NonNull T key;

        IdentifiableItem(final Class<I> type, final T key) {
            super(type);
            this.key = requireNonNull(key, "Key may not be null.");
        }

        /**
         * Return an IdentifiableItem instance backed by the specified class with specified key.
         *
         * @param type Backing class
         * @param key Key
         * @param <T> List type
         * @param <I> Key type
         * @return An IdentifiableItem
         * @throws NullPointerException if any argument is null.
         */
        public static <T extends Identifiable<I> & DataObject, I extends Identifier<T>>
                @NonNull IdentifiableItem<T, I> of(final Class<T> type, final I key) {
            return new IdentifiableItem<>(type, key);
        }

        /**
         * Return an IdentifiableItem instance backed by the specified class with specified key. The class is in turn
         * defined in a {@code grouping} used in a corresponding {@code case} statement.
         *
         * @param caseType defining case class
         * @param type Backing class
         * @param <C> Case type
         * @param <T> List type
         * @param <I> Key type
         * @return A new PathArgument
         * @throws NullPointerException if any argument is null.
         */
        public static <C extends ChoiceIn<?> & DataObject, T extends ChildOf<? super C> & Identifiable<I>,
                I extends Identifier<T>> @NonNull IdentifiableItem<T, I> of(final Class<C> caseType,
                        final Class<T> type, final I key) {
            return new CaseIdentifiableItem<>(caseType, type, key);
        }

        /**
         * Return the data object type backing this PathArgument.
         *
         * @return Data object type.
         */
        @Override
        public final @NonNull T getKey() {
            return key;
        }

        @Override
        public String toString() {
            return getType().getName() + "[key=" + key + "]";
        }
    }

    private static final class CaseItem<C extends ChoiceIn<?> & DataObject, T extends ChildOf<? super C>>
            extends Item<T> {
        @Serial
        private static final long serialVersionUID = 1L;

        private final Class<C> caseType;

        CaseItem(final Class<C> caseType, final Class<T> type) {
            super(type);
            this.caseType = requireNonNull(caseType);
        }

        @Override
        public Optional<Class<C>> getCaseType() {
            return Optional.of(caseType);
        }
    }

    private static final class CaseIdentifiableItem<C extends ChoiceIn<?> & DataObject,
            T extends ChildOf<? super C> & Identifiable<K>, K extends Identifier<T>> extends IdentifiableItem<T, K> {
        @Serial
        private static final long serialVersionUID = 1L;

        private final Class<C> caseType;

        CaseIdentifiableItem(final Class<C> caseType, final Class<T> type, final K key) {
            super(type, key);
            this.caseType = requireNonNull(caseType);
        }

        @Override
        public Optional<Class<C>> getCaseType() {
            return Optional.of(caseType);
        }
    }

    // FIXME: rename to 'Builder'
    // FIXME: introduce KeyedBuilder with specialized build() method
    public interface InstanceIdentifierBuilder<T extends DataObject> {
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
        <N extends ChildOf<? super T>> @NonNull InstanceIdentifierBuilder<N> child(Class<N> container);

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
        <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>>
                @NonNull InstanceIdentifierBuilder<N> child(Class<C> caze, Class<N> container);

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
        <N extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<N>>
                @NonNull InstanceIdentifierBuilder<N> child(Class<@NonNull N> listItem, K listKey);

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
        <C extends ChoiceIn<? super T> & DataObject, K extends Identifier<N>,
                N extends Identifiable<K> & ChildOf<? super C>> @NonNull InstanceIdentifierBuilder<N> child(
                        Class<C> caze, Class<N> listItem, K listKey);

        /**
         * Build an identifier which refers to a specific augmentation of the current InstanceIdentifier referenced by
         * the builder.
         *
         * @param container augmentation class
         * @param <N> augmentation type
         * @return this builder
         * @throws NullPointerException if {@code container} is null
         */
        <N extends DataObject & Augmentation<? super T>> @NonNull InstanceIdentifierBuilder<N> augmentation(
                Class<N> container);

        /**
         * Build the instance identifier.
         *
         * @return Resulting instance identifier.
         */
        @NonNull InstanceIdentifier<T> build();
    }

    @Serial
    private Object writeReplace() throws ObjectStreamException {
        return new InstanceIdentifierV3<>(this);
    }
}
