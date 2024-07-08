/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;

/**
 * Abstract base class for our internal implementation of {@link DataTreeCandidateNode}, which we instantiate from a
 * serialized stream. We do not retain the before-image and do not implement {@link #getModifiedChild(PathArgument)}, as
 * that method is only useful for end users. Instances based on this class should never be leaked outside of
 * this component.
 */
abstract class AbstractDataTreeCandidateNode
        extends org.opendaylight.yangtools.yang.data.tree.spi.AbstractDataTreeCandidateNode {
    AbstractDataTreeCandidateNode(final ModificationType type) {
        super(type);
    }

    @Override
    public final DataTreeCandidateNode modifiedChild(final PathArgument identifier) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public final NormalizedNode dataBefore() {
        throw new UnsupportedOperationException("Before-image not available after serialization");
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("internal", true);
    }
}
