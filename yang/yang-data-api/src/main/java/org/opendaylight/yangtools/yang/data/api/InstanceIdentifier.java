/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Path;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class InstanceIdentifier implements Path<InstanceIdentifier>, Immutable, Serializable {

    private static final long serialVersionUID = 8467409862384206193L;
    private final List<PathArgument> path;

    private transient String toStringCache = null;
    private transient Integer hashCodeCache = null;

    public List<PathArgument> getPath() {
        return path;
    }

    public InstanceIdentifier(final List<? extends PathArgument> path) {
        this.path = ImmutableList.copyOf(path);
    }

    private InstanceIdentifier(final NodeIdentifier nodeIdentifier) {
        this.path = ImmutableList.<PathArgument> of(nodeIdentifier);
    }

    @Override
    public int hashCode() {
        /*
         * The hashCodeCache is safe, since the object contract requires
         * immutability of the object and all objects referenced from this
         * object.
         *
         * Used lists, maps are immutable. Path Arguments (elements) are also
         * immutable, since the PathArgument contract requires immutability.
         *
         * The cache is thread-safe - if multiple computations occurs at the
         * same time, cache will be overwritten with same result.
         */
        if (hashCodeCache == null) {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((path == null) ? 0 : path.hashCode());
            hashCodeCache = result;
        }
        return hashCodeCache;
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
        final InstanceIdentifier other = (InstanceIdentifier) obj;
        if (this.hashCode() != obj.hashCode()) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
    }

    public InstanceIdentifier node(final QName name) {
        return node(new NodeIdentifier(name));
    }

    public InstanceIdentifier node(final PathArgument arg) {
        return new InstanceIdentifier(ImmutableList.<PathArgument>builder().addAll(path).add(arg).build());
    }

    /**
     * Get the relative path from an ancestor. This method attempts to perform the reverse
     * of concatenating a base (ancestor) and a path.
     *
     * @param ancestor Ancestor against which the relative path should be calculated
     * @return This object's relative path from parent, or Optional.absent() if the
     *         specified parent is not in fact an ancestor of this object.
     */
    public Optional<InstanceIdentifier> relativeTo(final InstanceIdentifier ancestor) {
        if (ancestor.contains(this)) {
            final int common = ancestor.path.size();
            return Optional.of(new InstanceIdentifier(path.subList(common, path.size())));
        } else {
            return Optional.absent();
        }
    }

    // Static factories & helpers

    public static InstanceIdentifier of(final QName name) {
        return new InstanceIdentifier(new NodeIdentifier(name));
    }

    static public InstanceIdentifierBuilder builder() {
        return new BuilderImpl();
    }

    static public InstanceIdentifierBuilder builder(final InstanceIdentifier origin) {
        return new BuilderImpl(origin.getPath());
    }

    public static InstanceIdentifierBuilder builder(final QName node) {
        return builder().node(node);
    }

    public interface PathArgument extends Immutable, Serializable {

        /**
         * If applicable returns uniqee QName of data node as defined in YANG
         * Schema.
         *
         * This method may return null, if the corresponding schema node, does
         * not have QName associated, such as in cases of augmentations.
         *
         * @return
         */
        QName getNodeType();

    }

    public interface InstanceIdentifierBuilder extends Builder<InstanceIdentifier> {
        InstanceIdentifierBuilder node(QName nodeType);

        InstanceIdentifierBuilder nodeWithKey(QName nodeType, Map<QName, Object> keyValues);

        InstanceIdentifierBuilder nodeWithKey(QName nodeType, QName key, Object value);

        @Deprecated
        InstanceIdentifier getIdentifier();

        InstanceIdentifier build();
    }

    /**
     * Simple path argument identifying a {@link ContainerNode} or {@link LeafNode} leaf
     * overal data tree.
     *
     */
    public static final class NodeIdentifier implements PathArgument {

        /**
         *
         */
        private static final long serialVersionUID = -2255888212390871347L;

        private final QName nodeType;

        public NodeIdentifier(final QName node) {
            this.nodeType = node;
        }

        @Override
        public QName getNodeType() {
            return nodeType;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((nodeType == null) ? 0 : nodeType.hashCode());
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
            final NodeIdentifier other = (NodeIdentifier) obj;
            if (nodeType == null) {
                if (other.nodeType != null) {
                    return false;
                }
            } else if (!nodeType.equals(other.nodeType)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return nodeType.toString();
        }
    }

    /**
     *
     * Composite path argument identifying a {@link MapEntryNode} leaf
     * overal data tree.
     *
     */
    public static final class NodeIdentifierWithPredicates implements PathArgument {

        /**
         *
         */
        private static final long serialVersionUID = -4787195606494761540L;

        private final QName nodeType;
        private final Map<QName, Object> keyValues;

        public NodeIdentifierWithPredicates(final QName node, final Map<QName, Object> keyValues) {
            this.nodeType = node;
            this.keyValues = ImmutableMap.copyOf(keyValues);
        }

        public NodeIdentifierWithPredicates(final QName node, final QName key, final Object value) {
            this.nodeType = node;
            this.keyValues = ImmutableMap.of(key, value);
        }

        @Override
        public QName getNodeType() {
            return nodeType;
        }

        public Map<QName, Object> getKeyValues() {
            return keyValues;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((keyValues == null) ? 0 : keyValues.hashCode());
            result = prime * result + ((nodeType == null) ? 0 : nodeType.hashCode());
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
            final NodeIdentifierWithPredicates other = (NodeIdentifierWithPredicates) obj;
            if (keyValues == null) {
                if (other.keyValues != null) {
                    return false;
                }
            } else if (!keyValues.equals(other.keyValues)) {
                return false;
            }
            if (nodeType == null) {
                if (other.nodeType != null) {
                    return false;
                }
            } else if (!nodeType.equals(other.nodeType)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return nodeType + "[" + keyValues + "]";
        }
    }

    /**
     * Simple path argument identifying a {@link LeafSetEntryNode} leaf
     * overal data tree.
     *
     */
    public static final class NodeWithValue implements PathArgument {

       /**
        *
        * Composite path argument identifying a {@link AugmentationNode} leaf
        * overal data tree.
        *
        */
        private static final long serialVersionUID = -3637456085341738431L;

        private final QName nodeType;
        private final Object value;

        public NodeWithValue(final QName node, final Object value) {
            this.nodeType = node;
            this.value = value;
        }

        @Override
        public QName getNodeType() {
            return nodeType;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            result = prime * result + ((nodeType == null) ? 0 : nodeType.hashCode());
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
            final NodeWithValue other = (NodeWithValue) obj;
            if (value == null) {
                if (other.value != null) {
                    return false;
                }
            } else if (!value.equals(other.value)) {
                return false;
            }
            if (nodeType == null) {
                if (other.nodeType != null) {
                    return false;
                }
            } else if (!nodeType.equals(other.nodeType)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return nodeType + "[" + value + "]";
        }

    }


    public static final class AugmentationIdentifier implements PathArgument {


        private static final long serialVersionUID = -8122335594681936939L;
        private final QName nodeType;
        private final ImmutableSet<QName> childNames;

        @Override
        public QName getNodeType() {
            return nodeType;
        }

        public AugmentationIdentifier(final QName nodeType, final Set<QName> childNames) {
            super();
            this.nodeType = nodeType;
            this.childNames = ImmutableSet.copyOf(childNames);
        }

        public Set<QName> getPossibleChildNames() {
            return childNames;
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("AugmentationIdentifier{");
            sb.append("childNames=").append(childNames);
            sb.append('}');
            return sb.toString();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof AugmentationIdentifier)) {
                return false;
            }

            final AugmentationIdentifier that = (AugmentationIdentifier) o;

            if (childNames != null ? !childNames.equals(that.childNames) : that.childNames != null) {
                return false;
            }
            if (nodeType != null ? !nodeType.equals(that.nodeType) : that.nodeType != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = nodeType != null ? nodeType.hashCode() : 0;
            result = 31 * result + (childNames != null ? childNames.hashCode() : 0);
            return result;
        }
    }

    private static class BuilderImpl implements InstanceIdentifierBuilder {

        private final ImmutableList.Builder<PathArgument> path;

        public BuilderImpl() {
            path = ImmutableList.<PathArgument> builder();
        }

        public BuilderImpl(final List<? extends PathArgument> prefix) {
            path = ImmutableList.<PathArgument> builder().addAll(prefix);
        }

        @Override
        public InstanceIdentifierBuilder node(final QName nodeType) {
            path.add(new NodeIdentifier(nodeType));
            return this;
        }

        @Override
        public InstanceIdentifierBuilder nodeWithKey(final QName nodeType, final QName key, final Object value) {
            path.add(new NodeIdentifierWithPredicates(nodeType, key, value));
            return this;
        }

        @Override
        public InstanceIdentifierBuilder nodeWithKey(final QName nodeType, final Map<QName, Object> keyValues) {
            path.add(new NodeIdentifierWithPredicates(nodeType, keyValues));
            return this;
        }

        @Override
        @Deprecated
        public InstanceIdentifier toInstance() {
            return build();
        }

        @Override
        public InstanceIdentifier build() {
            return new InstanceIdentifier(path.build());
        }

        @Override
        @Deprecated
        public InstanceIdentifier getIdentifier() {
            return build();
        }
    }

    @Override
    public boolean contains(final InstanceIdentifier other) {
        if (other == null) {
            throw new IllegalArgumentException("other should not be null");
        }
        final int localSize = this.path.size();
        final List<PathArgument> otherPath = other.getPath();
        if (localSize > other.path.size()) {
            return false;
        }
        for (int i = 0; i < localSize; i++) {
            if (!path.get(i).equals(otherPath.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        /*
         * The toStringCache is safe, since the object contract requires
         * immutability of the object and all objects referenced from this
         * object.
         *
         * Used lists, maps are immutable. Path Arguments (elements) are also
         * immutable, since the PathArgument contract requires immutability.
         *
         * The cache is thread-safe - if multiple computations occurs at the
         * same time, cache will be overwritten with same result.
         */
        if (toStringCache != null) {
            return toStringCache;
        }
        final StringBuilder builder = new StringBuilder();
        for (final PathArgument argument : path) {
            builder.append("/");
            builder.append(argument.toString());
        }
        toStringCache = builder.toString();
        return toStringCache;
    }
}
