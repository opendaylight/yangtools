/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.api;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Extension to the {@link DataTreeSnapshotCursor} which allows modifying the data tree. An instance of this interface
 * can be obtained from a {@link DataTreeModification} and modifications made through this interface are staged
 * in the parent modification.
 */
public interface DataTreeModificationCursor extends DataTreeSnapshotCursor {
    /**
     * Delete the specified child.
     *
     * @param child Child identifier
     * @throws BackendFailedException when implementation-specific errors occurs while servicing the request.
     */
    void delete(PathArgument child);

    /**
     * Merge the specified data with the currently-present data
     * at specified path.
     *
     * @param child Child identifier
     * @param data Data to be merged
     * @throws BackendFailedException when implementation-specific errors occurs while servicing the request.
     */
    void merge(PathArgument child, NormalizedNode data);

    /**
     * Replace the data at specified path with supplied data.
     *
     * @param child Child identifier
     * @param data New node data
     * @throws BackendFailedException when implementation-specific errors occurs while servicing the request.
     */
    void write(PathArgument child, NormalizedNode data);
}
