/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;

abstract class AbstractLeafCandidateNode implements DataTreeCandidateNode {
    private final NormalizedNode<?, ?> data;

    protected AbstractLeafCandidateNode(final NormalizedNode<?, ?> data) {
        this.data = Preconditions.checkNotNull(data);
    }

    protected final Optional<NormalizedNode<?, ?>> dataOptional() {
        return Optional.of(data);
    }

    @Nonnull
    @Override
    public final Collection<DataTreeCandidateNode> getChildNodes() {
        return Collections.emptyList();
    }

    @Override
    @Nonnull
    public final PathArgument getIdentifier() {
        return data.getIdentifier();
    }

    @Override
    public final DataTreeCandidateNode getModifiedChild(final PathArgument identifier) {
        return null;
    }
}