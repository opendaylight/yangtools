/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * A {@link DataTreeModification} which allows creation of a {@link DataTreeModificationCursor}.
 */
@Beta
public interface CursorAwareDataTreeModification extends DataTreeModification, CursorAwareDataTreeSnapshot {
    /**
     * Create a new {@link DataTreeModificationCursor} at specified path. May fail
     * if specified path does not exist. It is a programming error to use normal
     *
     * @param path Path at which the cursor is to be anchored
     * @return A new cursor, or null if the path does not exist.
     * @throws IllegalStateException if there is another cursor currently open,
     *                               or the modification is already {@link #ready()}.
     * @deprecated Use {@link #openCursor(YangInstanceIdentifier)} instead.
     */
    @Deprecated
    @Override
    DataTreeModificationCursor createCursor(YangInstanceIdentifier path);

    /**
     * Create a new {@link DataTreeModificationCursor} at specified path. May fail
     * if specified path does not exist.
     *
     * @param path Path at which the cursor is to be anchored
     * @return A new cursor, or empty if the path does not exist.
     * @throws IllegalStateException if there is another cursor currently open,
     *                               or the modification is already {@link #ready()}.
     */
    @Override
    default Optional<? extends DataTreeModificationCursor> openCursor(final YangInstanceIdentifier path) {
        return Optional.ofNullable(createCursor(path));
    }

    /**
     * Create a new {@link DataTreeModificationCursor} at the root of the modification.
     *
     * @return A new cursor
     * @throws IllegalStateException if there is another cursor currently open.
     */
    @Override
    default DataTreeModificationCursor openCursor() {
        return openCursor(YangInstanceIdentifier.EMPTY).get();
    }
}
