/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * A set of utility methods for interacting with {@link NormalizedNode} objects.
 */
@Beta
public final class NormalizedNodes {
    private static final int STRINGTREE_INDENT = 4;

    private NormalizedNodes() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    /**
     * Find duplicate NormalizedNode instances within a subtree. Duplicates are those, which compare
     * as equal, but do not refer to the same object.
     *
     * @param node A normalized node subtree, may not be null
     * @return A Map of NormalizedNode/DuplicateEntry relationships.
     */
    public static Map<NormalizedNode<?, ?>, DuplicateEntry> findDuplicates(final @NonNull NormalizedNode<?, ?> node) {
        return Maps.filterValues(DuplicateFinder.findDuplicates(node), input -> !input.getDuplicates().isEmpty());
    }

    public static Optional<NormalizedNode<?, ?>> findNode(final YangInstanceIdentifier rootPath,
            final NormalizedNode<?, ?> rootNode, final YangInstanceIdentifier childPath) {
        final Optional<YangInstanceIdentifier> relativePath = childPath.relativeTo(rootPath);
        return relativePath.isPresent() ? findNode(rootNode, relativePath.get()) : Optional.empty();
    }

    public static Optional<NormalizedNode<?, ?>> findNode(final Optional<NormalizedNode<?, ?>> parent,
            final Iterable<PathArgument> relativePath) {
        final Iterator<PathArgument> pathIterator = requireNonNull(relativePath, "Relative path must not be null")
                .iterator();
        Optional<NormalizedNode<?, ?>> currentNode = requireNonNull(parent, "Parent must not be null");
        while (currentNode.isPresent() && pathIterator.hasNext()) {
            currentNode = getDirectChild(currentNode.get(), pathIterator.next());
        }
        return currentNode;
    }

    public static Optional<NormalizedNode<?, ?>> findNode(final Optional<NormalizedNode<?, ?>> parent,
            final PathArgument... relativePath) {
        return findNode(parent, Arrays.asList(relativePath));
    }

    public static Optional<NormalizedNode<?, ?>> findNode(final NormalizedNode<?, ?> parent,
            final Iterable<PathArgument> relativePath) {
        return findNode(Optional.ofNullable(parent), relativePath);
    }

    public static Optional<NormalizedNode<?, ?>> findNode(final NormalizedNode<?, ?> parent,
            final SchemaPath relativePath) {
        checkArgument(!relativePath.isAbsolute(), "%s is not a relative path", relativePath);
        return findNode(Optional.ofNullable(parent), Iterables.transform(relativePath.getPathFromRoot(),
            NodeIdentifier::new));
    }

    public static Optional<NormalizedNode<?, ?>> findNode(final NormalizedNode<?, ?> parent,
            final PathArgument... relativePath) {
        return findNode(parent, Arrays.asList(relativePath));
    }

    public static Optional<NormalizedNode<?, ?>> findNode(final NormalizedNode<?, ?> tree,
            final YangInstanceIdentifier path) {
        return findNode(Optional.of(requireNonNull(tree, "Tree must not be null")),
            requireNonNull(path, "Path must not be null").getPathArguments());
    }

