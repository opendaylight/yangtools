/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public interface DataTreeSnapshotCursor extends AutoCloseable {
    void enter(PathArgument child) throws IllegalArgumentException;
    void exit() throws IllegalStateException;

    /**
     * Read a particular node from the snapshot.
     *
     * @param child Child identifier
     * @return Optional result encapsulating the presence and value of the node
     */
    Optional<NormalizedNode<?, ?>> readNode(PathArgument child);

    @Override
    void close();
}
