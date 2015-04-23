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
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Path;
import org.opendaylight.yangtools.util.HashCodeBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;

/**
 * Unique identifier of a particular node instance in the data tree.
 *
 * <p>
 * Java representation of YANG Built-in type <code>instance-identifier</code>,
 * which conceptually is XPath expression minimized to uniquely identify element
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
 * @see <a href="http://tools.ietf.org/html/rfc6020#section-9.13">RFC6020</a>
 */
public final class YangInstanceIdentifier implements Path<YangInstanceIdentifier>, Immutable, Serializable {
    /**
     * An empty {@link YangInstanceIdentifier}. It corresponds to the path of the conceptual
     * root of the YANG namespace.
     */
    public static final YangInstanceIdentifier EMPTY = trustedCreate(Collections.<PathArgument>emptyList());
    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<YangInstanceIdentifier, ImmutableList> LEGACYPATH_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(YangInstanceIdentifier.class, ImmutableList.class, "legacyPath");
    private static final AtomicReferenceFieldUpdater<YangInstanceIdentifier, String> TOSTRINGCACHE_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(YangInstanceIdentifier.class, String.class, "toStringCache");
    private static final Field PATHARGUMENTS_FIELD;

    private static final long serialVersionUID = 3L;
    private final transient Iterable<PathArgument> pathArguments;
    private final int hash;

    private volatile ImmutableList<PathArgument> legacyPath = null;
    private transient volatile String toStringCache = null;

    static {
        final Field f;
        try {
            f = YangInstanceIdentifier.class.getDeclaredField("pathArguments");
        } catch (NoSuchFieldException | SecurityException e) {
            throw new ExceptionInInitializerError(e);
        }
        f.setAccessible(true);

        PATHARGUMENTS_FIELD = f;
    }

    private ImmutableList<PathArgument> getLegacyPath() {
        // Temporary variable saves a volatile read
        ImmutableList<PathArgument> ret = legacyPath;
        if (ret == null) {
            // We could have used a synchronized block, but the window is quite
            // small and worst that can happen is duplicate object construction.
            ret = ImmutableList.copyOf(pathArguments);
            LEGACYPATH_UPDATER.lazySet(this, ret);
        }

        return ret;
    }

    /**
     * Returns a list of path arguments.
     *
     * @deprecated Use {@link #getPathArguments()} instead.
     * @return Immutable list of path arguments.
     */
    @Deprecated
    public List<PathArgument> getPath() {
        return getLegacyPath();
    }

    /**
     * Returns an ordered iteration of path arguments.
     *
     * @return Immutable iteration of path arguments.
     */
    public Iterable<PathArgument> getPathArguments() {
        return pathArguments;
    }

    /**
     * Returns an iterable of path arguments in reverse order. This is useful
     * when walking up a tree organized this way.
     *
     * @return Immutable iterable of path arguments in reverse order.
     */
    public Iterable<PathArgument> getReversePathArguments() {
        return getLegacyPath().reverse();
    }

    /**
     * Returns the last PathArgument. This is equivalent of iterating
     * to the last element of the iterable returned by {@link #getPathArguments()}.
     *
     * @return The last past argument, or null if there are no PathArguments.
     */
    public PathArgument getLastPathArgument() {
        return Iterables.getFirst(getReversePathArguments(), null);
    }

    private YangInstanceIdentifier(final Iterable<PathArgument> path, final int hash) {
        this.pathArguments = Preconditions.checkNotNull(path, "path must not be null.");
        this.hash = hash;
    }

    private static YangInstanceIdentifier trustedCreate(final Iterable<PathArgument> path) {
        final HashCodeBuilder<PathArgument> hash = new HashCodeBuilder<>();
        for (PathArgument a : path) {
            hash.addArgument(a);
        }

        return new YangInstanceIdentifier(path, hash.build());
    }

    public static YangInstanceIdentifier create(final Iterable<? extends PathArgument> path) {
        if (Iterables.isEmpty(path)) {
            return EMPTY;
        }

        return trustedCreate(ImmutableList.copyOf(path));
    }

