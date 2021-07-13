/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.AbstractList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A basic rope.
 *
 * @param <E> the type of elements in this rope
 */
public abstract class AbstractRope<E> extends AbstractList<E> {
    private static final class SplitResult<E> {
        private final RopeNode<E> leftNode;
        private final RopeNode<E> rightNode;

        SplitResult(final RopeNode<E> leftNode, final RopeNode<E> rightNode) {
            this.leftNode = requireNonNull(leftNode);
            this.rightNode = requireNonNull(rightNode);
        }
    }

    protected abstract static class RopeNode<E> {
        // as per https://en.wikipedia.org/wiki/Rope_(data_structure)#Description
        abstract int weight();

        // as per https://en.wikipedia.org/wiki/Rope_(data_structure)#Insert
        abstract @NonNull E index(int offset);

        // as per https://en.wikipedia.org/wiki/Rope_(data_structure)#Split
        abstract SplitResult<E> split(int splitPoint);

        // as per https://en.wikipedia.org/wiki/Rope_(data_structure)#Concat
        final RopeNode<E> concat(final RopeNode<E> other) {
            // we interpret 'this' as S1 and S2 to 'other'
            return new TrunkNode<>(this, other);
        }

        // i.e. the number of items stored
        abstract int size();
    }

    protected static final class LeafNode<E> extends RopeNode<E> {
        private final ImmutableList<E> values;

        LeafNode(final List<E> values) {
            this.values = ImmutableList.copyOf(values);
        }

        List<E> values() {
            return values;
        }

        @Override
        int weight() {
            return values().size();
        }

        @Override
        E index(final int offset) {
            return values().get(offset);
        }

        @Override
        int size() {
            return weight();
        }

        @Override
        SplitResult<E> split(final int splitPoint) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    protected static final class TrunkNode<T> extends RopeNode<T> {
        private final RopeNode<T> left;
        private final RopeNode<T> right;
        private final int leftWeight;

        private volatile int size;

        protected TrunkNode(final RopeNode<T> left, final RopeNode<T> right) {
            this.left = requireNonNull(left);
            this.right = right;
            this.leftWeight = left.weight();
        }

        @Override
        int weight() {
            return leftWeight;
        }

        @Override
        T index(final int offset) {
            // We do not have a right node or offset is contained in left, delegate to it
            return right == null || leftWeight > offset ? left.index(offset)
                // ... it has to lie in the right part
                : right.index(offset - leftWeight);
        }

        @Override
        int size() {
            int result = size;
            if (result == 0) {
                int tmp = left.size();
                if (right != null) {
                    tmp += right.size();
                }
                size = result = tmp;
            }
            return result;
        }

        @Override
        SplitResult<T> split(final int splitPoint) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    private static class ImmutableRope<T> extends AbstractRope<T> {
        private final RopeNode<T> root;

        ImmutableRope(final RopeNode<T> root) {
            this.root = requireNonNull(root);
        }

        @Override
        public T get(final int index) {
            return root.index(index);
        }

        @Override
        public int size() {
            return root.size();
        }
    }
}
