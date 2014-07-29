/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.Map;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.tree.StoreTreeNode;

/**
 * A set of utility methods for interacting with {@link org.opendaylight.controller.md.sal.dom.store.impl.tree.spi.TreeNode} objects.
 */
public final class TreeNodeUtils {
    private TreeNodeUtils() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    /**
     * Finds a node in tree
     *
     * @param tree Data Tree
     * @param path Path to the node
     * @return Optional with node if the node is present in tree, {@link Optional#absent()} otherwise.
     */
    public static <T extends StoreTreeNode<T>> Optional<T> findNode(final T tree, final YangInstanceIdentifier path) {
        Optional<T> current = Optional.<T> of(tree);
        Iterator<PathArgument> pathIter = path.getPathArguments().iterator();
        while (current.isPresent() && pathIter.hasNext()) {
            current = current.get().getChild(pathIter.next());
        }
        return current;
    }

    public static <T extends StoreTreeNode<T>> T findNodeChecked(final T tree, final YangInstanceIdentifier path) {
        T current = tree;

        int i = 1;
        for(PathArgument pathArg : path.getPathArguments()) {
            Optional<T> potential = current.getChild(pathArg);
            if (!potential.isPresent()) {
                throw new IllegalArgumentException(String.format("Child %s is not present in tree.",
                        Iterables.toString(Iterables.limit(path.getPathArguments(), i))));
            }
            current = potential.get();
            ++i;
        }
        return current;
    }

    /**
     * Finds a node or closest parent in  the tree
     *
     * @param tree Data Tree
     * @param path Path to the node
     * @return Map.Entry Entry with key which is path to closest parent and value is parent node.
     *
     */
    public static <T extends StoreTreeNode<T>> Map.Entry<YangInstanceIdentifier, T> findClosest(final T tree, final YangInstanceIdentifier path) {
        return findClosestsOrFirstMatch(tree, path, Predicates.<T>alwaysFalse());
    }

    public static <T extends StoreTreeNode<T>> Map.Entry<YangInstanceIdentifier, T> findClosestsOrFirstMatch(final T tree, final YangInstanceIdentifier path, final Predicate<T> predicate) {
        Optional<T> parent = Optional.<T>of(tree);
        Optional<T> current = Optional.<T> of(tree);

        int nesting = 0;
        Iterator<PathArgument> pathIter = path.getPathArguments().iterator();
        while (current.isPresent() && pathIter.hasNext() && !predicate.apply(current.get())) {
            parent = current;
            current = current.get().getChild(pathIter.next());
            nesting++;
        }
        if(current.isPresent()) {
            final YangInstanceIdentifier currentPath = YangInstanceIdentifier.create(path.getPath().subList(0, nesting));
            return new SimpleEntry<YangInstanceIdentifier,T>(currentPath,current.get());
        }

        /*
         * Subtracting 1 from nesting level at this point is safe, because we
         * cannot reach here with nesting == 0: that would mean the above check
         * for current.isPresent() failed, which it cannot, as current is always
         * present. At any rate we check state just to be on the safe side.
         */
        Preconditions.checkState(nesting > 0);
        final YangInstanceIdentifier parentPath = YangInstanceIdentifier.create(path.getPath().subList(0, nesting - 1));

        return new SimpleEntry<YangInstanceIdentifier,T>(parentPath,parent.get());
    }

    public static <T extends StoreTreeNode<T>> Optional<T> getChild(final Optional<T> parent,final PathArgument child) {
        if(parent.isPresent()) {
            return parent.get().getChild(child);
        }
        return Optional.absent();
    }

}
