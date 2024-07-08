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
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate.CandidateNode.Replaced;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;

/**
 * A {@link DataTreeCandidateNode} corresponding to {@link Replaced}.
 */
abstract sealed class ReplacedDataTreeCandidateNode<T extends NormalizedNode> extends AbstractDataTreeCandidateNode {
    private static final class Leaf extends ReplacedDataTreeCandidateNode<NormalizedNode> {
        Leaf(final NormalizedNode dataBefore, final NormalizedNode dataAfter) {
            super(dataBefore, dataAfter);
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
    private static final class Container extends ReplacedDataTreeCandidateNode<DistinctNodeContainer> {
        Container(final NormalizedNode dataBefore, final DistinctNodeContainer<?, ?> dataAfter) {
            super((DistinctNodeContainer) dataBefore, dataAfter);
        }

        @Override
        public Collection<DataTreeCandidateNode> childNodes() {
            return DataTreeCandidateNodes.containerDelta(dataBefore(), dataAfter());
        }

        @Override
        public DataTreeCandidateNode modifiedChild(final PathArgument childName) {
            return DataTreeCandidateNodes.containerDelta(dataBefore(), dataAfter(), requireNonNull(childName));
        }
    }

    private final @NonNull T dataBefore;
    private final @NonNull T dataAfter;

    private ReplacedDataTreeCandidateNode(final T dataBefore, final T dataAfter) {
        super(ModificationType.WRITE);
        this.dataBefore = requireNonNull(dataBefore);
        this.dataAfter = requireNonNull(dataAfter);
    }

    static @NonNull ReplacedDataTreeCandidateNode<?> of(final NormalizedNode dataBefore,
            final NormalizedNode dataAfter) {
        return switch (dataAfter) {
            case DistinctNodeContainer<?, ?> container -> new Container(dataBefore, container);
            default -> new Leaf(dataBefore, dataAfter);
        };
    }

    @Override
    public final PathArgument name() {
        return dataAfter.name();
    }

    @Override
    public final @NonNull T dataBefore() {
        return dataBefore;
    }

    @Override
    public final @NonNull T dataAfter() {
        return dataAfter;
    }

    @Override
    public final Replaced toModern() {
        return ImmutableCandidateNodes.replaced(dataBefore, dataAfter);
    }

    @Override
    protected final ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("dataBefore", dataBefore.prettyTree()).add("dataAfter", dataAfter.prettyTree());
    }
}
