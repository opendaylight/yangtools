/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;

final class RecursiveUnmodifiedCandidateNode
        <T extends DistinctNodeContainer<PathArgument, NormalizedNode> & NormalizedNode>
        extends AbstractRecursiveCandidateNode<T> {
    RecursiveUnmodifiedCandidateNode(final T data) {
        super(data);
    }

    @Override
    public ModificationType getModificationType() {
        return ModificationType.UNMODIFIED;
    }

    @Override
    public Optional<? extends NormalizedNode> getDataAfter() {
        return dataOptional();
    }

    @Override
    public Optional<? extends NormalizedNode> getDataBefore() {
        return dataOptional();
    }

    @Override
    DataTreeCandidateNode createContainer(final DistinctNodeContainer<PathArgument, NormalizedNode> childData) {
        return new RecursiveUnmodifiedCandidateNode(childData);
    }

    @Override
    DataTreeCandidateNode createLeaf(final NormalizedNode childData) {
        return new UnmodifiedLeafCandidateNode(childData);
    }
}
