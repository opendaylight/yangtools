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
 * A {@link DataTreeCandidateNode} corresponding to {@link DataTreeCandidate.CandidateNode.Unmodifed}.
 */
abstract sealed class UnmodifiedDataTreeCandidateNode<T extends NormalizedNode> extends AbstractDataTreeCandidateNode {
    private static final class Leaf extends UnmodifiedDataTreeCandidateNode<NormalizedNode> {
        Leaf(final NormalizedNode dataAfter) {
            super(dataAfter);
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

    @SuppressWarnings("rawtypes")
    private static final class Container extends UnmodifiedDataTreeCandidateNode<DistinctNodeContainer> {
        Container(final DistinctNodeContainer<?, ?> dataAfter) {
            super(dataAfter);
        }

        @Override
        public Collection<DataTreeCandidateNode> childNodes() {
            return childNodes(dataAfter(), UnmodifiedDataTreeCandidateNode::of);
        }

        @Override
        public DataTreeCandidateNode modifiedChild(final PathArgument childName) {
            return modifiedChild(dataAfter(), UnmodifiedDataTreeCandidateNode::of, childName);
        }
    }

    private final @NonNull T dataAfter;

    private UnmodifiedDataTreeCandidateNode(final T dataAfter) {
        super(ModificationType.UNMODIFIED);
        this.dataAfter = requireNonNull(dataAfter);
    }

    static @NonNull UnmodifiedDataTreeCandidateNode<?> of(final NormalizedNode dataAfter) {
        return switch (dataAfter) {
            case DistinctNodeContainer<?, ?> container -> new Container(container);
            default -> new Leaf(dataAfter);
        };
    }

    @Override
    public final PathArgument name() {
        return dataAfter.name();
    }

    @Override
    public final @NonNull T dataBefore() {
        return dataAfter;
    }

    @Override
    public final @NonNull T dataAfter() {
        return dataAfter;
    }

    @Override
    protected final ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("dataAfter", dataAfter.prettyTree());
    }
}
