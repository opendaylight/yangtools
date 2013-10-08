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

/**
 * Uniquely identifies data location in the overall of data tree 
 * modeled by YANG.
 * 
 * 
 */
public final class InstanceIdentifier<T extends DataObject> implements Path<InstanceIdentifier<?>>,Immutable {

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

        T getKey() {
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

        <N extends DataObject> InstanceIdentifierBuilder<N> node(Class<N> container);

        <N extends Identifiable<K> & DataObject, K extends Identifier<N>> InstanceIdentifierBuilder<N> node(
                Class<N> listItem, K listKey);

        <N extends ChildOf<? super T>> InstanceIdentifierBuilder<N> child(Class<N> container);
        
        <N extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<N>> InstanceIdentifierBuilder<N> child(
                Class<N> listItem, K listKey);

    }

    @SuppressWarnings("rawtypes")
    public static InstanceIdentifierBuilder<?> builder() {
        return new BuilderImpl();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static InstanceIdentifierBuilder<?> builder(InstanceIdentifier<?> basePath) {
        return new BuilderImpl(basePath.path,basePath.targetType);
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

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public InstanceIdentifier<T> toInstance() {
            List<PathArgument> immutablePath = Collections.unmodifiableList(new ArrayList<PathArgument>(path));
            return new InstanceIdentifier(immutablePath, target);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <N extends DataObject> InstanceIdentifierBuilder<N> node(Class<N> container) {
            target = container;
            path.add(new Item<N>(container));
            return (InstanceIdentifierBuilder<N>) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <N extends DataObject & Identifiable<K> , K extends Identifier<N>> InstanceIdentifierBuilder<N> node(
                Class<N> listItem, K listKey) {
            target = listItem;
            path.add(new IdentifiableItem<N, K>(listItem, listKey));
            return (InstanceIdentifierBuilder<N>) this;
        }
        
        @Override
        public <N extends ChildOf<? super T>> InstanceIdentifierBuilder<N> child(Class<N> container) {
            return node(container);
        }
        
        @Override
        public <N extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<N>> InstanceIdentifierBuilder<N> child(
                Class<N> listItem, K listKey) {
            return node(listItem,listKey);
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
}
