/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Path;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;

/**
 * Unique identifier of a partical node instance in the data tree.
 *
 *
 * <p>
 * Java representation of YANG Built-in type <code>instance-identifier</code>,
 * which conceptually is XPath expression minimised to uniquely identify element
 * in data tree which conforms to constraints maintained by YANG Model,
 * effectively this makes Instance Identifier a path to element in data tree.
 * <p>
 * Constraints put in YANG specification on instance-identifier allowed it to be
 * effectively represented in Java and it's evaluation does not require
 * full-blown XPath processor.
 * <p>
 * <h3>Path Arguments</h3>
 * Path to the node represented in instance identifier consists of
 * {@link PathArgument} which carries necessary information to uniquely identify
 * node on particular level in the subtree.
 * <p>
 * <ul>
 * <li>{@link NodeIdentifier} - Identifier of node, which has cardinality
 * <code>0..1</code> in particular subtree in data tree.</li>
 * <li>{@link NodeIdentifierWithPredicates} - Identifier of node (list item),
 * which has cardinality <code>0..n</code>.</li>
 * <li>{@link NodeWithValue} - Identifier of instance <code>leaf</code> node or
 * <code>leaf-list</code> node.</li>
 * <li>{@link AugmentationIdentifier} - Identifier of instance of
 * <code>augmentation</code> node.</li>
 * </ul>
 *
 *
 * @see http://tools.ietf.org/html/rfc6020#section-9.13
 *
 *
 */
public class InstanceIdentifier implements Path<InstanceIdentifier>, Immutable, Serializable {

    private static final long serialVersionUID = 8467409862384206193L;
    private final List<PathArgument> path;

    private transient String toStringCache = null;
    private transient Integer hashCodeCache = null;

    /**
     *
     * Returns a list of path arguments.
     *
     * @deprecated Use {@link #getPathArguments()} instead.
     * @return Immutable list of path arguments.
     */
    @Deprecated
    public List<PathArgument> getPath() {
        return path;
    }

    /**
     *
     * Returns a ordered iteration of path arguments.
     *
     * @return Immutable iteration of path arguments.
     */
    public Iterable<PathArgument> getPathArguments() {
        return path;
    }

    /**
     *
     *
     * @deprecated Use {@link #create(Iterable)} instead.
     * @param path
     */
    @Deprecated
    public InstanceIdentifier(final List<? extends PathArgument> path) {
        this.path = ImmutableList.copyOf(path);
    }

    private InstanceIdentifier(final Iterable<? extends PathArgument> path) {
        Preconditions.checkNotNull(path, "path must not be null.");
        this.path = ImmutableList.copyOf(path);
    }

    private InstanceIdentifier(final NodeIdentifier nodeIdentifier) {
        this.path = ImmutableList.<PathArgument> of(nodeIdentifier);
    }

    public static final InstanceIdentifier create(final Iterable<? extends PathArgument> path) {
        return new InstanceIdentifier(path);
    }

