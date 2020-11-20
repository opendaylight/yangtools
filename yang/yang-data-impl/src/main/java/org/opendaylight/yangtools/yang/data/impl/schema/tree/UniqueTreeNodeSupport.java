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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;

final class UniqueTreeNodeSupport implements Immutable {
    private final ImmutableList<UniqueValidator<?>> validators;

    UniqueTreeNodeSupport(final ImmutableList<UniqueValidator<?>> validators) {
        this.validators = requireNonNull(validators);
    }

    UniqueParentTreeNode newTreeNode(final UnkeyedListNode value, final Version version) {
        final TreeNode delegate = TreeNodeFactory.createTreeNode(value, version);
        final Map<Object, TreeNode> vectorToChild = new HashMap<>();

        // FIXME: fill value, make it immutable


        return new UniqueParentTreeNode(delegate, vectorToChild);
    }

    void recursivelyVerifyStructure(final UnkeyedListNode value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("validators", validators).toString();
    }

    @NonNull UniqueParentTreeNode seal(final TreeNode delegate,
            final Map<Object, Collection<TreeNode>> vectorToChild) {
        return new UniqueParentTreeNode(delegate, Maps.transformValues(vectorToChild,
            // FIXME: okay, this is harsh: we need to throw a proper exception
            Iterables::getOnlyElement));
    }
}
