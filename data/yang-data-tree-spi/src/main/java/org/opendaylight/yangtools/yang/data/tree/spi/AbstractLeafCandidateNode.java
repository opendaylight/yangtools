/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.PathNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;

abstract class AbstractLeafCandidateNode implements DataTreeCandidateNode {
    private final PathNode<?> data;

    AbstractLeafCandidateNode(final PathNode<?> data) {
        this.data = requireNonNull(data);
    }

    final @NonNull Optional<NormalizedNode> dataOptional() {
        return Optional.of(data);
    }

    @Override
    public final Collection<DataTreeCandidateNode> getChildNodes() {
        return List.of();
    }

    @Override
    public final PathArgument getIdentifier() {
        return data.pathArgument();
    }

    @Override
    public final Optional<DataTreeCandidateNode> getModifiedChild(final PathArgument identifier) {
        requireNonNull(identifier);
        return Optional.empty();
    }
}