/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

final class RecursiveWriteCandidateNode extends AbstractRecursiveCandidateNode {
    RecursiveWriteCandidateNode(final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> data) {
        super(data);
    }

    @Override
    public ModificationType getModificationType() {
        return ModificationType.WRITE;
    }

    @Override
    public Optional<NormalizedNode<?, ?>> getDataAfter() {
        return dataOptional();
    }

    @Override
    public Optional<NormalizedNode<?, ?>> getDataBefore() {
        return Optional.empty();
    }

    @Override
    DataTreeCandidateNode createContainer(
            final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> childData) {
        return new RecursiveWriteCandidateNode(childData);
    }

    @Override
    DataTreeCandidateNode createLeaf(final NormalizedNode<?, ?> childData) {
        return new WriteLeafCandidateNode(childData);
    }
}