    public static YangInstanceIdentifier create(final PathArgument... path) {
        // We are forcing a copy, since we cannot trust the user
        return create(Arrays.asList(path));
    }

    @Override
    public int hashCode() {
        /*
         * The caching is safe, since the object contract requires
         * immutability of the object and all objects referenced from this
         * object.
         * Used lists, maps are immutable. Path Arguments (elements) are also
         * immutable, since the PathArgument contract requires immutability.
         */
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
        YangInstanceIdentifier other = (YangInstanceIdentifier) obj;
        if (this.hashCode() != obj.hashCode()) {
            return false;
        }
        return Iterables.elementsEqual(pathArguments, other.pathArguments);
    }

    /**
     * Constructs a new Instance Identifier with new {@link NodeIdentifier} added to the end of path arguments
     *
     * @param name QName of {@link NodeIdentifier}
     * @return Instance Identifier with additional path argument added to the end.
     */
    public YangInstanceIdentifier node(final QName name) {
        return node(new NodeIdentifier(name));
    }

    /**
     *
     * Constructs a new Instance Identifier with new {@link PathArgument} added to the end of path arguments
     *
     * @param arg Path argument which should be added to the end
     * @return Instance Identifier with additional path argument added to the end.
     */
    public YangInstanceIdentifier node(final PathArgument arg) {
        return new YangInstanceIdentifier(Iterables.concat(pathArguments, Collections.singleton(arg)), HashCodeBuilder.nextHashCode(hash, arg));
    }

    /**
     * Get the relative path from an ancestor. This method attempts to perform
     * the reverse of concatenating a base (ancestor) and a path.
     *
     * @param ancestor
     *            Ancestor against which the relative path should be calculated
     * @return This object's relative path from parent, or Optional.absent() if
     *         the specified parent is not in fact an ancestor of this object.
     */
    public Optional<YangInstanceIdentifier> relativeTo(final YangInstanceIdentifier ancestor) {
        final Iterator<?> lit = pathArguments.iterator();
        final Iterator<?> oit = ancestor.pathArguments.iterator();
        int common = 0;

        while (oit.hasNext()) {
            // Ancestor is not really an ancestor
            if (!lit.hasNext() || !lit.next().equals(oit.next())) {
                return Optional.absent();
            }

            ++common;
        }

        if (common == 0) {
            return Optional.of(this);
        }
        if (!lit.hasNext()) {
            return Optional.of(EMPTY);
        }
        return Optional.of(trustedCreate(Iterables.skip(pathArguments, common)));
    }

