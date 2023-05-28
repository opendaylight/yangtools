/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;

abstract class AbstractLeafCandidateNode implements DataTreeCandidateNode {
    final @NonNull NormalizedNode data;

    AbstractLeafCandidateNode(final NormalizedNode data) {
        this.data = requireNonNull(data);
    }

    @Override
    public final List<DataTreeCandidateNode> childNodes() {
        return List.of();
    }

    @Override
    public final PathArgument name() {
        return data.name();
    }

    @Override
    public final DataTreeCandidateNode modifiedChild(final PathArgument childName) {
        requireNonNull(childName);
        return null;
    }
}