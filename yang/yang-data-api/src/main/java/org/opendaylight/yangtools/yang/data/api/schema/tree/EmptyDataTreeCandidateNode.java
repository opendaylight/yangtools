/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

final class EmptyDataTreeCandidateNode implements DataTreeCandidateNode {

    private final PathArgument identifier;

    EmptyDataTreeCandidateNode(final PathArgument identifier) {
        this.identifier = Preconditions.checkNotNull(identifier, "Identifier should not be null");
    }

    @Nonnull
    @Override
    public PathArgument getIdentifier() {
        return identifier;
    }

    @Nonnull
    @Override
    public Collection<DataTreeCandidateNode> getChildNodes() {
        return ImmutableList.of();
    }

    @Nullable
    @Override
    public DataTreeCandidateNode getModifiedChild(final PathArgument identifier) {
        return null;
    }

    @Nonnull
    @Override
    public ModificationType getModificationType() {
        return ModificationType.UNMODIFIED;
    }

    @Nonnull
    @Override
    public Optional<NormalizedNode<?, ?>> getDataAfter() {
        return Optional.absent();
    }

    @Nonnull
    @Override
    public Optional<NormalizedNode<?, ?>> getDataBefore() {
        return Optional.absent();
    }
}