    private static int hashCode(final Object value) {
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
    public static YangInstanceIdentifier of(final QName name) {
        return create(new NodeIdentifier(name));
    }

    /**
     *
     * Returns new builder for InstanceIdentifier with empty path arguments.
     *
     * @return new builder for InstanceIdentifier with empty path arguments.
     */
    public static InstanceIdentifierBuilder builder() {
        return new BuilderImpl();
    }

    /**
     *
     * Returns new builder for InstanceIdentifier with path arguments copied from original instance identifier.
     *
     * @param origin Instace Identifier from which path arguments are copied.
     * @return new builder for InstanceIdentifier with path arguments copied from original instance identifier.
     */
    public static InstanceIdentifierBuilder builder(final YangInstanceIdentifier origin) {
        return new BuilderImpl(origin.getPathArguments(), origin.hashCode());
    }

    /**
     * Path argument / component of InstanceIdentifier
     *
     * Path argument uniquely identifies node in data tree on particular
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
     */
    public interface PathArgument extends Comparable<PathArgument>, Immutable, Serializable {
        /**
         * If applicable returns unique QName of data node as defined in YANG
         * Schema.
         *
         * This method may return null, if the corresponding schema node, does
         * not have QName associated, such as in cases of augmentations.
         *
         * @return Node type
         */
        QName getNodeType();

        /**
         * Return the string representation of this object for use in context
         * provided by a previous object. This method can be implemented in
         * terms of {@link #toString()}, but implementations are encourage to
         * reuse any context already emitted by the previous object.
         *
         * @param previous Previous path argument
         * @return String representation
         */
        String toRelativeString(PathArgument previous);
    }

    private static abstract class AbstractPathArgument implements PathArgument {
        private static final AtomicReferenceFieldUpdater<AbstractPathArgument, Integer> HASH_UPDATER =
                AtomicReferenceFieldUpdater.newUpdater(AbstractPathArgument.class, Integer.class, "hash");
        private static final long serialVersionUID = -4546547994250849340L;
        private final QName nodeType;
        private volatile transient Integer hash = null;

        protected AbstractPathArgument(final QName nodeType) {
            this.nodeType = Preconditions.checkNotNull(nodeType);
        }

        @Override
        public final QName getNodeType() {
            return nodeType;
        }

        @Override
        public int compareTo(final PathArgument o) {
            return nodeType.compareTo(o.getNodeType());
        }

        protected int hashCodeImpl() {
            return 31 + getNodeType().hashCode();
        }

        @Override
        public final int hashCode() {
            Integer ret = hash;
            if (ret == null) {
                ret = hashCodeImpl();
                HASH_UPDATER.lazySet(this, ret);
            }

            return ret;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }

            return getNodeType().equals(((AbstractPathArgument)obj).getNodeType());
        }

        @Override
        public String toString() {
            return getNodeType().toString();
        }

        @Override
        public String toRelativeString(final PathArgument previous) {
            if (previous instanceof AbstractPathArgument) {
                final QNameModule mod = ((AbstractPathArgument)previous).getNodeType().getModule();
                if (getNodeType().getModule().equals(mod)) {
                    return getNodeType().getLocalName();
                }
            }

            return getNodeType().toString();
        }
    }

    /**
     * Fluent Builder of Instance Identifier instances
     */
    public interface InstanceIdentifierBuilder extends Builder<YangInstanceIdentifier> {
        /**
         * Adds {@link NodeIdentifier} with supplied QName to path arguments of resulting instance identifier.
         *
         * @param nodeType QName of {@link NodeIdentifier} which will be added
         * @return this builder
         */
        InstanceIdentifierBuilder node(QName nodeType);

        /**
         * Adds {@link NodeIdentifierWithPredicates} with supplied QName and key values to path arguments of resulting instance identifier.
         *
         * @param nodeType QName of {@link NodeIdentifierWithPredicates} which will be added
         * @param keyValues Map of key components and their respective values for {@link NodeIdentifierWithPredicates}
         * @return this builder
         */
        InstanceIdentifierBuilder nodeWithKey(QName nodeType, Map<QName, Object> keyValues);

        /**
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
         * Builds an {@link YangInstanceIdentifier} with path arguments from this builder
         *
         * @return {@link YangInstanceIdentifier}
         */
        @Override
        YangInstanceIdentifier build();

        /*
         * @deprecated use #build()
         */
        @Deprecated
        YangInstanceIdentifier toInstance();
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

        public Map<QName, Object> getKeyValues() {
            return keyValues;
        }

        @Override
        protected int hashCodeImpl() {
            final int prime = 31;
            int result = super.hashCodeImpl();
            result = prime * result;

            for (Entry<QName, Object> entry : keyValues.entrySet()) {
                result += Objects.hashCode(entry.getKey()) + YangInstanceIdentifier.hashCode(entry.getValue());
            }
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (!super.equals(obj)) {
                return false;
            }

            final Map<QName, Object> otherKeyValues = ((NodeIdentifierWithPredicates) obj).keyValues;
            if (keyValues.size() != otherKeyValues.size()) {
                return false;
            }

            for (Entry<QName, Object> entry : keyValues.entrySet()) {
                if (!otherKeyValues.containsKey(entry.getKey())
                        || !Objects.deepEquals(entry.getValue(), otherKeyValues.get(entry.getKey()))) {

                    return false;
                }
            }

            return true;
        }

        @Override
        public String toString() {
            return super.toString() + '[' + keyValues + ']';
        }

