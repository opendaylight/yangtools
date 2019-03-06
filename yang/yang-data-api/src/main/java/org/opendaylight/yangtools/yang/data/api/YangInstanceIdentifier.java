/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.LinearPath;
import org.opendaylight.yangtools.util.HashCodeBuilder;
import org.opendaylight.yangtools.util.ImmutableOffsetMap;
import org.opendaylight.yangtools.util.SharedSingletonMap;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;

/**
 * Unique identifier of a particular node instance in the data tree.
 *
 * <p>
 * Java representation of YANG Built-in type <code>instance-identifier</code>,
 * which conceptually is XPath expression minimized to uniquely identify element
 * in data tree which conforms to constraints maintained by YANG Model,
 * effectively this makes Instance Identifier a path to element in data tree.
 *
 * <p>
 * Constraints put in YANG specification on instance-identifier allowed it to be
 * effectively represented in Java and it's evaluation does not require
 * full-blown XPath processor.
 *
 * <p>
 * <h3>Path Arguments</h3>
 * Path to the node represented in instance identifier consists of
 * {@link PathArgument} which carries necessary information to uniquely identify
 * node on particular level in the subtree.
 *
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
 * @see <a href="http://tools.ietf.org/html/rfc6020#section-9.13">RFC6020</a>
 */
