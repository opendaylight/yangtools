/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;

/**
 * A set of utility methods for interacting with {@link NormalizedNode} objects.
 */
@Beta
public final class NormalizedNodes {
    private static final int STRINGTREE_INDENT = 4;

    private NormalizedNodes() {
        // Hidden on purpose
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
            final PathArgument pathArg) {
        return parent.flatMap(node -> getDirectChild(node, pathArg));
    }

    public static Optional<NormalizedNode<?, ?>> findNode(final Optional<NormalizedNode<?, ?>> parent,
            final PathArgument... relativePath) {
        return findNode(parent, Arrays.asList(relativePath));
    }

    public static Optional<NormalizedNode<?, ?>> findNode(final @Nullable NormalizedNode<?, ?> parent,
            final PathArgument pathArg) {
        return parent == null ? Optional.empty() : getDirectChild(parent, pathArg);
    }

    public static Optional<NormalizedNode<?, ?>> findNode(final NormalizedNode<?, ?> parent,
            final Iterable<PathArgument> relativePath) {
        return findNode(Optional.ofNullable(parent), relativePath);
    }

    /**
     * Lookup a node based on relative SchemaPath.
     *
     * @deprecated Use {@link #findNode(NormalizedNode, Descendant)} instead.
     */
    @Deprecated(forRemoval = true)
    public static Optional<NormalizedNode<?, ?>> findNode(final NormalizedNode<?, ?> parent,
            final SchemaPath relativePath) {
        checkArgument(!relativePath.isAbsolute(), "%s is not a relative path", relativePath);
        return findNode(Optional.ofNullable(parent), Iterables.transform(relativePath.getPathFromRoot(),
            NodeIdentifier::new));
    }

    public static Optional<NormalizedNode<?, ?>> findNode(final NormalizedNode<?, ?> parent, final Descendant path) {
        return findNode(Optional.ofNullable(parent),
            Iterables.transform(path.getNodeIdentifiers(), NodeIdentifier::new));
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Optional<NormalizedNode<?, ?>> getDirectChild(final NormalizedNode<?, ?> node,
            final PathArgument pathArg) {
        if (node instanceof DataContainerNode) {
            return (Optional) ((DataContainerNode<?>) node).getChild(pathArg);
        } else if (node instanceof MapNode && pathArg instanceof NodeIdentifierWithPredicates) {
            return (Optional) ((MapNode) node).getChild((NodeIdentifierWithPredicates) pathArg);
        } else if (node instanceof LeafSetNode && pathArg instanceof NodeWithValue) {
            return (Optional) ((LeafSetNode<?>) node).getChild((NodeWithValue) pathArg);
        }
        // Anything else, including ValueNode
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
}
