/*
 * Copyright (c) 2015, 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

/**
 * Utility implementation of {@link DataTreeCandidateNode} which acts as if
 * the {@link NormalizedNode} passed to it at creation time were freshly written.
 */
final class NormalizedNodeDataTreeCandidateNode implements DataTreeCandidateNode {
    /**
     * Convenience function for functional transformation of {@link NormalizedNode} into
     * a {@link DataTreeCandidateNode}.
     */
    private static final Function<NormalizedNode<?, ?>, DataTreeCandidateNode> FACTORY_FUNCTION =
            input -> input == null ? null : new NormalizedNodeDataTreeCandidateNode(input);
    private final NormalizedNode<?, ?> data;

    /**
     * Create a new instance backed by supplied data.
     *
     * @param data Backing {@link NormalizedNode} data.
     */
    NormalizedNodeDataTreeCandidateNode(@Nonnull final NormalizedNode<?, ?> data) {
        this.data = Preconditions.checkNotNull(data);
    }

    @Override
    public PathArgument getIdentifier() {
        return data.getIdentifier();
    }

    @Override
    public Collection<DataTreeCandidateNode> getChildNodes() {
        if (data instanceof NormalizedNodeContainer) {
            return ((NormalizedNodeContainer<?, ?, ?>) data).getValue().stream().map(FACTORY_FUNCTION).collect(
                    Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public DataTreeCandidateNode getModifiedChild(final PathArgument identifier) {
        if (data instanceof NormalizedNodeContainer) {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            final Optional<? extends NormalizedNode<?, ?>> child = ((NormalizedNodeContainer)data).getChild(identifier);
            return FACTORY_FUNCTION.apply(child.orNull());
        } else {
            return null;
        }
    }

    @Override
    public ModificationType getModificationType() {
        return ModificationType.WRITE;
    }

    @Override
    public Optional<NormalizedNode<?, ?>> getDataAfter() {
        return Optional.of(data);
    }

    @Override
    public Optional<NormalizedNode<?, ?>> getDataBefore() {
        return Optional.absent();
    }
}
