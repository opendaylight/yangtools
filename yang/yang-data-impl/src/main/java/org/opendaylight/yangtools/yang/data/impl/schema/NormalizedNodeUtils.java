/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

import com.google.common.base.Optional;

public final class NormalizedNodeUtils {
    private NormalizedNodeUtils() {
        throw new UnsupportedOperationException("Utilities class should not be instantiated");
    }

    public static Optional<NormalizedNode<?, ?>> findNode(final InstanceIdentifier rootPath, final NormalizedNode<?, ?> rootNode, final InstanceIdentifier childPath) {
        final Optional<InstanceIdentifier> relativePath = childPath.relativeTo(rootPath);
        if (relativePath.isPresent()) {
            return findNode(rootNode, relativePath.get());
        } else {
            return Optional.absent();
        }
    }

    public static Optional<NormalizedNode<?, ?>> findNode(final NormalizedNode<?, ?> tree, final InstanceIdentifier path) {
        checkNotNull(tree, "Tree must not be null");
        checkNotNull(path, "Path must not be null");

        Optional<NormalizedNode<?, ?>> currentNode = Optional.<NormalizedNode<?, ?>> of(tree);
        final Iterator<PathArgument> pathIterator = path.getPathArguments().iterator();
        while (currentNode.isPresent() && pathIterator.hasNext()) {
            currentNode = getDirectChild(currentNode.get(), pathIterator.next());
        }
        return currentNode;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Optional<NormalizedNode<?, ?>> getDirectChild(final NormalizedNode<?, ?> node, final PathArgument pathArg) {
        if (node instanceof LeafNode<?> || node instanceof LeafSetEntryNode<?>) {
            return Optional.absent();
        } else if (node instanceof DataContainerNode<?>) {
            return (Optional) ((DataContainerNode<?>) node).getChild(pathArg);
        } else if (node instanceof MapNode && pathArg instanceof NodeIdentifierWithPredicates) {
            return (Optional) ((MapNode) node).getChild((NodeIdentifierWithPredicates) pathArg);
        } else if (node instanceof LeafSetNode<?>) {
            return (Optional) ((LeafSetNode<?>) node).getChild((NodeWithValue) pathArg);
        }
        return Optional.absent();
    }
}
