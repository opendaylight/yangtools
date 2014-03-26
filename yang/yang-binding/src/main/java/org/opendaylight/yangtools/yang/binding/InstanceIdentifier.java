/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import java.util.Collections;
import java.util.List;

import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Path;

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
public final class InstanceIdentifier<T extends DataObject> implements Path<InstanceIdentifier<? extends DataObject>>,Immutable {

    private final List<PathArgument> path;
    private final Class<T> targetType;

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
    public InstanceIdentifier(Class<T> type) {
        this(Collections.<PathArgument> singletonList(new Item<>(type)), type);
    }

    /**
     * Create an instance identifier for a very specific object type.
     *
     * Example
     * <pre>
     *  List<PathArgument> path = Arrays.asList(new Item(Nodes.class))
     *  new InstanceIdentifier(path, Nodes.class);
     * </pre>
     *
     * @param path The path to a specific node in the data tree
     * @param type The type of the object which this instance identifier represents
     */
    public InstanceIdentifier(List<PathArgument> path, Class<T> type) {
        this.path = ImmutableList.copyOf(path);
        this.targetType = type;
    }

    /**
     *
     * @return A list of the elements of the path
     */
    public List<PathArgument> getPath() {
        return getPathArguments();
    }

    /**
     *
     * @return A list of the elements of the path
     */

    public List<PathArgument> getPathArguments() {
        return this.path;
    }

    /**
     *
     * @return The target type of this instance identifier
     */
    public Class<T> getTargetType() {
        return this.targetType;
    }

