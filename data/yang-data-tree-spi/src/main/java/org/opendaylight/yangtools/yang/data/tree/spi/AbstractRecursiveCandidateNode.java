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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;

abstract class AbstractRecursiveCandidateNode extends AbstractDataTreeCandidateNode {
    AbstractRecursiveCandidateNode(final DistinctNodeContainer<PathArgument, NormalizedNode> data) {
        super(data);
    }

    @Override
    public final DataTreeCandidateNode modifiedChild(final PathArgument childName) {
        final var child = data.childByArg(childName);
        return child != null ? createChild(child) : null;
    }

    @Override
    public final Collection<DataTreeCandidateNode> childNodes() {
        return Collections2.transform(data.body(), this::createChild);
    }

    abstract DataTreeCandidateNode createContainer(DistinctNodeContainer<PathArgument, NormalizedNode> childData);

    abstract DataTreeCandidateNode createLeaf(NormalizedNode childData);

    @SuppressWarnings("unchecked")
    private DataTreeCandidateNode createChild(final NormalizedNode childData) {
        if (childData instanceof DistinctNodeContainer) {
            return createContainer((DistinctNodeContainer<PathArgument, NormalizedNode>) childData);
        }
        return createLeaf(childData);
    }
}
