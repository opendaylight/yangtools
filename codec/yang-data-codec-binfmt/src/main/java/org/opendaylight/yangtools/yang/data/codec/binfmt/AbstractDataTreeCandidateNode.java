/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;

/**
 * Abstract base class for our internal implementation of {@link DataTreeCandidateNode},
 * which we instantiate from a serialized stream. We do not retain the before-image and
 * do not implement {@link #getModifiedChild(PathArgument)}, as that method is only
 * useful for end users. Instances based on this class should never be leaked outside of
 * this component.
 */
abstract class AbstractDataTreeCandidateNode implements DataTreeCandidateNode {
    private final ModificationType type;

    AbstractDataTreeCandidateNode(final ModificationType type) {
        this.type = requireNonNull(type);
    }

    @Override
    public final Optional<DataTreeCandidateNode> getModifiedChild(final PathArgument identifier) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public final ModificationType getModificationType() {
        return type;
    }

    @Override
    public final Optional<NormalizedNode> getDataBefore() {
        throw new UnsupportedOperationException("Before-image not available after serialization");
    }
}
