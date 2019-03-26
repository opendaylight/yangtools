/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Tip of a data tree instance. It acts as a point to which modifications can be applied.
 */
@Beta
@NonNullByDefault
public interface DataTreeTip {
    /**
     * Validate whether a particular modification can be applied to the data tree.
     *
     * @param modification Data tree modification.
     * @throws DataValidationFailedException If modification data is not valid.
     * @throws NullPointerException if modification is null
     * @throws IllegalArgumentException if modification is unrecognized
     */
    void validate(DataTreeModification modification) throws DataValidationFailedException;

    /**
     * Prepare a modification for commit.
     *
     * @param modification Data tree modification.
     * @return candidate data tree
     * @throws NullPointerException if modification is null
     * @throws IllegalArgumentException if modification is unrecognized
     */
    // FIXME: 4.0.0: throw DataValidationFailedException or similar
    DataTreeCandidateTip prepare(DataTreeModification modification);

    /**
     * {@inheritDoc}
     *
     * {@link DataTreeTip} implementations must not override the default identity hashCode method.
     */
    @Override
    int hashCode();

    /**
     * {@inheritDoc}
     *
     * {@link DataTreeTip} implementations must not override the default identity hashCode method, meaning their
     * equals implementation must result in identity comparison.
     */
    @Override
    boolean equals(@Nullable Object obj);
}
