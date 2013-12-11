/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Path;

import com.google.common.collect.Iterables;

/**
 * Uniquely identifies data location in the overall of data tree 
 * modeled by YANG.
 * 
 * 
 */
public final class InstanceIdentifier<T extends DataObject> implements Path<InstanceIdentifier<? extends DataObject>>,Immutable {

    private final List<PathArgument> path;
    private final Class<T> targetType;

    public InstanceIdentifier(Class<T> type) {
        path = Collections.<PathArgument> singletonList(new Item<>(type));
        this.targetType = type;
    }

    public InstanceIdentifier(List<PathArgument> path, Class<T> type) {
        this.path = Collections.<PathArgument> unmodifiableList(new ArrayList<>(path));
        this.targetType = type;
    }

    /**
     * 
     * @return path
     */
    public List<PathArgument> getPath() {
        return getPathArguments();
    }
    
    public List<PathArgument> getPathArguments() {
        return this.path;
    }

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
     * @param type component type
     * @return trimmed instance identifier, or null if the component type
     *         is not present.
     */
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

    public static final class Item<T extends DataObject> implements PathArgument {
        private final Class<T> type;

        public Item(Class<T> type) {
            this.type = type;
        }

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

        <N extends ChildOf<? super T>> InstanceIdentifierBuilder<N> child(Class<N> container);

        <N extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<N>> InstanceIdentifierBuilder<N> child(
                Class<N> listItem, K listKey);

        <N extends DataObject & Augmentation<? super T>> InstanceIdentifierBuilder<N> augmentation(Class<N> container);
        
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

    public static <T extends ChildOf<? extends DataRoot>> InstanceIdentifierBuilder<T> builder(Class<T> container) {
        return new BuilderImpl<T>().addNode(container);
    }

    public static <N extends Identifiable<K> & ChildOf<? extends DataRoot>, K extends Identifier<N>> InstanceIdentifierBuilder<N> builder(
            Class<N> listItem, K listKey) {
        return new BuilderImpl<N>().addNode(listItem, listKey);
    }

    public static <T extends DataObject> InstanceIdentifierBuilder<T> builder(InstanceIdentifier<T> basePath) {
        return new BuilderImpl<T>(basePath.path,basePath.targetType);
    }

    private static final class BuilderImpl<T extends DataObject> implements InstanceIdentifierBuilder<T> {

        private List<PathArgument> path;
        private Class<? extends DataObject> target = null;

        public BuilderImpl() {
            this.path = new ArrayList<>();
        }

        public BuilderImpl(List<? extends PathArgument> prefix,Class<? extends DataObject> target) {
            this.path = new ArrayList<>(prefix);
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
            List<PathArgument> immutablePath = Collections.unmodifiableList(new ArrayList<PathArgument>(path));
            return new InstanceIdentifier(immutablePath, target);
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
            if(!localArgument.getType().equals(otherPath.get(i).getType())) {
                return false;
            } 
            if(localArgument instanceof IdentifiableItem<?, ?> && !localArgument.equals(otherPath.get(i))) {
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
