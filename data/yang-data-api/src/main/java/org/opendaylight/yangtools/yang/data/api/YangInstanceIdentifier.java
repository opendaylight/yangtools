/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.VerifyException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Array;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.AbstractHierarchicalIdentifier;
import org.opendaylight.yangtools.concepts.Identifier;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.util.ImmutableOffsetMap;
import org.opendaylight.yangtools.util.SingletonSet;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;

/**
 * Unique identifier of a particular node instance in the data tree.
 *
 * <p>
 * Java representation of YANG Built-in type {@code instance-identifier}, which conceptually is XPath expression
 * minimized to uniquely identify element in data tree which conforms to constraints maintained by YANG Model,
 * effectively this makes Instance Identifier a path to element in data tree.
 *
 * <p>
 * Constraints put in YANG specification on instance-identifier allowed it to be effectively represented in Java and its
 * evaluation does not require a full-blown XPath processor.
 *
 * <h2>Path Arguments</h2>
 * Path to the node represented in instance identifier consists of {@link PathArgument} which carries necessary
 * information to uniquely identify node on particular level in the subtree.
 *
 * <ul>
 *   <li>{@link NodeIdentifier} - Identifier of node, which has cardinality {@code 0..1} in particular subtree in data
 *       tree</li>
 *   <li>{@link NodeIdentifierWithPredicates} - Identifier of node (list item), which has cardinality {@code 0..n}</li>
 *   <li>{@link NodeWithValue} - Identifier of instance {@code leaf} node or {@code leaf-list} node</li>
 * </ul>
 *
 * @see <a href="http://www.rfc-editor.org/rfc/rfc6020#section-9.13">RFC6020</a>
 */
