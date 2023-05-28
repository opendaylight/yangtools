/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.api;

import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * A single node within a {@link DataTreeCandidate}. The nodes are organized in tree hierarchy, reflecting
 * the modification from which this candidate was created. The node itself exposes the before- and after-image
 * of the tree restricted to the modified nodes.
 */
public interface DataTreeCandidateNode {
    /**
     * Get the node underlying {@link NormalizedNode#name()}.
     *
     * @return The node identifier.
     */
    @NonNull PathArgument name();

    /**
     * Get the node identifier.
     *
     * @implSpec Default implementation defers to {@link #name()}.
     * @return The node identifier.
     * @deprecated Use {@link #name()} instead.
     */
    @Deprecated(since = "11.0.0", forRemoval = true)
    default @NonNull PathArgument getIdentifier() {
        return name();
    }

    /**
     * Get an unmodifiable collection of modified child nodes. Note that the collection may include
     * {@link ModificationType#UNMODIFIED} nodes, which the caller is expected to handle as if they were not present.
     *
     * @return Unmodifiable collection of modified child nodes.
     */
    @NonNull Collection<DataTreeCandidateNode> childNodes();

    /**
     * Get an unmodifiable collection of modified child nodes. Note that the collection may include
     * {@link ModificationType#UNMODIFIED} nodes, which the caller is expected to handle as if they were not present.
     *
     * @implSpec Default implementation defers to {@link #childNodes()}.
     * @return Unmodifiable collection of modified child nodes.
     * @deprecated Use {@link #childNodes()} instead.
     */
    @Deprecated(since = "11.0.0", forRemoval = true)
    default @NonNull Collection<DataTreeCandidateNode> getChildNodes() {
        return childNodes();
    }

    /**
     * Returns modified child or empty. Note that this method may return an {@link ModificationType#UNMODIFIED} node
     * when there is evidence of the node or its parent being involved in modification which has turned out not to
     * modify the node's contents.
     *
     * @param childName Identifier of child node
     * @return Modified child or {@code null} if the specified child has not been modified
     * @throws NullPointerException if {@code childNamez} is {@code null}
     */
    @Nullable DataTreeCandidateNode modifiedChild(PathArgument childName);

    /**
     * Returns modified child or empty. Note that this method may return an {@link ModificationType#UNMODIFIED} node
     * when there is evidence of the node or its parent being involved in modification which has turned out not to
     * modify the node's contents.
     *
     * @implSpec Default implementation defers to {@link Optional#ofNullable(Object)} based on
     *           {@link #modifiedChild(PathArgument)}.
     * @param childName Identifier of child node
     * @return Modified child or empty.
     * @throws NullPointerException if {@code childIdentifier} is {@code null}
     */
    default @NonNull Optional<DataTreeCandidateNode> findModifiedChild(final PathArgument childName) {
        return Optional.ofNullable(modifiedChild(childName));
    }

    /**
     * Returns modified child or empty. Note that this method may return an {@link ModificationType#UNMODIFIED} node
     * when there is evidence of the node or its parent being involved in modification which has turned out not to
     * modify the node's contents.
     *
     * @implSpec Default implementation defers to {@link #findModifiedChild(PathArgument)}.
     * @param childName Identifier of child node
     * @return Modified child or empty.
     * @throws NullPointerException if {@code childIdentifier} is {@code null}
     */
    default @NonNull Optional<DataTreeCandidateNode> getModifiedChild(final PathArgument childName) {
        return findModifiedChild(childName);
    }

    /**
     * Return the type of modification this node is undergoing.
     *
     * @return Node modification type.
     */
    @NonNull ModificationType modificationType();

    /**
     * Return the type of modification this node is undergoing.
     *
     * @implSpec Default implementation defers to {@link #modificationType()}.
     * @return Node modification type.
     * @deprecated Use {@link #modificationType()} instead.
     */
    @Deprecated(since = "11.0.0", forRemoval = true)
    default @NonNull ModificationType getModificationType() {
        return modificationType();
    }

    /**
     * Return the before-image of data corresponding to the node.
     *
     * @return Node data as they were present in the tree before the modification was applied.
     */
    @Nullable NormalizedNode dataBefore();

    /**
     * Return the before-image of data corresponding to the node.
     *
     * @implSpec Default implementation defers to {@link Optional#ofNullable(Object)} based on {@link #dataBefore()}.
     * @return Node data as they were present in the tree before the modification was applied, or empty.
     */
    default @NonNull Optional<NormalizedNode> findDataBefore() {
        return Optional.ofNullable(dataBefore());
    }

    /**
     * Return the before-image of data corresponding to the node.
     *
     * @implSpec Default implementation defers to {@link #findDataBefore()}.
     * @return Node data as they were present in the tree before the modification was applied.
     * @deprecated Use {@link #dataBefore()} or {@link #findDataBefore()} instead.
     */
    @Deprecated(since = "11.0.0", forRemoval = true)
    default @NonNull Optional<NormalizedNode> getDataBefore() {
        return findDataBefore();
    }

    /**
     * Return the after-image of data corresponding to the node.
     *
     * @return Node data as they will be present in the tree after the modification is applied
     */
    @Nullable NormalizedNode dataAfter();

    /**
     * Return the after-image of data corresponding to the node.
     *
     * @implSpec Default implementation defers to {@link Optional#ofNullable(Object)} based on {@link #dataAfter()}.
     * @return Node data as they will be present in the tree after the modification is applied, or empty
     */
    default @NonNull Optional<NormalizedNode> findDataAfter() {
        return Optional.ofNullable(dataAfter());
    }

    /**
     * Return the after-image of data corresponding to the node.
     *
     * @implSpec Default implementation defers to {@link #findDataAfter()}.
     * @return Node data as they will be present in the tree after the modification is applied.
     * @deprecated Use {@link #dataAfter()} or {@link #findDataAfter()} instead.
     */
    @Deprecated(since = "11.0.0", forRemoval = true)
    default @NonNull Optional<NormalizedNode> getDataAfter() {
        return findDataAfter();
    }
}
