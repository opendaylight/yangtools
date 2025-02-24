/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.api;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/**
 * Interface representing a data tree which can be modified in an MVCC fashion.
 */
public interface DataTree extends DataTreeTip, ReadOnlyDataTree {
    /**
     * Make the data tree use a new schema context. The context will be used
     * only by subsequent operations.
     *
     * @param newModelContext new EffectiveModelContext
     * @throws IllegalArgumentException if the new context is incompatible
     * @throws NullPointerException if newModelContext is null
     */
    void setEffectiveModelContext(@NonNull EffectiveModelContext newModelContext);

    /**
     * Commit a data tree candidate.
     *
     * @param candidate data tree candidate
     */
    default void commit(final @NonNull DataTreeCandidate candidate) {
        commit(candidate, null);
    }

    /**
     * Commit a data tree candidate, attaching an optional {@link VersionInfo} to the resulting modification.
     *
     * @param candidate data tree candidate
     * @param info version info.
     */
    void commit(@NonNull DataTreeCandidate candidate, @Nullable VersionInfo info);
}
