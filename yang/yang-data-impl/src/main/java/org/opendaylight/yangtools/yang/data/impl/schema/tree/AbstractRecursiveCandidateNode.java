/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import java.util.Collection;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;

abstract class AbstractRecursiveCandidateNode implements DataTreeCandidateNode {

    private final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?,?>> data;

    protected AbstractRecursiveCandidateNode(final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> data) {
        this.data = data;
    }

    @SuppressWarnings("unchecked")
    static DataTreeCandidateNode deleteNode(final NormalizedNode<?, ?> data) {
        if (data instanceof NormalizedNodeContainer) {
            return new RecursiveDeleteCandidateNode((NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>>) data);
        }
        return LeafDataTreeCandidateNode.deleteNode(data);
    }

    @SuppressWarnings("unchecked")
    static DataTreeCandidateNode initialWriteNode(final NormalizedNode<?, ?> data) {
        if (data instanceof NormalizedNodeContainer) {
            return new RecursiveWriteCandidateNode((NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>>) data);
        }
        return LeafDataTreeCandidateNode.initialWriteNode(data);
    }

    @Override
    public final PathArgument getIdentifier() {
        return data.getIdentifier();
    }

    protected final Optional<NormalizedNode<?, ?>> dataOptional() {
        return Optional.<NormalizedNode<?, ?>>of(data);
    }

    @Override
    public final DataTreeCandidateNode getModifiedChild(final PathArgument identifier) {
        final Optional<NormalizedNode<?, ?>> potential = data.getChild(identifier);
        if (potential.isPresent()) {
            return createChild(potential.get());
        }
        return null;
    }


    @Override
    public final Collection<DataTreeCandidateNode> getChildNodes() {
        return Collections2.transform(data.getValue(), new Function<NormalizedNode<?,?>,DataTreeCandidateNode> () {
            @Override
            public DataTreeCandidateNode apply(final NormalizedNode<?, ?> input) {
                return createChild(input);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private final DataTreeCandidateNode createChild(final NormalizedNode<?, ?> childData) {
        if (childData instanceof NormalizedNodeContainer<?, ?, ?>) {
            return createContainer((NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>>) childData);
        }
        return createLeaf(childData);
    }

    protected abstract DataTreeCandidateNode createContainer(NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> childData);

    protected abstract DataTreeCandidateNode createLeaf(NormalizedNode<?,?> childData);
}