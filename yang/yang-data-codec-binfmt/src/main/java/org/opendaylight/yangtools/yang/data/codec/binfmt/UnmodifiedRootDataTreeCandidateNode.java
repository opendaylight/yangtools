/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import java.util.Collection;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;

final class UnmodifiedRootDataTreeCandidateNode extends AbstractDataTreeCandidateNode {
    static final UnmodifiedRootDataTreeCandidateNode INSTANCE = new UnmodifiedRootDataTreeCandidateNode();

    private UnmodifiedRootDataTreeCandidateNode() {
        super(ModificationType.UNMODIFIED);
    }

    @Override
    public PathArgument getIdentifier() {
        throw new UnsupportedOperationException("Root node does not have an identifier");
    }

    @Override
    public Optional<NormalizedNode<?, ?>> getDataAfter() {
        throw new UnsupportedOperationException("After-image not available after serialization");
    }

    @Override
    public Collection<DataTreeCandidateNode> getChildNodes() {
        throw new UnsupportedOperationException("Children not available after serialization");
    }
}