/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;

/**
 * Utility implementation of {@link DataTreeCandidateNode} which acts as if
 * the {@link NormalizedNode} passed to it at creation time were freshly written.
 */
final class NormalizedNodeDataTreeCandidateNode implements DataTreeCandidateNode {
    private final NormalizedNode data;

    /**
     * Create a new instance backed by supplied data.
     *
     * @param data Backing {@link NormalizedNode} data.
     */
    NormalizedNodeDataTreeCandidateNode(final @NonNull NormalizedNode data) {
        this.data = requireNonNull(data);
    }

    @Override
    public PathArgument name() {
        return data.name();
    }

    @Override
    public ModificationType modificationType() {
        return ModificationType.WRITE;
    }

    @Override
    public NormalizedNode dataBefore() {
        return null;
    }

    @Override
    public NormalizedNode dataAfter() {
        return data;
    }

    @Override
    public Collection<DataTreeCandidateNode> childNodes() {
        if (data instanceof DistinctNodeContainer<?, ?> container) {
            return Collections2.transform(container.body(),
                input -> input == null ? null : new NormalizedNodeDataTreeCandidateNode(input));
        }
        return ImmutableList.of();
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public DataTreeCandidateNode modifiedChild(final PathArgument childName) {
        if (data instanceof DistinctNodeContainer container) {
            final var child = container.childByArg(childName);
            return child != null ? new NormalizedNodeDataTreeCandidateNode(child) : null;
        }
        return null;
    }
}
