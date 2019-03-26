/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

final class EmptyDataTreeCandidateNode implements DataTreeCandidateNode {
    private final PathArgument identifier;

    EmptyDataTreeCandidateNode(final PathArgument identifier) {
        this.identifier = requireNonNull(identifier, "Identifier should not be null");
    }

    @Override
    public PathArgument getIdentifier() {
        return identifier;
    }

    @Override
    public Collection<DataTreeCandidateNode> getChildNodes() {
        return ImmutableList.of();
    }

    @Override
    public Optional<DataTreeCandidateNode> getModifiedChild(final PathArgument childIdentifier) {
        return Optional.empty();
    }

    @Override
    public ModificationType getModificationType() {
        return ModificationType.UNMODIFIED;
    }

    @Override
    public Optional<NormalizedNode<?, ?>> getDataAfter() {
        return Optional.empty();
    }

    @Override
    public Optional<NormalizedNode<?, ?>> getDataBefore() {
        return Optional.empty();
    }
}