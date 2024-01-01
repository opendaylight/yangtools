/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.api;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * A {@link DataTreeSnapshot} which allows creation of a {@link DataTreeSnapshotCursor}.
 */
@Beta
public interface CursorAwareDataTreeSnapshot extends DataTreeSnapshot {
    /**
     * Create a new {@link DataTreeSnapshotCursor} at specified path. May fail
     * if specified path does not exist.
     *
     * @param path Path at which the cursor is to be anchored
     * @return A new cursor, or empty if the path does not exist.
     * @throws IllegalStateException if there is another cursor currently open.
     */
    Optional<? extends DataTreeSnapshotCursor> openCursor(@NonNull YangInstanceIdentifier path);

    /**
     * Create a new {@link DataTreeSnapshotCursor} at the root of the modification.
     *
     * @return A new cursor
     * @throws IllegalStateException if there is another cursor currently open.
     */
    default DataTreeSnapshotCursor openCursor() {
        return openCursor(YangInstanceIdentifier.of()).orElseThrow();
    }
}
