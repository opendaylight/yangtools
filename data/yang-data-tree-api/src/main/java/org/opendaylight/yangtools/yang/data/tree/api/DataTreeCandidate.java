/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.api;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.base.VerifyException;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * An encapsulation of a validated data tree modification. This candidate is ready for atomic commit to the datastore.
 * It allows access to before- and after-state as it will be seen in to subsequent commit. This capture can be accessed
 * for reference, but cannot be modified and the content is limited to nodes which were affected by the modification
 * from which this instance originated.
 */
@NonNullByDefault
public interface DataTreeCandidate {
    /**
     * Get the candidate tree root node.
     *
     * @return Candidate tree root node
     */
    CandidateNode getRootNode();

    /**
     * Get the candidate tree root path. This is the path of the root node
     * relative to the root of InstanceIdentifier namespace.
     *
     * @return Relative path of the root node
     */
    YangInstanceIdentifier getRootPath();

    /**
     * {@inheritDoc}
     *
     * {@link DataTreeCandidate} implementations must not override the default identity hashCode method.
     */
    @Override
    int hashCode();

    /**
     * {@inheritDoc}
     *
     * {@link DataTreeCandidate} implementations must not override the default identity hashCode method, meaning their
     * equals implementation must result in identity comparison.
     */
    @Override
    boolean equals(@Nullable Object obj);

    /**
     * A single node within a {@link DataTreeCandidate}. The nodes are organized in tree hierarchy, reflecting
     * the modification from which this candidate was created. The node itself exposes the before- and after-image
     * of the tree restricted to the modified nodes.
     */
    sealed interface CandidateNode {
        /**
         * Get the node underlying {@link NormalizedNode#name()}.
         *
         * @return The node identifier.
         */
        PathArgument name();

        /**
         * Return the type of modification this node is undergoing.
         *
         * @return Node modification type.
         * @deprecated  This method exists only to express the ties to {@link ModificationType} and should not be used.
         */
        @Deprecated(since = "14.0.0", forRemoval = true)
        ModificationType modificationType();

        non-sealed interface Appeared extends WithDataAfter, WithChildren {
            @Override
            @Deprecated(since = "14.0.0", forRemoval = true)
            default ModificationType modificationType() {
                return ModificationType.APPEARED;
            }
        }

        non-sealed interface Created extends WithDataAfter {
            @Override
            @Deprecated(since = "14.0.0", forRemoval = true)
            default ModificationType modificationType() {
                return ModificationType.WRITE;
            }
        }

        non-sealed interface Deleted extends WithoutDataAfter {
            @Override
            @Deprecated(since = "14.0.0", forRemoval = true)
            default ModificationType modificationType() {
                return ModificationType.DELETE;
            }
        }

        non-sealed interface Disappeared extends WithoutDataAfter, WithChildren {
            @Override
            @Deprecated(since = "14.0.0", forRemoval = true)
            default ModificationType modificationType() {
                return ModificationType.DISAPPEARED;
            }
        }

        non-sealed interface Modified extends WithDataBefore, WithDataAfter, WithChildren {
            @Override
            @Deprecated(since = "14.0.0", forRemoval = true)
            default ModificationType modificationType() {
                return ModificationType.SUBTREE_MODIFIED;
            }
        }

        non-sealed interface Replaced extends WithDataBefore, WithDataAfter {
            @Override
            @Deprecated(since = "14.0.0", forRemoval = true)
            default ModificationType modificationType() {
                return ModificationType.WRITE;
            }
        }

        /**
         * The node has not been modified.
         */
        non-sealed interface Unmodified extends WithDataAfter {
            @Override
            @Deprecated(since = "14.0.0", forRemoval = true)
            default ModificationType modificationType() {
                return ModificationType.UNMODIFIED;
            }
        }

        /**
         * Intermediate helper trait guaranteeing {@link #dataBefore()} being non-null.
         */
        sealed interface WithDataBefore {
            /**
             * Return the before-image of data corresponding to the node.
             *
             * @return Node data as they were present in the tree before the modification was applied.
             */
            NormalizedNode dataBefore();
        }

        /**
         * Intermediate {@link CandidateNode} indicating the node will exist.
         */
        sealed interface WithDataAfter extends CandidateNode {
            /**
             * Return the after-image of data corresponding to the node.
             *
             * @return Node data as they will be present in the tree after the modification is applied
             */
            NormalizedNode dataAfter();

            @Override
            default PathArgument name() {
                return dataAfter().name();
            }
        }

        /**
         * Intermediate {@link CandidateNode} indicating the node will cease to exist.
         */
        sealed interface WithoutDataAfter extends CandidateNode, WithDataBefore {
            @Override
            default PathArgument name() {
                return dataBefore().name();
            }
        }

        /**
         * A non-terminal {@link CandidateNode} which has some child modifications dictating this node's
         * modification type. Examples include
         */
        sealed interface WithChildren {
            /**
             * Get an unmodifiable collection of modified child nodes. Note that the collection may include
             * {@link ModificationType#UNMODIFIED} nodes, which the caller is expected to handle as if they were not
             * present.
             *
             * @return Unmodifiable collection of modified child nodes.
             */
            Collection<CandidateNode> children();

            /**
             * Returns modified child or empty. Note that this method may return an {@link Unmodified} node when there
             * is evidence of the node or its parent being involved in modification which has turned out not to modify
             * the node's contents.
             *
             * @param arg {@link PathArgument} of child node
             * @return Modified child or {@code null} if the specified child has not been modified
             * @throws NullPointerException if {@code childNamez} is {@code null}
             */
            @Nullable CandidateNode modifiedChild(PathArgument arg);

            /**
             * Returns modified child or empty. Note that this method may return an {@link Unmodified} node when there
             * is evidence of the node or its parent being involved in modification which has turned out not to modify
             * the node's contents.
             *
             * @implSpec Default implementation defers to {@link Optional#ofNullable(Object)} based on
             *           {@link #modifiedChild(PathArgument)}.
             * @param childName Identifier of child node
             * @return Modified child or empty.
             * @throws NullPointerException if {@code childIdentifier} is {@code null}
             */
            default Optional<CandidateNode> findModifiedChild(final PathArgument childName) {
                return toOptional(modifiedChild(childName));
            }

            /**
             * Returns modified child or empty. Note that this method may return an {@link Unmodified} node when there
             * is evidence of the node or its parent being involved in modification which has turned out not to modify
             * the node's contents.
             *
             * @implSpec Default implementation defers to {@link #modifiedChild(PathArgument)}.
             * @param childName Identifier of child node
             * @return Modified child
             * @throws NullPointerException if {@code childName} is {@code null}
             * @throws VerifyException if no modified child with specified name is found
             */
            default CandidateNode getModifiedChild(final PathArgument childName) {
                return verifyNotNull(modifiedChild(childName), "No modified child named %s", childName);
            }

            // Helper to keep JDT null type analysis happy
            private static <T> Optional<T> toOptional(final @Nullable T obj) {
                return Optional.ofNullable(obj);
            }
        }
    }
}
