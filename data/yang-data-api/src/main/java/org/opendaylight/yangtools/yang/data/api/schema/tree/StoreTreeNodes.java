/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import static com.google.common.base.Verify.verify;

import com.google.common.annotations.Beta;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * A set of utility methods for interacting with {@link StoreTreeNode} objects.
 */
@Beta
public final class StoreTreeNodes {
    private StoreTreeNodes() {
        // Hidden on purpose
    }

    /**
     * Finds a node in tree.
     *
     * @param <T>
     *          Store tree node type.
     * @param tree Data Tree
     * @param path Path to the node
     * @return Optional with node if the node is present in tree, {@link Optional#empty()} otherwise.
     */
    public static <T extends StoreTreeNode<T>> Optional<? extends T> findNode(final T tree,
            final YangInstanceIdentifier path) {
        T current = tree;
        Iterator<PathArgument> pathIter = path.getPathArguments().iterator();
        while (current != null && pathIter.hasNext()) {
            current = current.childByArg(pathIter.next());
        }
        return Optional.ofNullable(current);
    }

    public static <T extends StoreTreeNode<T>> T findNodeChecked(final T tree, final YangInstanceIdentifier path) {
        T current = tree;

        int depth = 1;
        for (PathArgument pathArg : path.getPathArguments()) {
            current = current.childByArg(pathArg);
            if (current == null) {
                throw new IllegalArgumentException(String.format("Child %s is not present in tree.",
                        path.getAncestor(depth)));
            }
            ++depth;
        }
        return current;
    }

    /**
     * Finds a node or closest parent in the tree.
     *
     * @param <T>
     *          Store tree node type.
     * @param tree Data Tree
     * @param path Path to the node
     * @return Map.Entry Entry with key which is path to closest parent and value is parent node.
     */
    public static <T extends StoreTreeNode<T>> Entry<YangInstanceIdentifier, T> findClosest(final T tree,
            final YangInstanceIdentifier path) {
        return findClosestsOrFirstMatch(tree, path, input -> false);
    }

    public static <T extends StoreTreeNode<T>> Entry<YangInstanceIdentifier, T> findClosestsOrFirstMatch(final T tree,
            final YangInstanceIdentifier path, final Predicate<T> predicate) {
        T parent = tree;
        T current = tree;

        int nesting = 0;
        Iterator<PathArgument> pathIter = path.getPathArguments().iterator();
        while (current != null && pathIter.hasNext() && !predicate.test(current)) {
            parent = current;
            current = current.childByArg(pathIter.next());
            nesting++;
        }
        if (current != null) {
            final YangInstanceIdentifier currentPath = path.getAncestor(nesting);
            return new SimpleImmutableEntry<>(currentPath, current);
        }

        /*
         * Subtracting 1 from nesting level at this point is safe, because we
         * cannot reach here with nesting == 0: that would mean the above check
         * for current.isPresent() failed, which it cannot, as current is always
         * present. At any rate we verify state just to be on the safe side.
         */
        verify(nesting > 0);
        return new SimpleImmutableEntry<>(path.getAncestor(nesting - 1), parent);
    }
}