    /**
     * Expand a {@link YangInstanceIdentifier} child specification into a set of {@link YangInstanceIdentifier}s
     * which identify nodes in a specified root node.
     *
     * <p>
     * The specification takes the form of an instance identifier, with additional lookup mechanics. The mechanics
     * specifically means that children of {@link MapNode}s do not need to be precisely specified via
     * {@link NodeIdentifierWithPredicates}, but can instead be identified via a simple {@link NodeIdentifier}, meaning
     * 'all of the map's direct children'. In such a case this method will traverse each node recursively, and report
     * all nodes matching the criteria.
     *
     * @param rootNode Node to use as the root, corresponding to {@link YangInstanceIdentifier#empty()}
     * @param childSpec Child matching specification
     * @return A set of identifiers of matched nodes. This collection is guaranteed to hold each identifier exactly
     *         once.
     * @throws NullPointerException if any argument is null
     */
    // Note: we expose Collection rather than a Set so we do not need to do comparisons
    public static @NonNull Collection<Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> findNodes(
            final NormalizedNode<?, ?> rootNode, final YangInstanceIdentifier childSpec) {
        if (childSpec.isEmpty()) {
            return ImmutableSet.of(new SimpleImmutableEntry<>(childSpec, requireNonNull(rootNode)));
        }

        final ArrayDeque<PathArgument> args = new ArrayDeque<>(childSpec.getPathArguments());
        final List<Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> result = new ArrayList<>();
        findChildren(result, new ArrayList<>(args.size()), rootNode, args);
        return result;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Optional<NormalizedNode<?, ?>> getDirectChild(final NormalizedNode<?, ?> node,
            final PathArgument pathArg) {
        if (node instanceof ValueNode) {
            return Optional.empty();
        } else if (node instanceof DataContainerNode) {
            return (Optional) ((DataContainerNode<?>) node).getChild(pathArg);
        } else if (node instanceof MapNode && pathArg instanceof NodeIdentifierWithPredicates) {
            return (Optional) ((MapNode) node).getChild((NodeIdentifierWithPredicates) pathArg);
        } else if (node instanceof LeafSetNode && pathArg instanceof NodeWithValue) {
            return (Optional) ((LeafSetNode<?>) node).getChild((NodeWithValue) pathArg);
        }
        return Optional.empty();
    }

    /**
     * Convert a data subtree under a node into a human-readable string format.
     *
     * @param node Data subtree root
     * @return String containing a human-readable form of the subtree.
     */
    public static String toStringTree(final NormalizedNode<?, ?> node) {
        final StringBuilder builder = new StringBuilder();
        toStringTree(builder, node, 0);
        return builder.toString();
    }

    private static void toStringTree(final StringBuilder builder, final NormalizedNode<?, ?> node, final int offset) {
        final String prefix = " ".repeat(offset);

        builder.append(prefix).append(toStringTree(node.getIdentifier()));
        if (node instanceof NormalizedNodeContainer) {
            final NormalizedNodeContainer<?, ?, ?> container = (NormalizedNodeContainer<?, ?, ?>) node;

            builder.append(" {\n");
            for (NormalizedNode<?, ?> child : container.getValue()) {
                toStringTree(builder, child, offset + STRINGTREE_INDENT);
            }

            builder.append(prefix).append('}');
        } else {
            builder.append(' ').append(node.getValue());
        }
        builder.append('\n');
    }

    private static String toStringTree(final PathArgument identifier) {
        if (identifier instanceof NodeIdentifierWithPredicates) {
            return identifier.getNodeType().getLocalName() + ((NodeIdentifierWithPredicates) identifier).values();
        } else if (identifier instanceof AugmentationIdentifier) {
            return "augmentation";
        } else {
            return identifier.getNodeType().getLocalName();
        }
    }

    /*
     * Recursive children lookup. We store found children in 'result'. During recursive invocation we poll/push items
     * into 'remaining', so that we it becomes empty we know we have a hit and should store the node.
     *
     * The 'parentState' argument contains the state needed to build the matching YangInstanceIdentifier. While we
     * could build it up as we descend, it is a relatively expensive operation in that it computes hashCode -- which
     * would be a wasted effort if we end up not matching anything under a particular subtree search.
     *
     * Alternatively we could hold a List<PathArgument> and use YangInstanceIdentifier.create(), but that would mean
     * each identifier instance would hold the full copy of arguments. This is wasteful if we end up matching multiple
     * entries, as the YangInstanceIdentifier.node() method allows us to create a child identifier efficiently.
     *
     * We therefore use a mixed approach, where 'parentState' can hold either PathArgument or a YangInstanceIdentifier.
     * When asked to create an identifier we locate the first identifier entry and key off of it, mutating the list
     * to hold intermediate YangInstanceIdentifiers. This means we will end up reusing instances as much as possible.
     */
    private static void findChildren(final List<Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> result,
            final ArrayList<Object> parentState, final NormalizedNode<?, ?> parent,
            final ArrayDeque<PathArgument> remaining) {
        final PathArgument next = remaining.poll();
        if (next == null) {
            // Parent was a complete match, add it
            result.add(new SimpleImmutableEntry<>(createIdentifier(parentState), parent));
            return;
        }

        final Optional<NormalizedNode<?, ?>> optChild = getDirectChild(parent, next);
        if (optChild.isPresent()) {
            // We found the child directly, proceed to examine it
            findChildren(result, parentState, remaining, optChild.get());
        } else if (parent instanceof MapNode && next instanceof NodeIdentifier) {
            // Wildcard case: examine all children
            for (MapEntryNode child : ((MapNode) parent).getValue()) {
                findChildren(result, parentState, remaining, child);
            }
        }

        remaining.push(next);
    }

    private static void findChildren(final List<Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>> result,
            final ArrayList<Object> parentState, final ArrayDeque<PathArgument> remaining,
            final NormalizedNode<?, ?> node) {
        parentState.add(node.getIdentifier());
        findChildren(result, parentState, node, remaining);
        parentState.remove(parentState.size() - 1);
    }

    private static YangInstanceIdentifier createIdentifier(final ArrayList<Object> pathState) {
        // Note: pathState is guaranteed to be non-empty
        for (int i = pathState.size() - 1; i >= 0; --i) {
            final Object obj = pathState.get(i);
            if (obj instanceof YangInstanceIdentifier) {
                return createIdentifier((YangInstanceIdentifier) obj, pathState, i + 1);
            }
        }
        return createIdentifier(YangInstanceIdentifier.empty(), pathState, 0);
    }

    private static YangInstanceIdentifier createIdentifier(final YangInstanceIdentifier first,
            final ArrayList<Object> pathState, final int next) {
        YangInstanceIdentifier current = first;
        for (int i = next, size = pathState.size(); i < size; ++i) {
            final Object obj = pathState.get(i);
            verify(obj instanceof PathArgument, "Unexpected item %s in state %s", obj, pathState);
            current = current.node((PathArgument) obj);
            pathState.set(i, current);
        }
        return current;
    }
}