    @Override
    public String toString() {
        return "InstanceIdentifier [path=" + path + "]";
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
    public <T extends DataObject> InstanceIdentifier<T> firstIdentifierOf(final Class<T> type) {
        int i = 1;
        for (final PathArgument a : path) {
            if (type.equals(a.getType())) {
                return new InstanceIdentifier<>(path.subList(0, i), type);
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
    public <N extends Identifiable<K> & DataObject, K extends Identifier<N>> K firstKeyOf(final Class<N> listItem, final Class<K> listKey) {
        for (PathArgument i : path) {
            if (listItem.equals(i.getType())) {
                @SuppressWarnings("unchecked")
                final K ret = ((IdentifiableItem<N, K>)i).getKey();
                return ret;
            }
        }

        return null;
    }

    /**
     * Return the key associated with the last component of the specified identifier.
     *
     * @param id instance identifier
     * @return key associated with the last component
     */
    public static <N extends Identifiable<K> & DataObject, K extends Identifier<N>> K keyOf(final InstanceIdentifier<N> id) {
        @SuppressWarnings("unchecked")
        final K ret = ((IdentifiableItem<N, K>)Iterables.getLast(id.getPath())).getKey();
        return ret;
    }

    /**
     * Path argument of {@link InstanceIdentifier}.
     * <p>
     * Interface which implementations are used as path components of the
     * path in overall data tree.
     *
     */
    public interface PathArgument {

        Class<? extends DataObject> getType();
    }


    /**
     * An Item represents an object that probably is only one of it's kind. For example a Nodes object is only one of
     * a kind. In YANG terms this would probably represent a container.
     *
     * @param <T>
     */
    public static final class Item<T extends DataObject> implements PathArgument {
        private final Class<T> type;

        public Item(Class<T> type) {
            this.type = type;
        }

        @Override
        public Class<T> getType() {
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
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Item<?> other = (Item<?>) obj;
            if (type == null) {
                if (other.type != null)
                    return false;
            } else if (!type.equals(other.type))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return type.getName();
        }
    }

    /**
     * An IdentifiableItem represents a object that is usually present in a collection and can be identified uniquely
     * by a key. In YANG terms this would probably represent an item in a list.
     *
     * @param <I> An object that is identifiable by an identifier
     * @param <T> The identifier of the object
     */
    public static final class IdentifiableItem<I extends Identifiable<T> & DataObject, T extends Identifier<I>> implements
            PathArgument {

        private final T key;
        private final Class<I> type;

        public IdentifiableItem(Class<I> type, T key) {
            if (type == null)
                throw new IllegalArgumentException("Type must not be null.");
            if (key == null)
                throw new IllegalArgumentException("Key must not be null.");
            this.type = type;
            this.key = key;
        }

        public T getKey() {
            return this.key;
        }

        @Override
        public Class<I> getType() {
            return this.type;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj.hashCode() != hashCode()) {
                return false;
            }
            if (!(obj instanceof IdentifiableItem<?, ?>)) {
                return false;
            }
            IdentifiableItem<?, ?> foreign = (IdentifiableItem<?, ?>) obj;
            return key.equals(foreign.getKey());
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public String toString() {
            return type.getName() + "[key=" + key + "]";
        }
    }

    public interface InstanceIdentifierBuilder<T extends DataObject> extends Builder<InstanceIdentifier<T>> {
        /**
         * @deprecated use {@link child(Class)} or {@link augmentation(Class)} instead.
         */
        @Deprecated
        <N extends DataObject> InstanceIdentifierBuilder<N> node(Class<N> container);

        /**
         * @deprecated use {@link child(Class,Identifier)} or {@link augmentation(Class,Identifier)} instead.
         */
        @Deprecated
        <N extends Identifiable<K> & DataObject, K extends Identifier<N>> InstanceIdentifierBuilder<N> node(
                Class<N> listItem, K listKey);

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

    /**
     * @deprecated use {@link builder(Class)} or {@link builder(Class,Identifier)} instead.
     */
    @Deprecated
    @SuppressWarnings("rawtypes")
    public static InstanceIdentifierBuilder<?> builder() {
        return new BuilderImpl();
    }

    /**
     * Create an InstanceIdentifierBuilder for a specific type of InstanceIdentifier as specified by container
     *
     * @param container
     * @param <T>
     * @return
     */
    public static <T extends ChildOf<? extends DataRoot>> InstanceIdentifierBuilder<T> builder(Class<T> container) {
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
            Class<N> listItem, K listKey) {
        return new BuilderImpl<N>().addNode(listItem, listKey);
    }

    /**
     * Create a new InstanceIdentifierBuilder given a base InstanceIdentifier
     *
     * @param basePath
     * @param <T>
     * @return
     */
    public static <T extends DataObject> InstanceIdentifierBuilder<T> builder(InstanceIdentifier<T> basePath) {
        return new BuilderImpl<T>(basePath.path,basePath.targetType);
    }

    private static final class BuilderImpl<T extends DataObject> implements InstanceIdentifierBuilder<T> {

        private final ImmutableList.Builder<PathArgument> path;
        private Class<? extends DataObject> target = null;

        public BuilderImpl() {
            this.path = ImmutableList.builder();
        }

        public BuilderImpl(List<? extends PathArgument> prefix,Class<? extends DataObject> target) {
            this.path = ImmutableList.<PathArgument>builder().addAll(prefix);
            this.target = target;
        }

        @SuppressWarnings("unchecked")
        private <N extends DataObject> InstanceIdentifierBuilder<N> addNode(Class<N> container) {
            target = container;
            path.add(new Item<N>(container));
            return (InstanceIdentifierBuilder<N>) this;
        }

        @SuppressWarnings("unchecked")
        private <N extends DataObject & Identifiable<K> , K extends Identifier<N>> InstanceIdentifierBuilder<N> addNode(
                Class<N> listItem, K listKey) {
            target = listItem;
            path.add(new IdentifiableItem<N, K>(listItem, listKey));
            return (InstanceIdentifierBuilder<N>) this;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public InstanceIdentifier<T> toInstance() {
            return new InstanceIdentifier(path.build(), target);
        }

        @Override
        public InstanceIdentifier<T> build() {
            return toInstance();
        }

        @Override
        public <N extends DataObject> InstanceIdentifierBuilder<N> node(Class<N> container) {
            return addNode(container);
        }

        @Override
        public <N extends DataObject & Identifiable<K> , K extends Identifier<N>> InstanceIdentifierBuilder<N> node(
                Class<N> listItem, K listKey) {
            return addNode(listItem, listKey);
        }

        @Override
        public <N extends ChildOf<? super T>> InstanceIdentifierBuilder<N> child(Class<N> container) {
            return addNode(container);
        }

        @Override
        public <N extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<N>> InstanceIdentifierBuilder<N> child(
                Class<N> listItem, K listKey) {
            return addNode(listItem,listKey);
        }

        @Override
        public <N extends DataObject & Augmentation<? super T>> InstanceIdentifierBuilder<N> augmentation(
                Class<N> container) {
            return addNode(container);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((path == null) ? 0 : path.hashCode());
            return result;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        InstanceIdentifier<?> other = (InstanceIdentifier<?>) obj;
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
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
    public boolean contains(final InstanceIdentifier<?> other) {
        if(other == null) {
            throw new IllegalArgumentException("other should not be null");
        }
        final int localSize = this.path.size();
        final List<PathArgument> otherPath = other.getPath();
        if(localSize > other.path.size()) {
            return false;
        }
        for(int i = 0;i<localSize;i++ ) {
            if(!path.get(i).equals(otherPath.get(i))) {
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
    public boolean containsWildcarded(final InstanceIdentifier<?> other) {
        if(other == null) {
            throw new IllegalArgumentException("other should not be null");
        }
        final int localSize = this.path.size();
        final List<PathArgument> otherPath = other.getPath();
        if(localSize > other.path.size()) {
            return false;
        }
        for(int i = 0;i<localSize;i++ ) {
            final PathArgument localArgument = path.get(i);
            final PathArgument otherArgument = otherPath.get(i);
            if(!localArgument.getType().equals(otherArgument.getType())) {
                return false;
            }
            if(localArgument instanceof IdentifiableItem<?, ?> && otherArgument instanceof IdentifiableItem<?, ?> && !localArgument.equals(otherPath.get(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean isWildcarded() {
        for(PathArgument pathArgument : path) {
            if(Identifiable.class.isAssignableFrom(pathArgument.getType()) && !(pathArgument instanceof IdentifiableItem<?, ?>)) {
                return true;
            }
        }
        return false;
    }
}