    @Override
    public int hashCode() {
        /*
         * The hashCodeCache is safe, since the object contract requires
         * immutability of the object and all objects referenced from this
         * object.
         * Used lists, maps are immutable. Path Arguments (elements) are also
         * immutable, since the PathArgument contract requires immutability.
         * The cache is thread-safe - if multiple computations occurs at the
         * same time, cache will be overwritten with same result.
         */
        if (hashCodeCache == null) {
            final int prime = 31;
            int result = 1;
            result = prime * result + path.hashCode();
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
        InstanceIdentifier other = (InstanceIdentifier) obj;
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

    /**
     *
     * Constructs a new Instance Identifier with new {@link NodeIdentifier} added to the end of path arguments
     *
     * @param name QName of {@link NodeIdentifier}
     * @return Instance Identifier with additional path argument added to the end.
     */
    public InstanceIdentifier node(final QName name) {
        return node(new NodeIdentifier(name));
    }

    /**
     *
     * Constructs a new Instance Identifier with new {@link PathArgument} added to the end of path arguments
     *
     * @param arg Path argument which should be added to the end
     * @return Instance Identifier with additional path argument added to the end.
     */
    public InstanceIdentifier node(final PathArgument arg) {
        return create(ImmutableList.<PathArgument> builder().addAll(path).add(arg).build());
    }

    /**
     * Get the relative path from an ancestor. This method attempts to perform
     * the reverse
     * of concatenating a base (ancestor) and a path.
     *
     * @param ancestor
     *            Ancestor against which the relative path should be calculated
     * @return This object's relative path from parent, or Optional.absent() if
     *         the
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

    static int hashCode(final Object value) {
        if (value == null) {
            return 0;
        }

        if (value.getClass().equals(byte[].class)) {
            return Arrays.hashCode((byte[]) value);
        }

        if (value.getClass().isArray()) {
            int hash = 0;
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                hash += Objects.hashCode(Array.get(value, i));
            }

            return hash;
        }

        return Objects.hashCode(value);
    }

    // Static factories & helpers

    /**
     *
     * Returns a new InstanceIdentifier with only one path argument of type {@link NodeIdentifier} with supplied QName
     *
     * @param name QName of first node identifier
     * @return Instance Identifier with only one path argument of type {@link NodeIdentifier}
     */
    public static InstanceIdentifier of(final QName name) {
        return new InstanceIdentifier(new NodeIdentifier(name));
    }

    /**
     *
     * Returns new builder for InstanceIdentifier with empty path arguments.
     *
     * @return new builder for InstanceIdentifier with empty path arguments.
     */
    static public InstanceIdentifierBuilder builder() {
        return new BuilderImpl();
    }

    /**
    *
    * Returns new builder for InstanceIdentifier with path arguments copied from original instance identifier.
    *
    * @param origin Instace Identifier from which path arguments are copied.
    * @return new builder for InstanceIdentifier with path arguments copied from original instance identifier.
    */
    static public InstanceIdentifierBuilder builder(final InstanceIdentifier origin) {
        return new BuilderImpl(origin.getPath());
    }
   /**
    *
    * Returns new builder for InstanceIdentifier with first path argument set to {@link NodeIdentifier}.
    *
    * @param node QName of first {@link NodeIdentifier} path argument.
    * @return  new builder for InstanceIdentifier with first path argument set to {@link NodeIdentifier}.
    */
    public static InstanceIdentifierBuilder builder(final QName node) {
        return builder().node(node);
    }

    /**
     *
     * Path argument / component of InstanceIdentifier
     *
     * Path argument uniquelly identifies node in data tree on particular
     * level.
     * <p>
     * This interface itself is used as common parent for actual
     * path arguments types and should not be implemented by user code.
     * <p>
     * Path arguments SHOULD contain only minimum of information
     * required to uniquely identify node on particular subtree level.
     *
     * For actual path arguments types see:
     * <ul>
     * <li>{@link NodeIdentifier} - Identifier of container or leaf
     * <li>{@link NodeIdentifierWithPredicates} - Identifier of list entries, which have key defined
     * <li>{@link AugmentationIdentifier} - Identifier of augmentation
     * <li>{@link NodeWithValue} - Identifier of leaf-list entry
     * </ul>
     *
     *
     *
     */
    public interface PathArgument extends Comparable<PathArgument>, Immutable, Serializable {

        /**
         * If applicable returns unique QName of data node as defined in YANG
         * Schema.
         *
         * This method may return null, if the corresponding schema node, does
         * not have QName associated, such as in cases of augmentations.
         *
         * @return
         */
        QName getNodeType();

    }

    private static abstract class AbstractPathArgument implements PathArgument {
        private static final long serialVersionUID = -4546547994250849340L;
        protected final QName nodeType;

        protected AbstractPathArgument(final QName nodeType) {
            this.nodeType = Preconditions.checkNotNull(nodeType);
        }

        @Override
        public QName getNodeType() {
            return nodeType;
        }

        @Override
        public int compareTo(final PathArgument o) {
            return nodeType.compareTo(o.getNodeType());
        }

    }

    /**
     *
     * Fluent Builder of Instance Identifier instances
     *
     * @
     *
     */
    public interface InstanceIdentifierBuilder extends Builder<InstanceIdentifier> {

        /**
         *
         * Adds {@link NodeIdentifier} with supplied QName to path arguments of resulting instance identifier.
         *
         * @param nodeType QName of {@link NodeIdentifier} which will be added
         * @return this builder
         */
        InstanceIdentifierBuilder node(QName nodeType);

        /**
         *
         * Adds {@link NodeIdentifierWithPredicates} with supplied QName and key values to path arguments of resulting instance identifier.
         *
         * @param nodeType QName of {@link NodeIdentifierWithPredicates} which will be added
         * @param keyValues Map of key components and their respective values for {@link NodeIdentifierWithPredicates}
         * @return this builder
         */
        InstanceIdentifierBuilder nodeWithKey(QName nodeType, Map<QName, Object> keyValues);

        /**
         *
         * Adds {@link NodeIdentifierWithPredicates} with supplied QName and key, value.
         *
         * @param nodeType QName of {@link NodeIdentifierWithPredicates} which will be added
         * @param key QName of key which will be added
         * @param value value of key which will be added
         * @return this builder
         */
        InstanceIdentifierBuilder nodeWithKey(QName nodeType, QName key, Object value);

        /**
         *
         * @return
         * @deprecated use {@link #build()}
         *
         */
        @Deprecated
        InstanceIdentifier getIdentifier();

        /**
         *
         * Builds an {@link InstanceIdentifier} with path arguments from this builder
         *
         * @return {@link InstanceIdentifier}
         */
        InstanceIdentifier build();
    }

    /**
     * Simple path argument identifying a {@link org.opendaylight.yangtools.yang.data.api.schema.ContainerNode} or
     * {@link org.opendaylight.yangtools.yang.data.api.schema.LeafNode} leaf in particular subtree.
     */
    public static final class NodeIdentifier extends AbstractPathArgument {
        private static final long serialVersionUID = -2255888212390871347L;

        public NodeIdentifier(final QName node) {
            super(node);
        }

        @Override
        public int hashCode() {
            return 31 + nodeType.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof NodeIdentifier)) {
                return false;
            }
            final NodeIdentifier other = (NodeIdentifier) obj;
            return nodeType.equals(other.nodeType);
        }

        @Override
        public String toString() {
            return nodeType.toString();
        }

    }

