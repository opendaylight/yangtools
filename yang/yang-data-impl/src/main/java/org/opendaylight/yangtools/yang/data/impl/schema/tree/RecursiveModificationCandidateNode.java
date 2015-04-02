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
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;

abstract class RecursiveModificationCandidateNode implements DataTreeCandidateNode {

    private final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?,?>> data;

    private RecursiveModificationCandidateNode(final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> data) {
        this.data = data;
    }

    @SuppressWarnings("unchecked")
    static DataTreeCandidateNode deleteNode(final NormalizedNode<?, ?> data) {
        if(data instanceof NormalizedNodeContainer) {
            return new Delete((NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>>) data);
        }
        return LeafDataTreeCandidateNode.deleteNode(data);
    }

    @SuppressWarnings("unchecked")
    static DataTreeCandidateNode initialWriteNode(final NormalizedNode<?, ?> data) {
        if(data instanceof NormalizedNodeContainer) {
            return new InitialWrite((NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>>) data);
        }
        return LeafDataTreeCandidateNode.initialWrite(data);
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
        if(potential.isPresent()) {
            return createChild(potential.get());
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    private final  DataTreeCandidateNode createChild(final NormalizedNode<?, ?> childData) {
        if(childData instanceof NormalizedNodeContainer<?, ?, ?>) {
            return createContainer((NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>>) childData);
        }
        return createLeaf(childData);
    }

    protected abstract DataTreeCandidateNode createContainer(NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> childData);

    protected abstract DataTreeCandidateNode createLeaf(NormalizedNode<?,?> childData);

    @Override
    public Collection<DataTreeCandidateNode> getChildNodes() {
        return Collections2.transform(data.getValue(), new Function<NormalizedNode<?,?>,DataTreeCandidateNode> () {
            @Override
            public DataTreeCandidateNode apply(final NormalizedNode<?, ?> input) {
                return createChild(input);
            }
        });
    }

    private static final class InitialWrite extends RecursiveModificationCandidateNode {

        public InitialWrite(final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> data) {
            super(data);
        }

        @Override
        public ModificationType getModificationType() {
            return ModificationType.WRITE;
        }

        @Override
        protected DataTreeCandidateNode createContainer(
                final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> childData) {
            return new InitialWrite(childData);
        }

        @Override
        protected DataTreeCandidateNode createLeaf(final NormalizedNode<?, ?> childData) {
            return LeafDataTreeCandidateNode.initialWrite(childData);
        }

        @Override
        public Optional<NormalizedNode<?, ?>> getDataAfter() {
            return dataOptional();
        }

        @Override
        public Optional<NormalizedNode<?, ?>> getDataBefore() {
            return Optional.absent();
        }
    }

    private static final class Delete extends RecursiveModificationCandidateNode {

        public Delete(final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> data) {
            super(data);
        }

        @Override
        public ModificationType getModificationType() {
            return ModificationType.DELETE;
        }

        @Override
        protected DataTreeCandidateNode createContainer(
                final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> childData) {
            return new Delete(childData);
        }

        @Override
        public Optional<NormalizedNode<?, ?>> getDataAfter() {
            return Optional.absent();
        }

        @Override
        public Optional<NormalizedNode<?, ?>> getDataBefore() {
            return dataOptional();
        }

        @Override
        protected DataTreeCandidateNode createLeaf(final NormalizedNode<?, ?> childData) {
            return LeafDataTreeCandidateNode.deleteNode(childData);
        }
    }



}