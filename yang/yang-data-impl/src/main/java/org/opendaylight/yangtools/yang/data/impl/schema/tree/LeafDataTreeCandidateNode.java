/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Optional;
import java.util.Collection;
import java.util.Collections;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;

abstract class LeafDataTreeCandidateNode implements DataTreeCandidateNode {

    private final NormalizedNode<?, ?> data;

    private LeafDataTreeCandidateNode(final NormalizedNode<?, ?> data) {
        this.data = data;
    }

    static DataTreeCandidateNode initialWriteNode(final NormalizedNode<?, ?> childData) {
        return new InitialWrite(childData);
    }


    static DataTreeCandidateNode deleteNode(final NormalizedNode<?, ?> childData) {
        return new Delete(childData);
    }

    protected final Optional<NormalizedNode<?, ?>> dataOptional() {
        return Optional.<NormalizedNode<?, ?>>of(data);
    }

    @Override
    public final Collection<DataTreeCandidateNode> getChildNodes() {
        return Collections.emptyList();
    }

    @Override
    public final PathArgument getIdentifier() {
        return data.getIdentifier();
    }

    @Override
    public final DataTreeCandidateNode getModifiedChild(final PathArgument identifier) {
        return null;
    }

    private static final class Delete extends LeafDataTreeCandidateNode implements DataTreeCandidateNode {

        protected Delete(final NormalizedNode<?, ?> data) {
            super(data);
        }

        @Override
        public ModificationType getModificationType() {
            return ModificationType.DELETE;
        }

        @Override
        public Optional<NormalizedNode<?, ?>> getDataAfter() {
            return Optional.absent();
        }

        @Override
        public Optional<NormalizedNode<?, ?>> getDataBefore() {
            return dataOptional();
        }

    }

    private static final class InitialWrite extends LeafDataTreeCandidateNode {

        protected InitialWrite(final NormalizedNode<?, ?> data) {
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
            return Optional.absent();
        }

    }

}