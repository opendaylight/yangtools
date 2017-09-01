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
import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;

/**
 * Internal utility class for an empty candidate. We instantiate this class
 * for empty modifications, saving memory and processing speed. Instances
 * of this class are explicitly recognized and processing of them is skipped.
 */
final class NoopDataTreeCandidate extends AbstractDataTreeCandidate {
    private static final DataTreeCandidateNode ROOT = new DataTreeCandidateNode() {
        @Override
        @Nonnull
        public ModificationType getModificationType() {
            return ModificationType.UNMODIFIED;
        }

        @Nonnull
        @Override
        public Collection<DataTreeCandidateNode> getChildNodes() {
            return Collections.emptyList();
        }

        @Override
        @Nonnull
        public PathArgument getIdentifier() {
            throw new IllegalStateException("Attempted to read identifier of the no-operation change");
        }

        @Override
        @Nonnull
        public Optional<NormalizedNode<?, ?>> getDataAfter() {
            return Optional.absent();
        }

        @Override
        @Nonnull
        public Optional<NormalizedNode<?, ?>> getDataBefore() {
            return Optional.absent();
        }

        @Override
        public DataTreeCandidateNode getModifiedChild(final PathArgument identifier) {
            return null;
        }
    };
    private final TreeNode afterRoot;

    protected NoopDataTreeCandidate(final YangInstanceIdentifier rootPath, final ModifiedNode modificationRoot,
            final TreeNode afterRoot) {
        super(rootPath);
        Preconditions.checkArgument(modificationRoot.getOperation() == LogicalOperation.NONE);
        this.afterRoot = Preconditions.checkNotNull(afterRoot);
    }

    @Override
    public DataTreeCandidateNode getRootNode() {
        return ROOT;
    }

    @Override
    @Nonnull
    protected TreeNode getTipRoot() {
        return afterRoot;
    }
}
