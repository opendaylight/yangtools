/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

abstract class AbstractModifiedContainerNode extends AbstractContainerNode {
    private final Version subtreeVersion;

    protected AbstractModifiedContainerNode(final NormalizedNode<?, ?> data, final Version version,
            final Version subtreeVersion) {
        super(data, version);
        this.subtreeVersion = Preconditions.checkNotNull(subtreeVersion);
    }

    @Override
    public final Version getSubtreeVersion() {
        return subtreeVersion;
    }
}
