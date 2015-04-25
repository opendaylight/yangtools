/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Class encapsulation of set of modifications to a base tree. This tree is backed
 * by a read-only snapshot and tracks modifications on top of that. The modification
 * has the ability to rebase itself to a new snapshot.
 */
public interface DataTreeModification extends DataTreeSnapshot {
    /**
     * Delete the node at specified path.
     *
     * @param path Node path
     */
    void delete(YangInstanceIdentifier path);

    /**
     * Merge the specified data with the currently-present data
     * at specified path.
     *
     * @param path Node path
     * @param data Data to be merged
     */
    void merge(YangInstanceIdentifier path, NormalizedNode<?, ?> data);

    /**
     * Replace the data at specified path with supplied data.
     *
     * @param path Node path
     * @param data New node data
     */
    void write(YangInstanceIdentifier path, NormalizedNode<?, ?> data);

    /**
     * Finish creation of a modification, making it ready for application
     * to the data tree. Any calls to this object's methods except
     * {@link #applyToCursor(DataTreeModificationCursor)} will result
     * in undefined behavior, possibly with an
     * {@link IllegalStateException} being thrown.
     */
    void ready();

    /**
     * Apply the contents of this modification to a cursor. This can be used
     * to replicate this modification onto another one. The cursor's position
     * must match the root of this modification, otherwise performing this
     * operation will result in undefined behavior.
     *
     * @param cursor cursor to which this modification
     */
    void applyToCursor(@Nonnull DataTreeModificationCursor cursor);
}
