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
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

abstract class AbstractRecursiveCandidateNode extends AbstractDataTreeCandidateNode {
    AbstractRecursiveCandidateNode(final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> data) {
        super(data);
    }

    @SuppressWarnings("unchecked")
    static DataTreeCandidateNode deleteNode(final NormalizedNode<?, ?> data) {
        if (data instanceof NormalizedNodeContainer) {
            return new RecursiveDeleteCandidateNode(
                (NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>>) data);
        }
        return new DeleteLeafCandidateNode(data);
    }

    @SuppressWarnings("unchecked")
    static DataTreeCandidateNode replaceNode(final NormalizedNode<?, ?> oldData, final NormalizedNode<?, ?> newData) {
        if (isContainer(oldData)) {
            return new RecursiveReplaceCandidateNode(
                (NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>>) oldData,
                (NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>>) newData);
        }
        return new ReplaceLeafCandidateNode(oldData, newData);
    }

    @SuppressWarnings("unchecked")
    static DataTreeCandidateNode writeNode(final NormalizedNode<?, ?> data) {
        if (data instanceof NormalizedNodeContainer) {
            return new RecursiveWriteCandidateNode(
                (NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>>) data);
        }
        return new WriteLeafCandidateNode(data);
    }

    static boolean isContainer(final NormalizedNode<?, ?> data) {
        return data instanceof NormalizedNodeContainer;
    }

    @Override
    public final DataTreeCandidateNode getModifiedChild(final PathArgument identifier) {
        final Optional<NormalizedNode<?, ?>> potential = data().getChild(identifier);
        if (potential.isPresent()) {
            return createChild(potential.get());
        }
        return null;
    }

    @Override
    public final Collection<DataTreeCandidateNode> getChildNodes() {
        return Collections2.transform(data().getValue(), this::createChild);
    }

    @SuppressWarnings("unchecked")
    private DataTreeCandidateNode createChild(final NormalizedNode<?, ?> childData) {
        if (isContainer(childData)) {
            return createContainer((NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>>) childData);
        }
        return createLeaf(childData);
    }

    abstract DataTreeCandidateNode createContainer(
            NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> childData);

    abstract DataTreeCandidateNode createLeaf(NormalizedNode<?,?> childData);
}