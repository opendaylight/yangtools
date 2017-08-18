/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * A set of utility methods for interacting with {@link NormalizedNode} objects.
 */
@Beta
public final class NormalizedNodes {
    private static final int STRINGTREE_INDENT = 4;

    private NormalizedNodes() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    public static Optional<NormalizedNode<?, ?>> findNode(final YangInstanceIdentifier rootPath,
            final NormalizedNode<?, ?> rootNode, final YangInstanceIdentifier childPath) {
        final Optional<YangInstanceIdentifier> relativePath = childPath.relativeTo(rootPath);
        return relativePath.isPresent() ? findNode(rootNode, relativePath.get()) : Optional.empty();
    }

    public static Optional<NormalizedNode<?, ?>> findNode(final Optional<NormalizedNode<?, ?>> parent,
            final Iterable<PathArgument> relativePath) {
        requireNonNull(parent, "Parent must not be null");
        requireNonNull(relativePath, "Relative path must not be null");

        Optional<NormalizedNode<?, ?>> currentNode = parent;
        final Iterator<PathArgument> pathIterator = relativePath.iterator();
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
            final PathArgument... relativePath) {
        return findNode(parent, Arrays.asList(relativePath));
    }

    public static Optional<NormalizedNode<?, ?>> findNode(final NormalizedNode<?, ?> tree,
            final YangInstanceIdentifier path) {
        requireNonNull(tree, "Tree must not be null");
        requireNonNull(path, "Path must not be null");

        return findNode(Optional.of(tree), path.getPathArguments());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Optional<NormalizedNode<?, ?>> getDirectChild(final NormalizedNode<?, ?> node,
            final PathArgument pathArg) {
        if (node instanceof LeafNode<?> || node instanceof LeafSetEntryNode<?>) {
            return Optional.empty();
        } else if (node instanceof DataContainerNode<?>) {
            return (Optional) ((DataContainerNode<?>) node).getChild(pathArg);
        } else if (node instanceof MapNode && pathArg instanceof NodeIdentifierWithPredicates) {
            return (Optional) ((MapNode) node).getChild((NodeIdentifierWithPredicates) pathArg);
        } else if (node instanceof LeafSetNode<?>) {
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
        final String prefix = Strings.repeat(" ", offset);

        builder.append(prefix).append(toStringTree(node.getIdentifier()));
        if (node instanceof NormalizedNodeContainer<?, ?, ?>) {
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
            return identifier.getNodeType().getLocalName()
                + ((NodeIdentifierWithPredicates) identifier).getKeyValues().values();
        } else if (identifier instanceof AugmentationIdentifier) {
            return "augmentation";
        } else {
            return identifier.getNodeType().getLocalName();
        }
    }

    /**
     * Find duplicate NormalizedNode instances within a subtree. Duplicates are those, which compare
     * as equal, but do not refer to the same object.
     *
     * @param node A normalized node subtree, may not be null
     * @return A Map of NormalizedNode/DuplicateEntry relationships.
     */
    public static Map<NormalizedNode<?, ?>, DuplicateEntry> findDuplicates(@Nonnull final NormalizedNode<?, ?> node) {
        return Maps.filterValues(DuplicateFinder.findDuplicates(node), input -> !input.getDuplicates().isEmpty());
    }
}
