/*
 * Copyright (c) 2015, 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;

abstract class AbstractDataTreeCandidateNode
        <T extends DistinctNodeContainer<PathArgument, NormalizedNode> & NormalizedNode>
        implements DataTreeCandidateNode {
    private final @NonNull T data;

    AbstractDataTreeCandidateNode(final T data) {
        this.data = requireNonNull(data);
    }

    @Override
    public final PathArgument getIdentifier() {
        return data.getIdentifier();
    }

    final @NonNull Optional<T> dataOptional() {
        return Optional.of(data);
    }

    final @NonNull T data() {
        return data;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{data = " + data + "}";
    }
}