        @Override
        public String toRelativeString(final PathArgument previous) {
            return super.toRelativeString(previous) + '[' + keyValues + ']';
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

        public Object getValue() {
            return value;
        }

        @Override
        protected int hashCodeImpl() {
            final int prime = 31;
            int result = super.hashCodeImpl();
            result = prime * result + ((value == null) ? 0 : YangInstanceIdentifier.hashCode(value));
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            final NodeWithValue other = (NodeWithValue) obj;
            return Objects.deepEquals(value, other.value);
        }

        @Override
        public String toString() {
            return super.toString() + '[' + value + ']';
        }

        @Override
        public String toRelativeString(final PathArgument previous) {
            return super.toRelativeString(previous) + '[' + value + ']';
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
     * @see <a href="http://tools.ietf.org/html/rfc6020#section-7.15">RFC6020</a>
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
         * Returns set of all possible child nodes
         *
         * @return set of all possible child nodes.
         */
        public Set<QName> getPossibleChildNames() {
            return childNames;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("AugmentationIdentifier{");
            sb.append("childNames=").append(childNames).append('}');
            return sb.toString();
        }

        @Override
        public String toRelativeString(final PathArgument previous) {
            return toString();
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
            return childNames.equals(that.childNames);
        }

        @Override
        public int hashCode() {
            return childNames.hashCode();
        }

        @Override
        public int compareTo(final PathArgument o) {
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
        private final HashCodeBuilder<PathArgument> hash;
        private final List<PathArgument> path;

        public BuilderImpl() {
            this.hash = new HashCodeBuilder<>();
            this.path = new ArrayList<>();
        }

        public BuilderImpl(final Iterable<PathArgument> prefix, final int hash) {
            this.path = Lists.newArrayList(prefix);
            this.hash = new HashCodeBuilder<>(hash);
        }

        @Override
        public InstanceIdentifierBuilder node(final QName nodeType) {
            final PathArgument arg = new NodeIdentifier(nodeType);
            path.add(arg);
            hash.addArgument(arg);
            return this;
        }

        @Override
        public InstanceIdentifierBuilder nodeWithKey(final QName nodeType, final QName key, final Object value) {
            final PathArgument arg = new NodeIdentifierWithPredicates(nodeType, key, value);
            path.add(arg);
            hash.addArgument(arg);
            return this;
        }

        @Override
        public InstanceIdentifierBuilder nodeWithKey(final QName nodeType, final Map<QName, Object> keyValues) {
            final PathArgument arg = new NodeIdentifierWithPredicates(nodeType, keyValues);
            path.add(arg);
            hash.addArgument(arg);
            return this;
        }

        @Override
        @Deprecated
        public YangInstanceIdentifier toInstance() {
            return build();
        }

        @Override
        public YangInstanceIdentifier build() {
            return new YangInstanceIdentifier(ImmutableList.copyOf(path), hash.build());
        }
    }

    @Override
    public boolean contains(final YangInstanceIdentifier other) {
        Preconditions.checkArgument(other != null, "other should not be null");

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
        String ret = toStringCache;
        if (ret == null) {
            final StringBuilder builder = new StringBuilder("/");
            PathArgument prev = null;
            for (PathArgument argument : getPathArguments()) {
                if (prev != null) {
                    builder.append('/');
                }
                builder.append(argument.toRelativeString(prev));
                prev = argument;
            }

            ret = builder.toString();
            TOSTRINGCACHE_UPDATER.lazySet(this, ret);
        }
        return ret;
    }

    private void readObject(final ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        inputStream.defaultReadObject();

        try {
            PATHARGUMENTS_FIELD.set(this, legacyPath);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IOException(e);
        }
    }

    private Object readResolve() throws ObjectStreamException {
        return legacyPath.isEmpty() ? EMPTY : this;
    }

    private void writeObject(final ObjectOutputStream outputStream) throws IOException {
        /*
         * This may look strange, but what we are doing here is side-stepping the fact
         * that pathArguments is not generally serializable. We are forcing instantiation
         * of the legacy path, which is an ImmutableList (thus Serializable) and write
         * it out. The read path does the opposite -- it reads the legacyPath and then
         * uses invocation API to set the field.
         */
        getLegacyPath();
        outputStream.defaultWriteObject();
    }
}