public abstract sealed class YangInstanceIdentifier
        extends AbstractHierarchicalIdentifier<YangInstanceIdentifier, YangInstanceIdentifier.PathArgument>
        permits FixedYangInstanceIdentifier, StackedYangInstanceIdentifier {
    @java.io.Serial
    private static final long serialVersionUID = 4L;
    private static final VarHandle TO_STRING_CACHE;
    private static final VarHandle HASH;

    static {
        final var lookup = MethodHandles.lookup();
        try {
            HASH = lookup.findVarHandle(YangInstanceIdentifier.class, "hash", int.class);
            TO_STRING_CACHE = lookup.findVarHandle(YangInstanceIdentifier.class, "toStringCache", String.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @SuppressWarnings("unused")
    private int hash;
    @SuppressWarnings("unused")
    private transient String toStringCache = null;

    YangInstanceIdentifier() {
        // Package-private to prevent outside subclassing
    }

    /**
     * Return An empty {@link YangInstanceIdentifier}. It corresponds to the path of the conceptual root of the YANG
     * namespace.
     *
     * @return An empty YangInstanceIdentifier
     * @deprecated Use {@link #of()} instead.
     */
    @Deprecated(since = "11.0.0", forRemoval = true)
    public static final @NonNull YangInstanceIdentifier empty() {
        return of();
    }

    /**
     * Return an empty {@link YangInstanceIdentifier}. It corresponds to the path of the conceptual root of the YANG
     * namespace.
     *
     * @return An empty YangInstanceIdentifier
     */
    public static final @NonNull YangInstanceIdentifier of() {
        return FixedYangInstanceIdentifier.EMPTY_INSTANCE;
    }

    /**
     * Returns a new InstanceIdentifier with only one path argument of type {@link PathArgument}.
     *
     * @param name QName of first node identifier
     * @return A YangInstanceIdentifier
     * @throws NullPointerException if {@code name} is {@code null}
     */
    public static final @NonNull YangInstanceIdentifier of(final PathArgument name) {
        return new FixedYangInstanceIdentifier(ImmutableList.of(name));
    }

    /**
     * Returns a new InstanceIdentifier composed of supplied {@link PathArgument}s.
     *
     * @param path Path arguments
     * @return A YangInstanceIdentifier
     * @throws NullPointerException if {@code path} or any of its components is {@code null}
     */
    public static final @NonNull YangInstanceIdentifier of(final PathArgument... path) {
        // We are forcing a copy, since we cannot trust the user
        return of(ImmutableList.copyOf(path));
    }

    /**
     * Returns a new InstanceIdentifier composed of supplied {@link PathArgument}s.
     *
     * @param path Path arguments
     * @return A YangInstanceIdentifier
     * @throws NullPointerException if {@code path} is {@code null}
     */
    public static final @NonNull YangInstanceIdentifier of(final ImmutableList<PathArgument> path) {
        return path.isEmpty() ? of() : new FixedYangInstanceIdentifier(path);
    }

    /**
     * Returns a new InstanceIdentifier composed of supplied {@link PathArgument}s.
     *
     * @param path Path arguments
     * @return A YangInstanceIdentifier
     * @throws NullPointerException if {@code path} or any of its components is {@code null}
     */
    public static final @NonNull YangInstanceIdentifier of(final Collection<? extends PathArgument> path) {
        return path.isEmpty() ? of() : of(ImmutableList.copyOf(path));
    }

    /**
     * Returns a new InstanceIdentifier composed of supplied {@link PathArgument}s.
     *
     * @param path Path arguments
     * @return A YangInstanceIdentifier
     * @throws NullPointerException if {@code path} or any of its components is {@code null}
     */
    public static final @NonNull YangInstanceIdentifier of(final Iterable<? extends PathArgument> path) {
        return of(ImmutableList.copyOf(path));
    }

    /**
     * Returns a new {@link YangInstanceIdentifier} with only one path argument of type {@link NodeIdentifier} with
     * supplied {@link QName}. Note this is a convenience method aimed at test code. Production code should consider
     * using {@link #of(PathArgument)} instead.
     *
     * @param name QName of first {@link NodeIdentifier}
     * @return A YangInstanceIdentifier
     * @throws NullPointerException if {@code name} is {@code null}
     */
    public static final @NonNull YangInstanceIdentifier of(final QName name) {
        return of(new NodeIdentifier(name));
    }

    /**
     * Returns a new {@link YangInstanceIdentifier} with path arguments of type {@link NodeIdentifier} with
     * supplied {@link QName}s. Note this is a convenience method aimed at test code. Production code should consider
     * using {@link #of(PathArgument...)} instead.
     *
     * @param path QNames of {@link NodeIdentifier}s
     * @return A YangInstanceIdentifier
     * @throws NullPointerException if {@code path} or any of its components is {@code null}
     */
    public static final @NonNull YangInstanceIdentifier of(final QName... path) {
        return of(Arrays.stream(path).map(NodeIdentifier::new).collect(ImmutableList.toImmutableList()));
    }

    /**
     * Create a YangInstanceIdentifier composed of a single {@link PathArgument}.
     *
     * @param pathArgument Path argument
     * @return A {@link YangInstanceIdentifier}
     * @throws NullPointerException if {@code pathArgument} is null
     * @deprecated Use {@link #of(NodeIdentifier)} instead.
     */
    @Deprecated(since = "11.0.0", forRemoval = true)
    public static @NonNull YangInstanceIdentifier create(final PathArgument pathArgument) {
        return of(pathArgument);
    }

    /**
     * Create a YangInstanceIdentifier composed of specified {@link PathArgument}s.
     *
     * @param path Path arguments
     * @return A {@link YangInstanceIdentifier}
     * @throws NullPointerException if {@code path} or any of its components is {@code null}
     * @deprecated Use {@link #of(PathArgument...)} instead.
     */
    @Deprecated(since = "11.0.0", forRemoval = true)
    public static @NonNull YangInstanceIdentifier create(final PathArgument... path) {
        return of(path);
    }

    /**
     * Create a YangInstanceIdentifier composed of specified {@link PathArgument}s.
     *
     * @param path Path arguments
     * @return A {@link YangInstanceIdentifier}
     * @throws NullPointerException if {@code path} or any of its components is {@code null}
     * @deprecated Use {@link #of(Iterable)} instead.
     */
    @Deprecated(since = "11.0.0", forRemoval = true)
    public static @NonNull YangInstanceIdentifier create(final Iterable<? extends PathArgument> path) {
        return of(path);
    }

    abstract @NonNull YangInstanceIdentifier createRelativeIdentifier(int skipFromRoot);

    abstract @Nullable List<PathArgument> tryPathArguments();

    abstract @Nullable List<PathArgument> tryReversePathArguments();

    /**
     * Check if this instance identifier has empty path arguments, e.g. it is empty and corresponds to {@link #of()}.
     *
     * @return True if this instance identifier is empty, false otherwise.
     */
    public abstract boolean isEmpty();

    /**
     * Return an optimized version of this identifier, useful when the identifier
     * will be used very frequently.
     *
     * @return A optimized equivalent instance.
     */
    public abstract @NonNull YangInstanceIdentifier toOptimized();

    /**
     * Return the conceptual parent {@link YangInstanceIdentifier}, which has
     * one item less in {@link #getPathArguments()}.
     *
     * @return Parent {@link YangInstanceIdentifier}, or null if this object is {@link #of()}.
     */
    public abstract @Nullable YangInstanceIdentifier getParent();

    /**
     * Return the conceptual parent {@link YangInstanceIdentifier}, which has one item less in
     * {@link #getPathArguments()}.
     *
     * @return Parent {@link YangInstanceIdentifier}
     * @throws VerifyException if this object is {@link #of()}.
     */
    public abstract @NonNull YangInstanceIdentifier coerceParent();

    /**
     * Return the ancestor {@link YangInstanceIdentifier} with a particular depth, e.g. number of path arguments.
     *
     * @param depth Ancestor depth
     * @return Ancestor {@link YangInstanceIdentifier}
     * @throws IllegalArgumentException if the specified depth is negative or is greater than the depth of this object.
     */
    public abstract @NonNull YangInstanceIdentifier getAncestor(int depth);

    /**
     * Returns an ordered iteration of path arguments.
     *
     * @return Immutable iteration of path arguments.
     */
    public abstract @NonNull List<PathArgument> getPathArguments();

    /**
     * Returns an iterable of path arguments in reverse order. This is useful
     * when walking up a tree organized this way.
     *
     * @return Immutable iterable of path arguments in reverse order.
     */
    public abstract @NonNull List<PathArgument> getReversePathArguments();

    /**
     * Returns the last PathArgument. This is equivalent of iterating
     * to the last element of the iterable returned by {@link #getPathArguments()}.
     *
     * @return The last past argument, or null if there are no PathArguments.
     */
    public abstract PathArgument getLastPathArgument();

    /**
     * Create a {@link YangInstanceIdentifier} by taking a snapshot of provided path and iterating it backwards.
     *
     * @param pathTowardsRoot Path towards root
     * @return A {@link YangInstanceIdentifier} instance
     * @throws NullPointerException if {@code pathTowardsRoot} or any of its members is null
     */
    public static @NonNull YangInstanceIdentifier createReverse(final Deque<PathArgument> pathTowardsRoot) {
        final ImmutableList.Builder<PathArgument> builder = ImmutableList.builderWithExpectedSize(
            pathTowardsRoot.size());
        pathTowardsRoot.descendingIterator().forEachRemaining(builder::add);
        return YangInstanceIdentifier.of(builder.build());
    }

    /**
     * Create a {@link YangInstanceIdentifier} by walking specified stack backwards and extracting path components
     * from it.
     *
     * @param stackTowardsRoot Stack towards root,
     * @return A {@link YangInstanceIdentifier} instance
     * @throws NullPointerException if {@code pathTowardsRoot} is null
     */
    public static <T> @NonNull YangInstanceIdentifier createReverse(final Deque<? extends T> stackTowardsRoot,
            final Function<T, PathArgument> function) {
        final var builder = ImmutableList.<PathArgument>builderWithExpectedSize(stackTowardsRoot.size());
        final var it = stackTowardsRoot.descendingIterator();
        while (it.hasNext()) {
            builder.add(function.apply(it.next()));
        }
        return YangInstanceIdentifier.of(builder.build());
    }

    boolean pathArgumentsEqual(final YangInstanceIdentifier other) {
        return getPathArguments().equals(other.getPathArguments());
    }

    @Override
    public final boolean equals(final Object obj) {
        return this == obj || obj instanceof YangInstanceIdentifier other && pathArgumentsEqual(other);
    }

    /**
     * Constructs a new Instance Identifier with new {@link NodeIdentifier} added to the end of path arguments.
     *
     * @param name QName of {@link NodeIdentifier}
     * @return Instance Identifier with additional path argument added to the end.
     */
    public final @NonNull YangInstanceIdentifier node(final QName name) {
        return node(new NodeIdentifier(name));
    }

    /**
     * Constructs a new Instance Identifier with new {@link PathArgument} added to the end of path arguments.
     *
     * @param arg Path argument which should be added to the end
     * @return Instance Identifier with additional path argument added to the end.
     */
    public final @NonNull YangInstanceIdentifier node(final PathArgument arg) {
        return new StackedYangInstanceIdentifier(this, arg);
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
        if (this == ancestor) {
            return Optional.of(of());
        }
        if (ancestor.isEmpty()) {
            return Optional.of(this);
        }

        final var lit = getPathArguments().iterator();
        int common = 0;

        for (var element : ancestor.getPathArguments()) {
            // Ancestor is not really an ancestor
            if (!lit.hasNext() || !lit.next().equals(element)) {
                return Optional.empty();
            }

            ++common;
        }

        if (common == 0) {
            return Optional.of(this);
        }
        if (!lit.hasNext()) {
            return Optional.of(of());
        }

        return Optional.of(createRelativeIdentifier(common));
    }

    @Override
    protected final Iterator<PathArgument> itemIterator() {
        return getPathArguments().iterator();
    }

    @Override
    protected final Object writeReplace() {
        return new YIDv1(this);
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
    public final String toString() {
        /*
         * The toStringCache is safe, since the object contract requires
         * immutability of the object and all objects referenced from this
         * object.
         * Used lists, maps are immutable. Path Arguments (elements) are also
         * immutable, since the PathArgument contract requires immutability.
         * The cache is thread-safe - if multiple computations occurs at the
         * same time, cache will be overwritten with same result.
         */
        final var ret = (String) TO_STRING_CACHE.getAcquire(this);
        return ret != null ? ret : loadToString();
    }

    private String loadToString() {
        final StringBuilder builder = new StringBuilder("/");
        PathArgument prev = null;
        for (PathArgument argument : getPathArguments()) {
            if (prev != null) {
                builder.append('/');
            }
            builder.append(argument.toRelativeString(prev));
            prev = argument;
        }

        final String ret = builder.toString();
        final String witness = (String) TO_STRING_CACHE.compareAndExchangeRelease(this, null, ret);
        return witness == null ? ret : witness;
    }

    @Override
    public final int hashCode() {
        /*
         * The caching is safe, since the object contract requires
         * immutability of the object and all objects referenced from this
         * object.
         * Used lists, maps are immutable. Path Arguments (elements) are also
         * immutable, since the PathArgument contract requires immutability.
         */
        final int local = (int) HASH.getAcquire(this);
        return local != 0 ? local : loadHashCode();
    }

    private static int hashCode(final Object value) {
        if (value == null) {
            return 0;
        }

        if (byte[].class.equals(value.getClass())) {
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

    private int loadHashCode() {
        final int computed = computeHashCode();
        HASH.setRelease(this, computed);
        return computed;
    }

    abstract int computeHashCode();

    /**
     * Returns new builder for InstanceIdentifier with empty path arguments.
     *
     * @return new builder for InstanceIdentifier with empty path arguments.
     */
    public static @NonNull InstanceIdentifierBuilder builder() {
        return new YangInstanceIdentifierBuilder();
    }

    /**
     * Returns new builder for InstanceIdentifier with path arguments copied from original instance identifier.
     *
     * @param origin InstanceIdentifier from which path arguments are copied.
     * @return new builder for InstanceIdentifier with path arguments copied from original instance identifier.
     */
    public static @NonNull InstanceIdentifierBuilder builder(final YangInstanceIdentifier origin) {
        return new YangInstanceIdentifierBuilder(origin.getPathArguments());
    }

    /**
     * Path argument / component of InstanceIdentifier.
     * Path argument uniquely identifies node in data tree on particular
     * level.
     *
     * <p>
     * This interface itself is used as common parent for actual
     * path arguments types and should not be implemented by user code.
     *
     * <p>
     * Path arguments SHOULD contain only minimum of information
     * required to uniquely identify node on particular subtree level.
     *
     * <p>
     * For actual path arguments types see:
     * <ul>
     * <li>{@link NodeIdentifier} - Identifier of container or leaf
     * <li>{@link NodeIdentifierWithPredicates} - Identifier of list entries, which have key defined
     * <li>{@link NodeWithValue} - Identifier of leaf-list entry
     * </ul>
     */
    public abstract static sealed class PathArgument implements Identifier, Comparable<PathArgument> {
        @java.io.Serial
        private static final long serialVersionUID = -4546547994250849340L;

        private final @NonNull QName nodeType;
        private transient volatile int hashValue;

        protected PathArgument(final QName nodeType) {
            this.nodeType = requireNonNull(nodeType);
        }

        /**
         * Returns unique QName of data node as defined in YANG Schema, if available.
         *
         * @return Node type
         */
        public final @NonNull QName getNodeType() {
            return nodeType;
        }

        /**
         * Return the string representation of this object for use in context
         * provided by a previous object. This method can be implemented in
         * terms of {@link #toString()}, but implementations are encourage to
         * reuse any context already emitted by the previous object.
         *
         * @param previous Previous path argument
         * @return String representation
         */
        public @NonNull String toRelativeString(final PathArgument previous) {
            if (previous != null && nodeType.getModule().equals(previous.nodeType.getModule())) {
                return nodeType.getLocalName();
            }
            return nodeType.toString();
        }

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public int compareTo(final PathArgument o) {
            return nodeType.compareTo(o.nodeType);
        }

        protected int hashCodeImpl() {
            return nodeType.hashCode();
        }

        @Override
        public final int hashCode() {
            int local;
            return (local = hashValue) != 0 ? local : (hashValue = hashCodeImpl());
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }

            return nodeType.equals(((PathArgument) obj).nodeType);
        }

        @Override
        public String toString() {
            return nodeType.toString();
        }

        @java.io.Serial
        abstract Object writeReplace();
    }

    /**
     * Simple path argument identifying a {@link org.opendaylight.yangtools.yang.data.api.schema.ContainerNode} or
     * {@link org.opendaylight.yangtools.yang.data.api.schema.LeafNode} leaf in particular subtree.
     */
    public static final class NodeIdentifier extends PathArgument {
        @java.io.Serial
        private static final long serialVersionUID = -2255888212390871347L;
        private static final LoadingCache<QName, NodeIdentifier> CACHE = CacheBuilder.newBuilder().weakValues()
                .build(new CacheLoader<QName, NodeIdentifier>() {
                    @Override
                    public NodeIdentifier load(final QName key) {
                        return new NodeIdentifier(key);
                    }
                });

        public NodeIdentifier(final QName node) {
            super(node);
        }

        /**
         * Return a NodeIdentifier for a particular QName. Unlike the constructor, this factory method uses a global
         * instance cache, resulting in object reuse for equal inputs.
         *
         * @param node Node's QName
         * @return A {@link NodeIdentifier}
         */
        public static @NonNull NodeIdentifier create(final QName node) {
            return CACHE.getUnchecked(node);
        }

        @Override
        Object writeReplace() {
            return new NIv1(this);
        }
    }

    /**
     * Composite path argument identifying a {@link org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode} leaf
     * overall data tree.
     */
    public abstract static sealed class NodeIdentifierWithPredicates extends PathArgument {
        @Beta
        public static final class Singleton extends NodeIdentifierWithPredicates {
            @java.io.Serial
            private static final long serialVersionUID = 1L;

            private final @NonNull QName key;
            private final @NonNull Object value;

            Singleton(final QName node, final QName key, final Object value) {
                super(node);
                this.key = requireNonNull(key);
                this.value = requireNonNull(value);
            }

            @Override
            public SingletonSet<Entry<QName, Object>> entrySet() {
                return SingletonSet.of(singleEntry());
            }

            @Override
            public SingletonSet<QName> keySet() {
                return SingletonSet.of(key);
            }

            @Override
            public boolean containsKey(final QName qname) {
                return key.equals(requireNonNull(qname));
            }

            @Override
            public SingletonSet<Object> values() {
                return SingletonSet.of(value);
            }

            @Override
            public int size() {
                return 1;
            }

            @Override
            public ImmutableMap<QName, Object> asMap() {
                return ImmutableMap.of(key, value);
            }

            /**
             * Return the single entry contained in this object. This is equivalent to
             * {@code entrySet().iterator().next()}.
             *
             * @return A single entry.
             */
            public @NonNull Entry<QName, Object> singleEntry() {
                return new SimpleImmutableEntry<>(key, value);
            }

            @Override
            boolean equalMapping(final NodeIdentifierWithPredicates other) {
                final Singleton single = (Singleton) other;
                return key.equals(single.key) && Objects.deepEquals(value, single.value);
            }

            @Override
            Object keyValue(final QName qname) {
                return key.equals(qname) ? value : null;
            }
        }

        private static final class Regular extends NodeIdentifierWithPredicates {
            @java.io.Serial
            private static final long serialVersionUID = 1L;

            private final @NonNull Map<QName, Object> keyValues;

            Regular(final QName node, final Map<QName, Object> keyValues) {
                super(node);
                this.keyValues = requireNonNull(keyValues);
            }

            @Override
            public Set<Entry<QName, Object>> entrySet() {
                return keyValues.entrySet();
            }

            @Override
            public Set<QName> keySet() {
                return keyValues.keySet();
            }

            @Override
            public boolean containsKey(final QName qname) {
                return keyValues.containsKey(requireNonNull(qname));
            }

            @Override
            public Collection<Object> values() {
                return keyValues.values();
            }

            @Override
            public int size() {
                return keyValues.size();
            }

            @Override
            public Map<QName, Object> asMap() {
                return keyValues;
            }

            @Override
            Object keyValue(final QName qname) {
                return keyValues.get(qname);
            }

            @Override
            boolean equalMapping(final NodeIdentifierWithPredicates other) {
                final Map<QName, Object> otherKeyValues = ((Regular) other).keyValues;
                // TODO: benchmark to see if just calling equals() on the two maps is not faster
                if (keyValues == otherKeyValues) {
                    return true;
                }
                if (keyValues.size() != otherKeyValues.size()) {
                    return false;
                }

                for (Entry<QName, Object> entry : entrySet()) {
                    final Object otherValue = otherKeyValues.get(entry.getKey());
                    if (otherValue == null || !Objects.deepEquals(entry.getValue(), otherValue)) {
                        return false;
                    }
                }

                return true;
            }
        }

        @java.io.Serial
        private static final long serialVersionUID = -4787195606494761540L;

        NodeIdentifierWithPredicates(final QName node) {
            super(node);
        }

        public static @NonNull NodeIdentifierWithPredicates of(final QName node) {
            return new Regular(node, ImmutableMap.of());
        }

        public static @NonNull NodeIdentifierWithPredicates of(final QName node, final QName key, final Object value) {
            return new Singleton(node, key, value);
        }

        public static @NonNull NodeIdentifierWithPredicates of(final QName node, final Entry<QName, Object> entry) {
            return of(node, entry.getKey(), entry.getValue());
        }

        public static @NonNull NodeIdentifierWithPredicates of(final QName node, final Map<QName, Object> keyValues) {
            return keyValues.size() == 1 ? of(keyValues, node)
                    // Retains ImmutableMap for empty maps. For larger sizes uses a shared key set.
                    : new Regular(node, ImmutableOffsetMap.unorderedCopyOf(keyValues));
        }

        public static @NonNull NodeIdentifierWithPredicates of(final QName node,
                final ImmutableOffsetMap<QName, Object> keyValues) {
            return keyValues.size() == 1 ? of(keyValues, node) : new Regular(node, keyValues);
        }

        private static @NonNull NodeIdentifierWithPredicates of(final Map<QName, Object> keyValues, final QName node) {
            return of(node, keyValues.entrySet().iterator().next());
        }

        /**
         * Return the set of predicates keys and values. Keys are guaranteeed to be unique.
         *
         * @return Predicate set.
         */
        public abstract @NonNull Set<Entry<QName, Object>> entrySet();

        /**
         * Return the predicate key in the iteration order of {@link #entrySet()}.
         *
         * @return Predicate values.
         */
        public abstract @NonNull Set<QName> keySet();

        /**
         * Determine whether a particular predicate key is present.
         *
         * @param key Predicate key
         * @return True if the predicate is present, false otherwise
         * @throws NullPointerException if {@code key} is null
         */
        public abstract boolean containsKey(QName key);

        /**
         * Return the predicate values in the iteration order of {@link #entrySet()}.
         *
         * @return Predicate values.
         */
        public abstract @NonNull Collection<Object> values();

        @Beta
        public final @Nullable Object getValue(final QName key) {
            return keyValue(requireNonNull(key));
        }

        @Beta
        public final <T> @Nullable T getValue(final QName key, final Class<T> valueClass) {
            return valueClass.cast(getValue(key));
        }

        /**
         * Return the number of predicates present.
         *
         * @return The number of predicates present.
         */
        public abstract int size();

        /**
         * A Map-like view of this identifier's predicates. The view is expected to be stable and effectively-immutable.
         *
         * @return Map of predicates.
         */
        @Beta
        public abstract @NonNull Map<QName, Object> asMap();

        @Override
        protected final int hashCodeImpl() {
            int result = 31 * super.hashCodeImpl();
            for (Entry<QName, Object> entry : entrySet()) {
                result += entry.getKey().hashCode() + YangInstanceIdentifier.hashCode(entry.getValue());
            }
            return result;
        }

        @Override
        @SuppressWarnings("checkstyle:equalsHashCode")
        public final boolean equals(final Object obj) {
            return super.equals(obj) && equalMapping((NodeIdentifierWithPredicates) obj);
        }

        abstract boolean equalMapping(NodeIdentifierWithPredicates other);

        abstract @Nullable Object keyValue(@NonNull QName qname);

        @Override
        public final String toString() {
            return super.toString() + '[' + asMap() + ']';
        }

        @Override
        public final String toRelativeString(final PathArgument previous) {
            return super.toRelativeString(previous) + '[' + asMap() + ']';
        }

        @Override
        final Object writeReplace() {
            return new NIPv2(this);
        }
    }

    /**
     * Simple path argument identifying a {@link LeafSetEntryNode} leaf
     * overall data tree.
     */
    public static final class NodeWithValue<T> extends PathArgument {
        @java.io.Serial
        private static final long serialVersionUID = -3637456085341738431L;

        private final @NonNull T value;

        public NodeWithValue(final QName node, final T value) {
            super(node);
            this.value = requireNonNull(value);
        }

        public @NonNull T getValue() {
            return value;
        }

        @Override
        protected int hashCodeImpl() {
            return 31 * super.hashCodeImpl() + YangInstanceIdentifier.hashCode(value);
        }

        @Override
        @SuppressWarnings("checkstyle:equalsHashCode")
        public boolean equals(final Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            final NodeWithValue<?> other = (NodeWithValue<?>) obj;
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

        @Override
        Object writeReplace() {
            return new NIVv1(this);
        }
    }

    /**
     * Fluent Builder of Instance Identifier instances.
     */
    public interface InstanceIdentifierBuilder extends Mutable {
        /**
         * Adds a {@link PathArgument} to path arguments of resulting instance identifier.
         *
         * @param arg A {@link PathArgument} to be added
         * @return this builder
         */
        @NonNull InstanceIdentifierBuilder node(PathArgument arg);

        /**
         * Adds {@link NodeIdentifier} with supplied QName to path arguments of resulting instance identifier.
         *
         * @param nodeType QName of {@link NodeIdentifier} which will be added
         * @return this builder
         */
        @NonNull InstanceIdentifierBuilder node(QName nodeType);

        /**
         * Adds {@link NodeIdentifierWithPredicates} with supplied QName and key values to path arguments of resulting
         * instance identifier.
         *
         * @param nodeType QName of {@link NodeIdentifierWithPredicates} which will be added
         * @param keyValues Map of key components and their respective values for {@link NodeIdentifierWithPredicates}
         * @return this builder
         */
        @NonNull InstanceIdentifierBuilder nodeWithKey(QName nodeType, Map<QName, Object> keyValues);

        /**
         * Adds {@link NodeIdentifierWithPredicates} with supplied QName and key, value.
         *
         * @param nodeType QName of {@link NodeIdentifierWithPredicates} which will be added
         * @param key QName of key which will be added
         * @param value value of key which will be added
         * @return this builder
         */
        @NonNull InstanceIdentifierBuilder nodeWithKey(QName nodeType, QName key, Object value);

        /**
         * Adds a collection of {@link PathArgument}s to path arguments of resulting instance identifier.
         *
         * @param args {@link PathArgument}s to be added
         * @return this builder
         * @throws NullPointerException if any of the arguments is null
         */
        @NonNull InstanceIdentifierBuilder append(Collection<? extends PathArgument> args);

        /**
         * Adds a collection of {@link PathArgument}s to path arguments of resulting instance identifier.
         *
         * @param args {@link PathArgument}s to be added
         * @return this builder
         * @throws NullPointerException if any of the arguments is null
         */
        default @NonNull InstanceIdentifierBuilder append(final PathArgument... args) {
            return append(Arrays.asList(args));
        }

        /**
         * Builds an {@link YangInstanceIdentifier} with path arguments from this builder.
         *
         * @return {@link YangInstanceIdentifier}
         */
        @NonNull YangInstanceIdentifier build();
    }
}
