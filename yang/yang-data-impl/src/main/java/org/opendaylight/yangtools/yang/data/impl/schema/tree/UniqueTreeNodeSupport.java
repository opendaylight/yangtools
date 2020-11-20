/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.util.MapAdaptor;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;

abstract class UniqueTreeNodeSupport<T> implements Immutable {
    private static final class One extends UniqueTreeNodeSupport<UniqueValidator<?>> {
        One(final UniqueValidator<?> validator) {
            super(validator);
        }

        @Override
        Map<UniqueValidator<?>, Multimap<Object, TreeNode>> allocateDelta() {
            return new HashMap<>(2);
        }

        @Override
        UniqueParentTreeNode newTreeNode(final MapNode value, final Version version) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        UniqueParentTreeNode newTreeNode(final UnkeyedListNode value, final Version version) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        Object extractValues(final DataContainerNode<?> data) {
            return validators.extractValues(data);
        }

        @Override
        void removeChild(final Map<UniqueValidator<?>, Multimap<Object, TreeNode>> delta, final TreeNode child) {
            removeChild(validators, delta, child);
        }
    }

    private static final class Many extends UniqueTreeNodeSupport<ImmutableList<UniqueValidator<?>>> {
        Many(final ImmutableList<UniqueValidator<?>> validators) {
            super(validators);
        }

        @Override
        Map<UniqueValidator<?>, Multimap<Object, TreeNode>> allocateDelta() {
            return Maps.newHashMapWithExpectedSize(validators.size());
        }

        @Override
        UniqueParentTreeNode newTreeNode(final MapNode value, final Version version) {
            final Map<UniqueValidator<?>, Map<Object, TreeNode>> vectorToChild =
                Maps.newHashMapWithExpectedSize(validators.size());

            final Collection<MapEntryNode> children = value.getValue();
            for (UniqueValidator<?> validator : validators) {
                vectorToChild.put(validator, indexChildren(validator, children));
            }

            return new UniqueParentTreeNode(TreeNodeFactory.createTreeNode(value, version), vectorToChild);
        }

        @Override
        UniqueParentTreeNode newTreeNode(final UnkeyedListNode value, final Version version) {
            final Map<UniqueValidator<?>, Map<Object, TreeNode>> vectorToChild =
                Maps.newHashMapWithExpectedSize(validators.size());

            // FIXME: fill value

            return new UniqueParentTreeNode(TreeNodeFactory.createTreeNode(value, version), vectorToChild);
        }


        private static Map<Object, TreeNode> indexChildren(final UniqueValidator<?> validator,
                final Collection<MapEntryNode> children) {
            final Map<Object, TreeNode> ret = MAPS.initialSnapshot(children.size());
            for (MapEntryNode child : children) {
                ret.put(validator.extractValues(child), value)


                // FIXME: fill value
            }
            return ret;
        }

        @Override
        Object extractValues(final DataContainerNode<?> data) {
            return validators.stream()
                .collect(Collectors.toMap(Function.identity(), validator -> validator.extractValues(data)));
        }

        @Override
        void removeChild(final Map<UniqueValidator<?>, Multimap<Object, TreeNode>> delta, final TreeNode child) {
            validators.forEach(validator -> removeChild(validator, delta, child));
        }
    }

    // FIXME: Reuse whatever :)
    // FIXME: this should be used only for inner maps
    private static final MapAdaptor MAPS = MapAdaptor.getDefaultInstance();

    final T validators;

    UniqueTreeNodeSupport(final T validators) {
        this.validators = requireNonNull(validators);
    }

    static UniqueTreeNodeSupport<?> of(final ImmutableList<UniqueValidator<?>> validators) {
        return validators.size() == 1 ? new One(validators.get(0)) : new Many(validators);
    }

    final UniqueParentMutableNode newMutableTreeNode(final MapNode value, final Version version) {
        return newMutableTreeNode(newTreeNode(value, version));
    }

    final UniqueParentMutableNode newMutableTreeNode(final UnkeyedListNode value, final Version version) {
        return newMutableTreeNode(newTreeNode(value, version));
    }

    private final UniqueParentMutableNode newMutableTreeNode(final UniqueParentTreeNode immutable) {
        return new UniqueParentMutableNode(immutable.delegate().mutable(), this, immutable);
    }

    abstract UniqueParentTreeNode newTreeNode(MapNode value, Version version);

    abstract UniqueParentTreeNode newTreeNode(UnkeyedListNode value, Version version);

    abstract Map<UniqueValidator<?>, Multimap<Object, TreeNode>> allocateDelta();

    abstract Object extractValues(DataContainerNode<?> data);

    abstract void removeChild(Map<UniqueValidator<?>, Multimap<Object, TreeNode>> delta, TreeNode child);

    private static void removeChild(final UniqueValidator<?> validator,
            final Map<UniqueValidator<?>, Multimap<Object, TreeNode>> delta, final TreeNode child) {
        delta.computeIfAbsent(validator, key -> HashMultimap.create()).put(
            ((UniqueChildTreeNode) child).vector(), null);
    }

    final @NonNull UniqueParentTreeNode seal(final TreeNode delegate, final UniqueParentTreeNode prev,
            final Map<UniqueValidator<?>, Multimap<Object, TreeNode>> vectorToChild) {
        // FIXME: this needs use MAPS
        return new UniqueParentTreeNode(delegate, Maps.transformEntries(vectorToChild,
            (key, value) -> Maps.transformEntries(value.asMap(),
                (vector, child) -> {
                    final Iterator<TreeNode> it = child.iterator();
                    final TreeNode result = it.next();
                    if (it.hasNext()) {
                        // FIXME: pull in validator
                        throw new UniqueValidationFailedException(
                            "Conflict on " + key + " between " + result + " and " + it.next());
                    }
                    return result;
                })));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("validators", validators).toString();
    }
}
