/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import com.google.common.collect.Collections2;
import java.util.Collection;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

abstract class AbstractRecursiveCandidateNode extends AbstractDataTreeCandidateNode {
    AbstractRecursiveCandidateNode(final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> data) {
        super(data);
    }

    @Override
    public final DataTreeCandidateNode getModifiedChild(final PathArgument identifier) {
        return data().getChild(identifier).map(this::createChild).orElse(null);
    }

    @Override
    public final Collection<DataTreeCandidateNode> getChildNodes() {
        return Collections2.transform(data().getValue(), this::createChild);
    }

    abstract DataTreeCandidateNode createContainer(
            NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> childData);

    abstract DataTreeCandidateNode createLeaf(NormalizedNode<?,?> childData);

    @SuppressWarnings("unchecked")
    private DataTreeCandidateNode createChild(final NormalizedNode<?, ?> childData) {
        if (childData instanceof NormalizedNodeContainer) {
            return createContainer((NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>>) childData);
        }
        return createLeaf(childData);
    }
}
