/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;

final class RecursiveReplaceCandidateNode extends AbstractDataTreeCandidateNode {
    private final @NonNull DistinctNodeContainer<PathArgument, NormalizedNode> oldData;

    RecursiveReplaceCandidateNode(final DistinctNodeContainer<PathArgument, NormalizedNode> oldData,
            final DistinctNodeContainer<PathArgument, NormalizedNode> newData) {
        super(newData);
        this.oldData = requireNonNull(oldData);
    }

    @Override
    public ModificationType modificationType() {
        return ModificationType.WRITE;
    }

    @Override
    public NormalizedNode dataBefore() {
        return oldData;
    }

    @Override
    public NormalizedNode dataAfter() {
        return data;
    }

    @Override
    public DataTreeCandidateNode modifiedChild(final PathArgument childName) {
        return DataTreeCandidateNodes.containerDelta(oldData, data, childName);
    }

    @Override
    public Collection<DataTreeCandidateNode> childNodes() {
        return DataTreeCandidateNodes.containerDelta(oldData, data);
    }
}
