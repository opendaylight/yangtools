/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import com.google.common.collect.Collections2;
import java.util.Collection;
import java.util.Optional;
import org.checkerframework.checker.units.qual.C;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;

abstract class AbstractRecursiveCandidateNode
        <T extends DistinctNodeContainer<PathArgument, NormalizedNode> & NormalizedNode>
        extends AbstractDataTreeCandidateNode<T> {
    AbstractRecursiveCandidateNode(final T data) {
        super(data);
    }

    @Override
    public final Optional<DataTreeCandidateNode> getModifiedChild(final PathArgument identifier) {
        return data().findChildByArg(identifier).map(this::createChild);
    }

    @Override
    public final Collection<DataTreeCandidateNode> getChildNodes() {
        return Collections2.transform(data().body(), this::createChild);
    }

    abstract <X extends DistinctNodeContainer<PathArgument, NormalizedNode> & NormalizedNode>
        DataTreeCandidateNode createContainer(X childData);

    abstract DataTreeCandidateNode createLeaf(NormalizedNode childData);

    @SuppressWarnings("unchecked")
    private DataTreeCandidateNode createChild(final NormalizedNode childData) {
        if (childData instanceof DistinctNodeContainer) {
            return createContainer((C) childData);
        }
        return createLeaf(childData);
    }
}
