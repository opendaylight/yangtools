/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;

abstract class UniqueTreeNodeSupport<T> implements Immutable {
    private static final class One extends UniqueTreeNodeSupport<UniqueValidator<?>> {
        One(final UniqueValidator<?> validator) {
            super(validator);
        }

        @Override
        Object extractValues(final DataContainerNode data) {
            return validators.extractValues(data);
        }

        private Object extractValues(final TreeNode child) {
            return child instanceof UniqueVectorTreeNode uniqueVector ? uniqueVector.vector()
                : extractValues((DataContainerNode) child.getData());
        }

        @Override
        void addToIndex(final Map<UniqueValidator<?>, Map<Object, TreeNode>> index, final TreeNode child) {
            final Object vector = extractValues(child);
            final TreeNode prev = index.get(validators).putIfAbsent(vector, child);
            if (prev != null) {
                throw new UniqueValidationFailedException(String.format(
                    "Detected unique constraint violation on between %s and %s", vector, prev.getIdentifier(),
                    child.getIdentifier()));
            }
        }

        @Override
        void removeFromIndex(final Map<UniqueValidator<?>, Map<Object, TreeNode>> index, final TreeNode child) {
            index.get(validators).remove(extractValues(child), child);
        }
    }

    private static final class Many extends UniqueTreeNodeSupport<ImmutableList<UniqueValidator<?>>> {
        Many(final ImmutableList<UniqueValidator<?>> validators) {
            super(validators);
        }

        @Override
        Map<UniqueValidator<?>, Object> extractValues(final DataContainerNode data) {
            return validators.stream()
                .collect(Collectors.toMap(Function.identity(), validator -> validator.extractValues(data)));
        }

        private Map<UniqueValidator<?>, Object> extractValues(final TreeNode child) {
            return child instanceof UniqueVectorTreeNode uniqueVector
                ? (Map<UniqueValidator<?>, Object>) uniqueVector.vector()
                : extractValues((DataContainerNode) child.getData());
        }

        @Override
        void addToIndex(final Map<UniqueValidator<?>, Map<Object, TreeNode>> index, final TreeNode child) {
            for (Entry<UniqueValidator<?>, Object> entry : extractValues(child).entrySet()) {
                final TreeNode prev = index.get(entry.getKey()).putIfAbsent(entry.getValue(), child);
                if (prev != null) {
                    throw new UniqueValidationFailedException(String.format(
                        "Detected unique constraint violation on between %s and %s", entry.getValue(),
                        prev.getIdentifier(), child.getIdentifier()));
                }
            }
        }

        @Override
        void removeFromIndex(final Map<UniqueValidator<?>, Map<Object, TreeNode>> index, final TreeNode child) {
            for (Entry<UniqueValidator<?>, Object> entry : extractValues(child).entrySet()) {
                index.get(entry.getKey()).remove(entry.getValue(), child);
            }
        }
    }

    private static final MapAdaptor MAPS = MapAdaptor.getDefaultInstance();

    final T validators;

    UniqueTreeNodeSupport(final T validators) {
        this.validators = requireNonNull(validators);
    }

    static UniqueTreeNodeSupport<?> of(final ImmutableList<UniqueValidator<?>> validators) {
        return validators.size() == 1 ? new One(validators.get(0)) : new Many(validators);
    }

    private final UniqueIndexMutableNode newMutableTreeNode(final UniqueIndexTreeNode immutable) {
        return new UniqueIndexMutableNode(immutable.delegate().mutable(), this, immutable);
    }

    abstract Object extractValues(DataContainerNode data);

    abstract void addToIndex(Map<UniqueValidator<?>, Map<Object, TreeNode>> index, TreeNode node);

    abstract void removeFromIndex(Map<UniqueValidator<?>, Map<Object, TreeNode>> index, TreeNode node);

    final @NonNull UniqueIndexTreeNode seal(final TreeNode delegate, final UniqueIndexTreeNode prev,
            final Map<TreeNode, Boolean> delta) {
        // Take a deep snapshot of previous index
        final Map<UniqueValidator<?>, Map<Object, TreeNode>> index = MAPS.takeSnapshot(prev.index());
        for (Entry<UniqueValidator<?>, Map<Object, TreeNode>> entry : index.entrySet()) {
            entry.setValue(MAPS.takeSnapshot(entry.getValue()));
        }

        /*
         * We perform two passes on the nodes: first remove entries, purging old mappings then add new entries. This
         * prevents conflicts between removed and added data.
         */
        final Set<Entry<TreeNode, Boolean>> entries = delta.entrySet();
        for (Entry<TreeNode, Boolean> entry : entries) {
            if (!entry.getValue()) {
                removeFromIndex(index, entry.getKey());
            }
        }
        for (Entry<TreeNode, Boolean> entry : entries) {
            if (entry.getValue()) {
                addToIndex(index, entry.getKey());
            }
        }

        // Seal back to normal and return parent
        for (Entry<UniqueValidator<?>, Map<Object, TreeNode>> entry : index.entrySet()) {
            entry.setValue(MAPS.optimize(entry.getValue()));
        }
        return new UniqueIndexTreeNode(delegate, MAPS.optimize(index));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("validators", validators).toString();
    }

    Object extractValues(final TreeNode node, final Object prevVector) {
        // FIXME: extract values and compare with previous vector. If no update is made, return the previous vector
        return null;
    }
}