// FIXME: 3.0.0: this concept needs to be moved to yang-common, as parser components need the ability to refer
//               to data nodes -- most notably XPath expressions and {@code default} statement arguments need to be able
//               to represent these.
// FIXME: FixedYangInstanceIdentifier needs YangInstanceIdentifier initialized, but that includes initializing
//        this field. Figure out a way out of this pickle.
@SuppressFBWarnings("IC_SUPERCLASS_USES_SUBCLASS_DURING_INITIALIZATION")
public interface YangInstanceIdentifier
        extends LinearPath<YangInstanceIdentifier, YangInstanceIdentifier.PathArgument>, Serializable {
    /**
     * An empty {@link YangInstanceIdentifier}. It corresponds to the path of the conceptual root of the YANG namespace.
     */
    YangInstanceIdentifier EMPTY = FixedYangInstanceIdentifier.EMPTY_INSTANCE;

    /**
     * Returns an ordered iteration of path arguments.
     *
     * @return Immutable iteration of path arguments.
     * @deprecated Use {@link #getPathFromRoot()} instead.
     */
    @Deprecated
    default List<PathArgument> getPathArguments() {
        return getPathFromRoot();
    }

    /**
     * Returns an iterable of path arguments in reverse order. This is useful
     * when walking up a tree organized this way.
     *
     * @return Immutable iterable of path arguments in reverse order.
     * @deprecated Use {@link #getPathTowardsRoot()} instead.
     */
    @Deprecated
    default List<PathArgument> getReversePathArguments() {
        return getPathTowardsRoot();
    }

    /**
     * Returns the last PathArgument. This is equivalent of iterating
     * to the last element of the iterable returned by {@link #getPathArguments()}.
     *
     * @return The last past argument, or null if there are no PathArguments.
     * @deprecated Use {@link #getLastComponent()} instead.
     */
    @Deprecated
    default PathArgument getLastPathArgument() {
        return getLastComponent();
    }

    static YangInstanceIdentifier create(final Iterable<? extends PathArgument> path) {
        if (Iterables.isEmpty(path)) {
            return EMPTY;
        }

        final HashCodeBuilder<PathArgument> hash = new HashCodeBuilder<>();
        for (PathArgument a : path) {
            hash.addArgument(a);
        }

        return FixedYangInstanceIdentifier.create(path, hash.build());
    }

    static YangInstanceIdentifier create(final PathArgument... path) {
        // We are forcing a copy, since we cannot trust the user
        return create(Arrays.asList(path));
    }

    /**
     * Create a {@link YangInstanceIdentifier} by taking a snapshot of provided path and iterating it backwards.
     *
     * @param pathTowardsRoot Path towards root
     * @return A {@link YangInstanceIdentifier} instance
     * @throws NullPointerException if {@code pathTowardsRoot} or any of its members is null
     */
    static YangInstanceIdentifier createReverse(final Deque<PathArgument> pathTowardsRoot) {
        return createReverse(pathTowardsRoot, Objects::requireNonNull);
    }

    /**
     * Create a {@link YangInstanceIdentifier} by walking specified stack backwards and extracting path components
     * from it.
     *
     * @param stackTowardsRoot Stack towards root,
     * @return A {@link YangInstanceIdentifier} instance
     * @throws NullPointerException if {@code pathTowardsRoot} is null
     */
    static <T> YangInstanceIdentifier createReverse(final Deque<? extends T> stackTowardsRoot,
            final Function<T, PathArgument> function) {
        if (stackTowardsRoot.isEmpty()) {
            return EMPTY;
        }

        final ImmutableList.Builder<PathArgument> builder = ImmutableList.builderWithExpectedSize(
            stackTowardsRoot.size());
        final Iterator<? extends T> it = stackTowardsRoot.descendingIterator();
        while (it.hasNext()) {
            builder.add(function.apply(it.next()));
        }
        return YangInstanceIdentifier.create(builder.build());
    }

    /**
     * Constructs a new Instance Identifier with new {@link NodeIdentifier} added to the end of path arguments.
     *
     * @param name QName of {@link NodeIdentifier}
     * @return Instance Identifier with additional path argument added to the end.
     */
    default YangInstanceIdentifier node(final QName name) {
        return node(new NodeIdentifier(name));
    }

    /**
     * Constructs a new Instance Identifier with new {@link PathArgument} added to the end of path arguments.
     *
     * @param arg Path argument which should be added to the end
     * @return Instance Identifier with additional path argument added to the end.
     */
    default YangInstanceIdentifier node(final PathArgument arg) {
        return new StackedYangInstanceIdentifier(this, arg, HashCodeBuilder.nextHashCode(hashCode(), arg));
    }

    // Static factories & helpers

    /**
     * Returns a new InstanceIdentifier with only one path argument of type {@link NodeIdentifier} with supplied
     * QName.
     *
     * @param name QName of first node identifier
     * @return Instance Identifier with only one path argument of type {@link NodeIdentifier}
     */
    static YangInstanceIdentifier of(final QName name) {
        return create(new NodeIdentifier(name));
    }

    /**
     * Returns new builder for InstanceIdentifier with empty path arguments.
     *
     * @return new builder for InstanceIdentifier with empty path arguments.
     */
    static InstanceIdentifierBuilder builder() {
        return new YangInstanceIdentifierBuilder();
    }

    /**
     * Returns new builder for InstanceIdentifier with path arguments copied from original instance identifier.
     *
     * @param origin InstanceIdentifier from which path arguments are copied.
     * @return new builder for InstanceIdentifier with path arguments copied from original instance identifier.
     */
    static InstanceIdentifierBuilder builder(final YangInstanceIdentifier origin) {
        return new YangInstanceIdentifierBuilder(origin.getPathArguments(), origin.hashCode());
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
     * <li>{@link AugmentationIdentifier} - Identifier of augmentation
     * <li>{@link NodeWithValue} - Identifier of leaf-list entry
     * </ul>
     */
    public interface PathArgument extends Comparable<PathArgument>, Immutable, Serializable {
        /**
         * If applicable returns unique QName of data node as defined in YANG
         * Schema.
         *
         * <p>
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

    /**
     * Simple path argument identifying a {@link org.opendaylight.yangtools.yang.data.api.schema.ContainerNode} or
     * {@link org.opendaylight.yangtools.yang.data.api.schema.LeafNode} leaf in particular subtree.
     */
    public static final class NodeIdentifier extends AbstractPathArgument {
        private static final long serialVersionUID = -2255888212390871347L;
        private static final LoadingCache<QName, NodeIdentifier> CACHE = CacheBuilder.newBuilder().weakValues()
                .build(new CacheLoader<QName, NodeIdentifier>() {
                    @Override
                    public NodeIdentifier load(@Nonnull final QName key) {
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
        public static NodeIdentifier create(final QName node) {
            return CACHE.getUnchecked(node);
        }
    }

    /**
     * Composite path argument identifying a {@link org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode} leaf
     * overall data tree.
     */
    public static final class NodeIdentifierWithPredicates extends AbstractPathArgument {
        private static final long serialVersionUID = -4787195606494761540L;

        private final Map<QName, Object> keyValues;

        public NodeIdentifierWithPredicates(final QName node) {
            super(node);
            this.keyValues = ImmutableMap.of();
        }

        public NodeIdentifierWithPredicates(final QName node, final Map<QName, Object> keyValues) {
            super(node);
            // Retains ImmutableMap for empty maps. For larger sizes uses a shared key set.
            this.keyValues = ImmutableOffsetMap.unorderedCopyOf(keyValues);
        }

        public NodeIdentifierWithPredicates(final QName node, final ImmutableOffsetMap<QName, Object> keyValues) {
            super(node);
            this.keyValues = requireNonNull(keyValues);
        }

        public NodeIdentifierWithPredicates(final QName node, final SharedSingletonMap<QName, Object> keyValues) {
            super(node);
            this.keyValues = requireNonNull(keyValues);
        }

        public NodeIdentifierWithPredicates(final QName node, final QName key, final Object value) {
            this(node, SharedSingletonMap.unorderedOf(key, value));
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
                result += Objects.hashCode(entry.getKey()) + valueHashCode(entry.getValue());
            }
            return result;
        }

        @Override
        @SuppressWarnings("checkstyle:equalsHashCode")
        public boolean equals(final Object obj) {
            if (!super.equals(obj)) {
                return false;
            }

            final Map<QName, Object> otherKeyValues = ((NodeIdentifierWithPredicates) obj).keyValues;

            // TODO: benchmark to see if just calling equals() on the two maps is not faster
            if (keyValues == otherKeyValues) {
                return true;
            }
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
    public static final class NodeWithValue<T> extends AbstractPathArgument {
        private static final long serialVersionUID = -3637456085341738431L;

        private final T value;

        public NodeWithValue(final QName node, final T value) {
            super(node);
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        @Override
        protected int hashCodeImpl() {
            final int prime = 31;
            int result = super.hashCodeImpl();
            result = prime * result + valueHashCode(value);
            return result;
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
    }

    /**
     * Composite path argument identifying a {@link org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode}
     * node in particular subtree.
     *
     * <p>
     * Augmentation is uniquely identified by set of all possible child nodes.
     * This is possible
     * to identify instance of augmentation,
     * since RFC6020 states that <code>augment</code> that augment
     * statement must not add multiple nodes from same namespace
     * / module to the target node.
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
         * Construct new augmentation identifier using supplied set of possible
         * child nodes.
         *
         * @param childNames
         *            Set of possible child nodes.
         */
        public AugmentationIdentifier(final Set<QName> childNames) {
            this.childNames = ImmutableSet.copyOf(childNames);
        }

        /**
         * Returns set of all possible child nodes.
         *
         * @return set of all possible child nodes.
         */
        public Set<QName> getPossibleChildNames() {
            return childNames;
        }

        @Override
        public String toString() {
            return "AugmentationIdentifier{" + "childNames=" + childNames + '}';
        }

        @Override
        public String toRelativeString(final PathArgument previous) {
            return toString();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AugmentationIdentifier)) {
                return false;
            }

            AugmentationIdentifier that = (AugmentationIdentifier) obj;
            return childNames.equals(that.childNames);
        }

        @Override
        public int hashCode() {
            return childNames.hashCode();
        }

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public int compareTo(@Nonnull final PathArgument o) {
            if (!(o instanceof AugmentationIdentifier)) {
                return -1;
            }
            AugmentationIdentifier other = (AugmentationIdentifier) o;
            Set<QName> otherChildNames = other.getPossibleChildNames();
            int thisSize = childNames.size();
            int otherSize = otherChildNames.size();
            if (thisSize == otherSize) {
                // Quick Set-based comparison
                if (childNames.equals(otherChildNames)) {
                    return 0;
                }

                // We already know the sets are not equal, but have equal size, hence the sets differ in their elements,
                // but potentially share a common set of elements. The most consistent way of comparing them is using
                // total ordering defined by QName's compareTo. Hence convert both sets to lists ordered
                // by QName.compareTo() and decide on the first differing element.
                final List<QName> diff = new ArrayList<>(Sets.symmetricDifference(childNames, otherChildNames));
                verify(!diff.isEmpty(), "Augmentation identifiers %s and %s report no difference", this, o);
                diff.sort(QName::compareTo);
                return childNames.contains(diff.get(0)) ? -1 : 1;
            } else if (thisSize < otherSize) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    /**
     * Fluent Builder of Instance Identifier instances.
     */
    public interface InstanceIdentifierBuilder extends Builder<YangInstanceIdentifier> {
        /**
         * Adds a {@link PathArgument} to path arguments of resulting instance identifier.
         *
         * @param arg A {@link PathArgument} to be added
         * @return this builder
         */
        InstanceIdentifierBuilder node(PathArgument arg);

        /**
         * Adds {@link NodeIdentifier} with supplied QName to path arguments of resulting instance identifier.
         *
         * @param nodeType QName of {@link NodeIdentifier} which will be added
         * @return this builder
         */
        InstanceIdentifierBuilder node(QName nodeType);

        /**
         * Adds {@link NodeIdentifierWithPredicates} with supplied QName and key values to path arguments of resulting
         * instance identifier.
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
         * Adds a collection of {@link PathArgument}s to path arguments of resulting instance identifier.
         *
         * @param args {@link PathArgument}s to be added
         * @return this builder
         * @throws NullPointerException if any of the arguments is null
         */
        @Beta
        InstanceIdentifierBuilder append(Collection<? extends PathArgument> args);

        /**
         * Adds a collection of {@link PathArgument}s to path arguments of resulting instance identifier.
         *
         * @param args {@link PathArgument}s to be added
         * @return this builder
         * @throws NullPointerException if any of the arguments is null
         */
        @Beta
        default InstanceIdentifierBuilder append(final PathArgument... args) {
            return append(Arrays.asList(args));
        }

        /**
         * Builds an {@link YangInstanceIdentifier} with path arguments from this builder.
         *
         * @return {@link YangInstanceIdentifier}
         */
        @Override
        YangInstanceIdentifier build();
    }
}
