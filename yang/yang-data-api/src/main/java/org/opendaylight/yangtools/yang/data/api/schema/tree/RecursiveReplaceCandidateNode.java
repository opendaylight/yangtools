/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

final class RecursiveReplaceCandidateNode extends AbstractDataTreeCandidateNode {
    private final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> oldData;

    RecursiveReplaceCandidateNode(final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> oldData,
            final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> newData) {
        super(newData);
        this.oldData = requireNonNull(oldData);
    }

    @Override
    public ModificationType getModificationType() {
        return ModificationType.WRITE;
    }

    @Override
    public Optional<NormalizedNode<?, ?>> getDataAfter() {
        return super.dataOptional();
    }

    @Override
    public Optional<NormalizedNode<?, ?>> getDataBefore() {
        return Optional.of(oldData);
    }

    @Override
    public DataTreeCandidateNode getModifiedChild(final PathArgument identifier) {
        return DataTreeCandidateNodes.containerDelta(oldData, getData(), identifier);
    }

    @Override
    public Collection<DataTreeCandidateNode> getChildNodes() {
        return DataTreeCandidateNodes.containerDelta(oldData, getData());
    }
}