    /**
     * Composite path argument identifying a {@link org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode} leaf
     * overall data tree.
     */
    public static final class NodeIdentifierWithPredicates extends AbstractPathArgument {
        private static final long serialVersionUID = -4787195606494761540L;

        private final Map<QName, Object> keyValues;

        public NodeIdentifierWithPredicates(final QName node, final Map<QName, Object> keyValues) {
            super(node);
            this.keyValues = ImmutableMap.copyOf(keyValues);
        }

        public NodeIdentifierWithPredicates(final QName node, final QName key, final Object value) {
            this(node, ImmutableMap.of(key, value));
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
            result = prime * result + ((keyValues == null) ? 0 : hashKeyValues());
            result = prime * result + ((nodeType == null) ? 0 : nodeType.hashCode());
            return result;
        }

        private int hashKeyValues() {
            int hash = 0;
            for (Entry<QName, Object> entry : keyValues.entrySet()) {
                hash += Objects.hashCode(entry.getKey()) + InstanceIdentifier.hashCode(entry.getValue());
            }

            return hash;
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
            NodeIdentifierWithPredicates other = (NodeIdentifierWithPredicates) obj;
            if (keyValues == null) {
                if (other.keyValues != null) {
                    return false;
                }
            } else if (!keyValuesEquals(other.keyValues)) {
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

        private boolean keyValuesEquals(final Map<QName, Object> otherKeyValues) {
            if (otherKeyValues == null || keyValues.size() != otherKeyValues.size()) {
                return false;
            }

            boolean result = true;
            for (Entry<QName, Object> entry : keyValues.entrySet()) {
                if (!otherKeyValues.containsKey(entry.getKey())
                        || !Objects.deepEquals(entry.getValue(), otherKeyValues.get(entry.getKey()))) {

                    result = false;
                    break;
                }
            }

            return result;
        }

        @Override
        public String toString() {
            return nodeType + "[" + keyValues + "]";
        }
    }

    /**
     * Simple path argument identifying a {@link LeafSetEntryNode} leaf
     * overall data tree.
     */
    public static final class NodeWithValue extends AbstractPathArgument {
        private static final long serialVersionUID = -3637456085341738431L;

        private final Object value;

        public NodeWithValue(final QName node, final Object value) {
            super(node);
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
            result = prime * result + ((value == null) ? 0 : InstanceIdentifier.hashCode(value));
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
            NodeWithValue other = (NodeWithValue) obj;
            return Objects.deepEquals(value, other.value) && Objects.equals(nodeType, other.nodeType);
        }

        @Override
        public String toString() {
            return nodeType + "[" + value + "]";
        }

    }

    /**
     * Composite path argument identifying a {@link org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode} node in
     * particular subtree.
     *
     * Augmentation is uniquely identified by set of all possible child nodes.
     * This is possible
     * to identify instance of augmentation,
     * since RFC6020 states that <code>augment</code> that augment
     * statement must not add multiple nodes from same namespace
     * / module to the target node.
     *
     *
     * @see http://tools.ietf.org/html/rfc6020#section-7.15
     */
    public static final class AugmentationIdentifier implements PathArgument {
        private static final long serialVersionUID = -8122335594681936939L;
        private final ImmutableSet<QName> childNames;

        @Override
        public QName getNodeType() {
            // This should rather throw exception than return always null
            throw new UnsupportedOperationException("Augmentation node has no QName");
        }

        /**
         *
         * Construct new augmentation identifier using supplied set of possible
         * child nodes
         *
         * @param childNames
         *            Set of possible child nodes.
         */
        public AugmentationIdentifier(final Set<QName> childNames) {
            this.childNames = ImmutableSet.copyOf(childNames);
        }

        /**
         * Augmentation node has no QName
         *
         * @deprecated Use
         *             {@link AugmentationIdentifier#AugmentationIdentifier(Set)}
         *             instead.
         */
        @Deprecated
        public AugmentationIdentifier(final QName nodeType, final Set<QName> childNames) {
            this(childNames);
        }

        /**
         *
         * Returns set of all possible child nodes
         *
         * @return set of all possible child nodes.
         */
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

            AugmentationIdentifier that = (AugmentationIdentifier) o;

            if (!childNames.equals(that.childNames)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return childNames.hashCode();
        }

        @Override
        public int compareTo(PathArgument o) {
            if (!(o instanceof AugmentationIdentifier)) {
                return -1;
            }
            AugmentationIdentifier other = (AugmentationIdentifier) o;
            Set<QName> otherChildNames = other.getPossibleChildNames();
            int thisSize = childNames.size();
            int otherSize = otherChildNames.size();
            if (thisSize == otherSize) {
                Iterator<QName> otherIterator = otherChildNames.iterator();
                for (QName name : childNames) {
                    int c = name.compareTo(otherIterator.next());
                    if (c != 0) {
                        return c;
                    }
                }
                return 0;
            } else if (thisSize < otherSize) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    private static class BuilderImpl implements InstanceIdentifierBuilder {

        private final ImmutableList.Builder<PathArgument> path;

        public BuilderImpl() {
            path = ImmutableList.<PathArgument> builder();
        }

        public BuilderImpl(final List<? extends PathArgument> prefix) {
            path = ImmutableList.<PathArgument> builder();
            path.addAll(prefix);
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
         * Used lists, maps are immutable. Path Arguments (elements) are also
         * immutable, since the PathArgument contract requires immutability.
         * The cache is thread-safe - if multiple computations occurs at the
         * same time, cache will be overwritten with same result.
         */
        if (toStringCache != null) {
            return toStringCache;
        }

        final StringBuilder builder = new StringBuilder('/');
        boolean first = true;
        for (PathArgument argument : path) {
            if (first) {
                first = false;
            } else {
                builder.append('/');
            }
            builder.append(argument.toString());
        }

        toStringCache = builder.toString();
        return toStringCache;
    }
}
