/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2024 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;

/**
 * A {@link DataTreeCandidateNode} corresponding to {@link DataTreeCandidate.CandidateNode.Deleted}.
 */
abstract sealed class DeletedDataTreeCandidateNode<T extends NormalizedNode> extends AbstractDataTreeCandidateNode {
    private static final class Leaf extends DeletedDataTreeCandidateNode<NormalizedNode> {
        Leaf(final NormalizedNode dataBefore) {
            super(dataBefore);
        }

        @Override
        public List<DataTreeCandidateNode> childNodes() {
            return List.of();
        }

        @Override
        public DataTreeCandidateNode modifiedChild(final PathArgument childName) {
            return null;
        }
    }

    private static final class Container extends DeletedDataTreeCandidateNode<DistinctNodeContainer<?, ?>> {
        Container(final DistinctNodeContainer<?, ?> dataBefore) {
            super(dataBefore);
        }

        @Override
        public Collection<DataTreeCandidateNode> childNodes() {
            return childNodes(dataBefore(), DeletedDataTreeCandidateNode::of);
        }

        @Override
        public DataTreeCandidateNode modifiedChild(final PathArgument childName) {
            return modifiedChild(dataBefore(), DeletedDataTreeCandidateNode::of, childName);
        }
    }

    private final @NonNull T dataBefore;

    private DeletedDataTreeCandidateNode(final T dataBefore) {
        super(ModificationType.DELETE);
        this.dataBefore = requireNonNull(dataBefore);
    }

    static @NonNull DeletedDataTreeCandidateNode<?> of(final NormalizedNode dataBefore) {
        return switch (dataBefore) {
            case DistinctNodeContainer<?, ?> container -> new Container(container);
            default -> new Leaf(dataBefore);
        };
    }

    @Override
    public final PathArgument name() {
        return dataBefore.name();
    }

    @Override
    public final @NonNull T dataBefore() {
        return dataBefore;
    }

    @Override
    public final T dataAfter() {
        return null;
    }

    @Override
    protected final ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("dataBefore", dataBefore.prettyTree());
    }
}
