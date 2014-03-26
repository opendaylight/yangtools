/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Path;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 *
 * This instance identifier uniquely identifies a specific DataObject in the data tree modeled by YANG.
 *
 * For Example let's say you were trying to refer to a node in inventory which was modeled in YANG as follows,
 *
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
 * You could create an instance identifier as follows to get to a node with id "openflow:1"
 *
 * InstanceIdentifierBuilder.builder(Nodes.class).child(Node.class, new NodeKey(new NodeId("openflow:1")).build();
 *
 * This would be the same as using a path like so, "/nodes/node/openflow:1" to refer to the openflow:1 node
 *
 */
public abstract class InstanceIdentifier<T extends DataObject> implements Path<InstanceIdentifier<? extends DataObject>>, Immutable {
    // protected to differentiate internal and external access
    protected final Iterable<PathArgument> pathArguments;
    private final Class<T> targetType;
    private final int hashCode;

    private InstanceIdentifier(final Class<T> type, final Iterable<PathArgument> pathArguments, final int hashCode) {
        this.pathArguments = Preconditions.checkNotNull(pathArguments);
        this.targetType = Preconditions.checkNotNull(type);
        this.hashCode = hashCode;
    }

    /**
     * Check whether an instance identifier contains any wildcards. A wildcard
     * is an path argument which has a null key.
     *
     * @return @true if any of the path arguments has a null key.
     */
    public abstract boolean isWildcarded();

    /**
     * Return the type of data which this InstanceIdentifier identifies.
     *
     * @return Target type
     */
    public final Class<T> getTargetType() {
        return targetType;
    }

    /**
     * Return the path argument chain which makes up this instance identifier.
     *
     * @return Path argument chain. Immutable and does not contain nulls.
     */
    public final Iterable<PathArgument> getPathArguments() {
        return Iterables.unmodifiableIterable(pathArguments);
    }

    @Override
    public final int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (hashCode() != obj.hashCode()) {
            return false;
        }

        final InstanceIdentifier<?> other = (InstanceIdentifier<?>) obj;
        return Iterables.elementsEqual(pathArguments, other.pathArguments);
    }

    @Override
    public final String toString() {
        return addToStringAttributes(Objects.toStringHelper(this)).toString();
    }

    /**
     * Add class-specific toString attributes.
     *
     * @param toStringHelper ToStringHelper instance
     * @return ToStringHelper instance which was passed in
     */
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("targetType", targetType).add("path", Iterables.toString(getPathArguments()));
    }

    /**
     * Return the key associated with the last component of the specified identifier.
     *
     * @param id instance identifier
     * @return key associated with the last component
     */
    public static <N extends Identifiable<K> & DataObject, K extends Identifier<N>> K keyOf(final InstanceIdentifier<N> id) {
        @SuppressWarnings("unchecked")
        final K ret = ((KeyedInstanceIdentifier<N, K>)id).getKey();
        return ret;
    }

    /**
     * @deprecated Use {@link #getPathComponents()} instead.
     */
    @Deprecated
    public final List<PathArgument> getPath() {
        return ImmutableList.<PathArgument>copyOf(getPathArguments());
    }

    /**
     * Return an instance identifier trimmed at the first occurrence of a
     * specific component type.
     *
     * For example let's say an instance identifier was built like so,
     * <pre>
     *      identifier = InstanceIdentifierBuilder.builder(Nodes.class).child(Node.class, new NodeKey(new NodeId("openflow:1")).build();
     * </pre>
     *
     * And you wanted to obtain the Instance identifier which represented Nodes you would do it like so,
     *
     * <pre>
     *      identifier.firstIdentifierOf(Nodes.class)
     * </pre>
     *
     * @param type component type
     * @return trimmed instance identifier, or null if the component type
     *         is not present.
     */
    @SuppressWarnings("hiding")
    public final <T extends DataObject> InstanceIdentifier<T> firstIdentifierOf(final Class<T> type) {
        int i = 1;
        for (final PathArgument a : getPathArguments()) {
            if (type.equals(a.getType())) {
                @SuppressWarnings("unchecked")
                final InstanceIdentifier<T> ret = (InstanceIdentifier<T>) create(Iterables.limit(getPathArguments(), i));
                return ret;
            }

            ++i;
        }

        return null;
    }

    /**
     * Return the key associated with the first component of specified type in
     * an identifier.
     *
     * @param listItem component type
     * @param listKey component key type
     * @return key associated with the component, or null if the component type
     *         is not present.
     */
    public final <N extends Identifiable<K> & DataObject, K extends Identifier<N>> K firstKeyOf(final Class<N> listItem, final Class<K> listKey) {
        for (final PathArgument i : getPathArguments()) {
            if (listItem.equals(i.getType())) {
                @SuppressWarnings("unchecked")
                final K ret = ((IdentifiableItem<N, K>)i).getKey();
                return ret;
            }
        }

        return null;
    }

    /**
     * The contains method checks if the other identifier is fully contained within the current identifier. It does this
     * by looking at only the types of the path arguments and not by comparing the path arguments themselse.
     * If you want to compare path arguments you must use containsWildcarded
     *
     * To illustrate here is an example which explains the working of this api.
     *
     * Let's say you have two instance identifiers as follows,
     *
     * this = /nodes/node/openflow:1
     * other = /nodes/node/openflow:2
     *
     * then this.contains(other) will return true. To ensure that this and other are compared properly you must use
     * containsWildcarded
     *
     * @param other
     * @return
     */
    @Override
    public final boolean contains(final InstanceIdentifier<? extends DataObject> other) {
        Preconditions.checkNotNull(other, "other should not be null");

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
     * The containsWildcarded method checks if the other identifier is fully contained within the current identifier.
     * It does this by looking at both the type and identity of the path arguments.
     *
     * @param other
     * @return
     */
    public final boolean containsWildcarded(final InstanceIdentifier<?> other) {
        Preconditions.checkNotNull(other, "other should not be null");

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

    /**
     * Create a builder rooted at this key.
     *
     * @return A builder instance
     */
    public InstanceIdentifierBuilder<T> builder() {
        return new BuilderImpl<T>(new Item<T>(targetType), pathArguments, hashCode, isWildcarded());
    }

    private InstanceIdentifier<?> childIdentifer(final PathArgument arg) {
        return trustedCreate(arg, Iterables.concat(pathArguments, Collections.singleton(arg)), HashCodeBuilder.nextHashCode(hashCode, arg), isWildcarded());
    }

    @SuppressWarnings("unchecked")
    public final <N extends ChildOf<? super T>> InstanceIdentifier<N> child(final Class<N> container) {
        final PathArgument arg = new Item<>(container);
        return (InstanceIdentifier<N>) childIdentifer(arg);
    }

    @SuppressWarnings("unchecked")
    public final <N extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<N>> InstanceIdentifier<N> child(
            final Class<N> listItem, final K listKey) {
        final PathArgument arg = new IdentifiableItem<>(listItem, listKey);
        return (InstanceIdentifier<N>) childIdentifer(arg);
    }

    @SuppressWarnings("unchecked")
    public final <N extends DataObject & Augmentation<? super T>> InstanceIdentifier<N> augmentation(
            final Class<N> container) {
        final PathArgument arg = new Item<>(container);
        return (InstanceIdentifier<N>) childIdentifer(arg);
    }

    /**
     * Path argument of {@link InstanceIdentifier}.
     * <p>
     * Interface which implementations are used as path components of the
     * path in overall data tree.
     */
    public interface PathArgument {
        Class<? extends DataObject> getType();
    }

    public interface InstanceIdentifierBuilder<T extends DataObject> extends Builder<InstanceIdentifier<T>> {
        /**
         * Append the specified container as a child of the current InstanceIdentifier referenced by the builder.
         *
         * This method should be used when you want to build an instance identifier by appending top-level
         * elements
         *
         * Example,
         * <pre>
         *     InstanceIdentifier.builder().child(Nodes.class).build();
         *
         * </pre>
         *
         * NOTE :- The above example is only for illustration purposes InstanceIdentifier.builder() has been deprecated
         * and should not be used. Use InstanceIdentifier.builder(Nodes.class) instead
         *
         * @param container
         * @param <N>
         * @return
         */
        <N extends ChildOf<? super T>> InstanceIdentifierBuilder<N> child(Class<N> container);

        /**
         * Append the specified listItem as a child of the current InstanceIdentifier referenced by the builder.
         *
         * This method should be used when you want to build an instance identifier by appending a specific list element
         * to the identifier
         *
         * @param listItem
         * @param listKey
         * @param <N>
         * @param <K>
         * @return
         */
        <N extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<N>> InstanceIdentifierBuilder<N> child(
                Class<N> listItem, K listKey);

        /**
         * Build an identifier which refers to a specific augmentation of the current InstanceIdentifier referenced by
         * the builder
         *
         * @param container
         * @param <N>
         * @return
         */
        <N extends DataObject & Augmentation<? super T>> InstanceIdentifierBuilder<N> augmentation(Class<N> container);

        /**
         * Build the instance identifier.
         *
         * @return
         */
        InstanceIdentifier<T> build();
    }

    private static abstract class AbstractPathArgument<T extends DataObject> implements PathArgument {
        private final Class<T> type;

        protected AbstractPathArgument(final Class<T> type) {
            this.type = Preconditions.checkNotNull(type, "Type may not be null.");
        }

        @Override
        public final Class<T> getType() {
            return type;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final AbstractPathArgument<?> other = (AbstractPathArgument<?>) obj;
            if (type == null) {
                if (other.type != null) {
                    return false;
                }
            } else if (!type.equals(other.type)) {
                return false;
            }
            return true;
        }
    }

    /**
     * An Item represents an object that probably is only one of it's kind. For example a Nodes object is only one of
     * a kind. In YANG terms this would probably represent a container.
     *
     * @param <T>
     */
    public static final class Item<T extends DataObject> extends AbstractPathArgument<T> {
        public Item(final Class<T> type) {
            super(type);
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
    public static final class IdentifiableItem<I extends Identifiable<T> & DataObject, T extends Identifier<I>> extends AbstractPathArgument<I> {
        private final T key;

        public IdentifiableItem(final Class<I> type, final T key) {
            super(type);
            this.key = Preconditions.checkNotNull(key, "Key may not be null.");
        }

        public T getKey() {
            return this.key;
        }

        @Override
        public boolean equals(final Object obj) {
            return super.equals(obj) && obj.hashCode() == hashCode() && key.equals(((IdentifiableItem<?, ?>) obj).getKey());
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public String toString() {
            return getType().getName() + "[key=" + key + "]";
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static InstanceIdentifier<?> create(final PathArgument arg, final Iterable<? extends PathArgument> pathArguments, final int hashCode, final boolean wildcard) {
        if (wildcard) {
            return new WildcardInstanceIdentifier(arg.getType(), pathArguments, hashCode);
        } else {
            return new ConcreteInstanceIdentifier(arg.getType(), pathArguments, hashCode);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static InstanceIdentifier<?> createKeyed(final PathArgument arg, final Iterable<? extends PathArgument> pathArguments, final int hashCode, final boolean wildcard) {
        Identifier<?> key = null;
        if (arg instanceof IdentifiableItem<?, ?>) {
            key = ((IdentifiableItem<?, ?>)arg).key;
        }

        if (wildcard) {
            return new KeyedWildcardInstanceIdentifier(arg.getType(), pathArguments, hashCode, key);
        } else {
            return new KeyedConcreteInstanceIdentifier(arg.getType(), pathArguments, hashCode, key);
        }
    }

    private static final class HashCodeBuilder implements Builder<Integer> {
        int hashCode;

        HashCodeBuilder() {
            this(1);
        }

        HashCodeBuilder(final int seedHashCode) {
            this.hashCode = seedHashCode;
        }

        public static int nextHashCode(final int hashCode, final PathArgument arg) {
            return 31 * hashCode + arg.hashCode();
        }

        private void addArgument(final PathArgument arg) {
            hashCode = nextHashCode(hashCode, arg);
        }

        @Override
        public Integer toInstance() {
            return hashCode;
        }
    }

    private static InstanceIdentifier<?> trustedCreate(final PathArgument arg, final Iterable<PathArgument> pathArguments, final int hashCode, final boolean wildcard) {
        if (Identifiable.class.isAssignableFrom(arg.getType())) {
            return createKeyed(arg, pathArguments, hashCode, wildcard);
        } else {
            return create(arg, pathArguments, hashCode, wildcard);
        }
    }

    /**
     * Create an instance identifier for a very specific object type.
     *
     * Example
     * <pre>
     *  List<PathArgument> path = Arrays.asList(new Item(Nodes.class))
     *  new InstanceIdentifier(path);
     * </pre>
     *
     * @param path The path to a specific node in the data tree
     * @return InstanceIdentifier instance
     * @throws IllegalArgumentException if pathArguments is empty or
     *         contains a null element.
     */
    public static InstanceIdentifier<?> create(final Iterable<? extends PathArgument> pathArguments) {
        final Iterator<? extends PathArgument> it = Preconditions.checkNotNull(pathArguments, "pathArguments may not be null").iterator();
        final HashCodeBuilder hashBuilder = new HashCodeBuilder();
        boolean wildcard = false;
        PathArgument a = null;

        while (it.hasNext()) {
            a = it.next();
            Preconditions.checkArgument(a != null, "pathArguments may not contain null elements");

            // TODO: sanity check ChildOf();
            hashBuilder.addArgument(a);

            if (Identifiable.class.isAssignableFrom(a.getType()) && !(a instanceof IdentifiableItem<?, ?>)) {
                wildcard = true;
            }
        }
        Preconditions.checkArgument(a != null, "pathArguments may not be empty");

        final Iterable<PathArgument> immutableArguments;
        if (pathArguments instanceof ImmutableCollection<?>) {
            immutableArguments = (Iterable<PathArgument>) pathArguments;
        } else {
            immutableArguments = ImmutableList.copyOf(pathArguments);
        }

        return trustedCreate(a, immutableArguments, hashBuilder.toInstance(), wildcard);
    }

    /**
     * Create an instance identifier for a very specific object type.
     *
     * For example
     * <pre>
     *      new InstanceIdentifier(Nodes.class)
     * </pre>
     * would create an InstanceIdentifier for an object of type Nodes
     *
     * @param type The type of the object which this instance identifier represents
     */
    @SuppressWarnings("unchecked")
    public static <T extends DataObject> InstanceIdentifier<T> create(final Class<T> type) {
        return (InstanceIdentifier<T>) create(Collections.<PathArgument> singletonList(new Item<>(type)));
    }

    public static class ConcreteInstanceIdentifier<T extends DataObject> extends InstanceIdentifier<T> {
        private ConcreteInstanceIdentifier(final Class<T> type, final Iterable<PathArgument> path, final int hashCode) {
            super(type, path, hashCode);
        }

        @Override
        public boolean isWildcarded() {
            return false;
        }
    }

    public static class WildcardInstanceIdentifier<T extends DataObject> extends InstanceIdentifier<T> {
        private WildcardInstanceIdentifier(final Class<T> type, final Iterable<PathArgument> path, final int hashCode) {
            super(type, path, hashCode);
        }

        @Override
        public boolean isWildcarded() {
            return true;
        }
    }

    public static abstract class KeyedInstanceIdentifier<T extends Identifiable<K> & DataObject, K extends Identifier<T>> extends InstanceIdentifier<T> {
        private final K key;

        private KeyedInstanceIdentifier(final Class<T> type, final Iterable<PathArgument> pathArguments, final int hashCode, final K key) {
            super(type, pathArguments, hashCode);
            this.key = key;
        }

        public final K getKey() {
            return key;
        }

        @Override
        public final InstanceIdentifierBuilder<T> builder() {
            return new BuilderImpl<T>(new IdentifiableItem<T, K>(getTargetType(), key), getPathArguments(), hashCode(), isWildcarded());
        }
    }

    public static final class KeyedConcreteInstanceIdentifier<T extends Identifiable<K> & DataObject, K extends Identifier<T>> extends KeyedInstanceIdentifier<T, K> {
        private KeyedConcreteInstanceIdentifier(final Class<T> type, final Iterable<PathArgument> path, final int hashCode, final K key) {
            super(type, path, hashCode, key);
        }

        @Override
        public boolean isWildcarded() {
            return false;
        }
    }

    public static final class KeyedWildcardInstanceIdentifier<T extends Identifiable<K> & DataObject, K extends Identifier<T>> extends KeyedInstanceIdentifier<T, K> {
        private KeyedWildcardInstanceIdentifier(final Class<T> type, final Iterable<PathArgument> path, final int hashCode, final K key) {
            super(type, path, hashCode, key);
        }

        @Override
        public boolean isWildcarded() {
            return true;
        }
    }

    /**
     * Create a new InstanceIdentifierBuilder given a base InstanceIdentifier
     *
     * @param basePath
     * @param <T>
     * @return
     *
     * @deprecated Use {@link #builder()} instead.
     */
    @Deprecated
    public static <T extends DataObject> InstanceIdentifierBuilder<T> builder(final InstanceIdentifier<T> base) {
        return base.builder();
    }

    /**
     * Create an InstanceIdentifierBuilder for a specific type of InstanceIdentifier as specified by container
     *
     * @param container
     * @param <T>
     * @return
     */
    public static <T extends ChildOf<? extends DataRoot>> InstanceIdentifierBuilder<T> builder(final Class<T> container) {
        return new BuilderImpl<T>().addNode(container);
    }

    /**
     * Create an InstanceIdentifierBuilder for a specific type of InstanceIdentifier which represents an IdentifiableItem
     *
     * @param listItem
     * @param listKey
     * @param <N>
     * @param <K>
     * @return
     */
    public static <N extends Identifiable<K> & ChildOf<? extends DataRoot>, K extends Identifier<N>> InstanceIdentifierBuilder<N> builder(
            final Class<N> listItem, final K listKey) {
        return new BuilderImpl<N>().addNode(listItem, listKey);
    }

    private static final class BuilderImpl<T extends DataObject> implements InstanceIdentifierBuilder<T> {
        private final ImmutableList.Builder<PathArgument> pathBuilder;
        private final HashCodeBuilder hashBuilder;
        private boolean wildcard = false;
        private PathArgument arg = null;

        public BuilderImpl() {
            this.pathBuilder = ImmutableList.builder();
            this.hashBuilder = new HashCodeBuilder();
        }

        private BuilderImpl(final PathArgument item, final Iterable<? extends PathArgument> pathArguments, final int hashCode, final boolean wildcard) {
            this.pathBuilder = ImmutableList.<PathArgument>builder().addAll(pathArguments);
            this.hashBuilder = new HashCodeBuilder(hashCode);
            this.wildcard = wildcard;
            this.arg = item;
        }

        @SuppressWarnings("unchecked")
        private <N extends DataObject> InstanceIdentifierBuilder<N> addNode(final Class<N> container) {
            arg = new Item<N>(container);
            hashBuilder.addArgument(arg);
            pathBuilder.add(arg);

            if (Identifiable.class.isAssignableFrom(container)) {
                wildcard = true;
            }

            return (InstanceIdentifierBuilder<N>) this;
        }

        @SuppressWarnings("unchecked")
        private <N extends DataObject & Identifiable<K> , K extends Identifier<N>> InstanceIdentifierBuilder<N> addNode(
                final Class<N> listItem, final K listKey) {
            arg = new IdentifiableItem<N, K>(listItem, listKey);
            hashBuilder.addArgument(arg);
            pathBuilder.add(arg);
            return (InstanceIdentifierBuilder<N>) this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public InstanceIdentifier<T> toInstance() {
            Preconditions.checkState(arg != null, "No path arguments present");
            return (InstanceIdentifier<T>) trustedCreate(arg, pathBuilder.build(), hashBuilder.toInstance(), wildcard);
        }

        @Override
        public InstanceIdentifier<T> build() {
            return toInstance();
        }

        @Override
        public <N extends ChildOf<? super T>> InstanceIdentifierBuilder<N> child(final Class<N> container) {
            return addNode(container);
        }

        @Override
        public <N extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<N>> InstanceIdentifierBuilder<N> child(
                final Class<N> listItem, final K listKey) {
            return addNode(listItem, listKey);
        }

        @Override
        public <N extends DataObject & Augmentation<? super T>> InstanceIdentifierBuilder<N> augmentation(
                final Class<N> container) {
            return addNode(container);
        }

        @Override
        public int hashCode() {
            return hashBuilder.toInstance();
        }
    }
}
