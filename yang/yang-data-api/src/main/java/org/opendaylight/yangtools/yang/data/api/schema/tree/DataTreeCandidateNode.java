/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * A single node within a {@link DataTreeCandidate}. The nodes are organized in tree hierarchy, reflecting
 * the modification from which this candidate was created. The node itself exposes the before- and after-image
 * of the tree restricted to the modified nodes.
 */
// FIXME: 4.0.0: Use @NonNullByDefault
public interface DataTreeCandidateNode {
    /**
     * Get the node identifier.
     *
     * @return The node identifier.
     */
    @NonNull PathArgument getIdentifier();

    /**
     * Get an unmodifiable collection of modified child nodes. Note that the collection may include
     * {@link ModificationType#UNMODIFIED} nodes, which the caller is expected to handle as if they were not present.
     *
     * @return Unmodifiable collection of modified child nodes.
     */
    @NonNull Collection<DataTreeCandidateNode> getChildNodes();

    /**
     * Returns modified child or empty. Note that this method may return an {@link ModificationType#UNMODIFIED} node
     * when there is evidence of the node or its parent being involved in modification which has turned out not to
     * modify the node's contents.
     *
     * @param childIdentifier Identifier of child node
     * @return Modified child or empty.
     * @throws NullPointerException if {@code childIdentifier} is null
     */
    @NonNull Optional<DataTreeCandidateNode> getModifiedChild(PathArgument childIdentifier);

    /**
     * Return the type of modification this node is undergoing.
     *
     * @return Node modification type.
     */
    @NonNull ModificationType getModificationType();

    /**
     * Return the after-image of data corresponding to the node.
     *
     * @return Node data as they will be present in the tree after
     *         the modification is applied.
     */
    @NonNull Optional<NormalizedNode<?, ?>> getDataAfter();

    /**
     * Return the before-image of data corresponding to the node.
     *
     * @return Node data as they were present in the tree before
     *         the modification was applied.
     */
    @NonNull Optional<NormalizedNode<?, ?>> getDataBefore();
}
