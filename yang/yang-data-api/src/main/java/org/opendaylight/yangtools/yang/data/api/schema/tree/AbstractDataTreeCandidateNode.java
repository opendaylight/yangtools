/*
 * Copyright (c) 2015, 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

abstract class AbstractDataTreeCandidateNode implements DataTreeCandidateNode {
    private final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?,?>> data;

    AbstractDataTreeCandidateNode(final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> data) {
        this.data = requireNonNull(data);
    }

    @Override
    public final PathArgument getIdentifier() {
        return data.getIdentifier();
    }

    final Optional<NormalizedNode<?, ?>> dataOptional() {
        return Optional.of(data);
    }

    final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> getData() {
        return data;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{data = " + this.data + "}";
    }
}
