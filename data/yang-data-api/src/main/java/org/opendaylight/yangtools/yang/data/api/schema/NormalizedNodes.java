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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
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
    // FIXME: relocate to yang-data-util, where this can have a proper test suite
    public static Map<NormalizedNode, DuplicateEntry> findDuplicates(final @NonNull NormalizedNode node) {
        return Maps.filterValues(DuplicateFinder.findDuplicates(node), input -> !input.getDuplicates().isEmpty());
    }

    public static Optional<NormalizedNode> findNode(final YangInstanceIdentifier rootPath,
            final NormalizedNode rootNode, final YangInstanceIdentifier childPath) {
        final var relativePath = childPath.relativeTo(rootPath);
        return relativePath.isPresent() ? findNode(rootNode, relativePath.orElseThrow()) : Optional.empty();
    }

    public static Optional<NormalizedNode> findNode(final Optional<NormalizedNode> parent,
            final Iterable<PathArgument> relativePath) {
        final var pathIterator = requireNonNull(relativePath, "Relative path must not be null").iterator();
        var currentNode = requireNonNull(parent, "Parent must not be null");
        while (currentNode.isPresent() && pathIterator.hasNext()) {
            currentNode = getDirectChild(currentNode.orElseThrow(), pathIterator.next());
        }
        return currentNode;
    }

    public static Optional<NormalizedNode> findNode(final Optional<NormalizedNode> parent,
            final PathArgument pathArg) {
        return parent.flatMap(node -> getDirectChild(node, pathArg));
    }

    public static Optional<NormalizedNode> findNode(final Optional<NormalizedNode> parent,
            final PathArgument... relativePath) {
        return findNode(parent, Arrays.asList(relativePath));
    }

    public static Optional<NormalizedNode> findNode(final @Nullable NormalizedNode parent,
            final PathArgument pathArg) {
        return parent == null ? Optional.empty() : getDirectChild(parent, pathArg);
    }

    public static Optional<NormalizedNode> findNode(final NormalizedNode parent,
            final Iterable<PathArgument> relativePath) {
        return findNode(Optional.ofNullable(parent), relativePath);
    }

    public static Optional<NormalizedNode> findNode(final NormalizedNode parent, final Descendant path) {
        return findNode(Optional.ofNullable(parent),
            Lists.transform(path.getNodeIdentifiers(), NodeIdentifier::new));
    }

    public static Optional<NormalizedNode> findNode(final NormalizedNode parent,
            final PathArgument... relativePath) {
        return findNode(parent, Arrays.asList(relativePath));
    }

    public static Optional<NormalizedNode> findNode(final NormalizedNode tree,
            final YangInstanceIdentifier path) {
        return findNode(Optional.of(requireNonNull(tree, "Tree must not be null")),
            requireNonNull(path, "Path must not be null").getPathArguments());
    }

    public static Optional<NormalizedNode> getDirectChild(final NormalizedNode node,
            final PathArgument pathArg) {
        final NormalizedNode child;
        if (node instanceof DataContainerNode dataContainer && pathArg instanceof NodeIdentifier nid) {
            child = dataContainer.childByArg(nid);
        } else if (node instanceof MapNode map && pathArg instanceof NodeIdentifierWithPredicates nip) {
            child = map.childByArg(nip);
        } else if (node instanceof LeafSetNode<?> leafSet && pathArg instanceof NodeWithValue<?> nwv) {
            child = leafSet.childByArg(nwv);
        } else {
            // Anything else, including ValueNode
            child = null;
        }
        return Optional.ofNullable(child);
    }

    /**
     * Convert a data subtree under a node into a human-readable string format.
     *
     * @param node Data subtree root
     * @return String containing a human-readable form of the subtree.
     */
    public static String toStringTree(final NormalizedNode node) {
        final StringBuilder sb = new StringBuilder();
        toStringTree(sb, node, 0);
        return sb.toString();
    }

    private static void toStringTree(final StringBuilder sb, final NormalizedNode node, final int offset) {
        final String prefix = " ".repeat(offset);
        appendPathArgument(sb.append(prefix), node.name());
        if (node instanceof NormalizedNodeContainer<?> container) {
            sb.append(" {\n");
            for (var child : container.body()) {
                toStringTree(sb, child, offset + STRINGTREE_INDENT);
            }
            sb.append(prefix).append('}');
        } else {
            sb.append(' ').append(node.body());
        }
        sb.append('\n');
    }

    private static void appendPathArgument(final StringBuilder sb, final PathArgument arg) {
        sb.append(arg.getNodeType().getLocalName());
        if (arg instanceof NodeIdentifierWithPredicates nip) {
            sb.append(nip.values());
        }
    }
}
